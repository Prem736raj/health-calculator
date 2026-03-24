// data/model/WaterIntakeData.kt
package com.health.calculator.bmi.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Enums for Water Intake Calculator
enum class WaterActivityLevel(val displayName: String, val multiplier: Float) {
    SEDENTARY("Sedentary (little/no exercise)", 1.0f),
    LIGHT("Light exercise (1-2 days/week)", 1.1f),
    MODERATE("Moderate exercise (3-5 days/week)", 1.2f),
    HEAVY("Heavy exercise (6-7 days/week)", 1.4f),
    ATHLETE("Athlete/Very heavy exercise", 1.5f)
}

enum class ClimateType(val displayName: String, val multiplier: Float) {
    COLD("Cold", 0.9f),
    TEMPERATE("Temperate/Mild", 1.0f),
    HOT("Hot", 1.1f),
    VERY_HOT("Very Hot/Humid", 1.2f)
}

enum class HealthStatus(val displayName: String, val additionalMl: Int) {
    NORMAL("Normal", 0),
    PREGNANT("Pregnant", 300),
    BREASTFEEDING("Breastfeeding", 700),
    ILLNESS("Illness/Fever", 500)
}

@Entity(tableName = "water_intake_calculations")
data class WaterIntakeCalculation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Float,
    val age: Int,
    val gender: String,
    val activityLevel: String,
    val climate: String,
    val healthStatus: String,
    val recommendedIntakeMl: Int,
    val recommendedIntakeOz: Float,
    val recommendedGlasses: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_intake_log")
data class WaterIntakeLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
