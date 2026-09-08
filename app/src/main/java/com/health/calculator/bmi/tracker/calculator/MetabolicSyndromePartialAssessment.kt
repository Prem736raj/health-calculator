package com.health.calculator.bmi.tracker.calculator

data class PartialCriterionResult(
    val name: String,
    val icon: String,
    val isProvided: Boolean,
    val isMet: Boolean?,
    val value: String?,
    val threshold: String,
    val missingMessage: String
)

data class PartialAssessmentResult(
    val criteria: List<PartialCriterionResult>,
    val providedCount: Int,
    val metCount: Int,
    /** Whether the entered values are sufficient to show a complete screen. */
    val canReport: Boolean,
    val partialMessage: String,
    val minimumPossible: Int,
    val maximumPossible: Int
) {
    /** Legacy name retained for source compatibility; this app never diagnoses. */
    val canDiagnose: Boolean get() = false
}

object MetabolicSyndromePartialAssessment {
    fun evaluatePartial(
        waistCm: Float?,
        isMale: Boolean,
        systolic: Float?,
        diastolic: Float?,
        glucoseMgDl: Float?,
        trigMgDl: Float?,
        hdlMgDl: Float?,
        onBpMed: Boolean,
        onGlucoseMed: Boolean,
        onTrigMed: Boolean,
        onHdlMed: Boolean,
        ethnicity: Ethnicity = Ethnicity.US_ATP
    ): PartialAssessmentResult {
        val waistThreshold = if (isMale) ethnicity.maleWaistCm else ethnicity.femaleWaistCm
        val hdlThreshold = if (isMale) 40f else 50f
        val criteria = listOf(
            PartialCriterionResult("Central waist measurement", "📏", waistCm != null, waistCm?.let { it >= waistThreshold }, waistCm?.let { "%.1f cm".format(it) }, "≥ ${waistThreshold.toInt()} cm", "Enter waist circumference"),
            PartialCriterionResult("Elevated triglycerides", "🩸", trigMgDl != null, trigMgDl?.let { it >= 150f || onTrigMed }, trigMgDl?.let { "%.0f mg/dL".format(it) }, "≥ 150 mg/dL", "Enter triglycerides value"),
            PartialCriterionResult("Reduced HDL", "💛", hdlMgDl != null, hdlMgDl?.let { it < hdlThreshold || onHdlMed }, hdlMgDl?.let { "%.0f mg/dL".format(it) }, "< ${hdlThreshold.toInt()} mg/dL", "Enter HDL cholesterol value"),
            PartialCriterionResult("Elevated blood pressure", "❤️", systolic != null && diastolic != null, if (systolic != null && diastolic != null) systolic >= 130f || diastolic >= 85f || onBpMed else null, if (systolic != null && diastolic != null) "%.0f/%.0f mmHg".format(systolic, diastolic) else null, "≥ 130 systolic or ≥ 85 diastolic", "Enter blood pressure values"),
            PartialCriterionResult("Elevated fasting glucose", "🍯", glucoseMgDl != null, glucoseMgDl?.let { it >= 100f || onGlucoseMed }, glucoseMgDl?.let { "%.0f mg/dL".format(it) }, "≥ 100 mg/dL", "Enter fasting glucose value")
        )
        val provided = criteria.count { it.isProvided }
        val met = criteria.count { it.isMet == true }
        val maximum = met + (criteria.size - provided)
        return PartialAssessmentResult(
            criteria = criteria,
            providedCount = provided,
            metCount = met,
            canReport = provided == criteria.size,
            partialMessage = when {
                provided == 0 -> "Enter values to see a screening summary"
                provided == criteria.size -> "Complete screening reference"
                else -> "Partial screening: $provided of 5 values entered."
            },
            minimumPossible = met,
            maximumPossible = maximum
        )
    }
}
