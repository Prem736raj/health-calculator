package com.health.calculator.bmi.tracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_records")
data class PersonalRecord(
    @PrimaryKey
    val recordType: String, // PersonalRecordType.name
    val value: Double,
    val displayValue: String,
    val achievedAt: Long = System.currentTimeMillis(),
    val previousValue: Double? = null,
    val previousDisplayValue: String? = null
)

enum class PersonalRecordType(
    val displayName: String,
    val icon: String,
    val description: String,
    val lowerIsBetter: Boolean = false
) {
    BEST_BMI(
        displayName = "Best BMI",
        icon = "⚖️",
        description = "Closest to middle of normal range (21.7)",
        lowerIsBetter = false // special: closest to 21.7
    ),
    BEST_BP_SYSTOLIC(
        displayName = "Best Blood Pressure",
        icon = "❤️",
        description = "Closest to optimal (115/75)",
        lowerIsBetter = false // special: closest to optimal
    ),
    BEST_WHR(
        displayName = "Best WHR",
        icon = "📏",
        description = "Lowest risk waist-to-hip ratio",
        lowerIsBetter = true
    ),
    LOWEST_RESTING_HR(
        displayName = "Lowest Resting HR",
        icon = "💓",
        description = "Lower resting heart rate indicates better fitness",
        lowerIsBetter = true
    ),
    LONGEST_WATER_STREAK(
        displayName = "Longest Water Streak",
        icon = "💧",
        description = "Most consecutive days meeting water goal",
        lowerIsBetter = false
    ),
    LONGEST_TRACKING_STREAK(
        displayName = "Longest Tracking Streak",
        icon = "📅",
        description = "Most consecutive days using the app",
        lowerIsBetter = false
    ),
    HIGHEST_HEALTH_SCORE(
        displayName = "Highest Health Score",
        icon = "🏆",
        description = "Best overall health score achieved",
        lowerIsBetter = false
    ),
    MOST_WATER_SINGLE_DAY(
        displayName = "Most Water in a Day",
        icon = "🌊",
        description = "Highest daily water intake",
        lowerIsBetter = false
    ),
    LOWEST_WEIGHT(
        displayName = "Lowest Weight",
        icon = "🪶",
        description = "Lowest recorded weight",
        lowerIsBetter = true
    );

    fun isNewRecord(currentValue: Double, existingValue: Double?): Boolean {
        if (existingValue == null) return true
        return when (this) {
            BEST_BMI -> {
                val idealBmi = 21.7
                kotlin.math.abs(currentValue - idealBmi) < kotlin.math.abs(existingValue - idealBmi)
            }
            BEST_BP_SYSTOLIC -> {
                val idealSystolic = 115.0
                kotlin.math.abs(currentValue - idealSystolic) < kotlin.math.abs(existingValue - idealSystolic)
            }
            else -> if (lowerIsBetter) currentValue < existingValue else currentValue > existingValue
        }
    }
}
