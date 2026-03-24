package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*
import kotlin.math.cos
import kotlin.math.sin

// ─── Category Colors ───────────────────────────────────────────────────────────

fun getBpCategoryColor(category: BpCategory): Color {
    return when (category) {
        BpCategory.HYPOTENSION -> Color(0xFF42A5F5)        // Blue
        BpCategory.OPTIMAL -> Color(0xFF4CAF50)             // Green
        BpCategory.NORMAL -> Color(0xFF8BC34A)              // Light Green
        BpCategory.HIGH_NORMAL -> Color(0xFFFFC107)         // Amber
        BpCategory.ISOLATED_SYSTOLIC -> Color(0xFFFF9800)   // Orange
        BpCategory.GRADE_1_HYPERTENSION -> Color(0xFFFF7043) // Deep Orange
        BpCategory.GRADE_2_HYPERTENSION -> Color(0xFFF44336) // Red
        BpCategory.GRADE_3_HYPERTENSION -> Color(0xFFD32F2F) // Dark Red
        BpCategory.HYPERTENSIVE_CRISIS -> Color(0xFFB71C1C)  // Very Dark Red
    }
}

fun getBpRiskColor(risk: BpRiskLevel): Color {
    return when (risk) {
        BpRiskLevel.LOW -> Color(0xFF4CAF50)
        BpRiskLevel.MODERATE -> Color(0xFFFFC107)
        BpRiskLevel.HIGH -> Color(0xFFFF9800)
        BpRiskLevel.VERY_HIGH -> Color(0xFFF44336)
        BpRiskLevel.EMERGENCY -> Color(0xFFB71C1C)
    }
}

// ─── Main Result Section ───────────────────────────────────────────────────────

@Composable
fun BpResultSection(
    reading: BloodPressureReading,
    pulsePressureAnalysis: PulsePressureAnalysis,
    mapAnalysis: MapAnalysis,
    heartRateAnalysis: HeartRateAnalysis?,
    riskLevel: BpRiskLevel,
    gaugePosition: Float,
    isMultiReadingMode: Boolean = false,
    showAverageResult: Boolean = false,
    onTakeAnotherReading: () -> Unit = {},
    onShowAverage: () -> Unit = {},
    onAddNote: () -> Unit = {},
    onViewLog: () -> Unit = {},
    onViewTrends: () -> Unit = {},
    onViewExport: () -> Unit = {},
    onNavigateToEducation: () -> Unit = {}
) {
    val categoryColor = getBpCategoryColor(reading.category)
    val savedReadingId = null // This would come from DB if needed, but and BpShareButtons uses it for specific share if we had more logic. For now null is fine.

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main reading card
        BpReadingCard(reading = reading, categoryColor = categoryColor)

        // Animated Gauge
        BpGaugeCard(
            reading = reading,
            gaugePosition = gaugePosition,
            categoryColor = categoryColor
        )

        // Category Scale Chart
        BpCategoryScaleCard(currentCategory = reading.category)

        // Risk Level
        BpRiskLevelCard(riskLevel = riskLevel)

        // Advanced Metrics Section
        BpAdvancedMetricsSection(
            systolic = reading.systolic,
            diastolic = reading.diastolic,
            pulse = reading.pulse
        )

        Spacer(modifier = Modifier.height(8.dp))
        
        // Multi-reading controls
        if (isMultiReadingMode && !showAverageResult) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onTakeAnotherReading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add Reading")
                }
                Button(
                    onClick = onShowAverage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Avg Result")
                }
            }
        } else {
            OutlinedButton(
                onClick = onTakeAnotherReading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Another Reading (Avg)")
            }
        }

        // Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onAddNote,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Note")
            }
            OutlinedButton(
                onClick = onViewLog,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Log")
            }
        }

        // Share and Export
        BpShareButtons(
            reading = reading,
            savedReadingId = savedReadingId,
            onNavigateToExport = onViewExport
        )

        // View Trends Button
        Button(
            onClick = onViewTrends,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(Icons.Outlined.Timeline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Trends & Charts", fontWeight = FontWeight.Bold)
        }

        // Learn About Blood Pressure
        OutlinedButton(
            onClick = onNavigateToEducation,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF00897B)
            ),
            border = BorderStroke(1.dp, Color(0xFF00897B).copy(alpha = 0.3f))
        ) {
            Icon(Icons.Outlined.School, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Learn About Blood Pressure", fontWeight = FontWeight.Medium)
        }

        // Personalized Recommendations
        BpRecommendationsSection(category = reading.category)
    }
}

// ─── Reading Card ──────────────────────────────────────────────────────────────

@Composable
private fun BpReadingCard(
    reading: BloodPressureReading,
    categoryColor: Color
) {
    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }

    // Animate the numbers counting up
    val systolicAnim by animateIntAsState(
        targetValue = reading.systolic,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "systolic_anim"
    )
    val diastolicAnim by animateIntAsState(
        targetValue = reading.diastolic,
        animationSpec = tween(800, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "diastolic_anim"
    )

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = categoryColor.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, categoryColor.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reading value
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "$systolicAnim",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFE53935)
                    )
                    Text(
                        "/",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Light
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 4.dp).padding(bottom = 6.dp)
                    )
                    Text(
                        "$diastolicAnim",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1E88E5)
                    )
                }

                Text(
                    "mmHg",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = categoryColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        reading.category.displayName,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }

                Text(
                    reading.category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                // Measurement info
                if (reading.arm != null || reading.position != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        reading.arm?.let {
                            BpInfoChip(
                                icon = Icons.Outlined.FrontHand,
                                text = it.displayName
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        reading.position?.let {
                            BpInfoChip(
                                icon = Icons.Outlined.Accessibility,
                                text = it.displayName
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        BpInfoChip(
                            icon = Icons.Outlined.Schedule,
                            text = reading.formattedTime
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BpInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// ─── Gauge Card ────────────────────────────────────────────────────────────────

@Composable
private fun BpGaugeCard(
    reading: BloodPressureReading,
    gaugePosition: Float,
    categoryColor: Color
) {
    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }

    // Animate gauge needle with overshoot
    val animatedPosition by animateFloatAsState(
        targetValue = gaugePosition,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gauge_position"
    )

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { 60 }) + fadeIn(
            animationSpec = tween(600, delayMillis = 300)
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Blood Pressure Gauge",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Semi-circular gauge
                BpSemiCircularGauge(
                    position = animatedPosition,
                    categoryColor = categoryColor,
                    reading = reading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }
        }
    }
}

@Composable
private fun BpSemiCircularGauge(
    position: Float,
    categoryColor: Color,
    reading: BloodPressureReading,
    modifier: Modifier = Modifier
) {
    val gaugeColors = listOf(
        Color(0xFF42A5F5), // Hypotension - Blue
        Color(0xFF4CAF50), // Optimal - Green
        Color(0xFF8BC34A), // Normal - Light Green
        Color(0xFFFFC107), // High Normal - Amber
        Color(0xFFFF9800), // Grade 1 - Orange
        Color(0xFFF44336), // Grade 2 - Red
        Color(0xFFB71C1C)  // Grade 3/Crisis - Dark Red
    )

    val density = LocalDensity.current
    val textColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight - 20.dp.toPx()
        val radius = (canvasWidth / 2.5f).coerceAtMost(canvasHeight - 30.dp.toPx())
        val strokeWidth = 24.dp.toPx()

        // Draw colored arc segments
        val startAngle = 180f
        val totalSweep = 180f
        val segmentSweep = totalSweep / gaugeColors.size

        gaugeColors.forEachIndexed { index, color ->
            drawArc(
                color = color,
                startAngle = startAngle + (index * segmentSweep),
                sweepAngle = segmentSweep + 0.5f, // Slight overlap to avoid gaps
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
        }

        // Draw tick marks
        for (i in 0..6) {
            val angle = Math.toRadians((180.0 + (i * 30.0)))
            val innerR = radius - strokeWidth / 2 - 6.dp.toPx()
            val outerR = radius + strokeWidth / 2 + 6.dp.toPx()
            drawLine(
                color = textColor.copy(alpha = 0.3f),
                start = Offset(
                    centerX + (innerR * cos(angle)).toFloat(),
                    centerY + (innerR * sin(angle)).toFloat()
                ),
                end = Offset(
                    centerX + (outerR * cos(angle)).toFloat(),
                    centerY + (outerR * sin(angle)).toFloat()
                ),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // Draw needle
        val needleAngle = Math.toRadians((180.0 + (position * 180.0)))
        val needleLength = radius - strokeWidth / 2 - 12.dp.toPx()
        val needleEndX = centerX + (needleLength * cos(needleAngle)).toFloat()
        val needleEndY = centerY + (needleLength * sin(needleAngle)).toFloat()

        // Needle shadow
        drawLine(
            color = Color.Black.copy(alpha = 0.15f),
            start = Offset(centerX + 2, centerY + 2),
            end = Offset(needleEndX + 2, needleEndY + 2),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Needle line
        drawLine(
            color = categoryColor,
            start = Offset(centerX, centerY),
            end = Offset(needleEndX, needleEndY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center dot
        drawCircle(
            color = categoryColor,
            radius = 8.dp.toPx(),
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(centerX, centerY)
        )

        // Needle tip dot
        drawCircle(
            color = categoryColor,
            radius = 5.dp.toPx(),
            center = Offset(needleEndX, needleEndY)
        )

        // Labels at ends
        val labelPaint = android.graphics.Paint().apply {
            color = textColor.copy(alpha = 0.5f).hashCode()
            textSize = with(density) { 10.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
        }

        drawContext.canvas.nativeCanvas.apply {
            drawText(
                "Low",
                centerX - radius - 4.dp.toPx(),
                centerY + 16.dp.toPx(),
                labelPaint
            )
            drawText(
                "High",
                centerX + radius + 4.dp.toPx(),
                centerY + 16.dp.toPx(),
                labelPaint
            )
        }
    }
}

// ─── Category Scale Card ───────────────────────────────────────────────────────

@Composable
private fun BpCategoryScaleCard(currentCategory: BpCategory) {
    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }

    val categories = listOf(
        BpCategory.HYPOTENSION,
        BpCategory.OPTIMAL,
        BpCategory.NORMAL,
        BpCategory.HIGH_NORMAL,
        BpCategory.GRADE_1_HYPERTENSION,
        BpCategory.GRADE_2_HYPERTENSION,
        BpCategory.GRADE_3_HYPERTENSION,
        BpCategory.HYPERTENSIVE_CRISIS
    )

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { 80 }) + fadeIn(
            animationSpec = tween(600, delayMillis = 500)
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "WHO Classification",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                categories.forEach { category ->
                    val isCurrentCategory = category == currentCategory
                    val color = getBpCategoryColor(category)

                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (isCurrentCategory) 1f else 0.5f,
                        animationSpec = tween(500),
                        label = "category_alpha_${category.name}"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCurrentCategory) color.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .then(
                                if (isCurrentCategory)
                                    Modifier.border(
                                        1.dp,
                                        color.copy(alpha = 0.3f),
                                        RoundedCornerShape(10.dp)
                                    )
                                else Modifier
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Color dot
                            Box(
                                modifier = Modifier
                                    .size(if (isCurrentCategory) 14.dp else 10.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = animatedAlpha))
                            )

                            Column {
                                Text(
                                    category.displayName,
                                    style = if (isCurrentCategory)
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    else
                                        MaterialTheme.typography.bodySmall,
                                    color = if (isCurrentCategory) color
                                    else MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = animatedAlpha
                                    )
                                )
                            }
                        }

                        // Ranges
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "SYS ${category.systolicRange}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrentCategory) color
                                else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = animatedAlpha * 0.7f
                                ),
                                fontWeight = if (isCurrentCategory) FontWeight.SemiBold
                                else FontWeight.Normal
                            )
                            Text(
                                "DIA ${category.diastolicRange}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrentCategory) color
                                else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = animatedAlpha * 0.7f
                                ),
                                fontWeight = if (isCurrentCategory) FontWeight.SemiBold
                                else FontWeight.Normal
                            )
                        }

                        // Arrow indicator for current
                        if (isCurrentCategory) {
                            Icon(
                                Icons.Filled.ArrowLeft,
                                contentDescription = "Current",
                                tint = color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Risk Level Card ───────────────────────────────────────────────────────────

@Composable
private fun BpRiskLevelCard(riskLevel: BpRiskLevel) {
    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }
    val riskColor = getBpRiskColor(riskLevel)

    val riskIcon = when (riskLevel) {
        BpRiskLevel.LOW -> Icons.Filled.CheckCircle
        BpRiskLevel.MODERATE -> Icons.Filled.Info
        BpRiskLevel.HIGH -> Icons.Filled.Warning
        BpRiskLevel.VERY_HIGH -> Icons.Filled.Warning
        BpRiskLevel.EMERGENCY -> Icons.Filled.Error
    }

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(
            animationSpec = tween(600, delayMillis = 700)
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = riskColor.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, riskColor.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Risk icon with background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(riskColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        riskIcon,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Risk Assessment",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        riskLevel.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        riskLevel.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
            }
        }
    }
}



// ─── Emergency Dialog ──────────────────────────────────────────────────────────

@Composable
fun BpEmergencyDialog(
    reading: BloodPressureReading,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emergency_anim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emergency_pulse"
    )
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emergency_icon_scale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFFF3F3),
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB71C1C).copy(alpha = pulseAlpha * 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = null,
                    tint = Color(0xFFB71C1C).copy(alpha = pulseAlpha),
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }
        },
        title = {
            Text(
                "⚠️ HYPERTENSIVE CRISIS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFB71C1C),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Reading
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFB71C1C).copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFB71C1C).copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Your Reading",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFB71C1C).copy(alpha = 0.7f)
                        )
                        Text(
                            "${reading.systolic}/${reading.diastolic} mmHg",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }

                Text(
                    "This reading indicates a hypertensive crisis that requires immediate medical attention.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF424242)
                )

                // Symptoms warning
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF9C4)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "If you experience any of these symptoms:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5D4037)
                        )
                        val symptoms = listOf(
                            "Severe headache",
                            "Chest pain",
                            "Difficulty breathing",
                            "Vision changes",
                            "Numbness or weakness",
                            "Difficulty speaking"
                        )
                        symptoms.forEach { symptom ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD32F2F))
                                )
                                Text(
                                    symptom,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5D4037)
                                )
                            }
                        }
                    }
                }

                // Emergency number
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFB71C1C)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Call Emergency Services",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Emergency: 911 / 112",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "I Understand",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
