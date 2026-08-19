# Architecture

## Status

This document records confirmed architectural decisions for the codebase. Unresolved questions are tracked separately under `docs/pending/` and are not established conventions.

## System Shape

The application is a modular monolith. It is deployed as one Spring Boot application, while business capabilities are separated into packages under `modules/`.

The root package has the following responsibilities:

- `ConstructionCompanyApplication` is the application bootstrap.
- `config/` contains application-wide framework and infrastructure configuration.
- `shared/` contains technical building blocks used across business modules.
- `modules/` contains the business modules. Each direct child represents a business capability rather than a technical layer.

## Module Anatomy

A business module follows this canonical shape:

```text
modules/<module>/
├── features/
│   └── <use-case>/
│       ├── <UseCase>Controller.java
│       ├── <UseCase>Command.java or <UseCase>Query.java
│       ├── <UseCase>Handler.java
│       ├── <UseCase>Response.java          (optional)
│       └── <UseCase>Mapper.java            (optional)
├── <nested-area>/                          (optional)
│   ├── features/
│   │   └── <use-case>/...
│   └── shared/                             (optional)
└── shared/                                 (optional)
    ├── domain/
    ├── persistence/
    │   └── entity/
    ├── dto/
    ├── mapper/
    ├── policy/
    └── service/
```

The shape is a placement guide, not a requirement to create every directory. Optional packages are added only when they contain a concrete type. A module starts with feature slices and introduces local shared packages when multiple slices reuse the same concept.

Feature-specific requests, responses, mappers, and helpers remain inside the feature. Stable concepts reused across feature slices belong to the closest applicable `shared/` package: first the nested area's shared package, then the parent module's shared package.

JPA entities normally live under `shared/persistence/entity/` to keep persistence types discoverable. They still act as domain entities and may own business behavior; their package location does not make them an anemic persistence-only model.

## Vertical Feature Slices

Business modules are organized primarily by use case, not into module-wide `controller`, `service`, and `repository` layers.

Each use case lives in a package under `features/` and forms a vertical, end-to-end slice. For example:

```text
modules/project/
├── features/
│   └── getbyid/
│       ├── GetProjectByIdController.java
│       ├── GetProjectByIdQuery.java
│       └── GetProjectByIdHandler.java
└── shared/
```

A feature package owns the types used only by that use case. Depending on the use case, this can include its controller, command or query, handler, mapper, and feature-specific response types.

A slice is atomic in its ownership of a use case, not isolated from the rest of its business module. Feature handlers may depend on the module's shared domain model, repositories, mappers, and business policies. These dependencies do not break the vertical slice as long as they represent stable concepts reused by multiple use cases.

Handlers coordinate the use-case flow and define its transaction boundary. They may validate input, invoke shared domain behavior or policies, load and persist state, and translate the outcome into `Result<T>`. Extracting a reusable rule does not reduce the handler's ownership of the use case.

## Component Responsibilities

### Controller

- Exposes the HTTP entrypoint for one use case, accepts transport input, triggers Jakarta request validation, and delegates to the feature handler.
- Translates the handler outcome into the application response contract.
- Does not contain business rules, persistence queries, or transaction management.

### Command and Query

- A command is the immutable input for a use case that intends to change application state.
- A query is the immutable input for a read-only use case.
- They may declare transport-level validation constraints but do not access repositories or orchestrate behavior.
- A command or query may serve directly as the request body when a separate transport DTO would duplicate the same representation.

### Handler

- Implements one application use case and owns its execution flow.
- Loads required state, coordinates authorization or eligibility checks, invokes domain behavior, persists changes, and returns `Result<T>`.
- Defines the use case's transaction boundary when one is required.
- Has no dependency on HTTP request or response types.

### Entity

- Represents an identity-bearing domain concept and its persistent state.
- Protects invariants and owns behavior involving its own state.
- May use JPA annotations, but does not query repositories, invoke external systems, or depend on feature and web types.

### Value Object

- Represents a domain value without independent identity.
- Owns validation and behavior intrinsic to that value and should be immutable whenever JPA mapping requirements allow it.

### Domain Policy or Service

- Encapsulates a focused business rule involving multiple domain objects when no single entity naturally owns the rule.
- Operates on domain information supplied to it and does not become a general-purpose module service or use-case orchestrator.

### Shared Application Service or Validator

- Encapsulates reusable application behavior that legitimately requires repositories or external dependencies, such as resolving a valid parent resource for several nested-area features.
- Must represent one focused capability; generic containers such as `ProjectService` are not used to collect unrelated feature logic.
- Does not replace the handler as owner of a use case.

### Repository

- Provides persistence access for domain entities and may expose focused queries required by use cases.
- Does not implement HTTP concerns or complete business workflows.

### Mapper

- Converts between domain, persistence, and API representations without making business decisions or performing I/O.
- Remains feature-local unless the same mapping is reused by multiple features.

### DTO and Response

- Defines data crossing an application or API boundary and does not expose mutable JPA entities directly.
- Remains feature-local when it belongs to one use case; reusable representations belong to the closest applicable `shared/dto/` package.

## Pragmatic Domain Model

The application uses a pragmatic domain model rather than duplicating persistence and domain representations. JPA entities also act as domain entities and may contain business behavior and enforce their own invariants.

Business logic is placed according to the concept that owns it:

- An entity method enforces rules involving that entity's own state, such as a project status transition.
- A value object validates and operates on a domain value.
- A focused domain policy or service handles a reusable rule involving multiple domain objects when no single entity naturally owns it.
- A feature handler orchestrates the use case, including loading state, invoking domain behavior, handling results, persisting changes, and defining the transaction boundary.

Domain entities may use JPA annotations, but they must not depend on controllers, HTTP response types, feature commands, or injected Spring services. Repositories and external operations remain outside the entity.

Do not create separate persistence and domain models solely to remove JPA annotations. Introduce separate representations only when the models have materially different responsibilities that justify the mapping cost.

## Module-Local Shared Code

A module may contain a `shared/` package for code required by multiple feature slices in that module. Current examples include domain types, JPA entities, repositories, reusable DTOs, and mappers.

Code that is only used by one feature remains in that feature package. Code is moved to the module's `shared/` package only when multiple features need to share it.

Shared abstractions must describe a specific reusable concept. Do not collect unrelated behavior in a generic module-wide service such as `ProjectService`; prefer focused domain methods or policies named after the rule they enforce.

The root-level `shared/` package and a module's `shared/` package have different scopes:

- Root `shared/` is for application-wide technical primitives used across business modules.
- `modules/<module>/shared/` is for concepts shared only by features of that business module.

## Nested Business Areas

Project phases and project members are nested business areas inside the `project` module. They depend on a valid project for their existence, but each area is managed through its own use cases rather than as part of the general project feature set.

Each nested area has its own `features/` package and may have a local `shared/` package when several of its features reuse the same types:

```text
modules/project/
├── features/...
├── phases/
│   ├── features/...
│   └── shared/...
├── members/
│   ├── features/...
│   └── shared/...
└── shared/...
```

A business area belongs under a parent module when:

- it cannot exist without the parent resource;
- its use cases must verify or reference the parent;
- it has enough independently managed use cases to need its own feature namespace; and
- it does not represent an independent application-wide business capability.

Nested areas may use types exposed by their parent module's `shared/` package. Parent-level features must not depend on the implementation of a nested area's individual feature slices.

## Pending Decisions

Unresolved architecture questions are listed in `docs/pending/architecture-decisions.md`. They must not be inferred as requirements until promoted into this document.
