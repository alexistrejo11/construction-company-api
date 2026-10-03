# Construction Company API — Architecture

## Overview

A modular monolith built with Spring Boot: one deployable application, split into business modules, where each use case is its own vertical slice (controller, command or query, and handler in one package).

## Architecture Type

Modular monolith with vertical feature slices.

**Why:** The system has many independent use cases (87 endpoints) but is built and operated by a single developer as one deployable unit, so microservices would add network, deployment, and consistency overhead with no real benefit. Inside the monolith, organizing by use case instead of by technical layer means an endpoint can be added, changed, or removed by touching one package, without editing large shared controller or service classes.

It was also a deliberate experiment. My previous projects used the classic layered structure, with large controllers and services shared by many endpoints, and I wanted to try a structure where each use case stands on its own.

## Layers

Each business module has two kinds of code: feature slices (one per use case) and a module-level `shared/` package for concepts several slices reuse.

- **Controller** (feature slice) — binds and validates the HTTP request, calls exactly one handler, and translates its result into an HTTP response. Has no business rules, queries, or transactions.
- **Command / Query** (feature slice) — an immutable record describing the use-case input, with declarative validation constraints.
- **Handler** (feature slice) — owns the use case: loads state, checks authorization, calls domain behavior, persists changes, and defines the transaction boundary. Returns a `Result<T>` and knows nothing about HTTP.
- **Domain entities and policies** (module `shared/`) — JPA entities that also enforce their own invariants and state transitions, such as project status changes, plus focused policies for rules that span several objects. They never call repositories, HTTP, or feature types.
- **Repositories** (module `shared/`) — Spring Data persistence access and focused queries. No workflows.
- **Cross-cutting** (root `config/` and `shared/`) — security, the response envelope, error translation, pagination, tracing, rate limiting, and async configuration.

## Request Flow

```
HTTP request
  → TraceIdFilter (assigns X-Trace-Id)
  → Spring Security filter chain (session restore, CSRF, route-level auth)
  → Rate limit interceptor
  → Controller (Jakarta validation, @CurrentUser → UserContext)
  → Handler (@Transactional)
        → Authorization policy (permission + project membership)
        → Domain entity behavior (invariants, state transitions)
        → Repository (JPA)
  → Result<T>
  → Controller: success → ResponseWrapper  |  failure → AppErrorResolver
  → HTTP response (envelope + traceId)
```

Side effects that must not run unless the transaction succeeds are published as events and handled after commit:

```
CreateInvitationHandler (@Transactional)
  → saves invitation, publishes InvitationCreatedEvent
  → transaction commits
  → InvitationNotificationListener (@TransactionalEventListener AFTER_COMMIT)
        → persists in-app notification
        → sends email asynchronously (@Async thread pool)
```

## Domain Modules

- **auth** — login, logout, current session, and the CSRF token bootstrap.
- **user** — invitations, account activation, user administration, and the user's own profile.
- **project** — projects, with **phases** and **members** as nested areas that cannot exist without a project.
- **budget** — the operational budget, its items, lifecycle, and derived summary.
- **expense** — expenses recorded against budget items and their approval workflow.
- **evidence** — evidence records and file attachments for phases and expenses.
- **inventory** — items, locations, stock movements, and history-derived balances.
- **notification** — per-user in-app notifications and email delivery.
- **home** — public API landing endpoint.

## Key Design Decisions

### Vertical slices over a layered controller/service/repository structure

**Rationale:** One use case lives in one package, for example `project/features/updatestatus/` contains its controller, command, and handler. Adding or removing an endpoint does not require editing shared classes, changes stay small and easy to review, and handlers depend only on the stable concepts in the module's `shared/` package.

**Tradeoff:** There are many more small classes, and some boilerplate repeats across slices, such as resolving the parent project or mapping the response. A rule had to be set for when code is promoted to `shared/` (only once a second slice needs it) to avoid duplication or premature abstraction.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/tree/main/src/main/java/io/github/alexisTrejo11/construction/company/modules/project/features

### `Result<T>` for expected outcomes instead of exceptions

**Rationale:** Validation failures, not-found cases, conflicts, rejected state transitions, and authorization denials are normal business outcomes, not exceptional ones. Handlers return them as a typed `Result<T>` with an application-level category, so control flow stays visible in the code. A single resolver maps each category to an HTTP status, which keeps handlers and domain code free of HTTP concerns. Exceptions are reserved for truly unexpected failures, which a global handler catches.

**Tradeoff:** Every controller must check the result explicitly, and Java has no language support for result propagation, so composing several fallible steps in a handler is more verbose than letting an exception bubble up.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/src/main/java/io/github/alexisTrejo11/construction/company/shared/Result.java

### Pragmatic domain model: JPA entities are the domain entities

**Rationale:** Entities carry their own behavior and invariants, such as a project validating its status transitions and a movement refusing edits once posted. A separate persistence model would only duplicate the same shape, so it is not used. This keeps the domain rich without a mapping layer between "domain" and "persistence" objects.

**Tradeoff:** Domain classes carry JPA annotations and some persistence constraints, such as no-argument constructors and mutable fields for Hibernate. If persistence and domain needs ever diverge significantly, introducing a separate model later will be a larger refactor.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/tree/main/src/main/java/io/github/alexisTrejo11/construction/company/modules/project/shared/domain

## Persistence

- **Production:** PostgreSQL, with the schema owned by versioned Flyway migrations. Hibernate only *validates* that entity mappings match the schema at startup and never alters it.
- **Development:** SQLite, with Hibernate schema auto-update for fast iteration and no database server required.
- **Tests:** in-memory H2, recreated for every test context.
- **Sessions:** stored in the database through Spring Session JDBC, so sessions survive application restarts.
- Derived values, such as budget spent and remaining amounts and inventory balances, are calculated from their source records (approved expenses and posted movements) rather than stored, so there is no second source of truth to keep in sync.

## Project Tree

```text
src/main/java/io/github/alexisTrejo11/construction/company/
├── ConstructionCompanyApplication.java
├── config/                  # security, rate limiting, async, OpenAPI, mail, global error handling, tracing
├── shared/                  # Result, ResponseWrapper, AppErrorResolver, authorization catalog, pagination, base entity
└── modules/
    ├── auth/features/{login,logout,me,csrf}/
    ├── user/
    │   ├── features/{createinvitation,acceptinvitation,list,getbyid,update,updateroles,updatestatus,getcurrent,updatecurrent}/
    │   └── shared/{domain,persistence,dto,mapper,bootstrap}/
    ├── project/
    │   ├── features/{create,get,getbyid,getbycode,getmyprojects,update,updatestatus,restore,getsummary,getglobalsummary}/
    │   ├── phases/{features,shared}/
    │   ├── members/{features,shared}/
    │   └── shared/{domain,persistence,policy,dto,mapper}/
    ├── budget/{features,shared}/
    ├── expense/{features,shared}/
    ├── evidence/{features,shared/{access,storage,...}}/
    ├── inventory/{features,shared}/
    ├── notification/{features,shared}/
    └── home/features/gethome/

src/main/resources/
├── application.properties, application-dev.properties, application-prod.properties
├── db/migration/            # V1 … V7 Flyway migrations
└── templates/               # email templates
```
