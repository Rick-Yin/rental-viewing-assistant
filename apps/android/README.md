# Android MVP

This is the native Android MVP implementation for 看房助手.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- Room
- kotlinx.serialization
- Android Gradle Plugin 9.2.1
- Gradle Wrapper 9.4.1
- compileSdk 36
- minSdk 26

## Open

Open this directory in Android Studio:

```powershell
D:\Materials\Files\Toys\Ideas\rental-viewing-assistant\apps\android
```

The expected local Android tooling layout is:

- Android Studio: `D:\Software\Android\Android Studio`
- Android SDK: `D:\Software\Android\Sdk`
- Gradle cache: `D:\Software\Android\.gradle`
- Emulator AVD: `D:\Software\Android\avd`

## Build

```powershell
cd D:\Materials\Files\Toys\Ideas\rental-viewing-assistant\apps\android
.\gradlew.bat tasks --no-daemon
.\gradlew.bat assembleDebug --no-daemon
.\gradlew.bat :domain:testDebugUnitTest --no-daemon
```

The debug APK is generated at:

```text
apps/android/app/build/outputs/apk/debug/app-debug.apk
```

## Current Scope

The app now has a local-first MVP skeleton:

- Multi-module Android structure: `app`, `core`, `domain`, `data`, and feature modules.
- Hilt application wiring and Room-backed local persistence.
- Checklist content loaded from shared JSON assets at build time.
- Four primary tabs: `房源`, `对比`, `签约`, `我的`.
- Profile and template recommendation flow.
- Property creation, viewing records, checklist results, media notes, scoring, comparison candidates, signing sessions, signing materials, AI JSON import, and Quick Share/export text.
- Domain unit tests for scoring and checklist module filtering.

Still intentionally lightweight:

- Media capture currently records URI/file path plus note; full CameraX capture can be layered in next.
- Export is surfaced as generated text in-app; Storage Access Framework and Sharesheet integration can be added next.
- Runtime AI validation uses typed JSON/business checks rather than a full JSON Schema engine.
