# Phase 02: Validation And Notes

## Intent
- Lock the new UI contract in the smallest practical validation surface.
- Verify the change does not regress timer interaction or transition-state behavior.
- Update the concise research note so future work does not assume the indicators disappear outside edit mode.
- Keep this phase limited to tests, verification, and documentation.

## Prerequisites From Previous Phases
- `WearApp.kt` always renders both transition indicators.
- `TransitionButton.kt` supports non-interactive rendering for regular mode.
- Manual regular-mode behavior is stable enough to document without caveats.

## Handoff Notes
- Prefer extending an existing test file if there is already coverage around watch-face composition; do not introduce a new test framework for this small UI behavior change.
- If no meaningful automated seam exists, record that explicitly and lean on manual verification plus the note update.
- Keep the note update concise and behavior-focused; do not restate the full original product brief.

## Concrete Edits
- [ ] Change [app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt) only if a narrow regression assertion is genuinely needed
  - Avoid adding tests here unless the implementation introduces a new pure-Kotlin seam that belongs in the ViewModel layer.
  - Do not fake UI interaction behavior in ViewModel tests; this feature is about composition and input gating, not state resolution.
- [ ] Change one existing UI-adjacent test location if present
  - Candidate path 1: [app/src/test/java/com/cgm/timetwist/ui/](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/ui/)
  - Candidate path 2: [app/src/androidTest/java/com/cgm/timetwist/ui/](/mnt/nvme/github/TimeTwist/app/src/androidTest/java/com/cgm/timetwist/ui/)
  - Path-selection rule: only add automated UI coverage if a search confirms Compose test infrastructure already exists under one of these paths; otherwise skip automated UI tests for this pack.
  - If such infrastructure exists, add a focused test that renders the watch face with `inEditMode = false` and asserts transition indicators are present but do not invoke the cycling callback.
- [ ] Change [notes/_research/timer_chaining.md](/mnt/nvme/github/TimeTwist/notes/_research/timer_chaining.md)
  - Add that transition indicators remain visible on the main watch face in regular mode.
  - Add that only edit mode makes the indicators interactive.
  - Keep persistence, routing, and alert-policy sections intact unless the implementation required a documented UI-specific nuance.

## Search And Confirm Steps
- `rg -n "composeTestRule|createComposeRule|createAndroidComposeRule|androidTest" app/src/test app/src/androidTest`
- `rg -n "WearApp\\(|TransitionButton0To2|TransitionButton1To2" app/src/test app/src/androidTest`
- `rg -n "timer_chaining" notes/_research notes`
- `rg -n "Runtime Rules|Routing Semantics|Alert Policy" notes/_research/timer_chaining.md`
- `rg -n "visible|edit mode|regular mode|interactive" notes/_research/timer_chaining.md notes/timer_chaining.md`
- `rg -n "testDebugUnitTest|assembleDebug" notes/transition_buttons/00_index.md notes/no_dual_exit_timer2/00_index.md`
- Confirm before edit whether any Compose UI test infrastructure already exists; if it does not, do not invent a new test module for this feature.
- Confirm before edit that `notes/_research/timer_chaining.md` remains the canonical concise behavior summary for timer chaining.

## Edge Cases And Failure Modes
- A documentation-only update without manual verification could encode behavior that still has a tap-interference bug on device.
- A UI test that only checks for presence may miss the more important requirement that regular-mode taps do nothing.
- If no UI test framework exists, trying to add one here would be disproportionate and could distract from the actual feature.
- The note must distinguish “visible” from “interactive”; those are now different concerns.
- Manual verification should cover both dark mode and light mode because disabled visuals may differ.
- The note update must not imply that regular mode allows editing transitions.

## Tests / Validation
- Run `ANDROID_HOME=/home/mint/Android/Sdk ANDROID_SDK_ROOT=/home/mint/Android/Sdk GRADLE_USER_HOME=/tmp/timetwist-gradle ./gradlew testDebugUnitTest`
  - Expected result: the JVM unit suite passes unchanged or with any narrowly added UI-adjacent tests.
- If Compose UI tests already exist, run the smallest relevant existing command that includes the new assertion.
  - Expected result: the watch-face test proves regular-mode indicators are rendered and non-interactive.
- Manual verification
  - Save a non-default `0-2` state and a non-default `1-2` state.
  - Exit edit mode.
  - Expected result: both indicators remain visible in regular mode with the saved arrow states.
  - In regular mode, tap the indicator positions repeatedly.
  - Expected result: neither arrow state changes and no transition reset occurs.
  - In regular mode, start `timer0`, `timer1`, and `timer2` from their usual tap targets.
  - Expected result: visible indicators do not block timer interaction.
  - Repeat the visibility check once in dark mode and once in light mode.
  - Expected result: arrows remain legible in both themes.

## Definition Of Done
- [ ] Validation confirms that transition indicators remain visible in regular mode.
- [ ] Validation confirms that regular-mode taps on indicators do not change transition state.
- [ ] Validation confirms that visible indicators do not break normal timer taps.
- [ ] Any automated test added is narrowly scoped and reuses existing infrastructure.
- [ ] No new UI test framework or architecture was introduced solely for this change.
- [ ] `notes/_research/timer_chaining.md` documents visible-in-regular-mode and interactive-only-in-edit-mode behavior.
- [ ] `ANDROID_HOME=/home/mint/Android/Sdk ANDROID_SDK_ROOT=/home/mint/Android/Sdk GRADLE_USER_HOME=/tmp/timetwist-gradle ./gradlew testDebugUnitTest` is the recorded final validation command.
