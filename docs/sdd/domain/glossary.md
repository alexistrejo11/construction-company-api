# Domain Glossary

This glossary lists the business language used by the construction-company domain. Definitions are intentionally brief and will be refined as the business model is confirmed.

## Terms

### User

An account representing a person who can authenticate and participate in company operations.

### Invitation

A single-use, expiring onboarding credential used to create and activate a user account.

### Project

A construction project managed by the company.

### Project Phase

A major execution stage within a project.

### Phase Evidence

An optional `Evidence` record attached to a project phase, containing a title, description, captured images, and supporting metadata about work progress.

### Attachment

A reusable technical file resource associated with an evidence record.

### Site Location

The physical location of a project. It is currently treated as a project-owned value rather than an independent business entity.

### Project Member

A user assigned to participate in a project.

### Contractor

An external party involved in construction work or project services.

### Budget

The planned financial allocation associated with a project.

### Budget Item

A planned line item within a budget.

### Expense

A cost incurred against a project or budget.

### Approval

A future optional workflow in which a critical operation is requested, reviewed, and approved or rejected by another actor.

### Notification

A message generated to inform a user about a relevant event or action.

### Business Event

An immutable application-level fact that something meaningful happened in the system.

### Delivery Channel

A mechanism used to deliver a notification, such as in-app, email, or SMS.

### Inventory Item

A physical resource tracked by the inventory capability, such as material, tool, equipment, or office supply.

### Inventory Location

A warehouse or project site where inventory can be stored or used.

### Inventory Movement

A recorded operation that receives, issues, transfers, returns, or adjusts inventory.

## Terms To Confirm

- Company and organizational units.
- Vendor versus contractor.
- Expense, purchase, reimbursement, and payment.
- Budget, estimate, commitment, and actual cost.
- Approval request versus approval decision.
