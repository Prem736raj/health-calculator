package com.health.calculator.bmi.tracker.domain.usecase

import com.health.calculator.bmi.tracker.data.model.IBWGoal
import com.health.calculator.bmi.tracker.data.model.WeightPaceOption
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.ceil

class WeightGoalPlannerUseCase {

    companion object {
        const val CALORIES_PER_KG = 7700 // ~7700 kcal per kg of body weight
    }

    fun calculatePaceOptions(
        currentWeightKg: Double,
        targetWeightKg: Double
    ): List<WeightPaceOption> {
        val difference = abs(targetWeightKg - currentWeightKg)
        val isLoss = currentWeightKg > targetWeightKg

        val paces = listOf(
            Triple("Conservative", 0.25, "Safest & most sustainable"),
            Triple("Moderate", 0.5, "Recommended pace"),
            Triple("Aggressive", 1.0, "Maximum safe rate")
        )

        return paces.map { (name, weeklyKg, label) ->
            val weeks = ceil(difference / weeklyKg).toInt().coerceAtLeast(1)
            val dailyCalories = ((weeklyKg * CALORIES_PER_KG) / 7).toInt()
            val calendar = Calendar.getInstance().apply {
                add(Calendar.WEEK_OF_YEAR, weeks)
            }

            WeightPaceOption(
                name = name,
                weeklyChangeKg = if (isLoss) -weeklyKg else weeklyKg,
                label = label,
                dailyCalorieAdjustment = if (isLoss) -dailyCalories else dailyCalories,
                estimatedWeeks = weeks,
                estimatedDate = calendar.timeInMillis
            )
        }
    }

    fun getMilestoneReached(progressPercent: Float): Int? {
        return when {
            progressPercent >= 100f -> 100
            progressPercent >= 75f -> 75
            progressPercent >= 50f -> 50
            progressPercent >= 25f -> 25
            else -> null
        }
    }

    fun getNextMilestone(progressPercent: Float): Int {
        return when {
            progressPercent < 25f -> 25
            progressPercent < 50f -> 50
            progressPercent < 75f -> 75
            progressPercent < 100f -> 100
            else -> 100
        }
    }

    fun getMotivationalMessage(progressPercent: Float, isWeightLoss: Boolean): String {
        val action = if (isWeightLoss) "weight loss" else "weight gain"
        return when {
            progressPercent >= 100f -> "🎉 Congratulations! You've reached your ideal weight goal!"
            progressPercent >= 75f -> "🌟 Amazing progress! You're 75% of the way to your $action goal!"
            progressPercent >= 50f -> "💪 Halfway there! Keep going, you're doing great!"
            progressPercent >= 25f -> "👏 Great start! You've completed 25% of your $action journey!"
            progressPercent > 0f -> "🚀 You've started your journey! Every step counts!"
            else -> "Set a goal and start tracking your progress!"
        }
    }
}
