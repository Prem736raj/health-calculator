package com.health.calculator.bmi.tracker.data.model

data class FoodEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val name: String,
    val calories: Double,
    val proteinGrams: Double = 0.0,
    val carbGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
    val mealSlot: String = "Other", // Breakfast, Lunch, Dinner, Snack, Other
    val servingSize: String = "",
    val isPreset: Boolean = false
)

data class FoodPreset(
    val id: String,
    val name: String,
    val calories: Double,
    val proteinGrams: Double = 0.0,
    val carbGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
    val servingSize: String = "",
    val emoji: String = "🍽️",
    val isCustom: Boolean = false
)

data class DailyFoodLog(
    val date: String, // "YYYY-MM-DD"
    val entries: List<FoodEntry>,
    val targetCalories: Double,
    val targetProteinGrams: Double = 0.0,
    val targetCarbGrams: Double = 0.0,
    val targetFatGrams: Double = 0.0
) {
    val totalCalories: Double get() = entries.sumOf { it.calories }
    val totalProtein: Double get() = entries.sumOf { it.proteinGrams }
    val totalCarbs: Double get() = entries.sumOf { it.carbGrams }
    val totalFat: Double get() = entries.sumOf { it.fatGrams }
    val remainingCalories: Double get() = targetCalories - totalCalories
    val calorieProgress: Float get() = (totalCalories / targetCalories).toFloat().coerceIn(0f, 2f)
    val proteinProgress: Float
        get() = if (targetProteinGrams > 0) (totalProtein / targetProteinGrams).toFloat().coerceIn(0f, 1.5f) else 0f
    val carbProgress: Float
        get() = if (targetCarbGrams > 0) (totalCarbs / targetCarbGrams).toFloat().coerceIn(0f, 1.5f) else 0f
    val fatProgress: Float
        get() = if (targetFatGrams > 0) (totalFat / targetFatGrams).toFloat().coerceIn(0f, 1.5f) else 0f
    val entriesByMeal: Map<String, List<FoodEntry>>
        get() = entries.groupBy { it.mealSlot }
}
