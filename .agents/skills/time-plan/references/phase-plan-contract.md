# Phase Planning Contract

Use this contract when generating phased implementation planning files for this repository.

## Core Request

- Carefully and thoroughly plan a phased implementation plan for another LLM to execute locally.
- Assume LLM context is compacted between phases.
- Place output in `notes/PLAN_NAME/` where `PLAN_NAME` comes from the user request.

## Repo Context Constraints

- Treat the repository as an existing codebase.
- Do not rewrite architecture unless explicitly required.
- Prefer complete changes.
- Leave no legacy code.
- Leave no deprecated functions.
- Leave no dead code.
- Update relevant research notes in `notes/_research/` if implementation changes meaningfully.

## Reliability Requirements

- Hard limit: at most 10 phase files total.
- Hard limit: each phase file must be 300 lines or fewer.
- No repeated boilerplate: global conventions belong only in `00_index.md`.

## Required Output Files

- `00_index.md`
- `01_<name>.md`, `02_<name>.md`, ... (one or more phase files)

## `00_index.md` Requirements

- Scope summary with 5-10 bullets
- Phase list with 1-2 sentence goal each
- Cross-phase conventions:
  - naming
  - config patterns
  - logging and test conventions
- Acceptance criteria checklist (global definition of done)

## Phase File Requirements

Each phase file must include all sections below:

1. Intent
- 3-8 bullets

2. Prerequisites from previous phases
- Expected files, types, config keys to exist

3. Handoff notes
- Exact logs to look for
- Keep checklist-like and short

4. Concrete edits
- Checklist of exact file paths to modify/create
- Per file: Add / Change / Avoid bullets
- Atomic checklist items (independently doable)

5. Search and confirm steps
- 5-10 find-reference queries (strings or symbols)
- Confirm-before-edit assumptions

6. Edge cases and failure modes
- 5-10 bullets

7. Tests / validation
- Exact commands to run
- Tests to add/update and what they assert
- Manual verification with expected observable results

8. Definition of done
- 5-10 checkable bullets

## Global Writing Constraints

- Do not use placeholder phrases like `update as needed`.
- Do not omit file paths.
- Do not create architecture unless explicitly required by the plan.
- If unsure about file location, provide up to 3 candidate paths and how to choose.

## Mandatory Completeness Check

Every phase must include:
- Exact `rg` queries/search steps
- Concrete edits with precise names and/or config keys
- Explicit tests/commands
