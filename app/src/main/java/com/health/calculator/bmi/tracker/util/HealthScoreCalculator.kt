package com.health.calculator.bmi.tracker.util

import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * A lightweight, custom consistency indicator. It intentionally does not
 * score people by how medically healthy a BMI, blood pressure, or heart rate
 * looks. A point means that a metric was recorded (or that a daily habit was
 * logged), not that a disease risk is low.
 */
data class HealthScoreResult(
    val totalScore: Int,
    val maxPossibleScore: Int,
    val scoreBreakdown: List<HealthScoreComponent>,
    val category: HealthScoreCategory,
    val availableMetrics: Int,
    val totalMetrics: Int,
    val scoreName: String = "Wellness Score",
    val isClinicallyValidated: Boolean = false,
    val methodology: String = "Custom informational measure of recent metric availability and habit logging; not a diagnosis or clinical score."
)

data class HealthScoreComponent(
    val name: String,
    val emoji: String,
    val points: Int,
    val maxPoints: Int,
    val status: ComponentStatus,
    val statusMessage: String,
    val hasData: Boolean
)

enum class ComponentStatus { EXCELLENT, GOOD, FAIR, POOR, NO_DATA }

enum class HealthScoreCategory(
    val label: String,
    val emoji: String,
    val color: Color,
    val description: String
) {
    EXCELLENT("Great momentum", "🌟", Color(0xFF2E7D6F), "Most of your selected metrics are being recorded."),
    GOOD("On track", "👍", Color(0xFF2F6B8A), "You have a useful base of recent wellness data."),
    FAIR("Building consistency", "📊", Color(0xFFA66300), "A few more check-ins can make your trends easier to understand."),
    NEEDS_ATTENTION("Getting started", "🌱", Color(0xFFB45309), "Choose one small metric to record today."),
    CONCERNING("Limited data", "📝", Color(0xFFBA1A1A), "This reflects missing or lightly logged data, not your health."),
    INSUFFICIENT_DATA("Add a check-in", "📝", Color(0xFF63736F), "Record at least two metrics to see a useful consistency summary.")
}

data class HealthMetricsSnapshot(
    val bmi: Float? = null,
    val bmiCategory: String? = null,
    val bmiTimestamp: Long? = null,
    val systolicBP: Int? = null,
    val diastolicBP: Int? = null,
    val bpCategory: String? = null,
    val bpTimestamp: Long? = null,
    val whr: Float? = null,
    val whrCategory: String? = null,
    val whrTimestamp: Long? = null,
    val waterIntakeToday: Int = 0,
    val waterGoalToday: Int = 0,
    val caloriesConsumedToday: Int = 0,
    val calorieTargetToday: Int = 0,
    val restingHR: Int? = null,
    val restingHRTimestamp: Long? = null,
    val stepsToday: Int? = null
)

data class QuickStat(
    val id: String,
    val emoji: String,
    val label: String,
    val value: String,
    val subValue: String? = null,
    val color: Color,
    val progress: Float? = null,
    val timestamp: Long? = null,
    val calculatorRoute: String
)

data class LastActivity(
    val calculatorName: String,
    val emoji: String,
    val timestamp: Long,
    val route: String
)

object HealthScoreCalculator {
    private const val BMI_POINTS = 20
    private const val BP_POINTS = 20
    private const val WHR_POINTS = 15
    private const val WATER_POINTS = 15
    private const val CALORIE_POINTS = 15
    private const val RESTING_HR_POINTS = 15
    const val MAX_TOTAL_POINTS = 100

    fun calculateHealthScore(metrics: HealthMetricsSnapshot): HealthScoreResult {
        val components = listOf(
            presence("BMI", "📊", metrics.bmi != null, BMI_POINTS, "Recorded; BMI is informational"),
            presence("Blood Pressure", "💓", metrics.systolicBP != null && metrics.diastolicBP != null, BP_POINTS, "Recorded; review repeated readings separately"),
            presence("Waist-Hip Ratio", "📏", metrics.whr != null, WHR_POINTS, "Recorded; a body-proportion reference"),
            habit("Hydration", "💧", metrics.waterIntakeToday, metrics.waterGoalToday, WATER_POINTS),
            loggedCalories(metrics.caloriesConsumedToday, metrics.calorieTargetToday),
            presence("Resting Heart Rate", "❤️", metrics.restingHR != null, RESTING_HR_POINTS, "Recorded; varies with context")
        )
        val available = components.count { it.hasData }
        val points = components.sumOf { it.points }
        val maxForAvailable = components.filter { it.hasData }.sumOf { it.maxPoints }
        val normalized = if (maxForAvailable == 0) 0 else ((points.toDouble() / maxForAvailable) * 100).roundToInt()
        val category = when {
            available < 2 -> HealthScoreCategory.INSUFFICIENT_DATA
            normalized >= 80 -> HealthScoreCategory.EXCELLENT
            normalized >= 60 -> HealthScoreCategory.GOOD
            normalized >= 40 -> HealthScoreCategory.FAIR
            normalized >= 20 -> HealthScoreCategory.NEEDS_ATTENTION
            else -> HealthScoreCategory.CONCERNING
        }
        return HealthScoreResult(
            totalScore = normalized,
            maxPossibleScore = 100,
            scoreBreakdown = components,
            category = category,
            availableMetrics = available,
            totalMetrics = components.size
        )
    }

    private fun presence(name: String, emoji: String, hasData: Boolean, max: Int, message: String): HealthScoreComponent =
        HealthScoreComponent(name, emoji, if (hasData) max else 0, max, if (hasData) ComponentStatus.GOOD else ComponentStatus.NO_DATA, if (hasData) message else "Not recorded", hasData)

    private fun habit(name: String, emoji: String, intake: Int, goal: Int, max: Int): HealthScoreComponent {
        if (goal <= 0) return HealthScoreComponent(name, emoji, 0, max, ComponentStatus.NO_DATA, "Set a goal to track logging", false)
        val progress = (intake.toFloat() / goal).coerceIn(0f, 1f)
        val points = (progress * max).roundToInt()
        val status = when { progress >= 1f -> ComponentStatus.EXCELLENT; progress >= .5f -> ComponentStatus.GOOD; progress > 0f -> ComponentStatus.FAIR; else -> ComponentStatus.POOR }
        return HealthScoreComponent(name, emoji, points, max, status, "${(progress * 100).roundToInt()}% logged today", true)
    }

    private fun loggedCalories(consumed: Int, target: Int): HealthScoreComponent {
        if (target <= 0) return HealthScoreComponent("Calories", "🔥", 0, CALORIE_POINTS, ComponentStatus.NO_DATA, "Set a target to track logging", false)
        val hasData = consumed > 0
        return presence("Calories", "🔥", hasData, CALORIE_POINTS, "Logged today; target is informational")
    }

    /** Build up to four useful home stats without treating them as diagnoses. */
    fun buildQuickStats(metrics: HealthMetricsSnapshot): List<QuickStat> {
        val stats = mutableListOf<QuickStat>()
        metrics.bmi?.let {
            stats += QuickStat("bmi", "📊", "BMI", "%.1f".format(it), metrics.bmiCategory, Color(0xFF4F6BFF), timestamp = metrics.bmiTimestamp, calculatorRoute = "bmi_calculator")
        }
        if (metrics.systolicBP != null && metrics.diastolicBP != null) {
            stats += QuickStat("bp", "💓", "Blood Pressure", "${metrics.systolicBP}/${metrics.diastolicBP}", metrics.bpCategory, Color(0xFFE06B8B), timestamp = metrics.bpTimestamp, calculatorRoute = "blood_pressure_checker")
        }
        if (metrics.waterGoalToday > 0) {
            val progress = (metrics.waterIntakeToday.toFloat() / metrics.waterGoalToday).coerceIn(0f, 1f)
            stats += QuickStat("water", "💧", "Water", "${metrics.waterIntakeToday}ml", "/${metrics.waterGoalToday}ml", Color(0xFF3C9DD9), progress, calculatorRoute = "water_intake_calculator")
        }
        if (metrics.calorieTargetToday > 0) {
            val progress = (metrics.caloriesConsumedToday.toFloat() / metrics.calorieTargetToday).coerceIn(0f, 1f)
            stats += QuickStat("calories", "🔥", "Calories", "${metrics.caloriesConsumedToday}", "/${metrics.calorieTargetToday}", Color(0xFFE28B42), progress, calculatorRoute = "calorie_calculator")
        }
        if (metrics.stepsToday != null && stats.size < 4) {
            val progress = (metrics.stepsToday.toFloat() / 10000f).coerceIn(0f, 1f)
            stats += QuickStat("steps", "👟", "Steps", metrics.stepsToday.toString(), "/10000", Color(0xFF4E9F70), progress, calculatorRoute = "home")
        }
        return stats.take(4)
    }

    fun formatTimeAgo(timestamp: Long): String {
        val minutes = ChronoUnit.MINUTES.between(Instant.ofEpochMilli(timestamp), Instant.now())
        val hours = ChronoUnit.HOURS.between(Instant.ofEpochMilli(timestamp), Instant.now())
        val days = ChronoUnit.DAYS.between(Instant.ofEpochMilli(timestamp), Instant.now())
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            days < 30 -> "${days / 7} week${if (days / 7 > 1) "s" else ""} ago"
            else -> "${days / 30} month${if (days / 30 > 1) "s" else ""} ago"
        }
    }
}

/** New truthful name for new code; old object remains as a compatibility API. */
object WellnessScoreCalculator {
    fun calculate(metrics: HealthMetricsSnapshot): HealthScoreResult = HealthScoreCalculator.calculateHealthScore(metrics)
}
