# Phase 03: Watch Face Transition Controls

## Intent
- Render the two new transition buttons on the main watch face only while global edit mode is active.
- Place the buttons at the visual midpoint of the `timer0`/`timer2` and `timer1`/`timer2` separators.
- Draw each button’s default and directional states according to the approved arrow semantics.
- Wire button taps into the `TimerViewModel` cycling APIs added earlier.
- Preserve existing timer-button layout, edit-mode toggle behavior, and dark-mode behavior.

## Prerequisites From Previous Phases
- `TimerViewModel` exposes observable state and update APIs for both transition buttons.
- Chaining logic and repeat-conflict handling are already implemented and covered by tests.
- `WearApp` still owns the main quadrant layout and edit-mode state.

## Handoff Notes
- Keep the transition-control UI self-contained in `ui/`; prefer a dedicated composable rather than inlining all canvas/drawing logic in `WearApp`.
- The current watch-face layout is built from two `Row`s in a `Column`; plan placement around that real structure, not around a hypothetical central canvas.
- Use a conservative fixed size first so the controls stay tappable on round watches without obscuring timer text.
- Match the existing color/theme approach by deriving colors from `darkMode`, `inEditMode`, and current surface colors already used in `WearApp`.

## Concrete Edits
- [ ] Change [app/src/main/java/com/cgm/timetwist/ui/WearApp.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/ui/WearApp.kt)
  - Introduce layout structure that allows overlaying the two transition controls at the separator midpoints while preserving the current four-quadrant button arrangement.
  - Render the new controls only when `inEditMode` is `true`.
  - Pass the current transition states and tap handlers from `TimerViewModel` into the new composable(s).
  - Keep entering edit mode stopping active timers as it does today.
  - Avoid changing navigation or timer-button click semantics.
- [ ] Create `app/src/main/java/com/cgm/timetwist/ui/TransitionButton.kt`
  - Implement a circular control that renders its visual state via `Canvas`, `drawBehind`, or an equivalent Compose drawing API.
  - Support the four `0-2` visuals: default horizontal bisector, downward-arrow vertical bisector, upward-arrow vertical bisector, and double-arrow vertical bisector.
  - Support the four `1-2` visuals: default vertical bisector, left-arrow horizontal bisector, right-arrow horizontal bisector, and double-arrow horizontal bisector.
  - Keep line thickness and color explicit and easy to tune during manual verification.
  - Expose parameters for size, colors, orientation family, current state, and `onClick`.
- [ ] Change [app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt) only if a small API rename or state-access change from the UI integration requires it.
  - Avoid introducing Compose UI test infrastructure in this phase unless a search confirms it already exists.

## Search And Confirm Steps
- `rg -n "fun WearApp|inEditMode|darkMode|Row\\(|Column\\(|Box\\(" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
- `rg -n "TriangleShape|buttonPadding|offset\\(" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
- `rg -n "TimerButton\\(" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt app/src/main/java/com/cgm/timetwist/ui/TimerButton.kt`
- `rg -n "Canvas|drawBehind|drawLine|drawCircle|Path" app/src/main/java/com/cgm/timetwist/ui`
- `rg -n "googleBlue|googleGreen|googleYellow|googleRed|black|white|muted" app/src/main/java/com/cgm/timetwist/presentation app/src/main/java/com/cgm/timetwist/ui`
- `rg -n "Button\\(|ButtonDefaults|MaterialTheme" app/src/main/java/com/cgm/timetwist/ui`
- `rg -n "compose.ui.test|createComposeRule|onNode" app/src/test/java`
- Confirm before edit whether a full-screen `Box` wrapper is enough to overlay midpoint controls without refactoring timer buttons into a custom layout.
- Confirm before edit whether any existing resource or composable already draws arrows or circular indicators that can be reused.

## Edge Cases And Failure Modes
- Midpoint placement can drift on round screens if offsets are computed from assumptions rather than the actual composed layout.
- Button size that is too large will cover timer labels; too small will be hard to tap on a watch.
- Default-state line orientation must match the approved spec even though the visual separator itself is implied by quadrant gaps rather than a literal drawn stroke.
- Arrow direction must follow the corrected semantics from `notes/timer_chaining.md`, not earlier conflicting prose.
- Rendering must remain legible in both light and dark modes and while edit-mode colors mute the timer quadrants.
- Overlay controls must not intercept taps outside their visible circular bounds more than the underlying Wear `Button` already does.
- If the watch-face hierarchy is rewrapped in additional `Box` containers, existing padding and weight behavior can shift unexpectedly.

## Tests / Validation
- Run `./gradlew testDebugUnitTest`.
  - Expected result: existing JVM tests remain green after the UI integration.
- Run `./gradlew assembleDebug`.
  - Expected result: the app compiles with the new composable and any additional imports.
- Manual verification on emulator or device:
  - enter edit mode and verify both transition buttons appear
  - verify the buttons disappear outside edit mode
  - tap each button repeatedly and verify the visual cycle order
  - verify `0-2` arrowheads point down for `ZERO_TO_TWO`, up for `TWO_TO_ZERO`, and both for repeat
  - verify `1-2` arrowheads point left for `ONE_TO_TWO`, right for `TWO_TO_ONE`, and both for repeat
  - verify when one button enters `_REPEAT`, tapping the other into `_REPEAT` resets the first to default
  - verify dark mode and light mode both remain readable

## Definition Of Done
- [ ] A dedicated transition-button composable exists under `ui/`.
- [ ] `WearApp` renders both transition buttons only in edit mode.
- [ ] The controls are placed at the intended separator midpoints and remain tappable.
- [ ] Visual state for each directional and repeat case matches the approved arrow semantics.
- [ ] Existing timer buttons, dark-mode toggle, and edit-mode toggle still work.
- [ ] `./gradlew assembleDebug` succeeds after the new UI code lands.
