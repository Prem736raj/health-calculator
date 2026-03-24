package com.health.calculator.bmi.tracker.domain.usecase

import com.health.calculator.bmi.tracker.data.model.MacroResult
import com.health.calculator.bmi.tracker.data.model.DietPreset

class MacroCalculatorUseCase {

    companion object {
        const val CALORIES_PER_GRAM_PROTEIN = 4.0
        const val CALORIES_PER_GRAM_CARB = 4.0
        const val CALORIES_PER_GRAM_FAT = 9.0
        const val SATURATED_FAT_RATIO = 0.33 // ~1/3 of fat intake can be saturated
    }

    val dietPresets = listOf(
        DietPreset(
            id = "balanced",
            name = "Balanced",
            description = "Well-rounded for general health",
            carbPercent = 40,
            proteinPercent = 30,
            fatPercent = 30,
            emoji = "⚖️",
            color = 0xFF4CAF50
        ),
        DietPreset(
            id = "low_carb",
            name = "Low Carb",
            description = "Reduced carbs, higher protein & fat",
            carbPercent = 20,
            proteinPercent = 40,
            fatPercent = 40,
            emoji = "🥩",
            color = 0xFFFF9800
        ),
        DietPreset(
            id = "high_carb",
            name = "High Carb",
            description = "For endurance athletes",
            carbPercent = 55,
            proteinPercent = 25,
            fatPercent = 20,
            emoji = "🍝",
            color = 0xFF2196F3
        ),
        DietPreset(
            id = "keto",
            name = "Ketogenic",
            description = "Very low carb, high fat",
            carbPercent = 5,
            proteinPercent = 25,
            fatPercent = 70,
            emoji = "🥑",
            color = 0xFF9C27B0
        ),
        DietPreset(
            id = "high_protein",
            name = "High Protein",
            description = "For muscle building & weight loss",
            carbPercent = 30,
            proteinPercent = 40,
            fatPercent = 30,
            emoji = "💪",
            color = 0xFFF44336
        ),
        DietPreset(
            id = "custom",
            name = "Custom",
            description = "Set your own ratios",
            carbPercent = 40,
            proteinPercent = 30,
            fatPercent = 30,
            emoji = "⚙️",
            color = 0xFF607D8B
        )
    )

    /**
     * Calculate recommended protein based on body weight, activity level, and goal
     */
    fun calculateRecommendedProtein(
        weightKg: Double,
        activityLevel: String,
        goalType: String
    ): Double {
        val proteinPerKg = when {
            // Weight loss - higher protein to preserve muscle
            goalType.contains("lose", ignoreCase = true) -> 1.8
            // Muscle gain - high protein for muscle synthesis
            goalType.contains("gain", ignoreCase = true) -> 2.0
            // Active maintenance
            activityLevel in listOf("very_active", "extremely_active") -> 1.6
            activityLevel in listOf("moderate", "light") -> 1.4
            // Sedentary
            else -> 0.8
        }
        return weightKg * proteinPerKg
    }

    /**
     * Calculate macros from percentage ratios
     */
    fun calculateFromPercentages(
        totalCalories: Double,
        carbPercent: Int,
        proteinPercent: Int,
        fatPercent: Int,
        weightKg: Double,
        presetName: String,
        numberOfMeals: Int = 3
    ): MacroResult {
        // Calculate calories for each macro
        val proteinCalories = totalCalories * proteinPercent / 100.0
        val fatCalories = totalCalories * fatPercent / 100.0
        val carbCalories = totalCalories * carbPercent / 100.0

        // Convert to grams
        val proteinGrams = proteinCalories / CALORIES_PER_GRAM_PROTEIN
        val fatGrams = fatCalories / CALORIES_PER_GRAM_FAT
        val carbGrams = carbCalories / CALORIES_PER_GRAM_CARB

        // Fat breakdown (recommended: 1/3 saturated, 2/3 unsaturated)
        val saturatedFatGrams = fatGrams * SATURATED_FAT_RATIO
        val unsaturatedFatGrams = fatGrams * (1 - SATURATED_FAT_RATIO)

        // Fiber recommendation based on calories (rough estimate)
        val fiberRecommendation = when {
            totalCalories < 1500 -> 21.0
            totalCalories < 2000 -> 25.0
            totalCalories < 2500 -> 28.0
            else -> 30.0
        }

        return MacroResult(
            totalCalories = totalCalories,
            proteinGrams = proteinGrams,
            proteinCalories = proteinCalories,
            proteinPercent = proteinPercent.toFloat(),
            proteinPerKg = proteinGrams / weightKg,
            fatGrams = fatGrams,
            fatCalories = fatCalories,
            fatPercent = fatPercent.toFloat(),
            saturatedFatGrams = saturatedFatGrams,
            unsaturatedFatGrams = unsaturatedFatGrams,
            carbGrams = carbGrams,
            carbCalories = carbCalories,
            carbPercent = carbPercent.toFloat(),
            fiberRecommendation = fiberRecommendation,
            dietPresetName = presetName,
            numberOfMeals = numberOfMeals
        )
    }

    /**
     * Calculate macros starting from recommended protein
     */
    fun calculateFromProteinFirst(
        totalCalories: Double,
        weightKg: Double,
        activityLevel: String,
        goalType: String,
        fatPercent: Int = 30,
        numberOfMeals: Int = 3
    ): MacroResult {
        // Calculate recommended protein
        val recommendedProteinGrams = calculateRecommendedProtein(weightKg, activityLevel, goalType)
        val proteinCalories = recommendedProteinGrams * CALORIES_PER_GRAM_PROTEIN
        val proteinPercent = ((proteinCalories / totalCalories) * 100).coerceIn(10.0, 50.0).toInt()

        // Fat (use provided percentage, but ensure minimum 20%)
        val actualFatPercent = fatPercent.coerceAtLeast(20)

        // Remaining goes to carbs
        val carbPercent = (100 - proteinPercent - actualFatPercent).coerceAtLeast(5)

        // Recalculate to ensure 100%
        val adjustedProteinPercent = 100 - carbPercent - actualFatPercent

        return calculateFromPercentages(
            totalCalories = totalCalories,
            carbPercent = carbPercent,
            proteinPercent = adjustedProteinPercent,
            fatPercent = actualFatPercent,
            weightKg = weightKg,
            presetName = "Goal-Based",
            numberOfMeals = numberOfMeals
        )
    }

    /**
     * Get protein recommendation text based on goal
     */
    fun getProteinRecommendationText(activityLevel: String, goalType: String): String {
        return when {
            goalType.contains("lose", ignoreCase = true) ->
                "1.6-2.0 g/kg recommended during weight loss to preserve muscle mass"
            goalType.contains("gain", ignoreCase = true) ->
                "1.6-2.2 g/kg recommended for muscle building and recovery"
            activityLevel in listOf("very_active", "extremely_active") ->
                "1.4-1.6 g/kg recommended for your high activity level"
            activityLevel in listOf("moderate", "light") ->
                "1.2-1.6 g/kg recommended for active individuals"
            else ->
                "0.8-1.0 g/kg is the minimum for sedentary individuals"
        }
    }
}
