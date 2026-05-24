package com.example.bbtraveling.domain

import java.time.LocalDate

data class Hotel(
    val id: String,
    val name: String,
    val address: String,
    val rating: Int,
    val imageUrl: String,
    val rooms: List<HotelRoom>
)

data class HotelRoom(
    val id: String,
    val roomType: String,
    val price: Double,
    val images: List<String>
)

data class HotelReservation(
    val id: String,
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
    val guestEmail: String
)

data class TripImage(
    val id: String,
    val tripId: String,
    val ownerLogin: String,
    val uri: String,
    val title: String
)

