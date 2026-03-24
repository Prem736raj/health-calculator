// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/TEFSection.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.EnergyComponent
import com.health.calculator.bmi.tracker.data.model.MacroColors
import com.health.calculator.bmi.tracker.data.model.TEFData
import com.health.calculator.bmi.tracker.ui.utils.CascadeAnimatedItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Energy component colors
private val BMRColor = Color(0xFF5C6BC0)       // Indigo
private val ActivityColor = Color(0xFF26A69A)   // Teal
private val TEFColor = Color(0xFFFF7043)        // Deep Orange

@Composable
fun TEFSection(
    tefData: TEFData,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        Column {
            // ---- TEF Result Card ----
            CascadeAnimatedItem(index = 0, baseDelay = 80) {
                TEFResultCard(tefData = tefData)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Macro TEF Breakdown ----
            CascadeAnimatedItem(index = 1, baseDelay = 80) {
                MacroTEFBreakdownCard(tefData = tefData)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Complete Energy Expenditure ----
            CascadeAnimatedItem(index = 2, baseDelay = 80) {
                CompleteEnergyBreakdownCard(tefData = tefData)
            }
        }
    }
}

// ============================================================
// TEF Result Card
// ============================================================
@Composable
private fun TEFResultCard(tefData: TEFData) {
    val animatedTEF = remember { Animatable(0f) }
    LaunchedEffect(tefData.totalTEF) {
        animatedTEF.snapTo(0f)
        animatedTEF.animateTo(
            targetValue = tefData.totalTEF,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🌡️", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Thermic Effect of Food",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Energy spent digesting your food",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TEF Value Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${animatedTEF.value.toInt()}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = TEFColor,
                    fontSize = 44.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                    Text(
                        text = "kcal/day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // TEF percentage badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TEFColor.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, TEFColor.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = "≈ ${tefData.tefPercentOfIntake.roundToInt()}% of your food intake",
                        style = MaterialTheme.typography.labelMedium,
                        color = TEFColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Range indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Range: ${tefData.totalTEFLow.toInt()} – ${tefData.totalTEFHigh.toInt()} kcal/day",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interpretation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TEFColor.copy(alpha = 0.05f)
                )
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Text(text = "💡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tefData.getInterpretation(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Personalized insight
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                )
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Text(text = "🎯", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tefData.getPersonalizedInsight(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ============================================================
// Macro TEF Breakdown Card
// ============================================================
@Composable
private fun MacroTEFBreakdownCard(tefData: TEFData) {
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
                        Text(text = "🔬", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TEF by Macronutrient",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        animationSpec = tween(300),
                        label = "chevronTEF"
                    )
                    Icon(
                        Icons.Outlined.ExpandCircleDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation }
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
                    // Thermic effect rate reference
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Thermic Effect Rates:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TEFRateChip("Protein", "20-35%", MacroColors.Protein)
                                TEFRateChip("Carbs", "5-15%", MacroColors.Carbs)
                                TEFRateChip("Fat", "0-5%", MacroColors.Fat)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Individual macro TEF cards
                    MacroTEFItem(
                        emoji = "🔵",
                        label = "Protein",
                        intake = tefData.proteinCalories,
                        tefCalories = tefData.proteinTEF,
                        tefLow = tefData.proteinTEFLow,
                        tefHigh = tefData.proteinTEFHigh,
                        rateRange = "20-35%",
                        rateMid = TEFData.PROTEIN_TEF_MID,
                        color = MacroColors.Protein,
                        totalTEF = tefData.totalTEF
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MacroTEFItem(
                        emoji = "🟡",
                        label = "Carbohydrates",
                        intake = tefData.carbsCalories,
                        tefCalories = tefData.carbsTEF,
                        tefLow = tefData.carbsTEFLow,
                        tefHigh = tefData.carbsTEFHigh,
                        rateRange = "5-15%",
                        rateMid = TEFData.CARBS_TEF_MID,
                        color = MacroColors.Carbs,
                        totalTEF = tefData.totalTEF
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MacroTEFItem(
                        emoji = "🟠",
                        label = "Fat",
                        intake = tefData.fatCalories,
                        tefCalories = tefData.fatTEF,
                        tefLow = tefData.fatTEFLow,
                        tefHigh = tefData.fatTEFHigh,
                        rateRange = "0-5%",
                        rateMid = TEFData.FAT_TEF_MID,
                        color = MacroColors.Fat,
                        totalTEF = tefData.totalTEF
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total row
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🌡️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Total TEF",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${tefData.totalTEF.toInt()} kcal/day",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TEFColor
                            )
                            Text(
                                text = "Range: ${tefData.totalTEFLow.toInt()} – ${tefData.totalTEFHigh.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TEFRateChip(label: String, rate: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            text = rate,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun MacroTEFItem(
    emoji: String,
    label: String,
    intake: Float,
    tefCalories: Float,
    tefLow: Float,
    tefHigh: Float,
    rateRange: String,
    rateMid: Float,
    color: Color,
    totalTEF: Float
) {
    val proportion = if (totalTEF > 0) tefCalories / totalTEF else 0f
    val animatedBar = remember { Animatable(0f) }

    LaunchedEffect(proportion) {
        animatedBar.animateTo(
            proportion.coerceIn(0f, 1f),
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.04f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, color.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Intake: ${intake.toInt()} kcal × ${(rateMid * 100).roundToInt()}% avg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${tefCalories.toInt()} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "${tefLow.toInt()}–${tefHigh.toInt()} range",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Proportion of total TEF bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = animatedBar.value)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color.copy(alpha = 0.6f))
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${(proportion * 100).roundToInt()}% of total TEF",
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f),
                fontSize = 9.sp
            )
        }
    }
}

// ============================================================
// Complete Energy Expenditure Breakdown Card
// ============================================================
@Composable
private fun CompleteEnergyBreakdownCard(tefData: TEFData) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val components = listOf(
        EnergyComponent(
            label = "Basal Metabolic Rate",
            emoji = "🔥",
            calories = tefData.bmr,
            percentage = tefData.bmrPercentOfTotal,
            description = "Energy for basic life functions at rest",
            color = BMRColor
        ),
        EnergyComponent(
            label = "Physical Activity",
            emoji = "🏃",
            calories = tefData.activityCalories,
            percentage = tefData.activityPercentOfTotal,
            description = "Energy for exercise and daily movement",
            color = ActivityColor
        ),
        EnergyComponent(
            label = "Thermic Effect of Food",
            emoji = "🌡️",
            calories = tefData.totalTEF,
            percentage = tefData.tefPercentOfTotal,
            description = "Energy for digesting and processing food",
            color = TEFColor
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚡", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Complete Energy Expenditure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your full daily energy breakdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Three-segment donut chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnergyDonutChart(
                    bmrPct = tefData.bmrPercentOfTotal,
                    activityPct = tefData.activityPercentOfTotal,
                    tefPct = tefData.tefPercentOfTotal,
                    totalCalories = tefData.adjustedTDEE,
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.width(20.dp))

                // Legend
                Column {
                    components.forEach { component ->
                        EnergyLegendItem(component = component)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stacked horizontal bar
            EnergyStackedBar(
                bmrPct = tefData.bmrPercentOfTotal,
                activityPct = tefData.activityPercentOfTotal,
                tefPct = tefData.tefPercentOfTotal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed component cards
            components.forEachIndexed { index, component ->
                EnergyComponentCard(
                    component = component,
                    index = index
                )
                if (index < components.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Total
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Daily Energy Expenditure",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "BMR + Activity + TEF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        val animTotal = remember { Animatable(0f) }
                        LaunchedEffect(tefData.adjustedTDEE) {
                            animTotal.snapTo(0f)
                            animTotal.animateTo(
                                tefData.adjustedTDEE,
                                tween(800, easing = FastOutSlowInEasing)
                            )
                        }
                        Text(
                            text = "${animTotal.value.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "kcal/day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Equation display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EquationChip(
                        value = "${tefData.bmr.toInt()}",
                        label = "BMR",
                        color = BMRColor
                    )
                    Text(
                        text = " + ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    EquationChip(
                        value = "${tefData.activityCalories.toInt()}",
                        label = "Activity",
                        color = ActivityColor
                    )
                    Text(
                        text = " + ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    EquationChip(
                        value = "${tefData.totalTEF.toInt()}",
                        label = "TEF",
                        color = TEFColor
                    )
                    Text(
                        text = " = ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    EquationChip(
                        value = "${tefData.adjustedTDEE.toInt()}",
                        label = "Total",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EquationChip(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f),
            fontSize = 9.sp
        )
    }
}

// ============================================================
// Energy Donut Chart (3 segments)
// ============================================================
@Composable
private fun EnergyDonutChart(
    bmrPct: Float,
    activityPct: Float,
    tefPct: Float,
    totalCalories: Float,
    modifier: Modifier = Modifier
) {
    val total = (bmrPct + activityPct + tefPct).coerceAtLeast(1f)
    val bmrSweep = (bmrPct / total) * 360f
    val actSweep = (activityPct / total) * 360f
    val tefSweep = (tefPct / total) * 360f

    val animBMR = remember { Animatable(0f) }
    val animAct = remember { Animatable(0f) }
    val animTEF = remember { Animatable(0f) }

    LaunchedEffect(bmrSweep, actSweep, tefSweep) {
        launch {
            animBMR.animateTo(bmrSweep, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            delay(150)
            animAct.animateTo(actSweep, tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            delay(300)
            animTEF.animateTo(tefSweep, tween(800, easing = FastOutSlowInEasing))
        }
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            val gapDegrees = 4f
            val radius = (size.minDimension - strokeWidth) / 2
            val arcSize = Size(radius * 2, radius * 2)
            val topLeft = Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            )

            // Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(width = strokeWidth)
            )

            // BMR arc
            var startAngle = -90f
            drawArc(
                color = BMRColor,
                startAngle = startAngle,
                sweepAngle = (animBMR.value - gapDegrees).coerceAtLeast(0f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            // Activity arc
            startAngle += animBMR.value
            drawArc(
                color = ActivityColor,
                startAngle = startAngle,
                sweepAngle = (animAct.value - gapDegrees).coerceAtLeast(0f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            // TEF arc
            startAngle += animAct.value
            drawArc(
                color = TEFColor,
                startAngle = startAngle,
                sweepAngle = (animTEF.value - gapDegrees).coerceAtLeast(0f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${totalCalories.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "kcal/day",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Text(
                text = "total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun EnergyLegendItem(component: EnergyComponent) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(component.color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = component.emoji + " " + component.label.split(" ").first(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp
            )
            Text(
                text = "${component.percentage.roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = component.color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

// ============================================================
// Stacked Energy Bar
// ============================================================
@Composable
private fun EnergyStackedBar(
    bmrPct: Float,
    activityPct: Float,
    tefPct: Float
) {
    val total = (bmrPct + activityPct + tefPct).coerceAtLeast(1f)
    val bmrFrac = bmrPct / total
    val actFrac = activityPct / total
    val tefFrac = tefPct / total

    val animBMR by animateFloatAsState(bmrFrac, tween(600), label = "bmrBar")
    val animAct by animateFloatAsState(actFrac, tween(600), label = "actBar")

    Column {
        // Labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BMR ${bmrPct.roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = BMRColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
            Text(
                text = "Activity ${activityPct.roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = ActivityColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
            Text(
                text = "TEF ${tefPct.roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = TEFColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(animBMR.coerceAtLeast(0.01f))
                        .background(BMRColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(animAct.coerceAtLeast(0.01f))
                        .background(ActivityColor)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight((1f - animBMR - animAct).coerceAtLeast(0.01f))
                        .background(TEFColor)
                )
            }
        }
    }
}

// ============================================================
// Energy Component Detail Card
// ============================================================
@Composable
private fun EnergyComponentCard(
    component: EnergyComponent,
    index: Int
) {
    val animatedCalories = remember { Animatable(0f) }
    LaunchedEffect(component.calories) {
        animatedCalories.snapTo(0f)
        kotlinx.coroutines.delay((index * 150).toLong())
        animatedCalories.animateTo(
            component.calories,
            tween(600, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = component.color.copy(alpha = 0.06f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, component.color.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = component.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = component.label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = component.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${animatedCalories.value.toInt()} kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = component.color
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = component.color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${component.percentage.roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = component.color,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
