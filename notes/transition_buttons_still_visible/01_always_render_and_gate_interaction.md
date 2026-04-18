# Phase 01: Always Render And Gate Interaction

## Intent
- Keep transition-state arrows visible after exiting edit mode.
- Limit transition-button taps to edit mode without changing transition-state persistence.
- Reuse the existing `WearApp` layout and midpoint offsets instead of redesigning geometry.
- Keep the UI change localized to the watch-face composables.
- Prevent passive indicators from interfering with regular timer usage.

## Prerequisites From Previous Phases
- The main watch face renders transition controls through `TransitionButton0To2(...)` and `TransitionButton1To2(...)` in `WearApp.kt`.
- `TransitionButtonFrame(...)` currently wraps the artwork in a Wear `Button` and always accepts clicks.
- `inEditMode` already exists in `WearApp.kt` and controls whether transition controls are currently shown.

## Handoff Notes
- This is a UI-only behavior change; keep `TimerViewModel` and service logic untouched unless a search proves a UI test seam must be exposed.
- Prefer the smallest API change that lets `WearApp` say “show this control, but don’t let it respond.”
- If a disabled Wear `Button` still captures input in a way that blocks nearby timers, change only the frame implementation, not the public transition-state model.
- Keep the current colors and arrow drawings unless visibility testing shows a real readability issue.

## Concrete Edits
- [ ] Change [app/src/main/java/com/cgm/timetwist/ui/WearApp.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/ui/WearApp.kt)
  - Remove the `if (inEditMode)` wrapper that currently prevents transition controls from composing in regular mode.
  - Keep both transition controls aligned to `Alignment.Center` with the existing `maxWidth / 4` and `maxHeight / 4` offsets.
  - Pass the existing `timerViewModel` state and cycle callbacks unchanged.
  - Pass a new interaction flag derived from `inEditMode` into both transition button composables.
  - Avoid changing timer-button composition order unless needed to prevent z-order or hit-target regressions.
- [ ] Change [app/src/main/java/com/cgm/timetwist/ui/TransitionButton.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/ui/TransitionButton.kt)
  - Add an `enabled` or `interactive` parameter to `TransitionButton0To2(...)` and `TransitionButton1To2(...)`.
  - Thread that parameter through `TransitionButtonFrame(...)`.
  - Update `TransitionButtonFrame(...)` so edit mode remains clickable and regular mode is non-interactive.
  - If `androidx.wear.compose.material.Button(enabled = false, ...)` preserves the desired visuals and does not block surrounding touches, use that path.
  - If disabled `Button` semantics dim the artwork too aggressively or still consume taps in a problematic way, replace the `Button` wrapper with a `Box`/`Surface`-style container and apply clickable behavior only when enabled.
  - Keep the current canvas artwork, line colors, border drawing, and arrow mapping unchanged.
- [ ] Avoid changes to [app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt)
  - Do not add new state for regular-mode visibility.
  - Do not add guards in `cycleTransition0To2()` or `cycleTransition1To2()` for this feature; interaction gating belongs in the UI layer.

## Search And Confirm Steps
- `rg -n "if \\(inEditMode\\)|TransitionButton0To2|TransitionButton1To2" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
- `rg -n "TransitionButtonFrame|Button\\(" app/src/main/java/com/cgm/timetwist/ui/TransitionButton.kt`
- `rg -n "cycleTransition0To2|cycleTransition1To2" app/src/main/java/com/cgm/timetwist`
- `rg -n "maxWidth / 4|maxHeight / 4|Alignment.Center" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
- `rg -n "backgroundColor = transitionButtonBackground|lineColor = transitionButtonLine" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
- `rg -n "enabled *=|interactionSource|clickable" app/src/main/java/com/cgm/timetwist/ui`
- Confirm before edit that `TransitionButton0To2(...)` and `TransitionButton1To2(...)` are only called from `WearApp.kt`.
- Confirm before edit that there is no existing Compose UI test suite in `app/src/androidTest` covering watch-face hit testing.

## Edge Cases And Failure Modes
- A disabled Wear `Button` may apply a default disabled alpha that makes the arrows too faint in light or dark mode.
- A disabled Wear `Button` may still sit on top of the UI and capture touches even if it does not call `onClick`.
- Removing the `if (inEditMode)` wrapper changes composition lifetime; the controls must still reflect live state updates while remaining stable across mode toggles.
- The transition indicators must remain readable when their state is `DEFAULT`, not just when arrows are shown.
- Regular-mode indicators must not accidentally look like active timer buttons.
- Z-order changes could make the indicators hide behind timer quadrants or cover timer text if composition order is altered carelessly.
- Any frame replacement must preserve the current circular shape and canvas drawing bounds.

## Tests / Validation
- Run `ANDROID_HOME=/home/mint/Android/Sdk ANDROID_SDK_ROOT=/home/mint/Android/Sdk GRADLE_USER_HOME=/tmp/timetwist-gradle ./gradlew testDebugUnitTest`
  - Expected result: existing unit tests still pass; no transition-state or chaining regressions.
- If a lightweight unit seam already exists for `TransitionButtonFrame(...)`, add or update tests that assert:
  - regular-mode indicators compose without requiring edit mode
  - the click handler is only wired when the new interaction flag is true
- Manual verification
  - Launch the watch face with at least one non-default transition state saved.
  - Expected result: arrows remain visible after switching from edit mode back to regular mode.
  - Tap both transition indicators in regular mode.
  - Expected result: neither indicator changes state.
  - Tap timer buttons in regular mode near the visible indicators.
  - Expected result: timers still start/stop normally and the indicators do not hijack those taps.
  - Re-enter edit mode and tap each transition indicator.
  - Expected result: the same controls become interactive again and continue cycling through states.

## Definition Of Done
- [ ] `WearApp.kt` composes both transition indicators in both modes.
- [ ] `TransitionButton.kt` exposes a single interaction flag and respects it.
- [ ] Transition indicators remain visible immediately after leaving edit mode.
- [ ] Regular-mode taps on transition indicators do not mutate transition state.
- [ ] Edit-mode taps on transition indicators still cycle states exactly as before.
- [ ] No new persistence or ViewModel logic was added for this visibility change.
