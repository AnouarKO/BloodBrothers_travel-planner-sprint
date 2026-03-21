package com.example.bbtraveling.domain.validation

import com.example.bbtraveling.domain.Activity
import com.example.bbtraveling.domain.ActivityDraft
import com.example.bbtraveling.domain.Trip
import com.example.bbtraveling.domain.TripDraft
import java.time.LocalDate

// Junta validaciones para que UI y repo comprueben lo mismo
object TravelValidator {
    const val FIELD_TITLE = "title"
    const val FIELD_DESCRIPTION = "description"
    const val FIELD_CITY = "city"
    const val FIELD_COUNTRY = "country"
    const val FIELD_START_DATE = "startDate"
    const val FIELD_END_DATE = "endDate"
    const val FIELD_DATE = "date"
    const val FIELD_TIME = "time"
    const val FIELD_BUDGET = "budget"
    const val FIELD_COST = "cost"

    const val ERROR_TITLE_REQUIRED = "error_title_required"
    const val ERROR_DESCRIPTION_REQUIRED = "error_description_required"
    const val ERROR_CITY_REQUIRED = "error_city_required"
    const val ERROR_COUNTRY_REQUIRED = "error_country_required"
    const val ERROR_START_DATE_REQUIRED = "error_start_date_required"
    const val ERROR_END_DATE_REQUIRED = "error_end_date_required"
    const val ERROR_START_DATE_FUTURE = "error_start_date_future"
    const val ERROR_END_DATE_FUTURE = "error_end_date_future"
    const val ERROR_START_DATE_BEFORE_END = "error_start_date_before_end"
    const val ERROR_END_DATE_AFTER_START = "error_end_date_after_start"
    const val ERROR_DATE_REQUIRED = "error_date_required"
    const val ERROR_TIME_REQUIRED = "error_time_required"
    const val ERROR_BUDGET_REQUIRED = "error_budget_required"
    const val ERROR_BUDGET_NEGATIVE = "error_budget_negative"
    const val ERROR_COST_REQUIRED = "error_cost_required"
    const val ERROR_COST_NEGATIVE = "error_cost_negative"
    const val ERROR_ACTIVITY_DATE_FUTURE = "error_activity_date_future"
    const val ERROR_ACTIVITY_DATE_WITHIN_TRIP = "error_activity_date_within_trip"
    const val ERROR_TRIP_HAS_ACTIVITY_OUTSIDE_RANGE = "error_trip_has_activity_outside_range"
    const val ERROR_TRIP_NOT_FOUND = "error_trip_not_found"
    const val ERROR_ACTIVITY_NOT_FOUND = "error_activity_not_found"

    fun validateTripDraft(
        draft: TripDraft,
        today: LocalDate,
        existingActivities: List<Activity> = emptyList()
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (draft.title.isBlank()) errors[FIELD_TITLE] = ERROR_TITLE_REQUIRED
        if (draft.description.isBlank()) errors[FIELD_DESCRIPTION] = ERROR_DESCRIPTION_REQUIRED
        if (draft.city.isBlank()) errors[FIELD_CITY] = ERROR_CITY_REQUIRED
        if (draft.country.isBlank()) errors[FIELD_COUNTRY] = ERROR_COUNTRY_REQUIRED
        if (draft.startDate == null) errors[FIELD_START_DATE] = ERROR_START_DATE_REQUIRED
        if (draft.endDate == null) errors[FIELD_END_DATE] = ERROR_END_DATE_REQUIRED
        if (draft.budgetEur == null) errors[FIELD_BUDGET] = ERROR_BUDGET_REQUIRED

        if (draft.startDate != null && !draft.startDate.isAfter(today)) {
            errors[FIELD_START_DATE] = ERROR_START_DATE_FUTURE
        }
        if (draft.endDate != null && !draft.endDate.isAfter(today)) {
            errors[FIELD_END_DATE] = ERROR_END_DATE_FUTURE
        }
        if (draft.budgetEur != null && draft.budgetEur < 0.0) {
            errors[FIELD_BUDGET] = ERROR_BUDGET_NEGATIVE
        }
        if (draft.startDate != null && draft.endDate != null && !draft.startDate.isBefore(draft.endDate)) {
            errors[FIELD_START_DATE] = ERROR_START_DATE_BEFORE_END
            errors[FIELD_END_DATE] = ERROR_END_DATE_AFTER_START
        }
        if (draft.startDate != null && draft.endDate != null) {
            if (existingActivities.any { it.date.isBefore(draft.startDate) || it.date.isAfter(draft.endDate) }) {
                errors[FIELD_DATE] = ERROR_TRIP_HAS_ACTIVITY_OUTSIDE_RANGE
            }
        }

        return errors
    }

    fun validateActivityDraft(
        draft: ActivityDraft,
        trip: Trip,
        today: LocalDate
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (draft.title.isBlank()) errors[FIELD_TITLE] = ERROR_TITLE_REQUIRED
        if (draft.description.isBlank()) errors[FIELD_DESCRIPTION] = ERROR_DESCRIPTION_REQUIRED
        if (draft.date == null) errors[FIELD_DATE] = ERROR_DATE_REQUIRED
        if (draft.time == null) errors[FIELD_TIME] = ERROR_TIME_REQUIRED
        if (draft.costEur == null) errors[FIELD_COST] = ERROR_COST_REQUIRED

        if (draft.date != null && !draft.date.isAfter(today)) {
            errors[FIELD_DATE] = ERROR_ACTIVITY_DATE_FUTURE
        }
        if (draft.date != null && !trip.containsDate(draft.date)) {
            errors[FIELD_DATE] = ERROR_ACTIVITY_DATE_WITHIN_TRIP
        }
        if (draft.costEur != null && draft.costEur < 0.0) {
            errors[FIELD_COST] = ERROR_COST_NEGATIVE
        }

        return errors
    }
}
