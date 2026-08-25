# Endpoint Integration Testing

## Scope

Every implemented API endpoint requires an integration test before its implementation phase is complete. Tests start the Spring application context, exercise the HTTP endpoint through `MockMvc`, and use the `test` profile with the in-memory H2 database.

Tests are endpoint and workflow tests, not isolated unit tests. They cover the controller, validation, handler, persistence mappings, transaction behavior, response envelope, and database state together.

## Required Coverage

Each endpoint must cover:

- Its successful path, including the expected HTTP status, response envelope, and persisted effect when it mutates state.
- Request validation failures when the endpoint accepts input.
- Relevant expected failures, such as not found, conflict, invalid state transition, or forbidden access.
- A `204 No Content` assertion with an empty response body when the endpoint returns 204.

Tests for a workflow create prerequisites through their HTTP endpoints. For example, a project status-transition test first creates a project through `POST /v2/api/projects`, then invokes the status endpoint and verifies the resulting lifecycle state. Tests must not create the workflow's main resource by writing directly to repositories unless the endpoint under test cannot create that prerequisite.

## Authentication And External Boundaries

Endpoint tests do not repeat the authentication or invitation workflow. They use Spring Security test support to provide a valid authenticated principal or session stub appropriate to the endpoint. Authentication, session, CSRF, and invitation flows receive their own integration tests in Phase 3.

External side effects are mocked only at their infrastructure boundary. SMTP, file storage, and other network clients must be replaced with Spring test mocks. Business handlers, repositories, domain entities, and HTTP controllers remain real application components.

## Test Structure

- Use `@SpringBootTest` and `@AutoConfigureMockMvc` for endpoint integration tests.
- Use `@ActiveProfiles("test")`; the H2 database is recreated for the test context using `create-drop`.
- Use descriptive endpoint/workflow test names that state the behavior being verified.
- Assert the documented API fields, including `message`, `data` or `error`, `timestamp`, `traceId`, and `X-Trace-Id`.
- Do not assert incidental implementation details, generated IDs beyond their presence, or exact timestamps.

## Completion Rule

An endpoint is not complete until its required integration tests pass through `./gradlew test`. A phase may defer tests only while the test task or supporting infrastructure is unavailable; that deferral must be recorded explicitly in the implementation plan.
