# Phase 02: Validation And Note Alignment

## Intent
- Validate that the new visibility rule and light-mode color tweak behave as intended without regressing timer interaction.
- Align the concise chaining note with the new rule that `DEFAULT` indicators are hidden in regular mode.
- Keep this phase limited to verification and documentation, not new runtime behavior.

## Prerequisites From Previous Phases
- `WearApp.kt` now hides `DEFAULT` indicators in regular mode and keeps them visible in edit mode.
- The light-mode transition-button background now uses the same regular-mode red as the top-left controls.
- `TransitionButton.kt` still controls non-interactive rendering via `enabled`.

## Handoff Notes
- Do not invent a new UI test harness for this appearance tweak.
- If no existing Compose UI tests exist, record that fact and rely on JVM tests plus manual verification.
- Update only the canonical concise note in `notes/_research/timer_chaining.md`, not the original long-form prompt in `notes/timer_chaining.md`.

## Concrete Edits
- [ ] Change [notes/_research/timer_chaining.md](/mnt/nvme/github/TimeTwist/notes/_research/timer_chaining.md)
  - Replace the current statement that transition indicators remain visible in regular mode and edit mode.
  - State that both controls remain visible in edit mode, including `DEFAULT`.
  - State that regular mode shows only non-default transition indicators.
  - State that regular-mode indicators remain non-interactive.
  - Add one brief note that light-mode indicators use the same red fill as the top-left controls, if the note already describes UI appearance decisions; otherwise keep the note focused on visibility and interaction only.
- [ ] Avoid changes to test files unless a search finds an existing UI-adjacent test seam
  - Candidate path 1: [app/src/test/java/com/cgm/timetwist/ui/](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/ui/)
  - Candidate path 2: [app/src/androidTest/java/com/cgm/timetwist/ui/](/mnt/nvme/github/TimeTwist/app/src/androidTest/java/com/cgm/timetwist/ui/)
  - Path-selection rule: only add automated UI coverage if a search confirms Compose test infrastructure already exists; otherwise skip new automated UI tests for this pack.

## Search And Confirm Steps
- `rg -n "visible|edit mode|regular mode|interactive" notes/_research/timer_chaining.md notes/timer_chaining.md`
- `rg -n "Runtime Rules|Routing Semantics|Alert Policy" notes/_research/timer_chaining.md`
- `rg -n "composeTestRule|createComposeRule|createAndroidComposeRule|androidTest" app/src/test app/src/androidTest`
- `rg -n "WearApp\\(|TransitionButton0To2|TransitionButton1To2" app/src/test app/src/androidTest`
- `rg -n "testDebugUnitTest|assembleDebug" notes/transition_buttons/00_index.md notes/transition_buttons_still_visible/00_index.md`
- `rg -n "googleRed|transitionButtonBackground|DEFAULT" app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
- Confirm before edit that `notes/_research/timer_chaining.md` is still the canonical concise behavior summary for transition UI behavior.
- Confirm before edit that there is still no meaningful Compose UI test infrastructure in the repo.

## Edge Cases And Failure Modes
- The note must not imply that regular mode still shows `DEFAULT` indicators.
- The note must distinguish visibility from interactivity clearly.
- A manual test pass must check the case where one indicator is hidden and the other is shown.
- Manual verification should check that hidden indicators do not leave obvious empty artifacts at the segment midpoint.
- Manual verification should cover both dark mode and light mode because the new red fill applies only in light mode.
- If no UI test seam exists, avoid adding a fake ViewModel test for what is really a composition rule.

## Tests / Validation
- Run `ANDROID_HOME=/home/mint/Android/Sdk ANDROID_SDK_ROOT=/home/mint/Android/Sdk GRADLE_USER_HOME=/tmp/timetwist-gradle ./gradlew testDebugUnitTest`
  - Expected result: the JVM unit suite passes unchanged or with only narrowly justified existing-infra assertions.
- If Compose UI tests already exist, run the smallest relevant command that exercises the updated watch-face visibility behavior.
  - Expected result: the test proves `DEFAULT` indicators are absent in regular mode and non-default indicators remain present but passive.
- Manual verification
  - Set both transitions to `DEFAULT`, leave edit mode, and inspect the watch face.
  - Expected result: no transition indicators are shown.
  - Set only `transition_0_2` to a non-default state, leave edit mode, and inspect the watch face.
  - Expected result: only the `0-2` indicator is shown.
  - Set only `transition_1_2` to a non-default state, leave edit mode, and inspect the watch face.
  - Expected result: only the `1-2` indicator is shown.
  - With a visible non-default indicator in regular mode, tap the timer quadrants near it.
  - Expected result: timer interaction still works normally and the indicator does not cycle.
  - Compare light mode versus dark mode with a visible indicator.
  - Expected result: light mode uses the red fill, dark mode keeps the existing dark fill, and arrows remain legible in both.

## Definition Of Done
- [ ] Validation confirms `DEFAULT` indicators are hidden in regular mode.
- [ ] Validation confirms both indicators remain visible in edit mode for configuration.
- [ ] Validation confirms visible regular-mode indicators remain non-interactive.
- [ ] Validation confirms timer taps still work near visible indicators.
- [ ] Any automated test added reuses existing repo infrastructure.
- [ ] No new UI test framework was introduced solely for this tweak.
- [ ] `notes/_research/timer_chaining.md` matches the shipped visibility behavior.
- [ ] `ANDROID_HOME=/home/mint/Android/Sdk ANDROID_SDK_ROOT=/home/mint/Android/Sdk GRADLE_USER_HOME=/tmp/timetwist-gradle ./gradlew testDebugUnitTest` is the recorded validation command.
