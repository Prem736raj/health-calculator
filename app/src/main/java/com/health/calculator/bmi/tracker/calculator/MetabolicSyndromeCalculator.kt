package com.health.calculator.bmi.tracker.calculator

data class MetabolicCriterion(
    val name: String,
    val description: String,
    val userValue: String,
    val threshold: String,
    val isMet: Boolean,
    val isOnMedication: Boolean
)

data class MetabolicSyndromeResult(
    val criteria: List<MetabolicCriterion>,
    val criteriaMet: Int,
    val totalCriteria: Int = 5,
    val isSyndromePresent: Boolean,
    val riskLevel: MetabolicRiskLevel,
    val atpIIICriteriaMet: Int,
    val idfCriteriaMet: Int,
    val idfDiagnosis: Boolean,
    val diagnosisDiffers: Boolean
)

enum class MetabolicRiskLevel(val label: String, val description: String) {
    NONE("No Risk", "No metabolic syndrome indicators detected"),
    LOW("Low Risk", "At risk — Monitor these factors closely"),
    MODERATE("Moderate Risk", "At risk — Monitor these factors and consult your doctor"),
    HIGH("High Risk", "Metabolic Syndrome Present — Consult your healthcare provider"),
    VERY_HIGH("Very High Risk", "Metabolic Syndrome Present — Seek medical attention")
}

object MetabolicSyndromeCalculator {

    fun evaluate(
        waistCm: Float,
        isMale: Boolean,
        systolic: Float,
        diastolic: Float,
        fastingGlucoseMgDl: Float,
        triglyceridesMgDl: Float,
        hdlMgDl: Float,
        onWaistMedication: Boolean,
        onBpMedication: Boolean,
        onGlucoseMedication: Boolean,
        onTriglyceridesMedication: Boolean,
        onHdlMedication: Boolean
    ): MetabolicSyndromeResult {

        // --- ATP III Criteria ---
        val atpCriteria = mutableListOf<MetabolicCriterion>()

        // 1. Central Obesity (ATP III)
        val waistThresholdAtp = if (isMale) 102f else 88f
        val waistThresholdUnit = "cm"
        val waistMet = waistCm > waistThresholdAtp || onWaistMedication
        atpCriteria.add(
            MetabolicCriterion(
                name = "Central Obesity",
                description = if (isMale) "Waist > 102 cm (40 in)" else "Waist > 88 cm (35 in)",
                userValue = "%.1f %s".format(waistCm, waistThresholdUnit),
                threshold = "> %.0f %s".format(waistThresholdAtp, waistThresholdUnit),
                isMet = waistMet,
                isOnMedication = onWaistMedication
            )
        )

        // 2. Elevated Triglycerides
        val trigThreshold = 150f
        val trigMet = triglyceridesMgDl >= trigThreshold || onTriglyceridesMedication
        atpCriteria.add(
            MetabolicCriterion(
                name = "Elevated Triglycerides",
                description = "≥ 150 mg/dL (1.7 mmol/L)",
                userValue = "%.0f mg/dL".format(triglyceridesMgDl),
                threshold = "≥ 150 mg/dL",
                isMet = trigMet,
                isOnMedication = onTriglyceridesMedication
            )
        )

        // 3. Reduced HDL Cholesterol
        val hdlThreshold = if (isMale) 40f else 50f
        val hdlMet = hdlMgDl < hdlThreshold || onHdlMedication
        atpCriteria.add(
            MetabolicCriterion(
                name = "Reduced HDL Cholesterol",
                description = if (isMale) "< 40 mg/dL (1.03 mmol/L)" else "< 50 mg/dL (1.3 mmol/L)",
                userValue = "%.0f mg/dL".format(hdlMgDl),
                threshold = "< %.0f mg/dL".format(hdlThreshold),
                isMet = hdlMet,
                isOnMedication = onHdlMedication
            )
        )

        // 4. Elevated Blood Pressure
        val bpMet = systolic >= 130f || diastolic >= 85f || onBpMedication
        atpCriteria.add(
            MetabolicCriterion(
                name = "Elevated Blood Pressure",
                description = "Systolic ≥ 130 OR Diastolic ≥ 85 mmHg",
                userValue = "%.0f/%.0f mmHg".format(systolic, diastolic),
                threshold = "≥ 130/85 mmHg",
                isMet = bpMet,
                isOnMedication = onBpMedication
            )
        )

        // 5. Elevated Fasting Glucose
        val glucoseThreshold = 100f
        val glucoseMet = fastingGlucoseMgDl >= glucoseThreshold || onGlucoseMedication
        atpCriteria.add(
            MetabolicCriterion(
                name = "Elevated Fasting Glucose",
                description = "≥ 100 mg/dL (5.6 mmol/L)",
                userValue = "%.0f mg/dL".format(fastingGlucoseMgDl),
                threshold = "≥ 100 mg/dL",
                isMet = glucoseMet,
                isOnMedication = onGlucoseMedication
            )
        )

        val atpMetCount = atpCriteria.count { it.isMet }
        val atpDiagnosis = atpMetCount >= 3

        // --- IDF Criteria (lower waist cutoffs) ---
        val waistThresholdIdf = if (isMale) 94f else 80f
        val waistMetIdf = waistCm > waistThresholdIdf || onWaistMedication
        // IDF requires central obesity + 2 of remaining 4
        val otherMetCount = listOf(trigMet, hdlMet, bpMet, glucoseMet).count { it }
        val idfCriteriaMet = (if (waistMetIdf) 1 else 0) + otherMetCount
        val idfDiagnosis = waistMetIdf && otherMetCount >= 2

        val diagnosisDiffers = atpDiagnosis != idfDiagnosis

        val riskLevel = when (atpMetCount) {
            0 -> MetabolicRiskLevel.NONE
            1 -> MetabolicRiskLevel.LOW
            2 -> MetabolicRiskLevel.MODERATE
            3 -> MetabolicRiskLevel.HIGH
            4, 5 -> MetabolicRiskLevel.VERY_HIGH
            else -> MetabolicRiskLevel.NONE
        }

        return MetabolicSyndromeResult(
            criteria = atpCriteria,
            criteriaMet = atpMetCount,
            totalCriteria = 5,
            isSyndromePresent = atpDiagnosis,
            riskLevel = riskLevel,
            atpIIICriteriaMet = atpMetCount,
            idfCriteriaMet = idfCriteriaMet,
            idfDiagnosis = idfDiagnosis,
            diagnosisDiffers = diagnosisDiffers
        )
    }

    // Unit conversions
    fun mgDlToMmolL_glucose(mgDl: Float): Float = mgDl / 18.0182f
    fun mmolLToMgDl_glucose(mmolL: Float): Float = mmolL * 18.0182f

    fun mgDlToMmolL_triglycerides(mgDl: Float): Float = mgDl / 88.57f
    fun mmolLToMgDl_triglycerides(mmolL: Float): Float = mmolL * 88.57f

    fun mgDlToMmolL_hdl(mgDl: Float): Float = mgDl / 38.67f
    fun mmolLToMgDl_hdl(mmolL: Float): Float = mmolL * 38.67f

    fun cmToInches(cm: Float): Float = cm / 2.54f
    fun inchesToCm(inches: Float): Float = inches * 2.54f
}
