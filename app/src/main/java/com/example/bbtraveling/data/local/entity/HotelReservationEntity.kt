package com.example.bbtraveling.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "hotel_reservations",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["login"],
            childColumns = ["ownerLogin"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId"), Index("ownerLogin")]
)
data class HotelReservationEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val ownerLogin: String,
    val hotelId: String,
    val hotelName: String,
    val hotelAddress: String,
    val hotelRating: Int,
    val hotelImageUrl: String,
    val roomId: String,
    val roomType: String,
    val roomPrice: Double,
    val roomImageUrls: List<String>,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val guestName: String,
    val guestEmail: String,
    val createdAt: LocalDateTime
)

