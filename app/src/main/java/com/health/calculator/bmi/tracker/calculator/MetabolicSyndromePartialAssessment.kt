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
    val canDiagnose: Boolean,
    val partialMessage: String,
    val minimumPossible: Int,
    val maximumPossible: Int
)

object MetabolicSyndromePartialAssessment {

    fun evaluatePartial(
        waistCm: Float?,
        isMale: Boolean,
        systolic: Float?,
        diastolic: Float?,
        glucoseMgDl: Float?,
        trigMgDl: Float?,
        hdlMgDl: Float?,
        onWaistMed: Boolean,
        onBpMed: Boolean,
        onGlucoseMed: Boolean,
        onTrigMed: Boolean,
        onHdlMed: Boolean
    ): PartialAssessmentResult {

        val waistThreshold = if (isMale) 102f else 88f
        val hdlThreshold = if (isMale) 40f else 50f

        val criteria = listOf(
            PartialCriterionResult(
                name = "Central Obesity",
                icon = "📏",
                isProvided = waistCm != null,
                isMet = waistCm?.let { it > waistThreshold || onWaistMed },
                value = waistCm?.let { "%.1f cm".format(it) },
                threshold = "> ${waistThreshold.toInt()} cm",
                missingMessage = "Enter waist circumference"
            ),
            PartialCriterionResult(
                name = "Elevated Triglycerides",
                icon = "🩸",
                isProvided = trigMgDl != null,
                isMet = trigMgDl?.let { it >= 150f || onTrigMed },
                value = trigMgDl?.let { "%.0f mg/dL".format(it) },
                threshold = "≥ 150 mg/dL",
                missingMessage = "Enter triglycerides value"
            ),
            PartialCriterionResult(
                name = "Reduced HDL",
                icon = "💛",
                isProvided = hdlMgDl != null,
                isMet = hdlMgDl?.let { it < hdlThreshold || onHdlMed },
                value = hdlMgDl?.let { "%.0f mg/dL".format(it) },
                threshold = "< ${hdlThreshold.toInt()} mg/dL",
                missingMessage = "Enter HDL cholesterol value"
            ),
            PartialCriterionResult(
                name = "Elevated Blood Pressure",
                icon = "❤️",
                isProvided = systolic != null && diastolic != null,
                isMet = if (systolic != null && diastolic != null) {
                    systolic >= 130f || diastolic >= 85f || onBpMed
                } else null,
                value = if (systolic != null && diastolic != null) {
                    "%.0f/%.0f mmHg".format(systolic, diastolic)
                } else null,
                threshold = "≥ 130/85 mmHg",
                missingMessage = "Enter blood pressure values"
            ),
            PartialCriterionResult(
                name = "Elevated Fasting Glucose",
                icon = "🍯",
                isProvided = glucoseMgDl != null,
                isMet = glucoseMgDl?.let { it >= 100f || onGlucoseMed },
                value = glucoseMgDl?.let { "%.0f mg/dL".format(it) },
                threshold = "≥ 100 mg/dL",
                missingMessage = "Enter fasting glucose value"
            )
        )

        val providedCount = criteria.count { it.isProvided }
        val metCount = criteria.count { it.isMet == true }
        val missingCount = 5 - providedCount

        val minimumPossible = metCount
        val maximumPossible = metCount + missingCount

        val canDiagnose = providedCount == 5 ||
                metCount >= 3 ||
                (providedCount >= 3 && maximumPossible < 3)

        val partialMessage = when {
            providedCount == 5 -> "Complete assessment"
            providedCount == 0 -> "Enter your values to begin the assessment"
            metCount >= 3 -> "Metabolic syndrome is indicated even with incomplete data ($metCount criteria already met)"
            maximumPossible < 3 -> "Even if all remaining criteria were abnormal, the threshold would not be met"
            else -> "Partial assessment: $providedCount of 5 criteria entered. $missingCount more needed for complete results."
        }

        return PartialAssessmentResult(
            criteria = criteria,
            providedCount = providedCount,
            metCount = metCount,
            canDiagnose = canDiagnose,
            partialMessage = partialMessage,
            minimumPossible = minimumPossible,
            maximumPossible = maximumPossible
        )
    }
}
