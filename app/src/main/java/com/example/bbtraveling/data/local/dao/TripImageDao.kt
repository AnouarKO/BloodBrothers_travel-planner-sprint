package com.example.bbtraveling.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.bbtraveling.data.local.entity.TripImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripImageDao {

    @Query("SELECT * FROM trip_images WHERE tripId = :tripId ORDER BY createdAt DESC")
    fun observeImagesForTrip(tripId: String): Flow<List<TripImageEntity>>

    @Upsert
    suspend fun upsertImage(image: TripImageEntity)

    @Query("DELETE FROM trip_images WHERE id = :imageId")
    suspend fun deleteImage(imageId: String)
}

