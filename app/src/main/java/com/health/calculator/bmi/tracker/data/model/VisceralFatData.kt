package com.health.calculator.bmi.tracker.data.model

data class VisceralFatAssessment(
    val riskLevel: VisceralFatRisk,
    val estimatedLevel: Int, // 0 because no validated level is estimated in-app
    val waistCm: Float,
    val age: Int,
    val gender: Gender,
    val isAvailable: Boolean = false
)

enum class VisceralFatRisk(
    val label: String,
    val description: String,
    val levelRange: String,
    val riskLevel: Int // 0-3
) {
    LOW(
        "Not estimated",
        "Waist, age, and gender cannot measure visceral fat. A validated scan or device would be required.",
        "—",
        0
    ),
    MODERATE(
        "Not available",
        "Visceral fat cannot be estimated from waist, age, or gender in this app.",
        "—",
        1
    ),
    HIGH(
        "Not available",
        "Visceral fat cannot be estimated from waist, age, or gender in this app.",
        "—",
        2
    ),
    VERY_HIGH(
        "Not available",
        "Visceral fat cannot be estimated from waist, age, or gender in this app.",
        "—",
        3
    )
}

data class AbdominalObesityResult(
    val whoClassification: AbdominalObesityClass,
    val idfClassification: AbdominalObesityClass,
    val whoThreshold: Float,
    val idfThreshold: Float,
    val waistCm: Float,
    val gender: Gender
)

enum class AbdominalObesityClass(
    val label: String,
    val riskLevel: Int
) {
    NORMAL("Below reference", 0),
    ELEVATED("Above action point", 1),
    OBESE("Well above action point", 2)
}

data class CombinedRiskSummary(
    val whrRisk: Int,
    val waistRisk: Int,
    val whtrRisk: Int,
    val visceralRisk: Int,
    val overallRisk: OverallCentralRisk,
    val riskFactorCount: Int,
    val totalFactors: Int
)

enum class OverallCentralRisk(
    val label: String,
    val description: String,
    val riskLevel: Int
) {
    LOW(
        "Few indicators",
        "Few selected waist and ratio indicators are beyond their reference points; this is not a diagnosis.",
        0
    ),
    MODERATE(
        "Some indicators",
        "Some selected waist and ratio indicators are beyond their reference points; review the trend and context.",
        1
    ),
    HIGH(
        "Several indicators",
        "Several selected waist and ratio indicators are beyond their reference points; individualized guidance may help.",
        2
    ),
    VERY_HIGH(
        "Many indicators",
        "Many selected waist and ratio indicators are beyond their reference points; discuss the pattern with a professional.",
        3
    )
}

data class ImprovementTip(
    val category: TipCategory,
    val title: String,
    val description: String,
    val icon: String,
    val priority: Int // 1=highest
)

enum class TipCategory {
    EXERCISE, DIET, LIFESTYLE, MEDICAL
}

object VisceralFatCalculator {

    fun estimateVisceralFat(
        waistCm: Float,
        age: Int,
        gender: Gender
    ): VisceralFatAssessment {
        require(waistCm.isFinite() && waistCm in 30f..250f) { "Waist must be between 30 and 250 cm" }
        require(age in 18..120) { "This assessment supports adult ages 18–120" }
        return VisceralFatAssessment(
            riskLevel = VisceralFatRisk.LOW,
            estimatedLevel = 0,
            waistCm = waistCm,
            age = age,
            gender = gender,
            isAvailable = false
        )
    }

    fun classifyAbdominalObesity(
        waistCm: Float,
        gender: Gender
    ): AbdominalObesityResult {
        require(waistCm.isFinite() && waistCm in 30f..250f) { "Waist must be between 30 and 250 cm" }
        val whoThreshold = if (gender == Gender.FEMALE) 88f else 102f
        val idfThreshold = if (gender == Gender.FEMALE) 80f else 94f

        val whoClass = when {
            waistCm >= whoThreshold -> AbdominalObesityClass.OBESE
            waistCm >= idfThreshold -> AbdominalObesityClass.ELEVATED
            else -> AbdominalObesityClass.NORMAL
        }

        val idfClass = when {
            waistCm >= idfThreshold -> AbdominalObesityClass.ELEVATED
            else -> AbdominalObesityClass.NORMAL
        }

        return AbdominalObesityResult(
            whoClassification = whoClass,
            idfClassification = idfClass,
            whoThreshold = whoThreshold,
            idfThreshold = idfThreshold,
            waistCm = waistCm,
            gender = gender
        )
    }

    fun buildCombinedRiskSummary(
        whrCategory: WhrCategory,
        waistRiskLevel: WaistRiskLevel,
        whtrAtRisk: Boolean?,
        visceralFat: VisceralFatAssessment
    ): CombinedRiskSummary {
        val whrRisk = whrCategory.riskLevel
        val waistRisk = waistRiskLevel.riskLevel
        val whtrRisk = if (whtrAtRisk == true) 2 else if (whtrAtRisk == false) 0 else -1
        // Visceral fat is deliberately excluded: this app cannot measure it.
        val viscRisk = 0
        val activeFactors = mutableListOf(whrRisk, waistRisk)
        if (whtrRisk >= 0) activeFactors.add(whtrRisk)

        val avgRisk = activeFactors.average()
        val highRiskCount = activeFactors.count { it >= 2 }

        val overall = when {
            avgRisk >= 2.5 || highRiskCount >= 3 -> OverallCentralRisk.VERY_HIGH
            avgRisk >= 1.5 || highRiskCount >= 2 -> OverallCentralRisk.HIGH
            avgRisk >= 0.8 || highRiskCount >= 1 -> OverallCentralRisk.MODERATE
            else -> OverallCentralRisk.LOW
        }

        val riskFactorCount = activeFactors.count { it >= 1 }

        return CombinedRiskSummary(
            whrRisk = whrRisk,
            waistRisk = waistRisk,
            whtrRisk = whtrRisk,
            visceralRisk = viscRisk,
            overallRisk = overall,
            riskFactorCount = riskFactorCount,
            totalFactors = activeFactors.size
        )
    }

    fun generateImprovementTips(
        overallRisk: OverallCentralRisk,
        waistCm: Float,
        gender: Gender,
        whrCategory: WhrCategory
    ): List<ImprovementTip> {
        // These tips support measurement quality and sustainable wellness
        // habits. They do not promise visceral-fat reduction or a risk change.
        return listOf(
            ImprovementTip(
                TipCategory.LIFESTYLE,
                "Track the trend",
                "Measure at a similar time and position, then review changes over several weeks rather than reacting to one reading.",
                "📊", 1
            ),
            ImprovementTip(
                TipCategory.EXERCISE,
                "Choose enjoyable movement",
                "Build regular activity that fits your abilities. Start gradually and stop if you feel unwell.",
                "🚶", 2
            ),
            ImprovementTip(
                TipCategory.DIET,
                "Keep meals varied",
                "A varied pattern of foods and adequate fluids is more sustainable than extreme restriction or a single ‘fat-burning’ plan.",
                "🥗", 3
            ),
            ImprovementTip(
                TipCategory.MEDICAL,
                "Use professional context",
                "Waist measures cannot show visceral fat or diagnose a condition. Ask a healthcare professional if results or symptoms concern you.",
                "👩‍⚕️", 4
            )
        )
    }
}
