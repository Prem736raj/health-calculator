package com.health.calculator.bmi.tracker.data.model

data class IBWResult(
    val devineKg: Double,
    val robinsonKg: Double,
    val millerKg: Double,
    val hamwiKg: Double,
    val brocaKg: Double,
    val bmiLowerKg: Double,
    val bmiUpperKg: Double,
    val frameAdjustedDevineKg: Double,
    val currentWeightKg: Double?,
    val heightCm: Double,
    val gender: String,
    val frameSize: String,
    val age: Int?,
    val timestamp: Long = System.currentTimeMillis(),
    val heightWarning: String? = null
) {
    val isEdgeCaseHeight: Boolean
        get() = heightWarning != null

    val devineLbs: Double get() = devineKg * 2.20462
    val robinsonLbs: Double get() = robinsonKg * 2.20462
    val millerLbs: Double get() = millerKg * 2.20462
    val hamwiLbs: Double get() = hamwiKg * 2.20462
    val brocaLbs: Double get() = brocaKg * 2.20462
    val bmiLowerLbs: Double get() = bmiLowerKg * 2.20462
    val bmiUpperLbs: Double get() = bmiUpperKg * 2.20462
    val frameAdjustedDevineLbs: Double get() = frameAdjustedDevineKg * 2.20462

    val weightDifferenceKg: Double?
        get() = currentWeightKg?.let { it - frameAdjustedDevineKg }

    val weightDifferenceLbs: Double?
        get() = weightDifferenceKg?.let { it * 2.20462 }

    val isAboveIdeal: Boolean?
        get() = weightDifferenceKg?.let { it > 0 }

    val allFormulasKg: List<Pair<String, Double>>
        get() = listOf(
            "Devine (1974)" to devineKg,
            "Robinson (1983)" to robinsonKg,
            "Miller (1983)" to millerKg,
            "Hamwi (1964)" to hamwiKg,
            "Broca Index" to brocaKg
        )

    val averageKg: Double
        get() = allFormulasKg.map { it.second }.average()

    val rangeMinKg: Double
        get() = allFormulasKg.minOf { it.second }

    val rangeMaxKg: Double
        get() = allFormulasKg.maxOf { it.second }
}
