package com.example.bbtraveling.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.bbtraveling.data.local.entity.AccessLogEntity
import com.example.bbtraveling.data.local.entity.HotelReservationEntity
import com.example.bbtraveling.data.local.entity.ItineraryItemEntity
import com.example.bbtraveling.data.local.entity.TripEntity
import com.example.bbtraveling.data.local.entity.TripImageEntity
import com.example.bbtraveling.data.local.entity.UserProfileEntity
import com.example.bbtraveling.domain.ActivityCategory
import com.example.bbtraveling.domain.TripStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TravelDatabaseTest {

    private lateinit var database: TravelDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TravelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun userProfileDao_findsExistingUsernameBeforeRegistration() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "first@example.com", username = "test01"))

        val existingUser = database.userProfileDao().getUserByUsername("test01")

        assertEquals("first@example.com", existingUser?.login)
    }

    @Test
    fun tripDao_observesOnlyTripsForLoggedUserWithActivities() = runBlocking {
        val userDao = database.userProfileDao()
        val tripDao = database.tripDao()
        val itineraryItemDao = database.itineraryItemDao()
        userDao.upsertUser(user(login = "owner@example.com", username = "owner"))
        userDao.upsertUser(user(login = "other@example.com", username = "other"))

        tripDao.upsertTrip(trip(id = "trip-owner", ownerLogin = "owner@example.com", title = "Rome"))
        tripDao.upsertTrip(trip(id = "trip-other", ownerLogin = "other@example.com", title = "Paris"))
        itineraryItemDao.upsertItem(activity(id = "activity-owner", tripId = "trip-owner"))

        val ownerTrips = tripDao.observeTripsWithActivitiesForOwner("owner@example.com").first()

        assertEquals(1, ownerTrips.size)
        assertEquals("trip-owner", ownerTrips.first().trip.id)
        assertEquals("Rome", ownerTrips.first().trip.title)
        assertEquals(1, ownerTrips.first().activities.size)
        assertEquals("activity-owner", ownerTrips.first().activities.first().id)
    }

    @Test
    fun accessLogDao_persistsLoginAndLogoutEvents() = runBlocking {
        val accessLogDao = database.accessLogDao()
        accessLogDao.insertAccessLog(
            AccessLogEntity(
                userId = "firebase-uid",
                eventType = "LOGIN",
                occurredAt = LocalDateTime.of(2026, 5, 1, 10, 0)
            )
        )
        accessLogDao.insertAccessLog(
            AccessLogEntity(
                userId = "firebase-uid",
                eventType = "LOGOUT",
                occurredAt = LocalDateTime.of(2026, 5, 1, 11, 0)
            )
        )

        val logs = accessLogDao.observeAccessLogs("firebase-uid").first()

        assertEquals(2, logs.size)
        assertEquals("LOGOUT", logs.first().eventType)
        assertEquals("LOGIN", logs.last().eventType)
    }

    @Test
    fun hotelReservationDao_persistsReservationWithRoomImages() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        database.tripDao().upsertTrip(trip(id = "trip-owner", ownerLogin = "owner@example.com", title = "Barcelona"))

        database.hotelReservationDao().upsertReservation(
            HotelReservationEntity(
                id = "reservation-1",
                tripId = "trip-owner",
                ownerLogin = "owner@example.com",
                hotelId = "BCN01",
                hotelName = "Barcelona Central",
                hotelAddress = "Carrer Test, Barcelona",
                hotelRating = 4,
                hotelImageUrl = "http://15.224.84.148:8090/images/BCN01.png",
                roomId = "BCN01-R1",
                roomType = "Double room",
                roomPrice = 120.0,
                roomImageUrls = listOf("room-a.png", "room-b.png"),
                startDate = LocalDate.of(2026, 6, 1),
                endDate = LocalDate.of(2026, 6, 3),
                guestName = "owner",
                guestEmail = "owner@example.com",
                createdAt = LocalDateTime.of(2026, 5, 1, 9, 30)
            )
        )

        val reservations = database.hotelReservationDao().observeReservationsForOwner("owner@example.com").first()

        assertEquals(1, reservations.size)
        assertEquals(listOf("room-a.png", "room-b.png"), reservations.first().roomImageUrls)
    }

    @Test
    fun tripImageDao_persistsImagesForSpecificTrip() = runBlocking {
        database.userProfileDao().upsertUser(user(login = "owner@example.com", username = "owner"))
        database.tripDao().upsertTrip(trip(id = "trip-owner", ownerLogin = "owner@example.com", title = "Barcelona"))
        database.tripImageDao().upsertImage(
            TripImageEntity(
                id = "image-1",
                tripId = "trip-owner",
                ownerLogin = "owner@example.com",
                uri = "content://local/image/1",
                title = "Beach",
                createdAt = LocalDateTime.of(2026, 5, 1, 10, 0)
            )
        )

        val images = database.tripImageDao().observeImagesForTrip("trip-owner").first()

        assertEquals(1, images.size)
        assertEquals("Beach", images.first().title)
    }

    private fun user(login: String, username: String): UserProfileEntity {
        return UserProfileEntity(
            login = login,
            username = username,
            birthdate = LocalDate.of(2000, 1, 1),
            address = "Test street 1",
            country = "Spain",
            phone = "600000000",
            acceptsReceiveEmails = true
        )
    }

    private fun trip(id: String, ownerLogin: String, title: String): TripEntity {
        return TripEntity(
            id = id,
            ownerLogin = ownerLogin,
            title = title,
            description = "Trip description",
            city = "City",
            country = "Country",
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 3),
            status = TripStatus.Planning,
            accommodation = "Hotel",
            transport = "Train",
            travelers = 1,
            budgetEur = 300.0,
            createdAt = LocalDateTime.of(2026, 5, 1, 9, 0)
        )
    }

    private fun activity(id: String, tripId: String): ItineraryItemEntity {
        return ItineraryItemEntity(
            id = id,
            tripId = tripId,
            title = "Museum",
            description = "Visit",
            date = LocalDate.of(2026, 6, 2),
            time = LocalTime.of(10, 0),
            category = ActivityCategory.Museum,
            costEur = 20.0,
            createdAt = LocalDateTime.of(2026, 5, 1, 9, 30)
        )
    }
}

