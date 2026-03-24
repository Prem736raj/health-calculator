package com.health.calculator.bmi.tracker.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.health.calculator.bmi.tracker.data.model.SettingsData
import com.health.calculator.bmi.tracker.data.model.ThemeMode
import com.health.calculator.bmi.tracker.data.model.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Singleton DataStore instance for settings.
 * Separate from profile DataStore to keep concerns isolated.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "health_calculator_settings"
)

/**
 * Handles reading and writing app settings to DataStore.
 * Each setting is stored as an individual preference key
 * for granular updates without full serialization.
 */
class SettingsDataStore(private val context: Context) {

    // ─── Preference Keys ──────────────────────────────────────────────────
    private companion object {
        val KEY_UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val KEY_WATER_REMINDER = booleanPreferencesKey("water_reminder_enabled")
        val KEY_WEIGHT_REMINDER = booleanPreferencesKey("weight_reminder_enabled")
        val KEY_LAST_UPDATED = longPreferencesKey("settings_last_updated")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    // ─── Read Settings ────────────────────────────────────────────────────

    /**
     * Returns a Flow of the complete SettingsData, automatically emitting
     * updates whenever any setting changes.
     */
    val settingsFlow: Flow<SettingsData> = context.settingsDataStore.data.map { prefs ->
        SettingsData(
            unitSystem = prefs[KEY_UNIT_SYSTEM]?.let {
                try {
                    UnitSystem.valueOf(it)
                } catch (_: Exception) {
                    UnitSystem.METRIC
                }
            } ?: UnitSystem.METRIC,

            themeMode = prefs[KEY_THEME_MODE]?.let {
                try {
                    ThemeMode.valueOf(it)
                } catch (_: Exception) {
                    ThemeMode.SYSTEM
                }
            } ?: ThemeMode.SYSTEM,

            remindersEnabled = prefs[KEY_REMINDERS_ENABLED] ?: false,
            waterReminderEnabled = prefs[KEY_WATER_REMINDER] ?: false,
            weightReminderEnabled = prefs[KEY_WEIGHT_REMINDER] ?: false,
            lastUpdatedMillis = prefs[KEY_LAST_UPDATED] ?: System.currentTimeMillis()
        )
    }

    /**
     * Convenience flow that emits only the ThemeMode.
     * Used by the root composable to apply the theme without
     * recomposing on unrelated settings changes.
     */
    val themeModeFlow: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE]?.let {
            try {
                ThemeMode.valueOf(it)
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            }
        } ?: ThemeMode.SYSTEM
    }

    /**
     * Flow that emits whether the user has completed onboarding.
     * Used to decide whether to show onboarding on app launch.
     */
    val onboardingCompletedFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    /**
     * Marks onboarding as completed so it never shows again.
     */
    suspend fun setOnboardingCompleted() {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = true
        }
    }

    // ─── Write Settings ───────────────────────────────────────────────────

    /**
     * Saves all settings atomically.
     */
    suspend fun saveSettings(settings: SettingsData) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_UNIT_SYSTEM] = settings.unitSystem.name
            prefs[KEY_THEME_MODE] = settings.themeMode.name
            prefs[KEY_REMINDERS_ENABLED] = settings.remindersEnabled
            prefs[KEY_WATER_REMINDER] = settings.waterReminderEnabled
            prefs[KEY_WEIGHT_REMINDER] = settings.weightReminderEnabled
            prefs[KEY_LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Updates only the theme mode setting.
     * Optimized for immediate theme switching without touching other settings.
     */
    suspend fun updateThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
            prefs[KEY_LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Updates only the unit system setting.
     */
    suspend fun updateUnitSystem(system: UnitSystem) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_UNIT_SYSTEM] = system.name
            prefs[KEY_LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Updates a single notification toggle.
     */
    suspend fun updateReminderSetting(
        remindersEnabled: Boolean? = null,
        waterReminder: Boolean? = null,
        weightReminder: Boolean? = null
    ) {
        context.settingsDataStore.edit { prefs ->
            remindersEnabled?.let { prefs[KEY_REMINDERS_ENABLED] = it }
            waterReminder?.let { prefs[KEY_WATER_REMINDER] = it }
            weightReminder?.let { prefs[KEY_WEIGHT_REMINDER] = it }
            prefs[KEY_LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    /**
     * Resets all settings to default values.
     */
    suspend fun clearSettings() {
        context.settingsDataStore.edit { it.clear() }
    }
}
