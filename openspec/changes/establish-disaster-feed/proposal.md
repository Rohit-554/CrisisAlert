## Why

CrisisProtect currently contains only the starter Compose screen and has no disaster data capability. Establishing one complete earthquake-feed slice creates a usable foundation for the product while proving the intended offline-first architecture before additional disaster sources and features are added.

## What Changes

- Add a recent-earthquake feed backed by the official, public USGS magnitude-2.5+ past-day GeoJSON summary feed, which requires no API key.
- Normalize remote earthquake records into CrisisProtect domain models rather than exposing network DTOs to the UI.
- Cache successfully loaded events in Room and expose cached data reactively so previously loaded content remains usable offline.
- Add a Compose home experience with recent events, earthquake filtering, refresh, and explicit loading, empty, offline, and recoverable error states.
- Add basic event details using reliable fields already available in the cached event, including location, coordinates, time, severity information, and source attribution.
- Keep this first slice in package-based presentation, domain, and data layers inside the existing `:app` module.
- Record NASA EONET v3 as the intended later source for other natural-event categories and Open-Meteo as the intended later source for event-location weather, subject to their attribution and usage terms.
- Leave the map tile provider undecided until the map change is designed because OpenStreetMap data is open but its community tile servers are not an unrestricted production or offline tile service.
- Defer additional disaster sources, maps, weather, search, saved events, and background refresh to later changes.

## Capabilities

### New Capabilities

- `disaster-feed`: Retrieve, cache, browse, refresh, filter, and inspect recent earthquake events with offline-aware behavior.

### Modified Capabilities

None.

## Impact

- Replaces the placeholder home content with the first CrisisProtect user flow.
- Introduces domain, data, and presentation packages within `:app`.
- Adds networking, serialization, Room, lifecycle/ViewModel, navigation, coroutines/Flow, and Koin 4 dependencies and configuration.
- Adds network access to the Android manifest while requiring no device-location permission or API secret.
- Establishes external-source attribution and usage-policy constraints for this slice and its documented API roadmap.
- Adds unit and database-focused tests for mapping, repository behavior, filtering, and UI-state transitions.
