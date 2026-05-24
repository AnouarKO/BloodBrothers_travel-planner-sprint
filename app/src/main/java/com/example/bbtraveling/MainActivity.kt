package com.example.bbtraveling

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.bbtraveling.navigation.AppNavGraph
import com.example.bbtraveling.ui.theme.BBTravelingTheme
import com.example.bbtraveling.ui.viewmodel.AuthViewModel
import com.example.bbtraveling.ui.viewmodel.HotelBookingViewModel
import com.example.bbtraveling.ui.viewmodel.SettingsViewModel
import com.example.bbtraveling.ui.viewmodel.TripsViewModel
import com.example.bbtraveling.domain.repository.UserSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyLanguage(userSettingsRepository.readStoredLanguageTag())

        setContent {
            App()
        }
    }
}

@Composable
private fun App(
    authViewModel: AuthViewModel = hiltViewModel(),
    tripsViewModel: TripsViewModel = hiltViewModel(),
    hotelBookingViewModel: HotelBookingViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
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
                authViewModel = authViewModel,
                tripsViewModel = tripsViewModel,
                hotelBookingViewModel = hotelBookingViewModel,
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
