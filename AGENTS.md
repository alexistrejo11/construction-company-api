# Agent Instructions

## Project Shape

- This is a single-module Gradle modular monolith. The Spring Boot entrypoint is `src/main/java/io/github/alexisTrejo11/construction/company/ConstructionCompanyApplication.java`.
- Use the checked-in `./gradlew` wrapper. The build requires Java 26 (the wrapper uses Gradle 9.4.0); Spring Boot is 3.5.16.
- Business capabilities live in `modules/<module>/` as vertical feature slices: a use case normally has its controller, command/query, and handler under `features/<use-case>/`. Put types shared by feature slices in the closest `shared/` package; root `shared/` is only for cross-module technical primitives.
- Handlers own use-case orchestration and transaction boundaries. Controllers validate and translate HTTP only; JPA entities may contain domain behavior but must not depend on repositories, HTTP, or feature types.
- `docs/architecture.md` defines the confirmed package and ownership conventions. `docs/planification.md` is planning context, not evidence of implemented behavior or API contracts.

## Commands And Verification

- Compile production code: `./gradlew compileJava`.
- Run locally: `./gradlew bootRun`; build the executable jar: `./gradlew bootJar`.
- `./gradlew test` is disabled in `build.gradle`, and there are no Java test sources. The H2 `application-test.yml` is configuration only, so a successful test task is not test coverage.
- No lint, formatter, or static-analysis Gradle task is configured.

## Runtime And Persistence

- Copy `.env.example` to the ignored `.env` for local secrets and settings. Spring imports `.env`; `bootRun` also explicitly forwards its values as system properties and environment variables.
- The default `dev` profile uses SQLite (`DB_FILE`, default `dev_construction.db`), Hibernate `ddl-auto=update`, and disables Flyway. The `prod` profile uses PostgreSQL (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`), validates Hibernate mappings, and enables migrations from `src/main/resources/db/migration/`.
- Production schema changes require a new Flyway migration; do not rely on development's Hibernate schema update.

## API And Error Conventions

- Preserve expected business outcomes through `Result<T>` in handlers. Controllers must map failed results with `AppErrorResolver.handleResult(...)` and successful results with `ResponseWrapper`; do not throw ordinary conflict, validation, business-rule, or not-found failures.
- API routes use `/v2/api/**`. Verify mappings and authorization together when adding or changing routes.

## Build Caveat

- The checked-in `Dockerfile` is not functional for this repository: it uses Gradle/JDK 17 and copies a nonexistent `docker/` directory, while the Gradle build requires Java 26. Fix or verify it before using the image flow.
