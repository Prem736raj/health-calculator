// data/model/WaterReminderSettings.kt
package com.health.calculator.bmi.tracker.data.model

data class WaterReminderSettings(
    val isEnabled: Boolean = false,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 22,
    val endMinute: Int = 0,
    val frequencyMinutes: Int = 60,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val soundUri: String = "default",
    val smartSkipEnabled: Boolean = true,
    val behindScheduleNudge: Boolean = true
) {
    val startTimeFormatted: String
        get() {
            val amPm = if (startHour < 12) "AM" else "PM"
            val hour12 = if (startHour == 0) 12 else if (startHour > 12) startHour - 12 else startHour
            return String.format("%d:%02d %s", hour12, startMinute, amPm)
        }

    val endTimeFormatted: String
        get() {
            val amPm = if (endHour < 12) "AM" else "PM"
            val hour12 = if (endHour == 0) 12 else if (endHour > 12) endHour - 12 else endHour
            return String.format("%d:%02d %s", hour12, endMinute, amPm)
        }

    val frequencyLabel: String
        get() = when (frequencyMinutes) {
            30 -> "Every 30 minutes"
            60 -> "Every 1 hour"
            90 -> "Every 1.5 hours"
            120 -> "Every 2 hours"
            else -> "Every $frequencyMinutes min"
        }

    val wakingMinutes: Int
        get() {
            val startTotal = startHour * 60 + startMinute
            val endTotal = endHour * 60 + endMinute
            return if (endTotal > startTotal) endTotal - startTotal else (24 * 60 - startTotal + endTotal)
        }
}

enum class ReminderFrequency(val minutes: Int, val label: String) {
    THIRTY_MIN(30, "Every 30 min"),
    ONE_HOUR(60, "Every 1 hour"),
    NINETY_MIN(90, "Every 1.5 hours"),
    TWO_HOURS(120, "Every 2 hours")
}
