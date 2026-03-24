package com.health.calculator.bmi.tracker.data.management

import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import kotlin.math.abs

class DataIntegrityChecker private constructor() {

    companion object {
        @Volatile
        private var instance: DataIntegrityChecker? = null

        fun getInstance(): DataIntegrityChecker {
            return instance ?: synchronized(this) {
                instance ?: DataIntegrityChecker().also { instance = it }
            }
        }
    }

    fun checkIntegrity(entries: List<HistoryDisplayEntry>): IntegrityReport {
        val corrupted = mutableListOf<Long>()
        val duplicates = mutableListOf<Long>()
        val orphaned = mutableListOf<Long>()

        // Check for corrupted entries
        entries.forEach { entry ->
            if (isCorrupted(entry)) {
                corrupted.add(entry.id)
            }
        }

        // Check for duplicates (same type, same value, within 1 second)
        val sorted = entries.sortedBy { it.timestamp }
        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1]
            val curr = sorted[i]
            if (isDuplicate(prev, curr)) {
                duplicates.add(curr.id)
            }
        }

        // Check for orphaned entries (invalid calculator type)
        entries.forEach { entry ->
            if (isOrphaned(entry)) {
                orphaned.add(entry.id)
            }
        }

        return IntegrityReport(
            isComplete = true,
            totalEntries = entries.size,
            corruptedEntries = corrupted.size,
            duplicateEntries = duplicates.size,
            orphanedEntries = orphaned.size,
            statusMessage = buildStatusMessage(corrupted.size, duplicates.size, orphaned.size)
        )
    }

    fun findCorruptedIds(entries: List<HistoryDisplayEntry>): List<Long> {
        return entries.filter { isCorrupted(it) }.map { it.id }
    }

    fun findDuplicateIds(entries: List<HistoryDisplayEntry>): List<Long> {
        val sorted = entries.sortedBy { it.timestamp }
        val duplicateIds = mutableListOf<Long>()
        for (i in 1 until sorted.size) {
            if (isDuplicate(sorted[i - 1], sorted[i])) {
                duplicateIds.add(sorted[i].id)
            }
        }
        return duplicateIds
    }

    fun findOrphanedIds(entries: List<HistoryDisplayEntry>): List<Long> {
        return entries.filter { isOrphaned(it) }.map { it.id }
    }

    private fun isCorrupted(entry: HistoryDisplayEntry): Boolean {
        // Check for missing critical data
        if (entry.primaryValue.isBlank()) return true
        if (entry.primaryLabel.isBlank()) return true
        if (entry.timestamp <= 0) return true
        if (entry.timestamp > System.currentTimeMillis() + 86400000L) return true // future date beyond 1 day

        // Check for obviously invalid values
        val numericValue = entry.primaryValue.toDoubleOrNull()
        if (numericValue != null) {
            if (numericValue < 0 && entry.calculatorType != CalculatorType.CALORIE) return true
            if (numericValue.isNaN() || numericValue.isInfinite()) return true
        }

        return false
    }

    private fun isDuplicate(a: HistoryDisplayEntry, b: HistoryDisplayEntry): Boolean {
        if (a.calculatorType != b.calculatorType) return false
        if (a.primaryValue != b.primaryValue) return false
        val timeDiff = abs(a.timestamp - b.timestamp)
        return timeDiff < 1000L // Within 1 second
    }

    private fun isOrphaned(entry: HistoryDisplayEntry): Boolean {
        return try {
            CalculatorType.fromKey(entry.calculatorType.key) == null
        } catch (e: Exception) {
            true
        }
    }

    private fun buildStatusMessage(corrupted: Int, duplicates: Int, orphaned: Int): String {
        val total = corrupted + duplicates + orphaned
        if (total == 0) return "All data verified ✓ No issues found."

        val parts = mutableListOf<String>()
        if (corrupted > 0) parts.add("$corrupted corrupted")
        if (duplicates > 0) parts.add("$duplicates duplicate")
        if (orphaned > 0) parts.add("$orphaned orphaned")

        return "$total issue${if (total > 1) "s" else ""} found: ${parts.joinToString(", ")}."
    }
}
