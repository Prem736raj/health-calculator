package com.health.calculator.bmi.tracker.data.model

data class WeightGoalProgress(
    val currentWeight: Double,
    val goalWeight: Double,
    val startingWeight: Double,
    val totalToLoseOrGain: Double,
    val remainingToGoal: Double,
    val percentageComplete: Float,
    val isGainingGoal: Boolean,
    val estimatedDaysRemaining: Int?,
    val estimatedCompletionDate: Long?,
    val isGoalReached: Boolean,
    val averageWeeklyChange: Double?
) {
    val directionLabel: String
        get() = if (isGainingGoal) "gain" else "lose"
}
