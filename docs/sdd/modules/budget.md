# Budget Module

## Responsibility

Manage planned financial allocations and budget line items for construction projects.

## Main Concepts

- Budget.
- Budget item.

## Relationship With Project

The project retains an initial high-level estimated budget as part of its planning context. The `Budget` capability represents the operational financial plan, including budget items and recorded expenses.

The project estimate and operational budget may differ over time. The difference is intentional and supports comparison between the initial expectation and actual financial control.

The project estimate is not the source of truth for expense control. A project may exist before a budget is created, while a budget belongs to one project and is created when financial planning becomes relevant.

## Budget Lifecycle

The initial budget lifecycle is:

```text
DRAFT -> APPROVED -> CLOSED
```

- `DRAFT` budgets and their planned items can be prepared and adjusted.
- `APPROVED` budgets are used for operational expense control; changes require explicit budget operations.
- `CLOSED` budgets are read-only for ordinary operations.
- Closing a budget does not delete its financial history.

The exact rules for reopening or revising an approved budget remain pending.

## Budget Items

A budget item represents a planned allocation. It may optionally be associated with a project phase and contains a description, category, unit, planned quantity, and unit price.

The planned total is derived from planned quantity and unit price. It may be stored as a controlled snapshot for reporting, but the source values remain the quantity and price.

## Expenses

An expense is recorded against a budget item and therefore belongs to the budget and project context. Its initial lifecycle is:

```text
DRAFT -> PENDING_APPROVAL -> APPROVED
                         \-> REJECTED
```

Only approved expenses count toward executed amounts. Draft, pending, and rejected expenses remain visible as workflow history but do not reduce the remaining budget.

Recording an expense should not be blocked solely because it exceeds the planned budget. The system should expose an over-budget condition and let the approval or business policy decide how to proceed.

## Amounts and Derived Values

- Planned or approved budget amounts are maintained by the budget capability.
- Executed amounts are derived from approved expenses as the source of truth.
- Remaining balance and variance are derived from planned amounts and approved expenses.
- Persisted executed totals may be introduced later as a performance optimization, but they are redundant values and must never become a second source of truth.
- If derived totals are materialized, updates must be transactional and a reconciliation strategy must exist.

Budget items may similarly derive planned totals from quantity and unit price. Persisting those totals is acceptable only as a controlled optimization.

## Implemented API Scope

The initial implementation provides one budget per project, lifecycle transitions, budget item CRUD, expense CRUD, expense status transitions, and derived summaries. Expense creation requires a budget item and uses the budget currency; an expense in another currency is rejected. Over-budget expenses are recorded and surfaced in summaries rather than silently blocked.

Budget item and expense list endpoints use one-based pagination with a maximum page size of 100. Budget items support `search` and `category`; expenses support `search`, `status`, and `currency`. Materialized totals remain a deferred refinement.

## Access Rules

- To be derived from the domain model and authorization decisions.
