package com.example.bbtraveling.ui.screens

import android.app.TimePickerDialog
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.bbtraveling.R
import com.example.bbtraveling.domain.Activity
import com.example.bbtraveling.domain.ActivityCategory
import com.example.bbtraveling.domain.ActivityDraft
import com.example.bbtraveling.domain.OperationResult
import com.example.bbtraveling.domain.Trip
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
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class ActivityPreset(
    val titleResId: Int,
    val descriptionResId: Int,
    val category: ActivityCategory,
    val defaultCostEur: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    tripsViewModel: TripsViewModel,
    onBack: () -> Unit,
    onOpenGallery: () -> Unit
) {
    val trips by tripsViewModel.trips.collectAsState()
    val trip = trips.firstOrNull { it.id == tripId }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var openCreateDialog by remember { mutableStateOf(false) }
    var editActivity by remember { mutableStateOf<Activity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip?.title ?: stringResource(R.string.title_trip)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (trip == null) {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text(stringResource(R.string.msg_trip_not_found))
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf(
                    stringResource(R.string.tab_overview),
                    stringResource(R.string.tab_itinerary)
                ).forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) }
                    )
                }
            }

            if (selectedTab == 0) {
                OverviewTab(
                    trip = trip,
                    onOpenGallery = onOpenGallery
                )
            } else {
                ItineraryTab(
                    trip = trip,
                    onAddActivity = { openCreateDialog = true },
                    onEditActivity = { editActivity = it },
                    onDeleteActivity = { activityId ->
                        tripsViewModel.deleteActivity(trip.id, activityId)
                    }
                )
            }
        }
    }

    if (openCreateDialog) {
        trip?.let { currentTrip ->
            ActivityEditorDialog(
                title = stringResource(R.string.action_add_activity),
                trip = currentTrip,
                initialActivity = null,
                onDismiss = { openCreateDialog = false },
                onSubmit = { draft -> tripsViewModel.addActivity(currentTrip.id, draft) },
                onSuccess = { openCreateDialog = false }
            )
        }
    }

    editActivity?.let { currentActivity ->
        trip?.let { currentTrip ->
            ActivityEditorDialog(
                title = stringResource(R.string.title_edit_activity),
                trip = currentTrip,
                initialActivity = currentActivity,
                onDismiss = { editActivity = null },
                onSubmit = { draft -> tripsViewModel.updateActivity(currentTrip.id, currentActivity.id, draft) },
                onSuccess = { editActivity = null }
            )
        }
    }
}

@Composable
private fun OverviewTab(
    trip: Trip,
    onOpenGallery: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = trip.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(text = formatDateRange(trip.startDate, trip.endDate))
                    if (trip.destination.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.trip_destination_value, trip.destination),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            R.string.trip_status_value,
                            stringResource(tripStatusLabelResId(trip.status))
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = trip.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OverviewMetricCard(
                    title = stringResource(R.string.label_budget),
                    value = formatEuro(trip.budgetEur),
                    modifier = Modifier.weight(1f)
                )
                OverviewMetricCard(
                    title = stringResource(R.string.home_stat_spent),
                    value = formatEuro(trip.spentEur),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OverviewMetricCard(
                    title = stringResource(R.string.trip_remaining_title),
                    value = formatEuro(trip.remainingEur),
                    modifier = Modifier.weight(1f)
                )
                OverviewMetricCard(
                    title = stringResource(R.string.trip_activities_title),
                    value = trip.activities.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedButton(onClick = onOpenGallery, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_open_gallery))
            }
        }
    }
}

@Composable
private fun OverviewMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ItineraryTab(
    trip: Trip,
    onAddActivity: () -> Unit,
    onEditActivity: (Activity) -> Unit,
    onDeleteActivity: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedButton(onClick = onAddActivity, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_add_activity))
            }
        }

        items(trip.activities) { activity ->
            ActivityRow(
                activity = activity,
                onEdit = { onEditActivity(activity) },
                onDelete = { onDeleteActivity(activity.id) }
            )
        }
    }
}

@Composable
private fun ActivityRow(
    activity: Activity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(activity.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("${activity.date.format(DISPLAY_DATE_FORMAT)} ${activity.time.format(DISPLAY_TIME_FORMAT)}")
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(
                    R.string.activity_category_value,
                    stringResource(categoryLabelResId(activity.category))
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(R.string.activity_cost_value, formatEuro(activity.costEur)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                activity.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
private fun ActivityEditorDialog(
    title: String,
    trip: Trip,
    initialActivity: Activity?,
    onDismiss: () -> Unit,
    onSubmit: (ActivityDraft) -> OperationResult,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val presets = remember { activityPresets() }
    var activityTitle by rememberSaveable(initialActivity?.id) { mutableStateOf(initialActivity?.title.orEmpty()) }
    var description by rememberSaveable(initialActivity?.id) { mutableStateOf(initialActivity?.description.orEmpty()) }
    var date by rememberSaveable(initialActivity?.id) { mutableStateOf(initialActivity?.date) }
    var time by rememberSaveable(initialActivity?.id) { mutableStateOf(initialActivity?.time) }
    var category by rememberSaveable(initialActivity?.id) { mutableStateOf(initialActivity?.category ?: ActivityCategory.Other) }
    var costText by rememberSaveable(initialActivity?.id) {
        mutableStateOf(initialActivity?.costEur?.toMoneyInput().orEmpty())
    }
    var presetExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var datePickerVisible by remember { mutableStateOf(false) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var generalError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true),
        confirmButton = {
            TextButton(
                onClick = {
                    val result = onSubmit(
                        ActivityDraft(
                            title = activityTitle.trim(),
                            description = description.trim(),
                            date = date,
                            time = time,
                            category = category,
                            costEur = costText.toMoneyOrNull()
                        )
                    )

                    when (result) {
                        is OperationResult.Success -> onSuccess()
                        is OperationResult.Failure -> {
                            fieldErrors = result.fieldErrors
                            generalError = result.message
                        }
                    }
                }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PresetField(
                    expanded = presetExpanded,
                    presets = presets,
                    onExpandedChange = { presetExpanded = it },
                    onSelectPreset = { preset ->
                        activityTitle = context.getString(preset.titleResId)
                        description = context.getString(preset.descriptionResId)
                        category = preset.category
                        costText = preset.defaultCostEur.toMoneyInput()
                        generalError = null
                        fieldErrors = fieldErrors -
                            TravelValidator.FIELD_TITLE -
                            TravelValidator.FIELD_DESCRIPTION -
                            TravelValidator.FIELD_COST
                    }
                )

                OutlinedTextField(
                    value = activityTitle,
                    onValueChange = {
                        activityTitle = it
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

                CategoryField(
                    selectedCategory = category,
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    onSelectCategory = { category = it }
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = {
                        costText = it
                        generalError = null
                        fieldErrors = fieldErrors - TravelValidator.FIELD_COST
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.label_cost)) },
                    isError = fieldErrors.containsKey(TravelValidator.FIELD_COST),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                fieldErrors[TravelValidator.FIELD_COST]?.let {
                    ErrorText(it)
                }

                PickerField(
                    label = stringResource(R.string.label_date),
                    value = date?.format(DISPLAY_DATE_FORMAT) ?: stringResource(R.string.placeholder_select_date),
                    error = fieldErrors[TravelValidator.FIELD_DATE],
                    onClick = { datePickerVisible = true }
                )

                PickerField(
                    label = stringResource(R.string.label_time),
                    value = time?.format(DISPLAY_TIME_FORMAT) ?: stringResource(R.string.placeholder_select_time),
                    error = fieldErrors[TravelValidator.FIELD_TIME],
                    onClick = {
                        val start = time ?: LocalTime.of(12, 0)
                        TimePickerDialog(
                            context,
                            { _, hour: Int, minute: Int ->
                                time = LocalTime.of(hour, minute)
                                fieldErrors = fieldErrors - TravelValidator.FIELD_TIME
                            },
                            start.hour,
                            start.minute,
                            true
                        ).show()
                    }
                )

                generalError?.takeIf { it.isNotBlank() }?.let {
                    ErrorText(it)
                }
            }
        }
    )

    if (datePickerVisible) {
        DatePickerDialogInternal(
            initial = date,
            onDismiss = { datePickerVisible = false },
            onConfirm = {
                date = it
                fieldErrors = fieldErrors - TravelValidator.FIELD_DATE
            }
        )
    }
}

@Composable
private fun PresetField(
    expanded: Boolean,
    presets: List<ActivityPreset>,
    onExpandedChange: (Boolean) -> Unit,
    onSelectPreset: (ActivityPreset) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.label_activity_template), style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(
                onClick = { onExpandedChange(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_use_template))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                ActivityCategory.entries.forEachIndexed { index, category ->
                    Text(
                        text = stringResource(categoryLabelResId(category)),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    presets
                        .filter { it.category == category }
                        .forEach { preset ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(stringResource(preset.titleResId))
                                        Text(
                                            text = stringResource(R.string.activity_cost_value, formatEuro(preset.defaultCostEur)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectPreset(preset)
                                    onExpandedChange(false)
                                }
                            )
                        }
                    if (index != ActivityCategory.entries.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryField(
    selectedCategory: ActivityCategory,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelectCategory: (ActivityCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.label_category), style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(
                onClick = { onExpandedChange(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(categoryLabelResId(selectedCategory)))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                ActivityCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(stringResource(categoryLabelResId(category))) },
                        onClick = {
                            onSelectCategory(category)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerField(
    label: String,
    value: String,
    error: String?,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(value)
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
    val state = rememberDatePickerState(initialSelectedDateMillis = initial.toEpochMillis())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = state.selectedDateMillis?.toLocalDate()
                    if (selected != null) onConfirm(selected)
                    onDismiss()
                }
            ) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    ) {
        DatePicker(state = state)
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

private fun tripStatusLabelResId(status: TripStatus): Int {
    return when (status) {
        TripStatus.Draft -> R.string.trip_status_draft
        TripStatus.Planning -> R.string.trip_status_planning
        TripStatus.Upcoming -> R.string.trip_status_upcoming
        TripStatus.Completed -> R.string.trip_status_completed
    }
}

private fun categoryLabelResId(category: ActivityCategory): Int {
    return when (category) {
        ActivityCategory.Restaurant -> R.string.activity_category_restaurant
        ActivityCategory.Transport -> R.string.activity_category_transport
        ActivityCategory.Museum -> R.string.activity_category_museum
        ActivityCategory.Leisure -> R.string.activity_category_leisure
        ActivityCategory.Other -> R.string.activity_category_other
    }
}

private fun activityPresets(): List<ActivityPreset> {
    return listOf(
        ActivityPreset(
            titleResId = R.string.preset_restaurant_breakfast_title,
            descriptionResId = R.string.preset_restaurant_breakfast_description,
            category = ActivityCategory.Restaurant,
            defaultCostEur = 18.0
        ),
        ActivityPreset(
            titleResId = R.string.preset_restaurant_dinner_title,
            descriptionResId = R.string.preset_restaurant_dinner_description,
            category = ActivityCategory.Restaurant,
            defaultCostEur = 45.0
        ),
        ActivityPreset(
            titleResId = R.string.preset_transport_airport_title,
            descriptionResId = R.string.preset_transport_airport_description,
            category = ActivityCategory.Transport,
            defaultCostEur = 28.0
        ),
        ActivityPreset(
            titleResId = R.string.preset_transport_daypass_title,
            descriptionResId = R.string.preset_transport_daypass_description,
            category = ActivityCategory.Transport,
            defaultCostEur = 12.0
        ),
        ActivityPreset(
            titleResId = R.string.preset_museum_ticket_title,
            descriptionResId = R.string.preset_museum_ticket_description,
            category = ActivityCategory.Museum,
            defaultCostEur = 22.0
        ),
        ActivityPreset(
            titleResId = R.string.preset_museum_tour_title,
            descriptionResId = R.string.preset_museum_tour_description,
            category = ActivityCategory.Museum,
            defaultCostEur = 34.0
        ),
        ActivityPreset(
            titleResId = R.string.preset_leisure_walk_title,
            descriptionResId = R.string.preset_leisure_walk_description,
            category = ActivityCategory.Leisure,
            defaultCostEur = 0.0
        ),
        ActivityPreset(
            titleResId = R.string.preset_other_checkin_title,
            descriptionResId = R.string.preset_other_checkin_description,
            category = ActivityCategory.Other,
            defaultCostEur = 0.0
        )
    )
}

private fun Double.toMoneyInput(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else toString()
}

private fun String.toMoneyOrNull(): Double? {
    if (isBlank()) return null
    return replace(',', '.').toDoubleOrNull()
}

private val DISPLAY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val DISPLAY_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TripDetailScreenPreview() {
    val trips = previewTrips()
    PreviewScreenContainer {
        TripDetailScreen(
            tripId = trips.firstOrNull()?.id.orEmpty(),
            tripsViewModel = previewTripsViewModel(),
            onBack = {},
            onOpenGallery = {}
        )
    }
}
