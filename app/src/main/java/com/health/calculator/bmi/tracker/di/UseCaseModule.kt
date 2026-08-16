package com.health.calculator.bmi.tracker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.health.calculator.bmi.tracker.domain.usecases.MilestoneEvaluationUseCase
import com.health.calculator.bmi.tracker.data.repository.MilestonesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideMilestoneEvaluationUseCase(
        milestonesRepository: MilestonesRepository
    ): MilestoneEvaluationUseCase {
        return MilestoneEvaluationUseCase(milestonesRepository)
    }
}
