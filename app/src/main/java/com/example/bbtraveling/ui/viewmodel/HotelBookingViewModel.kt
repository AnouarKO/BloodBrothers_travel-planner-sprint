package com.example.bbtraveling.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bbtraveling.domain.Hotel
import com.example.bbtraveling.domain.HotelReservation
import com.example.bbtraveling.domain.HotelRoom
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.TripImage
import com.example.bbtraveling.domain.repository.HotelBookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HotelBookingUiState(
    val city: String = HotelBookingViewModel.DEFAULT_CITY,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val hotels: List<Hotel> = emptyList(),
    val searched: Boolean = false,
    val loading: Boolean = false,
    val message: HotelBookingMessage? = null,
    val error: HotelBookingMessage? = null
)

enum class HotelBookingMessage {
    SearchFailed,
    DatesRequired,
    InvalidDates,
    AuthRequired,
    BookingCreated,
    BookingFailed,
    ReservationCancelled,
    CancelFailed,
    ImageAdded,
    ImageDeleted,
    ImageFailed
}

@HiltViewModel
class HotelBookingViewModel @Inject constructor(
    private val repository: HotelBookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HotelBookingUiState())
    val uiState: StateFlow<HotelBookingUiState> = _uiState.asStateFlow()

    val reservations: StateFlow<List<HotelReservation>> = repository.observeReservations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeTripImages(tripId: String): Flow<List<TripImage>> {
        return repository.observeTripImages(tripId)
    }

    fun updateCity(city: String) {
        _uiState.update { it.copy(city = city, message = null, error = null) }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update { it.copy(startDate = date, message = null, error = null) }
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.update { it.copy(endDate = date, message = null, error = null) }
    }

    fun searchHotels() {
        val state = _uiState.value
        val startDate = state.startDate
        val endDate = state.endDate
        if (startDate == null || endDate == null) {
            _uiState.update { it.copy(error = HotelBookingMessage.DatesRequired, message = null) }
            return
        }
        if (!startDate.isBefore(endDate)) {
            _uiState.update { it.copy(error = HotelBookingMessage.InvalidDates, message = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, searched = true, error = null, message = null) }
            repository.searchHotels(
                city = state.city,
                startDate = startDate,
                endDate = endDate
            ).fold(
                onSuccess = { hotels ->
                    _uiState.update { it.copy(hotels = hotels, loading = false) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = HotelBookingMessage.SearchFailed
                        )
                    }
                }
            )
        }
    }

    fun bookRoom(hotel: Hotel, room: HotelRoom) {
        val state = _uiState.value
        val startDate = state.startDate
        val endDate = state.endDate
        if (startDate == null || endDate == null) {
            _uiState.update { it.copy(error = HotelBookingMessage.DatesRequired, message = null) }
            return
        }
        if (!startDate.isBefore(endDate)) {
            _uiState.update { it.copy(error = HotelBookingMessage.InvalidDates, message = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, message = null) }
            val result = repository.bookRoom(
                hotel = hotel,
                room = room,
                startDate = startDate,
                endDate = endDate
            )
            _uiState.update {
                it.copy(
                    loading = false,
                    message = if (result is OperationResult.Success) {
                        HotelBookingMessage.BookingCreated
                    } else {
                        null
                    },
                    error = result.toHotelError(default = HotelBookingMessage.BookingFailed)
                )
            }
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, message = null) }
            val result = repository.cancelReservation(reservationId)
            _uiState.update {
                it.copy(
                    loading = false,
                    message = if (result is OperationResult.Success) {
                        HotelBookingMessage.ReservationCancelled
                    } else {
                        null
                    },
                    error = result.toHotelError(default = HotelBookingMessage.CancelFailed)
                )
            }
        }
    }

    fun addTripImage(tripId: String, uri: String, title: String) {
        viewModelScope.launch {
            val result = repository.addTripImage(tripId = tripId, uri = uri, title = title)
            _uiState.update {
                it.copy(
                    message = if (result is OperationResult.Success) HotelBookingMessage.ImageAdded else null,
                    error = result.toHotelError(default = HotelBookingMessage.ImageFailed)
                )
            }
        }
    }

    fun deleteTripImage(imageId: String) {
        viewModelScope.launch {
            val result = repository.deleteTripImage(imageId)
            _uiState.update {
                it.copy(
                    message = if (result is OperationResult.Success) HotelBookingMessage.ImageDeleted else null,
                    error = result.toHotelError(default = HotelBookingMessage.ImageFailed)
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    private fun OperationResult.toHotelError(default: HotelBookingMessage): HotelBookingMessage? {
        return when (this) {
            is OperationResult.Success -> null
            is OperationResult.Failure -> when (message) {
                ERROR_AUTH_REQUIRED -> HotelBookingMessage.AuthRequired
                ERROR_INVALID_DATES -> HotelBookingMessage.InvalidDates
                else -> default
            }
        }
    }

    companion object {
        const val DEFAULT_CITY = "Barcelona"
        val CITY_OPTIONS = listOf("Barcelona", "Paris", "London")
        private const val ERROR_AUTH_REQUIRED = "User must be logged in."
        private const val ERROR_INVALID_DATES = "Start date must be before end date."
    }
}
