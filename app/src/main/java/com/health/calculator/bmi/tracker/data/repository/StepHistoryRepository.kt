package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.local.dao.StepHistoryDao
import com.health.calculator.bmi.tracker.data.model.StepHistoryEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** Durable local cache for the optional Health Connect steps feature. */
@Singleton
class StepHistoryRepository @Inject constructor(
    private val dao: StepHistoryDao
) {
    /** The recent window is intentionally bounded to keep dashboard work small. */
    val recentEntries: Flow<List<StepHistoryEntry>> = dao.observeRecent(35)

    suspend fun saveAll(entries: List<StepHistoryEntry>) {
        if (entries.isEmpty()) return
        dao.upsertAll(entries)
    }

    suspend fun save(entry: StepHistoryEntry) = dao.upsert(entry)

    /**
     * Converts stored midnight timestamps into a date map. Duplicate rows are
     * impossible in Room, but summing makes this safe if old data is restored.
     */
    fun asDateMap(
        entries: Iterable<StepHistoryEntry>,
        zone: ZoneId = ZoneId.systemDefault()
    ): Map<LocalDate, Int> = entries
        .filter { it.steps >= 0L }
        .groupBy { java.time.Instant.ofEpochMilli(it.dayStartMillis).atZone(zone).toLocalDate() }
        .mapValues { (_, values) ->
            values.sumOf { it.steps.coerceAtMost(Int.MAX_VALUE.toLong()) }
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt()
        }

    /** Keep the local cache bounded while retaining enough data for comparisons. */
    suspend fun prune(before: LocalDate, zone: ZoneId = ZoneId.systemDefault()) {
        val cutoff = before.atStartOfDay(zone).toInstant().toEpochMilli()
        dao.deleteBefore(cutoff)
    }
}
