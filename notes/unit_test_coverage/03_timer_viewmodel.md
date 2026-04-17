# Phase 03: TimerViewModel

## Intent
- Add regression tests for the app’s main timer orchestration logic.
- Verify `TimerViewModel` loads persisted state, updates timers, and starts exactly one active timer at a time.
- Verify service intents carry the expected extras when timers start.
- Exercise timer completion and repeat behavior with deterministic time advancement.
- Keep the tests JVM-only by using `Robolectric` plus coroutine-test controls.

## Prerequisites From Previous Phases
- Any time-provider or service-launch seam introduced in phase 01 exists and is wired into [TimerViewModel.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt).
- Preference helper tests from phase 02 are green and reflect the current key set.
- `Robolectric` and `kotlinx-coroutines-test` are available in [app/build.gradle.kts](/mnt/nvme/github/TimeTwist/app/build.gradle.kts).

## Handoff Notes
- Prefer one test class for `TimerViewModel` behavior rather than scattering assertions across multiple helper files.
- If the production `init` block starts a long-running loop immediately, make sure the test constructor path can control scheduler advancement before assertions.
- Inspect emitted service intents directly through the chosen seam or `Robolectric` shadow APIs.
- Keep timer IDs fixed as `timer0`, `timer1`, and `timer2`; later tests depend on those values.

## Concrete Edits
- [ ] Create `app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
  - Add tests for default state of `timer0`, `timer1`, and `timer2`.
  - Add tests that pre-populate preferences and verify the `init` path loads saved durations and flags.
  - Add tests for `getTimer` success and invalid-ID failure.
  - Add tests for `updateTimerDuration` mutating in-memory state and persisted values together.
  - Add tests for `startTimer` stopping other timers, setting `started`, storing a controlled `startTime`, and issuing a service start with matching extras.
  - Add tests for `stopTimers` clearing active flags and calling the service-stop path.
  - Add completion-path tests for non-repeating and repeating timers by advancing fake time and coroutine schedulers.
- [ ] Create a local fake or helper inside `TimerViewModelTest.kt` or a shared test helper path if phase 01 introduced a reusable seam.
  - Fake time source should return explicitly controlled millis values.
  - Fake service launcher should capture start/stop calls and intent extras for assertions.
  - Avoid mocking the entire Android `Context` when a real `Application` from `Robolectric` is simpler.
- [ ] Change [app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt) only if phase-01 seams still need a small follow-up to support deterministic tests.
  - Keep follow-up changes minimal and backward-compatible with existing `viewModel()` usage.
  - Avoid adding production-only branches whose only purpose is to satisfy tests.

## Search And Confirm Steps
- `rg -n "class TimerViewModel|init \\{|startTimers\\(|updateTimerDuration\\(|startTimer\\(|stopTimers\\(|getTimer\\(" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "timer0|timer1|timer2" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "startTime|elapsedTime|timeRemaining|started|repeating|sound|vibration|intervalStuff" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt app/src/main/java/com/cgm/timetwist/service/TimeDetails.kt`
- `rg -n "Intent\\(|putExtra\\(" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "saveTimerDetails|getTimerDetails|TIME_TWIST_PREFERENCES" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "runTest|TestScope|StandardTestDispatcher|advanceTimeBy|advanceUntilIdle" app/src/test/java`
- `rg -n "ShadowApplication|shadowOf\\(" app/src/test/java`
- Confirm before edit that `TimerViewModel` still derives from `AndroidViewModel`.
- Confirm before edit that `startTimer` still starts a service immediately after marking the chosen timer active.

## Edge Cases And Failure Modes
- The `init` loop can race against setup if test dispatchers are not controlled from construction time.
- `startTimer` currently stops the service before starting a new one; tests must assert this order if behavior matters.
- Updating one timer must not mutate the other two timers’ persisted configuration.
- Completion tests must handle the current one-second loop cadence rather than assuming millisecond-perfect updates.
- Repeating timers should restart with the saved duration and flags, not stale elapsed state from the previous cycle.
- Invalid timer IDs should still throw `IllegalArgumentException`.
- Tests that inspect `timeRemaining` immediately after `startTimer` must account for the immediate `updateTimer()` call.
- SharedPreferences state must be reset between tests or constructor-loading assertions will bleed across cases.

## Tests / Validation
- Run `./gradlew testDebugUnitTest`.
  - Expected result: `TimerViewModelTest` passes together with phase-02 tests.
- Add tests that assert:
  - persisted settings are loaded during `init`
  - `updateTimerDuration` updates both state and stored prefs
  - `startTimer("timer1", ...)` leaves only `timer1.started == true`
  - started-service extras include `startTime`, `durationMillis`, `repeating`, `sound`, `vibration`, and `intervalStuff`
  - `stopTimers` issues a stop-service path and clears active timers
  - repeating timers restart after completion while non-repeating timers stop
- Manual verification
  - Optional only if a seam change touched runtime behavior.
  - Expected observable result if manually checked: starting one timer stops any other active timer and timer settings still persist across app restarts.

## Definition Of Done
- [ ] `TimerViewModelTest.kt` covers constructor loading, updates, start/stop behavior, and completion paths.
- [ ] Tests use deterministic time and coroutine advancement instead of sleeping.
- [ ] Service-intent assertions verify the full extra set used by production.
- [ ] Invalid timer IDs are covered by explicit failure tests.
- [ ] Preference state is isolated between tests.
- [ ] `./gradlew testDebugUnitTest` remains green after the new orchestration suite lands.
