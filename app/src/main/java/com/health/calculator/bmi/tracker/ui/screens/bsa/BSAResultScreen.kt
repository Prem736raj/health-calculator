package com.health.calculator.bmi.tracker.ui.screens.bsa

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
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
import com.health.calculator.bmi.tracker.calculator.BSAResult
import com.health.calculator.bmi.tracker.ui.screens.bsa.*
import com.health.calculator.bmi.tracker.ui.theme.*
import kotlin.math.abs

private const val SQ_METER_TO_SQ_FEET = 10.7639f

@Composable
fun BSAResultScreen(
    result: BSAResult,
    isSaved: Boolean,
    isMale: Boolean?,
    onSave: () -> Unit,
    onRecalculate: () -> Unit,
    onShare: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(result) {
        animationStarted = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Smooth BSA value animation with bounce
    val animatedBSA = animateBSAValue(
        targetValue = if (animationStarted) result.primaryBSA else 0f
    )

    // Sequential section visibility
    val sectionVisibilities = animateSequentialVisibility(
        count = 7, // number of main sections
        trigger = animationStarted,
        baseDelay = 300,
        stagger = 200
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // === Primary Result Card (always visible first) ===
        AnimatedVisibility(
            visible = sectionVisibilities.getOrElse(0) { false },
            enter = scaleIn(tween(600)) + fadeIn(tween(600))
        ) {
            PrimaryResultCard(bsa = animatedBSA, result = result, animationStarted = animationStarted)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Unit Conversion ===
        AnimatedVisibility(
            visible = sectionVisibilities.getOrElse(1) { false },
            enter = slideInHorizontally(tween(400)) { -it } + fadeIn(tween(400))
        ) {
            UnitConversionCard(result = result)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Body Silhouette ===
        AnimatedVisibility(
            visible = sectionVisibilities.getOrElse(2) { false },
            enter = fadeIn(tween(600))
        ) {
            BodySilhouetteVisualization(bsa = result.primaryBSA, animationStarted = animationStarted)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Gender Comparison ===
        AnimatedVisibility(
            visible = sectionVisibilities.getOrElse(3) { false },
            enter = slideInHorizontally(tween(400)) { it } + fadeIn(tween(400))
        ) {
            GenderComparisonCard(bsa = result.primaryBSA, isMale = isMale)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Input Summary ===
        AnimatedVisibility(
            visible = sectionVisibilities.getOrElse(4) { false },
            enter = slideInVertically(tween(400)) { it / 2 } + fadeIn(tween(400))
        ) {
            InputSummaryCard(result = result)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Formula Comparison with animated bars ===
        AnimatedVisibility(
            visible = sectionVisibilities.getOrElse(5) { false },
            enter = expandVertically(tween(500)) + fadeIn(tween(500))
        ) {
            FormulaComparisonSection(result = result)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Statistics + Recommendation + Medical + Actions ===
        AnimatedVisibility(
            visible = sectionVisibilities.getOrElse(6) { false },
            enter = slideInVertically(tween(400)) { it / 3 } + fadeIn(tween(400))
        ) {
            Column {
                FormulaStatisticsCard(result = result)
                Spacer(modifier = Modifier.height(16.dp))
                FormulaRecommendationCard()
                Spacer(modifier = Modifier.height(16.dp))
                BSAMedicalApplicationsSection(bsa = result.primaryBSA)
                Spacer(modifier = Modifier.height(24.dp))
                ActionButtons(isSaved = isSaved, onSave = onSave, onRecalculate = onRecalculate, onShare = onShare)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PrimaryResultCard(
    bsa: Float,
    result: BSAResult,
    animationStarted: Boolean
) {
    AnimatedVisibility(
        visible = animationStarted,
        enter = scaleIn(tween(600)) + fadeIn(tween(600))
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📐", fontSize = 44.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Body Surface Area",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "%.4f".format(bsa),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "m²",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${result.selectedFormula.name} Formula (${result.selectedFormula.year})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitConversionCard(result: BSAResult) {
    val bsaSqFt = result.primaryBSA * SQ_METER_TO_SQ_FEET
    val bsaSqCm = result.primaryBSA * 10000f

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Unit Conversions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UnitValueColumn(
                    value = "%.4f".format(result.primaryBSA),
                    unit = "m²",
                    label = "Sq Meters"
                )
                UnitValueColumn(
                    value = "%.2f".format(bsaSqFt),
                    unit = "ft²",
                    label = "Sq Feet"
                )
                UnitValueColumn(
                    value = "%.0f".format(bsaSqCm),
                    unit = "cm²",
                    label = "Sq Centimeters"
                )
            }
        }
    }
}

@Composable
private fun UnitValueColumn(value: String, unit: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BodySilhouetteVisualization(
    bsa: Float,
    animationStarted: Boolean
) {
    val fillProgress by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "body_fill"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Body Surface Visualization",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(100.dp)
                        .height(190.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Body silhouette outline (refined points)
                    val bodyPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.0f)
                        cubicTo(w * 0.65f, h * 0.0f, w * 0.7f, h * 0.05f, w * 0.7f, h * 0.08f)
                        cubicTo(w * 0.7f, h * 0.12f, w * 0.65f, h * 0.15f, w * 0.55f, h * 0.15f)
                        lineTo(w * 0.55f, h * 0.18f)
                        cubicTo(w * 0.7f, h * 0.18f, w * 0.9f, h * 0.2f, w * 0.95f, h * 0.22f)
                        lineTo(w * 1.0f, h * 0.42f)
                        lineTo(w * 0.85f, h * 0.42f)
                        lineTo(w * 0.75f, h * 0.28f)
                        lineTo(w * 0.72f, h * 0.55f)
                        cubicTo(w * 0.73f, h * 0.58f, w * 0.78f, h * 0.6f, w * 0.78f, h * 0.62f)
                        lineTo(w * 0.78f, h * 0.92f)
                        lineTo(w * 0.82f, h * 0.95f)
                        lineTo(w * 0.82f, h * 1.0f)
                        lineTo(w * 0.6f, h * 1.0f)
                        lineTo(w * 0.6f, h * 0.95f)
                        lineTo(w * 0.6f, h * 0.62f)
                        lineTo(w * 0.5f, h * 0.6f)
                        lineTo(w * 0.4f, h * 0.62f)
                        lineTo(w * 0.4f, h * 0.95f)
                        lineTo(w * 0.4f, h * 1.0f)
                        lineTo(w * 0.18f, h * 1.0f)
                        lineTo(w * 0.18f, h * 0.95f)
                        lineTo(w * 0.22f, h * 0.92f)
                        lineTo(w * 0.22f, h * 0.62f)
                        cubicTo(w * 0.22f, h * 0.6f, w * 0.27f, h * 0.58f, w * 0.28f, h * 0.55f)
                        lineTo(w * 0.25f, h * 0.28f)
                        lineTo(w * 0.15f, h * 0.42f)
                        lineTo(w * 0.0f, h * 0.42f)
                        lineTo(w * 0.05f, h * 0.22f)
                        cubicTo(w * 0.1f, h * 0.2f, w * 0.3f, h * 0.18f, w * 0.45f, h * 0.18f)
                        lineTo(w * 0.45f, h * 0.15f)
                        cubicTo(w * 0.35f, h * 0.15f, w * 0.3f, h * 0.12f, w * 0.3f, h * 0.08f)
                        cubicTo(w * 0.3f, h * 0.05f, w * 0.35f, h * 0.0f, w * 0.5f, h * 0.0f)
                        close()
                    }

                    // Background silhouette
                    drawPath(
                        path = bodyPath,
                        color = surfaceVariant,
                        style = Fill
                    )

                    // Animated fill from bottom
                    val fillHeight = h * fillProgress
                    drawContext.canvas.save()
                    drawContext.canvas.clipRect(0f, h - fillHeight, w, h)
                    drawPath(
                        path = bodyPath,
                        color = primaryColor.copy(alpha = 0.35f),
                        style = Fill
                    )
                    drawContext.canvas.restore()

                    // Outline
                    drawPath(
                        path = bodyPath,
                        color = primaryColor.copy(alpha = 0.6f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // BSA value overlay
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp)
                ) {
                    Text(
                        text = "%.2f m²".format(bsa),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                    Text(
                        text = "Total surface",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The colored area represents the total external surface of your body as calculated by the selected formula.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GenderComparisonCard(bsa: Float, isMale: Boolean?) {
    val averageMale = 1.9f
    val averageFemale = 1.6f

    val gender = isMale ?: true
    val averageBSA = if (gender) averageMale else averageFemale
    val genderLabel = if (gender) "male" else "female"

    val difference = bsa - averageBSA
    val percentDiff = ((difference / averageBSA) * 100f)
    val isAbove = percentDiff > 0

    val comparisonColor = when {
        abs(percentDiff) < 5f -> HealthGreen
        abs(percentDiff) < 15f -> HealthYellow
        else -> HealthOrange
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (bsa / (averageBSA * 1.5f)).coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "gender_comparison"
    )

    val animatedAvgProgress by animateFloatAsState(
        targetValue = averageBSA / (averageBSA * 1.5f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "avg_marker"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = comparisonColor.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (gender) "♂" else "♀", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comparison with Average",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Your BSA vs Average Adult ${genderLabel.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // You
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("You", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(40.dp))
                    Box(modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animatedProgress).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primary))
                        Text("%.2f m²".format(bsa), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Avg
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Avg", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(40.dp))
                    Box(modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animatedAvgProgress).clip(RoundedCornerShape(6.dp)).background(comparisonColor.copy(alpha = 0.6f)))
                        Text("%.2f m²".format(averageBSA), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = comparisonColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(if (isAbove) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown, contentDescription = null, tint = comparisonColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your BSA is ${"%.1f".format(abs(percentDiff))}% ${if (isAbove) "above" else "below"} average for adult ${genderLabel}s",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = comparisonColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ReferenceChip("Avg Male", "~1.9 m²", isHighlighted = gender)
                ReferenceChip("Avg Female", "~1.6 m²", isHighlighted = !gender)
                ReferenceChip("Newborn", "~0.25 m²", isHighlighted = false)
            }
        }
    }
}

@Composable
private fun ReferenceChip(label: String, value: String, isHighlighted: Boolean) {
    Surface(
        color = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InputSummaryCard(result: BSAResult) {
    val totalInches = result.heightCm / 2.54f
    val ft = (totalInches / 12).toInt()
    val inch = totalInches - (ft * 12)
    val weightLbs = result.weightKg / 0.453592f

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚖️", fontSize = 20.sp)
                Text("%.1f kg".format(result.weightKg), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("(%.1f lbs)".format(weightLbs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📏", fontSize = 20.sp)
                Text("%.1f cm".format(result.heightCm), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("(%d'%.0f\")".format(ft, inch), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🧮", fontSize = 20.sp)
                Text(result.selectedFormula.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(result.selectedFormula.year, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FormulaComparisonSection(result: BSAResult) {
    val sortedResults = result.allResults.sortedByDescending { it.second }
    val maxBSA = sortedResults.firstOrNull()?.second ?: 1f

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.TableChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("All Formulas Comparison", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            sortedResults.forEachIndexed { index, (formula, value) ->
                val isSelected = formula.id == result.selectedFormula.id
                val barProgress = animateBarProgress(
                    targetProgress = value / maxBSA,
                    index = index
                )
                val labelColor = when (formula.label) {
                    "Most Used" -> HealthBlue
                    "Simplified" -> HealthGreen
                    "Pediatric" -> HealthOrange
                    "Japanese", "Asian" -> HealthTeal
                    "Modern" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) Icon(Icons.Filled.Star, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    else Spacer(modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.width(90.dp)) {
                        Text(formula.name, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium, maxLines = 1)
                        Surface(color = labelColor.copy(alpha = 0.12f), shape = RoundedCornerShape(3.dp)) {
                            Text(formula.label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = labelColor, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f).height(18.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(barProgress).clip(RoundedCornerShape(4.dp)).background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("%.4f".format(value), style = MaterialTheme.typography.bodySmall, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(52.dp), textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun FormulaStatisticsCard(result: BSAResult) {
    val values = result.allResults.map { it.second }
    val min = values.minOrNull() ?: 0f
    val max = values.maxOrNull() ?: 0f
    val avg = values.average().toFloat()
    val range = max - min
    val minFormula = result.allResults.minByOrNull { it.second }?.first?.name ?: ""
    val maxFormula = result.allResults.maxByOrNull { it.second }?.first?.name ?: ""

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Formula Statistics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatColumn("Minimum", "%.4f m²".format(min), minFormula, HealthBlue)
                StatColumn("Average", "%.4f m²".format(avg), "All formulas", HealthGreen)
                StatColumn("Maximum", "%.4f m²".format(max), maxFormula, HealthOrange)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Range spread", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        Text("%.4f m² (%.1f%%)".format(range, if (avg > 0) (range / avg) * 100 else 0), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)))
                        val selectedPos = if (range > 0) ((result.primaryBSA - min) / range).coerceIn(0f, 1f) else 0.5f
                        Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = (selectedPos * 280).dp).size(width = 4.dp, height = 8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("▲ Your selected formula (${result.selectedFormula.name})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, subLabel: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = color)
        Text(subLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FormulaRecommendationCard() {
    Card(colors = CardDefaults.cardColors(containerColor = HealthBlue.copy(alpha = 0.06f)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Formula Recommendations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = HealthBlue)
            }
            Spacer(modifier = Modifier.height(10.dp))
            FormulaRecItem("🏥", "Du Bois & Du Bois", "Most widely used in clinical practice worldwide. Default choice for most applications.")
            FormulaRecItem("⚡", "Mosteller", "Simplest formula with minimal math. Results very close to Du Bois for most adults.")
            FormulaRecItem("👶", "Haycock", "Preferred for pediatric patients (infants and children). Most accurate for small body sizes.")
            FormulaRecItem("🌏", "Fujimoto / Takahira", "May be more accurate for East Asian populations.")
            FormulaRecItem("🔬", "Shuter & Aslani", "Modern formula using CT-based measurements. Considered most anatomically accurate.")
            Spacer(modifier = Modifier.height(8.dp))
            Text("ℹ️ For most clinical purposes, any formula will give results within a few percent of each other. Your doctor will use the formula standard at their institution.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun FormulaRecItem(emoji: String, formula: String, recommendation: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 14.sp, modifier = Modifier.width(22.dp))
        Column {
            Text(formula, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(recommendation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun ActionButtons(isSaved: Boolean, onSave: () -> Unit, onRecalculate: () -> Unit, onShare: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onSave() }, enabled = !isSaved, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
            Icon(if (isSaved) Icons.Filled.Check else Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isSaved) "Saved" else "Save")
        }
        OutlinedButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onRecalculate() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Recalculate")
        }
        OutlinedButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onShare() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Share")
        }
    }
}
