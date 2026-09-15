# Notification Module

## Responsibility

Notify users about relevant business events and actions through one or more delivery channels.

Phase 10 initially implements invitation notifications only. Inventory,
expense, project-status, and low-stock producers remain deferred until their
recipient and threshold policies are defined.

## Main Concepts

- Notification.
- Application event.
- Delivery channel.

## Conceptual Separation

The system separates three concepts:

```text
Business event:
  something meaningful happened in the domain

Notification:
  a user-facing message created from an event or explicit request

Delivery channel:
  how the notification reaches the user
```

An event is not an email, and a notification is not the event itself. A single event may create an in-app notification, an email, both, or neither depending on policy.

## Notification Record

The persistent in-app notification is the canonical user-facing record. It
contains:

- Recipient.
- Notification type.
- Title and message.
- Read state or `readAt` timestamp.
- Creation timestamp.
- Optional `resourceType` and `resourceId` reference for client navigation.

The initial notification type is `INVITATION`. A notification belongs to one
persisted recipient user and uses `readAt == null` for unread state. The
recipient need not be active for the record to be persisted, although only
active users can authenticate and access the notification API.

Notifications are created by application workflows, not by arbitrary public clients through a generic create endpoint.

## Explicit Notifications

Some notifications are explicitly requested by a use case, such as an invitation or a direct operational message. The handler or application service may publish a notification request, but it must not call SMTP or an SMS provider directly.

## Event-Driven Notifications

Other notifications are consequences of business events:

```text
Inventory movement committed
    -> low-stock event detected
    -> notification policy resolves recipients
    -> in-app notification created
    -> email or SMS delivery attempted when configured
```

Examples include low stock, expense approval, expense rejection, budget variance, or project status changes.

The source handler may detect a condition and publish an immutable application event. It must not send email or SMS inline as part of the domain operation.

## Transaction Boundary

For events caused by a transactional operation, the initial implementation should publish immutable event data and handle it after the transaction commits:

```text
@Transactional handler
    -> change and persist business state
    -> publish application event
    -> transaction commits
    -> @TransactionalEventListener(AFTER_COMMIT)
    -> create notification and dispatch channels asynchronously
```

This prevents a notification from being sent for a business operation that later rolls back. Event payloads should contain identifiers and immutable values rather than lazy JPA entities.

Spring's `ApplicationEventPublisher` and `@TransactionalEventListener` are sufficient for the initial in-process implementation. `@Async` may be used for email and SMS delivery so external network latency does not block the business request.

## Low Stock

Low-stock detection belongs to inventory policy, not to the email service. The inventory workflow may publish a low-stock event when an item crosses its configured threshold. Notification policy decides whether to notify and which users or channels are relevant.

The system should avoid sending repeated alerts for every movement while an item remains below the threshold. The exact deduplication or re-notification policy remains to be defined.

Low-stock notifications are not part of the initial Phase 10 implementation.
They require an inventory threshold field, a threshold-crossing definition,
recipient resolution, and a deduplication or re-notification policy.

## Delivery Channels

The initial channels are:

- In-app notification, persisted in the application database.
- Email through SMTP and Spring Mail.
- SMS as a future provider-backed channel.

SMTP credentials, including a Gmail app password for development, must come from environment configuration and must never be committed. A production deployment should use a dedicated email provider or account rather than relying on a personal mailbox.

Delivery failures must not roll back the business transaction. Delivery status, retries, and provider errors may be added through a delivery record or outbox mechanism when reliability requirements justify the extra model.

## Future Reliability

The initial in-process event approach is appropriate for the portfolio project. If notifications become operationally critical, use a transactional outbox or equivalent durable event mechanism so events are not lost when the application stops after the database commit.

## Lifecycle and Invariants

- An unread notification remains available until it is marked read or removed by policy.
- A notification belongs to a valid recipient.
- Notification creation must not change the state of the business resource that caused it.
- Marking an already-read notification is idempotent and succeeds without changing its read timestamp.
- Users may only retrieve or update notifications belonging to themselves; an inaccessible identifier is reported as not found.

## Use Cases

- List notifications for the current user.
- Get a notification for the current user.
- Mark a notification as read.
- Create notifications from explicit application actions or business events.
- Dispatch notifications through configured channels.

## Access Rules

- Users may read and update only their own notifications.
- Internal application workflows create notifications for recipients.
- Delivery channel configuration is an infrastructure concern.

Notification routes require authentication but do not require a notification
permission. Ownership is enforced by handlers using the current user ID.

The initial list operation supports an optional `read` filter, one-based
`page`, `size`, and `sort`; default ordering is newest first. Mark-all returns
the number of notifications newly marked read.

## Implementation Status

Phase 10 implements notification persistence, current-user list/detail/read
operations, invitation event publication, post-commit notification creation,
and asynchronous Spring Mail delivery. There is no public notification-create
endpoint.

The initial event mechanism is in-process and best effort. Delivery status,
retries, SMS, low-stock notifications, and a transactional outbox are deferred
until their product or reliability policies are defined.

## Classification

Notifications are a supporting application capability used by business modules. They have their own persistence and delivery use cases, but they do not own the lifecycle of projects, expenses, inventory, or other business resources.
