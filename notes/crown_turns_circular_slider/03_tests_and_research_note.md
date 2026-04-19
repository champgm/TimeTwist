# Phase 03 — Tests and research note

## Intent

- Lock in the accumulator/wrap math for `applyRotaryDelta` with JVM unit tests.
- Record the final approach and tuning constant in `notes/_research/` so future edits know why `ROTARY_PIXELS_PER_UNIT` is 24f.
- Confirm the full build + test suite is green end-to-end.

## Prerequisites from previous phases

- Phase 01: `CircularSlider` reactive sync landed.
- Phase 02: `app/src/main/java/com/cgm/timetwist/ui/RotaryInput.kt` exists with `applyRotaryDelta(current: Long, accumulatorPx: Float, scrollPx: Float): Pair<Long, Float>` and `const val ROTARY_PIXELS_PER_UNIT: Float = 24f`.
- Phase 02: rotary handler wired on outer `EditScreen` Box.

## Handoff notes

- Final phase — nothing downstream.
- Produce a clean `git status` at end: only the four touched files plus new test and research note.
- Expected terminal line on success: `BUILD SUCCESSFUL` from both Gradle invocations.

## Concrete edits

- [ ] Create `app/src/test/java/com/cgm/timetwist/ui/RotaryInputTest.kt`
  - Add:
    ```
    package com.cgm.timetwist.ui

    import org.junit.Assert.assertEquals
    import org.junit.Test

    class RotaryInputTest {
        @Test
        fun `accumulates below threshold without change`() {
            val (next, rem) = applyRotaryDelta(current = 10L, accumulatorPx = 0f, scrollPx = 10f)
            assertEquals(10L, next)
            assertEquals(10f, rem, 0.0001f)
        }

        @Test
        fun `one unit emitted at threshold`() {
            val (next, rem) = applyRotaryDelta(current = 10L, accumulatorPx = 0f, scrollPx = ROTARY_PIXELS_PER_UNIT)
            assertEquals(11L, next)
            assertEquals(0f, rem, 0.0001f)
        }

        @Test
        fun `multiple units emitted from single large event`() {
            val (next, rem) = applyRotaryDelta(current = 0L, accumulatorPx = 0f, scrollPx = ROTARY_PIXELS_PER_UNIT * 3)
            assertEquals(3L, next)
            assertEquals(0f, rem, 0.0001f)
        }

        @Test
        fun `wraps past 60 forward`() {
            val (next, _) = applyRotaryDelta(current = 59L, accumulatorPx = 0f, scrollPx = ROTARY_PIXELS_PER_UNIT * 2)
            assertEquals(1L, next)
        }

        @Test
        fun `wraps past 0 backward`() {
            val (next, _) = applyRotaryDelta(current = 0L, accumulatorPx = 0f, scrollPx = -ROTARY_PIXELS_PER_UNIT)
            assertEquals(59L, next)
        }

        @Test
        fun `accumulator carries remainder across calls`() {
            val (n1, r1) = applyRotaryDelta(current = 0L, accumulatorPx = 0f, scrollPx = ROTARY_PIXELS_PER_UNIT * 0.6f)
            assertEquals(0L, n1)
            val (n2, _) = applyRotaryDelta(current = n1, accumulatorPx = r1, scrollPx = ROTARY_PIXELS_PER_UNIT * 0.6f)
            assertEquals(1L, n2)
        }
    }
    ```
  - Avoid: any Android or Compose imports.

- [ ] Create `notes/_research/crown_turns_circular_slider.md`
  - Add a short note with: problem, approach (reactive `LaunchedEffect` + lifted rotary handler + pure `applyRotaryDelta`), chosen `ROTARY_PIXELS_PER_UNIT = 24f` (reasoning: ~1 detent ≈ 20–30 px on tested Wear devices), and the two file-level touchpoints (`CircularSlider.kt`, `EditScreen.kt`, `RotaryInput.kt`).

## Search and confirm steps

- `rg -n "applyRotaryDelta" app/src` — 1 decl, 1 callsite, 6 test refs.
- `rg -n "ROTARY_PIXELS_PER_UNIT" app/src` — 1 decl, 1 callsite, ≥3 test refs.
- `rg -n "onRotaryScrollEvent" app/src/main/java` — exactly 1 match, inside `EditScreen.kt` outer Box.
- `rg -n "verticalScrollPixels" app/src/main/java` — exactly 1 match, inside new handler, no `.toLong()`.
- `ls app/src/test/java/com/cgm/timetwist/ui/` — contains `RotaryInputTest.kt`.
- `ls notes/_research/ | grep crown_turns_circular_slider` — new research note present.
- Confirm-before-edit: `org.junit.Assert` path matches existing tests — verify with `rg -n "org.junit" app/src/test/java/com/cgm/timetwist/service/CircularSliderTest.kt`.

## Edge cases and failure modes

- Float rounding: using `0.0001f` tolerance on remainder comparisons to avoid flakiness.
- `scrollPx = -0.0f` / `+0.0f`: both handled; remainder stays at 0f.
- Very large event (e.g. `scrollPx = 10_000f`): `delta` may exceed Long range only above `~Long.MAX_VALUE * 24`, not reachable; safe.
- Tests must not depend on Android framework — Compose state not touched.
- If `ROTARY_PIXELS_PER_UNIT` is later tuned: tests reference the constant directly, so they stay valid.
- Test name collision with `CircularSliderTest`: none — different package/class.
- JaCoCo report: unchanged task, no extra config needed.
- CI: existing `./gradlew` invocations are sufficient.

## Tests / validation

- Command: `./gradlew :app:testDebugUnitTest` — all tests pass, including new `RotaryInputTest` (6 tests).
- Command: `./gradlew :app:assembleDebug` — builds successfully.
- Optional: `./gradlew :app:jacocoTestReport` if the repo generates coverage — verify `RotaryInput.kt` appears in the report.
- Manual re-verification of phase 02 manual checks after test changes land.
- Expected test output contains: `RotaryInputTest > accumulates below threshold without change PASSED`.

## Definition of done

- [ ] `RotaryInputTest.kt` added with 6 passing tests.
- [ ] `notes/_research/crown_turns_circular_slider.md` added with approach + constant rationale.
- [ ] `./gradlew :app:testDebugUnitTest` green.
- [ ] `./gradlew :app:assembleDebug` green.
- [ ] No stray `Log.e("Rotary", ...)` remains; one `Log.d("Rotary", ...)` in `EditScreen.kt`.
- [ ] `rg -n "verticalScrollPixels" app/src/main/java` returns exactly 1 match.
- [ ] `rg -n "selectedColumn" app/src/main/java` returns 0 matches.
- [ ] Manual crown-scrub on device/emulator moves dragger smoothly for both minutes and seconds.
