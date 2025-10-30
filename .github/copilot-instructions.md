## Quick context

- Repo layout (important):
  - `ingestion-java/` — Spring Boot (Java 17) service that ingests transactions. Key files:
    - `src/main/java/.../TransactionIngestorApplication.java` (app entry)
    - `controller/TransactionController.java` — POST /transactions/ingest expects a JSON `Transaccion`.
    - `service/TransactionService.java` — ingestAndSave(...) simulates streaming and persists with JPA.
    - `repository/UsuarioRepository.java` (extends `JpaRepository<Usuario, String>`).
    - `src/main/resources/application.properties` — DB and server.port config (MySQL, default port 8000).
  - `dashboard-web/` — React + Vite frontend (MVP dashboard). Important files:
    - `src/App.jsx` — client logic: resolves backend via `VITE_ALERTS_BASE_URL` or localhost fallback.
    - `db.json`, `public/alerts.json` — mock data for the UI.
  - `decision-agent/`, `infra/` — infra and agent notes (README present); inspect before changing infra code.

## Architecture summary (the "why")

- Purpose: small MVP for ingesting transactions, persisting with MySQL, and showing alerts in a dashboard.
- Data flow: incoming JSON -> `TransactionController` (/transactions/ingest) -> `TransactionService` (validation/logging) -> JPA `TransactionRepository` -> MySQL.
- Frontend reads alerts from an API (env-configurable) or bundled fallback JSON (`public/alerts.json`). The frontend expects data shaped like an array or an object with `items|alerts`.

## How to run locally (concrete commands)

Backend (Windows PowerShell):

1) From repo root (Windows):

   cd ingestion-java
   .\mvnw.cmd spring-boot:run

Alternative build + run jar:

   cd ingestion-java
   .\mvnw.cmd package
   java -jar target\transaction-ingestor-0.0.1-SNAPSHOT.jar

Run tests:

   cd ingestion-java
   .\mvnw.cmd test

Notes: Java 17 is required (see `pom.xml`). The project uses the Maven wrapper (`mvnw` / `mvnw.cmd`).

Frontend (dashboard-web):

   cd dashboard-web
   npm install
   npm run dev

Mock backend for the frontend: the README suggests `json-server --watch db.json --port 8000`, but the client code falls back to port 8001 when running on localhost. Either port can be used — update `src/App.jsx` or the README to keep them consistent.

Quick mock example (from repo root):

   npx json-server --watch dashboard-web/db.json --port 8001

Set environment variable for deployed API:

   VITE_ALERTS_BASE_URL=https://api.example.com  (used by frontend build/runtime)

## Project-specific patterns & conventions

- Code and comments are written in Spanish: follow Spanish naming in new files for clarity (e.g., `Transaccion`, `Usuario`, `Alerta`).
- Layering: controller -> service -> repository is followed strictly. When adding features, put transport/validation in controller (use `@Valid`), business logic in service, persistence in repositories.
- JPA entities use `String` IDs in some repositories (see `UsuarioRepository`); double-check ID types when creating repositories.
- Configuration: DB and server port live in `ingestion-java/src/main/resources/application.properties`. By default it points to MySQL at `jdbc:mysql://localhost:3306/db_transacciones` and `server.port=8000`.

## Integration points & external dependencies

- Backend: uses MySQL (mysql-connector-j). The application.properties contains default creds (`root`/`admin`) — do not hardcode credentials in commits; prefer environment overrides when deploying.
- Frontend: depends on `openai` and `dotenv` in package.json (unused in core dashboard features but present). The app prefers `VITE_ALERTS_BASE_URL` for runtime API.
- Mocking: `dashboard-web/db.json` and `public/alerts.json` are intended as sample data for development.

## Files to inspect for common tasks (quick links)

- ingest endpoint example: `ingestion-java/src/main/java/com/transacciones/transaction_ingestor/controller/TransactionController.java`
- ingestion logic & JPA save: `ingestion-java/src/main/java/com/transacciones/transaction_ingestor/service/TransactionService.java`
- JPA repository example: `ingestion-java/src/main/java/com/transacciones/transaction_ingestor/repository/UsuarioRepository.java`
- frontend fallback/endpoint logic: `dashboard-web/src/App.jsx` and `dashboard-web/db.json`
- DB config: `ingestion-java/src/main/resources/application.properties`

## Prompts & guardrails for AI agents working on this repo

- Prefer small, local changes that preserve Spanish naming and existing layers. Example: "Add a new field `canal` to `Transaccion` entity, persist it, and show it in the dashboard table" should update model -> repository (if needed) -> service -> controller -> frontend mapping.
- When editing backend ports or endpoints, update `application.properties` and `src/App.jsx` (or document env usage) to keep frontend/backend consistent.
- For database-related changes, prefer `spring.jpa.hibernate.ddl-auto=update` (current default) for quick dev iteration, but note migrations are not present — add a migration workflow if this becomes production-sensitive.

## Small gotchas discovered

- Port mismatch: README in `dashboard-web` suggests `json-server` on port 8000 while `src/App.jsx` defaults to `8001` for localhost. Resolve one canonical port.
- `dashboard-web` falls back to `/alerts.json` (bundled) when no backend available — useful for offline dev or preview builds.

If anything here is unclear or you'd like the file to emphasize different developer workflows (Docker, CI, or secrets handling), tell me what to add and I'll iterate.
