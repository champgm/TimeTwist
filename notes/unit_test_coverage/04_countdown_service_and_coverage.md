# Phase 04: CountdownService And Coverage

## Intent
- Add regression tests for the foreground service countdown and alert rules.
- Verify the timer’s recurring alert cadence matches the behavior described in the README and current code.
- Keep service tests JVM-only with `Robolectric` plus small helper seams from phase 01.
- Add coverage reporting once the core suite exists.
- Document the resulting test strategy for future contributors.

## Prerequisites From Previous Phases
- `CountdownService` has testable helper seams or internal helpers for alert and formatting logic.
- Core utility and `TimerViewModel` tests are already green under `./gradlew testDebugUnitTest`.
- Any fake time or alert helper introduced earlier is available for reuse or easy recreation in service tests.

## Handoff Notes
- Focus assertions on observable service behavior: alert decisions, service start inputs, and notification/ongoing setup that does not crash under `Robolectric`.
- README currently says one cadence while code currently uses `3` and `10` second intervals with a `60000` threshold; treat that mismatch as something to resolve explicitly, not ignore.
- If you change the production cadence to match intended behavior, update tests and docs in the same phase.
- Keep coverage tooling lightweight; do not add CI if none exists yet.

## Concrete Edits
- [ ] Create `app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt`
  - Add tests for time formatting output if the formatting helper is exposed for direct testing.
  - Add tests that `intervalStuff = false` suppresses periodic alerts before completion.
  - Add tests for periodic alert decisions above and below the threshold used by production.
  - Add a completion-path test that verifies the final alert path triggers when time reaches zero.
  - Add a startup test that exercises `onStartCommand` with intent extras and confirms the service sets up foreground/ongoing state without crashing.
  - Avoid asserting brittle log output strings.
- [ ] Change [app/src/main/java/com/cgm/timetwist/service/CountdownService.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt) only if needed to finish seam extraction or to fix a confirmed cadence bug.
  - If cadence values are changed, keep the constants explicit and update README wording in the same phase.
  - Avoid broad notification refactors unrelated to testability or the cadence mismatch.
- [ ] Change [README.md](/mnt/nvme/github/TimeTwist/README.md)
  - Update the timer-vibration description if tests confirm the code behavior differs from the current prose.
  - Keep the rest of the product description unchanged.
- [ ] Change [app/build.gradle.kts](/mnt/nvme/github/TimeTwist/app/build.gradle.kts)
  - Add coverage reporting via `jacoco` or `kover`.
  - Expose one documented coverage task suitable for local use.
  - Avoid enforcing a hard threshold in the same phase unless the resulting number is already comfortably stable.
- [ ] Create `notes/_research/unit_test_coverage.md`
  - Summarize why the suite uses JVM tests plus `Robolectric`.
  - Record any production seams added for deterministic testing.
  - Record any resolved mismatch between README cadence text and implemented service behavior.

## Search And Confirm Steps
- `rg -n "SMALL_ALERT_INTERVAL|BIG_ALERT_INTERVAL|60000|intervalStuff|alertDevice|smallAlert|bigAlert" app/src/main/java/com/cgm/timetwist/service/CountdownService.kt README.md`
- `rg -n "onStartCommand|startForeground|OngoingActivity|NotificationCompat|updateStatus|formatTime" app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`
- `rg -n "SoundPoolManager|VibrationManager" app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`
- `rg -n "jacoco|kover" app/build.gradle.kts gradle.properties settings.gradle.kts`
- `rg -n "testDebugUnitTest" AGENTS.md README.md notes/unit_test_coverage`
- `rg -n "Robolectric|ShadowService|shadowOf\\(|ShadowNotificationManager" app/src/test/java`
- Confirm before edit whether the intended cadence is the README wording or the constants in `CountdownService.kt`.
- Confirm before edit whether a `notes/_research/` directory already exists; create it only if missing.

## Edge Cases And Failure Modes
- Service tests can become flaky if they rely on real coroutine delays instead of scheduler control.
- Notification or ongoing-activity assertions may be brittle if they depend on exact framework internals rather than observable setup success.
- Sound and vibration singletons can cause side effects in tests; route assertions through extracted alert decisions or controllable seams.
- A cadence mismatch between docs and code can lead to “passing” tests that lock in the wrong behavior.
- Coverage plugins can add noisy configuration if both `jacoco` and `kover` are introduced; choose one.
- Coverage reports should exclude no files unless there is a concrete reason.
- Updating README without aligning tests and code will recreate drift quickly.

## Tests / Validation
- Run `./gradlew testDebugUnitTest`.
  - Expected result: service tests pass along with earlier phases.
- Run the new coverage task after the suite is green.
  - Expected result: an HTML or XML report is generated locally without requiring a device.
- Add tests that assert:
  - periodic alerts are skipped when `intervalStuff` is disabled
  - alert cadence matches the chosen threshold and interval constants
  - completion triggers the final alert path
  - service startup handles the expected intent extras and initializes foreground state without exceptions
- Manual verification
  - If cadence behavior changed, run the app once and confirm timer vibration/sound feels consistent with the updated README text.
  - Expected observable result: timer alerts match the documented intervals.

## Definition Of Done
- [ ] `CountdownServiceTest.kt` covers cadence, completion, and startup behavior.
- [ ] Any cadence mismatch between docs and code has been resolved explicitly.
- [ ] A single local coverage-reporting task is configured and documented.
- [ ] `notes/_research/unit_test_coverage.md` documents the adopted test strategy and seams.
- [ ] `README.md` matches the tested service behavior.
- [ ] `./gradlew testDebugUnitTest` still passes after service tests and coverage configuration.
