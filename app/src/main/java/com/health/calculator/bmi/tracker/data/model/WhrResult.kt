package com.health.calculator.bmi.tracker.data.model

data class WhrResult(
    val waistCm: Float,
    val hipCm: Float,
    val gender: Gender,
    val age: Int,
    val heightCm: Float?,

    // WHR
    val whr: Float,
    val whrCategory: WhrCategory,

    // WHtR
    val whtr: Float?,
    val whtrAtRisk: Boolean?,

    // Waist Circumference Risk
    val waistRiskLevel: WaistRiskLevel,
    val waistThresholdIncreased: Float,
    val waistThresholdHigh: Float,

    // Body Shape
    val bodyShape: BodyShape,

    // Health Risks
    val healthRisks: List<HealthRiskItem>,

    val timestamp: Long = System.currentTimeMillis()
)

enum class WhrCategory(
    val label: String,
    val description: String,
    val riskLevel: Int // 0=low, 1=moderate, 2=high
) {
    LOW_RISK("Low Risk", "Your body fat distribution is in a healthy range", 0),
    MODERATE_RISK("Moderate Risk", "Your body fat distribution shows some concern", 1),
    HIGH_RISK("High Risk", "Your body fat distribution indicates elevated health risks", 2)
}

enum class WaistRiskLevel(
    val label: String,
    val description: String,
    val riskLevel: Int
) {
    NORMAL("Normal", "Your waist circumference is within a healthy range", 0),
    INCREASED("Increased Risk", "Your waist circumference indicates increased health risk", 1),
    SUBSTANTIALLY_INCREASED("Substantially Increased Risk", "Your waist circumference indicates substantially increased health risk", 2)
}

enum class BodyShape(
    val label: String,
    val emoji: String,
    val description: String,
    val riskNote: String
) {
    APPLE(
        "Apple Shape",
        "🍎",
        "You carry more weight around your midsection",
        "Apple-shaped body types tend to have higher risk for cardiovascular disease and metabolic conditions"
    ),
    PEAR(
        "Pear Shape",
        "🍐",
        "You carry more weight around your hips and thighs",
        "Pear-shaped body types generally have lower cardiovascular risk compared to apple shapes"
    ),
    BALANCED(
        "Balanced",
        "⚖️",
        "Your waist and hip measurements are nearly equal",
        "Monitor your measurements regularly to track any changes"
    )
}



object WhrCalculator {

    fun calculate(
        waistCm: Float,
        hipCm: Float,
        gender: Gender,
        age: Int,
        heightCm: Float? = null
    ): WhrResult {
        val whr = waistCm / hipCm
        val whrCategory = classifyWhr(whr, gender)

        val whtr = heightCm?.let { waistCm / it }
        val whtrAtRisk = whtr?.let { it > 0.5f }

        val waistRisk = classifyWaistRisk(waistCm, gender)
        val waistThresholdIncreased = if (gender == Gender.MALE) 94f else 80f
        val waistThresholdHigh = if (gender == Gender.MALE) 102f else 88f

        val bodyShape = determineBodyShape(waistCm, hipCm)
        val healthRisks = buildHealthRisks(whrCategory, waistRisk, bodyShape, whtrAtRisk)

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
            healthRisks = healthRisks
        )
    }

    private fun classifyWhr(whr: Float, gender: Gender): WhrCategory {
        return when (gender) {
            Gender.MALE -> when {
                whr < 0.90f -> WhrCategory.LOW_RISK
                whr in 0.90f..0.99f -> WhrCategory.MODERATE_RISK
                else -> WhrCategory.HIGH_RISK
            }
            Gender.FEMALE -> when {
                whr < 0.80f -> WhrCategory.LOW_RISK
                whr in 0.80f..0.84f -> WhrCategory.MODERATE_RISK
                else -> WhrCategory.HIGH_RISK
            }
            else -> when {
                whr < 0.85f -> WhrCategory.LOW_RISK
                whr in 0.85f..0.89f -> WhrCategory.MODERATE_RISK
                else -> WhrCategory.HIGH_RISK
            }
        }
    }

    private fun classifyWaistRisk(waistCm: Float, gender: Gender): WaistRiskLevel {
        return when (gender) {
            Gender.MALE -> when {
                waistCm > 102f -> WaistRiskLevel.SUBSTANTIALLY_INCREASED
                waistCm > 94f -> WaistRiskLevel.INCREASED
                else -> WaistRiskLevel.NORMAL
            }
            Gender.FEMALE -> when {
                waistCm > 88f -> WaistRiskLevel.SUBSTANTIALLY_INCREASED
                waistCm > 80f -> WaistRiskLevel.INCREASED
                else -> WaistRiskLevel.NORMAL
            }
            else -> when {
                waistCm > 94f -> WaistRiskLevel.SUBSTANTIALLY_INCREASED
                waistCm > 87f -> WaistRiskLevel.INCREASED
                else -> WaistRiskLevel.NORMAL
            }
        }
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
        bodyShape: BodyShape,
        whtrAtRisk: Boolean?
    ): List<HealthRiskItem> {
        val risks = mutableListOf<HealthRiskItem>()

        // Cardiovascular risk
        val cvdSeverity = when {
            whrCategory.riskLevel >= 2 || waistRisk.riskLevel >= 2 -> RiskSeverity.HIGH
            whrCategory.riskLevel >= 1 || waistRisk.riskLevel >= 1 -> RiskSeverity.MODERATE
            else -> RiskSeverity.MILD
        }
        risks.add(
            HealthRiskItem(
                title = "Cardiovascular Disease",
                description = when (cvdSeverity) {
                    RiskSeverity.MILD -> "Your body fat distribution suggests lower cardiovascular risk. Maintain a healthy lifestyle."
                    RiskSeverity.MODERATE -> "Your measurements suggest moderate cardiovascular risk. Regular exercise and a balanced diet can help."
                    RiskSeverity.HIGH -> "Your body fat distribution indicates higher cardiovascular risk. Consider consulting a healthcare provider."
                    RiskSeverity.SEVERE -> "Significantly elevated cardiovascular risk. Please consult a healthcare provider."
                },
                severity = cvdSeverity,
                icon = "❤️"
            )
        )

        // Type 2 Diabetes risk
        val diabetesSeverity = when {
            waistRisk.riskLevel >= 2 -> RiskSeverity.HIGH
            waistRisk.riskLevel >= 1 || whrCategory.riskLevel >= 1 -> RiskSeverity.MODERATE
            else -> RiskSeverity.MILD
        }
        risks.add(
            HealthRiskItem(
                title = "Type 2 Diabetes",
                description = when (diabetesSeverity) {
                    RiskSeverity.MILD -> "Lower risk based on your current measurements. Keep up healthy habits."
                    RiskSeverity.MODERATE -> "Moderate risk indicated. Maintaining a healthy weight and active lifestyle can reduce this risk."
                    RiskSeverity.HIGH -> "Elevated risk suggested by your measurements. Regular blood sugar monitoring and lifestyle changes are recommended."
                    RiskSeverity.SEVERE -> "Significantly elevated risk. Please consult a healthcare provider for screening."
                },
                severity = diabetesSeverity,
                icon = "🩸"
            )
        )

        // Metabolic Syndrome
        val metabolicSeverity = when {
            whrCategory.riskLevel >= 2 && waistRisk.riskLevel >= 2 -> RiskSeverity.SEVERE
            whrCategory.riskLevel >= 2 || waistRisk.riskLevel >= 2 -> RiskSeverity.HIGH
            whrCategory.riskLevel >= 1 -> RiskSeverity.MODERATE
            else -> RiskSeverity.MILD
        }
        risks.add(
            HealthRiskItem(
                title = "Metabolic Syndrome",
                description = when (metabolicSeverity) {
                    RiskSeverity.MILD -> "Your measurements suggest lower metabolic syndrome risk."
                    RiskSeverity.MODERATE -> "Some indicators suggest moderate risk. A balanced diet and regular activity are beneficial."
                    RiskSeverity.HIGH -> "Multiple indicators suggest elevated risk. Consider comprehensive health screening."
                    RiskSeverity.SEVERE -> "Strong indicators of elevated metabolic risk. Please consult a healthcare provider."
                },
                severity = metabolicSeverity,
                icon = "⚠️"
            )
        )

        // Hypertension
        val htnSeverity = when {
            bodyShape == BodyShape.APPLE && waistRisk.riskLevel >= 2 -> RiskSeverity.HIGH
            bodyShape == BodyShape.APPLE || waistRisk.riskLevel >= 1 -> RiskSeverity.MODERATE
            else -> RiskSeverity.MILD
        }
        risks.add(
            HealthRiskItem(
                title = "Hypertension",
                description = when (htnSeverity) {
                    RiskSeverity.MILD -> "Lower hypertension risk based on your body composition."
                    RiskSeverity.MODERATE -> "Moderate risk for elevated blood pressure. Regular monitoring is recommended."
                    RiskSeverity.HIGH -> "Higher risk for hypertension. Regular blood pressure checks and lifestyle modifications are important."
                    RiskSeverity.SEVERE -> "Significantly elevated risk. Please consult a healthcare provider."
                },
                severity = htnSeverity,
                icon = "💉"
            )
        )

        return risks
    }
}
