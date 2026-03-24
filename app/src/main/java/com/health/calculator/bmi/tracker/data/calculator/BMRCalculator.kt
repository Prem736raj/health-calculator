// File: com/health/calculator/bmi/tracker/data/calculator/BMRCalculator.kt
package com.health.calculator.bmi.tracker.data.calculator

import com.health.calculator.bmi.tracker.data.model.BMRFormula

object BMRCalculator {

    /**
     * Calculate BMR using the specified formula.
     * @param weightKg Weight in kilograms
     * @param heightCm Height in centimeters
     * @param age Age in years
     * @param isMale True for male, false for female
     * @param bodyFatPercentage Body fat % (required for Katch-McArdle & Cunningham)
     * @param formula The BMR formula to use
     * @return BMR in kcal/day, or null if inputs are invalid
     */
    fun calculate(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        isMale: Boolean,
        bodyFatPercentage: Float = 0f,
        formula: BMRFormula
    ): Float? {
        if (weightKg <= 0 || heightCm <= 0 || age <= 0) return null

        return when (formula) {
            BMRFormula.HARRIS_BENEDICT_ORIGINAL -> harrisBenedictOriginal(weightKg, heightCm, age, isMale)
            BMRFormula.HARRIS_BENEDICT_REVISED -> harrisBenedictRevised(weightKg, heightCm, age, isMale)
            BMRFormula.MIFFLIN_ST_JEOR -> mifflinStJeor(weightKg, heightCm, age, isMale)
            BMRFormula.WHO_FAO_UNU -> whoFaoUnu(weightKg, age, isMale)
            BMRFormula.KATCH_MCARDLE -> {
                if (bodyFatPercentage <= 0 || bodyFatPercentage >= 80) return null
                katchMcArdle(weightKg, bodyFatPercentage)
            }
            BMRFormula.CUNNINGHAM -> {
                if (bodyFatPercentage <= 0 || bodyFatPercentage >= 80) return null
                cunningham(weightKg, bodyFatPercentage)
            }
        }
    }

    /**
     * Harris-Benedict Original (1919)
     * Men:   BMR = 66.4730 + (13.7516 × W) + (5.0033 × H) − (6.7550 × A)
     * Women: BMR = 655.0955 + (9.5634 × W) + (1.8496 × H) − (4.6756 × A)
     */
    private fun harrisBenedictOriginal(weightKg: Float, heightCm: Float, age: Int, isMale: Boolean): Float {
        return if (isMale) {
            66.4730f + (13.7516f * weightKg) + (5.0033f * heightCm) - (6.7550f * age)
        } else {
            655.0955f + (9.5634f * weightKg) + (1.8496f * heightCm) - (4.6756f * age)
        }
    }

    /**
     * Harris-Benedict Revised (Roza & Shizgal, 1984)
     * Men:   BMR = 88.362 + (13.397 × W) + (4.799 × H) − (5.677 × A)
     * Women: BMR = 447.593 + (9.247 × W) + (3.098 × H) − (4.330 × A)
     */
    private fun harrisBenedictRevised(weightKg: Float, heightCm: Float, age: Int, isMale: Boolean): Float {
        return if (isMale) {
            88.362f + (13.397f * weightKg) + (4.799f * heightCm) - (5.677f * age)
        } else {
            447.593f + (9.247f * weightKg) + (3.098f * heightCm) - (4.330f * age)
        }
    }

    /**
     * Mifflin-St Jeor (1990) - RECOMMENDED
     * Men:   BMR = (10 × W) + (6.25 × H) − (5 × A) + 5
     * Women: BMR = (10 × W) + (6.25 × H) − (5 × A) − 161
     */
    private fun mifflinStJeor(weightKg: Float, heightCm: Float, age: Int, isMale: Boolean): Float {
        val base = (10f * weightKg) + (6.25f * heightCm) - (5f * age)
        return if (isMale) base + 5f else base - 161f
    }

    /**
     * WHO/FAO/UNU (1985) - Age-grouped equations
     * Uses weight in kg only (height not used in most age groups)
     */
    private fun whoFaoUnu(weightKg: Float, age: Int, isMale: Boolean): Float {
        return if (isMale) {
            when {
                age < 3 -> 60.9f * weightKg - 54f
                age < 10 -> 22.7f * weightKg + 495f
                age < 18 -> 17.5f * weightKg + 651f
                age < 30 -> 15.3f * weightKg + 679f
                age < 60 -> 11.6f * weightKg + 879f
                else -> 13.5f * weightKg + 487f
            }
        } else {
            when {
                age < 3 -> 61.0f * weightKg - 51f
                age < 10 -> 22.5f * weightKg + 499f
                age < 18 -> 12.2f * weightKg + 746f
                age < 30 -> 14.7f * weightKg + 496f
                age < 60 -> 8.7f * weightKg + 829f
                else -> 10.5f * weightKg + 596f
            }
        }
    }

    /**
     * Katch-McArdle (1996)
     * BMR = 370 + (21.6 × LBM)
     * LBM = Weight × (1 - BodyFat%/100)
     */
    private fun katchMcArdle(weightKg: Float, bodyFatPercentage: Float): Float {
        val leanBodyMass = weightKg * (1f - bodyFatPercentage / 100f)
        return 370f + (21.6f * leanBodyMass)
    }

    /**
     * Cunningham (1991)
     * BMR = 500 + (22 × LBM)
     * LBM = Weight × (1 - BodyFat%/100)
     */
    private fun cunningham(weightKg: Float, bodyFatPercentage: Float): Float {
        val leanBodyMass = weightKg * (1f - bodyFatPercentage / 100f)
        return 500f + (22f * leanBodyMass)
    }

    // Validation helpers
    fun validateWeight(weightKg: Float): String? {
        return when {
            weightKg <= 0f -> "Please enter a valid weight"
            weightKg < 10f -> "Weight seems too low. Please check"
            weightKg > 500f -> "Weight exceeds maximum range"
            else -> null
        }
    }

    fun validateHeight(heightCm: Float): String? {
        return when {
            heightCm <= 0f -> "Please enter a valid height"
            heightCm < 30f -> "Height seems too low. Please check"
            heightCm > 280f -> "Height exceeds maximum range"
            else -> null
        }
    }

    fun validateAge(age: Int): String? {
        return when {
            age <= 0 -> "Please enter a valid age"
            age < 2 -> "Age must be 2 or above"
            age > 120 -> "Please enter a valid age (2-120)"
            else -> null
        }
    }

    fun validateBodyFat(percentage: Float, isRequired: Boolean): String? {
        if (!isRequired) return null
        return when {
            percentage <= 0f -> "Please enter body fat percentage"
            percentage < 2f -> "Body fat percentage seems too low"
            percentage > 75f -> "Body fat percentage seems too high"
            else -> null
        }
    }
}
