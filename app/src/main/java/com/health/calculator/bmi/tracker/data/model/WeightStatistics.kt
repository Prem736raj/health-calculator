package com.health.calculator.bmi.tracker.data.model

data class WeightStatistics(
    val currentWeight: Double? = null,
    val startingWeight: Double? = null,
    val lowestWeight: Double? = null,
    val highestWeight: Double? = null,
    val totalChange: Double? = null,
    val averageWeeklyChange: Double? = null,
    val trendDirection: WeightTrendDirection = WeightTrendDirection.STABLE,
    val totalEntries: Int = 0,
    val firstEntryDate: Long? = null,
    val latestEntryDate: Long? = null
)

enum class WeightTrendDirection(val label: String, val emoji: String) {
    LOSING("Losing", "📉"),
    GAINING("Gaining", "📈"),
    STABLE("Stable", "➡️")
}
