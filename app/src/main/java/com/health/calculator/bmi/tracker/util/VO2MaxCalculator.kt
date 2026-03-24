package com.health.calculator.bmi.tracker.util

import androidx.compose.ui.graphics.Color

data class VO2MaxResult(
    val vo2Max: Float,
    val classification: VO2Classification,
    val fitnessAge: Int,
    val actualAge: Int,
    val fitnessAgeMessage: String,
    val percentile: Int,
    val improvementPotential: Float,
    val projectedVO2After6Months: Float
)

data class VO2Classification(
    val category: String,
    val color: Color,
    val emoji: String,
    val description: String,
    val rangeLabel: String
)

data class RecoveryHRGuideline(
    val category: String,
    val dropInFirstMinute: String,
    val color: Color,
    val emoji: String,
    val description: String
)

object VO2MaxCalculator {

    /**
     * Estimate VO2 Max using Uth et al. formula
     * VO2max ≈ 15.3 × (MHR / RHR)
     */
    fun estimateVO2Max(maxHR: Int, restingHR: Int): Float {
        if (restingHR <= 0) return 0f
        return 15.3f * (maxHR.toFloat() / restingHR.toFloat())
    }

    /**
     * Classify VO2 Max based on age and gender
     * Returns classification and percentile
     */
    fun classifyVO2Max(vo2Max: Float, age: Int, gender: String?): VO2Classification {
        val isMale = gender?.lowercase() != "female"
        val ranges = if (isMale) getMaleVO2Ranges(age) else getFemaleVO2Ranges(age)

        return when {
            vo2Max >= ranges[6] -> VO2Classification(
                "Superior", Color(0xFF1565C0), "🏆",
                "Exceptional cardiovascular fitness. Elite athlete level.",
                "≥${ranges[6].toInt()}"
            )
            vo2Max >= ranges[5] -> VO2Classification(
                "Excellent", Color(0xFF2196F3), "🌟",
                "Outstanding fitness. Well above average for your age.",
                "${ranges[5].toInt()}-${ranges[6].toInt() - 1}"
            )
            vo2Max >= ranges[4] -> VO2Classification(
                "Good", Color(0xFF4CAF50), "💪",
                "Above average fitness. Keep up the good work!",
                "${ranges[4].toInt()}-${ranges[5].toInt() - 1}"
            )
            vo2Max >= ranges[3] -> VO2Classification(
                "Above Average", Color(0xFF8BC34A), "👍",
                "Slightly above the norm. Room to grow with consistent training.",
                "${ranges[3].toInt()}-${ranges[4].toInt() - 1}"
            )
            vo2Max >= ranges[2] -> VO2Classification(
                "Average", Color(0xFFFFC107), "📊",
                "Typical fitness for your age and gender. Regular exercise can improve this.",
                "${ranges[2].toInt()}-${ranges[3].toInt() - 1}"
            )
            vo2Max >= ranges[1] -> VO2Classification(
                "Below Average", Color(0xFFFF9800), "📈",
                "Below the norm. Starting a regular exercise program will help significantly.",
                "${ranges[1].toInt()}-${ranges[2].toInt() - 1}"
            )
            else -> VO2Classification(
                "Poor", Color(0xFFF44336), "⚠️",
                "Needs improvement. Even light daily activity can make a big difference. Consult your doctor before starting intense exercise.",
                "<${ranges[1].toInt()}"
            )
        }
    }

    /**
     * Estimate "Fitness Age" based on VO2 Max
     * Compares VO2 Max to average values at different ages
     */
    fun estimateFitnessAge(vo2Max: Float, gender: String?): Int {
        val isMale = gender?.lowercase() != "female"
        val averages = if (isMale) maleAverageVO2ByAge else femaleAverageVO2ByAge

        // Find the age whose average VO2 Max is closest to user's
        var closestAge = 20
        var closestDiff = Float.MAX_VALUE

        averages.forEach { (age, avgVO2) ->
            val diff = kotlin.math.abs(vo2Max - avgVO2)
            if (diff < closestDiff) {
                closestDiff = diff
                closestAge = age
            }
        }

        return closestAge.coerceIn(15, 85)
    }

    /**
     * Calculate estimated percentile
     */
    fun estimatePercentile(vo2Max: Float, age: Int, gender: String?): Int {
        val isMale = gender?.lowercase() != "female"
        val ranges = if (isMale) getMaleVO2Ranges(age) else getFemaleVO2Ranges(age)

        return when {
            vo2Max >= ranges[6] -> 97
            vo2Max >= ranges[5] -> 90
            vo2Max >= ranges[4] -> 75
            vo2Max >= ranges[3] -> 60
            vo2Max >= ranges[2] -> 45
            vo2Max >= ranges[1] -> 25
            else -> 10
        }
    }

    /**
     * Full VO2 Max analysis
     */
    fun analyze(
        maxHR: Int,
        restingHR: Int,
        age: Int,
        gender: String?
    ): VO2MaxResult {
        val vo2Max = estimateVO2Max(maxHR, restingHR)
        val classification = classifyVO2Max(vo2Max, age, gender)
        val fitnessAge = estimateFitnessAge(vo2Max, gender)
        val percentile = estimatePercentile(vo2Max, age, gender)

        // Projected improvement: 15-20% with 6 months training
        val improvementPercent = when (classification.category) {
            "Poor" -> 0.20f
            "Below Average" -> 0.18f
            "Average" -> 0.15f
            "Above Average" -> 0.12f
            "Good" -> 0.10f
            "Excellent" -> 0.07f
            "Superior" -> 0.05f
            else -> 0.15f
        }
        val projected = vo2Max * (1 + improvementPercent)

        val fitnessAgeMessage = when {
            fitnessAge < age - 5 -> "🎉 Amazing! Your fitness age is ${age - fitnessAge} years younger than your actual age!"
            fitnessAge < age - 1 -> "👍 Great! You're fitter than average for your age."
            fitnessAge in (age - 1)..(age + 1) -> "📊 Your fitness matches your age. Regular training can make you younger!"
            fitnessAge > age + 5 -> "📈 Your fitness age is ${fitnessAge - age} years above your actual age. " +
                    "Regular cardio exercise can significantly improve this."
            else -> "📈 Room for improvement. Consistent exercise will lower your fitness age."
        }

        return VO2MaxResult(
            vo2Max = vo2Max,
            classification = classification,
            fitnessAge = fitnessAge,
            actualAge = age,
            fitnessAgeMessage = fitnessAgeMessage,
            percentile = percentile,
            improvementPotential = improvementPercent * 100,
            projectedVO2After6Months = projected
        )
    }

    /**
     * Recovery heart rate guidelines
     */
    fun getRecoveryHRGuidelines(): List<RecoveryHRGuideline> = listOf(
        RecoveryHRGuideline(
            "Excellent", ">40 BPM drop", Color(0xFF1565C0), "🏆",
            "Elite-level recovery. Your heart is very efficient at returning to baseline."
        ),
        RecoveryHRGuideline(
            "Good", "30-39 BPM drop", Color(0xFF4CAF50), "💪",
            "Above-average recovery. Indicates good cardiovascular fitness."
        ),
        RecoveryHRGuideline(
            "Average", "20-29 BPM drop", Color(0xFFFFC107), "📊",
            "Normal recovery rate. Regular Zone 2-3 training will improve this."
        ),
        RecoveryHRGuideline(
            "Below Average", "12-19 BPM drop", Color(0xFFFF9800), "📈",
            "Slower than ideal recovery. Focus on consistent aerobic exercise."
        ),
        RecoveryHRGuideline(
            "Poor", "<12 BPM drop", Color(0xFFF44336), "⚠️",
            "Slow recovery may indicate low fitness or medical concerns. Consult your doctor if concerned."
        )
    )

    // ============================================================
    // VO2 MAX REFERENCE TABLES
    // ============================================================

    // Male VO2 Max ranges by age: [Poor, BelowAvg, Avg, AboveAvg, Good, Excellent, Superior]
    private fun getMaleVO2Ranges(age: Int): List<Float> = when {
        age < 20 -> listOf(0f, 35f, 39f, 44f, 48f, 52f, 56f)
        age < 30 -> listOf(0f, 33f, 37f, 42f, 46f, 50f, 55f)
        age < 40 -> listOf(0f, 31f, 36f, 40f, 44f, 48f, 52f)
        age < 50 -> listOf(0f, 29f, 33f, 37f, 41f, 45f, 49f)
        age < 60 -> listOf(0f, 26f, 30f, 34f, 38f, 42f, 46f)
        age < 70 -> listOf(0f, 22f, 26f, 31f, 35f, 39f, 43f)
        else -> listOf(0f, 19f, 23f, 28f, 32f, 36f, 40f)
    }

    // Female VO2 Max ranges by age
    private fun getFemaleVO2Ranges(age: Int): List<Float> = when {
        age < 20 -> listOf(0f, 28f, 32f, 37f, 41f, 45f, 50f)
        age < 30 -> listOf(0f, 26f, 31f, 35f, 39f, 43f, 48f)
        age < 40 -> listOf(0f, 24f, 29f, 33f, 37f, 41f, 45f)
        age < 50 -> listOf(0f, 22f, 27f, 31f, 35f, 39f, 43f)
        age < 60 -> listOf(0f, 20f, 24f, 28f, 32f, 36f, 40f)
        age < 70 -> listOf(0f, 17f, 21f, 25f, 29f, 33f, 37f)
        else -> listOf(0f, 15f, 19f, 23f, 27f, 31f, 35f)
    }

    // Average VO2 Max by age (for fitness age calculation)
    private val maleAverageVO2ByAge = mapOf(
        15 to 50f, 20 to 47f, 25 to 45f, 30 to 43f,
        35 to 41f, 40 to 39f, 45 to 37f, 50 to 35f,
        55 to 33f, 60 to 31f, 65 to 29f, 70 to 27f,
        75 to 25f, 80 to 23f, 85 to 21f
    )

    private val femaleAverageVO2ByAge = mapOf(
        15 to 43f, 20 to 41f, 25 to 39f, 30 to 37f,
        35 to 35f, 40 to 33f, 45 to 31f, 50 to 29f,
        55 to 27f, 60 to 25f, 65 to 23f, 70 to 21f,
        75 to 20f, 80 to 19f, 85 to 18f
    )
}
