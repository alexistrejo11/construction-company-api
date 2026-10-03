# Validation

## Status

This document records the validation convention for commands, queries, handlers, and domain objects.

## Command and Query Contracts

Commands and queries define the structural contract of a use case. They should prefer Java `record` types and may use Jakarta Validation annotations to describe required fields, formats, ranges, and nested validation rules.

```java
public record CreateProjectCommand(
    @NotBlank String name,
    @NotNull @PositiveOrZero BigDecimal totalBudget
) {}
```

Jakarta Validation annotations have two roles:

- They document the expected shape of the input for every caller.
- They provide executable validation when the presentation boundary invokes the command with `@Valid`.

Callers must not assume that the annotations execute automatically when a command or query is passed directly to a handler.

## Presentation Validation

Controllers validate the transport shape before invoking a handler:

```java
public ResponseEntity<?> create(
    @Valid @RequestBody CreateProjectCommand command
) {
    return ...;
}
```

Presentation validation covers:

- Required and nullable fields.
- String formats and lengths.
- Numeric ranges.
- Nested request structure.
- Other constraints that can be evaluated without application state.

The same structural constraints must not be manually duplicated in handlers. A command can be bound directly from HTTP when its shape is already the application input contract; a separate request DTO is introduced only when the transport representation differs from the use-case input.

## Handler Contract

A handler's `execute` operation requires a non-null, structurally valid command or query. A `null` command is not an expected user error; it means that a preceding adapter violated the application contract.

Handlers may fail fast for a null command:

```java
public Result<ProjectResponse> execute(CreateProjectCommand command) {
    Objects.requireNonNull(command, "command must not be null");
    // The command has a valid structural contract at this boundary.
}
```

Handlers must still validate application-level preconditions, such as resource existence, permissions, allowed state, and cross-entity business rules. These are different from transport validation and should not be duplicated Jakarta constraints.

The handler interface may document the non-null precondition with JavaDoc:

```java
public interface CommandHandler<C, R> {
    /**
     * Requires a non-null, structurally valid command.
     */
    Result<R> execute(C command);
}
```

`@NotNull` on a handler method is not used solely as documentation because it can imply that runtime method validation is configured when it is not. Jakarta method constraints only execute when an appropriate method-validation mechanism is enabled and invoked.

## Domain Validation

Entities, value objects, and focused domain policies protect invariants that must hold regardless of the caller. They must not rely exclusively on controller validation.

Validation responsibilities are divided as follows:

```text
Controller: transport shape and stateless input constraints
Handler: application preconditions and use-case rules
Domain: entity and value-object invariants
Repository: persistence access, not business validation
Database: final structural integrity constraints
```

A domain object must not enter an invalid business state merely because it was created by a job, test, another handler, or a future adapter instead of an HTTP controller.

## Nullability and Optional

Required command and query fields use their normal types with appropriate Jakarta constraints such as `@NotNull`, `@NotBlank`, or `@Positive`.

`Optional` is not used as a general wrapper for nullable request fields. It complicates JSON binding, validation, API documentation, and mapping. Use `Optional<T>` primarily for return values that may legitimately be absent, such as repository lookups.

Optional input values should use a nullable field with an explicit contract. Collections should preferably be represented as empty collections rather than `null` when absence and emptiness have the same meaning.

## Records

Commands, queries, request DTOs, and response DTOs should prefer Java `record` types because they provide concise immutable data carriers and make the boundary contract visible in one place.

Records must not access repositories, orchestrate use cases, or contain infrastructure behavior. Domain value objects may also use records when their persistence and framework requirements allow it.
