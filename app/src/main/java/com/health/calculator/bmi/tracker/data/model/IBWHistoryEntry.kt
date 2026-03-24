package com.health.calculator.bmi.tracker.data.model

data class IBWHistoryEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val heightCm: Double,
    val gender: String,
    val frameSize: String,
    val age: Int?,
    val currentWeightKg: Double?,
    val devineKg: Double,
    val robinsonKg: Double,
    val millerKg: Double,
    val hamwiKg: Double,
    val brocaKg: Double,
    val bmiLowerKg: Double,
    val bmiUpperKg: Double,
    val frameAdjustedDevineKg: Double,
    val leanBodyWeightKg: Double?,
    val adjustedBodyWeightKg: Double?,
    val weightCategoryPercent: Double?,
    val weightCategory: String?,
    val goalWeightKg: Double?
)
