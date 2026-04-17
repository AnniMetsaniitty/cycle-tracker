# Medication & Cycle Tracker

A Spring Boot backend for tracking menstrual cycles and medication schedules.

## Current Status

The repository is no longer only an in-memory prototype. It now includes:

- Spring Boot REST API
- Layered architecture with controllers, services, repositories, and JPA entities
- H2-backed persistence for local development
- PostgreSQL profile for persistent storage
- User registration and login
- Cycle start, end, current-cycle, and history endpoints
- Medication window status endpoint
- Unit and integration test scaffolding
- Initial JavaFX desktop client that talks to the REST API

The desktop layer now uses a more structured UI split with dedicated auth and dashboard views. It currently covers login, registration, dashboard metrics, next medication date, medication status, history insights, and selected-cycle detail views.

## API Endpoints

- `POST /user/register`
- `POST /user/login`
- `POST /cycle/start`
- `POST /cycle/end/{userId}`
- `GET /cycle/current/{userId}`
- `GET /cycle/history/{userId}`
- `GET /medication/status/{userId}`

## Run

With Maven installed:

```bash
cd app
mvn spring-boot:run
```

The default development database is in-memory H2. The H2 console path is `/h2-console`, but it is disabled by default and must be enabled intentionally for local-only use.

To enable it for a local session:

```bash
cd app
CYCLE_TRACKER_H2_CONSOLE_ENABLED=true mvn spring-boot:run
```

Then open `/h2-console` on the local app instance.

## Persistent PostgreSQL

To run with persistent PostgreSQL locally:

1. Set explicit local-only Postgres credentials before starting Docker or the app.

You can export them in your shell:

```bash
export CYCLE_TRACKER_DB_NAME=cycletracker
export CYCLE_TRACKER_DB_USERNAME=your-local-db-user
export CYCLE_TRACKER_DB_PASSWORD='replace-with-a-local-password'
```

Or copy values from [app/.env.postgres.example](/home/anni/dev/14-Java/cycle-tracker/app/.env.postgres.example#L1) into your own local env file and load that into your shell.

2. Start PostgreSQL with Docker Compose:

```bash
cd /home/anni/dev/14-Java/cycle-tracker
docker compose up -d
```

The Docker mapping binds PostgreSQL to `127.0.0.1` only so it is not exposed on all host interfaces by default.

3. Start the backend with the `postgres` Spring profile:

```bash
cd /home/anni/dev/14-Java/cycle-tracker/app
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

The `postgres` Spring profile now requires explicit `CYCLE_TRACKER_DB_USERNAME` and `CYCLE_TRACKER_DB_PASSWORD` values instead of falling back to shared defaults. The database name still defaults to `cycletracker` unless you override `CYCLE_TRACKER_DB_URL` or `CYCLE_TRACKER_DB_NAME`.

In a second terminal, start the JavaFX client:

```bash
cd app
mvn javafx:run
```

If your backend is running on a different base URL:

```bash
cd app
mvn javafx:run -Dcycletracker.api.base-url=http://localhost:8080
```
