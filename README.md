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

## Multi-module architecture

The project is structured so a growing feature does not automatically become a growing app-wide dependency:

```mermaid
flowchart TB
    App[":app"\nApplication, navigation, DI, background work]
    Home[":feature:home"\nBrowse, filter, search]
    Details[":feature:details"\nEvent details, map, weather, haptics]
    Saved[":feature:saved"\nOffline bookmarks]
    Data[":data"\nRetrofit, Room, repository implementations]
    Domain[":core:domain"\nModels and repository contracts]
    Common[":core:common"\nSmall Android utilities]
    Design[":core:designsystem"\nTheme and shared resources]

    App --> Home & Details & Saved & Data & Design
    Home --> Domain & Common & Design
    Details --> Domain & Common & Design
    Saved --> Domain & Design
    Data --> Domain
```

This keeps feature ownership clear: teams can develop and test Home, Details, and Saved independently; feature modules only see stable domain contracts rather than Retrofit or Room; and the `app` module remains the single place that wires navigation and dependency injection. New features can depend on `core:domain` and shared UI without creating feature-to-feature dependencies.

## Runtime data flow

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
