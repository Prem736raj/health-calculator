package com.health.calculator.bmi.tracker.domain.usecase

import com.health.calculator.bmi.tracker.data.model.CalorieResult
import kotlin.math.pow

class CalorieCalculatorUseCase {

    companion object {
        const val MINIMUM_MALE_CALORIES = 1500.0
        const val MINIMUM_FEMALE_CALORIES = 1200.0
        const val CALORIES_PER_KG = 7700.0
    }

    fun calculate(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: String,
        bodyFatPercent: Double?,
        activityMultiplier: Double,
        activityLevelName: String,
        goalAdjustment: Int,
        goalName: String,
        weeklyChangeKg: Double
    ): CalorieResult {
        val isMale = gender.equals("Male", ignoreCase = true)

        // 1. BMR - Mifflin-St Jeor
        val bmrMifflin = if (isMale) {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + 5.0
        } else {
            (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 161.0
        }

        // 2. BMR - Katch-McArdle (if body fat % available)
        val bmrKatchMcArdle = bodyFatPercent?.let { bf ->
            val leanBodyMass = weightKg * (1 - bf / 100.0)
            370.0 + (21.6 * leanBodyMass)
        }

        // Use Katch-McArdle if body fat is provided, otherwise Mifflin
        val usedBmr: Double
        val formulaUsed: String
        if (bmrKatchMcArdle != null) {
            usedBmr = bmrKatchMcArdle
            formulaUsed = "Katch-McArdle"
        } else {
            usedBmr = bmrMifflin
            formulaUsed = "Mifflin-St Jeor"
        }

        // 3. TDEE (before TEF)
        val tdeeBeforeTef = usedBmr * activityMultiplier

        // 4. TEF (Thermic Effect of Food) ~10% of TDEE
        val tef = tdeeBeforeTef * 0.10

        // 5. Total TDEE
        val tdee = tdeeBeforeTef + tef

        // 6. Goal calories
        val rawGoalCalories = tdee + goalAdjustment

        // 7. Safety floor
        val minimumCalories = if (isMale) MINIMUM_MALE_CALORIES else MINIMUM_FEMALE_CALORIES
        val isBelowMinimum = rawGoalCalories < minimumCalories
        val safeGoalCalories = rawGoalCalories.coerceAtLeast(minimumCalories)

        return CalorieResult(
            bmrMifflin = bmrMifflin,
            bmrKatchMcArdle = bmrKatchMcArdle,
            usedBmr = usedBmr,
            bmrFormulaUsed = formulaUsed,
            activityMultiplier = activityMultiplier,
            activityLevelName = activityLevelName,
            tdee = tdee,
            tef = tef,
            goalCalories = rawGoalCalories,
            goalName = goalName,
            goalAdjustment = goalAdjustment,
            weeklyChangeKg = weeklyChangeKg,
            weightKg = weightKg,
            heightCm = heightCm,
            age = age,
            gender = gender,
            bodyFatPercent = bodyFatPercent,
            isBelowMinimum = isBelowMinimum,
            minimumCalories = minimumCalories,
            safeGoalCalories = safeGoalCalories
        )
    }
}
