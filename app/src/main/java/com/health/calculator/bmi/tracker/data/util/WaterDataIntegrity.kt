// data/util/WaterDataIntegrity.kt
package com.health.calculator.bmi.tracker.data.util

import android.content.Context
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.WaterStreakData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Handles water tracking data integrity:
 * - Midnight reset logic
 * - Streak calculations across timezones
 * - Historical data preservation
 */
class WaterDataIntegrity(private val context: Context) {

    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val prefs = context.getSharedPreferences("water_data_integrity", Context.MODE_PRIVATE)

    /**
     * Call on app start and periodically to ensure data integrity
     */
    suspend fun performIntegrityCheck() = withContext(Dispatchers.IO) {
        checkDayRollover()
        validateStreakData()
        cleanupOldCacheData()
    }

    /**
     * Check if day has rolled over and perform necessary updates
     */
    private suspend fun checkDayRollover() {
        val lastCheckedDate = prefs.getString(KEY_LAST_CHECK_DATE, "") ?: ""
        val todayDate = dateKeyFormat.format(Date())

        if (lastCheckedDate != todayDate) {
            // Day has changed - perform rollover
            if (lastCheckedDate.isNotEmpty()) {
                onDayRollover(lastCheckedDate, todayDate)
            }

            // Update last checked date
            prefs.edit()
                .putString(KEY_LAST_CHECK_DATE, todayDate)
                .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
                .apply()
        }
    }

    /**
     * Called when day changes (midnight reset)
     */
    private suspend fun onDayRollover(previousDate: String, newDate: String) {
        val db = AppDatabase.getDatabase(context)
        val gamificationDao = db.waterGamificationDao()

        // Get yesterday's total
        val yesterdayTotal = getYesterdayTotal()
        val goalMl = context.getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
            .getInt("daily_goal_ml", 2500)

        val metGoalYesterday = yesterdayTotal >= goalMl

        // Update streak based on yesterday's performance
        val existingStreak = gamificationDao.getStreakData() ?: WaterStreakData()

        val updatedStreak = if (metGoalYesterday) {
            // Goal was met - streak continues or increments
            existingStreak.copy(
                totalDaysTracked = existingStreak.totalDaysTracked + 1,
                totalDaysGoalMet = existingStreak.totalDaysGoalMet + 1
            )
        } else if (existingStreak.lastGoalMetDate != previousDate && existingStreak.currentStreak > 0) {
            // Goal not met and it wasn't already counted - break streak
            existingStreak.copy(
                currentStreak = 0,
                consecutiveAGrades = 0
            )
        } else {
            existingStreak
        }

        gamificationDao.updateStreakData(updatedStreak)

        // Save yesterday's summary for history
        saveYesterdaySummary(previousDate, yesterdayTotal, goalMl, metGoalYesterday)

        // Refresh widget cache
        try {
            com.health.calculator.bmi.tracker.widget.WaterWidgetDataProvider(context).refreshData()
        } catch (e: Exception) {
            // Widget not available
        }
    }

    /**
     * Get yesterday's total water intake
     */
    private suspend fun getYesterdayTotal(): Int {
        val db = AppDatabase.getDatabase(context)
        val dao = db.waterIntakeDao()

        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        val startOfYesterday = (yesterday.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfYesterday = (yesterday.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return dao.getTotalWaterForDaySync(startOfYesterday, endOfYesterday) ?: 0
    }

    /**
     * Save yesterday's summary for historical reference
     */
    private fun saveYesterdaySummary(date: String, totalMl: Int, goalMl: Int, metGoal: Boolean) {
        val summaryPrefs = context.getSharedPreferences("water_daily_summaries", Context.MODE_PRIVATE)
        summaryPrefs.edit()
            .putInt("${date}_total", totalMl)
            .putInt("${date}_goal", goalMl)
            .putBoolean("${date}_met", metGoal)
            .putLong("${date}_saved_at", System.currentTimeMillis())
            .apply()
    }

    /**
     * Validate and fix streak data across timezone changes
     */
    private suspend fun validateStreakData() {
        val db = AppDatabase.getDatabase(context)
        val gamificationDao = db.waterGamificationDao()
        val waterDao = db.waterIntakeDao()

        val existingStreak = gamificationDao.getStreakData() ?: return

        // Recalculate streak from actual log data
        val actualStreak = calculateActualStreak(waterDao)

        if (actualStreak != existingStreak.currentStreak) {
            // Fix discrepancy
            val correctedStreak = existingStreak.copy(
                currentStreak = actualStreak,
                longestStreak = maxOf(existingStreak.longestStreak, actualStreak)
            )
            gamificationDao.updateStreakData(correctedStreak)
        }
    }

    /**
     * Calculate actual streak by checking consecutive days
     */
    private suspend fun calculateActualStreak(
        waterDao: com.health.calculator.bmi.tracker.data.dao.WaterIntakeDao
    ): Int {
        val goalMl = context.getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
            .getInt("daily_goal_ml", 2500)

        var streak = 0
        val cal = Calendar.getInstance()

        // Check if today counts
        val todayTotal = getTotalForDate(waterDao, cal)
        if (todayTotal < goalMl) {
            // Today doesn't count yet, start from yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        // Count consecutive days meeting goal
        while (true) {
            val dayTotal = getTotalForDate(waterDao, cal)
            if (dayTotal >= goalMl) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)

                // Safety limit to prevent infinite loops
                if (streak > 1000) break
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Get total water intake for a specific date
     */
    private suspend fun getTotalForDate(
        dao: com.health.calculator.bmi.tracker.data.dao.WaterIntakeDao,
        cal: Calendar
    ): Int {
        val startOfDay = (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return dao.getTotalWaterForDaySync(startOfDay, endOfDay) ?: 0
    }

    /**
     * Clean up old cache data to prevent storage bloat (keep last 90 days)
     */
    private fun cleanupOldCacheData() {
        val summaryPrefs = context.getSharedPreferences("water_daily_summaries", Context.MODE_PRIVATE)
        val editor = summaryPrefs.edit()

        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -90)
        }
        val cutoffDateKey = dateKeyFormat.format(cutoffDate.time)

        val allKeys = summaryPrefs.all.keys.toList()
        allKeys.forEach { key ->
            val dateFromKey = key.substringBefore("_")
            if (dateFromKey < cutoffDateKey && dateFromKey.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                editor.remove(key)
            }
        }

        editor.apply()
    }

    companion object {
        private const val KEY_LAST_CHECK_DATE = "last_check_date"
        private const val KEY_LAST_CHECK_TIME = "last_check_time"
    }
}
