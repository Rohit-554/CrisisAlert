## Purpose

Help people rediscover relevant cached disaster events and retain an offline local collection of events they choose to save.

## ADDED Requirements

### Requirement: Local event search
The system SHALL let users search the cached disaster feed by event title and location text without sending the search query to an external service.

#### Scenario: User searches cached events
- **WHEN** a user enters a search query
- **THEN** the displayed feed contains only cached events whose title or location matches the query

#### Scenario: Search has no matches
- **WHEN** no cached events match a search query
- **THEN** the system shows an explicit no-results state while retaining the query and available filters

### Requirement: Durable saved events
The system SHALL let users save and remove individual events, show saved events in a dedicated offline-accessible view, and retain saved event details after a source refresh no longer includes that event.

#### Scenario: User saves an event
- **WHEN** a user saves an event from its details
- **THEN** the event is marked saved and appears in the saved-events view without requiring a network connection

#### Scenario: Saved event leaves a source snapshot
- **WHEN** a later source refresh no longer contains a saved event
- **THEN** the saved event and its last cached details remain accessible in the saved-events view

