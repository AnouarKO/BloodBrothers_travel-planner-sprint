package com.example.bbtraveling.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bbtraveling.data.MockData
import com.example.bbtraveling.data.datasource.FakeTripDataSource
import com.example.bbtraveling.data.repository.TripRepositoryImpl
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.UserSettings
import com.example.bbtraveling.domain.repository.UserSettingsRepository
import com.example.bbtraveling.ui.theme.BBTravelingTheme
import com.example.bbtraveling.ui.viewmodel.SettingsViewModel
import com.example.bbtraveling.ui.viewmodel.TripsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun PreviewScreenContainer(content: @Composable () -> Unit) {
    BBTravelingTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

fun previewTrips(): List<Trip> = MockData.initialTrips()

fun previewTripsViewModel(): TripsViewModel {
    return TripsViewModel(
        repository = TripRepositoryImpl(
            dataSource = FakeTripDataSource(initialTrips = previewTrips())
        )
    )
}

fun previewSettingsViewModel(): SettingsViewModel {
    return SettingsViewModel(
        repository = PreviewUserSettingsRepository()
    )
}

private class PreviewUserSettingsRepository(
    initialSettings: UserSettings = UserSettings(
        username = "Anouar",
        dateOfBirth = "12/03/2004",
        darkMode = false,
        languageTag = "en",
        termsAccepted = true
    )
) : UserSettingsRepository {

    private val mutableSettings = MutableStateFlow(initialSettings)

    override val settings: StateFlow<UserSettings> = mutableSettings.asStateFlow()

    override fun readStoredLanguageTag(): String = mutableSettings.value.languageTag

    override fun hasAcceptedTerms(): Boolean = mutableSettings.value.termsAccepted

    override fun updateUsername(value: String) {
        mutableSettings.value = mutableSettings.value.copy(username = value)
    }

    override fun updateDateOfBirth(value: String) {
        mutableSettings.value = mutableSettings.value.copy(dateOfBirth = value)
    }

    override fun updateDarkMode(enabled: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(darkMode = enabled)
    }

    override fun updateLanguage(languageTag: String) {
        mutableSettings.value = mutableSettings.value.copy(languageTag = languageTag)
    }

    override fun setTermsAccepted(accepted: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(termsAccepted = accepted)
    }
}
