package com.health.calculator.bmi.tracker.data.model

data class IBWAdditionalMetrics(
    val adjustedBodyWeightKg: Double?,
    val isAdjustedRelevant: Boolean,
    val leanBodyWeightKg: Double,
    val weightCategoryPercent: Double,
    val weightCategory: String,
    val weightCategoryDescription: String,
    val bmi: Double
) {
    val adjustedBodyWeightLbs: Double?
        get() = adjustedBodyWeightKg?.times(2.20462)

    val leanBodyWeightLbs: Double
        get() = leanBodyWeightKg * 2.20462

    val bodyFatEstimateKg: Double
        get() = 0.0 // will use actual weight from result

    fun getBodyFatPercent(actualWeightKg: Double): Double {
        if (actualWeightKg <= 0.1) return 0.0
        return ((actualWeightKg - leanBodyWeightKg) / actualWeightKg * 100).coerceIn(0.0, 70.0)
    }
}
