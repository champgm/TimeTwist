# TimeTwist - LLM-Friendly README

## Project Overview
TimeTwist is a **WearOS timer application** built with **Kotlin** and **Jetpack Compose**. The app features a unique flat interface with 5 buttons and 3 configurable timers. Its standout feature is **periodic vibration feedback** that reassures users their timer is running (every 5 seconds when ≤30s remain, every 15 seconds otherwise).

**Key Technologies:** Android WearOS, Kotlin, Jetpack Compose, Foreground Services, Shared Preferences

## Architecture Overview

### Core Components
- **Presentation Layer**: MainActivity, WearApp UI, ViewModels
- **Service Layer**: CountdownService (foreground service for timer execution)
- **UI Components**: Custom CircularSlider, TimerButtons, EditScreen
- **Utilities**: VibrationManager, SoundPoolManager

### State Management
- **TimerViewModel**: Manages 3 timer states (timer0, timer1, timer2) using Compose State
- **Shared Preferences**: Persists timer configurations and dark mode setting
- **Foreground Service**: Handles active countdown with ongoing notifications

## Key Features
1. **3 Configurable Timers** with Google color scheme (Blue, Yellow, Green)
2. **Unique Circular Slider Interface** for time editing via drag gestures
3. **Periodic Vibration Feedback** during countdown (key differentiator)
4. **Edit Mode Toggle** for timer configuration
5. **Dark/Light Mode** with persistent preference
6. **Sound & Vibration Options** per timer
7. **Repeating Timer Support**
8. **WearOS Optimized** with ongoing activity notifications

## Important Code Locations

### 🚀 Application Entry Points
- **Main Activity**: `app/src/main/java/com/cgm/timetwist/presentation/MainActivity.kt`
  - App initialization, permission handling, navigation setup
- **Main UI Composable**: `app/src/main/java/com/cgm/timetwist/ui/WearApp.kt`
  - Primary interface layout with 5-button design

### ⚡ Core Timer Logic
- **Timer State Management**: `app/src/main/java/com/cgm/timetwist/presentation/TimerViewModel.kt`
  - Three timer states, start/stop logic, SharedPreferences integration
- **Countdown Service**: `app/src/main/java/com/cgm/timetwist/service/CountdownService.kt`
  - Foreground service handling active timer countdown, vibration triggers
- **Timer Data Model**: `app/src/main/java/com/cgm/timetwist/service/TimeDetails.kt`
  - Timer configuration and state data structure

### 🎯 Unique UI Components
- **Circular Slider**: `app/src/main/java/com/cgm/timetwist/ui/CircularSlider.kt`
  - Custom drag-to-edit interface around screen edge (unique feature)
- **Timer Button**: `app/src/main/java/com/cgm/timetwist/ui/TimerButton.kt`
  - Reusable timer display/control component
- **Edit Screen**: `app/src/main/java/com/cgm/timetwist/ui/EditScreen.kt`
  - Timer configuration interface using circular slider

### 🔧 Supporting Systems
- **Vibration Manager**: `app/src/main/java/com/cgm/timetwist/VibrationManager.kt`
  - Handles periodic vibrations (core feature), different vibration patterns
- **Sound Manager**: `app/src/main/java/SoundPoolManager.kt`
  - Audio feedback using MP3 resources
- **Service Extensions**: `app/src/main/java/com/cgm/timetwist/service/Extensions.kt`
  - Time formatting utilities

### 🎨 UI Theme & Styling
- **Theme Definition**: `app/src/main/java/com/cgm/timetwist/presentation/theme/`
  - Color.kt, Theme.kt, Type.kt - Material theme customization
- **Color Constants**: `app/src/main/java/com/cgm/timetwist/presentation/MainActivity.kt` (lines 25-40)
  - Google color palette definitions and muted variants

## File Structure Navigation for LLMs

### Android App Structure
```
app/src/main/
├── AndroidManifest.xml                    # App permissions and service declarations
├── java/com/cgm/timetwist/
│   ├── presentation/                      # UI layer
│   │   ├── MainActivity.kt               # App entry point ⭐
│   │   ├── TimerViewModel.kt             # State management ⭐
│   │   └── theme/                        # UI theming
│   ├── service/                          # Business logic
│   │   ├── CountdownService.kt           # Timer countdown service ⭐
│   │   ├── TimeDetails.kt               # Data model
│   │   └── Extensions.kt                # Utilities
│   ├── ui/                              # UI components
│   │   ├── WearApp.kt                   # Main UI layout ⭐
│   │   ├── CircularSlider.kt            # Unique slider component ⭐
│   │   ├── TimerButton.kt               # Timer button component
│   │   └── EditScreen.kt                # Timer editing interface
│   ├── VibrationManager.kt              # Vibration handling ⭐
│   └── SoundPoolManager.kt              # Audio handling
└── res/                                 # Resources
    ├── raw/                            # Audio files (MP3)
    └── values/                         # Strings, colors
```

### Key Dependencies (build.gradle.kts)
- Jetpack Compose for WearOS
- Android ViewModel & Navigation
- Wear Compose Material

## Development Insights for LLMs

### Understanding the App Flow
1. **App Launch**: `MainActivity.onCreate()` → initializes managers → sets up navigation
2. **Main Interface**: `WearApp` composable renders 5-button layout
3. **Timer Start**: Button click → `TimerViewModel.startTimer()` → `CountdownService` starts
4. **Active Timer**: Service runs countdown loop, triggers periodic vibrations
5. **Edit Mode**: Toggle edit → navigate to `EditScreen` → use `CircularSlider`

### Critical Code Patterns
- **State Management**: Compose `MutableState` in ViewModel with SharedPreferences persistence
- **Service Communication**: Intent-based service start/stop with parameter passing
- **Custom UI**: Compose Canvas + drag gestures for circular slider implementation
- **Periodic Actions**: Coroutine delay loops for vibration timing logic

### Testing
- **Unit Tests**: `app/src/test/java/com/cgm/timetwist/service/CircularSliderTest.kt`
- **Manual Testing**: ADB commands documented in main README.md

This structure makes TimeTwist a focused, well-architected WearOS app with a unique UX approach to timer feedback through periodic vibrations. 
