# Phase 03: Docs And Final Validation

## Intent

- Record the implemented fix path and any remaining caveats in the repo’s research notes.
- Run the final repo-level validation for service and view-model lifecycle behavior.
- Leave a clear audit trail connecting the original investigation to the code changes.

## Prerequisites From Previous Phases

- `CountdownService` lifecycle hardening from phase 01 is merged locally.
- `TimerViewModel` reconciliation and regression coverage from phase 02 are merged locally.
- The original investigation note exists at `_notes/_research/wear-ongoing-activity-notification-icon-investigation.md`.

## Handoff Notes

- Do not move the original `_notes/_research/...` file; add repo-local follow-up notes under `notes/_research/`.
- Keep the final note specific to implemented behavior, not speculative platform theory already covered in the original investigation.
- If validation reveals a remaining issue, update the note with the residual risk instead of pretending the fix is complete.

## Concrete Edits

- [ ] Create [notes/_research/activity_icon_linger_fix.md](/mnt/nvme/github/TimeTwist/notes/_research/activity_icon_linger_fix.md)
  - Summarize the root cause that was actually fixed in production code.
  - Record the chosen restart policy (`START_NOT_STICKY` or alternative) and why it fits this timer app.
  - Record the explicit teardown strategy used to end the foreground notification / ongoing activity.
  - Note any remaining platform uncertainty that cannot be fully asserted in JVM tests.
- [ ] Optionally change [README.md](/mnt/nvme/github/TimeTwist/README.md) only if user-visible timer behavior or lifecycle guarantees need a short clarification.
  - Keep any README change limited to behavior that a user or contributor can observe.
  - Avoid adding implementation-detail prose about Wear notification internals unless necessary.
- [ ] Review [app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt) and [app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt)
  - Remove or update any test names/comments that still describe the pre-fix sticky-lifecycle behavior.

## Search And Confirm Steps

- `rg -n "START_STICKY|START_NOT_STICKY|stopForeground|ongoing activity|linger" app/src/main/java app/src/test/java README.md notes _notes`
- `rg -n "wear-ongoing-activity-notification-icon-investigation|activity_icon_linger_fix" notes _notes`
- `rg -n "manual starts should emit|service should emit completion alert|startup repair|stopTimers" app/src/test/java`
- `rg -n "foreground service|timer running|Wear OS|notification" README.md`
- `rg -n "notes/_research|_notes/_research" -g "*.md"`
- Confirm before edit that `notes/_research/` exists; create it if missing.
- Confirm before edit whether README actually needs changes after the code/test updates.
- Confirm before edit that the new research note describes implemented outcomes rather than repeating the original investigation verbatim.

## Edge Cases And Failure Modes

- Writing the follow-up note into `_notes/_research/` instead of `notes/_research/` would violate the repo-specific planning guardrail.
- README edits can create maintenance burden if they describe internal implementation choices rather than observable behavior.
- Test names that still mention sticky restart after the fix can mislead future contributors.
- A passing JVM suite does not prove every Wear launcher/watch-face surface refreshes immediately; residual platform lag, if any, should be documented honestly.
- If phase 02 chooses not to persist active-run state, the note should explain that decision and the resulting expectations clearly.

## Tests / Validation

- Run `./gradlew testDebugUnitTest`.
  - Expected result: full unit test suite passes from the repo root after all lifecycle and state changes.
- If Android SDK setup already exists locally, optionally run `./gradlew lint`.
  - Expected result: no new lint issues introduced by the service lifecycle changes.
- Manual verification
  - Start a timer and confirm the Wear icon appears.
  - Stop the timer before completion and confirm the icon disappears without waiting for a stale restart.
  - Let a short timer complete, return to the watch face/home screen, and confirm the icon disappears after completion instead of lingering with no active countdown.

## Definition Of Done

- [ ] `notes/_research/activity_icon_linger_fix.md` exists and describes the implemented fix path.
- [ ] Any outdated test names/comments referencing pre-fix behavior are updated.
- [ ] README remains accurate after the fix, with no unnecessary internal-detail sprawl.
- [ ] `./gradlew testDebugUnitTest` passes after all edits.
- [ ] Manual verification steps and expected outcomes are documented for future repro/regression checks.
