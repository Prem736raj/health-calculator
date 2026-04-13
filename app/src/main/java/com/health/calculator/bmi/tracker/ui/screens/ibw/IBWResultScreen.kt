package com.health.calculator.bmi.tracker.ui.screens.ibw

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import kotlin.math.abs
import com.health.calculator.bmi.tracker.data.model.IBWResult
import com.health.calculator.bmi.tracker.domain.usecase.AdjustedWeightMetrics
import com.health.calculator.bmi.tracker.ui.components.InfoCard
import com.health.calculator.bmi.tracker.data.model.IBWHistoryEntry
import com.health.calculator.bmi.tracker.data.repository.IBWStatistics

@Composable
fun IBWResultScreen(
    result: IBWResult,
    showInKg: Boolean,
    isSaved: Boolean,
    additionalMetrics: AdjustedWeightMetrics? = null,
    historyEntries: List<IBWHistoryEntry> = emptyList(),
    historyStatistics: IBWStatistics = IBWStatistics(),
    showEducational: Boolean = false,
    showHistory: Boolean = false,
    onToggleUnit: () -> Unit,
    onSave: () -> Unit,
    onRecalculate: () -> Unit,
    onShare: () -> Unit,
    onSetGoal: () -> Unit = {},
    onToggleEducational: () -> Unit = {},
    onToggleHistory: () -> Unit = {},
    onDeleteHistoryEntry: (Long) -> Unit = {},
    onNavigateToBMI: () -> Unit = {},
    onNavigateToBMR: () -> Unit = {},
    onNavigateToWHR: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Height warning banner (edge cases)
        result.heightWarning?.let { warning ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + expandVertically(tween(400))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800).copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Primary Result Card
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500))
        ) { PrimaryResultCard(result, showInKg, onToggleUnit) }
        Spacer(modifier = Modifier.height(16.dp))

        // Motivational Weight Comparison (replaces old WeightDifference + VisualScale)
        AnimatedVisibility(
            visible = visible && result.currentWeightKg != null,
            enter = fadeIn(tween(600, 200)) + slideInVertically(tween(600, 200))
        ) {
            IBWMotivationalComparison(
                result = result,
                showInKg = showInKg
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // BMI Range Card
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(700, 400)) + slideInVertically(tween(700, 400))
        ) { BMIRangeCard(result, showInKg) }
        Spacer(modifier = Modifier.height(16.dp))

        // Formula Comparison
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(800, 500)) + slideInVertically(tween(800, 500))
        ) { FormulaComparisonCard(result, showInKg) }

        // Additional Metrics
        if (additionalMetrics != null && result.currentWeightKg != null) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(900, 600)) + slideInVertically(tween(900, 600))
            ) {
                IBWAdditionalMetricsSection(
                    metrics = additionalMetrics,
                    actualWeightKg = result.currentWeightKg,
                    idealWeightKg = result.frameAdjustedDevineKg,
                    showInKg = showInKg
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(1000, 700)) + slideInVertically(tween(1000, 700))
        ) {
            ActionButtonsRow(isSaved, onSave, onRecalculate, onShare, onSetGoal)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cross-Calculator Links
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(1100, 800)) + slideInVertically(tween(1100, 800))
        ) {
            IBWCrossCalculatorLinks(
                onNavigateToBMI = onNavigateToBMI,
                onNavigateToBMR = onNavigateToBMR,
                onNavigateToWHR = onNavigateToWHR
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Educational & History Toggle Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onToggleEducational,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (showEducational)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else Color.Transparent
                )
            ) {
                Icon(Icons.Default.MenuBook, null, Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Learn", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = onToggleHistory,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (showHistory)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else Color.Transparent
                )
            ) {
                Icon(Icons.Default.History, null, Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("History (${historyEntries.size})", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Educational & History content sections
        AnimatedVisibility(
            visible = showEducational,
            enter = expandVertically(tween(400)) + fadeIn(tween(400)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(200))
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                IBWEducationalContent()
            }
        }

        AnimatedVisibility(
            visible = showHistory,
            enter = expandVertically(tween(400)) + fadeIn(tween(400)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(200))
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                IBWHistorySection(
                    entries = historyEntries,
                    statistics = historyStatistics,
                    showInKg = showInKg,
                    onDeleteEntry = onDeleteHistoryEntry
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PrimaryResultCard(
    result: IBWResult,
    showInKg: Boolean,
    onToggleUnit: () -> Unit
) {
    val weight = if (showInKg) result.frameAdjustedDevineKg else result.frameAdjustedDevineLbs
    val unit = if (showInKg) "kg" else "lbs"
    
    val animatedWeight by animateFloatAsState(
        targetValue = weight.toFloat(),
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "weight_anim"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ideal Body Weight",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Animated Weight Gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                
                // Background Track
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.5f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                // Progress
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to primaryColor,
                            0.75f to secondaryColor
                        ),
                        startAngle = 135f,
                        sweepAngle = 270f * (animatedWeight / (if(showInKg) 120f else 260f)).coerceAtMost(1f),
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f".format(animatedWeight),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Text(
                        text = unit.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                onClick = onToggleUnit,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Switch to ${if (showInKg) "lbs" else "kg"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = "Adjusted for ${result.frameSize} frame",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun WeightDifferenceCard(result: IBWResult, showInKg: Boolean) {
    val currentWeight = result.currentWeightKg ?: return
    val idealWeight = result.frameAdjustedDevineKg
    val diff = idealWeight - currentWeight
    val absDiff = abs(diff)
    val unit = if (showInKg) "kg" else "lbs"
    val displayDiff = if (showInKg) absDiff else absDiff * 2.20462
    
    val isUnderIdeal = diff > 0.5
    val isOverIdeal = diff < -0.5
    val isPerfect = !isUnderIdeal && !isOverIdeal

    val cardColor = when {
        isUnderIdeal -> Color(0xFF2196F3).copy(alpha = 0.1f)
        isOverIdeal -> Color(0xFFFF9800).copy(alpha = 0.1f)
        else -> Color(0xFF4CAF50).copy(alpha = 0.1f)
    }

    val icon = when {
        isUnderIdeal -> Icons.Default.TrendingUp
        isOverIdeal -> Icons.Default.TrendingDown
        else -> Icons.Default.EmojiEvents
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isPerfect) Color(0xFF4CAF50) else if (isUnderIdeal) Color(0xFF2196F3) else Color(0xFFFF9800)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = when {
                        isUnderIdeal -> "You're below your ideal weight"
                        isOverIdeal -> "You're above your ideal weight"
                        else -> "You're at your ideal weight!"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                if (!isPerfect) {
                    Text(
                        text = "Difference: ${"%.1f".format(displayDiff)} $unit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualWeightScale(result: IBWResult, showInKg: Boolean) {
    val current = result.currentWeightKg ?: return
    val ideal = result.frameAdjustedDevineKg
    val minBmi = result.bmiLowerKg
    val maxBmi = result.bmiUpperKg
    
    val minScale = (minOf(current, minBmi, ideal) - 5).coerceAtLeast(30.0)
    val maxScale = (maxOf(current, maxBmi, ideal) + 5).coerceAtMost(200.0)
    val scaleRange = maxOf(0.1, maxScale - minScale)

    fun getPos(value: Double) = ((value - minScale) / scaleRange).toFloat().coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Weight Comparison Scale",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("BMI Healthy Zone", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${"%.1f".format(if(showInKg) minBmi else minBmi*2.20462)} - ${"%.1f".format(if(showInKg) maxBmi else maxBmi*2.20462)} ${if(showInKg) "kg" else "lbs"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    
                    // 1. Ensure values are finite and valid
                    val minBmiPos = getPos(minBmi).takeIf { it.isFinite() } ?: 0f
                    val maxBmiPos = getPos(maxBmi).takeIf { it.isFinite() } ?: 0f
                    val idealPos = getPos(ideal).takeIf { it.isFinite() } ?: 0f
                    val currentPos = getPos(current).takeIf { it.isFinite() } ?: 0f
                    
                    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                        // 2. Extra safety: Check if width/height are valid before drawing
                        if (size.width <= 0f || size.height <= 0f) return@Canvas

                        val trackHeight = 12.dp.toPx()
                        val y = size.height / 2
                        
                        drawRoundRect(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            size = size.copy(height = trackHeight),
                            topLeft = Offset(0f, y - trackHeight/2),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )
                        
                        val startX = minBmiPos * size.width
                        val endX = maxBmiPos * size.width
                        val rectWidth = maxOf(0f, endX - startX)
                        
                        // Draw only if width is valid
                        if (rectWidth > 0f) {
                            drawRect(
                                color = Color(0xFF4CAF50).copy(alpha = 0.4f),
                                topLeft = Offset(startX, y - trackHeight/2),
                                size = androidx.compose.ui.geometry.Size(rectWidth, trackHeight)
                            )
                        }
                        
                        // Ensure idealX is a valid number before drawing circle
                        val idealX = idealPos * size.width
                        if (idealX.isFinite()) {
                            drawCircle(
                                color = primaryColor,
                                radius = 6.dp.toPx(),
                                center = Offset(idealX, y)
                            )
                        }
                        
                        // Ensure currentX is a valid number before drawing rect
                        val currentX = currentPos * size.width
                        if (currentX.isFinite()) {
                            drawRect(
                                color = secondaryColor,
                                size = androidx.compose.ui.geometry.Size(4.dp.toPx(), 24.dp.toPx()),
                                topLeft = Offset(currentX - 2.dp.toPx(), y - 12.dp.toPx())
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ideal", style = MaterialTheme.typography.labelSmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.secondary))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Current", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun BMIRangeCard(result: IBWResult, showInKg: Boolean) {
    val unit = if (showInKg) "kg" else "lbs"
    val lower = if (showInKg) result.bmiLowerKg else result.bmiLowerLbs
    val upper = if (showInKg) result.bmiUpperKg else result.bmiUpperLbs

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Healthy BMI Range (18.5 - 24.9)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "For your height, a healthy weight is between ${"%.1f".format(lower)} $unit and ${"%.1f".format(upper)} $unit.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FormulaComparisonCard(result: IBWResult, showInKg: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    
    val formulas = listOf(
        "Devine (Primary)" to if(showInKg) result.devineKg else result.devineLbs,
        "Robinson" to if(showInKg) result.robinsonKg else result.robinsonLbs,
        "Miller" to if(showInKg) result.millerKg else result.millerLbs,
        "Hamwi" to if(showInKg) result.hamwiKg else result.hamwiLbs,
        "Broca Index" to if(showInKg) result.brocaKg else result.brocaLbs
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Compare Formulas",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    formulas.forEach { (name, weight) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${"%.1f".format(weight)} ${if(showInKg) "kg" else "lbs"}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(
                        "Different formulas are used by clinicians for different medical purposes. The Devine formula is the most universally accepted standard.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    isSaved: Boolean,
    onSave: () -> Unit,
    onRecalculate: () -> Unit,
    onShare: () -> Unit,
    onSetGoal: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !isSaved,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSaved) "Saved ✓" else "Save")
            }

            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }
        }

        Button(
            onClick = onSetGoal,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Set Weight Goal",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        OutlinedButton(
            onClick = onRecalculate,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Recalculate")
        }
    }
}
