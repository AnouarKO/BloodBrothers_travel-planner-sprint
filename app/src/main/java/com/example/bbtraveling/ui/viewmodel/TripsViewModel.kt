package com.example.bbtraveling.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bbtraveling.domain.ActivityDraft
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.TripDraft
import com.example.bbtraveling.domain.repository.TripRepository
import com.example.bbtraveling.domain.validation.TravelValidator
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow

// Coordina acciones de viajes antes de llegar al repo
class TripsViewModel(
    private val repository: TripRepository
) : ViewModel() {

    val trips: StateFlow<List<Trip>> = repository.trips

    fun getTripById(tripId: String): Trip? = repository.getTripById(tripId)

    fun addTrip(draft: TripDraft): OperationResult {
        val uiValidation = TravelValidator.validateTripDraft(
            draft = draft,
            today = LocalDate.now(),
            existingActivities = emptyList()
        )
        if (uiValidation.isNotEmpty()) {
            Log.w(TAG, "addTrip rejected by UI validation: $uiValidation")
            return OperationResult.Failure(fieldErrors = uiValidation)
        }
        val result = repository.addTrip(draft)
        logResult("addTrip", result)
        return result
    }

    fun editTrip(tripId: String, draft: TripDraft, moveActivitiesWithTrip: Boolean = false): OperationResult {
        val trip = repository.getTripById(tripId)
            ?: return OperationResult.Failure(message = TravelValidator.ERROR_TRIP_NOT_FOUND)
        val activitiesForValidation = if (moveActivitiesWithTrip && draft.startDate != null) {
            trip.rescheduleActivities(draft.startDate)
        } else {
            trip.activities
        }
        val uiValidation = TravelValidator.validateTripDraft(
            draft = draft,
            today = LocalDate.now(),
            existingActivities = activitiesForValidation
        )
        if (uiValidation.isNotEmpty()) {
            Log.w(TAG, "editTrip rejected by UI validation: $uiValidation")
            return OperationResult.Failure(fieldErrors = uiValidation)
        }
        val result = repository.editTrip(
            tripId = tripId,
            draft = draft,
            moveActivitiesWithTrip = moveActivitiesWithTrip
        )
        logResult("editTrip", result)
        return result
    }

    fun deleteTrip(tripId: String): OperationResult {
        val result = repository.deleteTrip(tripId = tripId)
        logResult("deleteTrip", result)
        return result
    }

    fun addActivity(tripId: String, draft: ActivityDraft): OperationResult {
        val trip = repository.getTripById(tripId)
            ?: return OperationResult.Failure(message = TravelValidator.ERROR_TRIP_NOT_FOUND)
        val uiValidation = TravelValidator.validateActivityDraft(
            draft = draft,
            trip = trip,
            today = LocalDate.now()
        )
        if (uiValidation.isNotEmpty()) {
            Log.w(TAG, "addActivity rejected by UI validation: $uiValidation")
            return OperationResult.Failure(fieldErrors = uiValidation)
        }
        val result = repository.addActivity(tripId = tripId, draft = draft)
        logResult("addActivity", result)
        return result
    }

    fun updateActivity(tripId: String, activityId: String, draft: ActivityDraft): OperationResult {
        val trip = repository.getTripById(tripId)
            ?: return OperationResult.Failure(message = TravelValidator.ERROR_TRIP_NOT_FOUND)
        val uiValidation = TravelValidator.validateActivityDraft(
            draft = draft,
            trip = trip,
            today = LocalDate.now()
        )
        if (uiValidation.isNotEmpty()) {
            Log.w(TAG, "updateActivity rejected by UI validation: $uiValidation")
            return OperationResult.Failure(fieldErrors = uiValidation)
        }
        val result = repository.updateActivity(tripId = tripId, activityId = activityId, draft = draft)
        logResult("updateActivity", result)
        return result
    }

    fun deleteActivity(tripId: String, activityId: String): OperationResult {
        val result = repository.deleteActivity(tripId = tripId, activityId = activityId)
        logResult("deleteActivity", result)
        return result
    }

    private fun logResult(operation: String, result: OperationResult) {
        when (result) {
            is OperationResult.Success -> Log.i(TAG, "$operation success")
            is OperationResult.Failure -> {
                val failureDetail = result.message ?: result.fieldErrors.toString()
                if (result.fieldErrors.isNotEmpty() || result.message != null) {
                    Log.w(TAG, "$operation rejected: $failureDetail")
                } else {
                    Log.e(TAG, "$operation failed without detail")
                }
            }
        }
    }

    private companion object {
        const val TAG = "TripsViewModel"
    }
}

class TripsViewModelFactory(
    private val repository: TripRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripsViewModel::class.java)) {
            return TripsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
