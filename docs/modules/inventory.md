# Inventory Module

## Responsibility

Manage materials and inventory information used by construction projects.

Phase 9 is intentionally limited to catalog management, logistical locations,
stock movements, and history-derived balances. Low-stock notifications and
movement evidence are deferred until their cross-module contracts are defined.

## Main Concepts

- Inventory item.
- Inventory location.
- Inventory movement.

## Inventory Items

The inventory catalog uses a generic `InventoryItem` concept instead of a material-specific entity. Items may represent construction materials, tools, equipment, or office supplies.

Initial categories are:

```text
MATERIAL
TOOL
EQUIPMENT
OFFICE_SUPPLY
```

The category classifies an item; individual items such as concrete, drills, or paper are data records, not enum values.

Items may use quantity-based or serialized tracking depending on whether individual units must be identified.

An item has a unique code, name, category, unit, tracking mode, and active
status. The initial tracking modes are `QUANTITY` and `SERIALIZED`. Quantity
items use decimal positive quantities in their configured unit. Serialized
items use one positive unit per line and a required serial number; a serial
number may identify only one unit of one item.

Inactive items cannot be added to new drafts or posted movements. Existing
history remains readable, and deactivation does not delete or alter stock.

## Inventory Locations

Warehouses and project sites are represented by one simple `InventoryLocation` entity:

```text
InventoryLocation
├── id
├── code
├── name
├── type
├── projectId (nullable)
├── address
└── active
```

Initial location types are:

```text
WAREHOUSE
PROJECT_SITE
```

Location rules are intentionally simple:

- A warehouse does not require a project association.
- A project site requires a valid project association.
- The location code is unique.
- An inactive location cannot receive new inventory movements.
- An inactive location cannot be referenced by a new draft or posted movement.
- Location management uses ordinary CRUD operations without a complex lifecycle.

Location codes are unique. A location may be deactivated without deleting its
history, but it must not be used by new movements until activated again.

`Project` remains the owner of its business `SiteLocation`. An inventory project site represents the logistical location of stock and should not duplicate the project's complete address model unless a concrete requirement appears.

## Inventory Movements

Inventory movements transfer or adjust stock between locations:

```text
RECEIPT
ISSUE
TRANSFER
ADJUSTMENT
RETURN
```

A movement may contain multiple item lines. Evidence and attachments are not
part of the initial inventory API because the current evidence schema supports
only project phases and expenses. Evidence does not modify inventory by
itself.

Examples include receiving material at a warehouse, issuing material to a project site, transferring equipment between projects, and correcting a physical inventory count.

Movement direction rules are:

- `RECEIPT` has a target location and receives stock from outside inventory.
- `ISSUE` has a source location and sends stock outside inventory.
- `TRANSFER` has both a source and target location; they must differ.
- `RETURN` has a source location and sends stock outside inventory, such as a
  return to a supplier.
- `ADJUSTMENT` has one location and an explicit `INCREASE` or `DECREASE`
  direction.

All movement lines have positive quantities. A movement starts as `DRAFT` and
can be edited until it is posted. Posting changes it to `POSTED` and applies
its effect to the derived stock history. Posted movements are immutable. A
posted movement can be corrected once through `REVERSE`, which creates a new
draft reversal with inverted effects; the original remains unchanged. A
reversal is posted independently and cannot itself be reversed initially.

Movement posting rejects an issue, transfer, return, or decreasing adjustment
when the source balance is insufficient. Negative stock is not allowed.

## Inventory Balance

The initial source of truth for stock is the movement history. Current quantities can be derived from receipts, issues, transfers, returns, and adjustments.

An `InventoryBalance` table may be introduced later as a materialized performance optimization. If persisted, it must be updated transactionally, protected against concurrent updates, and supported by a reconciliation process. Movements should not be edited after posting; corrections should use reversing or adjustment movements.

## Initial Invariants

- Inventory items must have a valid category, unit, and tracking mode.
- Project sites must reference an existing project.
- Inactive locations cannot receive new stock.
- Posted movements are immutable; corrections use adjustment or reversing movements.
- Movement quantities must be positive and compatible with the item's tracking mode.
- A serialized line has quantity `1` and exactly one serial number.
- A serial number cannot be present at more than one location at the same time.
- A movement must contain at least one line.
- A transfer source and target must be different.
- Posted movements cannot be edited, posted again, or deleted.

## Use Cases

- Create, list, inspect, update, post, and reverse inventory movements.
- Create, list, inspect, update, and activate/deactivate inventory items.
- Create, list, inspect, update, and activate/deactivate inventory locations.
- Read item-by-location and location-by-item balances derived from posted history.

## Access Rules

All inventory permissions are global in Phase 9. Inventory operations do not
require project membership, including operations involving a `PROJECT_SITE`.
The authenticated user must still hold the specific inventory permission.
`COMPANY_ADMIN` receives all inventory permissions through the role catalog;
other roles receive none until an explicit role grant is approved.

Creating a project-site location still validates that the referenced project
exists. The project reference is a data-integrity dependency, not an inventory
authorization scope.

## Implementation Status

Phase 9 is implemented with item, location, movement, posting, reversal, and
history-derived balance endpoints. Production persistence is provided by the
`V6__create_inventory.sql` migration, and the complete documented endpoint
surface is covered by MockMvc integration tests.

Materialized balances, low-stock thresholds/events, and movement evidence are
deferred. Materialized balances require measured performance need; low-stock
events require a threshold-crossing and notification policy; movement evidence
requires extending the current evidence target schema beyond phases and
expenses.
