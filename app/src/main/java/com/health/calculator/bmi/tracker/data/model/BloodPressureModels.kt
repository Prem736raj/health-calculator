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
        displayName = "Hypotension",
        description = "Blood pressure is below normal range",
        systolicRange = "< 90",
        diastolicRange = "< 60",
        sortOrder = 0
    ),
    OPTIMAL(
        displayName = "Optimal",
        description = "Ideal blood pressure – keep it up!",
        systolicRange = "< 120",
        diastolicRange = "< 80",
        sortOrder = 1
    ),
    NORMAL(
        displayName = "Normal",
        description = "Blood pressure is within normal range",
        systolicRange = "120–129",
        diastolicRange = "80–84",
        sortOrder = 2
    ),
    HIGH_NORMAL(
        displayName = "High Normal",
        description = "Slightly elevated – monitor regularly",
        systolicRange = "130–139",
        diastolicRange = "85–89",
        sortOrder = 3
    ),
    ISOLATED_SYSTOLIC(
        displayName = "Isolated Systolic HTN",
        description = "Systolic elevated but diastolic normal",
        systolicRange = "≥ 140",
        diastolicRange = "< 90",
        sortOrder = 4
    ),
    GRADE_1_HYPERTENSION(
        displayName = "Grade 1 Hypertension",
        description = "Mild high blood pressure",
        systolicRange = "140–159",
        diastolicRange = "90–99",
        sortOrder = 5
    ),
    GRADE_2_HYPERTENSION(
        displayName = "Grade 2 Hypertension",
        description = "Moderate high blood pressure",
        systolicRange = "160–179",
        diastolicRange = "100–109",
        sortOrder = 6
    ),
    GRADE_3_HYPERTENSION(
        displayName = "Grade 3 Hypertension",
        description = "Severe high blood pressure",
        systolicRange = "≥ 180",
        diastolicRange = "≥ 110",
        sortOrder = 7
    ),
    HYPERTENSIVE_CRISIS(
        displayName = "Hypertensive Crisis",
        description = "Seek immediate medical attention!",
        systolicRange = "≥ 180",
        diastolicRange = "≥ 120",
        sortOrder = 8
    )
}

enum class BpRiskLevel(val displayName: String, val description: String) {
    LOW("Low Risk", "Your blood pressure is in a healthy range."),
    MODERATE("Moderate Risk", "Consider lifestyle changes and regular monitoring."),
    HIGH("High Risk", "Consult a healthcare provider. Lifestyle changes recommended."),
    VERY_HIGH("Very High Risk", "Medical attention strongly recommended."),
    EMERGENCY("EMERGENCY", "Seek immediate medical attention. Call emergency services if experiencing symptoms.")
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
        // Emergency check first
        if (systolic >= 180 && diastolic >= 120) {
            return BpCategory.HYPERTENSIVE_CRISIS
        }

        // Determine systolic category
        val systolicCategory = categorizeSystolic(systolic)
        // Determine diastolic category
        val diastolicCategory = categorizeDiastolic(diastolic)

        // Isolated Systolic Hypertension: systolic ≥ 140 AND diastolic < 90
        if (systolic >= 140 && diastolic < 90) {
            // But if diastolic is also very low, still consider ISH
            // Unless it's a crisis
            val sysGrade = systolicCategory
            if (sysGrade == BpCategory.GRADE_3_HYPERTENSION ||
                sysGrade == BpCategory.HYPERTENSIVE_CRISIS
            ) {
                return sysGrade
            }
            return BpCategory.ISOLATED_SYSTOLIC
        }

        // Hypotension check
        if (systolic < 90 || diastolic < 60) {
            return BpCategory.HYPOTENSION
        }

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
            systolic in 160..179 -> BpCategory.GRADE_2_HYPERTENSION
            systolic in 140..159 -> BpCategory.GRADE_1_HYPERTENSION
            systolic in 130..139 -> BpCategory.HIGH_NORMAL
            systolic in 120..129 -> BpCategory.NORMAL
            systolic >= 90 -> BpCategory.OPTIMAL
            else -> BpCategory.HYPOTENSION
        }
    }

    private fun categorizeDiastolic(diastolic: Int): BpCategory {
        return when {
            diastolic >= 110 -> BpCategory.GRADE_3_HYPERTENSION
            diastolic in 100..109 -> BpCategory.GRADE_2_HYPERTENSION
            diastolic in 90..99 -> BpCategory.GRADE_1_HYPERTENSION
            diastolic in 85..89 -> BpCategory.HIGH_NORMAL
            diastolic in 80..84 -> BpCategory.NORMAL
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
            sys == dia -> "Systolic and diastolic cannot be equal"
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
