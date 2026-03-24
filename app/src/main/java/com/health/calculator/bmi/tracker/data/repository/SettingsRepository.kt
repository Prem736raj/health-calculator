package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore
import com.health.calculator.bmi.tracker.data.model.SettingsData
import com.health.calculator.bmi.tracker.data.model.ThemeMode
import com.health.calculator.bmi.tracker.data.model.UnitSystem
import kotlinx.coroutines.flow.Flow

/**
 * Repository layer for settings data access.
 * Provides a clean API for the ViewModel to interact with settings storage.
 */
class SettingsRepository(
    private val settingsDataStore: SettingsDataStore
) {
    /** Observable stream of all settings */
    val settingsFlow: Flow<SettingsData> = settingsDataStore.settingsFlow

    /** Observable stream of just the theme mode */
    val themeModeFlow: Flow<ThemeMode> = settingsDataStore.themeModeFlow

    /** Save complete settings */
    suspend fun saveSettings(settings: SettingsData) {
        settingsDataStore.saveSettings(settings)
    }

    /** Update only theme mode for instant switching */
    suspend fun updateThemeMode(mode: ThemeMode) {
        settingsDataStore.updateThemeMode(mode)
    }

    /** Update only unit system */
    suspend fun updateUnitSystem(system: UnitSystem) {
        settingsDataStore.updateUnitSystem(system)
    }

    /** Update individual notification toggles */
    suspend fun updateReminderSetting(
        remindersEnabled: Boolean? = null,
        waterReminder: Boolean? = null,
        weightReminder: Boolean? = null
    ) {
        settingsDataStore.updateReminderSetting(
            remindersEnabled = remindersEnabled,
            waterReminder = waterReminder,
            weightReminder = weightReminder
        )
    }

    /** Reset all settings to defaults */
    suspend fun clearSettings() {
        settingsDataStore.clearSettings()
    }
}
