package com.health.calculator.bmi.tracker.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import com.health.calculator.bmi.tracker.data.local.AppDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideHistoryDao(database: AppDatabase) = database.historyDao()

    @Provides
    fun provideBloodPressureDao(database: AppDatabase) = database.bloodPressureDao()

    @Provides
    fun provideWaterIntakeDao(database: AppDatabase) = database.waterIntakeDao()

    @Provides
    fun provideWaterGamificationDao(database: AppDatabase) = database.waterGamificationDao()

    @Provides
    fun provideUrineColorDao(database: AppDatabase) = database.urineColorDao()

    @Provides
    fun provideFavoriteQuoteDao(database: AppDatabase) = database.favoriteQuoteDao()

    @Provides
    fun provideWeightDao(database: AppDatabase) = database.weightDao()

    @Provides
    fun provideStepHistoryDao(database: AppDatabase) = database.stepHistoryDao()

    @Provides
    fun provideFamilyProfileDao(database: AppDatabase) = database.familyProfileDao()

    @Provides
    fun provideMilestonesDao(database: AppDatabase) = database.milestonesDao()

    @Provides
    fun provideReminderDao(database: AppDatabase) = database.reminderDao()

    @Provides
    fun provideWeeklyReportDao(database: AppDatabase) = database.weeklyReportDao()

    @Provides
    fun provideChatDao(database: AppDatabase) = database.chatDao()
}
