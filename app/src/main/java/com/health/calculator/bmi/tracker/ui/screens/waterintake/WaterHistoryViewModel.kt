// ui/screens/waterintake/WaterHistoryViewModel.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DailyWaterData(
    val date: String,
    val totalMl: Int,
    val entries: List<WaterIntakeLog>
)

data class SelectedDayInfo(
    val date: Date,
    val totalMl: Int,
    val entries: List<WaterIntakeLog>
)

data class WaterStats(
    val averageDailyMl: Int,
    val daysGoalMet: Int,
    val goalMetPercentage: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val thisWeekTotalL: Float,
    val thisMonthTotalL: Float,
    val totalDaysTracked: Int,
    val bestDayMl: Int,
    val bestDayDate: String,
    val bestWeekTotalL: Float
)

data class WeeklyReport(
    val averageMl: Int,
    val daysGoalMet: Int,
    val totalLiters: Float,
    val trend: TrendDirection,
    val trendMessage: String,
    val motivationalMessage: String
)

enum class TrendDirection { IMPROVING, DECLINING, STEADY }

class WaterHistoryViewModel(
    application: Application,
    private val repository: WaterIntakeRepository
) : AndroidViewModel(application) {

    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    val dailyGoalMl: Int
        get() {
            val prefs = getApplication<Application>()
                .getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
            return prefs.getInt("daily_goal_ml", 2500)
        }

    var currentMonth by mutableStateOf(Calendar.getInstance())
        private set

    private val _dailyData = MutableStateFlow<Map<String, DailyWaterData>>(emptyMap())
    val dailyData: StateFlow<Map<String, DailyWaterData>> = _dailyData.asStateFlow()

    private val _selectedDayLogs = MutableStateFlow<SelectedDayInfo?>(null)
    val selectedDayLogs: StateFlow<SelectedDayInfo?> = _selectedDayLogs.asStateFlow()

    private val _stats = MutableStateFlow<WaterStats?>(null)
    val stats: StateFlow<WaterStats?> = _stats.asStateFlow()

    private val _weeklyReport = MutableStateFlow<WeeklyReport?>(null)
    val weeklyReport: StateFlow<WeeklyReport?> = _weeklyReport.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            repository.getAllWaterLogs().collect { allLogs ->
                // Group logs by date
                val grouped = allLogs.groupBy { log ->
                    dateKeyFormat.format(Date(log.timestamp))
                }

                val dailyMap = grouped.map { (dateKey, logs) ->
                    dateKey to DailyWaterData(
                        date = dateKey,
                        totalMl = logs.sumOf { it.amountMl },
                        entries = logs.sortedBy { it.timestamp }
                    )
                }.toMap()

                _dailyData.value = dailyMap

                // Calculate stats
                calculateStats(dailyMap)

                // Calculate weekly report
                calculateWeeklyReport(dailyMap)
            }
        }
    }

    fun selectDay(calendar: Calendar) {
        val dateKey = dateKeyFormat.format(calendar.time)
        val data = _dailyData.value[dateKey]

        _selectedDayLogs.value = SelectedDayInfo(
            date = calendar.time,
            totalMl = data?.totalMl ?: 0,
            entries = data?.entries ?: emptyList()
        )
    }

    fun changeMonth(delta: Int) {
        val newMonth = currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, delta)
        currentMonth = newMonth
    }

    private fun calculateStats(dailyMap: Map<String, DailyWaterData>) {
        if (dailyMap.isEmpty()) {
            _stats.value = null
            return
        }

        val goalMl = dailyGoalMl
        val activeDays = dailyMap.values.filter { it.totalMl > 0 }

        if (activeDays.isEmpty()) {
            _stats.value = null
            return
        }

        // Average daily
        val avgDaily = activeDays.sumOf { it.totalMl } / activeDays.size

        // Days goal met
        val daysGoalMet = activeDays.count { it.totalMl >= goalMl }
        val goalMetPct = if (activeDays.isNotEmpty()) (daysGoalMet * 100) / activeDays.size else 0

        // Streaks
        val sortedDates = activeDays.map { it.date }.sorted()
        val currentStreak = calculateCurrentStreak(dailyMap, goalMl)
        val longestStreak = calculateLongestStreak(dailyMap, goalMl)

        // This week total
        val thisWeekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val thisWeekTotal = activeDays.filter { day ->
            try {
                val date = dateKeyFormat.parse(day.date)
                date != null && !date.before(thisWeekStart.time)
            } catch (e: Exception) { false }
        }.sumOf { it.totalMl }

        // This month total
        val thisMonthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val thisMonthTotal = activeDays.filter { day ->
            try {
                val date = dateKeyFormat.parse(day.date)
                date != null && !date.before(thisMonthStart.time)
            } catch (e: Exception) { false }
        }.sumOf { it.totalMl }

        // Best day
        val bestDay = activeDays.maxByOrNull { it.totalMl }
        val bestDayDate = bestDay?.let {
            try {
                val date = dateKeyFormat.parse(it.date)
                if (date != null) displayDateFormat.format(date) else it.date
            } catch (e: Exception) { it.date }
        } ?: ""

        // Best week
        val bestWeekTotal = calculateBestWeekTotal(dailyMap)

        _stats.value = WaterStats(
            averageDailyMl = avgDaily,
            daysGoalMet = daysGoalMet,
            goalMetPercentage = goalMetPct,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            thisWeekTotalL = thisWeekTotal / 1000f,
            thisMonthTotalL = thisMonthTotal / 1000f,
            totalDaysTracked = activeDays.size,
            bestDayMl = bestDay?.totalMl ?: 0,
            bestDayDate = bestDayDate,
            bestWeekTotalL = bestWeekTotal / 1000f
        )
    }

    private fun calculateCurrentStreak(dailyMap: Map<String, DailyWaterData>, goalMl: Int): Int {
        var streak = 0
        val cal = Calendar.getInstance()

        // Start from today or yesterday
        val todayKey = dateKeyFormat.format(cal.time)
        val todayData = dailyMap[todayKey]
        if (todayData == null || todayData.totalMl < goalMl) {
            // Check if yesterday counts
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val key = dateKeyFormat.format(cal.time)
            val data = dailyMap[key]
            if (data != null && data.totalMl >= goalMl) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateLongestStreak(dailyMap: Map<String, DailyWaterData>, goalMl: Int): Int {
        if (dailyMap.isEmpty()) return 0

        val sortedDates = dailyMap.keys.sorted()
        var longest = 0
        var current = 0

        val cal = Calendar.getInstance()
        val firstDate = dateKeyFormat.parse(sortedDates.first()) ?: return 0
        val lastDate = dateKeyFormat.parse(sortedDates.last()) ?: return 0

        cal.time = firstDate
        while (!cal.time.after(lastDate)) {
            val key = dateKeyFormat.format(cal.time)
            val data = dailyMap[key]
            if (data != null && data.totalMl >= goalMl) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 0
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return longest
    }

    private fun calculateBestWeekTotal(dailyMap: Map<String, DailyWaterData>): Int {
        if (dailyMap.isEmpty()) return 0

        val sortedDates = dailyMap.keys.sorted()
        var bestWeek = 0

        // Sliding window of 7 days
        val cal = Calendar.getInstance()
        val firstDate = dateKeyFormat.parse(sortedDates.first()) ?: return 0
        val lastDate = dateKeyFormat.parse(sortedDates.last()) ?: return 0

        cal.time = firstDate
        while (!cal.time.after(lastDate)) {
            var weekTotal = 0
            val weekCal = cal.clone() as Calendar
            for (d in 0 until 7) {
                val key = dateKeyFormat.format(weekCal.time)
                weekTotal += dailyMap[key]?.totalMl ?: 0
                weekCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            bestWeek = maxOf(bestWeek, weekTotal)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return bestWeek
    }

    private fun calculateWeeklyReport(dailyMap: Map<String, DailyWaterData>) {
        val goalMl = dailyGoalMl
        val cal = Calendar.getInstance()

        // This week's data
        val thisWeekData = mutableListOf<Int>()
        val lastWeekData = mutableListOf<Int>()

        for (i in 6 downTo 0) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val key = dateKeyFormat.format(dayCal.time)
            thisWeekData.add(dailyMap[key]?.totalMl ?: 0)
        }

        for (i in 13 downTo 7) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val key = dateKeyFormat.format(dayCal.time)
            lastWeekData.add(dailyMap[key]?.totalMl ?: 0)
        }

        val thisWeekActive = thisWeekData.filter { it > 0 }
        if (thisWeekActive.isEmpty()) {
            _weeklyReport.value = null
            return
        }

        val thisAvg = thisWeekActive.average().toInt()
        val lastAvg = lastWeekData.filter { it > 0 }.let { if (it.isNotEmpty()) it.average().toInt() else 0 }
        val daysGoalMet = thisWeekData.count { it >= goalMl }
        val totalMl = thisWeekData.sum()

        val trend = when {
            lastAvg == 0 -> TrendDirection.STEADY
            thisAvg > lastAvg + 100 -> TrendDirection.IMPROVING
            thisAvg < lastAvg - 100 -> TrendDirection.DECLINING
            else -> TrendDirection.STEADY
        }

        val trendMessage = when (trend) {
            TrendDirection.IMPROVING -> "Your intake is improving! Up ${thisAvg - lastAvg}ml from last week."
            TrendDirection.DECLINING -> "Your intake has decreased by ${lastAvg - thisAvg}ml from last week."
            TrendDirection.STEADY -> "Your intake is consistent with last week."
        }

        val motivationalMessage = when {
            daysGoalMet >= 6 -> "🌟 Outstanding week! You're a hydration champion! Keep this incredible momentum going!"
            daysGoalMet >= 4 -> "💪 Great effort this week! You're building a solid hydration habit. A few more days to perfection!"
            daysGoalMet >= 2 -> "👍 Good start! Try to hit your goal more consistently. Every glass counts!"
            daysGoalMet >= 1 -> "🌱 You're getting started! Set reminders to help you remember to drink throughout the day."
            else -> "💧 This week is a fresh start! Try the quick-add buttons to make tracking easy and fun."
        }

        _weeklyReport.value = WeeklyReport(
            averageMl = thisAvg,
            daysGoalMet = daysGoalMet,
            totalLiters = totalMl / 1000f,
            trend = trend,
            trendMessage = trendMessage,
            motivationalMessage = motivationalMessage
        )
    }
}
