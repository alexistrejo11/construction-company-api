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
- Run `./gradlew test` for verification. Every implemented endpoint requires integration coverage under `docs/conventions/testing.md`; a successful task with no test sources is not endpoint coverage.
- No lint, formatter, or static-analysis Gradle task is configured.

## Runtime And Persistence

- Copy `.env.example` to the ignored `.env` for local secrets and settings. Spring imports `.env`; `bootRun` also explicitly forwards its values as system properties and environment variables.
- The default `dev` profile uses SQLite (`DB_FILE`, default `dev_construction.db`), Hibernate `ddl-auto=update`, and disables Flyway. The `prod` profile uses PostgreSQL (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`), validates Hibernate mappings, and enables migrations from `src/main/resources/db/migration/`.
- Production schema changes require a new Flyway migration; do not rely on development's Hibernate schema update.

## API And Error Conventions

- Preserve expected business outcomes through `Result<T>` in handlers. Controllers must map failed results with `AppErrorResolver.handleResult(...)` and successful results with `ResponseWrapper`; do not throw ordinary conflict, validation, business-rule, or not-found failures.
- API routes use `/v2/api/**`. Verify mappings and authorization together when adding or changing routes.

## Code And Feature Conventions

- Treat `docs/architecture.md`, `docs/api/endpoints.md`, and `docs/conventions/` as the source of truth. Do not invent routes, merge resource boundaries, or keep endpoints that are not in the documented contract.
- Keep authentication/session endpoints in `modules/auth/`. Keep invitation, account-profile, and administrative user endpoints in `modules/user/`. A module must not become a catch-all for another capability's HTTP API.
- One use case owns one feature package. Place its controller, command or query, handler, response, and feature-local mapper together under `modules/<module>/features/<use-case>/`.
- Controllers only bind/validate HTTP input, call one handler, and translate `Result<T>` through the response boundary. Put orchestration, transactions, repository calls, mail dispatch, token handling, and status rules in handlers or focused shared capabilities.
- Prefer records for commands, queries, request DTOs, and response DTOs. Do not put repositories, mail clients, or business orchestration in records.
- Write readable Java: one field, statement, annotation, parameter, and record component per logical line; use normal indentation; group imports; avoid wildcard imports; use explicit names instead of compressed `var` chains; and use blank lines to separate logical blocks.
- Import referenced project classes instead of writing fully qualified names in declarations or expressions. Use a fully qualified name only when a same-named type is already imported and cannot be resolved without a collision.
- Use `var` only when the initializer is a static factory or builder call whose declaring type makes the inferred type obvious, for example `var token = InvitationToken.of(rawToken);`. Do not use `var` for constructors, repository calls, chained expressions, or values whose type is not immediately visible.
- Do not compress methods, declarations, or multiple operations onto one line. Code should be easy to scan and edit without reformatting.
- Keep helpers focused and named after the rule or technical concern they implement. Do not introduce generic module-wide service containers.

## Build Caveat

- The checked-in `Dockerfile` is not functional for this repository: it uses Gradle/JDK 17 and copies a nonexistent `docker/` directory, while the Gradle build requires Java 26. Fix or verify it before using the image flow.
