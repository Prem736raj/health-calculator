// data/preferences/BpReminderPreferences.kt
package com.health.calculator.bmi.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bpDataStore: DataStore<Preferences> by preferencesDataStore(name = "bp_preferences")

data class BpReminderSettings(
    val morningReminderEnabled: Boolean = false,
    val morningReminderHour: Int = 7,
    val morningReminderMinute: Int = 0,
    val eveningReminderEnabled: Boolean = false,
    val eveningReminderHour: Int = 19,
    val eveningReminderMinute: Int = 0,
    val customReminderMessage: String = "Time to check your blood pressure! 🩺",
    val doctorReminderEnabled: Boolean = false,
    val doctorReminderTimestamp: Long = 0L,
    val doctorReminderNote: String = "",
    val onMedication: Boolean = false,
    val medicationName: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastMeasurementDate: String = "",
    val streakStartDate: String = "",
    val doctorVisitDismissedAt: Long = 0L,
    val consecutiveHypertensionCount: Int = 0
)

class BpReminderPreferences(private val context: Context) {

    companion object {
        private val MORNING_REMINDER_ENABLED = booleanPreferencesKey("bp_morning_reminder_enabled")
        private val MORNING_REMINDER_HOUR = intPreferencesKey("bp_morning_reminder_hour")
        private val MORNING_REMINDER_MINUTE = intPreferencesKey("bp_morning_reminder_minute")
        private val EVENING_REMINDER_ENABLED = booleanPreferencesKey("bp_evening_reminder_enabled")
        private val EVENING_REMINDER_HOUR = intPreferencesKey("bp_evening_reminder_hour")
        private val EVENING_REMINDER_MINUTE = intPreferencesKey("bp_evening_reminder_minute")
        private val CUSTOM_REMINDER_MESSAGE = stringPreferencesKey("bp_custom_reminder_message")
        private val DOCTOR_REMINDER_ENABLED = booleanPreferencesKey("bp_doctor_reminder_enabled")
        private val DOCTOR_REMINDER_TIMESTAMP = longPreferencesKey("bp_doctor_reminder_timestamp")
        private val DOCTOR_REMINDER_NOTE = stringPreferencesKey("bp_doctor_reminder_note")
        private val ON_MEDICATION = booleanPreferencesKey("bp_on_medication")
        private val MEDICATION_NAME = stringPreferencesKey("bp_medication_name")
        private val CURRENT_STREAK = intPreferencesKey("bp_current_streak")
        private val LONGEST_STREAK = intPreferencesKey("bp_longest_streak")
        private val LAST_MEASUREMENT_DATE = stringPreferencesKey("bp_last_measurement_date")
        private val STREAK_START_DATE = stringPreferencesKey("bp_streak_start_date")
        private val DOCTOR_VISIT_DISMISSED_AT = longPreferencesKey("bp_doctor_visit_dismissed_at")
        private val CONSECUTIVE_HYPERTENSION = intPreferencesKey("bp_consecutive_hypertension")
    }

    val settingsFlow: Flow<BpReminderSettings> = context.bpDataStore.data.map { prefs ->
        BpReminderSettings(
            morningReminderEnabled = prefs[MORNING_REMINDER_ENABLED] ?: false,
            morningReminderHour = prefs[MORNING_REMINDER_HOUR] ?: 7,
            morningReminderMinute = prefs[MORNING_REMINDER_MINUTE] ?: 0,
            eveningReminderEnabled = prefs[EVENING_REMINDER_ENABLED] ?: false,
            eveningReminderHour = prefs[EVENING_REMINDER_HOUR] ?: 19,
            eveningReminderMinute = prefs[EVENING_REMINDER_MINUTE] ?: 0,
            customReminderMessage = prefs[CUSTOM_REMINDER_MESSAGE]
                ?: "Time to check your blood pressure! 🩺",
            doctorReminderEnabled = prefs[DOCTOR_REMINDER_ENABLED] ?: false,
            doctorReminderTimestamp = prefs[DOCTOR_REMINDER_TIMESTAMP] ?: 0L,
            doctorReminderNote = prefs[DOCTOR_REMINDER_NOTE] ?: "",
            onMedication = prefs[ON_MEDICATION] ?: false,
            medicationName = prefs[MEDICATION_NAME] ?: "",
            currentStreak = prefs[CURRENT_STREAK] ?: 0,
            longestStreak = prefs[LONGEST_STREAK] ?: 0,
            lastMeasurementDate = prefs[LAST_MEASUREMENT_DATE] ?: "",
            streakStartDate = prefs[STREAK_START_DATE] ?: "",
            doctorVisitDismissedAt = prefs[DOCTOR_VISIT_DISMISSED_AT] ?: 0L,
            consecutiveHypertensionCount = prefs[CONSECUTIVE_HYPERTENSION] ?: 0
        )
    }

    suspend fun updateMorningReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.bpDataStore.edit { prefs ->
            prefs[MORNING_REMINDER_ENABLED] = enabled
            prefs[MORNING_REMINDER_HOUR] = hour
            prefs[MORNING_REMINDER_MINUTE] = minute
        }
    }

    suspend fun updateEveningReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.bpDataStore.edit { prefs ->
            prefs[EVENING_REMINDER_ENABLED] = enabled
            prefs[EVENING_REMINDER_HOUR] = hour
            prefs[EVENING_REMINDER_MINUTE] = minute
        }
    }

    suspend fun updateReminderMessage(message: String) {
        context.bpDataStore.edit { prefs ->
            prefs[CUSTOM_REMINDER_MESSAGE] = message
        }
    }

    suspend fun updateMedication(onMedication: Boolean, name: String = "") {
        context.bpDataStore.edit { prefs ->
            prefs[ON_MEDICATION] = onMedication
            prefs[MEDICATION_NAME] = name
        }
    }

    suspend fun updateStreak(current: Int, longest: Int, lastDate: String, startDate: String) {
        context.bpDataStore.edit { prefs ->
            prefs[CURRENT_STREAK] = current
            prefs[LONGEST_STREAK] = longest
            prefs[LAST_MEASUREMENT_DATE] = lastDate
            prefs[STREAK_START_DATE] = startDate
        }
    }

    suspend fun updateConsecutiveHypertension(count: Int) {
        context.bpDataStore.edit { prefs ->
            prefs[CONSECUTIVE_HYPERTENSION] = count
        }
    }

    suspend fun updateDoctorReminder(enabled: Boolean, timestamp: Long, note: String) {
        context.bpDataStore.edit { prefs ->
            prefs[DOCTOR_REMINDER_ENABLED] = enabled
            prefs[DOCTOR_REMINDER_TIMESTAMP] = timestamp
            prefs[DOCTOR_REMINDER_NOTE] = note
        }
    }

    suspend fun dismissDoctorVisitSuggestion() {
        context.bpDataStore.edit { prefs ->
            prefs[DOCTOR_VISIT_DISMISSED_AT] = System.currentTimeMillis()
        }
    }
}
