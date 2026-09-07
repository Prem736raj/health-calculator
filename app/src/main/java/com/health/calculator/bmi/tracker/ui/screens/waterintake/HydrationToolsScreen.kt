// ui/screens/waterintake/HydrationToolsScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private val WaterBlueMedium = Color(0xFF2196F3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationToolsScreen(
    viewModel: HydrationToolsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToElectrolytes: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val recentUrineColors by viewModel.recentUrineColors.collectAsStateWithLifecycle(initialValue = emptyList())
    val latestUrineColor by viewModel.latestUrineColor.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(stringResource(R.string.txt_text_placeholder_23), fontSize = 22.sp)
                        Text(stringResource(R.string.txt_hydration_tools), fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.txt_urine), fontSize = 11.sp) },
                    icon = { Text(stringResource(R.string.txt_text_placeholder_75), fontSize = 14.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.txt_symptoms), fontSize = 11.sp) },
                    icon = { Text(stringResource(R.string.txt_text_placeholder_74), fontSize = 14.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(stringResource(R.string.txt_food), fontSize = 11.sp) },
                    icon = { Text(stringResource(R.string.txt_text_placeholder_73), fontSize = 14.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text(stringResource(R.string.txt_electrolytes), fontSize = 11.sp) },
                    icon = { Text(stringResource(R.string.txt_text_placeholder_33), fontSize = 14.sp) }
                )
            }

            when (selectedTab) {
                0 -> UrineColorTab(
                    viewModel = viewModel,
                    recentEntries = recentUrineColors,
                    latestEntry = latestUrineColor,
                    isVisible = isVisible,
                    haptic = haptic
                )
                1 -> DehydrationSymptomsTab(
                    viewModel = viewModel,
                    isVisible = isVisible,
                    haptic = haptic
                )
                2 -> WaterFromFoodTab(
                    goalMl = viewModel.dailyGoalMl,
                    isVisible = isVisible
                )
                3 -> ElectrolytesTab(
                    isVisible = isVisible,
                    onNavigateToFull = onNavigateToElectrolytes
                )
            }
        }
    }
}

// ─── Urine Color Tab ─────────────────────────────────────────────────────────

@Composable
private fun UrineColorTab(
    viewModel: HydrationToolsViewModel,
    recentEntries: List<UrineColorEntry>,
    latestEntry: UrineColorEntry?,
    isVisible: Boolean,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    var selectedLevel by remember { mutableStateOf<Int?>(null) }
    var showLogConfirm by remember { mutableStateOf(false) }

    // Dismiss confirm
    LaunchedEffect(viewModel.showUrineLogConfirm) {
        if (viewModel.showUrineLogConfirm) {
            showLogConfirm = true
            delay(2500)
            showLogConfirm = false
            viewModel.dismissUrineConfirm()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 }
                ) {
                    UrineChartHeaderCard()
                }
            }

            // Color chart
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 40 }
                ) {
                    UrineColorChartCard(
                        selectedLevel = selectedLevel,
                        onSelect = { level ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedLevel = level
                        },
                        onLog = { level ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.logUrineColor(level)
                            selectedLevel = null
                        }
                    )
                }
            }

            // Latest reading
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible && latestEntry != null,
                    enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 40 }
                ) {
                    latestEntry?.let { LatestUrineCard(it) }
                }
            }

            // Recent history
            if (recentEntries.isNotEmpty()) {
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, 300))
                    ) {
                        Text(
                            stringResource(R.string.txt_recent_logs_1),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                items(recentEntries.take(5)) { entry ->
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -30 }
                    ) {
                        UrineHistoryEntry(entry)
                    }
                }
            }

            // Disclaimer
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 400))
                ) {
                    UrineDisclaimerCard()
                }
            }
        }

        // Log confirmation
        androidx.compose.animation.AnimatedVisibility(
            visible = showLogConfirm,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.txt_urine_color_logged), color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun UrineChartHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFA726), Color(0xFFFF7043))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.txt_text_placeholder_75), fontSize = 40.sp)
                Column {
                    Text(
                        stringResource(R.string.txt_urine_color_chart),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        stringResource(R.string.txt_your_urine_color_is_a_quick_in),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UrineColorChartCard(
    selectedLevel: Int?,
    onSelect: (Int) -> Unit,
    onLog: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.txt_tap_your_current_urine_color),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            UrineColor.entries.forEach { color ->
                val isSelected = selectedLevel == color.level
                val borderColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    label = "urine_border_${color.level}"
                )
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.02f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "urine_scale_${color.level}"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(color.colorHex).copy(alpha = 0.1f)
                            else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(color.level) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Color swatch
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Color(color.colorHex),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                Color.Black.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${color.level}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (color.level <= 4) Color.Black.copy(alpha = 0.5f)
                            else Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // Labels
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            color.label,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                color.hydrationLevel.emoji,
                                fontSize = 12.sp
                            )
                            Text(
                                color.status,
                                fontSize = 12.sp,
                                color = when (color.hydrationLevel) {
                                    HydrationLevel.WELL_HYDRATED -> Color(0xFF4CAF50)
                                    HydrationLevel.ADEQUATE -> Color(0xFF8BC34A)
                                    HydrationLevel.SLIGHTLY_DEHYDRATED -> Color(0xFFFF9800)
                                    HydrationLevel.DEHYDRATED -> Color(0xFFF44336)
                                }
                            )
                        }
                    }

                    // Selection indicator
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSelected,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = WaterBlueMedium,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Log button
            androidx.compose.animation.AnimatedVisibility(
                visible = selectedLevel != null,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Button(
                    onClick = { selectedLevel?.let { onLog(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.txt_log_this_color), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LatestUrineCard(entry: UrineColorEntry) {
    val color = UrineColor.entries.find { it.level == entry.colorLevel } ?: return
    val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(color.colorHex), RoundedCornerShape(10.dp))
                    .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Latest: ${color.label}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "${color.hydrationLevel.emoji} ${color.status} • ${timeFormat.format(Date(entry.timestamp))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun UrineHistoryEntry(entry: UrineColorEntry) {
    val color = UrineColor.entries.find { it.level == entry.colorLevel } ?: return
    val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(color.colorHex), RoundedCornerShape(6.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(color.label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                timeFormat.format(Date(entry.timestamp)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
        Text(
            color.hydrationLevel.emoji,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun UrineDisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Info,
                null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.txt_note_urine_color_can_be_affect) +
                        "This chart is a general guide and not a medical diagnostic tool. " +
                        "Consult a doctor if you have concerns.",
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ─── Dehydration Symptoms Tab ────────────────────────────────────────────────

@Composable
private fun DehydrationSymptomsTab(
    viewModel: HydrationToolsViewModel,
    isVisible: Boolean,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val symptoms = HydrationToolsViewModel.DEHYDRATION_SYMPTOMS
    val riskLevel = viewModel.dehydrationRiskLevel
    val checkedCount = viewModel.checkedSymptoms.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 }
            ) {
                SymptomsHeaderCard()
            }
        }

        // Risk indicator
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible && checkedCount > 0,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                DehydrationRiskCard(riskLevel, checkedCount)
            }
        }

        // Symptom checklist
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 40 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.txt_check_your_symptoms),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (checkedCount > 0) {
                                TextButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.clearSymptoms()
                                }) {
                                    Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(stringResource(R.string.txt_clear), fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        symptoms.forEachIndexed { index, symptom ->
                            val isChecked = viewModel.checkedSymptoms.contains(index)
                            SymptomCheckItem(
                                symptom = symptom,
                                isChecked = isChecked,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleSymptom(index)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Recommendation
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible && checkedCount > 0,
                enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 40 }
            ) {
                DehydrationRecommendationCard(riskLevel)
            }
        }

        // Disclaimer
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 400))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        Text(
                            stringResource(R.string.txt_this_is_not_a_medical_diagnosi),
                            fontSize = 11.sp, lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SymptomsHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.txt_text_placeholder_74), fontSize = 40.sp)
                Column {
                    Text(stringResource(R.string.txt_dehydration_checker), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        stringResource(R.string.txt_check_off_any_symptoms_you_re_),
                        color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SymptomCheckItem(
    symptom: DehydrationSymptom,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isChecked) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent,
        label = "symptom_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.tertiary,
                uncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        )
        Text(symptom.icon, fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                symptom.name,
                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                symptom.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        val severityColor = when (symptom.severity) {
            SymptomSeverity.MILD -> Color(0xFFFFC107)
            SymptomSeverity.MODERATE -> Color(0xFFFF9800)
            SymptomSeverity.SEVERE -> Color(0xFFF44336)
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(severityColor, CircleShape)
        )
    }
}

@Composable
private fun DehydrationRiskCard(risk: DehydrationRisk, symptomCount: Int) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "risk_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(risk.colorHex).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(risk.emoji, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    risk.label,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(risk.colorHex)
                )
                Text(
                    "$symptomCount of ${HydrationToolsViewModel.DEHYDRATION_SYMPTOMS.size} symptoms checked",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun DehydrationRecommendationCard(risk: DehydrationRisk) {
    val recommendations = when (risk) {
        DehydrationRisk.HIGH -> listOf(
            "🚰" to "Drink water immediately — aim for 2-3 glasses right now",
            "🏥" to "If symptoms persist or worsen, contact a healthcare provider",
            "❌" to "Avoid caffeine and alcohol which can worsen dehydration",
            "🧊" to "If available, sip on an electrolyte solution",
            "🛑" to "Rest in a cool place and avoid strenuous activity"
        )
        DehydrationRisk.MODERATE -> listOf(
            "💧" to "Drink a glass of water now and continue sipping regularly",
            "⏰" to "Set a reminder to drink every 30 minutes for the next 2 hours",
            "🍉" to "Eat water-rich fruits like watermelon or cucumber",
            "🌡️" to "If you're in a hot environment, move to a cooler area"
        )
        DehydrationRisk.MILD -> listOf(
            "💧" to "Have a glass of water — your body is giving early signals",
            "📱" to "Turn on water reminders to stay on track",
            "🥤" to "Keep a water bottle within reach"
        )
        DehydrationRisk.NONE -> emptyList()
    }

    if (recommendations.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(stringResource(R.string.txt_recommendations_1), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            recommendations.forEach { (icon, text) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(icon, fontSize = 16.sp)
                    Text(text, fontSize = 13.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// ─── Water From Food Tab ─────────────────────────────────────────────────────

@Composable
private fun WaterFromFoodTab(
    goalMl: Int,
    isVisible: Boolean
) {
    val foodWaterEstimate = (goalMl * 0.2f).toInt()
    val foods = HydrationToolsViewModel.WATER_RICH_FOODS

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 }
            ) {
                FoodWaterHeaderCard(foodWaterEstimate, goalMl)
            }
        }

        // The 20% fact
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 40 }
            ) {
                FoodContributionCard(foodWaterEstimate, goalMl)
            }
        }

        // Food list header
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 200))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.txt_text_placeholder_30), fontSize = 18.sp)
                    Text(stringResource(R.string.txt_water_rich_foods), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
            }
        }

        // Food items
        itemsIndexed(foods) { index, food ->
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(300, 200 + index * 50)) +
                        slideInHorizontally(tween(300, 200 + index * 50)) { 30 }
            ) {
                FoodItemCard(food)
            }
        }

        // Tips
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 500)) + slideInVertically(tween(500, 500)) { 40 }
            ) {
                FoodHydrationTipsCard()
            }
        }
    }
}

@Composable
private fun FoodWaterHeaderCard(estimatedMl: Int, goalMl: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF66BB6A), Color(0xFF388E3C))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.txt_text_placeholder_73), fontSize = 40.sp)
                Column {
                    Text(stringResource(R.string.txt_water_from_food), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        stringResource(R.string.txt_did_you_know_about_20_of_your_),
                        color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodContributionCard(estimatedMl: Int, goalMl: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.txt_your_food_water_estimate), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "~${estimatedMl}ml",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = Color(0xFF4CAF50)
                    )
                    Text(stringResource(R.string.txt_from_food), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }

                Text(stringResource(R.string.txt_text_placeholder_72), fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.padding(top = 8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "~${goalMl - estimatedMl}ml",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = WaterBlueMedium
                    )
                    Text(stringResource(R.string.txt_from_drinks), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            // Visual bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.2f)
                        .fillMaxHeight()
                        .background(Color(0xFF4CAF50))
                )
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight()
                        .background(WaterBlueMedium)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF4CAF50), CircleShape))
                    Text(stringResource(R.string.txt_food_20), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).background(WaterBlueMedium, CircleShape))
                    Text(stringResource(R.string.txt_drinks_80), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun FoodItemCard(food: WaterRichFood) {
    val percentColor = when {
        food.waterPercent >= 94 -> Color(0xFF1B5E20)
        food.waterPercent >= 90 -> Color(0xFF388E3C)
        food.waterPercent >= 85 -> Color(0xFF66BB6A)
        else -> Color(0xFF8BC34A)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(food.icon, fontSize = 28.sp)

            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    food.servingSize,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${food.waterPercent}%",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = percentColor
                )
                Text(
                    "~${food.waterMl}ml",
                    fontSize = 11.sp,
                    color = WaterBlueMedium
                )
            }

            // Mini water bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Gray.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(food.waterPercent / 100f)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(percentColor.copy(alpha = 0.7f), percentColor)
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun FoodHydrationTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(stringResource(R.string.txt_food_hydration_tips), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            val tips = listOf(
                "🥗" to "Include a salad with your main meals — lettuce & tomatoes are 94-95% water",
                "🍉" to "Snack on watermelon or strawberries for a hydrating treat",
                "🍲" to "Soups and broths count toward your water intake",
                "🥤" to "Smoothies with fruits and vegetables boost hydration",
                "🫖" to "Herbal teas (caffeine-free) count as water intake",
                "⚠️" to "Note: Cooking can reduce the water content of vegetables"
            )

            tips.forEach { (icon, tip) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(icon, fontSize = 16.sp)
                    Text(
                        tip, fontSize = 13.sp, lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ElectrolytesTab(
    isVisible: Boolean,
    onNavigateToFull: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick intro
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF8F00), Color(0xFFFF6F00))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(stringResource(R.string.txt_text_placeholder_33), fontSize = 36.sp)
                            Column {
                                Text(
                                    stringResource(R.string.txt_electrolyte_balance),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    stringResource(R.string.txt_beyond_water_minerals_your_bod),
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick facts
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 40 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            stringResource(R.string.txt_key_electrolytes),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickElectrolyteChip("🧂", "Sodium", Color(0xFFFF7043))
                            QuickElectrolyteChip("🍌", "Potassium", Color(0xFFFFCA28))
                            QuickElectrolyteChip("🥜", "Magnesium", Color(0xFF66BB6A))
                            QuickElectrolyteChip("🦴", "Calcium", Color(0xFF42A5F5))
                        }
                    }
                }
            }
        }

        // When needed quick list
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 40 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            stringResource(R.string.txt_when_you_need_electrolytes),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )

                        val situations = listOf(
                            "🏃" to "60+ minutes of intense exercise",
                            "☀️" to "Heavy sweating in hot weather",
                            "🤒" to "Illness with vomiting/diarrhea",
                            "💧" to "Drinking large amounts of plain water"
                        )

                        situations.forEach { (icon, text) ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(icon, fontSize = 18.sp)
                                Text(text, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // View full guide button
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 40 }
            ) {
                Button(
                    onClick = onNavigateToFull,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8F00)
                    )
                ) {
                    Text(stringResource(R.string.txt_text_placeholder_33), fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.txt_view_full_electrolyte_guide),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Quick recipe preview
        item {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 400)) + slideInVertically(tween(500, 400)) { 40 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToFull() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(stringResource(R.string.txt_text_placeholder_69), fontSize = 32.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.txt_diy_electrolyte_drink),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                stringResource(R.string.txt_simple_recipe_with_water_salt_),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Text(stringResource(R.string.txt_text_placeholder_61), fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickElectrolyteChip(emoji: String, name: String, color: Color) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}
