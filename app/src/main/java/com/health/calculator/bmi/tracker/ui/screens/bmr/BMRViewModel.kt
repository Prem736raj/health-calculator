// File: com/health/calculator/bmi/tracker/ui/screens/bmr/BMRViewModel.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.calculator.BMRCalculator
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.model.MacroColors
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import com.health.calculator.bmi.tracker.data.local.HealthDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import com.health.calculator.bmi.tracker.data.model.BMRHistoryPoint
import com.health.calculator.bmi.tracker.data.model.BMRTrendStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject
import com.health.calculator.bmi.tracker.data.calculator.BMRValidation
import com.health.calculator.bmi.tracker.data.preferences.BMRLastValuePreferences
import com.health.calculator.bmi.tracker.ui.screens.bmr.BMRShareFormatter

class BMRViewModel(application: Application) : AndroidViewModel(application) {

    private val _inputState = MutableStateFlow(BMRInputState())
    val inputState: StateFlow<BMRInputState> = _inputState.asStateFlow()

    private val _validationState = MutableStateFlow(BMRValidationState())
    val validationState: StateFlow<BMRValidationState> = _validationState.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    private val _triggerShake = MutableStateFlow(0)
    val triggerShake: StateFlow<Int> = _triggerShake.asStateFlow()

    // Results & State
    private val _resultData = MutableStateFlow<BMRResultData?>(null)
    val resultData: StateFlow<BMRResultData?> = _resultData.asStateFlow()

    private val _showResults = MutableStateFlow(false)
    val showResults: StateFlow<Boolean> = _showResults.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // TDEE & TEF States
    private val _selectedActivityLevel = MutableStateFlow(ActivityLevel.SEDENTARY)
    val selectedActivityLevel: StateFlow<ActivityLevel> = _selectedActivityLevel.asStateFlow()

    private val _currentMacros = MutableStateFlow(Triple(30f, 40f, 30f)) // protein, carbs, fat
    val currentMacros: StateFlow<Triple<Float, Float, Float>> = _currentMacros.asStateFlow()

    private val _tefData = MutableStateFlow<TEFData?>(null)
    val tefData: StateFlow<TEFData?> = _tefData.asStateFlow()

    private val _profileActivityLevel = MutableStateFlow<ActivityLevel?>(null)
    val profileActivityLevel: StateFlow<ActivityLevel?> = _profileActivityLevel.asStateFlow()

    // Profile population flag
    private var profilePopulated = false

    // History Repository
    private val historyRepository = HistoryRepository(
        HealthDatabase.getInstance(application).historyDao()
    )

    private val _bmrHistoryPoints = MutableStateFlow<List<BMRHistoryPoint>>(emptyList())
    val bmrHistoryPoints: StateFlow<List<BMRHistoryPoint>> = _bmrHistoryPoints.asStateFlow()

    private val _bmrTrendStats = MutableStateFlow(BMRTrendStats())
    val bmrTrendStats: StateFlow<BMRTrendStats> = _bmrTrendStats.asStateFlow()

    private val lastValuePreferences = BMRLastValuePreferences(application)

    private val _calculationWarnings = MutableStateFlow<List<String>>(emptyList())
    val calculationWarnings: StateFlow<List<String>> = _calculationWarnings.asStateFlow()

    // Macro state for sharing
    private val _currentMacroBreakdown = MutableStateFlow<MacroBreakdown?>(null)

    private val _currentMealCount = MutableStateFlow(3)
    val currentMealCount: StateFlow<Int> = _currentMealCount.asStateFlow()

    init {
        loadBMRHistory()
    }

    private fun loadBMRHistory() {
        viewModelScope.launch {
            historyRepository.getEntriesByType(CalculatorType.BMR).collect { entries ->
                val points = entries.mapNotNull { entry ->
                    try {
                        val json = JSONObject(entry.detailsJson ?: "{}")
                        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

                        BMRHistoryPoint(
                            bmr = json.optDouble("primaryBMR", json.optDouble("bmr", 0.0)).toFloat(),
                            tdee = json.optDouble("tdee", 0.0).toFloat().let {
                                if (it > 0) it
                                else {
                                    val multiplier = json.optDouble("activityMultiplier", 1.2).toFloat()
                                    json.optDouble("primaryBMR", json.optDouble("bmr", 0.0)).toFloat() * multiplier
                                }
                            },
                            weightKg = json.optDouble("weightKg", 0.0).toFloat(),
                            formulaName = json.optString("selectedFormula", json.optString("formula", "Unknown"))
                                .replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            activityLevel = json.optString("activityLevel", "SEDENTARY")
                                .replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            timestamp = entry.timestamp,
                            dateLabel = dateFormat.format(Date(entry.timestamp))
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.timestamp }

                _bmrHistoryPoints.value = points
                calculateTrendStats(points)
            }
        }
    }

    private fun calculateTrendStats(points: List<BMRHistoryPoint>) {
        if (points.isEmpty()) {
            _bmrTrendStats.value = BMRTrendStats()
            return
        }

        val currentResult = _resultData.value
        val currentPoint = points.lastOrNull()
        val previousPoint = if (points.size > 1) points[points.size - 2] else null

        val bmrValues = points.map { it.bmr }
        val tdeeValues = points.map { it.tdee }

        _bmrTrendStats.value = BMRTrendStats(
            currentBMR = currentResult?.primaryBMR ?: currentPoint?.bmr ?: 0f,
            averageBMR = bmrValues.average().toFloat(),
            highestBMR = bmrValues.maxOrNull() ?: 0f,
            lowestBMR = bmrValues.minOrNull() ?: 0f,
            firstBMR = points.first().bmr,
            changeFromFirst = (currentResult?.primaryBMR ?: currentPoint?.bmr ?: 0f) - points.first().bmr,
            changePercentFromFirst = if (points.first().bmr > 0)
                ((currentResult?.primaryBMR ?: currentPoint?.bmr ?: 0f) - points.first().bmr) / points.first().bmr * 100f
            else 0f,
            currentTDEE = currentResult?.let { (it.primaryBMR * (_selectedActivityLevel.value.multiplier)).toFloat() }
                ?: currentPoint?.tdee ?: 0f,
            averageTDEE = tdeeValues.average().toFloat(),
            totalReadings = points.size,
            previousBMR = previousPoint?.bmr ?: 0f,
            changeFromPrevious = if (previousPoint != null)
                (currentResult?.primaryBMR ?: currentPoint?.bmr ?: 0f) - previousPoint.bmr else 0f,
            previousWeight = previousPoint?.weightKg ?: 0f,
            currentWeight = currentResult?.weightKg ?: currentPoint?.weightKg ?: 0f,
            weightChange = if (previousPoint != null)
                (currentResult?.weightKg ?: currentPoint?.weightKg ?: 0f) - previousPoint.weightKg else 0f
        )
    }

    // ============================================================
    // Input Updates
    // ============================================================

    fun updateWeight(text: String) {
        val currentState = _inputState.value
        val parsed = text.toFloatOrNull() ?: 0f
        val weightKg = if (currentState.isUnitKg) parsed else parsed / 2.20462f

        _inputState.value = currentState.copy(
            weightText = text,
            weightKg = weightKg,
            hasModifiedProfile = currentState.isProfileDataUsed
        )

        if (_validationState.value.hasAttemptedCalculation) {
            _validationState.value = _validationState.value.copy(
                weightError = BMRCalculator.validateWeight(weightKg)
            )
        }
    }

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
                heightError = BMRCalculator.validateHeight(heightCm)
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
                heightError = BMRCalculator.validateHeight(totalCm)
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
                heightError = BMRCalculator.validateHeight(totalCm)
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
                ageError = BMRCalculator.validateAge(parsed)
            )
        }
    }

    fun updateGender(isMale: Boolean) {
        _inputState.value = _inputState.value.copy(
            isMale = isMale,
            hasModifiedProfile = _inputState.value.isProfileDataUsed
        )
    }

    fun updateFormula(formula: BMRFormula) {
        _inputState.value = _inputState.value.copy(selectedFormula = formula)
        if (!formula.requiresBodyFat) {
            _validationState.value = _validationState.value.copy(bodyFatError = null)
        }
    }

    fun updateBodyFat(text: String) {
        val parsed = text.toFloatOrNull() ?: 0f
        _inputState.value = _inputState.value.copy(
            bodyFatText = text,
            bodyFatPercentage = parsed
        )

        if (_validationState.value.hasAttemptedCalculation) {
            _validationState.value = _validationState.value.copy(
                bodyFatError = BMRCalculator.validateBodyFat(
                    parsed,
                    _inputState.value.selectedFormula.requiresBodyFat
                )
            )
        }
    }

    // ============================================================
    // TDEE & TEF Actions
    // ============================================================

    fun updateActivityLevel(level: ActivityLevel) {
        _selectedActivityLevel.value = level
        recalculateTEF()
    }

    fun updateMacros(proteinPct: Float, carbsPct: Float, fatPct: Float) {
        _currentMacros.value = Triple(proteinPct, carbsPct, fatPct)
        recalculateTEF()
    }

    fun recalculateTEF() {
        val result = _resultData.value ?: return
        val level = _selectedActivityLevel.value
        val tdee = (result.primaryBMR * level.multiplier).toFloat()
        val activityCalories = tdee - result.primaryBMR
        val (proteinPct, carbsPct, fatPct) = _currentMacros.value

        val proteinCal = tdee * (proteinPct / 100f)
        val carbsCal = tdee * (carbsPct / 100f)
        val fatCal = tdee * (fatPct / 100f)

        _tefData.value = TEFData.calculate(
            bmr = result.primaryBMR,
            activityCalories = activityCalories,
            tdee = tdee,
            proteinCalories = proteinCal,
            carbsCalories = carbsCal,
            fatCalories = fatCal,
            proteinPct = proteinPct,
            carbsPct = carbsPct,
            fatPct = fatPct
        )
    }

    fun setProfileActivityLevel(activityString: String?) {
        val level = ActivityLevel.fromProfileString(activityString)
        _profileActivityLevel.value = level
        if (level != null && !profilePopulated) {
            _selectedActivityLevel.value = level
        }
    }

    fun getTDEEData(): TDEEData? {
        val result = _resultData.value ?: return null
        val level = _selectedActivityLevel.value
        val tdee = (result.primaryBMR * level.multiplier).toFloat()
        return TDEEData(
            bmr = result.primaryBMR,
            activityLevel = level,
            tdee = tdee,
            activityCalories = tdee - result.primaryBMR
        )
    }

    // ============================================================
    // Unit Toggles
    // ============================================================

    fun toggleWeightUnit() {
        val current = _inputState.value
        val newIsKg = !current.isUnitKg

        val convertedText = if (current.weightKg > 0) {
            if (newIsKg) String.format("%.1f", current.weightKg)
            else String.format("%.1f", current.weightKg * 2.20462f)
        } else ""

        _inputState.value = current.copy(
            isUnitKg = newIsKg,
            weightText = convertedText
        )
    }

    fun toggleHeightUnit() {
        val current = _inputState.value
        val newIsCm = !current.isUnitCm

        if (newIsCm) {
            _inputState.value = current.copy(
                isUnitCm = true,
                heightText = if (current.heightCm > 0) String.format("%.0f", current.heightCm) else ""
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
        weightKg: Float,
        heightCm: Float,
        age: Int,
        isMale: Boolean,
        isUnitKg: Boolean,
        isUnitCm: Boolean,
        activityLevel: String? = null
    ) {
        if (profilePopulated) return
        if (weightKg <= 0 && heightCm <= 0 && age <= 0) return

        val weightText = if (weightKg > 0) {
            if (isUnitKg) String.format("%.1f", weightKg)
            else String.format("%.1f", weightKg * 2.20462f)
        } else ""

        val heightText = if (heightCm > 0 && isUnitCm) {
            String.format("%.0f", heightCm)
        } else ""

        val totalInches = heightCm / 2.54f
        val feetText = if (heightCm > 0 && !isUnitCm) "${(totalInches / 12).toInt()}" else ""
        val inchesText = if (heightCm > 0 && !isUnitCm) "${(totalInches % 12).toInt()}" else ""

        _inputState.value = BMRInputState(
            weightText = weightText,
            weightKg = weightKg,
            isUnitKg = isUnitKg,
            heightText = heightText,
            heightCm = heightCm,
            heightFeetText = feetText,
            heightInchesText = inchesText,
            isUnitCm = isUnitCm,
            ageText = if (age > 0) "$age" else "",
            age = age,
            isMale = isMale,
            selectedFormula = BMRFormula.MIFFLIN_ST_JEOR,
            isProfileDataUsed = true,
            hasModifiedProfile = false
        )
        
        if (activityLevel != null) {
            setProfileActivityLevel(activityLevel)
        }

        profilePopulated = true
    }

    // ============================================================
    // Validation & Calculation Trigger
    // ============================================================

    fun onCalculate(): Boolean {
        val input = _inputState.value

        _validationState.value = _validationState.value.copy(hasAttemptedCalculation = true)

        // Enhanced validation with edge case handling
        val validationResult = BMRValidation.validateInputs(
            weightKg = input.weightKg,
            heightCm = input.heightCm,
            age = input.age,
            bodyFatPercentage = input.bodyFatPercentage,
            formula = input.selectedFormula
        )

        _validationState.value = BMRValidationState(
            weightError = validationResult.weightError,
            heightError = validationResult.heightError,
            ageError = validationResult.ageError,
            bodyFatError = validationResult.bodyFatError,
            hasAttemptedCalculation = true
        )

        _calculationWarnings.value = validationResult.warnings

        if (!validationResult.isValid) {
            _triggerShake.value += 1
            return false
        }

        viewModelScope.launch {
            _isCalculating.value = true
            _showResults.value = false
            _isSaved.value = false

            delay(400)

            // Calculate primary formula
            val primaryResult = BMRCalculator.calculate(
                weightKg = input.weightKg,
                heightCm = input.heightCm,
                age = input.age,
                isMale = input.isMale,
                bodyFatPercentage = input.bodyFatPercentage,
                formula = input.selectedFormula
            )

            // Calculate all available formulas
            val allResults = mutableMapOf<BMRFormula, Float>()
            BMRFormula.entries.forEach { formula ->
                val result = BMRCalculator.calculate(
                    weightKg = input.weightKg,
                    heightCm = input.heightCm,
                    age = input.age,
                    isMale = input.isMale,
                    bodyFatPercentage = input.bodyFatPercentage,
                    formula = formula
                )
                if (result != null && result > 0) {
                    allResults[formula] = result
                }
            }

            // Safety clamp: BMR should be reasonable
            val clampedBMR = primaryResult?.coerceIn(200f, 10000f) ?: 0f

            _resultData.value = BMRResultData(
                primaryBMR = clampedBMR,
                selectedFormula = input.selectedFormula,
                allFormulaResults = allResults,
                weightKg = input.weightKg,
                heightCm = input.heightCm,
                age = input.age,
                isMale = input.isMale,
                bodyFatPercentage = input.bodyFatPercentage,
                isUnitKg = input.isUnitKg,
                isUnitCm = input.isUnitCm,
                timestamp = System.currentTimeMillis()
            )

            _isCalculating.value = false
            delay(100)
            _showResults.value = true

            // Calculate TEF
            recalculateTEF()

            // Save last value for home card
            val tdee = (clampedBMR * _selectedActivityLevel.value.multiplier).toFloat()
            lastValuePreferences.saveLastValue(
                bmr = clampedBMR,
                tdee = tdee,
                formulaName = input.selectedFormula.displayName
            )
        }

        return true
    }

    // ============================================================
    // Result Actions
    // ============================================================

    fun saveToHistory() {
        val result = _resultData.value ?: return
        if (_isSaved.value) return

        val tdeeData = getTDEEData()
        val tef = _tefData.value
        val macros = _currentMacroBreakdown.value

        viewModelScope.launch {
            val historyEntry = HistoryEntry(
                calculatorKey = CalculatorType.BMR.key,
                resultValue = result.primaryBMR.toString(),
                resultLabel = "kcal/day",
                category = buildString {
                    append(result.selectedFormula.displayName)
                    if (tdeeData != null) append(" | TDEE: ${tdeeData.tdee.toInt()}")
                    if (tef != null) append(" | TEF: ${tef.totalTEF.toInt()}")
                },
                detailsJson = buildString {
                    append("{")
                    append("\"weightKg\":${result.weightKg},")
                    append("\"heightCm\":${result.heightCm},")
                    append("\"age\":${result.age},")
                    append("\"isMale\":${result.isMale},")
                    append("\"formula\":\"${result.selectedFormula.name}\"")
                    if (tdeeData != null) {
                        append(",\"tdee\":${tdeeData.tdee},")
                        append("\"activityLevel\":\"${tdeeData.activityLevel.name}\",")
                        append("\"activityMultiplier\":${tdeeData.activityLevel.multiplier}")
                    }
                    if (tef != null) {
                        append(",\"tef\":${tef.totalTEF},")
                        append("\"adjustedTDEE\":${tef.adjustedTDEE}")
                    }
                    if (macros != null) {
                        append(",\"proteinPct\":${macros.proteinPercentage},")
                        append("\"carbsPct\":${macros.carbsPercentage},")
                        append("\"fatPct\":${macros.fatPercentage},")
                        append("\"dietApproach\":\"${macros.dietApproach.name}\"")
                    }
                    append("}")
                },
                timestamp = System.currentTimeMillis()
            )

            historyRepository.addEntry(historyEntry)
            _isSaved.value = true
            _saveSuccess.value = true

            // Update last value for home card
            val tdee = (result.primaryBMR * _selectedActivityLevel.value.multiplier).toFloat()
            lastValuePreferences.saveLastValue(
                bmr = result.primaryBMR,
                tdee = tdee,
                formulaName = result.selectedFormula.displayName
            )

            // Refresh trends
            loadBMRHistory()
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    fun getShareText(): String {
        val result = _resultData.value ?: return ""
        return BMRShareFormatter.formatCompleteResult(
            resultData = result,
            activityLevel = _selectedActivityLevel.value,
            macroBreakdown = _currentMacroBreakdown.value,
            tefData = _tefData.value,
            mealCount = _currentMealCount.value
        )
    }

    fun getQuickShareText(): String {
        val result = _resultData.value ?: return ""
        val level = _selectedActivityLevel.value
        return BMRShareFormatter.formatQuickResult(
            bmr = result.primaryBMR,
            tdee = (result.primaryBMR * level.multiplier).toFloat(),
            formulaName = result.selectedFormula.displayName,
            activityLevel = level.displayName
        )
    }

    fun updateMacroBreakdown(breakdown: MacroBreakdown) {
        _currentMacroBreakdown.value = breakdown
    }

    fun updateMealCount(count: Int) {
        _currentMealCount.value = count
    }

    fun recalculate() {
        _showResults.value = false
        _resultData.value = null
        _isSaved.value = false
        _calculationWarnings.value = emptyList()
        _tefData.value = null
        _validationState.value = _validationState.value.copy(hasAttemptedCalculation = false)
        // Input values are preserved in _inputState — no reset needed
    }

    fun clearAll() {
        _inputState.value = BMRInputState()
        _validationState.value = BMRValidationState()
        _resultData.value = null
        _showResults.value = false
        _isSaved.value = false
        _isCalculating.value = false
        _calculationWarnings.value = emptyList()
        _selectedActivityLevel.value = _profileActivityLevel.value ?: ActivityLevel.SEDENTARY
        _currentMacros.value = Triple(30f, 40f, 30f)
        _tefData.value = null
        _currentMacroBreakdown.value = null
        _currentMealCount.value = 3
        profilePopulated = false
    }
}
