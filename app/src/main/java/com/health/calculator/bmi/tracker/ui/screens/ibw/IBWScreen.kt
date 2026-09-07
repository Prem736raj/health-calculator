package com.health.calculator.bmi.tracker.ui.screens.ibw

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R


import androidx.hilt.navigation.compose.hiltViewModel

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IBWScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBMI: () -> Unit,
    onNavigateToBMR: () -> Unit,
    onNavigateToWHR: () -> Unit,
    viewModel: IBWViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.txt_ideal_body_weight)) },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            uiState.showGoalPlan -> viewModel.hideGoalPlan()
                            uiState.showResult -> viewModel.goBackToInput()
                            else -> onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    com.health.calculator.bmi.tracker.ui.components.CalculatorQualityAction(
                        com.health.calculator.bmi.tracker.presentation.navigation.CalculatorDestination.IDEAL_WEIGHT
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = when {
                uiState.showGoalPlan && uiState.result != null -> "goal"
                uiState.showResult && uiState.result != null -> "result"
                else -> "input"
            },
            modifier = Modifier.padding(padding),
            transitionSpec = {
                if (targetState == "goal" || (targetState == "result" && initialState == "input")) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "ibw_content"
        ) { screen ->
            when (screen) {
                "goal" -> {
                    IBWGoalPlanScreen(
                        result = uiState.result!!,
                        showInKg = uiState.showUnitInKg,
                        existingGoal = uiState.existingGoal,
                        paceOptions = uiState.paceOptions,
                        onGoalSelected = { targetKg, source ->
                            viewModel.selectGoal(targetKg, source)
                        },
                        onPaceSelected = { viewModel.selectPace(it) },
                        onSaveGoal = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.saveGoal()
                        },
                        onClearGoal = { viewModel.clearGoal() },
                        onNavigateToBMR = { calorieAdjustment ->
                            // Optional: Implementation for navigation to BMR with specific deficit
                        },
                        selectedGoalSource = uiState.selectedGoalSource,
                        selectedGoalWeightKg = uiState.selectedGoalWeightKg,
                        selectedPace = uiState.selectedPace
                    )
                }
                "result" -> {
                    IBWResultScreen(
                        result = uiState.result!!,
                        showInKg = uiState.showUnitInKg,
                        isSaved = uiState.isSaved,
                        additionalMetrics = uiState.additionalMetrics,
                        historyEntries = uiState.historyEntries,
                        historyStatistics = uiState.historyStatistics,
                        showEducational = uiState.showEducational,
                        showHistory = uiState.showHistory,
                        onToggleUnit = { viewModel.toggleResultUnit() },
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
                            context.startActivity(
                                Intent.createChooser(sendIntent, "Share IBW Result")
                            )
                        },
                        onSetGoal = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showGoalPlan()
                        },
                        onToggleEducational = { viewModel.toggleEducational() },
                        onToggleHistory = { viewModel.toggleHistory() },
                        onDeleteHistoryEntry = { viewModel.deleteHistoryEntry(it) },
                        onNavigateToBMI = onNavigateToBMI,
                        onNavigateToBMR = onNavigateToBMR,
                        onNavigateToWHR = onNavigateToWHR
                    )
                }
                else -> {
                    IBWInputContent(
                        uiState = uiState,
                        onUpdateHeight = viewModel::updateHeight,
                        onUpdateHeightFeet = viewModel::updateHeightFeet,
                        onUpdateHeightInches = viewModel::updateHeightInches,
                        onToggleHeightUnit = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleHeightUnit()
                        },
                        onUpdateGender = viewModel::updateGender,
                        onUpdateFrameSize = viewModel::updateFrameSize,
                        onUpdateWrist = viewModel::updateWristCircumference,
                        onDetermineFrame = viewModel::determineFrameSizeFromWrist,
                        onUpdateAge = viewModel::updateAge,
                        onUpdateCurrentWeight = viewModel::updateCurrentWeight,
                        onToggleWeightUnit = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleWeightUnit()
                        },
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
}

@Composable
private fun IBWInputContent(
    uiState: IBWUiState,
    onUpdateHeight: (String) -> Unit,
    onUpdateHeightFeet: (String) -> Unit,
    onUpdateHeightInches: (String) -> Unit,
    onToggleHeightUnit: () -> Unit,
    onUpdateGender: (String) -> Unit,
    onUpdateFrameSize: (String) -> Unit,
    onUpdateWrist: (String) -> Unit,
    onDetermineFrame: () -> Unit,
    onUpdateAge: (String) -> Unit,
    onUpdateCurrentWeight: (String) -> Unit,
    onToggleWeightUnit: () -> Unit,
    onCalculate: () -> Unit,
    onClear: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showFrameHelper by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.txt_ideal_body_weight_is_the_weigh),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Profile data indicator
        if (uiState.isProfileDataUsed) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.txt_using_profile_data),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // Gender Selection
        Text(
            text = stringResource(R.string.txt_gender),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Male", "Female").forEach { gender ->
                FilterChip(
                    selected = uiState.gender == gender,
                    onClick = { onUpdateGender(gender) },
                    label = { Text(gender) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        if (uiState.gender == gender) {
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        }
                    }
                )
            }
        }

        // Height Input
        Text(
            text = stringResource(R.string.txt_height),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (uiState.isMetricHeight) {
                OutlinedTextField(
                    value = uiState.heightValue,
                    onValueChange = onUpdateHeight,
                    label = { Text(stringResource(R.string.txt_height_cm)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = uiState.heightFeet,
                    onValueChange = onUpdateHeightFeet,
                    label = { Text(stringResource(R.string.txt_feet)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.heightInches,
                    onValueChange = onUpdateHeightInches,
                    label = { Text(stringResource(R.string.txt_inches)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            FilledTonalIconButton(
                onClick = onToggleHeightUnit,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    if (uiState.isMetricHeight) "ft" else "cm",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Frame Size Selection
        Text(
            text = stringResource(R.string.txt_frame_size),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Small", "Medium", "Large").forEach { size ->
                FilterChip(
                    selected = uiState.frameSize == size,
                    onClick = { onUpdateFrameSize(size) },
                    label = { Text(size) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        if (uiState.frameSize == size) {
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        }
                    }
                )
            }
        }

        // Frame Size Helper
        TextButton(
            onClick = { showFrameHelper = !showFrameHelper },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                if (showFrameHelper) Icons.Default.ExpandLess else Icons.Default.Help,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.txt_don_t_know_your_frame_size), style = MaterialTheme.typography.bodySmall)
        }

        AnimatedVisibility(visible = showFrameHelper) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.txt_measure_your_wrist),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        stringResource(R.string.txt_wrap_a_flexible_tape_measure_a),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )

                    OutlinedTextField(
                        value = uiState.wristCircumference,
                        onValueChange = onUpdateWrist,
                        label = { Text(stringResource(R.string.txt_wrist_circumference_cm)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = onDetermineFrame,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState.wristCircumference.isNotBlank()
                    ) {
                        Text(stringResource(R.string.txt_determine_frame_size))
                    }

                    Text(
                        text = if (uiState.gender == "Male") {
                            "Male (height > 165cm):\n• Small: < 16.5 cm\n• Medium: 16.5 - 19 cm\n• Large: > 19 cm"
                        } else {
                            "Female:\n• Under 155cm: S < 14, M 14-14.6, L > 14.6\n• 155-163cm: S < 15.2, M 15.2-15.9, L > 15.9\n• Over 163cm: S < 16, M 16-16.5, L > 16.5"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Age Input
        OutlinedTextField(
            value = uiState.age,
            onValueChange = onUpdateAge,
            label = { Text(stringResource(R.string.txt_age_optional)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Current Weight Input
        Text(
            text = stringResource(R.string.txt_current_weight_highly_recommen),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = uiState.currentWeight,
                onValueChange = onUpdateCurrentWeight,
                label = { Text(if (uiState.isMetricWeight) "Weight (kg)" else "Weight (lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            FilledTonalIconButton(
                onClick = onToggleWeightUnit,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    if (uiState.isMetricWeight) "lbs" else "kg",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Error message
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calculate Button
        Button(
            onClick = onCalculate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.txt_calculate_ideal_weight),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Clear button
        TextButton(
            onClick = onClear,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                Icons.Default.Clear,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.txt_clear_all_1))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
