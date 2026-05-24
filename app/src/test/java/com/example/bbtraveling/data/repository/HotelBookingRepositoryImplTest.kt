package com.example.bbtraveling.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.bbtraveling.data.local.TravelDatabase
import com.example.bbtraveling.data.local.entity.TripEntity
import com.example.bbtraveling.data.local.entity.UserProfileEntity
import com.example.bbtraveling.data.remote.api.HotelApiService
import com.example.bbtraveling.data.remote.dto.AvailabilityResponseDto
import com.example.bbtraveling.data.remote.dto.HotelApiMessageDto
import com.example.bbtraveling.data.remote.dto.HotelDto
import com.example.bbtraveling.data.remote.dto.HotelSummaryDto
import com.example.bbtraveling.data.remote.dto.ReservationDetailsDto
import com.example.bbtraveling.data.remote.dto.ReservationDto
import com.example.bbtraveling.data.remote.dto.ReservationsResponseDto
import com.example.bbtraveling.data.remote.dto.ReserveRequestDto
import com.example.bbtraveling.data.remote.dto.RoomDto
import com.example.bbtraveling.domain.AuthRegistration
import com.example.bbtraveling.domain.AuthResult
import com.example.bbtraveling.domain.AuthUser
import com.example.bbtraveling.domain.Hotel
import com.example.bbtraveling.domain.HotelRoom
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.TripStatus
import com.example.bbtraveling.domain.repository.AuthRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HotelBookingRepositoryImplTest {

    private lateinit var database: TravelDatabase
    private lateinit var api: FakeHotelApiService
    private lateinit var authRepository: FakeAuthRepository
    private val clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TravelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        api = FakeHotelApiService()
        authRepository = FakeAuthRepository()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun searchHotels_mapsAvailabilityResponseToDomainModels() = runBlocking {
        api.availabilityResponse = AvailabilityResponseDto(availableHotels = listOf(hotelDto()))
        val repository = createRepository()

        val result = repository.searchHotels(
            city = "Barcelona",
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 3)
        )

        assertTrue(result.isSuccess)
        val hotel = result.getOrThrow().first()
        assertEquals("BCN01", hotel.id)
        assertEquals("http://15.224.84.148:8090/images/BCN01.png", hotel.imageUrl)
        assertEquals("http://15.224.84.148:8090/images/BCN01-R1.png", hotel.rooms.first().images.first())
    }

    @Test
    fun bookRoom_createsTripReservationAndGalleryImages() = runBlocking {
        seedUser("owner@example.com")
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        api.reserveResponse = reserveResponseDto()
        val repository = createRepository()

        val result = repository.bookRoom(
            hotel = hotelDomain(),
            room = roomDomain(),
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 3)
        )

        assertTrue(result is OperationResult.Success)
        val reservations = database.hotelReservationDao().observeReservationsForOwner("owner@example.com").first()
        assertEquals(1, reservations.size)
        assertEquals("reservation-1", reservations.first().id)
        assertEquals("owner", api.lastReserveRequest?.guestName)
        val trips = database.tripDao().observeTripsWithActivitiesForOwner("owner@example.com").first()
        assertEquals(1, trips.size)
        assertEquals("Barcelona, Spain", trips.first().trip.city + ", " + trips.first().trip.country)
        assertEquals(240.0, trips.first().trip.budgetEur, 0.001)
        val images = database.tripImageDao().observeImagesForTrip(reservations.first().tripId).first()
        assertEquals(3, images.size)
    }

    @Test
    fun bookRoom_withoutAuthenticatedUserFails() = runBlocking {
        val repository = createRepository()

        val result = repository.bookRoom(
            hotel = hotelDomain(),
            room = roomDomain(),
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 3)
        )

        assertTrue(result is OperationResult.Failure)
        result as OperationResult.Failure
        assertEquals("User must be logged in.", result.message)
    }

    @Test
    fun cancelReservation_removesLocalReservationAndGeneratedTrip() = runBlocking {
        seedUser("owner@example.com")
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        api.reserveResponse = reserveResponseDto()
        val repository = createRepository()
        repository.bookRoom(
            hotel = hotelDomain(),
            room = roomDomain(),
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 3)
        )

        val result = repository.cancelReservation("reservation-1")

        assertTrue(result is OperationResult.Success)
        assertEquals("reservation-1", api.cancelledReservationId)
        assertTrue(database.hotelReservationDao().observeReservationsForOwner("owner@example.com").first().isEmpty())
        assertTrue(database.tripDao().observeTripsWithActivitiesForOwner("owner@example.com").first().isEmpty())
    }

    @Test
    fun addAndDeleteTripImage_updatesTripGalleryRows() = runBlocking {
        seedUser("owner@example.com")
        database.tripDao().upsertTrip(trip(id = "trip-1", ownerLogin = "owner@example.com"))
        authRepository.currentUserFlow.value = authUser("owner@example.com")
        val repository = createRepository()

        val addResult = repository.addTripImage(
            tripId = "trip-1",
            uri = "content://local/image/1",
            title = "Boarding pass"
        )

        assertTrue(addResult is OperationResult.Success)
        val image = database.tripImageDao().observeImagesForTrip("trip-1").first().single()
        assertEquals("Boarding pass", image.title)

        val deleteResult = repository.deleteTripImage(image.id)

        assertTrue(deleteResult is OperationResult.Success)
        assertTrue(database.tripImageDao().observeImagesForTrip("trip-1").first().isEmpty())
    }

    private fun createRepository(): HotelBookingRepositoryImpl {
        return HotelBookingRepositoryImpl(
            api = api,
            tripDao = database.tripDao(),
            reservationDao = database.hotelReservationDao(),
            tripImageDao = database.tripImageDao(),
            authRepository = authRepository,
            userProfileRepository = RoomUserProfileRepository(database.userProfileDao()),
            clock = clock
        )
    }

    private suspend fun seedUser(login: String) {
        database.userProfileDao().upsertUser(
            UserProfileEntity(
                login = login,
                username = login.substringBefore("@"),
                birthdate = LocalDate.of(2000, 1, 1),
                address = "Test street 1",
                country = "Spain",
                phone = "600000000",
                acceptsReceiveEmails = true
            )
        )
    }

    private fun hotelDomain(): Hotel {
        return Hotel(
            id = "BCN01",
            name = "Barcelona Central",
            address = "Carrer Test, Barcelona",
            rating = 4,
            imageUrl = "http://15.224.84.148:8090/images/BCN01.png",
            rooms = listOf(roomDomain())
        )
    }

    private fun roomDomain(): HotelRoom {
        return HotelRoom(
            id = "BCN01-R1",
            roomType = "Double room",
            price = 120.0,
            images = listOf(
                "http://15.224.84.148:8090/images/BCN01-R1.png",
                "http://15.224.84.148:8090/images/BCN01-R2.png"
            )
        )
    }

    private fun hotelDto(): HotelDto {
        return HotelDto(
            id = "BCN01",
            name = "Barcelona Central",
            address = "Carrer Test, Barcelona",
            rating = 4,
            imageUrl = "/images/BCN01.png",
            rooms = listOf(
                RoomDto(
                    id = "BCN01-R1",
                    roomType = "Double room",
                    price = 120.0,
                    images = listOf("/images/BCN01-R1.png")
                )
            )
        )
    }

    private fun reservationDto(): ReservationDto {
        return ReservationDto(
            id = "reservation-1",
            hotelId = "BCN01",
            roomId = "BCN01-R1",
            startDate = "2026-06-01",
            endDate = "2026-06-03",
            guestName = "owner",
            guestEmail = "owner@example.com"
        )
    }

    private fun reserveResponseDto(): HotelApiMessageDto {
        return HotelApiMessageDto(
            message = "Reservation confirmed",
            nights = 2,
            reservation = reservationDto()
        )
    }

    private fun trip(id: String, ownerLogin: String): TripEntity {
        return TripEntity(
            id = id,
            ownerLogin = ownerLogin,
            title = "Test trip",
            description = "Trip description",
            city = "Barcelona",
            country = "Spain",
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 3),
            status = TripStatus.Planning,
            accommodation = "Hotel",
            transport = "",
            travelers = 1,
            budgetEur = 200.0,
            createdAt = LocalDateTime.of(2026, 1, 1, 0, 0)
        )
    }

    private fun authUser(email: String): AuthUser {
        return AuthUser(
            userId = "uid-$email",
            login = email,
            email = email
        )
    }

    private class FakeHotelApiService : HotelApiService {
        var availabilityResponse = AvailabilityResponseDto(emptyList())
        var reserveResponse = HotelApiMessageDto(
            message = "Reservation confirmed",
            nights = 2,
            reservation = ReservationDto(
                id = "reservation-1",
                hotelId = "BCN01",
                roomId = "BCN01-R1",
                startDate = "2026-06-01",
                endDate = "2026-06-03",
                guestName = "owner",
                guestEmail = "owner@example.com"
            )
        )
        var lastReserveRequest: ReserveRequestDto? = null
        var cancelledReservationId: String? = null

        override suspend fun getHotels(groupId: String): List<HotelDto> = emptyList()

        override suspend fun checkAvailability(
            groupId: String,
            startDate: String,
            endDate: String,
            city: String?,
            hotelId: String?
        ): AvailabilityResponseDto = availabilityResponse

        override suspend fun reserveRoom(groupId: String, request: ReserveRequestDto): HotelApiMessageDto {
            lastReserveRequest = request
            return reserveResponse
        }

        override suspend fun cancelReservation(
            groupId: String,
            request: ReserveRequestDto
        ): HotelApiMessageDto {
            return HotelApiMessageDto(message = "Reservation cancelled")
        }

        override suspend fun getReservations(groupId: String, guestEmail: String?): ReservationsResponseDto {
            return ReservationsResponseDto(emptyList())
        }

        override suspend fun getReservationById(reservationId: String): ReservationDetailsDto {
            return reservationDetailsDto()
        }

        override suspend fun cancelReservationById(reservationId: String): ReservationDetailsDto {
            cancelledReservationId = reservationId
            return reservationDetailsDto()
        }

        private fun reservationDetailsDto(): ReservationDetailsDto {
            return ReservationDetailsDto(
                id = "reservation-1",
                hotelId = "BCN01",
                roomId = "BCN01-R1",
                startDate = "2026-06-01",
                endDate = "2026-06-03",
                guestName = "owner",
                guestEmail = "owner@example.com",
                hotel = HotelSummaryDto(
                    id = "BCN01",
                    name = "Barcelona Central",
                    address = "Carrer Test, Barcelona",
                    rating = 4,
                    imageUrl = "/images/BCN01.png"
                ),
                room = RoomDto(
                    id = "BCN01-R1",
                    roomType = "Double room",
                    price = 120.0,
                    images = listOf("/images/BCN01-R1.png", "/images/BCN01-R2.png")
                )
            )
        }
    }

    private class FakeAuthRepository : AuthRepository {
        val currentUserFlow = MutableStateFlow<AuthUser?>(null)
        override val currentUser: StateFlow<AuthUser?> = currentUserFlow

        override suspend fun login(email: String, password: String): AuthResult = AuthResult.Success()

        override suspend fun register(registration: AuthRegistration): AuthResult = AuthResult.Success()

        override suspend fun recoverPassword(email: String): AuthResult = AuthResult.Success()

        override suspend fun logout() {
            currentUserFlow.value = null
        }
    }
}
