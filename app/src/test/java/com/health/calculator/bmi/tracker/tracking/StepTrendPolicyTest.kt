package com.health.calculator.bmi.tracker.tracking

import com.health.calculator.bmi.tracker.domain.tracking.StepTrendPolicy
import com.health.calculator.bmi.tracker.domain.tracking.TrackingTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class StepTrendPolicyTest {

    @Test
    fun comparesTheTwoCompleteSevenDayWindows() {
        val today = LocalDate.of(2026, 9, 14)
        val values = buildMap {
            (0L..6L).forEach { offset -> put(today.minusDays(offset), 10_000) }
            (7L..13L).forEach { offset -> put(today.minusDays(offset), 5_000) }
        }

        val comparison = StepTrendPolicy.weeklyComparison(values, today)

        requireNotNull(comparison)
        assertEquals(10_000.0, comparison.currentAverage, 0.001)
        assertEquals(5_000.0, comparison.previousAverage, 0.001)
        assertEquals(100.0, comparison.percentChange, 0.001)
        assertEquals(TrackingTrend.UP, comparison.trend)
    }

    @Test
    fun incompletePreviousWindowDoesNotInventAComparison() {
        val today = LocalDate.of(2026, 9, 14)
        val values = (0L..6L).associate { offset -> today.minusDays(offset) to 7_000 }

        assertNull(StepTrendPolicy.weeklyComparison(values, today))
    }

    @Test
    fun zeroStepsAreValidDataButAZeroBaselineCannotProducePercentChange() {
        val today = LocalDate.of(2026, 9, 14)
        val values = buildMap {
            (0L..6L).forEach { offset -> put(today.minusDays(offset), 1_000) }
            (7L..13L).forEach { offset -> put(today.minusDays(offset), 0) }
        }

        assertNull(StepTrendPolicy.weeklyComparison(values, today))
    }
}
