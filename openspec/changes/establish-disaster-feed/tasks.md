## 1. Project Foundation

- [x] 1.1 Add version-catalog entries and Gradle configuration for Kotlin Serialization, Retrofit/OkHttp, Room with KSP, lifecycle ViewModel/Compose, Navigation Compose, Koin 4, coroutines testing, and Java time desugaring; verify `./gradlew assembleDebug` resolves dependencies and succeeds
- [x] 1.2 Add internet permission, the CrisisProtect `Application` entry point, and package directories for domain, data, and features while preserving `io.jadu.crisisprotect`; verify the debug app starts without dependency-injection errors

## 2. Domain and Remote Data

- [x] 2.1 Define immutable disaster event, disaster type, and refresh-result domain models plus the repository contract; verify domain code compiles without Android UI or remote DTO dependencies
- [x] 2.2 Implement minimal Kotlin Serialization DTOs and a Retrofit service for the exact USGS endpoint `https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson` with bounded OkHttp timeouts and no credential; verify a representative official-feed fixture decodes successfully in a unit test
- [x] 2.3 Implement defensive DTO-to-domain/entity mapping with `usgs:` identifiers, coordinate validation, nullable-field handling, epoch conversion, and invalid-feature exclusion; verify unit tests cover valid, missing, and malformed features

## 3. Room Cache and Repository

- [x] 3.1 Create the Room event entity, DAO queries for ordered feed and event-by-ID observation, database definition, and entity/domain mappers; verify Room schema generation and mapper unit tests succeed
- [ ] 3.2 Implement transactional replacement of only the USGS snapshot without clearing data before a valid response is ready; verify an in-memory Room test observes atomic replacement and stable descending ordering
- [ ] 3.3 Implement the offline-first repository with Room flows, typed refresh failures, valid-record filtering, and mutex-protected refresh; verify repository tests cover successful refresh, retained cache after failure, invalid top-level response, and concurrent refresh suppression
- [ ] 3.4 Define Koin database, network, repository, and ViewModel modules and start them from the application class; verify a Koin graph check or application startup resolves every declared dependency

## 4. Home Feed

- [x] 4.1 Implement Home ViewModel state that combines cached events with All/Earthquakes filtering and independently tracks initial load, refresh progress, and refresh failure; verify unit tests cover cached startup, empty success, initial failure, retry, filtering, and cached-content retention during failure
- [x] 4.2 Replace the placeholder screen with a Material 3 home screen containing the CrisisProtect app bar, accessible filter controls, lazy event list, event summaries, and navigation callbacks; verify Compose previews or UI tests cover populated and filter-empty states in light and dark themes
- [ ] 4.3 Add initial loading, full-screen recoverable error, pull-to-refresh or equivalent refresh action, and non-blocking refresh-failure feedback without hiding cached cards; verify a Compose UI test exercises retry and confirms cached content remains visible while refreshing

## 5. Event Details and Navigation

- [ ] 5.1 Add Navigation Compose routes that pass only the stable event ID between home and details; verify selecting a feed card opens the matching detail destination and back navigation returns to the feed
- [x] 5.2 Implement a Details ViewModel that observes the cached event by ID and exposes loading, content, and unavailable states; verify unit tests cover an existing offline event and a missing event
- [ ] 5.3 Build the accessible Material 3 details screen for available category, location, coordinates, occurrence/update times, magnitude, visible USGS attribution, and the preserved official event link, handling every optional field without fabricated values; verify previews or UI tests cover complete, partial, and unavailable events
- [ ] 5.4 Validate source URLs as HTTP/HTTPS and safely delegate them to an installed external handler; verify tests cover valid, invalid, absent, and unresolvable source links without crashes

## 6. Integration and Quality

- [x] 6.1 Move all user-facing text into string resources and add meaningful semantics, content descriptions, contrast-safe state indicators, and touch targets; verify accessibility checks or focused UI review find no color-only status communication
- [ ] 6.2 Run `./gradlew testDebugUnitTest` and the available Room/Compose instrumentation tests, fixing failures until all executed tests pass
- [ ] 6.3 Run `./gradlew assembleDebug`, install or launch the debug build, and manually verify successful refresh, cached relaunch, airplane-mode fallback, retry, filtering, details, and source-link behavior
- [ ] 6.4 Inspect the final diff for secrets, unrelated edits, duplicate JSON/network libraries, exposed DTOs, stale placeholder code, and external-source policy drift; verify the app uses only the documented USGS summary endpoint, contains no API key, displays USGS attribution, and does not prematurely implement deferred EONET, Open-Meteo, or map integrations
