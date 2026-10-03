# Construction Company API — Features

## Core Patterns

### Vertical Feature Slices

Each use case is a self-contained package with its controller, command or query record, and handler. Handlers own orchestration and the transaction boundary; controllers only bind, validate, and translate the result. Code is promoted to a module's `shared/` package only once a second slice needs it.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/tree/main/src/main/java/io/github/alexisTrejo11/construction/company/modules/expense/features

### Result-Based Error Handling

Handlers return a typed `Result<T>` for expected outcomes (validation, not found, conflict, forbidden, business rule) instead of throwing exceptions. A single `AppErrorResolver` maps each outcome category to an HTTP status and a consistent error envelope, so handlers and domain code never deal with HTTP.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/src/main/java/io/github/alexisTrejo11/construction/company/shared/AppErrorResolver.java

### Rich Domain Entities with Explicit State Machines

Lifecycles for projects, phases, budgets, and expenses are enforced by methods on the entities themselves (`submit()`, `approve()`, `updateStatus(...)`, `restore()`), never by assigning a status field directly. These methods return a `Result`, so an invalid transition, such as approving a draft expense or reopening a completed project, is rejected inside the domain model and surfaces as a business-rule error.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/tree/main/src/main/java/io/github/alexisTrejo11/construction/company/modules/expense/shared/domain

### Append-Only Inventory Ledger

Stock is never stored as an editable number. Balances are derived from the history of posted movements, and posted movements are immutable; corrections are made through a reversing movement that inverts the original's effect. This gives a complete audit trail by design and avoids a second source of truth.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/src/main/java/io/github/alexisTrejo11/construction/company/modules/inventory/features/InventoryHandler.java

## Cross-cutting Concerns

### Session-Based Authentication with CSRF Protection

Authentication uses Spring Security's native stateful sessions instead of JWT, persisted in the database with Spring Session JDBC. The session cookie is `HttpOnly`, `SameSite=Lax`, and `Secure` in production, and the session ID is rotated on login to prevent session fixation. CSRF protection stays enabled using a cookie-to-header token. Logging out or suspending a user takes effect immediately on the server, with no token blacklist needed.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/src/main/java/io/github/alexisTrejo11/construction/company/config/security/SecurityConfig.java

### Role + Permission + Project Membership Authorization

Authorization separates *who the user is* (global roles such as `PROJECT_MANAGER` or `FINANCE_OFFICER`), *what they can do* (fine-grained permissions such as `EXPENSE_APPROVE`, mapped from roles in a typed `EnumMap` catalog), and *where they can do it* (active project membership). A focused policy returns a `Result` instead of throwing, and the administrator's membership bypass is an explicit allow-list of permissions rather than a blanket grant.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/src/main/java/io/github/alexisTrejo11/construction/company/modules/project/shared/policy/DefaultProjectAuthorizationPolicy.java

### Invitation-Only Onboarding with Hashed Tokens

New accounts are created only through invitations. The raw invitation token is sent by email, and only its SHA-256 hash is stored, so a database leak does not expose usable links. Tokens are single-use and expire, and the invited user cannot sign in until they accept and set a password.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/src/main/java/io/github/alexisTrejo11/construction/company/modules/user/features/createinvitation/CreateInvitationHandler.java

### Request Tracing

A servlet filter assigns a unique trace ID to every request, puts it in the logging context (MDC), returns it in the `X-Trace-Id` header, and includes it in every response body, both success and error. Any client-reported error can be matched to its server log lines.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/src/main/java/io/github/alexisTrejo11/construction/company/config/TraceIdFilter.java

### Safe File Uploads

Evidence attachments are validated for size (10 MB) and content type (JPEG, PNG, PDF). They are stored under opaque, application-generated keys, never under user-supplied filenames or exposed filesystem paths. Storage sits behind a `FileStorage` interface, so the local disk implementation can be replaced by object storage without touching the handlers.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/tree/main/src/main/java/io/github/alexisTrejo11/construction/company/modules/evidence/shared/storage

### Concurrency-Safe Stock Posting

When a movement is posted, the movement row and every location it touches are locked with pessimistic write locks, and locations are always locked in ascending ID order so that two concurrent postings cannot deadlock. The balance check and the stock change happen inside that lock, so two simultaneous issues cannot both spend the same stock and drive the balance negative.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/src/main/java/io/github/alexisTrejo11/construction/company/modules/inventory/shared/persistence/InventoryLocationRepository.java

## Integrations

### Post-Commit Event-Driven Notifications

Handlers publish immutable application events instead of sending emails inline. A `@TransactionalEventListener(AFTER_COMMIT)` creates the in-app notification only after the business transaction has committed, so a rolled-back operation never notifies anyone. Email is then delivered on a dedicated `@Async` thread pool, so SMTP latency or failures never slow down or roll back the request.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/src/main/java/io/github/alexisTrejo11/construction/company/modules/notification/features/invitation/InvitationNotificationListener.java

### Versioned Schema Migrations with Startup Validation

The production PostgreSQL schema is owned by versioned Flyway migrations, and Hibernate runs in `validate` mode, so the application refuses to start if entity mappings drift from the real schema. Integrity rules also live in the database, for example a check constraint guarantees that evidence targets exactly one phase or one expense.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/tree/main/src/main/resources/db/migration

## Testing

### Endpoint Integration Test Suite

Every implemented endpoint is covered by integration tests that boot the full Spring context and call it over HTTP with MockMvc against an in-memory H2 database. The tests cover the success path, validation failures, expected business failures, and the persisted database effect. Workflows build their prerequisites through the real endpoints rather than by inserting rows directly, and only external boundaries (SMTP, file storage) are mocked. The suite (54 tests) runs on every pull request in CI.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/tree/main/src/test/java/io/github/alexisTrejo11/construction/company/modules
