package io.jadu.crisisprotect.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.jadu.crisisprotect.feature.details.DetailsRoute
import io.jadu.crisisprotect.feature.home.HomeRoute

private object Destinations {
    const val Home = "home"
    const val Details = "details/{eventId}"

    fun details(eventId: String): String = "details/${Uri.encode(eventId)}"
}

@Composable
fun CrisisProtectApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destinations.Home) {
        composable(Destinations.Home) {
            HomeRoute(onEventSelected = { eventId -> navController.navigate(Destinations.details(eventId)) })
        }
        composable(
            route = Destinations.Details,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")?.let(Uri::decode).orEmpty()
            DetailsRoute(eventId = eventId, onBack = navController::popBackStack)
        }
    }
}
