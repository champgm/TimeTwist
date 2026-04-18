# Transition Buttons Still Visible Plan Pack

## Scope Summary
- Keep both transition controls visible on the main watch face in both edit mode and regular mode.
- Preserve the current placement at the midpoint of `segment_0_2` and `segment_1_2`.
- Restrict interaction so transition controls only cycle state while global edit mode is active.
- Make regular-mode transition controls act as passive indicators of the currently saved routing state.
- Preserve all existing transition-state persistence, chaining, and alert behavior.
- Avoid introducing new `TimerViewModel` state or preference keys for this UI-only change.
- Prefer a minimal Compose change centered in `WearApp.kt` and `TransitionButton.kt`.
- Ensure visible-but-disabled controls do not block normal timer taps in regular mode.
- Add validation coverage only if the existing test surface can exercise the interaction contract cleanly; otherwise rely on manual verification.
- Update the chaining research note so the documented UI behavior matches the shipped app.

## Phases
- `01_always_render_and_gate_interaction.md`
  Render both transition controls in both modes and add a composable-level interaction flag so regular mode shows indicators without allowing taps.
- `02_validation_and_notes.md`
  Add focused regression coverage where practical, then update the concise chaining note to document always-visible indicators and edit-mode-only interaction.

## Cross-Phase Conventions
- Naming
  - Keep `TransitionButton0To2`, `TransitionButton1To2`, and `TransitionButtonFrame` as the existing UI seams unless a search proves the shared wrapper has already been renamed.
  - Use a single boolean such as `enabled` or `interactive`; do not add parallel “mode” enums for this change.
  - Keep `inEditMode` as the source of truth in `WearApp.kt`.
- Config patterns
  - Do not add or change shared-preference keys, service extras, or `TimerViewModel` persistence logic.
  - Do not alter transition-state enums or chaining resolution.
  - If disabled Wear `Button` semantics interfere with hit-testing, replace the frame implementation with a non-clickable container plus conditional click handling only in edit mode.
- Logging and test conventions
  - Prefer asserting user-visible behavior through existing Kotlin tests only if there is already a suitable seam; do not create heavy UI-test infrastructure for this small change.
  - Run validation from repo root with `./gradlew testDebugUnitTest`.
  - Pair automated checks with explicit manual verification on a round Wear layout because visibility and tap interference are UI-specific.

## Global Definition Of Done
- [ ] Both transition indicators are visible in regular mode and edit mode.
- [ ] Transition indicators only respond to taps while edit mode is active.
- [ ] Leaving edit mode no longer hides the saved transition arrows.
- [ ] Regular-mode indicators do not break normal timer-button interaction.
- [ ] The current midpoint placement and arrow artwork remain unchanged unless required to preserve usability.
- [ ] No new persistence keys, ViewModel fields, or service behavior are introduced for this feature.
- [ ] `./gradlew testDebugUnitTest` passes after the change.
- [ ] `notes/_research/timer_chaining.md` documents that indicators stay visible outside edit mode and are only interactive in edit mode.
