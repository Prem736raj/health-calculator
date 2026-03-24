// File: com/health/calculator/bmi/tracker/data/model/BMRTrendData.kt
package com.health.calculator.bmi.tracker.data.model

data class BMRHistoryPoint(
    val bmr: Float,
    val tdee: Float,
    val weightKg: Float,
    val formulaName: String,
    val activityLevel: String,
    val timestamp: Long,
    val dateLabel: String
)

data class BMRTrendStats(
    val currentBMR: Float = 0f,
    val averageBMR: Float = 0f,
    val highestBMR: Float = 0f,
    val lowestBMR: Float = 0f,
    val firstBMR: Float = 0f,
    val changeFromFirst: Float = 0f,
    val changePercentFromFirst: Float = 0f,
    val currentTDEE: Float = 0f,
    val averageTDEE: Float = 0f,
    val totalReadings: Int = 0,
    val previousBMR: Float = 0f,
    val changeFromPrevious: Float = 0f,
    val previousWeight: Float = 0f,
    val currentWeight: Float = 0f,
    val weightChange: Float = 0f
) {
    val hasMultipleReadings: Boolean get() = totalReadings > 1
    val hasPreviousReading: Boolean get() = totalReadings > 1
    val bmrIncreased: Boolean get() = changeFromPrevious > 0
    val bmrDecreased: Boolean get() = changeFromPrevious < 0

    fun getChangeInsight(): BMRChangeInsight {
        if (!hasMultipleReadings) return BMRChangeInsight.NO_DATA

        val absChange = kotlin.math.abs(changeFromPrevious)
        val absPercent = kotlin.math.abs(changeFromPrevious / previousBMR * 100f)

        return when {
            absPercent < 1f -> BMRChangeInsight.STABLE
            changeFromPrevious > 0 && weightChange > 0 -> BMRChangeInsight.INCREASED_WEIGHT_GAIN
            changeFromPrevious > 0 && weightChange <= 0 -> BMRChangeInsight.INCREASED_MUSCLE_GAIN
            changeFromPrevious < 0 && weightChange < 0 -> BMRChangeInsight.DECREASED_WEIGHT_LOSS
            changeFromPrevious < 0 && weightChange >= 0 -> BMRChangeInsight.DECREASED_CONCERNING
            else -> BMRChangeInsight.STABLE
        }
    }
}

enum class BMRChangeInsight(
    val emoji: String,
    val title: String,
    val message: String,
    val isPositive: Boolean
) {
    NO_DATA(
        emoji = "📊",
        title = "Start Tracking",
        message = "Track your BMR regularly to see trends and insights. We recommend checking monthly or whenever your weight changes significantly.",
        isPositive = true
    ),
    STABLE(
        emoji = "✅",
        title = "BMR Stable",
        message = "Your BMR has remained consistent. This indicates stable body composition and metabolic health. Keep up your current routine!",
        isPositive = true
    ),
    INCREASED_WEIGHT_GAIN(
        emoji = "📈",
        title = "BMR Increased",
        message = "Your BMR has increased alongside weight gain. This is expected — a larger body requires more energy at rest. If you're strength training, some of this may be muscle mass.",
        isPositive = true
    ),
    INCREASED_MUSCLE_GAIN(
        emoji = "💪",
        title = "BMR Increased",
        message = "Your BMR has increased even without weight gain (or with weight loss). This often indicates improved body composition — possibly more muscle mass. Great work!",
        isPositive = true
    ),
    DECREASED_WEIGHT_LOSS(
        emoji = "📉",
        title = "BMR Decreased",
        message = "Your BMR has decreased, which is expected with weight loss. To minimize BMR decline, maintain adequate protein intake and include strength training in your routine.",
        isPositive = false
    ),
    DECREASED_CONCERNING(
        emoji = "⚠️",
        title = "BMR Decreased",
        message = "Your BMR has decreased without significant weight loss. This could indicate decreased muscle mass or metabolic adaptation. Consider increasing protein intake and adding resistance exercise. If concerned, consult a healthcare provider.",
        isPositive = false
    )
}
