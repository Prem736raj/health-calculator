package com.health.calculator.bmi.tracker.widget

import androidx.annotation.DrawableRes
import com.health.calculator.bmi.tracker.R

enum class CalculatorType(
    val displayName: String,
    val shortName: String,
    val navDestination: String,
    @DrawableRes val iconRes: Int,
    val accentColorHex: String
) {
    BMI(
        displayName    = "BMI Calculator",
        shortName      = "BMI",
        navDestination = "bmi_calculator",
        iconRes        = R.drawable.ic_calc_bmi,
        accentColorHex = "#4CAF50"
    ),
    BLOOD_PRESSURE(
        displayName    = "Blood Pressure",
        shortName      = "BP",
        navDestination = "blood_pressure_checker",
        iconRes        = R.drawable.ic_calc_bp,
        accentColorHex = "#F44336"
    ),
    WATER(
        displayName    = "Water Intake",
        shortName      = "Water",
        navDestination = "water_tracker",
        iconRes        = R.drawable.ic_calc_water,
        accentColorHex = "#2196F3"
    ),
    CALORIES(
        displayName    = "Calorie Tracker",
        shortName      = "Calories",
        navDestination = "calorie_calculator",
        iconRes        = R.drawable.ic_calc_calories,
        accentColorHex = "#FF9800"
    ),
    HEART_RATE(
        displayName    = "Heart Rate Zones",
        shortName      = "HR Zone",
        navDestination = "heart_rate_calculator",
        iconRes        = R.drawable.ic_calc_heart_rate,
        accentColorHex = "#E91E63"
    ),
    BMR(
        displayName    = "BMR Calculator",
        shortName      = "BMR",
        navDestination = "bmr_calculator",
        iconRes        = R.drawable.ic_calc_bmi,
        accentColorHex = "#9C27B0"
    );

    companion object {
        fun fromName(name: String): CalculatorType {
            return values().find { it.name == name } ?: BMI
        }

        fun spinnerLabels(): Array<String> =
            values().map { it.displayName }.toTypedArray()
    }
}
