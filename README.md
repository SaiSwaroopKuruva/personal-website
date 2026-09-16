# Hello World — Spring Boot + React

A minimal full-stack "Hello World" demonstrating this architecture:

`GitHub` (source + Actions CI) → `Render` (Spring Boot API, Docker) + `Vercel` (React frontend) → `Neon` (Postgres)

```
.
├── backend/     Spring Boot 3 (Java 17) REST API, deployed to Render
├── frontend/    React + Vite app, deployed to Vercel
├── render.yaml  Render Blueprint for the backend service
└── .github/workflows/  CI for backend (Maven) and frontend (npm)
```

The API exposes `GET /api/hello`, which increments a visit counter row in
Neon Postgres and returns `{ "message": "...", "visits": N }`. The frontend
fetches this endpoint and displays the result.

## Local development

**Backend** (requires JDK 17+ and Maven, and a Neon/Postgres connection):

```bash
cd backend
cp .env.example .env   # fill in Neon credentials, then export them
mvn spring-boot:run
```

**Frontend**:

```bash
cd frontend
cp .env.example .env   # optional, defaults to http://localhost:8080
npm install
npm run dev
```

## Deployment setup

1. **Neon**: create a project at neon.tech, copy the connection string
   (host, database, user, password).
2. **Render**: create a new Blueprint from this repo (it will pick up
   `render.yaml`), or manually create a Web Service pointing at
   `backend/Dockerfile`. Set `SPRING_DATASOURCE_URL`,
   `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` from the Neon
   connection details.
3. **Vercel**: import this repo, set the project root to `frontend/`
   (framework auto-detected as Vite), and set env var `VITE_API_URL` to the
   Render service URL.
4. **GitHub Actions**: workflows in `.github/workflows/` build/test the
   backend and frontend on every push/PR. Render and Vercel auto-deploy on
   push via their own GitHub integrations; optionally set a
   `RENDER_DEPLOY_HOOK_URL` repo secret to also trigger Render from Actions.
