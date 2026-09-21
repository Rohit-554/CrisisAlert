package io.jadu.crisisprotect.feature.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.jadu.crisisprotect.R
import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.feature.common.openSafeWebUrl
import io.jadu.crisisprotect.feature.common.isSafeWebUrl
import io.jadu.crisisprotect.feature.common.toLocalizedDateTime
import io.jadu.crisisprotect.domain.model.DisasterType
import io.jadu.crisisprotect.ui.theme.CrisisProtectTheme
import java.time.Instant
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DetailsRoute(eventId: String, onBack: () -> Unit) {
    val viewModel: DetailsViewModel = koinViewModel(parameters = { parametersOf(eventId) })
    val uiState by viewModel.uiState.collectAsState()
    DetailsScreen(uiState = uiState, onBack = onBack, onToggleSaved = viewModel::toggleSaved, onRetryWeather = viewModel::retryWeather, onToggleHaptics = viewModel::toggleHapticExperience)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    uiState: DetailsUiState,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit = {},
    onRetryWeather: () -> Unit = {},
    onToggleHaptics: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.event_details)) },
                navigationIcon = {
                    IconButton(onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when (uiState) {
            DetailsUiState.Loading -> DetailsLoading(Modifier.padding(paddingValues))
            DetailsUiState.Unavailable -> DetailsUnavailable(Modifier.padding(paddingValues), onBack)
            is DetailsUiState.Content -> DetailsContent(
                event = uiState.event,
                weather = uiState.weather,
                snackbarHostState = snackbarHostState,
                onToggleSaved = onToggleSaved,
                onRetryWeather = onRetryWeather,
                haptics = uiState.haptics,
                onToggleHaptics = onToggleHaptics,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun DetailsContent(
    event: DisasterEvent,
    weather: WeatherUiState,
    snackbarHostState: SnackbarHostState,
    onToggleSaved: () -> Unit,
    onRetryWeather: () -> Unit,
    haptics: EventHapticUiState,
    onToggleHaptics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val noSourceHandlerMessage = stringResource(R.string.no_source_handler)
    val title = event.title ?: event.locationName ?: stringResource(R.string.event_title_unavailable)
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Public, null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.category_earthquake), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                event.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                event.magnitude?.let { magnitude ->
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.magnitude_format, magnitude)) })
                }
                AssistChip(
                    onClick = onToggleSaved,
                    label = { Text(if (event.isSaved) "Saved" else "Save event") },
                    leadingIcon = { Icon(if (event.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, null) },
                )
            }
        }
        if (haptics !is EventHapticUiState.Unsupported) item {
            DetailCard(stringResource(R.string.haptic_experience)) {
                Text(stringResource(R.string.haptic_educational_notice), style = MaterialTheme.typography.bodySmall)
                when (haptics) {
                    EventHapticUiState.Ready -> Button(onClick = onToggleHaptics) { Text(stringResource(R.string.feel_this_event)) }
                    EventHapticUiState.Playing -> Button(onClick = onToggleHaptics) { Text(stringResource(R.string.stop_haptic_experience)) }
                    EventHapticUiState.Unavailable -> Text(stringResource(R.string.haptic_unavailable))
                    EventHapticUiState.Unsupported -> Unit
                }
            }
        }
        item {
            DetailCard(stringResource(R.string.event_information)) {
                DetailRow(stringResource(R.string.occurred), event.occurredAt.toLocalizedDateTime())
                DetailRow(stringResource(R.string.last_updated), event.updatedAt?.toLocalizedDateTime() ?: stringResource(R.string.not_available))
                DetailRow(stringResource(R.string.source), event.source)
            }
        }
        item {
            DetailCard(stringResource(R.string.coordinates)) {
                val latitude = event.latitude?.toString() ?: stringResource(R.string.not_available)
                val longitude = event.longitude?.toString() ?: stringResource(R.string.not_available)
                DetailRow(stringResource(R.string.latitude), latitude)
                DetailRow(stringResource(R.string.longitude), longitude)
            }
        }
        event.latitude?.let { latitude -> event.longitude?.let { longitude ->
            item {
                DetailCard("Location map") {
                    EventLocationMap(latitude, longitude)
                    Text("© OpenFreeMap © OpenMapTiles © OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall)
                }
            }
        } }
        if (event.latitude != null && event.longitude != null) item {
            DetailCard("Current weather") {
                when (weather) {
                    WeatherUiState.Idle, WeatherUiState.Loading -> Text("Loading weather…")
                    is WeatherUiState.Available -> {
                        weather.weather.temperatureCelsius?.let { DetailRow("Temperature", "${it} °C") }
                        weather.weather.humidityPercent?.let { DetailRow("Humidity", "$it%") }
                        weather.weather.precipitationMm?.let { DetailRow("Precipitation", "$it mm") }
                        weather.weather.windSpeedKmh?.let { DetailRow("Wind", "$it km/h") }
                        weather.weather.condition?.let { DetailRow("Conditions", it) }
                        Text("Weather data: Open-Meteo", style = MaterialTheme.typography.labelSmall)
                    }
                    WeatherUiState.Unavailable -> {
                        Text("Weather is unavailable for this event.")
                        AssistChip(onRetryWeather, { Text(stringResource(R.string.retry)) })
                    }
                }
            }
        }
        item {
            DetailCard(stringResource(R.string.source)) {
                Text(stringResource(R.string.data_source_format, if (event.source == "USGS") stringResource(R.string.usgs_source) else event.source))
                event.upstreamSource?.let { upstream -> Text("Upstream source: $upstream") }
                event.upstreamSourceUrl?.takeIf(::isSafeWebUrl)?.let { upstreamUrl ->
                    AssistChip(
                        onClick = { if (!context.openSafeWebUrl(upstreamUrl)) scope.launch { snackbarHostState.showSnackbar(noSourceHandlerMessage) } },
                        label = { Text("Open upstream source") },
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                event.sourceUrl?.takeIf(::isSafeWebUrl)?.let { sourceUrl ->
                    AssistChip(
                        onClick = {
                            if (!context.openSafeWebUrl(sourceUrl)) {
                                scope.launch { snackbarHostState.showSnackbar(noSourceHandlerMessage) }
                            }
                        },
                        label = { Text(stringResource(R.string.open_original_source)) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, null) },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                } ?: Text(
                    text = stringResource(R.string.invalid_source_link),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun DetailCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, modifier = Modifier.padding(start = 16.dp), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DetailsLoading(modifier: Modifier) {
    Column(modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Text(stringResource(R.string.loading_event_details), Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun DetailsUnavailable(modifier: Modifier, onBack: () -> Unit) {
    Column(modifier.fillMaxSize().padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(40.dp))
        Text(stringResource(R.string.event_unavailable), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
        Text(stringResource(R.string.event_unavailable_detail), modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        AssistChip(onBack, { Text(stringResource(R.string.back_to_events)) }, Modifier.padding(top = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsCompletePreview() {
    CrisisProtectTheme {
            DetailsScreen(
            DetailsUiState.Content(
                DisasterEvent(
                    id = "usgs:preview",
                    title = "M 5.2 - Example region",
                    type = DisasterType.EARTHQUAKE,
                    latitude = 12.5,
                    longitude = 42.0,
                    locationName = "Example region",
                    occurredAt = Instant.ofEpochMilli(1_700_000_000_000),
                    updatedAt = null,
                    magnitude = 5.2,
                    source = "USGS",
                    sourceUrl = null,
                ),
            ),
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsUnavailablePreview() {
    CrisisProtectTheme { DetailsScreen(DetailsUiState.Unavailable, onBack = {}) }
}
