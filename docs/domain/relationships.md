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
InventoryMovementLine N ── 1 InventoryItem
InventoryLocation 0..1 ── 1 Project (only for PROJECT_SITE)
User 1 ── N Notification
Notification ── optionally references a resource type and identifier
```

Approval is excluded from the current relationship map. If introduced later, its target relationship must be designed explicitly rather than assumed to be polymorphic.

The inventory relationships above are confirmed for Phase 9. Movement lines
are owned by their movement and are replaced only while the movement is a
draft. Inventory locations may reference projects for data integrity, but
inventory authorization remains global rather than project-membership scoped.

The notification relationship is confirmed for Phase 10: a notification is
owned by one recipient user, and its optional resource reference is stored as
application metadata rather than a polymorphic foreign key.

## Questions Per Relationship

- Is the relationship required or optional?
- Which side owns the lifecycle?
- Can the child exist without the parent?
- Is deletion soft, restricted, cascading, or independent?
- Can the relationship change after creation?
- Does the relationship carry business attributes?
- Does the relationship define authorization scope?
