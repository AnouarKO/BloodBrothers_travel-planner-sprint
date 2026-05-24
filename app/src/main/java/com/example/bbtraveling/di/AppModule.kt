package com.example.bbtraveling.di

import android.content.Context
import androidx.room.Room
import com.example.bbtraveling.data.local.TravelDatabase
import com.example.bbtraveling.data.local.dao.AccessLogDao
import com.example.bbtraveling.data.local.dao.HotelReservationDao
import com.example.bbtraveling.data.local.dao.ItineraryItemDao
import com.example.bbtraveling.data.local.dao.TripImageDao
import com.example.bbtraveling.data.local.dao.TripDao
import com.example.bbtraveling.data.local.dao.UserProfileDao
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideTravelDatabase(
        @ApplicationContext context: Context
    ): TravelDatabase {
        return Room.databaseBuilder(
            context,
            TravelDatabase::class.java,
            "bbtraveling.db"
        ).fallbackToDestructiveMigration(false).build()
    }

    @Provides
    fun provideTripDao(database: TravelDatabase): TripDao = database.tripDao()

    @Provides
    fun provideItineraryItemDao(database: TravelDatabase): ItineraryItemDao = database.itineraryItemDao()

    @Provides
    fun provideUserProfileDao(database: TravelDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideAccessLogDao(database: TravelDatabase): AccessLogDao = database.accessLogDao()

    @Provides
    fun provideHotelReservationDao(database: TravelDatabase): HotelReservationDao = database.hotelReservationDao()

    @Provides
    fun provideTripImageDao(database: TravelDatabase): TripImageDao = database.tripImageDao()
}
