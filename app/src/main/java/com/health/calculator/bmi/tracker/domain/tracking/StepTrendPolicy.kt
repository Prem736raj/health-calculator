package com.health.calculator.bmi.tracker.domain.tracking

import java.time.LocalDate

/** Explainable week-over-week comparison for date-level step totals. */
object StepTrendPolicy {
    private const val WINDOW_DAYS = 7L

    fun weeklyComparison(
        stepsByDay: Map<LocalDate, Int>,
        today: LocalDate
    ): TrackingComparison? {
        val currentStart = today.minusDays(WINDOW_DAYS - 1)
        val previousStart = currentStart.minusDays(WINDOW_DAYS)
        val current = stepsByDay.valuesFor(currentStart, today)
        val previous = stepsByDay.valuesFor(previousStart, currentStart.minusDays(1))
        return buildTrackingComparison(current, previous)
    }

    private fun Map<LocalDate, Int>.valuesFor(start: LocalDate, end: LocalDate): List<Double> =
        filterKeys { it in start..end }
            .values
            .filter { it >= 0 }
            .map(Int::toDouble)
}
