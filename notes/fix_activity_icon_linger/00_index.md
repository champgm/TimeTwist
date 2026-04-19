# Plan Index: `fix_activity_icon_linger`

## Scope Summary

- Fix the occasional Wear OS home-screen/watch-face timer icon that remains visible when no countdown should still be active.
- Treat the icon as a lifecycle problem involving `CountdownService`, foreground notifications, and `androidx.wear.ongoing.OngoingActivity`.
- Preserve the existing app structure: service logic stays in `service/`, runtime state coordination stays in `presentation/`.
- Harden `CountdownService` against sticky restart and malformed start requests before it publishes any foreground notification or ongoing activity.
- Make teardown explicit so the service removes its notification-backed Wear surfaces deterministically.
- Reconcile runtime/UI state with service state well enough that process recreation does not leave the app showing “idle” while a stale service surface lingers.
- Add regression coverage for sticky restart, invalid starts, explicit teardown, and user-visible stop/completion flows.
- Update the relevant research note in `notes/_research/` so the implemented fix path is documented alongside the root-cause investigation.

## Phases

- `01_service_lifecycle_hardening.md`
  - Stop publishing the notification/icon when start input is invalid and make notification teardown explicit. This phase should remove the strongest root cause first: sticky restart and teardown ambiguity inside `CountdownService`.
- `02_state_and_regression_tests.md`
  - Align app runtime state and tests with the hardened service behavior. This phase closes the gap between UI expectations, service lifecycle, and regression coverage.
- `03_docs_and_validation.md`
  - Update the research note, verify commands/tests, and document the final expected behavior so the fix is auditable and maintainable.

## Cross-Phase Conventions

- Naming
  - Keep new helpers and flags in existing Kotlin style: `camelCase` functions/properties, `PascalCase` types.
  - Prefer service-specific helper names inside `CountdownService` such as `hasValidStartRequest`, `stopForegroundAndRemoveNotification`, or equivalent explicit names.
- Config patterns
  - Do not add new app-wide architecture or persistence layers. If state reconciliation needs persistence, keep it inside existing `TimerViewModel` / shared-preferences patterns.
  - Reuse the current notification ID and channel unless a test demonstrates they are part of the bug.
- Logging and test conventions
  - Prefer assertions over new production logs. Add logs only if they materially help distinguish sticky restart vs. valid manual start.
  - Keep tests in the existing JUnit4 + Robolectric setup and add only focused seams needed for deterministic lifecycle assertions.
  - Use the repo-standard validation command `./gradlew testDebugUnitTest`; add narrower commands only when they materially speed up a phase.

## Global Definition Of Done

- [ ] `CountdownService` no longer publishes an ongoing notification/activity for `null`, zero-duration, or already-expired start requests.
- [ ] Service stop paths explicitly remove the foreground notification rather than relying only on implicit teardown.
- [ ] The chosen service restart behavior is intentional and covered by tests.
- [ ] App runtime state no longer misrepresents the countdown as stopped while a stale service surface remains plausible.
- [ ] `CountdownServiceTest.kt` covers invalid starts, restart semantics, and teardown behavior.
- [ ] Any affected `TimerViewModel` behavior and tests are updated to match the new service contract.
- [ ] Relevant research notes under `notes/_research/` reflect the implemented fix and residual risks.
- [ ] `./gradlew testDebugUnitTest` passes from the repo root.
