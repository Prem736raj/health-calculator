package com.health.calculator.bmi.tracker.data.repository

import android.content.Context
import com.health.calculator.bmi.tracker.data.model.CalorieHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class CalorieHistoryRepository(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("calorie_history", Context.MODE_PRIVATE)
    }

    private val _entries = MutableStateFlow<List<CalorieHistoryEntry>>(emptyList())
    val entries: StateFlow<List<CalorieHistoryEntry>> = _entries.asStateFlow()

    init { loadEntries() }

    fun saveEntry(entry: CalorieHistoryEntry) {
        val current = _entries.value.toMutableList()
        current.add(0, entry)
        _entries.value = current
        persist(current)
    }

    fun deleteEntry(id: Long) {
        val current = _entries.value.toMutableList()
        current.removeAll { it.id == id }
        _entries.value = current
        persist(current)
    }

    fun clearAll() {
        _entries.value = emptyList()
        prefs.edit().clear().apply()
    }

    private fun loadEntries() {
        val json = prefs.getString("entries", null) ?: return
        try {
            val arr = JSONArray(json)
            val list = mutableListOf<CalorieHistoryEntry>()
            for (i in 0 until arr.length()) {
                list.add(fromJson(arr.getJSONObject(i)))
            }
            _entries.value = list
        } catch (_: Exception) {}
    }

    private fun persist(entries: List<CalorieHistoryEntry>) {
        val arr = JSONArray()
        entries.forEach { arr.put(toJson(it)) }
        prefs.edit().putString("entries", arr.toString()).apply()
    }

    private fun toJson(e: CalorieHistoryEntry) = JSONObject().apply {
        put("id", e.id); put("timestamp", e.timestamp)
        put("bmr", e.bmr); put("tdee", e.tdee)
        put("goalCalories", e.goalCalories); put("goalName", e.goalName)
        put("activityLevel", e.activityLevel); put("formulaUsed", e.formulaUsed)
        put("weightKg", e.weightKg); put("heightCm", e.heightCm)
        put("age", e.age); put("gender", e.gender)
        put("bodyFatPercent", e.bodyFatPercent ?: -1.0)
        put("weeklyChangeKg", e.weeklyChangeKg)
    }

    private fun fromJson(o: JSONObject) = CalorieHistoryEntry(
        id = o.getLong("id"), timestamp = o.getLong("timestamp"),
        bmr = o.getDouble("bmr"), tdee = o.getDouble("tdee"),
        goalCalories = o.getDouble("goalCalories"),
        goalName = o.getString("goalName"),
        activityLevel = o.getString("activityLevel"),
        formulaUsed = o.getString("formulaUsed"),
        weightKg = o.getDouble("weightKg"), heightCm = o.getDouble("heightCm"),
        age = o.getInt("age"), gender = o.getString("gender"),
        bodyFatPercent = o.getDouble("bodyFatPercent").takeIf { it >= 0 },
        weeklyChangeKg = o.getDouble("weeklyChangeKg")
    )
}
