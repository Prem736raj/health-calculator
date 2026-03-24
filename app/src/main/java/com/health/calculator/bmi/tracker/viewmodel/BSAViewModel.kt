package com.health.calculator.bmi.tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.calculator.BSACalculator
import com.health.calculator.bmi.tracker.calculator.BSAFormulaInfo
import com.health.calculator.bmi.tracker.calculator.BSAResult
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import com.health.calculator.bmi.tracker.data.model.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
data class BSAUiState(
    val weight: String = "",
    val height: String = "",
    val heightFeet: String = "",
    val heightInches: String = "",
    val weightUnitKg: Boolean = true,
    val heightUnitCm: Boolean = true,
    val isMale: Boolean? = null,
    val isFromProfile: Boolean = false,
    val selectedFormulaId: String = "dubois",
    val showResult: Boolean = false,
    val isSaved: Boolean = false,
    val result: BSAResult? = null,
    val weightError: String? = null,
    val heightError: String? = null,

    // Formulas
    val availableFormulas: List<BSAFormulaInfo> = BSACalculator.formulas,

    // Tracking
    val trackingRecords: List<com.health.calculator.bmi.tracker.data.model.BSARecord> = emptyList(),
    val trackingStats: com.health.calculator.bmi.tracker.data.model.BSAStatistics? = null,

    // Validation
    val validationWarnings: List<String> = emptyList()
)

class BSAViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BSAUiState())
    val uiState: StateFlow<BSAUiState> = _uiState.asStateFlow()

    private val historyRepository = HistoryRepository(com.health.calculator.bmi.tracker.data.local.AppDatabase.getDatabase(application).historyDao())
    private val trackingRepository = com.health.calculator.bmi.tracker.data.BSATrackingRepository(application)

    init {
        loadProfileData()
        loadTrackingData()
    }

    private fun loadTrackingData() {
        viewModelScope.launch {
            val records = trackingRepository.getRecords()
            val stats = trackingRepository.getStatistics()
            _uiState.value = _uiState.value.copy(
                trackingRecords = records,
                trackingStats = stats
            )
        }
    }

    private fun loadProfileData() {
        val prefs = getApplication<Application>().getSharedPreferences("user_profile", android.content.Context.MODE_PRIVATE)
        val profileWeight = prefs.getFloat("weight", -1f)
        val profileHeight = prefs.getFloat("height", -1f)
        val profileGender = prefs.getString("gender", null)

        _uiState.value = _uiState.value.copy(
            weight = if (profileWeight > 0) "%.1f".format(profileWeight) else "",
            height = if (profileHeight > 0) "%.1f".format(profileHeight) else "",
            isFromProfile = profileWeight > 0 && profileHeight > 0,
            isMale = when (profileGender) {
                "MALE", "Male" -> true
                "FEMALE", "Female" -> false
                else -> null
            }
        )
    }

    fun updateGender(isMale: Boolean) {
        _uiState.value = _uiState.value.copy(isMale = isMale)
    }

    fun updateWeight(value: String) {
        _uiState.value = _uiState.value.copy(
            weight = value,
            weightError = null,
            showResult = false,
            isFromProfile = false
        )
    }

    fun toggleWeightUnit() {
        val current = _uiState.value
        val currentVal = current.weight.toFloatOrNull()
        val converted = if (currentVal != null) {
            if (current.weightUnitKg) BSACalculator.kgToLbs(currentVal)
            else BSACalculator.lbsToKg(currentVal)
        } else null

        _uiState.value = current.copy(
            weightUnitKg = !current.weightUnitKg,
            weight = converted?.let { "%.1f".format(it) } ?: current.weight
        )
    }

    fun updateHeight(value: String) {
        _uiState.value = _uiState.value.copy(
            height = value,
            heightError = null,
            showResult = false,
            isFromProfile = false
        )
    }

    fun updateHeightFeet(value: String) {
        _uiState.value = _uiState.value.copy(
            heightFeet = value,
            heightError = null,
            showResult = false,
            isFromProfile = false
        )
    }

    fun updateHeightInches(value: String) {
        _uiState.value = _uiState.value.copy(
            heightInches = value,
            heightError = null,
            showResult = false,
            isFromProfile = false
        )
    }

    fun toggleHeightUnit() {
        val current = _uiState.value
        if (current.heightUnitCm) {
            // Converting cm to feet-inches
            val cm = current.height.toFloatOrNull()
            if (cm != null) {
                val (feet, inches) = BSACalculator.cmToFeetInches(cm)
                _uiState.value = current.copy(
                    heightUnitCm = false,
                    heightFeet = "$feet",
                    heightInches = "%.1f".format(inches)
                )
            } else {
                _uiState.value = current.copy(heightUnitCm = false)
            }
        } else {
            // Converting feet-inches to cm
            val feet = current.heightFeet.toIntOrNull() ?: 0
            val inches = current.heightInches.toFloatOrNull() ?: 0f
            val cm = BSACalculator.feetInchesToCm(feet, inches)
            _uiState.value = current.copy(
                heightUnitCm = true,
                height = if (cm > 0) "%.1f".format(cm) else current.height
            )
        }
    }

    fun selectFormula(formulaId: String) {
        _uiState.value = _uiState.value.copy(
            selectedFormulaId = formulaId,
            showResult = false
        )
    }

    fun calculate() {
        val state = _uiState.value

        // Parse weight in kg
        val weightKg = state.weight.toFloatOrNull()?.let {
            if (state.weightUnitKg) it else com.health.calculator.bmi.tracker.calculator.BSACalculator.lbsToKg(it)
        }

        // Parse height in cm
        val heightCm = if (state.heightUnitCm) {
            state.height.toFloatOrNull()
        } else {
            val feet = state.heightFeet.toIntOrNull() ?: 0
            val inches = state.heightInches.toFloatOrNull() ?: 0f
            if (feet > 0 || inches > 0) {
                com.health.calculator.bmi.tracker.calculator.BSACalculator.feetInchesToCm(feet, inches)
            } else null
        }

        // Validate with edge case handler
        val validation = com.health.calculator.bmi.tracker.calculator.BSAEdgeCaseValidator.validate(
            weightKg = weightKg,
            heightCm = heightCm,
            formulaId = state.selectedFormulaId
        )

        if (!validation.isValid) {
            _uiState.value = _uiState.value.copy(
                weightError = validation.weightError,
                heightError = validation.heightError
            )
            return
        }

        // Safe calculation with fallback
        val result = com.health.calculator.bmi.tracker.calculator.BSAEdgeCaseValidator.safeCalculateAll(
            weightKg = weightKg!!,
            heightCm = heightCm!!,
            selectedFormulaId = state.selectedFormulaId
        )

        // Check if result is valid
        if (result.primaryBSA <= 0f) {
            _uiState.value = _uiState.value.copy(
                weightError = "Unable to calculate BSA with these values. Please check your inputs."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            result = result,
            showResult = true,
            isSaved = false,
            weightError = null,
            heightError = null,
            validationWarnings = validation.warnings
        )
    }

    fun saveToHistory() {
        val state = _uiState.value
        val result = state.result ?: return

        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateTime = dateFormat.format(Date())

            // Save to BSA tracking
            val bsaRecord = com.health.calculator.bmi.tracker.data.model.BSARecord(
                timestamp = System.currentTimeMillis(),
                dateTime = dateTime,
                bsaValue = result.primaryBSA,
                formulaId = result.selectedFormula.id,
                formulaName = result.selectedFormula.name,
                weightKg = result.weightKg,
                heightCm = result.heightCm
            )
            trackingRepository.saveRecord(bsaRecord)

            // Save to general history
            val detailsJson = JSONObject().apply {
                put("bsa_value", "%.4f".format(result.primaryBSA))
                put("formula", result.selectedFormula.name)
                put("formula_id", result.selectedFormula.id)
                put("weight_kg", "%.1f".format(result.weightKg))
                put("height_cm", "%.1f".format(result.heightCm))
                put("isMale", state.isMale)
                put("date_time", dateTime)
                result.allResults.forEach { pair ->
                    put("bsa_${pair.first.id}", "%.4f".format(pair.second))
                }
            }

            val entry = HistoryEntry(
                calculatorKey = com.health.calculator.bmi.tracker.data.model.CalculatorType.BSA.key,
                resultValue = "%.2f".format(result.primaryBSA),
                resultLabel = "m²",
                category = result.selectedFormula.label,
                detailsJson = detailsJson.toString(),
                timestamp = System.currentTimeMillis()
            )
            historyRepository.addEntry(entry)

            // Reload tracking data
            loadTrackingData()

            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun resetResult() {
        _uiState.value = _uiState.value.copy(showResult = false, result = null, isSaved = false)
    }

    fun clearAll() {
        _uiState.value = BSAUiState()
    }

    fun getShareText(): String {
        val result = _uiState.value.result ?: return ""
        return "My BSA: ${"%.2f".format(result.primaryBSA)} m² (${result.selectedFormula.name} formula) - Health Calculator"
    }
}
