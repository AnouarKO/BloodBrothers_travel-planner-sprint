package com.example.bbtraveling.data.repository

import com.example.bbtraveling.data.local.dao.HotelReservationDao
import com.example.bbtraveling.data.local.dao.TripDao
import com.example.bbtraveling.data.local.dao.TripImageDao
import com.example.bbtraveling.data.local.entity.HotelReservationEntity
import com.example.bbtraveling.data.local.entity.TripEntity
import com.example.bbtraveling.data.local.entity.TripImageEntity
import com.example.bbtraveling.data.remote.api.HotelApiService
import com.example.bbtraveling.data.remote.dto.HotelDto
import com.example.bbtraveling.data.remote.dto.ReservationDto
import com.example.bbtraveling.data.remote.dto.ReserveRequestDto
import com.example.bbtraveling.data.remote.dto.RoomDto
import com.example.bbtraveling.domain.Hotel
import com.example.bbtraveling.domain.HotelReservation
import com.example.bbtraveling.domain.HotelRoom
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.TripImage
import com.example.bbtraveling.domain.TripStatus
import com.example.bbtraveling.domain.repository.AuthRepository
import com.example.bbtraveling.domain.repository.HotelBookingRepository
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
class HotelBookingRepositoryImpl @Inject constructor(
    private val api: HotelApiService,
    private val tripDao: TripDao,
    private val reservationDao: HotelReservationDao,
    private val tripImageDao: TripImageDao,
    private val authRepository: AuthRepository,
    private val clock: Clock
) : HotelBookingRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeReservations(): Flow<List<HotelReservation>> {
        return authRepository.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                reservationDao.observeReservationsForOwner(user.login).map { rows ->
                    rows.map { it.toDomain() }
                }
            }
        }
    }

    override fun observeTripImages(tripId: String): Flow<List<TripImage>> {
        return tripImageDao.observeImagesForTrip(tripId).map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun searchHotels(city: String, startDate: LocalDate, endDate: LocalDate): Result<List<Hotel>> {
        return runCatching {
            api.checkAvailability(
                groupId = GROUP_ID,
                startDate = startDate.toString(),
                endDate = endDate.toString(),
                city = city
            ).availableHotels.map { it.toDomain() }
        }
    }

    override suspend fun bookRoom(
        hotel: Hotel,
        room: HotelRoom,
        startDate: LocalDate,
        endDate: LocalDate
    ): OperationResult {
        val owner = authRepository.currentUser.value
            ?: return OperationResult.Failure(message = ERROR_AUTH_REQUIRED)
        if (!startDate.isBefore(endDate)) {
            return OperationResult.Failure(message = ERROR_INVALID_DATES)
        }

        return try {
            val guestName = owner.login.substringBefore("@").ifBlank { owner.login }
            val remoteReservation = api.reserveRoom(
                groupId = GROUP_ID,
                request = ReserveRequestDto(
                    hotelId = hotel.id,
                    roomId = room.id,
                    startDate = startDate.toString(),
                    endDate = endDate.toString(),
                    guestName = guestName,
                    guestEmail = owner.email
                )
            ).reservation ?: error(ERROR_REMOTE)
            val tripId = UUID.randomUUID().toString()
            val reservation = remoteReservation.toEntity(
                fallbackHotel = hotel,
                fallbackRoom = room,
                tripId = tripId,
                ownerLogin = owner.login,
                createdAt = LocalDateTime.now(clock)
            )
            val city = cityFromAddress(hotel.address)
            val nights = ChronoUnit.DAYS.between(startDate, endDate).coerceAtLeast(1)
            tripDao.upsertTrip(
                TripEntity(
                    id = tripId,
                    ownerLogin = owner.login,
                    title = "${hotel.name} ${room.roomType}",
                    description = "Hotel reservation ${reservation.id} for ${room.roomType}.",
                    city = city,
                    country = countryForCity(city),
                    startDate = startDate,
                    endDate = endDate,
                    status = TripStatus.Upcoming,
                    accommodation = "${hotel.name} - ${room.roomType}",
                    transport = "",
                    travelers = 1,
                    budgetEur = room.price * nights,
                    createdAt = LocalDateTime.now(clock)
                )
            )
            reservationDao.upsertReservation(reservation)
            saveReservationImages(reservation)
            OperationResult.Success
        } catch (exception: Exception) {
            OperationResult.Failure(message = exception.message ?: ERROR_REMOTE)
        }
    }

    override suspend fun cancelReservation(reservationId: String): OperationResult {
        val reservation = reservationDao.getReservation(reservationId)
            ?: return OperationResult.Failure(message = ERROR_RESERVATION_NOT_FOUND)
        return try {
            api.cancelReservationById(reservationId)
            reservationDao.deleteReservation(reservationId)
            tripDao.deleteTripById(reservation.tripId)
            OperationResult.Success
        } catch (exception: Exception) {
            OperationResult.Failure(message = exception.message ?: ERROR_REMOTE)
        }
    }

    override suspend fun addTripImage(tripId: String, uri: String, title: String): OperationResult {
        val owner = authRepository.currentUser.value
            ?: return OperationResult.Failure(message = ERROR_AUTH_REQUIRED)
        if (uri.isBlank()) return OperationResult.Failure(message = ERROR_IMAGE_REQUIRED)
        tripImageDao.upsertImage(
            TripImageEntity(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                ownerLogin = owner.login,
                uri = uri,
                title = title.ifBlank { "Trip image" },
                createdAt = LocalDateTime.now(clock)
            )
        )
        return OperationResult.Success
    }

    override suspend fun deleteTripImage(imageId: String): OperationResult {
        tripImageDao.deleteImage(imageId)
        return OperationResult.Success
    }

    private suspend fun saveReservationImages(reservation: HotelReservationEntity) {
        val imageRows = (listOf(reservation.hotelImageUrl) + reservation.roomImageUrls)
            .filter { it.isNotBlank() }
            .distinct()
            .mapIndexed { index, url ->
                TripImageEntity(
                    id = "${reservation.id}-$index",
                    tripId = reservation.tripId,
                    ownerLogin = reservation.ownerLogin,
                    uri = absoluteUrl(url),
                    title = reservation.hotelName,
                    createdAt = LocalDateTime.now(clock)
                )
            }
        imageRows.forEach { tripImageDao.upsertImage(it) }
    }

    private fun HotelDto.toDomain(): Hotel {
        return Hotel(
            id = id,
            name = name,
            address = address,
            rating = rating,
            imageUrl = absoluteUrl(imageUrl),
            rooms = rooms.map { it.toDomain() }
        )
    }

    private fun RoomDto.toDomain(): HotelRoom {
        return HotelRoom(
            id = id,
            roomType = roomType,
            price = price,
            images = images.map(::absoluteUrl)
        )
    }

    private fun ReservationDto.toEntity(
        fallbackHotel: Hotel,
        fallbackRoom: HotelRoom,
        tripId: String,
        ownerLogin: String,
        createdAt: LocalDateTime
    ): HotelReservationEntity {
        val roomValue = RoomDto(
            id = fallbackRoom.id,
            roomType = fallbackRoom.roomType,
            price = fallbackRoom.price,
            images = fallbackRoom.images
        )
        return HotelReservationEntity(
            id = id,
            tripId = tripId,
            ownerLogin = ownerLogin,
            hotelId = hotelId,
            hotelName = fallbackHotel.name,
            hotelAddress = fallbackHotel.address,
            hotelRating = fallbackHotel.rating,
            hotelImageUrl = absoluteUrl(fallbackHotel.imageUrl),
            roomId = roomId,
            roomType = roomValue.roomType,
            roomPrice = roomValue.price,
            roomImageUrls = roomValue.images.map(::absoluteUrl),
            startDate = LocalDate.parse(startDate),
            endDate = LocalDate.parse(endDate),
            guestName = guestName,
            guestEmail = guestEmail,
            createdAt = createdAt
        )
    }

    private fun HotelReservationEntity.toDomain(): HotelReservation {
        return HotelReservation(
            id = id,
            tripId = tripId,
            ownerLogin = ownerLogin,
            hotelId = hotelId,
            hotelName = hotelName,
            hotelAddress = hotelAddress,
            hotelRating = hotelRating,
            hotelImageUrl = hotelImageUrl,
            roomId = roomId,
            roomType = roomType,
            roomPrice = roomPrice,
            roomImageUrls = roomImageUrls,
            startDate = startDate,
            endDate = endDate,
            guestName = guestName,
            guestEmail = guestEmail
        )
    }

    private fun TripImageEntity.toDomain(): TripImage {
        return TripImage(
            id = id,
            tripId = tripId,
            ownerLogin = ownerLogin,
            uri = uri,
            title = title
        )
    }

    private fun absoluteUrl(value: String): String {
        return if (value.startsWith("http")) value else "${BASE_URL.trimEnd('/')}/$value".replace("//images", "/images")
    }

    private fun cityFromAddress(address: String): String {
        return when {
            address.contains("Barcelona", ignoreCase = true) -> "Barcelona"
            address.contains("Paris", ignoreCase = true) -> "Paris"
            address.contains("London", ignoreCase = true) -> "London"
            else -> address.substringAfterLast(",").trim().ifBlank { "Barcelona" }
        }
    }

    private fun countryForCity(city: String): String {
        return when (city.lowercase()) {
            "barcelona" -> "Spain"
            "paris" -> "France"
            "london" -> "United Kingdom"
            else -> ""
        }
    }

    private companion object {
        const val BASE_URL = "http://15.224.84.148:8090"
        const val GROUP_ID = "G05"
        const val ERROR_AUTH_REQUIRED = "User must be logged in."
        const val ERROR_INVALID_DATES = "Start date must be before end date."
        const val ERROR_IMAGE_REQUIRED = "Image is required."
        const val ERROR_REMOTE = "Remote hotel service failed."
        const val ERROR_RESERVATION_NOT_FOUND = "Reservation not found."
    }
}
