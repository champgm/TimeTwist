---
name: time-audit
description: Audit implementation work against a phased plan in `notes/PLAN_NAME/`. Use when a feature has been planned and implemented and you need a structured gap analysis between plan checklists and actual code/tests, including missing items, regressions, risks, and validation gaps.
---

# Time Audit

Audit implemented code against phased planning files and report concrete findings with file references. Default to auditing all phases in order unless a specific phase is explicitly requested.

Read the contract in [references/audit-contract.md](references/audit-contract.md) before starting.

## Required Input

- Require `PLAN_NAME`.
- Resolve plan files from `notes/<PLAN_NAME>/`.
- `PHASE` is optional:
  - Default: audit all numbered phase files in order.
  - Optional: audit only `notes/<PLAN_NAME>/<PHASE>_*.md` if user requests one phase.
- If plan files are missing or ambiguous, stop and ask for clarification.

## Workflow

1. Load plan intent.
- Read `notes/<PLAN_NAME>/00_index.md`.
- Read selected phase file(s).
- Extract expected file edits, config keys, symbols, and tests per phase.

2. Trace implementation evidence.
- Inspect changed code and tests tied to each checklist item.
- Verify search/confirm assumptions from phase docs were actually satisfied.
- Verify required commands/tests were run or can be reproduced.

3. Report findings first.
- Prioritize by severity: critical, high, medium, low.
- Include exact file paths and line references for each issue.
- Flag missing tests, behavior regressions, incomplete checklist items, and risky assumptions.

4. Provide residual risk and completion status.
- State which phase(s) pass vs fail audit.
- List unresolved items needed for sign-off.
- Keep summary short; findings are primary output.

## Output Format

- Section 1: Findings (ordered by severity with file references).
- Section 2: Open questions/assumptions.
- Section 3: Phase completion status and residual risks.
- If no findings are discovered, state that explicitly and still include residual testing gaps.

## Constraints

- Do not invent requirements not present in index/phase files.
- Do not rewrite architecture during audit; evaluate against requested scope.
- Be explicit when evidence is missing instead of asserting confidence.
