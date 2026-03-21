package com.example.bbtraveling.domain

import java.time.LocalDate
import java.time.LocalTime

data class ActivityDraft(
    val title: String,
    val description: String,
    val date: LocalDate?,
    val time: LocalTime?,
    val category: ActivityCategory,
    val costEur: Double?
)
