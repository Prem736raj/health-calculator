package com.health.calculator.bmi.tracker.domain.usecase

import com.health.calculator.bmi.tracker.data.model.*

class MealPlanningUseCase {

    val mealDistributions = mapOf(
        2 to MealDistributionPreset(
            id = "2_meals",
            name = "2 Meals",
            mealCount = 2,
            splits = listOf(45f, 55f),
            labels = listOf("Meal 1", "Meal 2"),
            defaultTimes = listOf("11:00 AM", "7:00 PM")
        ),
        3 to MealDistributionPreset(
            id = "3_meals",
            name = "3 Meals",
            mealCount = 3,
            splits = listOf(30f, 40f, 30f),
            labels = listOf("Breakfast", "Lunch", "Dinner"),
            defaultTimes = listOf("8:00 AM", "12:30 PM", "7:00 PM")
        ),
        4 to MealDistributionPreset(
            id = "4_meals",
            name = "4 Meals",
            mealCount = 4,
            splits = listOf(25f, 30f, 25f, 20f),
            labels = listOf("Breakfast", "Lunch", "Snack", "Dinner"),
            defaultTimes = listOf("8:00 AM", "12:30 PM", "4:00 PM", "7:30 PM")
        ),
        5 to MealDistributionPreset(
            id = "5_meals",
            name = "5 Meals",
            mealCount = 5,
            splits = listOf(20f, 25f, 15f, 25f, 15f),
            labels = listOf("Breakfast", "Snack 1", "Lunch", "Snack 2", "Dinner"),
            defaultTimes = listOf("7:30 AM", "10:30 AM", "1:00 PM", "4:30 PM", "7:30 PM")
        ),
        6 to MealDistributionPreset(
            id = "6_meals",
            name = "6 Meals",
            mealCount = 6,
            splits = listOf(18f, 12f, 22f, 12f, 22f, 14f),
            labels = listOf("Breakfast", "Snack 1", "Lunch", "Snack 2", "Dinner", "Evening Snack"),
            defaultTimes = listOf("7:00 AM", "9:30 AM", "12:00 PM", "3:00 PM", "6:30 PM", "9:00 PM")
        )
    )

    fun createMealPlan(
        totalCalories: Double,
        proteinGrams: Double,
        carbGrams: Double,
        fatGrams: Double,
        mealCount: Int,
        customSplits: List<Float>? = null
    ): MealPlan {
        val distribution = mealDistributions[mealCount]
            ?: mealDistributions[3]!! // Default to 3 meals

        val splits = customSplits ?: distribution.splits
        val isCustom = customSplits != null

        val meals = splits.mapIndexed { index, percent ->
            val mealCalories = totalCalories * percent / 100.0
            val mealProtein = proteinGrams * percent / 100.0
            val mealCarbs = carbGrams * percent / 100.0
            val mealFat = fatGrams * percent / 100.0

            val label = if (index < distribution.labels.size) distribution.labels[index]
            else "Meal ${index + 1}"

            val time = if (index < distribution.defaultTimes.size) distribution.defaultTimes[index]
            else "${7 + index * 3}:00"

            MealSlot(
                name = "meal_$index",
                label = label,
                percentOfDaily = percent,
                calories = mealCalories,
                proteinGrams = mealProtein,
                carbGrams = mealCarbs,
                fatGrams = mealFat,
                suggestedTime = time,
                mealIdeas = getMealIdeasForSlot(label, mealCalories.toInt())
            )
        }

        return MealPlan(
            meals = meals,
            totalCalories = totalCalories,
            mealCount = mealCount,
            distributionName = if (isCustom) "Custom" else distribution.name,
            isCustomDistribution = isCustom
        )
    }

    fun createIFPlan(
        mealPlan: MealPlan,
        ifType: String,
        windowStartHour: Int = 12
    ): IntermittentFastingPlan {
        val (fastingHours, eatingHours) = when (ifType) {
            "16:8" -> 16 to 8
            "18:6" -> 18 to 6
            "20:4" -> 20 to 4
            else -> 0 to 24
        }

        val windowEndHour = (windowStartHour + eatingHours) % 24

        // Redistribute meals within eating window
        val mealsInWindow = if (ifType != "none") {
            redistributeMealsInWindow(mealPlan.meals, windowStartHour, eatingHours)
        } else {
            mealPlan.meals
        }

        return IntermittentFastingPlan(
            type = ifType,
            fastingHours = fastingHours,
            eatingHours = eatingHours,
            windowStartHour = windowStartHour,
            windowEndHour = windowEndHour,
            mealsInWindow = mealsInWindow
        )
    }

    private fun redistributeMealsInWindow(
        meals: List<MealSlot>,
        windowStart: Int,
        windowDuration: Int
    ): List<MealSlot> {
        val mealCount = meals.size
        if (mealCount == 0) return meals

        val spacing = windowDuration.toFloat() / mealCount
        
        return meals.mapIndexed { index, meal ->
            val mealHour = windowStart + (spacing * index + spacing / 2).toInt()
            val adjustedHour = mealHour % 24
            val timeString = formatHour(adjustedHour)
            
            meal.copy(suggestedTime = timeString)
        }
    }

    private fun formatHour(hour: Int): String {
        val adjustedHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour < 12 || hour == 0) "AM" else "PM"
        return "$adjustedHour:00 $amPm"
    }

    fun createWorkoutNutrition(
        totalCalories: Double,
        proteinGrams: Double,
        carbGrams: Double,
        fatGrams: Double,
        workoutTime: String,
        enabled: Boolean
    ): WorkoutNutrition {
        if (!enabled) {
            return WorkoutNutrition(
                enabled = false,
                workoutTime = workoutTime,
                preWorkoutMeal = null,
                postWorkoutMeal = null,
                preWorkoutTiming = "",
                postWorkoutTiming = ""
            )
        }

        // Pre-workout: 15% of daily calories, higher carbs, moderate protein
        val preCalories = totalCalories * 0.15
        val preProtein = proteinGrams * 0.15
        val preCarbs = carbGrams * 0.20 // Higher carbs for energy
        val preFat = fatGrams * 0.10 // Lower fat for faster digestion

        val preMeal = MealSlot(
            name = "pre_workout",
            label = "Pre-Workout",
            percentOfDaily = 15f,
            calories = preCalories,
            proteinGrams = preProtein,
            carbGrams = preCarbs,
            fatGrams = preFat,
            suggestedTime = "1-2 hours before workout",
            mealIdeas = getPreWorkoutMealIdeas(preCalories.toInt())
        )

        // Post-workout: 20% of daily calories, high protein + carbs for recovery
        val postCalories = totalCalories * 0.20
        val postProtein = proteinGrams * 0.25 // Higher protein for recovery
        val postCarbs = carbGrams * 0.25 // Higher carbs to replenish glycogen
        val postFat = fatGrams * 0.10 // Lower fat

        val postMeal = MealSlot(
            name = "post_workout",
            label = "Post-Workout",
            percentOfDaily = 20f,
            calories = postCalories,
            proteinGrams = postProtein,
            carbGrams = postCarbs,
            fatGrams = postFat,
            suggestedTime = "Within 1 hour after workout",
            mealIdeas = getPostWorkoutMealIdeas(postCalories.toInt())
        )

        return WorkoutNutrition(
            enabled = true,
            workoutTime = workoutTime,
            preWorkoutMeal = preMeal,
            postWorkoutMeal = postMeal,
            preWorkoutTiming = "1-2 hours before",
            postWorkoutTiming = "Within 1 hour after"
        )
    }

    private fun getMealIdeasForSlot(label: String, calories: Int): List<MealIdea> {
        return when {
            label.contains("Breakfast", ignoreCase = true) -> getBreakfastIdeas(calories)
            label.contains("Lunch", ignoreCase = true) -> getLunchIdeas(calories)
            label.contains("Dinner", ignoreCase = true) -> getDinnerIdeas(calories)
            label.contains("Snack", ignoreCase = true) -> getSnackIdeas(calories)
            else -> getGenericMealIdeas(calories)
        }
    }

    // ... (rest of the ideas methods from the prompt)
    private fun getBreakfastIdeas(calories: Int): List<MealIdea> {
        return when {
            calories < 300 -> listOf(
                MealIdea("Greek yogurt + berries", 200, "🥣"),
                MealIdea("1 egg + 1 toast", 220, "🍳"),
                MealIdea("Overnight oats (small)", 250, "🥣")
            )
            calories < 450 -> listOf(
                MealIdea("2 eggs + 1 toast + fruit", 350, "🍳"),
                MealIdea("Oatmeal + banana + nuts", 380, "🥣"),
                MealIdea("Smoothie bowl with granola", 400, "🍇")
            )
            calories < 600 -> listOf(
                MealIdea("3 eggs + 2 toast + avocado", 500, "🥑"),
                MealIdea("Pancakes (2) + eggs + fruit", 520, "🥞"),
                MealIdea("Omelet + toast + yogurt", 480, "🍳")
            )
            else -> listOf(
                MealIdea("Full English breakfast", 650, "🍳"),
                MealIdea("Pancakes + bacon + eggs", 700, "🥓"),
                MealIdea("Breakfast burrito + fruit", 620, "🌯")
            )
        }
    }

    private fun getLunchIdeas(calories: Int): List<MealIdea> {
        return when {
            calories < 400 -> listOf(
                MealIdea("Salad with grilled chicken", 350, "🥗"),
                MealIdea("Soup + half sandwich", 320, "🥪"),
                MealIdea("Turkey wrap", 380, "🌯")
            )
            calories < 550 -> listOf(
                MealIdea("Grilled chicken + rice + veggies", 480, "🍗"),
                MealIdea("Tuna sandwich + salad", 450, "🥗"),
                MealIdea("Buddha bowl", 500, "🥙")
            )
            calories < 700 -> listOf(
                MealIdea("Chicken stir-fry + rice", 600, "🍜"),
                MealIdea("Salmon + quinoa + greens", 580, "🐟"),
                MealIdea("Burrito bowl", 620, "🥙")
            )
            else -> listOf(
                MealIdea("Chicken pasta + salad", 750, "🍝"),
                MealIdea("Steak + potato + veggies", 800, "🥩"),
                MealIdea("Double protein bowl", 720, "🥙")
            )
        }
    }

    private fun getDinnerIdeas(calories: Int): List<MealIdea> {
        return when {
            calories < 400 -> listOf(
                MealIdea("Grilled fish + vegetables", 320, "🐟"),
                MealIdea("Chicken salad", 350, "🥗"),
                MealIdea("Vegetable soup + bread", 300, "🍲")
            )
            calories < 550 -> listOf(
                MealIdea("Salmon + sweet potato + broccoli", 480, "🐟"),
                MealIdea("Lean beef stir-fry", 500, "🥩"),
                MealIdea("Chicken curry + rice (small)", 520, "🍛")
            )
            calories < 700 -> listOf(
                MealIdea("Steak + roasted vegetables", 600, "🥩"),
                MealIdea("Pasta with meat sauce", 620, "🍝"),
                MealIdea("Grilled chicken + mashed potato", 580, "🍗")
            )
            else -> listOf(
                MealIdea("Ribeye + loaded potato", 800, "🥩"),
                MealIdea("Full curry dinner + naan", 750, "🍛"),
                MealIdea("BBQ ribs + sides", 850, "🍖")
            )
        }
    }

    private fun getSnackIdeas(calories: Int): List<MealIdea> {
        return when {
            calories < 150 -> listOf(
                MealIdea("Apple + 1 tbsp peanut butter", 130, "🍎"),
                MealIdea("Greek yogurt (small)", 100, "🥛"),
                MealIdea("10 almonds", 70, "🥜")
            )
            calories < 250 -> listOf(
                MealIdea("Protein bar", 200, "🍫"),
                MealIdea("Banana + peanut butter", 220, "🍌"),
                MealIdea("Trail mix (small handful)", 180, "🥜")
            )
            calories < 350 -> listOf(
                MealIdea("Protein shake + fruit", 280, "🥤"),
                MealIdea("Cottage cheese + berries", 250, "🥣"),
                MealIdea("Avocado toast", 300, "🥑")
            )
            else -> listOf(
                MealIdea("Smoothie with protein", 380, "🥤"),
                MealIdea("Rice cakes + PB + banana", 350, "🍚"),
                MealIdea("Mini meal: chicken + rice", 400, "🍗")
            )
        }
    }

    private fun getPreWorkoutMealIdeas(calories: Int): List<MealIdea> {
        return listOf(
            MealIdea("Banana + small handful of nuts", minOf(calories, 200), "🍌"),
            MealIdea("Oatmeal + honey", minOf(calories, 250), "🥣"),
            MealIdea("Toast + peanut butter", minOf(calories, 220), "🍞"),
            MealIdea("Rice cakes + banana", minOf(calories, 180), "🍚")
        )
    }

    private fun getPostWorkoutMealIdeas(calories: Int): List<MealIdea> {
        return listOf(
            MealIdea("Protein shake + banana", minOf(calories, 350), "🥤"),
            MealIdea("Chicken breast + rice", minOf(calories, 450), "🍗"),
            MealIdea("Greek yogurt + granola + berries", minOf(calories, 380), "🥣"),
            MealIdea("Eggs + toast + fruit", minOf(calories, 400), "🍳")
        )
    }

    private fun getGenericMealIdeas(calories: Int): List<MealIdea> {
        return when {
            calories < 300 -> listOf(
                MealIdea("Light snack option", calories, "🍽️"),
                MealIdea("Fresh fruit + nuts", calories - 50, "🍇")
            )
            calories < 500 -> listOf(
                MealIdea("Balanced mini meal", calories, "🍽️"),
                MealIdea("Protein + veggies", calories - 50, "🥗")
            )
            else -> listOf(
                MealIdea("Full balanced meal", calories, "🍽️"),
                MealIdea("Protein + carbs + veggies", calories - 50, "🥙")
            )
        }
    }
}
