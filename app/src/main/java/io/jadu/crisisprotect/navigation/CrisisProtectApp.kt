package io.jadu.crisisprotect.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.jadu.crisisprotect.feature.details.DetailsRoute
import io.jadu.crisisprotect.feature.home.HomeRoute
import io.jadu.crisisprotect.feature.saved.SavedEventsRoute
import kotlinx.serialization.Serializable

@Serializable
private data object HomeRouteKey : NavKey

@Serializable
private data object SavedEventsRouteKey : NavKey

@Serializable
private data class DetailsRouteKey(val eventId: String) : NavKey

@Composable
fun CrisisProtectApp() {
    val backStack = rememberNavBackStack(HomeRouteKey)
    val destinations = entryProvider<NavKey> {
        entry<HomeRouteKey> {
            HomeRoute(
                onEventSelected = { eventId -> backStack.add(DetailsRouteKey(eventId)) },
                onSavedEvents = { backStack.add(SavedEventsRouteKey) },
            )
        }
        entry<SavedEventsRouteKey> {
            SavedEventsRoute(
                onBack = { backStack.removeLastOrNull() },
                onEventSelected = { eventId -> backStack.add(DetailsRouteKey(eventId)) },
            )
        }
        entry<DetailsRouteKey> { route ->
            DetailsRoute(
                eventId = route.eventId,
                onBack = { backStack.removeLastOrNull() },
            )
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = destinations,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>()),
    )
}
