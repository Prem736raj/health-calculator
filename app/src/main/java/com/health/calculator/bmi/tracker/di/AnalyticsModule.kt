package com.health.calculator.bmi.tracker.di

import com.health.calculator.bmi.tracker.data.analytics.FirebaseProductAnalytics
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideProductAnalytics(
        firebaseProductAnalytics: FirebaseProductAnalytics
    ): ProductAnalytics = firebaseProductAnalytics
}

/** Entry point for composables that are not Hilt-injected classes. */
@EntryPoint
@dagger.hilt.InstallIn(SingletonComponent::class)
interface AnalyticsEntryPoint {
    fun productAnalytics(): ProductAnalytics
}
