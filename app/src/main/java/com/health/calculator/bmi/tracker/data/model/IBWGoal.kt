package com.health.calculator.bmi.tracker.data.model

data class IBWGoal(
    val targetWeightKg: Double,
    val targetSource: String, // "Devine", "Robinson", "Average", "BMI Range", "Custom"
    val startWeightKg: Double,
    val startDate: Long = System.currentTimeMillis(),
    val selectedPace: String = "Moderate", // "Conservative", "Moderate", "Aggressive"
    val isActive: Boolean = true
) {
    val totalChangeKg: Double
        get() = targetWeightKg - startWeightKg

    val isWeightLoss: Boolean
        get() = totalChangeKg < 0

    val absChangeKg: Double
        get() = kotlin.math.abs(totalChangeKg)

    fun progressPercent(currentWeightKg: Double): Float {
        if (absChangeKg < 0.1) return 100f
        val achieved = kotlin.math.abs(currentWeightKg - startWeightKg)
        return ((achieved / absChangeKg) * 100).toFloat().coerceIn(0f, 100f)
    }

    fun remainingKg(currentWeightKg: Double): Double {
        return kotlin.math.abs(currentWeightKg - targetWeightKg)
    }

    fun isGoalReached(currentWeightKg: Double): Boolean {
        return if (isWeightLoss) {
            currentWeightKg <= targetWeightKg
        } else {
            currentWeightKg >= targetWeightKg
        }
    }
}

data class WeightPaceOption(
    val name: String,
    val weeklyChangeKg: Double,
    val label: String,
    val dailyCalorieAdjustment: Int,
    val estimatedWeeks: Int,
    val estimatedDate: Long
)
