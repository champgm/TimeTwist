# Repository Guidelines

## Project Structure & Module Organization
`TimeTwist` is a single-module Wear OS app. App code lives in `app/src/main/java/com/cgm/timetwist/` and is split by responsibility:
- `presentation/` for `MainActivity`, `TimerViewModel`, and theme files
- `ui/` for Compose screens and custom controls such as `CircularSlider`
- `service/` for timer logic, foreground service behavior, and shared models

Resources are in `app/src/main/res/`, unit tests are in `app/src/test/java/`, screenshots are in `screenshots/`, and app icons/source artwork are in `icon/`.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds a debug APK for local install or emulator testing.
- `./gradlew assembleRelease` creates the release APK used in the README deployment flow.
- `./gradlew testDebugUnitTest` runs the JUnit test suite under `app/src/test/java/`.
- `./gradlew lint` runs Android lint checks across the app module.
- `adb devices` verifies watch connectivity; `adb -s <ip:port> install -r app/build/outputs/apk/release/app-release.apk` reinstalls a release build on hardware.

Run commands from the repository root.
Local Android prerequisites:
- install Android SDK Platform `35`, Build-Tools `35.0.0`, and `platform-tools`
- ensure `local.properties` points at a valid local SDK path, or set `ANDROID_HOME` / `ANDROID_SDK_ROOT` before running Gradle
- the checked-in `gradlew` script is expected to run on Unix-like systems without line-ending fixes

## Coding Style & Naming Conventions
Use Kotlin with 4-space indentation and keep package names under `com.cgm.timetwist`. Follow existing naming:
- `PascalCase` for files, classes, composables, and data types
- `camelCase` for methods, properties, and local state
- descriptive screen/component names such as `EditScreen`, `TimerButton`, `CountdownService`

Prefer small Compose functions and keep UI, service, and presentation concerns in their existing directories. No formatter config is checked in, so match surrounding Kotlin style closely before submitting.

## Testing Guidelines
The project currently uses JUnit 4 with AssertJ and Mockito for unit tests. Add tests beside the code they cover in `app/src/test/java/...`, and name files `*Test.kt`. Prefer focused tests around timer math, slider behavior, and service logic. Run `./gradlew testDebugUnitTest` before opening a PR. If Gradle cannot resolve the SDK, fix `local.properties` or export `ANDROID_HOME` / `ANDROID_SDK_ROOT` rather than changing build scripts.

## Commit & Pull Request Guidelines
Recent history favors short, imperative commit subjects such as `Update a screenshot` or `Save darkmode & toggle intermittent (#3)`. Keep commits focused, describe behavior changes rather than implementation trivia, and reference issues when applicable.

PRs should include:
- a short description of the user-visible change
- linked issue or context when relevant
- screenshots for UI changes on the watch interface
- test notes listing commands run
