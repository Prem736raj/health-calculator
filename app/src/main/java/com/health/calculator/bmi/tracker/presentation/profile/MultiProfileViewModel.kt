package com.health.calculator.bmi.tracker.presentation.profile

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.data.repository.FamilyProfileRepository
import com.health.calculator.bmi.tracker.data.repository.HealthOverviewRepository
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for the multi-profile management features.
 */
data class MultiProfileUiState(
    val profiles: List<FamilyProfile> = emptyList(),
    val activeProfile: FamilyProfile? = null,
    val canAddMore: Boolean = true,
    val showProfileSwitcher: Boolean = false,
    val showAddProfileDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val profileToDelete: FamilyProfile? = null,
    val showColorPicker: Boolean = false,
    val newProfileName: String = "",
    val newProfileColor: ProfileColor = ProfileColor.BLUE,
    val showHealthConnections: Boolean = false,
    val healthConnectionMap: HealthConnectionMap? = null,
    val showShareDialog: Boolean = false,
    val shareConfig: ProfileShareConfig = ProfileShareConfig(),
    val healthOverview: HealthOverview = HealthOverview(),
    val showRecalculatePrompt: Boolean = false,
    val calculatorsToRecalculate: List<String> = emptyList(),
    val isSwitching: Boolean = false
)

/**
 * ViewModel responsible for profile switching, family member management,
 * health interconnections, and data sharing.
 */
class MultiProfileViewModel(
    private val familyProfileRepository: FamilyProfileRepository,
    private val healthOverviewRepository: HealthOverviewRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultiProfileUiState())
    val uiState: StateFlow<MultiProfileUiState> = _uiState.asStateFlow()

    init {
        // Initial migration if needed
        viewModelScope.launch {
            familyProfileRepository.migrateFromDataStore()
        }
        
        loadProfiles()
        loadHealthOverview()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            familyProfileRepository.allProfiles.collect { profiles ->
                val canAdd = profiles.size < FamilyProfile.MAX_PROFILES
                _uiState.update {
                    it.copy(
                        profiles = profiles,
                        activeProfile = profiles.find { p -> p.isActive },
                        canAddMore = canAdd
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

    fun showProfileSwitcher() {
        _uiState.update { it.copy(showProfileSwitcher = true) }
    }

    fun dismissProfileSwitcher() {
        _uiState.update { it.copy(showProfileSwitcher = false) }
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSwitching = true) }
            familyProfileRepository.switchProfile(profileId)
            _uiState.update {
                it.copy(
                    isSwitching = false,
                    showProfileSwitcher = false
                )
            }
        }
    }

    fun showAddProfileDialog() {
        viewModelScope.launch {
            val nextColor = familyProfileRepository.getNextAvailableColor()
            _uiState.update {
                it.copy(
                    showAddProfileDialog = true,
                    newProfileName = "",
                    newProfileColor = nextColor
                )
            }
        }
    }

    fun dismissAddProfileDialog() {
        _uiState.update { it.copy(showAddProfileDialog = false) }
    }

    fun updateNewProfileName(name: String) {
        _uiState.update { it.copy(newProfileName = name) }
    }

    fun updateNewProfileColor(color: ProfileColor) {
        _uiState.update { it.copy(newProfileColor = color) }
    }

    fun createProfile() {
        val state = _uiState.value
        if (state.newProfileName.isBlank()) return

        viewModelScope.launch {
            val newProfile = FamilyProfile(
                displayName = state.newProfileName.trim(),
                profileColor = state.newProfileColor.colorValue
            )
            val success = familyProfileRepository.createProfile(newProfile)
            if (success) {
                _uiState.update { it.copy(showAddProfileDialog = false) }
            }
        }
    }

    fun confirmDeleteProfile(profile: FamilyProfile) {
        _uiState.update {
            it.copy(showDeleteConfirm = true, profileToDelete = profile)
        }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false, profileToDelete = null) }
    }

    fun deleteProfile() {
        val profile = _uiState.value.profileToDelete ?: return
        viewModelScope.launch {
            familyProfileRepository.deleteProfile(profile.profileId)
            _uiState.update { it.copy(showDeleteConfirm = false, profileToDelete = null) }
        }
    }

    // --- Health Connections ---

    fun showHealthConnections() {
        viewModelScope.launch {
            val lastCalcTimes = historyRepository.getLastCalculatedTimes()
            val activeProfile = _uiState.value.activeProfile
            val profileModified = activeProfile?.createdAt ?: 0L

            val connectionMap = HealthConnectionsRegistry.buildConnectionMap(
                lastCalculatedTimes = lastCalcTimes,
                profileLastModified = profileModified
            )

            _uiState.update {
                it.copy(
                    showHealthConnections = true,
                    healthConnectionMap = connectionMap,
                    calculatorsToRecalculate = connectionMap.calculatorsNeedingRecalculation
                )
            }
        }
    }

    fun dismissHealthConnections() {
        _uiState.update { it.copy(showHealthConnections = false) }
    }

    fun checkRecalculationNeeded() {
        val calcs = _uiState.value.healthConnectionMap?.calculatorsNeedingRecalculation ?: return
        if (calcs.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    showRecalculatePrompt = true,
                    calculatorsToRecalculate = calcs
                )
            }
        }
    }

    fun dismissRecalculatePrompt() {
        _uiState.update { it.copy(showRecalculatePrompt = false) }
    }

    // --- Profile Sharing ---

    fun showShareDialog() {
        _uiState.update { it.copy(showShareDialog = true) }
    }

    fun dismissShareDialog() {
        _uiState.update { it.copy(showShareDialog = false) }
    }

    fun updateShareConfig(config: ProfileShareConfig) {
        _uiState.update { it.copy(shareConfig = config) }
    }

    fun shareProfile(context: Context) {
        val state = _uiState.value
        val profile = state.activeProfile ?: return

        val text = generateShareText(profile, state.healthOverview, state.shareConfig)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "My Health Summary - Health Calculator")
        }
        context.startActivity(Intent.createChooser(intent, "Share Health Summary"))

        _uiState.update { it.copy(showShareDialog = false) }
    }

    class Factory(
        private val familyProfileRepository: FamilyProfileRepository,
        private val healthOverviewRepository: HealthOverviewRepository,
        private val historyRepository: HistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MultiProfileViewModel(
                familyProfileRepository,
                healthOverviewRepository,
                historyRepository
            ) as T
        }
    }
}
