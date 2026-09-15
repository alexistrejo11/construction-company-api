# Error Handling

## Status

This document records the error-handling convention for the application. HTTP mappings and response details that are not decided yet are listed under `Pending Decisions`.

## Result-First Handling

The application uses `Result<T>` for expected outcomes that are part of normal business or application flow.

Expected outcomes include:

- Invalid credentials or a login request that cannot be authenticated.
- Request or field validation failures.
- Business rule violations.
- Resource-not-found outcomes when the requested resource may legitimately be absent.
- Conflicts such as duplicate business identifiers.
- Rejected state transitions.
- Expected authorization or eligibility failures when they are evaluated as part of a use case.

These outcomes are not exceptional from the application's perspective. A handler returns a failed `Result<T>` instead of throwing an exception for them.

```java
if (!project.isEditable()) {
    return Result.business("Project cannot be edited in its current state");
}
```

The result carries the outcome category and a message or error details. Controllers delegate failed results to `AppErrorResolver` rather than duplicating error-to-response logic in each feature.

The `Result.ErrorType` enum represents an application-level outcome category. It is not an HTTP status enum. `AppErrorResolver` owns the translation from an application category to an HTTP status so that handlers and domain code remain independent of transport concerns.

## Exceptions For Exceptional Failures

Exceptions are reserved for failures that are not expected outcomes of ordinary use-case execution, including:

- Unexpected infrastructure failures.
- Database or network failures that the use case cannot handle meaningfully.
- Programming errors and violated internal assumptions.
- Framework-level failures that occur outside normal handler result flow.
- External-system failures for which the application has no valid business fallback.

An exception may also be used when propagating an unexpected failure through infrastructure boundaries, but it must not replace a `Result<T>` merely because the outcome is an error.

## Custom Exception Hierarchy

The application will define a custom base exception for exceptional failures at application boundaries. Java exceptions must ultimately extend `Throwable`; therefore, a custom base exception cannot avoid the Java exception hierarchy entirely. The intended distinction is to avoid using generic `RuntimeException` directly throughout the codebase:

```text
Throwable
└── RuntimeException
    └── AppException
        ├── InfrastructureException
        └── IntegrationException
```

`AppException` should carry structured, non-sensitive diagnostic information where useful, such as an internal error code and safe details. It must not be used for ordinary validation, not-found, conflict, or business-rule outcomes that belong in `Result<T>`.

Exception subclasses should be created only when the exceptional category needs distinct handling, logging, recovery, or response behavior. A custom exception class for every message is not required.

## Proposed Response Envelope

The recommended API contract is one response envelope with mutually exclusive success and error shapes. The HTTP status is the authoritative indicator of success or failure; the body must not duplicate that state with a boolean field. A successful response contains `data` and does not render an `error` property. A failed response contains an `error` object and does not render `data`.

Recommended success shape:

```json
{
  "message": "Project fetched successfully",
  "data": {},
  "timestamp": "2026-08-17T12:00:00Z",
  "traceId": "..."
}
```

Recommended failure shape:

```json
{
  "message": "The request could not be processed",
  "error": {
    "error_type": "NOT_FOUND",
    "code": "PROJECT_NOT_FOUND",
    "message": "Project was not found",
    "details": []
  },
  "timestamp": "2026-08-17T12:00:00Z",
  "traceId": "..."
}
```

The outer `message` is a safe, high-level summary. The nested error contains the broad `error_type` category used for HTTP mapping, a stable machine-readable `code` for client behavior, a safe human-readable message, and optional structured details. `traceId` is response metadata and belongs at the envelope level for both success and failure responses. Internal exception class names, stack traces, SQL messages, and secrets must not be returned to clients.

`data` and `error` should be omitted rather than serialized as `null` when they do not apply. The HTTP status remains authoritative; a numeric status code should not be duplicated in the body unless a concrete client contract requires it.

The global exception handler remains the last boundary for uncaught exceptions. It must prevent implementation details from becoming the normal API contract and must log unexpected failures with enough context for diagnosis.

## Current Implementation Status

The active implementation uses the documented result and envelope model:

- `Result<T>` defines validation, unauthenticated, forbidden, not-found,
  conflict, business-rule, and unknown outcomes.
- `AppErrorResolver` centralizes failed-result to HTTP translation.
- `ResponseWrapper` contains mutually exclusive nullable `data` and `error`
  fields and no success boolean.
- Jakarta validation is handled centrally through
  `MethodArgumentNotValidException`.
- `GlobalExceptionHandler` handles framework and uncaught exception paths.

Phase 11 verifies remaining expected business failures and migrates any that
still use ordinary exceptions where a result is the established contract.

New code should follow this convention. Existing code should be migrated deliberately rather than changed mechanically, especially where exception behavior is part of a security or infrastructure boundary.

## Rationale

The distinction is based primarily on semantics, not on the claim that exceptions make ordinary Java execution expensive. Throwing an exception is comparatively expensive because the runtime creates and fills a stack trace; a `try/catch` block by itself is not the main performance concern.

Result-based handling makes expected outcomes explicit in handler signatures, keeps normal control flow visible, and avoids using exceptions as ordinary branching. Any performance benefit is secondary to this behavioral distinction.

## Layer Responsibilities

- Commands and queries declare transport-level validation constraints where appropriate.
- Handlers return `Result<T>` for expected business and application outcomes.
- Domain entities, value objects, and focused policies return or propagate the project's result type for expected rule failures.
- Controllers do not catch expected business exceptions to convert them into responses; they resolve failed results through `AppErrorResolver`.
- `GlobalExceptionHandler` handles uncaught exceptional failures and framework exceptions at the HTTP boundary.
- Repositories and infrastructure components may throw infrastructure exceptions when recovery or translation is not their responsibility; handlers should translate them only when a meaningful expected outcome exists.

## Pending Decisions

The following details still need an explicit decision:

- `Result.ErrorType` is `VALIDATION`, `UNAUTHENTICATED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `BUSINESS_RULE`, or `UNKNOWN`.
- These map to 400, 401, 403, 404, 409, 422, and 500 respectively.
- `traceId` is generated by the server, logged through MDC, included at the envelope level, and returned in `X-Trace-Id`.
- Jakarta validation remains a framework exception handled centrally with field/message details.
- Authentication and access-denied failures at the Spring Security boundary must use this envelope; handler-level expected denials return `Result`.
- Error responses never expose rejected values, exception types, stack traces, SQL details, passwords, or tokens.
