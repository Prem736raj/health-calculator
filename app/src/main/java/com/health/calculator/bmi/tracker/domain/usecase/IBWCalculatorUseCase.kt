package com.health.calculator.bmi.tracker.domain.usecase

import com.health.calculator.bmi.tracker.data.model.IBWResult
import kotlin.math.pow

class IBWCalculatorUseCase {

    companion object {
        const val MINIMUM_IBW_KG = 20.0 // Absolute minimum floor
        const val MINIMUM_HEIGHT_INCHES = 48.0 // 4 feet - below this formulas are unreliable
        const val MAXIMUM_HEIGHT_INCHES = 96.0 // 8 feet
    }

    fun calculate(
        heightCm: Double,
        gender: String,
        frameSize: String,
        currentWeightKg: Double? = null,
        age: Int? = null
    ): IBWResult {
        val heightInches = heightCm / 2.54
        val heightM = heightCm / 100.0
        val isMale = gender.equals("Male", ignoreCase = true)

        // Edge case: very short height - formulas use (height - 60 inches)
        // which goes negative below 60 inches
        val heightDelta = (heightInches - 60.0).coerceAtLeast(0.0)

        // For very short people, use a scaling approach
        val isVeryShort = heightInches < 60.0
        val shortScaleFactor = if (isVeryShort) heightInches / 60.0 else 1.0

        // 1. Devine Formula (1974)
        val devineBase = if (isMale) 50.0 else 45.5
        val devine = if (isVeryShort) {
            devineBase * shortScaleFactor
        } else {
            devineBase + 2.3 * heightDelta
        }

        // 2. Robinson Formula (1983)
        val robinsonBase = if (isMale) 52.0 else 49.0
        val robinsonFactor = if (isMale) 1.9 else 1.7
        val robinson = if (isVeryShort) {
            robinsonBase * shortScaleFactor
        } else {
            robinsonBase + robinsonFactor * heightDelta
        }

        // 3. Miller Formula (1983)
        val millerBase = if (isMale) 56.2 else 53.1
        val millerFactor = if (isMale) 1.41 else 1.36
        val miller = if (isVeryShort) {
            millerBase * shortScaleFactor
        } else {
            millerBase + millerFactor * heightDelta
        }

        // 4. Hamwi Formula (1964)
        val hamwiBase = if (isMale) 48.0 else 45.5
        val hamwiFactor = if (isMale) 2.7 else 2.2
        val hamwi = if (isVeryShort) {
            hamwiBase * shortScaleFactor
        } else {
            hamwiBase + hamwiFactor * heightDelta
        }

        // 5. BMI-based Range (always reliable)
        val bmiLower = 18.5 * heightM.pow(2)
        val bmiUpper = 24.9 * heightM.pow(2)

        // 6. Broca Index with gender adjustment
        val brocaRaw = heightCm - 100.0
        val broca = if (isMale) {
            (brocaRaw * 0.9).coerceAtLeast(MINIMUM_IBW_KG)
        } else {
            (brocaRaw * 0.85).coerceAtLeast(MINIMUM_IBW_KG)
        }

        // Frame size adjustment on Devine
        val frameMultiplier = when (frameSize.lowercase()) {
            "small" -> 0.90
            "large" -> 1.10
            else -> 1.0
        }
        val frameAdjustedDevine = (devine * frameMultiplier).coerceAtLeast(MINIMUM_IBW_KG)

        // Height warning flags
        val heightWarning = when {
            heightInches < MINIMUM_HEIGHT_INCHES ->
                "Your height is below the reliable range for these formulas. Results are estimated using scaling. The BMI-based range is most reliable for your height."
            heightInches > MAXIMUM_HEIGHT_INCHES ->
                "Your height is above the typical range for these formulas. Results may be less accurate. The BMI-based range is most reliable."
            isVeryShort ->
                "Standard IBW formulas were designed for heights above 5 feet (152 cm). Results have been adjusted using proportional scaling."
            else -> null
        }

        return IBWResult(
            devineKg = devine.coerceAtLeast(MINIMUM_IBW_KG),
            robinsonKg = robinson.coerceAtLeast(MINIMUM_IBW_KG),
            millerKg = miller.coerceAtLeast(MINIMUM_IBW_KG),
            hamwiKg = hamwi.coerceAtLeast(MINIMUM_IBW_KG),
            brocaKg = broca,
            bmiLowerKg = bmiLower,
            bmiUpperKg = bmiUpper,
            frameAdjustedDevineKg = frameAdjustedDevine,
            currentWeightKg = currentWeightKg,
            heightCm = heightCm,
            gender = gender,
            frameSize = frameSize,
            age = age,
            heightWarning = heightWarning
        )
    }
}
