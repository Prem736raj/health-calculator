package com.health.calculator.bmi.tracker.data.local

import androidx.room.*
import com.health.calculator.bmi.tracker.data.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for calculation history.
 */
@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getAllEntriesSync(limit: Int): List<HistoryEntry>

    @Query("SELECT * FROM history_entries WHERE calculator_key = :typeKey ORDER BY timestamp DESC")
    fun getEntriesByType(typeKey: String): Flow<List<HistoryEntry>>

    @Query("SELECT COUNT(*) FROM history_entries")
    fun getEntryCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HistoryEntry): Long

    @Query("DELETE FROM history_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: Long)

    @Query("UPDATE history_entries SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String)

    @Query("SELECT DISTINCT category FROM history_entries WHERE category IS NOT NULL ORDER BY category")
    fun getDistinctCategories(): Flow<List<String>>

    @Query("DELETE FROM history_entries")
    suspend fun deleteAllEntries()
    
    @Query("SELECT * FROM history_entries WHERE calculator_key = :typeKey ORDER BY timestamp DESC LIMIT 1")
    fun getLatestEntryByType(typeKey: String): Flow<HistoryEntry?>

    @Query("SELECT * FROM history_entries WHERE calculator_key = :typeKey ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEntryByTypeSync(typeKey: String): HistoryEntry?

    @Query("SELECT * FROM history_entries WHERE calculator_key = :typeKey ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getEntriesByTypeSync(typeKey: String, limit: Int): List<HistoryEntry>

    @Query("SELECT * FROM history_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): HistoryEntry?

    @Query("SELECT COUNT(*) FROM history_entries WHERE timestamp < :cutoff")
    suspend fun countOlderThan(cutoff: Long): Int

    @Query("SELECT COUNT(*) FROM history_entries WHERE calculator_key = :key")
    suspend fun countByType(key: String): Int

    @Query("DELETE FROM history_entries WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long): Int

    @Query("DELETE FROM history_entries WHERE calculator_key = :key")
    suspend fun deleteByType(key: String): Int

    @Query("SELECT calculator_key, MAX(timestamp) as last_calc FROM history_entries GROUP BY calculator_key")
    suspend fun getLastCalculatedTimes(): List<com.health.calculator.bmi.tracker.data.model.CalculatorLastCalc>

    @Query("SELECT COUNT(DISTINCT calculator_key) FROM history_entries")
    suspend fun getDistinctCalculatorCount(): Int

    @Query("SELECT DISTINCT calculator_key FROM history_entries")
    suspend fun getDistinctCalculatorKeys(): List<String>

    @Query("SELECT MIN(timestamp) FROM history_entries")
    suspend fun getFirstEntryTimestamp(): Long?

    @Query("SELECT calculator_key FROM history_entries GROUP BY calculator_key ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostUsedCalculator(): String?

    @Query("SELECT * FROM history_entries WHERE calculator_key = :typeKey AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    suspend fun getEntriesByTypeInRange(typeKey: String, startTime: Long, endTime: Long): List<HistoryEntry>

    @Query("SELECT calculator_key as `key`, COUNT(*) as usage_count FROM history_entries GROUP BY calculator_key")
    suspend fun getUsageCounts(): List<CalculatorUsage>
}

data class CalculatorUsage(
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "usage_count") val count: Int
)
