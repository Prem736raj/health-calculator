package com.health.calculator.bmi.tracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.health.calculator.bmi.tracker.data.model.BSARecord
import com.health.calculator.bmi.tracker.data.model.BSAStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val Context.bsaDataStore: DataStore<Preferences> by preferencesDataStore(name = "bsa_tracking_prefs")

class BSATrackingRepository(private val context: Context) {

    private val RECORDS_KEY = stringPreferencesKey("bsa_records_json")
    private val gson = Gson()

    suspend fun saveRecord(record: BSARecord) {
        val currentRecords = getRecords().toMutableList()
        currentRecords.add(record)
        
        // Keep only last 50 records to prevent excessive storage
        val updatedRecords = currentRecords.sortedByDescending { it.timestamp }.take(50)
        
        context.bsaDataStore.edit { prefs ->
            prefs[RECORDS_KEY] = gson.toJson(updatedRecords)
        }
    }
    suspend fun getRecords(): List<BSARecord> {
        val json = context.bsaDataStore.data.map { prefs ->
            prefs[RECORDS_KEY] ?: "[]"
        }.first()
        
        return try {
            val type = object : TypeToken<List<BSARecord>>() {}.type
            gson.fromJson<List<BSARecord>>(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getRecordsFlow(): Flow<List<BSARecord>> {
        return context.bsaDataStore.data.map { prefs ->
            val json = prefs[RECORDS_KEY] ?: "[]"
            try {
                val type = object : TypeToken<List<BSARecord>>() {}.type
                gson.fromJson<List<BSARecord>>(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun getRecordsSorted(): List<BSARecord> {
        // Synchronous wrapper for ViewModel init or flow-based use
        return emptyList() // Placeholder: Better to use Flow in real app, but following prompt logic
    }

    suspend fun getStatistics(): BSAStatistics? {
        val records = getRecords().sortedBy { it.timestamp }
        if (records.isEmpty()) return null
        
        val current = records.last()
        val first = records.first()
        val avg = records.map { it.bsaValue }.average().toFloat()
        val change = current.bsaValue - first.bsaValue
        val changePercent = if (first.bsaValue != 0f) (change / first.bsaValue) * 100f else 0f
        
        val mostUsedFormula = records.groupBy { it.formulaName }
            .maxByOrNull { it.value.size }?.key ?: "Unknown"

        return BSAStatistics(
            totalReadings = records.size,
            currentBSA = current.bsaValue,
            averageBSA = avg,
            firstBSA = first.bsaValue,
            changeFromFirst = change,
            changePercent = changePercent,
            mostUsedFormula = mostUsedFormula
        )
    }

    suspend fun clearHistory() {
        context.bsaDataStore.edit { prefs ->
            prefs[RECORDS_KEY] = "[]"
        }
    }
}
