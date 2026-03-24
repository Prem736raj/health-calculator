package com.health.calculator.bmi.tracker.data.model

data class BpHomeCardInfo(
    val hasReading: Boolean = false,
    val lastSystolic: Int = 0,
    val lastDiastolic: Int = 0,
    val lastCategory: BpCategory = BpCategory.OPTIMAL,
    val lastReadingTime: String = "",
    val isConcerning: Boolean = false,
    val streakDays: Int = 0
)
