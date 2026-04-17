# Implementation Contract

Use this contract when implementing a phased plan in this repository.

## Core Request

- Implement each phase from a plan directory under `notes/<PLAN_NAME>/`.
- Execute phases in numeric order by default.
- If a specific phase is requested, implement only that phase.

## Non-Negotiables

- Follow `documentation/LLM_TYPE_SAFETY_GUIDE.md`.
- Do not edit files in `notes_and_plans/`.
- Do not perform write operations with git (read-only git usage only).
- Preserve existing behavior unless the phase explicitly changes it.
- If code is particularly complicated, add inline comments.

## Required Process

1. Read the index and target phase file(s).
2. Locate exact code points referenced by the phase (search symbols/strings).
3. Implement checklist items in the specified order.
5. Add/update tests or validation hooks required by the phase.
6. Run all automated tests
7. Fix failures caused by the phase changes.

## Missing Instruction Policy

- If phase instructions are missing critical details, stop and ask for clarification.
- Do not invent new architecture.

## Quality Bar

- No new coercion helpers in decision logic.

## Required Delivery Format

- List changed/created files (paths).
- Provide per-file summary (1-2 bullets each).
- If incomplete, explain what was not completed, why, what was tried, and what remains.
- State phase number and phase name completed.
