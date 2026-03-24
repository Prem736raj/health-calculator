package com.health.calculator.bmi.tracker.data.repository

import android.content.Context
import com.health.calculator.bmi.tracker.data.model.DailyFoodLog
import com.health.calculator.bmi.tracker.data.model.FoodEntry
import com.health.calculator.bmi.tracker.data.model.FoodPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class FoodLogRepository(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("food_log", Context.MODE_PRIVATE)
    }
    private val presetPrefs by lazy {
        context.getSharedPreferences("food_presets", Context.MODE_PRIVATE)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayKey get() = dateFormat.format(Date())

    private val _todayLog = MutableStateFlow(emptyDailyLog())
    val todayLog: StateFlow<DailyFoodLog> = _todayLog.asStateFlow()

    private val _customPresets = MutableStateFlow<List<FoodPreset>>(emptyList())
    val customPresets: StateFlow<List<FoodPreset>> = _customPresets.asStateFlow()

    val defaultPresets = listOf(
        FoodPreset("rice_1cup", "Rice (1 cup, cooked)", 206.0, 4.3, 44.5, 0.4, "1 cup (186g)", "🍚"),
        FoodPreset("chicken_100g", "Chicken Breast (100g)", 165.0, 31.0, 0.0, 3.6, "100g", "🍗"),
        FoodPreset("egg_1", "Egg (1 large)", 72.0, 6.3, 0.4, 4.8, "1 large (50g)", "🥚"),
        FoodPreset("banana_1", "Banana (1 medium)", 105.0, 1.3, 27.0, 0.4, "1 medium (118g)", "🍌"),
        FoodPreset("bread_1slice", "Bread (1 slice)", 79.0, 3.0, 15.0, 1.0, "1 slice (30g)", "🍞"),
        FoodPreset("milk_1cup", "Milk (1 cup)", 149.0, 8.0, 12.0, 8.0, "1 cup (244ml)", "🥛"),
        FoodPreset("apple_1", "Apple (1 medium)", 95.0, 0.5, 25.0, 0.3, "1 medium (182g)", "🍎"),
        FoodPreset("oats_1cup", "Oatmeal (1 cup, cooked)", 166.0, 5.9, 28.1, 3.6, "1 cup (234g)", "🥣"),
        FoodPreset("salmon_100g", "Salmon (100g)", 208.0, 20.0, 0.0, 13.0, "100g", "🐟"),
        FoodPreset("broccoli_1cup", "Broccoli (1 cup)", 55.0, 3.7, 11.0, 0.6, "1 cup (91g)", "🥦"),
        FoodPreset("peanut_butter_1tbsp", "Peanut Butter (1 tbsp)", 95.0, 4.0, 3.0, 8.0, "1 tbsp (16g)", "🥜"),
        FoodPreset("greek_yogurt_100g", "Greek Yogurt (100g)", 59.0, 10.0, 3.6, 0.4, "100g", "🥛"),
        FoodPreset("sweet_potato_1", "Sweet Potato (medium)", 103.0, 2.3, 24.0, 0.1, "1 medium (130g)", "🍠"),
        FoodPreset("almonds_10", "Almonds (10 nuts)", 69.0, 2.6, 2.4, 6.0, "10 nuts (14g)", "🥜"),
        FoodPreset("pasta_1cup", "Pasta (1 cup, cooked)", 220.0, 8.0, 43.0, 1.3, "1 cup (140g)", "🍝")
    )

    init {
        loadTodayLog()
        loadCustomPresets()
    }

    fun addEntry(entry: FoodEntry) {
        val current = _todayLog.value
        val updatedEntries = current.entries + entry
        val updated = current.copy(entries = updatedEntries)
        _todayLog.value = updated
        saveTodayLog(updated)
    }

    fun removeEntry(entryId: Long) {
        val current = _todayLog.value
        val updatedEntries = current.entries.filter { it.id != entryId }
        val updated = current.copy(entries = updatedEntries)
        _todayLog.value = updated
        saveTodayLog(updated)
    }

    fun updateDailyTargets(
        targetCalories: Double,
        targetProtein: Double = 0.0,
        targetCarbs: Double = 0.0,
        targetFat: Double = 0.0
    ) {
        val updated = _todayLog.value.copy(
            targetCalories = targetCalories,
            targetProteinGrams = targetProtein,
            targetCarbGrams = targetCarbs,
            targetFatGrams = targetFat
        )
        _todayLog.value = updated
        saveTodayLog(updated)
    }

    fun addCustomPreset(preset: FoodPreset) {
        val current = _customPresets.value.toMutableList()
        current.add(preset)
        _customPresets.value = current
        saveCustomPresets(current)
    }

    fun removeCustomPreset(presetId: String) {
        val current = _customPresets.value.toMutableList()
        current.removeAll { it.id == presetId }
        _customPresets.value = current
        saveCustomPresets(current)
    }

    fun checkAndResetIfNewDay() {
        val savedDate = prefs.getString("log_date", "") ?: ""
        val today = todayKey
        if (savedDate != today) {
            // Save yesterday's log to history
            val yesterdayLog = _todayLog.value
            if (yesterdayLog.entries.isNotEmpty()) {
                saveToHistory(yesterdayLog, savedDate)
            }
            // Reset for today
            val fresh = emptyDailyLog()
            _todayLog.value = fresh
            saveTodayLog(fresh)
        }
    }

    fun getHistoricalLogs(): List<DailyFoodLog> {
        val json = prefs.getString("history_logs", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { emptyList() }
    }

    private fun loadTodayLog() {
        val savedDate = prefs.getString("log_date", "") ?: ""
        val today = todayKey
        if (savedDate == today) {
            val json = prefs.getString("today_log", null)
            if (json != null) {
                try {
                    _todayLog.value = fromJson(JSONObject(json))
                    return
                } catch (_: Exception) {}
            }
        }
        _todayLog.value = emptyDailyLog()
    }

    private fun loadCustomPresets() {
        val json = presetPrefs.getString("custom_presets", null) ?: return
        try {
            val arr = JSONArray(json)
            val list = mutableListOf<FoodPreset>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    FoodPreset(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        calories = o.getDouble("calories"),
                        proteinGrams = o.getDouble("protein"),
                        carbGrams = o.getDouble("carbs"),
                        fatGrams = o.getDouble("fat"),
                        servingSize = o.getString("serving"),
                        emoji = o.getString("emoji"),
                        isCustom = true
                    )
                )
            }
            _customPresets.value = list
        } catch (_: Exception) {}
    }

    private fun saveTodayLog(log: DailyFoodLog) {
        prefs.edit()
            .putString("log_date", todayKey)
            .putString("today_log", toJson(log).toString())
            .apply()
    }

    private fun saveCustomPresets(presets: List<FoodPreset>) {
        val arr = JSONArray()
        presets.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id); put("name", p.name); put("calories", p.calories)
                put("protein", p.proteinGrams); put("carbs", p.carbGrams); put("fat", p.fatGrams)
                put("serving", p.servingSize); put("emoji", p.emoji)
            })
        }
        presetPrefs.edit().putString("custom_presets", arr.toString()).apply()
    }

    private fun saveToHistory(log: DailyFoodLog, date: String) {
        val current = getHistoricalLogs().toMutableList()
        current.add(0, log.copy(date = date))
        val arr = JSONArray()
        current.take(90).forEach { arr.put(toJson(it)) }
        prefs.edit().putString("history_logs", arr.toString()).apply()
    }

    private fun emptyDailyLog(): DailyFoodLog {
        val targetCals = prefs.getFloat("target_calories", 2000f).toDouble()
        return DailyFoodLog(
            date = todayKey,
            entries = emptyList(),
            targetCalories = targetCals
        )
    }

    private fun toJson(log: DailyFoodLog): JSONObject {
        val entriesArr = JSONArray()
        log.entries.forEach { e ->
            entriesArr.put(JSONObject().apply {
                put("id", e.id); put("timestamp", e.timestamp)
                put("name", e.name); put("calories", e.calories)
                put("protein", e.proteinGrams); put("carbs", e.carbGrams); put("fat", e.fatGrams)
                put("mealSlot", e.mealSlot); put("serving", e.servingSize); put("isPreset", e.isPreset)
            })
        }
        return JSONObject().apply {
            put("date", log.date); put("entries", entriesArr)
            put("targetCalories", log.targetCalories)
            put("targetProtein", log.targetProteinGrams)
            put("targetCarbs", log.targetCarbGrams)
            put("targetFat", log.targetFatGrams)
        }
    }

    private fun fromJson(o: JSONObject): DailyFoodLog {
        val entriesArr = o.getJSONArray("entries")
        val entries = (0 until entriesArr.length()).map { i ->
            val e = entriesArr.getJSONObject(i)
            FoodEntry(
                id = e.getLong("id"), timestamp = e.getLong("timestamp"),
                name = e.getString("name"), calories = e.getDouble("calories"),
                proteinGrams = e.getDouble("protein"), carbGrams = e.getDouble("carbs"),
                fatGrams = e.getDouble("fat"), mealSlot = e.getString("mealSlot"),
                servingSize = e.getString("serving"), isPreset = e.getBoolean("isPreset")
            )
        }
        return DailyFoodLog(
            date = o.getString("date"),
            entries = entries,
            targetCalories = o.getDouble("targetCalories"),
            targetProteinGrams = o.getDouble("targetProtein"),
            targetCarbGrams = o.getDouble("targetCarbs"),
            targetFatGrams = o.getDouble("targetFat")
        )
    }

    fun saveTargetCalories(calories: Double) {
        prefs.edit().putFloat("target_calories", calories.toFloat()).apply()
    }

    fun getWeeklyAdherence(): kotlinx.coroutines.flow.Flow<Float> = kotlinx.coroutines.flow.flow {
        val history = getHistoricalLogs()
        val today = _todayLog.value
        
        val last7Days = (history + today).take(7)
        if (last7Days.isEmpty()) {
            emit(0f)
            return@flow
        }
        
        var adherenceSum = 0f
        last7Days.forEach { log ->
            val consumed = log.entries.sumOf { it.calories }
            val target = log.targetCalories
            if (target > 0) {
                // Adherence = 1 if within +/- 10% of target, otherwise linear drop-off?
                // Or just (consumed / target) capped at 1?
                // Let's use simple (consumed / target) capped at 1 for now.
                val ratio = (consumed / target).toFloat()
                adherenceSum += ratio.coerceIn(0f, 1f)
            }
        }
        emit(adherenceSum / last7Days.size)
    }

    fun getDailyCalorieGoal(): kotlinx.coroutines.flow.Flow<Int> = kotlinx.coroutines.flow.flow {
        emit(prefs.getFloat("target_calories", 2000f).toInt())
    }

    suspend fun getAverageCaloriesInRange(startMillis: Long, endMillis: Long): Int {
        val logs = getLogsInTimeRange(startMillis, endMillis)
        if (logs.isEmpty()) return 0
        val total = logs.sumOf { it.entries.sumOf { entry -> entry.calories } }
        val days = ((endMillis - startMillis) / (24 * 60 * 60 * 1000)).toInt() + 1
        return (total / days.coerceAtLeast(1)).toInt()
    }

    suspend fun getDaysLoggedInRange(startMillis: Long, endMillis: Long): Int {
        val logs = getLogsInTimeRange(startMillis, endMillis)
        return logs.count { it.entries.isNotEmpty() }
    }

    suspend fun getDaysOnTargetInRange(startMillis: Long, endMillis: Long): Int {
        val logs = getLogsInTimeRange(startMillis, endMillis)
        return logs.count { log ->
            val consumed = log.entries.sumOf { it.calories }
            val target = log.targetCalories
            consumed <= target && consumed >= target - 500 // Within a reasonable range
        }
    }

    private fun getLogsInTimeRange(startMillis: Long, endMillis: Long): List<DailyFoodLog> {
        val allHistory = getHistoricalLogs()
        val today = _todayLog.value
        
        val startStr = dateFormat.format(Date(startMillis))
        val endStr = dateFormat.format(Date(endMillis))
        
        return (allHistory + today).filter { log ->
            log.date >= startStr && log.date <= endStr
        }
    }
}
