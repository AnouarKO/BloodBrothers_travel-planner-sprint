package com.example.bbtraveling

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.bbtraveling.navigation.AppNavGraph
import com.example.bbtraveling.ui.theme.BBTravelingTheme
import com.example.bbtraveling.ui.viewmodel.SettingsViewModel
import com.example.bbtraveling.ui.viewmodel.SettingsViewModelFactory
import com.example.bbtraveling.ui.viewmodel.TripsViewModel
import com.example.bbtraveling.ui.viewmodel.TripsViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(this)
        applyLanguage(appContainer.userSettingsRepository.readStoredLanguageTag())

        setContent {
            val tripsViewModel: TripsViewModel = viewModel(
                factory = TripsViewModelFactory(appContainer.tripRepository)
            )
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(appContainer.userSettingsRepository)
            )
            App(
                tripsViewModel = tripsViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}

@Composable
private fun App(
    tripsViewModel: TripsViewModel,
    settingsViewModel: SettingsViewModel
) {
    val settings by settingsViewModel.settings.collectAsState()

    LaunchedEffect(settings.languageTag) {
        applyLanguage(settings.languageTag)
    }

    BBTravelingTheme(darkTheme = settings.darkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            AppNavGraph(
                navController = navController,
                tripsViewModel = tripsViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}

private fun applyLanguage(languageTag: String) {
    val targetLocales = LocaleListCompat.forLanguageTags(languageTag)
    if (AppCompatDelegate.getApplicationLocales() != targetLocales) {
        AppCompatDelegate.setApplicationLocales(targetLocales)
    }
}
