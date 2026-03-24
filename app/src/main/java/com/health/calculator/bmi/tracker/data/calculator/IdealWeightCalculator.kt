package com.health.calculator.bmi.tracker.data.calculator

import kotlin.math.roundToInt

/**
 * Calculator for Ideal Body Weight (IBW) using various scientific formulas.
 */
object IdealWeightCalculator {

    /**
     * Common formulas for Ideal Body Weight (IBW):
     * All formulas below compute weight in kg based on height in inches.
     */
    
    /**
     * Robinson Formula (1983)
     * Male: 52 kg + 1.9 kg per inch over 5 feet
     * Female: 49 kg + 1.7 kg per inch over 5 feet
     */
    fun calculateRobinson(heightCm: Float, isMale: Boolean): Float {
        val heightInches = heightCm / 2.54f
        val inchesOver5Feet = (heightInches - 60f).coerceAtLeast(0f)
        return if (isMale) {
            52f + (1.9f * inchesOver5Feet)
        } else {
            49f + (1.7f * inchesOver5Feet)
        }
    }

    /**
     * Miller Formula (1983)
     * Male: 56.2 kg + 1.41 kg per inch over 5 feet
     * Female: 53.1 kg + 1.36 kg per inch over 5 feet
     */
    fun calculateMiller(heightCm: Float, isMale: Boolean): Float {
        val heightInches = heightCm / 2.54f
        val inchesOver5Feet = (heightInches - 60f).coerceAtLeast(0f)
        return if (isMale) {
            56.2f + (1.41f * inchesOver5Feet)
        } else {
            53.1f + (1.36f * inchesOver5Feet)
        }
    }

    /**
     * Devine Formula (1974) - Most widely used
     * Male: 50.0 kg + 2.3 kg per inch over 5 feet
     * Female: 45.5 kg + 2.3 kg per inch over 5 feet
     */
    fun calculateDevine(heightCm: Float, isMale: Boolean): Float {
        val heightInches = heightCm / 2.54f
        val inchesOver5Feet = (heightInches - 60f).coerceAtLeast(0f)
        return if (isMale) {
            50f + (2.3f * inchesOver5Feet)
        } else {
            45.5f + (2.3f * inchesOver5Feet)
        }
    }

    /**
     * Hamwi Formula (1964)
     * Male: 48.0 kg + 2.7 kg per inch over 5 feet
     * Female: 45.5 kg + 2.2 kg per inch over 5 feet
     */
    fun calculateHamwi(heightCm: Float, isMale: Boolean): Float {
        val heightInches = heightCm / 2.54f
        val inchesOver5Feet = (heightInches - 60f).coerceAtLeast(0f)
        return if (isMale) {
            48f + (2.7f * inchesOver5Feet)
        } else {
            45.5f + (2.2f * inchesOver5Feet)
        }
    }

    /**
     * Calculate healthy weight range based on WHO BMI (18.5 - 25)
     */
    fun calculateHealthyRange(heightCm: Float): Pair<Float, Float> {
        val heightMeters = heightCm / 100f
        val minWeight = 18.5f * (heightMeters * heightMeters)
        val maxWeight = 25.0f * (heightMeters * heightMeters)
        return Pair(minWeight, maxWeight)
    }

    /**
     * Validation logic
     */
    fun validateHeight(heightCm: Float): String? {
        return when {
            heightCm <= 0 -> "Please enter your height"
            heightCm < 30 -> "Height is too low"
            heightCm > 300 -> "Height is too high"
            else -> null
        }
    }

    fun validateAge(age: Int): String? {
        return when {
            age <= 0 -> "Please enter your age"
            age < 2 -> "Age must be at least 2"
            age > 120 -> "Please enter a valid age"
            else -> null
        }
    }
}
