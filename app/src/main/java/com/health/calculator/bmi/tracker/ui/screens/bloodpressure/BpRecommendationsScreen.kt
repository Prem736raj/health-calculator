// ui/screens/bloodpressure/BpRecommendationsScreen.kt
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.*

// ─── Main Recommendations Section (for embedding in result screen) ─────────────

@Composable
fun BpRecommendationsSection(
    category: BpCategory
) {
    val guidance = remember(category) { BpRecommendationsProvider.getGuidance(category) }
    val categoryColor = getBpCategoryColor(category)
    val haptic = LocalHapticFeedback.current

    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(
            animationSpec = tween(600, delayMillis = 200)
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Header Message Card
            RecommendationHeaderCard(
                emoji = guidance.headerEmoji,
                message = guidance.headerMessage,
                tone = guidance.headerTone,
                categoryColor = categoryColor,
                monitoringFrequency = guidance.monitoringFrequency
            )

            // Urgency Note (if exists)
            guidance.urgencyNote?.let { note ->
                UrgencyNoteCard(note = note, tone = guidance.headerTone)
            }

            // Lifestyle Recommendations
            if (guidance.recommendations.isNotEmpty()) {
                ExpandableRecommendationCard(
                    title = "Lifestyle Recommendations",
                    icon = "💡",
                    recommendations = guidance.recommendations,
                    categoryColor = categoryColor,
                    initiallyExpanded = true
                )
            }

            // Diet Tips
            if (guidance.dietTips.isNotEmpty()) {
                ExpandableRecommendationCard(
                    title = "Diet & Nutrition",
                    icon = "🥗",
                    recommendations = guidance.dietTips,
                    categoryColor = categoryColor,
                    initiallyExpanded = false
                )
            }

            // Exercise Tips
            if (guidance.exerciseTips.isNotEmpty()) {
                ExpandableRecommendationCard(
                    title = "Exercise & Activity",
                    icon = "🏃",
                    recommendations = guidance.exerciseTips,
                    categoryColor = categoryColor,
                    initiallyExpanded = false
                )
            }

            // Health Risks
            if (guidance.healthRisks.isNotEmpty()) {
                HealthRisksCard(
                    risks = guidance.healthRisks,
                    tone = guidance.headerTone
                )
            }

            // Doctor Advice
            guidance.doctorAdvice?.let { advice ->
                DoctorAdviceCard(advice = advice, tone = guidance.headerTone)
            }

            // White Coat Syndrome (always available)
            WhiteCoatSyndromeCard()

            // Medical Disclaimer
            MedicalDisclaimerCard()
        }
    }
}

// ─── Header Card ───────────────────────────────────────────────────────────────

@Composable
private fun RecommendationHeaderCard(
    emoji: String,
    message: String,
    tone: BpGuidanceTone,
    categoryColor: Color,
    monitoringFrequency: String
) {
    val bgColor = when (tone) {
        BpGuidanceTone.POSITIVE -> Color(0xFF4CAF50).copy(alpha = 0.08f)
        BpGuidanceTone.GENTLE_AWARENESS -> Color(0xFF8BC34A).copy(alpha = 0.08f)
        BpGuidanceTone.CAUTIOUS -> Color(0xFFFFC107).copy(alpha = 0.08f)
        BpGuidanceTone.CONCERNED -> Color(0xFFFF9800).copy(alpha = 0.08f)
        BpGuidanceTone.URGENT -> Color(0xFFF44336).copy(alpha = 0.08f)
        BpGuidanceTone.EMERGENCY -> Color(0xFFB71C1C).copy(alpha = 0.08f)
        BpGuidanceTone.INFORMATIONAL -> Color(0xFF42A5F5).copy(alpha = 0.08f)
    }

    val borderColor = categoryColor.copy(alpha = 0.25f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Personalized Guidance",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }

            // Monitoring frequency badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = categoryColor.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = categoryColor
                    )
                    Text(
                        "Recommended monitoring: $monitoringFrequency",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor
                    )
                }
            }
        }
    }
}

// ─── Urgency Note Card ─────────────────────────────────────────────────────────

@Composable
private fun UrgencyNoteCard(note: String, tone: BpGuidanceTone) {
    val isEmergency = tone == BpGuidanceTone.EMERGENCY || tone == BpGuidanceTone.URGENT

    val infiniteTransition = rememberInfiniteTransition(label = "urgency_pulse")
    val pulseAlpha by if (isEmergency) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    val bgColor = if (isEmergency) Color(0xFFB71C1C).copy(alpha = 0.08f * pulseAlpha)
    else Color(0xFFFF9800).copy(alpha = 0.08f)

    val textColor = if (isEmergency) Color(0xFFB71C1C) else Color(0xFFE65100)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isEmergency) Color(0xFFB71C1C).copy(alpha = 0.3f * pulseAlpha)
            else Color(0xFFFF9800).copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                if (isEmergency) Icons.Filled.Error else Icons.Filled.Warning,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                note,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isEmergency) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

// ─── Expandable Recommendation Card ────────────────────────────────────────────

@Composable
private fun ExpandableRecommendationCard(
    title: String,
    icon: String,
    recommendations: List<BpRecommendation>,
    categoryColor: Color,
    initiallyExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = !isExpanded
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(icon, style = MaterialTheme.typography.titleMedium)
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = categoryColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "${recommendations.size}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }
                }

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(300),
                    label = "chevron"
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    recommendations.forEachIndexed { index, rec ->
                        val itemEnter = remember {
                            MutableTransitionState(false).apply { targetState = true }
                        }

                        AnimatedVisibility(
                            visibleState = itemEnter,
                            enter = slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = tween(300, delayMillis = index * 80)
                            ) + fadeIn(animationSpec = tween(300, delayMillis = index * 80))
                        ) {
                            RecommendationItem(recommendation = rec)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationItem(recommendation: BpRecommendation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            recommendation.icon,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                recommendation.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                recommendation.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

// ─── Health Risks Card ─────────────────────────────────────────────────────────

@Composable
private fun HealthRisksCard(risks: List<String>, tone: BpGuidanceTone) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val riskColor = when (tone) {
        BpGuidanceTone.CAUTIOUS -> Color(0xFFFF9800)
        BpGuidanceTone.CONCERNED -> Color(0xFFFF7043)
        BpGuidanceTone.URGENT, BpGuidanceTone.EMERGENCY -> Color(0xFFF44336)
        BpGuidanceTone.INFORMATIONAL -> Color(0xFF42A5F5)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = !isExpanded
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.HealthAndSafety,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "Health Risks to Be Aware Of",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(300),
                    label = "risk_chevron"
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    risks.forEach { risk ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(riskColor.copy(alpha = 0.05f))
                                .padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(riskColor)
                                    .align(Alignment.Top)
                                    .offset(y = 6.dp)
                            )
                            Text(
                                risk,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Doctor Advice Card ────────────────────────────────────────────────────────

@Composable
private fun DoctorAdviceCard(advice: String, tone: BpGuidanceTone) {
    val isUrgent = tone == BpGuidanceTone.URGENT || tone == BpGuidanceTone.EMERGENCY || tone == BpGuidanceTone.CONCERNED

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent) Color(0xFFE3F2FD) else Color(0xFFF3E5F5).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isUrgent) BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUrgent) Color(0xFF1E88E5).copy(alpha = 0.12f)
                        else Color(0xFF7B1FA2).copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.LocalHospital,
                    contentDescription = null,
                    tint = if (isUrgent) Color(0xFF1E88E5) else Color(0xFF7B1FA2),
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Medical Advice",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUrgent) Color(0xFF1565C0) else Color(0xFF6A1B9A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    advice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ─── White Coat Syndrome Card ──────────────────────────────────────────────────

@Composable
private fun WhiteCoatSyndromeCard() {
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val whiteCoatInfo = remember { BpRecommendationsProvider.getWhiteCoatSyndromeInfo() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = !isExpanded
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🥼", style = MaterialTheme.typography.titleMedium)
                    Column {
                        Text(
                            "White Coat Syndrome",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Why readings may differ at the doctor's office",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(300),
                    label = "wc_chevron"
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    whiteCoatInfo.forEach { info ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8EAF6).copy(alpha = 0.4f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                info.icon,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    info.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    info.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Medical Disclaimer Card ───────────────────────────────────────────────────

@Composable
private fun MedicalDisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp).padding(top = 2.dp)
            )
            Column {
                Text(
                    "Medical Disclaimer",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "This information is for educational purposes only and should not replace professional medical advice, diagnosis, or treatment. Always consult your healthcare provider for personal medical decisions. If you believe you are experiencing a medical emergency, call your local emergency services immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }
    }
}
