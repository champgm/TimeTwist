# Phase 02: Tests And Notes

## Intent
- Lock the stronger arrow-state validity rule in tests so future UI or persistence changes cannot reintroduce dual exits from `timer2`.
- Cover both direct setter calls and tap-cycle behavior, because the production UI uses cycle methods rather than direct enum assignment.
- Confirm that valid combinations still survive unchanged.
- Update the research note so the documented rule matches the shipped state machine.

## Prerequisites From Previous Phases
- `TimerViewModel.kt` enforces the broader outgoing-from-`timer2` conflict rule.
- Startup normalization resets invalid persisted dual-exit pairs to `DEFAULT`.
- Existing chaining tests for `timer0`/`timer2` and `timer1`/`timer2` still exist in `TimerViewModelTest.kt`.

## Handoff Notes
- Keep this phase focused on regression coverage and documentation; do not add new runtime features.
- Add tests near the existing transition-state tests instead of creating a parallel suite unless the current file is already split.
- Update the concise research note in `notes/_research/`, not the original long-form prompt in `notes/timer_chaining.md`.

## Concrete Edits
- [ ] Change [app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt)
  - Add cycle-driven tests showing that tapping into an outgoing-from-`timer2` state resets the older conflicting button.
  - Add regression tests proving valid mixed-direction combinations remain intact, such as `TWO_TO_ZERO + ONE_TO_TWO` only clearing when the second button is later tapped into `TWO_TO_ONE` or `ONE_TWO_REPEAT`.
  - Keep existing routing assertions for valid states green so the stronger state guard does not break chaining.
- [ ] Change [notes/_research/timer_chaining.md](/mnt/nvme/github/TimeTwist/notes/_research/timer_chaining.md)
  - Add the rule that `timer2` may have at most one outgoing arrow across both transition buttons.
  - Document that the newest conflicting tap wins during normal interaction.
  - Document that invalid persisted dual-exit combinations are repaired on startup by resetting both buttons to `DEFAULT`.
  - Keep the existing routing semantics and alert-policy text intact.

## Search And Confirm Steps
- `rg -n "cycleTransition0To2|cycleTransition1To2" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "cycleTransition0To2 should visit every state|cycleTransition1To2 should visit every state" app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "timer2 completion should start timer0|timer2 completion should start timer1|zero two repeat|one two repeat" app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "transition0To2.value|transition1To2.value|getTransitionState0To2|getTransitionState1To2" app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "Only one transition button may remain in a `_REPEAT` state|Routing Semantics|Alert Policy" notes/_research/timer_chaining.md`
- `rg -n "timer2|outgoing|DEFAULT|REPEAT" notes/_research/timer_chaining.md notes/timer_chaining.md`
- Confirm before edit that the research note is still the canonical concise behavior summary for shipped chaining semantics.
- Confirm before edit that no other note under `notes/_research/` already documents startup normalization for transition-state corruption.

## Edge Cases And Failure Modes
- A cycle-based test must reflect the real tap order; a direct setter test alone is not enough because the UI never calls the setters in arbitrary order.
- Valid “both arrows into `timer2`” configurations must remain allowed.
- A test that sets up an invalid pair directly may need to assert immediate reset behavior rather than a transient impossible state.
- Startup normalization documentation must clearly distinguish between invalid persisted state and valid user taps.
- The note update must not drift back to the earlier narrower wording that only mentioned `_REPEAT` conflicts.
- Existing routing tests may need light setup changes if they previously depended on an impossible combination being temporarily allowed.

## Tests / Validation
- Run `./gradlew testDebugUnitTest`.
  - Expected result: `TimerViewModelTest` passes with new cycle-driven conflict coverage and no routing regressions.
- Add or update tests that assert:
  - cycling `transition0To2` into `TWO_TO_ZERO` clears an existing `transition1To2 = TWO_TO_ONE`
  - cycling `transition1To2` into `TWO_TO_ONE` clears an existing `transition0To2 = TWO_TO_ZERO`
  - cycling either button into its `_REPEAT` state still clears the older conflicting button
  - valid `ZERO_TO_TWO + ONE_TO_TWO` remains intact after both updates
  - valid `TWO_TO_ZERO + ONE_TO_TWO` remains intact until the `1-2` button is later advanced to an outgoing-from-`timer2` state
- Manual verification
  - Enter edit mode and tap both transition buttons through their state cycles.
  - Expected observable result: as soon as the second button would create a second outgoing arrow from `timer2`, the older conflicting button visibly returns to `DEFAULT`.

## Definition Of Done
- [ ] Tests cover the broader no-dual-exit rule through both direct updates and cycle-driven state changes.
- [ ] Tests still cover valid one-sided and inward-only combinations.
- [ ] No existing routing regression test is removed without an equivalent replacement.
- [ ] `notes/_research/timer_chaining.md` states the new no-dual-exit rule clearly.
- [ ] The research note documents startup repair by resetting persisted dual-exit combinations to `DEFAULT`.
- [ ] `./gradlew testDebugUnitTest` is the final recorded validation command for the pack.
