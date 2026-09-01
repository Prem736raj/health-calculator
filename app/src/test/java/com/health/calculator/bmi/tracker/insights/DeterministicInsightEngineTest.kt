package com.health.calculator.bmi.tracker.insights

import com.health.calculator.bmi.tracker.domain.insights.DeterministicInsightEngine
import com.health.calculator.bmi.tracker.domain.insights.DeterministicInsightInput
import com.health.calculator.bmi.tracker.domain.insights.InsightMetric
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicInsightEngineTest {
    private val today = LocalDate.of(2026, 9, 1)
    private val zone = ZoneOffset.UTC

    private fun timestamp(day: LocalDate): Long = day.atStartOfDay(zone).toInstant().toEpochMilli()

    @Test
    fun weightComparisonDescribesObservedAveragesWithoutCausalClaims() {
        val input = DeterministicInsightInput(
            today = today,
            zone = zone,
            weightEntries = listOf(
                InsightMetric(timestamp(today), 90.0),
                InsightMetric(timestamp(today.minusDays(2)), 91.0),
                InsightMetric(timestamp(today.minusDays(8)), 93.0),
                InsightMetric(timestamp(today.minusDays(10)), 94.0)
            )
        )

        val insight = DeterministicInsightEngine.generate(input).first { it.id == "weight_weekly_comparison" }
        assertTrue(insight.message.contains("average logged weight"))
        assertTrue(insight.evidence.contains("% change"))
        assertFalse(insight.message.contains("because", ignoreCase = true))
        assertFalse(insight.message.contains("diagnos", ignoreCase = true))
    }

    @Test
    fun smallWeightChangeIsNotOverstated() {
        val input = DeterministicInsightInput(
            today = today,
            zone = zone,
            weightEntries = listOf(
                InsightMetric(timestamp(today), 90.0),
                InsightMetric(timestamp(today.minusDays(8)), 89.0)
            )
        )

        val insight = DeterministicInsightEngine.generate(input).first { it.id == "weight_weekly_comparison" }
        assertTrue(insight.message.contains("about the same"))
    }

    @Test
    fun hydrationInsightCountsGoalDaysAndTrackedDays() {
        val input = DeterministicInsightInput(
            today = today,
            zone = zone,
            waterGoalMl = 2_000,
            waterByDayMl = mapOf(
                today to 2_100,
                today.minusDays(1) to 1_000,
                today.minusDays(2) to 2_000,
                today.minusDays(8) to 2_000
            )
        )

        val insight = DeterministicInsightEngine.generate(input).first { it.id == "hydration_consistency" }
        assertTrue(insight.message.contains("2 of the last 7 days"))
        assertTrue(insight.evidence.contains("3 days had a water log"))
    }

    @Test
    fun missingCurrentDataProducesOneActionableEmptyState() {
        val insights = DeterministicInsightEngine.generate(
            DeterministicInsightInput(today = today, zone = zone)
        )

        assertEquals(1, insights.size)
        assertEquals("no_recent_checkins", insights.single().id)
        assertEquals("track", insights.single().actionRoute)
    }

    @Test
    fun stepComparisonUsesRecordedDaysOnly() {
        val input = DeterministicInsightInput(
            today = today,
            zone = zone,
            stepsByDay = mapOf(
                today to 10_000,
                today.minusDays(1) to 8_000,
                today.minusDays(8) to 6_000,
                today.minusDays(9) to 6_000
            )
        )

        val insight = DeterministicInsightEngine.generate(input).first { it.id == "steps_weekly_comparison" }
        assertTrue(insight.message.contains("across 2 recorded days"))
        assertTrue(insight.message.contains("+50.0%"))
    }
}
