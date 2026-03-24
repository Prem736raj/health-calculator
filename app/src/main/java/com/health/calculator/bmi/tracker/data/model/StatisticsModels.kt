package com.health.calculator.bmi.tracker.data.model

import java.text.SimpleDateFormat
import java.util.*

data class OverallStatistics(
    val totalCalculations: Int = 0,
    val totalActiveDays: Int = 0,
    val daysSinceFirstUse: Int = 0,
    val firstCalculationDate: Long = 0L,
    val mostUsedCalculator: CalculatorUsageStat? = null,
    val leastUsedCalculator: CalculatorUsageStat? = null,
    val averageCalculationsPerDay: Float = 0f,
    val averageCalculationsPerWeek: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
) {
    val firstUseDateFormatted: String
        get() = if (firstCalculationDate > 0) {
            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(firstCalculationDate))
        } else "N/A"
}

data class CalculatorUsageStat(
    val type: CalculatorType,
    val totalCalculations: Int,
    val lastCalculationDate: Long = 0L,
    val percentageOfTotal: Float = 0f,
    val averageValue: String? = null,
    val trend: TrendDirection = TrendDirection.STABLE
)

enum class TrendDirection {
    IMPROVING, DECLINING, STABLE, FLUCTUATING;

    val icon: String
        get() = when (this) {
            IMPROVING -> "↑"
            DECLINING -> "↓"
            STABLE -> "→"
            FLUCTUATING -> "⇅"
        }
}

data class HealthTrendSummary(
    val title: String,
    val currentStatus: String,
    val changePercentage: Float,
    val direction: TrendDirection,
    val insight: String,
    val recommendation: String
)

data class HeatmapMonth(
    val monthName: String,
    val year: Int,
    val days: List<HeatmapDay>
)

data class HeatmapDay(
    val dayOfMonth: Int,
    val date: Long,
    val intensity: Float, // 0.0 to 1.0 based on calculation count
    val count: Int
)

data class PeriodReport(
    val periodName: String, // "Weekly Report", "Monthly Report", etc.
    val startDate: Long,
    val endDate: Long,
    val totalCalculations: Int,
    val activeDays: Int,
    val topCalculators: List<CalculatorUsageStat>,
    val highlights: List<String>,
    val healthInsights: List<HealthTrendSummary>
) {
    val dateRangeFormatted: String
        get() {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            return "${sdf.format(Date(startDate))} - ${sdf.format(Date(endDate))}"
        }
}
