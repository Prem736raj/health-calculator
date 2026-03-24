package com.health.calculator.bmi.tracker.ui.screens.history

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.export.*
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.model.toDisplayEntry
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.health.calculator.bmi.tracker.data.management.DataCleanupManager
import com.health.calculator.bmi.tracker.data.management.UndoableDelete
import com.health.calculator.bmi.tracker.data.management.StorageAnalyzer
import com.health.calculator.bmi.tracker.data.management.DataIntegrityChecker

data class HistoryUiState(
    val groupedEntries: List<GroupedHistoryEntries> = emptyList(),
    val isLoading: Boolean = true,
    val filter: HistoryFilter = HistoryFilter(),
    val sortOption: HistorySortOption = HistorySortOption.NEWEST_FIRST,
    val collapsedGroups: Set<DateGroup> = emptySet(),
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val availableCategories: List<String> = emptyList(),
    val exportIntent: Intent? = null,
    val dialogState: HistoryDialogState = HistoryDialogState.None,
    val exportProgress: ExportProgress = ExportProgress(),
    val currentSchedule: ExportSchedule = ExportSchedule(),
    val showExportSheet: Boolean = false,
    val showScheduleDialog: Boolean = false,
    val undoableDelete: UndoableDelete? = null,
    val showUndoSnackbar: Boolean = false
)

sealed interface HistoryDialogState {
    object None : HistoryDialogState
    data class EditNote(val id: Long, val currentNote: String?) : HistoryDialogState
    data class ConfirmDelete(val ids: Set<Long>) : HistoryDialogState
    object ClearAll : HistoryDialogState
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = HistoryRepository(database.historyDao())
    private val exportManager = DataExportManager.getInstance(application)
    private val scheduleManager = ExportScheduleManager.getInstance(application)
    private val cleanupManager = DataCleanupManager.getInstance(
        application,
        repository,
        StorageAnalyzer.getInstance(application),
        DataIntegrityChecker.getInstance()
    )

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadHistory()
        loadCategories()
        observeExportProgress()
        observeSchedule()
    }

    private fun observeExportProgress() {
        exportManager.exportProgress
            .onEach { progress ->
                _uiState.update { it.copy(exportProgress = progress) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSchedule() {
        scheduleManager.schedule
            .onEach { schedule ->
                _uiState.update { it.copy(currentSchedule = schedule) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadHistory() {
        combine(
            repository.getAllEntries(),
            _searchQuery,
            _uiState.map { it.filter }.distinctUntilChanged(),
            _uiState.map { it.sortOption }.distinctUntilChanged(),
            _uiState.map { it.selectedIds }.distinctUntilChanged(),
            _uiState.map { it.collapsedGroups }.distinctUntilChanged()
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val entries = args[0] as List<HistoryEntry>
            val query = args[1] as String
            val filter = args[2] as HistoryFilter
            val sort = args[3] as HistorySortOption
            val selected = args[4] as Set<Long>
            val collapsed = args[5] as Set<DateGroup>

            var processed = entries.map { it.toDisplayEntry() }

            // Apply Search
            if (query.isNotBlank()) {
                processed = processed.filter {
                    it.calculatorType.displayName.contains(query, ignoreCase = true) ||
                    it.primaryValue.contains(query, ignoreCase = true) ||
                    it.category?.contains(query, ignoreCase = true) == true ||
                    it.note?.contains(query, ignoreCase = true) == true
                }
            }

            // Apply Filter
            if (filter.isActive) {
                processed = processed.filter { entry ->
                    val typeMatch = filter.selectedTypes.isEmpty() || entry.calculatorType in filter.selectedTypes
                    val dateMatch = (filter.startDate == null || entry.timestamp >= filter.startDate) &&
                                   (filter.endDate == null || entry.timestamp <= filter.endDate)
                    val categoryMatch = filter.selectedCategories.isEmpty() || entry.category in filter.selectedCategories
                    typeMatch && dateMatch && categoryMatch
                }
            }

            // Apply Sort
            processed = when (sort) {
                HistorySortOption.NEWEST_FIRST -> processed.sortedByDescending { it.timestamp }
                HistorySortOption.OLDEST_FIRST -> processed.sortedBy { it.timestamp }
                HistorySortOption.VALUE_HIGHEST -> processed.sortedByDescending { it.primaryValue.toDoubleOrNull() ?: 0.0 }
                HistorySortOption.VALUE_LOWEST -> processed.sortedBy { it.primaryValue.toDoubleOrNull() ?: 0.0 }
                HistorySortOption.TYPE_ALPHABETICAL -> processed.sortedBy { it.calculatorType.displayName }
            }

            // Update selection state
            processed = processed.map { it.copy(isSelected = it.id in selected) }

            // Group by date
            val grouped = processed.groupBy { DateGroup.fromTimestamp(it.timestamp) }
                .map { (group, groupEntries) ->
                    GroupedHistoryEntries(
                        dateGroup = group,
                        entries = groupEntries,
                        isCollapsed = group in collapsed
                    )
                }
                .sortedBy { it.dateGroup.order }

            grouped
        }
        .onEach { groups ->
            _uiState.update { it.copy(groupedEntries = groups, isLoading = false) }
        }
        .launchIn(viewModelScope)
    }

    fun onEntryClick(id: Long) {
        if (_uiState.value.isSelectionMode) {
            toggleSelection(id)
        }
    }

    fun onEntryLongClick(id: Long) {
        toggleSelection(id)
    }

    fun onNoteClick(id: Long, currentNote: String?) {
        editNote(id, currentNote)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            database.historyDao().getDistinctCategories().collect { categories ->
                _uiState.update { it.copy(availableCategories = categories) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun setSortOption(sort: HistorySortOption) {
        _uiState.update { it.copy(sortOption = sort) }
    }

    fun toggleGroupCollapse(group: DateGroup) {
        _uiState.update { state ->
            val newCollapsed = if (group in state.collapsedGroups)
                state.collapsedGroups - group
            else
                state.collapsedGroups + group
            state.copy(collapsedGroups = newCollapsed)
        }
    }

    fun toggleSelection(id: Long) {
        _uiState.update { state ->
            val newSelected = if (id in state.selectedIds)
                state.selectedIds - id
            else
                state.selectedIds + id
            
            state.copy(
                selectedIds = newSelected,
                isSelectionMode = newSelected.isNotEmpty()
            )
        }
    }

    fun selectAll() {
        val allIds = _uiState.value.groupedEntries.flatMap { it.entries }.map { it.id }.toSet()
        _uiState.update { it.copy(selectedIds = allIds, isSelectionMode = true) }
    }

    fun deselectAll() {
        _uiState.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
    }

    fun requestDeleteSelected() {
        val selected = _uiState.value.selectedIds
        if (selected.isNotEmpty()) {
            _uiState.update { it.copy(dialogState = HistoryDialogState.ConfirmDelete(selected)) }
        }
    }

    fun confirmDelete() {
        val dialog = _uiState.value.dialogState
        if (dialog is HistoryDialogState.ConfirmDelete) {
            viewModelScope.launch {
                val ids = dialog.ids
                if (ids.size == 1) {
                    val id = ids.first()
                    val data = cleanupManager.softDeleteEntry(id)
                    if (data != null) {
                        _uiState.update {
                            it.copy(
                                dialogState = HistoryDialogState.None,
                                undoableDelete = UndoableDelete(entryId = id, entryData = data),
                                showUndoSnackbar = true
                            )
                        }
                    } else {
                        repository.deleteEntry(id)
                        _uiState.update { it.copy(dialogState = HistoryDialogState.None) }
                    }
                } else {
                    ids.forEach { repository.deleteEntry(it) }
                    _uiState.update { it.copy(dialogState = HistoryDialogState.None) }
                }
                deselectAll()
            }
        }
    }

    fun undoDelete() {
        val undoable = _uiState.value.undoableDelete ?: return
        viewModelScope.launch {
            cleanupManager.restoreEntry(undoable.entryData)
            _uiState.update { it.copy(undoableDelete = null, showUndoSnackbar = false) }
        }
    }

    fun clearUndoSnackbar() {
        _uiState.update { it.copy(undoableDelete = null, showUndoSnackbar = false) }
    }

    fun deleteEntry(id: Long) {
        _uiState.update { it.copy(dialogState = HistoryDialogState.ConfirmDelete(setOf(id))) }
    }

    fun editNote(id: Long, currentNote: String?) {
        _uiState.update { it.copy(dialogState = HistoryDialogState.EditNote(id, currentNote)) }
    }

    fun saveNote(id: Long, note: String) {
        viewModelScope.launch {
            repository.updateNote(id, note)
            _uiState.update { it.copy(dialogState = HistoryDialogState.None) }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = HistoryDialogState.None) }
    }

    fun exportSelected() {
        val selectedEntries = _uiState.value.groupedEntries
            .flatMap { it.entries }
            .filter { it.isSelected }

        if (selectedEntries.isEmpty()) return

        val exportText = buildString {
            append("Health Calculator History Export\n")
            append("Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}\n\n")
            selectedEntries.forEach { entry ->
                append("${entry.formattedDate} ${entry.formattedTime}: ${entry.calculatorType.displayName}\n")
                append("Result: ${entry.primaryValue} ${entry.primaryLabel}\n")
                entry.category?.let { append("Category: $it\n") }
                entry.note?.let { append("Note: $it\n") }
                append("-------------------\n")
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Health Calculator History Export")
            putExtra(Intent.EXTRA_TEXT, exportText)
        }
        
        _uiState.update { it.copy(exportIntent = intent) }
    }

    fun clearExportIntent() {
        _uiState.update { it.copy(exportIntent = null) }
    }

    fun saveHistoryEntry(entry: HistoryEntry) {
        viewModelScope.launch {
            repository.addEntry(entry)
        }
    }

    // Export Actions
    fun showExportSheet(show: Boolean) {
        _uiState.update { it.copy(showExportSheet = show) }
    }

    fun showScheduleDialog(show: Boolean) {
        _uiState.update { it.copy(showScheduleDialog = show) }
    }

    fun startExport(config: ExportConfig) {
        viewModelScope.launch {
            val finalEntries = when {
                _uiState.value.isSelectionMode -> {
                    val selectedIds = _uiState.value.selectedIds
                    _uiState.value.groupedEntries
                        .flatMap { it.entries }
                        .filter { it.id in selectedIds }
                }
                config.scope == ExportScope.CALCULATOR && config.calculatorType != null -> {
                    _uiState.value.groupedEntries
                        .flatMap { it.entries }
                        .filter { it.calculatorType == config.calculatorType }
                }
                config.scope == ExportScope.FILTERED -> {
                    _uiState.value.groupedEntries.flatMap { it.entries }
                }
                else -> {
                    repository.getAllEntries().first().map { it.toDisplayEntry() }
                }
            }

            if (finalEntries.isEmpty()) return@launch

            exportManager.exportData(finalEntries, config)
            _uiState.update { it.copy(showExportSheet = false) }
        }
    }

    fun shareExportedFile() {
        val progress = _uiState.value.exportProgress
        if (progress.isComplete && progress.resultUri != null) {
            // We need to know which format was used. 
            // For now, assume PDF or get from last config if we stored it.
            // Simplified: Use a generic share if possible or just PDF.
            exportManager.shareFile(progress.resultUri, ExportFormat.PDF)
        }
    }

    fun generateAndShareReportCard(card: HealthReportCard) {
        viewModelScope.launch {
            val uri = exportManager.generateShareImage(card)
            uri?.let { exportManager.shareImage(it) }
        }
    }

    fun updateSchedule(schedule: ExportSchedule) {
        viewModelScope.launch {
            scheduleManager.updateSchedule(schedule)
            _uiState.update { it.copy(showScheduleDialog = false) }
        }
    }

    fun resetExportProgress() {
        exportManager.resetProgress()
    }
}
