// app/src/main/java/com/health/calculator/bmi/tracker/core/constants/AppConstants.kt

package com.health.calculator.bmi.tracker.core.constants

object AppConstants {
    const val APP_NAME = "Health Calculator"
    const val APP_SUBTITLE = "BMI Tracker & More"
    const val APP_VERSION = "1.0.0"

    // DataStore
    const val DATASTORE_NAME = "health_calculator_preferences"

    // Database
    const val DATABASE_NAME = "health_calculator_db"
    const val DATABASE_VERSION = 1

    // Medical Disclaimer
    const val MEDICAL_DISCLAIMER_SHORT =
        "This app is not a medical device. Results are for informational purposes only."
    const val MEDICAL_DISCLAIMER_FULL =
        "This app is intended for informational and educational purposes only. " +
                "It is not a substitute for professional medical advice, diagnosis, or treatment. " +
                "Always seek the advice of your physician or other qualified health provider " +
                "with any questions you may have regarding a medical condition. " +
                "Never disregard professional medical advice or delay in seeking it " +
                "because of something you have read or calculated in this app."

    // Calculator IDs
    const val CALC_BMI = "bmi"
    const val CALC_BMR = "bmr"
    const val CALC_BLOOD_PRESSURE = "blood_pressure"
    const val CALC_WAIST_HIP = "waist_hip"
    const val CALC_WATER_INTAKE = "water_intake"
    const val CALC_METABOLIC_SYNDROME = "metabolic_syndrome"
    const val CALC_BSA = "bsa"
    const val CALC_IDEAL_WEIGHT = "ideal_weight"
    const val CALC_DAILY_CALORIE = "daily_calorie"
    const val CALC_HEART_RATE_ZONE = "heart_rate_zone"

    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 200
    const val ANIMATION_DURATION_MEDIUM = 400
    const val ANIMATION_DURATION_LONG = 600

    // Limits
    const val MAX_HISTORY_ITEMS = 1000
    const val MAX_WATER_GLASSES_PER_DAY = 30
    const val MAX_BP_READINGS_PER_DAY = 20
}
