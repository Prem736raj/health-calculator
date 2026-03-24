package com.health.calculator.bmi.tracker.domain.usecase

import com.health.calculator.bmi.tracker.data.model.*
import java.text.SimpleDateFormat
import java.util.*

class CalorieHistoryAnalyticsUseCase {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    fun computeStats(
        logs: List<DailyFoodLog>,
        targetCalories: Double,
        targetProtein: Double,
        targetCarbs: Double,
        targetFat: Double
    ): CalorieHistoryStats {
        if (logs.isEmpty()) return CalorieHistoryStats(
            0.0, 0.0, 0.0, 0, 0, null, null, 0, 0,
            null, 0.0, 0.0, 0.0, targetCalories, targetProtein, targetCarbs, targetFat
        )

        val logsWithData = logs.filter { it.entries.isNotEmpty() }

        val avgCalories = logsWithData.map { it.totalCalories }.average().takeIf { !it.isNaN() } ?: 0.0
        val avgProtein = logsWithData.map { it.totalProtein }.average().takeIf { !it.isNaN() } ?: 0.0
        val avgCarbs = logsWithData.map { it.totalCarbs }.average().takeIf { !it.isNaN() } ?: 0.0
        val avgFat = logsWithData.map { it.totalFat }.average().takeIf { !it.isNaN() } ?: 0.0

        // Last 7 days
        val last7 = logsWithData.take(7)
        val weeklyAvg = last7.map { it.totalCalories }.average().takeIf { !it.isNaN() } ?: 0.0
        val weeklyTotal = last7.sumOf { it.totalCalories }

        // Days at target
        val daysAtTarget = logsWithData.count {
            kotlin.math.abs(it.totalCalories - targetCalories) <= 100
        }

        // High/Low days
        val highDay = logsWithData.maxByOrNull { it.totalCalories }
        val lowDay = logsWithData.minByOrNull { it.totalCalories }

        // Most logged food
        val allFoodNames = logsWithData.flatMap { log -> log.entries.map { it.name } }
        val mostLogged = allFoodNames.groupBy { it }.maxByOrNull { it.value.size }?.key

        // Streak calculation
        val streak = computeCurrentStreak(logs, targetCalories)
        val longestStreak = computeLongestStreak(logs, targetCalories)

        return CalorieHistoryStats(
            averageDailyCalories = avgCalories,
            weeklyAverage = weeklyAvg,
            weeklyTotal = weeklyTotal,
            daysAtTarget = daysAtTarget,
            totalDaysTracked = logsWithData.size,
            highestCalorieDay = highDay?.let { dayFormat.format(parseDate(it.date)) to it.totalCalories },
            lowestCalorieDay = lowDay?.let { dayFormat.format(parseDate(it.date)) to it.totalCalories },
            currentStreak = streak,
            longestStreak = longestStreak,
            mostLoggedFood = mostLogged,
            averageProtein = avgProtein,
            averageCarbs = avgCarbs,
            averageFat = avgFat,
            targetCalories = targetCalories,
            targetProtein = targetProtein,
            targetCarbs = targetCarbs,
            targetFat = targetFat
        )
    }

    fun getCalendarData(
        logs: List<DailyFoodLog>,
        year: Int,
        month: Int // 0-based
    ): Map<Int, DayCalorieStatus> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val result = mutableMapOf<Int, DayCalorieStatus>()

        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dateKey = dateFormat.format(cal.time)
            val log = logs.find { it.date == dateKey }

            result[day] = DayCalorieStatus(
                date = dateKey,
                totalCalories = log?.totalCalories ?: 0.0,
                targetCalories = log?.targetCalories ?: 2000.0,
                hasData = log != null && log.entries.isNotEmpty()
            )
        }
        return result
    }

    fun getFilteredTrendData(
        logs: List<DailyFoodLog>,
        days: Int
    ): List<Pair<String, Double>> {
        return logs
            .filter { it.entries.isNotEmpty() }
            .take(days)
            .reversed()
            .map { log ->
                val date = runCatching { parseDate(log.date) }.getOrNull()
                    ?.let { dayFormat.format(it) } ?: log.date
                date to log.totalCalories
            }
    }

    fun getWeeklySummaries(logs: List<DailyFoodLog>): List<WeeklyCalorieSummary> {
        val logsWithData = logs.filter { it.entries.isNotEmpty() }.reversed()
        val weeks = logsWithData.chunked(7)
        return weeks.mapIndexed { index, weekLogs ->
            val weekLabel = if (index == 0) "This Week" else "${(index) * 7}-${(index + 1) * 7} days ago"
            WeeklyCalorieSummary(
                weekLabel = weekLabel,
                averageCalories = weekLogs.map { it.totalCalories }.average().takeIf { !it.isNaN() } ?: 0.0,
                totalCalories = weekLogs.sumOf { it.totalCalories },
                targetCalories = weekLogs.firstOrNull()?.targetCalories ?: 2000.0,
                daysTracked = weekLogs.size
            )
        }
    }

    fun getMacroTrend(
        logs: List<DailyFoodLog>,
        days: Int
    ): Triple<Double, Double, Double> {
        val filtered = logs.filter { it.entries.isNotEmpty() }.take(days)
        if (filtered.isEmpty()) return Triple(0.0, 0.0, 0.0)
        val avgP = filtered.map { it.totalProtein }.average().takeIf { !it.isNaN() } ?: 0.0
        val avgC = filtered.map { it.totalCarbs }.average().takeIf { !it.isNaN() } ?: 0.0
        val avgF = filtered.map { it.totalFat }.average().takeIf { !it.isNaN() } ?: 0.0
        return Triple(avgP, avgC, avgF)
    }

    private fun computeCurrentStreak(logs: List<DailyFoodLog>, target: Double): Int {
        var streak = 0
        val today = dateFormat.format(Date())
        val sortedLogs = logs.sortedByDescending { it.date }
        for (log in sortedLogs) {
            if (log.entries.isEmpty()) break
            if (kotlin.math.abs(log.totalCalories - target) <= 200) streak++
            else break
        }
        return streak
    }

    private fun computeLongestStreak(logs: List<DailyFoodLog>, target: Double): Int {
        var longest = 0; var current = 0
        logs.reversed().forEach { log ->
            if (log.entries.isNotEmpty() && kotlin.math.abs(log.totalCalories - target) <= 200) {
                current++; if (current > longest) longest = current
            } else current = 0
        }
        return longest
    }

    private fun parseDate(dateStr: String): Date =
        runCatching { dateFormat.parse(dateStr)!! }.getOrDefault(Date())
}
