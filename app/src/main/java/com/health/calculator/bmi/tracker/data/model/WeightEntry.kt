package com.health.calculator.bmi.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weightKg: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String? = null,
    val source: WeightSource = WeightSource.MANUAL
) {
    val weightLbs: Double
        get() = weightKg * 2.20462

    fun formattedWeight(useMetric: Boolean): String {
        return if (useMetric) {
            String.format("%.1f kg", weightKg)
        } else {
            String.format("%.1f lbs", weightLbs)
        }
    }
}

enum class WeightSource(val displayName: String) {
    MANUAL("Manual Entry"),
    BMI_CALCULATOR("BMI Calculator"),
    BMR_CALCULATOR("BMR Calculator"),
    IBW_CALCULATOR("Ideal Weight Calculator"),
    CALORIE_CALCULATOR("Calorie Calculator"),
    PROFILE("Profile")
}
