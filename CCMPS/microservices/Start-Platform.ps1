param(
    [switch]$SmokeTest
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
if (-not $env:JAVA_HOME) {
    throw 'Set JAVA_HOME to a JDK 17 or later before running this script.'
}
$java = Join-Path $env:JAVA_HOME 'bin\java.exe'
if (-not (Test-Path -LiteralPath $java)) {
    throw 'JAVA_HOME does not point to a JDK containing bin\java.exe.'
}
if (-not $env:JWT_SECRET -or [Text.Encoding]::UTF8.GetByteCount($env:JWT_SECRET) -lt 32) {
    throw 'Set JWT_SECRET to a private random value of at least 32 UTF-8 bytes.'
}
if ($SmokeTest -and (-not $env:APP_ADMIN_USERNAME -or -not $env:APP_ADMIN_PASSWORD)) {
    throw 'Smoke testing requires APP_ADMIN_USERNAME and APP_ADMIN_PASSWORD for the isolated admin account.'
}

$services = [ordered]@{
    'discovery-server' = 8761
    'customer-service' = 8092
    'card-service' = 8093
    'auth-service' = 8091
    'transaction-service' = 8094
    'payment-service' = 8095
    'statement-service' = 8096
    'rewards-service' = 8097
    'fraud-service' = 8098
    'admin-service' = 8099
    'api-gateway' = 8090
}
$processes = @()
$logs = Join-Path $root '.run'
New-Item -ItemType Directory -Force -Path $logs | Out-Null

function Wait-Health($process, $port, $name) {
    $deadline = (Get-Date).AddSeconds(120)
    do {
        if ($process.HasExited) {
            throw "$name exited. See $logs\$name.log and $logs\$name.err.log."
        }
        try {
            $health = Invoke-RestMethod "http://localhost:$port/actuator/health" -TimeoutSec 3
            if ($health.status -eq 'UP') { return }
        } catch [System.Net.WebException] {
            # Startup is expected to refuse connections until the web server is ready.
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)
    throw "$name did not become healthy within 120 seconds."
}

try {
    foreach ($entry in $services.GetEnumerator()) {
        $jar = Join-Path $root "$($entry.Key)\target\$($entry.Key)-1.0.0-SNAPSHOT.jar"
        if (-not (Test-Path -LiteralPath $jar)) {
            throw "Build first: mvn -f microservices\pom.xml clean package"
        }
        $listener = Get-NetTCPConnection -LocalPort $entry.Value -State Listen -ErrorAction SilentlyContinue
        if ($listener) { throw "Port $($entry.Value) is already in use; no existing process was stopped." }
    }
    foreach ($entry in $services.GetEnumerator()) {
        $name = $entry.Key
        $jar = Join-Path $root "$name\target\$name-1.0.0-SNAPSHOT.jar"
        $arguments = @('-Xms32m', '-Xmx160m', '-jar', "`"$jar`"")
        if ($SmokeTest -and $name -ne 'discovery-server') {
            $arguments += '--spring.datasource.url=jdbc:h2:mem:smoke;DB_CLOSE_DELAY=-1'
            if ($name -eq 'auth-service') {
                $arguments += '--app.demo.enabled=false'
            }
            $fixture = Join-Path $root "$name\src\main\resources\smoke-data.sql"
            if (Test-Path -LiteralPath $fixture) {
                $arguments += '--spring.jpa.defer-datasource-initialization=true'
                $arguments += '--spring.sql.init.mode=always'
                $arguments += '--spring.sql.init.data-locations=classpath:smoke-data.sql'
            }
        }
        $process = Start-Process -FilePath $java -ArgumentList $arguments -NoNewWindow `
                -WorkingDirectory (Join-Path $root $name) `
                -RedirectStandardOutput (Join-Path $logs "$name.log") `
                -RedirectStandardError (Join-Path $logs "$name.err.log") -PassThru
        $processes += $process
        Wait-Health $process $entry.Value $name
        Write-Host "$name ready on $($entry.Value)"
    }
    if ($SmokeTest) {
        & (Join-Path $root 'Test-Platform.ps1')
        # Stop only the card-service process launched by this invocation.
        $cardIndex = @($services.Keys).IndexOf('card-service')
        $card = $processes[$cardIndex]
        Stop-Process -Id $card.Id -Force
        & (Join-Path $root 'Test-Platform.ps1') -DependencyDown
    } else {
        Write-Host 'Gateway: http://localhost:8090 | Eureka: http://localhost:8761'
        Write-Host 'Press Ctrl+C to stop the processes launched by this script.'
        while ($true) {
            foreach ($process in $processes) {
                if ($process.HasExited) { throw "Service process $($process.Id) exited unexpectedly." }
            }
            Start-Sleep -Seconds 2
        }
    }
} finally {
    foreach ($process in $processes) {
        if (-not $process.HasExited) {
            Stop-Process -Id $process.Id -Force -ErrorAction Continue
        }
    }
}
