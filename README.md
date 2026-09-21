# FinAdvisor — Financial Advisor Platform

A production-ready foundation for an Indian financial advisory platform: a Next.js frontend, a Spring
Boot 3 authentication API, and PostgreSQL (Neon in production), wired together with Docker Compose for
local development and GitHub Actions for CI/CD to GitHub Pages and Render.

**Live site:** https://saiswaroopkuruva.github.io/personal-website/ (published after the first approved
deploy — see [Section 13](#13-github-actions--cicd)).

## 1. Project Overview

Phase 1 delivers the foundation of the platform:

- A premium, responsive fintech landing page
- JWT-based authentication (register, login, refresh, logout, current user)
- A protected dashboard with placeholder portfolio widgets
- Local development via Docker Compose (Next.js + Spring Boot + PostgreSQL)
- CI pipelines for both apps, deploying to GitHub Pages (frontend) and Render (backend)

## 2. Architecture

**Hosted:**

```
GitHub → GitHub Actions ─┬─→ GitHub Pages (static Next.js export)
                          └─→ Render (Spring Boot API) → Neon PostgreSQL
```

**Local development:**

```
Docker Compose
 ├─ frontend   (Next.js, :3000)
 ├─ backend    (Spring Boot, :8080)
 └─ postgres   (PostgreSQL, :5432)
```

**Request flow:** Browser → GitHub Pages (static Next.js) → HTTPS → Render (Spring Boot) → Neon PostgreSQL.

## 3. Tech Stack

**Frontend:** Next.js (App Router), TypeScript, TailwindCSS, shadcn/ui-style components, React Hook Form,
Zod, Axios, Zustand, React Query.

**Backend:** Spring Boot 3, Java 21, Spring Security, Spring Data JPA, PostgreSQL, JWT, Flyway, Lombok,
Jakarta Validation, springdoc-openapi (Swagger).

**Infrastructure:** Docker/Docker Compose (local), GitHub Actions (CI/CD), GitHub Pages (frontend
hosting), Render (backend hosting), Neon (managed PostgreSQL).

## 4. Repository Structure

```
.
├── app/
│   ├── api/                 Spring Boot 3 API
│   │   └── src/main/java/finadvisor/
│   │       ├── controller/     REST controllers (auth, profile, email, password, security, risk, notifications, admin)
│   │       ├── service/        Business logic interfaces (+ impl/ package)
│   │       ├── repository/     Spring Data JPA repositories
│   │       ├── dto/             Request/response DTOs, organized into profile/ risk/ security/ verification/
│   │       │                    notification/ admin/ sub-packages
│   │       ├── entity/         JPA entities and enums
│   │       ├── mapper/          Entity ↔ DTO mapping components
│   │       ├── validator/       Custom Jakarta Validation constraints (PAN, PIN code, password strength)
│   │       ├── risk/            Configurable risk questionnaire catalog + scoring engine
│   │       ├── notification/    Outbound email abstraction
│   │       ├── events/          Domain events (audit trail)
│   │       ├── listener/        Async event listeners (audit log persistence)
│   │       ├── mutualfund/     Phase 3 feature package - self-contained MVC slice (see below)
│   │       ├── config/         Security, CORS, JWT, upload, OpenAPI configuration (cross-cutting only)
│   │       ├── security/       JWT filter, user principal, user details service, request metadata
│   │       ├── exception/      Custom exceptions + global exception handler
│   │       └── util/           Shared constants and helpers
│   │   └── src/main/resources/db/seed/  Dev-only demo data migrations (mutual funds), NOT run in production
│   └── web/                 Next.js App Router application
│       └── src/
│           ├── app/             Routes (landing, auth pages, dashboard, profile/*, mutual-funds/*, calculators/*)
│           ├── components/      UI primitives, layout, landing, auth, dashboard, profile, mutual-funds, calculators
│           ├── hooks/           React Query hooks (auth, profile, risk, security, notifications, mutual funds, calculators)
│           ├── lib/             Axios client, API services, validation schemas
│           ├── store/           Zustand stores (auth, mutual fund comparison selection)
│           └── types/           Shared TypeScript types
├── infrastructure/
│   ├── docker-compose.yml   Local dev orchestration (web + api + postgres)
│   ├── render.yaml           Render Blueprint for the api service
│   └── docker/               api.Dockerfile, web.Dockerfile
└── .github/workflows/         ci-cd.yml (build FE+BE, deploy on push to main)
```

`finadvisor.mutualfund` is organized as its own self-contained MVC slice (feature-based, not by technical
layer) so all Phase 3 code is easy to locate in one place:

```
finadvisor/mutualfund/
├── controller/    MutualFundController, MutualFundFavoriteController, CalculatorController, AdminMutualFundSyncController
├── service/       Business logic interfaces (+ impl/)
├── repository/    Spring Data JPA repositories + MutualFundSpecifications
├── dto/           Request/response DTOs (+ calculator/ sub-package)
├── entity/        JPA entities and enums
├── mapper/        Entity ↔ DTO mapping
├── provider/       Market-data provider abstraction (+ demo/ dev provider)
├── scheduler/      Scheduled data-sync job
├── config/         Provider/calculator properties, cache config
└── exception/      Feature-specific exceptions
```

Everything outside `mutualfund/` (`controller/`, `service/`, `repository/`, `dto/`, `entity/`, `config/`,
`exception/`, etc. at the `finadvisor` root) is cross-cutting/shared code from Phase 1/2 (auth, profile,
risk, security, notifications) and common infrastructure (`SecurityConfig`, `GlobalExceptionHandler`, JWT).

## 5. Local Development Setup

Prerequisites: Docker Desktop, or Node.js 20+ and JDK 21 + Maven if running services natively.

**Fastest path — Docker Compose:**

```bash
docker compose -f infrastructure/docker-compose.yml up --build
```

This starts PostgreSQL, the backend on `http://localhost:8080`, and the frontend on `http://localhost:3000`.

## 6. Environment Variables

**Frontend** (`app/web/.env.local`, see `app/web/.env.example`):

| Variable | Description |
|---|---|
| `NEXT_PUBLIC_API_URL` | Base URL of the backend API (defaults to `http://localhost:8080`) |

GitHub Pages serves static files only, so `NEXT_PUBLIC_API_URL` cannot be read at runtime there — the CI
`deploy` job bakes it into the build from the `NEXT_PUBLIC_API_URL` repository variable (see
[Section 14](#14-github-pages-deployment)).

**Backend** (`app/api/.env`, see `app/api/.env.example`):

| Variable | Description |
|---|---|
| `DATABASE_URL` | JDBC connection string (local Postgres or Neon) |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Database credentials |
| `JWT_SECRET` | Secret used to sign access tokens — must be a long, random value |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access token lifetime in milliseconds (default `900000` = 15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token lifetime in milliseconds (default `604800000` = 7 days) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins |
| `UPLOAD_DIR` | Local directory for uploaded profile photos (default `uploads`) |
| `UPLOAD_MAX_FILE_SIZE_BYTES` | Max profile photo size in bytes (default `2097152` = 2MB) |

Never commit real values for these — `.env`, `.env.local`, `.env.*.local`, and `application-local.*` are
gitignored.

## 7. Docker Setup

- `infrastructure/docker/api.Dockerfile` — multi-stage Maven build → Temurin 21 JRE runtime image with an actuator health check.
- `infrastructure/docker/web.Dockerfile` — multi-stage Node 20 build producing a minimal Next.js production image.
- `infrastructure/docker-compose.yml` — runs all three services locally. The Postgres container is for local development
  only; deployed environments use Neon.

## 8. Frontend Setup

```bash
cd app/web
npm install
npm run dev      # http://localhost:3000
npm run lint
npm run test
npm run build
```

## 9. Backend Setup

```bash
cd app/api
cp .env.example .env   # fill in local/Neon values, then export them
mvn spring-boot:run
```

Swagger UI is available at `http://localhost:8080/swagger-ui.html` once the app is running.

## 10. Database Setup

Local development uses the `postgres` service defined in `docker-compose.yml` (database `finadvisor`,
user/password `finadvisor`). Deployed environments use a Neon PostgreSQL connection string supplied via
`DATABASE_URL`/`DATABASE_USERNAME`/`DATABASE_PASSWORD`.

## 11. Flyway Migrations

Migrations live in `app/api/src/main/resources/db/migration` and run automatically on startup:

- `V1__create_users_table.sql` — `users` table (auth + risk profile)
- `V2__create_refresh_tokens_table.sql` — `refresh_tokens` table (FK to `users`, unique token, expiry)
- `V3__user_management_and_risk_profile.sql` — Phase 2 schema (see [Section 21](#21-phase-2-user-management-security--risk-profiling)):
  - Extends `users` with `role`, profile/KYC fields, verification flags, investor preference fields,
    `notification_preferences` (JSONB), `profile_picture`, `last_login`, `status`, `created_by`/`updated_by`.
  - Adds `device_id` to `refresh_tokens` (links a session to the device it was issued to).
  - New tables: `user_addresses`, `user_devices`, `email_verification_tokens`, `password_reset_tokens`,
    `password_history`, `risk_assessment_results`, `audit_logs`.
- `V4__mutual_fund_platform.sql` — Phase 3 schema (see [Section 22](#22-phase-3-mutual-fund-platform)):
  `mutual_fund_amcs`, `mutual_funds`, `mutual_fund_nav_history`, `mutual_fund_holdings`,
  `mutual_fund_managers`, `mutual_fund_returns`, `user_mutual_fund_favorites`, `mutual_fund_data_sync`.
- `db/seed/V9001__seed_demo_mutual_fund_data.sql` — dev-only demo AMC/fund/NAV/returns/holdings/manager
  data, only applied when the `dev` Spring profile is active (never in production).

Hibernate's `ddl-auto` is set to `validate` — schema changes must go through new Flyway migrations, never
manual edits.

## 12. Running Tests

```bash
# Backend
cd app/api && mvn test

# Frontend
cd app/web && npm run test
```

## 13. GitHub Actions / CI-CD

A single workflow, `.github/workflows/ci-cd.yml`, runs on every pull request and push to `main`:

- **`backend-build`** — compiles, tests, and packages the Spring Boot app, and validates the Docker image.
- **`frontend-build`** — installs dependencies, lints, tests, and builds the Next.js app.
- **`deploy`** — runs only when both builds succeed **and** the event is a push to `main`. It targets the
  `owner` GitHub Environment, triggers the Render deploy hook, and publishes a static export of the
  frontend to GitHub Pages.

Because the `deploy` job is tied to the `owner` environment, it will not run until manually approved.
To enable this, configure the environment once in the repo: **Settings → Environments → New environment**,
name it `owner`, and under **Required reviewers** add the repository owner (or whichever
user/team should approve deploys). Every push to `main` will then build both apps automatically, but the
actual deployment pauses for that reviewer's approval before it proceeds.

## 14. GitHub Pages Deployment

The frontend is exported as a static site (`output: 'export'`, enabled only when `GITHUB_PAGES=true`) and
published to GitHub Pages by the `deploy` job.

1. In the repo, go to **Settings → Pages → Build and deployment → Source** and select **GitHub Actions**.
2. Optionally add a repository variable `NEXT_PUBLIC_API_URL` (**Settings → Secrets and variables →
   Actions → Variables**) pointing at your Render backend URL — GitHub Pages is static hosting, so this
   value is baked into the build at deploy time rather than read at runtime.
3. Approve the `owner` environment when a deploy run pauses for review (see [Section 13](#13-github-actions--cicd)).
4. Once deployed, the site is live at `https://<github-username>.github.io/personal-website/`.

## 15. Render Deployment

`infrastructure/render.yaml` defines the backend as a Docker web service. In the Render dashboard, the
Blueprint's "render.yaml path" must be set to `infrastructure/render.yaml` (Render only looks at the repo
root by default). Set the environment
variables `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `JWT_ACCESS_TOKEN_EXPIRATION`,
`JWT_REFRESH_TOKEN_EXPIRATION`, and `CORS_ALLOWED_ORIGINS` (the GitHub Pages site URL). Render's health check
uses the Spring Boot Actuator `/actuator/health` endpoint.

## 16. Neon PostgreSQL Setup

1. Create a project at [neon.tech](https://neon.tech) and copy the connection string.
2. Set `DATABASE_URL` (with `sslmode=require`), `DATABASE_USERNAME`, and `DATABASE_PASSWORD` as Render
   environment variables.
3. Flyway runs automatically against Neon on application startup — no manual schema changes.

## 17. Authentication Flow

1. `POST /api/auth/register` — creates a user, returns an access token (short-lived JWT) and a refresh
   token (opaque, stored in `refresh_tokens`).
2. `POST /api/auth/login` — validates credentials with BCrypt, returns the same token pair.
3. Requests to protected endpoints send `Authorization: Bearer <accessToken>`.
4. `POST /api/auth/refresh` — exchanges a valid, unexpired refresh token for a new token pair (refresh
   tokens are rotated — the old one is deleted).
5. `POST /api/auth/logout` — deletes the refresh token, ending the session.
6. `GET /api/auth/me` — returns the authenticated user's profile.

Login and register also record the device (IP + browser, upserted into `user_devices`) and update
`users.last_login`; login is rejected with `403` if the account is `SUSPENDED`/`DEACTIVATED`, and the JWT
filter re-checks account status on every request via `UserPrincipal.isEnabled()`. See
[Section 21](#21-phase-2-user-management-security--risk-profiling) for the full Phase 2 user management,
security, and risk profiling module built on top of this foundation.

On the frontend, tokens and user info are held in a Zustand store (persisted to `localStorage`), an Axios
interceptor attaches the access token to requests and transparently retries once via `/api/auth/refresh`
on a 401, and `/dashboard` is protected by a client-side `RequireAuth` guard that redirects to `/login`.

## 18. API Documentation / Swagger

Interactive API docs are served by springdoc-openapi:

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

## 19. Phase 1 Features

- Landing page: navigation, hero, features, how it works, investment categories, why choose us,
  testimonials, FAQ, CTA, footer — responsive with light/dark mode.
- Auth pages: `/login`, `/register`, `/forgot-password` (UI only), `/verify-email` (placeholder).
- Protected `/dashboard` with welcome card, portfolio summary, net worth, today's gain/loss, quick actions,
  recent activity, investment goals, and market snapshot (all placeholder data).
- JWT authentication API with refresh-token rotation, global exception handling, and Swagger docs.
- Docker Compose for local development; Dockerfiles for both apps.
- Separate GitHub Actions workflows for frontend and backend.

## 20. Phase 2 TODOs (superseded)

Historical note: this section originally listed Phase 1 → Phase 2 TODOs. All of them were addressed in
Phase 2 except test coverage expansion, which (along with new Phase 3 follow-ups) now lives in
[Section 23, Phase 4 TODOs](#23-phase-4-todos).

## 21. Phase 2: User Management, Security & Risk Profiling

Phase 2 adds a complete user management module on top of the Phase 1 auth foundation: profile
management, email verification, password management, account security, an investor risk assessment
engine, notification preferences, and admin support — all backed by audit logging.

### 21.1 Profile Module

`ProfileController` (`/api/profile`) exposes profile read/update, photo upload (validated local file
storage served from `/uploads/**`), investor preferences, preferred language, the quick "primary address"
fields, and a full address book (`/api/profile/addresses`, backed by the `user_addresses` table, supports
multiple HOME/WORK/OTHER addresses with a single default). All PAN, PIN code and password fields are
validated with custom Jakarta Validation constraints (`@ValidPan`, `@ValidPostalCode`, `@StrongPassword`).

### 21.2 Email Verification

`EmailVerificationController` (`/api/email`) issues single-use, 24-hour tokens (`email_verification_tokens`)
and marks `users.email_verified` once confirmed. `LoggingEmailSender` (`finadvisor.notification`) logs
outbound emails instead of dispatching them through a real provider — see [Known Limitations](#215-known-limitations).

### 21.3 Password Management & Account Security

`PasswordController` (`/api/password`) implements forgot/reset/change flows with:

- 1-hour, single-use reset tokens (`password_reset_tokens`).
- Reuse prevention against the current password and the last 5 password hashes (`password_history`).
- `@StrongPassword`: minimum 12 characters, upper/lowercase, digit, special character, common-password
  blocklist.
- Reset and forced logout of all sessions on password reset.

`SecurityController` (`/api/security`) surfaces devices (`user_devices`, upserted on every login with
IP/browser/last-seen), a login/security-event history (backed by `audit_logs`), "logout of all devices",
and per-device revocation (deletes the device's linked refresh tokens via the new `refresh_tokens.device_id`
column).

### 21.4 Risk Assessment Engine

A 15-question investor questionnaire (age, income, dependents, horizon, experience, market knowledge,
reaction to loss, emergency fund, objective, expected returns, current investments, debt obligations,
liquidity needs, risk appetite, tax-saving preference) is defined in `RiskQuestionCatalog`
(`finadvisor.risk`) — each option carries a 0–100 score. `RiskEngine` averages the selected scores into a
single 0–100 score and maps it to a 5-tier `RiskLevel`:

| Score | Risk Level |
|---|---|
| 0–25 | Conservative |
| 26–50 | Moderately Conservative |
| 51–70 | Balanced |
| 71–85 | Growth |
| 86–100 | Aggressive |

Each level maps to a recommendation: a summary, a suggested allocation across Debt / Large Cap / Mid Cap /
Small Cap / Gold / International / Cash, a suggested investment horizon, and suggested fund categories.
`RiskController` (`/api/risk`) exposes `GET /questions`, `POST /submit`, `GET /latest`, and
`GET /history` (paginated). Submitting an assessment also updates the user's coarse `riskProfile`
(Conservative/Moderate/Aggressive, used elsewhere in the platform) for backward compatibility with Phase 1.

### 21.5 Notification Preferences

`NotificationController` (`/api/notifications/preferences`) reads/replaces an 11-flag preference set
(email, SMS, push, in-app, marketing, investment alerts, goal reminders, market updates, security alerts,
weekly/monthly reports) stored as JSONB on `users.notification_preferences`.

### 21.6 Admin Support

`AdminUserController` (`/api/admin/users`, `@PreAuthorize("hasRole('ADMIN')")`) lets an administrator list
users, view a full profile, suspend/activate an account (suspension immediately revokes all refresh tokens
and blocks further login/JWT authentication), reset a user's risk profile/history, and view verification
status. A new `role` column (`USER`/`ADMIN`, default `USER`) was added to `users` to support this
authorization boundary.

### 21.7 Audit Logging

Profile updates, password changes, email verification, security actions (login, logout-all, device
revocation), and risk submissions publish a Spring `ApplicationEvent` (`AuditEvent`) that an async
`AuditEventListener` persists to `audit_logs` with the user, action, IP address, and timestamp — decoupling
business logic from audit persistence.

### 21.8 Frontend

New authenticated routes under `/profile`: `/profile` (overview with risk meter + allocation chart),
`/profile/edit`, `/profile/address`, `/profile/preferences`, `/profile/risk` (questionnaire wizard),
`/profile/security` (login history timeline), `/profile/devices`, `/profile/notifications`,
`/profile/change-password`, and `/profile/verify-email`. Reusable components: `ProfileCard`, `RiskMeter`,
`CircularProgress`, `AllocationChart`, `QuestionnaireWizard`, `ProgressBar`, `SecurityTimeline`,
`DeviceCard`, `NotificationCard`, `AddressCard`, plus shared `EmptyState`/`ErrorState`/`Skeleton` states.
The public `/verify-email` and new `/reset-password` pages now call the real verification/reset APIs
(replacing the Phase 1 placeholders).

### 21.9 Architecture Decisions

- **Layering kept consistent with Phase 1**: controllers/services/repositories stay in the existing
  `controller`/`service`/`service.impl`/`repository` packages; the new `profile`/`risk`/`notification`/
  `verification`/`security`/`preferences` groupings from the spec are expressed as **DTO sub-packages**
  (`dto.profile`, `dto.risk`, etc.) rather than duplicating the technical layers, to avoid fragmenting the
  established architecture.
- **Mapping**: manual mapper components (`finadvisor.mapper`) were used instead of adding MapStruct, to
  avoid a new annotation-processor dependency for a Phase 1 codebase that already uses simple manual
  mapping methods.
- **Device-session linkage**: `refresh_tokens.device_id` (nullable FK to `user_devices`) allows a single
  device to be revoked along with its active session(s).
- **Risk level vs. risk profile**: the fine-grained 5-tier `RiskLevel` (assessment results) is kept
  separate from the coarse 3-tier `RiskProfile` already used across Phase 1 (dashboard, register form);
  submitting an assessment keeps the latter in sync for backward compatibility.
- **File uploads**: profile photos are validated (size, MIME type) and stored on local disk under a
  configurable `app.upload.dir`, served via a Spring resource handler at `/uploads/**` — no new cloud
  storage dependency was introduced for this phase.

### 21.10 Security Considerations

- Passwords: BCrypt hashing, 12+ character minimum with complexity rules, reuse prevention, current-password
  verification on change, and full session revocation on reset.
- Forgot-password does not reveal whether an email is registered (mitigates account enumeration).
- Verification/reset tokens are single-use, time-boxed, and invalidated on use or superseded by a new request.
- Suspended/deactivated accounts are blocked at both login (`AccountNotActiveException`) and JWT
  authentication (`UserPrincipal.isEnabled()`), and have their sessions revoked immediately on suspension.
- Admin endpoints are protected by method-level `@PreAuthorize("hasRole('ADMIN')")` (`@EnableMethodSecurity`).
- Uploaded photos are restricted to PNG/JPEG/WEBP, size-capped, and stored under randomly generated
  filenames (no user-controlled path segments) to prevent path traversal and content-type abuse.
- All new endpoints are behind JWT authentication except the intentionally public token-based flows
  (`/api/email/verify`, `/api/password/forgot`, `/api/password/reset`) and static `/uploads/**` assets.

## 22. Phase 3: Mutual Fund Platform

Phase 3 adds a mutual fund discovery, analysis and calculator platform on top of the Phase 1/2
foundation. It is **discovery/educational only**: no purchases, redemptions, broker integrations, or bank
connections are implemented, and no personalized investment recommendations are made.

### 22.1 Database Schema

`V4__mutual_fund_platform.sql` adds:

- `mutual_fund_amcs` — asset management companies.
- `mutual_funds` — scheme master data (unique `scheme_code`/`isin`, category/sub-category, plan/option
  type, risk level, expense ratio, AUM, NAV, minimums, etc). Indexed on `amc_id`, `category`,
  `sub_category`, `risk_level`, `nav_date`, `scheme_name`, `status`.
- `mutual_fund_nav_history` — one row per fund per NAV date (unique `mutual_fund_id`+`nav_date`,
  composite index for efficient chart range queries).
- `mutual_fund_holdings` — portfolio holdings snapshots (security, sector, asset type, weight, market
  value, as-of date).
- `mutual_fund_managers`, `mutual_fund_returns` (unique per fund+period; `annualized` flag distinguishes
  CAGR from absolute returns), `user_mutual_fund_favorites` (unique per user+fund), and
  `mutual_fund_data_sync` (provider sync run log).

`db/seed/V9001__seed_demo_mutual_fund_data.sql` seeds 10 **entirely fictional** demo funds (AMCs, NAV
history, returns, holdings, managers) for local development only — see [22.7](#227-seed-data--demo-provider).

### 22.2 Provider Abstraction

`finadvisor.mutualfund.provider.MutualFundDataProvider` is the only interface the platform depends on for
external market data (`searchFunds`, `getFundDetails`, `getCurrentNav`, `getHistoricalNav`, `getHoldings`,
`getFundManagers`, `getFundReturns`, `syncFunds`). Provider-specific types never reach controllers —
`ProviderResponse<T>`/`ProviderFundData`/etc. are internal to `finadvisor.mutualfund.provider` and are
mapped into platform entities by `MutualFundDataSyncService`. `ProviderCallExecutor` applies exponential-backoff
retry, but only for calls that throw a `ProviderException` marked `retryable` (never for non-retryable
4xx-style failures). A provider outage never crashes the app: sync failures are caught, logged, and
recorded in `mutual_fund_data_sync` with `FAILED`/`PARTIAL_FAILURE` status.

To add a real provider (AMFI, MFApi.in, a paid vendor, etc.): implement `MutualFundDataProvider`, annotate
it `@ConditionalOnProperty(prefix = "mutualfund.provider", name = "name", havingValue = "<your-name>")`,
and set `MUTUAL_FUND_PROVIDER_NAME` accordingly — no other code changes required.

### 22.3 Data Synchronization

`MutualFundDataSyncService` upserts funds/AMCs, NAV, holdings, returns and managers from the active
provider, recording a `mutual_fund_data_sync` row per run. `MutualFundSyncScheduler` runs it on a cron
schedule (`mutualfund.sync.cron`, disabled by default via Spring's `-` "never fire" marker) and only when
`mutualfund.provider.enabled=true`. `AdminMutualFundSyncController` (`/api/admin/mutual-funds/sync*`,
`ROLE_ADMIN`) allows triggering syncs manually.

### 22.4 Caching

`CacheConfig` enables Spring's cache abstraction with an in-memory `ConcurrentMapCacheManager` (filter
metadata, fund details, popular funds, current NAV caches). Because it's the standard `@Cacheable`
abstraction, swapping in Redis later is a one-bean change (`RedisCacheManager`) with no service code
changes — Redis has not been introduced into this project yet, so `REDIS_URL` in `.env.example` is a
placeholder for that future work.

### 22.5 API Endpoints

Public (no auth required): fund discovery, details, history, returns, holdings, managers, filters,
comparison, and all three calculators.

| Method | Path | Description |
|---|---|---|
| GET | `/api/mutual-funds` | Paginated search/filter/sort |
| GET | `/api/mutual-funds/filters` | Filter metadata (cached) |
| GET | `/api/mutual-funds/compare?schemes=A,B` | Compare 2–4 schemes |
| GET | `/api/mutual-funds/{schemeCode}` | Full fund details |
| GET | `/api/mutual-funds/{schemeCode}/nav-history` | NAV history (`from`/`to`/`interval`) |
| GET | `/api/mutual-funds/{schemeCode}/returns` | Trailing returns (absolute vs. annualized) |
| GET | `/api/mutual-funds/{schemeCode}/holdings` | Paginated holdings (`sector`/`assetType`/`asOfDate`) |
| GET | `/api/mutual-funds/{schemeCode}/managers` | Fund manager info |
| POST/DELETE | `/api/mutual-funds/{schemeCode}/favorite` | Add/remove favorite (auth required) |
| GET | `/api/mutual-funds/favorites` | List favorites (auth required) |
| POST | `/api/calculators/sip` \| `/lumpsum` \| `/swp` | Investment calculators |
| POST | `/api/admin/mutual-funds/sync*` | Trigger provider sync (`ROLE_ADMIN`) |

### 22.6 Calculators

All calculations use `BigDecimal` exclusively (never `double`/`float`). SIP uses the standard annuity-due
monthly-compounding formula; lumpsum uses annual compounding; SWP is simulated month-by-month (growth then
capped withdrawal) so early corpus exhaustion is detected rather than assumed away. All three validate
inputs against configurable upper bounds (`mutualfund.calculator.*`) and every response is labeled an
estimate with a disclaimer — never a guarantee.

### 22.7 Seed Data & Demo Provider

Both the SQL seed migration and `DemoMutualFundDataProvider` (`finadvisor.mutualfund.provider.demo`,
active by default via `mutualfund.provider.name=demo`) use the same 10 clearly fictional schemes/AMCs so a
manually-triggered sync updates the same seeded rows. NAV history/returns are generated by a deterministic
drift+oscillation formula — never real market data — and are only loaded when the `dev` Spring profile is
active (`application-dev.properties` adds `classpath:db/seed` to `spring.flyway.locations`). Run locally
with `SPRING_PROFILES_ACTIVE=dev`.

### 22.8 Frontend

New routes: `/mutual-funds` (explorer: search, filters sidebar/mobile drawer, sort, pagination),
`/mutual-funds/[schemeCode]` (details, NAV chart, returns, holdings, managers, calculator links),
`/mutual-funds/compare` (up to 4 funds), `/mutual-funds/favorites` (auth-gated), and
`/calculators/sip|lumpsum|swp`. No charting library was added (consistent with the existing custom
`allocation-chart.tsx` pattern) — `NavChart` and `AmountBarChart` are lightweight custom SVG/CSS
components. All monetary values use Indian locale formatting (`formatInr`/`formatCrores`/`formatNav` in
`lib/utils.ts`, e.g. `₹10,00,000`). A reusable `Disclaimer` component appears on every page that shows
returns, calculator output, or comparisons.

### 22.9 Architecture Decisions / Known Limitations

- Sync orchestration (`syncFunds`/`syncNav`/etc.) lives on `MutualFundDataSyncService`, not on
  `MutualFundDataProvider` itself, so providers stay pure data sources with no DB/repository knowledge
  (SRP) — a deliberate deviation from listing those methods directly on the provider interface.
- No Testcontainers/H2 integration tests were added in this phase (the project has none yet for Phase
  1/2 either) — backend tests are Mockito-based unit tests only. Repository/controller integration tests
  and E2E tests are recommended for Phase 4.
- Filter/search state in the explorer is component-local (not URL query params), so filtered views aren't
  currently shareable via URL — a good Phase 4 improvement.
- "Popular/trending funds" (Part 31) was not exposed as a separate endpoint in this phase; the search API's
  default sort and filters cover fund discovery. A configurable popularity ranking is recommended for Phase 4.

## 23. Phase 4 TODOs

- Real mutual fund market-data provider integration (behind the existing `MutualFundDataProvider`
  abstraction).
- Actual purchase/redemption flows, broker/RTA integration, and bank account linking (explicitly out of
  scope through Phase 3).
- Redis-backed caching (swap `CacheConfig`'s `CacheManager` bean).
- URL-shareable mutual fund explorer filters; Testcontainers-based integration tests; E2E test suite.
- Real portfolio, net worth, and market data integrations (replacing dashboard placeholders).
- Role-based authorization beyond `USER`/`ADMIN` (e.g. advisor/compliance roles).
- Refresh token hashing at rest.


### 21.11 Known Limitations

- Outbound email (verification, password reset) is logged, not actually delivered — wire `EmailSender` to
  a real provider (SES, SendGrid, etc.) before production use.
- The risk questionnaire is defined in code (`RiskQuestionCatalog`) rather than the database; making it
  admin-editable is deferred to a future phase.
- Device fingerprinting is limited to IP address + coarse browser family parsed from `User-Agent`.

### 21.12 Remaining Work for Phase 3 (Mutual Fund Platform)

- Fund catalog, NAV data ingestion, and fund detail/comparison pages.
- Portfolio construction, order placement, and SIP management against the risk-based allocation
  recommendations produced in Phase 2.
- Real net worth, holdings, and transaction history (replacing dashboard placeholders).
- KYC document upload/verification workflow (the `kyc_status` field is modeled but not yet actionable).
- Payment/bank account linking for investments and withdrawals.

