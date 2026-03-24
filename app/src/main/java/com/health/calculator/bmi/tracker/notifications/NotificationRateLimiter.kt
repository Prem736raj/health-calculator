// notifications/NotificationRateLimiter.kt
package com.health.calculator.bmi.tracker.notifications

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

class NotificationRateLimiter(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "notification_rate_limiter",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val MAX_NOTIFICATIONS_PER_DAY = 8
        private const val MIN_MINUTES_BETWEEN_NOTIFICATIONS = 15
        private const val MIN_MINUTES_AFTER_APP_USE = 10

        private const val KEY_NOTIFICATION_COUNT = "notification_count"
        private const val KEY_NOTIFICATION_DATE = "notification_date"
        private const val KEY_LAST_NOTIFICATION_TIME = "last_notification_time"
        private const val KEY_LAST_APP_USE_TIME = "last_app_use_time"
        private const val KEY_NOTIFICATION_HISTORY = "notification_history"
    }

    /**
     * Check if a notification should be sent based on rate limiting rules.
     */
    fun shouldSendNotification(
        isHighPriority: Boolean = false,
        category: String = ""
    ): RateLimitResult {
        val now = System.currentTimeMillis()

        // Reset daily count if new day
        resetDailyCountIfNeeded()

        // Check 1: Daily limit
        val todayCount = getTodayNotificationCount()
        if (todayCount >= MAX_NOTIFICATIONS_PER_DAY && !isHighPriority) {
            return RateLimitResult(
                allowed = false,
                reason = "Daily limit reached ($MAX_NOTIFICATIONS_PER_DAY notifications)"
            )
        }

        // Check 2: Time since last notification
        val lastNotificationTime = prefs.getLong(KEY_LAST_NOTIFICATION_TIME, 0)
        val minutesSinceLastNotification = (now - lastNotificationTime) / (1000 * 60)
        if (minutesSinceLastNotification < MIN_MINUTES_BETWEEN_NOTIFICATIONS && !isHighPriority) {
            return RateLimitResult(
                allowed = false,
                reason = "Too soon after last notification (${MIN_MINUTES_BETWEEN_NOTIFICATIONS}min minimum)"
            )
        }

        // Check 3: Time since user used the app
        val lastAppUseTime = prefs.getLong(KEY_LAST_APP_USE_TIME, 0)
        val minutesSinceAppUse = (now - lastAppUseTime) / (1000 * 60)
        if (minutesSinceAppUse < MIN_MINUTES_AFTER_APP_USE) {
            return RateLimitResult(
                allowed = false,
                reason = "User recently used the app"
            )
        }

        // Check 4: Same category spam prevention
        if (category.isNotEmpty() && wasSameCategorySentRecently(category, 30)) {
            return RateLimitResult(
                allowed = false,
                reason = "Same category notification sent recently"
            )
        }

        return RateLimitResult(allowed = true)
    }

    /**
     * Record that a notification was sent.
     */
    fun recordNotificationSent(category: String = "") {
        val now = System.currentTimeMillis()

        prefs.edit().apply {
            putLong(KEY_LAST_NOTIFICATION_TIME, now)
            putInt(KEY_NOTIFICATION_COUNT, getTodayNotificationCount() + 1)

            // Record in history for category tracking
            val history = getNotificationHistory().toMutableList()
            history.add(NotificationRecord(now, category))

            // Keep last 50 records
            if (history.size > 50) {
                history.removeAt(0)
            }
            putString(KEY_NOTIFICATION_HISTORY, encodeHistory(history))

        }.apply()
    }

    /**
     * Record when user used the app (to prevent immediate notifications).
     */
    fun recordAppUsed() {
        prefs.edit().putLong(KEY_LAST_APP_USE_TIME, System.currentTimeMillis()).apply()
    }

    /**
     * Get count of notifications sent today.
     */
    fun getTodayNotificationCount(): Int {
        resetDailyCountIfNeeded()
        return prefs.getInt(KEY_NOTIFICATION_COUNT, 0)
    }

    /**
     * Get remaining notifications allowed today.
     */
    fun getRemainingNotificationsToday(): Int {
        return (MAX_NOTIFICATIONS_PER_DAY - getTodayNotificationCount()).coerceAtLeast(0)
    }

    private fun resetDailyCountIfNeeded() {
        val savedDate = prefs.getLong(KEY_NOTIFICATION_DATE, 0)
        val today = getTodayStartMillis()

        if (savedDate < today) {
            prefs.edit()
                .putLong(KEY_NOTIFICATION_DATE, today)
                .putInt(KEY_NOTIFICATION_COUNT, 0)
                .apply()
        }
    }

    private fun getTodayStartMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun wasSameCategorySentRecently(category: String, withinMinutes: Int): Boolean {
        val history = getNotificationHistory()
        val cutoff = System.currentTimeMillis() - (withinMinutes * 60 * 1000)

        return history.any { it.category == category && it.timestamp > cutoff }
    }

    private fun getNotificationHistory(): List<NotificationRecord> {
        val encoded = prefs.getString(KEY_NOTIFICATION_HISTORY, "") ?: ""
        return decodeHistory(encoded)
    }

    private fun encodeHistory(records: List<NotificationRecord>): String {
        return records.joinToString(";") { "${it.timestamp},${it.category}" }
    }

    private fun decodeHistory(encoded: String): List<NotificationRecord> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split(";").mapNotNull { part ->
            val split = part.split(",")
            if (split.size >= 2) {
                NotificationRecord(split[0].toLongOrNull() ?: 0, split.getOrElse(1) { "" })
            } else null
        }
    }

    data class NotificationRecord(val timestamp: Long, val category: String)
}

data class RateLimitResult(
    val allowed: Boolean,
    val reason: String = ""
)
