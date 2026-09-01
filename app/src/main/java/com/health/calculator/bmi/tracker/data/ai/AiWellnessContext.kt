package com.health.calculator.bmi.tracker.data.ai

import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import com.health.calculator.bmi.tracker.data.model.WeightEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/**
 * Minimal, user-consented context for personalization. Notes, identifiers, raw water amounts
 * and calculator payloads are intentionally excluded.
 */
data class AiWellnessContext(
    val weightEntriesLast7Days: Int = 0,
    val latestWeightKg: Double? = null,
    val weightChangeKgLast7Days: Double? = null,
    val waterLoggedDaysLast7: Int = 0,
    val waterGoalDaysLast7: Int? = null
) {
    val hasData: Boolean
        get() = weightEntriesLast7Days > 0 || latestWeightKg != null || waterLoggedDaysLast7 > 0

    fun toPromptSection(): String {
        if (!hasData) return "No recent app tracking context is available."

        return buildString {
            appendLine("This is a small, user-consented summary of recent app logs. It is not a diagnosis or a complete health record.")
            latestWeightKg?.let { appendLine("Latest logged weight: ${format(it)} kg.") }
            if (weightEntriesLast7Days > 0) {
                appendLine("Weight entries in the last 7 days: $weightEntriesLast7Days.")
            }
            weightChangeKgLast7Days?.let {
                appendLine("Change between the earliest and latest logged weight in that window: ${formatSigned(it)} kg.")
            }
            appendLine("Days with a water log in the last 7 days: $waterLoggedDaysLast7.")
            waterGoalDaysLast7?.let { appendLine("Days the saved water goal was met: $it of 7.") }
            append("Use these values only to personalize gentle, general suggestions; do not infer causes or medical risk.")
        }
    }

    private fun format(value: Double): String = String.format(Locale.US, "%.1f", value)
    private fun formatSigned(value: Double): String = String.format(Locale.US, "%+.1f", value)
}

object AiWellnessContextBuilder {
    fun build(
        today: LocalDate,
        zone: ZoneId,
        weights: List<WeightEntry>,
        waterLogs: List<WaterIntakeLog>,
        waterGoalMl: Int?
    ): AiWellnessContext {
        val start = today.minusDays(6)
        val recentWeights = weights
            .filter { dateFor(it.dateMillis, zone) in start..today && it.weightKg.isFinite() }
            .sortedBy { it.dateMillis }
        val recentWaterDays = waterLogs
            .groupBy { dateFor(it.timestamp, zone) }
            .mapValues { (_, logs) -> logs.sumOf { it.amountMl } }
            .filterKeys { it in start..today }

        return AiWellnessContext(
            weightEntriesLast7Days = recentWeights.size,
            latestWeightKg = recentWeights.lastOrNull()?.weightKg,
            weightChangeKgLast7Days = recentWeights.takeIf { it.size >= 2 }?.let {
                it.last().weightKg - it.first().weightKg
            },
            waterLoggedDaysLast7 = recentWaterDays.count { it.value > 0 },
            waterGoalDaysLast7 = waterGoalMl?.takeIf { it > 0 }?.let { goal ->
                recentWaterDays.count { it.value >= goal }
            }
        )
    }

    private fun dateFor(timestampMillis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(timestampMillis).atZone(zone).toLocalDate()
}
