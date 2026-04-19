# Phase 02: State Reconciliation And Regression Tests

## Intent

- Align `TimerViewModel` runtime expectations with the hardened service contract from phase 01.
- Reduce the chance that process recreation leaves the UI showing no active timer while service-side artifacts still exist.
- Add regression coverage for stop/start sequencing and countdown completion after the lifecycle changes.
- Keep the single-active-timer model and existing timer-button UX intact.

## Prerequisites From Previous Phases

- `CountdownService` has completed phase-01 validation and explicit teardown behavior.
- `CountdownServiceTest.kt` already covers rejected invalid starts and explicit notification removal.
- `TimerViewModel` still owns active timer state and service start/stop coordination in `app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`.

## Handoff Notes

- Do not introduce a background observer or bound-service architecture; keep reconciliation within existing `TimerViewModel` seams.
- Treat “UI thinks idle while service lifecycle says active” as a data-contract problem first, not a Compose rendering problem.
- Preserve the current start button behavior: starting one timer stops the others and starts exactly one service request.
- If you add any new persisted runtime state, keep it minimal and cleanly cleared on stop/completion.

## Concrete Edits

- [ ] Change [app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt)
  - Review whether the current in-memory-only `started` / `startTime` model is sufficient after phase-01 changes.
  - Add the smallest reconciliation needed so app restart does not silently present all timers as stopped while pending runtime/service cleanup is still plausible.
  - If persistence is needed, store only the active timer identity and start timestamp, clear them on `stopTimers(...)`, completion, and invalid-start recovery, and reuse the existing `TIME_TWIST_PREFERENCES`.
  - Ensure `startTimer(...)`, completion handling inside `startTimers()`, and `stopTimers(...)` leave runtime state consistent with the new service stop semantics.
  - Avoid adding timer-state persistence for all derived values if the active timer can be recomputed from a stored start timestamp and existing duration config.
- [ ] Change [app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt)
  - Add or update tests covering timer state after manual stop, completion, and app/view-model recreation.
  - If active-run state persistence is added, verify it is restored and/or cleared correctly on startup repair.
  - Add a regression test proving that stopping a timer clears any runtime markers used to infer an active service.
  - Keep existing transition-routing coverage green; do not regress the `suppressStartAlert` handoff behavior.
- [ ] Change [app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt)
  - Add any missing sequencing test needed to prove completion plus teardown still behaves correctly when the view-model stops/restarts services quickly.
  - Avoid duplicating view-model persistence assertions here.

## Search And Confirm Steps

- `rg -n "TIME_TWIST_PREFERENCES|saveTimerDetails|getTimerDetails|started|startTime" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "startTimers\\(|stopTimers\\(|startTimer\\(|stopService\\(" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "loadTimersFromPrefs|init \\{|onCleared" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "RecordingTimerServiceController|startRequests|stopCalls|startup repair|started" app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "TimerViewModel\\(|getApplication|ApplicationProvider|SharedPreferences" app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "stopTimers\\(|timer.value.started|timeRemaining <= 0L" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- Confirm before edit whether any active-run state is already persisted outside duration/repeating/sound/vibration/interval settings.
- Confirm before edit whether `TimerViewModel` startup currently performs any repair for active timers, or only for transition-button state.
- Confirm before edit whether current tests already simulate view-model recreation with shared preferences.

## Edge Cases And Failure Modes

- Persisting too much runtime state can reintroduce stale countdowns after a real manual stop if the cleanup path misses one key.
- Startup repair must distinguish “active timer should resume” from “stale marker should be cleared” using current time and configured duration.
- A timer that legitimately expired while the app process was gone should not be shown as running again on restart.
- Transition-driven starts must keep working if active-run state persistence is added.
- Clearing state on manual stop must happen even if service teardown fails or is delayed.
- Repeating timers and transition chains must not create two active timer markers simultaneously.
- Tests that rely on wall-clock time can become flaky if they do not control the injected `timeProvider`.

## Tests / Validation

- Run `./gradlew testDebugUnitTest`.
  - Expected result: both `TimerViewModelTest` and `CountdownServiceTest` pass after reconciliation changes.
- Add or update tests that assert:
  - manual `stopTimers(...)` clears all active timer markers and any persisted active-run state
  - timer completion clears the completed timer’s active marker before any next-timer transition begins
  - recreating `TimerViewModel` after a stale/expired active-run marker repairs back to “no active timer”
  - recreating `TimerViewModel` during a still-valid active timer either resumes that timer consistently or clears it by explicit documented policy
- Manual verification
  - Start a short timer, background the app, reopen it, then stop the timer.
  - Expected observable result: the UI and the Wear icon agree about whether a timer is active before and after the stop.

## Definition Of Done

- [ ] `TimerViewModel` state transitions remain consistent with the hardened service lifecycle.
- [ ] Any persisted active-run state is minimal, explicit, and cleared on all stop/completion paths.
- [ ] View-model recreation no longer leaves an obvious “idle UI vs lingering icon” mismatch.
- [ ] Existing transition and repeat behavior remains covered and green.
- [ ] `TimerViewModelTest.kt` contains regression coverage for stop, completion, and recreation behavior.
- [ ] No new architecture was introduced beyond minimal state reconciliation.
