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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.*

// ─── Quick Log Card ────────────────────────────────────────────────────────────

data class QuickLogSuggestion(
    val timeOfDay: BpTimeOfDay,
    val position: BpPosition?,
    val arm: BpArm?,
    val label: String,
    val emoji: String
)

@Composable
fun BpQuickLogCard(
    suggestion: QuickLogSuggestion?,
    onQuickLogClicked: (QuickLogSuggestion) -> Unit,
    onDismiss: () -> Unit
) {
    if (suggestion == null) return

    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { -50 }) + fadeIn(animationSpec = tween(400)),
        exit = slideOutVertically(targetOffsetY = { -50 }) + fadeOut()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    suggestion.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Quick Log",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        suggestion.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onQuickLogClicked(suggestion)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    )
                ) {
                    Icon(
                        Icons.Filled.FlashOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Use", fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// ─── Enhanced Emergency Alert (Pulsing) ────────────────────────────────────────

@Composable
fun BpEmergencyPulsingAlert(
    reading: BloodPressureReading,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emergency")

    val bgPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_pulse"
    )

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    val borderWidth by infiniteTransition.animateFloat(
        initialValue = 1.5f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_pulse"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFFF3F3),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp,
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB71C1C).copy(alpha = bgPulse * 0.15f))
                    .border(
                        width = borderWidth.dp,
                        color = Color(0xFFB71C1C).copy(alpha = bgPulse * 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = null,
                    tint = Color(0xFFB71C1C).copy(alpha = bgPulse),
                    modifier = Modifier
                        .size(48.dp)
                        .scale(iconScale)
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
                // Reading with pulsing border
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFB71C1C).copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        borderWidth.dp,
                        Color(0xFFB71C1C).copy(alpha = bgPulse * 0.3f)
                    )
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
                    "This reading indicates a hypertensive crisis requiring immediate medical attention.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF424242)
                )

                // Symptoms to watch
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Seek emergency care if you experience:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5D4037)
                        )
                        listOf(
                            "Severe headache",
                            "Chest pain or tightness",
                            "Difficulty breathing",
                            "Vision changes or blurriness",
                            "Numbness or weakness",
                            "Difficulty speaking"
                        ).forEach { symptom ->
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

                // Emergency call card with pulse
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFB71C1C).copy(alpha = bgPulse)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("I Understand", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ─── Category Reveal Animation ─────────────────────────────────────────────────

@Composable
fun BpCategoryReveal(
    category: BpCategory,
    isVisible: Boolean
) {
    val categoryColor = getBpCategoryColor(category)

    val colorAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "category_reveal"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "category_scale"
    )

    Card(
        modifier = Modifier
            .graphicsLayer {
                alpha = colorAlpha
                scaleX = scaleAnim
                scaleY = scaleAnim
            },
        colors = CardDefaults.cardColors(
            containerColor = categoryColor.copy(alpha = 0.15f * colorAlpha)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            category.displayName,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = categoryColor.copy(alpha = colorAlpha)
        )
    }
}

// ─── Animated Save Confirmation ────────────────────────────────────────────────

@Composable
fun BpSaveConfirmation(
    isVisible: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(animationSpec = tween(400)) +
                scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = slideOutVertically(targetOffsetY = { -40 }) + fadeOut(animationSpec = tween(300))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val checkScale by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "check_scale"
                )
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .size(22.dp)
                        .scale(checkScale)
                )
                Text(
                    "Reading saved to BP log ✓",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

// ─── Validation Edge Case Messages ─────────────────────────────────────────────

@Composable
fun BpEdgeCaseWarning(
    systolic: Int,
    diastolic: Int
) {
    val warning = getBpEdgeCaseWarning(systolic, diastolic) ?: return

    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = expandVertically() + fadeIn(animationSpec = tween(400))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3E0)
            ),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    warning,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE65100)
                )
            }
        }
    }
}

private fun getBpEdgeCaseWarning(systolic: Int, diastolic: Int): String? {
    return when {
        systolic == diastolic -> "Systolic and diastolic values cannot be equal. Please check your reading."
        systolic - diastolic < 10 -> "The difference between systolic and diastolic is unusually small. Please verify your reading."
        systolic > 250 -> "This is an extremely high systolic reading. Please verify and seek immediate medical attention if accurate."
        diastolic > 150 -> "This is an extremely high diastolic reading. Please verify and seek immediate medical attention if accurate."
        systolic < 70 -> "This is an extremely low systolic reading. If accurate and you feel unwell, seek medical attention."
        diastolic < 40 -> "This is an extremely low diastolic reading. If accurate and you feel unwell, seek medical attention."
        else -> null
    }
}
