package com.health.calculator.bmi.tracker.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.HealthOverviewRepository
import com.health.calculator.bmi.tracker.data.repository.ProfileRepository
import com.health.calculator.bmi.tracker.domain.model.UserProfile
import com.health.calculator.bmi.tracker.domain.usecases.ProfileCompletionResult
import com.health.calculator.bmi.tracker.domain.usecases.ProfileCompletionUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.health.calculator.bmi.tracker.data.repository.WeightRepository

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val healthOverview: HealthOverview = HealthOverview(),
    val completion: ProfileCompletionResult = ProfileCompletionResult(0, emptyList(), emptyList(), emptyList()),
    val selectedTab: ProfileTab = ProfileTab.MY_INFO,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val showSaveSuccess: Boolean = false,
    val showImagePickerDialog: Boolean = false,
    val showDatePicker: Boolean = false,
    val showGenderPicker: Boolean = false,
    val showActivityLevelPicker: Boolean = false,
    val showHealthGoalsPicker: Boolean = false,
    val showFrameSizePicker: Boolean = false,
    val showEthnicityPicker: Boolean = false,
    val showWeightLogDialog: Boolean = false,
    val weightInput: String = "",
    val noteInput: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val isWeightSaving: Boolean = false,
    val heightUnit: HeightUnit = HeightUnit.CM,
    val weightUnit: WeightUnit = WeightUnit.KG
)

enum class ProfileTab(val title: String) {
    MY_INFO("My Info"),
    HEALTH_OVERVIEW("Health Overview")
}

enum class HeightUnit { CM, FT_IN }
enum class WeightUnit { KG, LBS }

class ProfileViewModel(
    private val familyProfileRepository: com.health.calculator.bmi.tracker.data.repository.FamilyProfileRepository,
    private val profileRepository: ProfileRepository,
    private val healthOverviewRepository: HealthOverviewRepository,
    private val weightRepository: WeightRepository,
    private val profileCompletionUseCase: ProfileCompletionUseCase = ProfileCompletionUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _weightStatistics = MutableStateFlow<com.health.calculator.bmi.tracker.data.model.WeightStatistics?>(null)
    val weightStatistics: StateFlow<com.health.calculator.bmi.tracker.data.model.WeightStatistics?> = _weightStatistics.asStateFlow()

    init {
        loadProfile()
        loadHealthOverview()
        observeWeightStatistics()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            familyProfileRepository.activeProfileData.collect { profileData ->
                val profile = profileData?.let { 
                    // Convert ProfileData to UserProfile
                    UserProfile(
                        name = it.displayName,
                        profilePictureUri = it.profilePictureUri,
                        dateOfBirthMillis = it.dateOfBirthMillis,
                        gender = com.health.calculator.bmi.tracker.domain.model.Gender.fromString(it.gender.name),
                        heightCm = it.heightCm.toFloat(),
                        weightKg = it.weightKg.toFloat(),
                        goalWeightKg = it.goalWeightKg.toFloat(),
                        activityLevel = it.activityLevel,
                        healthGoals = it.healthGoals,
                        frameSize = it.frameSize,
                        ethnicityRegion = it.ethnicityRegion,
                        useMetricSystem = it.weightUnit == com.health.calculator.bmi.tracker.data.model.WeightUnit.KG,
                        updatedAt = it.lastUpdatedMillis
                    )
                } ?: UserProfile()

                val completion = profileCompletionUseCase.calculate(profile)
                _uiState.update {
                    it.copy(
                        profile = profile,
                        completion = completion,
                        heightUnit = if (profile.useMetricSystem) HeightUnit.CM else HeightUnit.FT_IN,
                        weightUnit = if (profile.useMetricSystem) WeightUnit.KG else WeightUnit.LBS
                    )
                }
            }
        }
    }

    private fun loadHealthOverview() {
        viewModelScope.launch {
            healthOverviewRepository.getHealthOverview().collect { overview ->
                _uiState.update { it.copy(healthOverview = overview) }
            }
        }
    }

    private fun observeWeightStatistics() {
        weightRepository.getWeightStatistics()
            .onEach { stats ->
                _weightStatistics.value = stats
            }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(profile = it.profile.copy(name = name)) }
    }

    fun updateProfilePicture(uri: Uri?) {
        _uiState.update {
            it.copy(
                profile = it.profile.copy(profilePictureUri = uri?.toString()),
                showImagePickerDialog = false
            )
        }
    }

    fun updateDateOfBirth(millis: Long) {
        _uiState.update {
            it.copy(
                profile = it.profile.copy(dateOfBirthMillis = millis),
                showDatePicker = false
            )
        }
    }

    fun updateGender(gender: com.health.calculator.bmi.tracker.domain.model.Gender) {
        _uiState.update {
            it.copy(
                profile = it.profile.copy(gender = gender),
                showGenderPicker = false
            )
        }
    }

    fun updateHeightCm(cm: Double) {
        _uiState.update { it.copy(profile = it.profile.copy(heightCm = cm.toFloat())) }
    }

    fun updateHeightFtIn(feet: Int, inches: Int) {
        val cm = (feet * 30.48) + (inches * 2.54)
        _uiState.update { it.copy(profile = it.profile.copy(heightCm = cm.toFloat())) }
    }

    fun updateWeightKg(kg: Double) {
        _uiState.update { it.copy(profile = it.profile.copy(weightKg = kg.toFloat())) }
    }

    fun updateWeightLbs(lbs: Double) {
        val kg = lbs / 2.20462
        _uiState.update { it.copy(profile = it.profile.copy(weightKg = kg.toFloat())) }
    }

    fun updateGoalWeightKg(kg: Double) {
        _uiState.update { it.copy(profile = it.profile.copy(goalWeightKg = kg.toFloat())) }
    }

    fun updateGoalWeightLbs(lbs: Double) {
        val kg = lbs / 2.20462
        _uiState.update { it.copy(profile = it.profile.copy(goalWeightKg = kg.toFloat())) }
    }

    fun updateActivityLevel(level: ActivityLevel) {
        _uiState.update {
            it.copy(
                profile = it.profile.copy(activityLevel = level),
                showActivityLevelPicker = false
            )
        }
    }

    fun updateHealthGoals(goals: List<HealthGoal>) {
        _uiState.update {
            it.copy(
                profile = it.profile.copy(healthGoals = goals),
                showHealthGoalsPicker = false
            )
        }
    }

    fun updateFrameSize(size: FrameSize) {
        _uiState.update {
            it.copy(
                profile = it.profile.copy(frameSize = size),
                showFrameSizePicker = false
            )
        }
    }

    fun updateEthnicity(ethnicity: EthnicityRegion) {
        _uiState.update {
            it.copy(
                profile = it.profile.copy(ethnicityRegion = ethnicity),
                showEthnicityPicker = false
            )
        }
    }

    fun toggleHeightUnit() {
        _uiState.update {
            it.copy(
                heightUnit = if (it.heightUnit == HeightUnit.CM) HeightUnit.FT_IN else HeightUnit.CM
            )
        }
    }

    fun toggleWeightUnit() {
        _uiState.update {
            it.copy(
                weightUnit = if (it.weightUnit == WeightUnit.KG) WeightUnit.LBS else WeightUnit.KG
            )
        }
    }

    fun showImagePickerDialog() {
        _uiState.update { it.copy(showImagePickerDialog = true) }
    }

    fun dismissImagePickerDialog() {
        _uiState.update { it.copy(showImagePickerDialog = false) }
    }

    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun dismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun showGenderPicker() {
        _uiState.update { it.copy(showGenderPicker = true) }
    }

    fun dismissGenderPicker() {
        _uiState.update { it.copy(showGenderPicker = false) }
    }

    fun showActivityLevelPicker() {
        _uiState.update { it.copy(showActivityLevelPicker = true) }
    }

    fun dismissActivityLevelPicker() {
        _uiState.update { it.copy(showActivityLevelPicker = false) }
    }

    fun showHealthGoalsPicker() {
        _uiState.update { it.copy(showHealthGoalsPicker = true) }
    }

    fun dismissHealthGoalsPicker() {
        _uiState.update { it.copy(showHealthGoalsPicker = false) }
    }

    fun showFrameSizePicker() {
        _uiState.update { it.copy(showFrameSizePicker = true) }
    }

    fun dismissFrameSizePicker() {
        _uiState.update { it.copy(showFrameSizePicker = false) }
    }

    fun showEthnicityPicker() {
        _uiState.update { it.copy(showEthnicityPicker = true) }
    }

    fun dismissEthnicityPicker() {
        _uiState.update { it.copy(showEthnicityPicker = false) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val activeProfile = familyProfileRepository.activeProfile.first()
            if (activeProfile != null) {
                val updatedProfile = activeProfile.copy(
                    displayName = _uiState.value.profile.name,
                    profilePictureUri = _uiState.value.profile.profilePictureUri,
                    dateOfBirthMillis = _uiState.value.profile.dateOfBirthMillis,
                    genderName = com.health.calculator.bmi.tracker.data.model.Gender.valueOf(_uiState.value.profile.gender.name).name,
                    heightCm = _uiState.value.profile.heightCm?.toDouble() ?: 0.0,
                    weightKg = _uiState.value.profile.weightKg?.toDouble() ?: 0.0,
                    goalWeightKg = _uiState.value.profile.goalWeightKg?.toDouble() ?: 0.0,
                    activityLevelName = _uiState.value.profile.activityLevel.name,
                    healthGoalNames = _uiState.value.profile.healthGoals.joinToString(",") { it.name },
                    frameSizeName = _uiState.value.profile.frameSize.name,
                    ethnicityRegionName = _uiState.value.profile.ethnicityRegion.name
                )
                familyProfileRepository.updateProfile(updatedProfile)
                
                // Also update legacy repository for backward compatibility if needed
                profileRepository.saveProfile(_uiState.value.profile)
            }
            
            _uiState.update {
                val completion = profileCompletionUseCase.calculate(it.profile)
                it.copy(
                    isSaving = false,
                    showSaveSuccess = true,
                    completion = completion
                )
            }
        }
    }

    fun dismissSaveSuccess() {
        _uiState.update { it.copy(showSaveSuccess = false) }
    }

    // Weight Logging methods
    fun showWeightLogDialog() {
        _uiState.update {
            it.copy(
                showWeightLogDialog = true,
                weightInput = it.profile.weightKg?.let { w -> 
                    String.format("%.1f", if (it.profile.useMetricSystem) w else w * 2.20462f) 
                } ?: "",
                noteInput = "",
                dateMillis = System.currentTimeMillis()
            )
        }
    }

    fun dismissWeightLogDialog() {
        _uiState.update { it.copy(showWeightLogDialog = false) }
    }

    fun updateWeightLogInput(input: String) {
        _uiState.update { it.copy(weightInput = input) }
    }

    fun updateWeightLogNote(note: String) {
        _uiState.update { it.copy(noteInput = note) }
    }

    fun updateWeightLogDate(millis: Long) {
        _uiState.update { it.copy(dateMillis = millis) }
    }

    fun saveWeightLog() {
        val weight = _uiState.value.weightInput.toDoubleOrNull() ?: return
        val weightKg = if (_uiState.value.profile.useMetricSystem) weight else weight / 2.20462

        viewModelScope.launch {
            _uiState.update { it.copy(isWeightSaving = true) }
            weightRepository.logWeight(
                weightKg = weightKg,
                dateMillis = _uiState.value.dateMillis,
                note = _uiState.value.noteInput.ifBlank { null },
                source = com.health.calculator.bmi.tracker.data.model.WeightSource.PROFILE
            )
            
            // Also update profile current weight if it's the latest
            profileRepository.saveProfile(_uiState.value.profile.copy(weightKg = weightKg.toFloat()))
            
            _uiState.update {
                it.copy(
                    isWeightSaving = false,
                    showWeightLogDialog = false
                )
            }
        }
    }
}

class ProfileViewModelFactory(
    private val familyProfileRepository: com.health.calculator.bmi.tracker.data.repository.FamilyProfileRepository,
    private val profileRepository: ProfileRepository,
    private val healthOverviewRepository: HealthOverviewRepository,
    private val weightRepository: WeightRepository,
    private val profileCompletionUseCase: com.health.calculator.bmi.tracker.domain.usecases.ProfileCompletionUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(
            familyProfileRepository,
            profileRepository,
            healthOverviewRepository,
            weightRepository,
            profileCompletionUseCase
        ) as T
    }
}
