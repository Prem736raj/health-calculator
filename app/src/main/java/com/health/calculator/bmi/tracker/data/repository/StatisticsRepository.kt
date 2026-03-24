package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import java.text.SimpleDateFormat
import kotlin.math.abs

class StatisticsRepository private constructor(
    private val historyRepository: HistoryRepository
) {
    companion object {
        @Volatile
        private var instance: StatisticsRepository? = null

        fun getInstance(historyRepository: HistoryRepository): StatisticsRepository {
            return instance ?: synchronized(this) {
                instance ?: StatisticsRepository(historyRepository).also { instance = it }
            }
        }
    }

    /** Overall statistics for all time */
    fun getOverallStatistics(): Flow<OverallStatistics> {
        return historyRepository.getAllEntries().map { entries ->
            if (entries.isEmpty()) return@map OverallStatistics()

            val totalCalculations = entries.size
            val distinctDays = entries.map { getStartOfDay(it.timestamp) }.distinct()
            val totalActiveDays = distinctDays.size
            
            val firstDate = entries.minOf { it.timestamp }
            val daysSinceFirstUse = ((System.currentTimeMillis() - firstDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)

            val usageByType = entries.groupBy { it.calculatorKey }
                .mapValues { (_, entries) -> entries.size }
            
            val mostUsedKey = usageByType.maxByOrNull { it.value }?.key
            val leastUsedKey = usageByType.minByOrNull { it.value }?.key
            
            val mostUsed = mostUsedKey?.let { key ->
                CalculatorUsageStat(
                    type = CalculatorType.fromKey(key) ?: CalculatorType.BMI,
                    totalCalculations = usageByType[key] ?: 0,
                    percentageOfTotal = (usageByType[key] ?: 0).toFloat() / totalCalculations
                )
            }

            val leastUsed = leastUsedKey?.let { key ->
                CalculatorUsageStat(
                    type = CalculatorType.fromKey(key) ?: CalculatorType.BMI,
                    totalCalculations = usageByType[key] ?: 0,
                    percentageOfTotal = (usageByType[key] ?: 0).toFloat() / totalCalculations
                )
            }

            OverallStatistics(
                totalCalculations = totalCalculations,
                totalActiveDays = totalActiveDays,
                daysSinceFirstUse = daysSinceFirstUse,
                firstCalculationDate = firstDate,
                mostUsedCalculator = mostUsed,
                leastUsedCalculator = leastUsed,
                averageCalculationsPerDay = totalCalculations.toFloat() / daysSinceFirstUse,
                averageCalculationsPerWeek = (totalCalculations.toFloat() / daysSinceFirstUse) * 7,
                currentStreak = calculateCurrentStreak(distinctDays),
                longestStreak = calculateLongestStreak(distinctDays)
            )
        }
    }

    /** Health trend summary for a specific calculator type */
    fun getTrendSummary(type: CalculatorType): Flow<HealthTrendSummary?> {
        return historyRepository.getAllEntries().map { entries ->
            val typeEntries = entries.filter { it.calculatorKey == type.key }
                .sortedBy { it.timestamp }
            
            if (typeEntries.size < 2) return@map null

            val latest = typeEntries.last()
            val previous = typeEntries[typeEntries.size - 2]
            
            val latestValue = latest.resultValue.toDoubleOrNull() ?: return@map null
            val previousValue = previous.resultValue.toDoubleOrNull() ?: return@map null
            
            val change = ((latestValue - previousValue) / previousValue.coerceAtLeast(0.1)) * 100
            val direction = when {
                abs(change) < 1.0 -> TrendDirection.STABLE
                change > 0 -> if (isIncreaseGood(type)) TrendDirection.IMPROVING else TrendDirection.DECLINING
                else -> if (!isIncreaseGood(type)) TrendDirection.IMPROVING else TrendDirection.DECLINING
            }

            HealthTrendSummary(
                title = "${type.displayName} Trend",
                currentStatus = "${latest.resultValue} ${latest.resultLabel ?: ""}",
                changePercentage = change.toFloat(),
                direction = direction,
                insight = generateInsight(type, latestValue, change),
                recommendation = generateRecommendation(type, latestValue, direction)
            )
        }
    }

    /** Get heatmap data for the last 6 months */
    fun getHeatmapData(): Flow<List<HeatmapMonth>> {
        return historyRepository.getAllEntries().map { entries ->
            val entryCounts = entries.groupBy { getStartOfDay(it.timestamp) }
                .mapValues { it.value.size }
            
            val months = mutableListOf<HeatmapMonth>()
            val calendar = Calendar.getInstance()
            
            // Generate last 6 months
            for (i in 0 until 6) {
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR)
                val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
                
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val monthDays = mutableListOf<HeatmapDay>()
                
                val monthCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                for (day in 1..daysInMonth) {
                    monthCal.set(Calendar.DAY_OF_MONTH, day)
                    val timestamp = monthCal.timeInMillis
                    val count = entryCounts[timestamp] ?: 0
                    monthDays.add(HeatmapDay(
                        dayOfMonth = day,
                        date = timestamp,
                        intensity = (count.toFloat() / 5).coerceAtMost(1.0f), // max intensity at 5 calculations
                        count = count
                    ))
                }
                
                months.add(0, HeatmapMonth(monthName, year, monthDays))
                calendar.add(Calendar.MONTH, -1)
            }
            months
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun calculateCurrentStreak(distinctDays: List<Long>): Int {
        if (distinctDays.isEmpty()) return 0
        val sortedDays = distinctDays.sortedDescending()
        val today = getStartOfDay(System.currentTimeMillis())
        val yesterday = today - (24 * 60 * 60 * 1000)
        
        if (sortedDays[0] < yesterday) return 0
        
        var streak = 0
        var current = if (sortedDays[0] == today) today else sortedDays[0]
        
        for (day in sortedDays) {
            if (day == current) {
                streak++
                current -= (24 * 60 * 60 * 1000)
            } else if (day < current) {
                break
            }
        }
        return streak
    }

    private fun calculateLongestStreak(distinctDays: List<Long>): Int {
        if (distinctDays.isEmpty()) return 0
        val sortedDays = distinctDays.sorted()
        var maxStreak = 0
        var currentStreak = 1
        
        for (i in 1 until sortedDays.size) {
            if (sortedDays[i] == sortedDays[i-1] + (24 * 60 * 60 * 1000)) {
                currentStreak++
            } else {
                maxStreak = maxOf(maxStreak, currentStreak)
                currentStreak = 1
            }
        }
        return maxOf(maxStreak, currentStreak)
    }

    private fun isIncreaseGood(type: CalculatorType): Boolean {
        return when (type) {
            CalculatorType.BMI, CalculatorType.WHR -> false
            CalculatorType.WATER_INTAKE -> true
            else -> false
        }
    }

    private fun generateInsight(type: CalculatorType, value: Double, change: Double): String {
        return when (type) {
            CalculatorType.BMI -> {
                if (change > 0) "Your BMI has increased by ${String.format("%.1f", abs(change))}%."
                else "Your BMI has decreased by ${String.format("%.1f", abs(change))}%."
            }
            CalculatorType.WATER_INTAKE -> {
                if (change > 0) "Great progress! You drank ${String.format("%.1f", abs(change))}% more water."
                else "You drank ${String.format("%.1f", abs(change))}% less water than before."
            }
            else -> "Your ${type.displayName} is ${if (change > 0) "up" else "down"} by ${String.format("%.1f", abs(change))}%."
        }
    }

    private fun generateRecommendation(type: CalculatorType, value: Double, direction: TrendDirection): String {
        if (direction == TrendDirection.IMPROVING) return "Keep up the excellent work! You're on the right track."
        
        return when (type) {
            CalculatorType.BMI -> {
                if (value > 25.0) "Consider a balanced diet and regular exercise to reach your target weight."
                else "Focus on nutritious meals and healthy weight maintenance."
            }
            CalculatorType.WATER_INTAKE -> "Try setting reminders to drink a glass of water every hour."
            else -> "Continue tracking and consult a professional for personalized advice."
        }
    }
}
