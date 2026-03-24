package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.data.model.Gender
import com.health.calculator.bmi.tracker.data.model.HeightUnit
import com.health.calculator.bmi.tracker.data.model.WeightUnit
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.BMIGoalSection
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.BMIHealthRiskSection
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.BMIQuickCheckCard
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.BMISliderInputSection
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.InputModeToggle
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.LastUsedInputCard
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.RealTimeBMIPreview
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedInputField
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedCalculateButton
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedClearButton
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.ValidationErrorSummary
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.ValidationResult
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.EdgeCaseWarningCard
import com.health.calculator.bmi.tracker.ui.utils.rememberHapticManager
import com.health.calculator.bmi.tracker.ui.utils.rememberShakeController
import com.health.calculator.bmi.tracker.ui.utils.StaggeredEntrance
import com.health.calculator.bmi.tracker.ui.utils.pulseModifier
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.IconButtonDefaults
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.Straighten
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedInputField
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedCalculateButton
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedClearButton
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.ValidationErrorSummary
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.EdgeCaseWarningCard
import com.health.calculator.bmi.tracker.ui.utils.rememberHapticManager
import com.health.calculator.bmi.tracker.ui.utils.rememberShakeController
import com.health.calculator.bmi.tracker.ui.utils.StaggeredEntrance
import com.health.calculator.bmi.tracker.ui.utils.pulseModifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiCalculatorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: BmiViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val focusManager = LocalFocusManager.current
    val hapticManager = rememberHapticManager()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val goalData by viewModel.bmiGoalState.collectAsState()
    val goalSaveSuccess by viewModel.goalSaveSuccess.collectAsState()

    LaunchedEffect(goalSaveSuccess) {
        if (goalSaveSuccess) {
            snackbarHostState.showSnackbar(
                message = "🎯 Goal set! Track your progress with each calculation.",
                duration = SnackbarDuration.Short
            )
            viewModel.resetGoalSaveSuccess()
        }
    }

    // Tab state
    val pagerState = rememberPagerState(pageCount = { 3 })
    val tabTitles = listOf(
        "Calculator" to Icons.Outlined.Calculate,
        "Trends" to Icons.Outlined.Timeline,
        "Learn" to Icons.Filled.MenuBook
    )

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    LaunchedEffect(uiState.showResult) {
        if (uiState.showResult) {
            delay(200)
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusManager.clearFocus() }
            ),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BMI Calculator",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (uiState.showResult) "Your Result"
                                   else if (pagerState.currentPage == 1) "Your BMI History"
                                   else if (pagerState.currentPage == 2) "Educational Content"
                                   else "WHO Standard Classification",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.showResult)
                                uiState.bmiResult?.let { Color(it.category.colorHex) } ?: Color(0xFF1E88E5)
                            else Color(0xFF1E88E5).copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.showResult) {
                                viewModel.recalculate()
                            } else {
                                onNavigateBack()
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!uiState.showResult && pagerState.currentPage == 0) {
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Default.List, contentDescription = "History")
                        }
                        AnimatedClearButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.clearAll()
                                hapticManager.lightTap()
                            }
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading || uiState.isCalculating) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF1E88E5))
                    if (uiState.isCalculating) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Calculating...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { it / 6 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // ── Tab Bar ────────────────────────────────────────
                    if (!uiState.showResult) {
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF1E88E5),
                            indicator = { tabPositions ->
                                if (pagerState.currentPage < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                        height = 3.dp,
                                        color = Color(0xFF1E88E5)
                                    )
                                }
                            },
                            divider = {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                    thickness = 0.5.dp
                                )
                            }
                        ) {
                            tabTitles.forEachIndexed { index, (title, icon) ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(icon, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = title,
                                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    },
                                    selectedContentColor = Color(0xFF1E88E5),
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // ── Content ────────────────────────────────────────
                    if (uiState.showResult && uiState.bmiResult != null) {
                        // Result view (no tabs)
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item(key = "result") {
                                BmiResultSection(
                                    result = uiState.bmiResult!!,
                                    isSavedToHistory = uiState.isSavedToHistory,
                                    shareText = uiState.shareText,
                                    onRecalculate = { viewModel.recalculate() },
                                    onDismissSaveConfirmation = { viewModel.dismissSaveConfirmation() }
                                )
                            }
                            
                            item(key = "healthRisk") {
                                BMIHealthRiskSection(
                                    bmi = uiState.bmiResult!!.bmiValue.toFloat(),
                                    age = uiState.bmiResult!!.inputAge,
                                    isMale = uiState.bmiResult!!.inputGender == Gender.MALE
                                )
                            }
                            
                            item(key = "goal") {
                                BMIGoalSection(
                                    currentBMI = uiState.bmiResult!!.bmiValue.toFloat(),
                                    currentWeight = uiState.bmiResult!!.inputWeightKg.toFloat(),
                                    heightCm = uiState.bmiResult!!.inputHeightCm.toFloat(),
                                    goalData = goalData,
                                    isUnitKg = uiState.bmiResult!!.displayWeightUnit == WeightUnit.KG,
                                    onSetGoal = { targetBMI, targetWeight ->
                                        viewModel.setGoal(targetBMI, targetWeight)
                                    },
                                    onClearGoal = {
                                        viewModel.clearGoal()
                                    }
                                )
                            }
                        }
                    } else {
                        // Tabbed view: Calculator / Learn
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> {
                                    // Calculator input
                                    BmiInputContent(
                                        uiState = uiState,
                                        viewModel = viewModel
                                    )
                                }
                                1 -> {
                                    // Trends
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        item(key = "trends") {
                                            BmiTrendSection(trendData = uiState.trendData)
                                        }
                                    }
                                }
                                2 -> {
                                    // Education content
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        item(key = "education") {
                                            BmiEducationContent()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BmiInputContent(
    uiState: BmiInputUiState,
    viewModel: BmiViewModel
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val useSliders by viewModel.useSliderInput.collectAsState()
    val sliderWeightKg by viewModel.sliderWeightKg.collectAsState()
    val sliderHeightCm by viewModel.sliderHeightCm.collectAsState()
    val lastUsedInput by viewModel.lastUsedInput.collectAsState()
    
    val hapticManager = rememberHapticManager()
    
    val weightShake = rememberShakeController()
    val heightShake = rememberShakeController()
    val ageShake = rememberShakeController()
    
    LaunchedEffect(uiState.validationState.shouldShakeWeight) {
        if (uiState.validationState.shouldShakeWeight) {
            hapticManager.errorPattern()
            weightShake.shake()
        }
    }
    
    LaunchedEffect(uiState.validationState.shouldShakeHeight) {
        if (uiState.validationState.shouldShakeHeight) {
            hapticManager.errorPattern()
            heightShake.shake()
        }
    }
    
    LaunchedEffect(uiState.validationState.shouldShakeAge) {
        if (uiState.validationState.shouldShakeAge) {
            hapticManager.errorPattern()
            ageShake.shake()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Profile Data Banner ---
        if (uiState.hasAnyProfileData && !uiState.isProfileLoaded && !uiState.isLoading) {
            ProfileDataBanner(onRestore = { viewModel.restoreProfileData() })
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Quick Check Section ---
        if (uiState.hasAnyProfileData) {
            BMIQuickCheckCard(
                profileName = null,
                profileWeightKg = uiState.weightKg.toFloat(),
                profileHeightCm = uiState.heightCm.toFloat(),
                profileAge = uiState.age,
                profileIsMale = uiState.selectedGender == Gender.MALE,
                isUnitKg = uiState.weightUnit == WeightUnit.KG,
                isUnitCm = uiState.heightUnit == HeightUnit.CM,
                onQuickCheck = {
                    viewModel.performQuickCheck()
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- Last Used Input Card ---
        if (lastUsedInput.isValid && !uiState.hasAnyProfileData) {
            StaggeredEntrance(index = 0, totalItems = 5) {
                LastUsedInputCard(
                    weightKg = lastUsedInput.weightKg,
                    heightCm = lastUsedInput.heightCm,
                    age = lastUsedInput.age,
                    isMale = lastUsedInput.isMale,
                    isUnitKg = uiState.weightUnit == WeightUnit.KG,
                    isUnitCm = uiState.heightUnit == HeightUnit.CM,
                    timestamp = lastUsedInput.timestamp,
                    onApply = {
                        viewModel.applyLastUsedInput(lastUsedInput)
                        hapticManager.mediumImpact()
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- Validation Errors Summary ---
        ValidationErrorSummary(
            validationResult = com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.ValidationResult(
                isValid = uiState.validationState.isValid,
                weightError = uiState.validationState.weightError,
                heightError = uiState.validationState.heightError,
                ageError = uiState.validationState.ageError
            ),
            visible = uiState.validationState.hasAttemptedCalculation && uiState.validationState.hasAnyError,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- Edge Case Message ---
        uiState.edgeCaseMessage?.let { message ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                EdgeCaseWarningCard(
                    edgeCaseMessage = message,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // --- Gender Selection ---
        SectionLabel("Gender")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GenderSelectButton(
                gender = Gender.MALE,
                isSelected = uiState.selectedGender == Gender.MALE,
                isFromProfile = uiState.genderFromProfile && uiState.selectedGender == Gender.MALE,
                modifier = Modifier.weight(1f),
                onClick = { 
                    viewModel.selectGender(Gender.MALE) 
                    hapticManager.lightTap()
                }
            )
            GenderSelectButton(
                gender = Gender.FEMALE,
                isSelected = uiState.selectedGender == Gender.FEMALE,
                isFromProfile = uiState.genderFromProfile && uiState.selectedGender == Gender.FEMALE,
                modifier = Modifier.weight(1f),
                onClick = { 
                    viewModel.selectGender(Gender.FEMALE) 
                    hapticManager.lightTap()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Age Input ---
        SectionLabel("Age")
        FormCard {
            AnimatedInputField(
                value = uiState.ageField.text,
                onValueChange = { viewModel.updateAge(it) },
                label = "Age (Years)",
                icon = Icons.Rounded.Person,
                errorMessage = if (uiState.ageField.isError) uiState.ageField.errorMessage else null,
                shakeController = ageShake,
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
            if (uiState.ageField.isFromProfile && !uiState.ageField.wasOverridden) {
                ProfileFieldBadge()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        InputModeToggle(
            useSliders = useSliders,
            onToggle = { 
                viewModel.setUseSliderInput(it)
                hapticManager.lightTap()
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = useSliders,
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically { it / 4 })
                    .togetherWith(fadeOut(tween(200)) + slideOutVertically { -it / 4 })
            },
            label = "inputModeSwitch"
        ) { isSliderMode ->
            if (isSliderMode) {
                BMISliderInputSection(
                    weightKg = sliderWeightKg,
                    heightCm = sliderHeightCm,
                    isUnitKg = uiState.weightUnit == WeightUnit.KG,
                    isUnitCm = uiState.heightUnit == HeightUnit.CM,
                    onWeightChange = { viewModel.updateSliderWeight(it) },
                    onHeightChange = { viewModel.updateSliderHeight(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // --- Weight Input ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionLabel("Weight", modifier = Modifier.padding(bottom = 0.dp))
            UnitToggle(
                leftLabel = "kg",
                rightLabel = "lbs",
                isLeftSelected = uiState.weightUnit == WeightUnit.KG,
                onToggle = { viewModel.toggleWeightUnit() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FormCard {
            AnimatedInputField(
                value = uiState.weightField.text,
                onValueChange = { viewModel.updateWeight(it) },
                label = "Your Weight",
                icon = Icons.Outlined.MonitorWeight,
                errorMessage = if (uiState.weightField.isError) uiState.weightField.errorMessage else null,
                shakeController = weightShake,
                suffix = if (uiState.weightUnit == WeightUnit.KG) "kg" else "lbs",
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
            if (uiState.weightField.isFromProfile && !uiState.weightField.wasOverridden) {
                ProfileFieldBadge()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Height Input ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionLabel("Height", modifier = Modifier.padding(bottom = 0.dp))
            UnitToggle(
                leftLabel = "cm",
                rightLabel = "ft/in",
                isLeftSelected = uiState.heightUnit == HeightUnit.CM,
                onToggle = { viewModel.toggleHeightUnit() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FormCard {
            if (uiState.heightUnit == HeightUnit.CM) {
                AnimatedInputField(
                    value = uiState.heightCmField.text,
                    onValueChange = { viewModel.updateHeightCm(it) },
                    label = "Your Height",
                    icon = Icons.Outlined.Height,
                    errorMessage = if (uiState.heightCmField.isError) uiState.heightCmField.errorMessage else null,
                    shakeController = heightShake,
                    suffix = "cm",
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        if (uiState.isCalculateEnabled) {
                            viewModel.calculateBmi()
                        }
                    }
                )
                if (uiState.heightCmField.isFromProfile && !uiState.heightCmField.wasOverridden) {
                    ProfileFieldBadge()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedInputField(
                            value = uiState.heightFeetField.text,
                            onValueChange = { viewModel.updateHeightFeet(it) },
                            label = "Feet",
                            icon = Icons.Outlined.Height,
                            errorMessage = if (uiState.heightFeetField.isError) uiState.heightFeetField.errorMessage else null,
                            shakeController = heightShake,
                            suffix = "ft",
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Next,
                            onImeAction = { focusManager.moveFocus(FocusDirection.Right) }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedInputField(
                            value = uiState.heightInchesField.text,
                            onValueChange = { viewModel.updateHeightInches(it) },
                            label = "Inches",
                            icon = Icons.Outlined.Height,
                            errorMessage = if (uiState.heightInchesField.isError) uiState.heightInchesField.errorMessage else null,
                            shakeController = heightShake,
                            suffix = "in",
                            imeAction = ImeAction.Done,
                            onImeAction = {
                                focusManager.clearFocus()
                                if (uiState.isCalculateEnabled) {
                                    viewModel.calculateBmi()
                                }
                            }
                        )
                    }
                }
                if (uiState.heightFeetField.isFromProfile && !uiState.heightFeetField.wasOverridden) {
                    ProfileFieldBadge()
                }
            }
        }
        
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Action Buttons ---
        AnimatedCalculateButton(
            onClick = {
                focusManager.clearFocus()
                hapticManager.mediumImpact()
                viewModel.calculateBmi()
                scope.launch {
                    scrollState.animateScrollTo(0)
                }
            },
            isLoading = uiState.validationState.isCalculating,
            enabled = uiState.isCalculateEnabled,
            modifier = pulseModifier(
                enabled = uiState.isReadyToCalculate && !uiState.showResult
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Calculates your Body Mass Index",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier.padding(bottom = 8.dp)) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun FormCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun ProfileFieldBadge() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 8.dp, bottomEnd = 0.dp, topEnd = 8.dp),
        modifier = Modifier
            .padding(top = 8.dp, end = 8.dp)
    ) {
        Text(
            text = "From Profile",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun GenderSelectButton(
    gender: Gender,
    isSelected: Boolean,
    isFromProfile: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Surface(
        modifier = modifier
            .height(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person, // Use Person icon
                    contentDescription = null,
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = gender.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
            if (isFromProfile) {
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    ProfileFieldBadge()
                }
            }
        }
    }
}

@Composable
private fun UnitToggle(
    leftLabel: String,
    rightLabel: String,
    isLeftSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left toggle
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isLeftSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isLeftSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = leftLabel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            // Right toggle
            Surface(
                shape = RoundedCornerShape(50),
                color = if (!isLeftSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (!isLeftSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = rightLabel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileDataBanner(onRestore: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Profile data found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = "USE IT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onRestore)
            )
        }
    }
}
