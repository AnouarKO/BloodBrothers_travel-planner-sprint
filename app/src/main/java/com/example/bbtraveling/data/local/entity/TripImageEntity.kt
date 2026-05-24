package com.example.bbtraveling.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "trip_images",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["login"],
            childColumns = ["ownerLogin"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId"), Index("ownerLogin")]
)
data class TripImageEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val ownerLogin: String,
    val uri: String,
    val title: String,
    val createdAt: LocalDateTime
)

