// File: com/health/calculator/bmi/tracker/data/model/TDEEData.kt
package com.health.calculator.bmi.tracker.data.model

data class TDEEData(
    val bmr: Float = 0f,
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val tdee: Float = 0f,
    val activityCalories: Float = 0f
) {
    val bmrPercentage: Float
        get() = if (tdee > 0) (bmr / tdee) * 100f else 0f

    val activityPercentage: Float
        get() = if (tdee > 0) (activityCalories / tdee) * 100f else 0f

    fun getCalorieGoals(): List<CalorieGoal> {
        return listOf(
            CalorieGoal(
                name = "Extreme Weight Loss",
                emoji = "🔴",
                calorieAdjustment = -1000,
                dailyCalories = (tdee - 1000f).coerceAtLeast(1200f),
                weeklyWeightChangeKg = -1000f / 7700f * 7f, // ~0.91 kg/week loss
                description = "Aggressive deficit — medical supervision recommended",
                goalType = GoalType.EXTREME_LOSS
            ),
            CalorieGoal(
                name = "Moderate Weight Loss",
                emoji = "🟠",
                calorieAdjustment = -500,
                dailyCalories = (tdee - 500f).coerceAtLeast(1200f),
                weeklyWeightChangeKg = -500f / 7700f * 7f, // ~0.45 kg/week loss
                description = "Recommended pace — sustainable and healthy",
                goalType = GoalType.MODERATE_LOSS
            ),
            CalorieGoal(
                name = "Mild Weight Loss",
                emoji = "🟡",
                calorieAdjustment = -250,
                dailyCalories = (tdee - 250f).coerceAtLeast(1200f),
                weeklyWeightChangeKg = -250f / 7700f * 7f, // ~0.23 kg/week loss
                description = "Gentle approach — easier to maintain long-term",
                goalType = GoalType.MILD_LOSS
            ),
            CalorieGoal(
                name = "Maintain Weight",
                emoji = "🟢",
                calorieAdjustment = 0,
                dailyCalories = tdee,
                weeklyWeightChangeKg = 0f,
                description = "Keep your current weight stable",
                goalType = GoalType.MAINTAIN
            ),
            CalorieGoal(
                name = "Mild Weight Gain",
                emoji = "🔵",
                calorieAdjustment = 250,
                dailyCalories = tdee + 250f,
                weeklyWeightChangeKg = 250f / 7700f * 7f, // ~0.23 kg/week gain
                description = "Lean gaining — minimize fat gain",
                goalType = GoalType.MILD_GAIN
            ),
            CalorieGoal(
                name = "Moderate Weight Gain",
                emoji = "🟣",
                calorieAdjustment = 500,
                dailyCalories = tdee + 500f,
                weeklyWeightChangeKg = 500f / 7700f * 7f, // ~0.45 kg/week gain
                description = "Standard bulking — muscle building focus",
                goalType = GoalType.MODERATE_GAIN
            )
        )
    }

    fun toShareText(): String {
        return buildString {
            append("⚡ My TDEE (Total Daily Energy Expenditure)\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("BMR: ${bmr.toInt()} kcal/day\n")
            append("Activity Level: ${activityLevel.displayName}\n")
            append("TDEE: ${tdee.toInt()} kcal/day\n\n")
            append("📊 Calorie Goals:\n")
            getCalorieGoals().forEach { goal ->
                append("  ${goal.emoji} ${goal.name}: ${goal.dailyCalories.toInt()} kcal/day\n")
            }
            append("\n⚠️ For informational purposes only.\n")
            append("Calculated using Health Calculator app.")
        }
    }
}

data class CalorieGoal(
    val name: String,
    val emoji: String,
    val calorieAdjustment: Int,
    val dailyCalories: Float,
    val weeklyWeightChangeKg: Float,
    val description: String,
    val goalType: GoalType
)

enum class GoalType {
    EXTREME_LOSS, MODERATE_LOSS, MILD_LOSS, MAINTAIN, MILD_GAIN, MODERATE_GAIN
}
