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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhrAdvancedMetricsScreen(
    whrResult: WhrResult,
    visceralFat: VisceralFatAssessment,
    abdominalObesity: AbdominalObesityResult,
    combinedRisk: CombinedRiskSummary,
    improvementTips: List<ImprovementTip>,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, tween(900, easing = EaseOutCubic))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Metrics", fontWeight = FontWeight.Bold) },
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
            // Combined Risk Summary
            CombinedRiskSummaryCard(
                summary = combinedRisk,
                animProgress = animProgress.value
            )

            // Visceral Fat Section
            VisceralFatCard(
                assessment = visceralFat,
                animProgress = animProgress.value
            )

            // Visceral vs Subcutaneous Diagram
            FatTypeDiagramCard()

            // Abdominal Obesity
            AbdominalObesityCard(
                result = abdominalObesity,
                animProgress = animProgress.value
            )

            // Risk Factor Breakdown
            RiskFactorBreakdownCard(
                summary = combinedRisk,
                whrResult = whrResult
            )

            // Improvement Tips
            ImprovementTipsSection(tips = improvementTips)

            // Disclaimer
            DisclaimerNote()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CombinedRiskSummaryCard(
    summary: CombinedRiskSummary,
    animProgress: Float
) {
    val riskColor = when (summary.overallRisk) {
        OverallCentralRisk.LOW -> Color(0xFF4CAF50)
        OverallCentralRisk.MODERATE -> Color(0xFFFFA726)
        OverallCentralRisk.HIGH -> Color(0xFFF44336)
        OverallCentralRisk.VERY_HIGH -> Color(0xFFB71C1C)
    }

    val riskEmoji = when (summary.overallRisk) {
        OverallCentralRisk.LOW -> "✅"
        OverallCentralRisk.MODERATE -> "⚠️"
        OverallCentralRisk.HIGH -> "🔴"
        OverallCentralRisk.VERY_HIGH -> "🚨"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = riskColor.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Central Obesity Risk Summary",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Surface(
                shape = CircleShape,
                color = riskColor.copy(alpha = 0.12f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(riskEmoji, fontSize = 36.sp)
                }
            }

            Text(
                summary.overallRisk.label,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = riskColor
            )

            Text(
                summary.overallRisk.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )

            // Risk factor count bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = riskColor
                    )
                    Text(
                        "${summary.riskFactorCount} of ${summary.totalFactors} indicators show concern",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun VisceralFatCard(
    assessment: VisceralFatAssessment,
    animProgress: Float
) {
    val riskColor = when (assessment.riskLevel) {
        VisceralFatRisk.LOW -> Color(0xFF4CAF50)
        VisceralFatRisk.MODERATE -> Color(0xFFFFA726)
        VisceralFatRisk.HIGH -> Color(0xFFF44336)
        VisceralFatRisk.VERY_HIGH -> Color(0xFFB71C1C)
    }

    val animatedLevel = remember { Animatable(0f) }
    LaunchedEffect(assessment.estimatedLevel) {
        animatedLevel.animateTo(
            assessment.estimatedLevel.toFloat(),
            tween(1000, easing = EaseOutCubic)
        )
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    tint = riskColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Visceral Fat Estimation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Level display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "Estimated Level",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "${animatedLevel.value.toInt()}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                    Text(
                        "out of 20",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = riskColor.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            assessment.riskLevel.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                        Text(
                            "Level ${assessment.riskLevel.levelRange}",
                            style = MaterialTheme.typography.bodySmall,
                            color = riskColor.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Visual level bar
            VisceralFatLevelBar(
                level = assessment.estimatedLevel,
                riskColor = riskColor,
                animProgress = animProgress
            )

            // Description
            Text(
                assessment.riskLevel.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                lineHeight = 18.sp
            )

            // What is visceral fat
            var expanded by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ℹ️", fontSize = 14.sp)
                            Text(
                                "What is visceral fat?",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Visceral fat is the fat stored deep inside your abdomen, surrounding vital organs like the liver, stomach, and intestines.",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                            Text(
                                "Unlike subcutaneous fat (under the skin), visceral fat is metabolically active and releases inflammatory substances that increase risk of:",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                            val risks = listOf(
                                "Heart disease and stroke",
                                "Type 2 diabetes",
                                "High blood pressure",
                                "Certain cancers (breast, colon)",
                                "Alzheimer's disease",
                                "Metabolic syndrome"
                            )
                            risks.forEach { risk ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        risk,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Estimation note
            Text(
                "⚠️ This is an estimation based on waist circumference, age, and gender. Accurate measurement requires medical imaging (DEXA or CT scan).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun VisceralFatLevelBar(
    level: Int,
    riskColor: Color,
    animProgress: Float
) {
    val green = Color(0xFF4CAF50)
    val yellow = Color(0xFFFFA726)
    val red = Color(0xFFF44336)
    val darkRed = Color(0xFFB71C1C)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            // Background zones
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .weight(9f)
                        .fillMaxHeight()
                        .background(green.copy(alpha = 0.2f))
                )
                Box(
                    Modifier
                        .weight(5f)
                        .fillMaxHeight()
                        .background(yellow.copy(alpha = 0.2f))
                )
                Box(
                    Modifier
                        .weight(3f)
                        .fillMaxHeight()
                        .background(red.copy(alpha = 0.2f))
                )
                Box(
                    Modifier
                        .weight(3f)
                        .fillMaxHeight()
                        .background(darkRed.copy(alpha = 0.2f))
                )
            }

            // Position indicator
            val fraction = (level / 20f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction * animProgress),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 20.dp)
                        .background(riskColor, RoundedCornerShape(3.dp))
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text("Low (1-9)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp,
                color = green)
            Text("Mod (10-14)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp,
                color = yellow)
            Text("High (15+)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp,
                color = red)
            Text("20", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun FatTypeDiagramCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Visceral vs Subcutaneous Fat",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Cross-section diagram
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                FatCrossSectionDiagram()
            }

            // Comparison table
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Subcutaneous
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2196F3).copy(alpha = 0.5f))
                            )
                            Text(
                                "Subcutaneous",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }
                        Text("Under the skin", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                        Text("Pinchable fat", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                        Text("Lower health risk", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                        Text("Energy storage", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                    }
                }

                // Visceral
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF44336).copy(alpha = 0.5f))
                            )
                            Text(
                                "Visceral",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                        }
                        Text("Around organs", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                        Text("Deep belly fat", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                        Text("Higher health risk", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                        Text("Releases toxins", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FatCrossSectionDiagram() {
    val skinColor = Color(0xFFFFCC80)
    val subcutColor = Color(0xFF2196F3).copy(alpha = 0.35f)
    val visceralColor = Color(0xFFF44336).copy(alpha = 0.35f)
    val muscleColor = Color(0xFF8D6E63).copy(alpha = 0.25f)
    val organColor = Color(0xFF795548).copy(alpha = 0.3f)
    val textColor = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = Modifier
            .width(260.dp)
            .height(160.dp)
    ) {
        val cx = size.width / 2
        val cy = size.height / 2

        // Outer skin layer
        drawOval(
            color = skinColor.copy(alpha = 0.3f),
            topLeft = Offset(cx - 120f, cy - 70f),
            size = Size(240f, 140f)
        )
        drawOval(
            color = skinColor,
            topLeft = Offset(cx - 120f, cy - 70f),
            size = Size(240f, 140f),
            style = Stroke(width = 3f)
        )

        // Subcutaneous fat layer
        drawOval(
            color = subcutColor,
            topLeft = Offset(cx - 95f, cy - 52f),
            size = Size(190f, 104f)
        )

        // Muscle wall
        drawOval(
            color = muscleColor,
            topLeft = Offset(cx - 72f, cy - 38f),
            size = Size(144f, 76f)
        )
        drawOval(
            color = Color(0xFF8D6E63),
            topLeft = Offset(cx - 72f, cy - 38f),
            size = Size(144f, 76f),
            style = Stroke(width = 1.5f)
        )

        // Visceral fat (around organs)
        drawOval(
            color = visceralColor,
            topLeft = Offset(cx - 55f, cy - 28f),
            size = Size(110f, 56f)
        )

        // Organs representation
        drawRoundRect(
            color = organColor,
            topLeft = Offset(cx - 30f, cy - 18f),
            size = Size(28f, 36f),
            cornerRadius = CornerRadius(6f)
        )
        drawRoundRect(
            color = organColor,
            topLeft = Offset(cx + 5f, cy - 14f),
            size = Size(24f, 28f),
            cornerRadius = CornerRadius(6f)
        )
        drawCircle(
            color = organColor,
            radius = 10f,
            center = Offset(cx - 10f, cy + 8f)
        )

        // Labels with lines
        // Skin label
        drawLine(
            color = textColor.copy(alpha = 0.6f),
            start = Offset(cx + 115f, cy - 60f),
            end = Offset(cx + 140f, cy - 75f),
            strokeWidth = 1f
        )

        // Subcutaneous label
        drawLine(
            color = Color(0xFF2196F3),
            start = Offset(cx + 90f, cy - 35f),
            end = Offset(cx + 140f, cy - 45f),
            strokeWidth = 1.5f
        )

        // Visceral label
        drawLine(
            color = Color(0xFFF44336),
            start = Offset(cx + 50f, cy - 10f),
            end = Offset(cx + 140f, cy - 10f),
            strokeWidth = 1.5f
        )

        // Organ label
        drawLine(
            color = textColor.copy(alpha = 0.5f),
            start = Offset(cx + 30f, cy + 5f),
            end = Offset(cx + 140f, cy + 20f),
            strokeWidth = 1f
        )
    }
}

@Composable
private fun AbdominalObesityCard(
    result: AbdominalObesityResult,
    animProgress: Float
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Rule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Abdominal Obesity Classification",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // WHO Classification
            ClassificationRow(
                standard = "WHO",
                fullName = "World Health Organization",
                threshold = result.whoThreshold,
                waistCm = result.waistCm,
                classification = result.whoClassification,
                gender = result.gender,
                animProgress = animProgress
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // IDF Classification
            ClassificationRow(
                standard = "IDF",
                fullName = "International Diabetes Federation",
                threshold = result.idfThreshold,
                waistCm = result.waistCm,
                classification = result.idfClassification,
                gender = result.gender,
                animProgress = animProgress
            )

            // Note
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("📋", fontSize = 12.sp)
                    Text(
                        "IDF uses stricter cutoffs than WHO. The IDF standard is recommended for Asian populations and is increasingly adopted worldwide.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassificationRow(
    standard: String,
    fullName: String,
    threshold: Float,
    waistCm: Float,
    classification: AbdominalObesityClass,
    gender: Gender,
    animProgress: Float
) {
    val classColor = when (classification) {
        AbdominalObesityClass.NORMAL -> Color(0xFF4CAF50)
        AbdominalObesityClass.ELEVATED -> Color(0xFFFFA726)
        AbdominalObesityClass.OBESE -> Color(0xFFF44336)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    standard,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    fullName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = classColor.copy(alpha = 0.12f)
            ) {
                Text(
                    classification.label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = classColor
                )
            }
        }

        // Visual bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                val maxDisplay = threshold * 1.4f
                val normalFrac = (threshold / maxDisplay).coerceIn(0f, 1f)

                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .weight(normalFrac)
                            .fillMaxHeight()
                            .background(Color(0xFF4CAF50).copy(alpha = 0.25f))
                    )
                    Box(
                        Modifier
                            .weight(1f - normalFrac)
                            .fillMaxHeight()
                            .background(Color(0xFFF44336).copy(alpha = 0.25f))
                    )
                }

                val position = (waistCm / maxDisplay).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(position * animProgress),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        Modifier
                            .size(width = 4.dp, height = 14.dp)
                            .background(classColor, RoundedCornerShape(2.dp))
                    )
                }
            }

            Text(
                "${waistCm.toInt()}cm",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = classColor,
                modifier = Modifier.width(40.dp)
            )
        }

        Text(
            "Threshold: ${threshold.toInt()} cm for ${if (gender == Gender.FEMALE) "women" else "men"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RiskFactorBreakdownCard(
    summary: CombinedRiskSummary,
    whrResult: WhrResult
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Risk Factor Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            RiskFactorRow("WHR", summary.whrRisk, whrResult.whrCategory.label)
            RiskFactorRow("Waist Circumference", summary.waistRisk, whrResult.waistRiskLevel.label)
            if (summary.whtrRisk >= 0) {
                RiskFactorRow(
                    "Waist-to-Height",
                    summary.whtrRisk,
                    if (whrResult.whtrAtRisk == true) "At Risk" else "Normal"
                )
            }
            RiskFactorRow("Visceral Fat", summary.visceralRisk, "Level ${
                when (summary.visceralRisk) {
                    0 -> "Low"
                    1 -> "Moderate"
                    2 -> "High"
                    else -> "Very High"
                }
            }")
        }
    }
}

@Composable
private fun RiskFactorRow(
    label: String,
    riskLevel: Int,
    statusLabel: String
) {
    val color = when (riskLevel) {
        0 -> Color(0xFF4CAF50)
        1 -> Color(0xFFFFA726)
        2 -> Color(0xFFF44336)
        else -> Color(0xFFB71C1C)
    }

    val icon = when (riskLevel) {
        0 -> Icons.Filled.CheckCircle
        1 -> Icons.Filled.Warning
        else -> Icons.Filled.Error
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color.copy(alpha = 0.06f),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            statusLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ImprovementTipsSection(tips: List<ImprovementTip>) {
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
                    Icons.Outlined.TipsAndUpdates,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Personalized Recommendations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Group tips by category
            val grouped = tips.groupBy { it.category }

            grouped.forEach { (category, categoryTips) ->
                val categoryLabel = when (category) {
                    TipCategory.EXERCISE -> "🏃 Exercise"
                    TipCategory.DIET -> "🥗 Diet"
                    TipCategory.LIFESTYLE -> "🧘 Lifestyle"
                    TipCategory.MEDICAL -> "👨‍⚕️ Medical"
                }

                val categoryColor = when (category) {
                    TipCategory.EXERCISE -> Color(0xFF4CAF50)
                    TipCategory.DIET -> Color(0xFFFFA726)
                    TipCategory.LIFESTYLE -> Color(0xFF2196F3)
                    TipCategory.MEDICAL -> Color(0xFFF44336)
                }

                Text(
                    categoryLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )

                categoryTips.forEach { tip ->
                    TipCard(tip = tip, accentColor = categoryColor)
                }
            }
        }
    }
}

@Composable
private fun TipCard(
    tip: ImprovementTip,
    accentColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(tip.icon, fontSize = 20.sp)
                    Text(
                        tip.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    tip.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 8.dp, start = 28.dp)
                )
            }
        }
    }
}

@Composable
private fun DisclaimerNote() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                "This assessment is for informational purposes only. Visceral fat estimation is approximate. For accurate body composition analysis, consult a healthcare provider.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}
