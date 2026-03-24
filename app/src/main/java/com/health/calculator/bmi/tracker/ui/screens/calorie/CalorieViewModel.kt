package com.health.calculator.bmi.tracker.ui.screens.calorie

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.model.CalorieHistoryEntry
import com.health.calculator.bmi.tracker.data.model.CalorieResult
import com.health.calculator.bmi.tracker.data.model.DietPreset
import com.health.calculator.bmi.tracker.data.model.MacroResult
import com.health.calculator.bmi.tracker.data.model.MealPlan
import com.health.calculator.bmi.tracker.data.model.IntermittentFastingPlan
import com.health.calculator.bmi.tracker.data.model.WorkoutNutrition
import com.health.calculator.bmi.tracker.data.repository.CalorieHistoryRepository
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.domain.usecase.CalorieCalculatorUseCase
import com.health.calculator.bmi.tracker.domain.usecase.MacroCalculatorUseCase
import com.health.calculator.bmi.tracker.domain.usecase.MealPlanningUseCase
import com.health.calculator.bmi.tracker.data.repository.FoodLogRepository
import com.health.calculator.bmi.tracker.data.repository.HistoryRepository as MainHistoryRepository
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.HistoryEntry
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivityLevelOption(
    val id: String,
    val name: String,
    val description: String,
    val multiplier: Double,
    val emoji: String
)

data class GoalOption(
    val id: String,
    val name: String,
    val description: String,
    val calorieAdjustment: Int,
    val weeklyChangeKg: Double,
    val emoji: String,
    val color: Long
)

data class CalorieUiState(
    val weightValue: String = "",
    val isMetricWeight: Boolean = true,
    val heightValue: String = "",
    val heightFeet: String = "",
    val heightInches: String = "",
    val isMetricHeight: Boolean = true,
    val age: String = "",
    val gender: String = "Male",
    val bodyFatPercent: String = "",
    val showBodyFatField: Boolean = false,
    val selectedActivityLevel: String = "",
    val selectedGoal: String = "maintain",
    val isProfileDataUsed: Boolean = false,
    val showResult: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val result: CalorieResult? = null,
    val historyEntries: List<CalorieHistoryEntry> = emptyList(),
    // NEW macro fields
    val macroResult: MacroResult? = null,
    val selectedDietPresetId: String = "balanced",
    val customCarbPercent: Int = 40,
    val customProteinPercent: Int = 30,
    val customFatPercent: Int = 30,
    val numberOfMeals: Int = 3,
    val proteinRecommendationText: String = "",
    // NEW meal planning fields:
    val mealPlan: MealPlan? = null,
    val selectedMealCount: Int = 3,
    val customMealSplits: List<Float> = listOf(30f, 40f, 30f),
    val ifType: String = "none",
    val ifWindowStart: Int = 12,
    val ifPlan: IntermittentFastingPlan? = null,
    val workoutNutritionEnabled: Boolean = false,
    val workoutTime: String = "Morning",
    val workoutNutrition: WorkoutNutrition? = null,
    val showEducational: Boolean = false,
    val showQuickLog: Boolean = false,
    val dailyTarget: Int = 0,
    val todayIntake: Int = 0
)

class CalorieViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CalorieUiState())
    val uiState: StateFlow<CalorieUiState> = _uiState.asStateFlow()

    private val calculator = CalorieCalculatorUseCase()
    private val macroCalculator = MacroCalculatorUseCase()
    private val mealPlanningUseCase = MealPlanningUseCase()
    private val historyRepository = CalorieHistoryRepository(application.applicationContext)
    private val mainHistoryRepository = MainHistoryRepository(AppDatabase.getDatabase(application).historyDao())
    private val foodLogRepository = FoodLogRepository(application.applicationContext)

    val activityLevels = listOf(
        ActivityLevelOption("sedentary", "Sedentary", "Desk job, no exercise. Mostly sitting throughout the day with minimal physical movement.", 1.2, "🪑"),
        ActivityLevelOption("light", "Lightly Active", "Light exercise or walking 1-3 days/week. Some physical activity but mostly low intensity.", 1.375, "🚶"),
        ActivityLevelOption("moderate", "Moderately Active", "Moderate exercise 3-5 days/week. Regular workouts like jogging, cycling, or swimming.", 1.55, "🏃"),
        ActivityLevelOption("very_active", "Very Active", "Hard exercise 6-7 days/week. Intense training sessions with heavy lifting or HIIT.", 1.725, "💪"),
        ActivityLevelOption("extremely_active", "Extremely Active", "Very hard exercise, physical job, or training 2x/day. Professional athletes or heavy labor workers.", 1.9, "🔥")
    )

    val goalOptions = listOf(
        GoalOption("lose_aggressive", "Lose Weight - Aggressive", "~1 kg/week (2.2 lbs) • 1000 cal deficit", -1000, -1.0, "⚡", 0xFFF44336),
        GoalOption("lose_moderate", "Lose Weight - Moderate", "~0.5 kg/week (1.1 lbs) • 500 cal deficit", -500, -0.5, "📉", 0xFFFF9800),
        GoalOption("lose_slow", "Lose Weight - Slow", "~0.25 kg/week (0.55 lbs) • 250 cal deficit", -250, -0.25, "🐢", 0xFFFFC107),
        GoalOption("maintain", "Maintain Weight", "Keep your current weight • No adjustment", 0, 0.0, "⚖️", 0xFF4CAF50),
        GoalOption("gain_lean", "Gain Weight - Lean", "~0.25 kg/week (0.55 lbs) • 250 cal surplus", 250, 0.25, "📈", 0xFF2196F3),
        GoalOption("gain_moderate", "Gain Weight - Moderate", "~0.5 kg/week (1.1 lbs) • 500 cal surplus", 500, 0.5, "🏋️", 0xFF673AB7)
    )

    val dietPresets: List<DietPreset> get() = macroCalculator.dietPresets

    init {
        loadProfileData()
        viewModelScope.launch {
            historyRepository.entries.collect { entries ->
                _uiState.value = _uiState.value.copy(historyEntries = entries)
            }
        }
        viewModelScope.launch {
            foodLogRepository.checkAndResetIfNewDay()
            foodLogRepository.todayLog.collect { log ->
                log?.let {
                    _uiState.value = _uiState.value.copy(
                        todayIntake = it.totalCalories.toInt(),
                        dailyTarget = it.targetCalories.toInt()
                    )
                }
            }
        }
    }

    private fun loadProfileData() {
        val prefs = getApplication<Application>().getSharedPreferences("user_profile", 0)
        val weight = prefs.getFloat("weight", 0f)
        val height = prefs.getFloat("height", 0f)
        val age = prefs.getInt("age", 0)
        val gender = prefs.getString("gender", "Male") ?: "Male"
        val activityLevel = prefs.getString("activity_level", "") ?: ""

        if (weight > 0 || height > 0 || age > 0) {
            _uiState.value = _uiState.value.copy(
                weightValue = if (weight > 0) "%.1f".format(weight) else "",
                heightValue = if (height > 0) "%.1f".format(height) else "",
                age = if (age > 0) age.toString() else "",
                gender = gender,
                selectedActivityLevel = mapProfileActivity(activityLevel),
                isProfileDataUsed = true
            )
        }
    }

    private fun mapProfileActivity(level: String): String = when (level.lowercase()) {
        "sedentary" -> "sedentary"
        "lightly active", "light" -> "light"
        "moderately active", "moderate" -> "moderate"
        "very active" -> "very_active"
        "extremely active" -> "extremely_active"
        else -> ""
    }

    fun updateWeight(value: String) { _uiState.value = _uiState.value.copy(weightValue = value, errorMessage = null) }
    fun updateHeight(value: String) { _uiState.value = _uiState.value.copy(heightValue = value, errorMessage = null) }
    fun updateHeightFeet(value: String) { _uiState.value = _uiState.value.copy(heightFeet = value, errorMessage = null) }
    fun updateHeightInches(value: String) { _uiState.value = _uiState.value.copy(heightInches = value, errorMessage = null) }
    fun updateAge(value: String) { _uiState.value = _uiState.value.copy(age = value, errorMessage = null) }
    fun updateGender(gender: String) { _uiState.value = _uiState.value.copy(gender = gender, errorMessage = null) }
    fun updateBodyFatPercent(value: String) { _uiState.value = _uiState.value.copy(bodyFatPercent = value, errorMessage = null) }
    fun selectActivityLevel(id: String) { _uiState.value = _uiState.value.copy(selectedActivityLevel = id, errorMessage = null) }
    fun selectGoal(id: String) { _uiState.value = _uiState.value.copy(selectedGoal = id, errorMessage = null) }

    fun toggleBodyFatField() {
        _uiState.value = _uiState.value.copy(
            showBodyFatField = !_uiState.value.showBodyFatField,
            bodyFatPercent = if (_uiState.value.showBodyFatField) "" else _uiState.value.bodyFatPercent
        )
    }

    fun toggleWeightUnit() {
        val s = _uiState.value
        val w = s.weightValue.toDoubleOrNull() ?: 0.0
        _uiState.value = if (s.isMetricWeight) s.copy(isMetricWeight = false, weightValue = if (w > 0) "%.1f".format(w * 2.20462) else "")
        else s.copy(isMetricWeight = true, weightValue = if (w > 0) "%.1f".format(w / 2.20462) else "")
    }

    fun toggleHeightUnit() {
        val s = _uiState.value
        if (s.isMetricHeight) {
            val cm = s.heightValue.toDoubleOrNull() ?: 0.0
            val totalIn = cm / 2.54; val ft = (totalIn / 12).toInt(); val inches = totalIn % 12
            _uiState.value = s.copy(isMetricHeight = false, heightFeet = if (cm > 0) ft.toString() else "", heightInches = if (cm > 0) "%.1f".format(inches) else "")
        } else {
            val ft = s.heightFeet.toDoubleOrNull() ?: 0.0; val inches = s.heightInches.toDoubleOrNull() ?: 0.0
            val cm = (ft * 12 + inches) * 2.54
            _uiState.value = s.copy(isMetricHeight = true, heightValue = if (cm > 0) "%.1f".format(cm) else "")
        }
    }

    fun getHeightInCm(): Double? {
        val s = _uiState.value
        return if (s.isMetricHeight) s.heightValue.toDoubleOrNull()
        else { val ft = s.heightFeet.toDoubleOrNull() ?: return null; val i = s.heightInches.toDoubleOrNull() ?: 0.0; (ft * 12 + i) * 2.54 }
    }

    fun getWeightInKg(): Double? {
        val w = _uiState.value.weightValue.toDoubleOrNull() ?: return null
        return if (_uiState.value.isMetricWeight) w else w / 2.20462
    }

    fun validate(): Boolean {
        val s = _uiState.value
        val wKg = getWeightInKg()
        if (wKg == null || wKg < 20 || wKg > 350) { _uiState.value = s.copy(errorMessage = "Please enter a valid weight (20-350 kg)"); return false }
        val hCm = getHeightInCm()
        if (hCm == null || hCm < 50 || hCm > 300) { _uiState.value = s.copy(errorMessage = "Please enter a valid height (50-300 cm)"); return false }
        val age = s.age.toIntOrNull()
        if (age == null || age < 2 || age > 120) { _uiState.value = s.copy(errorMessage = "Please enter a valid age (2-120 years)"); return false }
        if (s.selectedActivityLevel.isBlank()) { _uiState.value = s.copy(errorMessage = "Please select your activity level"); return false }
        val bf = s.bodyFatPercent.toDoubleOrNull()
        if (bf != null && (bf < 2 || bf > 60)) { _uiState.value = s.copy(errorMessage = "Body fat should be between 2-60%"); return false }
        return true
    }

    fun calculate() {
        if (!validate()) return
        val s = _uiState.value
        val weightKg = getWeightInKg()!!
        val heightCm = getHeightInCm()!!
        val age = s.age.toInt()
        val bodyFat = s.bodyFatPercent.toDoubleOrNull()
        val activity = activityLevels.find { it.id == s.selectedActivityLevel }!!
        val goal = goalOptions.find { it.id == s.selectedGoal }!!

        viewModelScope.launch {
            val result = calculator.calculate(
                weightKg = weightKg, heightCm = heightCm, age = age,
                gender = s.gender, bodyFatPercent = bodyFat,
                activityMultiplier = activity.multiplier,
                activityLevelName = activity.name,
                goalAdjustment = goal.calorieAdjustment,
                goalName = goal.name,
                weeklyChangeKg = goal.weeklyChangeKg
            )

            // Calculate initial macros based on selected preset
            val preset = dietPresets.find { it.id == s.selectedDietPresetId }!!
            val macroResult = macroCalculator.calculateFromPercentages(
                totalCalories = result.safeGoalCalories,
                carbPercent = preset.carbPercent,
                proteinPercent = preset.proteinPercent,
                fatPercent = preset.fatPercent,
                weightKg = weightKg,
                presetName = preset.name,
                numberOfMeals = s.numberOfMeals
            )

            val proteinRecommendation = macroCalculator.getProteinRecommendationText(
                activity.id, goal.name
            )

            // Create meal plan
            val mealPlan = mealPlanningUseCase.createMealPlan(
                totalCalories = result.safeGoalCalories,
                proteinGrams = macroResult.proteinGrams,
                carbGrams = macroResult.carbGrams,
                fatGrams = macroResult.fatGrams,
                mealCount = s.selectedMealCount,
                customSplits = if (s.selectedMealCount == s.customMealSplits.size) s.customMealSplits else null
            )

            // Create IF plan
            val ifPlan = mealPlanningUseCase.createIFPlan(
                mealPlan = mealPlan,
                ifType = s.ifType,
                windowStartHour = s.ifWindowStart
            )

            // Create workout nutrition
            val workoutNutrition = mealPlanningUseCase.createWorkoutNutrition(
                totalCalories = result.safeGoalCalories,
                proteinGrams = macroResult.proteinGrams,
                carbGrams = macroResult.carbGrams,
                fatGrams = macroResult.fatGrams,
                workoutTime = s.workoutTime,
                enabled = s.workoutNutritionEnabled
            )

            _uiState.value = s.copy(
                result = result,
                showResult = true,
                errorMessage = null,
                isSaved = false,
                macroResult = macroResult,
                proteinRecommendationText = proteinRecommendation,
                mealPlan = mealPlan,
                ifPlan = ifPlan,
                workoutNutrition = workoutNutrition
            )
        }
    }

    fun selectDietPreset(presetId: String) {
        val s = _uiState.value
        val result = s.result ?: return
        val weightKg = getWeightInKg() ?: return

        val preset = dietPresets.find { it.id == presetId }!!

        if (presetId == "custom") {
            // Use current custom values
            val macroResult = macroCalculator.calculateFromPercentages(
                totalCalories = result.safeGoalCalories,
                carbPercent = s.customCarbPercent,
                proteinPercent = s.customProteinPercent,
                fatPercent = s.customFatPercent,
                weightKg = weightKg,
                presetName = "Custom",
                numberOfMeals = s.numberOfMeals
            )
            _uiState.value = s.copy(
                selectedDietPresetId = presetId,
                macroResult = macroResult
            )
        } else {
            val macroResult = macroCalculator.calculateFromPercentages(
                totalCalories = result.safeGoalCalories,
                carbPercent = preset.carbPercent,
                proteinPercent = preset.proteinPercent,
                fatPercent = preset.fatPercent,
                weightKg = weightKg,
                presetName = preset.name,
                numberOfMeals = s.numberOfMeals
            )
            _uiState.value = s.copy(
                selectedDietPresetId = presetId,
                macroResult = macroResult,
                customCarbPercent = preset.carbPercent,
                customProteinPercent = preset.proteinPercent,
                customFatPercent = preset.fatPercent
            )
        }
    }

    fun updateCustomMacros(carb: Int, protein: Int, fat: Int) {
        val s = _uiState.value
        val result = s.result ?: return
        val weightKg = getWeightInKg() ?: return

        // Ensure total is 100%
        val total = carb + protein + fat
        if (total != 100) return

        val macroResult = macroCalculator.calculateFromPercentages(
            totalCalories = result.safeGoalCalories,
            carbPercent = carb,
            proteinPercent = protein,
            fatPercent = fat,
            weightKg = weightKg,
            presetName = "Custom",
            numberOfMeals = s.numberOfMeals
        )

        _uiState.value = s.copy(
            customCarbPercent = carb,
            customProteinPercent = protein,
            customFatPercent = fat,
            macroResult = macroResult
        )
    }

    fun updateNumberOfMeals(meals: Int) {
        val s = _uiState.value
        val currentMacro = s.macroResult ?: return

        val updatedMacro = currentMacro.copy(numberOfMeals = meals)
        _uiState.value = s.copy(
            numberOfMeals = meals,
            macroResult = updatedMacro
        )
    }

    fun updateMealCount(count: Int) {
        val s = _uiState.value
        val result = s.result ?: return
        val macro = s.macroResult ?: return

        // Get default splits for the new meal count
        val distribution = mealPlanningUseCase.mealDistributions[count]
        val newSplits = distribution?.splits ?: List(count) { 100f / count }

        val mealPlan = mealPlanningUseCase.createMealPlan(
            totalCalories = result.safeGoalCalories,
            proteinGrams = macro.proteinGrams,
            carbGrams = macro.carbGrams,
            fatGrams = macro.fatGrams,
            mealCount = count,
            customSplits = null
        )

        val ifPlan = mealPlanningUseCase.createIFPlan(
            mealPlan = mealPlan,
            ifType = s.ifType,
            windowStartHour = s.ifWindowStart
        )

        _uiState.value = s.copy(
            selectedMealCount = count,
            customMealSplits = newSplits,
            mealPlan = mealPlan,
            ifPlan = ifPlan
        )
    }

    fun updateCustomMealSplits(splits: List<Float>) {
        val s = _uiState.value
        val result = s.result ?: return
        val macro = s.macroResult ?: return

        val mealPlan = mealPlanningUseCase.createMealPlan(
            totalCalories = result.safeGoalCalories,
            proteinGrams = macro.proteinGrams,
            carbGrams = macro.carbGrams,
            fatGrams = macro.fatGrams,
            mealCount = s.selectedMealCount,
            customSplits = splits
        )

        val ifPlan = mealPlanningUseCase.createIFPlan(
            mealPlan = mealPlan,
            ifType = s.ifType,
            windowStartHour = s.ifWindowStart
        )

        _uiState.value = s.copy(
            customMealSplits = splits,
            mealPlan = mealPlan,
            ifPlan = ifPlan
        )
    }

    fun updateIFType(type: String) {
        val s = _uiState.value
        val mealPlan = s.mealPlan ?: return

        val ifPlan = mealPlanningUseCase.createIFPlan(
            mealPlan = mealPlan,
            ifType = type,
            windowStartHour = s.ifWindowStart
        )

        _uiState.value = s.copy(
            ifType = type,
            ifPlan = ifPlan
        )
    }

    fun updateIFWindowStart(hour: Int) {
        val s = _uiState.value
        val mealPlan = s.mealPlan ?: return

        val ifPlan = mealPlanningUseCase.createIFPlan(
            mealPlan = mealPlan,
            ifType = s.ifType,
            windowStartHour = hour
        )

        _uiState.value = s.copy(
            ifWindowStart = hour,
            ifPlan = ifPlan
        )
    }

    fun updateWorkoutEnabled(enabled: Boolean) {
        val s = _uiState.value
        val result = s.result ?: return
        val macro = s.macroResult ?: return

        val workoutNutrition = mealPlanningUseCase.createWorkoutNutrition(
            totalCalories = result.safeGoalCalories,
            proteinGrams = macro.proteinGrams,
            carbGrams = macro.carbGrams,
            fatGrams = macro.fatGrams,
            workoutTime = s.workoutTime,
            enabled = enabled
        )

        _uiState.value = s.copy(
            workoutNutritionEnabled = enabled,
            workoutNutrition = workoutNutrition
        )
    }

    fun updateWorkoutTime(time: String) {
        val s = _uiState.value
        val result = s.result ?: return
        val macro = s.macroResult ?: return

        val workoutNutrition = mealPlanningUseCase.createWorkoutNutrition(
            totalCalories = result.safeGoalCalories,
            proteinGrams = macro.proteinGrams,
            carbGrams = macro.carbGrams,
            fatGrams = macro.fatGrams,
            workoutTime = time,
            enabled = s.workoutNutritionEnabled
        )

        _uiState.value = s.copy(
            workoutTime = time,
            workoutNutrition = workoutNutrition
        )
    }

    fun saveToHistory() {
        val r = _uiState.value.result ?: return
        val m = _uiState.value.macroResult
        
        historyRepository.saveEntry(CalorieHistoryEntry(
            bmr = r.usedBmr, tdee = r.tdee, goalCalories = r.safeGoalCalories,
            goalName = r.goalName, activityLevel = r.activityLevelName,
            formulaUsed = r.bmrFormulaUsed, weightKg = r.weightKg,
            heightCm = r.heightCm, age = r.age, gender = r.gender,
            bodyFatPercent = r.bodyFatPercent, weeklyChangeKg = r.weeklyChangeKg
        ))

        // Save to main unified history
        viewModelScope.launch {
            val detailsJson = JSONObject().apply {
                put("bmr", r.usedBmr)
                put("tdee", r.tdee)
                put("goalCalories", r.safeGoalCalories)
                put("goalName", r.goalName)
                put("activityLevel", r.activityLevelName)
                put("formulaUsed", r.bmrFormulaUsed)
                put("weightKg", r.weightKg)
                put("heightCm", r.heightCm)
                put("age", r.age)
                put("gender", r.gender)
                put("bodyFatPercent", r.bodyFatPercent ?: -1.0)
                put("weeklyChangeKg", r.weeklyChangeKg)
                if (m != null) {
                    put("proteinGrams", m.proteinGrams)
                    put("carbGrams", m.carbGrams)
                    put("fatGrams", m.fatGrams)
                    put("dietPreset", m.dietPresetName)
                }
            }

            val mainEntry = HistoryEntry(
                calculatorKey = CalculatorType.CALORIE.key,
                resultValue = "%.0f".format(r.safeGoalCalories),
                resultLabel = "kcal/day",
                category = r.goalName,
                detailsJson = detailsJson.toString(),
                timestamp = System.currentTimeMillis()
            )
            mainHistoryRepository.addEntry(mainEntry)
        }

        _uiState.value = _uiState.value.copy(isSaved = true)
    }

    fun goBackToInput() { _uiState.value = _uiState.value.copy(showResult = false) }

    fun clearAll() { _uiState.value = CalorieUiState(); loadProfileData() }

    fun getShareText(): String {
        val r = _uiState.value.result ?: return ""
        val m = _uiState.value.macroResult
        val mp = _uiState.value.mealPlan
        val ifp = _uiState.value.ifPlan

        val lines = mutableListOf<String>()
        lines.add("🔥 My Daily Nutrition Plan")
        lines.add("━━━━━━━━━━━━━━━━━━")
        lines.add("📊 Goal: ${r.goalName}")
        lines.add("🎯 Daily Calories: ${"%.0f".format(r.safeGoalCalories)} kcal")
        lines.add("")

        if (m != null) {
            lines.add("📈 Macros (${m.dietPresetName}):")
            lines.add("  🥩 Protein: ${"%.0f".format(m.proteinGrams)}g (${"%.0f".format(m.proteinPercent)}%)")
            lines.add("  🍞 Carbs: ${"%.0f".format(m.carbGrams)}g (${"%.0f".format(m.carbPercent)}%)")
            lines.add("  🥑 Fat: ${"%.0f".format(m.fatGrams)}g (${"%.0f".format(m.fatPercent)}%)")
            lines.add("")
        }

        if (ifp != null && ifp.type != "none") {
            lines.add("⏰ Fasting: ${ifp.type} (${ifp.fastingHours}h fast / ${ifp.eatingHours}h eat)")
            lines.add("🍴 Eating Window: ${formatHour(ifp.windowStartHour)} - ${formatHour(ifp.windowEndHour)}")
            lines.add("")
        }

        if (mp != null) {
            lines.add("🍽️ Meal Breakdown (${mp.mealCount} meals):")
            mp.meals.forEach { meal ->
                lines.add("  • ${meal.label} (${meal.suggestedTime}): ${"%.0f".format(meal.calories)} kcal")
                lines.add("    (${"%.0f".format(meal.proteinGrams)}g P | ${"%.0f".format(meal.carbGrams)}g C | ${"%.0f".format(meal.fatGrams)}g F)")
            }
            lines.add("")
        }

        lines.add("💡 Stats:")
        lines.add("TDEE: ${"%.0f".format(r.tdee)} kcal | BMR: ${"%.0f".format(r.usedBmr)} kcal")
        if (!r.isMaintenance) {
            lines.add("Projected: ${"%.2f".format(r.weeklyChangeDisplay)} kg/week")
        }
        lines.add("")
        lines.add("- Health Calculator: BMI Tracker")
        return lines.joinToString("\n")
    }

    fun toggleEducational() {
        _uiState.value = _uiState.value.copy(showEducational = !_uiState.value.showEducational)
    }

    fun toggleQuickLog() {
        _uiState.value = _uiState.value.copy(showQuickLog = !_uiState.value.showQuickLog)
    }

    fun logFood(name: String, calories: Int, protein: Float = 0f, carbs: Float = 0f, fat: Float = 0f) {
        viewModelScope.launch {
            val entry = FoodEntry(
                name = name,
                calories = calories.toDouble(),
                proteinGrams = protein.toDouble(),
                carbGrams = carbs.toDouble(),
                fatGrams = fat.toDouble(),
                mealSlot = getCurrentMealSlot(),
                isPreset = false
            )
            foodLogRepository.addEntry(entry)
        }
    }

    private fun getCurrentMealSlot(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> "Breakfast"
            in 11..14 -> "Lunch"
            in 15..17 -> "Snack"
            in 18..22 -> "Dinner"
            else -> "Other"
        }
    }

    private fun formatHour(hour: Int): String {
        val adjustedHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour < 12 || hour == 24) "AM" else "PM"
        return "$adjustedHour:00 $amPm"
    }
}
