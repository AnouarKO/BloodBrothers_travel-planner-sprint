package com.example.bbtraveling.domain

import java.time.LocalDate

data class TripDraft(
    val title: String,
    val description: String,
    val city: String,
    val country: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val status: TripStatus,
    val budgetEur: Double?
)
