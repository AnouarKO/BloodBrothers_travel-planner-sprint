package com.example.bbtraveling.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bbtraveling.data.MockData
import com.example.bbtraveling.domain.ActivityDraft
import com.example.bbtraveling.domain.AuthRegistration
import com.example.bbtraveling.domain.AuthResult
import com.example.bbtraveling.domain.AuthUser
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.TripDraft
import com.example.bbtraveling.domain.UserProfile
import com.example.bbtraveling.domain.UserSettings
import com.example.bbtraveling.domain.repository.AuthRepository
import com.example.bbtraveling.domain.repository.TripRepository
import com.example.bbtraveling.domain.repository.UserProfileRepository
import com.example.bbtraveling.domain.repository.UserSettingsRepository
import com.example.bbtraveling.ui.theme.BBTravelingTheme
import com.example.bbtraveling.ui.viewmodel.SettingsViewModel
import com.example.bbtraveling.ui.viewmodel.TripsViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

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
        repository = PreviewTripRepository(initialTrips = previewTrips())
    )
}

fun previewSettingsViewModel(): SettingsViewModel {
    return SettingsViewModel(
        repository = PreviewUserSettingsRepository(),
        authRepository = PreviewAuthRepository(),
        userProfileRepository = PreviewUserProfileRepository()
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

private class PreviewTripRepository(initialTrips: List<Trip>) : TripRepository {
    private val mutableTrips = MutableStateFlow(initialTrips)

    override val trips: StateFlow<List<Trip>> = mutableTrips.asStateFlow()

    override fun getTripById(tripId: String): Trip? = trips.value.firstOrNull { it.id == tripId }

    override fun addTrip(draft: TripDraft): OperationResult = OperationResult.Success

    override fun editTrip(
        tripId: String,
        draft: TripDraft,
        moveActivitiesWithTrip: Boolean
    ): OperationResult = OperationResult.Success

    override fun deleteTrip(tripId: String): OperationResult = OperationResult.Success

    override fun addActivity(tripId: String, draft: ActivityDraft): OperationResult = OperationResult.Success

    override fun updateActivity(
        tripId: String,
        activityId: String,
        draft: ActivityDraft
    ): OperationResult = OperationResult.Success

    override fun deleteActivity(tripId: String, activityId: String): OperationResult = OperationResult.Success
}

private class PreviewAuthRepository : AuthRepository {
    override val currentUser: StateFlow<AuthUser?> = MutableStateFlow(null).asStateFlow()

    override suspend fun login(email: String, password: String): AuthResult = AuthResult.Success()

    override suspend fun register(registration: AuthRegistration): AuthResult = AuthResult.Success()

    override suspend fun recoverPassword(email: String): AuthResult = AuthResult.Success()

    override suspend fun logout() = Unit
}

private class PreviewUserProfileRepository : UserProfileRepository {
    private val profile = UserProfile(
        login = "preview@example.com",
        username = "Anouar",
        birthdate = LocalDate.of(2004, 3, 12),
        address = "Test street 1",
        country = "Spain",
        phone = "600000000",
        acceptsReceiveEmails = true
    )

    override suspend fun getUser(login: String): UserProfile? = profile.takeIf { it.login == login }

    override fun observeUser(login: String): Flow<UserProfile?> = flowOf(profile.takeIf { it.login == login })

    override suspend fun upsertUser(profile: UserProfile) = Unit

    override suspend fun deleteUser(login: String) = Unit
}
