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
    val projectedVO2After6Months: Float,
    val isClinicalMeasurement: Boolean = false,
    val methodology: String = "Heart-rate ratio estimate; not a laboratory VO₂ max test or diagnosis."
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
        if (maxHR <= 0 || restingHR <= 0 || restingHR >= maxHR) return 0f
        return (15.3f * (maxHR.toFloat() / restingHR.toFloat())).takeIf { it.isFinite() && it > 0f } ?: 0f
    }

    /**
     * Classify VO2 Max based on age and gender
     * Returns classification and percentile
     */
    fun classifyVO2Max(vo2Max: Float, age: Int, gender: String?): VO2Classification {
        require(vo2Max.isFinite() && vo2Max > 0f) { "VO₂ max estimate must be positive" }
        require(age in 18..120) { "VO₂ max reference bands support adult ages 18–120" }
        val isMale = gender?.lowercase() != "female"
        val ranges = if (isMale) getMaleVO2Ranges(age) else getFemaleVO2Ranges(age)

        return when {
            vo2Max >= ranges[6] -> VO2Classification(
                "Superior", Color(0xFF1565C0), "🏆",
                "An estimate in the highest reference band for this age group; not a clinical assessment.",
                "≥${ranges[6].toInt()}"
            )
            vo2Max >= ranges[5] -> VO2Classification(
                "Excellent", Color(0xFF2196F3), "🌟",
                "An estimate above the usual reference bands for this age group.",
                "${ranges[5].toInt()}-${ranges[6].toInt() - 1}"
            )
            vo2Max >= ranges[4] -> VO2Classification(
                "Good", Color(0xFF4CAF50), "💪",
                "An estimate in an above-reference band; repeat under similar conditions to track change.",
                "${ranges[4].toInt()}-${ranges[5].toInt() - 1}"
            )
            vo2Max >= ranges[3] -> VO2Classification(
                "Above Average", Color(0xFF8BC34A), "👍",
                "An estimate slightly above the reference midpoint.",
                "${ranges[3].toInt()}-${ranges[4].toInt() - 1}"
            )
            vo2Max >= ranges[2] -> VO2Classification(
                "Average", Color(0xFFFFC107), "📊",
                "An estimate around the reference midpoint for this age group.",
                "${ranges[2].toInt()}-${ranges[3].toInt() - 1}"
            )
            vo2Max >= ranges[1] -> VO2Classification(
                "Below Average", Color(0xFFFF9800), "📈",
                "An estimate below the reference midpoint; use gentle, progressive activity if appropriate.",
                "${ranges[1].toInt()}-${ranges[2].toInt() - 1}"
            )
            else -> VO2Classification(
                "Poor", Color(0xFFF44336), "⚠️",
                "An estimate below the reference bands. Consider your current fitness and seek professional advice before changing intensity.",
                "<${ranges[1].toInt()}"
            )
        }
    }

    /**
     * Estimate "Fitness Age" based on VO2 Max
     * Compares VO2 Max to average values at different ages
     */
    fun estimateFitnessAge(vo2Max: Float, gender: String?): Int {
        if (!vo2Max.isFinite() || vo2Max <= 0f) return 0
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
        if (!vo2Max.isFinite() || vo2Max <= 0f || age !in 18..120) return 0
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
        require(age in 18..120) { "VO₂ max reference bands support adult ages 18–120" }
        require(maxHR in 80..240) { "Maximum heart rate must be between 80 and 240 BPM" }
        require(restingHR in 30..200 && restingHR < maxHR) {
            "Resting heart rate must be between 30 and 200 BPM and below maximum heart rate"
        }
        val vo2Max = estimateVO2Max(maxHR, restingHR)
        val classification = classifyVO2Max(vo2Max, age, gender)
        val fitnessAge = estimateFitnessAge(vo2Max, gender)
        val percentile = estimatePercentile(vo2Max, age, gender)

        // A six-month projection is not supportable from a single heart-rate
        // ratio. Keep the legacy field equal to the estimate and explain the
        // uncertainty instead of promising an improvement percentage.
        val fitnessAgeMessage = if (fitnessAge > 0) {
            "Reference-age comparison only (${fitnessAge} years); it is not an age prediction or health diagnosis."
        } else {
            "Reference-age comparison is unavailable for this estimate."
        }

        return VO2MaxResult(
            vo2Max = vo2Max,
            classification = classification,
            fitnessAge = fitnessAge,
            actualAge = age,
            fitnessAgeMessage = fitnessAgeMessage,
            percentile = percentile,
            improvementPotential = 0f,
            projectedVO2After6Months = vo2Max
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
