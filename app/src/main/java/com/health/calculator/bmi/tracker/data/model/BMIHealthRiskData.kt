package com.health.calculator.bmi.tracker.data.model

/**
 * Informational BMI context used by the result screen.
 *
 * BMI is a screening measure based on height and weight. It does not measure
 * body fat, diagnose disease, or describe an individual's health on its own.
 */
data class HealthRiskInfo(
    val category: String,
    val categoryIcon: String,
    val toneMessage: String,
    val riskLevel: RiskLevel,
    val healthRisks: List<HealthRiskItem>,
    val recommendations: List<RecommendationItem>,
    val actionSteps: List<String>,
    val doctorNote: String
)

data class HealthRiskItem(
    val icon: String,
    val title: String,
    val description: String,
    val severity: RiskSeverity
)

data class RecommendationItem(
    val icon: String,
    val title: String,
    val description: String
)

/** Kept for UI/database compatibility; labels describe reference context, not diagnosis. */
enum class RiskLevel(val label: String, val emoji: String) {
    LOW("Reference range", "🟢"),
    MODERATE("Slightly outside reference", "🟡"),
    HIGH("Farther from reference", "🟠"),
    VERY_HIGH("Well outside reference", "🔴"),
    EXTREMELY_HIGH("Well outside reference", "🔴")
}

enum class RiskSeverity {
    MILD, MODERATE, HIGH, SEVERE
}

object BMIHealthRiskProvider {
    fun getHealthRiskInfo(bmi: Float, age: Int, isMale: Boolean): HealthRiskInfo {
        if (!bmi.isFinite() || bmi <= 0f) {
            return buildInfo(
                category = "Unavailable",
                icon = "ℹ️",
                tone = "Enter a valid height and weight to see an adult BMI reference category.",
                level = RiskLevel.MODERATE,
                detail = "BMI cannot be interpreted until the inputs are valid."
            )
        }

        val context = when {
            bmi < 16f -> BmiContext("Below 16.0", RiskLevel.VERY_HIGH, "🔴", "This value is well below the adult BMI reference range.", "Very low BMI can have many possible explanations; unintentional weight change or difficulty eating is worth discussing with a clinician.")
            bmi < 17f -> BmiContext("16.0–16.9", RiskLevel.HIGH, "🟠", "This value is below the adult BMI reference range.", "BMI is only one screening measure. Consider the direction of change, nutrition, and how you feel.")
            bmi < 18.5f -> BmiContext("17.0–18.4", RiskLevel.MODERATE, "🟡", "This value is slightly below the adult BMI reference range.", "A clinician or dietitian can help interpret this alongside your history and body composition.")
            bmi < 25f -> BmiContext("18.5–24.9", RiskLevel.LOW, "🟢", "This value is within the adult BMI reference range.", "A reference-range BMI does not rule out health concerns and does not measure body composition.")
            bmi < 30f -> BmiContext("25.0–29.9", RiskLevel.MODERATE, "🟡", "This value is above the adult BMI reference range.", "BMI can be influenced by muscle, age, ethnicity, and other factors; consider waist measurement and personal context.")
            bmi < 35f -> BmiContext("30.0–34.9", RiskLevel.HIGH, "🟠", "This value is well above the adult BMI reference range.", "A healthcare professional can help put this result in context and discuss goals that are appropriate for you.")
            bmi < 40f -> BmiContext("35.0–39.9", RiskLevel.VERY_HIGH, "🔴", "This value is well above the adult BMI reference range.", "BMI alone cannot identify a cause or determine treatment. Consider a routine conversation with a healthcare professional.")
            else -> BmiContext("40.0 or higher", RiskLevel.EXTREMELY_HIGH, "🔴", "This value is well above the adult BMI reference range.", "Please seek individualized, non-judgmental guidance from a healthcare professional; BMI is not a diagnosis.")
        }

        return buildInfo(context.category, context.icon, context.tone, context.level, context.detail)
    }

    private data class BmiContext(
        val category: String,
        val level: RiskLevel,
        val icon: String,
        val tone: String,
        val detail: String
    )

    private fun buildInfo(
        category: String,
        icon: String,
        tone: String,
        level: RiskLevel,
        detail: String
    ): HealthRiskInfo = HealthRiskInfo(
        category = category,
        categoryIcon = icon,
        toneMessage = tone,
        riskLevel = level,
        healthRisks = listOf(
            HealthRiskItem(
                icon = "ℹ️",
                title = "What this measure means",
                description = detail,
                severity = when (level) {
                    RiskLevel.LOW -> RiskSeverity.MILD
                    RiskLevel.MODERATE -> RiskSeverity.MODERATE
                    RiskLevel.HIGH -> RiskSeverity.HIGH
                    RiskLevel.VERY_HIGH, RiskLevel.EXTREMELY_HIGH -> RiskSeverity.SEVERE
                }
            ),
            HealthRiskItem(
                icon = "🧭",
                title = "Context matters",
                description = "BMI does not account for muscle mass, pregnancy, fluid changes, age, ethnicity, or where body fat is stored.",
                severity = RiskSeverity.MILD
            )
        ),
        recommendations = listOf(
            RecommendationItem(
                icon = "📏",
                title = "Add useful context",
                description = "Track a waist measurement, weight trend, activity, and how you feel rather than relying on a single number."
            ),
            RecommendationItem(
                icon = "🥗",
                title = "Choose sustainable habits",
                description = "Regular meals, varied foods, enjoyable movement, sleep, and support are more useful than extreme targets."
            ),
            RecommendationItem(
                icon = "👩‍⚕️",
                title = "Ask a professional when needed",
                description = "Discuss persistent symptoms, rapid unintentional change, pregnancy, or concerns about eating and weight with a qualified professional."
            )
        ),
        actionSteps = listOf(
            "Save this result as a baseline",
            "Review the trend instead of one reading",
            "Use waist and activity tracking for additional context",
            "Seek individualized guidance when a result worries you"
        ),
        doctorNote = "BMI is an informational screening measure, not a diagnosis. A clinician can interpret it with your history, examination, and other measurements."
    )
}
