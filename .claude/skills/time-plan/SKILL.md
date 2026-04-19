---
name: time-plan
description: Create phased implementation plan packs for this repository. Use when asked to transform plans into execution-ready phase files for another LLM to run locally, including concrete file edits, ripgrep discovery steps, edge cases, tests, and handoff checklists.
---

# Phased Implementation Planner

Build an execution plan another LLM can apply locally in phases, with context compaction between phases. Produce a `00_index.md` plus numbered phase files that are concrete, testable, and repo-specific.

Read the contract in [references/phase-plan-contract.md](references/phase-plan-contract.md) before writing output files.

## Required Input

- Require a plan name (`PLAN_NAME`) from the user request.
- Create a new folder, `notes/<PLAN_NAME>/`. 
- Write output only to `notes/<PLAN_NAME>/`.
- If `PLAN_NAME` is not provided, ask for it before generating files.

## Workflow

1. Read context before writing.
- Read `README.md`, `AGENTS.md` and any docs directly tied to the requested scope.
- Inspect existing implementation/research notes under `notes/` and `notes/_research/` only when relevant.

2. Design the phase breakdown first.
- Keep total phase files at or below 10.
- Keep each phase independently executable by a fresh LLM context.
- Group work by dependency order and validation checkpoints.

3. Write `00_index.md`.
- Include scope summary bullets (5-10).
- List all phases with 1-2 sentence goals.
- Define cross-phase conventions once (naming, config, logging/tests).
- Add a global definition-of-done checklist.

4. Write each phase file (`01_<name>.md`, `02_<name>.md`, ...).
- Follow the exact required section set in the contract.
- Make every checklist item atomic and tied to explicit file paths.
- Include exact symbol/text search queries and confirm-before-edit checks.
- Include exact test commands, expected assertions, and manual verification.

5. Run a quality pass before finalizing.
- Remove placeholder language and vague instructions.
- Verify no repeated global boilerplate inside phase files.
- Verify line and file-count limits.
- Verify all paths, config keys, type names, and logger names are explicit.

## Output Rules

- Place output in `notes/<PLAN_NAME>/` (never outside `notes/`).
- Use this exact folder shape: `notes/<PLAN_NAME>/00_index.md` and `notes/<PLAN_NAME>/01_<name>.md`...
- Keep each phase file under 300 lines.
- Do not create architecture that was not requested.
- Do not include phrases like `update as needed`.
- If file location is uncertain, list up to 3 candidate paths and include a path-selection rule.

## Repo-Specific Guardrails

- Treat this as an existing codebase and preserve architecture unless explicitly asked to change it.
- Prefer complete migrations over leaving dead/legacy/deprecated paths.
- Include updates to relevant notes in `notes/_research/` when implementation meaningfully changes.

## Pre-Delivery Checklist

- `00_index.md` exists and includes all required sections.
- Every phase file contains all eight required sections in the contract.
- Each phase contains 5-10 search queries and explicit confirm-before-edit assumptions.
- Each phase defines tests/validation commands with concrete expected outcomes.
- Phase files do not repeat global conventions from `00_index.md`.
- Total phase file count and per-file line limits are satisfied.
