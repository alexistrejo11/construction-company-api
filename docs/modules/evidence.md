# Evidence and Attachments

## Responsibility

Provide reusable supporting records for construction work and business operations without owning the lifecycle of the resource being documented.

## Evidence

`Evidence` is a business-supporting record that explains or supports an event, progress update, decision, or transaction.

An evidence record may contain:

- Title.
- Description.
- Recorded timestamp.
- Author or reporting user.
- One or more attachments.

Evidence may be associated with a project, phase, expense, or another business resource. It does not automatically change the target resource's status or lifecycle.

## Attachment

`Attachment` represents a reusable technical file resource associated with evidence. It contains file metadata and storage information, such as:

- Original file name.
- Content type.
- Storage key or URL.
- File size and checksum when required.
- Creation timestamp.

Attachment is not a business event and does not contain project, phase, or expense rules.

## Usage Rules

- Do not create separate business concepts such as `PhaseEvidence` and `ExpenseAttachment` when the same evidence model can support both contexts.
- Expense-specific data such as receipt number, vendor, and amount remains on `Expense`.
- Evidence is optional by default and does not block status transitions unless a future business policy explicitly requires it.
- Evidence and attachments may have their own create, list, view, and removal use cases, but they do not control the lifecycle of the target resource.

## Persistence Association

The conceptual model is shared, but the persistence association strategy is still open. Candidate approaches include explicit association tables, a polymorphic target reference, or a constrained set of owner relationships. The implementation must preserve resource integrity and avoid orphaned evidence.

## Legacy Migration

The current code contains `ExpenseAttachmentEntity` and `Expense.invoiceUrl`, which represent an older expense-specific file model. They are transitional implementation details and should be migrated deliberately toward the shared `Evidence` and `Attachment` concepts.
