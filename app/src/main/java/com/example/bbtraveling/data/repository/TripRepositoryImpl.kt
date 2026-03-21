package com.example.bbtraveling.data.repository

import com.example.bbtraveling.data.datasource.FakeTripDataSource
import com.example.bbtraveling.domain.Activity
import com.example.bbtraveling.domain.ActivityDraft
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.TripDraft
import com.example.bbtraveling.domain.repository.TripRepository
import com.example.bbtraveling.domain.validation.TravelValidator
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.StateFlow

// Aqui va la logica de viajes y actividades sobre memoria
class TripRepositoryImpl(
    private val dataSource: FakeTripDataSource,
    private val clock: Clock = Clock.systemDefaultZone()
) : TripRepository {

    override val trips: StateFlow<List<Trip>> = dataSource.trips

    override fun getTripById(tripId: String): Trip? = trips.value.firstOrNull { it.id == tripId }

    override fun addTrip(draft: TripDraft): OperationResult {
        val errors = TravelValidator.validateTripDraft(
            draft = draft,
            today = today(),
            existingActivities = emptyList()
        )
        if (errors.isNotEmpty()) return OperationResult.Failure(fieldErrors = errors)

        val newTrip = Trip(
            id = UUID.randomUUID().toString(),
            title = draft.title.trim(),
            startDate = checkNotNull(draft.startDate),
            endDate = checkNotNull(draft.endDate),
            description = draft.description.trim(),
            destination = buildDestination(draft.city, draft.country),
            status = draft.status,
            accommodation = "",
            transport = "",
            travelers = 1,
            budgetEur = checkNotNull(draft.budgetEur),
            activities = emptyList(),
            photos = emptyList()
        )

        dataSource.updateTrips(trips.value + newTrip)
        return OperationResult.Success
    }

    override fun editTrip(
        tripId: String,
        draft: TripDraft,
        moveActivitiesWithTrip: Boolean
    ): OperationResult {
        val currentTrip = getTripById(tripId)
            ?: return OperationResult.Failure(message = TravelValidator.ERROR_TRIP_NOT_FOUND)

        // Si el viaje se mueve tambien podemos mover actividades y asi no se bloquea la edicion
        val activitiesForValidation = if (moveActivitiesWithTrip && draft.startDate != null) {
            currentTrip.rescheduleActivities(draft.startDate)
        } else {
            currentTrip.activities
        }

        val errors = TravelValidator.validateTripDraft(
            draft = draft,
            today = today(),
            existingActivities = activitiesForValidation
        )
        if (errors.isNotEmpty()) return OperationResult.Failure(fieldErrors = errors)

        val updatedTrip = currentTrip.copy(
            title = draft.title.trim(),
            description = draft.description.trim(),
            destination = buildDestination(draft.city, draft.country),
            startDate = checkNotNull(draft.startDate),
            endDate = checkNotNull(draft.endDate),
            status = draft.status,
            budgetEur = checkNotNull(draft.budgetEur),
            activities = activitiesForValidation.sortedWith(compareBy<Activity>({ it.date }, { it.time }))
        )

        return replaceTrip(updatedTrip)
    }

    override fun deleteTrip(tripId: String): OperationResult {
        val currentTrips = trips.value
        if (currentTrips.none { it.id == tripId }) {
            return OperationResult.Failure(message = TravelValidator.ERROR_TRIP_NOT_FOUND)
        }
        dataSource.updateTrips(currentTrips.filterNot { it.id == tripId })
        return OperationResult.Success
    }

    override fun addActivity(tripId: String, draft: ActivityDraft): OperationResult {
        val trip = getTripById(tripId) ?: return OperationResult.Failure(message = TravelValidator.ERROR_TRIP_NOT_FOUND)
        val errors = TravelValidator.validateActivityDraft(draft = draft, trip = trip, today = today())
        if (errors.isNotEmpty()) return OperationResult.Failure(fieldErrors = errors)

        val updatedTrip = trip.copy(
            activities = (trip.activities + Activity(
                id = UUID.randomUUID().toString(),
                title = draft.title.trim(),
                description = draft.description.trim(),
                date = checkNotNull(draft.date),
                time = checkNotNull(draft.time),
                category = draft.category,
                costEur = checkNotNull(draft.costEur)
            )).sortedWith(compareBy<Activity>({ it.date }, { it.time }))
        )
        return replaceTrip(updatedTrip)
    }

    override fun updateActivity(tripId: String, activityId: String, draft: ActivityDraft): OperationResult {
        val trip = getTripById(tripId) ?: return OperationResult.Failure(message = TravelValidator.ERROR_TRIP_NOT_FOUND)
        if (trip.activities.none { it.id == activityId }) {
            return OperationResult.Failure(message = TravelValidator.ERROR_ACTIVITY_NOT_FOUND)
        }

        val errors = TravelValidator.validateActivityDraft(draft = draft, trip = trip, today = today())
        if (errors.isNotEmpty()) return OperationResult.Failure(fieldErrors = errors)

        val updatedTrip = trip.copy(
            activities = trip.activities
                .map { current ->
                    if (current.id == activityId) {
                        current.copy(
                            title = draft.title.trim(),
                            description = draft.description.trim(),
                            date = checkNotNull(draft.date),
                            time = checkNotNull(draft.time),
                            category = draft.category,
                            costEur = checkNotNull(draft.costEur)
                        )
                    } else {
                        current
                    }
                }
                .sortedWith(compareBy<Activity>({ it.date }, { it.time }))
        )
        return replaceTrip(updatedTrip)
    }

    override fun deleteActivity(tripId: String, activityId: String): OperationResult {
        val trip = getTripById(tripId) ?: return OperationResult.Failure(message = TravelValidator.ERROR_TRIP_NOT_FOUND)
        if (trip.activities.none { it.id == activityId }) {
            return OperationResult.Failure(message = TravelValidator.ERROR_ACTIVITY_NOT_FOUND)
        }

        val updatedTrip = trip.copy(
            activities = trip.activities.filterNot { it.id == activityId }
        )
        return replaceTrip(updatedTrip)
    }

    private fun replaceTrip(updatedTrip: Trip): OperationResult {
        val currentTrips = trips.value
        if (currentTrips.none { it.id == updatedTrip.id }) {
            return OperationResult.Failure(message = TravelValidator.ERROR_TRIP_NOT_FOUND)
        }

        dataSource.updateTrips(currentTrips.map { trip ->
            if (trip.id == updatedTrip.id) updatedTrip else trip
        })
        return OperationResult.Success
    }

    private fun today(): LocalDate = LocalDate.now(clock)

    private fun buildDestination(city: String, country: String): String {
        return "${city.trim()}, ${country.trim()}"
    }
}
