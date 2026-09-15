# Domain Entities

This catalog lists candidate business entities. It is an inventory, not a finalized schema.

For each entity, the following sections will be completed before implementation decisions are finalized:

- Purpose and business responsibility.
- Identity.
- Lifecycle and states.
- Relationships and ownership.
- Invariants.
- Main use cases.
- Access rules.

## Core Candidates

### User

- Purpose: Represents an authenticated person or account in the company system.
- Identity: A persistent user identity with a unique email address.
- Lifecycle: `INVITED`, `ACTIVE`, `SUSPENDED`, or `DISABLED`.
- Relationships: May have multiple global roles, project memberships, invitations, and notifications.
- Invariants: Only active users may authenticate; users cannot assign their own roles.
- Use cases: Accept invitation, authenticate, update profile, suspend, disable, and manage roles through authorized administration.
- Access rules: Derived from account status, global roles, permissions, and project memberships.

### Invitation

- Purpose: Onboard a user into the private business system without public self-registration.
- Identity: A single-use, expiring invitation identity and token.
- Lifecycle: To be defined; it must support pending, accepted, and expired outcomes.
- Relationships: Targets an email address and assigns initial global role(s) before user activation.
- Invariants: An invitation cannot be accepted more than once or after expiration.
- Use cases: Create, send, accept, resend, expire, and cancel.
- Access rules: Creation is restricted to authorized users; acceptance requires possession of the invitation token.

### Project

- Purpose: Represents a construction project managed by the company.
- Identity: A persistent project identity and unique business code.
- Lifecycle: `PLANNING`, `IN_PROGRESS`, `ON_HOLD`, `COMPLETED`, or `CANCELLED`.
- Relationships: Owns or references phases, members, budget, expenses, and related project resources.
- Invariants: A project cannot move out of a terminal state through an ordinary transition; financial and date values must remain valid.
- Use cases: Create, view, update general information, change status, restore when supported, and manage related resources.
- Access rules: To be derived from global permissions and project scope.

### Site Location

- Purpose: Represents the physical location embedded in a project.
- Identity: No independent identity; its identity is the owning project.
- Lifecycle: Created and updated as part of the project.
- Relationships: Belongs to one project.
- Invariants: Location fields and coordinates must satisfy structural and domain validation when provided.
- Use cases: Read and update through project operations.
- Access rules: Inherits project access.

### Project Phase

- Purpose: Represents a major execution stage within a project.
- Identity: A persistent phase identity within one project.
- Lifecycle: Initially proposed as `PLANNED`, `IN_PROGRESS`, `ON_HOLD`, `COMPLETED`, or `CANCELLED`.
- Relationships: Belongs to one project and may contain evidence and budget items.
- Invariants: A phase cannot move out of a terminal state through an ordinary transition; dates, order, and allocated amounts must remain valid.
- Use cases: Create, update, reorder, change status, view, and attach lightweight evidence.
- Access rules: Requires project scope and a permission appropriate to the operation.

### Evidence

- Purpose: Reusable evidence record that provides optional supporting context for phase progress or another business operation.
- Identity: A persistent evidence identity.
- Lifecycle: Created and managed as a supporting record; detailed lifecycle is pending.
- Relationships: Belongs to one supported target (`ProjectPhase` or `Expense`) and one author, and may contain attachments. Phase 7.5 adds the expense target; full expense behavior remains in Phase 8.
- Invariants: Titles, descriptions, timestamps, and associations must be valid when provided.
- Use cases: Add, list, view, and remove evidence.
- Access rules: Inherits the scope of its associated resource.

### Attachment

- Purpose: Reusable technical file resource associated with evidence.
- Identity: A persistent attachment identity and storage reference.
- Lifecycle: Created, accessed, and removed independently from the target resource's lifecycle.
- Relationships: Belongs to one evidence record.
- Invariants: File metadata and storage reference must be valid.
- Use cases: Upload, list, view metadata, and remove.
- Access rules: Inherits evidence and target-resource scope.

### Project Member

- Purpose: Represents a user's membership and scope within a project.
- Identity: A membership identity or the unique user-project relationship.
- Lifecycle: Initially proposed as active, removed, or otherwise inactive; exact states are pending.
- Relationships: References one user and one project, and may carry assignment metadata.
- Invariants: A user-project relationship is unique and cannot exist without both resources.
- Use cases: Add, list, update membership state, and remove a member.
- Access rules: Managed through global permissions; no project-specific role is required initially.

### Contractor

- Purpose:
- Identity:
- Lifecycle:
- Relationships:
- Invariants:
- Use cases:
- Access rules:

### Budget

- Purpose: Represents the operational financial plan for one project.
- Identity: A persistent budget identity with one budget per project in the initial model.
- Lifecycle: `DRAFT`, `APPROVED`, or `CLOSED`.
- Relationships: Belongs to one project, contains budget items, and provides context for expenses.
- Invariants: Currency and planned amounts remain consistent; closed budgets cannot be changed through ordinary operations.
- Use cases: Create, update, approve, close, summarize, and manage items.
- Access rules: Derived from global permissions and project scope.

### Budget Item

- Purpose: Represents a planned allocation within a budget.
- Identity: A persistent item identity within one budget.
- Lifecycle: Follows the budget lifecycle with item-specific update restrictions.
- Relationships: Belongs to one budget and may reference one project phase.
- Invariants: Quantity, unit price, and planned total remain consistent; planned values cannot be negative.
- Use cases: Add, update, remove when allowed, and inspect.
- Access rules: Inherits budget and project scope.

### Expense

- Purpose: Represents an actual cost incurred against a budget plan.
- Identity: A persistent expense identity.
- Lifecycle: `DRAFT`, `PENDING_APPROVAL`, `APPROVED`, or `REJECTED`.
- Relationships: Belongs to one project and, for Phase 8 workflow expenses, one budget and one budget item; it may contain evidence. The project reference is retained for scoped access and compatibility with the Phase 7.5 target foundation.
- Invariants: Amount and currency are valid; only approved expenses count as executed cost.
- Use cases: Create, update while draft, submit, approve, reject, and inspect.
- Access rules: Derived from global permissions, project scope, and approval policy.

### Approval

- Status: Future optional capability; excluded from the current domain workflow.
- Purpose: Model a separate request, review, and decision for a critical operation if the product later requires it.
- Identity: To be defined if introduced.
- Lifecycle: Proposed as `PENDING`, `APPROVED`, or `REJECTED`.
- Relationships: Would target a business operation or resource through an explicit persistence design.
- Invariants: To be defined if introduced.
- Use cases: Request, approve, reject, and review history.
- Access rules: Would complement, not replace, permission checks.

## Supporting Candidates

### Notification

- Purpose: User-facing record created from an explicit action or business event.
- Identity: A persistent notification identity for one recipient.
- Lifecycle: Unread while `readAt` is null, then read; read is idempotent.
- Relationships: Belongs to one user and may contain a nullable resource type and identifier.
- Invariants: Recipient is valid; notification creation does not mutate the source business resource; users can access only their own records.
- Use cases: List, view, mark as read, and create through application workflows.
- Access rules: A user may access only their own notifications.

### Inventory Item

- Purpose: Represents a catalog item tracked by inventory.
- Identity: Persistent identity with a unique business code.
- Lifecycle: Active or inactive; deactivation preserves history.
- Relationships: Referenced by inventory movement lines.
- Invariants: Category, unit, tracking mode, and name are valid; serialized
  items require one serial per unit and quantity items use configured-unit
  quantities.
- Use cases: Create, list, inspect, update, change status, and inspect balances.
- Access rules: Global inventory permissions; project membership is not required.

### Inventory Location

- Purpose: Represents a warehouse or project site where stock is held.
- Identity: Persistent identity with a unique location code.
- Lifecycle: Active or inactive; deactivation preserves history.
- Relationships: Optionally references one project and is referenced as a
  movement source or target.
- Invariants: Warehouses have no required project; project sites reference an
  existing project; inactive locations cannot be used by new movements.
- Use cases: Create, list, inspect, update, change status, and inspect balances.
- Access rules: Global inventory permissions; project membership is not required.

### Inventory Movement

- Purpose: Represents a stock receipt, issue, transfer, return, or adjustment.
- Identity: Persistent movement identity with immutable posted history.
- Lifecycle: `DRAFT` -> `POSTED`; a posted movement may produce one reversing
  draft and cannot be edited or deleted.
- Relationships: Has one or more movement lines and optional source/target
  locations; each line references one inventory item.
- Invariants: Lines have positive quantities compatible with tracking mode;
  direction-specific locations are valid; posting cannot produce negative stock.
- Use cases: Create, list, inspect, update drafts, post, and reverse posted
  movements.
- Access rules: Global inventory permissions; project membership is not required.

### Inventory Movement Line

- Purpose: Represents one item and quantity/serial effect within a movement.
- Identity: Persistent line identity owned by one movement.
- Lifecycle: Mutable only while its parent movement is a draft.
- Relationships: Belongs to one inventory movement and references one inventory item.
- Invariants: Quantity is positive; serialized lines have quantity one and one
  serial number; a posted line cannot be changed independently.
- Use cases: Create and replace lines while editing a draft, inspect as movement
  detail, and derive stock effects.
- Access rules: Inherits the parent movement's global inventory permissions.

### Audit Log

- Decide whether this is required as a cross-cutting technical record.

### Session

- Treat as an authentication/infrastructure concern unless the product requires session management as a business capability.
