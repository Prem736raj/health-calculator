package com.health.calculator.bmi.tracker.data.model

data class CalorieHistoryStats(
    val averageDailyCalories: Double,
    val weeklyAverage: Double,
    val weeklyTotal: Double,
    val daysAtTarget: Int,
    val totalDaysTracked: Int,
    val highestCalorieDay: Pair<String, Double>?,
    val lowestCalorieDay: Pair<String, Double>?,
    val currentStreak: Int,
    val longestStreak: Int,
    val mostLoggedFood: String?,
    val averageProtein: Double,
    val averageCarbs: Double,
    val averageFat: Double,
    val targetCalories: Double,
    val targetProtein: Double,
    val targetCarbs: Double,
    val targetFat: Double
) {
    val adherencePercent: Float
        get() = if (totalDaysTracked > 0)
            (daysAtTarget.toFloat() / totalDaysTracked * 100f) else 0f

    val calorieDeficitOrSurplus: Double
        get() = averageDailyCalories - targetCalories

    val proteinAdherence: Float
        get() = if (targetProtein > 0) (averageProtein / targetProtein * 100f).toFloat() else 0f

    val carbAdherence: Float
        get() = if (targetCarbs > 0) (averageCarbs / targetCarbs * 100f).toFloat() else 0f

    val fatAdherence: Float
        get() = if (targetFat > 0) (averageFat / targetFat * 100f).toFloat() else 0f
}

data class DayCalorieStatus(
    val date: String,          // "YYYY-MM-DD"
    val totalCalories: Double,
    val targetCalories: Double,
    val hasData: Boolean
) {
    val difference: Double get() = totalCalories - targetCalories
    val absDifference: Double get() = kotlin.math.abs(difference)

    val statusColor: CalorieAdherenceStatus
        get() = when {
            !hasData -> CalorieAdherenceStatus.NO_DATA
            absDifference <= 100 -> CalorieAdherenceStatus.ON_TARGET
            absDifference <= 200 -> CalorieAdherenceStatus.CLOSE
            difference > 200 -> CalorieAdherenceStatus.OVER
            else -> CalorieAdherenceStatus.UNDER
        }
}

enum class CalorieAdherenceStatus {
    ON_TARGET, CLOSE, OVER, UNDER, NO_DATA
}

data class WeeklyCalorieSummary(
    val weekLabel: String,
    val averageCalories: Double,
    val totalCalories: Double,
    val targetCalories: Double,
    val daysTracked: Int
)
