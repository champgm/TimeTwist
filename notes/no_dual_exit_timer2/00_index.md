# No Dual Exit From Timer2 Plan Pack

## Scope Summary
- Prevent transition state from ever leaving `timer2` with two simultaneous outgoing arrows.
- Enforce the rule at transition-update time in `TimerViewModel`, not only at runtime resolution.
- Treat `TWO_TO_ZERO`, `ZERO_TWO_REPEAT`, `TWO_TO_ONE`, and `ONE_TWO_REPEAT` as states that point away from `timer2`.
- Keep the existing “latest tap wins” behavior when a new selection conflicts with an older one.
- Reset the older conflicting button to `DEFAULT` immediately and persist that reset in the same update.
- Preserve the existing single-`_REPEAT` rule; the new rule is broader and must coexist with it.
- Normalize invalid persisted state on startup so corrupted or legacy preferences cannot leave the UI in an impossible configuration.
- Use a conservative startup recovery rule: if both buttons load with outgoing arrows from `timer2`, reset both to `DEFAULT` and persist the normalized state.
- Keep runtime chaining behavior unchanged for already-valid configurations.
- Add focused `TimerViewModel` regression coverage and update the chaining research note.

## Phases
- `01_state_rules_and_persistence_normalization.md`
  Extend transition update logic so invalid `timer2` double-exit combinations are prevented immediately, and normalize invalid persisted state during `TimerViewModel` initialization.
- `02_tests_and_notes.md`
  Add regression coverage for both direct updates and cycle-driven taps, then document the broader `timer2` outgoing-arrow rule in `notes/_research/`.

## Cross-Phase Conventions
- Naming
  - Keep all rule enforcement in `app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`.
  - Reuse the existing enum names and preference keys; do not introduce replacement state types.
  - Name any added unit tests in the existing `TimerViewModelTest.kt` file unless a search confirms the suite has been split.
- Config patterns
  - Reuse `TIME_TWIST_PREFERENCES`, `transition_0_2`, and `transition_1_2`.
  - Persist normalized state immediately after conflict resolution or startup repair.
  - Do not add timestamps, migration classes, or new preference metadata for this rule.
- Logging and test conventions
  - Prefer state and preference assertions over logging.
  - Keep tests deterministic through the existing `TimerViewModel` seams and shared-preference cleanup helpers.
  - Run validation from repo root with `./gradlew testDebugUnitTest`; no UI build is required unless code changes spill into Compose APIs.

## Global Definition Of Done
- [ ] No tap sequence can leave both transition buttons pointing away from `timer2`.
- [ ] The most recently tapped outgoing-from-`timer2` state wins and the older conflicting button resets to `DEFAULT`.
- [ ] The single-`_REPEAT` rule continues to work after the broader conflict rule is added.
- [ ] Invalid persisted combinations are repaired during `TimerViewModel` initialization.
- [ ] Repaired startup state is written back to preferences so the invalid pair does not return on the next launch.
- [ ] Existing valid chaining behavior for `timer0 <-> timer2` and `timer1 <-> timer2` remains unchanged.
- [ ] `./gradlew testDebugUnitTest` passes with new `TimerViewModel` assertions.
- [ ] `notes/_research/timer_chaining.md` documents the no-dual-exit rule and startup normalization behavior.
