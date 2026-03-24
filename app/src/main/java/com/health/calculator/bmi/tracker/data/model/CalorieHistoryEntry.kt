package com.health.calculator.bmi.tracker.data.model

data class CalorieHistoryEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val bmr: Double,
    val tdee: Double,
    val goalCalories: Double,
    val goalName: String,
    val activityLevel: String,
    val formulaUsed: String,
    val weightKg: Double,
    val heightCm: Double,
    val age: Int,
    val gender: String,
    val bodyFatPercent: Double?,
    val weeklyChangeKg: Double
)
