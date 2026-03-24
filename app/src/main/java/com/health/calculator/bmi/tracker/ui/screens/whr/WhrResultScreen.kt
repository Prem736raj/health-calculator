package com.health.calculator.bmi.tracker.ui.screens.whr

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Size
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
import com.health.calculator.bmi.tracker.data.model.*
import android.content.Intent
import kotlin.math.cos
import kotlin.math.sin
import com.health.calculator.bmi.tracker.ui.screens.whr.WhrEdgeCaseHandler
import com.health.calculator.bmi.tracker.ui.screens.whr.AnimatedBodyShapeIcon
import com.health.calculator.bmi.tracker.ui.screens.whr.WhrShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhrResultScreen(
    result: WhrResult,
    onNavigateBack: () -> Unit,
    onRecalculate: () -> Unit,
    onSaveToHistory: () -> Unit,
    onViewProgress: () -> Unit = {},
    onViewAdvancedMetrics: () -> Unit = {},
    onNavigateToEducation: () -> Unit = {},
    showHeightInput: Boolean = false,
    onHeightSubmitted: ((Float) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isSaved by remember { mutableStateOf(false) }
    var showHeightDialog by remember { mutableStateOf(showHeightInput && result.heightCm == null) }
    var heightInput by remember { mutableStateOf("") }

    // Animation
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic)
        )
    }

    // Height Input Dialog
    if (showHeightDialog) {
        AlertDialog(
            onDismissRequest = { showHeightDialog = false },
            title = { Text("Enter Your Height") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Height is needed to calculate your Waist-to-Height Ratio",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                heightInput = it
                            }
                        },
                        label = { Text("Height (cm)") },
                        suffix = { Text("cm") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        heightInput.toFloatOrNull()?.let { h ->
                            if (h in 50f..300f) {
                                onHeightSubmitted?.invoke(h)
                                showHeightDialog = false
                            }
                        }
                    }
                ) { Text("Calculate") }
            },
            dismissButton = {
                TextButton(onClick = { showHeightDialog = false }) {
                    Text("Skip")
                }
            }
        )
    }

    val categoryColor = when (result.whrCategory) {
        WhrCategory.LOW_RISK -> Color(0xFF4CAF50)
        WhrCategory.MODERATE_RISK -> Color(0xFFFFA726)
        WhrCategory.HIGH_RISK -> Color(0xFFF44336)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WHR Results", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main WHR Result Card
            WhrMainResultCard(
                whr = result.whr,
                category = result.whrCategory,
                gender = result.gender,
                categoryColor = categoryColor,
                animationProgress = animationProgress.value
            )

            // Edge case warning
            val edgeCaseMessage = WhrEdgeCaseHandler.getEdgeCaseMessage(result.whr, result.gender)
            edgeCaseMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 16.sp)
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // WHR Visual Scale
            WhrVisualScale(
                whr = result.whr,
                gender = result.gender,
                categoryColor = categoryColor,
                animationProgress = animationProgress.value
            )

            // Body Shape Card
            BodyShapeCard(
                bodyShape = result.bodyShape,
                animationProgress = animationProgress.value
            )

            // Waist Circumference Risk
            WaistCircumferenceRiskCard(
                waistCm = result.waistCm,
                riskLevel = result.waistRiskLevel,
                thresholdIncreased = result.waistThresholdIncreased,
                thresholdHigh = result.waistThresholdHigh,
                gender = result.gender,
                animationProgress = animationProgress.value
            )

            // WHtR Section
            WaistToHeightCard(
                whtr = result.whtr,
                whtrAtRisk = result.whtrAtRisk,
                heightCm = result.heightCm,
                waistCm = result.waistCm,
                onAddHeight = { showHeightDialog = true },
                animationProgress = animationProgress.value
            )

            // Health Risks
            HealthRisksSection(
                risks = result.healthRisks,
                animationProgress = animationProgress.value
            )

            // Measurement Summary
            MeasurementSummaryCard(result = result)

            // Medical Disclaimer
            DisclaimerCard()

            // Action Buttons
            ActionButtonsRow(
                isSaved = isSaved,
                onSave = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSaveToHistory()
                    isSaved = true
                },
                onRecalculate = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRecalculate()
                },
                onShare = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    WhrShareUtils.shareResult(
                        context,
                        WhrShareUtils.buildDetailedShareText(result)
                    )
                }
            )

            // --- ADD: Progress/Trend Button ---
            OutlinedButton(
                onClick = onViewProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    Icons.Outlined.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "View WHR Progress & Trends",
                    fontWeight = FontWeight.SemiBold
                )
            }

            // --- ADD: Advanced Metrics Button ---
            OutlinedButton(
                onClick = onViewAdvancedMetrics,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    Icons.Outlined.Science,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Visceral Fat & Advanced Metrics",
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // --- ADD: Educational Content Link Button ---
            OutlinedButton(
                onClick = onNavigateToEducation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    Icons.Outlined.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Learn About WHR",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WhrMainResultCard(
    whr: Float,
    category: WhrCategory,
    gender: Gender,
    categoryColor: Color,
    animationProgress: Float
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = categoryColor.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Your Waist-to-Hip Ratio",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Animated WHR gauge
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                WhrGaugeCanvas(
                    whr = whr,
                    gender = gender,
                    categoryColor = categoryColor,
                    progress = animationProgress
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.2f", whr * animationProgress),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    Text(
                        "WHR",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Category Chip
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = categoryColor.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Text(
                        category.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
            }

            Text(
                category.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // WHO Thresholds reference
            val thresholds = if (gender == Gender.FEMALE) {
                "Female thresholds — Low: <0.80 | Moderate: 0.80-0.84 | High: ≥0.85"
            } else {
                "Male thresholds — Low: <0.90 | Moderate: 0.90-0.99 | High: ≥1.00"
            }
            Text(
                thresholds,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun WhrGaugeCanvas(
    whr: Float,
    gender: Gender,
    categoryColor: Color,
    progress: Float
) {
    val green = Color(0xFF4CAF50)
    val yellow = Color(0xFFFFA726)
    val red = Color(0xFFF44336)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 14.dp.toPx()
        val padding = strokeWidth / 2 + 8.dp.toPx()
        val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
        val topLeft = Offset(padding, padding)

        val startAngle = 135f
        val totalSweep = 270f

        // Background track
        drawArc(
            color = trackColor,
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Green zone
        drawArc(
            color = green.copy(alpha = 0.3f),
            startAngle = startAngle,
            sweepAngle = totalSweep * 0.45f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Yellow zone
        drawArc(
            color = yellow.copy(alpha = 0.3f),
            startAngle = startAngle + totalSweep * 0.45f,
            sweepAngle = totalSweep * 0.25f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Red zone
        drawArc(
            color = red.copy(alpha = 0.3f),
            startAngle = startAngle + totalSweep * 0.70f,
            sweepAngle = totalSweep * 0.30f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Needle position
        val minWhr = 0.5f
        val maxWhr = if (gender == Gender.FEMALE) 1.1f else 1.2f
        val normalizedWhr = ((whr - minWhr) / (maxWhr - minWhr)).coerceIn(0f, 1f)
        val needleAngle = startAngle + totalSweep * normalizedWhr * progress

        val needleRad = Math.toRadians(needleAngle.toDouble())
        val centerX = topLeft.x + arcSize.width / 2
        val centerY = topLeft.y + arcSize.height / 2
        val needleLength = arcSize.width / 2 - strokeWidth
        val needleEnd = Offset(
            (centerX + needleLength * cos(needleRad)).toFloat(),
            (centerY + needleLength * sin(needleRad)).toFloat()
        )

        // Needle dot
        drawCircle(
            color = categoryColor,
            radius = 10.dp.toPx(),
            center = needleEnd
        )
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = needleEnd
        )

        // Center dot
        drawCircle(
            color = categoryColor.copy(alpha = 0.3f),
            radius = 6.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}

@Composable
private fun WhrVisualScale(
    whr: Float,
    gender: Gender,
    categoryColor: Color,
    animationProgress: Float
) {
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
                "WHO Risk Classification",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            val categories = if (gender == Gender.FEMALE) {
                listOf(
                    Triple("Low Risk", "<0.80", Color(0xFF4CAF50)),
                    Triple("Moderate", "0.80-0.84", Color(0xFFFFA726)),
                    Triple("High Risk", "≥0.85", Color(0xFFF44336))
                )
            } else {
                listOf(
                    Triple("Low Risk", "<0.90", Color(0xFF4CAF50)),
                    Triple("Moderate", "0.90-0.99", Color(0xFFFFA726)),
                    Triple("High Risk", "≥1.00", Color(0xFFF44336))
                )
            }

            // Scale bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    categories.forEach { (_, _, color) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(color.copy(alpha = 0.3f))
                        )
                    }
                }

                // Position indicator
                val minVal = if (gender == Gender.FEMALE) 0.60f else 0.70f
                val maxVal = if (gender == Gender.FEMALE) 1.05f else 1.15f
                val fraction = ((whr - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction * animationProgress)
                        .padding(end = 0.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                categoryColor,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            // Category labels
            Row(modifier = Modifier.fillMaxWidth()) {
                categories.forEach { (label, range, color) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = color,
                            fontSize = 10.sp
                        )
                        Text(
                            range,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Text(
                "Gender: ${if (gender == Gender.FEMALE) "Female" else "Male"} (WHO standards)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun BodyShapeCard(
    bodyShape: BodyShape,
    animationProgress: Float
) {
    val shapeColor = when (bodyShape) {
        BodyShape.APPLE -> Color(0xFFF44336)
        BodyShape.PEAR -> Color(0xFF4CAF50)
        BodyShape.BALANCED -> Color(0xFF2196F3)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = shapeColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use animated body shape icon
            AnimatedBodyShapeIcon(
                bodyShape = bodyShape,
                modifier = Modifier.size(76.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Body Shape",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    bodyShape.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = shapeColor
                )
                Text(
                    bodyShape.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    bodyShape.riskNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun WaistCircumferenceRiskCard(
    waistCm: Float,
    riskLevel: WaistRiskLevel,
    thresholdIncreased: Float,
    thresholdHigh: Float,
    gender: Gender,
    animationProgress: Float
) {
    val riskColor = when (riskLevel) {
        WaistRiskLevel.NORMAL -> Color(0xFF4CAF50)
        WaistRiskLevel.INCREASED -> Color(0xFFFFA726)
        WaistRiskLevel.SUBSTANTIALLY_INCREASED -> Color(0xFFF44336)
    }

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
                    Icons.Outlined.Straighten,
                    contentDescription = null,
                    tint = riskColor,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Waist Circumference Risk",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Current waist value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your waist: ${String.format("%.1f", waistCm)} cm",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = riskColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        riskLevel.label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }
            }

            // Visual bar with thresholds
            WaistRiskBar(
                waistCm = waistCm,
                thresholdIncreased = thresholdIncreased,
                thresholdHigh = thresholdHigh,
                riskColor = riskColor,
                progress = animationProgress
            )

            // Threshold labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Normal",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        fontSize = 10.sp
                    )
                    Text(
                        "<${thresholdIncreased.toInt()} cm",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Increased",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFA726),
                        fontSize = 10.sp
                    )
                    Text(
                        ">${thresholdIncreased.toInt()} cm",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "High",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF44336),
                        fontSize = 10.sp
                    )
                    Text(
                        ">${thresholdHigh.toInt()} cm",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Text(
                "WHO cutoffs for ${if (gender == Gender.FEMALE) "females" else "males"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun WaistRiskBar(
    waistCm: Float,
    thresholdIncreased: Float,
    thresholdHigh: Float,
    riskColor: Color,
    progress: Float
) {
    val minVal = 50f
    val maxVal = thresholdHigh + 30f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            val normalFraction = (thresholdIncreased - minVal) / (maxVal - minVal)
            val increasedFraction = (thresholdHigh - thresholdIncreased) / (maxVal - minVal)
            val highFraction = 1f - normalFraction - increasedFraction

            Box(
                modifier = Modifier
                    .weight(normalFraction)
                    .fillMaxHeight()
                    .background(Color(0xFF4CAF50).copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .weight(increasedFraction)
                    .fillMaxHeight()
                    .background(Color(0xFFFFA726).copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .weight(highFraction)
                    .fillMaxHeight()
                    .background(Color(0xFFF44336).copy(alpha = 0.3f))
            )
        }

        // Position indicator
        val fraction = ((waistCm - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction * progress),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(riskColor, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun WaistToHeightCard(
    whtr: Float?,
    whtrAtRisk: Boolean?,
    heightCm: Float?,
    waistCm: Float,
    onAddHeight: () -> Unit,
    animationProgress: Float
) {
    val whtrColor = when {
        whtrAtRisk == true -> Color(0xFFF44336)
        whtrAtRisk == false -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

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
                    Icons.Outlined.Height,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Waist-to-Height Ratio (WHtR)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (whtr != null && heightCm != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            String.format("%.2f", whtr),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = whtrColor
                        )
                        Text(
                            "Height: ${String.format("%.0f", heightCm)} cm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = whtrColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            if (whtrAtRisk == true) "Increased Risk" else "Normal",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = whtrColor
                        )
                    }
                }

                // WHtR visual
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .background(Color(0xFF4CAF50).copy(alpha = 0.3f))
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxHeight()
                                .background(Color(0xFFF44336).copy(alpha = 0.3f))
                        )
                    }

                    val position = (whtr / 1f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(position * animationProgress),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(whtrColor, RoundedCornerShape(2.dp))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Normal (<0.5)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color(0xFF4CAF50))
                    Text("At Risk (≥0.5)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color(0xFFF44336))
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 16.sp)
                        Text(
                            "Keep your waist to less than half your height for optimal health.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                // No height available
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onAddHeight() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Add your height to calculate WHtR",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Tap to enter height or add it in your profile",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthRisksSection(
    risks: List<HealthRiskItem>,
    animationProgress: Float
) {
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
                    Icons.Outlined.MonitorHeart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Health Risk Assessment",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            risks.forEach { risk ->
                HealthRiskRow(risk = risk)
            }
        }
    }
}

@Composable
private fun HealthRiskRow(risk: HealthRiskItem) {
    val riskColor = when (risk.severity) {
        RiskSeverity.MILD -> Color(0xFF4CAF50)
        RiskSeverity.MODERATE -> Color(0xFFFFA726)
        RiskSeverity.HIGH -> Color(0xFFF44336)
        RiskSeverity.SEVERE -> Color(0xFFB71C1C)
    }

    val riskLabel = when (risk.severity) {
        RiskSeverity.MILD -> "Low"
        RiskSeverity.MODERATE -> "Moderate"
        RiskSeverity.HIGH -> "High"
        RiskSeverity.SEVERE -> "Very High"
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = riskColor.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(risk.icon, fontSize = 20.sp)
                    Text(
                        risk.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = riskColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            riskLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                    }
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    risk.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MeasurementSummaryCard(result: WhrResult) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Measurement Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SummaryRow("Waist", "${String.format("%.1f", result.waistCm)} cm")
            SummaryRow("Hip", "${String.format("%.1f", result.hipCm)} cm")
            SummaryRow("WHR", String.format("%.2f", result.whr))
            result.whtr?.let {
                SummaryRow("WHtR", String.format("%.2f", it))
            }
            SummaryRow("Gender", if (result.gender == Gender.FEMALE) "Female" else "Male")
            SummaryRow("Age", "${result.age} years")
            SummaryRow("Body Shape", result.bodyShape.label)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DisclaimerCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                "This tool is for informational purposes only and is not a substitute for professional medical advice, diagnosis, or treatment. Always consult a qualified healthcare provider.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                lineHeight = 16.sp,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    isSaved: Boolean,
    onSave: () -> Unit,
    onRecalculate: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !isSaved,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSaved)
                    Color(0xFF4CAF50)
                else
                    MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.8f),
                disabledContentColor = Color.White
            )
        ) {
            Icon(
                if (isSaved) Icons.Filled.Check else Icons.Outlined.Save,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (isSaved) "Saved to History" else "Save to History",
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recalculate
            OutlinedButton(
                onClick = onRecalculate,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recalculate", fontSize = 13.sp)
            }

            // Share
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share", fontSize = 13.sp)
            }
        }
    }
}

private fun buildShareText(result: WhrResult): String {
    return buildString {
        appendLine("📐 Waist-to-Hip Ratio Result")
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        appendLine("WHR: ${String.format("%.2f", result.whr)} — ${result.whrCategory.label}")
        appendLine("Waist: ${String.format("%.1f", result.waistCm)} cm")
        appendLine("Hip: ${String.format("%.1f", result.hipCm)} cm")
        appendLine("Body Shape: ${result.bodyShape.emoji} ${result.bodyShape.label}")
        result.whtr?.let {
            appendLine("WHtR: ${String.format("%.2f", it)} — ${if (result.whtrAtRisk == true) "At Risk" else "Normal"}")
        }
        appendLine("Waist Risk: ${result.waistRiskLevel.label}")
        appendLine("━━━━━━━━━━━━━━━━━━━━")
        appendLine("Calculated using Health Calculator: BMI Tracker")
    }
}
