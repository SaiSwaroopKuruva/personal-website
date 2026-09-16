# NiveshPath — Financial Advisor Platform

A production-ready foundation for an Indian financial advisory platform: a Next.js frontend, a Spring
Boot 3 authentication API, and PostgreSQL (Neon in production), wired together with Docker Compose for
local development and GitHub Actions for CI/CD to Vercel and Render.

## 1. Project Overview

Phase 1 delivers the foundation of the platform:

- A premium, responsive fintech landing page
- JWT-based authentication (register, login, refresh, logout, current user)
- A protected dashboard with placeholder portfolio widgets
- Local development via Docker Compose (Next.js + Spring Boot + PostgreSQL)
- CI pipelines for both apps, deploying to Vercel (frontend) and Render (backend)

## 2. Architecture

**Hosted:**

```
GitHub → GitHub Actions ─┬─→ Vercel (Next.js frontend)
                          └─→ Render (Spring Boot API) → Neon PostgreSQL
```

**Local development:**

```
Docker Compose
 ├─ frontend   (Next.js, :3000)
 ├─ backend    (Spring Boot, :8080)
 └─ postgres   (PostgreSQL, :5432)
```

**Request flow:** Browser → Vercel (Next.js) → HTTPS → Render (Spring Boot) → Neon PostgreSQL.

## 3. Tech Stack

**Frontend:** Next.js (App Router), TypeScript, TailwindCSS, shadcn/ui-style components, React Hook Form,
Zod, Axios, Zustand, React Query.

**Backend:** Spring Boot 3, Java 21, Spring Security, Spring Data JPA, PostgreSQL, JWT, Flyway, Lombok,
Jakarta Validation, springdoc-openapi (Swagger).

**Infrastructure:** Docker/Docker Compose (local), GitHub Actions (CI/CD), Vercel (frontend hosting),
Render (backend hosting), Neon (managed PostgreSQL).

## 4. Repository Structure

```
.
├── backend/                Spring Boot 3 API
│   └── src/main/java/com/example/helloworld/
│       ├── controller/     REST controllers
│       ├── service/        Business logic (+ impl/ package)
│       ├── repository/     Spring Data JPA repositories
│       ├── dto/            Request/response DTOs
│       ├── entity/         JPA entities
│       ├── config/         Security, CORS, JWT, OpenAPI configuration
│       ├── security/       JWT filter, user principal, user details service
│       ├── exception/      Custom exceptions + global exception handler
│       └── util/           Shared constants
├── frontend/                Next.js App Router application
│   └── src/
│       ├── app/             Routes (landing, auth pages, dashboard)
│       ├── components/      UI primitives, layout, landing, auth, dashboard
│       ├── hooks/           React Query hooks for auth
│       ├── lib/             Axios client, API services, validation schemas
│       ├── store/           Zustand auth store
│       └── types/           Shared TypeScript types
├── docker-compose.yml        Local dev orchestration (frontend + backend + postgres)
├── render.yaml                Render Blueprint for the backend
└── .github/workflows/         frontend.yml and backend.yml CI/CD pipelines
```

## 5. Local Development Setup

Prerequisites: Docker Desktop, or Node.js 20+ and JDK 21 + Maven if running services natively.

**Fastest path — Docker Compose:**

```bash
docker compose up --build
```

This starts PostgreSQL, the backend on `http://localhost:8080`, and the frontend on `http://localhost:3000`.

## 6. Environment Variables

**Frontend** (`frontend/.env.local`, see `frontend/.env.example`):

| Variable | Description |
|---|---|
| `NEXT_PUBLIC_API_URL` | Base URL of the backend API (defaults to `http://localhost:8080`) |

**Backend** (`backend/.env`, see `backend/.env.example`):

| Variable | Description |
|---|---|
| `DATABASE_URL` | JDBC connection string (local Postgres or Neon) |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Database credentials |
| `JWT_SECRET` | Secret used to sign access tokens — must be a long, random value |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access token lifetime in milliseconds (default `900000` = 15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token lifetime in milliseconds (default `604800000` = 7 days) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins |

Never commit real values for these — `.env`, `.env.local`, `.env.*.local`, and `application-local.*` are
gitignored.

## 7. Docker Setup

- `backend/Dockerfile` — multi-stage Maven build → Temurin 21 JRE runtime image with an actuator health check.
- `frontend/Dockerfile` — multi-stage Node 20 build producing a minimal Next.js production image.
- `docker-compose.yml` — runs all three services locally. The Postgres container is for local development
  only; deployed environments use Neon.

## 8. Frontend Setup

```bash
cd frontend
npm install
npm run dev      # http://localhost:3000
npm run lint
npm run test
npm run build
```

## 9. Backend Setup

```bash
cd backend
cp .env.example .env   # fill in local/Neon values, then export them
mvn spring-boot:run
```

Swagger UI is available at `http://localhost:8080/swagger-ui.html` once the app is running.

## 10. Database Setup

Local development uses the `postgres` service defined in `docker-compose.yml` (database `finadvisor`,
user/password `finadvisor`). Deployed environments use a Neon PostgreSQL connection string supplied via
`DATABASE_URL`/`DATABASE_USERNAME`/`DATABASE_PASSWORD`.

## 11. Flyway Migrations

Migrations live in `backend/src/main/resources/db/migration` and run automatically on startup:

- `V1__create_users_table.sql` — `users` table (auth + risk profile)
- `V2__create_refresh_tokens_table.sql` — `refresh_tokens` table (FK to `users`, unique token, expiry)

Hibernate's `ddl-auto` is set to `validate` — schema changes must go through new Flyway migrations, never
manual edits.

## 12. Running Tests

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm run test
```

## 13. GitHub Actions / CI-CD

- `.github/workflows/backend.yml` — builds, tests, and packages the Spring Boot app, validates the
  Docker image, and (on `main`) optionally triggers a Render deploy hook.
- `.github/workflows/frontend.yml` — installs dependencies, lints, tests, and builds the Next.js app, then
  (on `main`) deploys to Vercel using the Vercel CLI.

Both workflows run on pull requests and pushes to `main`, scoped to their respective directories.

## 14. Vercel Deployment

1. Import this repository into Vercel, set the root directory to `frontend`.
2. Configure the `NEXT_PUBLIC_API_URL` environment variable to point to your Render backend URL.
3. Add `VERCEL_TOKEN` (and, if not using `vercel link` locally, `VERCEL_ORG_ID`/`VERCEL_PROJECT_ID`) as
   GitHub repository secrets so `frontend.yml` can deploy on push to `main`.

## 15. Render Deployment

`render.yaml` defines the backend as a Docker web service. In the Render dashboard, set the environment
variables `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `JWT_ACCESS_TOKEN_EXPIRATION`,
`JWT_REFRESH_TOKEN_EXPIRATION`, and `CORS_ALLOWED_ORIGINS` (the Vercel frontend URL). Render's health check
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

## 20. Phase 2 TODOs

- Real portfolio, net worth, and market data integrations (replacing dashboard placeholders).
- Password reset delivery (email infrastructure) for `/forgot-password`.
- Email verification delivery and confirmation flow for `/verify-email`.
- Role-based authorization beyond a single `ROLE_USER`.
- Refresh token hashing at rest and device/session management.
- Expanded automated test coverage (integration tests with Testcontainers, E2E tests).

