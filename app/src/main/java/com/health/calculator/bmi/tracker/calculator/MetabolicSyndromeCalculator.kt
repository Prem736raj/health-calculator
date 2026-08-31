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
    /** Legacy field retained for persistence compatibility; UI must call this a screening result. */
    val isSyndromePresent: Boolean,
    val riskLevel: MetabolicRiskLevel,
    val atpIIICriteriaMet: Int,
    val idfCriteriaMet: Int,
    val idfDiagnosis: Boolean,
    val diagnosisDiffers: Boolean,
    val selectedEthnicity: Ethnicity = Ethnicity.US_ATP,
    val isClinicallyValidated: Boolean = false,
    val interpretation: String = "Informational screening summary; a clinician must confirm any diagnosis."
)

enum class MetabolicRiskLevel(val label: String, val description: String) {
    NONE("0 markers", "No screening markers met in the values entered."),
    LOW("1 marker", "One screening marker met; review the value and keep tracking."),
    MODERATE("2 markers", "Two screening markers met; consider discussing the pattern at a routine visit."),
    HIGH("3 markers", "Three screening markers met. This is not a diagnosis; ask a healthcare professional for context."),
    VERY_HIGH("4–5 markers", "Several screening markers met. This is not a diagnosis; arrange professional review.")
}

/**
 * Harmonized five-marker screening reference. It is deliberately described as
 * a screen: medication status and one-time measurements cannot establish a
 * diagnosis, and fasting status/laboratory quality matter.
 */
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
        onHdlMedication: Boolean,
        ethnicity: Ethnicity = Ethnicity.US_ATP
    ): MetabolicSyndromeResult {
        require(listOf(waistCm, systolic, diastolic, fastingGlucoseMgDl, triglyceridesMgDl, hdlMgDl).all { it.isFinite() }) {
            "Metabolic screening values must be finite"
        }
        require(waistCm > 0f && systolic > 0f && diastolic > 0f && fastingGlucoseMgDl >= 0f && triglyceridesMgDl >= 0f && hdlMgDl >= 0f) {
            "Metabolic screening values must be positive"
        }

        val waistThreshold = if (isMale) ethnicity.maleWaistCm else ethnicity.femaleWaistCm
        val hdlThreshold = if (isMale) 40f else 50f
        val waistMet = waistCm >= waistThreshold || onWaistMedication
        val triglyceridesMet = triglyceridesMgDl >= 150f || onTriglyceridesMedication
        val hdlMet = hdlMgDl < hdlThreshold || onHdlMedication
        val bpMet = systolic >= 130f || diastolic >= 85f || onBpMedication
        val glucoseMet = fastingGlucoseMgDl >= 100f || onGlucoseMedication

        val criteria = listOf(
            MetabolicCriterion(
                "Central waist measurement",
                "Selected ${ethnicity.displayName} reference",
                "%.1f cm".format(waistCm),
                "≥ %.0f cm".format(waistThreshold),
                waistMet,
                onWaistMedication
            ),
            MetabolicCriterion("Elevated triglycerides", "Fasting or clinician-reported laboratory value", "%.0f mg/dL".format(triglyceridesMgDl), "≥ 150 mg/dL", triglyceridesMet, onTriglyceridesMedication),
            MetabolicCriterion("Reduced HDL cholesterol", "Sex-specific reference", "%.0f mg/dL".format(hdlMgDl), "< %.0f mg/dL".format(hdlThreshold), hdlMet, onHdlMedication),
            MetabolicCriterion("Elevated blood pressure", "Either number can meet this marker", "%.0f/%.0f mmHg".format(systolic, diastolic), "≥ 130 systolic or ≥ 85 diastolic", bpMet, onBpMedication),
            MetabolicCriterion("Elevated fasting glucose", "Fasting value or glucose-lowering medication", "%.0f mg/dL".format(fastingGlucoseMgDl), "≥ 100 mg/dL", glucoseMet, onGlucoseMedication)
        )

        val count = criteria.count { it.isMet }
        val idfWaistMet = waistCm >= waistThreshold || onWaistMedication
        val idfOther = listOf(triglyceridesMet, hdlMet, bpMet, glucoseMet).count { it }
        val idfScreen = idfWaistMet && idfOther >= 2
        return MetabolicSyndromeResult(
            criteria = criteria,
            criteriaMet = count,
            isSyndromePresent = count >= 3,
            riskLevel = when (count) {
                0 -> MetabolicRiskLevel.NONE
                1 -> MetabolicRiskLevel.LOW
                2 -> MetabolicRiskLevel.MODERATE
                3 -> MetabolicRiskLevel.HIGH
                else -> MetabolicRiskLevel.VERY_HIGH
            },
            atpIIICriteriaMet = count,
            idfCriteriaMet = (if (idfWaistMet) 1 else 0) + idfOther,
            idfDiagnosis = idfScreen,
            diagnosisDiffers = (count >= 3) != idfScreen,
            selectedEthnicity = ethnicity
        )
    }

    fun mgDlToMmolL_glucose(mgDl: Float): Float = mgDl / 18.0182f
    fun mmolLToMgDl_glucose(mmolL: Float): Float = mmolL * 18.0182f
    fun mgDlToMmolL_triglycerides(mgDl: Float): Float = mgDl / 88.57f
    fun mmolLToMgDl_triglycerides(mmolL: Float): Float = mmolL * 88.57f
    fun mgDlToMmolL_hdl(mgDl: Float): Float = mgDl / 38.67f
    fun mmolLToMgDl_hdl(mmolL: Float): Float = mmolL * 38.67f
    fun cmToInches(cm: Float): Float = cm / 2.54f
    fun inchesToCm(inches: Float): Float = inches * 2.54f
}
