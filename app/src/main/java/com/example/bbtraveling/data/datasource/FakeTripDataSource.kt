package com.example.bbtraveling.data.datasource

import com.example.bbtraveling.data.MockData
import com.example.bbtraveling.domain.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Guarda la lista de viajes en memoria para este sprint
class FakeTripDataSource(
    initialTrips: List<Trip> = MockData.initialTrips()
) {
    private val _trips = MutableStateFlow(initialTrips)
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    fun updateTrips(newTrips: List<Trip>) {
        _trips.value = newTrips
    }
}
