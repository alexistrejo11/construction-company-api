# Product Planning

This file contains high-level planning context only. It is not the source of truth for implemented behavior or API contracts.

## Scope

The application is an internal construction-company API for managing projects, users, project work, budgets, expenses, contractors, inventory, and notifications.

## Domain Documentation

The current business model is documented in:

- `docs/domain/glossary.md`
- `docs/domain/entities.md`
- `docs/domain/relationships.md`
- `docs/modules/`

## Architecture

The confirmed structural architecture is documented in `docs/architecture.md`.

## API Contract

The complete endpoint catalog is maintained exclusively in `docs/api/endpoints.md`. Do not add endpoint definitions to this planning file.

The ordered implementation checklist is maintained in `docs/implementation-plan.md`.

## Cross-Cutting Conventions

- Error handling: `docs/conventions/error-handling.md`
- Validation: `docs/conventions/validation.md`
- Authentication and sessions: `docs/conventions/authentication.md`
- Authorization: `docs/conventions/authorization.md`

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
