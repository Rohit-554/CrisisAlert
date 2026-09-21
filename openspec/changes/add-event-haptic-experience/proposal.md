## Why

Disaster-feed entries currently provide visual and textual context only. An optional, clearly educational haptic experience can help users understand relative event character and intensity without representing the app as an alerting or sensor-detection system.

## What Changes

- Add an opt-in haptic experience control to eligible disaster-event details.
- Provide distinct, short haptic patterns for earthquakes, wildfires, storms, floods, and volcanic events, with earthquake magnitude affecting the selected pattern tier.
- Make playback foreground-only, manually started and stopped, respectful of device haptic settings, and unavailable when hardware support is absent.
- Label every experience as an educational representation rather than real-time sensing, an emergency alert, or a measured reproduction.

## Capabilities

### New Capabilities

- `event-haptic-experience`: Provide safe, opt-in, event-specific educational haptic playback from disaster-event details.

### Modified Capabilities

None.

## Impact

- Updates the detail UI and introduces a small haptics abstraction, Android vibrator integration, and `VIBRATE` manifest permission.
- Uses local cached event data only; adds no sensor access, network request, background work, notification, or location permission.
