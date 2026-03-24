// ui/screens/waterintake/WaterIntakeViewModel.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.model.ClimateType
import com.health.calculator.bmi.tracker.data.model.HealthStatus
import com.health.calculator.bmi.tracker.data.model.WaterActivityLevel
import com.health.calculator.bmi.tracker.data.model.WaterIntakeCalculation
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository
import kotlinx.coroutines.launch

class WaterIntakeViewModel(
    application: Application,
    private val repository: WaterIntakeRepository
) : AndroidViewModel(application) {

    // Input states
    var weightValue by mutableStateOf("")
        private set
    var isMetric by mutableStateOf(true)
        private set
    var ageValue by mutableStateOf("")
        private set
    var selectedGender by mutableStateOf("Male")
        private set
    var selectedActivityLevel by mutableStateOf(WaterActivityLevel.SEDENTARY)
        private set
    var selectedClimate by mutableStateOf(ClimateType.TEMPERATE)
        private set
    var selectedHealthStatus by mutableStateOf(HealthStatus.NORMAL)
        private set

    // Profile auto-fill tracking
    var isUsingProfileData by mutableStateOf(false)
        private set

    // Validation states
    var weightError by mutableStateOf<String?>(null)
        private set
    var ageError by mutableStateOf<String?>(null)
        private set

    // Result state
    var calculationResult by mutableStateOf<WaterIntakeCalculation?>(null)
        private set
    var showResult by mutableStateOf(false)
        private set

    fun updateWeight(value: String) {
        weightValue = value
        weightError = null
        isUsingProfileData = false
    }

    fun toggleUnit() {
        val currentWeight = weightValue.toFloatOrNull()
        if (currentWeight != null) {
            weightValue = if (isMetric) {
                // kg to lbs
                String.format("%.1f", currentWeight * 2.20462f)
            } else {
                // lbs to kg
                String.format("%.1f", currentWeight / 2.20462f)
            }
        }
        isMetric = !isMetric
    }

    fun updateAge(value: String) {
        ageValue = value
        ageError = null
        isUsingProfileData = false
    }

    fun updateGender(gender: String) {
        selectedGender = gender
    }

    fun updateActivityLevel(level: WaterActivityLevel) {
        selectedActivityLevel = level
    }

    fun updateClimate(climate: ClimateType) {
        selectedClimate = climate
    }

    fun updateHealthStatus(status: HealthStatus) {
        selectedHealthStatus = status
    }

    fun loadFromProfile(
        profileWeight: Float?,
        profileAge: Int?,
        profileGender: String?,
        profileActivityLevel: String?,
        profileIsMetric: Boolean = true
    ) {
        var loaded = false
        profileWeight?.let {
            weightValue = if (profileIsMetric) {
                String.format("%.1f", it)
            } else {
                String.format("%.1f", it * 2.20462f)
            }
            isMetric = profileIsMetric
            loaded = true
        }
        profileAge?.let {
            ageValue = it.toString()
            loaded = true
        }
        profileGender?.let {
            selectedGender = it
        }
        profileActivityLevel?.let { activityStr ->
            WaterActivityLevel.entries.find {
                it.name.equals(activityStr, ignoreCase = true)
            }?.let {
                selectedActivityLevel = it
            }
        }
        isUsingProfileData = loaded
    }

    fun validate(): Boolean {
        var isValid = true

        val weight = weightValue.toFloatOrNull()
        if (weight == null || weight <= 0) {
            weightError = "Please enter a valid weight"
            isValid = false
        } else {
            val weightKg = if (isMetric) weight else weight / 2.20462f
            if (weightKg < 20 || weightKg > 300) {
                weightError = "Weight seems outside a realistic range"
                isValid = false
            }
        }

        val age = ageValue.toIntOrNull()
        if (age == null || age < 1) {
            ageError = "Please enter a valid age"
            isValid = false
        } else if (age < 2 || age > 120) {
            ageError = "Age must be between 2 and 120"
            isValid = false
        }

        return isValid
    }

    fun calculate() {
        if (!validate()) return

        val weight = weightValue.toFloat()
        val weightKg = if (isMetric) weight else weight / 2.20462f
        val age = ageValue.toInt()

        // Base water calculation: 35ml per kg of body weight (WHO guideline)
        var baseIntakeMl = (weightKg * 35).toInt()

        // Age adjustment
        baseIntakeMl = when {
            age < 4 -> (weightKg * 100).toInt().coerceAtMost(1300)
            age < 9 -> 1400
            age < 14 -> 1800
            age < 18 -> 2200
            age in 18..30 -> baseIntakeMl
            age in 31..55 -> (baseIntakeMl * 0.95f).toInt()
            age in 56..75 -> (baseIntakeMl * 0.90f).toInt()
            else -> (baseIntakeMl * 0.85f).toInt()
        }

        // Gender adjustment
        if (selectedGender == "Female" && age >= 18) {
            baseIntakeMl = (baseIntakeMl * 0.9f).toInt()
        }

        // Activity level multiplier
        baseIntakeMl = (baseIntakeMl * selectedActivityLevel.multiplier).toInt()

        // Climate multiplier
        baseIntakeMl = (baseIntakeMl * selectedClimate.multiplier).toInt()

        // Health status additional
        baseIntakeMl += selectedHealthStatus.additionalMl

        // Clamp to reasonable range
        val recommendedMl = baseIntakeMl.coerceIn(1000, 6000)
        val recommendedOz = recommendedMl / 29.5735f
        val glasses = (recommendedMl / 250f).toInt() // 250ml per glass

        val result = WaterIntakeCalculation(
            weightKg = weightKg,
            age = age,
            gender = selectedGender,
            activityLevel = selectedActivityLevel.name,
            climate = selectedClimate.name,
            healthStatus = selectedHealthStatus.name,
            recommendedIntakeMl = recommendedMl,
            recommendedIntakeOz = recommendedOz,
            recommendedGlasses = glasses
        )

        calculationResult = result
        showResult = true
    }

    fun saveGoalForTracking() {
        calculationResult?.let { result ->
            viewModelScope.launch {
                // Save calculation to history
                repository.saveCalculation(result)
            }
        }
    }

    fun saveToHistory() {
        calculationResult?.let { result ->
            viewModelScope.launch {
                repository.saveCalculation(result)
                // Save goal amount for daily tracking
                saveGoalToPreferences(result.recommendedIntakeMl)
            }
        }
    }

    private fun saveGoalToPreferences(goalMl: Int) {
        val prefs = getApplication<Application>()
            .getSharedPreferences("water_intake_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("daily_goal_ml", goalMl)
            .putLong("goal_set_date", System.currentTimeMillis())
            .apply()
    }

    fun getSavedGoalMl(): Int {
        val prefs = getApplication<Application>()
            .getSharedPreferences("water_intake_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getInt("daily_goal_ml", 2500) // default 2500ml
    }

    fun clearAll() {
        weightValue = ""
        ageValue = ""
        selectedGender = "Male"
        selectedActivityLevel = WaterActivityLevel.SEDENTARY
        selectedClimate = ClimateType.TEMPERATE
        selectedHealthStatus = HealthStatus.NORMAL
        weightError = null
        ageError = null
        showResult = false
        calculationResult = null
        isUsingProfileData = false
    }

    fun resetResult() {
        showResult = false
    }
}
