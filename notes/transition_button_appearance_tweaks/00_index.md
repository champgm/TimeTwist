# Transition Button Appearance Tweaks Plan Pack

## Scope Summary
- Hide transition indicators in regular mode when their state is `DEFAULT`.
- Keep both transition indicators visible in edit mode, including `DEFAULT`, so users can still discover and configure transitions.
- Keep regular-mode indicators passive and non-interactive when they are shown.
- Change the light-mode transition-button background to the same red used by the top-left quadrant buttons in regular mode.
- Leave dark-mode transition-button styling unchanged.
- Preserve current midpoint placement, button size, arrow artwork, and line colors unless validation reveals a concrete readability issue.
- Preserve all transition persistence, runtime routing, timer chaining, and alert behavior.
- Avoid adding new `TimerViewModel` state, preference keys, or service logic for these appearance-only changes.
- Update the concise chaining research note so it no longer claims `DEFAULT` indicators remain visible in regular mode.

## Phases
- `01_visibility_rules_and_light_mode_styling.md`
  Update `WearApp.kt` so regular mode only renders non-default indicators, while edit mode always renders both, and switch the light-mode transition-button background to the same red source used by the top-left controls.
- `02_validation_and_note_alignment.md`
  Validate the appearance changes through existing repo checks plus manual verification, and revise `notes/_research/timer_chaining.md` so it documents edit-mode visibility versus regular-mode active-only visibility correctly.

## Cross-Phase Conventions
- Naming
  - Keep `TransitionButton0To2`, `TransitionButton1To2`, and `TransitionButtonFrame` as the existing UI seams.
  - Keep `inEditMode` as the only source of truth for editability.
  - Use the existing `TransitionState0To2.DEFAULT` and `TransitionState1To2.DEFAULT` enum entries directly; do not add alias helpers unless a search shows duplication risk.
- Config patterns
  - Do not add or change shared-preference keys, `TimerViewModel` fields, or service extras.
  - Keep visibility decisions in `WearApp.kt`; do not move them into persistence or routing code.
  - Reuse the existing red color source already driving the top-left quadrant buttons in light mode instead of introducing a new color constant.
- Logging and test conventions
  - Prefer existing JVM/unit-test infrastructure only; do not add new Compose UI test infrastructure for this UI tweak.
  - Run validation from repo root with `./gradlew testDebugUnitTest`.
  - Pair automated checks with explicit manual verification in both dark and light mode because these changes are primarily visual.

## Global Definition Of Done
- [ ] Both transition indicators are still visible in edit mode even when their state is `DEFAULT`.
- [ ] In regular mode, each transition indicator is hidden when its state is `DEFAULT`.
- [ ] In regular mode, non-default indicators remain visible and non-interactive.
- [ ] The light-mode transition-button background matches the same red used by the top-left quadrant buttons in regular mode.
- [ ] Dark-mode transition-button styling remains unchanged.
- [ ] No `TimerViewModel`, preference, routing, or service behavior is changed.
- [ ] `./gradlew testDebugUnitTest` passes after the change.
- [ ] `notes/_research/timer_chaining.md` documents the new visibility rule accurately.
