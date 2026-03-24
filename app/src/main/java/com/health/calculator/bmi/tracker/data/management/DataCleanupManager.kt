package com.health.calculator.bmi.tracker.data.management

import android.content.Context
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class DataCleanupManager private constructor(
    private val context: Context,
    private val historyRepository: HistoryRepository,
    private val storageAnalyzer: StorageAnalyzer,
    private val integrityChecker: DataIntegrityChecker
) {
    companion object {
        @Volatile
        private var instance: DataCleanupManager? = null

        fun getInstance(
            context: Context,
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

    suspend fun deleteEverything() {
        withContext(Dispatchers.IO) {
            // Clear history
            historyRepository.clearAllHistory()

            // Clear cache
            storageAnalyzer.clearCache()

            // Clear exports
            storageAnalyzer.clearExports()

            // Clear backups
            storageAnalyzer.clearBackups()

            // Clear DataStore files
            clearDataStoreFiles()

            // Clear shared preferences
            clearSharedPrefs()
        }
    }

    private fun clearDataStoreFiles() {
        val datastoreDir = File(context.filesDir, "datastore")
        if (datastoreDir.exists()) {
            datastoreDir.listFiles()?.forEach { it.delete() }
        }
    }

    private fun clearSharedPrefs() {
        val prefsDir = File(context.filesDir.parent ?: "", "shared_prefs")
        if (prefsDir.exists()) {
            prefsDir.listFiles()?.forEach { file ->
                // Keep critical system/library prefs if necessary, but here we clear most
                if (!file.name.contains("androidx") && !file.name.contains("google")) {
                    file.delete()
                }
            }
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
