package com.health.calculator.bmi.tracker.calculator

import kotlin.math.abs
import kotlin.math.log10

data class BSAValidationResult(
    val isValid: Boolean,
    val weightError: String? = null,
    val heightError: String? = null,
    val warnings: List<String> = emptyList()
)

object BSAEdgeCaseValidator {

    // Absolute limits
    private const val MIN_WEIGHT_KG = 0.5f   // Premature infant
    private const val MAX_WEIGHT_KG = 500f    // Extreme case
    private const val MIN_HEIGHT_CM = 20f     // Premature infant
    private const val MAX_HEIGHT_CM = 280f    // Extremely tall

    fun validate(weightKg: Float?, heightCm: Float?, formulaId: String): BSAValidationResult {
        val warnings = mutableListOf<String>()

        // Null checks
        if (weightKg == null) {
            return BSAValidationResult(false, weightError = "Please enter weight")
        }
        if (heightCm == null) {
            return BSAValidationResult(false, heightError = "Please enter height")
        }

        // Zero/Negative
        if (weightKg <= 0f) {
            return BSAValidationResult(false, weightError = "Weight must be greater than zero")
        }
        if (heightCm <= 0f) {
            return BSAValidationResult(false, heightError = "Height must be greater than zero")
        }

        // Range checks
        if (weightKg < MIN_WEIGHT_KG) {
            return BSAValidationResult(false, weightError = "Weight too low (minimum ${MIN_WEIGHT_KG} kg)")
        }
        if (weightKg > MAX_WEIGHT_KG) {
            return BSAValidationResult(false, weightError = "Weight too high (maximum ${MAX_WEIGHT_KG} kg)")
        }
        if (heightCm < MIN_HEIGHT_CM) {
            return BSAValidationResult(false, heightError = "Height too low (minimum ${MIN_HEIGHT_CM} cm)")
        }
        if (heightCm > MAX_HEIGHT_CM) {
            return BSAValidationResult(false, heightError = "Height too high (maximum ${MAX_HEIGHT_CM} cm)")
        }

        // Boyd formula specific: log10(weight) must be valid
        if (formulaId == "boyd") {
            if (weightKg <= 0f) {
                return BSAValidationResult(false, weightError = "Boyd formula requires positive weight")
            }
            val logW = log10(weightKg.toDouble())
            val exponent = 0.7285 - 0.0188 * logW
            if (exponent <= 0 || exponent.isNaN() || exponent.isInfinite()) {
                warnings.add("Boyd formula may be less accurate at this weight. Consider Du Bois or Mosteller.")
            }
        }

        // Pediatric warnings
        if (weightKg < 3f || heightCm < 50f) {
            if (formulaId != "haycock") {
                warnings.add("For very small children/infants, the Haycock formula is recommended for best accuracy.")
            }
            warnings.add("Values for premature or very young infants should be interpreted with caution.")
        }

        // Very large adult warnings
        if (weightKg > 200f) {
            warnings.add("BSA formulas may be less accurate at very high weights. Results should be used with caution.")
        }
        if (heightCm > 210f) {
            warnings.add("BSA formulas may be less accurate for extremely tall individuals.")
        }

        // Sanity check: weight vs height plausibility
        val bmi = weightKg / ((heightCm / 100f) * (heightCm / 100f))
        if (bmi < 8f || bmi > 80f) {
            warnings.add("The weight/height combination seems unusual. Please verify your entries.")
        }

        return BSAValidationResult(
            isValid = true,
            warnings = warnings
        )
    }

    /**
     * Safe calculation that handles edge cases for all formulas
     */
    fun safeCalculate(weightKg: Float, heightCm: Float, formulaId: String): Float {
        return try {
            val result = BSACalculator.calculateSingle(weightKg, heightCm, formulaId)
            when {
                result.isNaN() -> BSACalculator.calculateSingle(weightKg, heightCm, "dubois")
                result.isInfinite() -> BSACalculator.calculateSingle(weightKg, heightCm, "dubois")
                result <= 0f -> BSACalculator.calculateSingle(weightKg, heightCm, "dubois")
                result > 10f -> BSACalculator.calculateSingle(weightKg, heightCm, "dubois") // Implausible BSA
                else -> result
            }
        } catch (e: Exception) {
            // Fallback to Du Bois if any formula crashes
            try {
                BSACalculator.calculateSingle(weightKg, heightCm, "dubois")
            } catch (e2: Exception) {
                0f
            }
        }
    }

    /**
     * Safe calculation for all formulas at once with fallback
     */
    fun safeCalculateAll(weightKg: Float, heightCm: Float, selectedFormulaId: String): com.health.calculator.bmi.tracker.calculator.BSAResult {
        val selectedFormula = BSACalculator.formulas.find { it.id == selectedFormulaId }
            ?: BSACalculator.formulas[0]

        val allResults = BSACalculator.formulas.map { formula ->
            Pair(formula, safeCalculate(weightKg, heightCm, formula.id))
        }.filter { it.second > 0f }

        val primaryBSA = allResults.find { it.first.id == selectedFormulaId }?.second
            ?: allResults.firstOrNull()?.second
            ?: 0f

        return BSAResult(
            primaryBSA = primaryBSA,
            selectedFormula = selectedFormula,
            allResults = allResults,
            weightKg = weightKg,
            heightCm = heightCm
        )
    }
}
