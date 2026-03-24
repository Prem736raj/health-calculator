package com.health.calculator.bmi.tracker

import android.app.Application
import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.ThemeMode
import com.health.calculator.bmi.tracker.data.repository.*
import kotlinx.coroutines.flow.Flow

/**
 * Custom Application class that provides app-wide access to theme settings and repositories.
 */
class HealthCalculatorApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    /** DataStore instances */
    val settingsDataStore: SettingsDataStore by lazy { SettingsDataStore(this) }
    val profileDataStore: ProfileDataStore by lazy { ProfileDataStore(this) }

    /** Repositories */
    val profileRepository: ProfileRepository by lazy { ProfileRepository(profileDataStore) }
    val historyRepository: HistoryRepository by lazy { HistoryRepository(database.historyDao()) }
    val waterIntakeRepository: WaterIntakeRepository by lazy { WaterIntakeRepository(database.waterIntakeDao()) }
    val waterGamificationRepository: WaterGamificationRepository by lazy { WaterGamificationRepository(database.waterGamificationDao()) }
    val foodLogRepository: FoodLogRepository by lazy { FoodLogRepository(this) }
    val weightRepository: WeightRepository by lazy { WeightRepository(database.weightDao()) }
    val familyProfileRepository: FamilyProfileRepository by lazy {
        FamilyProfileRepository(database.familyProfileDao(), profileRepository)
    }
    val weightReminderManager: com.health.calculator.bmi.tracker.notifications.WeightReminderManager by lazy {
        com.health.calculator.bmi.tracker.notifications.WeightReminderManager(this)
    }
    
    val healthOverviewRepository: HealthOverviewRepository by lazy {
        HealthOverviewRepository(
            historyRepository = historyRepository,
            waterGamificationRepository = waterGamificationRepository,
            foodLogRepository = foodLogRepository
        )
    }

    val milestonesRepository: MilestonesRepository by lazy {
        MilestonesRepository(database.milestonesDao(), historyRepository)
    }

    val milestoneEvaluationUseCase: com.health.calculator.bmi.tracker.domain.usecases.MilestoneEvaluationUseCase by lazy {
        com.health.calculator.bmi.tracker.domain.usecases.MilestoneEvaluationUseCase(milestonesRepository)
    }

    val reminderRepository: ReminderRepository by lazy {
        ReminderRepository(database.reminderDao(), this)
    }

    val weeklyReportDao by lazy { database.weeklyReportDao() }

    /**
     * Flow of the current theme mode.
     */
    val themeModeFlow: Flow<ThemeMode> by lazy {
        settingsDataStore.themeModeFlow
    }

    /**
     * Flow of whether onboarding has been completed.
     */
    val onboardingCompletedFlow: Flow<Boolean> by lazy {
        settingsDataStore.onboardingCompletedFlow
    }
}
