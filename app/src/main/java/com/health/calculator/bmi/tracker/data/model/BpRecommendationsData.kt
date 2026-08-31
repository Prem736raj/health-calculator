package com.health.calculator.bmi.tracker.data.model

data class BpRecommendation(
    val icon: String,
    val title: String,
    val description: String
)

data class BpCategoryGuidance(
    val headerEmoji: String,
    val headerMessage: String,
    val headerTone: BpGuidanceTone,
    val monitoringFrequency: String,
    val recommendations: List<BpRecommendation>,
    val dietTips: List<BpRecommendation>,
    val exerciseTips: List<BpRecommendation>,
    val healthRisks: List<String>,
    val doctorAdvice: String?,
    val urgencyNote: String?
)

/** Presentation tone retained for saved UI state; wording is intentionally non-diagnostic. */
enum class BpGuidanceTone {
    POSITIVE,
    GENTLE_AWARENESS,
    CAUTIOUS,
    CONCERNED,
    URGENT,
    EMERGENCY,
    INFORMATIONAL
}

/**
 * Safe, measurement-focused blood-pressure education.
 *
 * A single home reading cannot diagnose hypertension or predict an event. The
 * app offers repeat-measurement guidance and escalation language without
 * prescribing medicines or claiming a user's risk.
 */
object BpRecommendationsProvider {

    fun getGuidance(category: BpCategory): BpCategoryGuidance {
        val isSeverelyElevated = category == BpCategory.GRADE_3_HYPERTENSION ||
                category == BpCategory.HYPERTENSIVE_CRISIS
        val isAboveReference = category == BpCategory.HIGH_NORMAL ||
                category == BpCategory.ISOLATED_SYSTOLIC ||
                category == BpCategory.GRADE_1_HYPERTENSION ||
                category == BpCategory.GRADE_2_HYPERTENSION ||
                isSeverelyElevated

        val (emoji, message, tone, frequency) = when (category) {
            BpCategory.OPTIMAL -> Quad("✅", "This reading is within the usual adult reference range.", BpGuidanceTone.POSITIVE, "Occasionally or as advised")
            BpCategory.NORMAL -> Quad("📈", "The top number is in an elevated reference range; a single reading is not a diagnosis.", BpGuidanceTone.GENTLE_AWARENESS, "Repeat on separate days")
            BpCategory.HIGH_NORMAL -> Quad("📋", "One or both numbers are in a Stage 1 reference range. Review the technique and trend.", BpGuidanceTone.CAUTIOUS, "Repeat on separate days")
            BpCategory.ISOLATED_SYSTOLIC -> Quad("📋", "The top number is elevated while the bottom number is below 90. Confirm with repeat readings.", BpGuidanceTone.CAUTIOUS, "Repeat on separate days")
            BpCategory.GRADE_1_HYPERTENSION, BpCategory.GRADE_2_HYPERTENSION -> Quad("🩺", "This reading is in a Stage 2 reference range. Recheck carefully and discuss repeated results with a professional.", BpGuidanceTone.CONCERNED, "Follow a clinician's advice")
            BpCategory.GRADE_3_HYPERTENSION, BpCategory.HYPERTENSIVE_CRISIS -> Quad("⚠️", "This reading is markedly elevated. Sit quietly, repeat it with a validated cuff, and seek prompt medical advice if it remains high or symptoms are present.", BpGuidanceTone.URGENT, "Repeat promptly; follow professional advice")
            BpCategory.HYPOTENSION -> Quad("💙", "This reading is below the usual adult reference range. Symptoms and repeated readings provide important context.", BpGuidanceTone.INFORMATIONAL, "Repeat when rested or as advised")
        }

        val common = listOf(
            BpRecommendation("📏", "Check the setup", "Rest for five minutes, keep the cuff on bare skin at heart level, and avoid talking during the reading."),
            BpRecommendation("🔁", "Look for a pattern", "Take two readings about one minute apart and record the date, time and context."),
            BpRecommendation("📝", "Keep the trend", "Share repeated readings and any symptoms with a qualified healthcare professional."),
            BpRecommendation("🚫", "No self-prescribing", "Do not start, stop or change medicines based on this app or one reading.")
        )

        val lifestyle = listOf(
            BpRecommendation("🥗", "Balanced meals", "Include vegetables, fruit, whole grains and varied protein sources; adapt to your dietary needs."),
            BpRecommendation("🧂", "Notice sodium", "Food labels and less-processed choices can help you understand sodium intake; targets vary by context."),
            BpRecommendation("🚶", "Move comfortably", "Choose regular, enjoyable activity that fits your abilities. Ask a professional before major changes if you have concerns."),
            BpRecommendation("😴", "Support recovery", "Sleep, stress support and avoiding tobacco can support overall wellbeing.")
        )

        val riskContext = when {
            isSeverelyElevated -> listOf("Markedly elevated readings deserve prompt confirmation and professional advice.", "Symptoms and repeat measurements change the appropriate next step.")
            isAboveReference -> listOf("One reading cannot establish a diagnosis.", "Repeated measurements, technique and personal context matter.")
            category == BpCategory.HYPOTENSION -> listOf("Some people have lower readings without a problem.", "Dizziness, fainting or persistent symptoms warrant professional advice.")
            else -> emptyList()
        }

        val doctorAdvice = when {
            isSeverelyElevated -> "If a careful repeat is still at or above 180/120 mmHg, or you have concerning symptoms, seek urgent medical care or local emergency services."
            isAboveReference -> "Discuss a pattern of elevated readings with a qualified healthcare professional; do not infer a diagnosis from this screen."
            category == BpCategory.HYPOTENSION -> "Discuss repeated low readings or symptoms such as dizziness or fainting with a qualified healthcare professional."
            else -> null
        }

        return BpCategoryGuidance(
            headerEmoji = emoji,
            headerMessage = message,
            headerTone = tone,
            monitoringFrequency = frequency,
            recommendations = common,
            dietTips = lifestyle.take(2),
            exerciseTips = lifestyle.drop(2),
            healthRisks = riskContext,
            doctorAdvice = doctorAdvice,
            urgencyNote = if (isSeverelyElevated) "Repeat after resting and seek prompt care if it remains high or symptoms are present." else null
        )
    }

    fun getWhiteCoatSyndromeInfo(): List<BpRecommendation> = listOf(
        BpRecommendation("🏥", "Clinic and home readings can differ", "Stress, timing and technique can change a measurement; one setting is not automatically the whole picture."),
        BpRecommendation("📊", "Use a consistent routine", "Use a validated upper-arm monitor, rest first, and record two readings about one minute apart."),
        BpRecommendation("📋", "Share the log", "Bring home readings and the monitor to an appointment when possible so a professional can interpret the pattern."),
        BpRecommendation("🩺", "Keep context", "Medicines, caffeine, exercise, pain and illness can affect readings; note anything relevant."),
        BpRecommendation("ℹ️", "Not a diagnosis", "The app cannot determine white-coat hypertension or masked hypertension by itself.")
    )

    private data class Quad(
        val first: String,
        val second: String,
        val third: BpGuidanceTone,
        val fourth: String
    )
}
