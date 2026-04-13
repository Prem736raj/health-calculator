// ui/screens/waterintake/WaterTrackingViewModel.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository
import com.health.calculator.bmi.tracker.data.model.PlantMood
import com.health.calculator.bmi.tracker.data.model.PlantStage
import com.health.calculator.bmi.tracker.data.model.PlantState
import com.health.calculator.bmi.tracker.data.preferences.PlantPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository as MainHistoryRepository
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryEntry
import org.json.JSONObject
import java.util.Calendar
import com.health.calculator.bmi.tracker.widget.WaterWidgetSyncManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first

class WaterTrackingViewModel(
    application: Application,
    private val repository: WaterIntakeRepository
) : AndroidViewModel(application) {

    private val mainHistoryRepository = MainHistoryRepository(
        AppDatabase.getDatabase(application).historyDao()
    )

    // Daily goal from saved preferences
    val dailyGoalMl: Int
        get() {
            val prefs = getApplication<Application>()
                .getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
            return prefs.getInt("daily_goal_ml", 2500)
        }

    // Today's time boundaries
    private val todayStart: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

    private val todayEnd: Long
        get() {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return cal.timeInMillis
        }

    // Today's water logs (newest first)
    val todayLogs: Flow<List<WaterIntakeLog>> =
        repository.getWaterLogsForDay(todayStart, todayEnd)

    // Today's total
    val todayTotal: Flow<Int> =
        repository.getTotalWaterForDay(todayStart, todayEnd)
            .map { it ?: 0 }

    private val plantPrefs by lazy { PlantPreferences(application) }

    val isPlantVisible: Boolean
        get() = plantPrefs.isPlantVisible

    val plantName: String
        get() = plantPrefs.plantName

    fun getPlantState(currentMl: Int, goalMl: Int, streakDays: Int): PlantState {
        val totalDays = plantPrefs.totalDaysTracked
        val percentage = if (goalMl > 0) currentMl.toFloat() / goalMl else 0f

        val stage = when {
            totalDays >= 180 -> PlantStage.FULL_BLOOM
            totalDays >= 90 -> PlantStage.FLOWERING
            totalDays >= 30 -> PlantStage.GROWING_PLANT
            totalDays >= 7 -> PlantStage.SMALL_PLANT
            else -> PlantStage.SPROUT
        }

        val mood = when {
            percentage >= 1f -> PlantMood.THRIVING
            percentage >= 0.75f -> PlantMood.HAPPY
            percentage >= 0.5f -> PlantMood.NEUTRAL
            percentage >= 0.25f -> PlantMood.THIRSTY
            else -> PlantMood.WILTING
        }

        return PlantState(
            stage = stage,
            mood = mood,
            totalDaysTracked = totalDays,
            currentStreak = streakDays,
            todayPercentage = percentage,
            goalReachedToday = percentage >= 1f
        )
    }

    fun updatePlantTracking(dateKey: String) {
        if (plantPrefs.lastTrackedDate != dateKey) {
            plantPrefs.totalDaysTracked = plantPrefs.totalDaysTracked + 1
            plantPrefs.lastTrackedDate = dateKey
        }
    }

    fun setPlantVisible(visible: Boolean) {
        plantPrefs.isPlantVisible = visible
    }

    fun setPlantName(name: String) {
        plantPrefs.plantName = name
    }

    // Track "just watered" state
    private val _justWatered = MutableStateFlow(false)
    val justWatered: StateFlow<Boolean> = _justWatered.asStateFlow()

    // Last added entry for undo
    private var lastAddedId: Long? = null

    fun addWater(amountMl: Int, note: String = "") {
        viewModelScope.launch {
            val id = repository.logWater(amountMl, note)
            lastAddedId = id

            // Update last log time for smart reminder skip
            val prefs = getApplication<Application>()
                .getSharedPreferences("water_reminder_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putLong("last_log_time", System.currentTimeMillis())
                .apply()
                
            // Save to main unified history
            val detailsJson = JSONObject().apply {
                put("amountMl", amountMl)
                put("note", note)
                put("timestamp", System.currentTimeMillis())
            }

            val mainEntry = HistoryEntry(
                calculatorKey = CalculatorType.WATER_INTAKE.key,
                resultValue = amountMl.toString(),
                resultLabel = "ml",
                category = if (amountMl >= 250) "Glass" else "Sip",
                detailsJson = detailsJson.toString(),
                timestamp = System.currentTimeMillis()
            )
            mainHistoryRepository.addEntry(mainEntry)

            // Trigger plant animation
            _justWatered.value = true
            
            // Sync to widget
            syncToWidget()
            
            delay(1500)
            _justWatered.value = false
        }
    }

    private fun syncToWidget() {
        viewModelScope.launch {
            val total = todayTotal.first()
            val logs = todayLogs.first()
            WaterWidgetSyncManager.syncToWidgets(
                context = getApplication(),
                todayIntakeMl = total,
                glassesCount = logs.size,
                goalMl = dailyGoalMl
            )
        }
    }

    fun undoLastEntry() {
        viewModelScope.launch {
            lastAddedId?.let { id ->
                val entry = repository.getWaterLogById(id)
                if (entry != null) {
                    repository.deleteWaterLog(entry)
                    syncToWidget()
                }
                lastAddedId = null
            }
        }
    }

    fun deleteEntry(log: WaterIntakeLog) {
        viewModelScope.launch {
            repository.deleteWaterLog(log)
            syncToWidget()
        }
    }

    // Save yesterday's total to history (called at midnight or app start)
    fun checkAndSaveYesterdayData() {
        val prefs = getApplication<Application>()
            .getSharedPreferences("water_intake_prefs", Context.MODE_PRIVATE)
        val lastSavedDate = prefs.getLong("last_saved_date", 0L)

        val yesterdayStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterdayEnd = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        // Only save if we haven't already saved for yesterday
        if (lastSavedDate < yesterdayStart) {
            viewModelScope.launch {
                repository.getTotalWaterForDay(yesterdayStart, yesterdayEnd)
                    .collect { total ->
                        if (total != null && total > 0) {
                            // Save to history via the general history system
                            prefs.edit()
                                .putLong("last_saved_date", System.currentTimeMillis())
                                .putInt("yesterday_total_ml", total)
                                .apply()

                            // Save daily summary to main history (optional, maybe too much?)
                            // For now, let's just log every glass as we did in addWater
                        }
                        return@collect
                    }
            }
        }
    }
}
