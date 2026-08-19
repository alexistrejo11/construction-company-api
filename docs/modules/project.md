# Project Module

## Responsibility

Define the construction project and its directly owned business capabilities.

## Nested Areas

- `phases/`
- `members/`

## Main Concepts

- Project.
- Project phase.
- Project member.
- Site location as a project-owned value object.
- Optional phase evidence.

## Initial Project Lifecycle Proposal

```text
PLANNING <-> IN_PROGRESS
    |             |
    v             v
CANCELLED      ON_HOLD
                  |
                  v
              IN_PROGRESS

IN_PROGRESS -> COMPLETED
```

`COMPLETED` and `CANCELLED` are terminal states for ordinary status transitions.

### Status Transition Rules

| Current status | Allowed ordinary transitions |
|---|---|
| `PLANNING` | `IN_PROGRESS`, `CANCELLED` |
| `IN_PROGRESS` | `ON_HOLD`, `COMPLETED`, `CANCELLED` |
| `ON_HOLD` | `IN_PROGRESS`, `CANCELLED` |
| `COMPLETED` | None |
| `CANCELLED` | None |

Restoring a cancelled project is a separate administrative operation, not an ordinary status transition. Its target state and required conditions remain to be confirmed.

The lifecycle should remain flexible for normal project administration:

- A project can be created with limited required information and completed later.
- General information such as name, description, location, and planned dates may be updated according to the current status.
- Status changes use explicit domain behavior rather than direct field assignment.
- Starting a project does not require every related resource to be complete.
- Completing a project should normally verify that no phase remains operationally open once phase states are defined.
- An explicit administrative override may allow completion with unresolved work when the business requires it; the override should be a distinct use case and permission rather than a hidden bypass.

## General Update Policy

The project update operation should remain separate from status transitions and related-resource management.

- The project code is a stable business identifier and should not be changed through ordinary updates.
- Descriptive information may be updated while the project is active or on hold, subject to validation.
- Planned dates may be adjusted when doing so does not violate date consistency rules.
- Location may be updated through the project update operation because it is a project-owned value object.
- Budget changes should eventually be managed through the budget capability rather than silently changing project financial state.
- Completed or cancelled projects are read-only for ordinary operational updates.

## Project Phases

Project phases have their own lifecycle and use cases while remaining subordinate to a project. A phase cannot exist without a valid project, but it can be managed independently once the project exists.

Initial phase states:

```text
PLANNED -> IN_PROGRESS -> COMPLETED
    |            |
    v            v
CANCELLED     ON_HOLD -> IN_PROGRESS
```

Phase status changes are explicit domain operations and require an appropriate permission from the authenticated user's global role plus membership in the project. The exact role-to-permission matrix is defined after the domain model is complete.

### Phase Invariants

- A phase belongs to exactly one project.
- A phase name is required and is meaningful within its project.
- Sequence order is managed within the project and must remain unambiguous.
- Allocated budget cannot be negative.
- Planned and actual dates must remain internally consistent.
- A completed or cancelled phase cannot be changed through ordinary operational updates.
- A project may contain phases in different states; the project does not require every phase to be complete before normal work can continue.

## Phase Evidence

The project domain may associate reusable `Evidence` records with phases. A lightweight evidence record may contain:

- Title.
- Description.
- Image URLs.
- Captured or reported timestamp.
- Author or reporting user.

Evidence is supporting information, not an automatic state machine. A phase status transition must not depend on evidence by default. A later business policy may require evidence for specific transitions without coupling the phase status field to the evidence entity.

This keeps status changes as a direct domain operation while allowing the system to provide context for decisions and progress tracking.

## Project Membership

`ProjectMember` remains an entity rather than an array of user IDs because the relationship has its own identity, uniqueness, lifecycle, and assignment metadata. The initial model does not assign a custom role per project. Membership provides project scope; global roles and permissions provide capabilities.

Custom per-project roles or permission overrides are deferred until the business requires different capabilities for the same user in different projects.

### Membership Lifecycle

The initial membership lifecycle is intentionally small:

```text
ACTIVE <-> INACTIVE
```

An inactive membership no longer grants project scope but remains available for historical reference and possible reactivation. Removing a member should normally deactivate the relationship instead of deleting its history.

Membership operations must preserve one relationship per user and project. The membership entity may retain assignment and removal timestamps without carrying a project-specific role.

## Initial Invariants

- Project code is unique.
- Total budget cannot be negative.
- Estimated end date cannot precede the start date when both are provided.
- A completed or cancelled project cannot be changed through ordinary operational updates.
- A project phase or project member cannot exist without a valid project.
- Site location is not an independent aggregate and has no repository or lifecycle of its own.

These are initial proposals and must be refined as the phase, budget, expense, and approval workflows are defined.

## Site Location

`SiteLocation` is currently modeled as an embedded value object. Its fields are updated as part of a project operation and it inherits project authorization. It should become an independent entity only if the business requires its own identity, lifecycle, history, or independent reuse.

## Persistence Note

The current JPA model exposes project-owned phase and member collections with cascading behavior. Whether those relationships remain aggregate-controlled or are persisted through independently managed nested-area repositories is still tracked in `docs/pending/architecture-decisions.md`.

## Use Cases

Initial project use-case candidates:

- Create a project.
- Get a project by identifier or business code.
- List or search projects.
- Update general project information.
- Change project status.
- Cancel a project.
- Restore a cancelled project through an explicit administrative operation.
- Get project summaries.
- Manage project phases and members through their nested areas.
- Record and review optional phase evidence.

## Access Rules

- To be derived from the domain model and authorization decisions.
