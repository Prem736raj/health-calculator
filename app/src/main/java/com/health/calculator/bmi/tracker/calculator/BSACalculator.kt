package com.health.calculator.bmi.tracker.calculator

import kotlin.math.pow

data class BSAFormulaInfo(
    val id: String,
    val name: String,
    val label: String,
    val authors: String,
    val year: String,
    val description: String,
    val bestFor: String,
    val formula: String
)

data class BSAResult(
    val primaryBSA: Float,
    val selectedFormula: BSAFormulaInfo,
    val allResults: List<Pair<BSAFormulaInfo, Float>>,
    val weightKg: Float,
    val heightCm: Float
)

object BSACalculator {

    val formulas: List<BSAFormulaInfo> = listOf(
        BSAFormulaInfo(
            id = "dubois",
            name = "Du Bois & Du Bois",
            label = "Most Used",
            authors = "Du Bois D, Du Bois EF",
            year = "1916",
            description = "The most widely used formula in clinical practice worldwide. Based on measurements of 9 individuals.",
            bestFor = "General adult use",
            formula = "BSA = 0.007184 × W^0.425 × H^0.725"
        ),
        BSAFormulaInfo(
            id = "mosteller",
            name = "Mosteller",
            label = "Simplified",
            authors = "Mosteller RD",
            year = "1987",
            description = "A simplified formula that's easy to calculate. Provides results very close to Du Bois for most adults.",
            bestFor = "Quick calculations",
            formula = "BSA = √(W × H / 3600)"
        ),
        BSAFormulaInfo(
            id = "haycock",
            name = "Haycock",
            label = "Pediatric",
            authors = "Haycock GB, Schwartz GJ, Wisotsky DH",
            year = "1978",
            description = "Specifically validated for infants, children, and adolescents. Considered the most accurate for pediatric patients.",
            bestFor = "Children & infants",
            formula = "BSA = 0.024265 × W^0.5378 × H^0.3964"
        ),
        BSAFormulaInfo(
            id = "gehan",
            name = "Gehan & George",
            label = "Research",
            authors = "Gehan EA, George SL",
            year = "1970",
            description = "Derived from a large dataset of 401 subjects. Considered very accurate across a wide range of body sizes.",
            bestFor = "Research & wide range of sizes",
            formula = "BSA = 0.0235 × W^0.51456 × H^0.42246"
        ),
        BSAFormulaInfo(
            id = "boyd",
            name = "Boyd",
            label = "Historical",
            authors = "Boyd E",
            year = "1935",
            description = "One of the earliest formulas, based on extensive body measurement data. Still used in some clinical settings.",
            bestFor = "Historical reference",
            formula = "BSA = 0.0003207 × W^(0.7285-0.0188×log₁₀W) × H^0.3"
        ),
        BSAFormulaInfo(
            id = "fujimoto",
            name = "Fujimoto",
            label = "Japanese",
            authors = "Fujimoto S, et al.",
            year = "1968",
            description = "Derived from Japanese population data. May be more accurate for East Asian body types.",
            bestFor = "Japanese / East Asian population",
            formula = "BSA = 0.008883 × W^0.444 × H^0.663"
        ),
        BSAFormulaInfo(
            id = "takahira",
            name = "Takahira",
            label = "Asian",
            authors = "Takahira H",
            year = "1925",
            description = "Another formula derived from Asian population measurements. Commonly used in Japan.",
            bestFor = "Asian population",
            formula = "BSA = 0.007241 × W^0.425 × H^0.725"
        ),
        BSAFormulaInfo(
            id = "shuter",
            name = "Shuter & Aslani",
            label = "Modern",
            authors = "Shuter B, Aslani A",
            year = "2000",
            description = "A modern formula using CT-based body surface measurements for improved accuracy.",
            bestFor = "Modern clinical use",
            formula = "BSA = 0.00949 × W^0.441 × H^0.655"
        )
    )

    fun calculate(weightKg: Float, heightCm: Float, formulaId: String): BSAResult {
        val selectedFormula = formulas.find { it.id == formulaId } ?: formulas[0]

        val allResults = formulas.map { formula ->
            Pair(formula, calculateSingle(weightKg, heightCm, formula.id))
        }

        val primaryBSA = allResults.find { it.first.id == formulaId }?.second ?: 0f

        return BSAResult(
            primaryBSA = primaryBSA,
            selectedFormula = selectedFormula,
            allResults = allResults,
            weightKg = weightKg,
            heightCm = heightCm
        )
    }

    fun calculateSingle(weightKg: Float, heightCm: Float, formulaId: String): Float {
        return when (formulaId) {
            "dubois" -> dubois(weightKg, heightCm)
            "mosteller" -> mosteller(weightKg, heightCm)
            "haycock" -> haycock(weightKg, heightCm)
            "gehan" -> gehan(weightKg, heightCm)
            "boyd" -> boyd(weightKg, heightCm)
            "fujimoto" -> fujimoto(weightKg, heightCm)
            "takahira" -> takahira(weightKg, heightCm)
            "shuter" -> shuter(weightKg, heightCm)
            else -> dubois(weightKg, heightCm)
        }
    }

    // Du Bois & Du Bois (1916)
    private fun dubois(w: Float, h: Float): Float {
        return try {
            val result = 0.007184f * w.toDouble().pow(0.425).toFloat() * h.toDouble().pow(0.725).toFloat()
            if (result.isNaN() || result.isInfinite() || result <= 0f) 0f else result
        } catch (e: Exception) { 0f }
    }

    // Mosteller (1987)
    private fun mosteller(w: Float, h: Float): Float {
        return try {
            val result = kotlin.math.sqrt((w * h / 3600f).toDouble()).toFloat()
            if (result.isNaN() || result.isInfinite() || result <= 0f) 0f else result
        } catch (e: Exception) { 0f }
    }

    // Haycock (1978)
    private fun haycock(w: Float, h: Float): Float {
        return try {
            val result = 0.024265f * w.toDouble().pow(0.5378).toFloat() * h.toDouble().pow(0.3964).toFloat()
            if (result.isNaN() || result.isInfinite() || result <= 0f) 0f else result
        } catch (e: Exception) { 0f }
    }

    // Gehan & George (1970)
    private fun gehan(w: Float, h: Float): Float {
        return try {
            val result = 0.0235f * w.toDouble().pow(0.51456).toFloat() * h.toDouble().pow(0.42246).toFloat()
            if (result.isNaN() || result.isInfinite() || result <= 0f) 0f else result
        } catch (e: Exception) { 0f }
    }

    // Boyd (1935) - hardened against edge cases
    private fun boyd(w: Float, h: Float): Float {
        return try {
            if (w <= 0f || h <= 0f) return dubois(w, h) // Fallback

            val logW = kotlin.math.log10(w.toDouble())
            val exponent = 0.7285 - 0.0188 * logW

            // Guard against problematic exponent values
            if (exponent <= 0 || exponent > 2.0 || exponent.isNaN() || exponent.isInfinite()) {
                return dubois(w, h) // Fallback to Du Bois
            }

            val result = (0.0003207f * w.toDouble().pow(exponent).toFloat() * h.toDouble().pow(0.3).toFloat())

            // Sanity check
            if (result.isNaN() || result.isInfinite() || result <= 0f || result > 10f) {
                dubois(w, h)
            } else {
                result
            }
        } catch (e: Exception) {
            dubois(w, h) // Fallback on any error
        }
    }

    // Fujimoto (1968)
    private fun fujimoto(w: Float, h: Float): Float {
        return try {
            val result = 0.008883f * w.toDouble().pow(0.444).toFloat() * h.toDouble().pow(0.663).toFloat()
            if (result.isNaN() || result.isInfinite() || result <= 0f) 0f else result
        } catch (e: Exception) { 0f }
    }

    // Takahira (1925)
    private fun takahira(w: Float, h: Float): Float {
        return try {
            val result = 0.007241f * w.toDouble().pow(0.425).toFloat() * h.toDouble().pow(0.725).toFloat()
            if (result.isNaN() || result.isInfinite() || result <= 0f) 0f else result
        } catch (e: Exception) { 0f }
    }

    // Shuter & Aslani (2000)
    private fun shuter(w: Float, h: Float): Float {
        return try {
            val result = 0.00949f * w.toDouble().pow(0.441).toFloat() * h.toDouble().pow(0.655).toFloat()
            if (result.isNaN() || result.isInfinite() || result <= 0f) 0f else result
        } catch (e: Exception) { 0f }
    }

    // Unit conversions
    fun lbsToKg(lbs: Float): Float = lbs * 0.453592f
    fun kgToLbs(kg: Float): Float = kg / 0.453592f
    fun feetInchesToCm(feet: Int, inches: Float): Float = (feet * 30.48f) + (inches * 2.54f)
    fun cmToFeetInches(cm: Float): Pair<Int, Float> {
        val totalInches = cm / 2.54f
        val feet = (totalInches / 12).toInt()
        val remainingInches = totalInches - (feet * 12)
        return Pair(feet, remainingInches)
    }
}
