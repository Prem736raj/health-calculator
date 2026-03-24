package com.health.calculator.bmi.tracker.data.model

data class HealthOverview(
    val latestBmi: HealthMetricSummary? = null,
    val latestBp: HealthMetricSummary? = null,
    val latestWhr: HealthMetricSummary? = null,
    val latestBmr: HealthMetricSummary? = null,
    val waterStreak: Int = 0,
    val calorieAdherence: Float = 0f,
    val metabolicSyndromeStatus: HealthMetricSummary? = null,
    val healthScore: Int = -1 // -1 means not calculated
)

data class HealthMetricSummary(
    val label: String,
    val value: String,
    val category: String,
    val categoryColor: HealthCategoryColor = HealthCategoryColor.NEUTRAL,
    val lastUpdated: Long = System.currentTimeMillis(),
    val navigateRoute: String = ""
)

enum class HealthCategoryColor {
    EXCELLENT,   // Green
    GOOD,        // Blue
    MODERATE,    // Yellow
    WARNING,     // Orange
    DANGER,      // Red
    NEUTRAL      // Gray
}
