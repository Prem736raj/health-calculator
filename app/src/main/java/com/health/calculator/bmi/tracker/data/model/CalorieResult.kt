package com.health.calculator.bmi.tracker.data.model

data class CalorieResult(
    val bmrMifflin: Double,
    val bmrKatchMcArdle: Double?,
    val usedBmr: Double,
    val bmrFormulaUsed: String,
    val activityMultiplier: Double,
    val activityLevelName: String,
    val tdee: Double,
    val tef: Double,
    val goalCalories: Double,
    val goalName: String,
    val goalAdjustment: Int,
    val weeklyChangeKg: Double,
    val weightKg: Double,
    val heightCm: Double,
    val age: Int,
    val gender: String,
    val bodyFatPercent: Double?,
    val isBelowMinimum: Boolean,
    val minimumCalories: Double,
    val safeGoalCalories: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    val bmrCalories: Double get() = usedBmr
    val activityCalories: Double get() = tdee - usedBmr - tef
    val bmrPercent: Float get() = ((usedBmr / tdee) * 100).toFloat()
    val activityPercent: Float get() = ((activityCalories / tdee) * 100).toFloat()
    val tefPercent: Float get() = ((tef / tdee) * 100).toFloat()

    val dailyDeficitOrSurplus: Int get() = goalAdjustment
    val isDeficit: Boolean get() = goalAdjustment < 0
    val isSurplus: Boolean get() = goalAdjustment > 0
    val isMaintenance: Boolean get() = goalAdjustment == 0

    val weeklyChangeDisplay: Double get() = kotlin.math.abs(weeklyChangeKg)
    val monthlyChangeKg: Double get() = kotlin.math.abs(weeklyChangeKg * 4.33)

    val caloriesPerHour: Double get() = goalCalories / 24.0
    val caloriesPerMeal3: Double get() = goalCalories / 3.0
    val caloriesPerMeal4: Double get() = goalCalories / 4.0
    val caloriesPerMeal5: Double get() = goalCalories / 5.0
}
