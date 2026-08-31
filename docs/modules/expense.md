# Expense Module

## Responsibility

Manage costs incurred by construction projects and their supporting evidence.

## Main Concepts

- Expense.
- Evidence and attachments as shared supporting concepts.

## Relationship With Budget

An expense belongs to a budget and is normally associated with a budget item. The budget and item provide the planning context; the expense represents an actual incurred cost.

Phase 7.5 introduces only the minimal persisted expense target needed for referentially safe evidence association. Its project ownership, amount, currency, and initial draft state are placeholders for the complete Phase 8 budget-linked workflow.

## Initial Lifecycle

```text
DRAFT -> PENDING_APPROVAL -> APPROVED
                         \-> REJECTED
```

Only approved expenses contribute to executed budget amounts. Rejected expenses are retained as history and are not counted as executed cost.

## Initial Invariants

- An expense must belong to a valid budget and budget item.
- The expense amount must be positive.
- The expense currency must be compatible with the budget currency unless currency conversion is explicitly introduced.
- An approved expense cannot be edited through ordinary operations.
- Evidence such as invoices, receipts, or attachments is optional unless a later approval policy requires it.

Expense-specific financial metadata remains on `Expense`; files and supporting descriptions should use the shared `Evidence` and `Attachment` concepts.

## Implemented API Scope

Expenses are created as drafts against a budget item, must use the budget currency, and follow the documented submit, approve, reject, and return-to-draft transitions. Only approved expenses contribute to derived executed totals. Evidence and attachment support is provided by the shared evidence capability.

## Access Rules

- To be derived from the domain model and authorization decisions.
