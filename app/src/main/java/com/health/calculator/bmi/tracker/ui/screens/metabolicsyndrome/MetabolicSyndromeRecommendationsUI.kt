package com.health.calculator.bmi.tracker.ui.screens.metabolicsyndrome

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
import com.health.calculator.bmi.tracker.calculator.CardiovascularRiskSummary
import com.health.calculator.bmi.tracker.calculator.CriterionRecommendation
import com.health.calculator.bmi.tracker.calculator.MetabolicSyndromeRecommendations
import com.health.calculator.bmi.tracker.calculator.MetabolicSyndromeResult
import com.health.calculator.bmi.tracker.ui.theme.*

@Composable
fun DetailedRecommendationsSection(
    result: MetabolicSyndromeResult,
    modifier: Modifier = Modifier
) {
    val recommendations = remember(result) {
        result.criteria.map { criterion ->
            MetabolicSyndromeRecommendations.getRecommendationForCriterion(
                criterionName = criterion.name,
                isMet = criterion.isMet,
                isOnMedication = criterion.isOnMedication
            )
        }
    }

    val cardiovascularRisk = remember(result) {
        MetabolicSyndromeRecommendations.getCardiovascularRiskSummary(result.criteriaMet)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // === Section Header ===
        Text(
            text = "Detailed Analysis & Recommendations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // === Cardiovascular Risk Summary ===
        CardiovascularRiskCard(riskSummary = cardiovascularRisk)

        Spacer(modifier = Modifier.height(16.dp))

        // === Individual Criterion Recommendations ===
        Text(
            text = "Criterion-by-Criterion Analysis",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        recommendations.forEach { recommendation ->
            CriterionRecommendationCard(recommendation = recommendation)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // === Medical Consultation Banner ===
        if (cardiovascularRisk.shouldSeekMedical) {
            Spacer(modifier = Modifier.height(8.dp))
            MedicalConsultationBanner(criteriaMet = result.criteriaMet)
        }
    }
}

@Composable
private fun CardiovascularRiskCard(riskSummary: CardiovascularRiskSummary) {
    val riskColor = when (riskSummary.riskLevel) {
        "Low" -> HealthGreen
        "Low-Moderate" -> HealthYellow
        "Moderate" -> HealthOrange
        "High" -> HealthRed
        "Very High" -> Color(0xFFB71C1C)
        else -> Color.Gray
    }

    val animatedProgress by animateFloatAsState(
        targetValue = riskSummary.riskScore / 100f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "cv_risk_progress"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = riskColor.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = riskColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cardiovascular Risk Estimation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Risk Gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = Size(
                        size.width - strokeWidth,
                        size.height - strokeWidth
                    )
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background arc
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Colored arc
                    drawArc(
                        color = riskColor,
                        startAngle = 150f,
                        sweepAngle = 240f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${riskSummary.riskScore}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = riskColor
                    )
                    Text(
                        text = riskSummary.riskLevel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = riskColor.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = riskSummary.riskDescription,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = riskSummary.overallMessage,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (riskSummary.actionItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    color = riskColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Recommended Actions",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = riskColor,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                riskSummary.actionItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (item.startsWith("🏥") || item.startsWith("🚨")) "" else "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = riskColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp, top = 1.dp)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CriterionRecommendationCard(recommendation: CriterionRecommendation) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }

    val borderColor = when (recommendation.urgencyLevel) {
        "positive" -> HealthGreen
        "caution" -> HealthYellow
        "warning" -> HealthRed
        else -> Color.Gray
    }

    val bgColor = if (recommendation.isAbnormal) {
        HealthRed.copy(alpha = 0.04f)
    } else {
        HealthGreen.copy(alpha = 0.04f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = !expanded
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = recommendation.icon,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recommendation.criterionName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (recommendation.isAbnormal) "⚠️ Needs Attention" else "✅ Healthy Range",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (recommendation.isAbnormal) HealthRed else HealthGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Status badge
                Surface(
                    color = borderColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (recommendation.isAbnormal) "Abnormal" else "Normal",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = borderColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Collapsed: Show brief message
            if (!expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (recommendation.isAbnormal) {
                        "Tap to see health risks and improvement tips"
                    } else {
                        recommendation.normalMessage
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(300)) + fadeOut(tween(200))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(
                        color = borderColor.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Health Meaning
                    Text(
                        text = "What this means",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.healthMeaning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    // Health Risks (only if abnormal)
                    if (recommendation.isAbnormal && recommendation.risks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Associated Health Risks",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = HealthRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        recommendation.risks.forEach { risk ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(HealthRed.copy(alpha = 0.7f))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = risk,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    // Recommendations
                    if (recommendation.recommendations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (recommendation.isAbnormal)
                                "How to Improve"
                            else
                                "Tips to Maintain",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (recommendation.isAbnormal) HealthOrange else HealthGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        recommendation.recommendations.forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Normal positive message
                    if (!recommendation.isAbnormal) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = HealthGreen.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎉", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = recommendation.normalMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = HealthGreen,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalConsultationBanner(criteriaMet: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val bannerColor = if (criteriaMet >= 4) Color(0xFFB71C1C) else HealthRed

    Card(
        colors = CardDefaults.cardColors(
            containerColor = bannerColor.copy(alpha = 0.1f * pulseAlpha)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.MedicalServices,
                    contentDescription = null,
                    tint = bannerColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Medical Consultation Recommended",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = bannerColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when {
                    criteriaMet >= 4 -> "With $criteriaMet of 5 criteria met, it is strongly recommended that you see a healthcare provider as soon as possible for a comprehensive metabolic evaluation and personalized treatment plan."
                    criteriaMet == 3 -> "With metabolic syndrome present (3 of 5 criteria met), please schedule an appointment with your healthcare provider for proper evaluation and to discuss management strategies."
                    else -> "Consider consulting your healthcare provider to discuss your risk factors and develop a prevention plan."
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = bannerColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = bannerColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "This is not a diagnosis. Only a healthcare professional can diagnose and treat metabolic syndrome.",
                        style = MaterialTheme.typography.labelSmall,
                        color = bannerColor.copy(alpha = 0.8f),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
