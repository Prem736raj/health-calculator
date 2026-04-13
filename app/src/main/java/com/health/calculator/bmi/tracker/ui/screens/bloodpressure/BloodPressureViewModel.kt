package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.BloodPressureRepository
import com.health.calculator.bmi.tracker.data.repository.ProfileRepository
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository as MainHistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class BloodPressureUiState(
    // Input fields
    val systolic: String = "",
    val diastolic: String = "",
    val pulse: String = "",
    val selectedArm: BpArm? = null,
    val selectedPosition: BpPosition? = null,
    val selectedTimeOfDay: BpTimeOfDay = BloodPressureCalculator.getCurrentTimeOfDay(),
    val measurementTime: LocalDateTime = LocalDateTime.now(),
    val measurementTimeFormatted: String = LocalDateTime.now().format(
        DateTimeFormatter.ofPattern("hh:mm a")
    ),
    val note: String = "",

    // Validation errors
    val systolicError: String? = null,
    val diastolicError: String? = null,
    val crossFieldError: String? = null,
    val pulseError: String? = null,

    // UI state
    val showResult: Boolean = false,
    val showEmergencyWarning: Boolean = false,
    val showEmergencyDialog: Boolean = false,
    val isCalculating: Boolean = false,
    val showTimePicker: Boolean = false,
    val showSaveSuccess: Boolean = false,
    val showNoteDialog: Boolean = false,
    val editingNoteForId: Long? = null,
    val editingNoteText: String = "",

    // Result
    val result: BloodPressureReading? = null,
    val savedReadingId: Long? = null,
    val pulsePressureAnalysis: PulsePressureAnalysis? = null,
    val mapAnalysis: MapAnalysis? = null,
    val heartRateAnalysis: HeartRateAnalysis? = null,
    val riskLevel: BpRiskLevel? = null,
    val gaugePosition: Float = 0f,

    // Multi-reading average
    val isMultiReadingMode: Boolean = false,
    val currentReadings: List<BloodPressureReading> = emptyList(),
    val averagedReading: BloodPressureReading? = null,
    val showAverageResult: Boolean = false,

    // Previous reading hints
    val previousSystolicHint: String? = null,
    val previousDiastolicHint: String? = null,
    val previousPulseHint: String? = null,

    // Profile data
    val profileAge: Int? = null,
    val profileGender: String? = null,

    // Reminders & Streaks
    val onMedication: Boolean = false,
    val medicationName: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val showDoctorSuggestion: Boolean = false,
    val showMilestoneCelebration: Boolean = false,
    val milestoneMessage: String? = null,
    val quickLogSuggestion: QuickLogSuggestion? = null,
    val showQuickLog: Boolean = false,
    val showEdgeCaseWarning: Boolean = false
) {
    fun calculateAge(birthMillis: Long): Int {
        val birthDate = java.time.Instant.ofEpochMilli(birthMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
            .let { (java.time.LocalDate.now().format(it).toInt() - birthDate.format(it).toInt()) / 10000 }
    }
}

class BloodPressureViewModel(
    application: Application,
    private val milestoneEvaluationUseCase: com.health.calculator.bmi.tracker.domain.usecases.MilestoneEvaluationUseCase
) : AndroidViewModel(application) {

    private val profileRepository = ProfileRepository(ProfileDataStore(application))
    private val database = AppDatabase.getDatabase(application)
    private val repository = BloodPressureRepository(database.bloodPressureDao())
    private val mainHistoryRepository = MainHistoryRepository(AppDatabase.getDatabase(application).historyDao())
    private val bpPrefs = com.health.calculator.bmi.tracker.data.preferences.BpReminderPreferences(application)

    class Factory(
        private val application: Application,
        private val milestoneEvaluationUseCase: com.health.calculator.bmi.tracker.domain.usecases.MilestoneEvaluationUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BloodPressureViewModel(application, milestoneEvaluationUseCase) as T
        }
    }

    private val _uiState = MutableStateFlow(BloodPressureUiState())
    val uiState: StateFlow<BloodPressureUiState> = _uiState.asStateFlow()

    private val _lastPulseReading = MutableStateFlow<Int?>(null)
    val lastPulseReading: StateFlow<Int?> = _lastPulseReading.asStateFlow()

    init {
        loadProfileData()
        loadPreviousReadingHints()
        loadBpPreferences()
        generateQuickLogSuggestion()
    }

    private fun loadBpPreferences() {
        viewModelScope.launch {
            bpPrefs.settingsFlow.collect { settings ->
                _uiState.update { state ->
                    state.copy(
                        onMedication = settings.onMedication,
                        medicationName = settings.medicationName,
                        currentStreak = settings.currentStreak,
                        longestStreak = settings.longestStreak,
                        showDoctorSuggestion = com.health.calculator.bmi.tracker.data.manager.BpStreakManager.shouldSuggestDoctorVisit(
                            settings.consecutiveHypertensionCount,
                            settings.doctorVisitDismissedAt
                        )
                    )
                }
            }
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            profileRepository.profileFlow.collect { profile ->
                _uiState.update { state ->
                    state.copy(
                        profileAge = calculateAge(profile.dateOfBirthMillis),
                        profileGender = profile.gender.name
                    )
                }
            }
        }
    }

    private fun loadPreviousReadingHints() {
        viewModelScope.launch {
            val latest = repository.getLatestReading()
            latest?.let { entity ->
                _uiState.update {
                    it.copy(
                        previousSystolicHint = entity.systolic.toString(),
                        previousDiastolicHint = entity.diastolic.toString(),
                        previousPulseHint = entity.pulse?.toString()
                    )
                }
                _lastPulseReading.value = entity.pulse
            }
        }
    }

    private fun generateQuickLogSuggestion() {
        viewModelScope.launch {
            val recentReadings = repository.getRecentReadings(10)
            if (recentReadings.size < 3) return@launch // Need pattern data

            // Analyze patterns
            val currentTimeOfDay = BloodPressureCalculator.getCurrentTimeOfDay()

            // Find most common position and arm
            val commonPosition = recentReadings
                .mapNotNull { it.position }
                .groupBy { it }
                .maxByOrNull { it.value.size }
                ?.key?.let {
                    try { BpPosition.valueOf(it) } catch (e: Exception) { null }
                }

            val commonArm = recentReadings
                .mapNotNull { it.arm }
                .groupBy { it }
                .maxByOrNull { it.value.size }
                ?.key?.let {
                    try { BpArm.valueOf(it) } catch (e: Exception) { null }
                }

            // Check if they usually measure at this time of day
            val todMatches = recentReadings.count {
                it.timeOfDay == currentTimeOfDay.name
            }

            if (todMatches >= 2) {
                val emoji = when (currentTimeOfDay) {
                    BpTimeOfDay.MORNING -> "🌅"
                    BpTimeOfDay.AFTERNOON -> "☀️"
                    BpTimeOfDay.EVENING -> "🌆"
                    BpTimeOfDay.NIGHT -> "🌙"
                }

                val posLabel = commonPosition?.let { ", ${it.displayName}" } ?: ""
                val armLabel = commonArm?.let { ", ${it.displayName}" } ?: ""

                val suggestion = QuickLogSuggestion(
                    timeOfDay = currentTimeOfDay,
                    position = commonPosition,
                    arm = commonArm,
                    label = "Your usual ${currentTimeOfDay.displayName.lowercase()} reading$posLabel$armLabel",
                    emoji = emoji
                )

                _uiState.update {
                    it.copy(
                        quickLogSuggestion = suggestion,
                        showQuickLog = true
                    )
                }
            }
        }
    }

    fun onQuickLogApplied(suggestion: QuickLogSuggestion) {
        _uiState.update {
            it.copy(
                selectedTimeOfDay = suggestion.timeOfDay,
                selectedPosition = suggestion.position,
                selectedArm = suggestion.arm,
                showQuickLog = false,
                measurementTime = java.time.LocalDateTime.now(),
                measurementTimeFormatted = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
                )
            )
        }
    }

    fun onDismissQuickLog() {
        _uiState.update { it.copy(showQuickLog = false) }
    }

    fun onSystolicChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(3)
        _uiState.update { state ->
            val sys = filtered.toIntOrNull()
            val dia = state.diastolic.toIntOrNull()
            state.copy(
                systolic = filtered,
                systolicError = null,
                crossFieldError = null,
                showResult = false,
                showAverageResult = false,
                showEmergencyWarning = sys != null && dia != null &&
                        BloodPressureCalculator.isEmergencyReading(sys, dia)
            )
        }
    }

    fun onDiastolicChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(3)
        _uiState.update { state ->
            val sys = state.systolic.toIntOrNull()
            val dia = filtered.toIntOrNull()
            state.copy(
                diastolic = filtered,
                diastolicError = null,
                crossFieldError = null,
                showResult = false,
                showAverageResult = false,
                showEmergencyWarning = sys != null && dia != null &&
                        BloodPressureCalculator.isEmergencyReading(sys, dia)
            )
        }
    }

    fun onPulseChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(3)
        _uiState.update {
            it.copy(
                pulse = filtered,
                pulseError = null,
                showResult = false,
                showAverageResult = false
            )
        }
    }

    fun onArmSelected(arm: BpArm?) {
        _uiState.update { it.copy(selectedArm = arm) }
    }

    fun onPositionSelected(position: BpPosition?) {
        _uiState.update { it.copy(selectedPosition = position) }
    }

    fun onTimeOfDaySelected(timeOfDay: BpTimeOfDay) {
        _uiState.update { it.copy(selectedTimeOfDay = timeOfDay) }
    }

    fun onMeasurementTimeChanged(hour: Int, minute: Int) {
        val newTime = _uiState.value.measurementTime
            .withHour(hour)
            .withMinute(minute)
        _uiState.update {
            it.copy(
                measurementTime = newTime,
                measurementTimeFormatted = newTime.format(
                    DateTimeFormatter.ofPattern("hh:mm a")
                ),
                showTimePicker = false
            )
        }
    }

    fun onShowTimePicker(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onCheckPressed() {
        val state = _uiState.value

        val systolicError = BloodPressureCalculator.validateSystolic(state.systolic)
        val diastolicError = BloodPressureCalculator.validateDiastolic(state.diastolic)
        val pulseError = BloodPressureCalculator.validatePulse(state.pulse)
        val crossFieldError = if (systolicError == null && diastolicError == null) {
            BloodPressureCalculator.validateSystolicOverDiastolic(state.systolic, state.diastolic)
        } else null

        if (systolicError != null || diastolicError != null || pulseError != null || crossFieldError != null) {
            _uiState.update {
                it.copy(
                    systolicError = systolicError,
                    diastolicError = diastolicError,
                    pulseError = pulseError,
                    crossFieldError = crossFieldError
                )
            }
            return
        }

        val systolic = state.systolic.toInt()
        val diastolic = state.diastolic.toInt()
        val pulse = state.pulse.toIntOrNull()

        val category = BloodPressureCalculator.categorize(systolic, diastolic)
        val riskLevel = BloodPressureCalculator.getRiskLevel(category)
        val ppAnalysis = BpAdvancedMetrics.analyzePulsePressure(systolic, diastolic)
        val mapAnalysis = BpAdvancedMetrics.analyzeMAP(systolic, diastolic)
        val hrAnalysis = pulse?.let { BpAdvancedMetrics.analyzeHeartRate(it) }
        val gaugePos = BloodPressureCalculator.getGaugePosition(systolic, diastolic)

        val reading = BloodPressureReading(
            systolic = systolic,
            diastolic = diastolic,
            pulse = pulse,
            arm = state.selectedArm,
            position = state.selectedPosition,
            timeOfDay = state.selectedTimeOfDay,
            measurementTime = state.measurementTime,
            category = category,
            riskLevel = riskLevel
        )

        // Determine if there's an edge case to warn about
        val hasEdgeCaseWarning = getBpEdgeCaseWarning(systolic, diastolic) != null

        _uiState.update { it.copy(isCalculating = true, showResult = false) }

        viewModelScope.launch {
            kotlinx.coroutines.delay(600)

            // Auto-save the reading
            val savedId = repository.saveReading(
                reading = reading,
                note = state.note,
                isPartOfAverage = state.isMultiReadingMode,
                onMedication = state.onMedication,
                medicationName = state.medicationName
            )

            // Save to main unified history
            val detailsJson = JSONObject().apply {
                put("systolic", reading.systolic)
                put("diastolic", reading.diastolic)
                put("pulse", reading.pulse ?: -1)
                put("category", reading.category.name)
                put("riskLevel", reading.riskLevel.name)
                put("arm", reading.arm?.name ?: "")
                put("position", reading.position?.name ?: "")
                put("timeOfDay", reading.timeOfDay?.name ?: "")
                put("note", state.note)
                put("onMedication", state.onMedication)
            }

            val mainEntry = HistoryEntry(
                calculatorKey = CalculatorType.BLOOD_PRESSURE.key,
                resultValue = "${reading.systolic}/${reading.diastolic}",
                resultLabel = "mmHg",
                category = reading.category.displayName,
                detailsJson = detailsJson.toString(),
                timestamp = System.currentTimeMillis()
            )
            mainHistoryRepository.addEntry(mainEntry)

            // Milestone Evaluation
            milestoneEvaluationUseCase.onBpRecorded(reading.systolic.toDouble(), reading.category.displayName)

            // Update streak
            val settings = bpPrefs.settingsFlow.first()
            val streakResult = com.health.calculator.bmi.tracker.data.manager.BpStreakManager.calculateStreakUpdate(settings)
            
            bpPrefs.updateStreak(
                streakResult.currentStreak,
                streakResult.longestStreak,
                streakResult.lastMeasurementDate,
                if (settings.streakStartDate.isEmpty()) streakResult.lastMeasurementDate else settings.streakStartDate
            )

            // Update hypertension count for doctor suggestion
            val isHypertensive = com.health.calculator.bmi.tracker.data.manager.BpStreakManager.isHypertensionCategory(reading.category.name)
            if (isHypertensive) {
                bpPrefs.updateConsecutiveHypertension(settings.consecutiveHypertensionCount + 1)
            } else {
                bpPrefs.updateConsecutiveHypertension(0)
            }

            // Add to multi-reading list if in multi mode
            val updatedReadings = if (state.isMultiReadingMode) {
                state.currentReadings + reading
            } else {
                listOf(reading)
            }

            _uiState.update {
                it.copy(
                    result = reading,
                    savedReadingId = savedId,
                    pulsePressureAnalysis = ppAnalysis,
                    mapAnalysis = mapAnalysis,
                    heartRateAnalysis = hrAnalysis,
                    riskLevel = riskLevel,
                    gaugePosition = gaugePos,
                    showResult = true,
                    isCalculating = false,
                    showSaveSuccess = true,
                    showEmergencyDialog = riskLevel == BpRiskLevel.EMERGENCY,
                    currentReadings = updatedReadings,
                    showMilestoneCelebration = streakResult.isNewMilestone,
                    milestoneMessage = streakResult.milestoneMessage,
                    showEdgeCaseWarning = hasEdgeCaseWarning
                )
            }

            // Load hints again to update with the newly saved reading
            loadPreviousReadingHints()

            // Dismiss save success after a delay
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(showSaveSuccess = false) }
        }
    }

    fun onTakeAnotherReading() {
        _uiState.update {
            it.copy(
                systolic = "",
                diastolic = "",
                pulse = "",
                note = "",
                showResult = false,
                showAverageResult = false,
                isMultiReadingMode = true,
                measurementTime = LocalDateTime.now(),
                measurementTimeFormatted = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("hh:mm a")
                ),
                selectedTimeOfDay = BloodPressureCalculator.getCurrentTimeOfDay()
            )
        }
    }

    fun onShowAverage() {
        val readings = _uiState.value.currentReadings
        if (readings.size < 2) return

        val avgSystolic = readings.map { it.systolic }.average().toInt()
        val avgDiastolic = readings.map { it.diastolic }.average().toInt()
        val avgPulse = readings.mapNotNull { it.pulse }.let {
            if (it.isNotEmpty()) it.average().toInt() else null
        }

        val category = BloodPressureCalculator.categorize(avgSystolic, avgDiastolic)
        val riskLevel = BloodPressureCalculator.getRiskLevel(category)
        val ppAnalysis = BpAdvancedMetrics.analyzePulsePressure(avgSystolic, avgDiastolic)
        val mapAnalysis = BpAdvancedMetrics.analyzeMAP(avgSystolic, avgDiastolic)
        val hrAnalysis = avgPulse?.let { BpAdvancedMetrics.analyzeHeartRate(it) }
        val gaugePos = BloodPressureCalculator.getGaugePosition(avgSystolic, avgDiastolic)

        val averagedReading = BloodPressureReading(
            systolic = avgSystolic,
            diastolic = avgDiastolic,
            pulse = avgPulse,
            arm = readings.last().arm,
            position = readings.last().position,
            timeOfDay = readings.last().timeOfDay,
            measurementTime = LocalDateTime.now(),
            category = category,
            riskLevel = riskLevel
        )

        viewModelScope.launch {
            repository.saveAveragedReadings(
                individualReadings = readings,
                averagedReading = averagedReading,
                note = "Average of ${readings.size} readings"
            )
            
            // Save averaged result to main history
            val detailsJson = JSONObject().apply {
                put("systolic", averagedReading.systolic)
                put("diastolic", averagedReading.diastolic)
                put("pulse", averagedReading.pulse ?: -1)
                put("category", averagedReading.category.name)
                put("riskLevel", averagedReading.riskLevel.name)
                put("isAveraged", true)
                put("readingsCount", readings.size)
            }

            val mainEntry = HistoryEntry(
                calculatorKey = CalculatorType.BLOOD_PRESSURE.key,
                resultValue = "${averagedReading.systolic}/${averagedReading.diastolic}",
                resultLabel = "mmHg (Avg)",
                category = averagedReading.category.displayName,
                detailsJson = detailsJson.toString(),
                timestamp = System.currentTimeMillis()
            )
            mainHistoryRepository.addEntry(mainEntry)

            // Refresh hints
            loadPreviousReadingHints()
        }

        _uiState.update {
            it.copy(
                averagedReading = averagedReading,
                showAverageResult = true,
                result = averagedReading,
                pulsePressureAnalysis = ppAnalysis,
                mapAnalysis = mapAnalysis,
                heartRateAnalysis = hrAnalysis,
                riskLevel = riskLevel,
                gaugePosition = gaugePos,
                showResult = true,
                isMultiReadingMode = false,
                currentReadings = emptyList() // clear current readings
            )
        }
    }

    fun onShowNoteDialog(readingId: Long? = null, currentNote: String = "") {
        _uiState.update {
            it.copy(
                showNoteDialog = true,
                editingNoteForId = readingId,
                editingNoteText = currentNote
            )
        }
    }

    fun onNoteDialogTextChange(text: String) {
        _uiState.update { it.copy(editingNoteText = text) }
    }

    fun onSaveNote() {
        val state = _uiState.value
        val id = state.editingNoteForId ?: state.savedReadingId

        if (id != null) {
            viewModelScope.launch {
                repository.updateNote(id, state.editingNoteText)
            }
        }

        _uiState.update {
            it.copy(
                showNoteDialog = false,
                note = state.editingNoteText,
                editingNoteForId = null,
                editingNoteText = ""
            )
        }
    }

    fun onDismissNoteDialog() {
        _uiState.update {
            it.copy(
                showNoteDialog = false,
                editingNoteForId = null,
                editingNoteText = ""
            )
        }
    }

    fun dismissEmergencyDialog() {
        _uiState.update { it.copy(showEmergencyDialog = false) }
    }

    fun onClearAll() {
        _uiState.update {
            BloodPressureUiState(
                selectedTimeOfDay = BloodPressureCalculator.getCurrentTimeOfDay(),
                measurementTime = LocalDateTime.now(),
                measurementTimeFormatted = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("hh:mm a")
                ),
                profileAge = it.profileAge,
                profileGender = it.profileGender,
                previousSystolicHint = it.previousSystolicHint,
                previousDiastolicHint = it.previousDiastolicHint,
                previousPulseHint = it.previousPulseHint
            )
        }
    }

    fun dismissSaveSuccess() {
        _uiState.update { it.copy(showSaveSuccess = false) }
    }

    fun onMedicationToggle(enabled: Boolean) {
        _uiState.update { it.copy(onMedication = enabled) }
        viewModelScope.launch { bpPrefs.updateMedication(enabled, _uiState.value.medicationName) }
    }

    fun onMedicationNameChange(name: String) {
        _uiState.update { it.copy(medicationName = name) }
        viewModelScope.launch { bpPrefs.updateMedication(_uiState.value.onMedication, name) }
    }

    fun onDismissDoctorSuggestion() {
        viewModelScope.launch { bpPrefs.dismissDoctorVisitSuggestion() }
    }

    fun onDismissMilestone() {
        _uiState.update { it.copy(showMilestoneCelebration = false, milestoneMessage = null) }
    }

    private fun getBpEdgeCaseWarning(systolic: Int, diastolic: Int): String? {
        return when {
            systolic - diastolic < 10 -> "The difference between systolic and diastolic is unusually small."
            systolic > 250 -> "Extremely high systolic reading detected."
            diastolic > 150 -> "Extremely high diastolic reading detected."
            systolic < 70 -> "Extremely low systolic reading detected."
            diastolic < 40 -> "Extremely low diastolic reading detected."
            else -> null
        }
    }
}
