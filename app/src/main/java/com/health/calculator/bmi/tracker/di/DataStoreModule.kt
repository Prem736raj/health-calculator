package com.health.calculator.bmi.tracker.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideProfileDataStore(@ApplicationContext context: Context): ProfileDataStore {
        return ProfileDataStore(context)
    }
}
