package com.health.calculator.bmi.tracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WhrRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("whr_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _entries = MutableStateFlow<List<WhrHistoryEntry>>(emptyList())
    val entries: StateFlow<List<WhrHistoryEntry>> = _entries.asStateFlow()

    private val _goal = MutableStateFlow<WhrGoal?>(null)
    val goal: StateFlow<WhrGoal?> = _goal.asStateFlow()

    init {
        loadEntries()
        loadGoal()
    }

    private fun loadEntries() {
        val json = prefs.getString("whr_entries", null)
        if (json != null) {
            val type = object : TypeToken<List<WhrHistoryEntry>>() {}.type
            _entries.value = gson.fromJson(json, type) ?: emptyList()
        }
    }

    private fun saveEntries() {
        val json = gson.toJson(_entries.value)
        prefs.edit().putString("whr_entries", json).apply()
    }

    private fun loadGoal() {
        val json = prefs.getString("whr_goal", null)
        if (json != null) {
            _goal.value = gson.fromJson(json, WhrGoal::class.java)
        }
    }

    private fun saveGoal() {
        val json = gson.toJson(_goal.value)
        prefs.edit().putString("whr_goal", json).apply()
    }

    fun addEntry(entry: WhrHistoryEntry) {
        _entries.value = _entries.value + entry
        saveEntries()
    }

    fun deleteEntry(id: String) {
        _entries.value = _entries.value.filter { it.id != id }
        saveEntries()
    }

    fun clearAll() {
        _entries.value = emptyList()
        saveEntries()
    }

    fun setGoal(goal: WhrGoal?) {
        _goal.value = goal
        saveGoal()
    }

    fun getEntriesInRange(timeRange: WhrTimeRange): List<WhrHistoryEntry> {
        if (timeRange == WhrTimeRange.ALL_TIME) return _entries.value

        val cutoff = System.currentTimeMillis() - (timeRange.days.toLong() * 24 * 60 * 60 * 1000)
        return _entries.value.filter { it.timestamp >= cutoff }
    }

    fun getProgressStats(): WhrProgressStats? {
        val sorted = _entries.value.sortedBy { it.timestamp }
        if (sorted.isEmpty()) return null

        val current = sorted.last()
        val first = sorted.first()
        val bestWhr = sorted.minOf { it.whr }
        val worstWhr = sorted.maxOf { it.whr }
        val avgWhr = sorted.map { it.whr }.average().toFloat()

        val whrChange = current.whr - first.whr
        val waistChange = current.waistCm - first.waistCm
        val hipChange = current.hipCm - first.hipCm

        return WhrProgressStats(
            currentWhr = current.whr,
            firstWhr = first.whr,
            bestWhr = bestWhr,
            worstWhr = worstWhr,
            averageWhr = avgWhr,
            currentWaist = current.waistCm,
            firstWaist = first.waistCm,
            waistChange = waistChange,
            currentHip = current.hipCm,
            firstHip = first.hipCm,
            hipChange = hipChange,
            totalMeasurements = sorted.size,
            whrChange = whrChange,
            whrTrend = determineTrend(whrChange, isLowerBetter = true),
            waistTrend = determineTrend(waistChange, isLowerBetter = true),
            hipTrend = determineTrend(hipChange, isLowerBetter = false)
        )
    }

    fun getComparison(): WhrComparison? {
        val sorted = _entries.value.sortedBy { it.timestamp }
        if (sorted.size < 2) return null

        val current = sorted.last()
        val previous = sorted[sorted.size - 2]

        return WhrComparison(
            previousWhr = previous.whr,
            currentWhr = current.whr,
            whrDiff = current.whr - previous.whr,
            whrDirection = determineTrend(current.whr - previous.whr, isLowerBetter = true),
            previousWaist = previous.waistCm,
            currentWaist = current.waistCm,
            waistDiff = current.waistCm - previous.waistCm,
            waistDirection = determineTrend(current.waistCm - previous.waistCm, isLowerBetter = true),
            previousHip = previous.hipCm,
            currentHip = current.hipCm,
            hipDiff = current.hipCm - previous.hipCm,
            hipDirection = determineTrend(current.hipCm - previous.hipCm, isLowerBetter = false),
            previousDate = previous.timestamp,
            currentDate = current.timestamp
        )
    }

    private fun determineTrend(change: Float, isLowerBetter: Boolean): WhrTrendDirection {
        val threshold = 0.01f
        return when {
            kotlin.math.abs(change) < threshold -> WhrTrendDirection.STEADY
            (change < 0 && isLowerBetter) || (change > 0 && !isLowerBetter) -> WhrTrendDirection.IMPROVING
            else -> WhrTrendDirection.WORSENING
        }
    }
}
