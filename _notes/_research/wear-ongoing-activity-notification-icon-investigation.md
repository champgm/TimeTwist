# Wear Ongoing-Activity Notification Icon Investigation

Date: 2026-04-19

## Question

Why can TimeTwist occasionally show its small timer icon on the Wear OS home screen/watch face even when there is no timer that should still be counting down?

## Short answer

The icon is not just a plain notification badge. In this app it is the Wear OS surface for an `OngoingActivity` that is backed by a foreground-service notification. That icon is expected to remain visible only while the app has a valid ongoing timer.

In the current implementation, the most plausible ways for the icon to appear when it should not are:

1. The foreground service is restarted because it returns `START_STICKY`, but the restart arrives with a `null` `Intent`. The service then recreates the foreground notification and `OngoingActivity` anyway, even though it no longer has valid timer parameters.
2. The service never explicitly removes the foreground notification or cancels the ongoing activity on teardown. Cleanup is left to implicit framework behavior, which is more likely to leave stale UI on Wear surfaces occasionally.
3. App UI state and service state are not persisted together. If the process is recreated, the UI can believe no timer is active while the service or its notification surface still exists, which makes the icon look incorrect from the user’s perspective.

## How the icon is supposed to work on Wear OS

### Platform mechanism

TimeTwist uses two linked mechanisms:

1. A foreground service notification.
2. A Wear `OngoingActivity` attached to that notification.

Per Android’s Wear documentation, the tappable icon on the watch face / home screen is shown by pairing an ongoing notification with an `OngoingActivity`. The activity ends when the underlying ongoing notification is canceled.

Relevant docs:

- Wear ongoing activities: <https://developer.android.com/training/wearables/ongoing-activity>
- `androidx.wear.ongoing.OngoingActivity` reference: <https://developer.android.com/reference/androidx/wear/ongoing/OngoingActivity>
- Foreground-service stop behavior: <https://developer.android.com/develop/background-work/services/fgs/stop-fgs?hl=en>
- `Service.START_STICKY` behavior: <https://developer.android.com/reference/android/app/Service.html>

### What TimeTwist does

`CountdownService` creates the notification channel in [`app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:111), posts a foreground notification in [`setupOngoingActivity()`](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:222), then applies a Wear `OngoingActivity` with a static icon and a touch `PendingIntent` in the same method.

Important code path:

- `startForeground(NOTIFICATION_ID, notification)` at [CountdownService.kt:226](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:226)
- `OngoingActivity.Builder(...).build()` at [CountdownService.kt:229](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:229)
- `ongoingActivity?.apply(applicationContext)` at [CountdownService.kt:235](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:235)
- ongoing status updates every second at [CountdownService.kt:265](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:265)

That means the home-screen icon is expected whenever this service considers itself active enough to call `setupOngoingActivity()`.

## Findings

### 1. `START_STICKY` can recreate the service with no valid timer state

This is the strongest root-cause candidate.

`CountdownService.onStartCommand()` returns `START_STICKY` at [CountdownService.kt:193](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:193).

Android’s `Service` docs say that when a sticky service is restarted after process death, `onStartCommand()` can be called with a `null` `Intent` if there is no pending start command to redeliver.

TimeTwist does not special-case that path. Instead it falls back to default values:

- `durationMillis = intent?.getLongExtra("durationMillis", 0L) ?: 0L` at [CountdownService.kt:156](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:156)
- `startTime = intent?.getLongExtra("startTime", 0L) ?: 0L` at [CountdownService.kt:157](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:157)

Even with those invalid defaults, the service still does all of the following:

- computes times
- calls `setupOngoingActivity(durationMillis)` at [CountdownService.kt:169](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:169)
- posts the foreground notification
- applies the Wear `OngoingActivity`

Then the coroutine sees that the timer is already expired and immediately goes to completion/teardown.

#### Why this can look like the reported bug

If the process was killed while the service was previously in the started state, Wear may receive a fresh foreground notification and ongoing-activity registration even though there is no longer a meaningful countdown to display. The service then stops quickly, but the UI removal is asynchronous and not explicitly forced by the app. On a watch face or launcher surface, that can present as:

- the timer icon reappears
- there is no real countdown associated with it
- it disappears later, or persists until the system refreshes the surface

This is a very good match for an “occasional” stale icon.

### 2. The service never explicitly removes the foreground notification

The app starts foreground mode in [`setupOngoingActivity()`](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:222), but it never calls:

- `stopForeground(STOP_FOREGROUND_REMOVE)`, or
- `NotificationManager.cancel(NOTIFICATION_ID)`

on normal completion, user stop, or `onDestroy()`.

`onDestroy()` only does this:

- plays a big alert at [CountdownService.kt:201](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:201)
- clears a few local fields at [CountdownService.kt:203](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:203)

It does **not** explicitly remove the notification or end the ongoing activity.

Android’s foreground-service docs say that stopping a foreground service removes its notification, so in many cases this code will appear to work. But Wear’s ongoing-activity surfaces are fed from notification lifecycle events, and the app is relying on implicit cleanup rather than asserting the desired end state itself. That raises the chance of stale presentation on:

- watch face home-screen icon
- launcher recents entry
- brief stop/start transitions

This is especially relevant because the Wear docs describe ending an ongoing activity by canceling the ongoing notification. TimeTwist is not doing that directly.

### 3. The app can lose timer UI state while the service lifecycle continues independently

`TimerViewModel` stores timer configuration in shared preferences, but not the active-run state (`started`, `startTime`, active service identity, or notification identity).

Relevant persistence code:

- saves only duration/repeating/sound/vibration/interval config in [TimerViewModel.kt:68](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt:68)
- recreates timers from those preferences in [TimerViewModel.kt:185](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt:185)

The active countdown state is held only in memory:

- `started`
- `startTime`
- `timeRemaining`

If the app process is recreated, the UI can come back showing no active timer even though the notification/foreground-service side is still present or being restarted. From the user’s point of view, that is indistinguishable from “icon visible, no timer running.”

This does not by itself prove the timer is truly absent. It does prove the UI has no durable source of truth for whether the service is active.

### 4. Service setup happens before the timer is validated

`onStartCommand()` computes `timeRemaining`, but it calls `setupOngoingActivity(durationMillis)` before any guard that says “only create the ongoing activity if there is still meaningful time left.”

Current order:

1. read extras
2. compute `timeRemaining`
3. call `setupOngoingActivity(durationMillis)`
4. later decide whether the loop should run

That means the icon can be published even for:

- expired timers
- malformed starts
- sticky restarts with missing extras

Even if teardown happens immediately afterward, publication has already happened.

### 5. Teardown and restart paths can overlap

`TimerViewModel` stops the service before starting another timer at [TimerViewModel.kt:400](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt:400), and also stops the service when a timer completes at [TimerViewModel.kt:234](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt:234).

At the same time, the service coroutine can independently call `stopSelf()` in its `finally` block at [CountdownService.kt:188](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:188).

Because all runs reuse the same notification ID (`83210`) and no explicit foreground-removal call is made, there is room for transient edge cases during:

- rapid manual stop/start
- timer completion transitioning directly into another timer
- self-repeat

This is a weaker explanation than the sticky-restart issue, because in most of those cases there really is another timer starting. Still, it increases the chance of brief stale icon behavior.

### 6. `onDestroy()` always fires a big alert, even on manual stop

This is not the icon bug directly, but it matters because it shows teardown is not distinguished between:

- natural completion
- manual cancel
- replacement by a new timer

`onDestroy()` calls `bigAlert()` unconditionally at [CountdownService.kt:200](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt:200). The tests even assert a second big alert on teardown after completion in [`CountdownServiceTest.kt`](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt:74).

That broad “every destroy path is equivalent” treatment makes it more likely that notification cleanup semantics are also being handled too loosely.

## Ranked list of conditions where the icon may be present when it should not be

### High confidence

1. **Process death while the service is started, followed by sticky restart with `null` intent**
   The service recreates the foreground notification and Wear ongoing activity from zero/default timer data before stopping itself.

2. **Any teardown path where Wear surface cleanup depends on implicit framework cancellation**
   The service stops, but because the app never explicitly removes the foreground notification / ongoing activity, the watch face or launcher can lag and show a stale icon.

### Medium confidence

3. **App/UI process recreation after timer-state loss**
   The UI says no timer is active because active state is not persisted, while the service side or notification side is still alive long enough to keep the icon visible.

4. **Malformed or already-expired start request**
   Since `setupOngoingActivity()` runs before validating remaining time, even a bad start can briefly surface the icon.

### Lower confidence but still plausible

5. **Fast stop/start or chained-transition races**
   Reuse of the same notification ID combined with implicit cleanup can create short stale intervals on Wear surfaces.

## What is *not* the primary issue

- The icon resource itself is not the problem.
- The notification channel importance is probably not the problem.
- The static icon fallback behavior of `OngoingActivity` is working as intended.

The problem is lifecycle correctness: when the app decides an activity is ongoing, and how decisively it tells the system that the activity has ended.

## Most likely concrete scenario

The most likely scenario is:

1. A timer is or was running.
2. The process gets killed or restarted by the system while the sticky service is still considered started.
3. Android recreates the service and calls `onStartCommand(null, ...)`.
4. TimeTwist treats the missing intent as zero-valued timer data.
5. It still posts the foreground notification and reapplies the Wear `OngoingActivity`.
6. The coroutine then notices there is no real remaining time and stops the service.
7. Because cleanup is implicit rather than explicit, the home-screen/watch-face icon can remain visible temporarily or occasionally longer than it should.

That scenario matches both the Android framework contract and the current code very closely.

## Suggested fixes to verify later

This document is research only, but the code changes most likely to eliminate the bug are:

1. Return `START_NOT_STICKY` unless sticky restart is a deliberate requirement.
2. If `intent == null`, or if `durationMillis <= 0`, or if computed `timeRemaining <= 0`, do **not** call `setupOngoingActivity()`; stop immediately instead.
3. On every real stop path, call explicit foreground teardown such as `stopForeground(Service.STOP_FOREGROUND_REMOVE)` before `stopSelf()` / in `onDestroy()` as appropriate.
4. Consider explicitly canceling the notification ID if needed to match Wear’s “stop ongoing activity by canceling the ongoing notification” guidance.
5. Persist enough active-run state, or reconcile against the running service/notification, so the UI can accurately reflect whether a timer is active after process recreation.

## Conclusion

The occasional stale home-screen icon is most likely a lifecycle bug, not a rendering bug. The app advertises an ongoing activity too early, keeps the service sticky, and relies on implicit notification cleanup when stopping. The strongest failure mode is sticky service restart with a `null` intent, which can recreate the Wear icon without a valid countdown behind it.
