package com.health.calculator.bmi.tracker.data

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import java.text.SimpleDateFormat
import java.util.*

class WaterWidgetRepository private constructor(@ApplicationContext context: Context) {

    private val applicationContext = context.applicationContext

    companion object {
        private const val PREFS_NAME = "water_widget_prefs"
        private const val KEY_TODAY_INTAKE_ML = "today_intake_ml"
        private const val KEY_GOAL_ML = "goal_ml"
        private const val KEY_GLASSES_COUNT = "glasses_count"
        private const val KEY_LAST_LOGGED = "last_logged_time"
        private const val KEY_LAST_DATE = "last_date"
        private const val DEFAULT_GOAL_ML = 2500

        // Singleton for cross-process access
        @Volatile
        private var instance: WaterWidgetRepository? = null

        fun getInstance(@ApplicationContext context: Context): WaterWidgetRepository {
            return instance ?: synchronized(this) {
                instance ?: WaterWidgetRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val prefs: SharedPreferences
        get() = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ─── Data Classes ────────────────────────────────────────────────

    data class WaterWidgetData(
        val todayIntakeMl: Int,
        val goalMl: Int,
        val glassesCount: Int,
        val lastLoggedTime: String,
        val percentage: Int,
        val intakeFormatted: String,
        val goalFormatted: String
    )

    // ─── Check & Reset for Midnight ──────────────────────────────────

    private fun checkAndResetForNewDay() {
        val today = getTodayDate()
        val lastDate = prefs.getString(KEY_LAST_DATE, "")

        if (lastDate != today) {
            prefs.edit {
                putInt(KEY_TODAY_INTAKE_ML, 0)
                putInt(KEY_GLASSES_COUNT, 0)
                putString(KEY_LAST_LOGGED, "")
                putString(KEY_LAST_DATE, today)
            }
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // ─── Get Widget Data ─────────────────────────────────────────────

    fun getWidgetData(): WaterWidgetData {
        checkAndResetForNewDay()

        val intakeMl = prefs.getInt(KEY_TODAY_INTAKE_ML, 0)
        val goalMl = prefs.getInt(KEY_GOAL_ML, DEFAULT_GOAL_ML)
        val glasses = prefs.getInt(KEY_GLASSES_COUNT, 0)
        val lastLogged = prefs.getString(KEY_LAST_LOGGED, "") ?: ""

        val percentage = if (goalMl > 0) {
            ((intakeMl.toFloat() / goalMl.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else 0

        return WaterWidgetData(
            todayIntakeMl = intakeMl,
            goalMl = goalMl,
            glassesCount = glasses,
            lastLoggedTime = lastLogged,
            percentage = percentage,
            intakeFormatted = formatMilliliters(intakeMl),
            goalFormatted = formatMilliliters(goalMl)
        )
    }

    // ─── Add Water ───────────────────────────────────────────────────

    /**
     * Adds water to Room and the widget cache as one awaited operation.
     * Broadcast receivers must not start detached work that can be killed when
     * onReceive returns.
     */
    suspend fun addWaterPersisted(amountMl: Int): WaterWidgetData {
        require(amountMl in 1..2_000) { "Water amount must be between 1 and 2000 ml" }
        checkAndResetForNewDay()

        val db = AppDatabase.getDatabase(applicationContext)
        db.waterIntakeDao().insertWaterLog(
            WaterIntakeLog(
                amountMl = amountMl,
                timestamp = System.currentTimeMillis(),
                note = "Quick add from widget"
            )
        )

        val currentIntake = prefs.getInt(KEY_TODAY_INTAKE_ML, 0)
        val currentGlasses = prefs.getInt(KEY_GLASSES_COUNT, 0)
        prefs.edit {
            putInt(KEY_TODAY_INTAKE_ML, currentIntake + amountMl)
            putInt(KEY_GLASSES_COUNT, currentGlasses + 1)
            putString(KEY_LAST_LOGGED, getCurrentTime())
            putString(KEY_LAST_DATE, getTodayDate())
        }

        return getWidgetData()
    }

    // ─── Sync from App ───────────────────────────────────────────────

    fun syncFromApp(intakeMl: Int, glassesCount: Int, goalMl: Int) {
        prefs.edit {
            putInt(KEY_TODAY_INTAKE_ML, intakeMl)
            putInt(KEY_GLASSES_COUNT, glassesCount)
            putInt(KEY_GOAL_ML, goalMl)
            putString(KEY_LAST_DATE, getTodayDate())
            if (intakeMl > 0) {
                val lastLogged = prefs.getString(KEY_LAST_LOGGED, "")
                if (lastLogged.isNullOrEmpty()) {
                    putString(KEY_LAST_LOGGED, getCurrentTime())
                }
            }
        }
    }

    fun updateGoal(goalMl: Int) {
        prefs.edit { putInt(KEY_GOAL_ML, goalMl) }
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private fun formatMilliliters(ml: Int): String {
        return when {
            ml >= 1000 -> {
                val liters = ml / 1000f
                if (liters == liters.toLong().toFloat()) {
                    "${liters.toLong()}L"
                } else {
                    String.format(Locale.getDefault(), "%.1fL", liters)
                }
            }
            else -> "${ml}ml"
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date())
    }


}

