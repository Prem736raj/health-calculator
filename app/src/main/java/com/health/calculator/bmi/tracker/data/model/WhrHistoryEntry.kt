package com.health.calculator.bmi.tracker.data.model

data class WhrHistoryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val waistCm: Float,
    val hipCm: Float,
    val whr: Float,
    val whtr: Float? = null,
    val gender: Gender,
    val age: Int,
    val category: WhrCategory,
    val waistRiskLevel: WaistRiskLevel,
    val bodyShape: BodyShape,
    val timestamp: Long = System.currentTimeMillis()
)

data class WhrGoal(
    val targetWaistCm: Float,
    val setDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

data class WhrProgressStats(
    val currentWhr: Float,
    val firstWhr: Float,
    val bestWhr: Float,
    val worstWhr: Float,
    val averageWhr: Float,
    val currentWaist: Float,
    val firstWaist: Float,
    val waistChange: Float,
    val currentHip: Float,
    val firstHip: Float,
    val hipChange: Float,
    val totalMeasurements: Int,
    val whrChange: Float,
    val whrTrend: WhrTrendDirection,
    val waistTrend: WhrTrendDirection,
    val hipTrend: WhrTrendDirection
)

data class WhrComparison(
    val previousWhr: Float,
    val currentWhr: Float,
    val whrDiff: Float,
    val whrDirection: WhrTrendDirection,
    val previousWaist: Float,
    val currentWaist: Float,
    val waistDiff: Float,
    val waistDirection: WhrTrendDirection,
    val previousHip: Float,
    val currentHip: Float,
    val hipDiff: Float,
    val hipDirection: WhrTrendDirection,
    val previousDate: Long,
    val currentDate: Long
)

enum class WhrTrendDirection {
    IMPROVING, WORSENING, STEADY
}

enum class WhrTimeRange(val label: String, val days: Int) {
    THIRTY_DAYS("30 Days", 30),
    NINETY_DAYS("90 Days", 90),
    ONE_YEAR("1 Year", 365),
    ALL_TIME("All Time", Int.MAX_VALUE)
}
