# CrisisProtect

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-0A7E8C)](https://developer.android.com/about/versions/nougat)

An offline-first Android earthquake feed. CrisisProtect fetches the USGS M2.5+ past-day feed, stores valid events locally, and keeps cached information available when the network fails.

## Stack

- Kotlin, Jetpack Compose, Material 3, Navigation Compose
- Retrofit + OkHttp + Kotlin Serialization
- Room, Coroutines/Flow, Koin
- R8 optimization for release builds

## Data flow

```mermaid
flowchart LR
    U[USGS GeoJSON feed] --> N[Retrofit + Serialization]
    N --> R[Offline-first repository]
    R -->|valid snapshot| D[(Room cache)]
    D --> V[ViewModels]
    V --> UI[Compose feed & details]
    R -->|network failure| D
```

## Build

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The production feed is the public [USGS Earthquake GeoJSON summary](https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php); it requires no API key.

