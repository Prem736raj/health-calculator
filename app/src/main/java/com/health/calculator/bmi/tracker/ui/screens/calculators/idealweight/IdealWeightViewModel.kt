package com.health.calculator.bmi.tracker.ui.screens.calculators.idealweight

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.calculator.IdealWeightCalculator
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale
import kotlin.math.roundToInt

data class IdealWeightInputState(
    val heightText: String = "",
    val heightFeetText: String = "",
    val heightInchesText: String = "",
    val heightCm: Float = 0f,
    val isUnitCm: Boolean = true,
    val ageText: String = "",
    val age: Int = 0,
    val isMale: Boolean = true,
    val isProfileDataUsed: Boolean = false,
    val hasModifiedProfile: Boolean = false
)

data class IdealWeightValidationState(
    val heightError: String? = null,
    val ageError: String? = null,
    val hasAttemptedCalculation: Boolean = false
) {
    val isValid: Boolean get() = heightError == null && ageError == null
    val hasAnyError: Boolean get() = heightError != null || ageError != null
}

data class IdealWeightResultData(
    val robinson: Float,
    val miller: Float,
    val devine: Float,
    val hamwi: Float,
    val healthyRangeMin: Float,
    val healthyRangeMax: Float,
    val heightCm: Float,
    val age: Int,
    val isMale: Boolean,
    val isUnitCm: Boolean,
    val timestamp: Long
)

class IdealWeightViewModel(application: Application) : AndroidViewModel(application) {

    private val _inputState = MutableStateFlow(IdealWeightInputState())
    val inputState: StateFlow<IdealWeightInputState> = _inputState.asStateFlow()

    private val _validationState = MutableStateFlow(IdealWeightValidationState())
    val validationState: StateFlow<IdealWeightValidationState> = _validationState.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    private val _triggerShake = MutableStateFlow(0)
    val triggerShake: StateFlow<Int> = _triggerShake.asStateFlow()

    private val _resultData = MutableStateFlow<IdealWeightResultData?>(null)
    val resultData: StateFlow<IdealWeightResultData?> = _resultData.asStateFlow()

    private val _showResults = MutableStateFlow(false)
    val showResults: StateFlow<Boolean> = _showResults.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private var profilePopulated = false

    private val historyRepository = HistoryRepository(
        AppDatabase.getDatabase(application).historyDao()
    )

    // ============================================================
    // Input Updates
    // ============================================================

    fun updateHeight(text: String) {
        val currentState = _inputState.value
        val parsed = text.toFloatOrNull() ?: 0f
        val heightCm = if (currentState.isUnitCm) parsed else parsed * 2.54f

        _inputState.value = currentState.copy(
            heightText = text,
            heightCm = heightCm,
            hasModifiedProfile = currentState.isProfileDataUsed
        )

        if (_validationState.value.hasAttemptedCalculation) {
            _validationState.value = _validationState.value.copy(
                heightError = IdealWeightCalculator.validateHeight(heightCm)
            )
        }
    }

    fun updateHeightFeet(text: String) {
        val currentState = _inputState.value
        val feet = text.toIntOrNull() ?: 0
        val inches = currentState.heightInchesText.toIntOrNull() ?: 0
        val totalCm = ((feet * 12) + inches) * 2.54f

        _inputState.value = currentState.copy(
            heightFeetText = text,
            heightCm = totalCm,
            hasModifiedProfile = currentState.isProfileDataUsed
        )

        if (_validationState.value.hasAttemptedCalculation) {
            _validationState.value = _validationState.value.copy(
                heightError = IdealWeightCalculator.validateHeight(totalCm)
            )
        }
    }

    fun updateHeightInches(text: String) {
        val currentState = _inputState.value
        val feet = currentState.heightFeetText.toIntOrNull() ?: 0
        val inches = text.toIntOrNull() ?: 0
        val totalCm = ((feet * 12) + inches) * 2.54f

        _inputState.value = currentState.copy(
            heightInchesText = text,
            heightCm = totalCm,
            hasModifiedProfile = currentState.isProfileDataUsed
        )

        if (_validationState.value.hasAttemptedCalculation) {
            _validationState.value = _validationState.value.copy(
                heightError = IdealWeightCalculator.validateHeight(totalCm)
            )
        }
    }

    fun updateAge(text: String) {
        val parsed = text.toIntOrNull() ?: 0
        _inputState.value = _inputState.value.copy(
            ageText = text,
            age = parsed,
            hasModifiedProfile = _inputState.value.isProfileDataUsed
        )

        if (_validationState.value.hasAttemptedCalculation) {
            _validationState.value = _validationState.value.copy(
                ageError = IdealWeightCalculator.validateAge(parsed)
            )
        }
    }

    fun updateGender(isMale: Boolean) {
        _inputState.value = _inputState.value.copy(
            isMale = isMale,
            hasModifiedProfile = _inputState.value.isProfileDataUsed
        )
    }

    fun toggleHeightUnit() {
        val current = _inputState.value
        val newIsCm = !current.isUnitCm

        if (newIsCm) {
            _inputState.value = current.copy(
                isUnitCm = true,
                heightText = if (current.heightCm > 0) String.format(Locale.US, "%.0f", current.heightCm) else ""
            )
        } else {
            val totalInches = current.heightCm / 2.54f
            val feet = (totalInches / 12).toInt()
            val inches = (totalInches % 12).toInt()
            _inputState.value = current.copy(
                isUnitCm = false,
                heightFeetText = if (current.heightCm > 0) "$feet" else "",
                heightInchesText = if (current.heightCm > 0) "$inches" else ""
            )
        }
    }

    // ============================================================
    // Profile Data
    // ============================================================

    fun populateFromProfile(
        heightCm: Float,
        age: Int,
        isMale: Boolean,
        isUnitCm: Boolean
    ) {
        if (profilePopulated) return
        if (heightCm <= 0 && age <= 0) return

        val heightText = if (heightCm > 0 && isUnitCm) {
            String.format(Locale.US, "%.0f", heightCm)
        } else ""

        val totalInches = heightCm / 2.54f
        val feetText = if (heightCm > 0 && !isUnitCm) "${(totalInches / 12).toInt()}" else ""
        val inchesText = if (heightCm > 0 && !isUnitCm) "${(totalInches % 12).toInt()}" else ""

        _inputState.value = IdealWeightInputState(
            heightText = heightText,
            heightCm = heightCm,
            heightFeetText = feetText,
            heightInchesText = inchesText,
            isUnitCm = isUnitCm,
            ageText = if (age > 0) "$age" else "",
            age = age,
            isMale = isMale,
            isProfileDataUsed = true,
            hasModifiedProfile = false
        )

        profilePopulated = true
    }

    // ============================================================
    // Calculation
    // ============================================================

    fun onCalculate(): Boolean {
        val input = _inputState.value

        val heightError = IdealWeightCalculator.validateHeight(input.heightCm)
        val ageError = IdealWeightCalculator.validateAge(input.age)

        _validationState.value = IdealWeightValidationState(
            heightError = heightError,
            ageError = ageError,
            hasAttemptedCalculation = true
        )

        if (heightError != null || ageError != null) {
            _triggerShake.value += 1
            return false
        }

        viewModelScope.launch {
            _isCalculating.value = true
            _showResults.value = false
            _isSaved.value = false

            delay(600) // Aesthetic delay

            val robinson = IdealWeightCalculator.calculateRobinson(input.heightCm, input.isMale)
            val miller = IdealWeightCalculator.calculateMiller(input.heightCm, input.isMale)
            val devine = IdealWeightCalculator.calculateDevine(input.heightCm, input.isMale)
            val hamwi = IdealWeightCalculator.calculateHamwi(input.heightCm, input.isMale)
            val (rangeMin, rangeMax) = IdealWeightCalculator.calculateHealthyRange(input.heightCm)

            _resultData.value = IdealWeightResultData(
                robinson = robinson,
                miller = miller,
                devine = devine,
                hamwi = hamwi,
                healthyRangeMin = rangeMin,
                healthyRangeMax = rangeMax,
                heightCm = input.heightCm,
                age = input.age,
                isMale = input.isMale,
                isUnitCm = input.isUnitCm,
                timestamp = System.currentTimeMillis()
            )

            _isCalculating.value = false
            delay(100)
            _showResults.value = true
        }
        return true
    }

    // ============================================================
    // History & Sharing
    // ============================================================

    fun saveToHistory() {
        val result = _resultData.value ?: return
        if (_isSaved.value) return

        viewModelScope.launch {
            val json = JSONObject().apply {
                put("robinson", result.robinson)
                put("miller", result.miller)
                put("devine", result.devine)
                put("hamwi", result.hamwi)
                put("rangeMin", result.healthyRangeMin)
                put("rangeMax", result.healthyRangeMax)
                put("heightCm", result.heightCm)
                put("isMale", result.isMale)
                put("age", result.age)
            }

            val historyEntry = HistoryEntry(
                calculatorKey = CalculatorType.IBW.key,
                resultValue = result.devine.toString(),
                resultLabel = "kg",
                category = "Devine Formula",
                detailsJson = json.toString(),
                timestamp = System.currentTimeMillis()
            )

            historyRepository.addEntry(historyEntry)
            _isSaved.value = true
            _saveSuccess.value = true
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    fun getShareText(): String {
        val res = _resultData.value ?: return ""
        val unit = if (res.isUnitCm) "${res.heightCm.toInt()}cm" else {
            val totalIn = res.heightCm / 2.54f
            "${(totalIn / 12).toInt()}ft ${(totalIn % 12).toInt()}in"
        }
        
        return buildString {
            append("🎯 Ideal Body Weight Results\n")
            append("Height: $unit | Age: ${res.age} | ${if (res.isMale) "Male" else "Female"}\n\n")
            append("━━━━━━━━━━━━━━━━\n")
            append("📍 Devine (Most used): ${String.format(Locale.US, "%.1f", res.devine)} kg\n")
            append("📍 Robinson: ${String.format(Locale.US, "%.1f", res.robinson)} kg\n")
            append("📍 Miller: ${String.format(Locale.US, "%.1f", res.miller)} kg\n")
            append("📍 Hamwi: ${String.format(Locale.US, "%.1f", res.hamwi)} kg\n")
            append("━━━━━━━━━━━━━━━━\n\n")
            append("💪 WHO Healthy Range: ${String.format(Locale.US, "%.1f", res.healthyRangeMin)} - ${String.format(Locale.US, "%.1f", res.healthyRangeMax)} kg\n\n")
            append("Calculated via Health Calculator App")
        }
    }

    fun recalculate() {
        _showResults.value = false
        _resultData.value = null
        _isSaved.value = false
        _validationState.value = _validationState.value.copy(hasAttemptedCalculation = false)
    }

    fun clearAll() {
        _inputState.value = IdealWeightInputState()
        _validationState.value = IdealWeightValidationState()
        _resultData.value = null
        _showResults.value = false
        _isSaved.value = false
        _isCalculating.value = false
        profilePopulated = false
    }
}
