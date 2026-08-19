# Domain Relationships

This document will describe ownership, cardinality, and lifecycle dependencies between domain concepts.

## Initial Relationship Map

```text
User 1 ── N ProjectMember N ── 1 Project
Project 1 ── N ProjectPhase
ProjectPhase 1 ── N Evidence (optional)
Expense 1 ── N Evidence (optional)
Evidence 1 ── N Attachment
Project 1 ── 0..1 Budget
Budget 1 ── N BudgetItem
ProjectPhase 1 ── N BudgetItem (optional association)
Budget 1 ── N Expense
BudgetItem 1 ── N Expense
Project N ── N Contractor
InventoryItem 1 ── N InventoryMovementLine
InventoryLocation 1 ── N InventoryMovement (as source or target)
InventoryMovement 1 ── N InventoryMovementLine
Notification ── references a user and a relevant event
```

Approval is excluded from the current relationship map. If introduced later, its target relationship must be designed explicitly rather than assumed to be polymorphic.

These relationships are candidates and must be confirmed against business rules before being treated as the domain model.

## Questions Per Relationship

- Is the relationship required or optional?
- Which side owns the lifecycle?
- Can the child exist without the parent?
- Is deletion soft, restricted, cascading, or independent?
- Can the relationship change after creation?
- Does the relationship carry business attributes?
- Does the relationship define authorization scope?
