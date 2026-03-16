# Project State

## Status

As of March 12, 2026, the repository includes a working Spring Boot backend and a refactored JavaFX desktop client layer.

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
- Added a JavaFX desktop client that consumes the backend REST API
- Added login, registration, dashboard, medication status, and history views in the desktop app
- Refactored the JavaFX client into a clearer app-controller plus dedicated auth/dashboard views
- Added next-medication display, history insights, and selected-cycle detail panel to the dashboard
- Updated `README.md` and technical documentation to reflect the implemented backend

## Current Architecture

- Backend framework: Spring Boot
- Desktop client: JavaFX
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
- `app/src/main/resources/ui/cycle-tracker.css`
- `app/src/test/java/com/annimetsaniitty/cycletracker/`
- `app/src/main/java/com/annimetsaniitty/cycletracker/ui/`
- `app/src/main/java/com/annimetsaniitty/cycletracker/ui/view/`
- `app/src/main/java/com/annimetsaniitty/cycletracker/client/`

## Verified

- `mvn test` runs successfully
- `mvn spring-boot:run` runs successfully
- `mvn javafx:run` starts the desktop client successfully
- Changes committed and pushed to `origin/main`

## Latest Commits

- `864bcbd` Add backend test coverage
- `f746704` Build Spring Boot cycle tracker backend

## Known Gaps

- No PostgreSQL profile/configuration has been added yet
- No real session/auth token flow yet; login currently validates credentials and returns user data
- Desktop client does not yet include advanced navigation, edit flows, or background sync states
- No deployment packaging beyond standard Spring Boot setup

## Next Recommended Step

Extend the JavaFX client layer:

- add richer validation and user feedback
- add statistics and trend views
- add more dedicated history/dashboard subviews or move to FXML if preferred
- add API base URL configuration in the UI settings
- add PostgreSQL profile/configuration to match the documentation more closely
- improve desktop packaging and distribution

## Local Commands

```bash
cd app
mvn test
mvn spring-boot:run
mvn javafx:run
```

## Notes

- The repo documentation still mentions a broader long-term architecture. Treat the current backend implementation as the active baseline.
- For future work, keep this file updated after each major milestone so progress survives across sessions.
