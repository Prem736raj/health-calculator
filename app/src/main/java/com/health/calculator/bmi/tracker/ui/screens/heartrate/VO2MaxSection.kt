package com.health.calculator.bmi.tracker.ui.screens.heartrate

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.util.VO2MaxCalculator
import com.health.calculator.bmi.tracker.util.VO2MaxResult
import com.health.calculator.bmi.tracker.util.RecoveryHRGuideline

// ============================================================
// MAIN VO2 MAX SECTION
// ============================================================

@Composable
fun VO2MaxSection(
    maxHR: Int,
    restingHR: Int?,
    age: Int,
    gender: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.txt_text_placeholder_63), fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(R.string.txt_vo_max_fitness_age),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.txt_estimated_aerobic_capacity_and),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        if (restingHR != null && restingHR > 0) {
            val vo2Result = remember(maxHR, restingHR, age, gender) {
                VO2MaxCalculator.analyze(maxHR, restingHR, age, gender)
            }

            // VO2 Max Result Card
            VO2MaxResultCard(result = vo2Result)
        } else {
            // No resting HR provided
            NoRestingHRCard()
        }
    }
}

// ============================================================
// NO RESTING HR CARD
// ============================================================

@Composable
private fun NoRestingHRCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.txt_text_placeholder_63), fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.txt_resting_heart_rate_required),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.txt_to_estimate_your_vo_max_and_fi) +
                        "Go back and select the Karvonen formula, or enter your resting HR to unlock this feature.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF2196F3).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.txt_text_placeholder_1), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.txt_tip_measure_your_pulse_first_t),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2196F3),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

// ============================================================
// VO2 MAX RESULT CARD
// ============================================================

@Composable
private fun VO2MaxResultCard(result: VO2MaxResult) {
    val accentColor = MaterialTheme.colorScheme.primary
    val animatedVO2 by animateFloatAsState(
        targetValue = result.vo2Max,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "vo2_anim"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // VO2 Max circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Background ring
                Canvas(modifier = Modifier.size(140.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    drawArc(
                        color = accentColor.copy(alpha = 0.12f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Animated progress (normalize to 0-70 range for display)
                    val progress = (animatedVO2 / 70f).coerceIn(0f, 1f)
                    drawArc(
                        color = accentColor,
                        startAngle = 135f,
                        sweepAngle = 270f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.txt_1f).format(animatedVO2),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                    Text(
                        text = stringResource(R.string.txt_ml_kg_min),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Text(
                text = "Informational estimate only. Repeat under similar conditions to look for a personal trend; age and fitness reference bands are not shown because methods vary.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Formula note
            Text(
                text = result.methodology,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatBubble(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 16.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            fontSize = 10.sp
        )
    }
}

// ============================================================
// FITNESS AGE CARD
// ============================================================

@Composable
private fun FitnessAgeCard(result: VO2MaxResult) {
    val animatedFitnessAge by animateIntAsState(
        targetValue = result.fitnessAge,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "fitness_age"
    )

    val ageDiff = result.fitnessAge - result.actualAge
    val isYounger = ageDiff < -1
    val isOlder = ageDiff > 1
    val accentColor = when {
        isYounger -> Color(0xFF4CAF50)
        isOlder -> Color(0xFFFF9800)
        else -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reference-age comparison",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Age comparison visual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Actual age
                AgeColumn(
                    label = "Actual Age",
                    age = result.actualAge,
                    emoji = "📅",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    isHighlighted = false
                )

                // Arrow
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Text(
                        text = when {
                            isYounger -> "→"
                            isOlder -> "→"
                            else -> "="
                        },
                        fontSize = 24.sp,
                        color = accentColor
                    )
                    if (ageDiff != 0) {
                        Text(
                            text = "Reference difference: ${kotlin.math.abs(ageDiff)} yrs",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 10.sp
                        )
                    }
                }

                // Fitness age
                AgeColumn(
                    label = "Reference age",
                    age = animatedFitnessAge,
                    emoji = "📊",
                    color = accentColor,
                    isHighlighted = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Message
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = accentColor.copy(alpha = 0.08f)
                )
            ) {
                Text(
                    text = result.fitnessAgeMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(14.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun AgeColumn(
    label: String,
    age: Int,
    emoji: String,
    color: Color,
    isHighlighted: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = if (isHighlighted) 28.sp else 22.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$age",
            style = if (isHighlighted) MaterialTheme.typography.headlineMedium
            else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = stringResource(R.string.txt_years),
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
    }
}

// ============================================================
// VO2 CLASSIFICATION GAUGE
// ============================================================

@Composable
private fun VO2ClassificationGauge(result: VO2MaxResult) {
    val categories = listOf(
        "Poor" to Color(0xFFF44336),
        "Below Avg" to Color(0xFFFF9800),
        "Average" to Color(0xFFFFC107),
        "Above Avg" to Color(0xFF8BC34A),
        "Good" to Color(0xFF4CAF50),
        "Excellent" to Color(0xFF2196F3),
        "Superior" to Color(0xFF1565C0)
    )

    val currentIndex = categories.indexOfFirst {
        it.first.startsWith(result.classification.category.take(4), ignoreCase = true)
    }.coerceIn(0, categories.lastIndex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.txt_where_you_stand),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Horizontal gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                categories.forEachIndexed { index, (name, color) ->
                    val isCurrent = index == currentIndex

                    val animatedHeight by animateDpAsState(
                        targetValue = if (isCurrent) 40.dp else 24.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "gauge_$index"
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isCurrent) {
                            Text(
                                text = stringResource(R.string.txt_text_placeholder_62),
                                fontSize = 10.sp,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(animatedHeight)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isCurrent) color
                                    else color.copy(alpha = 0.25f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrent) {
                                Text(
                                    text = stringResource(R.string.txt_0f).format(result.vo2Max),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = if (isCurrent) color
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// IMPROVEMENT SECTION
// ============================================================

@Composable
private fun VO2ImprovementSection(result: VO2MaxResult) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.txt_text_placeholder_4), fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Using this estimate responsibly",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // A single heart-rate estimate cannot support a reliable future
            // projection, so show the current estimate and make that limit
            // explicit rather than promising a percentage change.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProjectionCard(
                    label = "Current",
                    value = "%.1f".format(result.vo2Max),
                    unit = "ml/kg/min",
                    emoji = "📊",
                    color = result.classification.color
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("↔", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "No projection",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                ProjectionCard(
                    label = "Repeat to track",
                    value = "—",
                    unit = "same conditions",
                    emoji = "📝",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Repeat the measurement under similar conditions to observe a trend; this estimate is not a clinical test.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            // Expanded tips
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = stringResource(R.string.txt_how_to_improve_vo_max),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    val tips = listOf(
                        Triple("🏃", "Build an aerobic base",
                            "Comfortable aerobic sessions can support fitness. Choose duration and frequency that fit your current abilities."),
                        Triple("⚡", "Use intervals carefully",
                            "Intervals are optional and demanding. Build gradually, use the talk test, and seek advice before changing intensity if you have concerns."),
                        Triple("📅", "Consistency is key",
                            "Regular movement is more useful than a promised percentage change. Allow recovery and adjust to how you feel."),
                        Triple("😴", "Recovery matters",
                            "Rest and sleep support wellbeing; individual needs vary. Take breaks when tired or unwell."),
                        Triple("📊", "Track progress",
                            "If you repeat the estimate, use similar conditions and look at a longer trend rather than one result."),
                        Triple("⏱️", "Progress gradually",
                            "Increase duration or intensity gradually and leave room for recovery; there is no universal weekly percentage.")
                    )

                    tips.forEach { (emoji, title, description) ->
                        ImprovementTipRow(
                            emoji = emoji,
                            title = title,
                            description = description
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            if (!isExpanded) {
                Text(
                    text = stringResource(R.string.txt_tap_for_improvement_tips),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ProjectionCard(
    label: String,
    value: String,
    unit: String,
    emoji: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = color.copy(alpha = 0.6f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ImprovementTipRow(emoji: String, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                lineHeight = 17.sp
            )
        }
    }
}

// ============================================================
// RECOVERY HEART RATE SECTION
// ============================================================

@Composable
private fun RecoveryHeartRateSection() {
    var isExpanded by remember { mutableStateOf(false) }
    val guidelines = remember { VO2MaxCalculator.getRecoveryHRGuidelines() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.txt_text_placeholder_27), fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.txt_recovery_heart_rate),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.txt_how_quickly_your_hr_drops_afte),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Key insight
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.txt_text_placeholder_1), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.txt_a_healthy_heart_should_drop_20),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Recovery classifications
                    Text(
                        text = stringResource(R.string.txt_recovery_rate_classifications),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    guidelines.forEach { guideline ->
                        RecoveryGuidelineRow(guideline = guideline)
                        if (guideline != guidelines.last()) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // How to test
                    Text(
                        text = stringResource(R.string.txt_how_to_test_your_recovery_hr),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val steps = listOf(
                        "Exercise at Zone 4-5 intensity for at least 2-3 minutes",
                        "Immediately stop and stand still (don't sit down)",
                        "Note your peak heart rate right when you stop",
                        "After exactly 60 seconds, check your heart rate again",
                        "Subtract: Peak HR − HR at 1 minute = Recovery Drop",
                        "A drop of 20+ BPM is considered healthy"
                    )

                    steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.txt_track_your_recovery_hr_monthly) +
                                "look for patterns under similar conditions; a single recovery reading is not diagnostic.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        lineHeight = 17.sp
                    )

                    Text(
                        text = guidelines.firstOrNull()?.referenceNote.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            if (!isExpanded) {
                Text(
                    text = stringResource(R.string.txt_tap_for_recovery_test_guide),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RecoveryGuidelineRow(guideline: RecoveryHRGuideline) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(guideline.color.copy(alpha = 0.04f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = guideline.emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = guideline.category,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = guideline.color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = guideline.color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = guideline.dropInFirstMinute,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = guideline.color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }
            Text(
                text = guideline.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                lineHeight = 14.sp
            )
        }
    }
}
