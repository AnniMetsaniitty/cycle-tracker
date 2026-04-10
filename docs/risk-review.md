# Risk Review

Last reviewed: 2026-04-10

## 1. Unauthenticated user-id-based data access

- Severity: High
- Status: Partially Mitigated
- Explanation: Cycle and medication endpoints trust caller-supplied `userId`, allowing clients to read or mutate another user's cycle data if they know or guess the id.
- Impacted files: `CycleController.java`, `MedicationController.java`, `UserController.java`, `UserService.java`, `CycleService.java`, `MedicationService.java`, desktop API client/session code
- Date updated: 2026-04-07
- Done: Added opaque bearer tokens issued on registration/login, required bearer authorization on cycle and medication endpoints, and rejected token/requested-user mismatches with 403 responses. Updated the desktop API client to send the token and added integration coverage for missing-token and cross-user access denial.
- Remaining gaps: Token durability and expiry were later partially addressed under risk 2. Existing endpoint paths still include `userId`, but the server now requires it to match the bearer token.

## 2. Login has no durable session or token

- Severity: High
- Status: Partially Mitigated
- Explanation: Login now returns a persisted expiring bearer token, but sessions still lack logout invalidation, refresh handling, and full framework-level security integration.
- Impacted files: `UserService.java`, `UserController.java`, `UserResponse.java`, desktop API client/session code
- Date updated: 2026-04-08
- Done: Replaced in-memory token tracking with persisted hashed bearer tokens, added configurable token expiry, and reject expired tokens with 401 responses. Added focused integration coverage for persisted token issuance and expired-token rejection.
- Remaining gaps: There is still no logout endpoint/token revocation flow, refresh-token flow, scheduled expired-token cleanup, or full Spring Security filter chain. Existing clients must still keep the access token in process memory.

## 3. Unsafe automatic schema evolution

- Severity: High
- Status: Partially Mitigated
- Explanation: Hibernate automatic schema mutation has been disabled; startup now validates the schema against an explicit SQL definition instead of updating it implicitly.
- Impacted files: `application.properties`, `schema.sql`, database migration/config files
- Date updated: 2026-04-08
- Done: Replaced `spring.jpa.hibernate.ddl-auto=update` with `validate`, enabled SQL initialization, and added an explicit idempotent `schema.sql` for the current tables and constraints.
- Remaining gaps: Schema changes are now explicit and reviewable, but there is still no versioned migration history or rollback workflow. Future schema changes should update the explicit SQL or move to a lightweight migration tool.

## 4. Concurrent cycle start consistency

- Severity: Medium-High
- Status: Partially Mitigated
- Explanation: Cycle-changing operations now serialize per user through a pessimistic database lock, reducing the race that could leave conflicting active-cycle state.
- Impacted files: `CycleService.java`, `CycleRepository.java`, `UserRepository.java`, tests
- Date updated: 2026-04-10
- Done: Added a pessimistic write lock on the user row for cycle start/end operations and added an integration test that exercises concurrent cycle starts and verifies only one active cycle remains.
- Remaining gaps: The invariant is enforced through service-level locking, not a database constraint on the `cycles` table. Direct writes outside this code path could still violate it.

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
