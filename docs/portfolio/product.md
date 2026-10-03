# Construction Company API — Product

## Problem / Vision

Construction companies often track projects, budgets, expenses, site evidence, and materials across spreadsheets, chat threads, and email. Nobody has one reliable answer to "how far along is this project, how much have we really spent, and where is our stock?"

This system gives a construction company one private back office for its operations: projects and their phases, the people working on them, the operational budget and the expenses charged against it, photo and document evidence of work, warehouse and site inventory, and the notifications that keep people informed.

## Users / Actors

The system is private. There is no public signup; every person enters through an invitation from an authorized user.

- **Company administrator** — invites people, assigns their organizational roles, suspends or reactivates accounts, and can act on any project.
- **Project team members** — project managers, site engineers, quantity surveyors, and finance officers. Each person has one or more organizational roles and works only inside the projects they are a member of.
- **Contractors** — external parties represented as a role, so they can be given limited access in the future.
- **Invited person** — someone who has received an invitation but has not yet accepted it. They cannot sign in until they set a password and activate the account.

Today only the company administrator role carries operational permissions. The other roles exist in the model, but their permissions are granted deliberately as each workflow is approved, instead of being given broad access up front.

## Domain Model

A **Project** is the center of the domain. It has a unique business code, a site location, planned dates, and a status. A project contains ordered **Phases** (major execution stages, each with its own status) and has **Project Members**, the users allowed to work on it. Membership is kept as history: removing someone deactivates their membership instead of erasing it.

Each project can have one operational **Budget**, made of **Budget Items** (planned quantity × unit price, optionally tied to a phase). **Expenses** are actual costs recorded against a budget item, in the budget's currency.

**Evidence** documents real-world work: a title, a description, an author, and **Attachments** (photos or PDFs). A piece of evidence belongs to exactly one phase or one expense, and it never changes the status of what it documents.

**Inventory** is company-wide. **Inventory Items** (materials, tools, equipment, office supplies) are stored at **Inventory Locations** (warehouses or project sites). Stock changes only through **Inventory Movements**: receipts, issues, transfers, returns, and adjustments. Current stock is calculated from the history of posted movements, not edited by hand.

**Notifications** are personal messages for a user, created by the system when something relevant happens, such as receiving an invitation. They are never created directly by users.

## Business Rules

**Projects and phases**
- A project moves through `PLANNING → IN_PROGRESS ⇄ ON_HOLD → COMPLETED`, and can be cancelled before completion. Completed and cancelled projects are read-only.
- Restoring a cancelled project is a separate administrative action that returns it to planning, not an ordinary status change.
- Whoever creates a project automatically becomes an active member of it.
- A project's code is unique and never changes.

**Budgets and expenses**
- A budget moves through `DRAFT → APPROVED → CLOSED`. A closed budget is read-only, but its history is kept.
- An expense moves through `DRAFT → PENDING_APPROVAL → APPROVED` or `REJECTED`. A rejected expense can be returned to draft and corrected.
- Only approved expenses count as money spent. Drafts, pending, and rejected expenses stay visible but do not reduce the remaining budget.
- An expense that goes over budget is still recorded. The over-budget condition is shown in the budget summary so a person can decide what to do, instead of the system silently blocking it.

**Inventory**
- Stock can never go negative. An issue, transfer, return, or decrease that would exceed available stock is rejected.
- A movement is a draft until it is posted. Posted movements can never be edited or deleted; mistakes are corrected with a reversing movement, so the full history is preserved.
- A serialized item (such as a specific generator) moves one unit at a time, identified by its serial number, and a serial number can only be in one place at a time.
- Inactive items and locations keep their history but cannot be used in new movements.

**Access**
- A person can only act on a project if their role allows the action *and* they are an active member of that project. Administrators can act on any project for explicitly defined operations.
- People only ever see their own notifications.

## Key Workflows

**Onboarding a new team member**
An administrator invites a person by email and assigns their initial roles. The person receives an email with a single-use, expiring link, sets their password and basic profile, and their account becomes active. Only then can they sign in.

**Controlling project spending**
A project gets a budget with planned line items. Once the budget is approved, team members record expenses against its items, attach receipts or invoices as evidence, and submit them for review. Approved expenses update the budget summary (planned, spent, remaining, and variance), so overspending is visible as it happens.

**Moving materials to a site**
Materials are received into a warehouse, then transferred to a project site with a movement. When the movement is posted, the system checks that enough stock exists and records the change. If something was entered incorrectly, a reversing movement undoes it without rewriting history.

## Scope

**In scope:** invitation-based user management, role-based and project-based access, projects with phases and members, phase and expense evidence with file attachments, one revisable budget per project with budget items and an expense approval workflow, company-wide inventory with movement history, and in-app plus email notifications for invitations.

**Out of scope (for now):** public signup, contractor and supplier management, generic multi-step approval workflows, formal budget versioning, low-stock alerts, SMS notifications, payments, and any client-facing or customer portal.
