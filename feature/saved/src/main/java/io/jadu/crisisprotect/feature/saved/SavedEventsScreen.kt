package io.jadu.crisisprotect.feature.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.jadu.crisisprotect.domain.model.DisasterEvent
import io.jadu.crisisprotect.domain.repository.DisasterRepository
import org.koin.compose.koinInject

@Composable
fun SavedEventsRoute(onBack: () -> Unit, onEventSelected: (String) -> Unit) {
    val repository: DisasterRepository = koinInject()
    val events by repository.observeSavedEvents().collectAsState(emptyList())
    SavedEventsScreen(events, onBack, onEventSelected)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedEventsScreen(events: List<DisasterEvent>, onBack: () -> Unit, onEventSelected: (String) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Saved events") }, navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { padding ->
        if (events.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding), Arrangement.Center, Alignment.CenterHorizontally) {
                Icon(Icons.Default.Bookmark, null)
                Text("No saved events", style = MaterialTheme.typography.titleMedium)
                Text("Saved events remain available offline.")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(events, key = { it.id }) { event ->
                    Card(Modifier.fillMaxWidth().clickable { onEventSelected(event.id) }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(event.title ?: event.locationName ?: "Event details unavailable", style = MaterialTheme.typography.titleMedium)
                            Text(event.type.name.lowercase().replaceFirstChar(Char::uppercase), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
