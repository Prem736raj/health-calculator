// ui/screens/bloodpressure/BpAdvancedMetricsComponents.kt
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*

// ─── Main Advanced Metrics Section ─────────────────────────────────────────────

@Composable
fun BpAdvancedMetricsSection(
    systolic: Int,
    diastolic: Int,
    pulse: Int?
) {
    val ppAnalysis = remember(systolic, diastolic) {
        BpAdvancedMetrics.analyzePulsePressure(systolic, diastolic)
    }
    val mapAnalysis = remember(systolic, diastolic) {
        BpAdvancedMetrics.analyzeMAP(systolic, diastolic)
    }
    val hrAnalysis = remember(pulse) {
        pulse?.let { BpAdvancedMetrics.analyzeHeartRate(it) }
    }

    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { 80 }) + fadeIn(
            animationSpec = tween(600, delayMillis = 100)
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    "  DETAILED ANALYSIS  ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }

            // Pulse Pressure Card
            PulsePressureCard(analysis = ppAnalysis)

            // MAP Card
            MapCard(analysis = mapAnalysis)

            // Heart Rate Card
            hrAnalysis?.let { hr ->
                HeartRateCard(analysis = hr)
            }
        }
    }
}

// ─── Pulse Pressure Card ───────────────────────────────────────────────────────

@Composable
private fun PulsePressureCard(analysis: PulsePressureAnalysis) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val statusColor = when (analysis.category) {
        PpCategory.VERY_NARROW -> Color(0xFF42A5F5)
        PpCategory.NARROW -> Color(0xFF29B6F6)
        PpCategory.NORMAL -> Color(0xFF4CAF50)
        PpCategory.SLIGHTLY_WIDE -> Color(0xFFFFC107)
        PpCategory.WIDE -> Color(0xFFFF9800)
        PpCategory.VERY_WIDE -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF7E57C2).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.CompareArrows,
                            contentDescription = null,
                            tint = Color(0xFF7E57C2),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            "Pulse Pressure",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Systolic − Diastolic",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${analysis.value}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        "mmHg",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor.copy(alpha = 0.6f)
                    )
                }
            }

            // Category badge
            Card(
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (analysis.isNormal) Icons.Filled.CheckCircle else Icons.Outlined.Info,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        analysis.interpretation,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            // Visual Scale
            MetricHorizontalScale(
                position = analysis.normalizedPosition,
                color = statusColor,
                zones = listOf(
                    ScaleZone("Very\nNarrow", Color(0xFF42A5F5), 0f, 0.15f),
                    ScaleZone("Narrow", Color(0xFF29B6F6), 0.15f, 0.30f),
                    ScaleZone("Normal", Color(0xFF4CAF50), 0.30f, 0.55f),
                    ScaleZone("Slightly\nWide", Color(0xFFFFC107), 0.55f, 0.70f),
                    ScaleZone("Wide", Color(0xFFFF9800), 0.70f, 0.85f),
                    ScaleZone("Very\nWide", Color(0xFFF44336), 0.85f, 1f)
                ),
                valueLabel = "${analysis.value} mmHg"
            )

            // Expand for details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = !isExpanded
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isExpanded) "Hide Details" else "Learn More",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(300), label = "pp_chevron"
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = rotation },
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Text(
                        analysis.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )

                    // Reference ranges
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Reference Ranges:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            PpCategory.entries.forEach { cat ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (cat == analysis.category) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (cat) {
                                                    PpCategory.VERY_NARROW -> Color(0xFF42A5F5)
                                                    PpCategory.NARROW -> Color(0xFF29B6F6)
                                                    PpCategory.NORMAL -> Color(0xFF4CAF50)
                                                    PpCategory.SLIGHTLY_WIDE -> Color(0xFFFFC107)
                                                    PpCategory.WIDE -> Color(0xFFFF9800)
                                                    PpCategory.VERY_WIDE -> Color(0xFFF44336)
                                                }
                                            )
                                    )
                                    Text(
                                        "${cat.displayName}: ${cat.rangeLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (cat == analysis.category) FontWeight.Bold else FontWeight.Normal,
                                        color = if (cat == analysis.category) statusColor
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── MAP Card ──────────────────────────────────────────────────────────────────

@Composable
private fun MapCard(analysis: MapAnalysis) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val statusColor = when (analysis.category) {
        MapCategory.CRITICALLY_LOW -> Color(0xFFF44336)
        MapCategory.LOW -> Color(0xFFFF9800)
        MapCategory.NORMAL -> Color(0xFF4CAF50)
        MapCategory.ELEVATED -> Color(0xFFFFC107)
        MapCategory.HIGH -> Color(0xFFFF7043)
        MapCategory.VERY_HIGH -> Color(0xFFD32F2F)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF00897B).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Speed,
                            contentDescription = null,
                            tint = Color(0xFF00897B),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text("Mean Arterial Pressure", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("DIA + ⅓(SYS − DIA)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${analysis.value}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text("mmHg", style = MaterialTheme.typography.labelSmall, color = statusColor.copy(alpha = 0.6f))
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (analysis.isNormal) Icons.Filled.CheckCircle else Icons.Outlined.Warning,
                        contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp)
                    )
                    Text(analysis.interpretation, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = statusColor)
                }
            }

            MetricHorizontalScale(
                position = analysis.normalizedPosition,
                color = statusColor,
                zones = listOf(
                    ScaleZone("Critical", Color(0xFFF44336), 0f, 0.17f),
                    ScaleZone("Low", Color(0xFFFF9800), 0.17f, 0.30f),
                    ScaleZone("Normal", Color(0xFF4CAF50), 0.30f, 0.60f),
                    ScaleZone("Elevated", Color(0xFFFFC107), 0.60f, 0.72f),
                    ScaleZone("High", Color(0xFFFF7043), 0.72f, 0.86f),
                    ScaleZone("V.High", Color(0xFFD32F2F), 0.86f, 1f)
                ),
                valueLabel = "${analysis.value} mmHg"
            )

            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isExpanded = !isExpanded
                },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isExpanded) "Hide Details" else "Learn More",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium
                )
                val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, tween(300), label = "map_chev")
                Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(18.dp).graphicsLayer { rotationZ = rotation }, tint = MaterialTheme.colorScheme.primary)
            }

            AnimatedVisibility(isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Text(analysis.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Reference Ranges:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            MapCategory.entries.forEach { cat ->
                                val catColor = when (cat) {
                                    MapCategory.CRITICALLY_LOW -> Color(0xFFF44336)
                                    MapCategory.LOW -> Color(0xFFFF9800)
                                    MapCategory.NORMAL -> Color(0xFF4CAF50)
                                    MapCategory.ELEVATED -> Color(0xFFFFC107)
                                    MapCategory.HIGH -> Color(0xFFFF7043)
                                    MapCategory.VERY_HIGH -> Color(0xFFD32F2F)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(if (cat == analysis.category) 8.dp else 6.dp).clip(CircleShape).background(catColor))
                                    Text(
                                        "${cat.displayName}: ${cat.rangeLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (cat == analysis.category) FontWeight.Bold else FontWeight.Normal,
                                        color = if (cat == analysis.category) statusColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Heart Rate Card ───────────────────────────────────────────────────────────

@Composable
private fun HeartRateCard(analysis: HeartRateAnalysis) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val statusColor = when (analysis.category) {
        HrCategory.SEVERELY_LOW -> Color(0xFFD32F2F)
        HrCategory.BRADYCARDIA -> Color(0xFFFF9800)
        HrCategory.ATHLETIC -> Color(0xFF2196F3)
        HrCategory.NORMAL -> Color(0xFF4CAF50)
        HrCategory.ELEVATED -> Color(0xFFFFC107)
        HrCategory.TACHYCARDIA -> Color(0xFFFF7043)
        HrCategory.DANGEROUS -> Color(0xFFB71C1C)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hr_heartbeat")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween((60000 / analysis.bpm.coerceIn(30, 200))),
            repeatMode = RepeatMode.Reverse
        ), label = "heart_scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE91E63).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Favorite, contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(22.dp).graphicsLayer { scaleX = heartScale; scaleY = heartScale }
                        )
                    }
                    Column {
                        Text("Heart Rate Analysis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Resting heart rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${analysis.bpm}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = statusColor)
                    Text("BPM", style = MaterialTheme.typography.labelSmall, color = statusColor.copy(alpha = 0.6f))
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (analysis.isNormal) Icons.Filled.CheckCircle else Icons.Outlined.Info,
                        null, tint = statusColor, modifier = Modifier.size(14.dp)
                    )
                    Text("${analysis.category.displayName}: ${analysis.interpretation}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = statusColor)
                }
            }

            MetricHorizontalScale(
                position = analysis.normalizedPosition,
                color = statusColor,
                zones = listOf(
                    ScaleZone("Very\nLow", Color(0xFFD32F2F), 0f, 0.10f),
                    ScaleZone("Brady", Color(0xFFFF9800), 0.10f, 0.28f),
                    ScaleZone("Normal", Color(0xFF4CAF50), 0.28f, 0.63f),
                    ScaleZone("Elevated", Color(0xFFFFC107), 0.63f, 0.75f),
                    ScaleZone("Tachy", Color(0xFFFF7043), 0.75f, 0.88f),
                    ScaleZone("Danger", Color(0xFFB71C1C), 0.88f, 1f)
                ),
                valueLabel = "${analysis.bpm} BPM"
            )

            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isExpanded = !isExpanded
                },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isExpanded) "Hide Details" else "Learn More", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, tween(300), label = "hr_chev")
                Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(18.dp).graphicsLayer { rotationZ = rotation }, tint = MaterialTheme.colorScheme.primary)
            }

            AnimatedVisibility(isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Text(analysis.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))

                    if (analysis.riskFactors.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Key Points:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                analysis.riskFactors.forEach { factor ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                                        Box(Modifier.size(5.dp).clip(CircleShape).background(statusColor).offset(y = 5.dp))
                                        Text(factor, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Reference Ranges:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            HrCategory.entries.filter { it != HrCategory.ATHLETIC }.forEach { cat ->
                                val catColor = when (cat) {
                                    HrCategory.SEVERELY_LOW -> Color(0xFFD32F2F)
                                    HrCategory.BRADYCARDIA -> Color(0xFFFF9800)
                                    HrCategory.ATHLETIC -> Color(0xFF2196F3)
                                    HrCategory.NORMAL -> Color(0xFF4CAF50)
                                    HrCategory.ELEVATED -> Color(0xFFFFC107)
                                    HrCategory.TACHYCARDIA -> Color(0xFFFF7043)
                                    HrCategory.DANGEROUS -> Color(0xFFB71C1C)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(if (cat == analysis.category) 8.dp else 6.dp).clip(CircleShape).background(catColor))
                                    Text(
                                        "${cat.displayName}: ${cat.rangeLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (cat == analysis.category) FontWeight.Bold else FontWeight.Normal,
                                        color = if (cat == analysis.category) statusColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Shared Horizontal Scale Component ─────────────────────────────────────────

data class ScaleZone(
    val label: String,
    val color: Color,
    val startFraction: Float,
    val endFraction: Float
)

@Composable
private fun MetricHorizontalScale(
    position: Float,
    color: Color,
    zones: List<ScaleZone>,
    valueLabel: String
) {
    val animatedPosition by animateFloatAsState(
        targetValue = position,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale_pos"
    )

    val density = LocalDensity.current
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            val barHeight = 14.dp.toPx()
            val barY = 6.dp.toPx()
            val barRadius = 7.dp.toPx()

            // Draw zones
            zones.forEach { zone ->
                val left = size.width * zone.startFraction
                val right = size.width * zone.endFraction

                drawRoundRect(
                    color = zone.color.copy(alpha = 0.35f),
                    topLeft = Offset(left, barY),
                    size = Size(right - left, barHeight),
                    cornerRadius = CornerRadius(
                        if (zone == zones.first()) barRadius else 0f,
                        if (zone == zones.last()) barRadius else 0f
                    )
                )
            }

            // Indicator
            val indicatorX = size.width * animatedPosition
            val indicatorRadius = 10.dp.toPx()
            val indicatorY = barY + barHeight / 2

            // Shadow
            drawCircle(Color.Black.copy(alpha = 0.12f), indicatorRadius + 2.dp.toPx(), Offset(indicatorX + 1, indicatorY + 1))
            // White border
            drawCircle(Color.White, indicatorRadius + 1.5.dp.toPx(), Offset(indicatorX, indicatorY))
            // Fill
            drawCircle(color, indicatorRadius, Offset(indicatorX, indicatorY))
            // Inner dot
            drawCircle(Color.White, 3.dp.toPx(), Offset(indicatorX, indicatorY))

            // Value label above
            val labelPaint = android.graphics.Paint().apply {
                this.color = textColor.hashCode()
                textSize = with(density) { 10.sp.toPx() }
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                isAntiAlias = true
            }
        }

        // Zone labels below
        Row(modifier = Modifier.fillMaxWidth()) {
            zones.forEach { zone ->
                Box(
                    modifier = Modifier.weight(zone.endFraction - zone.startFraction),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        zone.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = zone.color.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 10.sp
                    )
                }
            }
        }
    }
}
