package com.health.calculator.bmi.tracker.util

import com.health.calculator.bmi.tracker.ui.components.HeartRateFormula
import com.health.calculator.bmi.tracker.ui.components.FitnessLevel
import androidx.compose.ui.graphics.Color

data class HeartRateZone(
    val zoneNumber: Int,
    val zoneName: String,
    val subtitle: String,
    val bpmLow: Int,
    val bpmHigh: Int,
    val percentLow: Int,
    val percentHigh: Int,
    val color: Color,
    val purpose: String,
    val effortDescription: String,
    val talkTest: String,
    val icon: String,
    val recommendedDuration: String
)

data class HeartRateZoneResult(
    val maxHeartRate: Int,
    val restingHeartRate: Int?,
    val heartRateReserve: Int?,
    val formulaUsed: HeartRateFormula,
    val zones: List<HeartRateZone>,
    val age: Int,
    val fitnessLevel: FitnessLevel,
    val gender: String?,
    val timestamp: Long = System.currentTimeMillis()
)

object HeartRateZoneCalculator {

    /**
     * Calculate Max Heart Rate using selected formula
     */
    fun calculateMaxHR(
        age: Int,
        formula: HeartRateFormula,
        gender: String? = null,
        customMaxHR: Int? = null
    ): Int {
        return when (formula) {
            HeartRateFormula.STANDARD -> 220 - age
            HeartRateFormula.TANAKA -> (208 - (0.7 * age)).toInt()
            HeartRateFormula.GULATI -> (206 - (0.88 * age)).toInt()
            HeartRateFormula.KARVONEN -> 220 - age // MHR same, zones differ
            HeartRateFormula.CUSTOM -> customMaxHR ?: (220 - age)
        }
    }

    /**
     * Calculate all Max HR values from every formula for comparison
     */
    fun calculateAllFormulaMHR(
        age: Int,
        gender: String? = null,
        customMaxHR: Int? = null
    ): Map<HeartRateFormula, Int> {
        return mapOf(
            HeartRateFormula.STANDARD to (220 - age),
            HeartRateFormula.TANAKA to (208 - (0.7 * age)).toInt(),
            HeartRateFormula.GULATI to (206 - (0.88 * age)).toInt(),
            HeartRateFormula.KARVONEN to (220 - age),
        ).let { map ->
            if (customMaxHR != null) {
                map + (HeartRateFormula.CUSTOM to customMaxHR)
            } else map
        }
    }

    /**
     * Calculate Heart Rate Zones
     */
    fun calculateZones(
        age: Int,
        formula: HeartRateFormula,
        restingHR: Int? = null,
        gender: String? = null,
        fitnessLevel: FitnessLevel = FitnessLevel.INTERMEDIATE,
        customMaxHR: Int? = null
    ): HeartRateZoneResult {
        val mhr = calculateMaxHR(age, formula, gender, customMaxHR)
        val useKarvonen = formula == HeartRateFormula.KARVONEN && restingHR != null
        val hrr = if (useKarvonen && restingHR != null) mhr - restingHR else null

        val zones = buildZones(
            mhr = mhr,
            restingHR = restingHR,
            hrr = hrr,
            useKarvonen = useKarvonen,
            fitnessLevel = fitnessLevel
        )

        return HeartRateZoneResult(
            maxHeartRate = mhr,
            restingHeartRate = restingHR,
            heartRateReserve = hrr,
            formulaUsed = formula,
            zones = zones,
            age = age,
            fitnessLevel = fitnessLevel,
            gender = gender
        )
    }

    private fun buildZones(
        mhr: Int,
        restingHR: Int?,
        hrr: Int?,
        useKarvonen: Boolean,
        fitnessLevel: FitnessLevel
    ): List<HeartRateZone> {
        // Zone percentage ranges
        data class ZoneRange(val low: Int, val high: Int)

        val zoneRanges = listOf(
            ZoneRange(50, 60),
            ZoneRange(60, 70),
            ZoneRange(70, 80),
            ZoneRange(80, 90),
            ZoneRange(90, 100)
        )

        fun calcBPM(percent: Int): Int {
            return if (useKarvonen && hrr != null && restingHR != null) {
                // Karvonen: Target HR = ((MHR − Resting HR) × %Intensity) + Resting HR
                ((hrr * percent / 100.0) + restingHR).toInt()
            } else {
                // Standard: Target HR = MHR × %Intensity
                (mhr * percent / 100.0).toInt()
            }
        }

        return listOf(
            HeartRateZone(
                zoneNumber = 1,
                zoneName = "Recovery",
                subtitle = "Very Light",
                bpmLow = calcBPM(zoneRanges[0].low),
                bpmHigh = calcBPM(zoneRanges[0].high),
                percentLow = zoneRanges[0].low,
                percentHigh = zoneRanges[0].high,
                color = Color(0xFF90CAF9), // Light Blue
                purpose = "Warm-up, cool-down, active recovery. Improves overall health and helps recovery from harder workouts.",
                effortDescription = "Very easy effort. Feels comfortable and sustainable for a very long time.",
                talkTest = "Can hold a full conversation easily",
                icon = "🚶",
                recommendedDuration = when (fitnessLevel) {
                    FitnessLevel.BEGINNER -> "20-40 min"
                    FitnessLevel.INTERMEDIATE -> "15-30 min"
                    FitnessLevel.ADVANCED -> "10-20 min"
                }
            ),
            HeartRateZone(
                zoneNumber = 2,
                zoneName = "Fat Burn",
                subtitle = "Light",
                bpmLow = calcBPM(zoneRanges[1].low),
                bpmHigh = calcBPM(zoneRanges[1].high),
                percentLow = zoneRanges[1].low,
                percentHigh = zoneRanges[1].high,
                color = Color(0xFF42A5F5), // Blue
                purpose = "Primary fat burning zone. Builds endurance base and improves body's ability to use fat as fuel.",
                effortDescription = "Easy, comfortable pace. You should feel like you can keep going for a long time.",
                talkTest = "Can talk easily in full sentences",
                icon = "🔥",
                recommendedDuration = when (fitnessLevel) {
                    FitnessLevel.BEGINNER -> "30-60 min"
                    FitnessLevel.INTERMEDIATE -> "45-90 min"
                    FitnessLevel.ADVANCED -> "60-120 min"
                }
            ),
            HeartRateZone(
                zoneNumber = 3,
                zoneName = "Aerobic",
                subtitle = "Moderate",
                bpmLow = calcBPM(zoneRanges[2].low),
                bpmHigh = calcBPM(zoneRanges[2].high),
                percentLow = zoneRanges[2].low,
                percentHigh = zoneRanges[2].high,
                color = Color(0xFF66BB6A), // Green
                purpose = "Improves cardiovascular fitness and endurance. Strengthens the heart and increases aerobic capacity.",
                effortDescription = "Moderate effort. You're working but still feel in control.",
                talkTest = "Can speak in short sentences with some effort",
                icon = "💪",
                recommendedDuration = when (fitnessLevel) {
                    FitnessLevel.BEGINNER -> "15-30 min"
                    FitnessLevel.INTERMEDIATE -> "30-60 min"
                    FitnessLevel.ADVANCED -> "45-90 min"
                }
            ),
            HeartRateZone(
                zoneNumber = 4,
                zoneName = "Anaerobic",
                subtitle = "Hard",
                bpmLow = calcBPM(zoneRanges[3].low),
                bpmHigh = calcBPM(zoneRanges[3].high),
                percentLow = zoneRanges[3].low,
                percentHigh = zoneRanges[3].high,
                color = Color(0xFFFFA726), // Orange
                purpose = "Increases speed, power, and anaerobic threshold. Improves performance and calorie burn.",
                effortDescription = "Hard effort. Breathing is heavy and muscles feel fatigued.",
                talkTest = "Can only speak a few words at a time",
                icon = "⚡",
                recommendedDuration = when (fitnessLevel) {
                    FitnessLevel.BEGINNER -> "5-10 min intervals"
                    FitnessLevel.INTERMEDIATE -> "10-20 min intervals"
                    FitnessLevel.ADVANCED -> "20-40 min intervals"
                }
            ),
            HeartRateZone(
                zoneNumber = 5,
                zoneName = "VO₂ Max",
                subtitle = "Maximum",
                bpmLow = calcBPM(zoneRanges[4].low),
                bpmHigh = calcBPM(zoneRanges[4].high),
                percentLow = zoneRanges[4].low,
                percentHigh = zoneRanges[4].high,
                color = Color(0xFFEF5350), // Red
                purpose = "Maximum performance and sprint capacity. Only for short bursts. Pushes absolute limits.",
                effortDescription = "All-out, maximum effort. Unsustainable for more than a few minutes.",
                talkTest = "Cannot speak at all",
                icon = "🚀",
                recommendedDuration = when (fitnessLevel) {
                    FitnessLevel.BEGINNER -> "30 sec - 2 min bursts"
                    FitnessLevel.INTERMEDIATE -> "1-3 min intervals"
                    FitnessLevel.ADVANCED -> "2-5 min intervals"
                }
            )
        )
    }
}
