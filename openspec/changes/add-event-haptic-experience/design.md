## Context

See proposal.md and the `event-haptic-experience` spec. The application is a Compose Android app with cached event detail data and a minimum SDK of 24. Details already have type, optional earthquake magnitude, and lifecycle-driven ViewModels; no device sensors are currently requested or needed.

## Goals / Non-Goals

**Goals:**

- Keep haptic generation local, bounded, cancellable, and independent of network and event refresh.
- Make the haptic selection deterministic from cached event category and optional magnitude.
- Keep platform-specific vibrator calls outside detail UI state.

**Non-Goals:**

- Reading accelerometer, location, microphone, or any other device sensor.
- Reproducing a physical event, providing medical/safety guidance, or raising alerts/notifications.
- Repeating, scheduled, background, or notification haptics.

## Decisions

### Use a haptics controller with event-derived pattern definitions

The detail screen will request start/stop from a small controller that maps a `DisasterEvent` into a named, finite pattern. It exposes availability and playback state to the ViewModel/UI but does not persist either state. Earthquake magnitude maps to low, medium, or high tiers when present; all other supported categories use category-specific patterns. This keeps the definitions reviewable and prevents presentation composables from owning vibrator resources.

Direct `Vibrator` calls in `DetailsScreen` were rejected because they make cancellation on screen exit and hardware fallback difficult to keep consistent.

### Use capability-aware foreground vibration with compatibility fallback

On supported platform versions the controller will use a finite `VibrationEffect`; pre-26 devices receive a bounded legacy fallback. It first verifies actuator support and the user haptic-feedback preference. `VIBRATE` is added only for foreground user-triggered effects. Each pattern is capped at approximately two seconds, has no repeat index, and is cancelled on explicit stop, disposal, or detail-route exit.

Advanced primitives and envelope APIs were rejected as the primary path because their hardware support varies substantially. They can be considered as an optional future enhancement behind the same controller.

### Make the experience visibly optional and non-alarmist

Supported detail screens show a `Feel this event` control and an educational-disclaimer label. While playing, the control changes to Stop. Unsupported, disabled, or hardware-unavailable states remain readable and do not block event details. No user setting is required in the first slice because the platform haptic preference is authoritative.

Automatic playback was rejected because it can be startling, conflicts with expected haptic accessibility behavior, and could be mistaken for an alert.

## Risks / Trade-offs

- [Haptic motors differ substantially across devices] -> Use relative pattern semantics, finite fallback patterns, and never promise exact physical reproduction.
- [Custom vibration can be uncomfortable or distracting] -> Require a tap, cap duration, clearly expose Stop, and cancel on navigation.
- [System haptic preference cannot guarantee identical OEM behavior] -> Check availability and the platform preference before playback; treat no playback as a supported outcome.
- [A pattern is mistaken for an emergency signal] -> Use educational/non-alert language and omit notification/background operation.

## Migration Plan

1. Add the manifest permission and haptics controller with deterministic pattern selection.
2. Connect detail UI state and controls, including cancellation when leaving details.
3. Validate supported, unavailable, stopped, and route-exit behavior on a physical device; remove the controller and permission to roll back with no data migration.
