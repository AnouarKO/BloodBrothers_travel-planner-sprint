package com.example.bbtraveling.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bbtraveling.ui.screens.GalleryScreen
import com.example.bbtraveling.ui.screens.SplashScreen
import com.example.bbtraveling.ui.screens.TermsScreen
import com.example.bbtraveling.ui.screens.TripDetailScreen
import com.example.bbtraveling.ui.viewmodel.SettingsViewModel
import com.example.bbtraveling.ui.viewmodel.TripsViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    tripsViewModel: TripsViewModel,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {
        composable(Routes.Splash) {
            SplashScreen(
                onFinished = {
                    val nextRoute = if (settingsViewModel.hasAcceptedTerms()) {
                        Routes.Main
                    } else {
                        Routes.TermsOnboarding
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Main) {
            MainShell(
                rootNavController = navController,
                tripsViewModel = tripsViewModel,
                settingsViewModel = settingsViewModel
            )
        }

        composable(
            route = Routes.TripDetail,
            arguments = listOf(navArgument(Routes.ARG_TRIP_ID) { type = NavType.StringType })
        ) { entry ->
            val tripId = entry.arguments?.getString(Routes.ARG_TRIP_ID).orEmpty()
            TripDetailScreen(
                tripId = tripId,
                tripsViewModel = tripsViewModel,
                onBack = { navController.popBackStack() },
                onOpenGallery = { navController.navigate(Routes.galleryTrip(tripId)) }
            )
        }

        composable(
            route = Routes.GalleryTrip,
            arguments = listOf(navArgument(Routes.ARG_TRIP_ID) { type = NavType.StringType })
        ) { entry ->
            val tripId = entry.arguments?.getString(Routes.ARG_TRIP_ID)
            val trips by tripsViewModel.trips.collectAsState()
            GalleryScreen(
                tripId = tripId,
                trips = trips,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Terms) {
            TermsScreen(
                onAccept = { navController.popBackStack() },
                onReject = { navController.popBackStack() }
            )
        }

        composable(Routes.TermsOnboarding) {
            TermsScreen(
                onAccept = {
                    settingsViewModel.acceptTerms()
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.TermsOnboarding) { inclusive = true }
                    }
                },
                onReject = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.TermsOnboarding) { inclusive = true }
                    }
                }
            )
        }
    }
}
