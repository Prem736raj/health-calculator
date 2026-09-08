// File: com/health/calculator/bmi/tracker/data/calculator/BMRValidation.kt
package com.health.calculator.bmi.tracker.data.calculator

import com.health.calculator.bmi.tracker.data.model.BMRFormula

object BMRValidation {

    data class ValidationResult(
        val isValid: Boolean,
        val weightError: String? = null,
        val heightError: String? = null,
        val ageError: String? = null,
        val bodyFatError: String? = null,
        val warnings: List<String> = emptyList()
    )

    fun validateInputs(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        bodyFatPercentage: Float,
        formula: BMRFormula
    ): ValidationResult {
        val warnings = mutableListOf<String>()

        val weightError = when {
            !weightKg.isFinite() || weightKg <= 0f -> "Please enter a valid weight"
            weightKg < 10f -> "Weight is too low for accurate BMR calculation"
            weightKg > 500f -> "Weight exceeds maximum range (500 kg)"
            weightKg < 25f -> {
                warnings.add("Very low weight may produce less accurate BMR estimates")
                null
            }
            weightKg > 300f -> {
                warnings.add("Very high weight — BMR formulas may be less accurate at extremes")
                null
            }
            else -> null
        }

        val heightError = when {
            formula == BMRFormula.WHO_FAO_UNU -> null // WHO doesn't need height
            !heightCm.isFinite() || heightCm <= 0f -> "Please enter a valid height"
            heightCm < 100f -> "Adult estimates require a height of at least 100 cm"
            heightCm > 250f -> "Please enter a height no greater than 250 cm"
            heightCm > 220f -> {
                warnings.add("Very tall height — results may be less comparable across equations")
                null
            }
            else -> null
        }

        val ageError = when {
            age <= 0 -> "Please enter a valid age"
            age < MedicalCalculationPolicy.ADULT_MIN_AGE -> "Adult estimates are for ages ${MedicalCalculationPolicy.ADULT_MIN_AGE} and over"
            age > MedicalCalculationPolicy.MAX_AGE -> "Please enter a valid age (${MedicalCalculationPolicy.ADULT_MIN_AGE}-${MedicalCalculationPolicy.MAX_AGE})"
            age > 80 -> {
                warnings.add("BMR estimates become less accurate at advanced ages")
                null
            }
            else -> null
        }

        val bodyFatError = when {
            !formula.requiresBodyFat -> null
            !bodyFatPercentage.isFinite() || bodyFatPercentage <= 0f -> "Please enter body fat percentage"
            bodyFatPercentage < 2f -> "Body fat percentage is too low (minimum essential fat is ~2-3%)"
            bodyFatPercentage > 75f -> "Body fat percentage seems too high (maximum ~70%)"
            bodyFatPercentage < 5f -> {
                warnings.add("Very low body fat — this level is rarely sustainable")
                null
            }
            bodyFatPercentage > 60f -> {
                warnings.add("Very high body fat — lean body mass formulas may be less precise")
                null
            }
            else -> null
        }

        val isValid = weightError == null && heightError == null &&
                ageError == null && bodyFatError == null

        return ValidationResult(
            isValid = isValid,
            weightError = weightError,
            heightError = heightError,
            ageError = ageError,
            bodyFatError = bodyFatError,
            warnings = warnings
        )
    }

    /**
     * Reference test values for verification.
     * Known BMR values from published formula papers.
     */
    fun verifyCalculations(): List<ReferenceTest> {
        return listOf(
            // Mifflin-St Jeor: Male, 70kg, 175cm, 30yr -> (10*70)+(6.25*175)-(5*30)+5 = 700+1093.75-150+5 = 1648.75
            ReferenceTest("Mifflin Male 70kg 175cm 30yr", BMRFormula.MIFFLIN_ST_JEOR,
                70f, 175f, 30, true, 0f, 1648.75f),
            // Mifflin-St Jeor: Female, 60kg, 165cm, 25yr -> (10*60)+(6.25*165)-(5*25)-161 = 600+1031.25-125-161 = 1345.25
            ReferenceTest("Mifflin Female 60kg 165cm 25yr", BMRFormula.MIFFLIN_ST_JEOR,
                60f, 165f, 25, false, 0f, 1345.25f),
            // Harris-Benedict Original: Male, 70kg, 175cm, 30yr -> 66.473+(13.7516*70)+(5.0033*175)-(6.755*30)
            // = 66.473+962.612+875.5775-202.65 = 1702.01
            ReferenceTest("HB Original Male 70kg 175cm 30yr", BMRFormula.HARRIS_BENEDICT_ORIGINAL,
                70f, 175f, 30, true, 0f, 1702.01f),
            // Katch-McArdle: 70kg, 20% BF -> LBM=56kg -> 370+(21.6*56) = 370+1209.6 = 1579.6
            ReferenceTest("Katch-McArdle 70kg 20%BF", BMRFormula.KATCH_MCARDLE,
                70f, 175f, 30, true, 20f, 1579.6f),
        )
    }

    data class ReferenceTest(
        val description: String,
        val formula: BMRFormula,
        val weightKg: Float,
        val heightCm: Float,
        val age: Int,
        val isMale: Boolean,
        val bodyFat: Float,
        val expectedBMR: Float,
        val tolerance: Float = 1f // Allow 1 kcal tolerance for floating point
    )
}
