package com.health.calculator.bmi.tracker.data.backup

data class BackupMetadata(
    val id: String,
    val fileName: String,
    val timestamp: Long,
    val sizeBytes: Long,
    val version: Int = BACKUP_VERSION,
    val entryCount: Int = 0,
    val hasProfile: Boolean = false,
    val hasSettings: Boolean = false,
    val hasAchievements: Boolean = false,
    val source: BackupSource = BackupSource.LOCAL
) {
    companion object {
        const val BACKUP_VERSION = 1
    }

    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> "%.1f MB".format(java.util.Locale.US, mb)
                kb >= 1.0 -> "%.0f KB".format(java.util.Locale.US, kb)
                else -> "$sizeBytes bytes"
            }
        }

    val formattedDate: String
        get() {
            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
}

enum class BackupSource(val label: String) {
    LOCAL("Local Device"),
    GOOGLE_DRIVE("Google Drive"),
    QR_TRANSFER("Device Transfer")
}

enum class BackupFrequency(val label: String, val hours: Long) {
    DAILY("Daily", 24),
    WEEKLY("Weekly", 168),
    MONTHLY("Monthly", 720)
}

data class BackupState(
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val progress: Float = 0f,
    val statusMessage: String = "",
    val isComplete: Boolean = false,
    val error: String? = null,
    val lastBackupTime: Long = 0L,
    val availableBackups: List<BackupMetadata> = emptyList(),
    val autoBackupEnabled: Boolean = false,
    val autoBackupFrequency: BackupFrequency = BackupFrequency.WEEKLY,
    val wifiOnlyBackup: Boolean = true,
    val showRestoreConfirm: Boolean = false,
    val restoreMode: RestoreMode = RestoreMode.REPLACE,
    val selectedBackup: BackupMetadata? = null,
    val showTransferQr: Boolean = false,
    val transferQrContent: String? = null,
    val showBackupSheet: Boolean = false
)

enum class RestoreMode(val label: String, val description: String) {
    REPLACE("Replace", "Replace all current data with backup data"),
    MERGE("Merge", "Add backup data to existing data (duplicates skipped)")
}
