package com.health.calculator.bmi.tracker.data.preferences

import android.content.Context

/**
 * Small, app-private store for the single weekly weigh-in reminder.
 *
 * The Settings screen exposes only an on/off switch today, so the default
 * schedule is intentionally gentle and predictable. Keeping the schedule in
 * one place lets the receiver restore it after a reboot without relying on a
 * stale PendingIntent or an in-memory ViewModel.
 */
data class WeightReminderSettings(
    val enabled: Boolean = false,
    val dayOfWeek: Int = java.util.Calendar.MONDAY,
    val hour: Int = 9,
    val minute: Int = 0
)

class WeightReminderPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        FILE_NAME,
        Context.MODE_PRIVATE
    )

    fun load(): WeightReminderSettings = WeightReminderSettings(
        enabled = preferences.getBoolean(KEY_ENABLED, false),
        dayOfWeek = preferences.getInt(KEY_DAY, java.util.Calendar.MONDAY),
        hour = preferences.getInt(KEY_HOUR, 9),
        minute = preferences.getInt(KEY_MINUTE, 0)
    ).sanitized()

    fun save(settings: WeightReminderSettings) {
        val safe = settings.sanitized()
        preferences.edit()
            .putBoolean(KEY_ENABLED, safe.enabled)
            .putInt(KEY_DAY, safe.dayOfWeek)
            .putInt(KEY_HOUR, safe.hour)
            .putInt(KEY_MINUTE, safe.minute)
            .apply()
    }

    private fun WeightReminderSettings.sanitized(): WeightReminderSettings = copy(
        dayOfWeek = dayOfWeek.coerceIn(java.util.Calendar.SUNDAY, java.util.Calendar.SATURDAY),
        hour = hour.coerceIn(0, 23),
        minute = minute.coerceIn(0, 59)
    )

    private companion object {
        const val FILE_NAME = "weight_reminder_prefs"
        const val KEY_ENABLED = "enabled"
        const val KEY_DAY = "day_of_week"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
    }
}
