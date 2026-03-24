// data/models/WeeklyReport.kt
package com.health.calculator.bmi.tracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_reports")
data class WeeklyReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekStartDate: Long, // Monday epoch millis
    val weekEndDate: Long,   // Sunday epoch millis
    val generatedAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,

    // Weight
    val weightStart: Double? = null,
    val weightEnd: Double? = null,
    val weightChange: Double? = null,
    val weightEntryCount: Int = 0,

    // BMI
    val avgBmi: Double? = null,
    val bmiReadingCount: Int = 0,
    val bestBmiCategory: String? = null,

    // Blood Pressure
    val avgSystolic: Double? = null,
    val avgDiastolic: Double? = null,
    val bpReadingCount: Int = 0,
    val prevWeekAvgSystolic: Double? = null,
    val prevWeekAvgDiastolic: Double? = null,

    // Water
    val waterDaysGoalMet: Int = 0,
    val waterDaysTracked: Int = 0,
    val avgWaterIntakeMl: Int = 0,
    val waterGoalMl: Int = 2500,

    // Calories
    val avgCaloriesConsumed: Int = 0,
    val calorieTarget: Int = 2000,
    val calorieDaysLogged: Int = 0,
    val calorieDaysOnTarget: Int = 0,

    // Exercise
    val exerciseMinutes: Int = 0,
    val exerciseSessions: Int = 0,

    // Health Score
    val healthScoreStart: Int = -1,
    val healthScoreEnd: Int = -1,
    val healthScoreChange: Int = 0,

    // Achievements
    val milestonesEarned: Int = 0,
    val personalRecordsSet: Int = 0,
    val bestAchievement: String? = null,
    val streakUpdates: String? = null, // JSON encoded

    // Overall
    val overallGrade: String = "B", // A, B, C, D, F
    val overallMessage: String = "",
    val focusSuggestions: String = "" // JSON encoded list
)

data class WeeklyReportSummary(
    val report: WeeklyReport,
    val metricSummaries: List<MetricWeeklySummary>,
    val highlights: List<WeeklyHighlight>,
    val nextWeekGoals: List<NextWeekGoal>,
    val previousWeekReport: WeeklyReport? = null
)

data class MetricWeeklySummary(
    val metricName: String,
    val icon: String,
    val currentValue: String,
    val previousValue: String?,
    val trend: MetricTrend,
    val detail: String,
    val isGood: Boolean
)

enum class MetricTrend(val arrow: String, val label: String) {
    IMPROVING("📈", "Improving"),
    DECLINING("📉", "Declining"),
    STABLE("➡️", "Stable"),
    NEW("🆕", "New this week"),
    NO_DATA("—", "No data")
}

data class WeeklyHighlight(
    val icon: String,
    val title: String,
    val description: String,
    val type: HighlightType
)

enum class HighlightType {
    ACHIEVEMENT, RECORD, STREAK, IMPROVEMENT, MILESTONE
}

data class NextWeekGoal(
    val icon: String,
    val suggestion: String,
    val reason: String,
    val priority: Int // 1 = highest
)
