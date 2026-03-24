package com.health.calculator.bmi.tracker.data.model

data class VisceralFatAssessment(
    val riskLevel: VisceralFatRisk,
    val estimatedLevel: Int, // 1-20 scale
    val waistCm: Float,
    val age: Int,
    val gender: Gender
)

enum class VisceralFatRisk(
    val label: String,
    val description: String,
    val levelRange: String,
    val riskLevel: Int // 0-3
) {
    LOW(
        "Low",
        "Your visceral fat level appears to be in a healthy range. Continue maintaining your current lifestyle.",
        "1-9",
        0
    ),
    MODERATE(
        "Moderate",
        "Your visceral fat level suggests some accumulation around organs. Lifestyle modifications can help.",
        "10-14",
        1
    ),
    HIGH(
        "High",
        "Your visceral fat level is elevated, increasing risk for metabolic conditions. Consider lifestyle changes.",
        "15-17",
        2
    ),
    VERY_HIGH(
        "Very High",
        "Your visceral fat level is significantly elevated. Please consult a healthcare provider for guidance.",
        "18-20",
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
    NORMAL("Normal", 0),
    ELEVATED("At Risk", 1),
    OBESE("Abdominal Obesity", 2)
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
        "Low Risk",
        "Your central obesity indicators are within healthy ranges. Keep up the good work!",
        0
    ),
    MODERATE(
        "Moderate Risk",
        "Some of your central obesity indicators suggest moderate concern. Lifestyle improvements can help.",
        1
    ),
    HIGH(
        "High Risk",
        "Multiple indicators suggest elevated central obesity risk. Consider consulting a healthcare provider.",
        2
    ),
    VERY_HIGH(
        "Very High Risk",
        "Your central obesity indicators suggest significantly elevated health risks. Please seek medical advice.",
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
        // Estimation based on waist circumference, age, and gender
        // This is a simplified model — actual measurement requires DEXA/CT scan
        val baseLevel = when (gender) {
            Gender.MALE -> {
                val waistFactor = ((waistCm - 70f) / 5f).coerceIn(0f, 12f)
                val ageFactor = ((age - 20f) / 10f).coerceIn(0f, 5f)
                (waistFactor + ageFactor).toInt().coerceIn(1, 20)
            }
            Gender.FEMALE -> {
                val waistFactor = ((waistCm - 60f) / 5f).coerceIn(0f, 12f)
                val ageFactor = ((age - 20f) / 12f).coerceIn(0f, 5f)
                (waistFactor + ageFactor).toInt().coerceIn(1, 20)
            }
            else -> {
                val waistFactor = ((waistCm - 65f) / 5f).coerceIn(0f, 12f)
                val ageFactor = ((age - 20f) / 11f).coerceIn(0f, 5f)
                (waistFactor + ageFactor).toInt().coerceIn(1, 20)
            }
        }

        val risk = when {
            baseLevel <= 9 -> VisceralFatRisk.LOW
            baseLevel <= 14 -> VisceralFatRisk.MODERATE
            baseLevel <= 17 -> VisceralFatRisk.HIGH
            else -> VisceralFatRisk.VERY_HIGH
        }

        return VisceralFatAssessment(
            riskLevel = risk,
            estimatedLevel = baseLevel,
            waistCm = waistCm,
            age = age,
            gender = gender
        )
    }

    fun classifyAbdominalObesity(
        waistCm: Float,
        gender: Gender
    ): AbdominalObesityResult {
        val whoThreshold = if (gender == Gender.FEMALE) 88f else 102f
        val idfThreshold = if (gender == Gender.FEMALE) 80f else 94f

        val whoClass = when {
            waistCm >= whoThreshold -> AbdominalObesityClass.OBESE
            waistCm >= idfThreshold -> AbdominalObesityClass.ELEVATED
            else -> AbdominalObesityClass.NORMAL
        }

        val idfClass = when {
            waistCm >= whoThreshold -> AbdominalObesityClass.OBESE
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
        val viscRisk = visceralFat.riskLevel.riskLevel

        val activeFactors = mutableListOf(whrRisk, waistRisk, viscRisk)
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
        val tips = mutableListOf<ImprovementTip>()
        val waistThreshold = if (gender == Gender.FEMALE) 80f else 94f
        val cmToReduce = (waistCm - waistThreshold).coerceAtLeast(0f)

        when (overallRisk) {
            OverallCentralRisk.LOW -> {
                tips.add(
                    ImprovementTip(
                        TipCategory.EXERCISE, "Stay Active",
                        "Continue with at least 150 minutes of moderate exercise per week to maintain your healthy metrics.",
                        "🏃", 1
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.DIET, "Balanced Nutrition",
                        "Maintain your balanced diet rich in fruits, vegetables, lean proteins, and whole grains.",
                        "🥗", 2
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.LIFESTYLE, "Regular Monitoring",
                        "Continue tracking your measurements monthly to stay on top of your health.",
                        "📊", 3
                    )
                )
            }
            OverallCentralRisk.MODERATE -> {
                tips.add(
                    ImprovementTip(
                        TipCategory.EXERCISE, "Increase Cardio Activity",
                        "Aim for 200-300 minutes of moderate cardio per week. Brisk walking, cycling, and swimming are excellent for reducing waist circumference.",
                        "🚴", 1
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.EXERCISE, "Core & Strength Training",
                        "Add 2-3 strength training sessions per week. Building muscle helps burn visceral fat more effectively than cardio alone.",
                        "💪", 2
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.DIET, "Reduce Added Sugars",
                        "Cut back on sugary drinks, processed snacks, and refined carbs. These are strongly linked to visceral fat accumulation.",
                        "🚫", 3
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.DIET, "Increase Fiber Intake",
                        "Aim for 25-30g of fiber daily. Soluble fiber helps reduce visceral fat — try oats, beans, flaxseeds, and vegetables.",
                        "🌾", 4
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.LIFESTYLE, "Manage Stress",
                        "Chronic stress raises cortisol, which promotes fat storage around the abdomen. Try meditation, yoga, or deep breathing.",
                        "🧘", 5
                    )
                )
                if (cmToReduce > 0) {
                    tips.add(
                        ImprovementTip(
                            TipCategory.MEDICAL, "Reduction Target",
                            "Reducing your waist by ${String.format("%.1f", cmToReduce)} cm could move you to a lower risk category. A loss of 1-2 cm per month is a realistic goal.",
                            "🎯", 6
                        )
                    )
                }
            }
            OverallCentralRisk.HIGH, OverallCentralRisk.VERY_HIGH -> {
                tips.add(
                    ImprovementTip(
                        TipCategory.MEDICAL, "Consult a Healthcare Provider",
                        "Your measurements suggest elevated health risks. A doctor can perform thorough assessments and create a personalized plan.",
                        "👨‍⚕️", 1
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.EXERCISE, "Start with Walking",
                        "Begin with 30-minute brisk walks daily and gradually increase intensity. High-Intensity Interval Training (HIIT) is very effective for visceral fat.",
                        "🚶", 2
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.EXERCISE, "Resistance Training",
                        "Incorporate resistance exercises 3-4 times per week. Muscle mass increases metabolism and helps target abdominal fat.",
                        "🏋️", 3
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.DIET, "Mediterranean Diet Approach",
                        "Focus on olive oil, fish, nuts, vegetables, and whole grains. This diet pattern is proven to reduce visceral fat and improve metabolic health.",
                        "🫒", 4
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.DIET, "Eliminate Trans Fats",
                        "Avoid hydrogenated oils, fried foods, and ultra-processed snacks. Trans fats directly contribute to abdominal fat deposits.",
                        "⛔", 5
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.DIET, "Control Portions",
                        "Use smaller plates, eat slowly, and stop when 80% full. A moderate calorie deficit of 500 kcal/day leads to ~0.5kg/week loss.",
                        "🍽️", 6
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.LIFESTYLE, "Prioritize Sleep",
                        "Aim for 7-9 hours of quality sleep. Poor sleep disrupts hormones (leptin, ghrelin, cortisol) that regulate fat storage.",
                        "😴", 7
                    )
                )
                tips.add(
                    ImprovementTip(
                        TipCategory.LIFESTYLE, "Limit Alcohol",
                        "Reduce alcohol intake — excess alcohol is directly linked to increased abdominal fat ('beer belly').",
                        "🍷", 8
                    )
                )
                if (cmToReduce > 0) {
                    tips.add(
                        ImprovementTip(
                            TipCategory.MEDICAL, "Waist Reduction Target",
                            "Reducing your waist by ${String.format("%.1f", cmToReduce)} cm would significantly lower your risk. Even a 5 cm reduction provides meaningful health benefits.",
                            "📏", 9
                        )
                    )
                }
            }
        }

        return tips.sortedBy { it.priority }
    }
}
