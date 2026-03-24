package com.health.calculator.bmi.tracker.ui.screens.heartrate

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
            Text("🫁", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "VO₂ Max & Fitness Age",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Estimated aerobic capacity and cardiovascular fitness",
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

            // Fitness Age Card
            FitnessAgeCard(result = vo2Result)

            // VO2 Classification Gauge
            VO2ClassificationGauge(result = vo2Result)

            // Improvement Section
            VO2ImprovementSection(result = vo2Result)

            // Recovery HR Section
            RecoveryHeartRateSection()
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
            Text("🫁", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Resting Heart Rate Required",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "To estimate your VO₂ Max and Fitness Age, we need your resting heart rate. " +
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
                    Text("💡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tip: Measure your pulse first thing in the morning for the most accurate reading",
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
    val animatedVO2 by animateFloatAsState(
        targetValue = result.vo2Max,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "vo2_anim"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = result.classification.color.copy(alpha = 0.06f)
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
                        color = result.classification.color.copy(alpha = 0.12f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Animated progress (normalize to 0-70 range for display)
                    val progress = (animatedVO2 / 70f).coerceIn(0f, 1f)
                    drawArc(
                        color = result.classification.color,
                        startAngle = 135f,
                        sweepAngle = 270f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f".format(animatedVO2),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = result.classification.color
                    )
                    Text(
                        text = "ml/kg/min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Classification badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = result.classification.color.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = result.classification.emoji,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.classification.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = result.classification.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.classification.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBubble(
                    emoji = "📊",
                    value = "Top ${100 - result.percentile}%",
                    label = "Percentile"
                )
                StatBubble(
                    emoji = "📏",
                    value = result.classification.rangeLabel,
                    label = "Range"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formula note
            Text(
                text = "Estimated using Uth et al. formula: 15.3 × (MHR ÷ RHR)",
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
                text = "🎂 Fitness Age",
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
                            text = if (isYounger) "${-ageDiff} yrs younger"
                            else "+$ageDiff yrs",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 10.sp
                        )
                    }
                }

                // Fitness age
                AgeColumn(
                    label = "Fitness Age",
                    age = animatedFitnessAge,
                    emoji = if (isYounger) "💪" else if (isOlder) "📈" else "👍",
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
            text = "years",
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
                text = "📏 Where You Stand",
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
                                text = "▼",
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
                                    text = "%.0f".format(result.vo2Max),
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
                    Text("📈", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Improvement Potential",
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

            // Projected improvement
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
                    Text("→", fontSize = 20.sp, color = Color(0xFF4CAF50))
                    Text(
                        text = "6 months",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                ProjectionCard(
                    label = "Projected",
                    value = "%.1f".format(result.projectedVO2After6Months),
                    unit = "ml/kg/min",
                    emoji = "🎯",
                    color = Color(0xFF4CAF50)
                )
            }

            Text(
                text = "Up to ${"%.0f".format(result.improvementPotential)}% improvement possible with consistent training",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4CAF50),
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
                        text = "How to Improve VO₂ Max",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    val tips = listOf(
                        Triple("🏃", "Zone 2-3 Base Training",
                            "Build aerobic base with 3-4 sessions/week of 30-60 min in Zone 2-3. This is the foundation for VO₂ Max improvement."),
                        Triple("⚡", "HIIT for Maximum Gains",
                            "High-intensity intervals (Zone 4-5) are the single most effective way to boost VO₂ Max. Try 4×4 min intervals at Zone 4 with 3 min Zone 2 recovery."),
                        Triple("📅", "Consistency is Key",
                            "Expect 15-20% improvement in 3-6 months with 3-5 sessions/week. Most gains come in the first 3 months."),
                        Triple("😴", "Recovery Matters",
                            "VO₂ Max improves during recovery, not during exercise. Get 7-9 hours of sleep and take rest days."),
                        Triple("📊", "Track Progress",
                            "Retest every 4-6 weeks. A dropping resting heart rate is the best indicator of improving fitness."),
                        Triple("⏱️", "Progressive Overload",
                            "Gradually increase duration before intensity. Add 10% more training volume per week maximum.")
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
                    text = "Tap for improvement tips",
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
                    Text("⏱️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Recovery Heart Rate",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "How quickly your HR drops after exercise",
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
                    Text("💡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "A healthy heart should drop 20+ BPM in the first minute after stopping intense exercise.",
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
                        text = "Recovery Rate Classifications",
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
                        text = "🧪 How to Test Your Recovery HR",
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
                        text = "💡 Track your recovery HR monthly. As fitness improves, " +
                                "your heart will recover faster — it's one of the best indicators of cardiovascular health!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        lineHeight = 17.sp
                    )
                }
            }

            if (!isExpanded) {
                Text(
                    text = "Tap for recovery test guide",
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
