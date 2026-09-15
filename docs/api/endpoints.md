# API Endpoints

## Status

This is the single endpoint catalog for the target API. It is a planning contract: the current Java controllers may not implement every endpoint yet.

## Conventions

- Base path: `/v2/api`.
- `PATCH` is used for partial resource updates.
- State changes use explicit action endpoints when the operation represents a business transition.
- Permissions listed below are implemented permissions for the current endpoint set.
- Project-scoped operations require an active project membership unless an explicit global permission bypass applies.
- Success and error response envelopes follow `docs/conventions/error-handling.md`.
- Commands, queries, request DTOs, and response DTOs follow `docs/conventions/validation.md` and `docs/architecture.md`.

## Authentication and Invitations

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/auth/login` | Public | Authenticate an active user and establish a session. | `200 OK` |
| POST | `/auth/logout` | Authenticated | Invalidate the current session. | `204 No Content` |
| GET | `/auth/me` | Authenticated | Return the current authenticated user context. | `200 OK` |
| POST | `/invitations` | `USER_INVITE` | Create and send a user invitation. | `201 Created` |
| POST | `/invitations/{token}/accept` | Invitation token | Accept an invitation, create credentials, and activate the account. | `200 OK` |
| POST | `/invitations/{invitationId}/resend` | `USER_INVITE` | Resend an unused invitation. | `204 No Content` |
| DELETE | `/invitations/{invitationId}` | `USER_INVITE` | Cancel an unused invitation. | `204 No Content` |

There is no public signup endpoint in the target API.

## Users

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| GET | `/users` | `USER_READ` | List users. | `200 OK` |
| GET | `/users/{userId}` | `USER_READ` | Get a user's administrative profile. | `200 OK` |
| PATCH | `/users/{userId}` | `USER_UPDATE` | Update administrative user data. | `200 OK` |
| PATCH | `/users/{userId}/roles` | `USER_ROLE_MANAGE` | Assign or replace global roles. | `200 OK` |
| PATCH | `/users/{userId}/status` | `USER_STATUS_MANAGE` | Suspend, reactivate, or disable an account. | `200 OK` |
| GET | `/users/me` | Authenticated | Get the current user's profile. | `200 OK` |
| PATCH | `/users/me` | Authenticated | Update the current user's editable profile fields. | `200 OK` |

## Projects

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/projects` | `PROJECT_CREATE` | Create a project. | `201 Created` |
| GET | `/projects` | `PROJECT_READ` | List and filter projects available to the user. | `200 OK` |
| GET | `/projects/my-projects` | `PROJECT_READ` | List projects where the current user has active membership. | `200 OK` |
| GET | `/projects/{projectId}` | `PROJECT_READ` | Get a project by identifier. | `200 OK` |
| GET | `/projects/code/{code}` | `PROJECT_READ` | Get a project by business code. | `200 OK` |
| PATCH | `/projects/{projectId}` | `PROJECT_UPDATE` | Update general project information. | `200 OK` |
| PATCH | `/projects/{projectId}/status` | `PROJECT_CHANGE_STATUS` | Change project status. | `200 OK` |
| POST | `/projects/{projectId}/restore` | `PROJECT_RESTORE` | Restore a cancelled project through an explicit administrative operation. | `200 OK` |
| GET | `/projects/{projectId}/summary` | `PROJECT_READ` | Get project-level summary data. | `200 OK` |
| GET | `/projects/summary` | `PROJECT_GLOBAL_SUMMARY` | Get an organization-level project summary. | `200 OK` |

Cancellation is performed through the status transition endpoint. There is no generic project delete endpoint in the target contract.

## Project Phases

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/projects/{projectId}/phases` | `PHASE_CREATE` | Create a phase in a project. | `201 Created` |
| GET | `/projects/{projectId}/phases` | `PHASE_READ` | List project phases. | `200 OK` |
| GET | `/projects/{projectId}/phases/{phaseId}` | `PHASE_READ` | Get a phase. | `200 OK` |
| PATCH | `/projects/{projectId}/phases/{phaseId}` | `PHASE_UPDATE` | Update general phase data. | `200 OK` |
| PATCH | `/projects/{projectId}/phases/{phaseId}/status` | `PHASE_CHANGE_STATUS` | Change phase status. | `200 OK` |
| PATCH | `/projects/{projectId}/phases/reorder` | `PHASE_REORDER` | Reorder phases in a project. | `200 OK` |

There is no phase reactivation endpoint while cancelled phases remain terminal in the initial model.

## Project Members

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/projects/{projectId}/members` | `MEMBER_MANAGE` | Add a user as an active project member. | `201 Created` |
| GET | `/projects/{projectId}/members` | `MEMBER_READ` | List project members. | `200 OK` |
| GET | `/projects/{projectId}/members/{userId}` | `MEMBER_READ` | Get a membership. | `200 OK` |
| PATCH | `/projects/{projectId}/members/{userId}/status` | `MEMBER_MANAGE` | Activate or deactivate a membership while preserving history. | `200 OK` |

There is no project-role update endpoint. Project membership provides scope; global roles provide capabilities.

## Evidence and Attachments

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/projects/{projectId}/phases/{phaseId}/evidence` | `EVIDENCE_CREATE` | Create evidence for a phase. | `201 Created` |
| GET | `/projects/{projectId}/phases/{phaseId}/evidence` | `EVIDENCE_READ` | List phase evidence. | `200 OK` |
| POST | `/expenses/{expenseId}/evidence` | `EVIDENCE_CREATE` | Create evidence for an expense. | `201 Created` |
| GET | `/expenses/{expenseId}/evidence` | `EVIDENCE_READ` | List expense evidence. | `200 OK` |
| GET | `/evidence/{evidenceId}` | `EVIDENCE_READ` | Get evidence details. | `200 OK` |
| PATCH | `/evidence/{evidenceId}` | `EVIDENCE_UPDATE` | Update evidence metadata. | `200 OK` |
| DELETE | `/evidence/{evidenceId}` | `EVIDENCE_DELETE` | Remove evidence. | `204 No Content` |
| POST | `/evidence/{evidenceId}/attachments` | `ATTACHMENT_CREATE` | Upload an attachment to evidence. | `201 Created` |
| GET | `/evidence/{evidenceId}/attachments` | `ATTACHMENT_READ` | List attachment metadata. | `200 OK` |
| DELETE | `/attachments/{attachmentId}` | `ATTACHMENT_DELETE` | Remove an attachment. | `204 No Content` |

Evidence is mutable and does not directly change the lifecycle of its target resource.

Phase 7 supports evidence for project phases. Phase 7.5 adds the explicit expense association and expense evidence endpoints; full budget-linked expense workflows remain Phase 8.

## Budgets

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/projects/{projectId}/budget` | `BUDGET_CREATE` | Create the operational budget for a project. | `201 Created` |
| GET | `/projects/{projectId}/budget` | `BUDGET_READ` | Get the budget associated with a project. | `200 OK` |
| GET | `/budgets/{budgetId}` | `BUDGET_READ` | Get a budget by identifier. | `200 OK` |
| PATCH | `/budgets/{budgetId}` | `BUDGET_UPDATE` | Update allowed budget data. | `200 OK` |
| POST | `/budgets/{budgetId}/approve` | `BUDGET_APPROVE` | Approve a draft budget. | `200 OK` |
| POST | `/budgets/{budgetId}/revise` | `BUDGET_REVISE` | Revise an approved budget without creating a new version. | `200 OK` |
| POST | `/budgets/{budgetId}/close` | `BUDGET_CLOSE` | Close a budget and make it read-only for ordinary operations. | `200 OK` |
| GET | `/budgets/{budgetId}/summary` | `BUDGET_READ` | Get planned, executed, balance, and variance values. | `200 OK` |

The initial model uses one revisable budget per project. Formal budget versioning is deferred.

## Budget Items

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/budgets/{budgetId}/items` | `BUDGET_ITEM_CREATE` | Create a budget item. | `201 Created` |
| GET | `/budgets/{budgetId}/items` | `BUDGET_ITEM_READ` | List budget items with optional `search`, `category`, `page`, and `size` filters. | `200 OK` |
| GET | `/budgets/{budgetId}/items/{itemId}` | `BUDGET_ITEM_READ` | Get a budget item. | `200 OK` |
| PATCH | `/budgets/{budgetId}/items/{itemId}` | `BUDGET_ITEM_UPDATE` | Update a budget item when allowed. | `200 OK` |
| DELETE | `/budgets/{budgetId}/items/{itemId}` | `BUDGET_ITEM_DELETE` | Remove an unused budget item when allowed. | `204 No Content` |

## Expenses

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/budgets/{budgetId}/expenses` | `EXPENSE_CREATE` | Create a draft expense against a budget item. | `201 Created` |
| GET | `/budgets/{budgetId}/expenses` | `EXPENSE_READ` | List budget expenses with optional `search`, `status`, `currency`, `page`, and `size` filters. | `200 OK` |
| GET | `/expenses/{expenseId}` | `EXPENSE_READ` | Get an expense. | `200 OK` |
| PATCH | `/expenses/{expenseId}` | `EXPENSE_UPDATE` | Update an expense while it is draft. | `200 OK` |
| POST | `/expenses/{expenseId}/submit` | `EXPENSE_SUBMIT` | Submit a draft expense for review. | `200 OK` |
| POST | `/expenses/{expenseId}/approve` | `EXPENSE_APPROVE` | Approve a submitted expense. | `200 OK` |
| POST | `/expenses/{expenseId}/reject` | `EXPENSE_REJECT` | Reject a submitted expense. | `200 OK` |
| POST | `/expenses/{expenseId}/return-to-draft` | `EXPENSE_CORRECT` | Return a rejected expense to draft for correction. | `200 OK` |

Only approved expenses count toward executed budget amounts.

## Contractors and Suppliers

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/contractors` | `CONTRACTOR_CREATE` | Create a contractor or supplier. | `201 Created` |
| GET | `/contractors` | `CONTRACTOR_READ` | List and filter contractors or suppliers. | `200 OK` |
| GET | `/contractors/{contractorId}` | `CONTRACTOR_READ` | Get a contractor or supplier. | `200 OK` |
| PATCH | `/contractors/{contractorId}` | `CONTRACTOR_UPDATE` | Update contractor or supplier data. | `200 OK` |
| PATCH | `/contractors/{contractorId}/status` | `CONTRACTOR_STATUS_MANAGE` | Activate or deactivate a contractor or supplier. | `200 OK` |
| GET | `/contractors/{contractorId}/projects` | `CONTRACTOR_READ` | List projects related to a contractor or supplier. | `200 OK` |

## Inventory Items

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/inventory/items` | `INVENTORY_ITEM_CREATE` | Create an inventory item. | `201 Created` |
| GET | `/inventory/items` | `INVENTORY_ITEM_READ` | List and filter inventory items. | `200 OK` |
| GET | `/inventory/items/{itemId}` | `INVENTORY_ITEM_READ` | Get an inventory item. | `200 OK` |
| PATCH | `/inventory/items/{itemId}` | `INVENTORY_ITEM_UPDATE` | Update inventory item data. | `200 OK` |
| PATCH | `/inventory/items/{itemId}/status` | `INVENTORY_ITEM_STATUS_MANAGE` | Activate or deactivate an inventory item. | `200 OK` |
| GET | `/inventory/items/{itemId}/balance` | `INVENTORY_BALANCE_READ` | Get item balance by location. | `200 OK` |

Inventory item list filters are `search`, `category`, `trackingMode`, and
`active`, with optional one-based `page`, `size`, and `sort` parameters. Item
balance responses contain item identity and balances grouped by location.

## Inventory Locations

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/inventory/locations` | `INVENTORY_LOCATION_CREATE` | Create a warehouse or project site. | `201 Created` |
| GET | `/inventory/locations` | `INVENTORY_LOCATION_READ` | List inventory locations. | `200 OK` |
| GET | `/inventory/locations/{locationId}` | `INVENTORY_LOCATION_READ` | Get an inventory location. | `200 OK` |
| PATCH | `/inventory/locations/{locationId}` | `INVENTORY_LOCATION_UPDATE` | Update location data. | `200 OK` |
| PATCH | `/inventory/locations/{locationId}/status` | `INVENTORY_LOCATION_STATUS_MANAGE` | Activate or deactivate a location. | `200 OK` |
| GET | `/inventory/locations/{locationId}/balance` | `INVENTORY_BALANCE_READ` | Get stock held at a location. | `200 OK` |

Inventory location list filters are `search`, `type`, `projectId`, and
`active`, with optional one-based `page`, `size`, and `sort` parameters.
Location balance responses contain location identity and balances grouped by
item.

## Inventory Movements

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| POST | `/inventory/movements` | `INVENTORY_MOVEMENT_CREATE` | Create a draft receipt, issue, transfer, adjustment, or return. | `201 Created` |
| GET | `/inventory/movements` | `INVENTORY_MOVEMENT_READ` | List and filter movements. | `200 OK` |
| GET | `/inventory/movements/{movementId}` | `INVENTORY_MOVEMENT_READ` | Get movement details and lines. | `200 OK` |
| PATCH | `/inventory/movements/{movementId}` | `INVENTORY_MOVEMENT_UPDATE` | Update a draft movement. | `200 OK` |
| POST | `/inventory/movements/{movementId}/post` | `INVENTORY_MOVEMENT_POST` | Post a draft movement and apply its stock effect. | `200 OK` |
| POST | `/inventory/movements/{movementId}/reverse` | `INVENTORY_MOVEMENT_REVERSE` | Create a reversing movement for a posted movement. | `201 Created` |

Posted movements are immutable. The reverse endpoint is the correction mechanism rather than an update or delete operation.

Movement list filters are `type`, `status`, `sourceLocationId`,
`targetLocationId`, `itemId`, `from`, `to`, `page`, `size`, and `sort`.
Movement detail includes its lines. `RECEIPT` uses a target location, `ISSUE`
and `RETURN` use a source location, `TRANSFER` uses different source and
target locations, and `ADJUSTMENT` uses a location plus `INCREASE` or
`DECREASE`. All quantities are positive. A serialized line contains exactly
one serial and quantity `1`.

## Notifications

| Method | Path | Permission | Description | Success |
|---|---|---|---|---|
| GET | `/notifications` | Authenticated | List notifications for the current user. | `200 OK` |
| GET | `/notifications/{notificationId}` | Authenticated | Get a notification belonging to the current user. | `200 OK` |
| PATCH | `/notifications/{notificationId}/read` | Authenticated | Mark a notification as read. | `200 OK` |
| PATCH | `/notifications/read-all` | Authenticated | Mark all current-user notifications as read. | `200 OK` |

There is no public notification creation endpoint. Notifications are created by application workflows and business events.

`GET /notifications` supports optional `read`, one-based `page`, `size`, and
`sort` parameters and returns newest notifications first by default. The
response is a `PageResponse` containing notification type, title, message,
optional `resourceType`/`resourceId`, `readAt`, and creation metadata.
`PATCH /notifications/{notificationId}/read` is idempotent. The read-all
response contains the number of notifications newly marked read. A notification
owned by another user is returned as not found to avoid resource disclosure.

## Deferred Features

- Generic approval workflow endpoints are excluded from the current API.
- Budget version endpoints are excluded; the initial model revises one budget in place.
- Public signup endpoints are excluded; users enter through invitations.
