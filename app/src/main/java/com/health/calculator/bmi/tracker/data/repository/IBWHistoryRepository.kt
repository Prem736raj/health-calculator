package com.health.calculator.bmi.tracker.data.repository

import android.content.Context
import com.health.calculator.bmi.tracker.data.model.IBWHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class IBWHistoryRepository(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("ibw_history", Context.MODE_PRIVATE)
    }

    private val _entries = MutableStateFlow<List<IBWHistoryEntry>>(emptyList())
    val entries: Flow<List<IBWHistoryEntry>> = _entries.asStateFlow()

    init {
        loadEntries()
    }

    fun saveEntry(entry: IBWHistoryEntry) {
        val current = _entries.value.toMutableList()
        current.add(0, entry)
        _entries.value = current
        persistEntries(current)
    }

    fun deleteEntry(id: Long) {
        val current = _entries.value.toMutableList()
        current.removeAll { it.id == id }
        _entries.value = current
        persistEntries(current)
    }

    fun clearAll() {
        _entries.value = emptyList()
        prefs.edit().clear().apply()
    }

    fun getStatistics(): IBWStatistics {
        val entries = _entries.value
        if (entries.isEmpty()) return IBWStatistics()

        val entriesWithWeight = entries.filter { it.currentWeightKg != null }

        val closestToIdeal = entriesWithWeight.minByOrNull {
            kotlin.math.abs((it.currentWeightKg ?: 0.0) - it.frameAdjustedDevineKg)
        }

        val furthestFromIdeal = entriesWithWeight.maxByOrNull {
            kotlin.math.abs((it.currentWeightKg ?: 0.0) - it.frameAdjustedDevineKg)
        }

        val latestIdealKg = entries.firstOrNull()?.frameAdjustedDevineKg
        val latestActualKg = entries.firstOrNull()?.currentWeightKg
        val firstActualKg = entriesWithWeight.lastOrNull()?.currentWeightKg

        val weightChange = if (latestActualKg != null && firstActualKg != null) {
            latestActualKg - firstActualKg
        } else null

        return IBWStatistics(
            totalEntries = entries.size,
            closestToIdealEntry = closestToIdeal,
            furthestFromIdealEntry = furthestFromIdeal,
            latestIdealWeightKg = latestIdealKg,
            latestActualWeightKg = latestActualKg,
            weightChangeSinceFirstKg = weightChange,
            averagePercentOfIBW = entriesWithWeight
                .mapNotNull { it.weightCategoryPercent }
                .takeIf { it.isNotEmpty() }
                ?.average()
        )
    }

    private fun loadEntries() {
        val jsonString = prefs.getString("ibw_entries", null) ?: return
        try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<IBWHistoryEntry>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(jsonToEntry(obj))
            }
            _entries.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun persistEntries(entries: List<IBWHistoryEntry>) {
        val jsonArray = JSONArray()
        entries.forEach { entry ->
            jsonArray.put(entryToJson(entry))
        }
        prefs.edit().putString("ibw_entries", jsonArray.toString()).apply()
    }

    private fun entryToJson(entry: IBWHistoryEntry): JSONObject {
        return JSONObject().apply {
            put("id", entry.id)
            put("timestamp", entry.timestamp)
            put("heightCm", entry.heightCm)
            put("gender", entry.gender)
            put("frameSize", entry.frameSize)
            put("age", entry.age ?: -1)
            put("currentWeightKg", entry.currentWeightKg ?: -1.0)
            put("devineKg", entry.devineKg)
            put("robinsonKg", entry.robinsonKg)
            put("millerKg", entry.millerKg)
            put("hamwiKg", entry.hamwiKg)
            put("brocaKg", entry.brocaKg)
            put("bmiLowerKg", entry.bmiLowerKg)
            put("bmiUpperKg", entry.bmiUpperKg)
            put("frameAdjustedDevineKg", entry.frameAdjustedDevineKg)
            put("leanBodyWeightKg", entry.leanBodyWeightKg ?: -1.0)
            put("adjustedBodyWeightKg", entry.adjustedBodyWeightKg ?: -1.0)
            put("weightCategoryPercent", entry.weightCategoryPercent ?: -1.0)
            put("weightCategory", entry.weightCategory ?: "")
            put("goalWeightKg", entry.goalWeightKg ?: -1.0)
        }
    }

    private fun jsonToEntry(obj: JSONObject): IBWHistoryEntry {
        return IBWHistoryEntry(
            id = obj.getLong("id"),
            timestamp = obj.getLong("timestamp"),
            heightCm = obj.getDouble("heightCm"),
            gender = obj.getString("gender"),
            frameSize = obj.getString("frameSize"),
            age = obj.getInt("age").takeIf { it >= 0 },
            currentWeightKg = obj.getDouble("currentWeightKg").takeIf { it >= 0 },
            devineKg = obj.getDouble("devineKg"),
            robinsonKg = obj.getDouble("robinsonKg"),
            millerKg = obj.getDouble("millerKg"),
            hamwiKg = obj.getDouble("hamwiKg"),
            brocaKg = obj.getDouble("brocaKg"),
            bmiLowerKg = obj.getDouble("bmiLowerKg"),
            bmiUpperKg = obj.getDouble("bmiUpperKg"),
            frameAdjustedDevineKg = obj.getDouble("frameAdjustedDevineKg"),
            leanBodyWeightKg = obj.getDouble("leanBodyWeightKg").takeIf { it >= 0 },
            adjustedBodyWeightKg = obj.getDouble("adjustedBodyWeightKg").takeIf { it >= 0 },
            weightCategoryPercent = obj.getDouble("weightCategoryPercent").takeIf { it >= 0 },
            weightCategory = obj.getString("weightCategory").takeIf { it.isNotBlank() },
            goalWeightKg = obj.getDouble("goalWeightKg").takeIf { it >= 0 }
        )
    }
}

data class IBWStatistics(
    val totalEntries: Int = 0,
    val closestToIdealEntry: IBWHistoryEntry? = null,
    val furthestFromIdealEntry: IBWHistoryEntry? = null,
    val latestIdealWeightKg: Double? = null,
    val latestActualWeightKg: Double? = null,
    val weightChangeSinceFirstKg: Double? = null,
    val averagePercentOfIBW: Double? = null
)
