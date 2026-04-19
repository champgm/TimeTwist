# Phase 01 — CircularSlider reactive sync

## Intent

- Make `CircularSlider` reposition its dragger whenever `originalValue` changes after first init.
- Reuse the existing `timeValueToPosition(...)` helper — no new math.
- Preserve the drag path: `detectDragGestures` continues to own updates when the user drags.
- Avoid feedback loops: external updates must not re-emit via `setNewTimeValue`.
- Keep the single-initialization behavior of `sliderState` / `draggerState` for center+radius.

## Prerequisites from previous phases

- None (this is phase 1).
- Expect to find: `app/src/main/java/com/cgm/timetwist/ui/CircularSlider.kt`, `CircularSliderState.kt` with `isInitialized: Boolean`.

## Handoff notes

- Next phase (EditScreen rotary handler) depends on this reactive sync — without it, rotary updates will change the number but the dragger will not move.
- Leave a final `git diff` touching only `CircularSlider.kt`.
- Log snippet to expect when exercising manually: none (this phase adds no log).

## Concrete edits

- [ ] `app/src/main/java/com/cgm/timetwist/ui/CircularSlider.kt`
  - Add: import `androidx.compose.runtime.LaunchedEffect`.
  - Add: inside the `CircularSlider` composable, after `draggerState` declaration and after the `Box(...) { ... }` container that initializes `sliderState`/`draggerState`, insert
    ```
    LaunchedEffect(originalValue, sliderState.value.isInitialized) {
        if (sliderState.value.isInitialized) {
            val newOffset = timeValueToPosition(
                originalValue,
                sliderState.value.center,
                sliderState.value.radius,
                draggerRadiusPx.doubleValue,
            )
            draggerState.value = DraggerState(newOffset, true)
        }
    }
    ```
  - Change: nothing in the existing `detectDragGestures` block — the drag path continues to write `draggerState` directly.
  - Avoid: calling `setNewTimeValue(originalValue)` inside the effect (would loop).
  - Avoid: reading `draggerState.value` inside the effect's key list.

## Search and confirm steps

- `rg -n "fun CircularSlider\(" app/src/main/java/com/cgm/timetwist/ui` — confirm single definition at `CircularSlider.kt:117`.
- `rg -n "timeValueToPosition" app/src/main/java` — confirm signature `(Double, DoubleOffset, Double, Double) -> DoubleOffset` at `CircularSlider.kt:37`.
- `rg -n "DraggerState\(" app/src/main/java` — confirm constructor shape used elsewhere (line ~146, ~204).
- `rg -n "isInitialized" app/src/main/java/com/cgm/timetwist/ui` — confirm `CircularSliderState.isInitialized` exists.
- `rg -n "LaunchedEffect" app/src/main/java/com/cgm/timetwist` — confirm import style used in project.
- `rg -n "onGloballyPositioned" app/src/main/java/com/cgm/timetwist/ui/CircularSlider.kt` — confirm it still performs first-time init and is not moved.
- Confirm-before-edit: the existing `onGloballyPositioned` block does BOTH `sliderState` init AND `draggerState` init. The new `LaunchedEffect` must run AFTER first init (gated on `sliderState.value.isInitialized`), so the first composition's external-value init is still driven by `onGloballyPositioned`; the effect then becomes authoritative on subsequent `originalValue` changes.

## Edge cases and failure modes

- Composition before `onGloballyPositioned` fires: `isInitialized` is false, effect is a no-op — no crash.
- Rapid rotary events: each recomposition with a new `originalValue` cancels and relaunches the effect; result is always the latest.
- Drag in progress while external update arrives: drag writes `draggerState` directly inside `detectDragGestures` — effect may overwrite briefly if parent state catches up, which is acceptable (drag callback sets `originalValue` via `setNewTimeValue` each frame anyway).
- Non-integer `originalValue` (e.g. 59.8): `timeValueToPosition` already handles doubles cleanly.
- Wrap at 60/0: `timeValueToPosition` already uses `(x / 60) * 2π`; values at 60.0 and 0.0 map to the same angle — acceptable.
- Center/radius change after init: current code never re-initializes them; do not attempt to in this phase.
- `originalValue` == NaN: not reachable from EditScreen (Long source), ignore.
- Multiple `CircularSlider` instances in tree: out of scope, EditScreen uses one at a time.

## Tests / validation

- Command: `./gradlew :app:testDebugUnitTest`
- Existing `app/src/test/java/com/cgm/timetwist/service/CircularSliderTest.kt` must still pass.
- No new unit test here (the effect is a Compose integration concern; math is unchanged).
- Manual: build and run debug APK, enter edit screen, verify dragger still renders at the correct starting angle for `seconds=30`, `seconds=0`, `seconds=59`.
- Expected observable: dragger appears exactly where it did before this change for initial composition; no flicker.

## Definition of done

- [ ] `LaunchedEffect(originalValue, sliderState.value.isInitialized)` added in `CircularSlider.kt`.
- [ ] Import for `LaunchedEffect` present.
- [ ] `detectDragGestures` block unchanged.
- [ ] `onGloballyPositioned` block unchanged.
- [ ] `./gradlew :app:testDebugUnitTest` passes.
- [ ] `./gradlew :app:assembleDebug` passes.
- [ ] Manual drag still scrubs correctly.
