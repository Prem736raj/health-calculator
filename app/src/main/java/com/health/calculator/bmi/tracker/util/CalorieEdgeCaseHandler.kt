package com.health.calculator.bmi.tracker.util

/**
 * Handles edge cases for calorie calculations
 */
object CalorieEdgeCaseHandler {

    // Minimum safe calorie intakes
    const val MIN_CALORIES_MALE = 1500
    const val MIN_CALORIES_FEMALE = 1200
    const val MAX_REASONABLE_CALORIES = 10000
    const val MAX_SINGLE_FOOD_ENTRY = 5000

    data class CalorieValidationResult(
        val adjustedCalories: Int,
        val warning: String? = null,
        val severity: WarningSeverity = WarningSeverity.NONE
    )

    enum class WarningSeverity {
        NONE, INFO, WARNING, DANGER
    }

    /**
     * Validates and potentially adjusts calorie target
     */
    fun validateCalorieTarget(
        calculatedCalories: Int,
        isMale: Boolean,
        tdee: Int
    ): CalorieValidationResult {
        val minCalories = if (isMale) MIN_CALORIES_MALE else MIN_CALORIES_FEMALE

        return when {
            // Unusually high values should be verified before any other message.
            calculatedCalories > MAX_REASONABLE_CALORIES -> CalorieValidationResult(
                adjustedCalories = MAX_REASONABLE_CALORIES,
                warning = "The calculated value seems unusually high. Please verify your inputs; the app capped the planning value at $MAX_REASONABLE_CALORIES kcal/day.",
                severity = WarningSeverity.WARNING
            )

            // Below absolute minimum
            calculatedCalories < minCalories -> CalorieValidationResult(
                adjustedCalories = minCalories,
                warning = "Your calculated target ($calculatedCalories kcal) is below this app's conservative planning floor of $minCalories kcal/day. " +
                        "It was adjusted for safety; consider discussing personal targets with a qualified professional.",
                severity = WarningSeverity.WARNING
            )

            // Very low but above minimum
            calculatedCalories < minCalories + 200 -> CalorieValidationResult(
                adjustedCalories = calculatedCalories,
                warning = "Your calorie target is close to this app's planning floor. Prioritize adequate nutrition and consider individualized guidance.",
                severity = WarningSeverity.INFO
            )

            // Very high calorie needs (extremely active)
            calculatedCalories > 4000 -> CalorieValidationResult(
                adjustedCalories = calculatedCalories,
                warning = "ℹ️ Your high calorie needs reflect a very active lifestyle. " +
                        "Focus on quality nutrition to fuel your activity level.",
                severity = WarningSeverity.INFO
            )

            // Deficit more than 50% of TDEE
            calculatedCalories < tdee * 0.5 -> CalorieValidationResult(
                adjustedCalories = calculatedCalories,
                warning = "⚠️ Your target represents a very large calorie deficit " +
                        "(more than 50% reduction from your TDEE of $tdee cal). " +
                        "This rate of loss may not be sustainable. " +
                        "Consider a more moderate approach.",
                severity = WarningSeverity.WARNING
            )

            else -> CalorieValidationResult(
                adjustedCalories = calculatedCalories,
                warning = null,
                severity = WarningSeverity.NONE
            )
        }
    }

    /**
     * Validates a food log entry
     */
    fun validateFoodEntry(
        calories: Int,
        foodName: String
    ): CalorieValidationResult {
        return when {
            foodName.isBlank() -> CalorieValidationResult(
                adjustedCalories = calories.coerceAtLeast(0),
                warning = "Please enter a food name.",
                severity = WarningSeverity.WARNING
            )

            calories <= 0 -> CalorieValidationResult(
                adjustedCalories = 0,
                warning = "Calories must be a positive number.",
                severity = WarningSeverity.WARNING
            )

            calories > MAX_SINGLE_FOOD_ENTRY -> CalorieValidationResult(
                adjustedCalories = calories,
                warning = "That's a lot of calories for a single food item. " +
                        "Are you sure \"$foodName\" is $calories calories?",
                severity = WarningSeverity.INFO
            )

            else -> CalorieValidationResult(
                adjustedCalories = calories,
                severity = WarningSeverity.NONE
            )
        }
    }

    /**
     * Validates daily total calories consumed
     */
    fun validateDailyTotal(
        totalConsumed: Int,
        target: Int
    ): String? {
        val ratio = if (target > 0) totalConsumed.toFloat() / target else 0f

        return when {
            ratio > 2.0f -> "You've logged more than double your planning target. If this is accurate, simply note it and return to your usual routine."

            ratio > 1.5f -> "You're significantly over your calorie target today. " +
                    "Don't stress — one day doesn't define your progress!"

            ratio > 1.1f -> "You're a bit over your target today. One day does not define your progress; return to your usual routine."

            else -> null
        }
    }
}
