package com.health.calculator.bmi.tracker.data.calculator

/**
 * Shared guardrails for the app's informational calculators.
 *
 * These values are intentionally kept in one place so that screens do not
 * quietly drift apart on age limits, BMI boundaries, or wording.
 */
object MedicalCalculationPolicy {
    const val ADULT_MIN_AGE = 18
    const val MAX_AGE = 120
    const val BMI_UNDERWEIGHT = 18.5
    const val BMI_NORMAL_UPPER = 24.9
    const val BMI_OVERWEIGHT_UPPER = 29.9
    const val WAIST_TO_HEIGHT_ACTION_POINT = 0.5
    const val WHO_WHR_MALE_ACTION_POINT = 0.90
    const val WHO_WHR_FEMALE_ACTION_POINT = 0.85
    const val IDF_SOUTH_ASIAN_MALE_WAIST_CM = 90.0
    const val IDF_SOUTH_ASIAN_FEMALE_WAIST_CM = 80.0

    const val GENERAL_DISCLAIMER =
        "Informational estimate only. This is not a diagnosis or a substitute for professional care."
}

data class BmiCalculation(
    val bmi: Double,
    val healthyWeightMinKg: Double,
    val healthyWeightMaxKg: Double,
    val category: BmiReferenceCategory
)

enum class BmiReferenceCategory(val label: String) {
    SEVERE_THINNESS("Below 16.0"),
    MODERATE_THINNESS("16.0–16.9"),
    MILD_THINNESS("17.0–18.4"),
    REFERENCE_RANGE("18.5–24.9"),
    OVERWEIGHT("25.0–29.9"),
    OBESITY_CLASS_I("30.0–34.9"),
    OBESITY_CLASS_II("35.0–39.9"),
    OBESITY_CLASS_III("40.0 or higher")
}

/** Adult BMI math shared by production code and tests. */
object BmiCalculator {
    fun calculate(weightKg: Double, heightCm: Double): BmiCalculation? {
        if (!weightKg.isFinite() || !heightCm.isFinite() || weightKg <= 0.0 || heightCm <= 0.0) return null
        val heightM = heightCm / 100.0
        if (heightM <= 0.0) return null
        val bmi = weightKg / (heightM * heightM)
        if (!bmi.isFinite()) return null
        val category = classify(bmi)
        val heightSquared = heightM * heightM
        return BmiCalculation(
            bmi = bmi,
            healthyWeightMinKg = MedicalCalculationPolicy.BMI_UNDERWEIGHT * heightSquared,
            healthyWeightMaxKg = MedicalCalculationPolicy.BMI_NORMAL_UPPER * heightSquared,
            category = category
        )
    }

    fun classify(bmi: Double): BmiReferenceCategory = when {
        bmi < 16.0 -> BmiReferenceCategory.SEVERE_THINNESS
        bmi < 17.0 -> BmiReferenceCategory.MODERATE_THINNESS
        bmi < 18.5 -> BmiReferenceCategory.MILD_THINNESS
        bmi < 25.0 -> BmiReferenceCategory.REFERENCE_RANGE
        bmi < 30.0 -> BmiReferenceCategory.OVERWEIGHT
        bmi < 35.0 -> BmiReferenceCategory.OBESITY_CLASS_I
        bmi < 40.0 -> BmiReferenceCategory.OBESITY_CLASS_II
        else -> BmiReferenceCategory.OBESITY_CLASS_III
    }

    fun kilogramsFromPounds(pounds: Double): Double? =
        if (pounds.isFinite() && pounds > 0.0) pounds * 0.45359237 else null

    fun poundsFromKilograms(kilograms: Double): Double? =
        if (kilograms.isFinite() && kilograms > 0.0) kilograms / 0.45359237 else null
}
