package com.health.calculator.bmi.tracker.ui.screens.whr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.model.Gender
import com.health.calculator.bmi.tracker.data.model.HeightUnit
import com.health.calculator.bmi.tracker.data.datastore.ProfileDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class WhrInputState(
    val waistValue: String = "",
    val hipValue: String = "",
    val gender: Gender = Gender.MALE,
    val age: String = "",
    val useMetric: Boolean = true,
    val waistError: String? = null,
    val hipError: String? = null,
    val ageError: String? = null,
    val waistWarning: String? = null,
    val isProfileDataLoaded: Boolean = false,
    val showMeasurementGuide: Boolean = false,
    val showWaistGuide: Boolean = false,
    val showHipGuide: Boolean = false
)

class WhrViewModel(
    private val profileDataStore: ProfileDataStore? = null
) : ViewModel() {

    private val _inputState = MutableStateFlow(WhrInputState())
    val inputState: StateFlow<WhrInputState> = _inputState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            profileDataStore?.let { prefs ->
                prefs.profileFlow.collect { profile ->
                    if (profile.heightCm > 0) {
                        
                        // Calculate age from DOB if available
                        var userAge = 0
                        profile.dateOfBirthMillis?.let { dobMillis ->
                            val dob = Instant.ofEpochMilli(dobMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                            val now = LocalDate.now()
                            userAge = ChronoUnit.YEARS.between(dob, now).toInt()
                        }
                        
                        _inputState.update {
                            it.copy(
                                gender = if (profile.gender != Gender.NOT_SET) profile.gender else Gender.MALE,
                                age = if (userAge > 0) userAge.toString() else "",
                                useMetric = profile.heightUnit == HeightUnit.CM,
                                isProfileDataLoaded = true
                            )
                        }
                    }
                }
            }
        }
    }

    fun updateWaist(value: String) {
        _inputState.update {
            it.copy(
                waistValue = value,
                waistError = null,
                waistWarning = null
            )
        }
        validateWaistHipRelation()
    }

    fun updateHip(value: String) {
        _inputState.update {
            it.copy(
                hipValue = value,
                hipError = null,
                waistWarning = null
            )
        }
        validateWaistHipRelation()
    }

    fun updateGender(gender: Gender) {
        _inputState.update { it.copy(gender = gender) }
    }

    fun updateAge(value: String) {
        _inputState.update {
            it.copy(
                age = value,
                ageError = null
            )
        }
    }

    fun toggleUnit() {
        val currentState = _inputState.value
        val newUseMetric = !currentState.useMetric

        val convertedWaist = if (currentState.waistValue.isNotBlank()) {
            val waistFloat = currentState.waistValue.toFloatOrNull()
            if (waistFloat != null) {
                if (newUseMetric) {
                    // inches to cm
                    String.format("%.1f", waistFloat * 2.54f)
                } else {
                    // cm to inches
                    String.format("%.1f", waistFloat / 2.54f)
                }
            } else currentState.waistValue
        } else ""

        val convertedHip = if (currentState.hipValue.isNotBlank()) {
            val hipFloat = currentState.hipValue.toFloatOrNull()
            if (hipFloat != null) {
                if (newUseMetric) {
                    String.format("%.1f", hipFloat * 2.54f)
                } else {
                    String.format("%.1f", hipFloat / 2.54f)
                }
            } else currentState.hipValue
        } else ""

        _inputState.update {
            it.copy(
                useMetric = newUseMetric,
                waistValue = convertedWaist,
                hipValue = convertedHip,
                waistError = null,
                hipError = null,
                waistWarning = null
            )
        }
    }

    fun toggleMeasurementGuide() {
        _inputState.update { it.copy(showMeasurementGuide = !it.showMeasurementGuide) }
    }

    fun toggleWaistGuide() {
        _inputState.update { it.copy(showWaistGuide = !it.showWaistGuide) }
    }

    fun toggleHipGuide() {
        _inputState.update { it.copy(showHipGuide = !it.showHipGuide) }
    }

    fun clearAll() {
        _inputState.update {
            WhrInputState(
                useMetric = it.useMetric,
                gender = it.gender
            )
        }
    }

    private fun validateWaistHipRelation() {
        val state = _inputState.value
        val waist = state.waistValue.toFloatOrNull() ?: return
        val hip = state.hipValue.toFloatOrNull() ?: return

        if (waist > hip) {
            _inputState.update {
                it.copy(waistWarning = "Waist is larger than hip — this is unusual but allowed")
            }
        }
    }

    fun validate(): Boolean {
        val state = _inputState.value
        var isValid = true

        val waist = state.waistValue.toFloatOrNull()
        val hip = state.hipValue.toFloatOrNull()
        val age = state.age.toIntOrNull()

        // Waist validation
        if (waist == null || state.waistValue.isBlank()) {
            _inputState.update { it.copy(waistError = "Please enter waist measurement") }
            isValid = false
        } else if (state.useMetric) {
            if (waist < 40f || waist > 200f) {
                _inputState.update { it.copy(waistError = "Waist should be between 40-200 cm") }
                isValid = false
            }
        } else {
            if (waist < 16f || waist > 80f) {
                _inputState.update { it.copy(waistError = "Waist should be between 16-80 inches") }
                isValid = false
            }
        }

        // Hip validation
        if (hip == null || state.hipValue.isBlank()) {
            _inputState.update { it.copy(hipError = "Please enter hip measurement") }
            isValid = false
        } else if (state.useMetric) {
            if (hip < 50f || hip > 200f) {
                _inputState.update { it.copy(hipError = "Hip should be between 50-200 cm") }
                isValid = false
            }
        } else {
            if (hip < 20f || hip > 80f) {
                _inputState.update { it.copy(hipError = "Hip should be between 20-80 inches") }
                isValid = false
            }
        }

        // Age validation
        if (age == null || state.age.isBlank()) {
            _inputState.update { it.copy(ageError = "Please enter your age") }
            isValid = false
        } else if (age < 2 || age > 120) {
            _inputState.update { it.copy(ageError = "Age must be between 2 and 120") }
            isValid = false
        }

        return isValid
    }

    fun getWaistInCm(): Float {
        val state = _inputState.value
        val value = state.waistValue.toFloatOrNull() ?: 0f
        return if (state.useMetric) value else value * 2.54f
    }

    fun getHipInCm(): Float {
        val state = _inputState.value
        val value = state.hipValue.toFloatOrNull() ?: 0f
        return if (state.useMetric) value else value * 2.54f
    }
}
