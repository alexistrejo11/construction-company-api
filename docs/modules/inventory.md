# Inventory Module

## Responsibility

Manage materials and inventory information used by construction projects.

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
- Location management uses ordinary CRUD operations without a complex lifecycle.

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

A movement may contain multiple item lines and can use `Evidence` and `Attachment` as optional supporting records. Evidence does not modify inventory by itself.

Examples include receiving material at a warehouse, issuing material to a project site, transferring equipment between projects, and correcting a physical inventory count.

## Inventory Balance

The initial source of truth for stock is the movement history. Current quantities can be derived from receipts, issues, transfers, returns, and adjustments.

An `InventoryBalance` table may be introduced later as a materialized performance optimization. If persisted, it must be updated transactionally, protected against concurrent updates, and supported by a reconciliation process. Movements should not be edited after posting; corrections should use reversing or adjustment movements.

## Initial Invariants

- Inventory items must have a valid category, unit, and tracking mode.
- Project sites must reference an existing project.
- Inactive locations cannot receive new stock.
- Posted movements are immutable; corrections use adjustment or reversing movements.
- Movement quantities must be positive and compatible with the item's tracking mode.

## Use Cases

- To be listed and confirmed.

## Access Rules

- To be derived from the domain model and authorization decisions.
