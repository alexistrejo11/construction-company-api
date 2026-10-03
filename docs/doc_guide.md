# Backend Documentation Guide

This guide defines how to document a backend project for a portfolio using five plain Markdown files, in this reading order:

```
docs/portfolio/
  product.md         → what it is and why (non-technical)
  architecture.md    → how it's organized
  api.md             → how to talk to it from the outside
  features.md        → what technical capabilities it has
  infrastructure.md  → how it runs in the real world
```

General rule for all five: if there's no honest, real content for a section, **omit it** rather than filling it with generic or aspirational content. A short, honest file beats a long one padded with filler.

## Portfolio Docs vs. Project Docs

Portfolio docs are an external showcase, written for someone evaluating the project (recruiters, engineers, clients). They are separate from, and independent of, any other documentation in the repository, such as Spec-Driven Development (SDD) specs, conventions, planning notes, ADRs, or backlogs.

- **Location:** always `docs/portfolio/`. No other documentation lives in that folder, and portfolio files live nowhere else.
- **Self-contained:** each file must make sense on its own. Do not link to or depend on internal docs (`docs/sdd/`, `docs/pending/`, etc.); link only to source code, public tooling (Swagger, Postman), or other files inside `docs/portfolio/`.
- **Not a source of truth for development:** internal docs and the code drive implementation. Portfolio docs are never used as specs, contracts, or agent instructions.
- **Implemented behavior only:** describe what exists and is verified today. Proposals, pending decisions, stubs, and "to be defined" content stay in internal docs.
- **Written separately:** internal docs can be used as input, but portfolio content is curated and rewritten for an external reader, not copied or auto-generated from them. Update it when a milestone is completed, not on every change.

---

## 1. [product.md](http://product.md)

The source of truth — the definition everything else (architecture, features, code) is a *consequence* of. Written for a non-technical reader too: no stack names, no framework references, no code paths.

**1. Problem / Vision** What this solves, and for whom. 2-3 sentences.

```
## Problem / Vision

Pharmacies currently manage inventory and orders through disconnected spreadsheets.
This platform gives them a single system to track stock, take orders, and fulfill them,
without needing separate tools for each step.
```

**2. Users / Actors** Who interacts with this system and what they need from it.

**3. Domain Model** Entities, relationships, and invariants — described conceptually, not as an ERD or table schema.

```
## Domain Model

An **Order** belongs to a **Customer** and contains one or more **Items**.
An Order cannot receive new Items once it has been dispatched.
A **Customer** can have multiple Orders, but only one active Cart at a time.
```

**4. Business Rules** *(optional — only if there are enough rules to separate from the Domain Model above)*

```
## Business Rules

- An Order total must always equal the sum of its Item subtotals.
- A Customer cannot check out with an empty Cart.
- Inventory cannot go negative — an Order that would cause this is rejected at checkout.
```

**5. Scope** What's explicitly in and out.

```
## Scope

**In scope:** inventory tracking, order intake, basic fulfillment status.
**Out of scope:** payment processing (handled by a separate service), delivery logistics.
```

### Checklist

- No stack names, framework references, or code paths anywhere in this file.
- Problem/Vision explains the "why," not the "how."
- Scope explicitly lists what's out, not just what's in.
- A non-technical reader could understand this file without opening [architecture.md](http://architecture.md).

---

## 2. [architecture.md](http://architecture.md)

Answers: **how are the pieces organized, and how do they connect?**

**1. Overview** 1-2 sentences, specific to the architecture. Example: "Hexagonal architecture with 4 layers, event-driven communication between services."

**2. Architecture Type** Name the pattern (hexagonal, clean, layered, onion, event-driven, etc.), followed by a short "Why" explaining why this pattern was chosen for this specific project — not a generic definition.

```
## Architecture Type

Hexagonal Architecture (Ports & Adapters).

**Why:** Chosen because the domain logic needs to stay independent from delivery
mechanisms (REST today, possibly message queues later). Keeps core business
rules testable without spinning up infrastructure.
```

**3. Layers** Short list, 1-2 lines per layer: its responsibility, and what it must NOT know about.

```
## Layers

- **Presentation** — handles HTTP input/output, validation, serialization. Has no business logic.
- **Application / Service** — orchestrates use cases, calls domain and infrastructure through interfaces.
- **Domain** — core business rules. No framework or infrastructure dependencies.
- **Infrastructure** — persistence, external services, messaging. Implements the interfaces defined by the domain.
```

**4. Communication / Request Flow** A flow diagram as a fenced code block (plain text/ASCII or mermaid):

```
## Request Flow

Request → Presentation → Service → Domain → Infrastructure → Response
```

If the project has multiple services, add a second diagram showing how they communicate with each other (sync/HTTP, async/events).

**5. Domain Modules** *(optional — only with clear bounded contexts)* Short list of business modules (auth, payment, inventory...) with one line each. Skip entirely for small projects without a clear domain split.

**6. Key Design Decisions** 2-4 decisions max. Two well-explained decisions beat five shallow ones. Each decision needs a rationale AND a tradeoff:

```
## Key Design Decisions

### Hexagonal over classic MVC

**Rationale:** Keeps the domain isolated from framework concerns, makes unit
testing the business logic straightforward without mocking the framework.

**Tradeoff:** More boilerplate for simple CRUD operations — interfaces and
adapters add ceremony even where a direct implementation would be shorter.

**Reference:** https://github.com/{user}/{repo}/tree/main/src/domain
```

**7. Project Tree** A fenced code block with the real folder structure. Quick-reference, can go last.

### Checklist

- Does it explain *why* the pattern was chosen, not just its name?
- Is the flow diagram understandable without reading the code?
- Does every decision include both a rationale and a tradeoff?

---

## 3. [api.md](http://api.md)

Documents the external **contract** — how to talk to this project from the outside. Not implementation detail, not a live testing tool.

**1. Overview** Protocol (REST/GraphQL/gRPC/mixed), base URL, versioning strategy if one exists.

```
## Overview

REST over HTTPS, JSON payloads. Base URL: `https://api.example.com/v1`.
Versioning via URL prefix; no breaking changes within a major version.
```

**2. Documentation Tooling** Swagger UI / Redoc / Postman collection, with a real reachable link. If none exist, say so and skip to the endpoint table — don't imply tooling that isn't there.

**3. Authentication** The single most important section — what someone needs before they can try anything.

- Mechanism: JWT, OAuth2, API key, session cookie, etc.
- How to send it, with a literal example:

```
Authorization: Bearer <token>
```

- Where the token comes from (login endpoint, OAuth flow, issued manually, etc.)
- Any per-endpoint exceptions (public endpoints that skip auth).

**4. Request / Response Conventions**

- Envelope/wrapper, if responses are wrapped in a standard shape, shown with a real example.
- Pagination format (cursor or offset), with a real request + response example.

```
GET /v1/orders?cursor=eyJpZCI6MTIzfQ&limit=20

{
  "data": [ ... ],
  "meta": { "nextCursor": "eyJpZCI6MTQzfQ", "hasMore": true }
}
```

**5. Endpoints** A flat table — no payload detail (that's what Swagger/Redoc is for, if it exists).


| Method | Path           | Description                            | Auth     |
| ------ | -------------- | -------------------------------------- | -------- |
| GET    | `/v1/orders`   | List orders for the authenticated user | required |
| POST   | `/v1/orders`   | Create an order                        | required |
| GET    | `/v1/products` | List public product catalog            | none     |


**6. GraphQL Schema** *(only if applicable)* Same high-level treatment — queries/mutations with one line each, not the full schema.

**7. Error Handling** *(optional)* Standard error shape if one exists, and any status codes that aren't self-explanatory.

### Checklist

- Auth section includes a literal request example, not just the mechanism name.
- Pagination format shown with a real example.
- Endpoint table has method + path + one-line description — no payload bodies.
- Tooling links are real and reachable, or the section says there is none.
- Nothing here duplicates implementation detail already covered in [features.md](http://features.md).

---

## 4. [features.md](http://features.md)

Answers: **what technical capabilities were implemented, as named, self-contained pieces?**

Litmus test: if it can be explained in 3-4 lines without needing the architecture diagram, it belongs here. If it needs the diagram to make sense, it belongs in [architecture.md](http://architecture.md) instead. If a feature has an externally visible effect on the API contract (a header, an error code), document that effect in [api.md](http://api.md) — this file stays focused on the internal implementation.

Group into categories if there are several (optional, only useful once the list grows):

- **Core Patterns** — CQRS, Event Sourcing, Query Bus, Command Bus, Repository Pattern, Factory Pattern, etc.
- **Cross-cutting Concerns** — rate limiting, authentication (JWT/OAuth), caching, structured logging, centralized error handling.
- **Integrations** — Kafka/event messaging, PostgreSQL views/materialized views, third-party services, gRPC.

Each feature entry:

```
### Rate Limiting with Redis

Per-user request throttling on write endpoints using a sliding window
counter stored in Redis, to protect the service from abuse.

**Reference:** https://github.com/{user}/{repo}/blob/main/src/middleware/rate-limit.ts
```

- Title must be specific and recognizable to another engineer — not a vague buzzword.
- 2-3 line description: what problem it solves, how it's implemented at a high level.
- Reference link optional but preferred when there's a concrete file/folder to point to.

### Checklist

- Is the title specific (not a generic buzzword)?
- Does each feature link to the real implementation when possible?
- Does it avoid duplicating something better explained in [architecture.md](http://architecture.md) or [api.md](http://api.md)?

---

## 5. [infrastructure.md](http://infrastructure.md)

Answers: **how does this run and get deployed in the real world?**

**1. Overview** 1-2 lines on how the project is deployed overall. Example: "Containerized with Docker, CI/CD via GitHub Actions, deployed to a VPS through a Cloudflare Tunnel."

**2. Deployment Pipeline** Sequential steps, each with what it does (not just the tool name) and a reference when relevant:

```
## Deployment Pipeline

### 1. Containerization — Docker

Multi-stage build to keep the final image small. Base image: node:20-alpine.

**Reference:** https://github.com/{user}/{repo}/blob/main/Dockerfile

### 2. CI/CD — GitHub Actions

On push to main: runs tests, builds the image, pushes to the registry.

**Reference:** https://github.com/{user}/{repo}/blob/main/.github/workflows/deploy.yml

### 3. Hosting — VPS via Cloudflare Tunnel

Deployed to a self-managed VPS, exposed through a Cloudflare Tunnel
(no public inbound ports opened directly).
```

**3. Infra Stack** Plain list of tools: Docker, (Kubernetes if applicable), tunnel/hosting provider, other infra tooling.

**4. Monitoring & Observability** *(optional — only if something real is actually running)* Short description of what is monitored and with what (Grafana, Prometheus, Loki, ELK). Link to a public/accessible dashboard if there is one. Never aspirational — only if it's real and running today.

### Checklist

- Does each pipeline step explain what it does, not just name the tool?
- Are code references short/relevant snippets or file links, not full files pasted in?
- Is Monitoring only included if something is actually running and observable?

