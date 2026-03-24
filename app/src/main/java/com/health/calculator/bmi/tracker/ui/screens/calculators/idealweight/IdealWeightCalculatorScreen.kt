package com.health.calculator.bmi.tracker.ui.screens.calculators.idealweight

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.ui.utils.rememberHapticManager
import com.health.calculator.bmi.tracker.ui.utils.rememberShakeController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdealWeightCalculatorScreen(
    onNavigateBack: () -> Unit,
    profileHeightCm: Float = 0f,
    profileAge: Int = 0,
    profileIsMale: Boolean = true,
    profileUnitCm: Boolean = true,
    viewModel: IdealWeightViewModel = viewModel()
) {
    val inputState by viewModel.inputState.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val isCalculating by viewModel.isCalculating.collectAsState()
    val triggerShake by viewModel.triggerShake.collectAsState()
    val resultData by viewModel.resultData.collectAsState()
    val showResults by viewModel.showResults.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val context = LocalContext.current
    val haptic = rememberHapticManager()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Tabs
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Calculator", "Learn")

    // Shake Controllers
    val heightShakeController = rememberShakeController()
    val ageShakeController = rememberShakeController()

    // Load profile once
    LaunchedEffect(Unit) {
        viewModel.populateFromProfile(
            heightCm = profileHeightCm,
            age = profileAge,
            isMale = profileIsMale,
            isUnitCm = profileUnitCm
        )
    }

    // Handle shake triggers
    LaunchedEffect(triggerShake) {
        if (triggerShake > 0) {
            haptic.errorPattern()
            if (validationState.heightError != null) launch { heightShakeController.shake() }
            if (validationState.ageError != null) launch { ageShakeController.shake() }
        }
    }

    // Save success snackbar
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            snackbarHostState.showSnackbar("✅ Result saved to history!")
            viewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Ideal Body Weight", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {},
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { 
                                haptic.lightTap()
                                selectedTab = index 
                            },
                            text = { 
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        if (!showResults) {
                            IdealWeightInputSection(
                                inputState = inputState,
                                validationState = validationState,
                                isCalculating = isCalculating,
                                onHeightUpdate = viewModel::updateHeight,
                                onHeightFeetUpdate = viewModel::updateHeightFeet,
                                onHeightInchesUpdate = viewModel::updateHeightInches,
                                onAgeUpdate = viewModel::updateAge,
                                onGenderUpdate = viewModel::updateGender,
                                onToggleHeightUnit = viewModel::toggleHeightUnit,
                                onCalculate = {
                                    val valid = viewModel.onCalculate()
                                    if (valid) {
                                        haptic.successPattern()
                                    }
                                },
                                onClearAll = viewModel::clearAll,
                                weightShakeController = rememberShakeController(), // Not used 
                                heightShakeController = heightShakeController,
                                ageShakeController = ageShakeController
                            )
                        } else {
                            resultData?.let { data ->
                                IdealWeightResultSection(
                                    resultData = data,
                                    isSaved = isSaved,
                                    onSave = {
                                        haptic.successPattern()
                                        viewModel.saveToHistory()
                                    },
                                    onRecalculate = {
                                        haptic.lightTap()
                                        viewModel.recalculate()
                                    },
                                    onShare = {
                                        haptic.lightTap()
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, viewModel.getShareText())
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Results"))
                                    },
                                    visible = showResults
                                )
                            }
                        }
                    }
                }
                1 -> IdealWeightEducationContent()
            }
        }
    }
}
