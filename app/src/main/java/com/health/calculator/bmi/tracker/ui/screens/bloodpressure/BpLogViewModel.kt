package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.local.HealthDatabase
import com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.BloodPressureRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BpLogDetailState(
    val entity: BloodPressureEntity? = null,
    val reading: BloodPressureReading? = null,
    val groupReadings: List<BloodPressureEntity> = emptyList(),
    val isVisible: Boolean = false
)

data class BpLogUiState(
    val readings: List<BloodPressureEntity> = emptyList(),
    val isLoading: Boolean = true,
    val selectedDetail: BpLogDetailState = BpLogDetailState(),
    val showDeleteConfirm: Boolean = false,
    val deletingEntity: BloodPressureEntity? = null,
    val showNoteDialog: Boolean = false,
    val editingNoteId: Long? = null,
    val editingNoteText: String = "",
    val readingsCount: Int = 0
)

class BpLogViewModel(application: Application) : AndroidViewModel(application) {

    private val database = HealthDatabase.getInstance(application)
    private val repository = BloodPressureRepository(database.bloodPressureDao())

    private val _uiState = MutableStateFlow(BpLogUiState())
    val uiState: StateFlow<BpLogUiState> = _uiState.asStateFlow()

    init {
        loadReadings()
    }

    private fun loadReadings() {
        viewModelScope.launch {
            repository.allMainReadings.collect { entities ->
                _uiState.update {
                    it.copy(
                        readings = entities,
                        readingsCount = entities.size,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onReadingClicked(entity: BloodPressureEntity) {
        viewModelScope.launch {
            val groupReadings = entity.averageGroupId?.let {
                repository.getReadingsByGroupId(it)
            } ?: emptyList()

            _uiState.update {
                it.copy(
                    selectedDetail = BpLogDetailState(
                        entity = entity,
                        reading = BloodPressureRepository.entityToReading(entity),
                        groupReadings = groupReadings,
                        isVisible = true
                    )
                )
            }
        }
    }

    fun onDismissDetail() {
        _uiState.update {
            it.copy(selectedDetail = BpLogDetailState())
        }
    }

    fun onDeleteRequested(entity: BloodPressureEntity) {
        _uiState.update {
            it.copy(
                showDeleteConfirm = true,
                deletingEntity = entity
            )
        }
    }

    fun onConfirmDelete() {
        val entity = _uiState.value.deletingEntity ?: return
        viewModelScope.launch {
            repository.deleteReadingWithGroup(entity)
            _uiState.update {
                it.copy(
                    showDeleteConfirm = false,
                    deletingEntity = null,
                    selectedDetail = BpLogDetailState()
                )
            }
        }
    }

    fun onDismissDelete() {
        _uiState.update {
            it.copy(showDeleteConfirm = false, deletingEntity = null)
        }
    }

    fun onEditNote(id: Long, currentNote: String) {
        _uiState.update {
            it.copy(
                showNoteDialog = true,
                editingNoteId = id,
                editingNoteText = currentNote
            )
        }
    }

    fun onNoteTextChange(text: String) {
        _uiState.update { it.copy(editingNoteText = text) }
    }

    fun onSaveNote() {
        val id = _uiState.value.editingNoteId ?: return
        val note = _uiState.value.editingNoteText

        viewModelScope.launch {
            repository.updateNote(id, note)
            _uiState.update {
                it.copy(
                    showNoteDialog = false,
                    editingNoteId = null,
                    editingNoteText = ""
                )
            }
        }
    }

    fun onDismissNoteDialog() {
        _uiState.update {
            it.copy(
                showNoteDialog = false,
                editingNoteId = null,
                editingNoteText = ""
            )
        }
    }
}
