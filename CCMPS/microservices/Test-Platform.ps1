param([switch]$DependencyDown)

$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8090'

function Assert-Status($uri, $expected, $headers) {
    $actual = 0
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri $uri -Headers $headers -TimeoutSec 15
        $actual = [int]$response.StatusCode
    } catch [System.Net.WebException] {
        if (-not $_.Exception.Response) { throw }
        $actual = [int]$_.Exception.Response.StatusCode
    }
    if ($actual -ne $expected) {
        throw "Expected HTTP $expected from $uri; received $actual."
    }
}

if ($DependencyDown) {
    $login = Invoke-RestMethod "$base/api/auth/login" -Method Post -ContentType 'application/json' `
        -Body (@{username=$env:APP_ADMIN_USERNAME;password=$env:APP_ADMIN_PASSWORD} | ConvertTo-Json)
    $headers = @{Authorization="Bearer $($login.accessToken)"}
    for ($i = 0; $i -lt 6; $i++) {
        Assert-Status "$base/api/admin/dashboard" 503 $headers
    }
    Write-Host 'Dependency outage rejected with 503; no fake dashboard counts returned.'
    return
}

$deadline = (Get-Date).AddSeconds(100)
do {
    $registry = Invoke-RestMethod 'http://localhost:8761/eureka/apps' -Headers @{Accept='application/json'}
    $names = @($registry.applications.application | ForEach-Object { $_.name })
    if ($names.Count -ge 10) { break }
    Start-Sleep -Seconds 2
} while ((Get-Date) -lt $deadline)
if ($names.Count -lt 10) { throw 'Not all services registered with Eureka.' }
Start-Sleep -Seconds 35

Assert-Status "$base/api/admin/dashboard" 401 @{}
$login = Invoke-RestMethod "$base/api/auth/login" -Method Post -ContentType 'application/json' `
    -Body (@{username=$env:APP_ADMIN_USERNAME;password=$env:APP_ADMIN_PASSWORD} | ConvertTo-Json)
if ($login.role -ne 'ADMIN') { throw 'Admin bootstrap/login failed.' }
$headers = @{Authorization="Bearer $($login.accessToken)"}
Assert-Status 'http://localhost:8093/api/admin/metrics/cards' 200 $headers
Assert-Status 'http://localhost:8092/api/admin/metrics/customers' 200 $headers
Assert-Status 'http://localhost:8098/api/admin/metrics/fraud' 200 $headers
Assert-Status 'http://localhost:8099/api/admin/dashboard' 200 $headers
$dashboard = Invoke-RestMethod "$base/api/admin/dashboard" -Headers $headers
if ($null -eq $dashboard.pendingLimitRequestCount) { throw 'Invalid dashboard contract.' }
Assert-Status "$base/api/admin/limit-requests" 200 $headers
Assert-Status "$base/api/admin/fraud-alerts" 200 $headers
Assert-Status "$base/internal/customers/00000000-0000-0000-0000-000000000001" 403 $headers

$username = 'smoke.cardholder'
$password = [Guid]::NewGuid().ToString('N')
$body = @{username=$username;password=$password;fullName='Smoke Test';
          email="$username@example.test";phone='9000000000'} | ConvertTo-Json
$registered = Invoke-RestMethod "$base/api/auth/register" -Method Post -ContentType 'application/json' -Body $body
$userLogin = Invoke-RestMethod "$base/api/auth/login" -Method Post -ContentType 'application/json' `
    -Body (@{username=$username;password=$password} | ConvertTo-Json)
$userHeaders = @{Authorization="Bearer $($userLogin.accessToken)"}
Assert-Status "$base/api/auth/me" 200 $userHeaders
Assert-Status "$base/api/customers/$($registered.customerId)" 200 $userHeaders
Assert-Status "$base/api/customers/00000000-0000-0000-0000-000000000001" 403 $userHeaders
Assert-Status "$base/api/accounts/customer/$($registered.customerId)" 200 $userHeaders
Assert-Status "$base/api/admin/dashboard" 403 $userHeaders
Assert-Status "$base/api/payments?accountId=00000000-0000-0000-0000-000000000001" 404 $headers

$accountId = '00000000-0000-0000-0000-000000000010'
$cardId = '00000000-0000-0000-0000-000000000011'
$alertId = '00000000-0000-0000-0000-000000000016'
foreach ($uri in @(
        "$base/api/accounts/$accountId",
        "$base/api/cards?accountId=$accountId",
        "$base/api/transactions?accountId=$accountId",
        "$base/api/transactions/00000000-0000-0000-0000-000000000012",
        "$base/api/statements?accountId=$accountId",
        "$base/api/statements/00000000-0000-0000-0000-000000000013/lines",
        "$base/api/rewards/ledger?accountId=$accountId",
        "$base/api/fraud/alerts?accountId=$accountId"
)) {
    Assert-Status $uri 200 $userHeaders
}
$card = Invoke-RestMethod "$base/api/cards/$cardId/activate" -Method Post -Headers $userHeaders
if ($card.status -ne 'ACTIVE') { throw 'Activation failed.' }
$card = Invoke-RestMethod "$base/api/cards/$cardId/freeze" -Method Post -Headers $userHeaders
if (-not $card.frozen) { throw 'Freeze failed.' }
$card = Invoke-RestMethod "$base/api/cards/$cardId/unfreeze" -Method Post -Headers $userHeaders
if ($card.frozen) { throw 'Unfreeze failed.' }

$paymentHeaders = @{Authorization=$userHeaders.Authorization; 'Idempotency-Key'='smoke-payment'}
$paymentBody = @{accountId=$accountId;amount=20;currency='INR'} | ConvertTo-Json
$payment = Invoke-RestMethod "$base/api/payments" -Method Post -Headers $paymentHeaders `
    -ContentType 'application/json' -Body $paymentBody
$retry = Invoke-RestMethod "$base/api/payments" -Method Post -Headers $paymentHeaders `
    -ContentType 'application/json' -Body $paymentBody
if ($payment.paymentReference -ne $retry.paymentReference -or $payment.status -ne 'PENDING') {
    throw 'Payment idempotency/state failed.'
}
Assert-Status "$base/api/payments?accountId=$accountId" 200 $userHeaders

$limit = Invoke-RestMethod "$base/api/limits/requests" -Method Post -Headers $userHeaders `
    -ContentType 'application/json' -Body (@{accountId=$accountId;requestedLimit=1500;reason='Smoke request'} | ConvertTo-Json)
$decision = Invoke-RestMethod "$base/api/admin/limit-requests/$($limit.id)/decision" `
    -Method Post -Headers $headers -ContentType 'application/json' `
    -Body (@{decision='APPROVE';reason='Smoke review'} | ConvertTo-Json)
$account = Invoke-RestMethod "$base/api/accounts/$accountId" -Headers $userHeaders
if ($decision.status -ne 'APPROVED' -or $account.creditLimit -ne 1500) {
    throw 'Atomic limit approval failed.'
}
$redemption = Invoke-RestMethod "$base/api/rewards/redemptions" -Method Post -Headers $userHeaders `
    -ContentType 'application/json' -Body (@{accountId=$accountId;points=100} | ConvertTo-Json)
$balance = Invoke-RestMethod "$base/api/rewards/balance?accountId=$accountId" -Headers $userHeaders
if ($balance.points -ne 400) { throw 'Reward redemption failed.' }

$alert = Invoke-RestMethod "$base/api/fraud/alerts/$alertId/response" -Method Post -Headers $userHeaders `
    -ContentType 'application/json' -Body '{"response":"NOT_RECOGNIZED"}'
if ($alert.status -ne 'REVIEW_REQUIRED') { throw 'Fraud response failed.' }
$review = Invoke-RestMethod "$base/api/admin/fraud-alerts/$alertId/decision" -Method Post -Headers $headers `
    -ContentType 'application/json' -Body '{"disposition":"CONFIRMED","notes":"Smoke analyst review"}'
if ($review.status -ne 'CLOSED' -or $review.analystDisposition -ne 'CONFIRMED') {
    throw 'Fraud review failed.'
}
$alert = Invoke-RestMethod "$base/api/fraud/alerts/$alertId" -Headers $userHeaders
if ($null -ne $alert.analystNotes -or $null -ne $alert.reviewedBy) {
    throw 'Admin audit information leaked into customer response.'
}
Write-Host 'Discovery, gateway, authentication, ownership and all domain workflows passed.'
