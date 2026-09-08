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
        require(heightCm.isFinite() && heightCm in 100.0..250.0) {
            "Height must be between 100 and 250 cm"
        }
        require(currentWeightKg == null || (currentWeightKg.isFinite() && currentWeightKg > 0.0)) {
            "Current weight must be a positive finite value"
        }
        require(age == null || age in 18..120) { "These adult estimates support ages 18–120" }
        val heightInches = heightCm / 2.54
        val heightM = heightCm / 100.0
        val isMale = gender.equals("Male", ignoreCase = true)

        // Devine, Robinson, Miller, Hamwi and Broca are historical adult
        // equations built around a 60-inch reference. Do not invent a
        // proportional extrapolation for shorter adults; expose the BMI
        // reference range below instead.
        val isVeryShort = heightInches < 60.0
        val heightDelta = heightInches - 60.0
        val historicalFormulaValue: (Double) -> Double = { formula ->
            if (isVeryShort) Double.NaN else formula
        }

        // 1. Devine Formula (1974)
        val devineBase = if (isMale) 50.0 else 45.5
        val devine = historicalFormulaValue(devineBase + 2.3 * heightDelta)

        // 2. Robinson Formula (1983)
        val robinsonBase = if (isMale) 52.0 else 49.0
        val robinsonFactor = if (isMale) 1.9 else 1.7
        val robinson = historicalFormulaValue(robinsonBase + robinsonFactor * heightDelta)

        // 3. Miller Formula (1983)
        val millerBase = if (isMale) 56.2 else 53.1
        val millerFactor = if (isMale) 1.41 else 1.36
        val miller = historicalFormulaValue(millerBase + millerFactor * heightDelta)

        // 4. Hamwi Formula (1964)
        val hamwiBase = if (isMale) 48.0 else 45.5
        val hamwiFactor = if (isMale) 2.7 else 2.2
        val hamwi = historicalFormulaValue(hamwiBase + hamwiFactor * heightDelta)

        // 5. BMI-based reference range (informational adult context)
        val bmiLower = 18.5 * heightM.pow(2)
        val bmiUpper = 24.9 * heightM.pow(2)

        // 6. Broca Index with gender adjustment
        val brocaRaw = heightCm - 100.0
        val broca = if (isVeryShort) Double.NaN else if (isMale) {
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
        val frameAdjustedDevine = if (devine.isFinite()) {
            (devine * frameMultiplier).coerceAtLeast(MINIMUM_IBW_KG)
        } else {
            Double.NaN
        }

        // Height warning flags
        val heightWarning = when {
            heightInches < MINIMUM_HEIGHT_INCHES ->
                "Your height is outside the usual validation range, and historical equations are unavailable below 60 inches. The BMI reference range remains available."
            heightInches > MAXIMUM_HEIGHT_INCHES ->
                "Your height is outside the usual validation range for these formulas. Results may be less comparable across equations."
            isVeryShort ->
                "Historical IBW equations are not reported below 60 inches because extrapolating them would invent precision. The BMI reference range remains available."
            else -> null
        }

        return IBWResult(
            devineKg = devine.takeIf { it.isFinite() }?.coerceAtLeast(MINIMUM_IBW_KG) ?: Double.NaN,
            robinsonKg = robinson.takeIf { it.isFinite() }?.coerceAtLeast(MINIMUM_IBW_KG) ?: Double.NaN,
            millerKg = miller.takeIf { it.isFinite() }?.coerceAtLeast(MINIMUM_IBW_KG) ?: Double.NaN,
            hamwiKg = hamwi.takeIf { it.isFinite() }?.coerceAtLeast(MINIMUM_IBW_KG) ?: Double.NaN,
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
