package com.health.calculator.bmi.tracker.tracking

import com.health.calculator.bmi.tracker.data.model.StepHistoryEntry
import com.health.calculator.bmi.tracker.data.local.dao.StepHistoryDao
import com.health.calculator.bmi.tracker.data.repository.StepHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StepHistoryRepositoryTest {

    @Test
    fun dateMapSumsDuplicateRestoredRowsAndIgnoresNegativeValues() {
        val zone = ZoneId.of("UTC")
        val day = LocalDate.of(2026, 9, 14)
        val start = day.atStartOfDay(zone).toInstant().toEpochMilli()

        // The DAO normally prevents duplicate primary keys; this also verifies
        // the repository remains safe if a legacy restore contains duplicates.
        val values = listOf(
            StepHistoryEntry(start, 4_000),
            StepHistoryEntry(start, 2_000),
            StepHistoryEntry(start + 86_400_000L, -1)
        )

        val repository = StepHistoryRepository(FakeStepHistoryDao())

        assertEquals(mapOf(day to 6_000), repository.asDateMap(values, zone))
    }

    private class FakeStepHistoryDao : StepHistoryDao {
        override suspend fun upsert(entry: StepHistoryEntry) = Unit
        override suspend fun upsertAll(entries: List<StepHistoryEntry>) = Unit
        override fun observeRecent(limit: Int): Flow<List<StepHistoryEntry>> = emptyFlow()
        override suspend fun getInRange(startMillis: Long, endMillis: Long): List<StepHistoryEntry> = emptyList()
        override suspend fun deleteBefore(cutoffMillis: Long): Int = 0
    }
}
