package com.health.calculator.bmi.tracker.data.models

data class QuietHours(
    val isEnabled: Boolean = false,
    val startHour: Int = 22, // 10 PM
    val startMinute: Int = 0,
    val endHour: Int = 7, // 7 AM
    val endMinute: Int = 0,
    val allowEmergencyOverride: Boolean = true
) {
    fun isInQuietPeriod(hour: Int, minute: Int): Boolean {
        if (!isEnabled) return false

        val currentMinutes = hour * 60 + minute
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes until endMinutes
        } else {
            // Crosses midnight
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    val startTimeFormatted: String
        get() = String.format("%02d:%02d", startHour, startMinute)

    val endTimeFormatted: String
        get() = String.format("%02d:%02d", endHour, endMinute)
}
