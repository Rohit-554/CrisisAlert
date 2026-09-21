## Why

The earthquake-only feed gives CrisisProtect a reliable foundation, but it does not yet provide the multi-hazard context, location visualization, weather context, discovery, persistence, or timely refresh needed for a useful disaster-awareness experience. The deferred capabilities can now build on the established Room-backed architecture and an open map stack that needs no account or payment method.

## What Changes

- Add NASA EONET v3 events as a second source for non-earthquake natural hazards, preserving EONET and upstream-source attribution while continuing to identify USGS data separately.
- Expand the local, model-driven feed filter to cover all supported disaster categories and add local text search over cached event titles and locations.
- Add an event-detail MapLibre map centred on valid event coordinates with a non-sensitive event marker and visible OpenFreeMap, OpenMapTiles, and OpenStreetMap attribution; require no map credential.
- Load current event-location weather from Open-Meteo only when a user opens an eligible event detail, with loading, unavailable, and retry states plus attribution.
- Let users save and remove saved events locally, browse saved events offline, and keep them when their source event leaves the latest-source snapshot.
- Schedule bounded background refreshes for source feeds and present the cache as the last successful snapshot when the device is offline or a refresh fails.

## Capabilities

### New Capabilities

- `multi-hazard-feed`: Merge, cache, browse, filter, and attribute USGS and EONET disaster events without presenting either source as an emergency-alert authority.
- `event-map-and-weather`: Show a MapLibre/OpenFreeMap event-location map and user-triggered current weather for coordinate-bearing events with clear source attribution and failure handling.
- `event-search-and-saved-events`: Search cached events and maintain a durable, offline-accessible local saved-events collection.
- `background-feed-refresh`: Refresh disaster-source snapshots on a bounded, network-constrained background schedule while preserving cached content on failure.

### Modified Capabilities

None. The current `disaster-feed` capability is still an active, unarchived change, so its new multi-hazard behavior is captured by the new capabilities in this change rather than creating a delta against a main spec that does not yet exist.

## Impact

- Updates the existing domain models, Room schema/DAOs, repository, Home and Details features, navigation, Koin graph, strings, and tests.
- Adds EONET and Open-Meteo HTTP clients, WorkManager scheduling, MapLibre Android dependencies, and OpenFreeMap style configuration without map credentials.
- Requires internet access and external source attribution, but does not request device-location permission, implement routing, or make CrisisProtect an alerting authority.
