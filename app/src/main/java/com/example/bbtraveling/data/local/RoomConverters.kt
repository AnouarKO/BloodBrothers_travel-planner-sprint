package com.example.bbtraveling.data.local

import androidx.room.TypeConverter
import com.example.bbtraveling.domain.ActivityCategory
import com.example.bbtraveling.domain.TripStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RoomConverters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun fromTripStatus(value: TripStatus?): String? = value?.name

    @TypeConverter
    fun toTripStatus(value: String?): TripStatus? = value?.let(TripStatus::valueOf)

    @TypeConverter
    fun fromActivityCategory(value: ActivityCategory?): String? = value?.name

    @TypeConverter
    fun toActivityCategory(value: String?): ActivityCategory? = value?.let(ActivityCategory::valueOf)

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.joinToString(separator = "||")

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return value
            ?.takeIf { it.isNotBlank() }
            ?.split("||")
            .orEmpty()
    }
}
