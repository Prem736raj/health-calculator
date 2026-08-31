package com.health.calculator.bmi.tracker.calculator

data class CriterionRecommendation(
    val criterionName: String,
    val isAbnormal: Boolean,
    val healthMeaning: String,
    val risks: List<String>,
    val recommendations: List<String>,
    val normalMessage: String,
    val icon: String,
    val urgencyLevel: String
)

/**
 * Compatibility model for the old UI. The former cardiovascular risk score is
 * intentionally no longer calculated; marker counts are not individual risk
 * predictions.
 */
data class CardiovascularRiskSummary(
    val riskLevel: String,
    val riskDescription: String,
    val riskScore: Int,
    val overallMessage: String,
    val actionItems: List<String>,
    val shouldSeekMedical: Boolean
)

object MetabolicSyndromeRecommendations {
    fun getRecommendationForCriterion(criterionName: String, isMet: Boolean, isOnMedication: Boolean): CriterionRecommendation {
        val label = criterionName
        val meaning = if (isMet) {
            "This screening marker is at or beyond the selected reference. A single value cannot diagnose a condition; fasting status, medicines, repeat testing, and personal context matter."
        } else {
            "This screening marker is below the selected reference for the value entered. It does not rule out other health concerns."
        }
        val medicationNote = if (isOnMedication) " Medication status is included as a marker and should not be changed without professional advice." else ""
        return CriterionRecommendation(
            criterionName = label,
            isAbnormal = isMet,
            healthMeaning = meaning + medicationNote,
            risks = emptyList(),
            recommendations = listOf(
                "Review the measurement technique and units.",
                "Keep a dated record so repeated results can be discussed.",
                "Ask a qualified healthcare professional if the pattern or symptoms concern you."
            ),
            normalMessage = "This marker is below the selected reference for this entry; continue tracking in context.",
            icon = when {
                label.contains("waist", true) -> "📏"
                label.contains("pressure", true) -> "❤️"
                label.contains("glucose", true) -> "🍯"
                label.contains("hdl", true) -> "💛"
                else -> "🩸"
            },
            urgencyLevel = if (isMet) "caution" else "positive"
        )
    }

    fun getCardiovascularRiskSummary(criteriaMet: Int): CardiovascularRiskSummary {
        val bounded = criteriaMet.coerceIn(0, 5)
        return CardiovascularRiskSummary(
            riskLevel = "$bounded of 5 markers",
            riskDescription = "Marker count only; this is not an individual cardiovascular risk estimate.",
            riskScore = 0,
            overallMessage = "The app found $bounded of 5 screening markers at or beyond a reference. This is not a diagnosis or a prediction of cardiovascular events.",
            actionItems = listOf(
                "Review the values and units.",
                "Repeat measurements according to professional guidance.",
                "Share the summary with a healthcare professional if useful."
            ),
            shouldSeekMedical = false
        )
    }
}
