# Phase 02: Pure Logic And Preferences

## Intent
- Expand the current math-only test surface into a reliable fast regression layer.
- Add coverage for time-formatting helpers that affect timer labels.
- Add real persistence tests around timer preference save/load behavior.
- Keep these tests independent from long-running coroutines or service lifecycle details.
- Build confidence in utility behavior before tackling orchestration code.

## Prerequisites From Previous Phases
- `Robolectric` and coroutine-test dependencies exist in [app/build.gradle.kts](/mnt/nvme/github/TimeTwist/app/build.gradle.kts).
- Any minimal time or helper seams from phase 01 compile and keep production behavior unchanged.
- Existing file [app/src/test/java/com/cgm/timetwist/service/CircularSliderTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/service/CircularSliderTest.kt) still exists.

## Handoff Notes
- Prefer separate test files by production concern: slider math, extensions, and timer preference persistence.
- Keep assertions numeric and explicit; avoid broad tolerance unless the math truly requires it.
- Use a real `Context` from `Robolectric` or `ApplicationProvider` for `SharedPreferences`.
- Clear preferences between tests so timer IDs do not leak state across test cases.

## Concrete Edits
- [ ] Change [app/src/test/java/com/cgm/timetwist/service/CircularSliderTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/service/CircularSliderTest.kt)
  - Add wraparound coverage for `rotatedAngleToTimeValue`.
  - Add round-trip tests between `timeValueToPosition`, `positionToRotatedAngle`, and `rotatedAngleToTimeValue`.
  - Add cases that use non-zero dragger radius where the current tests simplify to `0.0`.
  - Avoid weakening existing assertions to make inaccurate math pass.
- [ ] Create `app/src/test/java/com/cgm/timetwist/service/ExtensionsTest.kt`
  - Add tests for `getTime`, `getMinutes`, and `getSeconds`.
  - Cover `0`, sub-minute, one-minute, multi-minute, and hour-plus values as formatted by the current implementation.
  - Avoid rewriting extension behavior in production unless a test reveals a genuine bug.
- [ ] Create `app/src/test/java/com/cgm/timetwist/presentation/TimerPreferencesTest.kt`
  - Test `saveTimerDetails` writes all fields used by `getTimerDetails`.
  - Test `getTimerDetails` returns `null` when no saved duration exists.
  - Test persisted booleans round-trip for repeating, sound, vibration, and `intervalStuff`.
  - Avoid asserting on unrelated `TimeDetails` fields that are not persisted.

## Search And Confirm Steps
- `rg -n "fun timeValueToPosition|fun draggerPositionToAngle|fun positionToRotatedAngle|fun rotatedAngleToTimeValue|fun angleToPosition" app/src/main/java/com/cgm/timetwist/ui/CircularSlider.kt`
- `rg -n "data class DoubleOffset" app/src/main/java/com/cgm/timetwist/ui/CircularSlider.kt`
- `rg -n "fun Long.getTime|fun Long.getMinutes|fun Long.getSeconds" app/src/main/java/com/cgm/timetwist/service/Extensions.kt`
- `rg -n "TIME_TWIST_PREFERENCES|saveTimerDetails|getTimerDetails" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "durationMillis|repeating|vibration|sound|intervalStuff" app/src/main/java/com/cgm/timetwist/service/TimeDetails.kt app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "SharedPreferences|ApplicationProvider|Robolectric" app/src/test/java`
- Confirm before edit that `CircularSliderTest.kt` is the only existing slider test file.
- Confirm before edit that `getTimerDetails` still keys existence off `${timerId}_durationMillis`.

## Edge Cases And Failure Modes
- Angle-to-time math can fail near `0` and `2π`; assert wrap behavior explicitly.
- Using only zero-radius dragger values can hide offset bugs in production slider behavior.
- `getTime()` currently formats minutes and seconds only; values over one hour should reflect current behavior, not an invented new format.
- Preference tests can become flaky if they share the same preference file and do not clear it.
- `getTimerDetails` intentionally ignores unsaved timers; do not turn a missing timer into a default object silently.
- Tests should not assume `startTime`, `elapsedTime`, or `started` are persisted when they are not.

## Tests / Validation
- Run `./gradlew testDebugUnitTest`.
  - Expected result: new utility and persistence tests pass alongside the existing slider suite.
- Add or update these tests:
  - `CircularSliderTest`: assert wraparound and round-trip consistency.
  - `ExtensionsTest`: assert exact string outputs for representative durations.
  - `TimerPreferencesTest`: assert save/load round-trips and missing-value behavior.
- Manual verification
  - No manual device check required if the suite passes; this phase covers pure logic and prefs only.
  - Expected observable result: no production code behavior changes unless a real persistence or formatting bug is fixed.

## Definition Of Done
- [ ] Slider math tests cover wraparound, round-trip, and non-zero dragger-radius cases.
- [ ] Time-format extension tests cover representative duration boundaries.
- [ ] Preference helper tests use a real Android-backed context under JVM tests.
- [ ] Tests assert only fields that production code actually persists.
- [ ] `./gradlew testDebugUnitTest` stays green after adding the new files.
- [ ] No production refactor was introduced in this phase unless required by a failing test.
