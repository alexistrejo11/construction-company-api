# Implementation Plan

## Purpose

This document tracks the implementation of the target API architecture and domain model. It is an execution checklist, not a replacement for the architecture, domain, convention, or endpoint documents.

## Working Rules

- Complete a phase's decisions before implementing its code.
- Implement vertical slices end to end: command/query, handler, controller, persistence, response, and focused verification.
- Keep target design separate from legacy code until the replacement is ready.
- Update the relevant documentation when implementation decisions change.
- Do not mark a checklist item complete until the code and verification are complete.

## Phase 0: Baseline and Workspace

Goal: establish a clean, reproducible starting point.

- [x] Review `AGENTS.md` and all required documentation.
- [x] Select the first MVP vertical slices and defer non-essential endpoints.
- [x] Confirm the Java 26 toolchain and Gradle wrapper work locally.
- [x] Run `./gradlew compileJava` and record the baseline result.
- [x] Confirm the current test limitation: no Java tests and disabled `test` task.
- [x] Decide whether to repair or defer the current Dockerfile mismatch.
- [x] Create a branch or implementation checkpoint before structural migration.

### Baseline Record (2026-08-19)

- `./gradlew --version` reports Gradle 9.4.0 running on Java 26.0.1.
- `./gradlew compileJava` succeeds. Existing warnings concern Lombok's `Notification.read` builder default, unmapped `NotificationMapper` target properties, and deprecated rate-limiter API usage.
- `src/test/` has no Java sources and `build.gradle` disables the `test` task.
- The Dockerfile was repaired for Java 26 and the repository layout. `docker compose config --quiet` succeeds; image-build verification is pending because the local Docker daemon is not running.
- Commits are the implementation checkpoints; a separate migration branch is not required.

### Initial MVP Scope

The first MVP delivers a private, project-scoped workspace. Implement these vertical slices in phase order:

- Invitation creation and acceptance, then session login, logout, and current-user context.
- Current-user profile retrieval and update.
- Project creation, listing, lookup by ID, general update, and status transition.
- Project-member creation, listing, lookup, and active/inactive status changes.
- Project-phase creation, listing, lookup, general update, status transition, and reorder.

The following endpoints are deferred beyond the MVP: invitation resend and cancellation; administrative user listing, role management, and account-status management; project restoration and summary endpoints; evidence and attachments; budgets, budget items, and expenses; contractors; inventory; notifications; and approvals. Public signup and generic approval workflows remain excluded from the target API.

## Phase 1: Persistence Foundation

Goal: make the database model support the target domain without relying on development-only schema updates.

- [x] Review current entities against `docs/domain/entities.md`.
- [x] Decide the final package ownership of entities, repositories, and nested-area persistence.
- [x] Decide cascade and orphan-removal behavior for project phases and members.
- [x] Define foreign keys, unique constraints, nullable columns, indexes, and deletion behavior.
- [x] Replace or reconcile legacy entities that conflict with the target model.
- [x] Define the initial production schema migration.
- [x] Add a new Flyway migration under `src/main/resources/db/migration/`.
- [x] Confirm the `prod` profile validates the migrated schema.
- [x] Confirm the `dev` profile still starts with SQLite.

### Phase 1 Record (2026-08-19)

- The initial schema covers the MVP foundation: users, invitations, projects, project members, and project phases.
- Domain entities and their enums/embeddables live in the owning module's `shared/domain/`; module repositories remain in `shared/persistence/`.
- Project phases and members have independent repositories and foreign keys. They are not `Project` cascade or orphan-removal collections.
- The legacy V1/V2 reimbursement schema and demo data were replaced because no deployed PostgreSQL database depends on them.
- A fresh SQLite development database starts successfully. PostgreSQL migration and Hibernate validation are deferred to the deployment workflow before production use.

## Phase 2: API and Response Foundation

Goal: establish stable transport contracts before implementing feature slices.

- [x] Review `docs/api/endpoints.md` and mark MVP, deferred, and future endpoints.
- [x] Confirm request, command/query, response, and permission names for each MVP slice.
- [x] Confirm the API version and resource path conventions.
- [x] Implement the response envelope from `docs/conventions/error-handling.md`.
- [x] Remove the response `success` boolean.
- [x] Ensure success responses omit `error` and failure responses omit `data`.
- [x] Add the structured error response with `error_type`, `code`, `message`, and `details`.
- [x] Add or finalize `traceId` handling at the envelope level.
- [x] Add `FORBIDDEN` to the result taxonomy if required by authorization flows.
- [x] Align `AppErrorResolver` with the final result-to-HTTP mapping.
- [x] Align `GlobalExceptionHandler` and security error handlers with the same envelope.
- [x] Define the pagination response and request contracts without exposing Spring `Page` or `Pageable` at the API boundary.
- [ ] Add focused serialization or controller tests once the test task is enabled.

### Phase 2 Record (2026-08-19)

- Active API routes use `/v2/api`; deferred approval and contractor routes were removed.
- Success responses contain `message`, `data`, `timestamp`, and `traceId`; failure responses contain `message`, `error`, `timestamp`, and `traceId`.
- `Result` errors map to 400, 401, 403, 404, 409, 422, or 500. Security boundary failures use the same envelope.
- Trace IDs are generated by the server and returned in `X-Trace-Id`.
- Pagination contracts use 1-based `page`, bounded `size`, optional `sort`, and an `items` response shape without exposing Spring pagination types.
- The Gradle test task is enabled. Endpoint integration tests are required for subsequent implementation work under `docs/conventions/testing.md`.

## Phase 3: Sessions, Invitations, and Users

Goal: replace JWT authentication with stateful Spring Security sessions and implement private onboarding.

- [x] Remove JWT bearer authentication from the active security flow.
- [x] Configure stateful Spring Security session management.
- [x] Define session cookie attributes and expiration.
- [x] Define and enable the CSRF strategy for session-based authentication.
- [x] Implement the active account statuses: `INVITED`, `ACTIVE`, `SUSPENDED`, and `DISABLED`.
- [x] Implement invitation creation with assigned initial global roles.
- [x] Generate single-use, expiring invitation tokens.
- [x] Send invitation email through the configured mail channel.
- [x] Implement invitation acceptance and password creation.
- [x] Activate the user only after invitation acceptance completes.
- [x] Implement login for active users only.
- [x] Implement logout and session invalidation.
- [x] Implement `/auth/me` and `/users/me`.
- [x] Implement administrative user listing, profile updates, role updates, and status changes.
- [x] Hash passwords with BCrypt and never return password fields in responses.
- [x] Remove `UNKNOWN` as a valid persisted role in the target model.
- [x] Replace legacy mutable auth DTOs where appropriate with records.
- [x] Add authentication, invitation, and account-status verification.
- [x] Add complete MockMvc integration coverage for every implemented Phase 3 endpoint under `docs/conventions/testing.md`.

## Phase 4: Authorization Foundation

Goal: enforce global permissions and project scope without coupling domain code to Spring Security.

- [x] Define the initial `Permission` enum from implemented use cases.
- [x] Define the initial global roles.
- [x] Implement the immutable role-to-permission catalog.
- [x] Implement immutable `UserContext` from the authenticated principal.
- [x] Add the presentation adapter or argument resolver for the current user.
- [x] Implement focused authorization policies.
- [x] Implement active project-membership checks.
- [x] Ensure domain entities and repositories do not read `SecurityContextHolder`.
- [x] Configure coarse route-level security rules.
- [x] Return expected forbidden outcomes through the application result flow.
- [x] Add authorization checks for User, Invitation, Project, Phase, and Member use cases.
- [ ] Verify that users cannot access another user's notifications or private resources.
- [x] Add MockMvc authorization coverage for every currently implemented Phase 4 boundary.

## Phase 5: Project Core

Goal: implement the first complete business flow.

- [x] Implement project create command, handler, controller, mapper, and response.
- [x] Implement project lookup by ID and business code.
- [x] Implement project listing and `my-projects` query.
- [x] Implement project general update with `PATCH`.
- [x] Protect the project business code from ordinary updates.
- [x] Implement explicit project status transitions.
- [x] Implement project cancellation through the status operation.
- [x] Implement explicit restoration for cancelled projects.
- [x] Implement project summaries.
- [x] Implement project validation for dates, budget estimate, and required fields.
- [x] Verify project authorization by permission and membership scope.
- [x] Add complete MockMvc integration coverage for every implemented Phase 5 endpoint under `docs/conventions/testing.md`.

## Phase 6: Project Members and Phases

Goal: implement project-scoped work management.

### Members

- [x] Implement add-member use case.
- [x] Enforce one membership per user and project.
- [x] Implement active/inactive membership status.
- [x] Preserve membership history when deactivating a member.
- [x] Remove project-specific roles from the target model.
- [x] Implement member listing and member lookup.
- [x] Verify membership authorization.

### Phases

- [x] Add phase status to persistence if not already present.
- [x] Implement phase create, read, update, and list use cases.
- [x] Implement phase status transitions.
- [x] Implement phase reorder.
- [x] Validate phase dates, order, allocated budget, and project ownership.
- [x] Prevent ordinary changes to completed or cancelled phases.
- [x] Verify that project completion handles open phases according to the documented policy.
- [x] Add complete MockMvc integration coverage for every implemented Phase 6 endpoint under `docs/conventions/testing.md`.

## Phase 7: Evidence and Attachments

Goal: replace resource-specific attachment models with reusable evidence support.

- [ ] Define the persistence association between `Evidence` and supported resources.
- [ ] Define the `Attachment` storage metadata and storage key contract.
- [ ] Implement evidence create, read, update, and delete operations.
- [ ] Implement attachment upload and metadata listing.
- [ ] Implement safe file-name, MIME type, and file-size validation.
- [ ] Keep file storage behind an application/infrastructure boundary.
- [ ] Migrate or deprecate `ExpenseAttachmentEntity`.
- [ ] Remove or replace legacy `Expense.invoiceUrl`.
- [ ] Verify evidence does not modify the lifecycle of its target resource.
- [ ] Add complete MockMvc integration coverage for every implemented Phase 7 endpoint under `docs/conventions/testing.md`.

## Phase 8: Budget and Expenses

Goal: implement financial planning and expense control.

### Budget

- [ ] Implement one budget per project.
- [ ] Implement budget lifecycle: `DRAFT`, `APPROVED`, `CLOSED`.
- [ ] Implement budget create, read, update, approve, revise, close, and summary use cases.
- [ ] Keep budget revision in place; defer formal versioning.
- [ ] Implement budget item create, read, update, and removal rules.
- [ ] Derive planned item totals from quantity and unit price.

### Expenses

- [ ] Implement expense lifecycle: `DRAFT`, `PENDING_APPROVAL`, `APPROVED`, and `REJECTED`.
- [ ] Implement expense create, read, and draft update.
- [ ] Implement submit, approve, reject, and return-to-draft operations.
- [ ] Count only approved expenses as executed cost.
- [ ] Derive balance and variance from approved expenses.
- [ ] Decide whether persisted executed totals are needed for performance.
- [ ] If totals are materialized, add transactional updates and reconciliation.
- [ ] Add evidence support for receipts and invoices.
- [ ] Verify over-budget behavior without silently blocking normal expense recording.
- [ ] Add complete MockMvc integration coverage for every implemented Phase 8 endpoint under `docs/conventions/testing.md`.

## Phase 9: Inventory

Goal: implement generic inventory for materials, tools, equipment, and supplies.

- [ ] Implement `InventoryItem` and its categories.
- [ ] Implement quantity and serialized tracking modes.
- [ ] Implement `InventoryLocation` for warehouses and project sites.
- [ ] Implement location CRUD and active/inactive behavior.
- [ ] Implement draft inventory movements.
- [ ] Implement movement lines and positive quantity validation.
- [ ] Implement posting movements and applying stock effects.
- [ ] Prevent edits to posted movements.
- [ ] Implement reversing or adjustment movements.
- [ ] Derive balances from movement history initially.
- [ ] Add a materialized balance only if performance requires it.
- [ ] Add low-stock threshold policy if the inventory item model supports it.
- [ ] Publish low-stock events only when the threshold-crossing policy is defined.
- [ ] Add complete MockMvc integration coverage for every implemented Phase 9 endpoint under `docs/conventions/testing.md`.

## Phase 10: Notifications

Goal: deliver explicit and event-driven user notifications without coupling business operations to external mail or SMS providers.

- [ ] Implement in-app notification persistence.
- [ ] Implement current-user notification listing and read operations.
- [ ] Remove the public generic notification creation endpoint.
- [ ] Define immutable application event payloads.
- [ ] Configure `ApplicationEventPublisher` for business events.
- [ ] Handle transactional events with `@TransactionalEventListener(AFTER_COMMIT)`.
- [ ] Dispatch email asynchronously through Spring Mail.
- [ ] Keep SMTP credentials in environment configuration.
- [ ] Add channel delivery status only if retries or diagnostics require it.
- [ ] Add SMS only after selecting a provider and defining its failure policy.
- [ ] Add notification deduplication for repeated conditions such as low stock.
- [ ] Consider a transactional outbox if notification loss becomes unacceptable.
- [ ] Add complete MockMvc integration coverage for every implemented Phase 10 endpoint under `docs/conventions/testing.md`.

## Phase 11: Verification and Hardening

Goal: verify the implemented system and close migration gaps.

- [x] Enable the Gradle test task.
- [ ] Add handler tests for expected `Result` outcomes.
- [ ] Add domain tests for status transitions and invariants.
- [ ] Add repository integration tests with the test database.
- [ ] Add controller tests for response envelopes and validation.
- [ ] Add security tests for sessions, CSRF, authentication, and authorization.
- [ ] Add migration verification against PostgreSQL.
- [ ] Verify no API response exposes JPA entities.
- [ ] Verify every controller mapping uses `/v2/api`.
- [ ] Verify no active code depends on JWT configuration.
- [ ] Verify no secrets or `.env` files are tracked.
- [ ] Run `./gradlew compileJava`.
- [ ] Run `./gradlew test` after tests are enabled.
- [ ] Run `./gradlew bootJar`.
- [ ] Review Docker build compatibility with Java 26 and the actual repository layout.

## Definition Of Done

A phase is complete when:

- Its domain and API decisions are documented.
- Its production code follows the architecture and conventions.
- Its database changes have the correct migration strategy.
- Its expected business outcomes use `Result<T>`.
- Its controllers expose only presentation DTOs.
- Its authorization rules are enforced at the appropriate boundary.
- Its focused verification passes.
- Its legacy replacement or migration notes are recorded.
