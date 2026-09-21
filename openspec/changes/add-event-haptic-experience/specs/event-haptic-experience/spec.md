## Purpose

Give people an optional, safe tactile way to explore the character and relative intensity of cached disaster events without treating the experience as sensing, prediction, or emergency notification.

## ADDED Requirements

### Requirement: Opt-in educational event haptics
The system SHALL provide a manually triggered haptic experience from details for supported event categories, label it as an educational representation, and SHALL NOT start haptic playback automatically, in the background, or as an emergency alert.

#### Scenario: User starts a supported event experience
- **WHEN** a user selects the haptic experience control for a supported event
- **THEN** the system plays that event's short educational haptic pattern and presents a control to stop it

#### Scenario: Unsupported event is displayed
- **WHEN** a user opens an event whose category has no haptic pattern
- **THEN** the details do not offer a misleading event-haptic experience

### Requirement: Distinct bounded disaster representations
The system SHALL provide distinguishable, bounded haptic patterns for earthquakes, wildfires, severe storms, floods, and volcanic events. Earthquake patterns SHALL use an available event magnitude to select a relative intensity tier and SHALL use a neutral default tier when magnitude is unavailable.

#### Scenario: Earthquake magnitude affects the representation
- **WHEN** a user starts an earthquake haptic experience for events with different available magnitude tiers
- **THEN** the selected patterns communicate the differing relative intensity without claiming physical reproduction

#### Scenario: Playback reaches its end
- **WHEN** an event haptic pattern completes
- **THEN** it stops without repeating and returns the control to its ready state

### Requirement: User and device control
The system SHALL respect the device's haptic-feedback preference, avoid playback when a vibration actuator is unavailable, allow the user to stop active playback, and stop playback when the detail screen leaves the foreground.

#### Scenario: Haptics are disabled or unavailable
- **WHEN** device haptics are disabled or the device lacks a usable vibrator
- **THEN** the system does not vibrate and communicates that the experience is unavailable

#### Scenario: User stops playback
- **WHEN** a user selects Stop during an active event haptic experience
- **THEN** vibration stops immediately and no further pattern segments play
