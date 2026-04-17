# Phase 01: Transition State And Persistence

## Intent
- Introduce an explicit model for the two transition buttons and their four states each.
- Persist both transition-button states alongside existing timer preferences.
- Expose transition state from `TimerViewModel` as observable Compose state.
- Add update APIs for cycling or setting transition states without yet changing timer-completion routing.
- Keep the production app behavior unchanged outside of new persisted state.

## Prerequisites From Previous Phases
- None; this is the first implementation phase in this pack.

## Handoff Notes
- Keep the transition model small and local to existing architecture; avoid a new manager class unless a search confirms there is already a home for preference-only state.
- `TimerViewModel` already owns preference loading and timer orchestration; prefer adding transition state there over pushing it into the UI.
- Keep new preference keys explicit and centralized so later tests can assert them directly.
- Do not modify `TimeDetails` for this feature unless a later phase proves it is necessary.

## Concrete Edits
- [ ] Change [app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt)
  - Add explicit transition-state types for the `0-2` and `1-2` buttons.
  - Add persisted preference helpers for loading and saving both transition states through `TIME_TWIST_PREFERENCES`.
  - Add two observable state holders on `TimerViewModel` for the current transition-button values.
  - Load persisted transition-button values during `init` alongside timer details.
  - Add public or internal `TimerViewModel` methods to cycle each button state in UI order and to save the resulting state immediately.
  - Add repeat-state conflict resolution when a button is updated so only one `_REPEAT` state can survive; if a newly selected state is `_REPEAT`, reset the opposite button to `DEFAULT` before persisting.
  - Avoid changing timer completion, service startup, or repeat precedence in this phase.
- [ ] Change [app/src/test/java/com/cgm/timetwist/presentation/TimerPreferencesTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/presentation/TimerPreferencesTest.kt)
  - Add preference round-trip coverage for the new transition keys and default values.
  - Keep existing timer-preference coverage intact.
- [ ] Change [app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt)
  - Add constructor assertions for default transition-button state.
  - Add constructor assertions for loading persisted transition-button state.
  - Add tests for cycling each button through all four states and persisting each update.
  - Add tests for the `_REPEAT` conflict rule where the later-tapped button wins and the other button resets to `DEFAULT`.

## Search And Confirm Steps
- `rg -n "TIME_TWIST_PREFERENCES|saveTimerDetails|getTimerDetails" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "class TimerViewModel|init \\{|loadTimersFromPrefs\\(|updateTimerDuration\\(" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "mutableStateOf\\(" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
- `rg -n "timer0|timer1|timer2" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "SharedPreferences|TIME_TWIST_PREFERENCES|TimerPreferencesTest" app/src/test/java app/src/main/java`
- `rg -n "repeating = true|repeating = false|updateTimerDuration" app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "timer_chaining|transition_button|ZERO_TO_TWO|ONE_TO_TWO" notes app/src/main/java app/src/test/java`
- Confirm before edit that no existing type already models cross-timer transitions.
- Confirm before edit that `TimerViewModel` remains the sole owner of timer preference I/O.

## Edge Cases And Failure Modes
- Transition defaults must be stable even when no preference keys exist yet.
- Saving transition state must not overwrite or regress existing timer duration/repeat/sound/vibration keys.
- The `_REPEAT` conflict rule must work in both directions: `0-2` taking precedence over `1-2` and vice versa.
- Cycling order must be deterministic so UI taps and tests remain aligned.
- A fresh constructor path must not depend on any UI composable having run first.
- Avoid storing raw ordinals if enum order is likely to be fragile; if ordinals are used, keep tests that lock the persisted mapping.
- ViewModel tests must clear preferences between cases or transition-state assertions will bleed across runs.

## Tests / Validation
- Run `./gradlew testDebugUnitTest`.
  - Expected result: updated preference and `TimerViewModel` tests pass with no runtime behavior changes yet.
- Add tests that assert:
  - both transition buttons default to `DEFAULT`
  - persisted transition values are loaded during `TimerViewModel` init
  - cycling `0-2` visits `DEFAULT -> ZERO_TO_TWO -> TWO_TO_ZERO -> ZERO_TWO_REPEAT -> DEFAULT`
  - cycling `1-2` visits `DEFAULT -> ONE_TO_TWO -> TWO_TO_ONE -> ONE_TWO_REPEAT -> DEFAULT`
  - setting one button to `_REPEAT` while the other is already `_REPEAT` resets the older one to `DEFAULT`
- Manual verification
  - Not required in this phase because the watch-face UI is unchanged.
  - Expected observable result if inspected with a debugger: `TimerViewModel` exposes the persisted transition state immediately after app launch.

## Definition Of Done
- [ ] Transition-state types exist in production code with explicit `DEFAULT`, directional, and `_REPEAT` values.
- [ ] `TimerViewModel` exposes observable state for both transition buttons.
- [ ] New transition keys are loaded and saved through the existing preferences file.
- [ ] Transition-state updates enforce the single-`_REPEAT` rule immediately.
- [ ] Existing timer-preference behavior remains intact.
- [ ] `TimerPreferencesTest.kt` and `TimerViewModelTest.kt` cover defaults, persistence, cycling, and repeat-conflict handling.
