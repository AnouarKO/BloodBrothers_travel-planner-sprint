package com.example.bbtraveling.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.bbtraveling.data.local.TravelDatabase
import com.example.bbtraveling.data.local.entity.ItineraryItemEntity
import com.example.bbtraveling.data.local.entity.TripEntity
import com.example.bbtraveling.data.local.entity.UserProfileEntity
import com.example.bbtraveling.domain.ActivityCategory
import com.example.bbtraveling.domain.ActivityDraft
import com.example.bbtraveling.domain.AuthRegistration
import com.example.bbtraveling.domain.AuthResult
import com.example.bbtraveling.domain.AuthUser
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.TripDraft
import com.example.bbtraveling.domain.TripStatus
import com.example.bbtraveling.domain.repository.AuthRepository
import com.example.bbtraveling.domain.validation.TravelValidator
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomTripRepositoryTest {

    private lateinit var database: TravelDatabase
    private lateinit var authRepository: FakeAuthRepository
    private val clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TravelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        authRepository = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun tripsFlow_filtersTripsByCurrentUser() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        database.userProfileDao().upsertUser(user(login = "other@example.com", username = "other"))
        database.tripDao().upsertTrip(trip(id = "owner-trip", ownerLogin = "owner@example.com", title = "Rome"))
        database.tripDao().upsertTrip(trip(id = "other-trip", ownerLogin = "other@example.com", title = "Paris"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")

        val repository = createRepository()

        waitUntil { repository.trips.value.size == 1 }
        assertEquals("owner-trip", repository.trips.value.first().id)
    }

    @Test
    fun addTrip_persistsTripForAuthenticatedUser() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()

        val result = repository.addTrip(validTripDraft(title = "Lisbon"))

        assertTrue(result is OperationResult.Success)
        waitUntil { repository.trips.value.any { it.title == "Lisbon" } }
        val storedTrip = database.tripDao()
            .observeTripsWithActivitiesForOwner("owner@example.com")
            .first()
            .first { it.trip.title == "Lisbon" }
        assertEquals("owner@example.com", storedTrip.trip.ownerLogin)
    }

    @Test
    fun addTrip_withoutAuthenticatedUserFails() {
        val repository = createRepository()

        val result = repository.addTrip(validTripDraft(title = "Lisbon"))

        assertTrue(result is OperationResult.Failure)
        result as OperationResult.Failure
        assertEquals("User must be logged in.", result.message)
    }

    @Test
    fun addTrip_withDuplicatedTitleForSameUserFails() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()
        repository.addTrip(validTripDraft(title = "Lisbon"))
        waitUntil { repository.trips.value.any { it.title == "Lisbon" } }

        val duplicateResult = repository.addTrip(validTripDraft(title = " lisbon "))

        assertTrue(duplicateResult is OperationResult.Failure)
        duplicateResult as OperationResult.Failure
        assertEquals(
            TravelValidator.ERROR_TRIP_TITLE_DUPLICATED,
            duplicateResult.fieldErrors[TravelValidator.FIELD_TITLE]
        )
    }

    @Test
    fun addActivity_persistsItineraryItemForTrip() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()
        repository.addTrip(validTripDraft(title = "Lisbon"))
        waitUntil { repository.trips.value.any { it.title == "Lisbon" } }
        val tripId = repository.trips.value.first { it.title == "Lisbon" }.id

        val result = repository.addActivity(
            tripId = tripId,
            draft = ActivityDraft(
                title = "Museum",
                description = "Morning visit",
                date = LocalDate.of(2026, 6, 2),
                time = LocalTime.of(10, 0),
                category = ActivityCategory.Museum,
                costEur = 20.0
            )
        )

        assertTrue(result is OperationResult.Success)
        waitUntil {
            repository.trips.value.firstOrNull { it.id == tripId }?.activities?.size == 1
        }
        assertEquals("Museum", repository.trips.value.first { it.id == tripId }.activities.first().title)
    }

    @Test
    fun addActivity_outsideTripRangeFails() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        database.tripDao().upsertTrip(trip(id = "owner-trip", ownerLogin = "owner@example.com", title = "Lisbon"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()
        waitUntil { repository.trips.value.any { it.id == "owner-trip" } }

        val result = repository.addActivity(
            tripId = "owner-trip",
            draft = ActivityDraft(
                title = "Invalid activity",
                description = "Outside range",
                date = LocalDate.of(2026, 6, 10),
                time = LocalTime.of(9, 30),
                category = ActivityCategory.Other,
                costEur = 10.0
            )
        )

        assertTrue(result is OperationResult.Failure)
        result as OperationResult.Failure
        assertTrue(result.fieldErrors.containsKey(TravelValidator.FIELD_DATE))
    }

    @Test
    fun editTrip_updatesExistingTrip() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        database.tripDao().upsertTrip(trip(id = "owner-trip", ownerLogin = "owner@example.com", title = "Lisbon"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()
        waitUntil { repository.trips.value.any { it.id == "owner-trip" } }

        val result = repository.editTrip(
            tripId = "owner-trip",
            draft = validTripDraft(title = "Edited Lisbon").copy(
                city = "Porto",
                country = "Portugal",
                status = TripStatus.Completed,
                budgetEur = 450.0
            )
        )

        assertTrue(result is OperationResult.Success)
        waitUntil { repository.trips.value.any { it.title == "Edited Lisbon" } }
        val updatedTrip = repository.trips.value.first { it.id == "owner-trip" }
        assertEquals("Porto, Portugal", updatedTrip.destination)
        assertEquals(TripStatus.Completed, updatedTrip.status)
        assertEquals(450.0, updatedTrip.budgetEur, 0.001)
    }

    @Test
    fun editTrip_withActivitiesOutsideNewRangeFails() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        database.tripDao().upsertTrip(trip(id = "owner-trip", ownerLogin = "owner@example.com", title = "Lisbon"))
        database.itineraryItemDao().upsertItem(activity(id = "activity-1", tripId = "owner-trip"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()
        waitUntil { repository.trips.value.firstOrNull { it.id == "owner-trip" }?.activities?.size == 1 }

        val result = repository.editTrip(
            tripId = "owner-trip",
            draft = validTripDraft(title = "Narrow Lisbon").copy(
                startDate = LocalDate.of(2026, 6, 3),
                endDate = LocalDate.of(2026, 6, 4)
            ),
            moveActivitiesWithTrip = false
        )

        assertTrue(result is OperationResult.Failure)
        result as OperationResult.Failure
        assertTrue(result.fieldErrors.containsKey(TravelValidator.FIELD_DATE))
    }

    @Test
    fun editTrip_withMoveActivitiesWithTripReschedulesActivities() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        database.tripDao().upsertTrip(trip(id = "owner-trip", ownerLogin = "owner@example.com", title = "Lisbon"))
        database.itineraryItemDao().upsertItem(activity(id = "activity-1", tripId = "owner-trip"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()
        waitUntil { repository.trips.value.firstOrNull { it.id == "owner-trip" }?.activities?.size == 1 }

        val result = repository.editTrip(
            tripId = "owner-trip",
            draft = validTripDraft(title = "Rescheduled Lisbon").copy(
                startDate = LocalDate.of(2026, 7, 10),
                endDate = LocalDate.of(2026, 7, 12)
            ),
            moveActivitiesWithTrip = true
        )

        assertTrue(result is OperationResult.Success)
        waitUntil {
            repository.trips.value
                .firstOrNull { it.id == "owner-trip" }
                ?.activities
                ?.firstOrNull()
                ?.date == LocalDate.of(2026, 7, 11)
        }
        val updatedTrip = repository.trips.value.first { it.id == "owner-trip" }
        assertEquals(LocalDate.of(2026, 7, 10), updatedTrip.startDate)
        assertEquals(LocalDate.of(2026, 7, 11), updatedTrip.activities.first().date)
    }

    @Test
    fun deleteTrip_removesTripFromDatabase() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        database.tripDao().upsertTrip(trip(id = "owner-trip", ownerLogin = "owner@example.com", title = "Lisbon"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()
        waitUntil { repository.trips.value.any { it.id == "owner-trip" } }

        val result = repository.deleteTrip("owner-trip")

        assertTrue(result is OperationResult.Success)
        waitUntil { repository.trips.value.none { it.id == "owner-trip" } }
    }

    @Test
    fun updateActivity_updatesExistingActivity() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        database.tripDao().upsertTrip(trip(id = "owner-trip", ownerLogin = "owner@example.com", title = "Lisbon"))
        database.itineraryItemDao().upsertItem(activity(id = "activity-1", tripId = "owner-trip"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()
        waitUntil { repository.trips.value.firstOrNull { it.id == "owner-trip" }?.activities?.size == 1 }

        val result = repository.updateActivity(
            tripId = "owner-trip",
            activityId = "activity-1",
            draft = ActivityDraft(
                title = "Updated Activity",
                description = "Updated itinerary item",
                date = LocalDate.of(2026, 6, 2),
                time = LocalTime.of(16, 0),
                category = ActivityCategory.Transport,
                costEur = 18.0
            )
        )

        assertTrue(result is OperationResult.Success)
        waitUntil {
            repository.trips.value
                .firstOrNull { it.id == "owner-trip" }
                ?.activities
                ?.firstOrNull()
                ?.title == "Updated Activity"
        }
        val activity = repository.trips.value.first { it.id == "owner-trip" }.activities.first()
        assertEquals(LocalTime.of(16, 0), activity.time)
        assertEquals(ActivityCategory.Transport, activity.category)
        assertEquals(18.0, activity.costEur, 0.001)
    }

    @Test
    fun deleteActivity_removesActivity() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        database.tripDao().upsertTrip(trip(id = "owner-trip", ownerLogin = "owner@example.com", title = "Lisbon"))
        database.itineraryItemDao().upsertItem(activity(id = "activity-1", tripId = "owner-trip"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()
        waitUntil { repository.trips.value.firstOrNull { it.id == "owner-trip" }?.activities?.size == 1 }

        val result = repository.deleteActivity(tripId = "owner-trip", activityId = "activity-1")

        assertTrue(result is OperationResult.Success)
        waitUntil { repository.trips.value.firstOrNull { it.id == "owner-trip" }?.activities?.isEmpty() == true }
    }

    private fun createRepository(): RoomTripRepository {
        return RoomTripRepository(
            tripDao = database.tripDao(),
            itineraryItemDao = database.itineraryItemDao(),
            authRepository = authRepository,
            clock = clock
        )
    }

    private suspend fun waitUntil(predicate: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 2_000
        while (!predicate()) {
            if (System.currentTimeMillis() > deadline) {
                error("Condition was not reached before timeout")
            }
            delay(25)
        }
    }

    private fun validTripDraft(title: String): TripDraft {
        return TripDraft(
            title = title,
            description = "Trip description",
            city = "Lisbon",
            country = "Portugal",
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 3),
            status = TripStatus.Planning,
            budgetEur = 300.0
        )
    }

    private fun user(login: String, username: String): UserProfileEntity {
        return UserProfileEntity(
            login = login,
            username = username,
            birthdate = LocalDate.of(2000, 1, 1),
            address = "Test street 1",
            country = "Spain",
            phone = "600000000",
            acceptsReceiveEmails = true
        )
    }

    private fun trip(id: String, ownerLogin: String, title: String): TripEntity {
        return TripEntity(
            id = id,
            ownerLogin = ownerLogin,
            title = title,
            description = "Trip description",
            city = "City",
            country = "Country",
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 3),
            status = TripStatus.Planning,
            accommodation = "Hotel",
            transport = "Train",
            travelers = 1,
            budgetEur = 300.0,
            createdAt = LocalDateTime.of(2026, 5, 1, 9, 0)
        )
    }

    private fun activity(id: String, tripId: String): ItineraryItemEntity {
        return ItineraryItemEntity(
            id = id,
            tripId = tripId,
            title = "Seed Activity",
            description = "Seed itinerary item",
            date = LocalDate.of(2026, 6, 2),
            time = LocalTime.of(10, 0),
            category = ActivityCategory.Museum,
            costEur = 30.0,
            createdAt = LocalDateTime.of(2026, 5, 1, 10, 0)
        )
    }

    private fun authUser(email: String): AuthUser {
        return AuthUser(
            userId = "uid-$email",
            login = email,
            email = email
        )
    }

    private class FakeAuthRepository : AuthRepository {
        val currentUserFlow = MutableStateFlow<AuthUser?>(null)
        override val currentUser: StateFlow<AuthUser?> = currentUserFlow

        override suspend fun login(email: String, password: String): AuthResult = AuthResult.Success()

        override suspend fun register(registration: AuthRegistration): AuthResult = AuthResult.Success()

        override suspend fun recoverPassword(email: String): AuthResult = AuthResult.Success()

        override suspend fun logout() {
            currentUserFlow.value = null
        }
    }
}

