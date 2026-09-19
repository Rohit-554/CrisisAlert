## Purpose

Provide a dependable recent-earthquake feed that remains useful with intermittent connectivity and gives users reliable source-backed event details.

## ADDED Requirements

### Requirement: Recent earthquake feed
The system SHALL present recent earthquake events in reverse chronological order and SHALL identify each item by a stable source event identifier so the same event is not displayed more than once.

#### Scenario: Recent events are available
- **WHEN** the user opens the home screen and event data is available
- **THEN** the system displays the events from newest to oldest without duplicate source events

#### Scenario: Feed has no events
- **WHEN** loading completes successfully and no recent events are available
- **THEN** the system displays a clear empty state rather than an error or an indefinite loading indicator

### Requirement: Official earthquake data source
The system SHALL obtain this capability's remote earthquake data from the official USGS magnitude-2.5+ past-day GeoJSON summary feed at `https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.geojson`, SHALL identify USGS as the source, and MUST NOT require an API credential for that feed.

#### Scenario: Refresh requests earthquake data
- **WHEN** the system performs a remote feed refresh
- **THEN** it requests the official USGS magnitude-2.5+ past-day GeoJSON summary endpoint

#### Scenario: USGS event is presented
- **WHEN** an event originating from the USGS feed is displayed
- **THEN** the system identifies USGS as its source and preserves the official event link when supplied

#### Scenario: Application is configured for the earthquake feed
- **WHEN** the application is built or run
- **THEN** access to the USGS feed requires no embedded API key, token, or user credential

### Requirement: Useful event summaries
The system SHALL show each event's available title or location, occurrence time, earthquake category, and source-provided magnitude without inventing values for unavailable fields.

#### Scenario: Complete event summary
- **WHEN** an event contains all summary fields
- **THEN** the event card displays its title or location, occurrence time, earthquake category, and magnitude

#### Scenario: Optional summary data is missing
- **WHEN** an event omits an optional summary field
- **THEN** the event card remains usable and represents that field as unavailable or omits it without presenting fabricated information

### Requirement: Filter events by disaster type
The system SHALL let the user view all available events or restrict the feed to earthquake events, and SHALL preserve the selected filter while the home screen remains active.

#### Scenario: Earthquake filter selected
- **WHEN** the user selects the earthquake filter
- **THEN** the feed contains only events categorized as earthquakes

#### Scenario: Filter has no matching events
- **WHEN** the selected filter has no matching cached events
- **THEN** the system displays a filter-specific empty state and keeps the filter controls available

### Requirement: Offline-first loading
The system SHALL expose previously cached events immediately when available, attempt to refresh them from the public source, and continue showing cached events if that refresh fails.

#### Scenario: Cached data exists at startup
- **WHEN** the user opens the app with cached events available
- **THEN** the system displays the cached events without waiting for the remote refresh to finish

#### Scenario: Refresh succeeds
- **WHEN** the public source returns a valid event feed
- **THEN** the system updates the cache and the visible feed reflects the refreshed data

#### Scenario: Refresh fails with cached data
- **WHEN** the refresh fails and cached events are available
- **THEN** the system keeps the cached events visible and displays a non-blocking indication that fresh data could not be loaded

#### Scenario: Initial load fails without cached data
- **WHEN** the refresh fails and no cached events are available
- **THEN** the system displays a recoverable error state with a retry action

### Requirement: User-initiated refresh
The system SHALL provide a refresh action, indicate refresh progress without replacing visible cached content, and prevent concurrent refresh requests.

#### Scenario: User refreshes a populated feed
- **WHEN** the user requests a refresh while events are visible
- **THEN** the system keeps those events visible and shows an in-progress refresh indication until the request completes

#### Scenario: User requests refresh while one is running
- **WHEN** a refresh is already in progress and the user invokes refresh again
- **THEN** the system does not start an additional concurrent refresh

### Requirement: Event details
The system SHALL allow the user to open a cached event and view all reliable available details, including its category, location, coordinates, occurrence and update times, magnitude, source attribution, and source link.

#### Scenario: Open an event from the feed
- **WHEN** the user selects an event card
- **THEN** the system opens a details view for that event using locally available data

#### Scenario: Details are viewed offline
- **WHEN** the device is offline and the selected event exists in the cache
- **THEN** the details view remains available without requiring a network request

#### Scenario: Detail field is unavailable
- **WHEN** an optional detail field is missing from the source data
- **THEN** the details view clearly marks the information as unavailable or omits the field without showing a fabricated value

#### Scenario: Source link is available
- **WHEN** the event contains a valid source link and the user invokes it
- **THEN** the system delegates opening the original source using an available external handler

#### Scenario: Requested event is unavailable
- **WHEN** the selected event identifier does not resolve to a cached event
- **THEN** the system displays a recoverable unavailable-event state with navigation back to the feed

### Requirement: Resilient external data handling
The system SHALL treat malformed records, missing optional fields, HTTP failures, timeouts, and connectivity failures as recoverable conditions and SHALL not crash because of them.

#### Scenario: Feed contains an invalid record
- **WHEN** one record cannot be safely normalized but other valid records are present
- **THEN** the system excludes the invalid record and continues processing the valid records

#### Scenario: Response cannot be processed
- **WHEN** the remote response as a whole is invalid or unsuccessful
- **THEN** the system handles the attempt as a refresh failure using the applicable cached-data or no-cache behavior
