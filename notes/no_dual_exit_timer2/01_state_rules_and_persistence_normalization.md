# Phase 01: State Rules And Persistence Normalization

## Intent
- Prevent invalid transition-state combinations as soon as either transition button is updated.
- Broaden conflict detection beyond `_REPEAT` vs `_REPEAT` so any pair of states that both point away from `timer2` is rejected.
- Keep the “new tap wins” interaction model for live button taps.
- Normalize invalid persisted combinations during `TimerViewModel` startup.
- Persist repaired startup state immediately so impossible combinations do not survive relaunches.

## Prerequisites From Previous Phases
- The transition enums and preference helpers already exist in `TimerViewModel.kt`.
- `updateTransition0To2(...)` and `updateTransition1To2(...)` already persist state and enforce the single-`_REPEAT` rule.
- `loadTimersFromPrefs()` already restores both transition values during initialization.

## Handoff Notes
- Keep this as a `TimerViewModel` rule change; do not move validation into Compose.
- Centralize the new rule in a small helper rather than duplicating the same state checks in both update methods and init.
- Startup normalization should be conservative because no tap-order history exists in preferences.
- Do not change `resolveTimer2Transition()` semantics in this phase except, at most, to keep it defensive.

## Concrete Edits
- [ ] Change [app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt)
  - Add a private helper that returns whether a `TransitionState0To2` points away from `timer2`.
  - Add a private helper that returns whether a `TransitionState1To2` points away from `timer2`.
  - Add a small normalization helper for the two transition states that applies both conflict rules:
    - only one `_REPEAT` state may survive
    - only one outgoing-from-`timer2` state may survive
  - Update `updateTransition0To2(...)` so when `newState` points away from `timer2` and `transition1To2` already points away from `timer2`, `transition1To2` resets to `DEFAULT` before persisting.
  - Update `updateTransition1To2(...)` so when `newState` points away from `timer2` and `transition0To2` already points away from `timer2`, `transition0To2` resets to `DEFAULT` before persisting.
  - Keep the existing newer-tap-wins behavior for `_REPEAT` conflicts, but make sure the broader outgoing-from-`timer2` rule also runs for directional states.
  - After loading both transition prefs in `loadTimersFromPrefs()`, detect the invalid case where both loaded values point away from `timer2`; reset both transition states to `DEFAULT` and persist both repaired values.
  - Avoid adding new preference keys, timestamps, or migration classes.
- [ ] Change [app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt)
  - Add direct-update tests for the new outgoing-from-`timer2` conflict rule in both directions.
  - Add an initialization test for repairing an invalid persisted pair by resetting both buttons to `DEFAULT`.
  - Keep existing transition-cycling and routing tests intact unless their setup now violates the stronger state-validity rule.

## Search And Confirm Steps
- `rg -n "updateTransition0To2|updateTransition1To2|loadTimersFromPrefs" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "TransitionState0To2|TransitionState1To2" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "ZERO_TWO_REPEAT|ONE_TWO_REPEAT|TWO_TO_ZERO|TWO_TO_ONE" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "saveTransitionState0To2|getTransitionState0To2|saveTransitionState1To2|getTransitionState1To2" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "constructor should load persisted timers and transitions|constructor should default invalid transition persistence" app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "setting repeat on 1 to 2 should reset repeat on 0 to 2|setting repeat on 0 to 2 should reset repeat on 1 to 2" app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "time_twist_preferences|transition_0_2|transition_1_2" app/src/test/java app/src/main/java`
- Confirm before edit that no other production file mutates `transition0To2` or `transition1To2` directly.
- Confirm before edit that preferences currently store only the enum names and not any tap-order metadata.

## Edge Cases And Failure Modes
- `ZERO_TWO_REPEAT` conflicts with both `TWO_TO_ONE` and `ONE_TWO_REPEAT` because it includes an arrow away from `timer2`.
- `ONE_TWO_REPEAT` conflicts with both `TWO_TO_ZERO` and `ZERO_TWO_REPEAT` for the same reason.
- Directional conflicts must be resolved even when neither side is a `_REPEAT` state.
- Startup repair must not accidentally wipe a valid one-sided configuration such as `TWO_TO_ZERO + ONE_TO_TWO`.
- Persisted-state repair must write the normalized values back immediately or the invalid pair will reappear on next init.
- The broader conflict rule must not block valid states that only point into `timer2`, such as `ZERO_TO_TWO + ONE_TO_TWO`.
- The runtime resolver may still keep its defensive `null` branch for impossible dual-exit states, but tests should no longer rely on that branch being reachable from normal updates.

## Tests / Validation
- Run `./gradlew testDebugUnitTest`.
  - Expected result: the `TimerViewModel` suite passes with new conflict-rule and startup-repair coverage.
- Add or update tests that assert:
  - `updateTransition0To2(TWO_TO_ZERO)` resets an existing `transition1To2 = TWO_TO_ONE` to `DEFAULT`
  - `updateTransition1To2(TWO_TO_ONE)` resets an existing `transition0To2 = TWO_TO_ZERO` to `DEFAULT`
  - `updateTransition0To2(ZERO_TWO_REPEAT)` resets an existing `transition1To2 = TWO_TO_ONE` to `DEFAULT`
  - `updateTransition1To2(ONE_TWO_REPEAT)` resets an existing `transition0To2 = TWO_TO_ZERO` to `DEFAULT`
  - loading persisted `TWO_TO_ZERO + TWO_TO_ONE` repairs both values to `DEFAULT`
  - loading persisted `ZERO_TWO_REPEAT + ONE_TWO_REPEAT` repairs both values to `DEFAULT`
- Manual verification
  - Not required in this phase because the UI shape does not change.
  - Expected observable result if inspected through state: the impossible dual-exit pair cannot survive any setter call or relaunch.

## Definition Of Done
- [ ] A single helper or equivalent shared logic defines what “points away from `timer2`” means for each transition enum.
- [ ] `updateTransition0To2(...)` clears the older conflicting `1-2` state when the new `0-2` state points away from `timer2`.
- [ ] `updateTransition1To2(...)` clears the older conflicting `0-2` state when the new `1-2` state points away from `timer2`.
- [ ] Existing single-`_REPEAT` behavior still works after the broader conflict rule is added.
- [ ] `loadTimersFromPrefs()` repairs persisted dual-exit combinations by resetting both transition states to `DEFAULT`.
- [ ] Repaired startup state is persisted back into `time_twist_preferences`.
