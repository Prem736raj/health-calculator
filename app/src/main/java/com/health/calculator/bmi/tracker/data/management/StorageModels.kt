package com.health.calculator.bmi.tracker.data.management

data class StorageInfo(
    val totalBytes: Long = 0L,
    val historyBytes: Long = 0L,
    val cacheBytes: Long = 0L,
    val settingsBytes: Long = 0L,
    val exportsBytes: Long = 0L,
    val backupsBytes: Long = 0L
) {
    val totalFormatted: String get() = formatBytes(totalBytes)
    val historyFormatted: String get() = formatBytes(historyBytes)
    val cacheFormatted: String get() = formatBytes(cacheBytes)
    val settingsFormatted: String get() = formatBytes(settingsBytes)
    val exportsFormatted: String get() = formatBytes(exportsBytes)
    val backupsFormatted: String get() = formatBytes(backupsBytes)

    fun percentOf(part: Long): Float {
        if (totalBytes == 0L) return 0f
        return part.toFloat() / totalBytes.toFloat()
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> "%.2f GB".format(gb)
                mb >= 1.0 -> "%.1f MB".format(mb)
                kb >= 1.0 -> "%.0f KB".format(kb)
                else -> "$bytes B"
            }
        }
    }
}

data class IntegrityReport(
    val isChecking: Boolean = false,
    val isComplete: Boolean = false,
    val totalEntries: Int = 0,
    val corruptedEntries: Int = 0,
    val duplicateEntries: Int = 0,
    val orphanedEntries: Int = 0,
    val issuesFixed: Int = 0,
    val statusMessage: String = ""
) {
    val totalIssues: Int get() = corruptedEntries + duplicateEntries + orphanedEntries
    val isHealthy: Boolean get() = totalIssues == 0
}

enum class CleanupAge(val label: String, val months: Int) {
    THREE_MONTHS("3 months", 3),
    SIX_MONTHS("6 months", 6),
    ONE_YEAR("1 year", 12),
    TWO_YEARS("2 years", 24)
}

data class CleanupPreview(
    val entriesAffected: Int = 0,
    val spaceFreed: Long = 0L,
    val oldestEntry: Long = 0L,
    val newestAffected: Long = 0L
)

data class UndoableDelete(
    val entryId: Long,
    val entryData: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 5000L
)

data class DataManagementState(
    val storageInfo: StorageInfo = StorageInfo(),
    val isLoadingStorage: Boolean = true,
    val integrityReport: IntegrityReport = IntegrityReport(),
    val showCleanupByAge: Boolean = false,
    val showCleanupByCalculator: Boolean = false,
    val showDeleteEverything: Boolean = false,
    val deleteEverythingStep: Int = 0,
    val deleteConfirmText: String = "",
    val cleanupPreview: CleanupPreview? = null,
    val selectedCleanupAge: CleanupAge? = null,
    val selectedCleanupTypes: Set<com.health.calculator.bmi.tracker.data.model.CalculatorType> = emptySet(),
    val undoableDelete: UndoableDelete? = null,
    val snackbarMessage: String? = null,
    val isDeleting: Boolean = false,
    val isClearing: Boolean = false,
    val operationComplete: Boolean = false
)
