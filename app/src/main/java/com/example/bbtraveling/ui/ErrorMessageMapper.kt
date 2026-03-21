package com.example.bbtraveling.ui

import androidx.annotation.StringRes
import com.example.bbtraveling.R
import com.example.bbtraveling.domain.validation.TravelValidator

// Pasa codigos de error a textos de la interfaz
@StringRes
fun errorMessageResId(errorCode: String): Int {
    return when (errorCode) {
        TravelValidator.ERROR_TITLE_REQUIRED -> R.string.error_title_required
        TravelValidator.ERROR_DESCRIPTION_REQUIRED -> R.string.error_description_required
        TravelValidator.ERROR_CITY_REQUIRED -> R.string.error_city_required
        TravelValidator.ERROR_COUNTRY_REQUIRED -> R.string.error_country_required
        TravelValidator.ERROR_START_DATE_REQUIRED -> R.string.error_start_date_required
        TravelValidator.ERROR_END_DATE_REQUIRED -> R.string.error_end_date_required
        TravelValidator.ERROR_START_DATE_FUTURE -> R.string.error_start_date_future
        TravelValidator.ERROR_END_DATE_FUTURE -> R.string.error_end_date_future
        TravelValidator.ERROR_START_DATE_BEFORE_END -> R.string.error_start_date_before_end
        TravelValidator.ERROR_END_DATE_AFTER_START -> R.string.error_end_date_after_start
        TravelValidator.ERROR_DATE_REQUIRED -> R.string.error_date_required
        TravelValidator.ERROR_TIME_REQUIRED -> R.string.error_time_required
        TravelValidator.ERROR_BUDGET_REQUIRED -> R.string.error_budget_required
        TravelValidator.ERROR_BUDGET_NEGATIVE -> R.string.error_budget_negative
        TravelValidator.ERROR_COST_REQUIRED -> R.string.error_cost_required
        TravelValidator.ERROR_COST_NEGATIVE -> R.string.error_cost_negative
        TravelValidator.ERROR_ACTIVITY_DATE_FUTURE -> R.string.error_activity_date_future
        TravelValidator.ERROR_ACTIVITY_DATE_WITHIN_TRIP -> R.string.error_activity_date_within_trip
        TravelValidator.ERROR_TRIP_HAS_ACTIVITY_OUTSIDE_RANGE -> R.string.error_trip_has_activity_outside_range
        TravelValidator.ERROR_TRIP_NOT_FOUND -> R.string.error_trip_not_found
        TravelValidator.ERROR_ACTIVITY_NOT_FOUND -> R.string.error_activity_not_found
        else -> R.string.error_generic
    }
}
