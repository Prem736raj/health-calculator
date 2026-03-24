// File: com/health/calculator/bmi/tracker/ui/screens/bmr/BMRCalculatorScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.data.model.BMRFormula
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedCalculateButton
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedClearButton
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedInputField
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.ValidationErrorSummary
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.ValidationResult
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.BMRBodyFatInput
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.BMRFormulaSelector
import com.health.calculator.bmi.tracker.ui.utils.StaggeredEntrance
import com.health.calculator.bmi.tracker.ui.utils.rememberHapticManager
import com.health.calculator.bmi.tracker.ui.utils.rememberShakeController
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.BMRResultSection
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.BMRActionButtons
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.TDEESection
import com.health.calculator.bmi.tracker.ui.utils.CascadeAnimatedItem
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.MacroSection
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.TEFSection
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.MealTimingSection
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.BMRAgeCurveSection
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.BMRFormulaComparisonSection
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.BMREducationalSection
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.BMRTrendSection
import com.health.calculator.bmi.tracker.ui.screens.bmr.components.BMRWarningCards
import com.health.calculator.bmi.tracker.data.model.GoalType
import com.health.calculator.bmi.tracker.data.model.MacroBreakdown
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.InputModeToggle
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.WeightSliderPicker
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.HeightSliderPicker
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMRCalculatorScreen(
    onNavigateBack: () -> Unit,
    // Pass profile data from your navigation/profile state:
    profileWeightKg: Float = 0f,
    profileHeightCm: Float = 0f,
    profileAge: Int = 0,
    profileIsMale: Boolean = true,
    profileUnitKg: Boolean = true,
    profileUnitCm: Boolean = true,
    profileActivityLevel: String? = null,
    viewModel: BMRViewModel = viewModel()
) {
    val inputState by viewModel.inputState.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val isCalculating by viewModel.isCalculating.collectAsState()
    val triggerShake by viewModel.triggerShake.collectAsState()
    val resultData by viewModel.resultData.collectAsState()
    val showResults by viewModel.showResults.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val selectedActivityLevel by viewModel.selectedActivityLevel.collectAsState()
    val currentMacros by viewModel.currentMacros.collectAsState()
    val profileActivityLevelState by viewModel.profileActivityLevel.collectAsState()
    val tefData by viewModel.tefData.collectAsState()
    val bmrTrendStats by viewModel.bmrTrendStats.collectAsState()
    val bmrHistoryPoints by viewModel.bmrHistoryPoints.collectAsState()
    val calculationWarnings by viewModel.calculationWarnings.collectAsState()

    var useSliders by remember { mutableStateOf(true) }
    var selectedCalorieTarget by remember { mutableFloatStateOf(0f) }

    var currentProteinGrams by remember { mutableFloatStateOf(0f) }
    var currentCarbsGrams by remember { mutableFloatStateOf(0f) }
    var currentFatGrams by remember { mutableFloatStateOf(0f) }

    // Update selected calorie target when TDEE changes
    LaunchedEffect(showResults, selectedActivityLevel) {
        if (showResults && resultData != null) {
            selectedCalorieTarget = (resultData!!.primaryBMR * selectedActivityLevel.multiplier).toFloat()
        }
    }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Haptic when results appear
    LaunchedEffect(showResults) {
        if (showResults) {
            haptic.mediumImpact()
        }
    }

    // Save success snackbar
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar(
                message = "✅ BMR result saved to history!",
                duration = SnackbarDuration.Short
            )
            viewModel.resetSaveSuccess()
        }
    }

    // Shake controllers
    val weightShakeController = rememberShakeController()
    val heightShakeController = rememberShakeController()
    val ageShakeController = rememberShakeController()
    val bodyFatShakeController = rememberShakeController()

    // Populate from profile on first load
    LaunchedEffect(Unit) {
        viewModel.populateFromProfile(
            weightKg = profileWeightKg,
            heightCm = profileHeightCm,
            age = profileAge,
            isMale = profileIsMale,
            isUnitKg = profileUnitKg,
            isUnitCm = profileUnitCm,
            activityLevel = profileActivityLevel
        )
    }

    // Handle shake triggers
    LaunchedEffect(triggerShake) {
        if (triggerShake > 0) {
            haptic.errorPattern()
            if (validationState.weightError != null) {
                launch { weightShakeController.shake() }
            }
            if (validationState.heightError != null) {
                launch { heightShakeController.shake() }
            }
            if (validationState.ageError != null) {
                launch { ageShakeController.shake() }
            }
            if (validationState.bodyFatError != null) {
                launch { bodyFatShakeController.shake() }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BMR Calculator",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ---- BMR Explanation Header ----
            StaggeredEntrance(index = 0, totalItems = 8) {
                BMRExplanationHeader()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Profile Data Indicator ----
            if (inputState.isProfileDataUsed && !inputState.hasModifiedProfile) {
                StaggeredEntrance(index = 1, totalItems = 8) {
                    ProfileDataIndicator()
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ---- Formula Selector ----
            StaggeredEntrance(index = 2, totalItems = 8) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        BMRFormulaSelector(
                            selectedFormula = inputState.selectedFormula,
                            onFormulaSelected = { formula ->
                                haptic.lightTap()
                                viewModel.updateFormula(formula)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Input Fields Card ----
            StaggeredEntrance(index = 3, totalItems = 8) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Input Mode Toggle
                        InputModeToggle(
                            useSliders = useSliders,
                            onToggle = { useSliders = it },
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Weight Input
                        AnimatedVisibility(
                            visible = !useSliders,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            AnimatedInputField(
                                value = inputState.weightText,
                                onValueChange = { viewModel.updateWeight(it) },
                                label = if (inputState.isUnitKg) "Weight (kg)" else "Weight (lbs)",
                                icon = Icons.Outlined.MonitorWeight,
                                errorMessage = if (validationState.hasAttemptedCalculation)
                                    validationState.weightError else null,
                                shakeController = weightShakeController,
                                suffix = if (inputState.isUnitKg) "kg" else "lbs",
                                trailingContent = {
                                    UnitToggleButton(
                                        isFirstOption = inputState.isUnitKg,
                                        firstLabel = "kg",
                                        secondLabel = "lbs",
                                        onClick = {
                                            haptic.lightTap()
                                            viewModel.toggleWeightUnit()
                                        }
                                    )
                                }
                            )
                        }

                        AnimatedVisibility(
                            visible = useSliders,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            WeightSliderPicker(
                                weightKg = if (inputState.weightKg > 0) inputState.weightKg else 70f,
                                isUnitKg = inputState.isUnitKg,
                                onWeightChange = { kg ->
                                    val formatted = if (inputState.isUnitKg) {
                                        String.format("%.1f", kg).replace(",", ".")
                                    } else {
                                        String.format("%.1f", kg * 2.20462f).replace(",", ".")
                                    }
                                    viewModel.updateWeight(formatted)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Height Input
                        AnimatedVisibility(
                            visible = !useSliders,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            if (inputState.isUnitCm) {
                                AnimatedInputField(
                                    value = inputState.heightText,
                                    onValueChange = { viewModel.updateHeight(it) },
                                    label = "Height (cm)",
                                    icon = Icons.Outlined.Height,
                                    errorMessage = if (validationState.hasAttemptedCalculation)
                                        validationState.heightError else null,
                                    shakeController = heightShakeController,
                                    suffix = "cm",
                                    trailingContent = {
                                        UnitToggleButton(
                                            isFirstOption = true,
                                            firstLabel = "cm",
                                            secondLabel = "ft-in",
                                            onClick = {
                                                haptic.lightTap()
                                                viewModel.toggleHeightUnit()
                                            }
                                        )
                                    }
                                )
                            } else {
                                // Feet-Inches mode
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AnimatedInputField(
                                        value = inputState.heightFeetText,
                                        onValueChange = { viewModel.updateHeightFeet(it) },
                                        label = "Feet",
                                        icon = Icons.Outlined.Height,
                                        errorMessage = null,
                                        shakeController = heightShakeController,
                                        suffix = "ft",
                                        keyboardType = KeyboardType.Number,
                                        modifier = Modifier.weight(1f)
                                    )
                                    AnimatedInputField(
                                        value = inputState.heightInchesText,
                                        onValueChange = { viewModel.updateHeightInches(it) },
                                        label = "Inches",
                                        icon = Icons.Outlined.Straighten,
                                        errorMessage = if (validationState.hasAttemptedCalculation)
                                            validationState.heightError else null,
                                        shakeController = heightShakeController,
                                        suffix = "in",
                                        keyboardType = KeyboardType.Number,
                                        modifier = Modifier.weight(1f),
                                        trailingContent = {
                                            UnitToggleButton(
                                                isFirstOption = false,
                                                firstLabel = "cm",
                                                secondLabel = "ft-in",
                                                onClick = {
                                                    haptic.lightTap()
                                                    viewModel.toggleHeightUnit()
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = useSliders,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            HeightSliderPicker(
                                heightCm = if (inputState.heightCm > 0) inputState.heightCm else 170f,
                                isUnitCm = inputState.isUnitCm,
                                onHeightChange = { cm ->
                                    if (inputState.isUnitCm) {
                                        viewModel.updateHeight(cm.roundToInt().toString())
                                    } else {
                                        val totalInches = cm / 2.54
                                        val feet = (totalInches / 12).toInt()
                                        val inches = (totalInches % 12).roundToInt()
                                        viewModel.updateHeightFeet(feet.toString())
                                        viewModel.updateHeightInches(inches.toString())
                                    }
                                }
                            )
                        }

                        // WHO formula note
                        AnimatedVisibility(
                            visible = inputState.selectedFormula == BMRFormula.WHO_FAO_UNU,
                            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(modifier = Modifier.padding(8.dp)) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "The WHO/FAO/UNU equation primarily uses weight. Height is optional for this formula.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Age Input
                        AnimatedInputField(
                            value = inputState.ageText,
                            onValueChange = { viewModel.updateAge(it) },
                            label = "Age",
                            icon = Icons.Outlined.Cake,
                            errorMessage = if (validationState.hasAttemptedCalculation)
                                validationState.ageError else null,
                            shakeController = ageShakeController,
                            suffix = "years",
                            keyboardType = KeyboardType.Number,
                            imeAction = if (inputState.selectedFormula.requiresBodyFat)
                                ImeAction.Next else ImeAction.Done
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Gender Selector
                        GenderSelector(
                            isMale = inputState.isMale,
                            onGenderChange = { isMale ->
                                haptic.lightTap()
                                viewModel.updateGender(isMale)
                            }
                        )
                    }
                }
            }

            // ---- Body Fat Input (conditional) ----
            StaggeredEntrance(index = 4, totalItems = 8) {
                BMRBodyFatInput(
                    visible = inputState.selectedFormula.requiresBodyFat,
                    bodyFatText = inputState.bodyFatText,
                    onBodyFatChange = { viewModel.updateBodyFat(it) },
                    errorMessage = if (validationState.hasAttemptedCalculation)
                        validationState.bodyFatError else null,
                    shakeController = bodyFatShakeController,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Validation Error Summary ----
            ValidationErrorSummary(
                validationResult = ValidationResult(
                    isValid = validationState.isValid,
                    weightError = validationState.weightError,
                    heightError = validationState.heightError,
                    ageError = validationState.ageError
                ),
                visible = validationState.hasAttemptedCalculation && validationState.hasAnyError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Calculate & Clear Buttons ----
            StaggeredEntrance(index = 5, totalItems = 8) {
                Column {
                    AnimatedCalculateButton(
                        onClick = {
                            haptic.lightTap()
                            val isValid = viewModel.onCalculate()
                            if (isValid) {
                                scope.launch {
                                    delay(600)
                                    // Scroll to results
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                        },
                        isLoading = isCalculating,
                        enabled = !isCalculating
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedClearButton(
                            onClick = {
                                haptic.lightTap()
                                viewModel.clearAll()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---- BMR Quick Info ----
            StaggeredEntrance(index = 6, totalItems = 8) {
                BMRQuickInfoCard()
            }

            // ---- Results Section ----
            if (showResults && resultData != null) {
                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Result header
                CascadeAnimatedItem(index = 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📋", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your BMR Results",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Edge case warnings
                BMRWarningCards(
                    warnings = calculationWarnings,
                    visible = showResults && calculationWarnings.isNotEmpty()
                )

                if (calculationWarnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Main results
                BMRResultSection(
                    resultData = resultData!!,
                    visible = showResults
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                CascadeAnimatedItem(index = 4, baseDelay = 100) {
                    BMRActionButtons(
                        isSaved = isSaved,
                        onSave = {
                            haptic.successPattern()
                            viewModel.saveToHistory()
                        },
                        onRecalculate = {
                            haptic.lightTap()
                            viewModel.recalculate()
                            scope.launch {
                                delay(200)
                                scrollState.animateScrollTo(0)
                            }
                        },
                        onShare = {
                            haptic.lightTap()
                            val shareText = viewModel.getShareText()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(
                                Intent.createChooser(sendIntent, "Share BMR Result")
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---- BMR Trend ----
                CascadeAnimatedItem(index = 5, baseDelay = 100) {
                    BMRTrendSection(
                        historyPoints = bmrHistoryPoints,
                        stats = bmrTrendStats,
                        visible = showResults
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TDEE Section
                CascadeAnimatedItem(index = 6, baseDelay = 100) {
                    TDEESection(
                        bmr = resultData!!.primaryBMR,
                        selectedActivityLevel = selectedActivityLevel,
                        profileActivityLevel = profileActivityLevelState,
                        onActivityLevelChanged = { level ->
                            haptic.lightTap()
                            viewModel.updateActivityLevel(level)
                            // Update calorie target to maintain weight by default
                            selectedCalorieTarget = (resultData!!.primaryBMR * level.multiplier).toFloat()
                        },
                        onCalorieTargetChanged = { calories ->
                            haptic.lightTap()
                            selectedCalorieTarget = calories
                        },
                        visible = showResults
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Macro Section
                CascadeAnimatedItem(index = 7, baseDelay = 100) {
                    MacroSection(
                        totalCalories = selectedCalorieTarget.let {
                            if (it > 0) it else (resultData!!.primaryBMR * selectedActivityLevel.multiplier).toFloat()
                        },
                        visible = showResults,
                        onMacroChanged = { proteinPct, carbsPct, fatPct ->
                            viewModel.updateMacros(proteinPct, carbsPct, fatPct)
                            val cals = if (selectedCalorieTarget > 0) selectedCalorieTarget
                            else (resultData!!.primaryBMR * selectedActivityLevel.multiplier).toFloat()
                            currentProteinGrams = cals * proteinPct / 100f / 4f
                            currentCarbsGrams = cals * carbsPct / 100f / 4f
                            currentFatGrams = cals * fatPct / 100f / 9f

                            // Update macro breakdown for share
                            viewModel.updateMacroBreakdown(
                                MacroBreakdown(
                                    totalCalories = cals,
                                    proteinPercentage = proteinPct,
                                    carbsPercentage = carbsPct,
                                    fatPercentage = fatPct
                                )
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Meal Timing Section
                CascadeAnimatedItem(index = 8, baseDelay = 100) {
                    val cals = if (selectedCalorieTarget > 0) selectedCalorieTarget
                    else (resultData!!.primaryBMR * selectedActivityLevel.multiplier).toFloat()

                    MealTimingSection(
                        totalCalories = cals,
                        proteinGrams = currentProteinGrams,
                        carbsGrams = currentCarbsGrams,
                        fatGrams = currentFatGrams,
                        visible = showResults
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TEF Section
                tefData?.let { tef ->
                    CascadeAnimatedItem(index = 9, baseDelay = 100) {
                        TEFSection(
                            tefData = tef,
                            visible = showResults
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ---- BMR Age Curve Comparison ----
                CascadeAnimatedItem(index = 10, baseDelay = 100) {
                    BMRAgeCurveSection(
                        userBMR = resultData!!.primaryBMR,
                        userAge = resultData!!.age,
                        isMale = resultData!!.isMale,
                        visible = showResults
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---- Formula Insights ----
                CascadeAnimatedItem(index = 11, baseDelay = 100) {
                    BMRFormulaComparisonSection(
                        allResults = resultData!!.allFormulaResults,
                        selectedFormula = resultData!!.selectedFormula,
                        visible = showResults
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---- Educational Content ----
                CascadeAnimatedItem(index = 12, baseDelay = 100) {
                    BMREducationalSection(
                        visible = showResults
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Medical disclaimer
                CascadeAnimatedItem(index = 13, baseDelay = 100) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Medical Disclaimer: BMR calculations are estimates based on " +
                                        "statistical formulas. Individual metabolism varies. These results " +
                                        "are for informational purposes only and should not replace " +
                                        "professional medical or nutritional advice.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                lineHeight = 16.sp,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ============================================================
// Sub-components
// ============================================================

@Composable
private fun BMRExplanationHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🔥", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Basal Metabolic Rate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Your BMR is the number of calories your body burns at complete rest — just to keep you alive (breathing, circulation, cell production).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileDataIndicator() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF4CAF50).copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF4CAF50).copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Using profile data — you can adjust any values below",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF388E3C),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GenderSelector(
    isMale: Boolean,
    onGenderChange: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "Gender",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GenderChip(
                label = "Male",
                emoji = "♂️",
                isSelected = isMale,
                onClick = { onGenderChange(true) },
                modifier = Modifier.weight(1f)
            )
            GenderChip(
                label = "Female",
                emoji = "♀️",
                isSelected = !isMale,
                onClick = { onGenderChange(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GenderChip(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "genderBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(200),
        label = "genderContent"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.97f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "genderScale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ) else null,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun UnitToggleButton(
    isFirstOption: Boolean,
    firstLabel: String,
    secondLabel: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Text(
            text = if (isFirstOption) secondLabel else firstLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BMRQuickInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💡 Good to know",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            QuickInfoItem(
                emoji = "🔥",
                text = "BMR accounts for 60-75% of your total daily calorie burn"
            )
            Spacer(modifier = Modifier.height(6.dp))
            QuickInfoItem(
                emoji = "⚡",
                text = "Your actual daily calorie needs (TDEE) = BMR × Activity Factor"
            )
            Spacer(modifier = Modifier.height(6.dp))
            QuickInfoItem(
                emoji = "📊",
                text = "BMR varies with age, gender, weight, height, and body composition"
            )
            Spacer(modifier = Modifier.height(6.dp))
            QuickInfoItem(
                emoji = "⭐",
                text = "Mifflin-St Jeor is the most widely recommended formula for most adults"
            )
        }
    }
}

@Composable
private fun QuickInfoItem(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}
