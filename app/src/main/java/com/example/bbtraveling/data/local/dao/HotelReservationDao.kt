package com.example.bbtraveling.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.bbtraveling.data.local.entity.HotelReservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HotelReservationDao {

    @Query("SELECT * FROM hotel_reservations WHERE ownerLogin = :ownerLogin ORDER BY startDate ASC")
    fun observeReservationsForOwner(ownerLogin: String): Flow<List<HotelReservationEntity>>

    @Query("SELECT * FROM hotel_reservations WHERE id = :reservationId LIMIT 1")
    suspend fun getReservation(reservationId: String): HotelReservationEntity?

    @Upsert
    suspend fun upsertReservation(reservation: HotelReservationEntity)

    @Query("DELETE FROM hotel_reservations WHERE id = :reservationId")
    suspend fun deleteReservation(reservationId: String)
}

