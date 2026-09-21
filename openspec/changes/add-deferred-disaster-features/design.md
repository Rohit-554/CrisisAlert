## Context

See `proposal.md` for motivation and the new capability specs for behavior. The active `establish-disaster-feed` change establishes a single-module Compose app whose Room cache is the source of truth for USGS earthquakes. The new slice extends those package boundaries rather than introducing Gradle modules. It requires source-specific data adapters, a Room migration, a map SDK, background work, and detail-level weather loading.

## Goals / Non-Goals

**Goals:**

- Preserve offline-first feed and detail behavior while adding independently refreshable EONET data.
- Keep source DTOs, database entities, domain models, map presentation, and UI state isolated.
- Render a credential-free map with MapLibre Native and an OpenFreeMap style while retaining mandatory attribution.
- Bound background and weather requests to source terms and avoid collecting user location.

**Non-Goals:**

- Device location, routing, geofencing, turn-by-turn navigation, or proximity alerts.
- An emergency-notification or alerting system.
- General full-text search infrastructure, cloud sync, user accounts, shared saved events, or offline map downloads.
- Cross-source event deduplication; source provenance remains explicit and events are independently displayed.

## Decisions

### Extend the existing source-of-truth cache with source ownership and saved snapshots

Room remains the single observable store. Event rows will retain a stable source-qualified identifier and source ownership. EONET refresh replaces only EONET-owned, non-saved snapshot rows after a valid full response; USGS keeps its existing independently scoped replacement. A saved event receives a durable saved reference and is retained as a last-known event snapshot if the refreshed source no longer returns it. Saved status is represented separately from remote-source identity so saving cannot alter source data.

The repository exposes cached combined events, cached saved events, local search/filter inputs, refresh operations, save/unsave operations, and an event-detail stream. DAO queries perform ordering and saved-event joins; the ViewModel applies transient category and case-insensitive title/location search filtering to cached models.

An in-memory saved list was rejected because it violates offline durability. Deleting all source rows on refresh was rejected because it would orphan saved event details.

### Add EONET through a defensive, source-specific adapter

The EONET v3 GeoJSON adapter maps only events with a stable identifier, valid geometry, recognizable supported category, and usable date. IDs are namespaced as `eonet:<id>`. The mapping retains EONET as the primary source and serializes available upstream-source names/URLs for details attribution. Invalid individual features are excluded; an unusable top-level response fails that source refresh without changing its cache.

No category mapping is invented for unknown EONET categories: they are excluded until explicitly supported. This keeps UI labels and filtering meaningful. Cross-source deduplication is deferred because EONET curation and USGS lifecycle semantics do not supply enough stable equivalence data for a safe automated merge.

### Use MapLibre Native and OpenFreeMap for the embedded event map

The details UI embeds MapLibre Native Android with `AndroidView`/Compose interoperability, a fixed event camera, a single coordinate marker, and OpenFreeMap's vector style URL. The map is presentation-only: it does not enable user location, routing, or offline packs. Visible OpenFreeMap, OpenMapTiles, and OpenStreetMap attribution remains mandatory.

MapLibre Native is retrieved from Maven Central and OpenFreeMap requires neither account nor API key. The style URL remains centralized and replaceable because the public OpenFreeMap service is provided as-is without an SLA. The app renders a compact unavailable state if map initialization fails.

Leaflet was rejected because it requires a browser/WebView and does not fit the native Compose application. Direct public OSM tiles were rejected because the project needs a provider with clear production behavior; OpenFreeMap provides the selected no-account vector style while keeping a self-hosting path available.

### Load Open-Meteo weather lazily per detail screen

The details ViewModel requests Open-Meteo's `current` fields once when an event with valid coordinates is observed. It holds independent loading, content, and recoverable-error state so the cached event is never hidden. Explicit retry makes another request; returning to or recomposing the same details route must not fan out duplicate requests. No weather response is written to Room in this increment because it is transient context rather than event data.

The weather adapter requests only temperature, relative humidity, precipitation, weather code, and wind speed. The UI maps weather codes to readable labels and shows Open-Meteo attribution. Open-Meteo is preferred over bulk feed enrichment because it restricts calls to user intent and stays within the provider's published limits.

### Schedule one unique network-constrained WorkManager refresh chain

Application startup schedules unique periodic work with a connected-network constraint and the platform minimum periodic interval. The worker resolves the same repository, serializes against foreground refresh through the repository mutex, and runs all source refreshes. It returns retry for transient failures and failure only for non-retryable configuration/response failures; it never clears valid cached snapshots on failure.

WorkManager is preferred over a foreground service or alarms because the app does not need precise timing and Android can batch constrained periodic work. Initial foreground loading remains unchanged; background work improves freshness but is not required for app usability.

### Extend navigation with Saved Events while keeping event-ID details

The root navigation adds a saved-events destination and routes all event detail entry points by stable ID. The existing details route remains cache-backed, so both current-feed and retained saved events work offline. Search remains on the main feed screen and operates on cached data only.

## Risks / Trade-offs

- [OpenFreeMap public service changes or is unavailable] -> Keep its style URL behind a single configuration boundary, preserve the coordinate fallback, and self-host or select a managed provider before requiring an SLA.
- [EONET category/date geometry varies by source] -> Map only explicitly supported valid features, preserve provenance, and test malformed payloads.
- [Open-Meteo use exceeds its free-service terms] -> Request only user-opened details, request selected current fields, avoid feed fan-out, and reevaluate before commercial use.
- [Background work is delayed or skipped by Android] -> Treat it as best-effort; foreground refresh and the cache remain the primary product behavior.
- [Saved snapshots increase local database size] -> Retain only user-saved source-orphaned events and remove the retained snapshot when it is unsaved.
- [Map rendering increases APK size/startup work] -> Create the map only on eligible detail screens and retain a non-blocking unavailable state.

## Migration Plan

1. Add MapLibre Android and WorkManager dependencies; verify a clean build resolves them without a map credential.
2. Create the Room schema migration and regression-test existing USGS data, saved references, source-scoped snapshot replacement, and retained saved rows.
3. Add EONET, repository orchestration, local filters/search, and saved-events navigation before wiring maps/weather so the basic multi-source offline flow is testable.
4. Add MapLibre/OpenFreeMap and lazy weather details with UI and adapter tests, then add unique periodic work.
5. Validate debug and release builds with no credentials in the diff, run unit/database/UI tests, and manually test offline, refresh failure, source attribution, save retention, map unavailable, and background-work scheduling.

Rollback removes the new worker scheduling and feature wiring. The Room migration must retain old event rows; a subsequent corrective migration can safely remove new tables/columns only after users have upgraded past the affected version.
