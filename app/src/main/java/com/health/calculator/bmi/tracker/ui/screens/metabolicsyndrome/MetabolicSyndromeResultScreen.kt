package com.health.calculator.bmi.tracker.ui.screens.metabolicsyndrome

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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.calculator.Ethnicity
import com.health.calculator.bmi.tracker.calculator.MetabolicCriterion
import com.health.calculator.bmi.tracker.calculator.MetabolicRiskLevel
import com.health.calculator.bmi.tracker.calculator.MetabolicSyndromeResult
import com.health.calculator.bmi.tracker.calculator.MultiStandardComparison
import com.health.calculator.bmi.tracker.ui.theme.*

@Composable
fun MetabolicSyndromeResultScreen(
    result: MetabolicSyndromeResult,
    isSaved: Boolean,
    standardsComparison: MultiStandardComparison?,
    onSave: () -> Unit,
    onRecalculate: () -> Unit,
    onShare: () -> Unit,
    onEthnicityChange: (Ethnicity) -> Unit,
    onNavigateToCalculator: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(result) {
        animationStarted = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Animated values
    val riskColor = animateRiskColor(criteriaMet = result.criteriaMet)
    val gaugeProgress = animateGaugeProgress(targetProgress = result.criteriaMet / 5f)
    val animatedCount = animateCountIncrement(targetCount = result.criteriaMet)
    val criterionVisibilities = rememberCriterionAnimStates(
        count = result.criteria.size,
        trigger = animationStarted
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // === Animated Risk Gauge ===
        RiskGauge(
            criteriaMet = animatedCount,
            animationProgress = gaugeProgress,
            riskColor = riskColor,
            isSyndromePresent = result.isSyndromePresent
        )

        Spacer(modifier = Modifier.height(20.dp))

        // === Diagnosis Card ===
        DiagnosisCard(result = result, animationStarted = animationStarted, riskColor = riskColor)

        Spacer(modifier = Modifier.height(16.dp))

        // === Criteria Cards ===
        Text(
            text = "Criteria Assessment",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        result.criteria.forEachIndexed { index, criterion ->
            AnimatedVisibility(
                visible = criterionVisibilities.getOrElse(index) { false },
                enter = slideInHorizontally(
                    initialOffsetX = { if (index % 2 == 0) -it else it },
                    animationSpec = tween(400)
                ) + fadeIn(tween(400))
            ) {
                CriterionCard(criterion = criterion)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Standards Comparison ===
        if (standardsComparison != null) {
            StandardsComparisonSection(
                comparison = standardsComparison,
                onEthnicityChange = onEthnicityChange
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // === Risk Message ===
        RiskMessageCard(riskLevel = result.riskLevel, criteriaMet = result.criteriaMet)

        Spacer(modifier = Modifier.height(16.dp))

        // === Detailed Recommendations (expandable) ===
        var showRecommendations by remember { mutableStateOf(false) }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showRecommendations = !showRecommendations }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Detailed Analysis & Recommendations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Tap to ${if (showRecommendations) "collapse" else "view health insights"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(if (showRecommendations) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        AnimatedVisibility(
            visible = showRecommendations,
            enter = expandVertically(tween(400)) + fadeIn(tween(400)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(200))
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                DetailedRecommendationsSection(result = result)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Cross Links ===
        CrossLinksSection(onNavigate = onNavigateToCalculator)

        Spacer(modifier = Modifier.height(24.dp))

        // === Action Buttons ===
        ActionButtons(isSaved = isSaved, onSave = onSave, onRecalculate = onRecalculate, onShare = onShare)

        Spacer(modifier = Modifier.height(16.dp))

        // === Disclaimer ===
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "This assessment is for educational purposes only. It does not replace professional medical diagnosis. Please consult your healthcare provider for proper evaluation and treatment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun RiskGauge(
    criteriaMet: Int,
    animationProgress: Float,
    riskColor: Color,
    isSyndromePresent: Boolean
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 20.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = 135f, sweepAngle = 270f, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val segmentAngle = 270f / 5f
            val segColors = listOf(HealthGreen, HealthYellow, HealthOrange, HealthRed, Color(0xFFB71C1C))
            for (i in 0 until 5) {
                drawArc(
                    color = segColors[i].copy(alpha = 0.15f),
                    startAngle = 135f + (i * segmentAngle),
                    sweepAngle = segmentAngle - 2f, useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            drawArc(
                color = riskColor,
                startAngle = 135f,
                sweepAngle = animationProgress * 270f,
                useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$criteriaMet/5",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = riskColor
            )
            Text(
                text = "Criteria Met",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DiagnosisCard(result: MetabolicSyndromeResult, animationStarted: Boolean, riskColor: Color) {
    val diagnosisColor = if (result.isSyndromePresent) Color(0xFFB71C1C) else HealthGreen
    val diagnosisIcon = if (result.isSyndromePresent) Icons.Filled.Warning else Icons.Filled.CheckCircle

    AnimatedVisibility(
        visible = animationStarted,
        enter = scaleIn(tween(500, delayMillis = 600)) + fadeIn(tween(500, delayMillis = 600))
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = diagnosisColor.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(diagnosisColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(diagnosisIcon, contentDescription = null, tint = diagnosisColor, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (result.isSyndromePresent) "Metabolic Syndrome Present" else "Metabolic Syndrome Not Present",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = diagnosisColor
                    )
                    Text(
                        text = "Based on ATP III criteria (${result.criteriaMet} of 5 met)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CriterionCard(criterion: MetabolicCriterion) {
    val statusColor = if (criterion.isMet) HealthRed else HealthGreen
    val statusIcon = if (criterion.isMet) Icons.Filled.Warning else Icons.Filled.CheckCircle

    val bgAlpha by animateFloatAsState(
        targetValue = 0.06f,
        animationSpec = tween(600),
        label = "criterion_bg"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = bgAlpha)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(criterion.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    if (criterion.isOnMedication) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                            Text("💊 Medicated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Your value: ${criterion.userValue}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Threshold: ${criterion.threshold}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                Text(
                    text = if (criterion.isMet) "Abnormal" else "Normal",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun RiskMessageCard(riskLevel: MetabolicRiskLevel, criteriaMet: Int) {
    val (icon, message) = when (riskLevel) {
        MetabolicRiskLevel.NONE -> Pair("🎉", "Excellent! No metabolic syndrome indicators detected. Continue maintaining your healthy lifestyle.")
        MetabolicRiskLevel.LOW -> Pair("👀", "You have 1 risk factor. While metabolic syndrome is not present, monitor this factor and maintain healthy habits.")
        MetabolicRiskLevel.MODERATE -> Pair("⚠️", "You have 2 risk factors. You're at borderline risk. Focus on lifestyle improvements and schedule a check-up.")
        MetabolicRiskLevel.HIGH -> Pair("🔴", "Metabolic syndrome is present with $criteriaMet criteria met. Please consult your healthcare provider for evaluation.")
        MetabolicRiskLevel.VERY_HIGH -> Pair("🚨", "Metabolic syndrome is present with $criteriaMet of 5 criteria met. Seek medical attention promptly.")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$icon ${riskLevel.label}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun ActionButtons(isSaved: Boolean, onSave: () -> Unit, onRecalculate: () -> Unit, onShare: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onSave() },
            enabled = !isSaved,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(if (isSaved) Icons.Filled.Check else Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isSaved) "Saved" else "Save")
        }
        OutlinedButton(
            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onRecalculate() },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Recalculate")
        }
        OutlinedButton(
            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onShare() },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Share")
        }
    }
}
