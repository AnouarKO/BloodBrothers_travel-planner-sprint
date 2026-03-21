package com.example.bbtraveling.data.repository

import com.example.bbtraveling.data.datasource.FakeTripDataSource
import com.example.bbtraveling.domain.Activity
import com.example.bbtraveling.domain.ActivityCategory
import com.example.bbtraveling.domain.ActivityDraft
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.TripDraft
import com.example.bbtraveling.domain.TripStatus
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TripRepositoryImplTest {

    private lateinit var repository: TripRepositoryImpl

    @Before
    fun setup() {
        val initialTrips = listOf(
            Trip(
                id = "trip-1",
                title = "Seed Trip",
                startDate = LocalDate.of(2026, 3, 10),
                endDate = LocalDate.of(2026, 3, 12),
                description = "Seed description",
                destination = "Rome",
                status = TripStatus.Planning,
                accommodation = "Hotel",
                transport = "Flight",
                travelers = 2,
                budgetEur = 400.0,
                activities = listOf(
                    Activity(
                        id = "activity-1",
                        title = "Seed Activity",
                        description = "Seed itinerary item",
                        date = LocalDate.of(2026, 3, 11),
                        time = LocalTime.of(10, 0),
                        category = ActivityCategory.Museum,
                        costEur = 30.0
                    )
                ),
                photos = emptyList()
            )
        )

        repository = TripRepositoryImpl(
            dataSource = FakeTripDataSource(initialTrips = initialTrips),
            clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        )
    }

    @Test
    fun addTrip_addsItemToInMemoryList() {
        val result = repository.addTrip(
            TripDraft(
                title = "New Trip",
                description = "New description",
                city = "Madrid",
                country = "Spain",
                startDate = LocalDate.of(2026, 4, 10),
                endDate = LocalDate.of(2026, 4, 12),
                status = TripStatus.Upcoming,
                budgetEur = 500.0
            )
        )

        assertTrue(result is OperationResult.Success)
        assertEquals(2, repository.trips.value.size)
        assertEquals("Madrid, Spain", repository.trips.value.last().destination)
    }

    @Test
    fun addTrip_withInvalidDates_returnsFieldErrors() {
        val result = repository.addTrip(
            TripDraft(
                title = "Invalid trip",
                description = "description",
                city = "Paris",
                country = "France",
                startDate = LocalDate.of(2026, 4, 12),
                endDate = LocalDate.of(2026, 4, 10),
                status = TripStatus.Planning,
                budgetEur = 200.0
            )
        )

        assertTrue(result is OperationResult.Failure)
        result as OperationResult.Failure
        assertTrue(result.fieldErrors.containsKey("startDate"))
        assertTrue(result.fieldErrors.containsKey("endDate"))
    }

    @Test
    fun editTrip_updatesExistingTrip() {
        val result = repository.editTrip(
            tripId = "trip-1",
            draft = TripDraft(
                title = "Edited trip",
                description = "Edited description",
                city = "Milan",
                country = "Italy",
                startDate = LocalDate.of(2026, 3, 10),
                endDate = LocalDate.of(2026, 3, 16),
                status = TripStatus.Completed,
                budgetEur = 450.0
            ),
            moveActivitiesWithTrip = false
        )

        assertTrue(result is OperationResult.Success)
        val updated = repository.getTripById("trip-1")
        assertEquals("Edited trip", updated?.title)
        assertEquals(LocalDate.of(2026, 3, 10), updated?.startDate)
        assertEquals(TripStatus.Completed, updated?.status)
        assertEquals(450.0, updated?.budgetEur ?: 0.0, 0.001)
        assertEquals("Milan, Italy", updated?.destination)
    }

    @Test
    fun editTrip_withActivitiesOutsideNewRange_returnsValidationError() {
        val result = repository.editTrip(
            tripId = "trip-1",
            draft = TripDraft(
                title = "Edited trip",
                description = "Edited description",
                city = "Milan",
                country = "Italy",
                startDate = LocalDate.of(2026, 3, 12),
                endDate = LocalDate.of(2026, 3, 14),
                status = TripStatus.Planning,
                budgetEur = 450.0
            ),
            moveActivitiesWithTrip = false
        )

        assertTrue(result is OperationResult.Failure)
        result as OperationResult.Failure
        assertTrue(result.fieldErrors.containsKey("date"))
    }

    @Test
    fun editTrip_withMoveActivitiesWithTrip_reschedulesExistingActivities() {
        val result = repository.editTrip(
            tripId = "trip-1",
            draft = TripDraft(
                title = "Rescheduled trip",
                description = "Edited description",
                city = "Rome",
                country = "Italy",
                startDate = LocalDate.of(2026, 3, 20),
                endDate = LocalDate.of(2026, 3, 22),
                status = TripStatus.Upcoming,
                budgetEur = 450.0
            ),
            moveActivitiesWithTrip = true
        )

        assertTrue(result is OperationResult.Success)
        val updatedTrip = repository.getTripById("trip-1")
        assertEquals(LocalDate.of(2026, 3, 20), updatedTrip?.startDate)
        assertEquals(LocalDate.of(2026, 3, 21), updatedTrip?.activities?.first()?.date)
    }

    @Test
    fun editPastTrip_withMoveActivitiesWithTrip_breaksValidationLoop() {
        val repository = TripRepositoryImpl(
            dataSource = FakeTripDataSource(
                initialTrips = listOf(
                    Trip(
                        id = "past-trip",
                        title = "Rome",
                        startDate = LocalDate.of(2025, 12, 10),
                        endDate = LocalDate.of(2025, 12, 12),
                        description = "Past trip",
                        destination = "Rome, Italy",
                        status = TripStatus.Completed,
                        accommodation = "Hotel",
                        transport = "Flight",
                        travelers = 2,
                        budgetEur = 400.0,
                        activities = listOf(
                            Activity(
                                id = "past-activity",
                                title = "Colosseum",
                                description = "Morning visit",
                                date = LocalDate.of(2025, 12, 11),
                                time = LocalTime.of(10, 0),
                                category = ActivityCategory.Museum,
                                costEur = 30.0
                            )
                        ),
                        photos = emptyList()
                    )
                )
            ),
            clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        )

        val result = repository.editTrip(
            tripId = "past-trip",
            draft = TripDraft(
                title = "Rome Again",
                description = "Rescheduled trip",
                city = "Rome",
                country = "Italy",
                startDate = LocalDate.of(2026, 4, 10),
                endDate = LocalDate.of(2026, 4, 12),
                status = TripStatus.Upcoming,
                budgetEur = 500.0
            ),
            moveActivitiesWithTrip = true
        )

        assertTrue(result is OperationResult.Success)
        val updatedTrip = repository.getTripById("past-trip")
        assertEquals(LocalDate.of(2026, 4, 10), updatedTrip?.startDate)
        assertEquals(LocalDate.of(2026, 4, 11), updatedTrip?.activities?.first()?.date)
    }

    @Test
    fun deleteTrip_removesTripFromDataSource() {
        val result = repository.deleteTrip("trip-1")

        assertTrue(result is OperationResult.Success)
        assertEquals(0, repository.trips.value.size)
    }

    @Test
    fun addActivity_addsActivityToTrip() {
        val result = repository.addActivity(
            tripId = "trip-1",
            draft = ActivityDraft(
                title = "Visit museum",
                description = "Morning museum",
                date = LocalDate.of(2026, 3, 12),
                time = LocalTime.of(9, 30),
                category = ActivityCategory.Museum,
                costEur = 22.0
            )
        )

        assertTrue(result is OperationResult.Success)
        val updatedTrip = repository.getTripById("trip-1")
        assertEquals(2, updatedTrip?.activities?.size)
        assertEquals(52.0, updatedTrip?.spentEur ?: 0.0, 0.001)
    }

    @Test
    fun addActivity_outsideTripRange_returnsValidationError() {
        val result = repository.addActivity(
            tripId = "trip-1",
            draft = ActivityDraft(
                title = "Invalid activity",
                description = "Outside range",
                date = LocalDate.of(2026, 3, 20),
                time = LocalTime.of(9, 30),
                category = ActivityCategory.Other,
                costEur = 10.0
            )
        )

        assertTrue(result is OperationResult.Failure)
        result as OperationResult.Failure
        assertTrue(result.fieldErrors.containsKey("date"))
    }

    @Test
    fun updateActivity_updatesExistingActivity() {
        val result = repository.updateActivity(
            tripId = "trip-1",
            activityId = "activity-1",
            draft = ActivityDraft(
                title = "Updated Activity",
                description = "Updated itinerary item",
                date = LocalDate.of(2026, 3, 11),
                time = LocalTime.of(16, 0),
                category = ActivityCategory.Transport,
                costEur = 18.0
            )
        )

        assertTrue(result is OperationResult.Success)
        val activity = repository.getTripById("trip-1")?.activities?.first()
        assertEquals("Updated Activity", activity?.title)
        assertEquals(LocalTime.of(16, 0), activity?.time)
        assertEquals(ActivityCategory.Transport, activity?.category)
        assertEquals(18.0, activity?.costEur ?: 0.0, 0.001)
    }

    @Test
    fun deleteActivity_removesActivity() {
        val result = repository.deleteActivity(tripId = "trip-1", activityId = "activity-1")

        assertTrue(result is OperationResult.Success)
        val activities = repository.getTripById("trip-1")?.activities.orEmpty()
        assertEquals(0, activities.size)
    }
}
