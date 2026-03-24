// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/TDEESection.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.ActivityLevel
import com.health.calculator.bmi.tracker.data.model.GoalType
import com.health.calculator.bmi.tracker.data.model.TDEEData
import com.health.calculator.bmi.tracker.ui.utils.CascadeAnimatedItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TDEESection(
    bmr: Float,
    selectedActivityLevel: ActivityLevel,
    profileActivityLevel: ActivityLevel?,
    onActivityLevelChanged: (ActivityLevel) -> Unit,
    onCalorieTargetChanged: (Float) -> Unit = {},
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val tdee = (bmr * selectedActivityLevel.multiplier).toFloat()
    val activityCalories = tdee - bmr

    val tdeeData = TDEEData(
        bmr = bmr,
        activityLevel = selectedActivityLevel,
        tdee = tdee,
        activityCalories = activityCalories
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(200)),
        modifier = modifier
    ) {
        Column {
            // ---- TDEE Header ----
            CascadeAnimatedItem(index = 0, baseDelay = 80) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚡", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Total Daily Energy Expenditure",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Your estimated daily calorie needs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Activity Level Selector
                        ActivityLevelSelector(
                            selectedLevel = selectedActivityLevel,
                            profileLevel = profileActivityLevel,
                            onLevelSelected = onActivityLevelChanged
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- TDEE Result Card ----
            CascadeAnimatedItem(index = 1, baseDelay = 80) {
                TDEEResultCard(tdeeData = tdeeData)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Visual Breakdown ----
            CascadeAnimatedItem(index = 2, baseDelay = 80) {
                CalorieBreakdownCard(tdeeData = tdeeData)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Calorie Goals ----
            CascadeAnimatedItem(index = 3, baseDelay = 80) {
                // Calorie Goals Table
                CalorieGoalsCard(
                    tdeeData = tdeeData,
                    onGoalSelected = { goal ->
                        onCalorieTargetChanged(goal.dailyCalories)
                    }
                )
            }
        }
    }
}

// ============================================================
// Activity Level Selector
// ============================================================
@Composable
private fun ActivityLevelSelector(
    selectedLevel: ActivityLevel,
    profileLevel: ActivityLevel?,
    onLevelSelected: (ActivityLevel) -> Unit
) {
    Column {
        // Profile indicator
        if (profileLevel != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Profile: ${profileLevel.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Activity level cards
        ActivityLevel.entries.forEach { level ->
            val isSelected = level == selectedLevel

            ActivityLevelCard(
                level = level,
                isSelected = isSelected,
                onClick = { onLevelSelected(level) }
            )

            if (level != ActivityLevel.entries.last()) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ActivityLevelCard(
    level: ActivityLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        animationSpec = tween(200),
        label = "activityBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else Color.Transparent,
        animationSpec = tween(200),
        label = "activityBorder"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.98f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "activityScale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
        else null,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Text(text = level.emoji, fontSize = 22.sp)

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = level.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    ) {
                        Text(
                            text = "×${level.multiplier}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            // Selection indicator
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                )
            }
        }
    }
}

// ============================================================
// TDEE Result Card
// ============================================================
@Composable
private fun TDEEResultCard(tdeeData: TDEEData) {
    val animatedTDEE = remember { Animatable(0f) }
    LaunchedEffect(tdeeData.tdee) {
        animatedTDEE.snapTo(0f)
        animatedTDEE.animateTo(
            targetValue = tdeeData.tdee,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Your Daily Calorie Needs",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${animatedTDEE.value.toInt()}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 52.sp
                )

                Text(
                    text = "kcal/day",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${tdeeData.activityLevel.emoji} ${tdeeData.activityLevel.displayName} (×${tdeeData.activityLevel.multiplier})",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
// Calorie Breakdown Card (Visual: Donut + Stacked Bar)
// ============================================================
@Composable
private fun CalorieBreakdownCard(tdeeData: TDEEData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📊", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calorie Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut chart
                DonutChart(
                    bmrPercentage = tdeeData.bmrPercentage,
                    activityPercentage = tdeeData.activityPercentage,
                    modifier = Modifier.size(130.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Legend
                Column {
                    BreakdownLegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = "BMR (Rest)",
                        value = "${tdeeData.bmr.toInt()} kcal",
                        percentage = "${tdeeData.bmrPercentage.roundToInt()}%"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BreakdownLegendItem(
                        color = MaterialTheme.colorScheme.tertiary,
                        label = "Activity",
                        value = "${tdeeData.activityCalories.toInt()} kcal",
                        percentage = "${tdeeData.activityPercentage.roundToInt()}%"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total TDEE",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${tdeeData.tdee.toInt()} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stacked bar
            StackedCalorieBar(tdeeData = tdeeData)

            Spacer(modifier = Modifier.height(8.dp))

            // Equation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BMR ${tdeeData.bmr.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " × ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${tdeeData.activityLevel.multiplier}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = " = ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "TDEE ${tdeeData.tdee.toInt()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    bmrPercentage: Float,
    activityPercentage: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val animatedBMR = remember { Animatable(0f) }
    val animatedActivity = remember { Animatable(0f) }

    LaunchedEffect(bmrPercentage, activityPercentage) {
        launch {
            animatedBMR.animateTo(
                bmrPercentage / 100f * 360f,
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(200)
            animatedActivity.animateTo(
                activityPercentage / 100f * 360f,
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            val arcSize = Size(radius * 2, radius * 2)
            val topLeft = Offset(center.x - radius, center.y - radius)

            // Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // BMR arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = animatedBMR.value,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            // Activity arc
            drawArc(
                color = tertiaryColor,
                startAngle = -90f + animatedBMR.value,
                sweepAngle = animatedActivity.value,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "⚡", fontSize = 20.sp)
            Text(
                text = "TDEE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BreakdownLegendItem(
    color: Color,
    label: String,
    value: String,
    percentage: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = percentage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun StackedCalorieBar(tdeeData: TDEEData) {
    val bmrFraction = tdeeData.bmrPercentage / 100f
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val animatedBMR = remember { Animatable(0f) }
    LaunchedEffect(bmrFraction) {
        animatedBMR.animateTo(
            bmrFraction,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            // Activity portion (full width background)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tertiaryColor)
            )
            // BMR portion (overlaid from left)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedBMR.value)
                    .background(primaryColor)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BMR: ${tdeeData.bmr.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Activity: +${tdeeData.activityCalories.toInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = tertiaryColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============================================================
// Calorie Goals Card
// ============================================================
@Composable
private fun CalorieGoalsCard(
    tdeeData: TDEEData,
    onGoalSelected: (com.health.calculator.bmi.tracker.data.model.CalorieGoal) -> Unit = {}
) {
    val goals = tdeeData.getCalorieGoals()
    var expandedGoal by remember { mutableStateOf<GoalType?>(GoalType.MAINTAIN) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎯", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calorie Targets by Goal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap any goal to see details and projected weight change",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            goals.forEachIndexed { index, goal ->
                CalorieGoalItem(
                    goal = goal,
                    tdee = tdeeData.tdee,
                    isExpanded = expandedGoal == goal.goalType,
                    onClick = {
                        expandedGoal = if (expandedGoal == goal.goalType) null
                        else goal.goalType
                        // Notify parent of selected calorie target
                        if (expandedGoal == goal.goalType) {
                            onGoalSelected(goal)
                        }
                    },
                    animationDelay = index * 60
                )

                if (index < goals.lastIndex) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Note
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Weight change estimates assume 1 kg ≈ 7,700 kcal. " +
                            "Actual results vary by individual metabolism. " +
                            "Never eat below 1,200 kcal/day without medical supervision.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CalorieGoalItem(
    goal: com.health.calculator.bmi.tracker.data.model.CalorieGoal,
    tdee: Float,
    isExpanded: Boolean,
    onClick: () -> Unit,
    animationDelay: Int
) {
    val goalColor = getGoalColor(goal.goalType)
    val containerColor by animateColorAsState(
        targetValue = if (isExpanded) goalColor.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        animationSpec = tween(200),
        label = "goalBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isExpanded) goalColor.copy(alpha = 0.3f) else Color.Transparent,
        animationSpec = tween(200),
        label = "goalBorder"
    )

    val barFraction = (goal.dailyCalories / (tdee * 1.3f)).coerceIn(0f, 1f)
    val animatedBar = remember { Animatable(0f) }
    LaunchedEffect(barFraction) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        animatedBar.animateTo(
            barFraction,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    val weeklyChangeText = getWeeklyChangeText(goal.weeklyWeightChangeKg)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isExpanded) androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        else null,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = goal.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium,
                        color = if (isExpanded) goalColor else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${goal.dailyCalories.toInt()} kcal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = goalColor
                    )
                    if (goal.calorieAdjustment != 0) {
                        val sign = if (goal.calorieAdjustment > 0) "+" else ""
                        Text(
                            text = "$sign${goal.calorieAdjustment}",
                            style = MaterialTheme.typography.labelSmall,
                            color = goalColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Calorie bar
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = animatedBar.value)
                        .clip(RoundedCornerShape(3.dp))
                        .background(goalColor.copy(alpha = 0.7f))
                )
            }

            // Expanded details
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GoalDetailChip(
                            label = "Weekly Change",
                            value = weeklyChangeText,
                            color = goalColor
                        )
                        GoalDetailChip(
                            label = "Monthly Change",
                            value = getMonthlyChangeText(goal.weeklyWeightChangeKg),
                            color = goalColor
                        )
                    }

                    // Safety warning for extreme loss
                    if (goal.goalType == GoalType.EXTREME_LOSS) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF44336).copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Extreme deficits are not recommended without medical supervision. Consult a healthcare provider.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Minimum calorie warning
                    if (goal.dailyCalories <= 1200f && goal.calorieAdjustment < 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ Minimum 1,200 kcal/day applied for safety",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalDetailChip(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

private fun getGoalColor(goalType: GoalType): Color {
    return when (goalType) {
        GoalType.EXTREME_LOSS -> Color(0xFFF44336)
        GoalType.MODERATE_LOSS -> Color(0xFFFF9800)
        GoalType.MILD_LOSS -> Color(0xFFFFC107)
        GoalType.MAINTAIN -> Color(0xFF4CAF50)
        GoalType.MILD_GAIN -> Color(0xFF2196F3)
        GoalType.MODERATE_GAIN -> Color(0xFF7C4DFF)
    }
}

private fun getWeeklyChangeText(weeklyKg: Float): String {
    val absKg = abs(weeklyKg)
    return when {
        weeklyKg == 0f -> "No change"
        weeklyKg < 0 -> "↓ ${String.format("%.2f", absKg)} kg"
        else -> "↑ ${String.format("%.2f", absKg)} kg"
    }
}

private fun getMonthlyChangeText(weeklyKg: Float): String {
    val monthlyKg = abs(weeklyKg * 4.33f)
    return when {
        weeklyKg == 0f -> "No change"
        weeklyKg < 0 -> "↓ ${String.format("%.1f", monthlyKg)} kg"
        else -> "↑ ${String.format("%.1f", monthlyKg)} kg"
    }
}
