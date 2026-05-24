package com.example.bbtraveling.data.remote.api

import com.example.bbtraveling.data.remote.dto.AvailabilityResponseDto
import com.example.bbtraveling.data.remote.dto.HotelApiMessageDto
import com.example.bbtraveling.data.remote.dto.HotelDto
import com.example.bbtraveling.data.remote.dto.ReservationDetailsDto
import com.example.bbtraveling.data.remote.dto.ReservationsResponseDto
import com.example.bbtraveling.data.remote.dto.ReserveRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HotelApiService {

    @GET("hotels/{groupId}/hotels")
    suspend fun getHotels(
        @Path("groupId") groupId: String
    ): List<HotelDto>

    @GET("hotels/{groupId}/availability")
    suspend fun checkAvailability(
        @Path("groupId") groupId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("city") city: String? = null,
        @Query("hotel_id") hotelId: String? = null
    ): AvailabilityResponseDto

    @POST("hotels/{groupId}/reserve")
    suspend fun reserveRoom(
        @Path("groupId") groupId: String,
        @Body request: ReserveRequestDto
    ): HotelApiMessageDto

    @POST("hotels/{groupId}/cancel")
    suspend fun cancelReservation(
        @Path("groupId") groupId: String,
        @Body request: ReserveRequestDto
    ): HotelApiMessageDto

    @GET("hotels/{groupId}/reservations")
    suspend fun getReservations(
        @Path("groupId") groupId: String,
        @Query("guest_email") guestEmail: String? = null
    ): ReservationsResponseDto

    @GET("reservations/{reservationId}")
    suspend fun getReservationById(
        @Path("reservationId") reservationId: String
    ): ReservationDetailsDto

    @DELETE("reservations/{reservationId}")
    suspend fun cancelReservationById(
        @Path("reservationId") reservationId: String
    ): ReservationDetailsDto
}
