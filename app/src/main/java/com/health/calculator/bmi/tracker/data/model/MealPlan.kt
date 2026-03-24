package com.health.calculator.bmi.tracker.data.model

data class MealPlan(
    val meals: List<MealSlot>,
    val totalCalories: Double,
    val mealCount: Int,
    val distributionName: String,
    val isCustomDistribution: Boolean = false
)

data class MealSlot(
    val name: String,
    val label: String, // "Breakfast", "Lunch", etc.
    val percentOfDaily: Float,
    val calories: Double,
    val proteinGrams: Double,
    val carbGrams: Double,
    val fatGrams: Double,
    val suggestedTime: String,
    val mealIdeas: List<MealIdea>
)

data class MealIdea(
    val description: String,
    val calories: Int,
    val emoji: String
)

data class IntermittentFastingPlan(
    val type: String, // "16:8", "18:6", "20:4", "none"
    val fastingHours: Int,
    val eatingHours: Int,
    val windowStartHour: Int, // 24-hour format
    val windowEndHour: Int,
    val mealsInWindow: List<MealSlot>
)

data class WorkoutNutrition(
    val enabled: Boolean,
    val workoutTime: String,
    val preWorkoutMeal: MealSlot?,
    val postWorkoutMeal: MealSlot?,
    val preWorkoutTiming: String, // "1-2 hours before"
    val postWorkoutTiming: String // "within 1 hour after"
)

data class MealDistributionPreset(
    val id: String,
    val name: String,
    val mealCount: Int,
    val splits: List<Float>, // percentages that sum to 100
    val labels: List<String>,
    val defaultTimes: List<String>
)
