# Unit Test Coverage Strategy

## Why JVM + Robolectric
- The app’s highest-risk behavior lives in `TimerViewModel`, `SharedPreferences`, and `CountdownService`, all of which can be exercised on the JVM.
- `Robolectric` keeps the first-pass suite fast and local while still providing real `Application`, `Context`, `SharedPreferences`, and `Service` behavior.
- `kotlinx-coroutines-test` makes the timer loops deterministic without `Thread.sleep` or device-backed instrumentation.

## Production Seams Added
- `TimerViewModel` now accepts an internal `timeProvider`, `TimerServiceController`, and optional coroutine scope so tests can control time and capture service start/stop requests.
- `CountdownService` now exposes internal helper logic for countdown alert decisions and countdown formatting.
- `CountdownService` also accepts internal `timeProvider`, `TimerAlerter`, and coroutine-scope overrides so service tests can validate cadence and completion behavior without triggering real sound or vibration.

## Cadence Alignment
- README described the intended cadence as every 5 seconds with 30 seconds or less remaining, and every 15 seconds otherwise.
- The previous implementation used different constants and threshold values.
- The service helpers and tests now align the implementation with the documented cadence:
  - `<= 30s`: every 5 seconds
  - `> 30s`: every 15 seconds

## Coverage Task
- Coverage reporting is exposed through `./gradlew jacocoTestReport`.
- The task depends on `testDebugUnitTest` and writes HTML/XML reports from the debug unit-test execution data.
