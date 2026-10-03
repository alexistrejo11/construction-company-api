# Spec-Driven Development Docs

This folder holds the specifications used to build the application: architecture, API contract, domain model, module specs, and cross-cutting conventions. This README is the index and high-level planning context only. It is not the source of truth for implemented behavior or API contracts.

## Index

### Architecture

- [Architecture](architecture.md): confirmed package structure, vertical feature slices, and component responsibilities.

### API Contract

- [API Endpoints](api/endpoints.md): the single endpoint catalog, with permissions and success statuses. Do not add endpoint definitions anywhere else.

### Domain

- [Domain Overview](domain/README.md)
- [Glossary](domain/glossary.md): shared business terminology.
- [Entities](domain/entities.md): entity catalog.
- [Relationships](domain/relationships.md): ownership, cardinality, and lifecycle dependencies.

### Modules

- [Modules Overview](modules/README.md)
- [User](modules/user.md)
- [Project](modules/project.md)
- [Budget](modules/budget.md)
- [Expense](modules/expense.md)
- [Evidence and Attachments](modules/evidence.md)
- [Inventory](modules/inventory.md)
- [Notification](modules/notification.md)
- [Contractor](modules/contractor.md): not implemented yet.
- [Approval](modules/approval.md): potential future module.

### Cross-Cutting Conventions

- [Error Handling](conventions/error-handling.md)
- [Validation](conventions/validation.md)
- [Authentication and Sessions](conventions/authentication.md)
- [Authorization](conventions/authorization.md)
- [Endpoint Integration Testing](conventions/testing.md)

### Outside This Folder

- [Implementation Backlog](../pending/implementation-backlog.md): open implementation work, verification, and known issues.
- [Pending Architecture Decisions](../pending/architecture-decisions.md): unresolved questions; not requirements until promoted into [Architecture](architecture.md).
- [Portfolio Docs](../portfolio/): external showcase following the [documentation guide](../doc_guide.md). Never used as a spec.

## Scope

The application is an internal construction-company API for managing projects, users, project work, budgets, expenses, contractors, inventory, and notifications.

## Infrastructure Direction

- PostgreSQL is the target deployment database.
- SQLite is used for development.
- H2 is used for test configuration.
- Spring Data JPA and Hibernate provide persistence.
- Flyway migrations are used for production schema management.
- SMTP email is the initial external notification channel.

These infrastructure notes are summarized from the current project configuration. Operational details belong in dedicated documentation when they are finalized.

## Future Features

- Generic approval workflows are excluded from the current API and may be considered for critical operations later.
- Formal budget versioning is deferred; the initial model revises one budget in place.
- Public signup is excluded; users enter through invitations.
- Transactional outbox delivery is deferred until notification reliability requires it.
