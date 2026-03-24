package com.health.calculator.bmi.tracker.data.models

data class HealthJourneySummary(
    val daysSinceFirstUse: Int = 0,
    val totalCalculations: Int = 0,
    val calculatorsUsed: Int = 0,
    val totalCalculatorsAvailable: Int = 10,
    val currentHealthScore: Int = -1,
    val healthScoreChange: Int = 0, // compared to first recorded
    val currentTrackingStreak: Int = 0,
    val longestTrackingStreak: Int = 0,
    val goalsSet: Int = 0,
    val goalsAchieved: Int = 0,
    val milestonesEarned: Int = 0,
    val totalMilestonesAvailable: Int = MilestoneType.values().size,
    val personalRecordsSet: Int = 0,
    val firstUseDate: Long? = null,
    val mostUsedCalculator: String? = null,
    val favoriteTimeOfDay: String? = null // "Morning", "Afternoon", "Evening"
)
