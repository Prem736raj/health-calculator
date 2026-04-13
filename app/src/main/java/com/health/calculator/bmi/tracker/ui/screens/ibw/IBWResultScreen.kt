package com.health.calculator.bmi.tracker.ui.screens.ibw

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.health.calculator.bmi.tracker.data.model.IBWHistoryEntry
import com.health.calculator.bmi.tracker.data.model.IBWResult
import com.health.calculator.bmi.tracker.data.repository.IBWStatistics
import com.health.calculator.bmi.tracker.domain.usecase.AdjustedWeightMetrics

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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFF9800).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFFE65100),
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

        // Motivational Weight Comparison
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
                Icon(Icons.AutoMirrored.Filled.MenuBook, null, Modifier.size(16.dp))
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

    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorSecondary = MaterialTheme.colorScheme.secondary
    val colorTertiary = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Ideal Body Weight",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Animated Weight Gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    // Background Track
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.White.copy(alpha = 0.5f),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Progress
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                0f to colorPrimary,
                                0.5f to colorSecondary,
                                1f to colorTertiary
                            ),
                            startAngle = 135f,
                            sweepAngle = 270f * (animatedWeight / (if(showInKg) 120f else 260f)).coerceAtMost(1f),
                            useCenter = false,
                            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.1f".format(animatedWeight),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
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

                Spacer(modifier = Modifier.height(24.dp))

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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f).height(56.dp),
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
                modifier = Modifier.weight(1f).height(56.dp),
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
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
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
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Recalculate")
        }
    }
}
