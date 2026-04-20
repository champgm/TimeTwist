# Crown turns CircularSlider — research note

## Problem
On `EditScreen`, the Wear OS rotating crown only mutated `seconds` and used a raw
`verticalScrollPixels.toLong()` add. Result: huge per-event jumps, no support for
minutes, and the `CircularSlider` dragger never moved because it kept its own
`draggerState` after first init.

## Approach
1. **Reactive `CircularSlider`** — added a `LaunchedEffect(originalValue, isInitialized)`
   that re-runs `timeValueToPosition(...)` and writes a fresh `DraggerState` whenever
   the external value changes. The `detectDragGestures` path is untouched, so dragging
   keeps owning the dragger during a gesture.
2. **Lifted rotary handler** — moved `focusRequester` / `focusable` / `onRotaryScrollEvent`
   off the seconds-only inner Box and onto the outer `EditScreen` Box. The handler
   dispatches to `seconds` or `minutes` based on `focusedField`, so tapping a field
   switches what the crown drives.
3. **Pure `applyRotaryDelta`** — accumulator-based scaler in `RotaryInput.kt`. Pure
   Kotlin (no Android imports) so it is JVM-testable. Wraps with `((v % 60) + 60) % 60`.

## Tuning constant
`ROTARY_PIXELS_PER_UNIT = 24f` — one detent on tested Wear devices delivers roughly
20–30 px per `onRotaryScrollEvent`, so 24 produces ~1 unit per detent without
feeling sluggish or skipping values during fast spins. Sub-threshold input is
preserved in the accumulator across events.

## Touchpoints
- `app/src/main/java/com/cgm/timetwist/ui/CircularSlider.kt` — reactive sync.
- `app/src/main/java/com/cgm/timetwist/ui/EditScreen.kt` — rotary handler on outer Box.
- `app/src/main/java/com/cgm/timetwist/ui/RotaryInput.kt` — `applyRotaryDelta` + constant.
- `app/src/test/java/com/cgm/timetwist/ui/RotaryInputTest.kt` — accumulator + wrap tests.
