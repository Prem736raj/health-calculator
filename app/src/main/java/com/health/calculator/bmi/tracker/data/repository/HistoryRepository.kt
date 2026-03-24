package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.local.HistoryDao
import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Repository layer for calculation history data access.
 */
class HistoryRepository(
    private val historyDao: HistoryDao
) {
    /** All history entries, ordered newest first */
    fun getAllEntries(): Flow<List<HistoryEntry>> {
        return historyDao.getAllEntries()
    }

    suspend fun getAllEntriesSync(limit: Int = Int.MAX_VALUE): List<HistoryEntry> {
        return historyDao.getAllEntriesSync(limit)
    }

    /** Total count of history entries */
    val entryCount: Flow<Int> = historyDao.getEntryCount()

    /** Get entries filtered by calculator type */
    fun getEntriesByType(type: CalculatorType): Flow<List<HistoryEntry>> {
        return historyDao.getEntriesByType(type.key)
    }

    /** Get the most recent entry for a specific calculator */
    fun getLatestEntry(type: CalculatorType): Flow<HistoryEntry?> {
        return historyDao.getLatestEntryByType(type.key)
    }

    fun getLatestByType(calculatorKey: String): Flow<HistoryEntry?> {
        return historyDao.getLatestEntryByType(calculatorKey)
    }

    suspend fun getLatestByTypeSync(calculatorKey: String): HistoryEntry? {
        return historyDao.getLatestEntryByTypeSync(calculatorKey)
    }

    suspend fun getConsecutiveDaysWithEntryType(typeKey: String): Int {
        val entries = historyDao.getEntriesByTypeSync(typeKey, 30) // Check last 30 days
        if (entries.isEmpty()) return 0

        var streak = 0
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        
        var currentDay = cal.timeInMillis
        val oneDay = 24 * 60 * 60 * 1000L

        // Check if there's an entry for today or yesterday to start the streak
        val lastEntryDate = entries[0].timestamp
        cal.timeInMillis = lastEntryDate
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val lastEntryDay = cal.timeInMillis

        if (lastEntryDay < currentDay - oneDay) return 0 // Streak broken if no entry yesterday or today

        var lastProcessedDay = currentDay + oneDay // Future to start
        val distinctDays = entries.map {
            cal.timeInMillis = it.timestamp
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        for (day in distinctDays) {
            if (day == lastProcessedDay - oneDay || (lastProcessedDay > currentDay && (day == currentDay || day == currentDay - oneDay))) {
                streak++
                lastProcessedDay = day
            } else {
                break
            }
        }
        return streak
    }

    suspend fun getLastEntryDate(): Long? {
        return historyDao.getAllEntriesSync(1).firstOrNull()?.timestamp
    }

    /** Insert a new history entry, returns the generated ID */
    suspend fun addEntry(entry: HistoryEntry): Long {
        return historyDao.insertEntry(entry)
    }

    /** Delete entry by ID */
    suspend fun deleteEntry(id: Long) {
        historyDao.deleteEntryById(id)
    }

    /** Delete all history entries */
    suspend fun deleteAllEntries() {
        historyDao.deleteAllEntries()
    }

    suspend fun updateNote(id: Long, note: String) {
        historyDao.updateNote(id, note)
    }

    suspend fun clearAllHistory() {
        historyDao.deleteAllEntries()
    }

    suspend fun insertParsedEntry(entry: ParsedHistoryEntry) {
        val historyEntry = HistoryEntry(
            calculatorKey = entry.calculatorKey,
            resultValue = entry.primaryValue.toString(),
            resultLabel = entry.primaryLabel,
            category = entry.category,
            timestamp = entry.timestamp,
            detailsJson = entry.details.entries.joinToString("|") { "${it.key}:${it.value}" },
            note = entry.note
        )
        historyDao.insertEntry(historyEntry)
    }

    suspend fun getEntryById(id: Long): Map<String, String>? {
        val entry = historyDao.getById(id) ?: return null
        return mapOf(
            "id" to entry.id.toString(),
            "calculator_key" to entry.calculatorKey,
            "result_value" to entry.resultValue,
            "result_label" to (entry.resultLabel ?: ""),
            "category" to (entry.category ?: ""),
            "timestamp" to entry.timestamp.toString(),
            "note" to (entry.note ?: ""),
            "details_json" to (entry.detailsJson ?: "")
        )
    }

    suspend fun restoreEntryFromMap(data: Map<String, String>): Long {
        val entry = HistoryEntry(
            calculatorKey = data["calculator_key"] ?: "",
            resultValue = data["result_value"] ?: "",
            resultLabel = data["result_label"] ?: "",
            category = data["category"]?.takeIf { it.isNotBlank() },
            timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
            note = data["note"]?.takeIf { it.isNotBlank() },
            detailsJson = data["details_json"]?.takeIf { it.isNotBlank() }
        )
        return historyDao.insertEntry(entry)
    }

    suspend fun getEntriesCountOlderThan(cutoffTimestamp: Long): Int {
        return historyDao.countOlderThan(cutoffTimestamp)
    }

    suspend fun getEntriesCountByType(calculatorKey: String): Int {
        return historyDao.countByType(calculatorKey)
    }

    /**
     * Returns a map of calculator keys to their most recent calculation timestamps.
     */
    suspend fun getLastCalculatedTimes(): Map<String, Long> {
        return historyDao.getLastCalculatedTimes().associate { 
            it.calculatorKey to it.lastCalcTime 
        }
    }

    suspend fun getTotalCalculationCount(): Int {
        return historyDao.getEntryCount().first()
    }

    suspend fun getDistinctCalculatorTypes(): Set<String> {
        return historyDao.getDistinctCalculatorKeys().toSet()
    }

    suspend fun getFirstEntryDate(): Long? {
        return historyDao.getFirstEntryTimestamp()
    }

    suspend fun getMostUsedCalculator(): String? {
        return historyDao.getMostUsedCalculator()
    }

    suspend fun getEntriesByTypeInRange(typeKey: String, startTime: Long, endTime: Long): List<ParsedHistoryEntry> {
        return historyDao.getEntriesByTypeInRange(typeKey, startTime, endTime).map { it.toParsedEntry() }
    }

    suspend fun getCalculatorUsageCounts(): List<Pair<String, Int>> {
        return historyDao.getUsageCounts().map { it.key to it.count }
    }
}
