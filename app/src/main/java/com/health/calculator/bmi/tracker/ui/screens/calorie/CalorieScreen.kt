package com.health.calculator.bmi.tracker.ui.screens.calorie

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.health.calculator.bmi.tracker.core.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: CalorieViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Calorie Requirement") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.showResult) viewModel.goBackToInput()
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = uiState.showResult,
            modifier = Modifier.padding(padding),
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "calorie_content"
        ) { showResult ->
            if (showResult && uiState.result != null) {
                CalorieResultScreen(
                    result = uiState.result!!,
                    macroResult = uiState.macroResult,
                    mealPlan = uiState.mealPlan,
                    ifPlan = uiState.ifPlan,
                    workoutNutrition = uiState.workoutNutrition,
                    selectedMealCount = uiState.selectedMealCount,
                    customMealSplits = uiState.customMealSplits,
                    ifType = uiState.ifType,
                    ifWindowStart = uiState.ifWindowStart,
                    workoutEnabled = uiState.workoutNutritionEnabled,
                    workoutTime = uiState.workoutTime,
                    dietPresets = viewModel.dietPresets,
                    selectedPresetId = uiState.selectedDietPresetId,
                    customCarbPercent = uiState.customCarbPercent,
                    customProteinPercent = uiState.customProteinPercent,
                    customFatPercent = uiState.customFatPercent,
                    numberOfMeals = uiState.numberOfMeals,
                    proteinRecommendationText = uiState.proteinRecommendationText,
                    isSaved = uiState.isSaved,
                    onSave = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveToHistory()
                    },
                    onRecalculate = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.goBackToInput()
                    },
                    onShare = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val shareText = viewModel.getShareText()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Nutrition Plan"))
                    },
                    onPresetSelected = { viewModel.selectDietPreset(it) },
                    onCustomMacrosChanged = { c, p, f -> viewModel.updateCustomMacros(c, p, f) },
                    onMealCountChanged = { viewModel.updateNumberOfMeals(it) },
                    onMealPlanCountChanged = { viewModel.updateMealCount(it) },
                    onCustomSplitsChanged = { viewModel.updateCustomMealSplits(it) },
                    onIFTypeChanged = { viewModel.updateIFType(it) },
                    onIFWindowStartChanged = { viewModel.updateIFWindowStart(it) },
                    onWorkoutEnabledChanged = { viewModel.updateWorkoutEnabled(it) },
                    onWorkoutTimeChanged = { viewModel.updateWorkoutTime(it) },
                    onTrackToday = {
                        val result = uiState.result
                        val macros = uiState.macroResult
                        if (result != null) {
                            val route = Screen.FoodLog.route + 
                                "?calories=${result.safeGoalCalories.toFloat()}" +
                                "&protein=${macros?.proteinGrams?.toFloat() ?: 0f}" +
                                "&carbs=${macros?.carbGrams?.toFloat() ?: 0f}" +
                                "&fat=${macros?.fatGrams?.toFloat() ?: 0f}"
                            navController.navigate(route)
                        }
                    },
                    onViewHistory = {
                        navController.navigate(Screen.CalorieHistory.route)
                    },
                    onNavigateToBMR = { navController.navigate(Screen.BmrCalculator.route) },
                    onNavigateToIBW = { navController.navigate(Screen.IdealWeightCalculator.route) },
                    onNavigateToBMI = { navController.navigate(Screen.BmiCalculator.route) },
                    showEducational = uiState.showEducational,
                    onToggleEducational = { viewModel.toggleEducational() }
                )
            } else {
                CalorieInputScreen(
                    uiState = uiState,
                    activityLevels = viewModel.activityLevels,
                    goalOptions = viewModel.goalOptions,
                    onUpdateWeight = viewModel::updateWeight,
                    onToggleWeightUnit = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleWeightUnit()
                    },
                    onUpdateHeight = viewModel::updateHeight,
                    onUpdateHeightFeet = viewModel::updateHeightFeet,
                    onUpdateHeightInches = viewModel::updateHeightInches,
                    onToggleHeightUnit = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleHeightUnit()
                    },
                    onUpdateAge = viewModel::updateAge,
                    onUpdateGender = viewModel::updateGender,
                    onUpdateBodyFat = viewModel::updateBodyFatPercent,
                    onToggleBodyFat = viewModel::toggleBodyFatField,
                    onSelectActivity = viewModel::selectActivityLevel,
                    onSelectGoal = viewModel::selectGoal,
                    onCalculate = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.calculate()
                    },
                    onClear = viewModel::clearAll
                )
            }
        }
    }
}
