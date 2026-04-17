---
name: time-implement
description: Implement phases from a phased plan under `notes/PLAN_NAME/`. Use when asked to execute planning outputs (index plus numbered phase files) in code, including locating referenced symbols, applying checklist edits in order, updating tests, running `check_and_test.py`, and reporting results with changed files and completion status.
---

# Shaman Implement

Execute an existing plan pack in `notes/<PLAN_NAME>/`, preserving repository constraints and phase order. By default, implement all phases in numeric order, validate with repo checks, and report completion clearly.

Read the contract in [references/implementation-contract.md](references/implementation-contract.md) before modifying code.

## Required Input

- Require `PLAN_NAME` from the user request.
- Resolve files using:
  - `notes/<PLAN_NAME>/00_index.md`
  - all numbered phase files in `notes/<PLAN_NAME>/` in numeric order (`01_*.md`, `02_*.md`, ...)
- `PHASE` is optional and only used when user explicitly asks to run a specific phase.
- If `PLAN_NAME` is missing or ambiguous, ask for clarification before editing code.

## Workflow

1. Load instructions for the plan and target phases.
- Read `notes/<PLAN_NAME>/00_index.md`.
- Select target phases:
  - Default: execute all phase files in numeric order.
  - Optional: execute only `notes/<PLAN_NAME>/<PHASE>_*.md` when user requests a specific phase.
- Follow checklist order exactly for each phase.

2. Locate exact edit points before coding.
- Run each phase's symbol/string search queries first.
- Confirm assumptions listed in each phase before making edits.
- If required code points do not exist, stop and ask for clarification.

3. Implement checklist items in order.
- Preserve existing behavior unless the phase explicitly changes it.
- Avoid introducing new architecture not required by the phase.
- Add inline comments only for genuinely complex code paths.

4. Add/update validation from the phase.
- Implement tests and validation hooks specified by each phase.
- Run automated tests.
- Fix failures caused by your changes.

5. Produce completion report.
- Provide changed/created file paths.
- Provide a 1-2 bullet summary per changed file.
- State phase number(s) and phase name(s) completed.
- If incomplete, list what remains, why, and what you tried.

## Non-Negotiables

- Do not edit files under `notes_and_plans/`.
- Do not perform write operations with git.

## Output Rules

- Execute all phases in numeric order by default.
- If user requests a specific phase, execute only that phase.
- Do not claim completion if checks fail or required items are unresolved.
- When phase instructions are missing critical details, stop and ask instead of inventing behavior.
