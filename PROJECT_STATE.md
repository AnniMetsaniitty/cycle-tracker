# Project State

## Status

As of March 11, 2026, the repository has moved from a prototype to a working Spring Boot backend and the changes are pushed to GitHub.

## Completed

- Replaced the in-memory demo app with a Spring Boot application
- Added layered backend architecture: controllers, services, repositories, DTOs, exception handling
- Implemented JPA domain models for `User`, `Cycle`, and `Medication`
- Added REST endpoints for:
  - user registration
  - user login
  - cycle start
  - cycle end
  - current active cycle
  - cycle history
  - medication status
- Added H2 configuration for local development
- Added unit and integration test scaffolding
- Fixed cycle day calculation to use total elapsed days across month boundaries
- Updated `README.md` and technical documentation to reflect the implemented backend

## Current Architecture

- Backend framework: Spring Boot
- Persistence: Spring Data JPA
- Development database: H2
- Password hashing: BCrypt
- Build tool: Maven
- Language level: Java 17 in Maven config

## Important Files

- `app/pom.xml`
- `app/src/main/java/com/annimetsaniitty/cycletracker/CycleTrackerApplication.java`
- `app/src/main/java/com/annimetsaniitty/cycletracker/controller/`
- `app/src/main/java/com/annimetsaniitty/cycletracker/service/`
- `app/src/main/java/com/annimetsaniitty/cycletracker/repository/`
- `app/src/main/java/com/annimetsaniitty/cycletracker/model/`
- `app/src/main/resources/application.properties`
- `app/src/test/java/com/annimetsaniitty/cycletracker/`

## Verified

- `mvn test` runs successfully
- `mvn spring-boot:run` runs successfully
- Changes committed and pushed to `origin/main`

## Latest Commits

- `864bcbd` Add backend test coverage
- `f746704` Build Spring Boot cycle tracker backend

## Known Gaps

- JavaFX client layer is not implemented yet
- No PostgreSQL profile/configuration has been added yet
- No real session/auth token flow yet; login currently validates credentials and returns user data
- No desktop UI screens yet for login, dashboard, history, or medication tracking
- No deployment packaging beyond standard Spring Boot setup

## Next Recommended Step

Build the JavaFX client layer described in the documentation:

- JavaFX application bootstrap
- login/register views
- dashboard view
- cycle actions wired to REST API
- medication status display
- cycle history view

## Local Commands

```bash
cd app
mvn test
mvn spring-boot:run
```

## Notes

- The repo documentation still mentions a broader long-term architecture. Treat the current backend implementation as the active baseline.
- For future work, keep this file updated after each major milestone so progress survives across sessions.
