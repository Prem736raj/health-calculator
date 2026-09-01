package com.health.calculator.bmi.tracker.tracking

import com.health.calculator.bmi.tracker.domain.tracking.TrackingQualityPolicy
import com.health.calculator.bmi.tracker.domain.tracking.TrackingTrend
import com.health.calculator.bmi.tracker.domain.tracking.buildTrackingComparison
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TrackingQualityPolicyTest {
    @Test
    fun weightValidationUsesInclusiveMetricBoundaries() {
        assertNull(TrackingQualityPolicy.validateWeightKg(20.0))
        assertNull(TrackingQualityPolicy.validateWeightKg(350.0))
        assertTrue(TrackingQualityPolicy.validateWeightKg(19.99) != null)
        assertTrue(TrackingQualityPolicy.validateWeightKg(350.01) != null)
        assertTrue(TrackingQualityPolicy.validateWeightKg(Double.NaN) != null)
    }

    @Test
    fun malformedOrOverlongNotesAreRejected() {
        assertNull(TrackingQualityPolicy.validateNote("Short note"))
        assertTrue(TrackingQualityPolicy.validateNote("x".repeat(281)) != null)
        assertNull(TrackingQualityPolicy.validateWaterMl(1))
        assertNull(TrackingQualityPolicy.validateWaterMl(5_000))
        assertTrue(TrackingQualityPolicy.validateWaterMl(0) != null)
        assertTrue(TrackingQualityPolicy.validateWaterMl(5_001) != null)
    }

    @Test
    fun futureLogDatesAreRejectedWithSmallClockTolerance() {
        val now = 1_000_000L
        assertNull(TrackingQualityPolicy.validateTimestamp(now + TrackingQualityPolicy.FUTURE_DATE_TOLERANCE_MILLIS, now))
        assertTrue(TrackingQualityPolicy.validateTimestamp(now + TrackingQualityPolicy.FUTURE_DATE_TOLERANCE_MILLIS + 1, now) != null)
    }

    @Test
    fun comparisonUsesAverageAndAvoidsDivisionByZero() {
        val comparison = buildTrackingComparison(
            currentValues = listOf(90.0, 91.0),
            previousValues = listOf(100.0, 101.0)
        )!!
        assertEquals(90.5, comparison.currentAverage, 0.0001)
        assertEquals(100.5, comparison.previousAverage, 0.0001)
        assertEquals(TrackingTrend.DOWN, comparison.trend)
        assertNull(buildTrackingComparison(listOf(1.0), listOf(0.0)))
    }

    @Test
    fun streakCountsConsecutiveCompletedDaysWithoutPunishingFutureDays() {
        val today = LocalDate.of(2026, 9, 1)
        val dates = setOf(today, today.minusDays(1), today.minusDays(2), today.plusDays(1))
        assertEquals(3, TrackingQualityPolicy.currentLoggingStreak(dates, today))
        assertEquals(0, TrackingQualityPolicy.currentLoggingStreak(setOf(today.minusDays(1)), today))
    }
}
