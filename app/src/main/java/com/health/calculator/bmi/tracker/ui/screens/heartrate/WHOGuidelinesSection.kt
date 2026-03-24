package com.health.calculator.bmi.tracker.ui.screens.heartrate

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.util.*
import com.health.calculator.bmi.tracker.ui.components.FitnessLevel

// ============================================================
// MAIN WHO GUIDELINES SECTION
// ============================================================

@Composable
fun WHOGuidelinesSection(
    age: Int,
    fitnessLevel: FitnessLevel,
    zones: List<HeartRateZone>,
    selectedGoalName: String,
    modifier: Modifier = Modifier
) {
    val guideline = remember(age) { WHOExerciseGuidelinesEngine.getGuidelineForAge(age) }
    val weeklyPlan = remember(selectedGoalName, fitnessLevel, age) {
        WHOExerciseGuidelinesEngine.generateWeeklyPlan(selectedGoalName, fitnessLevel, age)
    }

    var expandedSection by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🏥", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "WHO Exercise Guidelines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "World Health Organization Physical Activity Recommendations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Age-specific guideline card
        WHOGuidelineCard(guideline = guideline)

        // Zone-to-intensity mapping
        ZoneIntensityMappingCard(zones = zones)

        // Weekly Plan
        WeeklyPlanCard(
            plan = weeklyPlan,
            isExpanded = expandedSection == 1,
            onToggle = { expandedSection = if (expandedSection == 1) -1 else 1 }
        )

        // Weekly exercise tracker
        WeeklyExerciseTracker(
            guideline = guideline,
            zones = zones
        )
    }
}

// ============================================================
// WHO GUIDELINE CARD
// ============================================================

@Composable
private fun WHOGuidelineCard(guideline: WHOGuideline) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1565C0).copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Age group badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1565C0).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "📋 ${guideline.ageGroup} (${guideline.ageRange})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Key message
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = guideline.keyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Targets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TargetCard(
                    emoji = "🟢",
                    label = "Moderate",
                    value = "${guideline.moderateMinPerWeek.first}-${guideline.moderateMinPerWeek.last}",
                    unit = "min/week",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "OR",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                TargetCard(
                    emoji = "🔴",
                    label = "Vigorous",
                    value = "${guideline.vigorousMinPerWeek.first}-${guideline.vigorousMinPerWeek.last}",
                    unit = "min/week",
                    color = Color(0xFFE53935),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Additional recommendations
            var showAllRecs by remember { mutableStateOf(false) }

            Column {
                val displayRecs = if (showAllRecs) guideline.additionalRecommendations
                else guideline.additionalRecommendations.take(3)

                displayRecs.forEach { rec ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = Color(0xFF1565C0),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp, top = 1.dp)
                        )
                        Text(
                            text = rec,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 17.sp
                        )
                    }
                }

                if (guideline.additionalRecommendations.size > 3) {
                    TextButton(
                        onClick = { showAllRecs = !showAllRecs },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (showAllRecs) "Show less" else "Show all recommendations",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1565C0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetCard(
    emoji: String,
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

// ============================================================
// ZONE INTENSITY MAPPING
// ============================================================

@Composable
private fun ZoneIntensityMappingCard(zones: List<HeartRateZone>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📊 Zone ↔ WHO Intensity Mapping",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Moderate section
            IntensityGroup(
                label = "Moderate Intensity",
                emoji = "🟢",
                color = Color(0xFF4CAF50),
                zones = zones.filter { it.zoneNumber in 1..3 },
                description = "Counts toward 150-300 min/week target"
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Vigorous section
            IntensityGroup(
                label = "Vigorous Intensity",
                emoji = "🔴",
                color = Color(0xFFE53935),
                zones = zones.filter { it.zoneNumber in 4..5 },
                description = "1 min vigorous = 2 min moderate equivalent"
            )
        }
    }
}

@Composable
private fun IntensityGroup(
    label: String,
    emoji: String,
    color: Color,
    zones: List<HeartRateZone>,
    description: String
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        zones.forEach { zone ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(zone.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zone ${zone.zoneNumber} — ${zone.zoneName}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${zone.bpmLow}-${zone.bpmHigh} BPM",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = zone.color
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            modifier = Modifier.padding(start = 24.dp)
        )
    }
}

// ============================================================
// WEEKLY PLAN CARD
// ============================================================

@Composable
private fun WeeklyPlanCard(
    plan: WeeklyPlan,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = plan.goalEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📅 Weekly Plan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = plan.planName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStat(
                    value = "${plan.totalActiveMin}",
                    label = "Total min",
                    emoji = "⏱️"
                )
                MiniStat(
                    value = "${plan.totalModerateMin}",
                    label = "Moderate",
                    emoji = "🟢"
                )
                MiniStat(
                    value = "${plan.totalVigorousMin}",
                    label = "Vigorous",
                    emoji = "🔴"
                )
                MiniStat(
                    value = "${plan.days.count { it.isRestDay }}",
                    label = "Rest days",
                    emoji = "😴"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // WHO compliance
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (plan.meetsWHOGuideline) Color(0xFF4CAF50).copy(alpha = 0.1f)
                else Color(0xFFFF9800).copy(alpha = 0.1f)
            ) {
                Text(
                    text = plan.whoComplianceNote,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (plan.meetsWHOGuideline) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Expanded day-by-day plan
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    plan.days.forEach { day ->
                        DayPlanRow(day = day)
                        if (day.dayName != "Sunday") {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            if (!isExpanded) {
                Text(
                    text = "Tap to see day-by-day plan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 14.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun DayPlanRow(day: WeeklyPlanDay) {
    val bgColor = if (day.isRestDay)
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day name
            Text(
                text = day.emoji,
                fontSize = 18.sp,
                modifier = Modifier.width(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.dayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (day.isRestDay) {
                    Text(
                        text = "Rest & Recovery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    Text(
                        text = day.activitySuggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (!day.isRestDay) {
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = day.zoneFocus,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${day.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// ============================================================
// WEEKLY EXERCISE TRACKER
// ============================================================

@Composable
fun WeeklyExerciseTracker(
    guideline: WHOGuideline,
    zones: List<HeartRateZone>
) {
    var showLogDialog by remember { mutableStateOf(false) }
    var loggedSessions by remember { mutableStateOf(listOf<ExerciseLogEntry>()) }

    val totalModerateMin = loggedSessions
        .filter { it.intensity == ExerciseIntensity.MODERATE }
        .sumOf { it.durationMinutes }
    val totalVigorousMin = loggedSessions
        .filter { it.intensity == ExerciseIntensity.VIGOROUS }
        .sumOf { it.durationMinutes }
    val totalEquivalent = totalModerateMin +
            WHOExerciseGuidelinesEngine.vigorousToModerateEquivalent(totalVigorousMin)
    val targetMin = guideline.moderateMinPerWeek.first
    val progress = if (targetMin > 0) (totalEquivalent.toFloat() / targetMin).coerceIn(0f, 1.5f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📊 Weekly Exercise Tracker",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = { showLogDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Ring
                WeeklyProgressRing(
                    progress = progress,
                    totalMin = totalEquivalent,
                    targetMin = targetMin,
                    modifier = Modifier.size(100.dp)
                )

                // Stats
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrackerStat(
                        emoji = "🟢",
                        label = "Moderate",
                        value = "$totalModerateMin min",
                        target = "${guideline.moderateMinPerWeek.first} min"
                    )
                    TrackerStat(
                        emoji = "🔴",
                        label = "Vigorous",
                        value = "$totalVigorousMin min",
                        target = "${guideline.vigorousMinPerWeek.first} min"
                    )
                    TrackerStat(
                        emoji = "📊",
                        label = "Equivalent",
                        value = "$totalEquivalent min",
                        target = "$targetMin min"
                    )
                }
            }

            // WHO target note
            if (totalEquivalent > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (progress >= 1f) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else Color(0xFF2196F3).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (progress >= 1f)
                            "🎉 You've met the WHO weekly exercise target!"
                        else
                            "📈 ${targetMin - totalEquivalent} more moderate-equivalent minutes to reach WHO target",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (progress >= 1f) Color(0xFF4CAF50) else Color(0xFF2196F3),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Logged sessions
            if (loggedSessions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "This Week's Sessions",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                loggedSessions.forEachIndexed { index, session ->
                    LoggedSessionRow(
                        session = session,
                        onDelete = {
                            loggedSessions = loggedSessions.toMutableList().also {
                                it.removeAt(index)
                            }
                        }
                    )
                    if (index < loggedSessions.lastIndex) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }

    // Log dialog
    if (showLogDialog) {
        ExerciseLogDialog(
            zones = zones,
            onDismiss = { showLogDialog = false },
            onLog = { entry ->
                loggedSessions = loggedSessions + entry
                showLogDialog = false
            }
        )
    }
}

@Composable
private fun WeeklyProgressRing(
    progress: Float,
    totalMin: Int,
    targetMin: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "weekly_progress"
    )

    val ringColor = when {
        progress >= 1f -> Color(0xFF4CAF50)
        progress >= 0.6f -> Color(0xFF8BC34A)
        progress >= 0.3f -> Color(0xFFFFC107)
        else -> Color(0xFFBDBDBD)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            drawArc(
                color = ringColor.copy(alpha = 0.12f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = (animatedProgress * 360f).coerceAtMost(360f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress * 100).toInt().coerceAtMost(100)}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = ringColor
            )
            Text(
                text = "$totalMin/$targetMin",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TrackerStat(emoji: String, label: String, value: String, target: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = " / $target",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun LoggedSessionRow(
    session: ExerciseLogEntry,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = session.intensity.emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Zone ${session.zoneNumber} — ${session.zoneName}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            if (session.activityNote.isNotBlank()) {
                Text(
                    text = session.activityNote,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        Text(
            text = "${session.durationMinutes} min",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

// ============================================================
// EXERCISE LOG DIALOG
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLogDialog(
    zones: List<HeartRateZone>,
    onDismiss: () -> Unit,
    onLog: (ExerciseLogEntry) -> Unit
) {
    var selectedZone by remember { mutableIntStateOf(2) } // Default Zone 3 (index 2)
    var duration by remember { mutableStateOf("30") }
    var activityNote by remember { mutableStateOf("") }

    val selectedZoneData = zones.getOrNull(selectedZone)
    val intensity = WHOExerciseGuidelinesEngine.getZoneIntensity(selectedZone + 1)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val dur = duration.toIntOrNull() ?: 0
                    if (dur > 0 && selectedZoneData != null) {
                        onLog(
                            ExerciseLogEntry(
                                dayOfWeek = java.time.LocalDate.now().dayOfWeek.value,
                                durationMinutes = dur,
                                zoneNumber = selectedZoneData.zoneNumber,
                                zoneName = selectedZoneData.zoneName,
                                intensity = intensity,
                                activityNote = activityNote
                            )
                        )
                    }
                },
                enabled = (duration.toIntOrNull() ?: 0) > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Log Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text(
                text = "🏋️ Log Exercise Session",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Zone selector
                Text(
                    text = "Select Zone",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    zones.forEachIndexed { index, zone ->
                        FilterChip(
                            selected = selectedZone == index,
                            onClick = { selectedZone = index },
                            label = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = zone.icon,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Z${zone.zoneNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = zone.color.copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                // Selected zone info
                selectedZoneData?.let { zone ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = zone.color.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = zone.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${zone.zoneName} • ${zone.bpmLow}-${zone.bpmHigh} BPM • ${intensity.emoji} ${intensity.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = zone.color
                            )
                        }
                    }
                }

                // Duration
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { c -> c.isDigit() } },
                    label = { Text("Duration") },
                    suffix = { Text("minutes") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Activity note
                OutlinedTextField(
                    value = activityNote,
                    onValueChange = { activityNote = it },
                    label = { Text("Activity (optional)") },
                    placeholder = { Text("e.g., Running, Cycling...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
