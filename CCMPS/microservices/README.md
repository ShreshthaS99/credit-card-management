# CCMPS microservices

This independent Maven reactor leaves the original application in `..\src` and
its `..\pom.xml` unchanged. The gateway preserves the existing `/api/...` paths.
Do not run the monolith against the new service databases.

## Service ownership

| Module | Port | Responsibility / owned tables |
|---|---:|---|
| discovery-server | 8761 | Eureka registry |
| api-gateway | 8090 | Spring Cloud Gateway MVC, discovery-based routing, JWT/CORS |
| auth-service | 8091 | Login, registration, `/api/auth/me`, admin bootstrap; `app_users` |
| customer-service | 8092 | Profiles; `customers` |
| card-service | 8093 | Accounts, cards, limits and admin limit decisions; `card_accounts`, `credit_cards`, `limit_requests` |
| transaction-service | 8094 | Transaction queries; `card_transactions` |
| payment-service | 8095 | Idempotent payment intents; `payments` |
| statement-service | 8096 | Statement queries; `statements`, `statement_lines` |
| rewards-service | 8097 | Reward ledger and redemption; `reward_entries` |
| fraud-service | 8098 | Customer responses and admin dispositions; `fraud_alerts` |
| admin-service | 8099 | Dashboard aggregation via Feign; no database |
| service-common | - | Security, HTTP contracts, error handling, Feign bearer relay; **no JPA entities/repositories** |

Accounts, cards and limits are one consistency boundary: approving a limit
request and updating the credit limit remain a single locked database transaction.
Domain services do not read another service's tables.

## Prerequisites and build

- JDK 17+ and Maven.
- Spring Boot 4.1.1 and Spring Cloud 2025.1.2, managed by their BOMs.
- About 3 GB of available memory for all eleven application processes.
- H2 file databases are the local default, so MySQL is not required for a demo.

From the original project directory:

```powershell
mvn -f .\microservices\pom.xml clean package
```

Set `JAVA_HOME` if Maven/Java is not already configured.

## Run on Windows

All services must share the same private `JWT_SECRET`. Do not commit it.
Generate one for the local session and enter an initial admin password:

```powershell
$bytes = New-Object byte[] 48
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:APP_ADMIN_USERNAME = "platform.admin"
$secure = Read-Host "Initial admin password (at least 12 characters)" -AsSecureString
$env:APP_ADMIN_PASSWORD = (New-Object System.Net.NetworkCredential("", $secure)).Password
.\microservices\Start-Platform.ps1
```

The script checks ports, starts only its own processes, waits for health, and
stops those processes when interrupted. Logs are in `microservices\.run`.
It does not stop or change an existing application. Eureka binds to loopback
by default; it is not intended to be exposed publicly without infrastructure
access controls.

After the admin is created, stop the platform and remove both `APP_ADMIN_*`
variables before starting it normally. Accounts persist. The bootstrap does not
reset passwords or promote an existing cardholder.

Sign in at `POST http://localhost:8090/api/auth/login`; use the returned
`accessToken` as a bearer token. Point the React API base URL to
`http://localhost:8090`. Configure `FRONTEND_ORIGIN` if it differs from
`http://localhost:5173`. Allow up to a minute for discovery caches on first startup.
Only the gateway applies browser CORS headers; downstream services leave CORS
disabled to avoid duplicate headers on proxied responses. Call the gateway from
the frontend rather than individual service ports.
Public login and registration ignore bearer headers so an expired or stale
frontend token cannot prevent signing in again. All protected endpoints still
validate bearer tokens; login still verifies the supplied username/password.

To run a single service from IntelliJ, import this reactor and run its
`*Application` class, with the module as the working directory. Supply
`JWT_SECRET` to each service except discovery. Start discovery first, then the
domain services, then the gateway. Local data is under each module's `data`
directory when launched by the script.

### Local demo login without manual registration

For a synthetic `aarav` cardholder, enable the `local-demo` Spring profile on
**auth-service only** and supply `APP_DEMO_PASSWORD` (at least 12 characters,
at most 72 UTF-8 bytes). In IntelliJ, set **Active profiles** to `local-demo`
and add the password to that run configuration's environment variables along
with the shared `JWT_SECRET`. Do not put passwords in source files or logs.

Start discovery and all domain services first, wait for their discovery
registrations, then start auth-service and the gateway. The auth-service startup
runner automatically creates the login, customer profile and sample records
through the existing registration/provisioning flow. No registration form or
manual database insert is needed. Sign in through the UI with username `aarav`
and the password you supplied in `APP_DEMO_PASSWORD`.

The bootstrap runs only with this explicit profile and with
`APP_DEMO_ENABLED` not set to `false`. It never resets an existing account's
password, enables a disabled account, or changes its role/customer mapping.
An incompatible existing `aarav` account causes an explicit startup error;
use its original credentials without this profile rather than deleting data.
Dependency failures also fail startup explicitly; restart auth-service after
the dependencies are available. Repeated successful startup preserves data.
Keep this profile disabled outside local development. The UI's demo-account
hint alone does not mean an account has been created.

## Feign, authentication and circuit breakers

- Gateway routes use `lb://service-name`; Feign resolves names through Eureka
  and Spring Cloud LoadBalancer, not hardcoded service ports.
- Payments and account-scoped reads call card-service to check ownership.
  The admin dashboard calls customer, card and fraud metrics endpoints.
- Authentication service alone owns credentials. Registration provisions a
  customer over an authenticated Feign call, then saves the login.
  Provisioning uses a stable customer UUID and is idempotent for identical
  details. If the remote call succeeds but login persistence fails, retrying
  the same registration converges; an abandoned attempt can leave an orphan
  customer and needs an operational cleanup policy. There is no distributed
  transaction or claim of atomic cross-service registration.
- Each service independently verifies HMAC-SHA256 JWT signatures, expiration,
  issuer and audience. JWTs expire after 15 minutes by default. Local validation
  means disabling a user affects new logins immediately but existing tokens
  remain valid until expiry; there is no revocation service yet.
- User bearer tokens propagate on Feign calls. Auth uses a signed SYSTEM token
  for customer provisioning; the gateway blocks `/internal/**` entirely.
- Cardholders are checked against the JWT customer ID and authoritative
  account ownership. Admin routes require ADMIN even when called directly.
- Feign uses 1.5-second connect and 3-second read timeouts. Resilience4j opens
  after at least 5 calls at a 50% failure rate, with a 10-second open interval.
  Dependency 4xx responses do not count as availability failures. There are no
  automatic mutation retries, fake balances, empty-success fallbacks or
  client-declared payment success. Outages/open circuits return HTTP 503.
- Shared HMAC keys simplify this local migration but allow any holder to sign
  tokens. For production, replace with an identity provider/asymmetric signing
  and public-key/JWKS verification, use TLS/service identities, restrict service
  ports and secure Eureka. This is not a production-compliance implementation.

## Databases and existing data

Each persistence service uses a separate H2 file by default. For MySQL, configure
each process independently with `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`, using
different schemas and least-privilege users. Never point all services at the
monolith schema. `DB_URL` selects the JDBC driver automatically.

The existing MySQL data and demo loader are **not automatically migrated or
imported**. This demo platform now provisions the original sample account/card,
two transactions, statement/line, 500 reward points and open fraud alert for each
new cardholder. Successful cardholder login also completes provisioning for
existing users such as `aarav`. Admin login does not seed data.

Each owner stores a unique customer provisioning receipt in the same transaction
as its sample records. Repeated logins do not reset balances, activated cards,
redeemed rewards or reviewed alerts. Partial provisioning is retried on the next
registration/login; a dependent-service failure is returned explicitly rather
than issuing a success response with incomplete data. All five sample-data
services must be available for these cardholder registration/login requests.
Set `APP_DEMO_ENABLED=false` on auth-service to disable this demo behavior.
Internal seed endpoints require a SYSTEM token and are blocked by the gateway.
These are synthetic demo records, not a financial opening-balance workflow or
copies of the old user's actual history.

Actual historical domain data still needs an explicit import. Do not mix
automatic sample provisioning with real migrated customer data: disable it
before importing. During cutover, stop writes, back up the original
database, export each owner's tables, retain all UUIDs and copy records to the
corresponding service database. Cross-service links are UUID references, not
cross-schema foreign keys. Validate row counts and financial totals before
switching the UI. Do not copy passwords into documentation or logs.

`ddl-auto=update` is kept for local development. Use reviewed schema migrations,
backups and reconciliation for a real deployment.

## Verification

`mvn -f .\microservices\pom.xml test` covers JWT authorization/ownership and
circuit-breaker semantics. To run the full isolated startup/HTTP smoke check
(requires the admin environment variables above):

```powershell
.\microservices\Start-Platform.ps1 -SmokeTest
```

Smoke mode uses in-memory service databases and stops its child processes on exit.
Its SQL fixtures load only when explicitly selected by the smoke launcher.
It disables automatic demo provisioning to keep its fixed workflow fixtures isolated.
It checks discovery, gateway routing, login, registration across Feign, admin
queues, negative authorization, ownership, activation/freeze/unfreeze, transaction
and statement reads, idempotent payments, limit approval, reward redemption and
fraud response/review. It then
stops only its own card-service process and verifies repeated dashboard requests
fail explicitly with 503. It does not modify existing H2 files or MySQL data.

Processor integration, billing generation, notifications and automatic data
migration were not present in the monolith and are not implemented by splitting
it. Payment intents remain PENDING until an authoritative provider workflow is
implemented.
