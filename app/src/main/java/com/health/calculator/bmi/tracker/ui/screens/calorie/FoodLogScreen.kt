package com.health.calculator.bmi.tracker.ui.screens.calorie

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.data.model.DailyFoodLog
import com.health.calculator.bmi.tracker.data.model.FoodEntry
import com.health.calculator.bmi.tracker.data.model.FoodPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(
    onNavigateBack: () -> Unit,
    targetCalories: Double = 2000.0,
    targetProtein: Double = 0.0,
    targetCarbs: Double = 0.0,
    targetFat: Double = 0.0,
    viewModel: FoodLogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Set targets from calorie calculator
    LaunchedEffect(targetCalories) {
        viewModel.setTargetCalories(targetCalories, targetProtein, targetCarbs, targetFat)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Log") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    var showEdu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showEdu = true }) {
                        Icon(Icons.Default.Info, "Learn")
                    }
                    if (showEdu) {
                        ModalBottomSheet(
                            onDismissRequest = { showEdu = false },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                CalorieEducationalContent()
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.showAddFoodDialog()
                },
                icon = { Icon(Icons.Default.Add, "Add food") },
                text = { Text("Add Food") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        val log = uiState.todayLog

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily Summary Ring
            log?.let { DailySummaryCard(it) }

            // Macro Progress Bars
            log?.let { MacroProgressCard(it) }

            // Quick Add Presets
            QuickAddSection(
                defaultPresets = viewModel.defaultPresets,
                customPresets = uiState.customPresets,
                isExpanded = uiState.showQuickAdd,
                onToggle = viewModel::toggleQuickAdd,
                onPresetTap = { preset ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.addFoodFromPreset(preset)
                },
                onAddCustomPreset = viewModel::showAddPresetDialog,
                onRemoveCustomPreset = viewModel::removeCustomPreset
            )

            // Per-Meal Log
            log?.let { foodLog ->
                MealLogSection(
                    log = foodLog,
                    onRemoveEntry = { viewModel.removeEntry(it) }
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // FAB space
        }

        // Add Food Dialog
        if (uiState.showAddFoodDialog) {
            AddFoodDialog(
                uiState = uiState,
                onUpdateName = viewModel::updateFoodName,
                onUpdateCalories = viewModel::updateFoodCalories,
                onUpdateProtein = viewModel::updateFoodProtein,
                onUpdateCarbs = viewModel::updateFoodCarbs,
                onUpdateFat = viewModel::updateFoodFat,
                onUpdateMealSlot = viewModel::updateFoodMealSlot,
                onUpdateServing = viewModel::updateFoodServingSize,
                onToggleMacros = viewModel::toggleMacroFields,
                onSave = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.addFoodFromForm()
                },
                onDismiss = viewModel::hideAddFoodDialog
            )
        }

        // Add Custom Preset Dialog
        if (uiState.showAddPresetDialog) {
            AddCustomPresetDialog(
                uiState = uiState,
                onUpdateName = viewModel::updatePresetName,
                onUpdateCalories = viewModel::updatePresetCalories,
                onUpdateProtein = viewModel::updatePresetProtein,
                onUpdateCarbs = viewModel::updatePresetCarbs,
                onUpdateFat = viewModel::updatePresetFat,
                onUpdateServing = viewModel::updatePresetServing,
                onUpdateEmoji = viewModel::updatePresetEmoji,
                onSave = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.saveCustomPreset()
                },
                onDismiss = viewModel::hideAddPresetDialog
            )
        }
    }
}

@Composable
private fun DailySummaryCard(log: DailyFoodLog) {
    val remaining = log.remainingCalories
    val progress = log.calorieProgress
    val isOver = remaining < 0
    val isNear = remaining in 0.0..200.0

    val (ringColor, statusText, statusColor) = when {
        progress >= 1.2f -> Triple(Color(0xFFF44336), "⚠️ ${(-remaining).toInt()} cal over target — it's okay, tomorrow is a new day!", Color(0xFFF44336))
        progress >= 1.0f -> Triple(Color(0xFFFF9800), "🍊 ${(-remaining).toInt()} cal over target. Watch your next meal.", Color(0xFFFF9800))
        isNear -> Triple(Color(0xFFFFC107), "⚡ Almost there! ${remaining.toInt()} cal remaining.", Color(0xFFFFC107))
        else -> Triple(Color(0xFF4CAF50), "✅ ${remaining.toInt()} calories remaining", Color(0xFF4CAF50))
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "calRing"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Today's Calories",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Calorie Ring
            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 14.dp.toPx()
                    drawArc(
                        color = ringColor.copy(alpha = 0.15f),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ringColor,
                        startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${"%.0f".format(log.totalCalories)}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        ),
                        color = ringColor
                    )
                    Text(
                        "/ ${"%.0f".format(log.targetCalories)} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip("Consumed", "${"%.0f".format(log.totalCalories)}", Color(0xFF2196F3))
                StatChip("Target", "${"%.0f".format(log.targetCalories)}", Color(0xFF4CAF50))
                StatChip(
                    if (isOver) "Over" else "Left",
                    "${"%.0f".format(kotlin.math.abs(remaining))}",
                    if (isOver) Color(0xFFF44336) else Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun MacroProgressCard(log: DailyFoodLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Macronutrients",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            MacroProgressBar(
                label = "Protein",
                consumed = log.totalProtein,
                target = log.targetProteinGrams,
                progress = log.proteinProgress,
                color = Color(0xFFF44336),
                unit = "g"
            )
            Spacer(modifier = Modifier.height(10.dp))
            MacroProgressBar(
                label = "Carbs",
                consumed = log.totalCarbs,
                target = log.targetCarbGrams,
                progress = log.carbProgress,
                color = Color(0xFFFFEB3B),
                unit = "g"
            )
            Spacer(modifier = Modifier.height(10.dp))
            MacroProgressBar(
                label = "Fat",
                consumed = log.totalFat,
                target = log.targetFatGrams,
                progress = log.fatProgress,
                color = Color(0xFF4CAF50),
                unit = "g"
            )
        }
    }
}

@Composable
private fun MacroProgressBar(
    label: String,
    consumed: Double,
    target: Double,
    progress: Float,
    color: Color,
    unit: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "macroBar"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            }
            if (target > 0) {
                Text(
                    "${"%.0f".format(consumed)} / ${"%.0f".format(target)} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            } else {
                Text(
                    "${"%.0f".format(consumed)} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (target > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().height(8.dp)
            ) {}
        }
    }
}

@Composable
private fun QuickAddSection(
    defaultPresets: List<FoodPreset>,
    customPresets: List<FoodPreset>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onPresetTap: (FoodPreset) -> Unit,
    onAddCustomPreset: () -> Unit,
    onRemoveCustomPreset: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFFFFEB3B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Quick Add",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Row {
                    TextButton(onClick = onAddCustomPreset) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp)
                    }
                    IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            "Toggle",
                            Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Default presets
                    Text(
                        "Common Foods",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(defaultPresets) { preset ->
                            PresetChip(preset = preset, onTap = onPresetTap, onRemove = null)
                        }
                    }

                    // Custom presets
                    if (customPresets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "My Foods",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(customPresets) { preset ->
                                PresetChip(
                                    preset = preset,
                                    onTap = onPresetTap,
                                    onRemove = { onRemoveCustomPreset(preset.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Always show first row of presets when collapsed
            if (!isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(defaultPresets.take(5)) { preset ->
                        PresetChip(preset = preset, onTap = onPresetTap, onRemove = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    preset: FoodPreset,
    onTap: (FoodPreset) -> Unit,
    onRemove: (() -> Unit)?
) {
    Surface(
        modifier = Modifier.clickable { onTap(preset) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(preset.emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                preset.name.split(" ").first(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 64.dp)
            )
            Text(
                "${"%.0f".format(preset.calories)} cal",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp
            )
            onRemove?.let {
                Icon(
                    Icons.Default.Close, "Remove",
                    modifier = Modifier.size(10.dp).clickable { it() },
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun MealLogSection(
    log: DailyFoodLog,
    onRemoveEntry: (Long) -> Unit
) {
    val mealOrder = listOf("Breakfast", "Lunch", "Snack", "Dinner", "Other")
    val mealEmojis = mapOf(
        "Breakfast" to "🌅", "Lunch" to "☀️",
        "Snack" to "🍎", "Dinner" to "🌙", "Other" to "🍽️"
    )

    if (log.entries.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🍽️", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No food logged yet",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "Tap + to add food or use Quick Add",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        mealOrder.forEach { mealName ->
            val entries = log.entriesByMeal[mealName] ?: return@forEach
            val mealCalories = entries.sumOf { it.calories }
            val emoji = mealEmojis[mealName] ?: "🍽️"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(emoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                mealName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            "${"%.0f".format(mealCalories)} kcal",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    entries.forEach { entry ->
                        FoodEntryRow(entry = entry, onRemove = { onRemoveEntry(entry.id) })
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodEntryRow(entry: FoodEntry, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.name, style = MaterialTheme.typography.bodyMedium)
            if (entry.servingSize.isNotBlank()) {
                Text(
                    entry.servingSize,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }
            if (entry.proteinGrams > 0 || entry.carbGrams > 0 || entry.fatGrams > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (entry.proteinGrams > 0) MacroMiniChip("P", "${"%.0f".format(entry.proteinGrams)}g", Color(0xFFF44336))
                    if (entry.carbGrams > 0) MacroMiniChip("C", "${"%.0f".format(entry.carbGrams)}g", Color(0xFFFFEB3B))
                    if (entry.fatGrams > 0) MacroMiniChip("F", "${"%.0f".format(entry.fatGrams)}g", Color(0xFF4CAF50))
                }
            }
        }
        Text(
            "${"%.0f".format(entry.calories)} kcal",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Delete, "Remove",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun AddFoodDialog(
    uiState: FoodLogUiState,
    onUpdateName: (String) -> Unit,
    onUpdateCalories: (String) -> Unit,
    onUpdateProtein: (String) -> Unit,
    onUpdateCarbs: (String) -> Unit,
    onUpdateFat: (String) -> Unit,
    onUpdateMealSlot: (String) -> Unit,
    onUpdateServing: (String) -> Unit,
    onToggleMacros: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val mealSlots = listOf("Breakfast", "Lunch", "Snack", "Dinner", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.newFoodName,
                    onValueChange = onUpdateName,
                    label = { Text("Food name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.newFoodCalories,
                    onValueChange = onUpdateCalories,
                    label = { Text("Calories *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    suffix = { Text("kcal") }
                )
                OutlinedTextField(
                    value = uiState.newFoodServingSize,
                    onValueChange = onUpdateServing,
                    label = { Text("Serving size (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Meal slot
                Text("Meal", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(mealSlots) { slot ->
                        FilterChip(
                            selected = uiState.newFoodMealSlot == slot,
                            onClick = { onUpdateMealSlot(slot) },
                            label = { Text(slot, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Macro toggle
                TextButton(onClick = onToggleMacros) {
                    Icon(
                        if (uiState.showMacroFields) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null, Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (uiState.showMacroFields) "Hide macros" else "+ Add macros (optional)",
                        fontSize = 12.sp
                    )
                }

                AnimatedVisibility(visible = uiState.showMacroFields) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = uiState.newFoodProtein,
                                onValueChange = onUpdateProtein,
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.newFoodCarbs,
                                onValueChange = onUpdateCarbs,
                                label = { Text("Carbs (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = uiState.newFoodFat,
                            onValueChange = onUpdateFat,
                            label = { Text("Fat (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                uiState.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, shape = RoundedCornerShape(10.dp)) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AddCustomPresetDialog(
    uiState: FoodLogUiState,
    onUpdateName: (String) -> Unit,
    onUpdateCalories: (String) -> Unit,
    onUpdateProtein: (String) -> Unit,
    onUpdateCarbs: (String) -> Unit,
    onUpdateFat: (String) -> Unit,
    onUpdateServing: (String) -> Unit,
    onUpdateEmoji: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val emojiOptions = listOf("🍽️", "🍗", "🥚", "🍎", "🥦", "🍞", "🥛", "🐟", "🥑", "🥜", "🍚", "🥗", "🍌", "☕", "🧃")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Food") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Emoji selector
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(emojiOptions) { emoji ->
                        Surface(
                            modifier = Modifier.clickable { onUpdateEmoji(emoji) }.size(36.dp),
                            shape = CircleShape,
                            color = if (uiState.presetEmoji == emoji)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.presetName,
                    onValueChange = onUpdateName,
                    label = { Text("Food name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = uiState.presetCalories,
                        onValueChange = onUpdateCalories,
                        label = { Text("Calories *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.presetServing,
                        onValueChange = onUpdateServing,
                        label = { Text("Serving") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = uiState.presetProtein,
                        onValueChange = onUpdateProtein,
                        label = { Text("Protein") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        suffix = { Text("g", fontSize = 10.sp) }
                    )
                    OutlinedTextField(
                        value = uiState.presetCarbs,
                        onValueChange = onUpdateCarbs,
                        label = { Text("Carbs") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        suffix = { Text("g", fontSize = 10.sp) }
                    )
                    OutlinedTextField(
                        value = uiState.presetFat,
                        onValueChange = onUpdateFat,
                        label = { Text("Fat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        suffix = { Text("g", fontSize = 10.sp) }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, shape = RoundedCornerShape(10.dp)) { Text("Save Preset") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun MacroMiniChip(label: String, value: String, color: Color) {
    Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.1f)) {
        Row(modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(2.dp))
            Text(value, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f), fontSize = 8.sp)
        }
    }
}
