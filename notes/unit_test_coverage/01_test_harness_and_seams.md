# Phase 01: Test Harness And Seams

## Intent
- Add the missing unit-test dependencies required to test Android-backed code on the JVM.
- Keep `testDebugUnitTest` as the primary execution path for the suite.
- Introduce the smallest possible deterministic seams for time and coroutine-driven logic.
- Avoid changing any user-visible timer behavior.
- Leave the codebase ready for tests in later phases without requiring emulator infrastructure.

## Prerequisites From Previous Phases
- None.
- Existing file [app/build.gradle.kts](/mnt/nvme/github/TimeTwist/app/build.gradle.kts) still defines the `app` module and `testDebugUnitTest`.
- Existing files [app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt) and [app/src/main/java/com/cgm/timetwist/service/CountdownService.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt) still own timer orchestration.

## Handoff Notes
- Look for successful dependency resolution during `testDebugUnitTest`; no new source set should be created.
- Watch for any compile errors caused by final/private methods after seam extraction.
- If `Robolectric` complains about SDK configuration, confirm `compileSdk = 35` is unchanged and local Android SDK setup is valid.
- Keep seam names explicit: `timeProvider`, `serviceStarter`, `timerAlerter`, or similar, not generic utility names.

## Concrete Edits
- [ ] Change [app/build.gradle.kts](/mnt/nvme/github/TimeTwist/app/build.gradle.kts)
  - Add `testImplementation` entries for `org.robolectric:robolectric` and `org.jetbrains.kotlinx:kotlinx-coroutines-test`.
  - Add any needed AndroidX test core dependency only if `ApplicationProvider` or equivalent is used by later tests.
  - Avoid duplicate dependency declarations already present in the file.
- [ ] Change [app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt)
  - Add a minimal time-source seam so tests can control values currently read from `System.currentTimeMillis()`.
  - Add a narrow service-start/service-stop seam or helper so tests can inspect intended service launches without relying on real background execution.
  - Keep existing public `startTimer`, `stopTimers`, `getTimer`, and `updateTimerDuration` methods intact.
  - Avoid changing timer IDs, default durations, or preference keys.
- [ ] Change [app/src/main/java/com/cgm/timetwist/service/CountdownService.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt)
  - Extract pure countdown/alert decision logic into testable internal helpers where current private methods are too entangled with Android service lifecycle.
  - Keep `onStartCommand`, `onCreate`, `onDestroy`, and notification behavior intact.
  - Avoid moving the service into a new package or introducing a new architecture layer.
- [ ] Create one shared test helper file under `app/src/test/java/com/cgm/timetwist/` only if later phases need reusable fake time or coroutine wiring.
  - Keep helpers tiny and scoped to test determinism.
  - Avoid speculative helpers not used by later phases.

## Search And Confirm Steps
- `rg -n "System.currentTimeMillis|startService\\(|stopService\\(" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "CoroutineScope|viewModelScope|delay\\(" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "smallAlert|bigAlert|alertDevice|formatTime|updateStatus" app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`
- `rg -n "testImplementation|androidTestImplementation" app/build.gradle.kts`
- `rg -n "Robolectric|kotlinx-coroutines-test|ApplicationProvider" app/src/test/java app/build.gradle.kts`
- `rg -n "saveTimerDetails|getTimerDetails" app/src/main/java/com/cgm/timetwist`
- Confirm before edit that no existing helper already abstracts time reads or service launching.
- Confirm before edit that any extracted helper can stay `internal` or `private` to avoid widening the public API more than necessary.

## Edge Cases And Failure Modes
- Adding `Robolectric` without the right dependency versions can break the whole unit-test configuration.
- Direct constructor changes on `TimerViewModel` can break `viewModel()` usage in `MainActivity`; preserve the default path used by production.
- Over-extracting service logic can create dead code or duplicate branches.
- Time-source seams must keep production behavior identical when tests are not injecting overrides.
- Service-start seams must preserve existing intent extras and start order.
- Coroutines-test integration must not accidentally stop the production `viewModelScope` loop from running in the app.
- Private helper extraction in `CountdownService` must not change when alerts fire at the start or end of the timer.

## Tests / Validation
- Run `./gradlew testDebugUnitTest`.
  - Expected result: build stays green after dependency and seam changes, even before most new tests are added.
- If dependency resolution fails, rerun with the same command after fixing `app/build.gradle.kts`; do not switch to `androidTest`.
- Add or update at least one smoke test in `app/src/test/java` that touches a new seam.
  - Assert a fake or injected time source can drive deterministic output.
  - Assert the seam compiles against existing production entry points.
- Manual verification
  - Start the app on device or emulator only if needed to sanity-check that timers still start and stop as before.
  - Expected observable result: no user-visible behavior change from this phase alone.

## Definition Of Done
- [ ] `app/build.gradle.kts` includes the Android-aware JVM test libraries needed for later phases.
- [ ] `TimerViewModel` no longer depends exclusively on raw `System.currentTimeMillis()` calls in untestable branches.
- [ ] `CountdownService` exposes testable countdown/alert decision logic without changing runtime behavior.
- [ ] Production code still compiles without requiring test-only constructors at call sites.
- [ ] No new modules, flavors, or instrumentation source sets were introduced.
- [ ] `./gradlew testDebugUnitTest` succeeds after the seam work.
