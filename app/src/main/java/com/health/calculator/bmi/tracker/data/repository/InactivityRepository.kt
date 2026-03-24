// app/src/main/java/com/health/calculator/bmi/tracker/data/repository/InactivityRepository.kt
package com.health.calculator.bmi.tracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.health.calculator.bmi.tracker.data.models.InactivityState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.inactivityDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "inactivity_preferences"
)

class InactivityRepository(private val context: Context) {

    private object Keys {
        val LAST_APP_OPEN = longPreferencesKey("last_app_open_time")
        val LAST_ACTIVITY = longPreferencesKey("last_activity_time")
        val LAST_NOTIFICATION_LEVEL = intPreferencesKey("last_inactivity_notif_level")
        val INACTIVITY_NOTIFS_ENABLED = booleanPreferencesKey("inactivity_notifs_enabled")
        val STREAK_PROTECTION_ENABLED = booleanPreferencesKey("streak_protection_enabled")
        val HAS_SEEN_WELCOME_BACK = booleanPreferencesKey("has_seen_welcome_back")
        val STREAK_FREEZE_COUNT = intPreferencesKey("streak_freeze_count")
        val STREAK_FREEZE_LAST_USED = longPreferencesKey("streak_freeze_last_used")
        val WATER_STREAK_BEFORE_BREAK = intPreferencesKey("water_streak_before_break")
        val TRACKING_STREAK_BEFORE_BREAK = intPreferencesKey("tracking_streak_before_break")
    }

    fun getInactivityState(): Flow<InactivityState> {
        return context.inactivityDataStore.data.map { prefs ->
            val lastOpen = prefs[Keys.LAST_APP_OPEN] ?: System.currentTimeMillis()
            val now = System.currentTimeMillis()
            val daysInactive = ((now - lastOpen) / (24 * 60 * 60 * 1000)).toInt()

            InactivityState(
                lastAppOpenTime = lastOpen,
                lastActivityTime = prefs[Keys.LAST_ACTIVITY] ?: lastOpen,
                daysInactive = daysInactive,
                lastInactivityNotificationLevel = prefs[Keys.LAST_NOTIFICATION_LEVEL] ?: 0,
                inactivityNotificationsEnabled = prefs[Keys.INACTIVITY_NOTIFS_ENABLED] ?: true,
                streakProtectionEnabled = prefs[Keys.STREAK_PROTECTION_ENABLED] ?: true,
                hasSeenWelcomeBack = prefs[Keys.HAS_SEEN_WELCOME_BACK] ?: true
            )
        }
    }

    suspend fun recordAppOpened() {
        context.inactivityDataStore.edit { prefs ->
            prefs[Keys.LAST_APP_OPEN] = System.currentTimeMillis()
            prefs[Keys.LAST_ACTIVITY] = System.currentTimeMillis()
        }
    }

    suspend fun recordActivity() {
        context.inactivityDataStore.edit { prefs ->
            prefs[Keys.LAST_ACTIVITY] = System.currentTimeMillis()
        }
    }

    suspend fun updateNotificationLevel(level: Int) {
        context.inactivityDataStore.edit { prefs ->
            prefs[Keys.LAST_NOTIFICATION_LEVEL] = level
        }
    }

    suspend fun resetNotificationLevel() {
        context.inactivityDataStore.edit { prefs ->
            prefs[Keys.LAST_NOTIFICATION_LEVEL] = 0
        }
    }

    suspend fun setWelcomeBackSeen(seen: Boolean) {
        context.inactivityDataStore.edit { prefs ->
            prefs[Keys.HAS_SEEN_WELCOME_BACK] = seen
        }
    }

    suspend fun setInactivityNotificationsEnabled(enabled: Boolean) {
        context.inactivityDataStore.edit { prefs ->
            prefs[Keys.INACTIVITY_NOTIFS_ENABLED] = enabled
        }
    }

    suspend fun setStreakProtectionEnabled(enabled: Boolean) {
        context.inactivityDataStore.edit { prefs ->
            prefs[Keys.STREAK_PROTECTION_ENABLED] = enabled
        }
    }

    // Streak freeze
    fun getStreakFreezeCount(): Flow<Int> {
        return context.inactivityDataStore.data.map { prefs ->
            prefs[Keys.STREAK_FREEZE_COUNT] ?: 1 // Start with 1 free freeze
        }
    }

    suspend fun useStreakFreeze(): Boolean {
        var success = false
        context.inactivityDataStore.edit { prefs ->
            val count = prefs[Keys.STREAK_FREEZE_COUNT] ?: 1
            if (count > 0) {
                prefs[Keys.STREAK_FREEZE_COUNT] = count - 1
                prefs[Keys.STREAK_FREEZE_LAST_USED] = System.currentTimeMillis()
                success = true
            }
        }
        return success
    }

    suspend fun addStreakFreeze(count: Int = 1) {
        context.inactivityDataStore.edit { prefs ->
            val current = prefs[Keys.STREAK_FREEZE_COUNT] ?: 0
            prefs[Keys.STREAK_FREEZE_COUNT] = (current + count).coerceAtMost(5)
        }
    }

    suspend fun saveStreakBeforeBreak(waterStreak: Int, trackingStreak: Int) {
        context.inactivityDataStore.edit { prefs ->
            prefs[Keys.WATER_STREAK_BEFORE_BREAK] = waterStreak
            prefs[Keys.TRACKING_STREAK_BEFORE_BREAK] = trackingStreak
        }
    }

    fun getStreakBeforeBreak(): Flow<Pair<Int, Int>> {
        return context.inactivityDataStore.data.map { prefs ->
            Pair(
                prefs[Keys.WATER_STREAK_BEFORE_BREAK] ?: 0,
                prefs[Keys.TRACKING_STREAK_BEFORE_BREAK] ?: 0
            )
        }
    }

    suspend fun markNeedsWelcomeBack() {
        context.inactivityDataStore.edit { prefs ->
            prefs[Keys.HAS_SEEN_WELCOME_BACK] = false
        }
    }

    fun getLastAppOpenTime(): Long {
        return context.getSharedPreferences("inactivity_quick", Context.MODE_PRIVATE)
            .getLong("last_open", System.currentTimeMillis())
    }

    fun saveLastAppOpenTimeQuick() {
        context.getSharedPreferences("inactivity_quick", Context.MODE_PRIVATE)
            .edit()
            .putLong("last_open", System.currentTimeMillis())
            .apply()
    }
}
