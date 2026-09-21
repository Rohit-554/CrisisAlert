## Purpose

Keep source-backed disaster data reasonably current when the app is not open while protecting cached content and device resources.

## ADDED Requirements

### Requirement: Network-constrained background refresh
The system SHALL schedule periodic refresh work that runs only when network connectivity is available, refreshes all configured sources within their published usage limits, and avoids concurrent source-refresh operations.

#### Scenario: Scheduled refresh has network access
- **WHEN** scheduled refresh work runs with network connectivity
- **THEN** it refreshes the configured source snapshots and updates the cache only with valid source results

#### Scenario: Scheduled refresh has no network access
- **WHEN** a scheduled refresh is due while network connectivity is unavailable
- **THEN** it does not attempt source requests and leaves existing cached content unchanged

### Requirement: Failure-safe cached feed
The system SHALL retain the last successfully cached events when background refresh fails and SHALL use normal platform retry behavior for transient failures.

#### Scenario: Background refresh fails
- **WHEN** a transient source or network failure occurs during background refresh
- **THEN** previously cached events remain available on the next app launch and the failed snapshot does not replace them

