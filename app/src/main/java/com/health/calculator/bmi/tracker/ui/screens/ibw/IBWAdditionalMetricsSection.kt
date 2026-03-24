package com.health.calculator.bmi.tracker.ui.screens.ibw

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.domain.usecase.AdjustedWeightMetrics
import com.health.calculator.bmi.tracker.domain.usecase.SportWeightNote
import com.health.calculator.bmi.tracker.domain.usecase.getSportWeightNotes
import kotlin.math.abs

@Composable
fun IBWAdditionalMetricsSection(
    metrics: AdjustedWeightMetrics,
    actualWeightKg: Double,
    idealWeightKg: Double,
    showInKg: Boolean
) {
    val unit = if (showInKg) "kg" else "lbs"
    val factor = if (showInKg) 1.0 else 2.20462

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Section Header
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Additional Metrics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 1. Weight Category Relative to IBW
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100))
        ) {
            WeightCategoryCard(
                metrics = metrics,
                actualWeightKg = actualWeightKg,
                idealWeightKg = idealWeightKg,
                showInKg = showInKg
            )
        }

        // 2. Adjusted Body Weight (only when relevant)
        AnimatedVisibility(
            visible = visible && metrics.isAdjustedRelevant,
            enter = fadeIn(tween(600, 200)) + slideInVertically(tween(600, 200))
        ) {
            AdjustedBodyWeightCard(
                adjustedWeightKg = metrics.adjustedBodyWeightKg,
                actualWeightKg = actualWeightKg,
                idealWeightKg = idealWeightKg,
                showInKg = showInKg
            )
        }

        // 3. Lean Body Weight
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(700, 300)) + slideInVertically(tween(700, 300))
        ) {
            LeanBodyWeightCard(
                metrics = metrics,
                actualWeightKg = actualWeightKg,
                showInKg = showInKg
            )
        }

        // 4. Sport-Specific Notes
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(800, 400)) + slideInVertically(tween(800, 400))
        ) {
            SportSpecificNotesCard()
        }
    }
}

@Composable
private fun WeightCategoryCard(
    metrics: AdjustedWeightMetrics,
    actualWeightKg: Double,
    idealWeightKg: Double,
    showInKg: Boolean
) {
    val unit = if (showInKg) "kg" else "lbs"
    val factor = if (showInKg) 1.0 else 2.20462
    val percent = metrics.weightCategoryPercent

    val categoryColor = when {
        percent < 80 -> Color(0xFFF44336)
        percent < 90 -> Color(0xFFFF9800)
        percent <= 110 -> Color(0xFF4CAF50)
        percent <= 120 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val categoryIcon = when {
        percent < 80 -> "⚠️"
        percent < 90 -> "📉"
        percent <= 110 -> "✅"
        percent <= 120 -> "📈"
        else -> "⚠️"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight vs Ideal",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = categoryIcon,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Percentage display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                val animatedPercent by animateFloatAsState(
                    targetValue = percent.toFloat(),
                    animationSpec = tween(1200, easing = FastOutSlowInEasing),
                    label = "percent"
                )
                Text(
                    text = "${"%.0f".format(animatedPercent)}%",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = categoryColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "of IBW",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category label
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = categoryColor.copy(alpha = 0.12f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = metrics.weightCategory,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = categoryColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual scale bar
            WeightCategoryScale(percent = percent)

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = metrics.weightCategoryDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weight details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricChip(
                    label = "Actual",
                    value = "${"%.1f".format(actualWeightKg * factor)} $unit"
                )
                MetricChip(
                    label = "Ideal",
                    value = "${"%.1f".format(idealWeightKg * factor)} $unit"
                )
                MetricChip(
                    label = "Difference",
                    value = "${if (actualWeightKg > idealWeightKg) "+" else ""}${"%.1f".format((actualWeightKg - idealWeightKg) * factor)} $unit"
                )
            }
        }
    }
}

@Composable
private fun WeightCategoryScale(percent: Double) {
    val zones = listOf(
        Triple(0f to 80f, Color(0xFFF44336), "<80%"),
        Triple(80f to 90f, Color(0xFFFF9800), "80-89%"),
        Triple(90f to 110f, Color(0xFF4CAF50), "90-110%"),
        Triple(110f to 120f, Color(0xFFFF9800), "111-120%"),
        Triple(120f to 160f, Color(0xFFF44336), ">120%")
    )

    val animatedPos by animateFloatAsState(
        targetValue = ((percent.toFloat() - 60f) / 100f).coerceIn(0f, 1f),
        animationSpec = tween(1200, 200, easing = FastOutSlowInEasing),
        label = "scalePos"
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barY = size.height * 0.4f
                val barHeight = 10.dp.toPx()
                val totalWidth = size.width
                val scaleMin = 60f
                val scaleMax = 160f
                val scaleRange = scaleMax - scaleMin

                zones.forEach { (range, color, _) ->
                    val startX = ((range.first - scaleMin) / scaleRange * totalWidth).coerceIn(0f, totalWidth)
                    val endX = ((range.second - scaleMin) / scaleRange * totalWidth).coerceIn(0f, totalWidth)
                    drawLine(
                        color = color.copy(alpha = 0.4f),
                        start = Offset(startX, barY),
                        end = Offset(endX, barY),
                        strokeWidth = barHeight,
                        cap = StrokeCap.Round
                    )
                }

                // Position marker
                val markerX = animatedPos * totalWidth
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = Offset(markerX, barY)
                )
                val markerColor = when {
                    percent < 80 -> Color(0xFFF44336)
                    percent < 90 -> Color(0xFFFF9800)
                    percent <= 110 -> Color(0xFF4CAF50)
                    percent <= 120 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
                drawCircle(
                    color = markerColor,
                    radius = 7.dp.toPx(),
                    center = Offset(markerX, barY)
                )
            }
        }

        // Scale labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("60%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text("80%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text("100%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            Text("120%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text("160%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AdjustedBodyWeightCard(
    adjustedWeightKg: Double?,
    actualWeightKg: Double,
    idealWeightKg: Double,
    showInKg: Boolean
) {
    val unit = if (showInKg) "kg" else "lbs"
    val factor = if (showInKg) 1.0 else 2.20462

    adjustedWeightKg ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE91E63).copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE91E63).copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Adjusted Body Weight",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "For clinical use when weight > 120% of IBW",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ABW value
            val animatedAbw by animateFloatAsState(
                targetValue = (adjustedWeightKg * factor).toFloat(),
                animationSpec = tween(1000, easing = FastOutSlowInEasing),
                label = "abw"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${"%.1f".format(animatedAbw)}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE91E63)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE91E63).copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formula explanation
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Formula: IBW + 0.4 × (Actual Weight − IBW)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${"%.1f".format(idealWeightKg * factor)} + 0.4 × (${"%.1f".format(actualWeightKg * factor)} − ${"%.1f".format(idealWeightKg * factor)}) = ${"%.1f".format(adjustedWeightKg * factor)} $unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Usage note
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFE91E63).copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Used by healthcare providers for medication dosing in patients significantly above ideal weight. This is not a weight goal — it's a clinical reference value.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun LeanBodyWeightCard(
    metrics: AdjustedWeightMetrics,
    actualWeightKg: Double,
    showInKg: Boolean
) {
    val unit = if (showInKg) "kg" else "lbs"
    val factor = if (showInKg) 1.0 else 2.20462
    val lbw = metrics.leanBodyWeightKg * factor
    val fatPercent = metrics.getBodyFatPercent(actualWeightKg)
    val fatMassKg = actualWeightKg - metrics.leanBodyWeightKg
    val fatMass = fatMassKg * factor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Lean Body Weight",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Estimated weight minus body fat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body composition visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Lean mass
                CompositionCircle(
                    value = "${"%.1f".format(lbw)}",
                    unit = unit,
                    label = "Lean Mass",
                    percent = (100 - fatPercent).toFloat(),
                    color = MaterialTheme.colorScheme.primary
                )

                // Fat mass
                CompositionCircle(
                    value = "${"%.1f".format(fatMass)}",
                    unit = unit,
                    label = "Fat Mass",
                    percent = fatPercent.toFloat(),
                    color = Color(0xFFFF9800)
                )

                // Body Fat %
                CompositionCircle(
                    value = "${"%.1f".format(fatPercent)}",
                    unit = "%",
                    label = "Body Fat",
                    percent = fatPercent.toFloat(),
                    color = Color(0xFFE91E63)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Composition bar
            BodyCompositionBar(
                leanPercent = (100 - fatPercent).toFloat(),
                fatPercent = fatPercent.toFloat()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Info note
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "This is an estimation based on BMI-derived formulas. For accurate body composition, consider DEXA scan or bioimpedance analysis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CompositionCircle(
    value: String,
    unit: String,
    label: String,
    percent: Float,
    color: Color
) {
    val animatedSweep by animateFloatAsState(
        targetValue = percent * 3.6f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "sweep"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            val trackColor = color.copy(alpha = 0.12f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = color
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun BodyCompositionBar(leanPercent: Float, fatPercent: Float) {
    val animatedLean by animateFloatAsState(
        targetValue = leanPercent,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "lean"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(animatedLean.coerceAtLeast(1f))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((100f - animatedLean).coerceAtLeast(1f))
                    .background(Color(0xFFFF9800))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Lean ${"%.0f".format(leanPercent)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF9800))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Fat ${"%.0f".format(fatPercent)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SportSpecificNotesCard() {
    var expanded by remember { mutableStateOf(false) }
    val sportNotes = remember { getSportWeightNotes() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF673AB7).copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.SportsGymnastics,
                                contentDescription = null,
                                tint = Color(0xFF673AB7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sport-Specific Weight Notes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Ideal weight varies by activity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sportNotes.forEach { note ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = note.icon,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = note.sport,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF673AB7).copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = note.bmiRange,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF673AB7),
                                                modifier = Modifier.padding(
                                                    horizontal = 6.dp,
                                                    vertical = 2.dp
                                                ),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // Disclaimer
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Athletes may have ideal weights outside standard ranges due to higher muscle mass. Standard IBW formulas may not be appropriate for competitive athletes. Consult a sports medicine professional.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF9800).copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
