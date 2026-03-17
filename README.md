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

The default development database is in-memory H2. The H2 console is available at `/h2-console`.

## Persistent PostgreSQL

To run with persistent PostgreSQL locally:

1. Start PostgreSQL with Docker Compose:

```bash
cd /home/anni/dev/14-Java/cycle-tracker
docker compose up -d
```

2. Start the backend with the `postgres` Spring profile:

```bash
cd /home/anni/dev/14-Java/cycle-tracker/app
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

The default PostgreSQL connection values are:

- database: `cycletracker`
- username: `cycletracker`
- password: `cycletracker`

You can override them with environment variables listed in [app/.env.postgres.example](/home/anni/dev/14-Java/cycle-tracker/app/.env.postgres.example#L1).

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
