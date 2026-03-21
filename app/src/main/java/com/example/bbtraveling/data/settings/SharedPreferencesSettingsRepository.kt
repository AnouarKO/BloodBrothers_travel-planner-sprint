package com.example.bbtraveling.data.settings

import android.content.Context
import com.example.bbtraveling.domain.UserSettings
import com.example.bbtraveling.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Guarda ajustes simples y los carga al volver a entrar
class SharedPreferencesSettingsRepository(context: Context) : UserSettingsRepository {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(loadSettings())

    override val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    override fun readStoredLanguageTag(): String {
        return preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE).orEmpty().ifBlank { DEFAULT_LANGUAGE }
    }

    override fun hasAcceptedTerms(): Boolean {
        return preferences.getBoolean(KEY_TERMS_ACCEPTED, false)
    }

    override fun updateUsername(value: String) {
        preferences.edit().putString(KEY_USERNAME, value).apply()
        _settings.value = _settings.value.copy(username = value)
    }

    override fun updateDateOfBirth(value: String) {
        preferences.edit().putString(KEY_DATE_OF_BIRTH, value).apply()
        _settings.value = _settings.value.copy(dateOfBirth = value)
    }

    override fun updateDarkMode(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _settings.value = _settings.value.copy(darkMode = enabled)
    }

    override fun updateLanguage(languageTag: String) {
        preferences.edit().putString(KEY_LANGUAGE, languageTag).apply()
        _settings.value = _settings.value.copy(languageTag = languageTag)
    }

    override fun setTermsAccepted(accepted: Boolean) {
        preferences.edit().putBoolean(KEY_TERMS_ACCEPTED, accepted).apply()
        _settings.value = _settings.value.copy(termsAccepted = accepted)
    }

    private fun loadSettings(): UserSettings {
        return UserSettings(
            username = preferences.getString(KEY_USERNAME, "").orEmpty(),
            dateOfBirth = preferences.getString(KEY_DATE_OF_BIRTH, "").orEmpty(),
            darkMode = preferences.getBoolean(KEY_DARK_MODE, false),
            languageTag = readStoredLanguageTag(),
            termsAccepted = hasAcceptedTerms()
        )
    }

    private companion object {
        const val PREFS_NAME = "bbtraveling_settings"
        const val KEY_USERNAME = "username"
        const val KEY_DATE_OF_BIRTH = "date_of_birth"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_LANGUAGE = "language"
        const val KEY_TERMS_ACCEPTED = "terms_accepted"
        const val DEFAULT_LANGUAGE = "en"
    }
}
