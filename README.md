# Hello World — Spring Boot + React

A minimal full-stack "Hello World" demonstrating this architecture:

`GitHub` (source + Actions CI/CD) → `Render` (Spring Boot API, Docker) + `GitHub Pages` (React frontend) → `Neon` (Postgres)

```
.
├── backend/     Spring Boot 3 (Java 17) REST API, deployed to Render
├── frontend/    React + Vite app, deployed to GitHub Pages
├── render.yaml  Render Blueprint for the backend service
└── .github/workflows/ci-cd.yml  Build/test backend+frontend, deploy to Pages/Render
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
3. **GitHub Pages**: in the repo, go to **Settings → Pages → Source** and
   select **GitHub Actions**. Then go to **Settings → Secrets and variables →
   Actions → Variables** and add a repository variable `VITE_API_URL` set to
   the Render service URL. Pushing to `main` runs
   [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml), which builds
   the app with that URL baked in and publishes it to
   `https://<your-username>.github.io/personal-website/`.
4. **GitHub Actions**: the single workflow in
   [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml) builds/tests
   the backend and frontend on every push/PR, then (on `main` only) deploys
   the frontend to Pages. Render auto-deploys on push via its own GitHub
   integration; optionally set a `RENDER_DEPLOY_HOOK_URL` repo secret to also
   trigger Render from this workflow.
