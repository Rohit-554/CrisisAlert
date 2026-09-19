# AGENT.md — CrisisLens

## Project Overview

**CrisisLens** is a modern Android application that helps users discover and track recent natural disasters around the world using free and publicly available APIs.

The application should aggregate events such as:

- Earthquakes
- Wildfires
- Storms
- Volcanoes
- Floods
- Other major natural disaster events when supported by the selected APIs

The goal is to build a realistic, production-style Android application that can also be used to demonstrate modern Android development practices and AI-assisted development workflows.

---

## Core User Experience

Users should be able to:

1. Browse recent disaster events.
2. Filter events by disaster type.
3. Search events by title, location, or relevant keywords.
4. Open an event and view detailed information.
5. View the disaster location on a map.
6. View current weather information for the event location.
7. Save or bookmark important events.
8. Access previously loaded data while offline.
9. Refresh the application to retrieve the latest available events.
10. Clearly understand loading, empty, offline, and error states.

---

## Technical Requirements

### Platform

- Android
- Kotlin
- Jetpack Compose
- Material 3

### Architecture

Use a clean, production-friendly architecture.

Preferred structure:

- Presentation
- Domain
- Data

Use **MVVM** for presentation logic and follow Clean Architecture principles where useful.

Avoid unnecessary abstraction or overengineering.

Suggested modules/packages:

```text
com.crisislens
├── core
│   ├── common
│   ├── designsystem
│   ├── network
│   └── database
├── data
│   ├── local
│   ├── remote
│   ├── mapper
│   └── repository
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── feature
│   ├── home
│   ├── details
│   ├── saved
│   ├── search
│   └── settings
└── navigation
```

The exact package structure may evolve if a simpler structure is more appropriate.

---

## Required Libraries and Technologies

Use:

- Kotlin
- Jetpack Compose
- Material 3
- Coroutines
- Kotlin Flow / StateFlow
- Room
- Koin 4 for dependency injection
- Retrofit **or** Ktor Client for networking
- Kotlin Serialization or Moshi for JSON parsing
- Navigation Compose or Navigation 3
- WorkManager only if background refresh becomes useful

Prefer stable library versions unless there is a strong reason to use alpha or beta APIs.

Do not mix multiple libraries that solve the same problem without a clear need.

---

## API Requirements

Use only APIs that are:

- Free
- Publicly accessible
- Suitable for educational/demo usage
- Available without paid subscriptions

Prefer APIs that do not require authentication.

If an API key is required, it must have a usable free tier.

Potential sources may include public services for:

- Earthquakes
- Global natural events
- Wildfires
- Weather
- Volcano activity

Do not hardcode API secrets in source code.

API keys should be supplied through:

- `local.properties`
- environment variables
- Gradle BuildConfig fields

Secrets must never be committed to version control.

---

## Unified Disaster Model

Different APIs will return different response structures.

Normalize them into a common domain model.

Example:

```kotlin
data class DisasterEvent(
    val id: String,
    val title: String,
    val description: String?,
    val type: DisasterType,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val startedAt: Instant?,
    val updatedAt: Instant?,
    val severity: DisasterSeverity?,
    val source: String,
    val sourceUrl: String?,
    val imageUrl: String? = null,
    val isSaved: Boolean = false
)
```

Example disaster types:

```kotlin
enum class DisasterType {
    EARTHQUAKE,
    WILDFIRE,
    STORM,
    VOLCANO,
    FLOOD,
    OTHER
}
```

Do not expose API DTO models directly to the UI layer.

---

## Data Flow

Use a repository-driven architecture.

Expected flow:

```text
Public APIs
    ↓
Remote Data Sources
    ↓
DTOs
    ↓
Mappers
    ↓
Repository
    ↓
Room Cache
    ↓
Domain Models
    ↓
ViewModel
    ↓
StateFlow
    ↓
Compose UI
```

The UI should primarily consume domain models and UI state rather than network DTOs.

---

## Offline-First Behaviour

Previously loaded disaster information should remain available when the device has no internet connection.

Room should act as the local cache.

Expected behaviour:

1. Load cached data immediately when available.
2. Attempt to fetch fresh remote data.
3. Update the local database after a successful response.
4. Observe Room using Flow.
5. UI automatically updates when cached data changes.
6. If the network request fails, continue displaying available cached data.
7. Show a non-blocking offline/error indication when appropriate.

Do not delete useful cached data simply because a network request fails.

---

## Home Screen

The main screen should contain:

- App title / top app bar
- Search entry point or search bar
- Disaster type filters
- Recent disaster event list
- Pull-to-refresh or refresh action
- Navigation to saved events

Suggested filter chips:

- All
- Earthquakes
- Wildfires
- Storms
- Volcanoes
- Floods

Each event card should display useful information such as:

- Event title
- Disaster type
- Location
- Date/time
- Severity when available
- Saved/bookmarked state

Avoid overcrowding cards.

---

## Event Details Screen

The details screen should display as much reliable information as available:

- Event name
- Disaster category
- Description
- Location
- Coordinates
- Start/update time
- Severity
- Original data source
- Source link
- Map/location section
- Current weather section
- Save/bookmark action

Missing API fields should be handled gracefully.

Never display fake placeholder facts as real data.

---

## Weather

When coordinates are available, retrieve current weather for the disaster location.

Weather may include:

- Temperature
- Weather condition
- Wind speed
- Humidity
- Precipitation if available

Weather calls should preferably happen when opening event details instead of requesting weather for every event in the feed.

Cache weather data only if it provides a meaningful UX benefit.

---

## Maps

Display an event's location visually when latitude and longitude are available.

The initial implementation may use any suitable free or practical Android map solution.

The map feature should not block the rest of the app if map configuration is unavailable.

A fallback should still show:

```text
Latitude: ...
Longitude: ...
```

---

## Saved Events

Users can bookmark events.

Saved state must persist locally using Room.

Saved events should be accessible through a dedicated screen.

Saving an event must not depend on network availability.

Users should be able to remove saved events.

---

## Search

Search should support matching relevant fields such as:

- Event title
- Location
- Disaster type

Prefer local filtering/searching over excessive API calls unless remote search provides meaningful value.

Debounce search input if necessary.

---

## UI State

Each feature should expose explicit UI state.

Example:

```kotlin
sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val events: List<DisasterEvent>,
        val selectedFilter: DisasterType?,
        val isRefreshing: Boolean,
        val isOffline: Boolean
    ) : HomeUiState

    data class Error(
        val message: String,
        val cachedEvents: List<DisasterEvent> = emptyList()
    ) : HomeUiState
}
```

Prefer immutable UI models.

Avoid putting business logic directly inside composables.

---

## Compose Guidelines

Composable functions should:

- Remain focused on UI
- Receive state from ViewModels
- Emit user actions through callbacks
- Avoid direct repository/network/database access
- Prefer state hoisting
- Be reusable where reasonable

Create previews for important reusable components when practical.

Use Material 3 components consistently.

Support dark mode.

---

## ViewModel Guidelines

ViewModels should:

- Expose immutable `StateFlow`
- Handle user actions
- Coordinate domain/use-case logic
- Never hold references to Activity or View objects
- Avoid performing raw networking directly
- Avoid exposing mutable state publicly

Prefer:

```kotlin
private val _uiState = MutableStateFlow(...)
val uiState = _uiState.asStateFlow()
```

or state derived using `combine`, `map`, and `stateIn`.

---

## Koin 4

Use Koin 4 for dependency injection.

Inject:

- API clients
- Room database
- DAOs
- repositories
- use cases
- ViewModels

Keep module definitions organized by responsibility.

Example concept:

```text
networkModule
databaseModule
repositoryModule
domainModule
viewModelModule
```

Avoid using global service locators manually.

---

## Networking

Networking code must handle:

- Successful responses
- HTTP errors
- Timeouts
- Connectivity failures
- Malformed responses
- Missing/null fields

Never assume external APIs are perfectly reliable.

Convert network exceptions into application-level failures that can be handled gracefully.

---

## Database

Use Room for:

- Disaster event cache
- Saved/bookmarked state
- Potential metadata needed for offline operation

Entities should remain separate from domain models when their responsibilities differ.

Use mapper functions such as:

```text
DTO -> Entity
Entity -> Domain
Domain -> Entity
```

where appropriate.

---

## Testing

The codebase should be testable.

Prioritize tests for:

- DTO/domain mapping
- Repositories
- Filtering
- Search
- Save/bookmark behaviour
- ViewModel state transitions
- Error handling

Avoid requiring Android framework dependencies for domain-layer tests.

Use fake repositories where appropriate.

---

## Error Handling

Never crash because an external API:

- returns null
- omits fields
- returns an unexpected category
- fails temporarily

Display understandable states such as:

- Unable to refresh
- Showing cached data
- No events found
- No saved events
- Weather unavailable
- Location unavailable

Errors should be recoverable whenever possible.

---

## Loading Behaviour

Avoid blocking the entire app unnecessarily.

Use:

- Initial loading state
- Pull-to-refresh indicator
- Skeleton/progress indicators where useful
- Cached content while refreshing

Refreshing should not replace already visible cached content with a full-screen loading state.

---

## Performance

Consider:

- LazyColumn for event lists
- Stable UI models
- Avoiding unnecessary recomposition
- Avoiding weather requests for every feed item
- Database queries using Flow
- Pagination only if the chosen APIs/data volume require it

Do not prematurely optimize.

---

## Accessibility

Include:

- meaningful content descriptions
- readable text sizes
- sufficient contrast
- reasonable touch targets
- semantic labels where appropriate

Do not rely exclusively on color to communicate disaster categories or severity.

---

## Security and Privacy

The app should request only permissions it genuinely requires.

Avoid location permission unless a future feature specifically needs the user's current location.

Viewing disaster coordinates does not require device location permission.

Do not collect user information.

Do not log API keys or sensitive values.

---

## Code Quality Rules

Follow these rules when modifying the project:

1. Write idiomatic Kotlin.
2. Prefer simple solutions over clever abstractions.
3. Do not duplicate logic unnecessarily.
4. Do not create interfaces without a meaningful abstraction boundary.
5. Keep functions focused.
6. Use descriptive names.
7. Avoid giant ViewModels.
8. Avoid giant composables.
9. Separate UI, business, and data concerns.
10. Never expose DTOs directly to UI.
11. Avoid hardcoded strings in UI where resources are appropriate.
12. Do not silently swallow exceptions.
13. Do not introduce a dependency unless it provides clear value.
14. Keep the project compiling after meaningful milestones.
15. Run relevant tests after important changes.

---

## Agent Behaviour

When an AI coding agent works on this repository:

### Before Coding

The agent should:

1. Inspect the existing project structure.
2. Read this `AGENT.md`.
3. Understand existing dependencies and conventions.
4. Avoid rewriting working code unnecessarily.
5. Break large features into smaller implementation steps.

### While Coding

The agent should:

- Make focused changes.
- Preserve existing project conventions.
- Reuse existing components before creating duplicates.
- Keep compile errors from accumulating.
- Update related tests where appropriate.
- Verify API models against actual API responses.
- Handle nullable/external data defensively.

### After Coding

The agent should verify:

- Project compiles.
- Relevant tests pass.
- Imports are clean.
- No hardcoded secrets were introduced.
- No unrelated files were modified.
- Offline behaviour still works.
- Loading and error states are handled.

---

## Development Philosophy

This project should demonstrate how a real Android application evolves rather than being generated as one massive code dump.

Prefer incremental development:

```text
Requirement
    ↓
Plan
    ↓
Small implementation
    ↓
Compile/Test
    ↓
Review
    ↓
Next feature
```

When requirements are ambiguous, make the smallest reasonable assumption and document it rather than building unnecessary functionality.

---

## Suggested Development Order

A reasonable sequence is:

1. Project setup and dependencies
2. Base architecture
3. Disaster domain models
4. First public disaster API integration
5. Home event feed
6. Room cache
7. Offline-first repository
8. Event filtering
9. Event details
10. Weather API integration
11. Maps
12. Saved events
13. Search
14. Error/loading/offline polish
15. Tests
16. UI polish and accessibility

Do not attempt to implement every external API simultaneously.

Start with one reliable event source and establish the architecture before adding additional sources.

---

## Definition of Done

A feature is considered complete when:

- It works functionally.
- Loading state is handled.
- Failure state is handled.
- Offline behaviour is considered where relevant.
- Code follows the current architecture.
- The project compiles.
- Important logic has appropriate tests.
- No secrets are committed.
- UI behaves correctly with missing/null API data.

---

## Product Direction

CrisisLens is primarily an educational but realistic Android project.

The implementation should be understandable by Android developers who already know basic Kotlin and Android concepts while still exposing them to production-oriented patterns such as:

- Clean architecture
- API integration
- Local caching
- Offline-first development
- Dependency injection
- Reactive state management
- Debugging
- Testing
- Agent-assisted development
- Iterative feature planning

Avoid making the codebase unnecessarily complicated simply to demonstrate architectural concepts.

The final application should feel like a useful real-world product rather than a collection of disconnected demo features.
