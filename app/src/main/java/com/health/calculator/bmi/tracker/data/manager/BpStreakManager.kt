// data/manager/BpStreakManager.kt
package com.health.calculator.bmi.tracker.data.manager

import com.health.calculator.bmi.tracker.data.model.BpCategory
import com.health.calculator.bmi.tracker.data.preferences.BpReminderSettings
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val isNewMilestone: Boolean,
    val milestoneMessage: String?,
    val lastMeasurementDate: String
)

object BpStreakManager {

    private val DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE

    fun calculateStreakUpdate(settings: BpReminderSettings): StreakInfo {
        val today = LocalDate.now().format(DATE_FORMAT)
        val lastDate = settings.lastMeasurementDate

        if (lastDate == today) {
            // Already measured today, no change
            return StreakInfo(
                currentStreak = settings.currentStreak,
                longestStreak = settings.longestStreak,
                isNewMilestone = false,
                milestoneMessage = null,
                lastMeasurementDate = today
            )
        }

        val newStreak: Int
        val startDate: String

        if (lastDate.isEmpty()) {
            // First ever measurement
            newStreak = 1
            startDate = today
        } else {
            val lastLocalDate = LocalDate.parse(lastDate, DATE_FORMAT)
            val todayDate = LocalDate.now()
            val daysBetween = ChronoUnit.DAYS.between(lastLocalDate, todayDate)

            if (daysBetween == 1L) {
                // Consecutive day
                newStreak = settings.currentStreak + 1
                startDate = settings.streakStartDate.ifEmpty { today }
            } else {
                // Streak broken
                newStreak = 1
                startDate = today
            }
        }

        val newLongest = maxOf(newStreak, settings.longestStreak)
        val (isMilestone, milestoneMsg) = checkMilestone(newStreak)

        return StreakInfo(
            currentStreak = newStreak,
            longestStreak = newLongest,
            isNewMilestone = isMilestone,
            milestoneMessage = milestoneMsg,
            lastMeasurementDate = today
        )
    }

    private fun checkMilestone(streak: Int): Pair<Boolean, String?> {
        return when (streak) {
            3 -> true to "🎉 3 days in a row! Great start!"
            7 -> true to "🔥 1 week streak! You're building a healthy habit!"
            14 -> true to "⭐ 2 weeks strong! Consistency is key!"
            21 -> true to "💪 3 weeks! This is becoming routine!"
            30 -> true to "🏆 30 days! One month of daily tracking!"
            60 -> true to "🌟 60 days! Two months – incredible dedication!"
            90 -> true to "👑 90 days! You're a BP tracking champion!"
            100 -> true to "💯 100 days! Triple digits!"
            180 -> true to "🎊 6 months! Half a year of health tracking!"
            365 -> true to "🏅 1 YEAR! A full year of daily BP tracking!"
            else -> false to null
        }
    }

    fun getStreakEmoji(streak: Int): String {
        return when {
            streak == 0 -> "💤"
            streak < 3 -> "🌱"
            streak < 7 -> "🌿"
            streak < 14 -> "🔥"
            streak < 30 -> "⭐"
            streak < 60 -> "💪"
            streak < 90 -> "🏆"
            streak < 180 -> "👑"
            streak < 365 -> "🌟"
            else -> "🏅"
        }
    }

    fun getMotivationalMessage(streak: Int): String {
        return when {
            streak == 0 -> "Start tracking today!"
            streak == 1 -> "Great start! Keep it going tomorrow."
            streak < 3 -> "Building momentum – you've got this!"
            streak < 7 -> "Almost a full week! Don't break the chain!"
            streak < 14 -> "Over a week! You're forming a habit."
            streak < 30 -> "Impressive consistency! Keep going."
            streak < 60 -> "A whole month of tracking! Excellent."
            streak < 90 -> "Your doctor would be proud!"
            else -> "Incredible dedication to your health!"
        }
    }

    fun shouldSuggestDoctorVisit(
        consecutiveHypertensionCount: Int,
        lastDismissedAt: Long
    ): Boolean {
        if (consecutiveHypertensionCount < 3) return false
        // Don't show again if dismissed within last 7 days
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return lastDismissedAt < sevenDaysAgo
    }

    fun isHypertensionCategory(categoryName: String): Boolean {
        return try {
            val category = BpCategory.valueOf(categoryName)
            category.sortOrder >= BpCategory.GRADE_1_HYPERTENSION.sortOrder
        } catch (e: Exception) {
            false
        }
    }
}
