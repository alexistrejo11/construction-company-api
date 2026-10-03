# Implementation Backlog

This file tracks implementation work that is still open. It is not a source of business rules or API contracts: specifications live in `docs/sdd/` (index: `docs/sdd/README.md`).

Remove an item once its code, documentation, and verification are complete.

## Documented Endpoints Not Yet Implemented

### Contractors and Suppliers

All contractor routes in `docs/sdd/api/endpoints.md` are unimplemented, along with the contractor domain model, persistence, migration, and permissions catalog entries.

- [ ] `POST /v2/api/contractors`
- [ ] `GET /v2/api/contractors`
- [ ] `GET /v2/api/contractors/{contractorId}`
- [ ] `PATCH /v2/api/contractors/{contractorId}`
- [ ] `PATCH /v2/api/contractors/{contractorId}/status`
- [ ] `GET /v2/api/contractors/{contractorId}/projects`

### Invitations

- [ ] `POST /v2/api/invitations/{invitationId}/resend`
- [ ] `DELETE /v2/api/invitations/{invitationId}`

## Verification Still Pending

- [ ] Run the Flyway migrations against PostgreSQL and confirm Hibernate `validate` passes under the `prod` profile. This is an operational deployment check, not a Gradle test gate.
- [ ] Build the Docker image with a running Docker daemon and confirm it starts.

## Documentation Gaps

- [ ] `GET /v2/api/auth/csrf` is implemented and public but is not listed in `docs/sdd/api/endpoints.md`.

## Known Issues

- [ ] `GlobalExceptionHandler` maps every unhandled exception to `500`, so unsupported HTTP methods return `500` instead of `405` and unknown routes may not return `404`.
- [ ] `springdoc.swagger-ui.path` is `/swagger-ui.html`, but only `/swagger-ui/**` is public; the configured entry path requires authentication.
- [ ] `SecurityConfig` uses a deprecated `DaoAuthenticationProvider` API.

## Deferred Refinements

These were intentionally deferred during implementation. Start them only when their trigger applies.

### Budget and Expenses

- [ ] Materialize executed budget totals, with transactional updates and reconciliation, when derived totals become a measured performance problem.

### Inventory

- [ ] Materialize inventory balances when deriving them from posted movements becomes a measured performance problem.
- [ ] Define a low-stock threshold policy on inventory items.
- [ ] Emit low-stock events once threshold-crossing and notification recipient policies are defined.
- [ ] Support evidence and attachments for inventory movements; the evidence schema currently has no movement target.

### Notifications

- [ ] Add expense, project-status, low-stock, and other notification producers once their recipient policies are defined.
- [ ] Track channel delivery status and retries when diagnostics require them.
- [ ] Add SMS once a provider and failure policy are selected.
- [ ] Deduplicate notifications once repeated-event producers exist.
- [ ] Introduce a transactional outbox when notification loss becomes unacceptable.
