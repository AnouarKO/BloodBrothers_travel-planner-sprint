package com.example.bbtraveling.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bbtraveling.R
import com.example.bbtraveling.domain.Hotel
import com.example.bbtraveling.domain.HotelReservation
import com.example.bbtraveling.domain.HotelRoom
import com.example.bbtraveling.ui.formatEuro
import com.example.bbtraveling.ui.viewmodel.HotelBookingMessage
import com.example.bbtraveling.ui.viewmodel.HotelBookingUiState
import com.example.bbtraveling.ui.viewmodel.HotelBookingViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelBookingScreen(
    viewModel: HotelBookingViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }

    val messageText = state.message?.let { stringResource(it.stringResId()) }
    val errorText = state.error?.let { stringResource(it.stringResId()) }
    LaunchedEffect(messageText, errorText) {
        val text = errorText ?: messageText
        if (!text.isNullOrBlank()) {
            snackbarHostState.showSnackbar(text)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_hotels)) }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.tab_search_hotels)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.tab_reservations)) }
                )
            }

            if (selectedTab == 0) {
                HotelSearchContent(
                    state = state,
                    onCityChange = viewModel::updateCity,
                    onStartDateChange = viewModel::updateStartDate,
                    onEndDateChange = viewModel::updateEndDate,
                    onSearch = viewModel::searchHotels,
                    onBookRoom = viewModel::bookRoom
                )
            } else {
                ReservationsContent(
                    reservations = reservations,
                    loading = state.loading,
                    onCancelReservation = viewModel::cancelReservation
                )
            }
        }
    }
}

@Composable
private fun HotelSearchContent(
    state: HotelBookingUiState,
    onCityChange: (String) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onSearch: () -> Unit,
    onBookRoom: (Hotel, HotelRoom) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SearchPanel(
                state = state,
                onCityChange = onCityChange,
                onStartDateChange = onStartDateChange,
                onEndDateChange = onEndDateChange,
                onSearch = onSearch
            )
        }

        if (state.loading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }

        if (state.searched && !state.loading && state.hotels.isEmpty()) {
            item {
                EmptyCard(text = stringResource(R.string.hotel_no_results))
            }
        }

        items(state.hotels) { hotel ->
            HotelCard(
                hotel = hotel,
                onBookRoom = { room -> onBookRoom(hotel, room) }
            )
        }
    }
}

@Composable
private fun SearchPanel(
    state: HotelBookingUiState,
    onCityChange: (String) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onSearch: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Hotel, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(stringResource(R.string.hotel_search_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.hotel_search_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            CityField(selectedCity = state.city, onCityChange = onCityChange)
            DateField(
                label = stringResource(R.string.label_start_date),
                date = state.startDate,
                onDateSelected = onStartDateChange
            )
            DateField(
                label = stringResource(R.string.label_end_date),
                date = state.endDate,
                onDateSelected = onEndDateChange
            )
            Button(
                onClick = onSearch,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_search_hotels))
            }
        }
    }
}

@Composable
private fun CityField(
    selectedCity: String,
    onCityChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.label_city), style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedCity)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                HotelBookingViewModel.CITY_OPTIONS.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city) },
                        onClick = {
                            onCityChange(city)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var datePickerVisible by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(
            onClick = { datePickerVisible = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Event, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(date.format(DISPLAY_DATE_FORMAT))
        }
    }
    if (datePickerVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.toEpochMillis())
        DatePickerDialog(
            onDismissRequest = { datePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDate()?.let(onDateSelected)
                        datePickerVisible = false
                    }
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerVisible = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun HotelCard(
    hotel: Hotel,
    onBookRoom: (HotelRoom) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column {
            AsyncImage(
                model = hotel.imageUrl,
                contentDescription = hotel.name,
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(hotel.name, style = MaterialTheme.typography.titleLarge)
                Text(hotel.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.hotel_rating_value, hotel.rating))
                hotel.rooms.forEach { room ->
                    RoomRow(room = room, onBookRoom = { onBookRoom(room) })
                }
            }
        }
    }
}

@Composable
private fun RoomRow(
    room: HotelRoom,
    onBookRoom: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = room.images.firstOrNull(),
                contentDescription = room.roomType,
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(room.roomType, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.hotel_room_price, formatEuro(room.price)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onBookRoom) {
                Text(stringResource(R.string.action_book_room))
            }
        }
    }
}

@Composable
private fun ReservationsContent(
    reservations: List<HotelReservation>,
    loading: Boolean,
    onCancelReservation: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (loading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }

        if (reservations.isEmpty() && !loading) {
            item {
                EmptyCard(text = stringResource(R.string.hotel_no_reservations))
            }
        }

        items(reservations) { reservation ->
            ReservationCard(
                reservation = reservation,
                onCancel = { onCancelReservation(reservation.id) }
            )
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: HotelReservation,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column {
            AsyncImage(
                model = reservation.hotelImageUrl,
                contentDescription = reservation.hotelName,
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(reservation.hotelName, style = MaterialTheme.typography.titleLarge)
                        Text(reservation.roomType, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.action_cancel_reservation)
                        )
                    }
                }
                Text(reservation.hotelAddress, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    stringResource(
                        R.string.hotel_dates_value,
                        reservation.startDate.format(DISPLAY_DATE_FORMAT),
                        reservation.endDate.format(DISPLAY_DATE_FORMAT)
                    )
                )
                Text(stringResource(R.string.hotel_guest_value, reservation.guestEmail))
                Text(stringResource(R.string.hotel_room_price, formatEuro(reservation.roomPrice)))
            }
        }
    }
}

@Composable
private fun EmptyCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = false) {},
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@StringRes
private fun HotelBookingMessage.stringResId(): Int {
    return when (this) {
        HotelBookingMessage.SearchFailed -> R.string.hotel_error_search_failed
        HotelBookingMessage.InvalidDates -> R.string.hotel_error_invalid_dates
        HotelBookingMessage.AuthRequired -> R.string.hotel_error_auth_required
        HotelBookingMessage.BookingCreated -> R.string.hotel_booking_created
        HotelBookingMessage.BookingFailed -> R.string.hotel_error_booking_failed
        HotelBookingMessage.ReservationCancelled -> R.string.hotel_booking_cancelled
        HotelBookingMessage.CancelFailed -> R.string.hotel_error_cancel_failed
        HotelBookingMessage.ImageAdded -> R.string.hotel_gallery_added
        HotelBookingMessage.ImageDeleted -> R.string.hotel_gallery_deleted
        HotelBookingMessage.ImageFailed -> R.string.hotel_gallery_error
    }
}

private fun LocalDate.toEpochMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

private val DISPLAY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
