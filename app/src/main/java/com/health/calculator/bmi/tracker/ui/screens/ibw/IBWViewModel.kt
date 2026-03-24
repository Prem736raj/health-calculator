package com.health.calculator.bmi.tracker.ui.screens.ibw

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.domain.usecase.*
import com.health.calculator.bmi.tracker.data.repository.IBWHistoryRepository
import com.health.calculator.bmi.tracker.data.repository.IBWStatistics
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository as MainHistoryRepository
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

data class IBWUiState(
    val heightValue: String = "",
    val heightFeet: String = "",
    val heightInches: String = "",
    val isMetricHeight: Boolean = true,
    val gender: String = "Male",
    val frameSize: String = "Medium",
    val wristCircumference: String = "",
    val age: String = "",
    val currentWeight: String = "",
    val isMetricWeight: Boolean = true,
    val showResult: Boolean = false,
    val result: IBWResult? = null,
    val showUnitInKg: Boolean = true,
    val isProfileDataUsed: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    // Goal planning fields
    val showGoalPlan: Boolean = false,
    val selectedGoalSource: String? = null,
    val selectedGoalWeightKg: Double? = null,
    val selectedPace: String = "Moderate",
    val paceOptions: List<WeightPaceOption> = emptyList(),
    val existingGoal: IBWGoal? = null,
    val isGoalSaved: Boolean = false,
    // Additional metrics
    val additionalMetrics: AdjustedWeightMetrics? = null,
    // History & Educational
    val showEducational: Boolean = false,
    val showHistory: Boolean = false,
    val historyEntries: List<IBWHistoryEntry> = emptyList(),
    val historyStatistics: IBWStatistics = IBWStatistics(),
    val homeSummary: IBWHomeSummaryData = IBWHomeSummaryData(null, null, null, null)
)

class IBWViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(IBWUiState())
    val uiState: StateFlow<IBWUiState> = _uiState.asStateFlow()

    private val calculator = IBWCalculatorUseCase()
    private val goalPlanner = WeightGoalPlannerUseCase()
    private val adjustedWeightUseCase = AdjustedWeightUseCase()
    private val historyRepository = IBWHistoryRepository(application.applicationContext)
    private val mainHistoryRepository = MainHistoryRepository(AppDatabase.getDatabase(application).historyDao())

    init {
        viewModelScope.launch {
            historyRepository.entries.collect { entries ->
                val lastEntry = entries.firstOrNull()
                val homeSummary = if (lastEntry != null) {
                    IBWHomeSummaryData(
                        idealWeightKg = lastEntry.frameAdjustedDevineKg,
                        currentWeightKg = lastEntry.currentWeightKg,
                        differenceKg = lastEntry.currentWeightKg?.let {
                            it - lastEntry.frameAdjustedDevineKg
                        },
                        frameSize = lastEntry.frameSize,
                        hasData = true
                    )
                } else IBWHomeSummaryData(null, null, null, null)

                _uiState.value = _uiState.value.copy(
                    historyEntries = entries,
                    historyStatistics = historyRepository.getStatistics(),
                    homeSummary = homeSummary
                )
            }
        }
    }

    fun updateHeight(value: String) {
        _uiState.value = _uiState.value.copy(heightValue = value, errorMessage = null)
    }

    fun updateHeightFeet(value: String) {
        _uiState.value = _uiState.value.copy(heightFeet = value, errorMessage = null)
    }

    fun updateHeightInches(value: String) {
        _uiState.value = _uiState.value.copy(heightInches = value, errorMessage = null)
    }

    fun toggleHeightUnit() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isMetricHeight = !currentState.isMetricHeight)
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateFrameSize(size: String) {
        _uiState.value = _uiState.value.copy(frameSize = size)
    }

    fun updateWristCircumference(value: String) {
        _uiState.value = _uiState.value.copy(wristCircumference = value)
    }

    fun determineFrameSizeFromWrist() {
        val state = _uiState.value
        val wrist = state.wristCircumference.toFloatOrNull() ?: return
        val height = state.heightValue.toFloatOrNull() ?: return
        val isMale = state.gender == "Male"

        val detectedSize = if (isMale) {
            when {
                wrist < 16.5 -> "Small"
                wrist <= 19.0 -> "Medium"
                else -> "Large"
            }
        } else {
            when {
                height < 155 -> when {
                    wrist < 14.0 -> "Small"
                    wrist <= 14.6 -> "Medium"
                    else -> "Large"
                }
                height <= 163 -> when {
                    wrist < 15.2 -> "Small"
                    wrist <= 15.9 -> "Medium"
                    else -> "Large"
                }
                else -> when {
                    wrist < 16.0 -> "Small"
                    wrist <= 16.5 -> "Medium"
                    else -> "Large"
                }
            }
        }
        _uiState.value = state.copy(frameSize = detectedSize)
    }

    fun updateAge(value: String) {
        _uiState.value = _uiState.value.copy(age = value)
    }

    fun updateCurrentWeight(value: String) {
        _uiState.value = _uiState.value.copy(currentWeight = value)
    }

    fun toggleWeightUnit() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isMetricWeight = !currentState.isMetricWeight)
    }

    fun calculate() {
        val state = _uiState.value
        
        val heightCm = if (state.isMetricHeight) {
            state.heightValue.toDoubleOrNull()
        } else {
            val feet = state.heightFeet.toDoubleOrNull() ?: 0.0
            val inches = state.heightInches.toDoubleOrNull() ?: 0.0
            (feet * 30.48) + (inches * 2.54)
        }

        if (heightCm == null || heightCm < 50 || heightCm > 300) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid height (50-300 cm)")
            return
        }

        val age = state.age.toIntOrNull()
        if (age != null && (age < 2 || age > 120)) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid age (2-120)")
            return
        }

        val currentWeightKg = if (state.currentWeight.isNotBlank()) {
            val weight = state.currentWeight.toDoubleOrNull()
            if (weight != null && weight > 0) {
                if (state.isMetricWeight) weight else weight / 2.20462
            } else null
        } else null

        viewModelScope.launch {
            val result = calculator.calculate(
                heightCm = heightCm,
                gender = state.gender,
                frameSize = state.frameSize,
                currentWeightKg = currentWeightKg,
                age = age
            )

            val additionalMetrics = if (currentWeightKg != null && currentWeightKg > 0) {
                adjustedWeightUseCase.calculate(
                    actualWeightKg = currentWeightKg,
                    idealWeightKg = result.frameAdjustedDevineKg,
                    heightCm = heightCm,
                    isMale = state.gender.equals("Male", ignoreCase = true)
                )
            } else null

            val homeSummary = IBWHomeSummaryData(
                idealWeightKg = result.frameAdjustedDevineKg,
                currentWeightKg = currentWeightKg,
                differenceKg = result.weightDifferenceKg,
                frameSize = state.frameSize,
                hasData = true
            )

            _uiState.value = state.copy(
                result = result,
                showResult = true,
                errorMessage = null,
                isSaved = false,
                additionalMetrics = additionalMetrics,
                homeSummary = homeSummary
            )
        }
    }

    fun toggleResultUnit() {
        _uiState.value = _uiState.value.copy(showUnitInKg = !_uiState.value.showUnitInKg)
    }

    fun saveToHistory() {
        val state = _uiState.value
        val result = state.result ?: return
        val metrics = state.additionalMetrics

        val entry = IBWHistoryEntry(
            heightCm = result.heightCm,
            gender = result.gender,
            frameSize = result.frameSize,
            age = result.age,
            currentWeightKg = result.currentWeightKg,
            devineKg = result.devineKg,
            robinsonKg = result.robinsonKg,
            millerKg = result.millerKg,
            hamwiKg = result.hamwiKg,
            brocaKg = result.brocaKg,
            bmiLowerKg = result.bmiLowerKg,
            bmiUpperKg = result.bmiUpperKg,
            frameAdjustedDevineKg = result.frameAdjustedDevineKg,
            leanBodyWeightKg = metrics?.leanBodyWeightKg,
            adjustedBodyWeightKg = metrics?.adjustedBodyWeightKg,
            weightCategoryPercent = metrics?.weightCategoryPercent,
            weightCategory = metrics?.weightCategory,
            goalWeightKg = state.selectedGoalWeightKg
        )

        historyRepository.saveEntry(entry)

        // Save to main history
        viewModelScope.launch {
            val detailsJson = JSONObject().apply {
                put("heightCm", result.heightCm)
                put("gender", result.gender)
                put("frameSize", result.frameSize)
                put("age", result.age ?: -1)
                put("currentWeightKg", result.currentWeightKg ?: -1.0)
                put("devineKg", result.devineKg)
                put("robinsonKg", result.robinsonKg)
                put("millerKg", result.millerKg)
                put("hamwiKg", result.hamwiKg)
                put("brocaKg", result.brocaKg)
                put("bmiLowerKg", result.bmiLowerKg)
                put("bmiUpperKg", result.bmiUpperKg)
                put("frameAdjustedDevineKg", result.frameAdjustedDevineKg)
                put("leanBodyWeightKg", metrics?.leanBodyWeightKg ?: -1.0)
                put("adjustedBodyWeightKg", metrics?.adjustedBodyWeightKg ?: -1.0)
                put("weightCategory", metrics?.weightCategory ?: "")
            }

            val mainEntry = HistoryEntry(
                calculatorKey = CalculatorType.IBW.key,
                resultValue = "%.1f".format(result.frameAdjustedDevineKg),
                resultLabel = "kg",
                category = metrics?.weightCategory ?: "Ideal Weight",
                detailsJson = detailsJson.toString(),
                timestamp = System.currentTimeMillis()
            )
            mainHistoryRepository.addEntry(mainEntry)
        }

        _uiState.value = state.copy(isSaved = true)
    }

    fun goBackToInput() {
        _uiState.value = _uiState.value.copy(showResult = false, showGoalPlan = false)
    }

    fun clearAll() {
        _uiState.value = IBWUiState(
            isMetricHeight = _uiState.value.isMetricHeight,
            isMetricWeight = _uiState.value.isMetricWeight
        )
    }

    fun getShareText(): String {
        val result = _uiState.value.result ?: return ""
        val showInKg = _uiState.value.showUnitInKg
        val unit = if (showInKg) "kg" else "lbs"
        val factor = if (showInKg) 1.0 else 2.20462

        val idealWeight = "%.1f".format(result.frameAdjustedDevineKg * factor)
        val parts = mutableListOf<String>()
        parts.add("My Ideal Body Weight: $idealWeight $unit (Devine Formula, ${result.frameSize} frame)")

        result.currentWeightKg?.let { current ->
            val currentDisplay = "%.1f".format(current * factor)
            parts.add("Current: $currentDisplay $unit")

            val diff = result.weightDifferenceKg ?: 0.0
            val absDiff = "%.1f".format(kotlin.math.abs(diff) * factor)
            when {
                diff > 0.5 -> parts.add("Goal: Lose $absDiff $unit")
                diff < -0.5 -> parts.add("Goal: Gain $absDiff $unit")
                else -> parts.add("Status: At ideal weight! ✅")
            }
        }

        parts.add("")
        parts.add("Healthy BMI range: ${"%.1f".format(result.bmiLowerKg * factor)}-${"%.1f".format(result.bmiUpperKg * factor)} $unit")
        parts.add("")
        parts.add("- Health Calculator: BMI Tracker")

        return parts.joinToString("\n")
    }

    // Goal planning methods
    fun showGoalPlan() {
        _uiState.value = _uiState.value.copy(showGoalPlan = true)
    }

    fun hideGoalPlan() {
        _uiState.value = _uiState.value.copy(showGoalPlan = false)
    }

    fun toggleEducational() {
        _uiState.value = _uiState.value.copy(showEducational = !_uiState.value.showEducational)
    }

    fun toggleHistory() {
        _uiState.value = _uiState.value.copy(showHistory = !_uiState.value.showHistory)
    }

    fun deleteHistoryEntry(id: Long) {
        historyRepository.deleteEntry(id)
    }

    fun selectGoal(targetKg: Double, source: String) {
        val state = _uiState.value
        val currentWeightKg = state.result?.currentWeightKg ?: return

        val paceOptions = goalPlanner.calculatePaceOptions(currentWeightKg, targetKg)

        _uiState.value = state.copy(
            selectedGoalSource = source,
            selectedGoalWeightKg = targetKg,
            paceOptions = paceOptions,
            isGoalSaved = false
        )
    }

    fun selectPace(pace: String) {
        _uiState.value = _uiState.value.copy(selectedPace = pace)
    }

    fun saveGoal() {
        val state = _uiState.value
        val targetKg = state.selectedGoalWeightKg ?: return
        val currentWeightKg = state.result?.currentWeightKg ?: return
        val source = state.selectedGoalSource ?: return

        val goal = IBWGoal(
            targetWeightKg = targetKg,
            targetSource = source,
            startWeightKg = currentWeightKg,
            selectedPace = state.selectedPace
        )

        _uiState.value = state.copy(
            existingGoal = goal,
            isGoalSaved = true
        )
    }

    fun clearGoal() {
        _uiState.value = _uiState.value.copy(
            existingGoal = null,
            isGoalSaved = false,
            selectedGoalSource = null,
            selectedGoalWeightKg = null
        )
    }

    fun getProgressMessage(): String? {
        val state = _uiState.value
        val goal = state.existingGoal ?: return null
        val currentWeight = state.result?.currentWeightKg ?: return null
        val progress = goal.progressPercent(currentWeight)
        return goalPlanner.getMotivationalMessage(progress, goal.isWeightLoss)
    }
}
