package com.example.bbtraveling.domain.repository

import com.example.bbtraveling.domain.UserSettings
import kotlinx.coroutines.flow.StateFlow

interface UserSettingsRepository {
    val settings: StateFlow<UserSettings>

    fun readStoredLanguageTag(): String
    fun hasAcceptedTerms(): Boolean
    fun updateUsername(value: String)
    fun updateDateOfBirth(value: String)
    fun updateDarkMode(enabled: Boolean)
    fun updateLanguage(languageTag: String)
    fun setTermsAccepted(accepted: Boolean)
}
