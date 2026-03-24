// ui/screens/bloodpressure/BpExportViewModel.kt
package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.export.BpExportManager
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity
// import com.health.calculator.bmi.tracker.data.preferences.ProfilePreferences
import com.health.calculator.bmi.tracker.data.repository.BloodPressureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class BpExportUiState(
    val totalReadings: Int = 0,
    val dateRange: String = "—",
    val hasLatestReading: Boolean = false,
    val latestReadingText: String = "",
    val latestEntity: BloodPressureEntity? = null,
    val isGeneratingPdf: Boolean = false,
    val isGeneratingCsv: Boolean = false,
    val isGeneratingDoctorReport: Boolean = false,
    val isGeneratingImage: Boolean = false,
    val showSuccess: Boolean = false,
    val successMessage: String = ""
)

class BpExportViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = BloodPressureRepository(database.bloodPressureDao())
    private val exportManager = BpExportManager(application)
    // private val profilePreferences = ProfilePreferences(application)

    private val _uiState = MutableStateFlow(BpExportUiState())
    val uiState: StateFlow<BpExportUiState> = _uiState.asStateFlow()

    private var allReadings: List<BloodPressureEntity> = emptyList()
    private var profileName: String = ""

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.allMainReadings.collect { readings ->
                allReadings = readings

                val dateRange = if (readings.isNotEmpty()) {
                    val newest = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(readings.first().measurementTimestamp),
                        ZoneId.systemDefault()
                    ).format(DateTimeFormatter.ofPattern("MMM dd"))
                    val oldest = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(readings.last().measurementTimestamp),
                        ZoneId.systemDefault()
                    ).format(DateTimeFormatter.ofPattern("MMM dd"))
                    if (readings.size == 1) newest else "$oldest – $newest"
                } else "—"

                val latest = readings.firstOrNull()
                val latestText = latest?.let { "${it.systolic}/${it.diastolic} mmHg" } ?: ""

                _uiState.update {
                    it.copy(
                        totalReadings = readings.size,
                        dateRange = dateRange,
                        hasLatestReading = latest != null,
                        latestReadingText = latestText,
                        latestEntity = latest
                    )
                }
            }
        }

        /*
        viewModelScope.launch {
            profilePreferences.profileFlow.collect { profile ->
                profileName = profile.name ?: ""
            }
        }
        */
    }

    fun onExportPdf() {
        if (allReadings.isEmpty()) return
        _uiState.update { it.copy(isGeneratingPdf = true) }

        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) {
                exportManager.exportToPdf(allReadings, profileName, isDoctorReport = false)
            }
            _uiState.update { it.copy(isGeneratingPdf = false) }

            uri?.let {
                exportManager.shareFile(it, "application/pdf", "Share BP Report")
                _uiState.update { state ->
                    state.copy(showSuccess = true, successMessage = "PDF report generated!")
                }
            }
        }
    }

    fun onExportCsv() {
        if (allReadings.isEmpty()) return
        _uiState.update { it.copy(isGeneratingCsv = true) }

        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) {
                exportManager.exportToCsv(allReadings, profileName)
            }
            _uiState.update { it.copy(isGeneratingCsv = false) }

            uri?.let {
                exportManager.shareFile(it, "text/csv", "Share BP Data")
                _uiState.update { state ->
                    state.copy(showSuccess = true, successMessage = "CSV exported!")
                }
            }
        }
    }

    fun onExportDoctorReport() {
        if (allReadings.isEmpty()) return
        _uiState.update { it.copy(isGeneratingDoctorReport = true) }

        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) {
                exportManager.exportToPdf(allReadings, profileName, isDoctorReport = true)
            }
            _uiState.update { it.copy(isGeneratingDoctorReport = false) }

            uri?.let {
                exportManager.shareFile(it, "application/pdf", "Share Doctor Report")
                _uiState.update { state ->
                    state.copy(showSuccess = true, successMessage = "Doctor report generated!")
                }
            }
        }
    }

    fun onShareLatestAsText() {
        val entity = _uiState.value.latestEntity ?: return
        val text = exportManager.formatReadingAsText(entity)
        exportManager.shareText(text)
    }

    fun onShareLatestAsImage() {
        val entity = _uiState.value.latestEntity ?: return
        _uiState.update { it.copy(isGeneratingImage = true) }

        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) {
                exportManager.createReadingImage(entity)
            }
            _uiState.update { it.copy(isGeneratingImage = false) }

            uri?.let {
                exportManager.shareImage(it)
                _uiState.update { state ->
                    state.copy(showSuccess = true, successMessage = "Image created!")
                }
            }
        }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(showSuccess = false) }
    }
}
