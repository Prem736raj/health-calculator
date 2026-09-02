package com.health.calculator.bmi.tracker.domain.engagement

/**
 * Product rules for healthy, user-controlled engagement.
 *
 * These values intentionally live outside notification/UI code so that the
 * app can test its tone and opt-in defaults without needing an Android
 * runtime. Engagement is a nudge, never a measure of health or worth.
 */
object WellnessEngagementPolicy {
    /** Re-engagement notifications are opt-in on a fresh install. */
    const val DEFAULT_INACTIVITY_NOTIFICATIONS_ENABLED = false
    const val DEFAULT_STREAK_PROTECTION_ENABLED = false

    fun weeklyRhythmLabel(legacyGrade: String): String = when (legacyGrade.uppercase()) {
        "A" -> "Strong logging rhythm"
        "B" -> "Steady logging rhythm"
        "C" -> "Building a logging rhythm"
        "D", "F" -> "A lighter logging week"
        else -> "Your weekly logging rhythm"
    }

    fun weeklyRhythmMessage(legacyGrade: String): String = when (legacyGrade.uppercase()) {
        "A" -> "You recorded several check-ins this week. Keep the parts of this routine that feel useful."
        "B" -> "You recorded a steady set of check-ins this week. Small, sustainable steps are welcome."
        "C" -> "Some check-ins are here to review. Choose one small action only if it supports your goals."
        "D", "F" -> "There are fewer check-ins this week. A missed day does not erase your progress."
        else -> "This is an informational snapshot of the check-ins you recorded."
    }

    fun streakReminderTitle(streakDays: Int): String = when {
        streakDays > 1 -> "A gentle check-in for your $streakDays-day rhythm"
        else -> "A gentle wellness check-in"
    }

    fun streakReminderMessage(streakType: String): String = when (streakType) {
        "water" -> "If it fits your day, you can log water or open the tracker. Skipping today is okay."
        else -> "If it fits your day, you can add a check-in. Your previous entries will still be here."
    }
}
