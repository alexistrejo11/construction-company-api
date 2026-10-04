# Construction Company API

[![Backend CI-CD](https://github.com/alexistrejo11/contruction-company-api/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/alexistrejo11/contruction-company-api/actions/workflows/ci-cd.yml)
![Java](https://img.shields.io/badge/Java-26-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F)
![Gradle](https://img.shields.io/badge/Gradle-9.4.0-02303A)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A private back-office REST API for construction companies. It manages projects and their phases, project teams, operational budgets and the expenses charged against them, photo and document evidence of site work, company-wide inventory, and in-app and email notifications. Everything lives in one system with role-based and project-based access control.

| | |
|---|---|
| **Live API** | <https://api.construction.alexis-trejo.com> |
| **Swagger UI** | <https://api.construction.alexis-trejo.com/swagger-ui/index.html> |
| **OpenAPI spec** | <https://api.construction.alexis-trejo.com/api-docs> |
| **Health** | <https://api.construction.alexis-trejo.com/actuator/health> |

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Authentication](#authentication)
- [API Overview](#api-overview)
- [Testing](#testing)
- [Deployment](#deployment)
- [Documentation](#documentation)
- [Project Status](#project-status)
- [License](#license)

---

## Features

- **Invitation-only user management.** There is no public signup. Administrators invite users by email with single-use, expiring links, assign global roles, and suspend or reactivate accounts.
- **Projects, phases, and members.** Projects follow a status lifecycle (`PLANNING → IN_PROGRESS ⇄ ON_HOLD → COMPLETED`, with cancellation and administrative restore). Each project has ordered phases and a membership history.
- **Budgets and expense approval.** Each project has one revisable budget (`DRAFT → APPROVED → CLOSED`) made of budget items. Expenses move through `DRAFT → PENDING_APPROVAL → APPROVED / REJECTED`, and only approved expenses count as executed spend. Budget summaries report planned, executed, remaining, and variance amounts.
- **Evidence and attachments.** Phases and expenses can carry evidence with JPEG, PNG, or PDF attachments (up to 10 MB each).
- **Inventory with movement ledger.** Items and locations (warehouses or project sites) are company-wide. Stock changes only through receipts, issues, transfers, returns, and adjustments. Posted movements are immutable and are corrected by reversal. Stock can never go negative, and serialized items are tracked unit by unit.
- **Notifications.** Workflows create personal in-app notifications, and invitations are also sent by SMTP email.
- **Cross-cutting concerns.** Cookie-based sessions with CSRF protection, permission and project-membership authorization, a consistent response envelope with trace IDs, token-bucket rate limiting, Actuator health checks and a Prometheus metrics endpoint, and Flyway-managed production schemas.

## Tech Stack

| Area | Technology |
|---|---|
| Language and runtime | Java 26 |
| Framework | Spring Boot 3.5.16 (Web, Data JPA, Validation, Security, Mail, Cache, Actuator) |
| Sessions | Spring Session JDBC |
| Persistence | Hibernate, PostgreSQL (prod), SQLite (dev), H2 (tests) |
| Migrations | Flyway |
| API docs | springdoc-openapi (Swagger UI) |
| Rate limiting and caching | Bucket4j, Caffeine |
| Observability | Spring Boot Actuator, Micrometer Prometheus registry |
| Build | Gradle 9.4.0 wrapper |
| Delivery | Docker (multi-stage), GitHub Actions, GitHub Container Registry |

## Architecture

The application is a **modular monolith**: one Spring Boot deployable, with each business capability in its own module under `modules/`. Inside a module, code is organized as **vertical feature slices**. Each use case owns its controller, command or query, handler, and response in a single package.

```text
src/main/java/io/github/alexisTrejo11/construction/company/
├── ConstructionCompanyApplication.java
├── config/                 # framework and infrastructure configuration (security, tracing, ...)
├── shared/                 # cross-module technical primitives (Result, response envelope, errors)
└── modules/
    ├── auth/               # login, logout, session context, CSRF
    ├── user/               # invitations, account profile, user administration
    ├── project/            # projects, plus nested phases/ and members/ areas
    ├── budget/             # budgets and budget items
    ├── expense/            # expense lifecycle and approval
    ├── evidence/           # evidence and file attachments
    ├── inventory/          # items, locations, movements, balances
    ├── notification/       # in-app and email notifications
    └── home/               # API root landing endpoint
```

A typical feature slice:

```text
modules/project/features/getbyid/
├── GetProjectByIdController.java   # binds and validates HTTP input
├── GetProjectByIdQuery.java        # immutable input record
└── GetProjectByIdHandler.java      # orchestration and transaction boundary, returns Result<T>
```

Main conventions:

- **Controllers** only bind and validate HTTP input, call one handler, and translate the result. They map failures with `AppErrorResolver.handleResult(...)` and successes with `ResponseWrapper`.
- **Handlers** own the use-case flow and transaction boundaries. They return expected business outcomes as `Result<T>` instead of throwing exceptions for not-found, conflict, or validation failures.
- **JPA entities are the domain model.** They enforce their own invariants and state transitions but never depend on repositories, HTTP types, or feature types.

See [Architecture](docs/sdd/architecture.md) for the full package and ownership rules.

## Getting Started

### Prerequisites

- **JDK 26.** Gradle is provided by the checked-in `./gradlew` wrapper.
- **Docker** (optional), only needed to run the production-like stack with PostgreSQL.

### Run locally (SQLite, no database server needed)

```bash
git clone https://github.com/alexistrejo11/contruction-company-api.git
cd contruction-company-api

cp .env.example .env    # then fill in values, at least BOOTSTRAP_ADMIN_*
./gradlew bootRun
```

The API starts on **<http://localhost:8017>** with the `dev` profile:

- API root: <http://localhost:8017/>
- Swagger UI: <http://localhost:8017/swagger-ui/index.html>
- Health: <http://localhost:8017/actuator/health>

On first start against an empty database, a bootstrap initializer creates an active `COMPANY_ADMIN` from `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD`. Use those credentials to sign in and invite other users. The initializer never recreates users once one exists.

### Run the production-like stack (API + PostgreSQL 17)

```bash
cp .env.example .env    # DB_PASSWORD is required
docker compose up --build
```

Compose runs the `prod` profile: PostgreSQL, Flyway migrations, and Hibernate schema validation. The API is exposed on port `8017`.

### Useful Gradle tasks

| Command | Purpose |
|---|---|
| `./gradlew compileJava` | Compile production code |
| `./gradlew bootRun` | Run the app locally; forwards `.env` values to the JVM |
| `./gradlew test` | Run the unit and integration test suite |
| `./gradlew bootJar` | Build the executable jar into `build/libs/` |

## Configuration

Configuration comes from environment variables. Locally, Spring imports them from an ignored `.env` file; see [`.env.example`](.env.example) for the full template.

| Variable | Default | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` (SQLite) or `prod` (PostgreSQL + Flyway) |
| `SERVER_PORT` | `8017` | HTTP port |
| `DB_FILE` | `dev_construction.db` | SQLite file (`dev` only) |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | none | PostgreSQL connection (`prod` only) |
| `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_PASSWORD` | none | First administrator, created only when no users exist |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | `localhost`, `1025` | SMTP server used for invitation emails |
| `FILE_UPLOAD_DIR` | `./uploads` | Attachment storage directory |
| `FILE_UPLOAD_MAX_SIZE_BYTES` | `10485760` | Maximum attachment size (10 MB) |
| `FILE_UPLOAD_ALLOWED_CONTENT_TYPES` | `image/jpeg,image/png,application/pdf` | Accepted attachment types |
| `APP_NAME`, `APP_DESCRIPTION`, `APP_VERSION` | built-in | Metadata shown by `GET /` and in OpenAPI |

### Profiles

| Profile | Database | Schema management |
|---|---|---|
| `dev` (default) | SQLite file | Hibernate `ddl-auto=update`, Flyway disabled |
| `prod` | PostgreSQL | Flyway migrations, Hibernate `validate` |
| `test` | In-memory H2 | Recreated per test context |

> Production schema changes require a new Flyway migration in [`src/main/resources/db/migration/`](src/main/resources/db/migration/). Do not rely on development's automatic Hibernate schema update.

## Authentication

The API uses **stateful, cookie-based sessions** (Spring Security + Spring Session JDBC), not bearer tokens. Every state-changing request (`POST`, `PATCH`, `DELETE`), including login, must send a CSRF token.

```bash
BASE=http://localhost:8017/v2/api

# 1. Get a CSRF token (sets the XSRF-TOKEN cookie)
curl -c cookies.txt "$BASE/auth/csrf"
XSRF=$(awk '/XSRF-TOKEN/ {print $7}' cookies.txt)

# 2. Log in (sets the SESSION cookie)
curl -b cookies.txt -c cookies.txt \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $XSRF" \
  -d '{"email":"admin@example.com","password":"change-me"}' \
  "$BASE/auth/login"

# 3. Call the API with the session cookie
curl -b cookies.txt "$BASE/auth/me"
```

- Sessions last 24 hours, are stored server-side, and the session ID rotates on login.
- Public endpoints: `GET /`, `GET /actuator/health`, `GET /actuator/info`, `GET /v2/api/auth/csrf`, `POST /v2/api/auth/login`, `POST /v2/api/invitations/{token}/accept`, and the Swagger/OpenAPI docs.
- Authorization requires the endpoint's permission (granted through global roles). For project-scoped resources, the user also needs an active membership in that project.

Details: [Authentication and Sessions](docs/sdd/conventions/authentication.md) and [Authorization](docs/sdd/conventions/authorization.md).

## API Overview

All business routes live under **`/v2/api`**. Partial updates use `PATCH`, and business transitions use explicit action endpoints such as `POST /expenses/{id}/approve`.

| Resource | Base path | Highlights |
|---|---|---|
| Auth | `/auth` | CSRF token, login, logout, current session |
| Invitations | `/invitations` | Invite users, accept an invitation |
| Users | `/users` | Administration, roles, status, own profile (`/users/me`) |
| Projects | `/projects` | CRUD, status, restore, per-project and global summaries |
| Phases | `/projects/{projectId}/phases` | CRUD, status, reorder |
| Members | `/projects/{projectId}/members` | Add, list, activate or deactivate |
| Evidence | `/projects/{projectId}/phases/{phaseId}/evidence`, `/expenses/{expenseId}/evidence`, `/evidence` | Evidence and multipart attachments |
| Budgets | `/projects/{projectId}/budget`, `/budgets` | Create, approve, revise, close, summary, budget items |
| Expenses | `/budgets/{budgetId}/expenses`, `/expenses` | Draft, submit, approve, reject, return to draft |
| Inventory | `/inventory/items`, `/inventory/locations`, `/inventory/movements` | Catalog, balances, post and reverse movements |
| Notifications | `/notifications` | List, read, mark all as read |

### Response envelope

Responses with a body share one envelope. Success responses carry `data`, and error responses carry `error`. The HTTP status code indicates success or failure. Every response includes a `traceId`, which is also returned in the `X-Trace-Id` header.

```json
{
  "message": "Project fetched successfully",
  "data": { "id": 42, "code": "PRJ-2026-001", "name": "North Tower", "status": "IN_PROGRESS" },
  "timestamp": "2026-10-03T17:00:00Z",
  "traceId": "8f2a0c4e-6b1d-4a7e-9c3f-2d5e8a1b7c90"
}
```

```json
{
  "message": "Request validation failed",
  "error": {
    "error_type": "VALIDATION",
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed",
    "details": [ { "field": "email", "message": "must be a well-formed email address" } ]
  },
  "timestamp": "2026-10-03T17:00:00Z",
  "traceId": "..."
}
```

List endpoints use one-based `page` and `size` query parameters (maximum size 100) plus resource-specific filters. Rate-limited requests receive `429 Too Many Requests` with an `X-Rate-Limit-Retry-After-Seconds` header.

The complete endpoint catalog, with permissions and success statuses, is in [API Endpoints](docs/sdd/api/endpoints.md). Error and validation rules are in [Error Handling](docs/sdd/conventions/error-handling.md) and [Validation](docs/sdd/conventions/validation.md).

## Testing

```bash
./gradlew test
```

Tests run against in-memory H2 with the `test` profile. Every implemented endpoint is expected to have integration coverage through the full HTTP, security, and persistence stack, as defined in [Endpoint Integration Testing](docs/sdd/conventions/testing.md). Test sources live in [`src/test/java`](src/test/java/io/github/alexisTrejo11/construction/company/).

## Deployment

The [GitHub Actions pipeline](.github/workflows/ci-cd.yml) runs on every pull request and every push to `main`:

1. **Test:** sets up JDK 26 and runs `./gradlew test`.
2. **Publish** (`main` only): builds the multi-stage [`Dockerfile`](Dockerfile) and pushes the image to GitHub Container Registry, tagged `latest` and with the commit SHA.
3. **Deploy** (`main` only): connects to the host over SSH through Cloudflare Access, pulls the new image, and recreates only this project's container.

The production image runs on `eclipse-temurin:26-jre` as a non-root user. Cloudflare terminates TLS in front of the host.

## Documentation

| Document | Purpose |
|---|---|
| [SDD index](docs/sdd/README.md) | Entry point to the specifications and planning context |
| [Architecture](docs/sdd/architecture.md) | Package structure, feature slices, component responsibilities |
| [API Endpoints](docs/sdd/api/endpoints.md) | Single source of truth for routes, permissions, and statuses |
| [Domain overview](docs/sdd/domain/README.md) | [Glossary](docs/sdd/domain/glossary.md), [Entities](docs/sdd/domain/entities.md), [Relationships](docs/sdd/domain/relationships.md) |
| [Modules overview](docs/sdd/modules/README.md) | [User](docs/sdd/modules/user.md), [Project](docs/sdd/modules/project.md), [Budget](docs/sdd/modules/budget.md), [Expense](docs/sdd/modules/expense.md), [Evidence](docs/sdd/modules/evidence.md), [Inventory](docs/sdd/modules/inventory.md), [Notification](docs/sdd/modules/notification.md) |
| Conventions | [Error Handling](docs/sdd/conventions/error-handling.md), [Validation](docs/sdd/conventions/validation.md), [Authentication](docs/sdd/conventions/authentication.md), [Authorization](docs/sdd/conventions/authorization.md), [Testing](docs/sdd/conventions/testing.md) |
| [Implementation Backlog](docs/pending/implementation-backlog.md) | Open work, pending verification, known issues |
| [Pending Architecture Decisions](docs/pending/architecture-decisions.md) | Unresolved questions (not requirements) |
| [Portfolio](docs/portfolio/) | External showcase: [Product](docs/portfolio/product.md), [Architecture](docs/portfolio/architecture.md), [API](docs/portfolio/api.md), [Features](docs/portfolio/features.md), [Infrastructure](docs/portfolio/infrastructure.md) |

Contributor and agent guidelines are in [`AGENTS.md`](AGENTS.md).

## Project Status

Version **2.0.0**. Implemented: authentication, users and invitations, projects (with phases and members), evidence and attachments, budgets and budget items, expenses, inventory, and notifications.

Not yet implemented:

- Contractor and supplier management. It is specified in [Contractor](docs/sdd/modules/contractor.md), but no routes exist yet.
- The invitation resend and cancel endpoints.

Deferred by design: generic approval workflows, formal budget versioning, low-stock alerts, SMS notifications, and a transactional outbox. See the [Implementation Backlog](docs/pending/implementation-backlog.md) for the full list and known issues.

## License

Released under the [MIT License](LICENSE).
