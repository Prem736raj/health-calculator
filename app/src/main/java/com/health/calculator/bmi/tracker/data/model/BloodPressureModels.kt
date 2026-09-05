package com.health.calculator.bmi.tracker.data.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class BpArm(val displayName: String) {
    LEFT("Left Arm"),
    RIGHT("Right Arm")
}

enum class BpPosition(val displayName: String) {
    SITTING("Sitting"),
    STANDING("Standing"),
    LYING_DOWN("Lying Down")
}

enum class BpTimeOfDay(val displayName: String) {
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    NIGHT("Night")
}

enum class BpCategory(
    val displayName: String,
    val description: String,
    val systolicRange: String,
    val diastolicRange: String,
    val sortOrder: Int
) {
    HYPOTENSION(
        displayName = "Below 90/60",
        description = "A low reading; symptoms and repeated readings matter",
        systolicRange = "< 90",
        diastolicRange = "< 60",
        sortOrder = 0
    ),
    OPTIMAL(
        displayName = "Normal",
        description = "Below 120/80 mmHg",
        systolicRange = "< 120",
        diastolicRange = "< 80",
        sortOrder = 1
    ),
    NORMAL(
        displayName = "Elevated",
        description = "120–129 systolic and below 80 diastolic",
        systolicRange = "120–129",
        diastolicRange = "< 80",
        sortOrder = 2
    ),
    HIGH_NORMAL(
        displayName = "Stage 1 range",
        description = "130–139 systolic or 80–89 diastolic",
        systolicRange = "130–139",
        diastolicRange = "80–89",
        sortOrder = 3
    ),
    ISOLATED_SYSTOLIC(
        displayName = "Stage 2 range (systolic)",
        description = "Systolic ≥ 140 with diastolic below 90",
        systolicRange = "≥ 140",
        diastolicRange = "< 90",
        sortOrder = 4
    ),
    GRADE_1_HYPERTENSION(
        displayName = "Stage 2 range",
        description = "140 or higher systolic or 90 or higher diastolic",
        systolicRange = "≥ 140",
        diastolicRange = "≥ 90",
        sortOrder = 5
    ),
    GRADE_2_HYPERTENSION(
        displayName = "Stage 2 range",
        description = "Stage 2 reference range retained for older saved results",
        systolicRange = "≥ 140",
        diastolicRange = "≥ 90",
        sortOrder = 6
    ),
    GRADE_3_HYPERTENSION(
        displayName = "Severely elevated",
        description = "180+ systolic or 120+ diastolic",
        systolicRange = "≥ 180",
        diastolicRange = "≥ 110",
        sortOrder = 7
    ),
    HYPERTENSIVE_CRISIS(
        displayName = "Severely elevated reading",
        description = "Repeat carefully and seek urgent advice, especially with symptoms",
        systolicRange = "≥ 180",
        diastolicRange = "≥ 120",
        sortOrder = 8
    )
}

enum class BpRiskLevel(val displayName: String, val description: String) {
    LOW("Within reference", "One reading is informational; keep a consistent measurement routine."),
    MODERATE("Above or below reference", "Repeat readings on separate occasions and review the trend."),
    HIGH("Higher reading", "Discuss repeated readings with a healthcare professional."),
    VERY_HIGH("Markedly elevated", "Repeat after resting and seek prompt professional advice."),
    EMERGENCY("Severely elevated", "If a repeat reading is still ≥180/120 or you have concerning symptoms, seek urgent medical care.")
}
data class BloodPressureReading(
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int? = null,
    val arm: BpArm? = null,
    val position: BpPosition? = null,
    val timeOfDay: BpTimeOfDay? = null,
    val measurementTime: LocalDateTime = LocalDateTime.now(),
    val category: BpCategory = BpCategory.OPTIMAL,
    val riskLevel: BpRiskLevel = BpRiskLevel.LOW,
    val notes: String = ""
) {
    val formattedTime: String
        get() = measurementTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

    val formattedDate: String
        get() = measurementTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

    val formattedDateTime: String
        get() = measurementTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a"))

    val readingString: String
        get() = "$systolic/$diastolic mmHg"

    val meanArterialPressure: Double
        get() = diastolic + (systolic - diastolic) / 3.0

    val pulsePressure: Int
        get() = systolic - diastolic
}

object BloodPressureCalculator {

    fun categorize(systolic: Int, diastolic: Int): BpCategory {
        // A severe value in either number needs a careful repeat and prompt
        // advice; do not wait for both numbers to cross the threshold.
        if (systolic >= 180 || diastolic >= 120) {
            return BpCategory.HYPERTENSIVE_CRISIS
        }

        // Determine systolic category
        val systolicCategory = categorizeSystolic(systolic)
        // Determine diastolic category
        val diastolicCategory = categorizeDiastolic(diastolic)

        // Hypotension check
        if (systolic < 90 || diastolic < 60) {
            return BpCategory.HYPOTENSION
        }

        // Isolated systolic elevation remains useful as a descriptive label.
        if (systolic >= 140 && diastolic < 90) return BpCategory.ISOLATED_SYSTOLIC

        // Use the HIGHER (worse) category when they differ
        return if (systolicCategory.sortOrder >= diastolicCategory.sortOrder) {
            systolicCategory
        } else {
            diastolicCategory
        }
    }

    private fun categorizeSystolic(systolic: Int): BpCategory {
        return when {
            systolic >= 180 -> BpCategory.GRADE_3_HYPERTENSION
            systolic >= 140 -> BpCategory.GRADE_1_HYPERTENSION
            systolic in 130..139 -> BpCategory.HIGH_NORMAL
            systolic in 120..129 -> BpCategory.NORMAL
            systolic >= 90 -> BpCategory.OPTIMAL
            else -> BpCategory.HYPOTENSION
        }
    }

    private fun categorizeDiastolic(diastolic: Int): BpCategory {
        return when {
            diastolic >= 120 -> BpCategory.GRADE_3_HYPERTENSION
            diastolic >= 90 -> BpCategory.GRADE_1_HYPERTENSION
            diastolic in 80..89 -> BpCategory.HIGH_NORMAL
            diastolic >= 60 -> BpCategory.OPTIMAL
            else -> BpCategory.HYPOTENSION
        }
    }

    fun getRiskLevel(category: BpCategory): BpRiskLevel {
        return when (category) {
            BpCategory.HYPOTENSION -> BpRiskLevel.MODERATE
            BpCategory.OPTIMAL -> BpRiskLevel.LOW
            BpCategory.NORMAL -> BpRiskLevel.LOW
            BpCategory.HIGH_NORMAL -> BpRiskLevel.MODERATE
            BpCategory.ISOLATED_SYSTOLIC -> BpRiskLevel.MODERATE
            BpCategory.GRADE_1_HYPERTENSION -> BpRiskLevel.HIGH
            BpCategory.GRADE_2_HYPERTENSION -> BpRiskLevel.VERY_HIGH
            BpCategory.GRADE_3_HYPERTENSION -> BpRiskLevel.VERY_HIGH
            BpCategory.HYPERTENSIVE_CRISIS -> BpRiskLevel.EMERGENCY
        }
    }

    fun isEmergencyReading(systolic: Int, diastolic: Int): Boolean {
        return systolic >= 180 || diastolic >= 120
    }

    /**
     * Returns a fractional position 0.0–1.0 on the gauge for a given category.
     */
    fun getGaugePosition(systolic: Int, diastolic: Int): Float {
        val category = categorize(systolic, diastolic)
        // Map systolic primarily to gauge position
        return when {
            systolic < 90 -> 0.02f + (systolic / 90f) * 0.1f
            systolic < 120 -> 0.12f + ((systolic - 90f) / 30f) * 0.15f
            systolic < 130 -> 0.27f + ((systolic - 120f) / 10f) * 0.1f
            systolic < 140 -> 0.37f + ((systolic - 130f) / 10f) * 0.1f
            systolic < 160 -> 0.47f + ((systolic - 140f) / 20f) * 0.15f
            systolic < 180 -> 0.62f + ((systolic - 160f) / 20f) * 0.18f
            else -> 0.8f + ((systolic - 180f).coerceAtMost(40f) / 40f) * 0.18f
        }.coerceIn(0.02f, 0.98f)
    }

    fun validateSystolic(value: String): String? {
        if (value.isBlank()) return "Systolic pressure is required"
        val num = value.toIntOrNull()
        return when {
            num == null -> "Enter a valid number"
            num <= 0 -> "Value must be positive"
            num < 60 -> "Systolic must be at least 60 mmHg"
            num > 300 -> "Systolic must be below 300 mmHg"
            else -> null
        }
    }

    fun validateDiastolic(value: String): String? {
        if (value.isBlank()) return "Diastolic pressure is required"
        val num = value.toIntOrNull()
        return when {
            num == null -> "Enter a valid number"
            num <= 0 -> "Value must be positive"
            num < 30 -> "Diastolic must be at least 30 mmHg"
            num > 200 -> "Diastolic must be below 200 mmHg"
            else -> null
        }
    }

    fun validateSystolicOverDiastolic(systolic: String, diastolic: String): String? {
        val sys = systolic.toIntOrNull() ?: return null
        val dia = diastolic.toIntOrNull() ?: return null
        return when {
            sys <= dia -> "Systolic must be higher than diastolic"
            sys - dia < 10 -> "The difference between systolic and diastolic seems too small. Please verify."
            else -> null
        }
    }

    fun validatePulse(value: String): String? {
        if (value.isBlank()) return null
        val num = value.toIntOrNull()
        return when {
            num == null -> "Enter a valid number"
            num <= 0 -> "Pulse must be positive"
            num < 30 -> "Pulse must be at least 30 BPM"
            num > 250 -> "Pulse must be below 250 BPM"
            else -> null
        }
    }

    fun getCurrentTimeOfDay(): BpTimeOfDay {
        val hour = LocalDateTime.now().hour
        return when {
            hour in 5..11 -> BpTimeOfDay.MORNING
            hour in 12..16 -> BpTimeOfDay.AFTERNOON
            hour in 17..20 -> BpTimeOfDay.EVENING
            else -> BpTimeOfDay.NIGHT
        }
    }
}
