# Pending Architecture Decisions

This file tracks unresolved design questions. Its contents are not architectural requirements.

## Module Dependency Rules

- Define whether one business module may import another module's `shared/` types directly or must use an explicit module-facing contract.
- Define how cross-module workflows are coordinated and how circular module dependencies are prevented.
- Define whether a nested area may access all parent `shared/` types or only a narrower public surface.

## General Placement Guide

- Define when a reusable concept should move from a feature to nested-area shared, module shared, or root shared.
- Define when a growing nested area should become an independent top-level module.
- Define which root-shared abstractions are permitted so that `shared/` does not become a catch-all package.

## Feature Conventions

- Decide whether every handler exposes a consistently named single public operation.
- Decide when separate transport requests are required instead of using commands or queries directly.
- Decide how feature-private helper classes are organized when a slice grows.

## Transaction Conventions

- Define the default transaction policy for commands and read-only queries.
- Define whether transaction annotations belong only on handlers or may also appear on focused shared application services.
- Define how asynchronous or multi-module workflows establish transaction boundaries.

## Nested-Area Persistence Ownership

Resolved by Phase 1: project phases and project members are subordinate
resources with their own repositories and update lifecycle. Foreign keys
preserve the rule that neither resource can exist without a valid project;
project cascade and orphan removal are not used.
