# Phase 02: Chaining Runtime And Service Alerts

## Intent
- Implement timer handoff logic when an active timer completes.
- Preserve the existing single-active-timer model while allowing configured chaining.
- Make transition rules override a completed timer’s self-repeat behavior.
- Distinguish manual starts from transition-triggered starts so chained starts can suppress the initial small alert.
- Keep handoffs immediate with no added buffer time.

## Prerequisites From Previous Phases
- Transition-state types and persisted `TimerViewModel` state from phase 01 exist.
- `TimerViewModelTest.kt` already covers baseline start/stop and completion behavior.
- `CountdownService` still owns the startup small alert and completion big alert paths.

## Handoff Notes
- Keep routing decisions centralized in `TimerViewModel`; the service should only react to a start request and its flags.
- Extend the existing start-request seam rather than bypassing it, so test fakes continue capturing all service inputs.
- Preserve manual timer-button behavior: tapping a timer still starts exactly one timer and stops the others.
- Be careful with `stopService` and `stopTimer` ordering so chained starts do not accidentally double-stop or double-alert.

## Concrete Edits
- [ ] Change [app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt)
  - Extend the internal timer-service request model with a flag that indicates whether the timer start was manual or transition-triggered, or a directly equivalent boolean such as `suppressStartAlert`.
  - Split the current start path so `startTimer(...)` remains the manual-entry API and delegates to a lower-level helper that accepts the start-origin flag.
  - Add a private resolver that maps a completed timer ID plus current transition-button state to the next timer ID, if any.
  - Update completion handling inside `startTimers()` so a completed timer:
    - stops the active service
    - stops the completed timer state
    - starts the routed destination immediately when a transition applies
    - otherwise falls back to self-repeat only if `repeating` is enabled
  - Keep self-repeat settings unchanged in stored preferences; only runtime resolution changes.
  - Avoid moving chaining logic into `WearApp` or `TimerButton`.
- [ ] Change [app/src/main/java/com/cgm/timetwist/service/CountdownService.kt](/mnt/nvme/github/TimeTwist/app/src/main/java/com/cgm/timetwist/service/CountdownService.kt)
  - Read the new service-start flag from the intent.
  - Suppress the initial `smallAlert()` call when the timer start is transition-triggered.
  - Keep periodic alert cadence and completion big alert behavior unchanged.
  - Avoid changing notification setup or ongoing-activity behavior unrelated to the new flag.
- [ ] Change [app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt)
  - Add routing tests for `ZERO_TO_TWO`, `TWO_TO_ZERO`, `ZERO_TWO_REPEAT`, `ONE_TO_TWO`, `TWO_TO_ONE`, and `ONE_TWO_REPEAT`.
  - Add precedence tests proving that a configured outgoing transition wins over the completed timer’s self-repeat.
  - Add assertions that transition-triggered starts pass the new “suppress start alert” signal through the service request seam.
  - Keep existing non-transition completion tests green.
- [ ] Change [app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt](/mnt/nvme/github/TimeTwist/app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt)
  - Add a manual-start test proving the initial small alert still fires.
  - Add a transition-start test proving the initial small alert is suppressed.
  - Add a chained-completion test proving the final big alert still fires when a timer ends.

## Search And Confirm Steps
- `rg -n "TimerServiceRequest|TimerServiceController|start\\(|stop\\(" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "startTimers\\(|if \\(timer.value.timeRemaining <= 0L\\)|repeating" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "startTimer\\(|stopTimers\\(|stopService\\(|startService\\(" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
- `rg -n "onStartCommand|smallAlert\\(|bigAlert\\(|alertDevice\\(" app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`
- `rg -n "putExtra\\(|getBooleanExtra\\(" app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`
- `rg -n "startRequests|stopCalls|RecordingTimerServiceController" app/src/test/java/com/cgm/timetwist/presentation/TimerViewModelTest.kt`
- `rg -n "smallAlerts|bigAlerts|onStartCommand" app/src/test/java/com/cgm/timetwist/service/CountdownServiceTest.kt`
- `rg -n "transition|ZERO_TO_TWO|TWO_TO_ONE|REPEAT" app/src/test/java app/src/main/java`
- Confirm before edit that `CountdownService` still emits its initial startup alert via a direct `smallAlert()` call near the top of `onStartCommand`.
- Confirm before edit that no other call sites start the service besides `TimerViewModel`.

## Edge Cases And Failure Modes
- A transition from `timer2` must choose the correct destination based on the active directional or repeat state and never try to start both timers.
- A completed timer with no outgoing transition should keep existing non-repeating and self-repeating behavior unchanged.
- Chained starts must not re-use stale `startTime` or `timeRemaining` from the completed timer.
- Suppressing the startup small alert must apply only to transition-triggered starts, not manual user taps.
- Stopping the old service and starting the new one in quick succession must not suppress the completion big alert.
- Repeating states on both buttons must already be impossible after phase 01, but completion logic should still avoid ambiguous fallback behavior if corrupted state is loaded.
- Existing tests may assert the exact number of service `stop` calls; update those expectations only where the new chaining behavior truly changes them.

## Tests / Validation
- Run `./gradlew testDebugUnitTest`.
  - Expected result: `TimerViewModelTest` and `CountdownServiceTest` pass with new chaining coverage.
- Add tests that assert:
  - `timer0` completion starts `timer2` under `ZERO_TO_TWO`
  - `timer2` completion starts `timer0` under `TWO_TO_ZERO`
  - `ZERO_TWO_REPEAT` alternates between `timer0` and `timer2`
  - `ONE_TWO_REPEAT` alternates between `timer1` and `timer2`
  - a completed timer with both self-repeat and an outgoing transition follows the transition, not self-repeat
  - transition-triggered starts set the new service-start flag while manual starts do not
  - `CountdownService` skips the initial small alert only when that flag is present
- Manual verification
  - Optional after tests: start a short timer pair and observe that the next timer begins immediately.
  - Expected observable result: the completion vibration/sound remains perceptible and is not immediately replaced by a new startup vibration.

## Definition Of Done
- [ ] `TimerViewModel` resolves the next timer from transition state and completed timer ID.
- [ ] Transition routing overrides self-repeat when both could apply.
- [ ] Manual starts still behave as before.
- [ ] Transition-triggered starts suppress the initial service small alert.
- [ ] Completion still triggers the finishing timer’s final big alert.
- [ ] Updated view-model and service tests cover routing, precedence, and chained alert policy.
