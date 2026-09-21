## Purpose

Give event details actionable geographic and current-weather context without collecting the user's location or hiding external-data limitations.

## ADDED Requirements

### Requirement: Event-location map
The system SHALL show an interactive MapLibre map using OpenFreeMap data centred on an event's valid coordinates and identify the event location with a marker. The map SHALL visibly attribute OpenFreeMap, OpenMapTiles, and OpenStreetMap contributors, and SHALL not request device-location permission or download maps for offline use.

#### Scenario: Event has valid coordinates
- **WHEN** a user opens details for an event with valid latitude and longitude
- **THEN** the details show the event-centred map, marker, and required map-data attribution

#### Scenario: Event has no mappable coordinates
- **WHEN** an event lacks valid coordinates
- **THEN** the details remain usable and do not show a misleading map or marker

### Requirement: On-demand event weather
The system SHALL request current weather for an event's coordinates only after the user opens its details, and SHALL show the available current temperature, relative humidity, precipitation, weather condition, and wind speed with Open-Meteo attribution.

#### Scenario: Current weather loads
- **WHEN** an eligible event-detail screen requests weather successfully
- **THEN** it shows the current weather values and identifies Open-Meteo as the source

#### Scenario: Weather is unavailable
- **WHEN** current weather cannot be loaded or is unavailable for the event coordinates
- **THEN** the event details remain usable, explain that weather is unavailable, and offer a retry without hiding cached event information
