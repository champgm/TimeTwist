# Transition Buttons Plan Pack

## Scope Summary
- Add two edit-mode-only circular transition buttons to the main Wear OS watch face at the midpoint of the `timer0`/`timer2` and `timer1`/`timer2` separators.
- Persist each button’s transition state in the existing `time_twist_preferences` store and restore it during `TimerViewModel` initialization.
- Keep the current runtime model where exactly one timer is active at a time.
- Resolve timer chaining in `TimerViewModel`, not in the Compose layer or by introducing a second service.
- Enforce the ambiguity rule that only one transition button may be in its `_REPEAT` state at a time; the most recently tapped button wins.
- Continue to preserve each timer’s existing `↻` configuration in storage and edit UI, but ignore self-repeat at runtime when an outgoing transition applies.
- Distinguish manual starts from transition-triggered starts so chained starts can suppress the initial small alert.
- Keep chained handoffs immediate with no explicit buffer or delay between timers.
- Add JVM regression coverage for transition persistence, chaining resolution, repeat precedence, and chained alert suppression.
- Record the new sequencing behavior in `notes/_research/` once implementation is complete.

## Phases
- `01_transition_state_and_persistence.md`
  Define the transition-state model, preference keys, and `TimerViewModel` state/update APIs without changing timer completion behavior yet.
- `02_chaining_runtime_and_service_alerts.md`
  Implement completion routing, repeat precedence, repeat-state conflict handling, and chained-start alert suppression across `TimerViewModel` and `CountdownService`.
- `03_watch_face_transition_controls.md`
  Add the edit-mode transition controls to the watch face, including placement, drawing, tap cycling, and theme-aware styling.
- `04_tests_and_research_note.md`
  Expand unit coverage around the new behavior and document the sequencing rules and alert policy in `notes/_research/`.

## Cross-Phase Conventions
- Naming
  - Keep production types under existing packages: `presentation` for orchestration state, `service` for timer-service behavior, and `ui` for Compose controls.
  - Use explicit transition names derived from the approved semantics: `ZERO_TO_TWO`, `TWO_TO_ZERO`, `ZERO_TWO_REPEAT`, `ONE_TO_TWO`, `TWO_TO_ONE`, `ONE_TWO_REPEAT`.
  - Name any new unit test files `*Test.kt` under `app/src/test/java/com/cgm/timetwist/...`.
- Config patterns
  - Reuse `TIME_TWIST_PREFERENCES` and keep all new keys explicit in code rather than constructing them ad hoc in multiple places.
  - Avoid adding new modules, navigation routes, services, or background workers.
  - Keep chained-start alert suppression as a simple explicit flag in the existing service start path rather than introducing a second service contract.
- Logging and test conventions
  - Prefer assertions on state, saved preferences, and captured service requests instead of logs.
  - Keep `TimerViewModel` tests deterministic via the existing injected time provider, service controller, and coroutine scope seams.
  - Keep `CountdownService` tests deterministic via its existing time provider, `TimerAlerter`, and coroutine-scope seams.
  - Run validation from repo root with `./gradlew testDebugUnitTest`; add `./gradlew assembleDebug` only when UI changes need a smoke build.

## Global Definition Of Done
- [ ] Transition state persists across app restarts and is restored by `TimerViewModel`.
- [ ] Tapping either transition button cycles through four states in a stable order and updates persisted state.
- [ ] At most one transition button can remain in a `_REPEAT` state after any tap sequence.
- [ ] When a timer completes, an applicable outgoing transition overrides that timer’s self-repeat behavior.
- [ ] Manual starts still emit the initial small alert, while transition-triggered starts suppress it.
- [ ] The watch face shows the transition controls only in edit mode and places them at the intended separator midpoints.
- [ ] `./gradlew testDebugUnitTest` passes with new `TimerViewModel` and `CountdownService` assertions.
- [ ] `notes/_research/` contains a concise note documenting the new transition rules, repeat precedence, and chained alert policy.
