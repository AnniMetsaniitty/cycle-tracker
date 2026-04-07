# Risk Review

Last reviewed: 2026-04-07

## 1. Unauthenticated user-id-based data access

- Severity: High
- Status: In Progress
- Explanation: Cycle and medication endpoints trust caller-supplied `userId`, allowing clients to read or mutate another user's cycle data if they know or guess the id.
- Impacted files: `CycleController.java`, `MedicationController.java`, `UserController.java`, `UserService.java`, `CycleService.java`, `MedicationService.java`, desktop API client/session code

## 2. Login has no durable session or token

- Severity: High
- Status: Open
- Explanation: Login validates credentials but returns only user data, so later requests cannot be tied to an authenticated caller.
- Impacted files: `UserService.java`, `UserController.java`, `UserResponse.java`, desktop API client/session code

## 3. Unsafe automatic schema evolution

- Severity: High
- Status: Open
- Explanation: `spring.jpa.hibernate.ddl-auto=update` mutates schemas implicitly without reviewed migrations or rollback history.
- Impacted files: `application.properties`, database migration/config files

## 4. Concurrent cycle start consistency

- Severity: Medium-High
- Status: Open
- Explanation: Starting a cycle ends the active cycle and creates a new one without locking or a database constraint that guarantees one active cycle per user.
- Impacted files: `CycleService.java`, `CycleRepository.java`, `Cycle.java`, database schema/migrations

## 5. Local database defaults can leak into shared use

- Severity: Medium
- Status: Open
- Explanation: Postgres defaults use `cycletracker/cycletracker` and Docker publishes port `5432` to the host.
- Impacted files: `application-postgres.properties`, `docker-compose.yml`, README/config docs

## 6. H2 console enabled in the default development profile

- Severity: Medium
- Status: Open
- Explanation: The default profile is H2 and the H2 console is enabled, which is convenient locally but risky if exposed beyond local development.
- Impacted files: `application.properties`, `application-h2.properties`

## 7. Minimal registration validation

- Severity: Medium
- Status: Open
- Explanation: Password validation only requires non-blank input, and username/email normalization is not enforced before uniqueness checks.
- Impacted files: `RegisterRequest.java`, `UserService.java`, tests

## 8. Narrow test coverage for risky paths

- Severity: Medium-Low
- Status: Open
- Explanation: Tests cover happy paths and cycle-day calculation but not cross-user access, failed auth behavior, duplicate races, Postgres runtime behavior, or concurrent cycle starts.
- Impacted files: `app/src/test/java/**`
