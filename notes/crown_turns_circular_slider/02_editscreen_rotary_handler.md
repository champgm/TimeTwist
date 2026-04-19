# Phase 02 — EditScreen rotary handler

## Intent

- Lift rotary handling off the seconds-only Box and onto the outer EditScreen `Box`, so the crown works regardless of which field is focused.
- Replace raw `verticalScrollPixels.toLong()` with a sensitivity-scaled accumulator that emits integer deltas.
- Route deltas to `seconds` or `minutes` based on `focusedField`, with mod‑60 wrap.
- Acquire focus automatically on screen entry so the crown is live without the user tapping.
- Keep the tap-to-switch-focus behavior on the minutes/seconds inner Boxes intact.

## Prerequisites from previous phases

- Phase 01 complete: `CircularSlider` responds to `originalValue` changes.
- Existing files: `app/src/main/java/com/cgm/timetwist/ui/EditScreen.kt`, `FocusedField` enum used as `FocusedField.MINUTES`, `FocusedField.SECONDS`.

## Handoff notes

- When manually exercising, look for logcat lines with tag `Rotary`, e.g. `D/Rotary: field=SECONDS raw=48.0 delta=2`.
- Leaving this phase: `EditScreen.kt` must contain no `verticalScrollPixels.toLong()` call, no `.onRotaryScrollEvent` on the seconds inner Box, and no leftover `focusRequester`/`focusable` on that inner Box.

## Concrete edits

- [ ] Create `app/src/main/java/com/cgm/timetwist/ui/RotaryInput.kt`
  - Add:
    ```
    package com.cgm.timetwist.ui

    const val ROTARY_PIXELS_PER_UNIT: Float = 24f

    fun applyRotaryDelta(
        current: Long,
        accumulatorPx: Float,
        scrollPx: Float,
    ): Pair<Long, Float> {
        val total = accumulatorPx + scrollPx
        val delta = (total / ROTARY_PIXELS_PER_UNIT).toInt()
        val remainder = total - delta * ROTARY_PIXELS_PER_UNIT
        val next = ((current + delta) % 60 + 60) % 60
        return next to remainder
    }
    ```
  - Avoid: any Android/Compose imports — pure Kotlin so it is JVM-testable.

- [ ] `app/src/main/java/com/cgm/timetwist/ui/EditScreen.kt`
  - Add: import `androidx.compose.runtime.mutableFloatStateOf`.
  - Change: declare `var rotaryAccumulator by remember { mutableFloatStateOf(0f) }` alongside the other `remember` state near the top of `EditScreen`.
  - Change: on the outer `Box` at line 68, append modifier chain:
    ```
    .focusRequester(focusRequester)
    .focusable()
    .onRotaryScrollEvent { event ->
        val (next, remainder) = applyRotaryDelta(
            current = when (focusedField) {
                FocusedField.SECONDS -> seconds
                FocusedField.MINUTES -> minutes
            },
            accumulatorPx = rotaryAccumulator,
            scrollPx = event.verticalScrollPixels,
        )
        rotaryAccumulator = remainder
        when (focusedField) {
            FocusedField.SECONDS -> seconds = next
            FocusedField.MINUTES -> minutes = next
        }
        Log.d("Rotary", "field=$focusedField raw=${event.verticalScrollPixels} delta=${next - (if (focusedField == FocusedField.SECONDS) seconds else minutes)}")
        true
    }
    ```
    Note: the log line is allowed to be simplified; the constraint is one `Log.d("Rotary", ...)` call, not two.
  - Remove: `.focusRequester(focusRequester)`, `.focusable()`, and `.onRotaryScrollEvent { ... }` from the seconds inner Box (currently lines ~151–162).
  - Remove: the now-unused `selectedColumn` / `LaunchedEffect(selectedColumn)` block if no longer needed — keep only the `focusRequester.requestFocus()` side effect via a `LaunchedEffect(Unit) { focusRequester.requestFocus() }`.
  - Avoid: changing `FocusedField` enum, `CircularSlider` call sites, or button handlers.
  - Avoid: reading `seconds`/`minutes` AFTER assignment for the log — capture delta from `applyRotaryDelta` instead. Cleaner form:
    ```
    val prev = if (focusedField == FocusedField.SECONDS) seconds else minutes
    // ... assign next ...
    Log.d("Rotary", "field=$focusedField raw=${event.verticalScrollPixels} delta=${next - prev}")
    ```

## Search and confirm steps

- `rg -n "onRotaryScrollEvent" app/src/main/java/com/cgm/timetwist` — confirm the single existing callsite in `EditScreen.kt` that this phase moves.
- `rg -n "verticalScrollPixels" app/src/main/java` — after edits, must return 1 match (inside the new handler) and no `.toLong()` form.
- `rg -n "focusRequester" app/src/main/java/com/cgm/timetwist/ui/EditScreen.kt` — after edits, `focusRequester(...)` appears on the outer Box only.
- `rg -n "FocusedField\." app/src/main/java/com/cgm/timetwist/ui/EditScreen.kt` — confirm `MINUTES` and `SECONDS` usage is unchanged.
- `rg -n "selectedColumn" app/src/main/java/com/cgm/timetwist/ui/EditScreen.kt` — after edits, 0 matches (dead code removed).
- `rg -n "ROTARY_PIXELS_PER_UNIT" app/src/main/java` — confirm single declaration in `RotaryInput.kt`.
- Confirm-before-edit: `onRotaryScrollEvent` returns `true` to indicate consumption — preserve that. The outer `Box` does not currently set `contentAlignment` of `Alignment.Center` by accident? It does (line 72). Preserve that and just append modifiers.
- Confirm-before-edit: `focusable()` on a `Box` with `fillMaxSize()` will not steal clicks from inner `clickable { }` handlers in the minutes/seconds Boxes on Wear OS — Compose propagates pointer events regardless of `focusable`. If a regression is observed in manual testing, revert to placing rotary on a dedicated invisible sibling Box.

## Edge cases and failure modes

- Crown spins past 0 going negative: `((current + delta) % 60 + 60) % 60` wraps correctly for negative `delta`.
- Very fast spin delivering large `scrollPx`: accumulator + divide handles any magnitude; `delta` can exceed 60 and the mod wraps.
- `scrollPx` == 0.0 event: accumulator unchanged, no state write, returns `true`.
- Field switched mid-spin: the accumulator still holds remainder from the previous field; first event after switch may feel slightly "primed" — acceptable, and resetting on switch would cause a lost unit.
- Focus lost when navigating away: `LaunchedEffect(Unit)` only runs once per composition; back-navigation re-enters with a fresh state, requesting focus again.
- User taps seconds/minutes Box: `focusedField` changes; the outer Box retains rotary focus (inner `clickable` handlers do not steal it).
- Haptics: none added in this phase.
- Accessibility: crown is the primary input on Wear; no talkback regression expected since `focusable()` on the root Box is standard.

## Tests / validation

- Command: `./gradlew :app:testDebugUnitTest` (existing suite plus the new `RotaryInputTest` added in phase 03).
- Command: `./gradlew :app:assembleDebug` must succeed.
- Manual on a Wear emulator or device:
  - Enter an edit screen (any color).
  - Rotate crown slowly — seconds dragger moves ~1 unit per detent; number text matches; dragger ring moves in sync.
  - Tap minutes Box — crown now drives minutes; seconds frozen.
  - Rotate crown backward through 0 — value wraps to 59 without jump.
  - Drag dragger directly — still works; crown still works after drag releases.
- Expected log during spin: `D/Rotary: field=SECONDS raw=<float> delta=<int>` repeating; no `Log.e` entries.

## Definition of done

- [ ] `RotaryInput.kt` created with `applyRotaryDelta` and `ROTARY_PIXELS_PER_UNIT`.
- [ ] Rotary handler on outer Box only; inner seconds Box has no rotary / focus modifiers.
- [ ] `selectedColumn` dead code removed.
- [ ] `focusRequester.requestFocus()` fires on screen entry.
- [ ] No `verticalScrollPixels.toLong()` anywhere in the repo.
- [ ] Crown drives minutes AND seconds based on `focusedField`.
- [ ] Wrap past 0/60 works in both directions.
- [ ] Drag path still works (no regression).
