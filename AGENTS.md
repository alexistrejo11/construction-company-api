# Agent Instructions

## Project Shape

- This is a single-module Gradle project named `construction-company-api`.
- The application entrypoint is `src/main/java/io/github/alexisTrejo11/construction/company/ConstructionCompanyApplication.java`.
- The backend is Spring Boot 3.5.16 and the Gradle toolchain is Java 26. Use the checked-in `./gradlew` wrapper rather than a system Gradle installation.
- Business code is organized under `modules/<bounded-area>/`. Most newer areas use feature slices such as `features/create`, with a controller, command/query, and handler; shared domain, DTO, mapper, and persistence types live under that module's `shared` package.
- Cross-cutting configuration is under `config/`; reusable result, response, exception, authentication, and persistence types are under `shared/`.

## Commands

- Compile production code: `./gradlew compileJava`.
- Run the application: `./gradlew bootRun`.
- Build the executable jar: `./gradlew bootJar`.
- Run the test task: `./gradlew test`.
- There are currently no Java test sources, and `build.gradle` disables the `test` task (`test { enabled = false }`); do not treat a successful `./gradlew test` as test coverage.
- No lint, formatter, or static-analysis task is configured in `build.gradle`.

## Runtime Configuration

- Copy `.env.example` to `.env` for local values; `.env` is ignored and must not be committed.
- `bootRun` loads `.env` itself and passes its key/value pairs as system properties and environment variables.
- The default Spring profile is `dev`. It uses SQLite at `DB_FILE` (default `dev_construction.db`), Hibernate `ddl-auto=update`, and disables Flyway.
- The `prod` profile expects PostgreSQL variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`), validates the schema, and enables Flyway migrations from `src/main/resources/db/migration/`.
- The test configuration is H2 in-memory with `create-drop`, but it is not currently exercised because tests are disabled and no Java tests are present.

## Implementation Constraints

- Preserve the existing `Result<T>` flow for expected business outcomes: handlers return `Result`, and controllers resolve failures through `AppErrorResolver` rather than throwing for ordinary conflicts, validation failures, or not-found cases.
- Keep transactional behavior in handlers/services where it already exists; do not move persistence logic into controllers without a concrete reason.
- Check the actual controller mappings before adding endpoints. Current feature controllers use `/v2/api/...`, while `SecurityConfig` still contains `/v1/api/...` matcher rules; route and authorization changes must account for this mismatch.
- Treat `docs/planification.md` as planning material, not proof that a planned module or endpoint already exists. Confirm behavior in the Java sources and configuration first.
- Database schema changes for production belong in a new Flyway migration under `src/main/resources/db/migration/`; do not rely on `ddl-auto=update` for production.

## Known Build Caveats

- `Dockerfile` uses Gradle 8.5/JDK 17 and copies a `docker` directory, but the repository has no tracked `docker/` directory and the Gradle build requires Java 26. Verify or fix this before treating the Docker image flow as functional.
