package io.jadu.crisisprotect.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.jadu.crisisprotect.R
import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.model.DisasterType
import io.jadu.crisisprotect.feature.common.toLocalizedDateTime
import io.jadu.crisisprotect.ui.theme.CrisisProtectTheme
import org.koin.androidx.compose.koinViewModel
import java.time.Instant

@Composable
fun HomeRoute(
    onEventSelected: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(uiState, viewModel::refresh, viewModel::selectFilter, onEventSelected)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onFilterSelected: (DisasterType?) -> Unit,
    onEventSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name))
                        Text(
                            stringResource(R.string.recent_global_events),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !uiState.isRefreshing) {
                        Icon(Icons.Default.Refresh, stringResource(R.string.refresh_events))
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            FilterRow(uiState.selectedFilter, onFilterSelected)
            if (uiState.isRefreshing && uiState.events.isNotEmpty()) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            if (uiState.refreshFailed && uiState.events.isNotEmpty()) RefreshFailureBanner(onRefresh)
            when {
                uiState.isInitialLoading && uiState.events.isEmpty() -> LoadingContent()
                uiState.refreshFailed && uiState.events.isEmpty() -> ErrorContent(onRefresh)
                uiState.events.isEmpty() -> EmptyContent(uiState.selectedFilter)
                else -> EventList(uiState.events, onEventSelected)
            }
        }
    }
}

@Composable
private fun FilterRow(selectedFilter: DisasterType?, onFilterSelected: (DisasterType?) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(selectedFilter == null, { onFilterSelected(null) }, { Text(stringResource(R.string.filter_all)) })
        FilterChip(
            selectedFilter == DisasterType.EARTHQUAKE,
            { onFilterSelected(DisasterType.EARTHQUAKE) },
            { Text(stringResource(R.string.filter_earthquakes)) },
        )
    }
}

@Composable
private fun EventList(events: List<DisasterEvent>, onEventSelected: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(events, key = { it.id }) { event -> EventCard(event) { onEventSelected(event.id) } }
    }
}

@Composable
private fun EventCard(event: DisasterEvent, onClick: () -> Unit) {
    val title = event.title ?: event.locationName ?: stringResource(R.string.event_title_unavailable)
    val eventDescription = stringResource(R.string.event_card_description, title)
    Card(
        Modifier.fillMaxWidth()
            .semantics { contentDescription = eventDescription }
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Public, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.category_earthquake), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                event.magnitude?.let { AssistChip(onClick, { Text(stringResource(R.string.magnitude_format, it)) }) }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(event.locationName ?: stringResource(R.string.location_unavailable), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            Text(event.occurredAt.toLocalizedDateTime(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RefreshFailureBanner(onRefresh: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.ErrorOutline, null)
        Text(stringResource(R.string.unable_to_refresh), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        AssistChip(onRefresh, { Text(stringResource(R.string.retry)) })
    }
}

@Composable
private fun LoadingContent() {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Text(stringResource(R.string.loading_events), Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ErrorContent(onRefresh: () -> Unit) = StateContent(
    stringResource(R.string.events_could_not_load),
    stringResource(R.string.events_load_error_detail),
    stringResource(R.string.retry),
    onRefresh,
)

@Composable
private fun EmptyContent(selectedFilter: DisasterType?) = StateContent(
    stringResource(if (selectedFilter == null) R.string.no_recent_events else R.string.no_matching_events),
    stringResource(if (selectedFilter == null) R.string.no_recent_events_detail else R.string.no_matching_events_detail),
)

@Composable
private fun StateContent(title: String, detail: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        Arrangement.Center,
        Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.Public, null, modifier = Modifier.size(40.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        if (actionLabel != null && onAction != null) AssistChip(onAction, { Text(actionLabel) }, Modifier.padding(top = 16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    CrisisProtectTheme { HomeScreen(HomeUiState(isInitialLoading = false, hasLoadedOnce = true), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun HomePopulatedPreview() {
    CrisisProtectTheme {
        HomeScreen(
            HomeUiState(
                events = listOf(
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
                isInitialLoading = false,
                hasLoadedOnce = true,
            ),
            onRefresh = {},
            onFilterSelected = {},
            onEventSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeDarkFilteredEmptyPreview() {
    CrisisProtectTheme(darkTheme = true) {
        HomeScreen(
            HomeUiState(selectedFilter = DisasterType.EARTHQUAKE, isInitialLoading = false, hasLoadedOnce = true),
            onRefresh = {},
            onFilterSelected = {},
            onEventSelected = {},
        )
    }
}
