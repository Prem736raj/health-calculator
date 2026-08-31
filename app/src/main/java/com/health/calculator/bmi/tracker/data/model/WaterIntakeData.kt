// data/model/WaterIntakeData.kt
package com.health.calculator.bmi.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.roundToInt

// Enums for Water Intake Calculator
enum class WaterActivityLevel(val displayName: String, val multiplier: Float) {
    SEDENTARY("Sedentary (little/no exercise)", 0.0f),
    LIGHT("Light exercise (1-2 days/week)", 0.0f),
    MODERATE("Moderate exercise (3-5 days/week)", 250.0f),
    HEAVY("Heavy exercise (6-7 days/week)", 500.0f),
    ATHLETE("Athlete/Very heavy exercise", 750.0f)
}

enum class ClimateType(val displayName: String, val multiplier: Float) {
    COLD("Cold", 0.0f),
    TEMPERATE("Temperate/Mild", 0.0f),
    HOT("Hot", 250.0f),
    VERY_HOT("Very Hot/Humid", 500.0f)
}

enum class HealthStatus(val displayName: String, val additionalMl: Int) {
    NORMAL("Normal", 0),
    PREGNANT("Pregnant", 300),
    BREASTFEEDING("Breastfeeding", 700),
    // Illness-related fluid needs vary widely and must not be auto-prescribed.
    ILLNESS("Illness/Fever (ask a clinician)", 0)
}

/**
 * National Academies adequate-intake estimates for beverages for healthy
 * adults. Food contributes to total water, so these are not universal targets.
 */
object WaterIntakeCalculator {
    fun beverageTargetMl(
        gender: String,
        activity: WaterActivityLevel,
        climate: ClimateType,
        healthStatus: HealthStatus
    ): Int {
        val baseline = if (gender.equals("Female", ignoreCase = true)) 2200 else 3000
        return (baseline + activity.multiplier + climate.multiplier + healthStatus.additionalMl)
            .roundToInt()
            .coerceIn(1000, 6000)
    }
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
