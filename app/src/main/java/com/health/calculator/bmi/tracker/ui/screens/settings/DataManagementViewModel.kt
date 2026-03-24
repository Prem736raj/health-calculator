package com.health.calculator.bmi.tracker.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.management.*
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import com.health.calculator.bmi.tracker.data.model.toDisplayEntry
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DataManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val historyRepository = HistoryRepository(database.historyDao())
    private val storageAnalyzer = StorageAnalyzer.getInstance(application)
    private val integrityChecker = DataIntegrityChecker.getInstance()
    private val cleanupManager = DataCleanupManager.getInstance(
        application,
        historyRepository,
        storageAnalyzer,
        integrityChecker
    )

    private val _uiState = MutableStateFlow(DataManagementState())
    val uiState: StateFlow<DataManagementState> = _uiState.asStateFlow()

    private var allEntries: List<HistoryDisplayEntry> = emptyList()

    init {
        loadStorageInfo()
        loadEntries()
    }

    private fun loadStorageInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStorage = true) }
            val info = storageAnalyzer.analyzeStorage()
            _uiState.update { it.copy(storageInfo = info, isLoadingStorage = false) }
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            historyRepository.getAllEntries().collect { rawEntries ->
                allEntries = rawEntries.map { with(historyRepository) { it.toDisplayEntry() } }
            }
        }
    }

    // === CLEANUP BY AGE ===

    fun showCleanupByAge() {
        _uiState.update { it.copy(showCleanupByAge = true, selectedCleanupAge = null, cleanupPreview = null) }
    }

    fun dismissCleanupByAge() {
        _uiState.update { it.copy(showCleanupByAge = false, selectedCleanupAge = null, cleanupPreview = null) }
    }

    fun previewCleanupByAge(age: CleanupAge) {
        _uiState.update { it.copy(selectedCleanupAge = age) }
        viewModelScope.launch {
            val preview = cleanupManager.previewCleanupByAge(allEntries, age)
            _uiState.update { it.copy(cleanupPreview = preview) }
        }
    }

    fun executeCleanupByAge() {
        val age = _uiState.value.selectedCleanupAge ?: return
        _uiState.update { it.copy(isClearing = true) }

        viewModelScope.launch {
            val deleted = cleanupManager.cleanupByAge(allEntries, age)
            _uiState.update {
                it.copy(
                    isClearing = false,
                    showCleanupByAge = false,
                    snackbarMessage = "$deleted entries deleted"
                )
            }
            loadStorageInfo()
        }
    }

    // === CLEANUP BY CALCULATOR ===

    fun showCleanupByCalculator() {
        _uiState.update { it.copy(showCleanupByCalculator = true, selectedCleanupTypes = emptySet()) }
    }

    fun dismissCleanupByCalculator() {
        _uiState.update { it.copy(showCleanupByCalculator = false, selectedCleanupTypes = emptySet()) }
    }

    fun toggleCleanupType(type: CalculatorType) {
        _uiState.update { state ->
            val newTypes = if (type in state.selectedCleanupTypes)
                state.selectedCleanupTypes - type
            else
                state.selectedCleanupTypes + type
            state.copy(selectedCleanupTypes = newTypes)
        }
    }

    fun executeCleanupByCalculator() {
        val types = _uiState.value.selectedCleanupTypes
        if (types.isEmpty()) return

        _uiState.update { it.copy(isClearing = true) }

        viewModelScope.launch {
            val deleted = cleanupManager.cleanupByCalculator(allEntries, types)
            _uiState.update {
                it.copy(
                    isClearing = false,
                    showCleanupByCalculator = false,
                    snackbarMessage = "$deleted entries from ${types.size} calculator(s) deleted"
                )
            }
            loadStorageInfo()
        }
    }

    // === CACHE CLEANUP ===

    fun clearCache() {
        viewModelScope.launch {
            val freed = storageAnalyzer.clearCache()
            _uiState.update {
                it.copy(snackbarMessage = "${StorageInfo.formatBytes(freed)} cache cleared")
            }
            loadStorageInfo()
        }
    }

    fun clearExports() {
        viewModelScope.launch {
            val freed = storageAnalyzer.clearExports()
            _uiState.update {
                it.copy(snackbarMessage = "${StorageInfo.formatBytes(freed)} exports cleared")
            }
            loadStorageInfo()
        }
    }

    // === DATA INTEGRITY ===

    fun runIntegrityCheck() {
        _uiState.update {
            it.copy(integrityReport = IntegrityReport(isChecking = true))
        }

        viewModelScope.launch {
            delay(500) // Brief delay for UX
            val report = integrityChecker.checkIntegrity(allEntries)
            _uiState.update { it.copy(integrityReport = report) }
        }
    }

    fun fixIntegrityIssues() {
        viewModelScope.launch {
            val fixed = cleanupManager.fixIntegrityIssues(allEntries)
            _uiState.update { state ->
                state.copy(
                    integrityReport = state.integrityReport.copy(
                        issuesFixed = fixed,
                        corruptedEntries = 0,
                        duplicateEntries = 0,
                        orphanedEntries = 0,
                        statusMessage = "$fixed issues fixed. Data is now clean."
                    ),
                    snackbarMessage = "$fixed issues fixed"
                )
            }
            loadStorageInfo()
        }
    }

    // === DELETE EVERYTHING ===

    fun showDeleteEverything() {
        _uiState.update {
            it.copy(
                showDeleteEverything = true,
                deleteEverythingStep = 0,
                deleteConfirmText = ""
            )
        }
    }

    fun dismissDeleteEverything() {
        _uiState.update {
            it.copy(
                showDeleteEverything = false,
                deleteEverythingStep = 0,
                deleteConfirmText = ""
            )
        }
    }

    fun nextDeleteStep() {
        _uiState.update { it.copy(deleteEverythingStep = it.deleteEverythingStep + 1) }
    }

    fun updateDeleteConfirmText(text: String) {
        _uiState.update { it.copy(deleteConfirmText = text) }
    }

    fun executeDeleteEverything() {
        if (_uiState.value.deleteConfirmText.uppercase() != "DELETE") return

        _uiState.update { it.copy(isDeleting = true) }

        viewModelScope.launch {
            cleanupManager.deleteEverything()

            _uiState.update {
                it.copy(
                    isDeleting = false,
                    showDeleteEverything = false,
                    deleteEverythingStep = 3,
                    operationComplete = true
                )
            }
        }
    }

    // === UNDO DELETE ===

    fun undoDelete() {
        val undoable = _uiState.value.undoableDelete ?: return

        viewModelScope.launch {
            val restored = cleanupManager.restoreEntry(undoable.entryData)
            _uiState.update {
                it.copy(
                    undoableDelete = null,
                    snackbarMessage = if (restored) "Entry restored" else "Failed to restore"
                )
            }
        }
    }

    fun clearUndo() {
        _uiState.update { it.copy(undoableDelete = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
