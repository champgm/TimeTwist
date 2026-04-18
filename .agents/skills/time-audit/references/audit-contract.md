# Audit Contract

Use this contract when auditing implementation against a phased plan.

## Core Request

- Audit implementation of a phased plan under `notes/<PLAN_NAME>/`.
- Default to all phases in numeric order.
- If user specifies a phase, audit only that phase.

## Audit Method

1. Read `notes/<PLAN_NAME>/00_index.md` and target phase file(s).
2. Build a checklist from each phase's required edits, tests, and definition-of-done items.
3. Verify implemented code and tests match the checklist.
4. Identify missing, partial, or incorrect implementation items.
5. Identify regressions, risky assumptions, and validation gaps.

## Findings Requirements

- Findings first, ordered by severity.
- Include exact file references and why each item matters.
- Mark clearly when evidence is absent or inconclusive.
- If no findings, say so explicitly and list residual risks.

## Scope Constraints

- Audit against the plan and repository rules; do not introduce unrelated rewrite proposals.
- Prefer concrete, actionable remediation notes.
