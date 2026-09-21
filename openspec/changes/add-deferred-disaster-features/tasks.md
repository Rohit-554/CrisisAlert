## 1. Build, Credential, and Data Foundations

- [ ] 1.1 Verify the `establish-disaster-feed` implementation and its Room schema are complete before extending them; run its relevant unit tests and `./gradlew assembleDebug`
- [ ] 1.2 Add version-catalog and Gradle configuration for MapLibre Native Android, WorkManager, and required test dependencies; verify dependency resolution and a debug build succeed without a map credential
- [ ] 1.3 Centralize the OpenFreeMap style URL, add visible OpenFreeMap/OpenMapTiles/OpenStreetMap attribution, and verify `git diff` and tracked files contain no map credentials
- [ ] 1.4 Evolve domain models and the Room schema for source ownership, EONET provenance, supported disaster categories, and retained saved-event snapshots; add and verify a Room migration test preserves existing USGS rows

## 2. Multi-Hazard Data and Cache Behavior

- [ ] 2.1 Implement minimal EONET v3 GeoJSON DTOs and a source-specific client with bounded timeouts; verify representative EONET fixtures decode without network access
- [ ] 2.2 Implement defensive EONET mapping with namespaced IDs, valid coordinate/date validation, explicit supported-category mapping, and upstream provenance retention; verify mapper tests cover valid, unknown-category, and malformed features
- [ ] 2.3 Extend repository and DAO refresh behavior to merge source-owned snapshots, preserve each source cache on independent refresh failure, and retain saved source-orphaned events; verify database and repository tests cover each outcome
- [ ] 2.4 Expose combined cached events and category filtering through the feed ViewModel/UI with per-source attribution and non-alert wording; verify unit/UI tests cover All and every supported category without a network request

## 3. Search and Saved Events

- [ ] 3.1 Add local case-insensitive title/location search state to the feed UI and ViewModel without transmitting queries; verify filtering and no-results unit/UI tests
- [ ] 3.2 Implement save and unsave repository operations plus a saved-events Room query that remains usable offline; verify save/unsave and source-snapshot-retention database tests
- [ ] 3.3 Add saved-events navigation and a dedicated Compose view that opens details by stable event ID; verify navigation and saved offline content behavior in UI tests or manual emulator testing
- [ ] 3.4 Add detail-screen saved-state controls and source/upstream attribution rendering; verify saved state changes update both detail and saved-events views reactively

## 4. Map and Weather Detail Context

- [ ] 4.1 Embed a MapLibre/OpenFreeMap event map on details for valid coordinates with a fixed event marker and visible OpenFreeMap/OpenMapTiles/OpenStreetMap attribution, without location permission or offline downloads; verify a device/emulator renders a map and coordinate-less events show no misleading map
- [ ] 4.2 Add an unavailable map fallback that leaves all event details usable when map initialization fails; verify it with a disabled/invalid local map configuration test path
- [ ] 4.3 Implement an Open-Meteo current-weather client and mapper that requests only the specified current fields; verify serialization and weather-code presentation mapping tests
- [ ] 4.4 Add independent detail weather state that loads only for opened coordinate-bearing events, deduplicates recomposition requests, supports retry, and preserves cached details on failure; verify ViewModel tests for success, unavailable, and retry states
- [ ] 4.5 Render weather values and Open-Meteo attribution in details; verify Compose UI tests or manual emulator testing for loaded and unavailable states

## 5. Background Refresh and Release Validation

- [ ] 5.1 Add one unique WorkManager periodic refresh with a connected-network constraint and repository-level refresh serialization; verify WorkManager configuration tests and that scheduling is idempotent
- [ ] 5.2 Implement worker result mapping that retries transient failures and never replaces valid cache content after failure; verify worker/repository tests for success, retry, and failure paths
- [ ] 5.3 Run the complete relevant test suite and `./gradlew assembleDebug`; manually validate offline cached browsing, one-source refresh failure, EONET attribution, search, saved-event retention, map fallback, weather retry, and background scheduling
- [ ] 5.4 Inspect the final diff for credentials, unwanted location permissions, prohibited offline tile downloads, unsupported OSM tile use, stale earthquake-only labels, and unrequested routing/alert features; verify all external-provider attribution remains visible
