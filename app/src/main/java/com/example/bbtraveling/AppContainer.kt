package com.example.bbtraveling

import android.content.Context
import com.example.bbtraveling.data.datasource.FakeTripDataSource
import com.example.bbtraveling.data.repository.TripRepositoryImpl
import com.example.bbtraveling.data.settings.SharedPreferencesSettingsRepository
import com.example.bbtraveling.domain.repository.TripRepository
import com.example.bbtraveling.domain.repository.UserSettingsRepository

// Aqui se montan los repositorios que usa la app
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val fakeTripDataSource: FakeTripDataSource by lazy {
        FakeTripDataSource()
    }

    val tripRepository: TripRepository by lazy {
        TripRepositoryImpl(dataSource = fakeTripDataSource)
    }

    val userSettingsRepository: UserSettingsRepository by lazy {
        SharedPreferencesSettingsRepository(appContext)
    }
}
