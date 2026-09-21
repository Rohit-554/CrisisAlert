## Purpose

Provide a unified, offline-capable view of recent earthquake and other natural-hazard events while clearly retaining each source's identity and limitations.

## ADDED Requirements

### Requirement: Multi-source disaster feed
The system SHALL retrieve supported non-earthquake events from NASA EONET in addition to the existing USGS earthquake feed, normalize them into the existing event model, and expose the combined cached feed in descending occurrence-time order.

#### Scenario: EONET event is available
- **WHEN** a valid EONET event is returned during refresh
- **THEN** it appears in the combined feed with its supported disaster category, source identity, and usable event details

#### Scenario: One source refresh fails
- **WHEN** one configured source cannot be refreshed
- **THEN** cached events from all sources remain available and the application reports that refresh could not complete

### Requirement: Source provenance and non-alert presentation
The system SHALL show the event's source attribution in details, retain available upstream EONET-source attribution, and SHALL NOT describe USGS or EONET feed entries as official emergency alerts.

#### Scenario: User inspects an EONET event
- **WHEN** a user opens an event supplied by EONET
- **THEN** the details identify EONET and the available upstream source without representing the event as an emergency alert

### Requirement: Category filtering
The system SHALL let users filter cached events by All and every disaster category currently supplied by the application; filtering SHALL not require a new network request.

#### Scenario: User selects a hazard category
- **WHEN** a user selects a category filter
- **THEN** the feed displays only cached events in that category and retains the chosen filter until it is changed

