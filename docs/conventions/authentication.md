# Authentication and Authorization

## Status

This document records the target authentication and authorization direction. The current code may not reflect these decisions yet; implementation should be migrated toward this model deliberately.

## Stateful Spring Security Sessions

The application will use Spring Security's stateful session mechanism instead of JWT bearer tokens as its primary authentication mechanism.

Authentication is established by Spring Security before a request reaches a controller. The authenticated principal is stored in Spring Security's `SecurityContext`, and the session maintains authentication between requests.

The application is therefore not designed around stateless JWT authentication. Existing JWT configuration, decoders, encoders, converters, and bearer-token rules are legacy implementation details that must be removed or replaced as part of the authentication migration.

The current code still configures `SessionCreationPolicy.STATELESS` and an OAuth2 resource server with JWT decoding. That configuration does not represent the target architecture and must not be copied into new security code.

The current code also contains `UserRole` and a separate `ProjectRole` enum, while `UserContext` currently stores string role names. These are legacy or transitional shapes; the target model uses one global role model plus permissions and project membership scope.

The exact login and logout endpoints, session repository, cookie settings, and session expiration policy remain implementation decisions. Choosing sessions also requires an explicit CSRF policy; CSRF must not be disabled merely because the application is an API.

## Invitation-Based Registration

The application is a private business system and will not provide public self-registration as its primary account-creation flow. New users enter through an invitation created by an authorized administrator or business user.

The target flow is:

```text
authorized user
    -> creates invitation
    -> assigns initial global role(s)
    -> invitation email is sent
    -> invited user accepts the invitation
    -> user creates a password and completes basic profile data
    -> account becomes ACTIVE
    -> user can log in
```

An invitation proves control of the invited email address and represents the initial organizational authorization. Email-domain validation is not required initially; work email addresses are supported without hardcoding a company domain.

Users must not choose their own global roles during invitation acceptance. Roles are assigned by the inviter or an authorized administrator. A user cannot log in or access normal application resources while the account is only invited.

The invitation flow uses a single-use, expiring invitation token. The token is an onboarding credential and is not a replacement for the authenticated session created after activation.

The application does not need a public signup endpoint in the initial model. A public registration flow may be considered later only as a separate product decision.

## Account Status

The target account lifecycle is intentionally small:

```text
INVITED -> ACTIVE -> SUSPENDED
                  -> DISABLED
```

- `INVITED`: invitation exists but the user has not completed onboarding; login is rejected.
- `ACTIVE`: the user may authenticate and use resources allowed by their roles and memberships.
- `SUSPENDED`: temporary access block; account data and memberships remain available.
- `DISABLED`: account access is permanently or administratively disabled according to policy.

An invited user has no effective application permissions until becoming `ACTIVE`. Profile completion is part of invitation acceptance, not a normal authenticated profile workflow.

## Security Filter Boundary

Spring Security's filter chain is the first security boundary for HTTP requests:

```text
HTTP request
    -> Spring Security filter chain
    -> authentication and SecurityContext
    -> route authorization
    -> controller
    -> handler
```

The filter chain is responsible for:

- Establishing or restoring authentication.
- Rejecting unauthenticated requests.
- Applying coarse route-level authorization rules.
- Handling authentication and access-denied responses at the security boundary.

Controllers must not manually authenticate requests or parse session credentials. They may receive an authenticated user representation through a dedicated presentation adapter.

## User Context

The application may expose an immutable `UserContext` derived from the authenticated Spring Security principal:

```java
public record UserContext(
    Long userId,
    String email,
    Set<UserRole> roles
) {}
```

`UserContext` is an application-level representation of the authenticated actor. It may be passed explicitly to handlers, application services, or focused authorization policies when the use case needs actor information.

The domain model, repositories, and persistence entities must not read `SecurityContextHolder` or depend directly on the HTTP request context. Authentication data should cross into application logic as explicit method input or through a focused application policy.

An argument resolver or similar presentation adapter may support a `@CurrentUser` controller parameter, but that adapter must only translate the authenticated principal into `UserContext`. It must not execute business authorization or load arbitrary domain state.

## Authorization Boundaries

Authorization has two levels:

- Route-level authorization in Spring Security handles broad rules such as whether an authenticated user or a role may access an endpoint family.
- Use-case authorization in handlers or focused policies handles resource-specific rules such as project membership, ownership, status, or permission to modify a particular project.

Resource-specific membership checks must not be forced into URL matcher configuration because they require loading and evaluating domain state.

Handlers remain responsible for coordinating authorization policies, while domain entities protect business invariants. Authorization policies must return the application's expected result type for ordinary access denials rather than using exceptions as normal branching.

## Global Roles and Permissions

The authorization model separates a user's global job role, the action they may perform, and the projects where that action applies:

```text
Role:
  the user's organizational responsibility

Permission:
  a concrete action such as PHASE_CREATE or EXPENSE_APPROVE

Membership:
  the projects on which a project-scoped permission may be applied
```

The target global roles for the construction-company domain are:

```text
COMPANY_ADMIN
PROJECT_MANAGER
SITE_ENGINEER
QUANTITY_SURVEYOR
FINANCE_OFFICER
CONTRACTOR
```

Users may have multiple global roles. Their effective permissions are the union of the permissions granted by those roles. `STAFF` is intentionally not used as a permanent role because it is too broad to communicate authorization intent. `UNKNOWN` must not be persisted as a valid role; unknown role values are invalid data.

Roles represent job responsibilities, not individual operations. Operations such as `PHASE_CREATE`, `EXPENSE_APPROVE`, `BUDGET_UPDATE`, and `MEMBER_MANAGE` are permissions and must not become additional roles unless they represent a genuine organizational responsibility.

Project-specific roles are not duplicated in a second role enum. Project membership provides the resource scope, while global roles and permissions provide the capabilities available within that scope. A non-administrator normally needs both the required global permission and membership in the target project.

Permissions may be global or project-scoped. A global permission, such as managing company users, does not require project membership. A project-scoped permission, such as updating a phase or approving an expense, requires the relevant project membership unless an explicit global bypass applies.

`COMPANY_ADMIN` may bypass project membership for explicitly defined administrative operations. This bypass must be permission-specific rather than an implicit grant for every future endpoint.

The initial model does not assign a different role to the same user for each project. Per-project permission overrides may be introduced later if the domain requires a user to have different capabilities in different projects.

## Pending Decisions

- Define the login and logout flow using Spring Security sessions.
- Define invitation creation, expiration, resend, acceptance, and cancellation behavior.
- Define session storage, cookie attributes, expiration, and invalidation behavior.
- Define the CSRF policy for browser-based session authentication.
- Define the role-to-permission matrix.
- Define which permissions are global and which are project-scoped.
- Define the final authorization policy implementation and whether method security complements handler policies.
- Define the canonical unauthenticated and access-denied response envelope.

The initial authorization design is documented in `docs/conventions/authorization.md`.
