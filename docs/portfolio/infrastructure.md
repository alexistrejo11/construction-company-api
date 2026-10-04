# Construction Company API — Infrastructure

## Overview

Containerized with Docker and shipped by a GitHub Actions pipeline: every pull request is tested, and every merge to `main` builds an image, publishes it to GitHub Container Registry, and deploys it to a self-hosted homelab VPS over SSH through Cloudflare Access. Public traffic reaches the API through Cloudflare, which terminates TLS, at `https://api.construction.alexis-trejo.com`.

## Deployment Pipeline

### 1. Containerization — Docker

Multi-stage build. The first stage uses GraalVM Native Image Community Java 25 and runs the Gradle wrapper's `nativeCompile` task; dependencies are resolved in their own layer before the sources are copied, so they stay cached across builds. The final stage is Debian slim and contains only the compiled native executable. It runs as a dedicated non-root `spring` user with its own writable `uploads/` directory for attachments.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/Dockerfile

### 2. CI — Tests on Every Pull Request

On every pull request and push to `main`, GitHub Actions sets up JDK 25 with Gradle caching and runs the full test suite (`./gradlew test`). Nothing is published unless the tests pass.

### 3. Publish — GitHub Container Registry

On `main` only, after tests pass, the image is built with Docker Buildx, using the GitHub Actions layer cache, and pushed to `ghcr.io` with two tags: `latest` and the commit SHA, so every deployment can be traced back to, and rolled back to, an exact commit.

### 4. Deploy — SSH over Cloudflare Access

The runner installs `cloudflared` and opens an SSH session through Cloudflare Access using a service token, so the server exposes no public SSH port. On the host, the job pulls the new image, recreates only this project's container with Docker Compose, and removes unused images for this project only, never running a host-wide prune that could affect other services. Deploy jobs run in a concurrency group so two deployments can never overlap.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/.github/workflows/ci-cd.yml

### 5. Hosting — Homelab VPS behind Cloudflare

The container runs on a self-managed server alongside my other projects, with PostgreSQL as the production database. Cloudflare sits in front for TLS and DNS, and session cookies are marked `Secure` in production.

## Runtime Configuration

The application has three profiles, each with its own database strategy:

| Profile | Database | Schema management |
|---|---|---|
| `prod` | PostgreSQL | Flyway migrations; Hibernate `validate` |
| `dev` (default) | SQLite file | Hibernate auto-update; Flyway off |
| `test` | In-memory H2 | Recreated per test context |

All secrets and environment-specific values (database, SMTP, upload directory, bootstrap admin) come from environment variables, loaded from an ignored `.env` file locally and from the host's environment in production. On the very first start against an empty database, a bootstrap initializer creates the first active company administrator from `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD`. It never recreates users once one exists, so the invitation-only model has a safe entry point.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/.env.example

## Getting Started Locally

```bash
cp .env.example .env    # fill in values; SQLite needs no database server
./gradlew bootRun       # starts on http://localhost:8017 with the dev profile
./gradlew test          # runs the integration test suite against H2
```

To run the production-like stack (API + PostgreSQL 17) locally:

```bash
docker compose up --build
```

The repository's `docker-compose.yml` starts PostgreSQL with a health check and only starts the API once the database is ready.

**Reference:** https://github.com/alexistrejo11/contruction-company-api/blob/main/docker-compose.yml

## Infra Stack

- Docker (multi-stage build) and Docker Compose
- GitHub Actions (CI/CD) and GitHub Container Registry
- Cloudflare (DNS, TLS) and Cloudflare Access with `cloudflared` (SSH tunnel for deployments)
- Self-managed homelab VPS
- PostgreSQL 17, Flyway
- Spring Boot Actuator and Micrometer Prometheus registry

## Monitoring & Observability

The application exposes health, info, and Prometheus metrics through Spring Boot Actuator. Only `/actuator/health` and `/actuator/info` are public, and health details are hidden in production; the metrics endpoints require authentication. The metrics are scraped by the homelab's Prometheus and visualized in Grafana. Every request also carries a trace ID in its logs and responses, so individual failures can be followed from a client report to the server log.
