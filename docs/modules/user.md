# User Module

## Responsibility

Manage users, their identity, account lifecycle, and global roles.

## Main Concepts

- User.
- Global role.
- Invitation.

## Account Lifecycle

```text
INVITED -> ACTIVE -> SUSPENDED
                  -> DISABLED
```

Users are created through an invitation flow rather than public self-registration. The invitation carries the email address and initial role assignment. The invited user creates a password and completes basic profile data before the account becomes active.

## Account Rules

- Only `ACTIVE` users may log in.
- Invited users have no effective permissions before activation.
- Users cannot choose or escalate their own global roles.
- Email-domain restrictions are not required initially.
- Suspension or disabling must prevent new authentication and invalidate active access according to the session policy.

## Invitation

An invitation is an onboarding record with an expiring, single-use token. It is an authentication-supporting entity, not a project membership and not a general approval workflow.

Its exact fields and use cases remain to be defined, but the flow must support creation, delivery, acceptance, expiration, and optional resend or cancellation.

## Use Cases

- To be listed and confirmed.

## Access Rules

- To be derived from the domain model and authorization decisions.
