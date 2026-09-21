## 1. Haptic Foundation

- [x] 1.1 Add the foreground-only vibration permission and a capability-aware haptics controller; verify the debug build completes without any sensor or location permission
- [x] 1.2 Define finite educational patterns for each supported disaster type and earthquake magnitude tier, with a pre-26 fallback; verify every pattern is non-repeating and remains within the two-second cap
- [x] 1.3 Respect system haptic feedback settings and unavailable actuators; verify the controller reports unavailable without attempting vibration

## 2. Detail Experience

- [x] 2.1 Add non-persisted haptic playback state and start/stop actions to the detail presentation layer; verify a new event detail uses its own event-derived pattern
- [x] 2.2 Render the supported-event `Feel this event`/Stop control, educational disclaimer, and unavailable state in details; verify unsupported categories expose no haptic control
- [x] 2.3 Cancel active playback on explicit Stop, detail-route exit, and ViewModel disposal; verify no vibration continues after leaving details

## 3. Validation

- [ ] 3.1 Add focused tests for pattern selection, earthquake magnitude tiers, unavailable hardware/settings, and cancellation behavior; verify they pass
- [ ] 3.2 Build the debug APK and manually validate all supported category patterns, Stop, system-haptics-disabled behavior, and detail navigation on a physical device
