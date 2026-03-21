package com.example.bbtraveling.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bbtraveling.domain.UserSettings
import com.example.bbtraveling.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.StateFlow

// Saca preferencias guardadas a la UI y registra cambios importantes
class SettingsViewModel(
    private val repository: UserSettingsRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = repository.settings

    fun updateUsername(value: String) {
        repository.updateUsername(value)
        Log.i(TAG, "username updated")
    }

    fun updateDateOfBirth(value: String) {
        repository.updateDateOfBirth(value)
        Log.i(TAG, "dateOfBirth updated")
    }

    fun updateDarkMode(enabled: Boolean) {
        repository.updateDarkMode(enabled)
        Log.i(TAG, "dark mode updated: $enabled")
    }

    fun updateLanguage(languageTag: String) {
        repository.updateLanguage(languageTag)
        Log.i(TAG, "language updated: $languageTag")
    }

    fun hasAcceptedTerms(): Boolean = repository.hasAcceptedTerms()

    fun acceptTerms() {
        repository.setTermsAccepted(true)
        Log.i(TAG, "terms accepted")
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}

class SettingsViewModelFactory(
    private val repository: UserSettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
