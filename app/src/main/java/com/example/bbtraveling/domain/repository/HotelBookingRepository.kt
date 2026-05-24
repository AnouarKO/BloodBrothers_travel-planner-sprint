package com.example.bbtraveling.domain.repository

import com.example.bbtraveling.domain.Hotel
import com.example.bbtraveling.domain.HotelReservation
import com.example.bbtraveling.domain.HotelRoom
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.TripImage
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface HotelBookingRepository {
    fun observeReservations(): Flow<List<HotelReservation>>
    fun observeTripImages(tripId: String): Flow<List<TripImage>>

    suspend fun searchHotels(city: String, startDate: LocalDate, endDate: LocalDate): Result<List<Hotel>>
    suspend fun bookRoom(hotel: Hotel, room: HotelRoom, startDate: LocalDate, endDate: LocalDate): OperationResult
    suspend fun cancelReservation(reservationId: String): OperationResult
    suspend fun addTripImage(tripId: String, uri: String, title: String): OperationResult
    suspend fun deleteTripImage(imageId: String): OperationResult
}

