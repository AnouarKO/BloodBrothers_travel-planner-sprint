package com.example.bbtraveling.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HotelDto(
    val id: String,
    val name: String,
    val address: String,
    val rating: Int,
    val rooms: List<RoomDto>,
    @SerializedName("image_url") val imageUrl: String
)

data class HotelSummaryDto(
    val id: String,
    val name: String,
    val address: String,
    val rating: Int,
    @SerializedName("image_url") val imageUrl: String
)

data class RoomDto(
    val id: String,
    @SerializedName("room_type") val roomType: String,
    val price: Double,
    val images: List<String>
)

data class AvailabilityResponseDto(
    @SerializedName("available_hotels") val availableHotels: List<HotelDto>
)

data class ReserveRequestDto(
    @SerializedName("hotel_id") val hotelId: String,
    @SerializedName("room_id") val roomId: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("guest_name") val guestName: String,
    @SerializedName("guest_email") val guestEmail: String
)

data class ReservationsResponseDto(
    val reservations: List<ReservationDetailsDto>
)

data class ReservationDto(
    val id: String,
    @SerializedName("hotel_id") val hotelId: String,
    @SerializedName("room_id") val roomId: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("guest_name") val guestName: String,
    @SerializedName("guest_email") val guestEmail: String
)

data class ReservationDetailsDto(
    val id: String,
    @SerializedName("hotel_id") val hotelId: String,
    @SerializedName("room_id") val roomId: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("guest_name") val guestName: String,
    @SerializedName("guest_email") val guestEmail: String,
    val hotel: HotelSummaryDto,
    val room: RoomDto
)

data class HotelApiMessageDto(
    val message: String? = null,
    val nights: Int? = null,
    val reservation: ReservationDto? = null
)
