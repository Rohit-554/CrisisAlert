## Context

The project is a single-module Android application containing the generated Compose starter screen. It has no networking, persistence, dependency injection, navigation, or ViewModel architecture yet. See `proposal.md` for the motivation and `specs/disaster-feed/spec.md` for the behavior contract.

The first slice must establish production-oriented boundaries without prematurely splitting a small application into Gradle modules. The canonical product name remains CrisisProtect, with the existing `io.jadu.crisisprotect` namespace and application ID.

## Goals / Non-Goals

**Goals:**

- Establish package-level presentation, domain, and data boundaries that can support later disaster sources.
- Make Room the observable source of truth for feed and detail screens.
- Isolate the external response model and tolerate partially malformed source data.
- Keep cached content visible through refresh failures and expose refresh status independently from content.
- Build seams that allow mapping, repository, filtering, and ViewModel behavior to be tested without real network calls.

**Non-Goals:**

- Creating multiple Gradle modules in this change.
- Defining a generalized multi-source aggregation or cross-source deduplication algorithm.
- Adding maps, weather, search, bookmarks, background work, pagination, or user location.
- Assigning a derived disaster-severity label that the source does not provide; the source magnitude is displayed as the reliable severity-related measure.

## Decisions

### Use the USGS magnitude 2.5+ past-day GeoJSON feed

The initial remote source will be the official USGS GeoJSON summary feed at `https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson`. The feed is documented as a programmatic interface, is updated every minute, requires no API key, provides stable feature identifiers, coordinates, timestamps, magnitude, place text, and source URLs, and keeps the initial data volume manageable without pagination. USGS recommends its real-time feeds rather than catalog queries for automated display applications, so CrisisProtect will consume the cached summary feed and will not build equivalent catalog queries.

The remote model will mirror only the fields used from the GeoJSON feature collection. A mapper will validate the feature identifier and coordinate shape, convert source epoch timestamps, preserve nullable optional fields, and create IDs namespaced as `usgs:<feature-id>`. Individual invalid features will be dropped; an invalid top-level response will fail the refresh.

Alternatives considered:

- The all-earthquakes past-day feed provides more data but adds low-value noise for a disaster-oriented first slice.
- NASA EONET covers multiple categories but would force aggregation and category-normalization decisions before the basic offline flow is proven.

Official references: [USGS GeoJSON summary format](https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php) and [USGS earthquake catalog API guidance](https://earthquake.usgs.gov/fdsnws/event/1/).

### Adopt a staged API provider roadmap

Research identified the following provider boundaries. Only USGS is implemented by this change; the other entries guide later OpenSpec changes and do not expand the current task scope.

| Capability | Provider and interface | Authentication and terms | Planned phase |
| --- | --- | --- | --- |
| Recent earthquakes | USGS magnitude-2.5+ past-day GeoJSON summary feed | No API key; public USGS data; show USGS source attribution and retain official event links | This change |
| Wildfires, severe storms, volcanoes, floods, and other natural events | NASA EONET v3 GeoJSON events endpoint, `https://eonet.gsfc.nasa.gov/api/v3/events/geojson` | Public interface with no credential parameter documented; retain EONET's event sources because event definitions and curation vary by upstream source | Later multi-hazard change |
| Current weather at an event | Open-Meteo forecast endpoint, `https://api.open-meteo.com/v1/forecast`, requesting only current temperature, relative humidity, precipitation, weather code, and wind speed for the event coordinates | No key for the free non-commercial service; maximum 600 calls/minute, 5,000/hour, and 10,000/day; CC BY 4.0 attribution required; commercial use requires a paid plan or replacement provider | Later weather change |
| Event map | Map renderer and tile provider not yet selected | OpenStreetMap data may be used with attribution, but `tile.openstreetmap.org` is best-effort, requires an identifiable User-Agent and cache compliance, prohibits offline prefetching, and is not assumed to be an unrestricted production tile backend | Later maps change |

NASA EONET v3 is selected for breadth rather than authority: it provides curated, near-real-time event metadata and filters for category, status, date range, source, and bounding box, but its upstream source and lifecycle semantics must remain visible. Open-Meteo is suitable for CrisisProtect's educational, non-commercial use, but the weather integration must make one on-demand request from the details screen rather than fan out across feed items. A later change must reassess its terms if the app becomes commercial.

Official references: [NASA EONET v3](https://eonet.gsfc.nasa.gov/docs/v3), [Open-Meteo weather API](https://open-meteo.com/en/docs), [Open-Meteo terms](https://open-meteo.com/en/terms), and [OpenStreetMap tile policy](https://operations.osmfoundation.org/policies/tiles/).

### Use Retrofit with Kotlin Serialization

Retrofit will provide a small declarative HTTP boundary, Kotlin Serialization will decode the GeoJSON response, and OkHttp will supply bounded connection/read timeouts. This combination is conventional for Android and keeps the source adapter replaceable behind a remote data source.

Ktor Client was considered and is viable, but it provides no material benefit for this Android-only, single-endpoint slice. Only one JSON stack will be introduced.

### Make Room the source of truth

The remote response will never flow directly to the UI. Refresh will map valid features to entities and replace the USGS feed snapshot in a Room transaction only after the full response has been successfully decoded. DAO `Flow` queries will emit the feed in descending occurrence-time order and expose a selected event by its stable ID.

Replacing only rows owned by the refreshed source avoids stale events accumulating while preserving a path for later independent sources. The transaction prevents observers from seeing a temporarily empty feed. A failed request or invalid response will not modify the existing cache.

Room entities, remote DTOs, domain models, and UI models remain distinct where their responsibilities differ. Timestamps will use `Instant` in the domain and epoch milliseconds at storage/network boundaries, with Java time desugaring for the current minimum SDK.

An in-memory or fake repository could be simpler initially, but it would not satisfy the offline contract and would postpone the central architectural risk.

### Expose content and refresh status independently

The repository will expose observed domain events and a suspending refresh operation returning a typed application result. A mutex will serialize refresh attempts. The Home ViewModel will combine the cached event stream with the selected filter and maintain initial-loading, refreshing, and non-blocking refresh-failure state separately.

This allows cached events to remain visible during refresh and after failure. A full-screen error is used only after the first refresh fails while the cache is empty. Retry and user refresh call the same operation.

A single sealed state with mutually exclusive loading/success/error branches was considered, but it tends to discard content when transient state changes. Orthogonal content and operation flags represent the required behavior more directly.

### Keep filtering local and model-driven

Filtering will operate on cached domain events in the ViewModel rather than making another remote request. The initial controls expose All and Earthquakes, while the filter type uses the domain disaster category so later sources can extend it without changing the flow.

### Navigate to cached details by stable event ID

Navigation Compose will route from home to details using the event ID as the only argument. The details ViewModel will observe that record from Room, so the screen works offline and does not depend on a large serialized navigation payload. Source links will be offered only for validated HTTP or HTTPS URLs and opened through an external Android intent when a handler is available.

Passing the full event through navigation was considered but would duplicate state, create encoding/versioning problems, and weaken Room's source-of-truth role.

### Use Koin 4 at application scope

A custom `Application` class will start Koin. Modules will group database, networking, repository, and ViewModel definitions without adding wrapper interfaces that lack a testing or architectural boundary. The repository interface belongs in the domain layer; the external service and DAO remain concrete data-layer boundaries.

## Risks / Trade-offs

- [USGS availability or schema changes] -> Keep its DTOs and mapping isolated, use timeouts, handle failures as recoverable, and cover representative payloads with mapper tests.
- [External API terms, limits, or versions change] -> Keep providers behind data-source boundaries, retain attribution in the UI, and recheck official terms during each future provider's implementation change.
- [EONET data is curated from heterogeneous upstream sources] -> Preserve EONET and upstream source metadata, avoid presenting it as an emergency alert authority, and define deduplication in the later multi-hazard design.
- [Open-Meteo free access is non-commercial and rate-limited] -> Request weather only when details are opened, include required attribution, and reassess the provider before any commercial release.
- [Community OpenStreetMap tile service is mistaken for unrestricted hosting] -> Do not commit to it in this change; select a policy-compliant hosted or self-hosted tile source when maps are designed.
- [Snapshot replacement removes an event no longer present in the past-day feed] -> Treat the cache as a mirror of the selected source window; durable saved events are explicitly deferred to a later storage design.
- [Partially malformed responses silently reduce the feed] -> Drop only records that cannot be safely identified or located and test null/malformed cases; fail the whole refresh when the top-level response is unusable.
- [Network errors are not proof that the device is offline] -> Phrase UI feedback as an inability to refresh or as possibly offline rather than asserting connectivity state from exceptions alone.
- [Architecture overhead in a small starter app] -> Keep one Gradle module and add abstractions only at remote, database, and repository boundaries.
- [External source URL has no installed handler] -> Detect handler availability and keep details usable if the link cannot be opened.

## Migration Plan

1. Add and configure the required plugins and dependencies while preserving the existing build variants.
2. Introduce domain models and repository contracts, followed by isolated remote and Room data layers.
3. Wire the data graph through Koin and verify the application starts before replacing the placeholder UI.
4. Add home and detail ViewModels, navigation, and Compose screens incrementally.
5. Run unit tests, database/instrumentation tests, and a debug build after each meaningful layer is connected.

There is no production data migration because this is the first database schema. If rollout must be reverted, the placeholder UI and new dependency wiring can be reverted together; uninstalling a development build clears the initial cache.
