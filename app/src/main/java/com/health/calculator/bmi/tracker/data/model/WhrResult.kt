package com.health.calculator.bmi.tracker.data.model

import com.health.calculator.bmi.tracker.calculator.Ethnicity

data class WhrResult(
    val waistCm: Float,
    val hipCm: Float,
    val gender: Gender,
    val age: Int,
    val heightCm: Float?,
    val whr: Float,
    val whrCategory: WhrCategory,
    val whtr: Float?,
    val whtrAtRisk: Boolean?,
    val waistRiskLevel: WaistRiskLevel,
    val waistThresholdIncreased: Float,
    val waistThresholdHigh: Float,
    val bodyShape: BodyShape,
    val healthRisks: List<HealthRiskItem>,
    val ethnicity: Ethnicity = Ethnicity.EUROPID,
    val timestamp: Long = System.currentTimeMillis()
)

enum class WhrCategory(
    val label: String,
    val description: String,
    val riskLevel: Int
) {
    LOW_RISK("Below action point", "Below the commonly used WHO waist-to-hip action point", 0),
    MODERATE_RISK("At action point", "At or near a population reference action point", 1),
    HIGH_RISK("Above action point", "Above a population reference action point; context is needed", 2)
}

enum class WaistRiskLevel(
    val label: String,
    val description: String,
    val riskLevel: Int
) {
    NORMAL("Below reference", "Below the selected population waist reference", 0),
    INCREASED("At or above reference", "At or above the selected population waist reference", 1),
    SUBSTANTIALLY_INCREASED("Higher reference band", "At or above the higher waist reference used for comparison", 2)
}

enum class BodyShape(
    val label: String,
    val emoji: String,
    val description: String,
    val riskNote: String
) {
    APPLE("More waist-centered", "🍎", "The waist measurement is larger than the hip measurement.", "Body shape is descriptive and cannot diagnose a condition."),
    PEAR("More hip-centered", "🍐", "The hip measurement is larger than the waist measurement.", "Body shape is descriptive and cannot diagnose a condition."),
    BALANCED("Similar waist and hip", "⚖️", "The waist and hip measurements are relatively similar.", "Body shape is descriptive and cannot diagnose a condition.")
}

object WhrCalculator {
    fun calculate(
        waistCm: Float,
        hipCm: Float,
        gender: Gender,
        age: Int,
        heightCm: Float? = null,
        ethnicity: Ethnicity = Ethnicity.EUROPID
    ): WhrResult {
        require(waistCm.isFinite() && hipCm.isFinite() && waistCm > 0f && hipCm > 0f) {
            "Waist and hip measurements must be positive"
        }
        require(age in 18..120) { "This adult comparison is for ages 18–120" }
        require(heightCm == null || (heightCm.isFinite() && heightCm > 0f)) { "Height must be positive" }

        val whr = waistCm / hipCm
        val whrCategory = classifyWhr(whr, gender)
        val whtr = heightCm?.let { waistCm / it }
        val whtrAtRisk = whtr?.let { it >= 0.5f }
        val waistThresholdIncreased = if (gender == Gender.MALE) ethnicity.maleWaistCm else ethnicity.femaleWaistCm
        val waistThresholdHigh = if (gender == Gender.MALE) {
            if (ethnicity == Ethnicity.US_ATP) 102f else waistThresholdIncreased + 8f
        } else {
            if (ethnicity == Ethnicity.US_ATP) 88f else waistThresholdIncreased + 8f
        }
        val waistRisk = classifyWaistRisk(waistCm, waistThresholdIncreased, waistThresholdHigh)
        val bodyShape = determineBodyShape(waistCm, hipCm)

        return WhrResult(
            waistCm = waistCm,
            hipCm = hipCm,
            gender = gender,
            age = age,
            heightCm = heightCm,
            whr = whr,
            whrCategory = whrCategory,
            whtr = whtr,
            whtrAtRisk = whtrAtRisk,
            waistRiskLevel = waistRisk,
            waistThresholdIncreased = waistThresholdIncreased,
            waistThresholdHigh = waistThresholdHigh,
            bodyShape = bodyShape,
            healthRisks = buildHealthRisks(whrCategory, waistRisk, whtrAtRisk),
            ethnicity = ethnicity
        )
    }

    private fun classifyWhr(whr: Float, gender: Gender): WhrCategory {
        val actionPoint = if (gender == Gender.MALE) 0.90f else 0.85f
        return when {
            whr < actionPoint - 0.05f -> WhrCategory.LOW_RISK
            whr < actionPoint -> WhrCategory.MODERATE_RISK
            else -> WhrCategory.HIGH_RISK
        }
    }

    private fun classifyWaistRisk(waistCm: Float, increased: Float, high: Float): WaistRiskLevel = when {
        waistCm >= high -> WaistRiskLevel.SUBSTANTIALLY_INCREASED
        waistCm >= increased -> WaistRiskLevel.INCREASED
        else -> WaistRiskLevel.NORMAL
    }

    private fun determineBodyShape(waistCm: Float, hipCm: Float): BodyShape {
        val ratio = waistCm / hipCm
        return when {
            ratio > 1.02f -> BodyShape.APPLE
            ratio < 0.95f -> BodyShape.PEAR
            else -> BodyShape.BALANCED
        }
    }

    private fun buildHealthRisks(
        whrCategory: WhrCategory,
        waistRisk: WaistRiskLevel,
        whtrAtRisk: Boolean?
    ): List<HealthRiskItem> = listOf(
        HealthRiskItem(
            icon = "ℹ️",
            title = "Use as body-shape context",
            description = "Waist-to-hip ratio and waist circumference describe body proportions. They are population-level screening measures and cannot diagnose cardiovascular disease, diabetes, hypertension, or metabolic syndrome.",
            severity = if (whrCategory == WhrCategory.HIGH_RISK || waistRisk == WaistRiskLevel.SUBSTANTIALLY_INCREASED) RiskSeverity.MODERATE else RiskSeverity.MILD
        ),
        HealthRiskItem(
            icon = "📏",
            title = "Waist-to-height reference",
            description = when (whtrAtRisk) {
                true -> "A waist-to-height ratio at or above 0.5 is a prompt to review the measurement and personal context; it is not a diagnosis."
                false -> "A waist-to-height ratio below 0.5 is below the commonly used action point; it does not rule out health concerns."
                null -> "Add height to see the waist-to-height reference."
            },
            severity = RiskSeverity.MILD
        )
    )
}
