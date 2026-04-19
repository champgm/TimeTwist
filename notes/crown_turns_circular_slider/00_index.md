# Plan: crown_turns_circular_slider

## Scope

- When `EditScreen` is open, rotating the Wear OS crown (rotary input) must move the `CircularSlider` dragger for the currently focused field (minutes or seconds), identical in effect to dragging.
- Today `EditScreen.kt:154-162` has an `onRotaryScrollEvent` only on the seconds Box that adds raw `verticalScrollPixels` to the underlying `Long`, causing huge jumps and only working when seconds is focused.
- `CircularSlider.kt` holds the dragger position in an internal `draggerState` initialized once in `onGloballyPositioned`; external updates to `originalValue` do not move the dragger.
- The fix is two-sided: make `CircularSlider` reactive to `originalValue`, and lift rotary handling to the EditScreen root with a sensitivity-scaled accumulator that wraps 0–60 for both fields.
- No architecture change. No new components, modules, or dependencies. Existing `timeValueToPosition(...)` is reused for the dragger sync.
- A focused (non-instrumented) unit test extension covers the wrap/accumulator math used by the rotary handler.

## Phases

1. `01_circularslider_reactive_sync.md` — Make `CircularSlider` observe `originalValue` and reposition its dragger via `timeValueToPosition(...)` whenever the external value changes (drag path untouched).
2. `02_editscreen_rotary_handler.md` — Move rotary focus/handler to the outer EditScreen Box, add accumulator + sensitivity, dispatch to minutes or seconds based on `focusedField` with mod‑60 wrap.
3. `03_tests_and_research_note.md` — Add JVM unit tests for the accumulator/wrap helper, update `notes/_research/`, and run the full test + build verification.

## Cross‑phase conventions

### Naming
- Constant: `ROTARY_PIXELS_PER_UNIT` (Float) — sensitivity scaler in `EditScreen.kt` (top‑level `private const val`).
- Helper function: `applyRotaryDelta(current: Long, accumulatorPx: Float, scrollPx: Float): Pair<Long, Float>` — pure function in a new file `app/src/main/java/com/cgm/timetwist/ui/RotaryInput.kt`. Returns `(newValue 0..59, newAccumulator)`.
- State in `EditScreen`: `var rotaryAccumulator by remember { mutableFloatStateOf(0f) }`.
- Do not rename existing symbols: `CircularSlider`, `CircularSliderState`, `DraggerState`, `timeValueToPosition`, `FocusedField`, `focusRequester`.

### Config / constants
- `ROTARY_PIXELS_PER_UNIT = 24f`. One constant only; tuned once. Declared in `RotaryInput.kt` and imported where needed.
- Value range is `0..59` (mod 60). Wrap rule: `((v % 60) + 60) % 60`.

### Logging
- Keep the single existing `Log.e("Rotary", ...)` call style but demote to `Log.d("Rotary", ...)` in the new handler and include `focusedField`, raw `verticalScrollPixels`, and `delta` in one formatted string.
- No new log tags.

### Tests
- JUnit 4 (matches existing `app/src/test/java/com/cgm/timetwist/service/CircularSliderTest.kt`).
- New test file: `app/src/test/java/com/cgm/timetwist/ui/RotaryInputTest.kt`.
- Test naming: backtick descriptive — e.g. `` `accumulates below threshold without change` ``.
- Run with `./gradlew :app:testDebugUnitTest`.

## Definition of done

- [ ] Crown rotation on `EditScreen` moves the `CircularSlider` dragger smoothly for whichever field (minutes/seconds) is focused.
- [ ] Tapping a different field switches which value the crown drives without losing focus for rotary events.
- [ ] Dragging the slider still works exactly as before (no regression in `detectDragGestures` path).
- [ ] Values wrap 59 → 0 and 0 → 59 cleanly via the crown, matching the drag wrap behavior.
- [ ] No raw `verticalScrollPixels.toLong()` addition remains anywhere in `EditScreen.kt`.
- [ ] `./gradlew :app:testDebugUnitTest` passes including new `RotaryInputTest`.
- [ ] `./gradlew :app:assembleDebug` succeeds.
- [ ] `notes/_research/crown_turns_circular_slider.md` written with final approach and sensitivity constant.
