package com.health.calculator.bmi.tracker.data

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WaterWidgetRepository private constructor(@ApplicationContext context: Context) {

    private val applicationContext = context.applicationContext
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

    fun addWater(amountMl: Int): WaterWidgetData {
        checkAndResetForNewDay()

        val currentIntake = prefs.getInt(KEY_TODAY_INTAKE_ML, 0)
        val currentGlasses = prefs.getInt(KEY_GLASSES_COUNT, 0)
        val newIntake = currentIntake + amountMl
        val newGlasses = currentGlasses + 1
        val currentTime = getCurrentTime()

        prefs.edit {
            putInt(KEY_TODAY_INTAKE_ML, newIntake)
            putInt(KEY_GLASSES_COUNT, newGlasses)
            putString(KEY_LAST_LOGGED, currentTime)
        }

        // Also sync with Room database
        syncToDatabase(amountMl)

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

    private fun syncToDatabase(amountMl: Int) {
        // Fire-and-forget sync to Room DB via coroutine
        repositoryScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val log = WaterIntakeLog(
                    amountMl = amountMl,
                    timestamp = System.currentTimeMillis(),
                    note = "Quick add from widget"
                )
                db.waterIntakeDao().insertWaterLog(log)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
