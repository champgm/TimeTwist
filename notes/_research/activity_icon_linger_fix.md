# Activity Icon Linger Fix

## Root Cause Fixed In Code

- `CountdownService` was returning `START_STICKY` and could be recreated with a `null` `Intent`.
- The service built its foreground notification and Wear `OngoingActivity` before rejecting invalid or already-expired countdown input.
- Service teardown relied on implicit framework cleanup, which made the Wear icon more likely to outlive the real countdown briefly.
- `TimerViewModel` did not persist any active-run marker, so process recreation could make the UI look idle even when a stale service-side surface was still plausible.

## Shipped Fix

- `CountdownService.onStartCommand()` now returns `START_NOT_STICKY`.
- The service now rejects `null`, zero-duration, and already-expired start requests before calling `startForeground(...)` or applying the Wear ongoing activity.
- Foreground teardown is now explicit via `stopForeground(STOP_FOREGROUND_REMOVE)` plus `NotificationManager.cancel(83210)`, followed by clearing the local ongoing-activity reference.
- Completion still emits a completion alert, but destroy-time cleanup no longer adds a second completion-style alert after the countdown has already finished.

## Runtime State Reconciliation

- `TimerViewModel` now persists only the active timer id and start timestamp in `time_twist_preferences`.
- Manual stop and completed-stop paths clear that runtime marker.
- On startup, the view model repairs expired markers back to "no active timer" and issues a service stop to help clear any stale surface.
- If the persisted active timer is still within its configured duration, the view model restores that timer's running state in the UI instead of showing an idle screen immediately.

## Validation

- `./gradlew testDebugUnitTest` passes after the fix.
- Added regression coverage for:
  - `null`-intent restart rejection
  - invalid/expired start rejection before foreground setup
  - explicit notification removal on destroy
  - persisted active-run restore and stale-marker cleanup in `TimerViewModel`

## Residual Risk

- JVM tests can prove notification lifecycle and app-state behavior, but they cannot fully prove how quickly every Wear launcher/watch-face surface refreshes after notification removal.
- `./gradlew lint` still fails on a pre-existing manifest `Instantiatable` error for `MainActivity`; that lint failure is unrelated to this fix and was not changed here.
