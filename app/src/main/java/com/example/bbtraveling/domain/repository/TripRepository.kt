package com.example.bbtraveling.domain.repository

import com.example.bbtraveling.domain.ActivityDraft
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.TripDraft
import kotlinx.coroutines.flow.StateFlow

interface TripRepository {
    val trips: StateFlow<List<Trip>>

    fun getTripById(tripId: String): Trip?
    fun addTrip(draft: TripDraft): OperationResult
    fun editTrip(tripId: String, draft: TripDraft, moveActivitiesWithTrip: Boolean = false): OperationResult
    fun deleteTrip(tripId: String): OperationResult

    fun addActivity(tripId: String, draft: ActivityDraft): OperationResult
    fun updateActivity(tripId: String, activityId: String, draft: ActivityDraft): OperationResult
    fun deleteActivity(tripId: String, activityId: String): OperationResult
}
