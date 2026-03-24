// widget/WaterWidgetDataProvider.kt
package com.health.calculator.bmi.tracker.widget

import android.content.Context
import android.content.SharedPreferences
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Provides water tracking data for widgets and quick access from home card.
 * Uses caching for instant widget loading.
 */
class WaterWidgetDataProvider(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("water_widget_prefs", Context.MODE_PRIVATE)

    /**
     * Get cached widget data (fast, for instant widget display)
     */
    fun getCachedData(): WaterWidgetData {
        return WaterWidgetData(
            currentMl = prefs.getInt(KEY_CURRENT_ML, 0),
            goalMl = prefs.getInt(KEY_GOAL_ML, 2500),
            percentage = prefs.getFloat(KEY_PERCENTAGE, 0f),
            streakDays = prefs.getInt(KEY_STREAK, 0),
            lastUpdateTime = prefs.getLong(KEY_LAST_UPDATE, 0L),
            isGoalMet = prefs.getBoolean(KEY_GOAL_MET, false)
        )
    }

    /**
     * Refresh widget data from database (call from background/coroutine)
     */
    suspend fun refreshData(): WaterWidgetData = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val dao = db.waterIntakeDao()

            // Get today's time boundaries
            val today = Calendar.getInstance()
            val startOfDay = today.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            // Get today's total water intake
            val currentMl = dao.getTotalWaterForDaySync(startOfDay, endOfDay) ?: 0

            // Get goal from preferences
            val goalPrefs = context.getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
            val goalMl = goalPrefs.getInt("daily_goal_ml", 2500)

            // Get streak from gamification data
            val streakDays = try {
                val streakData = db.waterGamificationDao().getStreakData()
                streakData?.currentStreak ?: 0
            } catch (e: Exception) {
                0
            }

            val percentage = if (goalMl > 0) (currentMl.toFloat() / goalMl * 100) else 0f
            val isGoalMet = currentMl >= goalMl

            val data = WaterWidgetData(
                currentMl = currentMl,
                goalMl = goalMl,
                percentage = percentage,
                streakDays = streakDays,
                lastUpdateTime = System.currentTimeMillis(),
                isGoalMet = isGoalMet
            )

            // Cache data for quick widget access
            cacheData(data)

            data
        } catch (e: Exception) {
            // Return cached data on error
            getCachedData()
        }
    }

    /**
     * Quick add water from widget or home card
     */
    suspend fun quickAddWater(amountMl: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val dao = db.waterIntakeDao()

            // Insert new water log
            dao.insertWaterLog(
                WaterIntakeLog(
                    amountMl = amountMl,
                    note = "Quick add from widget"
                )
            )

            // Update last log time for smart reminders
            context.getSharedPreferences("water_reminder_prefs", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_log_time", System.currentTimeMillis())
                .apply()

            // Refresh cached data
            refreshData()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Cache data for instant widget loading
     */
    private fun cacheData(data: WaterWidgetData) {
        prefs.edit()
            .putInt(KEY_CURRENT_ML, data.currentMl)
            .putInt(KEY_GOAL_ML, data.goalMl)
            .putFloat(KEY_PERCENTAGE, data.percentage)
            .putInt(KEY_STREAK, data.streakDays)
            .putLong(KEY_LAST_UPDATE, data.lastUpdateTime)
            .putBoolean(KEY_GOAL_MET, data.isGoalMet)
            .apply()
    }

    companion object {
        private const val KEY_CURRENT_ML = "widget_current_ml"
        private const val KEY_GOAL_ML = "widget_goal_ml"
        private const val KEY_PERCENTAGE = "widget_percentage"
        private const val KEY_STREAK = "widget_streak"
        private const val KEY_LAST_UPDATE = "widget_last_update"
        private const val KEY_GOAL_MET = "widget_goal_met"
    }
}

/**
 * Data class for widget display
 */
data class WaterWidgetData(
    val currentMl: Int,
    val goalMl: Int,
    val percentage: Float,
    val streakDays: Int,
    val lastUpdateTime: Long,
    val isGoalMet: Boolean
) {
    val currentLiters: Float get() = currentMl / 1000f
    val goalLiters: Float get() = goalMl / 1000f
    val remainingMl: Int get() = (goalMl - currentMl).coerceAtLeast(0)
}
