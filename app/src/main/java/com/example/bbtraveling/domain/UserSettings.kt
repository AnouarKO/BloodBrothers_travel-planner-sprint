package com.example.bbtraveling.domain

data class UserSettings(
    val username: String = "",
    val dateOfBirth: String = "",
    val darkMode: Boolean = false,
    val languageTag: String = "en",
    val termsAccepted: Boolean = false
)
