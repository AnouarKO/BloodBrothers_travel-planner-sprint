package com.example.bbtraveling.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class TripTest {

    private val trip = Trip(
        id = "trip-test",
        title = "Test Trip",
        startDate = LocalDate.of(2026, 1, 1),
        endDate = LocalDate.of(2026, 1, 3),
        description = "Summary",
        destination = "Test City",
        status = TripStatus.Planning,
        accommodation = "Hotel",
        transport = "Flight",
        travelers = 2,
        budgetEur = 100.0,
        activities = listOf(
            Activity(
                id = "a1",
                title = "Museum",
                description = "Center visit",
                date = LocalDate.of(2026, 1, 1),
                time = LocalTime.of(10, 0),
                category = ActivityCategory.Museum,
                costEur = 24.0
            ),
            Activity(
                id = "a2",
                title = "Lunch",
                description = "Old town lunch",
                date = LocalDate.of(2026, 1, 1),
                time = LocalTime.of(14, 0),
                category = ActivityCategory.Restaurant,
                costEur = 16.0
            )
        ),
        photos = emptyList()
    )

    @Test
    fun spentEur_isCalculatedFromActivities() {
        assertEquals(40.0, trip.spentEur, 0.001)
    }

    @Test
    fun remainingEur_isBudgetMinusSpent() {
        assertEquals(60.0, trip.remainingEur, 0.001)
    }

    @Test
    fun isOverBudget_returnsFalseWhenSpentBelowBudget() {
        assertFalse(trip.isOverBudget())
    }

    @Test
    fun averageActivityCost_returnsExpectedValue() {
        assertEquals(20.0, trip.averageActivityCost(), 0.001)
    }

    @Test
    fun projectedDailyBudget_handlesPositiveAndZeroDays() {
        assertEquals(50.0, trip.projectedDailyBudget(totalDays = 2), 0.001)
        assertEquals(0.0, trip.projectedDailyBudget(totalDays = 0), 0.001)
    }

    @Test
    fun isOverBudget_returnsTrueWhenSpentExceedsBudget() {
        val overBudgetTrip = trip.copy(
            budgetEur = 30.0
        )
        assertTrue(overBudgetTrip.isOverBudget())
    }
}
