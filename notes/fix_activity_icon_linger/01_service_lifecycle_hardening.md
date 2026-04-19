# Phase 01: Service Lifecycle Hardening

## Intent

- Prevent `CountdownService` from advertising an ongoing activity when no valid countdown exists.
- Eliminate the strongest stale-icon path: sticky restart with `null` intent or zero/default timer extras.
- Make notification teardown explicit and deterministic on all service stop paths.
- Preserve normal manual starts, periodic alerts, and completion behavior unless a lifecycle fix requires a narrowly scoped adjustment.
- Keep the fix inside existing service code rather than introducing new architecture.

## Prerequisites From Previous Phases

- The current `CountdownService` implementation still lives in `app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`.
- The current investigation note exists at either `notes/_research/` or `_notes/_research/`; use it for behavioral context only, not as an output path in this phase.
- Existing service tests in `app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt` are present and passing before edits begin.

## Handoff Notes

- Watch for any production path that still reaches `setupOngoingActivity(...)` before validating request data.
- Watch for stop paths that call `stopSelf()` without an explicit `stopForeground(...REMOVE)` or equivalent notification cancellation.
- Keep the service’s alert cadence unchanged in this phase unless the lifecycle refactor accidentally changes it.
- If `START_STICKY` is retained for any reason, the phase is incomplete unless `null` intent restart is explicitly safe and tested.

## Concrete Edits

- [ ] Change [app/src/main/java/com/cgm/timetwist/service/CountdownService.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt)
  - Add a small validation helper for start input, or directly guard in `onStartCommand()`, so the service can detect:
    - `intent == null`
    - `durationMillis <= 0L`
    - computed `timeRemaining <= 0L` before any ongoing-notification setup
  - Move `setupOngoingActivity(...)` behind that validation so invalid or already-expired starts never call `startForeground(...)` or `ongoingActivity?.apply(...)`.
  - Change the `onStartCommand()` return value to `START_NOT_STICKY` unless a narrower restart mode is demonstrably required by current UX.
  - Add one explicit teardown helper that removes the foreground notification using the current API (`stopForeground(Service.STOP_FOREGROUND_REMOVE)` or an equivalent compatibility-safe call) and nulls the ongoing-activity reference only after teardown work is done.
  - Invoke explicit teardown from the service completion path and the destruction path in a way that does not double-post or detach the notification.
  - Keep `NOTIFICATION_ID` and channel identifiers unchanged.
  - Avoid moving timer-routing or UI-state logic into this file.
- [ ] Change [app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt)
  - Add a test for `onStartCommand(null, ...)` that proves no foreground notification is posted and no countdown work proceeds.
  - Add a test for zero-duration or already-expired timer input that proves the service rejects the request before ongoing-activity setup.
  - Add a teardown-path test that proves a previously started foreground notification is removed on service shutdown.
  - Update any existing assertions that currently assume sticky behavior or implicit notification cleanup.
  - Avoid brittle assertions against internal Wear framework implementation details.

## Search And Confirm Steps

- `rg -n "onStartCommand|START_STICKY|START_NOT_STICKY|stopSelf\\(|onDestroy\\(" app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`
- `rg -n "setupOngoingActivity|startForeground|OngoingActivity|updateStatus" app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`
- `rg -n "durationMillis|startTime|timeRemaining|COUNTDOWN_TICK_MILLIS" app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`
- `rg -n "stopForeground|NotificationManager|cancel\\(" app/src/main/java/com/cgm/timetwist/service/CountdownService.kt app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "manual starts should emit|transition starts should suppress|lastForegroundNotification" app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt`
- `rg -n "START_STICKY|null intent|stop a foreground service|ongoing activity" notes _notes README.md app/src/test/java`
- Confirm before edit that `onStartCommand()` currently returns `START_STICKY`.
- Confirm before edit that `setupOngoingActivity(...)` is currently called before the coroutine loop decides whether any time remains.
- Confirm before edit that `onDestroy()` does not already call `stopForeground(...)` or `NotificationManager.cancel(...)`.

## Edge Cases And Failure Modes

- A valid timer with less than one second remaining must not be rejected if the intended behavior is to complete immediately with a final alert; decide this explicitly before coding.
- An invalid restart should not trigger the completion big alert, because that would convert a stale-icon bug into a spurious alert bug.
- Explicit teardown must not detach the notification and accidentally leave it posted after service destruction.
- Teardown may be reached both from a manual stop and from the coroutine `finally` block; guard against double-removal side effects.
- If `stopForeground(...)` is called after the service is already no longer in foreground state, tests should still pass and no crash should occur.
- If the service is destroyed during a transition to another timer, the old notification must disappear before the new timer publishes its own ongoing activity.
- Robolectric shadow behavior can differ from device behavior for Wear surfaces; prefer asserting notification/foreground state, not launcher UI.

## Tests / Validation

- Run `./gradlew testDebugUnitTest`.
  - Expected result: `CountdownServiceTest` remains green with new invalid-start and teardown coverage.
- Add or update tests that assert:
  - `onStartCommand(null, ...)` does not leave `shadowOf(service).lastForegroundNotification` populated
  - zero-duration or already-expired input does not post the foreground notification
  - a valid start still posts notification ID `83210`
  - service teardown removes the notification after a valid start
  - manual valid starts still emit the initial small alert
- Manual verification
  - Build and run the app on a watch or emulator only if local setup already exists.
  - Expected observable result: starting a timer still shows the icon; cancelling or allowing an invalid/stale service restart no longer leaves the icon visible.

## Definition Of Done

- [ ] `CountdownService` validates start input before calling `setupOngoingActivity(...)`.
- [ ] `onStartCommand()` no longer relies on sticky restart semantics for this timer use case.
- [ ] Invalid starts do not create a foreground notification.
- [ ] Service stop paths explicitly remove the notification.
- [ ] Existing valid-start behavior still posts the timer notification with ID `83210`.
- [ ] `CountdownServiceTest.kt` covers `null` intent, invalid timing input, valid start, and teardown behavior.
