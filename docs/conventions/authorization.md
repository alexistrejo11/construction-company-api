# Authorization

## Status

This document defines the initial authorization design. The initial matrix is intentionally restrictive while the operational use cases are still being implemented.

## Authorization Model

Authorization combines three concepts:

```text
Global role:
  the user's organizational responsibility

Permission:
  a concrete action the user may perform

Project membership:
  the project scope in which a project-scoped permission may apply
```

A normal project operation requires:

```text
authenticated user
    + required permission from a global role
    + active membership in the target project
```

Global administrative permissions may bypass project membership only when that behavior is explicitly defined for the permission.

## Permission Representation

Permissions should be represented by a typed enum rather than string literals spread through controllers and handlers:

```java
public enum Permission {
    PROJECT_READ,
    PROJECT_UPDATE,
    PHASE_CREATE,
    PHASE_UPDATE,
    PHASE_CHANGE_STATUS,
    MEMBER_MANAGE,
    BUDGET_UPDATE,
    EXPENSE_CREATE,
    EXPENSE_APPROVE
}
```

The list is illustrative and must be derived from confirmed use cases. A permission represents an action, not a person or job title.

## Role Permission Catalog

The mapping from global roles to permissions should live in one focused catalog rather than in controllers or individual handlers:

```java
public final class RolePermissionCatalog {
    public static Set<Permission> permissionsFor(UserRole role) {
        // Return the immutable permission set for this role.
    }
}
```

The catalog combines permissions from all roles assigned to a user. Permission sets should be immutable and should not be changed during request processing.

An enum-backed `EnumMap<UserRole, EnumSet<Permission>>` or equivalent typed mapping is preferred initially. A database-backed permission model is not required until permissions must be managed dynamically by administrators.

## Initial Matrix

- `COMPANY_ADMIN` has every permission defined for currently implemented endpoints.
- `PROJECT_MANAGER`, `SITE_ENGINEER`, `QUANTITY_SURVEYOR`, `FINANCE_OFFICER`, and `CONTRACTOR` have no permissions until their operational use cases and grants are explicitly approved.
- `COMPANY_ADMIN` bypasses active project membership for every currently implemented project, phase, and member permission. The bypass is enumerated in the project authorization policy; it is not an implicit future grant.
- When a non-administrator receives `PROJECT_READ`, project listings must contain only projects where that user has active membership.

The initial permission set is:

```text
USER_INVITE, USER_READ, USER_UPDATE, USER_ROLE_MANAGE, USER_STATUS_MANAGE,
PROJECT_CREATE, PROJECT_READ, PROJECT_UPDATE, PROJECT_CHANGE_STATUS,
PROJECT_RESTORE, PROJECT_GLOBAL_SUMMARY,
PHASE_CREATE, PHASE_READ, PHASE_UPDATE, PHASE_CHANGE_STATUS, PHASE_REORDER,
MEMBER_MANAGE, MEMBER_READ,
EVIDENCE_CREATE, EVIDENCE_READ, EVIDENCE_UPDATE, EVIDENCE_DELETE,
ATTACHMENT_CREATE, ATTACHMENT_READ, ATTACHMENT_DELETE,
BUDGET_CREATE, BUDGET_READ, BUDGET_UPDATE, BUDGET_APPROVE, BUDGET_REVISE, BUDGET_CLOSE,
BUDGET_ITEM_CREATE, BUDGET_ITEM_READ, BUDGET_ITEM_UPDATE, BUDGET_ITEM_DELETE,
EXPENSE_CREATE, EXPENSE_READ, EXPENSE_UPDATE, EXPENSE_SUBMIT, EXPENSE_APPROVE,
EXPENSE_REJECT, EXPENSE_CORRECT,
INVENTORY_ITEM_CREATE, INVENTORY_ITEM_READ, INVENTORY_ITEM_UPDATE,
INVENTORY_ITEM_STATUS_MANAGE, INVENTORY_LOCATION_CREATE,
INVENTORY_LOCATION_READ, INVENTORY_LOCATION_UPDATE,
INVENTORY_LOCATION_STATUS_MANAGE, INVENTORY_BALANCE_READ,
INVENTORY_MOVEMENT_CREATE, INVENTORY_MOVEMENT_READ,
INVENTORY_MOVEMENT_UPDATE, INVENTORY_MOVEMENT_POST,
INVENTORY_MOVEMENT_REVERSE
```

Inventory permissions are global in Phase 9. They do not require active
project membership, even when a location references a project. The project
reference is validated for existence only. `COMPANY_ADMIN` does not need a
project-membership bypass for inventory because these permissions are global.

## Authorization Policy

Authorization should be exposed through focused application policies, not a generic helper imported everywhere:

```java
public interface ProjectAuthorizationPolicy {
    Result<Void> requirePermission(
        UserContext user,
        Long projectId,
        Permission permission
    );
}
```

The policy coordinates global permission checks and active project membership checks. It may use a project-member repository, but it must not read `SecurityContextHolder` directly.

Handlers invoke the policy as part of their use-case flow. Controllers do not implement resource authorization, and domain entities do not resolve the current user.

## Check Versus Require

Use a boolean or predicate-style method only for a pure in-memory permission check:

```java
boolean hasPermission(UserContext user, Permission permission)
```

Use a result-based method when authorization is part of a use case and may return an expected denial:

```java
Result<Void> requirePermission(
    UserContext user,
    Long projectId,
    Permission permission
)
```

Expected authorization failures should return the application's forbidden result rather than throw an exception during ordinary handler execution.

## Design Pattern Guidance

A Strategy pattern is not required for the initial implementation. Role-to-permission lookup is data, not a family of interchangeable algorithms; a typed catalog is simpler and easier to inspect.

Introduce strategies or composable authorization specifications only when authorization rules genuinely require different algorithms, external policy providers, tenant-specific behavior, or complex reusable combinations. Do not create one strategy class per role by default.

Avoid generic methods such as `hasPermissions` that hide resource scope, membership, or the reason for denial. Prefer focused names such as `requireProjectPermission` or `requireActiveProjectMember`.

## Boundary Rules

- Spring Security establishes authentication and handles coarse route protection.
- Authorization policies evaluate application and resource-level permissions.
- Handlers orchestrate authorization policies for their use cases.
- Controllers do not contain role switches or membership queries.
- Domain entities enforce business invariants but do not decide who the current user is.
- Repositories provide membership and resource data but do not own authorization workflows.
