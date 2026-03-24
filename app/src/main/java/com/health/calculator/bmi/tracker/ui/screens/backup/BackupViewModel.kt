package com.health.calculator.bmi.tracker.ui.screens.backup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.backup.*
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BackupViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val historyRepository = HistoryRepository(database.historyDao())
    
    private val localBackupManager = LocalBackupManager.getInstance(application)
    private val googleDriveManager = GoogleDriveBackupManager.getInstance(application)
    private val qrTransferManager = QrTransferManager.getInstance(application)
    private val backupPreferences = BackupPreferences.getInstance(application)
    
    private val repository = BackupRepository.getInstance(
        application,
        localBackupManager,
        googleDriveManager,
        qrTransferManager,
        backupPreferences,
        historyRepository
    )

    val backupState: StateFlow<BackupState> = repository.backupState

    fun createLocalBackup() {
        viewModelScope.launch {
            val entries = historyRepository.getAllEntries().first().map { it.toDisplayEntry() }
            repository.createLocalBackup(
                entries = entries,
                profileData = emptyMap(),
                settingsData = emptyMap(),
                achievementsData = emptyMap()
            )
        }
    }

    fun backupToGoogleDrive() {
        viewModelScope.launch {
            val entries = historyRepository.getAllEntries().first().map { it.toDisplayEntry() }
            repository.backupToGoogleDrive(entries)
        }
    }

    fun fetchDriveBackups() {
        viewModelScope.launch {
            repository.fetchDriveBackups()
        }
    }

    fun restoreFromBackup(backup: BackupMetadata) {
        // If it's a local backup, we need a URI
        if (backup.source == BackupSource.LOCAL) {
            val uri = localBackupManager.getBackupFileUri(backup.fileName)
            if (uri != null) {
                _backupToRestore.value = backup
                _showRestoreConfirm.value = true
            }
        } else if (backup.source == BackupSource.GOOGLE_DRIVE) {
            _backupToRestore.value = backup
            _showRestoreConfirm.value = true
        }
    }

    private val _showRestoreConfirm = MutableStateFlow(false)
    val showRestoreConfirm = _showRestoreConfirm.asStateFlow()

    private val _backupToRestore = MutableStateFlow<BackupMetadata?>(null)
    val backupToRestore = _backupToRestore.asStateFlow()

    private val _restoreMode = MutableStateFlow(RestoreMode.REPLACE)
    val restoreMode = _restoreMode.asStateFlow()

    fun setRestoreMode(mode: RestoreMode) {
        _restoreMode.value = mode
    }

    fun confirmRestore() {
        val backup = _backupToRestore.value ?: return
        val mode = _restoreMode.value

        viewModelScope.launch {
            _showRestoreConfirm.value = false
            if (backup.source == BackupSource.LOCAL) {
                val uri = localBackupManager.getBackupFileUri(backup.fileName)
                if (uri != null) {
                    repository.restoreFromLocal(uri, mode)
                }
            } else if (backup.source == BackupSource.GOOGLE_DRIVE) {
                repository.restoreFromDrive(backup.id, mode)
            }
        }
    }

    fun cancelRestore() {
        _showRestoreConfirm.value = false
        _backupToRestore.value = null
    }

    fun toggleAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            val current = backupState.value
            repository.configureAutoBackup(
                enabled,
                current.autoBackupFrequency,
                current.wifiOnlyBackup
            )
        }
    }

    fun updateAutoBackupFrequency(frequency: BackupFrequency) {
        viewModelScope.launch {
            val current = backupState.value
            repository.configureAutoBackup(
                current.autoBackupEnabled,
                frequency,
                current.wifiOnlyBackup
            )
        }
    }

    fun updateWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            val current = backupState.value
            repository.configureAutoBackup(
                current.autoBackupEnabled,
                current.autoBackupFrequency,
                wifiOnly
            )
        }
    }

    fun generateTransferQr() {
        viewModelScope.launch {
            repository.generateTransferQr()
        }
    }

    fun hideTransferQr() {
        repository.resetState()
    }

    fun initDrive(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = googleDriveManager.initDriveService()
            onComplete(success)
        }
    }

    fun signOutDrive() {
        googleDriveManager.signOut()
        repository.resetState()
    }

    fun resetState() {
        repository.resetState()
    }

    fun deleteBackup(backup: BackupMetadata) {
        viewModelScope.launch {
            if (backup.source == BackupSource.LOCAL) {
                localBackupManager.deleteBackup(backup.fileName)
                repository.refreshLocalBackups()
            } else if (backup.source == BackupSource.GOOGLE_DRIVE) {
                googleDriveManager.deleteBackup(backup.id)
                repository.fetchDriveBackups()
            }
        }
    }
}
