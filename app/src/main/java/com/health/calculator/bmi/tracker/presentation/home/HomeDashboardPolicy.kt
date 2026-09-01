package com.health.calculator.bmi.tracker.presentation.home

/** Presentation limits that keep Home useful without turning it into a feed. */
object HomeDashboardPolicy {
    const val MAX_INSIGHT_PREVIEWS = 2
    const val MAX_LATEST_METRICS = 3
    val dailyMetricIds = listOf("steps", "water", "weight", "calories")

    fun <T> insightPreview(items: List<T>): List<T> = items.take(MAX_INSIGHT_PREVIEWS)
}
