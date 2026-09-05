// data/preferences/WaterReminderPreferences.kt
package com.health.calculator.bmi.tracker.data.preferences

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import android.content.SharedPreferences
import com.health.calculator.bmi.tracker.data.model.WaterReminderSettings

class WaterReminderPreferences(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("water_reminder_prefs", Context.MODE_PRIVATE)

    fun save(settings: WaterReminderSettings) {
        val safe = settings.copy(
            startHour = settings.startHour.coerceIn(0, 23),
            startMinute = settings.startMinute.coerceIn(0, 59),
            endHour = settings.endHour.coerceIn(0, 23),
            endMinute = settings.endMinute.coerceIn(0, 59),
            frequencyMinutes = settings.frequencyMinutes.coerceIn(15, 240)
        )
        prefs.edit().apply {
            putBoolean(KEY_ENABLED, safe.isEnabled)
            putInt(KEY_START_HOUR, safe.startHour)
            putInt(KEY_START_MINUTE, safe.startMinute)
            putInt(KEY_END_HOUR, safe.endHour)
            putInt(KEY_END_MINUTE, safe.endMinute)
            putInt(KEY_FREQUENCY, safe.frequencyMinutes)
            putBoolean(KEY_VIBRATION, safe.enableVibration)
            putBoolean(KEY_SOUND, safe.enableSound)
            putString(KEY_SOUND_URI, safe.soundUri)
            putBoolean(KEY_SMART_SKIP, safe.smartSkipEnabled)
            putBoolean(KEY_BEHIND_NUDGE, safe.behindScheduleNudge)
            apply()
        }
    }

    fun load(): WaterReminderSettings {
        return WaterReminderSettings(
            isEnabled = prefs.getBoolean(KEY_ENABLED, false),
            startHour = prefs.getInt(KEY_START_HOUR, 8).coerceIn(0, 23),
            startMinute = prefs.getInt(KEY_START_MINUTE, 0).coerceIn(0, 59),
            endHour = prefs.getInt(KEY_END_HOUR, 22).coerceIn(0, 23),
            endMinute = prefs.getInt(KEY_END_MINUTE, 0).coerceIn(0, 59),
            frequencyMinutes = prefs.getInt(KEY_FREQUENCY, 60).coerceIn(15, 240),
            enableVibration = prefs.getBoolean(KEY_VIBRATION, true),
            enableSound = prefs.getBoolean(KEY_SOUND, true),
            soundUri = prefs.getString(KEY_SOUND_URI, "default") ?: "default",
            smartSkipEnabled = prefs.getBoolean(KEY_SMART_SKIP, true),
            behindScheduleNudge = prefs.getBoolean(KEY_BEHIND_NUDGE, true)
        )
    }

    fun getLastLogTime(): Long = prefs.getLong(KEY_LAST_LOG_TIME, 0L)

    fun setLastLogTime(time: Long) {
        prefs.edit().putLong(KEY_LAST_LOG_TIME, time).apply()
    }

    companion object {
        private const val KEY_ENABLED = "reminder_enabled"
        private const val KEY_START_HOUR = "start_hour"
        private const val KEY_START_MINUTE = "start_minute"
        private const val KEY_END_HOUR = "end_hour"
        private const val KEY_END_MINUTE = "end_minute"
        private const val KEY_FREQUENCY = "frequency_minutes"
        private const val KEY_VIBRATION = "vibration"
        private const val KEY_SOUND = "sound"
        private const val KEY_SOUND_URI = "sound_uri"
        private const val KEY_SMART_SKIP = "smart_skip"
        private const val KEY_BEHIND_NUDGE = "behind_nudge"
        private const val KEY_LAST_LOG_TIME = "last_log_time"
    }
}
