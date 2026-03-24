package com.health.calculator.bmi.tracker.ui.components.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.util.*

// ============================================================
// MAIN HEALTH OVERVIEW SECTION
// ============================================================

@Composable
fun HealthOverviewSection(
    healthScore: HealthScoreResult,
    quickStats: List<QuickStat>,
    lastActivity: LastActivity?,
    onQuickStatClick: (String) -> Unit,
    onLastActivityClick: () -> Unit,
    onViewBreakdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✨", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "My Health Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (healthScore.category != HealthScoreCategory.INSUFFICIENT_DATA) {
                    TextButton(onClick = onViewBreakdown) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health Score Circle
            HealthScoreCircle(
                score = healthScore.totalScore,
                category = healthScore.category,
                availableMetrics = healthScore.availableMetrics,
                totalMetrics = healthScore.totalMetrics
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Stats Row
            if (quickStats.isNotEmpty()) {
                QuickStatsRow(
                    stats = quickStats,
                    onStatClick = onQuickStatClick
                )
            } else {
                EmptyStatsPrompt()
            }

            // Last Activity
            if (lastActivity != null) {
                Spacer(modifier = Modifier.height(14.dp))
                LastActivityRow(
                    activity = lastActivity,
                    onClick = onLastActivityClick
                )
            }
        }
    }
}

// ============================================================
// HEALTH SCORE CIRCLE
// ============================================================

@Composable
private fun HealthScoreCircle(
    score: Int,
    category: HealthScoreCategory,
    availableMetrics: Int,
    totalMetrics: Int
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "health_score"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "health_progress"
    )

    val hasData = category != HealthScoreCategory.INSUFFICIENT_DATA

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Score Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            // Background gradient glow
            if (hasData) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    val glowRadius = size.minDimension / 2
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                category.color.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            radius = glowRadius
                        ),
                        radius = glowRadius
                    )
                }
            }

            // Score arc
            Canvas(modifier = Modifier.size(130.dp)) {
                val strokeWidth = 14.dp.toPx()
                val arcRadius = (size.minDimension - strokeWidth) / 2

                // Background arc
                drawArc(
                    color = if (hasData) category.color.copy(alpha = 0.1f)
                    else Color(0xFF9E9E9E).copy(alpha = 0.1f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(arcRadius * 2, arcRadius * 2)
                )

                if (hasData) {
                    // Progress arc
                    drawArc(
                        color = category.color,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(arcRadius * 2, arcRadius * 2)
                    )
                }
            }

            // Center content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hasData) {
                    Text(
                        text = "$animatedScore",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = category.color
                    )
                    Text(
                        text = "/ 100",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                } else {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }

        // Category info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category.emoji,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = category.color
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics count
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📋", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$availableMetrics of $totalMetrics metrics tracked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ============================================================
// QUICK STATS ROW
// ============================================================

@Composable
private fun QuickStatsRow(
    stats: List<QuickStat>,
    onStatClick: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column {
        Text(
            text = "Quick Stats",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(stats, key = { it.id }) { stat ->
                QuickStatCard(
                    stat = stat,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStatClick(stat.calculatorRoute)
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickStatCard(
    stat: QuickStat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(95.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = stat.color.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Color indicator dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(stat.color)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stat.emoji,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Value
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = stat.color
                )
                stat.subValue?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stat.label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Progress bar (if applicable)
            stat.progress?.let { progress ->
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(stat.color.copy(alpha = 0.12f))
                ) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        label = "stat_progress_${stat.id}"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(stat.color)
                    )
                }
            }
        }
    }
}

// ============================================================
// EMPTY STATS PROMPT
// ============================================================

@Composable
private fun EmptyStatsPrompt() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📊", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Start Tracking Your Health",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Use the calculators below to see your stats here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ============================================================
// LAST ACTIVITY ROW
// ============================================================

@Composable
private fun LastActivityRow(
    activity: LastActivity,
    onClick: () -> Unit
) {
    val timeAgo = remember(activity.timestamp) {
        HealthScoreCalculator.formatTimeAgo(activity.timestamp)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🕐",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Last: ${activity.emoji} ${activity.calculatorName} • $timeAgo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Recalculate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
