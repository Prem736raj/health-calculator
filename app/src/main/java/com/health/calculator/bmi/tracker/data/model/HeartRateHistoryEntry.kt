package com.health.calculator.bmi.tracker.data.model

import com.health.calculator.bmi.tracker.ui.components.HeartRateFormula
import com.health.calculator.bmi.tracker.ui.components.FitnessLevel

data class HeartRateHistoryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val maxHeartRate: Int,
    val restingHeartRate: Int? = null,
    val heartRateReserve: Int? = null,
    val formulaUsed: String,
    val age: Int,
    val gender: String? = null,
    val fitnessLevel: String,
    val zone1Range: String,
    val zone2Range: String,
    val zone3Range: String,
    val zone4Range: String,
    val zone5Range: String,
    // NEW fields for tracking
    val vo2MaxEstimate: Float? = null,
    val vo2MaxClassification: String? = null,
    val fitnessAge: Int? = null,
    val calculatorType: String = "Heart Rate Zones",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = java.time.LocalDate.now().toString()
)

fun com.health.calculator.bmi.tracker.util.HeartRateZoneResult.toHistoryEntry(): HistoryEntry {
    // Calculate VO2 Max if resting HR available
    val vo2Max = if (restingHeartRate != null && restingHeartRate > 0) {
        com.health.calculator.bmi.tracker.util.VO2MaxCalculator.estimateVO2Max(maxHeartRate, restingHeartRate)
    } else null

    val vo2Classification = if (vo2Max != null && gender != null) {
        com.health.calculator.bmi.tracker.util.VO2MaxCalculator.classifyVO2Max(vo2Max, age, gender).category
    } else null

    val fitnessAgeEst = if (vo2Max != null && gender != null) {
        com.health.calculator.bmi.tracker.util.VO2MaxCalculator.estimateFitnessAge(vo2Max, gender)
    } else null

    val detailsJson = org.json.JSONObject().apply {
        put("maxHeartRate", maxHeartRate)
        put("restingHeartRate", restingHeartRate)
        put("age", age)
        put("gender", gender)
        put("formula", formulaUsed.label)
        put("vo2Max", vo2Max)
        put("vo2Classification", vo2Classification)
        put("fitnessAge", fitnessAgeEst)
        put("zone1", "${zones[0].bpmLow}-${zones[0].bpmHigh}")
        put("zone2", "${zones[1].bpmLow}-${zones[1].bpmHigh}")
        put("zone3", "${zones[2].bpmLow}-${zones[2].bpmHigh}")
        put("zone4", "${zones[3].bpmLow}-${zones[3].bpmHigh}")
        put("zone5", "${zones[4].bpmLow}-${zones[4].bpmHigh}")
    }

    return HistoryEntry(
        calculatorKey = CalculatorType.HEART_RATE.key,
        resultValue = maxHeartRate.toString(),
        resultLabel = "bpm (Max)",
        category = fitnessLevel.label,
        detailsJson = detailsJson.toString(),
        timestamp = System.currentTimeMillis()
    )
}
