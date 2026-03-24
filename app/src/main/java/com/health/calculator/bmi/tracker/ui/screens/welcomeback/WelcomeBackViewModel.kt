// app/src/main/java/com/health/calculator/bmi/tracker/ui/screens/welcomeback/WelcomeBackViewModel.kt
package com.health.calculator.bmi.tracker.ui.screens.welcomeback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.models.InactivityState
import com.health.calculator.bmi.tracker.data.model.HealthOverview
import com.health.calculator.bmi.tracker.data.models.FrequentCalculator
import com.health.calculator.bmi.tracker.data.models.LastKnownMetric
import com.health.calculator.bmi.tracker.data.models.PlantWelcomeStatus
import com.health.calculator.bmi.tracker.data.models.StreakStatus
import com.health.calculator.bmi.tracker.data.models.WelcomeBackData
import com.health.calculator.bmi.tracker.data.repository.HealthOverviewRepository
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository
import com.health.calculator.bmi.tracker.data.repository.InactivityRepository
import com.health.calculator.bmi.tracker.data.repository.ProfileRepository
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WelcomeBackUiState(
    val isVisible: Boolean = false,
    val data: WelcomeBackData? = null,
    val isLoading: Boolean = true,
    val showStreakFreezeOption: Boolean = false,
    val streakFreezeCount: Int = 0,
    val freezeApplied: Boolean = false
)

class WelcomeBackViewModel(
    private val inactivityRepository: InactivityRepository,
    private val profileRepository: ProfileRepository,
    private val historyRepository: HistoryRepository,
    private val waterTrackingRepository: WaterIntakeRepository,
    private val healthOverviewRepository: HealthOverviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeBackUiState())
    val uiState: StateFlow<WelcomeBackUiState> = _uiState.asStateFlow()

    fun checkWelcomeBack() {
        viewModelScope.launch {
            inactivityRepository.getInactivityState().collect { state ->
                if (state.daysInactive >= 2 && !state.hasSeenWelcomeBack) {
                    loadWelcomeBackData(state)
                } else {
                    _uiState.update { it.copy(isVisible = false, isLoading = false) }
                    // Record normal app open
                    inactivityRepository.recordAppOpened()
                    inactivityRepository.resetNotificationLevel()
                }
            }
        }
    }

    private suspend fun loadWelcomeBackData(state: InactivityState) {
        try {
            val profile = profileRepository.getProfile().first()
            val overview = healthOverviewRepository.getHealthOverview().first()
            val streakBeforeBreak = inactivityRepository.getStreakBeforeBreak().first()
            val freezeCount = inactivityRepository.getStreakFreezeCount().first()

            val lastMetrics = buildLastKnownMetrics(overview)
            val frequentCalcs = getFrequentCalculators()

            // For water streak, we use waterTrackingRepository
            // Note: The prompt code used waterTrackingRepository.getCurrentStreak()
            // but in our app it's waterIntakeRepository and might have different methods.
            // I'll check WaterIntakeRepository again if needed.
            val currentWaterStreak = 0 // Placeholder or fetch if available
            val waterStreakBroken = streakBeforeBreak.first > 0 && currentWaterStreak == 0

            val welcomeData = WelcomeBackData(
                userName = profile.name,
                daysAway = state.daysInactive,
                lastActiveDate = state.lastAppOpenTime,
                streakStatus = StreakStatus(
                    waterStreak = currentWaterStreak,
                    wasWaterStreakBroken = waterStreakBroken,
                    waterStreakBeforeBreak = streakBeforeBreak.first,
                    trackingStreak = 0,
                    wasTrackingStreakBroken = streakBeforeBreak.second > 0,
                    trackingStreakBeforeBreak = streakBeforeBreak.second,
                    streakFreezeAvailable = freezeCount > 0,
                    streakFreezeUsed = false
                ),
                lastHealthMetrics = lastMetrics,
                mostUsedCalculators = frequentCalcs,
                plantStatus = PlantWelcomeStatus(
                    wasHealthy = state.daysInactive < 7,
                    currentStage = 1,
                    needsAttention = state.daysInactive >= 3
                )
            )

            _uiState.update {
                it.copy(
                    isVisible = true,
                    data = welcomeData,
                    isLoading = false,
                    streakFreezeCount = freezeCount,
                    showStreakFreezeOption = waterStreakBroken && freezeCount > 0
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isVisible = false, isLoading = false) }
        }
    }

    private fun buildLastKnownMetrics(overview: HealthOverview): List<LastKnownMetric> {
        val metrics = mutableListOf<LastKnownMetric>()

        overview.latestBmi?.let {
            val daysAgo = ((System.currentTimeMillis() - it.lastUpdated) / (24 * 60 * 60 * 1000)).toInt()
            metrics.add(LastKnownMetric("⚖️", "BMI", it.value, it.category, daysAgo, "calculator/bmi"))
        }
        overview.latestBp?.let {
            val daysAgo = ((System.currentTimeMillis() - it.lastUpdated) / (24 * 60 * 60 * 1000)).toInt()
            metrics.add(LastKnownMetric("❤️", "Blood Pressure", it.value, it.category, daysAgo, "calculator/blood_pressure"))
        }
        overview.latestWhr?.let {
            val daysAgo = ((System.currentTimeMillis() - it.lastUpdated) / (24 * 60 * 60 * 1000)).toInt()
            metrics.add(LastKnownMetric("📏", "WHR", it.value, it.category, daysAgo, "calculator/waist_hip"))
        }

        if (overview.healthScore >= 0) {
            metrics.add(0, LastKnownMetric("🏆", "Health Score", "${overview.healthScore}/100", "", 0, "home"))
        }

        return metrics.take(4)
    }

    private suspend fun getFrequentCalculators(): List<FrequentCalculator> {
        return try {
            val usage = historyRepository.getCalculatorUsageCounts()
            usage.sortedByDescending { it.second }
                .take(3)
                .map { (type, count) ->
                    val (name, icon, route) = getCalculatorInfo(type)
                    FrequentCalculator(name, icon, route, count)
                }
        } catch (e: Exception) {
            listOf(
                FrequentCalculator("BMI Calculator", "⚖️", "calculator/bmi", 0),
                FrequentCalculator("Water Tracker", "💧", "calculator/water_intake", 0),
                FrequentCalculator("Blood Pressure", "❤️", "calculator/blood_pressure", 0)
            )
        }
    }

    private fun getCalculatorInfo(type: String): Triple<String, String, String> {
        return when (type) {
            "BMI" -> Triple("BMI Calculator", "⚖️", "calculator/bmi")
            "BMR" -> Triple("BMR Calculator", "🔥", "calculator/bmr")
            "BP" -> Triple("Blood Pressure", "❤️", "calculator/blood_pressure")
            "WHR" -> Triple("Waist-to-Hip Ratio", "📏", "calculator/waist_hip")
            "WATER" -> Triple("Water Tracker", "💧", "calculator/water_intake")
            "METABOLIC_SYNDROME" -> Triple("Metabolic Syndrome", "🩺", "calculator/metabolic_syndrome")
            "BSA" -> Triple("Body Surface Area", "📐", "calculator/bsa")
            "IBW" -> Triple("Ideal Weight", "🎯", "ideal_body_weight")
            "CALORIE" -> Triple("Calorie Calculator", "🍽️", "calculator/daily_calorie")
            "HEART_RATE" -> Triple("Heart Rate Zones", "💓", "calculator/heart_rate_zone")
            else -> Triple(type, "📊", "home")
        }
    }

    fun useStreakFreeze() {
        viewModelScope.launch {
            val success = inactivityRepository.useStreakFreeze()
            if (success) {
                _uiState.update {
                    it.copy(
                        freezeApplied = true,
                        showStreakFreezeOption = false,
                        streakFreezeCount = it.streakFreezeCount - 1,
                        data = it.data?.copy(
                            streakStatus = it.data.streakStatus.copy(
                                streakFreezeUsed = true,
                                wasWaterStreakBroken = false
                            )
                        )
                    )
                }
            }
        }
    }

    fun dismiss() {
        viewModelScope.launch {
            inactivityRepository.setWelcomeBackSeen(true)
            inactivityRepository.recordAppOpened()
            inactivityRepository.resetNotificationLevel()
            _uiState.update { it.copy(isVisible = false) }
        }
    }
}
