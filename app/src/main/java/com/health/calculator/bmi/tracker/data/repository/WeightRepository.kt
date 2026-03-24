package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.local.dao.WeightDao
import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.flow.*
import java.util.Calendar

class WeightRepository(private val weightDao: WeightDao) {

    fun getAllWeights(): Flow<List<WeightEntry>> = weightDao.getAllWeights()

    fun getLatestWeight(): Flow<WeightEntry?> = weightDao.getLatestWeight()

    fun getFirstWeight(): Flow<WeightEntry?> = weightDao.getFirstWeight()

    fun getWeightsSince(startMillis: Long): Flow<List<WeightEntry>> =
        weightDao.getWeightsSince(startMillis)

    fun getWeightsInRange(startMillis: Long, endMillis: Long): Flow<List<WeightEntry>> =
        weightDao.getWeightsInRange(startMillis, endMillis)

    suspend fun logWeight(
        weightKg: Double,
        dateMillis: Long = System.currentTimeMillis(),
        note: String? = null,
        source: WeightSource = WeightSource.MANUAL
    ): Long {
        val entry = WeightEntry(
            weightKg = weightKg,
            dateMillis = dateMillis,
            note = note,
            source = source
        )
        return weightDao.insertWeight(entry)
    }

    suspend fun deleteEntry(entry: WeightEntry) = weightDao.deleteWeight(entry)

    suspend fun deleteById(id: Long) = weightDao.deleteById(id)

    suspend fun deleteAll() = weightDao.deleteAll()

    fun getWeightStatistics(): Flow<WeightStatistics> {
        return combine(
            weightDao.getAllWeights(),
            weightDao.getLowestWeight(),
            weightDao.getHighestWeight(),
            weightDao.getEntryCount()
        ) { allWeights, lowest, highest, count ->
            if (allWeights.isEmpty()) {
                return@combine WeightStatistics()
            }

            val sorted = allWeights.sortedBy { it.dateMillis }
            val latest = sorted.last()
            val first = sorted.first()
            val totalChange = latest.weightKg - first.weightKg

            val weeklyChange = calculateAverageWeeklyChange(sorted)
            val trend = when {
                weeklyChange == null || kotlin.math.abs(weeklyChange) < 0.1 -> WeightTrendDirection.STABLE
                weeklyChange < 0 -> WeightTrendDirection.LOSING
                else -> WeightTrendDirection.GAINING
            }

            WeightStatistics(
                currentWeight = latest.weightKg,
                startingWeight = first.weightKg,
                lowestWeight = lowest,
                highestWeight = highest,
                totalChange = totalChange,
                averageWeeklyChange = weeklyChange,
                trendDirection = trend,
                totalEntries = count,
                firstEntryDate = first.dateMillis,
                latestEntryDate = latest.dateMillis
            )
        }
    }

    fun getGoalProgress(goalWeightKg: Double): Flow<WeightGoalProgress?> {
        return combine(
            weightDao.getAllWeights(),
            weightDao.getLatestWeight()
        ) { allWeights, latest ->
            if (allWeights.isEmpty() || latest == null) return@combine null

            val sorted = allWeights.sortedBy { it.dateMillis }
            val first = sorted.first()
            val isGaining = goalWeightKg > first.weightKg

            val totalToChange = kotlin.math.abs(goalWeightKg - first.weightKg)
            val remaining = kotlin.math.abs(goalWeightKg - latest.weightKg)
            val progress = if (totalToChange > 0) {
                ((totalToChange - remaining) / totalToChange).coerceIn(0.0, 1.0)
            } else 1.0

            val weeklyChange = calculateAverageWeeklyChange(sorted)
            val estimatedDays = if (weeklyChange != null && weeklyChange != 0.0) {
                val weeksRemaining = remaining / kotlin.math.abs(weeklyChange)
                (weeksRemaining * 7).toInt()
            } else null

            val estimatedDate = estimatedDays?.let {
                System.currentTimeMillis() + (it.toLong() * 24 * 60 * 60 * 1000)
            }

            val isReached = if (isGaining) {
                latest.weightKg >= goalWeightKg
            } else {
                latest.weightKg <= goalWeightKg
            }

            WeightGoalProgress(
                currentWeight = latest.weightKg,
                goalWeight = goalWeightKg,
                startingWeight = first.weightKg,
                totalToLoseOrGain = totalToChange,
                remainingToGoal = remaining,
                percentageComplete = progress.toFloat(),
                isGainingGoal = isGaining,
                estimatedDaysRemaining = estimatedDays,
                estimatedCompletionDate = estimatedDate,
                isGoalReached = isReached,
                averageWeeklyChange = weeklyChange
            )
        }
    }

    private fun calculateAverageWeeklyChange(sorted: List<WeightEntry>): Double? {
        if (sorted.size < 2) return null

        val first = sorted.first()
        val last = sorted.last()
        val daysDiff = (last.dateMillis - first.dateMillis).toDouble() / (1000 * 60 * 60 * 24)

        if (daysDiff < 1) return null

        val totalChange = last.weightKg - first.weightKg
        val weeks = daysDiff / 7.0

        return if (weeks > 0) totalChange / weeks else null
    }

    fun getFilteredWeights(timeFilter: WeightTimeFilter): Flow<List<WeightEntry>> {
        val now = System.currentTimeMillis()
        val startMillis = when (timeFilter) {
            WeightTimeFilter.SEVEN_DAYS -> now - 7L * 24 * 60 * 60 * 1000
            WeightTimeFilter.THIRTY_DAYS -> now - 30L * 24 * 60 * 60 * 1000
            WeightTimeFilter.NINETY_DAYS -> now - 90L * 24 * 60 * 60 * 1000
            WeightTimeFilter.ONE_YEAR -> now - 365L * 24 * 60 * 60 * 1000
            WeightTimeFilter.ALL_TIME -> 0L
        }
        return weightDao.getWeightsSince(startMillis)
    }
}

enum class WeightTimeFilter(val label: String) {
    SEVEN_DAYS("7 Days"),
    THIRTY_DAYS("30 Days"),
    NINETY_DAYS("90 Days"),
    ONE_YEAR("1 Year"),
    ALL_TIME("All")
}
