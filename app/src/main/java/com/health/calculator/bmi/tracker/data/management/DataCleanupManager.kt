package com.health.calculator.bmi.tracker.data.management

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class DataCleanupManager private constructor(
    @ApplicationContext private val context: Context,
    private val historyRepository: HistoryRepository,
    private val storageAnalyzer: StorageAnalyzer,
    private val integrityChecker: DataIntegrityChecker
) {
    companion object {
        @Volatile
        private var instance: DataCleanupManager? = null

        fun getInstance(
            @ApplicationContext context: Context,
            historyRepository: HistoryRepository,
            storageAnalyzer: StorageAnalyzer,
            integrityChecker: DataIntegrityChecker
        ): DataCleanupManager {
            return instance ?: synchronized(this) {
                instance ?: DataCleanupManager(
                    context.applicationContext,
                    historyRepository,
                    storageAnalyzer,
                    integrityChecker
                ).also { instance = it }
            }
        }
    }

    suspend fun previewCleanupByAge(
        entries: List<HistoryDisplayEntry>,
        age: CleanupAge
    ): CleanupPreview {
        val cutoff = Calendar.getInstance().apply {
            add(Calendar.MONTH, -age.months)
        }.timeInMillis

        val affected = entries.filter { it.timestamp < cutoff }

        return CleanupPreview(
            entriesAffected = affected.size,
            spaceFreed = affected.size * 512L, // Rough estimate per entry
            oldestEntry = affected.minOfOrNull { it.timestamp } ?: 0L,
            newestAffected = affected.maxOfOrNull { it.timestamp } ?: 0L
        )
    }

    suspend fun cleanupByAge(entries: List<HistoryDisplayEntry>, age: CleanupAge): Int {
        val cutoff = Calendar.getInstance().apply {
            add(Calendar.MONTH, -age.months)
        }.timeInMillis

        val toDelete = entries.filter { it.timestamp < cutoff }
        var deleted = 0

        withContext(Dispatchers.IO) {
            toDelete.forEach { entry ->
                historyRepository.deleteEntry(entry.id)
                deleted++
            }
        }

        return deleted
    }

    suspend fun cleanupByCalculator(
        entries: List<HistoryDisplayEntry>,
        types: Set<CalculatorType>
    ): Int {
        val toDelete = entries.filter { it.calculatorType in types }
        var deleted = 0

        withContext(Dispatchers.IO) {
            toDelete.forEach { entry ->
                historyRepository.deleteEntry(entry.id)
                deleted++
            }
        }

        return deleted
    }

    suspend fun fixIntegrityIssues(entries: List<HistoryDisplayEntry>): Int {
        val corruptedIds = integrityChecker.findCorruptedIds(entries)
        val duplicateIds = integrityChecker.findDuplicateIds(entries)
        val orphanedIds = integrityChecker.findOrphanedIds(entries)

        val allIssueIds = (corruptedIds + duplicateIds + orphanedIds).distinct()

        withContext(Dispatchers.IO) {
            allIssueIds.forEach { id ->
                historyRepository.deleteEntry(id)
            }
        }

        return allIssueIds.size
    }

    suspend fun deleteEverything(): Boolean {
        return withContext(Dispatchers.Main.immediate) {
            FullAppDataResetter.request(context)
        }
    }

    suspend fun softDeleteEntry(entryId: Long): Map<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                val data = historyRepository.getEntryById(entryId)
                if (data != null) {
                    historyRepository.deleteEntry(entryId)
                    data
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun restoreEntry(entryData: Map<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                historyRepository.restoreEntryFromMap(entryData)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}

