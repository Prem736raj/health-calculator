package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*

@Composable
fun BMIHealthRiskSection(
    bmi: Float,
    age: Int,
    isMale: Boolean,
    modifier: Modifier = Modifier
) {
    val healthRiskInfo = remember(bmi, age, isMale) {
        BMIHealthRiskProvider.getHealthRiskInfo(bmi, age, isMale)
    }

    var expandedRisks by remember { mutableStateOf(false) }
    var expandedRecommendations by remember { mutableStateOf(true) }
    var expandedActions by remember { mutableStateOf(false) }
    var expandedDoctor by remember { mutableStateOf(false) }

    val isNormal = bmi in 18.5f..24.9f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = healthRiskInfo.categoryIcon,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Health Risk Assessment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Based on your BMI category",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Risk level badge
                RiskLevelBadge(riskLevel = healthRiskInfo.riskLevel)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Personalized tone message
            ToneMessageCard(
                message = healthRiskInfo.toneMessage,
                isNormal = isNormal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Health Risks (not shown for Normal BMI)
            if (healthRiskInfo.healthRisks.isNotEmpty()) {
                ExpandableRiskSection(
                    title = "Potential Health Risks",
                    icon = "⚕️",
                    itemCount = healthRiskInfo.healthRisks.size,
                    expanded = expandedRisks,
                    onToggle = { expandedRisks = !expandedRisks },
                    accentColor = getRiskAccentColor(healthRiskInfo.riskLevel)
                ) {
                    healthRiskInfo.healthRisks.forEachIndexed { index, risk ->
                        HealthRiskCard(risk = risk)
                        if (index < healthRiskInfo.healthRisks.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recommendations
            ExpandableRiskSection(
                title = if (isNormal) "Tips to Stay Healthy" else "Recommendations",
                icon = if (isNormal) "✨" else "💡",
                itemCount = healthRiskInfo.recommendations.size,
                expanded = expandedRecommendations,
                onToggle = { expandedRecommendations = !expandedRecommendations },
                accentColor = if (isNormal) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            ) {
                healthRiskInfo.recommendations.forEachIndexed { index, rec ->
                    RecommendationCard(recommendation = rec)
                    if (index < healthRiskInfo.recommendations.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Steps
            ExpandableRiskSection(
                title = if (isNormal) "Keep It Up!" else "Action Steps",
                icon = "📋",
                itemCount = healthRiskInfo.actionSteps.size,
                expanded = expandedActions,
                onToggle = { expandedActions = !expandedActions },
                accentColor = MaterialTheme.colorScheme.tertiary
            ) {
                healthRiskInfo.actionSteps.forEachIndexed { index, step ->
                    ActionStepItem(
                        stepNumber = index + 1,
                        text = step,
                        isNormal = isNormal
                    )
                    if (index < healthRiskInfo.actionSteps.lastIndex) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Doctor's Note
            ExpandableRiskSection(
                title = "Healthcare Provider Note",
                icon = "🩺",
                itemCount = null,
                expanded = expandedDoctor,
                onToggle = { expandedDoctor = !expandedDoctor },
                accentColor = Color(0xFF1976D2)
            ) {
                DoctorNoteCard(
                    note = healthRiskInfo.doctorNote,
                    riskLevel = healthRiskInfo.riskLevel
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medical Disclaimer
            MedicalDisclaimerCard()
        }
    }
}

@Composable
private fun RiskLevelBadge(riskLevel: RiskLevel) {
    val badgeColor = when (riskLevel) {
        RiskLevel.LOW -> Color(0xFF4CAF50)
        RiskLevel.MODERATE -> Color(0xFFFFC107)
        RiskLevel.HIGH -> Color(0xFFFF9800)
        RiskLevel.VERY_HIGH -> Color(0xFFF44336)
        RiskLevel.EXTREMELY_HIGH -> Color(0xFFB71C1C)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = badgeColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            badgeColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = riskLevel.emoji,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = riskLevel.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = badgeColor
            )
        }
    }
}

@Composable
private fun ToneMessageCard(
    message: String,
    isNormal: Boolean
) {
    val backgroundColor = if (isNormal) {
        Color(0xFF4CAF50).copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    }
    val borderColor = if (isNormal) {
        Color(0xFF4CAF50).copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = if (isNormal) "💚" else "💙",
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ExpandableRiskSection(
    title: String,
    icon: String,
    itemCount: Int?,
    expanded: Boolean,
    onToggle: () -> Unit,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column {
            // Header (clickable)
            Surface(
                onClick = onToggle,
                color = Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (itemCount != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = accentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$itemCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    modifier = Modifier.padding(
                                        horizontal = 7.dp,
                                        vertical = 2.dp
                                    )
                                )
                            }
                        }
                    }

                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        animationSpec = tween(300),
                        label = "chevronRotation"
                    )
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
            }

            // Content
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(300)) + expandVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(tween(200)) + shrinkVertically(
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = 14.dp
                    )
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun HealthRiskCard(risk: HealthRiskItem) {
    val severityColor = when (risk.severity) {
        RiskSeverity.MILD -> Color(0xFFFFC107)
        RiskSeverity.MODERATE -> Color(0xFFFF9800)
        RiskSeverity.HIGH -> Color(0xFFF44336)
        RiskSeverity.SEVERE -> Color(0xFFB71C1C)
    }
    val severityLabel = when (risk.severity) {
        RiskSeverity.MILD -> "Mild"
        RiskSeverity.MODERATE -> "Moderate"
        RiskSeverity.HIGH -> "High"
        RiskSeverity.SEVERE -> "Severe"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = risk.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = risk.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Severity indicator
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = severityColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(severityColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = severityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = severityColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = risk.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: RecommendationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = recommendation.icon, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ActionStepItem(
    stepNumber: Int,
    text: String,
    isNormal: Boolean
) {
    val accentColor = if (isNormal) Color(0xFF4CAF50) else MaterialTheme.colorScheme.tertiary

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.12f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DoctorNoteCard(
    note: String,
    riskLevel: RiskLevel
) {
    val urgencyColor = when (riskLevel) {
        RiskLevel.LOW -> Color(0xFF4CAF50)
        RiskLevel.MODERATE -> Color(0xFF2196F3)
        RiskLevel.HIGH -> Color(0xFFFF9800)
        RiskLevel.VERY_HIGH -> Color(0xFFF44336)
        RiskLevel.EXTREMELY_HIGH -> Color(0xFFB71C1C)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = urgencyColor.copy(alpha = 0.06f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            urgencyColor.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalHospital,
                contentDescription = null,
                tint = urgencyColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (riskLevel == RiskLevel.VERY_HIGH || riskLevel == RiskLevel.EXTREMELY_HIGH) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = urgencyColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (riskLevel == RiskLevel.EXTREMELY_HIGH) "URGENT"
                            else "IMPORTANT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = urgencyColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun MedicalDisclaimerCard() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Medical Disclaimer: This health risk information is for educational " +
                    "and informational purposes only. It is not intended as medical advice, " +
                    "diagnosis, or treatment. BMI is one of many health indicators and does " +
                    "not account for individual factors like muscle mass, bone density, or " +
                    "overall body composition. Always consult a qualified healthcare " +
                    "professional for personalized medical guidance. Never disregard " +
                    "professional medical advice because of information provided here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            lineHeight = 16.sp,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun getRiskAccentColor(riskLevel: RiskLevel): Color {
    return when (riskLevel) {
        RiskLevel.LOW -> Color(0xFF4CAF50)
        RiskLevel.MODERATE -> Color(0xFFFFC107)
        RiskLevel.HIGH -> Color(0xFFFF9800)
        RiskLevel.VERY_HIGH -> Color(0xFFF44336)
        RiskLevel.EXTREMELY_HIGH -> Color(0xFFB71C1C)
    }
}
