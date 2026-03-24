// app/src/main/java/com/health/calculator/bmi/tracker/data/models/InactivityState.kt
package com.health.calculator.bmi.tracker.data.models

data class InactivityState(
    val lastAppOpenTime: Long = System.currentTimeMillis(),
    val lastActivityTime: Long = System.currentTimeMillis(),
    val daysInactive: Int = 0,
    val lastInactivityNotificationLevel: Int = 0, // 0=none, 1=2day, 2=5day, 3=14day, 4=30day
    val inactivityNotificationsEnabled: Boolean = true,
    val streakProtectionEnabled: Boolean = true,
    val hasSeenWelcomeBack: Boolean = true
)

data class WelcomeBackData(
    val userName: String?,
    val daysAway: Int,
    val lastActiveDate: Long,
    val streakStatus: StreakStatus,
    val lastHealthMetrics: List<LastKnownMetric>,
    val mostUsedCalculators: List<FrequentCalculator>,
    val plantStatus: PlantWelcomeStatus
)

data class LastKnownMetric(
    val icon: String,
    val name: String,
    val value: String,
    val category: String,
    val daysAgo: Int,
    val route: String
)

data class FrequentCalculator(
    val name: String,
    val icon: String,
    val route: String,
    val useCount: Int
)

data class StreakStatus(
    val waterStreak: Int,
    val wasWaterStreakBroken: Boolean,
    val waterStreakBeforeBreak: Int,
    val trackingStreak: Int,
    val wasTrackingStreakBroken: Boolean,
    val trackingStreakBeforeBreak: Int,
    val streakFreezeAvailable: Boolean,
    val streakFreezeUsed: Boolean
)

data class PlantWelcomeStatus(
    val wasHealthy: Boolean,
    val currentStage: Int,
    val needsAttention: Boolean
)

enum class InactivityLevel(
    val days: Int,
    val title: String,
    val message: String,
    val emoji: String
) {
    TWO_DAYS(
        days = 2,
        title = "We miss you! 👋",
        message = "Your health tracking streak is at risk. A quick check-in keeps your progress going!",
        emoji = "👋"
    ),
    FIVE_DAYS(
        days = 5,
        title = "It's been a few days 🌱",
        message = "Your plant is getting thirsty! A quick water log or health check would brighten its day.",
        emoji = "🌱"
    ),
    FOURTEEN_DAYS(
        days = 14,
        title = "Welcome back anytime! 💙",
        message = "Your health data is safely stored and waiting for you. No pressure — come back when you're ready.",
        emoji = "💙"
    ),
    THIRTY_DAYS(
        days = 30,
        title = "Still here for you 🤗",
        message = "Your health journey is always here. One small step is all it takes to restart.",
        emoji = "🤗"
    );

    companion object {
        fun forDays(days: Int): InactivityLevel? = when {
            days >= 30 -> THIRTY_DAYS
            days >= 14 -> FOURTEEN_DAYS
            days >= 5 -> FIVE_DAYS
            days >= 2 -> TWO_DAYS
            else -> null
        }

        fun getLevelNumber(level: InactivityLevel): Int = when (level) {
            TWO_DAYS -> 1
            FIVE_DAYS -> 2
            FOURTEEN_DAYS -> 3
            THIRTY_DAYS -> 4
        }
    }
}
