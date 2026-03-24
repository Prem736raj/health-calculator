package com.health.calculator.bmi.tracker.util

import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class HealthScoreResult(
    val totalScore: Int,
    val maxPossibleScore: Int,
    val scoreBreakdown: List<HealthScoreComponent>,
    val category: HealthScoreCategory,
    val availableMetrics: Int,
    val totalMetrics: Int
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
    EXCELLENT(
        "Excellent",
        "🌟",
        Color(0xFF4CAF50),
        "Outstanding! You're maintaining excellent health metrics."
    ),
    GOOD(
        "Good",
        "👍",
        Color(0xFF2196F3),
        "Great job! Your health metrics are in good shape."
    ),
    FAIR(
        "Fair",
        "📊",
        Color(0xFFFFC107),
        "You're on the right track. A few improvements can boost your score."
    ),
    NEEDS_ATTENTION(
        "Needs Attention",
        "⚠️",
        Color(0xFFFF9800),
        "Some metrics need attention. Consider lifestyle adjustments."
    ),
    CONCERNING(
        "Concerning",
        "🔴",
        Color(0xFFF44336),
        "Your metrics indicate health concerns. Consider consulting a healthcare provider."
    ),
    INSUFFICIENT_DATA(
        "Calculate More",
        "📝",
        Color(0xFF9E9E9E),
        "Calculate your health metrics to see your personalized health score."
    )
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
    val restingHRTimestamp: Long? = null
)

data class QuickStat(
    val id: String,
    val emoji: String,
    val label: String,
    val value: String,
    val subValue: String? = null,
    val color: Color,
    val progress: Float? = null, // 0-1 for progress-based stats
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
    
    const val MAX_TOTAL_POINTS = BMI_POINTS + BP_POINTS + WHR_POINTS + 
            WATER_POINTS + CALORIE_POINTS + RESTING_HR_POINTS // 100

    /**
     * Calculate overall health score from available metrics
     */
    fun calculateHealthScore(metrics: HealthMetricsSnapshot): HealthScoreResult {
        val components = mutableListOf<HealthScoreComponent>()
        var totalScore = 0
        var availableMetrics = 0

        // BMI Score
        val bmiComponent = calculateBMIScore(metrics.bmi, metrics.bmiCategory)
        components.add(bmiComponent)
        if (bmiComponent.hasData) {
            totalScore += bmiComponent.points
            availableMetrics++
        }

        // BP Score
        val bpComponent = calculateBPScore(metrics.systolicBP, metrics.diastolicBP, metrics.bpCategory)
        components.add(bpComponent)
        if (bpComponent.hasData) {
            totalScore += bpComponent.points
            availableMetrics++
        }

        // WHR Score
        val whrComponent = calculateWHRScore(metrics.whr, metrics.whrCategory)
        components.add(whrComponent)
        if (whrComponent.hasData) {
            totalScore += whrComponent.points
            availableMetrics++
        }

        // Water Score
        val waterComponent = calculateWaterScore(metrics.waterIntakeToday, metrics.waterGoalToday)
        components.add(waterComponent)
        if (waterComponent.hasData) {
            totalScore += waterComponent.points
            availableMetrics++
        }

        // Calorie Score
        val calorieComponent = calculateCalorieScore(
            metrics.caloriesConsumedToday, 
            metrics.calorieTargetToday
        )
        components.add(calorieComponent)
        if (calorieComponent.hasData) {
            totalScore += calorieComponent.points
            availableMetrics++
        }

        // Resting HR Score
        val hrComponent = calculateRestingHRScore(metrics.restingHR)
        components.add(hrComponent)
        if (hrComponent.hasData) {
            totalScore += hrComponent.points
            availableMetrics++
        }

        // Calculate normalized score (out of available metrics)
        val normalizedScore = if (availableMetrics > 0) {
            val maxForAvailable = components.filter { it.hasData }.sumOf { it.maxPoints }
            if (maxForAvailable > 0) {
                ((totalScore.toFloat() / maxForAvailable) * 100).roundToInt()
            } else 0
        } else 0

        val category = when {
            availableMetrics < 2 -> HealthScoreCategory.INSUFFICIENT_DATA
            normalizedScore >= 80 -> HealthScoreCategory.EXCELLENT
            normalizedScore >= 60 -> HealthScoreCategory.GOOD
            normalizedScore >= 40 -> HealthScoreCategory.FAIR
            normalizedScore >= 20 -> HealthScoreCategory.NEEDS_ATTENTION
            else -> HealthScoreCategory.CONCERNING
        }

        return HealthScoreResult(
            totalScore = normalizedScore,
            maxPossibleScore = 100,
            scoreBreakdown = components,
            category = category,
            availableMetrics = availableMetrics,
            totalMetrics = 6
        )
    }

    private fun calculateBMIScore(bmi: Float?, category: String?): HealthScoreComponent {
        if (bmi == null) {
            return HealthScoreComponent(
                name = "BMI",
                emoji = "📊",
                points = 0,
                maxPoints = BMI_POINTS,
                status = ComponentStatus.NO_DATA,
                statusMessage = "Not calculated",
                hasData = false
            )
        }

        val (points, status, message) = when {
            bmi >= 18.5f && bmi < 25f -> Triple(BMI_POINTS, ComponentStatus.EXCELLENT, "Normal weight")
            bmi >= 17f && bmi < 18.5f -> Triple(15, ComponentStatus.GOOD, "Slightly underweight")
            bmi >= 25f && bmi < 27f -> Triple(15, ComponentStatus.GOOD, "Slightly overweight")
            bmi >= 27f && bmi < 30f -> Triple(10, ComponentStatus.FAIR, "Overweight")
            bmi >= 30f && bmi < 35f -> Triple(5, ComponentStatus.POOR, "Obese Class I")
            bmi >= 35f -> Triple(0, ComponentStatus.POOR, "Obese Class II+")
            bmi < 17f -> Triple(5, ComponentStatus.POOR, "Underweight")
            else -> Triple(10, ComponentStatus.FAIR, "Check BMI")
        }

        return HealthScoreComponent(
            name = "BMI",
            emoji = "📊",
            points = points,
            maxPoints = BMI_POINTS,
            status = status,
            statusMessage = message,
            hasData = true
        )
    }

    private fun calculateBPScore(systolic: Int?, diastolic: Int?, category: String?): HealthScoreComponent {
        if (systolic == null || diastolic == null) {
            return HealthScoreComponent(
                name = "Blood Pressure",
                emoji = "💓",
                points = 0,
                maxPoints = BP_POINTS,
                status = ComponentStatus.NO_DATA,
                statusMessage = "Not recorded",
                hasData = false
            )
        }

        val (points, status, message) = when {
            systolic < 120 && diastolic < 80 -> Triple(BP_POINTS, ComponentStatus.EXCELLENT, "Optimal")
            systolic < 130 && diastolic < 85 -> Triple(17, ComponentStatus.GOOD, "Normal")
            systolic < 140 && diastolic < 90 -> Triple(12, ComponentStatus.FAIR, "High Normal")
            systolic < 160 && diastolic < 100 -> Triple(6, ComponentStatus.POOR, "Grade 1 Hypertension")
            systolic >= 160 || diastolic >= 100 -> Triple(0, ComponentStatus.POOR, "Grade 2+ Hypertension")
            systolic < 90 || diastolic < 60 -> Triple(8, ComponentStatus.FAIR, "Low BP")
            else -> Triple(10, ComponentStatus.FAIR, "Check BP")
        }

        return HealthScoreComponent(
            name = "Blood Pressure",
            emoji = "💓",
            points = points,
            maxPoints = BP_POINTS,
            status = status,
            statusMessage = message,
            hasData = true
        )
    }

    private fun calculateWHRScore(whr: Float?, category: String?): HealthScoreComponent {
        if (whr == null) {
            return HealthScoreComponent(
                name = "Waist-Hip Ratio",
                emoji = "📏",
                points = 0,
                maxPoints = WHR_POINTS,
                status = ComponentStatus.NO_DATA,
                statusMessage = "Not measured",
                hasData = false
            )
        }

        // Assuming we don't know gender, use general thresholds
        val (points, status, message) = when {
            whr < 0.85f -> Triple(WHR_POINTS, ComponentStatus.EXCELLENT, "Low risk")
            whr < 0.90f -> Triple(12, ComponentStatus.GOOD, "Moderate")
            whr < 0.95f -> Triple(7, ComponentStatus.FAIR, "Increased risk")
            whr >= 0.95f -> Triple(3, ComponentStatus.POOR, "High risk")
            else -> Triple(7, ComponentStatus.FAIR, "Check WHR")
        }

        return HealthScoreComponent(
            name = "Waist-Hip Ratio",
            emoji = "📏",
            points = points,
            maxPoints = WHR_POINTS,
            status = status,
            statusMessage = message,
            hasData = true
        )
    }

    private fun calculateWaterScore(intake: Int, goal: Int): HealthScoreComponent {
        if (goal <= 0) {
            return HealthScoreComponent(
                name = "Hydration",
                emoji = "💧",
                points = 0,
                maxPoints = WATER_POINTS,
                status = ComponentStatus.NO_DATA,
                statusMessage = "Set your water goal",
                hasData = false
            )
        }

        val progress = intake.toFloat() / goal
        val (points, status, message) = when {
            progress >= 1.0f -> Triple(WATER_POINTS, ComponentStatus.EXCELLENT, "Goal met!")
            progress >= 0.75f -> Triple(12, ComponentStatus.GOOD, "${(progress * 100).toInt()}% complete")
            progress >= 0.50f -> Triple(8, ComponentStatus.FAIR, "${(progress * 100).toInt()}% complete")
            progress >= 0.25f -> Triple(4, ComponentStatus.POOR, "${(progress * 100).toInt()}% complete")
            else -> Triple(0, ComponentStatus.POOR, "Just started")
        }

        return HealthScoreComponent(
            name = "Hydration",
            emoji = "💧",
            points = points,
            maxPoints = WATER_POINTS,
            status = status,
            statusMessage = message,
            hasData = true
        )
    }

    private fun calculateCalorieScore(consumed: Int, target: Int): HealthScoreComponent {
        if (target <= 0) {
            return HealthScoreComponent(
                name = "Calories",
                emoji = "🔥",
                points = 0,
                maxPoints = CALORIE_POINTS,
                status = ComponentStatus.NO_DATA,
                statusMessage = "Set your calorie target",
                hasData = false
            )
        }

        val ratio = consumed.toFloat() / target
        val (points, status, message) = when {
            ratio in 0.9f..1.1f -> Triple(CALORIE_POINTS, ComponentStatus.EXCELLENT, "On target")
            ratio in 0.8f..1.2f -> Triple(12, ComponentStatus.GOOD, "Close to target")
            ratio in 0.7f..1.3f -> Triple(8, ComponentStatus.FAIR, "Slightly off target")
            ratio < 0.7f -> Triple(5, ComponentStatus.FAIR, "Under eating")
            ratio > 1.3f -> Triple(3, ComponentStatus.POOR, "Over target")
            else -> Triple(8, ComponentStatus.FAIR, "Check intake")
        }

        return HealthScoreComponent(
            name = "Calories",
            emoji = "🔥",
            points = points,
            maxPoints = CALORIE_POINTS,
            status = status,
            statusMessage = message,
            hasData = true
        )
    }

    private fun calculateRestingHRScore(restingHR: Int?): HealthScoreComponent {
        if (restingHR == null) {
            return HealthScoreComponent(
                name = "Resting Heart Rate",
                emoji = "❤️",
                points = 0,
                maxPoints = RESTING_HR_POINTS,
                status = ComponentStatus.NO_DATA,
                statusMessage = "Not measured",
                hasData = false
            )
        }

        val (points, status, message) = when {
            restingHR < 60 -> Triple(RESTING_HR_POINTS, ComponentStatus.EXCELLENT, "Athletic level")
            restingHR in 60..70 -> Triple(RESTING_HR_POINTS, ComponentStatus.EXCELLENT, "Excellent")
            restingHR in 71..80 -> Triple(12, ComponentStatus.GOOD, "Good")
            restingHR in 81..90 -> Triple(8, ComponentStatus.FAIR, "Average")
            restingHR in 91..100 -> Triple(4, ComponentStatus.POOR, "Below average")
            restingHR > 100 -> Triple(0, ComponentStatus.POOR, "Elevated - consult doctor")
            else -> Triple(8, ComponentStatus.FAIR, "Check HR")
        }

        return HealthScoreComponent(
            name = "Resting Heart Rate",
            emoji = "❤️",
            points = points,
            maxPoints = RESTING_HR_POINTS,
            status = status,
            statusMessage = message,
            hasData = true
        )
    }

    /**
     * Build quick stats from available metrics
     */
    fun buildQuickStats(metrics: HealthMetricsSnapshot): List<QuickStat> {
        val stats = mutableListOf<QuickStat>()

        // BMI Stat
        if (metrics.bmi != null) {
            val color = when {
                metrics.bmi >= 18.5f && metrics.bmi < 25f -> Color(0xFF4CAF50)
                metrics.bmi >= 25f && metrics.bmi < 30f -> Color(0xFFFF9800)
                metrics.bmi >= 30f -> Color(0xFFF44336)
                metrics.bmi < 18.5f -> Color(0xFFFF9800)
                else -> Color(0xFF9E9E9E)
            }
            stats.add(
                QuickStat(
                    id = "bmi",
                    emoji = "📊",
                    label = "BMI",
                    value = "%.1f".format(metrics.bmi),
                    subValue = metrics.bmiCategory,
                    color = color,
                    timestamp = metrics.bmiTimestamp,
                    calculatorRoute = "bmi_calculator"
                )
            )
        }

        // BP Stat
        if (metrics.systolicBP != null && metrics.diastolicBP != null) {
            val color = when {
                metrics.systolicBP < 120 && metrics.diastolicBP < 80 -> Color(0xFF4CAF50)
                metrics.systolicBP < 140 && metrics.diastolicBP < 90 -> Color(0xFFFFC107)
                else -> Color(0xFFF44336)
            }
            stats.add(
                QuickStat(
                    id = "bp",
                    emoji = "💓",
                    label = "Blood Pressure",
                    value = "${metrics.systolicBP}/${metrics.diastolicBP}",
                    subValue = metrics.bpCategory,
                    color = color,
                    timestamp = metrics.bpTimestamp,
                    calculatorRoute = "blood_pressure_checker"
                )
            )
        }

        // Water Progress
        if (metrics.waterGoalToday > 0) {
            val progress = (metrics.waterIntakeToday.toFloat() / metrics.waterGoalToday).coerceIn(0f, 1f)
            val color = when {
                progress >= 1f -> Color(0xFF4CAF50)
                progress >= 0.6f -> Color(0xFF2196F3)
                progress >= 0.3f -> Color(0xFFFFC107)
                else -> Color(0xFFFF9800)
            }
            stats.add(
                QuickStat(
                    id = "water",
                    emoji = "💧",
                    label = "Water",
                    value = "${metrics.waterIntakeToday}ml",
                    subValue = "/${metrics.waterGoalToday}ml",
                    color = color,
                    progress = progress,
                    calculatorRoute = "water_intake_calculator"
                )
            )
        }

        // Calorie Progress
        if (metrics.calorieTargetToday > 0) {
            val progress = (metrics.caloriesConsumedToday.toFloat() / metrics.calorieTargetToday).coerceIn(0f, 1.5f)
            val color = when {
                progress in 0.9f..1.1f -> Color(0xFF4CAF50)
                progress < 0.9f -> Color(0xFF2196F3)
                else -> Color(0xFFFF9800)
            }
            stats.add(
                QuickStat(
                    id = "calories",
                    emoji = "🔥",
                    label = "Calories",
                    value = "${metrics.caloriesConsumedToday}",
                    subValue = "/${metrics.calorieTargetToday}",
                    color = color,
                    progress = progress.coerceAtMost(1f),
                    calculatorRoute = "calorie_calculator"
                )
            )
        }

        return stats.take(4) // Max 4 stats
    }

    /**
     * Format timestamp to relative time string
     */
    fun formatTimeAgo(timestamp: Long): String {
        val now = Instant.now()
        val then = Instant.ofEpochMilli(timestamp)
        val minutes = ChronoUnit.MINUTES.between(then, now)
        val hours = ChronoUnit.HOURS.between(then, now)
        val days = ChronoUnit.DAYS.between(then, now)

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
