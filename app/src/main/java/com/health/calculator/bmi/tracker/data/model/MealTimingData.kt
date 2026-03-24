// File: com/health/calculator/bmi/tracker/data/model/MealTimingData.kt
package com.health.calculator.bmi.tracker.data.model

data class MealTimingConfig(
    val pattern: EatingPattern = EatingPattern.STANDARD,
    val eatingWindowStartHour: Int = 8,
    val eatingWindowStartMinute: Int = 0,
    val totalCalories: Float = 2000f,
    val proteinGrams: Float = 150f,
    val carbsGrams: Float = 200f,
    val fatGrams: Float = 67f
) {
    val eatingWindowEndHour: Int
        get() {
            val endMinutes = (eatingWindowStartHour * 60 + eatingWindowStartMinute + pattern.eatingWindowHours * 60)
            return (endMinutes / 60) % 24
        }

    val eatingWindowEndMinute: Int
        get() {
            val endMinutes = (eatingWindowStartHour * 60 + eatingWindowStartMinute + pattern.eatingWindowHours * 60)
            return endMinutes % 60
        }

    val fastingHours: Int get() = 24 - pattern.eatingWindowHours

    fun getMeals(): List<TimedMealSlot> {
        val mealCount = pattern.mealCount
        val snackCount = pattern.snackCount
        val totalSlots = mealCount + snackCount

        val mealCaloriePortion = if (snackCount > 0) 0.25f else (1f / mealCount)
        val snackCaloriePortion = if (snackCount > 0) {
            val totalMealPortion = mealCaloriePortion * mealCount
            (1f - totalMealPortion) / snackCount
        } else 0f

        // Recalculate meal portions based on pattern
        val portions = buildList {
            when (pattern) {
                EatingPattern.STANDARD -> {
                    // 3 meals (25% each) + 2 snacks (12.5% each)
                    add(MealPortion("Breakfast", 0.25f, MealType.MEAL))
                    add(MealPortion("Morning Snack", 0.10f, MealType.SNACK))
                    add(MealPortion("Lunch", 0.30f, MealType.MEAL))
                    add(MealPortion("Afternoon Snack", 0.10f, MealType.SNACK))
                    add(MealPortion("Dinner", 0.25f, MealType.MEAL))
                }
                EatingPattern.THREE_MEALS -> {
                    add(MealPortion("Breakfast", 0.30f, MealType.MEAL))
                    add(MealPortion("Lunch", 0.40f, MealType.MEAL))
                    add(MealPortion("Dinner", 0.30f, MealType.MEAL))
                }
                EatingPattern.FOUR_MEALS -> {
                    add(MealPortion("Breakfast", 0.25f, MealType.MEAL))
                    add(MealPortion("Lunch", 0.30f, MealType.MEAL))
                    add(MealPortion("Snack", 0.15f, MealType.SNACK))
                    add(MealPortion("Dinner", 0.30f, MealType.MEAL))
                }
                EatingPattern.SIX_SMALL -> {
                    add(MealPortion("Meal 1", 0.17f, MealType.MEAL))
                    add(MealPortion("Meal 2", 0.17f, MealType.MEAL))
                    add(MealPortion("Meal 3", 0.17f, MealType.MEAL))
                    add(MealPortion("Meal 4", 0.17f, MealType.MEAL))
                    add(MealPortion("Meal 5", 0.16f, MealType.MEAL))
                    add(MealPortion("Meal 6", 0.16f, MealType.MEAL))
                }
                EatingPattern.IF_16_8, EatingPattern.IF_18_6, EatingPattern.IF_20_4, EatingPattern.CUSTOM -> {
                    val meals = pattern.mealCount
                    val portion = 1f / meals
                    for (i in 1..meals) {
                        add(MealPortion("Meal $i", portion, MealType.MEAL))
                    }
                }
            }
        }

        // Calculate meal times
        val windowMinutes = pattern.eatingWindowHours * 60
        val startMinutes = eatingWindowStartHour * 60 + eatingWindowStartMinute
        val slots = mutableListOf<TimedMealSlot>()

        portions.forEachIndexed { index, portion ->
            val minuteOffset = if (portions.size == 1) 0
            else (windowMinutes.toFloat() * index / (portions.size - 1).coerceAtLeast(1)).toInt()

            val mealMinutes = (startMinutes + minuteOffset) % (24 * 60)
            val hour = mealMinutes / 60
            val minute = mealMinutes % 60

            slots.add(
                TimedMealSlot(
                    name = portion.name,
                    type = portion.type,
                    calories = totalCalories * portion.fraction,
                    protein = proteinGrams * portion.fraction,
                    carbs = carbsGrams * portion.fraction,
                    fat = fatGrams * portion.fraction,
                    hour = hour,
                    minute = minute,
                    portionPercent = portion.fraction * 100f
                )
            )
        }

        return slots
    }
}

data class MealPortion(
    val name: String,
    val fraction: Float,
    val type: MealType
)

data class TimedMealSlot(
    val name: String,
    val type: MealType,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val hour: Int,
    val minute: Int,
    val portionPercent: Float
) {
    val timeString: String
        get() {
            val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val amPm = if (hour < 12) "AM" else "PM"
            return "${h}:${String.format("%02d", minute)} $amPm"
        }

    val time24String: String
        get() = "${String.format("%02d", hour)}:${String.format("%02d", minute)}"
}

enum class MealType { MEAL, SNACK }

enum class EatingPattern(
    val displayName: String,
    val shortName: String,
    val emoji: String,
    val eatingWindowHours: Int,
    val mealCount: Int,
    val snackCount: Int,
    val description: String,
    val isIntermittentFasting: Boolean
) {
    STANDARD(
        displayName = "Standard (3+2)",
        shortName = "Standard",
        emoji = "🍽️",
        eatingWindowHours = 14,
        mealCount = 3,
        snackCount = 2,
        description = "3 meals + 2 snacks spread across the day",
        isIntermittentFasting = false
    ),
    THREE_MEALS(
        displayName = "3 Meals Only",
        shortName = "3 Meals",
        emoji = "🥗",
        eatingWindowHours = 12,
        mealCount = 3,
        snackCount = 0,
        description = "Traditional breakfast, lunch, and dinner",
        isIntermittentFasting = false
    ),
    FOUR_MEALS(
        displayName = "4 Meals",
        shortName = "4 Meals",
        emoji = "🥘",
        eatingWindowHours = 12,
        mealCount = 3,
        snackCount = 1,
        description = "3 main meals + 1 afternoon snack",
        isIntermittentFasting = false
    ),
    SIX_SMALL(
        displayName = "6 Small Meals",
        shortName = "6 Small",
        emoji = "🍱",
        eatingWindowHours = 14,
        mealCount = 6,
        snackCount = 0,
        description = "6 smaller meals every 2-3 hours",
        isIntermittentFasting = false
    ),
    IF_16_8(
        displayName = "IF 16:8",
        shortName = "16:8",
        emoji = "⏰",
        eatingWindowHours = 8,
        mealCount = 3,
        snackCount = 0,
        description = "16 hours fasting, 8-hour eating window",
        isIntermittentFasting = true
    ),
    IF_18_6(
        displayName = "IF 18:6",
        shortName = "18:6",
        emoji = "⏱️",
        eatingWindowHours = 6,
        mealCount = 2,
        snackCount = 0,
        description = "18 hours fasting, 6-hour eating window",
        isIntermittentFasting = true
    ),
    IF_20_4(
        displayName = "IF 20:4",
        shortName = "20:4",
        emoji = "🎯",
        eatingWindowHours = 4,
        mealCount = 2,
        snackCount = 0,
        description = "20 hours fasting, 4-hour window (OMAD-like)",
        isIntermittentFasting = true
    ),
    CUSTOM(
        displayName = "Custom Window",
        shortName = "Custom",
        emoji = "🎛️",
        eatingWindowHours = 10,
        mealCount = 3,
        snackCount = 0,
        description = "Set your own eating window and meal count",
        isIntermittentFasting = true
    )
}
