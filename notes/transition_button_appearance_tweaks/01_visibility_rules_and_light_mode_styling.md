# Phase 01: Visibility Rules And Light Mode Styling

## Intent
- Hide transition indicators in regular mode when they are still in `DEFAULT`.
- Keep both transition indicators rendered in edit mode so users can configure transitions from a clean default state.
- Preserve non-interactive behavior outside edit mode for any indicator that is still shown.
- Switch the light-mode transition-button fill to the same red used by the top-left quadrant controls in regular mode.
- Keep the change localized to the watch-face UI layer.

## Prerequisites From Previous Phases
- `WearApp.kt` currently renders both `TransitionButton0To2(...)` and `TransitionButton1To2(...)` unconditionally.
- `TransitionButton.kt` already supports non-interactive rendering through an `enabled` flag.
- `TransitionState0To2.DEFAULT` and `TransitionState1To2.DEFAULT` are already the persisted default values and must remain so.

## Handoff Notes
- Treat this as a presentation-only change; do not touch `TimerViewModel` transition logic.
- Reuse the current placement and `enabled = inEditMode` wiring.
- Prefer explicit booleans in `WearApp.kt` over adding helper abstractions for this small visibility rule.

## Concrete Edits
- [ ] Change [app/src/main/java/com/cgm/timetwist/ui/WearApp.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/ui/WearApp.kt)
  - Add local booleans for whether each transition indicator should be shown in regular mode, based on `state != DEFAULT`.
  - Keep both indicators always rendered while `inEditMode` is `true`.
  - In regular mode, render `TransitionButton0To2(...)` only when `timerViewModel.transition0To2.value != TransitionState0To2.DEFAULT`.
  - In regular mode, render `TransitionButton1To2(...)` only when `timerViewModel.transition1To2.value != TransitionState1To2.DEFAULT`.
  - Keep `enabled = inEditMode` so visible regular-mode indicators remain passive.
  - Change the light-mode `transitionButtonBackground` color source from the current neutral color to the same regular-mode red used by the top-left quadrant buttons.
  - Keep dark-mode `transitionButtonBackground`, `transitionButtonLine`, size, offsets, and placement unchanged.
- [ ] Avoid changes to [app/src/main/java/com/cgm/timetwist/ui/TransitionButton.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/ui/TransitionButton.kt) unless a search proves the red background or conditional rendering cannot be expressed cleanly from `WearApp.kt`
  - Do not alter arrow drawing, border drawing, click gating, or button shape for this phase.
- [ ] Avoid changes to [app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt)
  - Do not add visibility state or presentation helpers.
  - Do not change transition persistence or cycle behavior.

## Search And Confirm Steps
- `rg -n "transitionButtonBackground|googleRed|mutedGoogleRed|mutedWhite" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
- `rg -n "TransitionButton0To2\\(|TransitionButton1To2\\(" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt app/src/main/java/com/cgm/timetwist/ui/TransitionButton.kt`
- `rg -n "enabled = inEditMode|enabled: Boolean|clickable" app/src/main/java/com/cgm/timetwist/ui`
- `rg -n "TransitionState0To2|TransitionState1To2|DEFAULT" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt app/src/main/java/com/cgm/timetwist/presentation`
- `rg -n "align\\(Alignment.Center\\)|offset\\(x = maxWidth / 4\\)|offset\\(y = maxHeight / 4\\)" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
- `rg -n "backgroundColor = when \\{|else -> googleRed|inEditMode -> mutedGoogleRed" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
- Confirm before edit that `TransitionButton0To2(...)` and `TransitionButton1To2(...)` are only called from `WearApp.kt`.
- Confirm before edit that no other screen depends on `DEFAULT` rendering as an always-visible indicator.

## Edge Cases And Failure Modes
- Hiding `DEFAULT` in regular mode must not also hide the indicators in edit mode.
- A transition set back to `DEFAULT` in edit mode must disappear immediately after leaving edit mode.
- The red light-mode background must not reduce line/arrow contrast below acceptable readability.
- The top-left quadrant uses different reds in edit mode versus regular mode; this change must use the regular-mode red specifically unless a search proves otherwise.
- Conditional composition must not change the midpoint placement of visible non-default indicators.
- Regular-mode visibility rules must work independently for the `0-2` and `1-2` controls.
- A stale import cleanup may be needed if the old neutral background color is no longer referenced in `WearApp.kt`.

## Tests / Validation
- Run `ANDROID_HOME=/home/mint/Android/Sdk ANDROID_SDK_ROOT=/home/mint/Android/Sdk GRADLE_USER_HOME=/tmp/timetwist-gradle ./gradlew testDebugUnitTest`
  - Expected result: the JVM unit suite still passes with no chaining or persistence regressions.
- If the implementation introduces any pure Kotlin helper for visibility decisions inside `WearApp.kt`, keep it private to the file and do not add dedicated unit tests unless there is already an existing UI-adjacent seam.
- Manual verification
  - Put both transition buttons in `DEFAULT`, exit edit mode, and observe the main watch face.
  - Expected result: neither transition indicator is visible in regular mode.
  - Set one transition button to a non-default state, leave the other at `DEFAULT`, then exit edit mode.
  - Expected result: only the active transition indicator remains visible.
  - Enter edit mode again with both transitions at `DEFAULT`.
  - Expected result: both transition controls reappear for configuration.
  - In light mode, compare a visible transition indicator against the top-left quadrant buttons.
  - Expected result: the transition indicator uses the same red background as the top-left regular-mode buttons.
  - In dark mode, verify the transition indicator still uses the current dark background treatment.
  - Expected result: no dark-mode styling regression is visible.

## Definition Of Done
- [ ] `WearApp.kt` renders transition indicators conditionally based on `inEditMode` plus `state != DEFAULT`.
- [ ] Both indicators remain available in edit mode for configuration.
- [ ] Regular-mode `DEFAULT` indicators are hidden.
- [ ] Regular-mode non-default indicators remain visible and non-interactive.
- [ ] Light-mode transition-button background uses the same regular-mode red as the top-left controls.
- [ ] Dark-mode transition-button appearance remains unchanged.
- [ ] No `TimerViewModel` or service code changed for this phase.
