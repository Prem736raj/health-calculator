package com.health.calculator.bmi.tracker.ai

import com.health.calculator.bmi.tracker.data.ai.AiWellnessContextBuilder
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import com.health.calculator.bmi.tracker.data.model.WeightEntry
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiWellnessContextTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 9, 2)

    private fun timestamp(day: LocalDate): Long = day.atStartOfDay(zone).toInstant().toEpochMilli()

    @Test
    fun contextContainsOnlyMinimalRecentSummaries() {
        val context = AiWellnessContextBuilder.build(
            today = today,
            zone = zone,
            weights = listOf(
                WeightEntry(weightKg = 80.0, dateMillis = timestamp(today.minusDays(5)), note = "private note"),
                WeightEntry(weightKg = 79.0, dateMillis = timestamp(today))
            ),
            waterLogs = listOf(
                WaterIntakeLog(amountMl = 2_000, timestamp = timestamp(today)),
                WaterIntakeLog(amountMl = 500, timestamp = timestamp(today.minusDays(1)))
            ),
            waterGoalMl = 2_000
        )

        val promptSection = context.toPromptSection()
        assertTrue(promptSection.contains("Latest logged weight: 79.0 kg"))
        assertTrue(promptSection.contains("Days with a water log in the last 7 days: 2"))
        assertTrue(promptSection.contains("Days the saved water goal was met: 1 of 7"))
        assertFalse(promptSection.contains("private note"))
        assertFalse(promptSection.contains("2000"))
    }

    @Test
    fun oldEntriesAreExcluded() {
        val context = AiWellnessContextBuilder.build(
            today = today,
            zone = zone,
            weights = listOf(WeightEntry(weightKg = 80.0, dateMillis = timestamp(today.minusDays(8)))),
            waterLogs = listOf(WaterIntakeLog(amountMl = 2_000, timestamp = timestamp(today.minusDays(8)))),
            waterGoalMl = 2_000
        )

        assertFalse(context.hasData)
    }
}
