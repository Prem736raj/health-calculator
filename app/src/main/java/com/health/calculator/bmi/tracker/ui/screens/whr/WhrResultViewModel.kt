package com.health.calculator.bmi.tracker.ui.screens.whr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import com.health.calculator.bmi.tracker.data.model.Gender
import com.health.calculator.bmi.tracker.data.model.WhrCalculator
import com.health.calculator.bmi.tracker.data.model.WhrResult
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository as MainHistoryRepository
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryEntry
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WhrResultViewModel(
    private val profileDataStore: ProfileDataStore,
    private val application: android.app.Application
) : ViewModel() {

    private val historyRepository = MainHistoryRepository(
        AppDatabase.getDatabase(application).historyDao()
    )

    private val _result = MutableStateFlow<WhrResult?>(null)
    val result: StateFlow<WhrResult?> = _result.asStateFlow()

    private val _heightFromProfile = MutableStateFlow<Float?>(null)
    val heightFromProfile: StateFlow<Float?> = _heightFromProfile.asStateFlow()

    init {
        // Collect height from profile store automatically
        viewModelScope.launch {
            profileDataStore.profileFlow.collect { profile ->
                if (profile.heightCm > 0) {
                    _heightFromProfile.value = profile.heightCm.toFloat()
                } else {
                    _heightFromProfile.value = null
                }
            }
        }
    }

    fun calculateResult(
        waistCm: Float,
        hipCm: Float,
        gender: Gender,
        age: Int
    ) {
        val heightCm = _heightFromProfile.value

        val whrResult = WhrCalculator.calculate(
            waistCm = waistCm,
            hipCm = hipCm,
            gender = gender,
            age = age,
            heightCm = heightCm
        )

        _result.value = whrResult
    }

    fun recalculateWithHeight(height: Float) {
        _heightFromProfile.value = height
        _result.value?.let { current ->
            val updatedResult = WhrCalculator.calculate(
                waistCm = current.waistCm,
                hipCm = current.hipCm,
                gender = current.gender,
                age = current.age,
                heightCm = height
            )
            _result.value = updatedResult
        }
    }

    fun saveToHistory() {
        _result.value?.let { result ->
            viewModelScope.launch {
                val detailsJson = JSONObject().apply {
                    put("waistCm", result.waistCm)
                    put("hipCm", result.hipCm)
                    put("gender", result.gender.name)
                    put("age", result.age)
                    put("whr", result.whr)
                    put("waistCategory", result.waistRiskLevel.name)
                    put("whrCategory", result.whrCategory.name)
                    put("healthRisk", result.bodyShape.name)
                    result.heightCm?.let { put("heightCm", it) }
                    result.whtr?.let { put("whtr", it) }
                }

                val entry = HistoryEntry(
                    calculatorKey = CalculatorType.WHR.key,
                    resultValue = "%.2f".format(result.whr),
                    resultLabel = "Ratio",
                    category = result.whrCategory.label,
                    detailsJson = detailsJson.toString(),
                    timestamp = System.currentTimeMillis()
                )
                historyRepository.addEntry(entry)
            }
        }
    }
}
