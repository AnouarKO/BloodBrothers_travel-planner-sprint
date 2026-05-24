package com.example.bbtraveling.ui.viewmodel

import com.example.bbtraveling.domain.Hotel
import com.example.bbtraveling.domain.HotelReservation
import com.example.bbtraveling.domain.HotelRoom
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.TripImage
import com.example.bbtraveling.domain.repository.HotelBookingRepository
import com.example.bbtraveling.testutil.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HotelBookingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeHotelBookingRepository
    private lateinit var viewModel: HotelBookingViewModel

    @Before
    fun setup() {
        repository = FakeHotelBookingRepository()
        viewModel = HotelBookingViewModel(repository)
    }

    @Test
    fun searchHotels_withoutDatesSetsRequiredErrorWithoutCallingRepository() {
        viewModel.searchHotels()

        assertEquals(HotelBookingMessage.DatesRequired, viewModel.uiState.value.error)
        assertEquals(0, repository.searchCalls)
    }

    @Test
    fun searchHotels_withInvalidDateRangeSetsErrorWithoutCallingRepository() {
        val startDate = LocalDate.of(2026, 6, 5)
        viewModel.updateStartDate(startDate)
        viewModel.updateEndDate(startDate)

        viewModel.searchHotels()

        assertEquals(HotelBookingMessage.InvalidDates, viewModel.uiState.value.error)
        assertEquals(0, repository.searchCalls)
    }

    @Test
    fun searchHotels_withValidDateRangeStoresHotelsInState() = runTest {
        repository.searchResult = Result.success(listOf(hotel()))
        viewModel.updateStartDate(LocalDate.of(2026, 6, 10))
        viewModel.updateEndDate(LocalDate.of(2026, 6, 12))

        viewModel.searchHotels()

        assertEquals(1, repository.searchCalls)
        assertEquals("BCN01", viewModel.uiState.value.hotels.single().id)
        assertTrue(viewModel.uiState.value.searched)
    }

    @Test
    fun searchHotels_withJulyDatesCallsRepository() = runTest {
        repository.searchResult = Result.success(listOf(hotel()))
        viewModel.updateStartDate(LocalDate.of(2026, 7, 1))
        viewModel.updateEndDate(LocalDate.of(2026, 7, 3))

        viewModel.searchHotels()

        assertEquals(1, repository.searchCalls)
        assertEquals("BCN01", viewModel.uiState.value.hotels.single().id)
    }

    @Test
    fun bookRoom_withSuccessfulResultShowsCreatedMessage() = runTest {
        repository.bookResult = OperationResult.Success
        viewModel.updateStartDate(LocalDate.of(2026, 6, 10))
        viewModel.updateEndDate(LocalDate.of(2026, 6, 12))

        viewModel.bookRoom(hotel(), room())

        assertEquals(1, repository.bookCalls)
        assertEquals(HotelBookingMessage.BookingCreated, viewModel.uiState.value.message)
    }

    @Test
    fun cancelReservation_withSuccessfulResultShowsCancelledMessage() = runTest {
        repository.cancelResult = OperationResult.Success

        viewModel.cancelReservation("reservation-1")

        assertEquals("reservation-1", repository.cancelledReservationId)
        assertEquals(HotelBookingMessage.ReservationCancelled, viewModel.uiState.value.message)
    }

    private fun hotel(): Hotel {
        return Hotel(
            id = "BCN01",
            name = "Barcelona Central",
            address = "Carrer Test, Barcelona",
            rating = 4,
            imageUrl = "http://15.224.84.148:8090/images/BCN01.png",
            rooms = listOf(room())
        )
    }

    private fun room(): HotelRoom {
        return HotelRoom(
            id = "BCN01-R1",
            roomType = "Double room",
            price = 120.0,
            images = emptyList()
        )
    }

    private class FakeHotelBookingRepository : HotelBookingRepository {
        var searchResult: Result<List<Hotel>> = Result.success(emptyList())
        var bookResult: OperationResult = OperationResult.Success
        var cancelResult: OperationResult = OperationResult.Success
        var searchCalls = 0
        var bookCalls = 0
        var cancelledReservationId: String? = null

        override fun observeReservations(): Flow<List<HotelReservation>> {
            return MutableStateFlow(emptyList())
        }

        override fun observeTripImages(tripId: String): Flow<List<TripImage>> {
            return flowOf(emptyList())
        }

        override suspend fun searchHotels(
            city: String,
            startDate: LocalDate,
            endDate: LocalDate
        ): Result<List<Hotel>> {
            searchCalls += 1
            return searchResult
        }

        override suspend fun bookRoom(
            hotel: Hotel,
            room: HotelRoom,
            startDate: LocalDate,
            endDate: LocalDate
        ): OperationResult {
            bookCalls += 1
            return bookResult
        }

        override suspend fun cancelReservation(reservationId: String): OperationResult {
            cancelledReservationId = reservationId
            return cancelResult
        }

        override suspend fun addTripImage(tripId: String, uri: String, title: String): OperationResult {
            return OperationResult.Success
        }

        override suspend fun deleteTripImage(imageId: String): OperationResult {
            return OperationResult.Success
        }
    }
}
