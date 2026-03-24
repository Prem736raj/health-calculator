// ui/screens/waterintake/WaterTrackingScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import com.health.calculator.bmi.tracker.data.model.HydrationScore
import com.health.calculator.bmi.tracker.data.model.WaterStreakData
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import com.health.calculator.bmi.tracker.util.WaterShareHelper
import com.health.calculator.bmi.tracker.ui.screens.waterintake.components.WaterGoalCelebration

import com.health.calculator.bmi.tracker.ui.screens.waterintake.components.HydrationPlantCard
import com.health.calculator.bmi.tracker.ui.screens.waterintake.components.PlantDetailDialog

// Water-themed colors
private val WaterBlueLight = Color(0xFF64B5F6)
private val WaterBlueMedium = Color(0xFF2196F3)
private val WaterBlueDark = Color(0xFF1565C0)
private val WaterBluePale = Color(0xFFBBDEFB)
private val WaterBlueSurface = Color(0xFFE3F2FD)
private val WaterCyan = Color(0xFF00BCD4)
private val GoalGreen = Color(0xFF4CAF50)
private val GoalGold = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackingScreen(
    viewModel: WaterTrackingViewModel,
    gamificationViewModel: WaterGamificationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToGamification: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToEducation: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val todayLogs by viewModel.todayLogs.collectAsState(initial = emptyList())
    val todayTotal by viewModel.todayTotal.collectAsState(initial = 0)
    val goalMl = viewModel.dailyGoalMl
    
    val streakData by gamificationViewModel.streakData.collectAsState(initial = null)
    val todayScore by gamificationViewModel.todayScore.collectAsState()

    val percentage = if (goalMl > 0) {
        ((todayTotal.toFloat() / goalMl) * 100).coerceAtMost(200f)
    } else 0f

    val goalReached = todayTotal >= goalMl

    // Celebration state
    var showCelebration by remember { mutableStateOf(false) }
    var previouslyReached by remember { mutableStateOf(goalReached) }
    var showCustomDialog by remember { mutableStateOf(false) }

    LaunchedEffect(goalReached) {
        if (goalReached && !previouslyReached) {
            showCelebration = true
            delay(4000)
            showCelebration = false
        }
        previouslyReached = goalReached
    }

    // Entrance animation
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
                        Text("💧", fontSize = 22.sp)
                        Text("Water Tracker", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToTools()
                    }) {
                        Icon(
                            Icons.Default.Science,
                            "Hydration Tools",
                            tint = WaterBlueMedium
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToHistory()
                    }) {
                        Icon(
                            Icons.Default.BarChart,
                            "History & Trends",
                            tint = WaterBlueMedium
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        WaterShareHelper.shareWaterAchievement(
                            context = context,
                            currentMl = todayTotal,
                            goalMl = viewModel.dailyGoalMl,
                            streakDays = streakData?.currentStreak ?: 0,
                            percentage = percentage
                        )
                    }) {
                        Icon(
                            Icons.Default.Share,
                            "Share progress",
                            tint = WaterBlueMedium
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToReminders()
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            "Reminder Settings",
                            tint = WaterBlueMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
            ) {
                // Plant companion
                if (viewModel.isPlantVisible) {
                    item {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -30 }
                        ) {
                            val todayTotalVal = todayTotal
                            val justWatered by viewModel.justWatered.collectAsState()
                            
                            // Initialize date key and update plant tracking
                            val currentDateKey = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
                            LaunchedEffect(currentDateKey) {
                                viewModel.updatePlantTracking(currentDateKey)
                            }
                            
                            val plantState = remember(todayTotalVal, justWatered, streakData?.currentStreak) {
                                viewModel.getPlantState(
                                    currentMl = todayTotalVal,
                                    goalMl = viewModel.dailyGoalMl,
                                    streakDays = streakData?.currentStreak ?: 0
                                ).copy(justWatered = justWatered)
                            }

                            var showPlantDetail by remember { mutableStateOf(false) }

                            HydrationPlantCard(
                                plantState = plantState,
                                onTap = { showPlantDetail = true }
                            )

                            if (showPlantDetail) {
                                PlantDetailDialog(
                                    plantState = plantState,
                                    plantName = viewModel.plantName,
                                    onDismiss = { showPlantDetail = false },
                                    onNameChange = { viewModel.setPlantName(it) },
                                    onHide = {
                                        viewModel.setPlantVisible(false)
                                        showPlantDetail = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Progress Ring
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.8f)
                    ) {
                        ProgressRingCard(
                            currentMl = todayTotal,
                            goalMl = goalMl,
                            percentage = percentage,
                            goalReached = goalReached
                        )
                    }
                }

                // Hydration Tools Access
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 50 }
                    ) {
                        ToolsQuickAccessCard(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToTools()
                            }
                        )
                    }
                }

                // Education Navigation Access
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, 400)) + slideInVertically(tween(500, 400)) { 40 }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNavigateToEducation()
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD)
                            ),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("📚", fontSize = 28.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Hydration Guide",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "Learn about hydration science, exercise needs & more",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Text("→", fontSize = 18.sp, color = WaterBlueMedium)
                            }
                        }
                    }
                }

                // Streak & Score Mini Card
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, 150)) + slideInVertically(tween(500, 150)) { 40 }
                    ) {
                        StreakAndScoreMiniCard(
                            currentStreak = streakData?.currentStreak ?: 0,
                            todayScore = todayScore,
                            onViewAchievements = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToGamification()
                            }
                        )
                    }
                }

                // Quick-add buttons
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 40 }
                    ) {
                        QuickAddSection(
                            onAdd = { ml ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.addWater(ml)
                            },
                            onCustom = { showCustomDialog = true },
                            onUndo = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.undoLastEntry()
                            },
                            canUndo = todayLogs.isNotEmpty()
                        )
                    }
                }

                // Glass indicators
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 40 }
                    ) {
                        GlassIndicatorsCard(
                            currentMl = todayTotal,
                            goalMl = goalMl
                        )
                    }
                }

                // Today's log header
                item {
                    AnimatedVisibility(
                        visible = isVisible && todayLogs.isNotEmpty(),
                        enter = fadeIn(tween(500, 400)) + slideInVertically(tween(500, 400)) { 40 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📋", fontSize = 18.sp)
                                Text(
                                    "Today's Log",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = "${todayLogs.size} entries",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Today's log entries
                items(
                    items = todayLogs,
                    key = { it.id }
                ) { log ->
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -40 }
                    ) {
                        WaterLogEntry(
                            log = log,
                            runningTotal = calculateRunningTotal(todayLogs, log),
                            onDelete = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteEntry(log)
                            }
                        )
                    }
                }

                // Empty state
                if (todayLogs.isEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(tween(500, 400))
                        ) {
                            EmptyLogState()
                        }
                    }
                }
            }

            // Celebration overlay
            AnimatedVisibility(
                visible = showCelebration,
                enter = fadeIn(tween(300)) + scaleIn(tween(500)),
                exit = fadeOut(tween(500)),
                modifier = Modifier.fillMaxSize()
            ) {
                WaterGoalCelebration(
                    currentMl = todayTotal,
                    goalMl = viewModel.dailyGoalMl,
                    streakDays = streakData?.currentStreak ?: 0,
                    onDismiss = { showCelebration = false }
                )
            }

            // Custom amount dialog
            if (showCustomDialog) {
                CustomAmountDialog(
                    onDismiss = { showCustomDialog = false },
                    onConfirm = { ml ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.addWater(ml)
                        showCustomDialog = false
                    }
                )
            }
        }
    }
}

// ─── Progress Ring ───────────────────────────────────────────────────────────

@Composable
private fun ProgressRingCard(
    currentMl: Int,
    goalMl: Int,
    percentage: Float,
    goalReached: Boolean
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "ring_progress"
    )

    val animatedSweep by animateFloatAsState(
        targetValue = (percentage / 100f * 360f).coerceAtMost(360f),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "ring_sweep"
    )

    // Pulse when goal reached
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (goalReached) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val ringColor = when {
        goalReached -> GoalGreen
        percentage >= 75 -> WaterBlueMedium
        percentage >= 50 -> WaterBlueLight
        percentage >= 25 -> WaterBluePale
        else -> WaterBluePale
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date header
            Text(
                text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )

            // Progress Ring
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 20f
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Background ring
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.1f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                ringColor.copy(alpha = 0.5f),
                                ringColor,
                                ringColor.copy(alpha = 0.8f)
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        topLeft = Offset(
                            center.x - radius,
                            center.y - radius
                        ),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Glow effect at the end of the arc
                    if (animatedSweep > 5f) {
                        val endAngle = Math.toRadians((-90f + animatedSweep).toDouble())
                        val dotX = center.x + radius * cos(endAngle).toFloat()
                        val dotY = center.y + radius * sin(endAngle).toFloat()
                        drawCircle(
                            color = ringColor.copy(alpha = 0.4f),
                            radius = strokeWidth * 0.8f,
                            center = Offset(dotX, dotY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = strokeWidth * 0.3f,
                            center = Offset(dotX, dotY)
                        )
                    }
                }

                // Center content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (goalReached) {
                        Text("🎉", fontSize = 28.sp)
                        Spacer(Modifier.height(4.dp))
                    }

                    Text(
                        text = "${animatedPercentage.toInt()}%",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (goalReached) GoalGreen else WaterBlueDark
                    )

                    val currentL = currentMl / 1000f
                    val goalL = goalMl / 1000f
                    Text(
                        text = "${String.format("%.1f", currentL)}L / ${String.format("%.1f", goalL)}L",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    if (goalReached) {
                        Text(
                            text = "Goal Reached!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoalGreen
                        )
                    } else {
                        val remainingMl = goalMl - currentMl
                        Text(
                            text = "${remainingMl}ml remaining",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

// ─── Quick-Add Buttons ───────────────────────────────────────────────────────

@Composable
private fun QuickAddSection(
    onAdd: (Int) -> Unit,
    onCustom: () -> Unit,
    onUndo: () -> Unit,
    canUndo: Boolean
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("➕", fontSize = 18.sp)
                    Text(
                        "Quick Add",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                // Undo button
                AnimatedVisibility(
                    visible = canUndo,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    TextButton(
                        onClick = onUndo,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Undo,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Undo", fontSize = 12.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddButton(
                    amount = 100,
                    icon = "💧",
                    label = "Sip",
                    onClick = { onAdd(100) },
                    modifier = Modifier.weight(1f)
                )
                QuickAddButton(
                    amount = 250,
                    icon = "🥛",
                    label = "Glass",
                    onClick = { onAdd(250) },
                    modifier = Modifier.weight(1f)
                )
                QuickAddButton(
                    amount = 500,
                    icon = "🍶",
                    label = "Bottle",
                    onClick = { onAdd(500) },
                    modifier = Modifier.weight(1f)
                )
                QuickAddButton(
                    amount = null,
                    icon = "✏️",
                    label = "Custom",
                    onClick = onCustom,
                    modifier = Modifier.weight(1f),
                    isCustom = true
                )
            }
        }
    }
}

@Composable
private fun QuickAddButton(
    amount: Int?,
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCustom: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )

    val splashAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(200),
        label = "splash"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }

    Card(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCustom)
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            else
                WaterBlueSurface
        ),
        elevation = CardDefaults.cardElevation(if (isPressed) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 26.sp)
            if (amount != null) {
                Text(
                    text = "+${amount}ml",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = WaterBlueDark
                )
            }
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// ─── Glass Indicators ────────────────────────────────────────────────────────

@Composable
private fun GlassIndicatorsCard(
    currentMl: Int,
    goalMl: Int
) {
    val totalGlasses = 8
    val mlPerGlass = goalMl.toFloat() / totalGlasses
    val filledGlasses = (currentMl / mlPerGlass).toInt().coerceAtMost(totalGlasses)
    val partialFill = ((currentMl / mlPerGlass) - filledGlasses).coerceIn(0f, 1f)

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🥛", fontSize = 18.sp)
                Text(
                    "$filledGlasses of $totalGlasses glasses",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0 until totalGlasses) {
                    val isFull = i < filledGlasses
                    val isPartial = i == filledGlasses && partialFill > 0f
                    GlassIndicator(
                        filled = isFull,
                        partialFill = if (isPartial) partialFill else 0f,
                        index = i
                    )
                }
            }

            // ML per glass label
            Text(
                text = "Each glass ≈ ${mlPerGlass.toInt()}ml",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GlassIndicator(
    filled: Boolean,
    partialFill: Float,
    index: Int
) {
    val animDelay = index * 50
    val animatedFill by animateFloatAsState(
        targetValue = when {
            filled -> 1f
            partialFill > 0f -> partialFill
            else -> 0f
        },
        animationSpec = tween(500, delayMillis = animDelay, easing = EaseOutCubic),
        label = "glass_fill_$index"
    )

    val bounceScale by animateFloatAsState(
        targetValue = if (filled) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "glass_bounce_$index"
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(bounceScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val glassWidth = size.width * 0.7f
            val glassHeight = size.height * 0.8f
            val left = (size.width - glassWidth) / 2
            val top = (size.height - glassHeight) / 2

            // Glass outline
            drawRoundRect(
                color = if (animatedFill > 0f) WaterBlueMedium.copy(alpha = 0.3f)
                else Color.Gray.copy(alpha = 0.2f),
                topLeft = Offset(left, top),
                size = Size(glassWidth, glassHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f),
                style = Stroke(width = 2f)
            )

            // Water fill
            if (animatedFill > 0f) {
                val fillHeight = glassHeight * animatedFill
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WaterBlueLight.copy(alpha = 0.7f),
                            WaterBlueMedium.copy(alpha = 0.9f)
                        ),
                        startY = top + glassHeight - fillHeight,
                        endY = top + glassHeight
                    ),
                    topLeft = Offset(left + 2, top + glassHeight - fillHeight),
                    size = Size(glassWidth - 4, fillHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
                )
            }
        }
    }
}

// ─── Water Log Entry ─────────────────────────────────────────────────────────

@Composable
private fun WaterLogEntry(
    log: WaterIntakeLog,
    runningTotal: Int,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        WaterBlueSurface,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        log.amountMl >= 500 -> "🍶"
                        log.amountMl >= 250 -> "🥛"
                        else -> "💧"
                    },
                    fontSize = 20.sp
                )
            }

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "+${log.amountMl}ml",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = WaterBlueDark
                    )
                    if (log.note.isNotBlank()) {
                        Text(
                            text = "• ${log.note}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = timeFormat.format(Date(log.timestamp)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "Total: ${runningTotal}ml",
                        fontSize = 12.sp,
                        color = WaterBlueMedium.copy(alpha = 0.7f)
                    )
                }
            }

            // Delete
            IconButton(
                onClick = {
                    if (showDeleteConfirm) {
                        onDelete()
                        showDeleteConfirm = false
                    } else {
                        showDeleteConfirm = true
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    if (showDeleteConfirm) Icons.Default.DeleteForever else Icons.Default.Close,
                    "Delete",
                    tint = if (showDeleteConfirm) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Empty State ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyLogState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WaterBlueSurface.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🌊", fontSize = 48.sp)
            Text(
                text = "No water logged today",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = WaterBlueDark
            )
            Text(
                text = "Tap the quick-add buttons above to start tracking your hydration!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Celebration Overlay ─────────────────────────────────────────────────────

@Composable
private fun CelebrationOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")

    // Floating emojis animation
    val emojis = listOf("🎉", "💧", "⭐", "🎊", "💪", "✨", "🌊", "🏆")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        // Floating emojis
        emojis.forEachIndexed { index, emoji ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 600f,
                targetValue = -200f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000 + index * 200,
                        delayMillis = index * 150,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "emoji_y_$index"
            )
            val offsetX = ((index * 47) % 300) - 150

            Text(
                text = emoji,
                fontSize = 28.sp,
                modifier = Modifier.offset(
                    x = offsetX.dp,
                    y = offsetY.dp
                )
            )
        }

        // Center card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🎉", fontSize = 56.sp)
                Text(
                    text = "Amazing!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoalGreen
                )
                Text(
                    text = "You've met your\nhydration goal today!",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Text(
                    text = "Great job staying hydrated! 💪",
                    fontSize = 13.sp,
                    color = GoalGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Custom Amount Dialog ────────────────────────────────────────────────────

@Composable
private fun CustomAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("💧", fontSize = 24.sp)
                Text("Custom Amount", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        error = null
                    },
                    label = { Text("Amount") },
                    suffix = { Text("ml", color = WaterBlueMedium) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick amount chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(150, 200, 330, 750).forEach { ml ->
                        FilterChip(
                            selected = amount == ml.toString(),
                            onClick = { amount = ml.toString() },
                            label = { Text("${ml}ml", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    placeholder = { Text("e.g., After workout") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ml = amount.toIntOrNull()
                    if (ml == null || ml <= 0) {
                        error = "Enter a valid amount"
                    } else if (ml > 3000) {
                        error = "That seems too much at once"
                    } else {
                        onConfirm(ml)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaterBlueMedium),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ─── Helper ──────────────────────────────────────────────────────────────────

private fun calculateRunningTotal(logs: List<WaterIntakeLog>, currentLog: WaterIntakeLog): Int {
    // Logs are in DESC order, running total needs ASC
    val sortedLogs = logs.sortedBy { it.timestamp }
    var total = 0
    for (log in sortedLogs) {
        total += log.amountMl
        if (log.id == currentLog.id) break
    }
    return total
}

@Composable
private fun StreakAndScoreMiniCard(
    currentStreak: Int,
    todayScore: HydrationScore?,
    onViewAchievements: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewAchievements() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streak
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (currentStreak > 0) "🔥" else "💤", fontSize = 24.sp)
                Text(
                    "$currentStreak",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = if (currentStreak > 0) Color(0xFFFF6B35) else Color.Gray
                )
                Text("Streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            )

            // Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(todayScore?.gradeEmoji ?: "😟", fontSize = 24.sp)
                Text(
                    todayScore?.grade ?: "F",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = WaterBlueDark
                )
                Text("Grade", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            )

            // Achievements button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏆", fontSize = 24.sp)
                Text(
                    "View",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = WaterBlueMedium
                )
                Text("Badges", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

// ─── Tools Quick Access ───────────────────────────────────────────────────────

@Composable
fun ToolsQuickAccessCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE3F2FD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🔬", fontSize = 24.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Hydration Tools", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Urine chart, symptoms & food estimate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(
                androidx.compose.material.icons.Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = WaterBlueMedium
            )
        }
    }
}
