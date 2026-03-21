package com.example.bbtraveling.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Trip(
    val id: String,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val description: String,
    val destination: String,
    val status: TripStatus,
    val accommodation: String,
    val transport: String,
    val travelers: Int,
    val budgetEur: Double,
    val activities: List<Activity>,
    val photos: List<Photo>
) {
    val spentEur: Double get() = activities.sumOf { it.costEur }
    val remainingEur: Double get() = budgetEur - spentEur

    fun isOverBudget(): Boolean = spentEur > budgetEur

    fun averageActivityCost(): Double = if (activities.isEmpty()) 0.0 else spentEur / activities.size

    fun projectedDailyBudget(totalDays: Int): Double = if (totalDays <= 0) 0.0 else budgetEur / totalDays

    fun isDateRangeValid(): Boolean = startDate.isBefore(endDate)

    fun containsDate(date: LocalDate): Boolean = !date.isBefore(startDate) && !date.isAfter(endDate)

    fun rescheduleActivities(newStartDate: LocalDate): List<Activity> {
        val dayOffset = ChronoUnit.DAYS.between(startDate, newStartDate)
        return activities.map { activity ->
            activity.copy(date = activity.date.plusDays(dayOffset))
        }
    }
}
