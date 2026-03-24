package com.health.calculator.bmi.tracker.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.health.calculator.bmi.tracker.core.constants.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Extension property to create DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppConstants.DATASTORE_NAME
)

/**
 * Manages app-level preferences using DataStore.
 * Will be expanded in later prompts with full profile data.
 */
class UserPreferences(private val context: Context) {

    companion object {
        // Onboarding
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // Theme
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "system", "light", "dark"

        // Units
        val KEY_USE_METRIC = booleanPreferencesKey("use_metric_system")

        // Profile setup
        val KEY_PROFILE_SETUP_COMPLETE = booleanPreferencesKey("profile_setup_complete")
    }

    private val dataStore = context.dataStore

    // ── Onboarding ───────────────────────────────────────────────────

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    // ── Theme ────────────────────────────────────────────────────────

    val themeMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_THEME_MODE] ?: "system"
        }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    // ── Units ────────────────────────────────────────────────────────

    val useMetricSystem: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_USE_METRIC] ?: true
        }

    suspend fun setUseMetricSystem(useMetric: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_USE_METRIC] = useMetric
        }
    }

    // ── Profile ──────────────────────────────────────────────────────

    val isProfileSetupComplete: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_PROFILE_SETUP_COMPLETE] ?: false
        }

    suspend fun setProfileSetupComplete(complete: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_PROFILE_SETUP_COMPLETE] = complete
        }
    }
}
