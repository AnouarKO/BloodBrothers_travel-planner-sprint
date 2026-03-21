package com.example.bbtraveling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bbtraveling.R
import com.example.bbtraveling.ui.preview.PreviewScreenContainer
import com.example.bbtraveling.ui.preview.previewSettingsViewModel
import com.example.bbtraveling.ui.viewmodel.SettingsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    settingsViewModel: SettingsViewModel,
    onBack: (() -> Unit)?
) {
    val settings by settingsViewModel.settings.collectAsState()
    val languageOptions = listOf(
        "en" to stringResource(R.string.lang_english),
        "es" to stringResource(R.string.lang_spanish),
        "ca" to stringResource(R.string.lang_catalan)
    )
    val languageSavedMessage = stringResource(R.string.pref_language_saved)
    val darkModeSavedMessage = stringResource(R.string.pref_dark_mode_saved)
    val usernameSavedMessage = stringResource(R.string.pref_username_saved)
    val dateOfBirthSavedMessage = stringResource(R.string.pref_date_of_birth_saved)
    var languageExpanded by remember { mutableStateOf(false) }
    var usernameDialogVisible by remember { mutableStateOf(false) }
    var birthDatePickerVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_preferences)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            PreferenceActionCard(
                title = stringResource(R.string.pref_username),
                value = settings.username.ifBlank { stringResource(R.string.pref_not_set) },
                subtitle = stringResource(R.string.pref_username_subtitle),
                leadingIcon = { PreferenceIcon { Icon(Icons.Rounded.Person, contentDescription = null) } },
                onClick = { usernameDialogVisible = true }
            )

            Spacer(Modifier.height(12.dp))

            PreferenceActionCard(
                title = stringResource(R.string.pref_date_of_birth),
                value = settings.dateOfBirth.ifBlank { stringResource(R.string.pref_select_date_of_birth) },
                subtitle = stringResource(R.string.pref_date_of_birth_subtitle),
                leadingIcon = { PreferenceIcon { Icon(Icons.Rounded.Cake, contentDescription = null) } },
                onClick = { birthDatePickerVisible = true }
            )

            Spacer(Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PreferenceIcon {
                            Icon(Icons.Rounded.Language, contentDescription = null)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.pref_language), style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.pref_language_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Box {
                        OutlinedButton(
                            onClick = { languageExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(languageOptions.firstOrNull { it.first == settings.languageTag }?.second.orEmpty())
                        }
                        DropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false }
                        ) {
                            languageOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.second) },
                                    onClick = {
                                        settingsViewModel.updateLanguage(option.first)
                                        languageExpanded = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message = languageSavedMessage)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            PreferenceSwitchCard(
                title = stringResource(R.string.pref_dark_mode),
                subtitle = stringResource(R.string.pref_dark_mode_subtitle),
                checked = settings.darkMode,
                onCheckedChange = { enabled ->
                    settingsViewModel.updateDarkMode(enabled)
                    scope.launch {
                        snackbarHostState.showSnackbar(message = darkModeSavedMessage)
                    }
                },
                leading = {
                    PreferenceIcon {
                        Icon(Icons.Rounded.Palette, contentDescription = null)
                    }
                }
            )
        }
    }

    if (usernameDialogVisible) {
        UsernameDialog(
            currentValue = settings.username,
            onDismiss = { usernameDialogVisible = false },
            onConfirm = { username ->
                settingsViewModel.updateUsername(username)
                usernameDialogVisible = false
                scope.launch {
                    snackbarHostState.showSnackbar(message = usernameSavedMessage)
                }
            }
        )
    }

    if (birthDatePickerVisible) {
        DateOfBirthDialog(
            initialDate = settings.dateOfBirth.toLocalDateOrNull(),
            onDismiss = { birthDatePickerVisible = false },
            onConfirm = { selectedDate ->
                settingsViewModel.updateDateOfBirth(selectedDate.format(PREFERENCE_DATE_FORMAT))
                birthDatePickerVisible = false
                scope.launch {
                    snackbarHostState.showSnackbar(message = dateOfBirthSavedMessage)
                }
            }
        )
    }
}

@Composable
private fun PreferenceActionCard(
    title: String,
    value: String,
    subtitle: String,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(value, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun UsernameDialog(
    currentValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var username by remember { mutableStateOf(currentValue) }
    var errorVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pref_username_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorVisible = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.pref_username)) },
                    isError = errorVisible
                )
                if (errorVisible) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.pref_username_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (username.isBlank()) {
                        errorVisible = true
                    } else {
                        onConfirm(username.trim())
                    }
                }
            ) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate.toEpochMillis())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = datePickerState.selectedDateMillis?.toLocalDate()
                    if (selected != null) {
                        onConfirm(selected)
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun PreferenceSwitchCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leading: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leading()
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun PreferenceIcon(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return runCatching { LocalDate.parse(this, PREFERENCE_DATE_FORMAT) }.getOrNull()
}

private fun LocalDate?.toEpochMillis(): Long? {
    if (this == null) return null
    return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

private val PREFERENCE_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreferencesScreenPreview() {
    PreviewScreenContainer {
        PreferencesScreen(
            settingsViewModel = previewSettingsViewModel(),
            onBack = {}
        )
    }
}
