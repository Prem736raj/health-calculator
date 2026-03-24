package com.health.calculator.bmi.tracker.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.BpHomeCardInfo
import com.health.calculator.bmi.tracker.data.provider.BpHomeCardProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.health.calculator.bmi.tracker.data.model.WhrHistoryEntry
import com.health.calculator.bmi.tracker.data.repository.WhrRepository
import com.health.calculator.bmi.tracker.data.preferences.RecommendationPreferencesManager
import com.health.calculator.bmi.tracker.util.SmartRecommendation
import com.health.calculator.bmi.tracker.util.SmartRecommendationEngine
import com.health.calculator.bmi.tracker.util.UserHealthContext
import kotlinx.coroutines.flow.combine
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository

data class HomeUiState(
    val bpInfo: BpHomeCardInfo = BpHomeCardInfo(),
    val bmrLastValue: com.health.calculator.bmi.tracker.data.preferences.BMRLastValue = com.health.calculator.bmi.tracker.data.preferences.BMRLastValue(),
    val whrLastEntry: WhrHistoryEntry? = null,
    val hrLastEntry: com.health.calculator.bmi.tracker.data.model.HistoryEntry? = null,
    val healthMetrics: com.health.calculator.bmi.tracker.util.HealthMetricsSnapshot = com.health.calculator.bmi.tracker.util.HealthMetricsSnapshot(),
    val healthScore: com.health.calculator.bmi.tracker.util.HealthScoreResult = com.health.calculator.bmi.tracker.util.HealthScoreCalculator.calculateHealthScore(com.health.calculator.bmi.tracker.util.HealthMetricsSnapshot()),
    val quickStats: List<com.health.calculator.bmi.tracker.util.QuickStat> = emptyList(),
    val lastActivity: com.health.calculator.bmi.tracker.util.LastActivity? = null,
    val recommendations: List<SmartRecommendation> = emptyList(),
    val calculatorCardsState: com.health.calculator.bmi.tracker.ui.components.home.CalculatorCardsState = com.health.calculator.bmi.tracker.ui.components.home.CalculatorCardsState(),
    val isLoading: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getInstance(application)
    private val bmrPrefs = com.health.calculator.bmi.tracker.data.preferences.BMRLastValuePreferences(application)
    private val bpProvider = BpHomeCardProvider(application)
    private val whrRepository = WhrRepository(application)
    private val historyRepository = HistoryRepository(database.historyDao())
    private val bloodPressureRepository = com.health.calculator.bmi.tracker.data.repository.BloodPressureRepository(database.bloodPressureDao())
    private val waterIntakeRepository = com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository(database.waterIntakeDao())
    private val foodLogRepository = com.health.calculator.bmi.tracker.data.repository.FoodLogRepository(application)
    private val recommendationPrefs = RecommendationPreferencesManager(application)
    private val bmiGoalPrefs = com.health.calculator.bmi.tracker.data.preferences.BMIGoalPreferences(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeBmr()
        observeBp()
        observeWhr()
        observeHeartRate()
        observeHealthMetrics()
        observeRecommendations()
    }

    private fun observeWhr() {
        viewModelScope.launch {
            whrRepository.entries.collect { entriesList ->
                val lastEntry = entriesList.maxByOrNull { it.timestamp }
                _uiState.update { it.copy(whrLastEntry = lastEntry) }
            }
        }
    }

    private fun observeBmr() {
        viewModelScope.launch {
            bmrPrefs.lastValueFlow.collect { bmr ->
                _uiState.update { it.copy(bmrLastValue = bmr) }
            }
        }
    }

    private fun observeBp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            bpProvider.getHomeCardInfo().collect { bpInfo ->
                _uiState.update {
                    it.copy(
                        bpInfo = bpInfo,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeHeartRate() {
        viewModelScope.launch {
            historyRepository.getLatestEntry(com.health.calculator.bmi.tracker.data.model.CalculatorType.HEART_RATE)
                .collect { entry ->
                    _uiState.update { it.copy(hrLastEntry = entry) }
                }
        }
    }

    private fun observeHealthMetrics() {
        viewModelScope.launch {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val endOfDay = calendar.timeInMillis

            kotlinx.coroutines.flow.combine(
                kotlinx.coroutines.flow.combine(
                    historyRepository.getLatestEntry(com.health.calculator.bmi.tracker.data.model.CalculatorType.BMI),
                    bloodPressureRepository.allMainReadings,
                    whrRepository.entries
                ) { bmi, bp, whr -> Triple(bmi, bp, whr) },
                kotlinx.coroutines.flow.combine(
                    waterIntakeRepository.getTotalWaterForDay(startOfDay, endOfDay),
                    foodLogRepository.todayLog,
                    historyRepository.getLatestEntry(com.health.calculator.bmi.tracker.data.model.CalculatorType.HEART_RATE)
                ) { water, food, hr -> Triple(water, food, hr) },
                kotlinx.coroutines.flow.combine(
                    bmrPrefs.lastValueFlow,
                    historyRepository.getLatestEntry(com.health.calculator.bmi.tracker.data.model.CalculatorType.BSA),
                    bmiGoalPrefs.bmiGoalFlow
                ) { bmr, bsa, goal -> Triple(bmr, bsa, goal) }
            ) { firstThree, lastThree, extraThree ->
                val (bmiEntry, bpReadings, whrEntries) = firstThree
                val (waterIntake, dailyFoodLog, hrEntry) = lastThree
                val (bmrValue, bsaEntry, bmiGoal) = extraThree
                
                val lastBp = bpReadings.maxByOrNull { it.measurementTimestamp }
                val lastWhr = whrEntries.maxByOrNull { it.timestamp }
                
                // Extract resting HR from JSON if available
                val restingHR = try {
                    if (hrEntry != null && !hrEntry.detailsJson.isNullOrBlank()) {
                        val json = org.json.JSONObject(hrEntry.detailsJson)
                        if (json.has("restingHeartRate") && !json.isNull("restingHeartRate")) {
                            json.getInt("restingHeartRate")
                        } else null
                    } else null
                } catch (_: Exception) { null }

                val maxHR = try {
                    if (hrEntry != null && !hrEntry.detailsJson.isNullOrBlank()) {
                        val json = org.json.JSONObject(hrEntry.detailsJson)
                        if (json.has("maxHeartRate") && !json.isNull("maxHeartRate")) {
                            json.getInt("maxHeartRate")
                        } else null
                    } else null
                } catch (_: Exception) { null }

                val metrics = com.health.calculator.bmi.tracker.util.HealthMetricsSnapshot(
                    bmi = bmiEntry?.resultValue?.toFloatOrNull(),
                    bmiCategory = bmiEntry?.category,
                    bmiTimestamp = bmiEntry?.timestamp,
                    systolicBP = lastBp?.systolic,
                    diastolicBP = lastBp?.diastolic,
                    bpCategory = lastBp?.category,
                    bpTimestamp = lastBp?.measurementTimestamp,
                    whr = lastWhr?.whr,
                    whrCategory = lastWhr?.category?.name,
                    whrTimestamp = lastWhr?.timestamp,
                    waterIntakeToday = waterIntake ?: 0,
                    waterGoalToday = contextToWaterGoal(),
                    caloriesConsumedToday = dailyFoodLog.entries.sumOf { it.calories }.toInt(),
                    calorieTargetToday = dailyFoodLog.targetCalories.toInt(),
                    restingHR = restingHR,
                    restingHRTimestamp = hrEntry?.timestamp
                )

                val score = com.health.calculator.bmi.tracker.util.HealthScoreCalculator.calculateHealthScore(metrics)
                val stats = com.health.calculator.bmi.tracker.util.HealthScoreCalculator.buildQuickStats(metrics)
                
                // Find last activity
                val activities = listOfNotNull(
                    bmiEntry?.let { com.health.calculator.bmi.tracker.util.LastActivity("BMI", "📊", it.timestamp, com.health.calculator.bmi.tracker.core.navigation.Screen.BmiCalculator.route) },
                    lastBp?.let { com.health.calculator.bmi.tracker.util.LastActivity("Blood Pressure", "💓", it.measurementTimestamp, com.health.calculator.bmi.tracker.core.navigation.Screen.BloodPressureCalculator.route) },
                    lastWhr?.let { com.health.calculator.bmi.tracker.util.LastActivity("Waist-Hip Ratio", "📏", it.timestamp, com.health.calculator.bmi.tracker.core.navigation.Screen.WaistToHipCalculator.route) },
                    hrEntry?.let { com.health.calculator.bmi.tracker.util.LastActivity("Heart Rate Zones", "❤️", it.timestamp, com.health.calculator.bmi.tracker.core.navigation.Screen.HeartRateZoneCalculator.route) }
                )
                val lastAct = activities.maxByOrNull { it.timestamp }

                _uiState.update { 
                    it.copy(
                        healthMetrics = metrics,
                        healthScore = score,
                        quickStats = stats,
                        lastActivity = lastAct,
                        calculatorCardsState = com.health.calculator.bmi.tracker.ui.components.home.CalculatorCardsState(
                            lastBMI = metrics.bmi,
                            lastBMICategory = metrics.bmiCategory,
                            lastBMR = bmrValue.bmr.toInt(),
                            lastTDEE = bmrValue.tdee.toInt(),
                            lastBPSystolic = metrics.systolicBP,
                            lastBPDiastolic = metrics.diastolicBP,
                            lastBPCategory = metrics.bpCategory,
                            lastWHR = metrics.whr,
                            lastWHRCategory = metrics.whrCategory,
                            waterIntakeToday = metrics.waterIntakeToday,
                            waterGoalToday = metrics.waterGoalToday,
                            caloriesConsumedToday = metrics.caloriesConsumedToday,
                            calorieTargetToday = metrics.calorieTargetToday,
                            restingHeartRate = metrics.restingHR,
                            maxHeartRate = maxHR,
                            idealBodyWeight = if (bmiGoal.isGoalSet) bmiGoal.targetWeight else null,
                            currentWeight = bmiGoal.startingWeight,
                            lastBSA = bsaEntry?.resultValue?.toFloatOrNull(),
                            metabolicCriteriaMet = calculateMetabolicCriteria(metrics)
                        )
                    )
                }
            }.collect {}
        }
    }

    private fun observeRecommendations() {
        viewModelScope.launch {
            combine(
                uiState,
                recommendationPrefs.dismissedRecommendations,
                bmiGoalPrefs.bmiGoalFlow,
                bloodPressureRepository.allMainReadings
            ) { state, dismissed, bmiGoal, bpReadings ->
                val context = UserHealthContext(
                    lastBMIValue = state.healthMetrics.bmi,
                    lastBMITimestamp = state.healthMetrics.bmiTimestamp,
                    lastBPSystolic = state.healthMetrics.systolicBP,
                    lastBPDiastolic = state.healthMetrics.diastolicBP,
                    lastBPTimestamp = state.healthMetrics.bpTimestamp,
                    bpReadingsCount = bpReadings.size,
                    waterLoggedToday = state.healthMetrics.waterIntakeToday > 0,
                    waterProgress = if (state.healthMetrics.waterGoalToday > 0) 
                        state.healthMetrics.waterIntakeToday.toFloat() / state.healthMetrics.waterGoalToday else 0f,
                    caloriesLoggedToday = state.healthMetrics.caloriesConsumedToday > 0,
                    calorieProgress = if (state.healthMetrics.calorieTargetToday > 0)
                        state.healthMetrics.caloriesConsumedToday.toFloat() / state.healthMetrics.calorieTargetToday else 0f,
                    currentWeight = bmiGoal.startingWeight, // Use starting weight as fallback or current if available
                    goalWeight = if (bmiGoal.isGoalSet) bmiGoal.targetWeight else null,
                    lastWHRValue = state.healthMetrics.whr,
                    lastWHRTimestamp = state.healthMetrics.whrTimestamp,
                    lastRestingHR = state.healthMetrics.restingHR,
                    lastHRTimestamp = state.healthMetrics.restingHRTimestamp,
                    profileComplete = bmiGoal.isGoalSet, // Proxy for now
                    dismissedRecommendations = dismissed
                )
                SmartRecommendationEngine.generateRecommendations(context)
            }.collect { recs ->
                _uiState.update { it.copy(recommendations = recs) }
            }
        }
    }

    fun dismissRecommendation(id: String) {
        viewModelScope.launch {
            val dismissUntil = SmartRecommendationEngine.calculateDismissUntil()
            recommendationPrefs.dismissRecommendation(id, dismissUntil)
        }
    }

    private fun calculateMetabolicCriteria(metrics: com.health.calculator.bmi.tracker.util.HealthMetricsSnapshot): Int? {
        // Simplified metabolic syndrome criteria detection
        val readings = listOfNotNull(
            metrics.bmi?.let { it >= 30f }, // Obesity
            metrics.systolicBP?.let { it >= 130 || (metrics.diastolicBP ?: 0) >= 85 }, // BP
            metrics.whr?.let { it >= 0.9f } // WHR
        )
        return if (readings.isEmpty()) null else readings.count { it }
    }

    private fun contextToWaterGoal(): Int {
        val goalPrefs = getApplication<Application>().getSharedPreferences("water_intake_prefs", android.content.Context.MODE_PRIVATE)
        return goalPrefs.getInt("daily_goal_ml", 2500)
    }
}
