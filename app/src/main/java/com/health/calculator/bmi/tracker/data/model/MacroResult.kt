package com.health.calculator.bmi.tracker.data.model

data class MacroResult(
    val totalCalories: Double,
    val proteinGrams: Double,
    val proteinCalories: Double,
    val proteinPercent: Float,
    val proteinPerKg: Double,
    val fatGrams: Double,
    val fatCalories: Double,
    val fatPercent: Float,
    val saturatedFatGrams: Double,
    val unsaturatedFatGrams: Double,
    val carbGrams: Double,
    val carbCalories: Double,
    val carbPercent: Float,
    val fiberRecommendation: Double,
    val dietPresetName: String,
    val numberOfMeals: Int = 3
) {
    // Per meal calculations
    val proteinPerMeal: Double get() = proteinGrams / numberOfMeals
    val fatPerMeal: Double get() = fatGrams / numberOfMeals
    val carbPerMeal: Double get() = carbGrams / numberOfMeals
    val caloriesPerMeal: Double get() = totalCalories / numberOfMeals

    // Validation
    val isBalanced: Boolean get() = proteinPercent + fatPercent + carbPercent in 99f..101f
    val hasSufficientProtein: Boolean get() = proteinPerKg >= 0.8
    val hasSufficientFat: Boolean get() = fatPercent >= 20f
}

data class DietPreset(
    val id: String,
    val name: String,
    val description: String,
    val carbPercent: Int,
    val proteinPercent: Int,
    val fatPercent: Int,
    val emoji: String,
    val color: Long
)
