package com.health.calculator.bmi.tracker.ui.screens.whr

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import com.health.calculator.bmi.tracker.ui.screens.whr.WhrHistoryEntryCard
import com.health.calculator.bmi.tracker.ui.screens.whr.WhrHistoryEntryDetailDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhrProgressScreen(
    viewModel: WhrProgressViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { viewModel.refreshData() }

    // Goal Dialog
    if (state.showGoalDialog) {
        GoalSettingDialog(
            currentInput = state.goalInput,
            error = state.goalError,
            existingGoal = state.goal,
            onInputChange = { viewModel.updateGoalInput(it) },
            onSave = { viewModel.saveGoal() },
            onClear = { viewModel.clearGoal() },
            onDismiss = { viewModel.dismissGoalDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WHR Progress", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.showGoalDialog()
                    }) {
                        Icon(Icons.Outlined.Flag, contentDescription = "Set Goal")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.entries.isEmpty()) {
            // Empty State
            EmptyProgressState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Motivational Message
                val message = viewModel.getMotivationalMessage()
                if (message.isNotEmpty()) {
                    MotivationalBanner(message = message)
                }

                // Comparison with previous reading
                state.comparison?.let { comparison ->
                    ComparisonCard(comparison = comparison)
                }

                // Goal Progress
                state.goal?.let { goal ->
                    GoalProgressCard(
                        goal = goal,
                        currentWaist = state.stats?.currentWaist ?: 0f,
                        firstWaist = state.stats?.firstWaist ?: 0f,
                        onEditGoal = { viewModel.showGoalDialog() }
                    )
                }

                // Time Range Filter
                TimeRangeSelector(
                    selected = state.selectedTimeRange,
                    onSelect = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.selectTimeRange(it)
                    }
                )

                // Graph Line Selector
                GraphLineSelector(
                    selectedLine = state.selectedGraphLine,
                    showAllLines = state.showAllLines,
                    onSelectLine = { viewModel.selectGraphLine(it) },
                    onToggleAll = { viewModel.toggleAllLines() }
                )

                // Trend Graph
                TrendGraphCard(
                    entries = state.filteredEntries,
                    selectedLine = state.selectedGraphLine,
                    showAllLines = state.showAllLines,
                    gender = state.entries.lastOrNull()?.gender ?: Gender.MALE
                )

                // Progress Stats
                state.stats?.let { stats ->
                    ProgressStatsCard(stats = stats)
                }

                // Category Distribution
                if (state.filteredEntries.isNotEmpty()) {
                    CategoryDistributionCard(entries = state.filteredEntries)
                }

                // Recent Measurements List
                var selectedEntry by remember { mutableStateOf<WhrHistoryEntry?>(null) }
                
                if (selectedEntry != null) {
                    WhrHistoryEntryDetailDialog(
                        entry = selectedEntry!!,
                        onDismiss = { selectedEntry = null }
                    )
                }

                if (state.filteredEntries.isNotEmpty()) {
                    Text(
                        "Recent Measurements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    
                    state.filteredEntries.sortedByDescending { it.timestamp }.take(10).forEach { entry ->
                        WhrHistoryEntryCard(
                            entry = entry,
                            onTap = { selectedEntry = entry },
                            onDelete = { viewModel.deleteEntry(entry.id) },
                            showDeleteOption = true,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EmptyProgressState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📊", fontSize = 48.sp)
                }
            }
            Text(
                "No Measurements Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Start tracking your waist-to-hip ratio to see trends and progress over time.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun MotivationalBanner(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ComparisonCard(comparison: WhrComparison) {
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Since Last Measurement",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    dateFormat.format(Date(comparison.previousDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ComparisonItem(
                    label = "WHR",
                    diff = comparison.whrDiff,
                    format = "%.2f",
                    direction = comparison.whrDirection,
                    suffix = ""
                )
                ComparisonItem(
                    label = "Waist",
                    diff = comparison.waistDiff,
                    format = "%.1f",
                    direction = comparison.waistDirection,
                    suffix = " cm"
                )
                ComparisonItem(
                    label = "Hip",
                    diff = comparison.hipDiff,
                    format = "%.1f",
                    direction = comparison.hipDirection,
                    suffix = " cm"
                )
            }
        }
    }
}

@Composable
private fun ComparisonItem(
    label: String,
    diff: Float,
    format: String,
    direction: WhrTrendDirection,
    suffix: String
) {
    val color = when (direction) {
        WhrTrendDirection.IMPROVING -> Color(0xFF4CAF50)
        WhrTrendDirection.WORSENING -> Color(0xFFF44336)
        WhrTrendDirection.STEADY -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    val icon = when (direction) {
        WhrTrendDirection.IMPROVING -> Icons.Filled.TrendingDown
        WhrTrendDirection.WORSENING -> Icons.Filled.TrendingUp
        WhrTrendDirection.STEADY -> Icons.Filled.TrendingFlat
    }

    val prefix = when {
        diff > 0 -> "+"
        else -> ""
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                "$prefix${String.format(format, diff)}$suffix",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun GoalProgressCard(
    goal: WhrGoal,
    currentWaist: Float,
    firstWaist: Float,
    onEditGoal: () -> Unit
) {
    val remaining = currentWaist - goal.targetWaistCm
    val totalNeeded = firstWaist - goal.targetWaistCm
    val progress = if (totalNeeded > 0) {
        ((firstWaist - currentWaist) / totalNeeded).coerceIn(0f, 1f)
    } else 1f

    val isGoalReached = remaining <= 0f
    val goalColor = if (isGoalReached) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(1000, easing = EaseOutCubic)
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = goalColor.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isGoalReached) Icons.Filled.EmojiEvents else Icons.Outlined.Flag,
                        contentDescription = null,
                        tint = goalColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        if (isGoalReached) "Goal Reached! 🎉" else "Waist Goal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = onEditGoal,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit Goal",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Target display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Current",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "${String.format("%.1f", currentWaist)} cm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (isGoalReached) "Achieved!" else "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        if (isGoalReached) "✓"
                        else "${String.format("%.1f", remaining)} cm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalReached) goalColor
                        else MaterialTheme.colorScheme.error
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Target",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "${String.format("%.1f", goal.targetWaistCm)} cm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = goalColor
                    )
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = goalColor,
                    trackColor = goalColor.copy(alpha = 0.15f)
                )
                Text(
                    "${(progress * 100).toInt()}% complete",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selected: WhrTimeRange,
    onSelect: (WhrTimeRange) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(WhrTimeRange.entries.toList()) { range -> // toList() to avoid entries conflict
            FilterChip(
                selected = selected == range,
                onClick = { onSelect(range) },
                label = {
                    Text(
                        range.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected == range) FontWeight.Bold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun GraphLineSelector(
    selectedLine: WhrGraphLine,
    showAllLines: Boolean,
    onSelectLine: (WhrGraphLine) -> Unit,
    onToggleAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WhrGraphLine.entries.forEach { line ->
                val lineColor = when (line) {
                    WhrGraphLine.WHR -> MaterialTheme.colorScheme.primary
                    WhrGraphLine.WAIST -> Color(0xFFF44336)
                    WhrGraphLine.HIP -> Color(0xFF2196F3)
                }
                FilterChip(
                    selected = selectedLine == line && !showAllLines,
                    onClick = { onSelectLine(line) },
                    label = {
                        Text(line.label, fontSize = 12.sp)
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(lineColor)
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        TextButton(onClick = onToggleAll) {
            Text(
                if (showAllLines) "Single" else "All",
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TrendGraphCard(
    entries: List<WhrHistoryEntry>,
    selectedLine: WhrGraphLine,
    showAllLines: Boolean,
    gender: Gender
) {
    if (entries.size < 2) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Need at least 2 measurements to show trends",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val waistColor = Color(0xFFF44336)
    val hipColor = Color(0xFF2196F3)
    val greenZone = Color(0xFF4CAF50)
    val yellowZone = Color(0xFFFFA726)
    val redZone = Color(0xFFF44336)
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    val dateFormat = SimpleDateFormat("M/d", Locale.getDefault())

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(entries.size, selectedLine, showAllLines) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(800, easing = EaseOutCubic))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                val graphLeft = 40.dp.toPx()
                val graphRight = size.width - 16.dp.toPx()
                val graphTop = 16.dp.toPx()
                val graphBottom = size.height - 30.dp.toPx()
                val graphWidth = graphRight - graphLeft
                val graphHeight = graphBottom - graphTop

                // Determine value range based on selected line
                val minVal: Float
                val maxVal: Float
                if (showAllLines) {
                    val allVals = entries.flatMap {
                        listOf(it.whr, it.waistCm / 100f, it.hipCm / 100f)
                    }
                    minVal = allVals.minOrNull()?.times(0.9f) ?: 0f
                    maxVal = allVals.maxOrNull()?.times(1.1f) ?: 1f
                } else if (selectedLine == WhrGraphLine.WHR) {
                    val vals = entries.map { it.whr }
                    minVal = (vals.minOrNull() ?: 0f - 0.1f).coerceAtLeast(0.5f)
                    maxVal = (vals.maxOrNull() ?: 1f + 0.1f).coerceAtMost(1.5f)
                } else if (selectedLine == WhrGraphLine.WAIST) {
                    val vals = entries.map { it.waistCm }
                    minVal = (vals.minOrNull() ?: 40f) - 5f
                    maxVal = (vals.maxOrNull() ?: 200f) + 5f
                } else {
                    val vals = entries.map { it.hipCm }
                    minVal = (vals.minOrNull() ?: 40f) - 5f
                    maxVal = (vals.maxOrNull() ?: 200f) + 5f
                }
                
                val valRange = (maxVal - minVal).coerceAtLeast(0.01f)

                fun valueToY(v: Float): Float {
                    return graphBottom - ((v - minVal) / valRange) * graphHeight
                }

                // Background zones for WHR
                if (selectedLine == WhrGraphLine.WHR && !showAllLines) {
                    val lowThreshold = if (gender == Gender.FEMALE) 0.80f else 0.90f
                    val highThreshold = if (gender == Gender.FEMALE) 0.85f else 1.00f

                    if (lowThreshold in minVal..maxVal) {
                        val y = valueToY(lowThreshold)
                        drawRect(
                            color = greenZone.copy(alpha = 0.06f),
                            topLeft = Offset(graphLeft, y),
                            size = androidx.compose.ui.geometry.Size(
                                graphWidth, graphBottom - y
                            )
                        )
                    }
                    if (highThreshold in minVal..maxVal) {
                        val yLow = valueToY(highThreshold)
                        val yHigh = valueToY(lowThreshold).coerceIn(graphTop, graphBottom)
                        drawRect(
                            color = yellowZone.copy(alpha = 0.06f),
                            topLeft = Offset(graphLeft, yLow),
                            size = androidx.compose.ui.geometry.Size(
                                graphWidth, (yHigh - yLow).coerceAtLeast(0f)
                            )
                        )
                        drawRect(
                            color = redZone.copy(alpha = 0.06f),
                            topLeft = Offset(graphLeft, graphTop),
                            size = androidx.compose.ui.geometry.Size(
                                graphWidth, (yLow - graphTop).coerceAtLeast(0f)
                            )
                        )
                    }

                    // Threshold lines
                    if (lowThreshold in minVal..maxVal) {
                        val y = valueToY(lowThreshold)
                        drawLine(
                            color = yellowZone.copy(alpha = 0.4f),
                            start = Offset(graphLeft, y),
                            end = Offset(graphRight, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(8f, 6f)
                            )
                        )
                    }
                    if (highThreshold in minVal..maxVal) {
                        val y = valueToY(highThreshold)
                        drawLine(
                            color = redZone.copy(alpha = 0.4f),
                            start = Offset(graphLeft, y),
                            end = Offset(graphRight, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(8f, 6f)
                            )
                        )
                    }
                }

                // Grid lines
                val gridCount = 4
                for (i in 0..gridCount) {
                    val y = graphTop + (graphHeight / gridCount) * i
                    drawLine(
                        color = surfaceVariant.copy(alpha = 0.5f),
                        start = Offset(graphLeft, y),
                        end = Offset(graphRight, y),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }

                // Draw lines function
                fun drawDataLine(
                    values: List<Float>,
                    color: Color,
                    strokeW: Float = 2.5.dp.toPx()
                ) {
                    if (values.size < 2) return
                    val step = graphWidth / (values.size - 1).coerceAtLeast(1)

                    val path = Path()
                    val animatedCount = (values.size * animProgress.value).toInt()
                        .coerceAtLeast(2)
                        .coerceAtMost(values.size)

                    for (i in 0 until animatedCount) {
                        val x = graphLeft + step * i
                        val y = valueToY(values[i])
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Data points
                    for (i in 0 until animatedCount) {
                        val x = graphLeft + step * i
                        val y = valueToY(values[i])
                        drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x, y))
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                // Draw lines based on selection
                if (showAllLines) {
                    drawDataLine(entries.map { it.whr }, primaryColor)
                    drawDataLine(entries.map { it.waistCm / 100f }, waistColor, 2.dp.toPx())
                    drawDataLine(entries.map { it.hipCm / 100f }, hipColor, 2.dp.toPx())
                } else {
                    val values = when (selectedLine) {
                        WhrGraphLine.WHR -> entries.map { it.whr }
                        WhrGraphLine.WAIST -> entries.map { it.waistCm }
                        WhrGraphLine.HIP -> entries.map { it.hipCm }
                    }
                    val lineColor = when (selectedLine) {
                        WhrGraphLine.WHR -> primaryColor
                        WhrGraphLine.WAIST -> waistColor
                        WhrGraphLine.HIP -> hipColor
                    }
                    drawDataLine(values, lineColor)
                }
            }

            // X-axis date labels
            if (entries.size >= 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        dateFormat.format(Date(entries.first().timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontSize = 10.sp
                    )
                    if (entries.size > 2) {
                        val midEntry = entries[entries.size / 2]
                        Text(
                            dateFormat.format(Date(midEntry.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        dateFormat.format(Date(entries.last().timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressStatsCard(stats: WhrProgressStats) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Progress Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // WHR Stats
            Text(
                "WHR",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Current", String.format("%.2f", stats.currentWhr))
                StatItem("First", String.format("%.2f", stats.firstWhr))
                StatItem("Best", String.format("%.2f", stats.bestWhr))
                StatItem("Avg", String.format("%.2f", stats.averageWhr))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Waist Stats
            Text(
                "Waist",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF44336)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Current", "${String.format("%.1f", stats.currentWaist)} cm")
                StatItem("First", "${String.format("%.1f", stats.firstWaist)} cm")
                StatItem(
                    "Change",
                    "${if (stats.waistChange > 0) "+" else ""}${
                        String.format("%.1f", stats.waistChange)
                    } cm",
                    valueColor = when (stats.waistTrend) {
                        WhrTrendDirection.IMPROVING -> Color(0xFF4CAF50)
                        WhrTrendDirection.WORSENING -> Color(0xFFF44336)
                        WhrTrendDirection.STEADY -> null
                    }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Hip Stats
            Text(
                "Hip",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2196F3)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Current", "${String.format("%.1f", stats.currentHip)} cm")
                StatItem("First", "${String.format("%.1f", stats.firstHip)} cm")
                StatItem(
                    "Change",
                    "${if (stats.hipChange > 0) "+" else ""}${
                        String.format("%.1f", stats.hipChange)
                    } cm"
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                StatItem(
                    "Total Measurements",
                    "${stats.totalMeasurements}",
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CategoryDistributionCard(entries: List<WhrHistoryEntry>) {
    val lowCount = entries.count { it.category == WhrCategory.LOW_RISK }
    val modCount = entries.count { it.category == WhrCategory.MODERATE_RISK }
    val highCount = entries.count { it.category == WhrCategory.HIGH_RISK }
    val total = entries.size.toFloat()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Category Distribution",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            DistributionBar(
                label = "Low Risk",
                count = lowCount,
                total = total,
                color = Color(0xFF4CAF50)
            )
            DistributionBar(
                label = "Moderate",
                count = modCount,
                total = total,
                color = Color(0xFFFFA726)
            )
            DistributionBar(
                label = "High Risk",
                count = highCount,
                total = total,
                color = Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun DistributionBar(
    label: String,
    count: Int,
    total: Float,
    color: Color
) {
    val fraction = if (total > 0) count / total else 0f
    val animFraction = remember { Animatable(0f) }
    LaunchedEffect(fraction) {
        animFraction.animateTo(fraction, tween(600, easing = EaseOutCubic))
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                "$count (${(fraction * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        LinearProgressIndicator(
            progress = { animFraction.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.12f)
        )
    }
}

@Composable
private fun GoalSettingDialog(
    currentInput: String,
    error: String?,
    existingGoal: WhrGoal?,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingGoal != null) "Update Waist Goal" else "Set Waist Goal",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Set a target waist circumference to track your progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                OutlinedTextField(
                    value = currentInput,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onInputChange(input)
                        }
                    },
                    label = { Text("Target Waist") },
                    suffix = { Text("cm") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Flag, contentDescription = null)
                    },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💡", fontSize = 14.sp)
                        Text(
                            "WHO recommends waist circumference below ${
                                "94 cm for men and 80 cm for women"
                            } for lower health risk.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Goal")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (existingGoal != null) {
                    TextButton(onClick = onClear) {
                        Text("Remove Goal", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
