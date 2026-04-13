package com.health.calculator.bmi.tracker.data.backup

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import com.health.calculator.bmi.tracker.data.model.ParsedHistoryEntry
import com.health.calculator.bmi.tracker.data.model.toDisplayEntry
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class BackupRepository(
    private val context: Context,
    private val localBackupManager: LocalBackupManager,
    private val googleDriveManager: GoogleDriveBackupManager,
    private val qrTransferManager: QrTransferManager,
    private val backupPreferences: BackupPreferences,
    private val historyRepository: HistoryRepository
) {
    private val _backupState = MutableStateFlow(BackupState())
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        repositoryScope.launch {
            observePreferences()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BackupRepository? = null

        fun getInstance(
            context: Context,
            localBackupManager: LocalBackupManager,
            googleDriveManager: GoogleDriveBackupManager,
            qrTransferManager: QrTransferManager,
            backupPreferences: BackupPreferences,
            historyRepository: HistoryRepository
        ): BackupRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BackupRepository(
                    context.applicationContext,
                    localBackupManager,
                    googleDriveManager,
                    qrTransferManager,
                    backupPreferences,
                    historyRepository
                ).also { INSTANCE = it }
            }
        }
    }

    private suspend fun observePreferences() {
        combine(
            backupPreferences.lastBackupTime,
            backupPreferences.autoBackupEnabled,
            backupPreferences.autoBackupFrequency,
            backupPreferences.wifiOnly
        ) { lastBackup, autoEnabled, frequency, wifiOnly ->
            _backupState.update {
                it.copy(
                    lastBackupTime = lastBackup,
                    autoBackupEnabled = autoEnabled,
                    autoBackupFrequency = frequency,
                    wifiOnlyBackup = wifiOnly
                )
            }
        }.collect()
    }

    // === LOCAL BACKUP ===

    suspend fun createLocalBackup(
        entries: List<HistoryDisplayEntry>,
        profileData: Map<String, String> = emptyMap(),
        settingsData: Map<String, String> = emptyMap(),
        achievementsData: Map<String, Any> = emptyMap()
    ) {
        _backupState.update {
            it.copy(isBackingUp = true, progress = 0f, statusMessage = "Creating backup...", isComplete = false, error = null)
        }

        try {
            val file = withContext(Dispatchers.IO) {
                localBackupManager.createLocalBackup(
                    historyEntries = entries,
                    profileData = profileData,
                    settingsData = settingsData,
                    achievementsData = achievementsData,
                    onProgress = { progress ->
                        _backupState.update {
                            it.copy(progress = progress, statusMessage = "Encrypting and saving...")
                        }
                    }
                )
            }

            backupPreferences.updateLastBackupTime(System.currentTimeMillis())

            _backupState.update {
                it.copy(
                    isBackingUp = false,
                    isComplete = true,
                    progress = 1f,
                    statusMessage = "Backup saved locally"
                )
            }
        } catch (e: Exception) {
            _backupState.update {
                it.copy(
                    isBackingUp = false,
                    error = "Backup failed: ${e.localizedMessage}"
                )
            }
        }
    }

    // === GOOGLE DRIVE BACKUP ===

    suspend fun backupToGoogleDrive(
        entries: List<HistoryDisplayEntry>,
        profileData: Map<String, String> = emptyMap(),
        settingsData: Map<String, String> = emptyMap(),
        achievementsData: Map<String, Any> = emptyMap()
    ) {
        _backupState.update {
            it.copy(isBackingUp = true, progress = 0f, statusMessage = "Preparing cloud backup...", isComplete = false, error = null)
        }

        try {
            // Create local encrypted backup first
            val file = withContext(Dispatchers.IO) {
                localBackupManager.createLocalBackup(
                    historyEntries = entries,
                    profileData = profileData,
                    settingsData = settingsData,
                    achievementsData = achievementsData,
                    onProgress = { p -> _backupState.update { it.copy(progress = p * 0.4f) } }
                )
            }

            _backupState.update {
                it.copy(progress = 0.4f, statusMessage = "Uploading to Google Drive...")
            }

            // Upload to Drive
            val encryptedData = withContext(Dispatchers.IO) { file.readBytes() }
            val success = googleDriveManager.uploadBackup(encryptedData, entries.size) { p ->
                _backupState.update { it.copy(progress = 0.4f + p * 0.6f) }
            }

            if (success) {
                backupPreferences.updateLastBackupTime(System.currentTimeMillis())
                backupPreferences.updateDriveBackupTime(System.currentTimeMillis())

                _backupState.update {
                    it.copy(
                        isBackingUp = false,
                        isComplete = true,
                        progress = 1f,
                        statusMessage = "Backed up to Google Drive"
                    )
                }
            } else {
                _backupState.update {
                    it.copy(isBackingUp = false, error = "Upload to Google Drive failed")
                }
            }
        } catch (e: Exception) {
            _backupState.update {
                it.copy(isBackingUp = false, error = "Cloud backup failed: ${e.localizedMessage}")
            }
        }
    }

    suspend fun fetchDriveBackups() {
        if (!googleDriveManager.isSignedIn()) return

        val driveBackups = googleDriveManager.listBackups()
        val localBackups = localBackupManager.getLocalBackups()

        _backupState.update {
            it.copy(availableBackups = (driveBackups + localBackups).sortedByDescending { b -> b.timestamp })
        }
    }

    // === RESTORE ===

    suspend fun restoreFromLocal(uri: Uri, mode: RestoreMode) {
        _backupState.update {
            it.copy(isRestoring = true, progress = 0f, statusMessage = "Restoring data...", isComplete = false, error = null)
        }

        try {
            val result = withContext(Dispatchers.IO) {
                localBackupManager.restoreFromFile(uri, mode) { p ->
                    _backupState.update { it.copy(progress = p * 0.8f) }
                }
            }

            if (result.success) {
                // Apply the restored data
                applyRestoreResult(result)

                _backupState.update {
                    it.copy(
                        isRestoring = false,
                        isComplete = true,
                        progress = 1f,
                        statusMessage = "Restored ${result.entryCount} entries"
                    )
                }
            } else {
                _backupState.update {
                    it.copy(isRestoring = false, error = result.error ?: "Restore failed")
                }
            }
        } catch (e: Exception) {
            _backupState.update {
                it.copy(isRestoring = false, error = "Restore failed: ${e.localizedMessage}")
            }
        }
    }

    suspend fun restoreFromDrive(backupId: String, mode: RestoreMode) {
        _backupState.update {
            it.copy(isRestoring = true, progress = 0f, statusMessage = "Downloading backup...", isComplete = false, error = null)
        }

        try {
            val encryptedData = googleDriveManager.downloadBackup(backupId) { p ->
                _backupState.update { it.copy(progress = p * 0.4f) }
            }

            if (encryptedData == null) {
                _backupState.update {
                    it.copy(isRestoring = false, error = "Failed to download backup")
                }
                return
            }

            _backupState.update {
                it.copy(progress = 0.4f, statusMessage = "Decrypting and restoring...")
            }

            // Decrypt
            val decryptedBytes = withContext(Dispatchers.IO) {
                BackupEncryption.decrypt(encryptedData)
            }

            _backupState.update { it.copy(progress = 0.6f) }

            // Save to temp file and restore
            val tempFile = withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "temp_restore.hcb")
                file.writeBytes(BackupEncryption.encrypt(decryptedBytes)) // re-encrypt for local restore
                file
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", tempFile
            )

            restoreFromLocal(uri, mode)
        } catch (e: Exception) {
            _backupState.update {
                it.copy(isRestoring = false, error = "Restore failed: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun applyRestoreResult(result: RestoreResult) {
        if (result.mode == RestoreMode.REPLACE) {
            // Clear existing data first
            historyRepository.clearAllHistory()
        }

        // Import history entries
        result.historyEntries.forEach { entry ->
            historyRepository.insertParsedEntry(entry)
        }
    }

    // === QR TRANSFER ===

    suspend fun generateTransferQr() {
        val entries = historyRepository.getAllEntries().first().map { it.toDisplayEntry() }

        val backupFile = withContext(Dispatchers.IO) {
            localBackupManager.createLocalBackup(
                historyEntries = entries,
                profileData = emptyMap(),
                settingsData = emptyMap(),
                achievementsData = emptyMap(),
                onProgress = { }
            )
        }

        val content = qrTransferManager.generateTransferQrContent(backupFile)
        _backupState.update {
            it.copy(showTransferQr = true, transferQrContent = content)
        }
    }

    // === AUTO BACKUP ===

    suspend fun configureAutoBackup(enabled: Boolean, frequency: BackupFrequency, wifiOnly: Boolean) {
        backupPreferences.updateAutoBackup(enabled, frequency, wifiOnly)

        if (enabled) {
            val constraints = Constraints.Builder()
                .apply {
                    if (wifiOnly) setRequiredNetworkType(NetworkType.UNMETERED)
                }
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
                frequency.hours, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag("auto_backup")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "health_auto_backup",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("health_auto_backup")
        }
    }

    fun resetState() {
        _backupState.update { BackupState() }
    }

    fun refreshLocalBackups() {
        val localBackups = localBackupManager.getLocalBackups()
        _backupState.update { it.copy(availableBackups = localBackups) }
    }
}
