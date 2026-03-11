# Medication & Cycle Tracker

A Spring Boot backend for tracking menstrual cycles and medication schedules.

## Current Status

The repository is no longer only an in-memory prototype. It now includes:

- Spring Boot REST API
- Layered architecture with controllers, services, repositories, and JPA entities
- H2-backed persistence for local development
- User registration and login
- Cycle start, end, current-cycle, and history endpoints
- Medication window status endpoint
- Unit and integration test scaffolding

The JavaFX desktop UI described in the technical documentation is still pending.

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
