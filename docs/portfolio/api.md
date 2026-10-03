# Construction Company API — API

## Overview

REST over HTTPS with JSON payloads, except attachment uploads, which use `multipart/form-data`.

- **Base URL:** `https://api.construction.alexis-trejo.com/v2/api`
- **Versioning:** URL prefix (`/v2`). The API root (`/`) and health endpoints live outside the versioned path.
- **Style:** `PATCH` for partial updates, and explicit action endpoints for business transitions (for example `POST /expenses/{id}/approve`) instead of generic status edits.

## Documentation Tooling

- **Swagger UI:** https://api.construction.alexis-trejo.com/swagger-ui/index.html
- **OpenAPI spec:** https://api.construction.alexis-trejo.com/api-docs
- **API root:** `GET https://api.construction.alexis-trejo.com/` returns the API name, version, base path, and documentation and health links.

## Authentication

The API uses **stateful, cookie-based sessions** (Spring Security + Spring Session JDBC), not bearer tokens. There is no public signup: accounts are created through invitations.

Because authentication relies on cookies, every state-changing request (`POST`, `PATCH`, `DELETE`), including login, must carry a **CSRF token**:

**1. Get a CSRF token** (sets the `XSRF-TOKEN` cookie):

```
GET /v2/api/auth/csrf
→ 204 No Content
Set-Cookie: XSRF-TOKEN=3f1c...; Path=/
```

**2. Log in**, sending the token back in a header:

```
POST /v2/api/auth/login
Content-Type: application/json
X-XSRF-TOKEN: 3f1c...
Cookie: XSRF-TOKEN=3f1c...

{ "email": "admin@example.com", "password": "••••••••" }

→ 200 OK
Set-Cookie: SESSION=YTk2...; Path=/; HttpOnly; SameSite=Lax; Secure
```

**3. Call the API** with the `SESSION` cookie, plus `X-XSRF-TOKEN` on every state-changing request:

```
PATCH /v2/api/projects/42/status
Cookie: SESSION=YTk2...; XSRF-TOKEN=3f1c...
X-XSRF-TOKEN: 3f1c...
```

- Sessions last 24 hours, are stored server-side, and the session ID is rotated on login to prevent session fixation.
- Logging out (`POST /auth/logout`) invalidates the session on the server immediately.
- **Public endpoints:** `GET /`, `GET /actuator/health`, `GET /actuator/info`, `GET /auth/csrf`, `POST /auth/login`, `POST /invitations/{token}/accept`, and the Swagger/OpenAPI docs.
- **Authorization:** an authenticated user also needs the endpoint's permission (granted through their roles) and, for project-scoped resources, an active membership in that project.

## Request / Response Conventions

**Envelope.** Every response uses the same envelope. Success responses carry `data`; error responses carry `error`; the two are never present together. The HTTP status is the authoritative success indicator, so the body has no `success` flag.

```json
{
  "message": "Project fetched successfully",
  "data": { "id": 42, "code": "PRJ-2026-001", "name": "North Tower", "status": "IN_PROGRESS" },
  "timestamp": "2026-10-03T17:00:00Z",
  "traceId": "8f2a0c4e-6b1d-4a7e-9c3f-2d5e8a1b7c90"
}
```

**Tracing.** Every response includes a `traceId`, also returned in the `X-Trace-Id` header and attached to server logs, so a client-reported error can be matched to its log lines.

**Pagination.** List endpoints use one-based offset pagination with `page` and `size` query parameters (maximum page size 100), plus resource-specific filters.

```
GET /v2/api/budgets/7/expenses?status=PENDING_APPROVAL&page=1&size=20

{
  "message": "Expenses fetched successfully",
  "data": {
    "items": [ ... ],
    "page": 1,
    "size": 20,
    "totalItems": 57,
    "totalPages": 3
  },
  "timestamp": "2026-10-03T17:00:00Z",
  "traceId": "..."
}
```

**Rate limiting.** Requests are throttled by a token bucket. Allowed responses include `X-Rate-Limit-Remaining`; throttled requests receive `429 Too Many Requests` with `X-Rate-Limit-Retry-After-Seconds`.

## Endpoints

All paths below are relative to `/v2/api` unless noted. "Session" means any authenticated user; other values are the required permission.

**Root and operational** (outside `/v2/api`)

| Method | Path | Description | Auth |
|---|---|---|---|
| GET | `/` | API landing information and links | none |
| GET | `/actuator/health` | Application health | none |
| GET | `/actuator/info` | Build information | none |

**Authentication and invitations**

| Method | Path | Description | Auth |
|---|---|---|---|
| GET | `/auth/csrf` | Issue a CSRF token cookie | none |
| POST | `/auth/login` | Authenticate and start a session | none |
| POST | `/auth/logout` | Invalidate the current session | session |
| GET | `/auth/me` | Current authenticated user context | session |
| POST | `/invitations` | Invite a user and email them a single-use link | `USER_INVITE` |
| POST | `/invitations/{token}/accept` | Accept an invitation, set credentials, activate the account | invitation token |

**Users**

| Method | Path | Description | Auth |
|---|---|---|---|
| GET | `/users` | List users | `USER_READ` |
| GET | `/users/{userId}` | Get a user's administrative profile | `USER_READ` |
| PATCH | `/users/{userId}` | Update administrative user data | `USER_UPDATE` |
| PATCH | `/users/{userId}/roles` | Replace a user's global roles | `USER_ROLE_MANAGE` |
| PATCH | `/users/{userId}/status` | Suspend, reactivate, or disable an account | `USER_STATUS_MANAGE` |
| GET | `/users/me` | Get my profile | session |
| PATCH | `/users/me` | Update my editable profile fields | session |

**Projects**

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/projects` | Create a project | `PROJECT_CREATE` |
| GET | `/projects` | List and filter accessible projects | `PROJECT_READ` |
| GET | `/projects/my-projects` | List projects I'm an active member of | `PROJECT_READ` |
| GET | `/projects/{projectId}` | Get a project | `PROJECT_READ` |
| GET | `/projects/code/{code}` | Get a project by business code | `PROJECT_READ` |
| PATCH | `/projects/{projectId}` | Update general project information | `PROJECT_UPDATE` |
| PATCH | `/projects/{projectId}/status` | Change project status (including cancellation) | `PROJECT_CHANGE_STATUS` |
| POST | `/projects/{projectId}/restore` | Restore a cancelled project | `PROJECT_RESTORE` |
| GET | `/projects/{projectId}/summary` | Project-level summary | `PROJECT_READ` |
| GET | `/projects/summary` | Organization-wide project summary | `PROJECT_GLOBAL_SUMMARY` |

**Project phases**

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/projects/{projectId}/phases` | Create a phase | `PHASE_CREATE` |
| GET | `/projects/{projectId}/phases` | List phases | `PHASE_READ` |
| GET | `/projects/{projectId}/phases/{phaseId}` | Get a phase | `PHASE_READ` |
| PATCH | `/projects/{projectId}/phases/{phaseId}` | Update phase data | `PHASE_UPDATE` |
| PATCH | `/projects/{projectId}/phases/{phaseId}/status` | Change phase status | `PHASE_CHANGE_STATUS` |
| PATCH | `/projects/{projectId}/phases/reorder` | Reorder phases | `PHASE_REORDER` |

**Project members**

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/projects/{projectId}/members` | Add a member | `MEMBER_MANAGE` |
| GET | `/projects/{projectId}/members` | List members | `MEMBER_READ` |
| GET | `/projects/{projectId}/members/{userId}` | Get a membership | `MEMBER_READ` |
| PATCH | `/projects/{projectId}/members/{userId}/status` | Activate or deactivate a membership | `MEMBER_MANAGE` |

**Evidence and attachments**

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/projects/{projectId}/phases/{phaseId}/evidence` | Create evidence for a phase | `EVIDENCE_CREATE` |
| GET | `/projects/{projectId}/phases/{phaseId}/evidence` | List phase evidence | `EVIDENCE_READ` |
| POST | `/expenses/{expenseId}/evidence` | Create evidence for an expense | `EVIDENCE_CREATE` |
| GET | `/expenses/{expenseId}/evidence` | List expense evidence | `EVIDENCE_READ` |
| GET | `/evidence/{evidenceId}` | Get evidence | `EVIDENCE_READ` |
| PATCH | `/evidence/{evidenceId}` | Update evidence metadata | `EVIDENCE_UPDATE` |
| DELETE | `/evidence/{evidenceId}` | Remove evidence | `EVIDENCE_DELETE` |
| POST | `/evidence/{evidenceId}/attachments` | Upload a file (JPEG, PNG, or PDF, max 10 MB) | `ATTACHMENT_CREATE` |
| GET | `/evidence/{evidenceId}/attachments` | List attachment metadata | `ATTACHMENT_READ` |
| DELETE | `/attachments/{attachmentId}` | Remove an attachment | `ATTACHMENT_DELETE` |

**Budgets and budget items**

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/projects/{projectId}/budget` | Create the project's budget | `BUDGET_CREATE` |
| GET | `/projects/{projectId}/budget` | Get the project's budget | `BUDGET_READ` |
| GET | `/budgets/{budgetId}` | Get a budget | `BUDGET_READ` |
| PATCH | `/budgets/{budgetId}` | Update budget data | `BUDGET_UPDATE` |
| POST | `/budgets/{budgetId}/approve` | Approve a draft budget | `BUDGET_APPROVE` |
| POST | `/budgets/{budgetId}/revise` | Revise an approved budget in place | `BUDGET_REVISE` |
| POST | `/budgets/{budgetId}/close` | Close a budget (read-only) | `BUDGET_CLOSE` |
| GET | `/budgets/{budgetId}/summary` | Planned, executed, remaining, and variance | `BUDGET_READ` |
| POST | `/budgets/{budgetId}/items` | Create a budget item | `BUDGET_ITEM_CREATE` |
| GET | `/budgets/{budgetId}/items` | List and filter budget items | `BUDGET_ITEM_READ` |
| GET | `/budgets/{budgetId}/items/{itemId}` | Get a budget item | `BUDGET_ITEM_READ` |
| PATCH | `/budgets/{budgetId}/items/{itemId}` | Update a budget item | `BUDGET_ITEM_UPDATE` |
| DELETE | `/budgets/{budgetId}/items/{itemId}` | Remove an unused budget item | `BUDGET_ITEM_DELETE` |

**Expenses**

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/budgets/{budgetId}/expenses` | Create a draft expense | `EXPENSE_CREATE` |
| GET | `/budgets/{budgetId}/expenses` | List and filter expenses | `EXPENSE_READ` |
| GET | `/expenses/{expenseId}` | Get an expense | `EXPENSE_READ` |
| PATCH | `/expenses/{expenseId}` | Update a draft expense | `EXPENSE_UPDATE` |
| POST | `/expenses/{expenseId}/submit` | Submit for approval | `EXPENSE_SUBMIT` |
| POST | `/expenses/{expenseId}/approve` | Approve a submitted expense | `EXPENSE_APPROVE` |
| POST | `/expenses/{expenseId}/reject` | Reject a submitted expense | `EXPENSE_REJECT` |
| POST | `/expenses/{expenseId}/return-to-draft` | Return a rejected expense to draft | `EXPENSE_CORRECT` |

**Inventory**

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/inventory/items` | Create an item | `INVENTORY_ITEM_CREATE` |
| GET | `/inventory/items` | List and filter items | `INVENTORY_ITEM_READ` |
| GET | `/inventory/items/{itemId}` | Get an item | `INVENTORY_ITEM_READ` |
| PATCH | `/inventory/items/{itemId}` | Update an item | `INVENTORY_ITEM_UPDATE` |
| PATCH | `/inventory/items/{itemId}/status` | Activate or deactivate an item | `INVENTORY_ITEM_STATUS_MANAGE` |
| GET | `/inventory/items/{itemId}/balance` | Item stock by location | `INVENTORY_BALANCE_READ` |
| POST | `/inventory/locations` | Create a warehouse or project site | `INVENTORY_LOCATION_CREATE` |
| GET | `/inventory/locations` | List and filter locations | `INVENTORY_LOCATION_READ` |
| GET | `/inventory/locations/{locationId}` | Get a location | `INVENTORY_LOCATION_READ` |
| PATCH | `/inventory/locations/{locationId}` | Update a location | `INVENTORY_LOCATION_UPDATE` |
| PATCH | `/inventory/locations/{locationId}/status` | Activate or deactivate a location | `INVENTORY_LOCATION_STATUS_MANAGE` |
| GET | `/inventory/locations/{locationId}/balance` | Stock held at a location | `INVENTORY_BALANCE_READ` |
| POST | `/inventory/movements` | Create a draft receipt, issue, transfer, adjustment, or return | `INVENTORY_MOVEMENT_CREATE` |
| GET | `/inventory/movements` | List and filter movements | `INVENTORY_MOVEMENT_READ` |
| GET | `/inventory/movements/{movementId}` | Get a movement with its lines | `INVENTORY_MOVEMENT_READ` |
| PATCH | `/inventory/movements/{movementId}` | Update a draft movement | `INVENTORY_MOVEMENT_UPDATE` |
| POST | `/inventory/movements/{movementId}/post` | Post a movement and apply its stock effect | `INVENTORY_MOVEMENT_POST` |
| POST | `/inventory/movements/{movementId}/reverse` | Create a reversing movement | `INVENTORY_MOVEMENT_REVERSE` |

**Notifications**

| Method | Path | Description | Auth |
|---|---|---|---|
| GET | `/notifications` | List my notifications (newest first) | session |
| GET | `/notifications/{notificationId}` | Get one of my notifications | session |
| PATCH | `/notifications/{notificationId}/read` | Mark as read (idempotent) | session |
| PATCH | `/notifications/read-all` | Mark all mine as read | session |

## Error Handling

Errors use the same envelope with an `error` object. `error_type` is the broad category, `code` is a stable machine-readable value, and `details` carries field-level information when relevant:

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

| Category | Status | Typical cause |
|---|---|---|
| `VALIDATION` | 400 | Invalid or malformed request body or parameters |
| `UNAUTHENTICATED` | 401 | No valid session, or invalid credentials |
| `FORBIDDEN` | 403 | Missing permission, or not an active member of the project |
| `NOT_FOUND` | 404 | Resource doesn't exist, or belongs to another user (notifications, to avoid disclosure) |
| `CONFLICT` | 409 | Duplicate business identifier (project code, item code, email), or a change blocked by existing data, such as deleting a budget item that already has expenses |
| `BUSINESS_RULE` | 422 | Rejected state transition or rule violation, such as insufficient stock or editing a posted movement |
| `UNKNOWN` | 500 | Unexpected server failure; no internal details are exposed |

Error responses never include stack traces, exception types, SQL messages, rejected values, or credentials.
