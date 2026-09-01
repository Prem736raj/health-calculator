package com.health.calculator.bmi.tracker.domain.insights

import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import com.health.calculator.bmi.tracker.data.model.WeightEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.roundToInt

/** A timestamped numeric value used by the explainable insight rules. */
data class InsightMetric(
    val timestampMillis: Long,
    val value: Double
)

data class DeterministicInsightInput(
    val today: LocalDate,
    val zone: ZoneId = ZoneId.systemDefault(),
    val weightEntries: List<InsightMetric> = emptyList(),
    val waterByDayMl: Map<LocalDate, Int> = emptyMap(),
    val waterGoalMl: Int = 0,
    val stepsByDay: Map<LocalDate, Int> = emptyMap(),
    val bloodPressureTimestamps: List<Long> = emptyList(),
    val goalWeightKg: Double? = null
)

data class WellnessInsight(
    val id: String,
    val title: String,
    val message: String,
    val evidence: String,
    val actionLabel: String,
    val actionRoute: String,
    val priority: Int
)

/**
 * Generates small, reproducible observations from user-entered data. The engine deliberately
 * describes logging patterns and comparisons; it never infers causes, diagnoses conditions, or
 * labels a value as medically safe/unsafe.
 */
object DeterministicInsightEngine {
    private const val WINDOW_DAYS = 7L
    private const val MAX_INSIGHTS = 5

    fun fromEntries(
        today: LocalDate,
        weights: List<WeightEntry>,
        waterLogs: List<WaterIntakeLog>,
        waterGoalMl: Int,
        stepsByDay: Map<LocalDate, Int> = emptyMap(),
        bloodPressureTimestamps: List<Long> = emptyList(),
        goalWeightKg: Double? = null,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<WellnessInsight> = generate(
        DeterministicInsightInput(
            today = today,
            zone = zone,
            weightEntries = weights.map { InsightMetric(it.dateMillis, it.weightKg) },
            waterByDayMl = waterLogs.groupBy { dayFor(it.timestamp, zone) }
                .mapValues { (_, logs) -> logs.sumOf { it.amountMl } },
            waterGoalMl = waterGoalMl,
            stepsByDay = stepsByDay,
            bloodPressureTimestamps = bloodPressureTimestamps,
            goalWeightKg = goalWeightKg
        )
    )

    fun generate(input: DeterministicInsightInput): List<WellnessInsight> {
        val currentStart = input.today.minusDays(WINDOW_DAYS - 1)
        val previousStart = currentStart.minusDays(WINDOW_DAYS)
        val insights = mutableListOf<WellnessInsight>()

        val currentWeights = input.weightEntries.filterByDate(input, currentStart, input.today)
        val previousWeights = input.weightEntries.filterByDate(input, previousStart, currentStart.minusDays(1))
        if (currentWeights.isNotEmpty() && previousWeights.isNotEmpty()) {
            val currentAverage = currentWeights.map { it.value }.average()
            val previousAverage = previousWeights.map { it.value }.average()
            val percent = percentChange(currentAverage, previousAverage)
            if (percent != null) {
                val direction = when {
                    // `percentChange` returns percentage points (for example, 5.0 = 5%),
                    // so keep the comparison threshold in the same unit. Small changes are
                    // intentionally described as stable rather than over-interpreted.
                    percent > 5.0 -> "higher"
                    percent < -5.0 -> "lower"
                    else -> "about the same"
                }
                insights += WellnessInsight(
                    id = "weight_weekly_comparison",
                    title = "Weight pattern",
                    message = "Your average logged weight was ${format(currentAverage)} kg this week, $direction than the previous week.",
                    evidence = "${currentWeights.size} entries vs ${previousWeights.size} entries · ${formatSigned(percent)}% change",
                    actionLabel = "View weight trend",
                    actionRoute = "weight_tracking",
                    priority = 30
                )
            }
        } else if (currentWeights.isNotEmpty()) {
            insights += WellnessInsight(
                id = "weight_first_week",
                title = "Weight trend started",
                message = "You logged ${currentWeights.size} weight ${if (currentWeights.size == 1) "entry" else "entries"} in the last 7 days.",
                evidence = "A previous 7-day window is needed for a comparison.",
                actionLabel = "View weight trend",
                actionRoute = "weight_tracking",
                priority = 20
            )
        }

        if (input.waterGoalMl > 0) {
            val currentGoalDays = countGoalDays(input.waterByDayMl, currentStart, input.today, input.waterGoalMl)
            val previousGoalDays = countGoalDays(input.waterByDayMl, previousStart, currentStart.minusDays(1), input.waterGoalMl)
            val currentTrackedDays = countTrackedDays(input.waterByDayMl, currentStart, input.today)
            if (currentTrackedDays > 0 || previousGoalDays > 0) {
                val change = currentGoalDays - previousGoalDays
                val comparison = when {
                    previousGoalDays == 0 && currentGoalDays > 0 -> "up from no completed days in the previous window"
                    change > 0 -> "up $change day${if (change == 1) "" else "s"} from the previous window"
                    change < 0 -> "down ${abs(change)} day${if (abs(change) == 1) "" else "s"} from the previous window"
                    else -> "the same as the previous window"
                }
                insights += WellnessInsight(
                    id = "hydration_consistency",
                    title = "Hydration consistency",
                    message = "Your water goal was met on $currentGoalDays of the last 7 days, $comparison.",
                    evidence = "Goal: ${input.waterGoalMl} ml · $currentTrackedDays days had a water log",
                    actionLabel = "Log water",
                    actionRoute = "water_tracker",
                    priority = 25
                )
            }
        }

        val currentSteps = valuesForDays(input.stepsByDay, currentStart, input.today)
        val previousSteps = valuesForDays(input.stepsByDay, previousStart, currentStart.minusDays(1))
        if (currentSteps.isNotEmpty() && previousSteps.isNotEmpty()) {
            val currentAverage = currentSteps.average()
            val previousAverage = previousSteps.average()
            val percent = percentChange(currentAverage, previousAverage)
            if (percent != null) {
                insights += WellnessInsight(
                    id = "steps_weekly_comparison",
                    title = "Step pattern",
                    message = "Your average daily steps were ${currentAverage.roundToInt()} across ${currentSteps.size} recorded days, ${formatSigned(percent)}% compared with the previous week.",
                    evidence = "${previousSteps.size} recorded days in the previous window",
                    actionLabel = "Review connected data",
                    actionRoute = "health_connections",
                    priority = 22
                )
            }
        } else if (currentSteps.isNotEmpty()) {
            insights += WellnessInsight(
                id = "steps_first_week",
                title = "Steps connected",
                message = "Steps are available for ${currentSteps.size} day${if (currentSteps.size == 1) "" else "s"} in the current window.",
                evidence = "Keep access enabled to unlock week-over-week comparisons.",
                actionLabel = "Review connected data",
                actionRoute = "health_connections",
                priority = 18
            )
        }

        val currentBpCount = input.bloodPressureTimestamps.count { dateFor(it, input.zone) in currentStart..input.today }
        val previousBpCount = input.bloodPressureTimestamps.count { dateFor(it, input.zone) in previousStart..currentStart.minusDays(1) }
        if (currentBpCount > 0 || previousBpCount > 0) {
            val delta = currentBpCount - previousBpCount
            val detail = when {
                delta > 0 -> "up $delta from the previous window"
                delta < 0 -> "down ${abs(delta)} from the previous window"
                else -> "the same as the previous window"
            }
            insights += WellnessInsight(
                id = "blood_pressure_logging",
                title = "Blood-pressure logging",
                message = "You recorded $currentBpCount blood-pressure reading${if (currentBpCount == 1) "" else "s"} in the last 7 days, $detail.",
                evidence = "This describes logging frequency, not the meaning of any reading.",
                actionLabel = "Review BP log",
                actionRoute = "blood_pressure_checker",
                priority = 21
            )
        }

        val hasCurrentData = currentWeights.isNotEmpty() || currentSteps.isNotEmpty() || currentTrackedWater(input.waterByDayMl, currentStart, input.today) > 0 || currentBpCount > 0
        if (!hasCurrentData) {
            insights += WellnessInsight(
                id = "no_recent_checkins",
                title = "Choose one small check-in",
                message = "There are no recent weight, water, steps or blood-pressure logs to compare yet.",
                evidence = "Your existing data stays private on this device unless you choose a connection or export.",
                actionLabel = "Open Track",
                actionRoute = "track",
                priority = 40
            )
        }

        input.goalWeightKg?.let { goal ->
            val latest = input.weightEntries.maxByOrNull { it.timestampMillis }?.value
            if (latest != null && goal.isFinite() && goal > 0.0) {
                insights += WellnessInsight(
                    id = "weight_goal_distance",
                    title = "Weight goal context",
                    message = "Your latest logged weight is ${format(latest)} kg; your saved goal is ${format(goal)} kg.",
                    evidence = "Distance is shown for personal tracking only and is not medical advice.",
                    actionLabel = "Review goal",
                    actionRoute = "weight_tracking",
                    priority = 15
                )
            }
        }

        return insights.sortedByDescending { it.priority }.distinctBy { it.id }.take(MAX_INSIGHTS)
    }

    private fun List<InsightMetric>.filterByDate(
        input: DeterministicInsightInput,
        start: LocalDate,
        end: LocalDate
    ): List<InsightMetric> = filter { dateFor(it.timestampMillis, input.zone) in start..end && it.value.isFinite() }

    private fun countGoalDays(values: Map<LocalDate, Int>, start: LocalDate, end: LocalDate, goal: Int): Int =
        values.count { (day, amount) -> day in start..end && amount >= goal }

    private fun countTrackedDays(values: Map<LocalDate, Int>, start: LocalDate, end: LocalDate): Int =
        values.count { (day, amount) -> day in start..end && amount > 0 }

    private fun currentTrackedWater(values: Map<LocalDate, Int>, start: LocalDate, end: LocalDate): Int =
        countTrackedDays(values, start, end)

    private fun valuesForDays(values: Map<LocalDate, Int>, start: LocalDate, end: LocalDate): List<Double> =
        values.filterKeys { it in start..end }.values.filter { it >= 0 }.map(Int::toDouble)

    private fun dateFor(timestampMillis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(timestampMillis).atZone(zone).toLocalDate()

    private fun dayFor(timestampMillis: Long, zone: ZoneId): LocalDate = dateFor(timestampMillis, zone)

    private fun percentChange(current: Double, previous: Double): Double? =
        if (!current.isFinite() || !previous.isFinite() || previous == 0.0) null else ((current - previous) / abs(previous)) * 100.0

    private fun format(value: Double): String = String.format(java.util.Locale.getDefault(), "%.1f", value)

    private fun formatSigned(value: Double): String = String.format(java.util.Locale.getDefault(), "%+.1f", value)
}
