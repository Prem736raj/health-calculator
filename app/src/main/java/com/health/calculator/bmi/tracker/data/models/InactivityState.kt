// app/src/main/java/com/health/calculator/bmi/tracker/data/models/InactivityState.kt
package com.health.calculator.bmi.tracker.data.models

data class InactivityState(
    val lastAppOpenTime: Long = System.currentTimeMillis(),
    val lastActivityTime: Long = System.currentTimeMillis(),
    val daysInactive: Int = 0,
    val lastInactivityNotificationLevel: Int = 0, // 0=none, 1=2day, 2=5day, 3=14day, 4=30day
    // Re-engagement is opt-in. Existing explicit preferences are preserved
    // by DataStore; these defaults apply only when a key is absent.
    val inactivityNotificationsEnabled: Boolean = false,
    val streakProtectionEnabled: Boolean = false,
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
        title = "A gentle check-in is here 👋",
        message = "If it fits your day, you can open the app and pick up where you left off. No pressure.",
        emoji = "👋"
    ),
    FIVE_DAYS(
        days = 5,
        title = "It's been a few days 🌱",
        message = "Your previous entries are still here. Add a check-in whenever it is useful for you.",
        emoji = "🌱"
    ),
    FOURTEEN_DAYS(
        days = 14,
        title = "Welcome back anytime! 💙",
        message = "Your data is safely stored and waiting for you. Come back whenever it works for you.",
        emoji = "💙"
    ),
    THIRTY_DAYS(
        days = 30,
        title = "Still here for you 🤗",
        message = "Your wellness history is still here. One optional check-in can help you reconnect.",
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
