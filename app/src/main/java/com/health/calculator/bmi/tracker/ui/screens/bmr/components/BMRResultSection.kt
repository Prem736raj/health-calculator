// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/BMRResultSection.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.BMRResultData
import com.health.calculator.bmi.tracker.data.model.BMRFormula
import com.health.calculator.bmi.tracker.ui.utils.CascadeAnimatedItem

@Composable
fun BMRResultSection(
    resultData: BMRResultData,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    var showKJ by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        Column {
            // ---- Primary Result Card ----
            CascadeAnimatedItem(index = 0, baseDelay = 100) {
                PrimaryBMRCard(
                    resultData = resultData,
                    showKJ = showKJ,
                    onToggleUnit = { showKJ = !showKJ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Interpretation ----
            CascadeAnimatedItem(index = 1, baseDelay = 100) {
                InterpretationCard(resultData = resultData)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- BMR Breakdown ----
            CascadeAnimatedItem(index = 2, baseDelay = 100) {
                BMRBreakdownCard(
                    resultData = resultData,
                    showKJ = showKJ
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Formula Comparison ----
            CascadeAnimatedItem(index = 3, baseDelay = 100) {
                FormulaComparisonCard(resultData = resultData)
            }
        }
    }
}

// ============================================================
// Primary BMR Result Card with Flame Visual
// ============================================================
@Composable
private fun PrimaryBMRCard(
    resultData: BMRResultData,
    showKJ: Boolean,
    onToggleUnit: () -> Unit
) {
    val displayValue = if (showKJ) resultData.bmrInKJ else resultData.primaryBMR
    val displayUnit = if (showKJ) "kJ/day" else "kcal/day"
    val level = resultData.getBMRLevel()

    val animatedValue = remember { Animatable(0f) }
    LaunchedEffect(displayValue) {
        animatedValue.snapTo(0f)
        animatedValue.animateTo(
            targetValue = displayValue,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Flame Animation
            FlameVisual(bmrValue = resultData.primaryBMR)

            Spacer(modifier = Modifier.height(16.dp))

            // BMR Value
            Text(
                text = "${animatedValue.value.toInt()}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 56.sp
            )

            // Unit with toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayUnit,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = onToggleUnit,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (showKJ) "kcal" else "kJ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Formula tag
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "📐 ${resultData.selectedFormula.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Level badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = getLevelColor(level).copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, getLevelColor(level).copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = level.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = level.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = getLevelColor(level)
                    )
                }
            }
        }
    }
}

// ============================================================
// Flame Visual Animation
// ============================================================
@Composable
private fun FlameVisual(bmrValue: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")

    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    val innerGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "innerGlow"
    )

    val flameColor1 = Color(0xFFFF6D00) // Deep orange
    val flameColor2 = Color(0xFFFF9100) // Orange
    val flameColor3 = Color(0xFFFFAB00) // Amber

    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size((90 * flameScale).dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            flameColor2.copy(alpha = 0.2f * innerGlow),
                            flameColor2.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Inner flame circle
        Box(
            modifier = Modifier
                .size((60 * flameScale).dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            flameColor3.copy(alpha = 0.4f),
                            flameColor1.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Flame emoji
        Text(
            text = "🔥",
            fontSize = (40 * flameScale).sp,
            modifier = Modifier.graphicsLayer {
                scaleX = flameScale
                scaleY = flameScale
            }
        )
    }
}

// ============================================================
// Interpretation Card
// ============================================================
@Composable
private fun InterpretationCard(resultData: BMRResultData) {
    val level = resultData.getBMRLevel()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💡", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "What This Means",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = resultData.getInterpretation(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = level.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

// ============================================================
// BMR Breakdown Card
// ============================================================
@Composable
private fun BMRBreakdownCard(
    resultData: BMRResultData,
    showKJ: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⏱️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BMR Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BreakdownItem(
                    label = "Per Day",
                    value = if (showKJ) "${resultData.bmrInKJ.toInt()} kJ"
                    else "${resultData.primaryBMR.toInt()} kcal",
                    icon = "📅",
                    color = MaterialTheme.colorScheme.primary
                )

                VerticalDividerLine()

                BreakdownItem(
                    label = "Per Hour",
                    value = if (showKJ) "${String.format("%.1f", resultData.bmrPerHourKJ)} kJ"
                    else "${String.format("%.1f", resultData.bmrPerHour)} kcal",
                    icon = "⏰",
                    color = MaterialTheme.colorScheme.tertiary
                )

                VerticalDividerLine()

                BreakdownItem(
                    label = "Per Minute",
                    value = if (showKJ) "${String.format("%.2f", resultData.bmrPerHourKJ / 60f)} kJ"
                    else "${String.format("%.2f", resultData.bmrPerHour / 60f)} kcal",
                    icon = "⚡",
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Equivalence row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🔄 Unit Conversion",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${resultData.primaryBMR.toInt()} kcal/day",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "=",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${resultData.bmrInKJ.toInt()} kJ/day",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownItem(
    label: String,
    value: String,
    icon: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VerticalDividerLine() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(60.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

// ============================================================
// Formula Comparison Card
// ============================================================
@Composable
private fun FormulaComparisonCard(resultData: BMRResultData) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header
            Surface(
                onClick = { expanded = !expanded },
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📊", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Formula Comparison",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${resultData.allFormulaResults.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        animationSpec = tween(300),
                        label = "chevron"
                    )
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp, end = 16.dp, bottom = 16.dp
                    )
                ) {
                    // Range indicator
                    if (resultData.allFormulaResults.size > 1) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(
                                    alpha = 0.3f
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Range",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${resultData.lowestBMR.toInt()} - ${resultData.highestBMR.toInt()} kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Difference",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${resultData.bmrRange.toInt()} kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Average",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${resultData.averageBMR.toInt()} kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Visual bar comparison
                    val maxBMR = resultData.highestBMR
                    val sortedResults = resultData.allFormulaResults.entries.toList()
                        .sortedByDescending { it.value }

                    sortedResults.forEachIndexed { index, entry ->
                        val formula = entry.key
                        val value = entry.value
                        val isSelected = formula == resultData.selectedFormula

                        FormulaComparisonBar(
                            formulaName = formula.displayName,
                            value = value,
                            maxValue = maxBMR,
                            isSelected = isSelected,
                            isRecommended = formula.isRecommended,
                            requiresBodyFat = formula.requiresBodyFat,
                            index = index
                        )

                        if (index < sortedResults.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Note about unavailable formulas
                    val unavailableCount = BMRFormula.entries.size - resultData.allFormulaResults.size
                    if (unavailableCount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$unavailableCount formula(s) not shown (require body fat % input)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormulaComparisonBar(
    formulaName: String,
    value: Float,
    maxValue: Float,
    isSelected: Boolean,
    isRecommended: Boolean,
    requiresBodyFat: Boolean,
    index: Int
) {
    val barFraction = if (maxValue > 0) (value / maxValue).coerceIn(0f, 1f) else 0f

    val animatedFraction = remember { Animatable(0f) }
    LaunchedEffect(barFraction) {
        animatedFraction.animateTo(
            targetValue = barFraction,
            animationSpec = tween(
                durationMillis = 600,
                delayMillis = index * 80,
                easing = FastOutSlowInEasing
            )
        )
    }

    val barColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    else Color.Transparent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ) else null
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
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
                    Text(
                        text = formulaName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "✓", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    if (isRecommended && !isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "⭐", fontSize = 10.sp)
                    }
                    if (requiresBodyFat) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "BF%",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${value.toInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = animatedFraction.value)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (isSelected) listOf(
                                    barColor.copy(alpha = 0.7f),
                                    barColor
                                ) else listOf(
                                    barColor,
                                    barColor
                                )
                            )
                        )
                )
            }
        }
    }
}

private fun getLevelColor(level: com.health.calculator.bmi.tracker.data.model.BMRLevel): Color {
    return when (level) {
        com.health.calculator.bmi.tracker.data.model.BMRLevel.LOW -> Color(0xFF2196F3)
        com.health.calculator.bmi.tracker.data.model.BMRLevel.BELOW_AVERAGE -> Color(0xFF4CAF50)
        com.health.calculator.bmi.tracker.data.model.BMRLevel.AVERAGE -> Color(0xFF4CAF50)
        com.health.calculator.bmi.tracker.data.model.BMRLevel.ABOVE_AVERAGE -> Color(0xFFFFC107)
        com.health.calculator.bmi.tracker.data.model.BMRLevel.HIGH -> Color(0xFFFF9800)
    }
}
