package com.health.calculator.bmi.tracker.domain.usecase

import kotlin.math.pow

data class AdjustedWeightMetrics(
    val adjustedBodyWeightKg: Double?,
    val isAdjustedRelevant: Boolean,
    val leanBodyWeightKg: Double,
    val weightCategoryPercent: Double,
    val weightCategory: String,
    val weightCategoryDescription: String,
    val bmi: Double
) {
    fun getBodyFatPercent(actualWeightKg: Double): Double {
        if (actualWeightKg <= 0.1) return 0.0
        return ((actualWeightKg - leanBodyWeightKg) / actualWeightKg * 100).coerceIn(0.0, 70.0)
    }
}

class AdjustedWeightUseCase {

    fun calculate(
        actualWeightKg: Double,
        idealWeightKg: Double,
        heightCm: Double,
        isMale: Boolean
    ): AdjustedWeightMetrics {
        require(actualWeightKg.isFinite() && actualWeightKg > 0.0) { "Weight must be a positive finite value" }
        require(idealWeightKg.isFinite() && idealWeightKg > 0.0) { "Reference weight must be a positive finite value" }
        require(heightCm.isFinite() && heightCm in 100.0..250.0) { "Height must be between 100 and 250 cm" }
        val heightM = heightCm / 100.0
        val bmi = actualWeightKg / heightM.pow(2)

        // 1. Adjusted Body Weight (only if actual > 120% IBW)
        val percentOfIBW = (actualWeightKg / idealWeightKg) * 100.0
        val isAdjustedRelevant = percentOfIBW > 120.0
        val adjustedBodyWeight = if (isAdjustedRelevant) {
            idealWeightKg + 0.4 * (actualWeightKg - idealWeightKg)
        } else null

        // 2. Lean Body Weight (Hume-Weyers, Janmahasatian)
        val leanBodyWeight = if (isMale) {
            (9270.0 * actualWeightKg) / (6680.0 + 216.0 * bmi)
        } else {
            (9270.0 * actualWeightKg) / (8780.0 + 244.0 * bmi)
        }

        // 3. Weight category relative to IBW
        val (category, description) = when {
            percentOfIBW < 80 -> "Significantly Underweight" to
                    "This result is below the selected height-based reference. Consider your history, goals, and nutrition context with a qualified professional."
            percentOfIBW < 90 -> "Underweight" to
                    "This result is below the selected height-based reference. Use it as context rather than a diagnosis."
            percentOfIBW <= 110 -> "Normal / Ideal Range" to
                    "This result is close to the selected height-based reference. Body composition and personal goals still matter."
            percentOfIBW <= 120 -> "Overweight" to
                    "This result is above the selected height-based reference. Focus on sustainable habits rather than a single target."
            else -> "Significantly Overweight" to
                    "This result is well above the selected height-based reference. Individualized, non-judgmental guidance may be useful."
        }

        return AdjustedWeightMetrics(
            adjustedBodyWeightKg = adjustedBodyWeight,
            isAdjustedRelevant = isAdjustedRelevant,
            leanBodyWeightKg = leanBodyWeight.coerceAtLeast(0.0),
            weightCategoryPercent = percentOfIBW,
            weightCategory = category,
            weightCategoryDescription = description,
            bmi = bmi
        )
    }
}

data class SportWeightNote(
    val sport: String,
    val icon: String,
    val bmiRange: String,
    val note: String
)

fun getSportWeightNotes(): List<SportWeightNote> {
    return listOf(
        SportWeightNote(
            sport = "General Population",
            icon = "👤",
            bmiRange = "BMI 18.5 - 24.9",
            note = "Adult BMI reference context; not a personal target or diagnosis."
        ),
        SportWeightNote(
            sport = "Distance Running",
            icon = "🏃",
            bmiRange = "BMI 18.0 - 22.0",
            note = "Elite runners typically have lower body weight for optimal endurance and efficiency."
        ),
        SportWeightNote(
            sport = "Swimming",
            icon = "🏊",
            bmiRange = "BMI 21.0 - 25.0",
            note = "Swimmers maintain moderate weight with balanced muscle mass for buoyancy and power."
        ),
        SportWeightNote(
            sport = "Cycling",
            icon = "🚴",
            bmiRange = "BMI 19.0 - 23.0",
            note = "Cyclists aim for low weight relative to power output, especially climbers."
        ),
        SportWeightNote(
            sport = "Strength / Powerlifting",
            icon = "🏋️",
            bmiRange = "BMI 25.0 - 35.0+",
            note = "Strength athletes often exceed standard BMI ranges due to significantly higher muscle mass."
        ),
        SportWeightNote(
            sport = "Bodybuilding",
            icon = "💪",
            bmiRange = "BMI 25.0 - 30.0",
            note = "Bodybuilders have high lean mass. Standard IBW formulas may not apply."
        ),
        SportWeightNote(
            sport = "Martial Arts",
            icon = "🥋",
            bmiRange = "BMI 20.0 - 26.0",
            note = "Varies by weight class. Athletes optimize weight for their competitive division."
        ),
        SportWeightNote(
            sport = "Team Sports (Football, Basketball)",
            icon = "⚽",
            bmiRange = "BMI 22.0 - 28.0",
            note = "Varies by position. Linemen vs. guards have very different ideal weights."
        )
    )
}
