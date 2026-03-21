package com.example.bbtraveling.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bbtraveling.R
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.TripDraft
import com.example.bbtraveling.domain.TripStatus
import com.example.bbtraveling.domain.validation.TravelValidator
import com.example.bbtraveling.ui.errorMessageResId
import com.example.bbtraveling.ui.formatEuro
import com.example.bbtraveling.ui.preview.PreviewScreenContainer
import com.example.bbtraveling.ui.preview.previewTrips
import com.example.bbtraveling.ui.preview.previewTripsViewModel
import com.example.bbtraveling.ui.viewmodel.TripsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private sealed class TripFilter(@param:androidx.annotation.StringRes val labelResId: Int) {
    data object All : TripFilter(R.string.trip_filter_all)
    data object Draft : TripFilter(R.string.trip_status_draft)
    data object Planning : TripFilter(R.string.trip_status_planning)
    data object Upcoming : TripFilter(R.string.trip_status_upcoming)
    data object Completed : TripFilter(R.string.trip_status_completed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    trips: List<Trip>,
    tripsViewModel: TripsViewModel,
    onTripClick: (String) -> Unit
) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    var editorTrip by remember { mutableStateOf<Trip?>(null) }
    var openCreateDialog by remember { mutableStateOf(false) }

    val filters = listOf(
        TripFilter.All,
        TripFilter.Draft,
        TripFilter.Planning,
        TripFilter.Upcoming,
        TripFilter.Completed
    )
    val filteredTrips = trips
        .filterBy(filters[selectedFilter])
        .sortedForDisplay()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_trips)) }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedFilter) {
                filters.forEachIndexed { index, filter ->
                    Tab(
                        selected = selectedFilter == index,
                        onClick = { selectedFilter = index },
                        text = { Text(stringResource(filter.labelResId)) }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { openCreateDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.action_add_trip))
                        }
                        SummaryCard(
                            total = trips.size,
                            shown = filteredTrips.size,
                            filter = stringResource(filters[selectedFilter].labelResId),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                items(filteredTrips) { trip ->
                    TripRowCard(
                        trip = trip,
                        onOpen = { onTripClick(trip.id) },
                        onEdit = { editorTrip = trip },
                        onDelete = { tripsViewModel.deleteTrip(trip.id) }
                    )
                }
            }
        }
    }

    if (openCreateDialog) {
        TripEditorDialog(
            title = stringResource(R.string.title_create_trip),
            initialTrip = null,
            onDismiss = { openCreateDialog = false },
            onSubmit = { draft, _ -> tripsViewModel.addTrip(draft) },
            onSuccess = { openCreateDialog = false }
        )
    }

    editorTrip?.let { currentTrip ->
        TripEditorDialog(
            title = stringResource(R.string.title_edit_trip),
            initialTrip = currentTrip,
            onDismiss = { editorTrip = null },
            onSubmit = { draft, moveActivitiesWithTrip ->
                tripsViewModel.editTrip(
                    currentTrip.id,
                    draft,
                    moveActivitiesWithTrip = moveActivitiesWithTrip
                )
            },
            onSuccess = { editorTrip = null }
        )
    }
}

@Composable
private fun SummaryCard(
    total: Int,
    shown: Int,
    filter: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(stringResource(R.string.trip_overview_title), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.trip_overview_summary, shown, total, filter),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TripRowCard(
    trip: Trip,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6A1CF7).copy(alpha = 0.10f)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = trip.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(text = formatDateRange(trip.startDate, trip.endDate))
            Spacer(Modifier.height(4.dp))
            if (trip.destination.isNotBlank()) {
                Text(
                    text = stringResource(R.string.trip_destination_value, trip.destination),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = stringResource(
                    R.string.trip_status_value,
                    stringResource(tripStatusLabelResId(trip.status))
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.trip_money_summary,
                    formatEuro(trip.budgetEur),
                    formatEuro(trip.spentEur),
                    formatEuro(trip.remainingEur)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = trip.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onOpen, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_open))
                }
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_edit))
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_delete))
                }
            }
        }
    }
}

@Composable
private fun TripEditorDialog(
    title: String,
    initialTrip: Trip?,
    onDismiss: () -> Unit,
    onSubmit: (TripDraft, Boolean) -> OperationResult,
    onSuccess: () -> Unit
) {
    var tripTitle by rememberSaveable(initialTrip?.id) { mutableStateOf(initialTrip?.title.orEmpty()) }
    var description by rememberSaveable(initialTrip?.id) { mutableStateOf(initialTrip?.description.orEmpty()) }
    var city by rememberSaveable(initialTrip?.id) { mutableStateOf(initialTrip?.destination.cityPart()) }
    var country by rememberSaveable(initialTrip?.id) { mutableStateOf(initialTrip?.destination.countryPart()) }
    var startDate by rememberSaveable(initialTrip?.id) { mutableStateOf(initialTrip?.startDate) }
    var endDate by rememberSaveable(initialTrip?.id) { mutableStateOf(initialTrip?.endDate) }
    var status by rememberSaveable(initialTrip?.id) { mutableStateOf(initialTrip?.status ?: TripStatus.Draft) }
    var budgetText by rememberSaveable(initialTrip?.id) {
        mutableStateOf(initialTrip?.budgetEur?.toMoneyInput().orEmpty())
    }
    var statusManuallySelected by rememberSaveable(initialTrip?.id) { mutableStateOf(initialTrip != null) }
    var startPickerVisible by remember { mutableStateOf(false) }
    var endPickerVisible by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var countryExpanded by remember { mutableStateOf(false) }
    var moveActivitiesWithTrip by rememberSaveable(initialTrip?.id) {
        mutableStateOf(initialTrip?.activities?.isNotEmpty() == true)
    }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var generalError by remember { mutableStateOf<String?>(null) }
    val countryOptions = listOf(
        stringResource(R.string.country_spain),
        stringResource(R.string.country_brazil),
        stringResource(R.string.country_italy),
        stringResource(R.string.country_france),
        stringResource(R.string.country_portugal),
        stringResource(R.string.country_germany),
        stringResource(R.string.country_united_kingdom),
        stringResource(R.string.country_netherlands),
        stringResource(R.string.country_belgium)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val result = onSubmit(
                        TripDraft(
                            title = tripTitle.trim(),
                            description = description.trim(),
                            city = city.trim(),
                            country = country.trim(),
                            startDate = startDate,
                            endDate = endDate,
                            status = status,
                            budgetEur = budgetText.toMoneyOrNull()
                        ),
                        moveActivitiesWithTrip
                    )

                    when (result) {
                        is OperationResult.Success -> onSuccess()
                        is OperationResult.Failure -> {
                            fieldErrors = result.fieldErrors
                            generalError = result.message
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = tripTitle,
                    onValueChange = {
                        tripTitle = it
                        generalError = null
                        fieldErrors = fieldErrors - TravelValidator.FIELD_TITLE
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_title)) },
                    isError = fieldErrors.containsKey(TravelValidator.FIELD_TITLE),
                    singleLine = true
                )
                fieldErrors[TravelValidator.FIELD_TITLE]?.let {
                    ErrorText(it)
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        generalError = null
                        fieldErrors = fieldErrors - TravelValidator.FIELD_DESCRIPTION
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_description)) },
                    isError = fieldErrors.containsKey(TravelValidator.FIELD_DESCRIPTION),
                    minLines = 2
                )
                fieldErrors[TravelValidator.FIELD_DESCRIPTION]?.let {
                    ErrorText(it)
                }

                OutlinedTextField(
                    value = city,
                    onValueChange = {
                        city = it
                        generalError = null
                        fieldErrors = fieldErrors - TravelValidator.FIELD_CITY
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_city)) },
                    isError = fieldErrors.containsKey(TravelValidator.FIELD_CITY),
                    singleLine = true
                )
                fieldErrors[TravelValidator.FIELD_CITY]?.let {
                    ErrorText(it)
                }

                CountryField(
                    selectedCountry = country,
                    options = countryOptions,
                    expanded = countryExpanded,
                    error = fieldErrors[TravelValidator.FIELD_COUNTRY],
                    onExpandedChange = { countryExpanded = it },
                    onSelectCountry = {
                        country = it
                        fieldErrors = fieldErrors - TravelValidator.FIELD_COUNTRY
                    }
                )

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = {
                        budgetText = it
                        generalError = null
                        fieldErrors = fieldErrors - TravelValidator.FIELD_BUDGET
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_budget)) },
                    isError = fieldErrors.containsKey(TravelValidator.FIELD_BUDGET),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                fieldErrors[TravelValidator.FIELD_BUDGET]?.let {
                    ErrorText(it)
                }

                DateField(
                    label = stringResource(R.string.label_start_date),
                    date = startDate,
                    error = fieldErrors[TravelValidator.FIELD_START_DATE],
                    onClick = { startPickerVisible = true }
                )
                DateField(
                    label = stringResource(R.string.label_end_date),
                    date = endDate,
                    error = fieldErrors[TravelValidator.FIELD_END_DATE],
                    onClick = { endPickerVisible = true }
                )

                fieldErrors[TravelValidator.FIELD_DATE]?.let {
                    ErrorText(it)
                }

                if (initialTrip != null && initialTrip.activities.isNotEmpty()) {
                    RescheduleActivitiesField(
                        checked = moveActivitiesWithTrip,
                        onCheckedChange = { moveActivitiesWithTrip = it }
                    )
                }

                StatusField(
                    selectedStatus = status,
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                    onSelectStatus = {
                        status = it
                        statusManuallySelected = true
                    }
                )
                Text(
                    text = stringResource(statusDescriptionResId(status)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                generalError?.takeIf { it.isNotBlank() }?.let {
                    ErrorText(it)
                }
            }
        }
    )

    if (startPickerVisible) {
        DatePickerDialogInternal(
            initial = startDate,
            onDismiss = { startPickerVisible = false },
            onConfirm = {
                startDate = it
                fieldErrors = fieldErrors - TravelValidator.FIELD_START_DATE
                if (!statusManuallySelected) {
                    status = suggestStatus(startDate = startDate, endDate = endDate)
                }
            }
        )
    }
    if (endPickerVisible) {
        DatePickerDialogInternal(
            initial = endDate,
            onDismiss = { endPickerVisible = false },
            onConfirm = {
                endDate = it
                fieldErrors = fieldErrors - TravelValidator.FIELD_END_DATE
                if (!statusManuallySelected) {
                    status = suggestStatus(startDate = startDate, endDate = endDate)
                }
            }
        )
    }
}

@Composable
private fun RescheduleActivitiesField(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
            Column {
                Text(
                    text = stringResource(R.string.label_move_itinerary_with_trip),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(R.string.label_move_itinerary_with_trip_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CountryField(
    selectedCountry: String,
    options: List<String>,
    expanded: Boolean,
    error: String?,
    onExpandedChange: (Boolean) -> Unit,
    onSelectCountry: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.label_country), style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(
                onClick = { onExpandedChange(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    selectedCountry.ifBlank { stringResource(R.string.placeholder_select_country) }
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelectCountry(option)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
        if (error != null) {
            ErrorText(error)
        }
    }
}

@Composable
private fun StatusField(
    selectedStatus: TripStatus,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelectStatus: (TripStatus) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.label_status), style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(
                onClick = { onExpandedChange(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(tripStatusLabelResId(selectedStatus)))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                TripStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(stringResource(tripStatusLabelResId(status))) },
                        onClick = {
                            onSelectStatus(status)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateField(
    label: String,
    date: LocalDate?,
    error: String?,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(date?.format(DISPLAY_DATE_FORMAT) ?: stringResource(R.string.placeholder_select_date))
        }
        if (error != null) {
            ErrorText(error)
        }
    }
}

@Composable
private fun ErrorText(errorCode: String) {
    Text(
        stringResource(errorMessageResId(errorCode)),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogInternal(
    initial: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initial.toEpochMillis())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = datePickerState.selectedDateMillis?.toLocalDate()
                    if (selected != null) onConfirm(selected)
                    onDismiss()
                }
            ) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun LocalDate?.toEpochMillis(): Long? {
    if (this == null) return null
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

private fun formatDateRange(startDate: LocalDate, endDate: LocalDate): String {
    return "${startDate.format(DISPLAY_DATE_FORMAT)} - ${endDate.format(DISPLAY_DATE_FORMAT)}"
}

private fun List<Trip>.filterBy(filter: TripFilter): List<Trip> {
    return when (filter) {
        TripFilter.All -> this
        TripFilter.Draft -> filter { it.status == TripStatus.Draft }
        TripFilter.Planning -> filter { it.status == TripStatus.Planning }
        TripFilter.Upcoming -> filter { it.status == TripStatus.Upcoming }
        TripFilter.Completed -> filter { it.status == TripStatus.Completed }
    }
}

private fun List<Trip>.sortedForDisplay(): List<Trip> {
    return sortedWith(compareBy<Trip>({ tripStatusSortOrder(it.status) }, { it.startDate }, { it.title }))
}

private fun tripStatusLabelResId(status: TripStatus): Int {
    return when (status) {
        TripStatus.Draft -> R.string.trip_status_draft
        TripStatus.Planning -> R.string.trip_status_planning
        TripStatus.Upcoming -> R.string.trip_status_upcoming
        TripStatus.Completed -> R.string.trip_status_completed
    }
}

private fun statusDescriptionResId(status: TripStatus): Int {
    return when (status) {
        TripStatus.Draft -> R.string.trip_status_draft_description
        TripStatus.Planning -> R.string.trip_status_planning_description
        TripStatus.Upcoming -> R.string.trip_status_upcoming_description
        TripStatus.Completed -> R.string.trip_status_completed_description
    }
}

private fun tripStatusSortOrder(status: TripStatus): Int {
    return when (status) {
        TripStatus.Upcoming -> 0
        TripStatus.Planning -> 1
        TripStatus.Draft -> 2
        TripStatus.Completed -> 3
    }
}

private fun suggestStatus(startDate: LocalDate?, endDate: LocalDate?): TripStatus {
    if (startDate == null || endDate == null) return TripStatus.Draft
    val daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), startDate)
    return if (daysUntilStart <= 30) TripStatus.Upcoming else TripStatus.Planning
}

private fun Double.toMoneyInput(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else toString()
}

private fun String.toMoneyOrNull(): Double? {
    if (isBlank()) return null
    return replace(',', '.').toDoubleOrNull()
}

private fun String?.cityPart(): String {
    return this?.substringBefore(",")?.trim().orEmpty()
}

private fun String?.countryPart(): String {
    if (this.isNullOrBlank()) return ""
    return substringAfter(",", "").trim()
}

private val DISPLAY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TripsScreenPreview() {
    PreviewScreenContainer {
        TripsScreen(
            trips = previewTrips(),
            tripsViewModel = previewTripsViewModel(),
            onTripClick = {}
        )
    }
}
