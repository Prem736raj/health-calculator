package com.health.calculator.bmi.tracker.ui.screens.metabolicsyndrome

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.AssessmentComparison
import com.health.calculator.bmi.tracker.data.model.CriterionTrend
import com.health.calculator.bmi.tracker.data.model.MetabolicSyndromeRecord
import com.health.calculator.bmi.tracker.data.model.MetabolicTrendDirection
import com.health.calculator.bmi.tracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MetabolicSyndromeTrackingSection(
    records: List<MetabolicSyndromeRecord>,
    comparison: AssessmentComparison?,
    isLabReminderEnabled: Boolean,
    reminderMonths: Int,
    onSetLabReminder: (Boolean, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Progress & Tracking",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (records.isEmpty()) {
            EmptyTrackingState()
        } else {
            // === Trend Arrows Overview ===
            if (comparison != null) {
                TrendArrowsOverview(comparison = comparison)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // === History Comparison ===
            if (comparison != null && comparison.previousDate != null) {
                HistoryComparisonCard(comparison = comparison)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // === Criteria Count Graph ===
            if (records.size >= 2) {
                CriteriaCountGraph(records = records)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // === Newly Normal Celebration ===
            if (comparison != null && comparison.newlyNormalCriteria.isNotEmpty()) {
                CelebrationCard(normalizedCriteria = comparison.newlyNormalCriteria)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // === Assessment Timeline ===
            AssessmentTimeline(records = records.take(5))
            Spacer(modifier = Modifier.height(16.dp))
        }

        // === Lab Reminder ===
        LabReminderCard(
            isEnabled = isLabReminderEnabled,
            months = reminderMonths,
            onSetReminder = onSetLabReminder
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EmptyTrackingState() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📊", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No assessments yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Complete your first metabolic syndrome assessment to start tracking your progress over time.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrendArrowsOverview(comparison: AssessmentComparison) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Risk Factor Trends",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            comparison.criterionTrends.forEach { trend ->
                TrendArrowRow(trend = trend)
                if (trend != comparison.criterionTrends.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendArrowRow(trend: CriterionTrend) {
    val trendColor = when (trend.trend) {
        MetabolicTrendDirection.IMPROVED -> HealthGreen
        MetabolicTrendDirection.WORSENED -> HealthRed
        MetabolicTrendDirection.UNCHANGED -> HealthYellow
        MetabolicTrendDirection.NEW -> HealthBlue
    }

    val trendIcon = when (trend.trend) {
        MetabolicTrendDirection.IMPROVED -> Icons.Filled.TrendingDown
        MetabolicTrendDirection.WORSENED -> Icons.Filled.TrendingUp
        MetabolicTrendDirection.UNCHANGED -> Icons.Filled.TrendingFlat
        MetabolicTrendDirection.NEW -> Icons.Filled.FiberNew
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = trend.icon,
            fontSize = 20.sp,
            modifier = Modifier.width(28.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trend.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = trend.currentValue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Status indicator
        Surface(
            color = if (trend.currentlyMet) HealthRed.copy(alpha = 0.12f) else HealthGreen.copy(alpha = 0.12f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = if (trend.currentlyMet) "⚠️" else "✅",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Trend arrow
        Surface(
            color = trendColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    trendIcon,
                    contentDescription = trend.trend.label,
                    tint = trendColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trend.trend.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = trendColor
                )
            }
        }
    }
}

@Composable
private fun HistoryComparisonCard(comparison: AssessmentComparison) {
    val overallColor = when (comparison.overallTrend) {
        MetabolicTrendDirection.IMPROVED -> HealthGreen
        MetabolicTrendDirection.WORSENED -> HealthRed
        MetabolicTrendDirection.UNCHANGED -> HealthYellow
        MetabolicTrendDirection.NEW -> HealthBlue
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = overallColor.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Compare,
                    contentDescription = null,
                    tint = overallColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comparison with Previous",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Side by side comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Previous
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Previous",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${comparison.previousCriteriaMet ?: "—"}/5",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = comparison.previousDate?.take(10) ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Arrow
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = overallColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = comparison.overallTrend.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = overallColor
                    )
                }

                // Current
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = overallColor.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${comparison.currentCriteriaMet}/5",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = overallColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = comparison.currentDate.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = overallColor.copy(alpha = 0.15f))

            Spacer(modifier = Modifier.height(12.dp))

            // Change summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChangeSummaryChip(
                    label = "Improved",
                    count = comparison.improvedCount,
                    color = HealthGreen
                )
                ChangeSummaryChip(
                    label = "Worsened",
                    count = comparison.worsenedCount,
                    color = HealthRed
                )
                ChangeSummaryChip(
                    label = "Unchanged",
                    count = comparison.unchangedCount,
                    color = HealthYellow
                )
            }
        }
    }
}

@Composable
private fun ChangeSummaryChip(label: String, count: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CriteriaCountGraph(records: List<MetabolicSyndromeRecord>) {
    val sortedRecords = remember(records) {
        records.sortedBy { it.timestamp }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "graph_anim"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Criteria Count Over Time",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Goal: Reduce abnormal criteria to 0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val lineColor = MaterialTheme.colorScheme.primary
            val dangerZoneColor = HealthRed.copy(alpha = 0.08f)
            val safeZoneColor = HealthGreen.copy(alpha = 0.08f)
            val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            val textColor = MaterialTheme.colorScheme.onSurfaceVariant
            val density = LocalDensity.current

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val paddingLeft = 40.dp.toPx()
                val paddingBottom = 30.dp.toPx()
                val paddingTop = 10.dp.toPx()
                val paddingRight = 16.dp.toPx()

                val graphWidth = size.width - paddingLeft - paddingRight
                val graphHeight = size.height - paddingTop - paddingBottom

                // Background zones
                // Danger zone (3-5 criteria = syndrome present)
                val dangerTop = paddingTop
                val dangerBottom = paddingTop + (graphHeight * 2f / 5f)
                drawRect(
                    color = dangerZoneColor,
                    topLeft = Offset(paddingLeft, dangerTop),
                    size = androidx.compose.ui.geometry.Size(graphWidth, dangerBottom - dangerTop)
                )

                // Safe zone (0-2 criteria)
                val safeTop = dangerBottom
                val safeBottom = paddingTop + graphHeight
                drawRect(
                    color = safeZoneColor,
                    topLeft = Offset(paddingLeft, safeTop),
                    size = androidx.compose.ui.geometry.Size(graphWidth, safeBottom - safeTop)
                )

                // Threshold line at 3
                val thresholdY = paddingTop + (graphHeight * 2f / 5f)
                drawLine(
                    color = HealthRed.copy(alpha = 0.5f),
                    start = Offset(paddingLeft, thresholdY),
                    end = Offset(size.width - paddingRight, thresholdY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f)
                    )
                )

                // Horizontal grid lines and y-axis labels
                for (i in 0..5) {
                    val y = paddingTop + (graphHeight * (5 - i) / 5f)
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(size.width - paddingRight, y),
                        strokeWidth = 1.dp.toPx()
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        "$i",
                        paddingLeft - 20.dp.toPx(),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = textColor.hashCode()
                            textSize = with(density) { 11.sp.toPx() }
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }

                // Plot data points and line
                if (sortedRecords.isNotEmpty()) {
                    val points = sortedRecords.mapIndexed { index, record ->
                        val x = paddingLeft + (graphWidth * index / (sortedRecords.size - 1).coerceAtLeast(1))
                        val y = paddingTop + graphHeight - (graphHeight * record.criteriaMet / 5f)
                        Offset(x, y)
                    }

                    // Animated line path
                    val animatedPoints = points.mapIndexed { index, point ->
                        val progress = (animatedProgress * points.size - index).coerceIn(0f, 1f)
                        if (index == 0) point
                        else {
                            val prev = points[index - 1]
                            Offset(
                                prev.x + (point.x - prev.x) * progress,
                                prev.y + (point.y - prev.y) * progress
                            )
                        }
                    }

                    // Draw line
                    if (animatedPoints.size >= 2) {
                        val path = Path().apply {
                            moveTo(animatedPoints.first().x, animatedPoints.first().y)
                            for (i in 1 until animatedPoints.size) {
                                val visibleIndex = (animatedProgress * points.size).toInt()
                                if (i <= visibleIndex) {
                                    lineTo(animatedPoints[i].x, animatedPoints[i].y)
                                }
                            }
                        }
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Draw dots
                    animatedPoints.forEachIndexed { index, point ->
                        val visibleIndex = (animatedProgress * points.size).toInt()
                        if (index <= visibleIndex) {
                            val record = sortedRecords[index]
                            val dotColor = when {
                                record.criteriaMet >= 3 -> HealthRed
                                record.criteriaMet >= 1 -> HealthOrange
                                else -> HealthGreen
                            }
                            drawCircle(
                                color = Color.White,
                                radius = 7.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = dotColor,
                                radius = 5.dp.toPx(),
                                center = point
                            )
                        }
                    }

                    // X-axis labels
                    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                    val step = (sortedRecords.size / 5).coerceAtLeast(1)
                    sortedRecords.forEachIndexed { index, record ->
                        if (index % step == 0 || index == sortedRecords.size - 1) {
                            val x = paddingLeft + (graphWidth * index / (sortedRecords.size - 1).coerceAtLeast(1))
                            drawContext.canvas.nativeCanvas.drawText(
                                dateFormat.format(Date(record.timestamp)),
                                x,
                                size.height - 4.dp.toPx(),
                                android.graphics.Paint().apply {
                                    color = textColor.hashCode()
                                    textSize = with(density) { 10.sp.toPx() }
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LegendDot(color = HealthRed.copy(alpha = 0.15f), label = "Syndrome Present (≥3)")
                Spacer(modifier = Modifier.width(16.dp))
                LegendDot(color = HealthGreen.copy(alpha = 0.15f), label = "Below Threshold (<3)")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CelebrationCard(normalizedCriteria: List<String>) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "celebration_scale"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = HealthGreen.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉",
                fontSize = (36 * scale).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Great Progress!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = HealthGreen
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "The following criteria moved from abnormal to normal:",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            normalizedCriteria.forEach { criterion ->
                Surface(
                    color = HealthGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$criterion is now normal!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = HealthGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your lifestyle changes are making a real difference. Keep going! 💪",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AssessmentTimeline(records: List<MetabolicSyndromeRecord>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Assessments",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            records.forEachIndexed { index, record ->
                val riskColor = when {
                    record.criteriaMet >= 4 -> Color(0xFFB71C1C)
                    record.criteriaMet >= 3 -> HealthRed
                    record.criteriaMet >= 2 -> HealthOrange
                    record.criteriaMet >= 1 -> HealthYellow
                    else -> HealthGreen
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timeline dot and line
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(riskColor)
                        )
                        if (index < records.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(32.dp)
                                    .background(
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.dateTime.take(16),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${record.criteriaMet}/5 criteria • ${record.riskLevel}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        color = riskColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (record.isSyndromePresent) "Present" else "Absent",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = riskColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabReminderCard(
    isEnabled: Boolean,
    months: Int,
    onSetReminder: (Boolean, Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedMonths by remember { mutableIntStateOf(months) }
    var showMonthPicker by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = HealthBlue.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HealthBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = HealthBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lab Test Reminder",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Reassessment recommended every 3-6 months",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { enabled ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSetReminder(enabled, selectedMonths)
                    }
                )
            }

            AnimatedVisibility(
                visible = isEnabled,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(
                        color = HealthBlue.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Remind me in:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 4, 6).forEach { monthOption ->
                            FilterChip(
                                selected = selectedMonths == monthOption,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedMonths = monthOption
                                    onSetReminder(true, monthOption)
                                },
                                label = {
                                    Text("$monthOption months")
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = HealthBlue.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔔", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            val reminderDate = Date(
                                System.currentTimeMillis() + (selectedMonths * 30L * 24 * 60 * 60 * 1000)
                            )
                            Text(
                                text = "Next check-up reminder: ${dateFormat.format(reminderDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = HealthBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You'll receive a notification: \"Time for your metabolic health check-up. Schedule blood work to reassess your risk factors.\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
