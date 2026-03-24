package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.WeightUnit
import com.health.calculator.bmi.tracker.data.model.kgToLbs
import kotlinx.coroutines.delay

private val BmiAccent = Color(0xFF1E88E5)

@Composable
fun BmiResultSection(
    result: BmiResult,
    isSavedToHistory: Boolean,
    shareText: String,
    onRecalculate: () -> Unit,
    onDismissSaveConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = Color(result.category.colorHex)
    val context = LocalContext.current

    var showBmiNumber by remember { mutableStateOf(false) }
    var showGauge by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showAdvice by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var showSavedBanner by remember { mutableStateOf(false) }

    val numberScale = remember { Animatable(0.3f) }

    LaunchedEffect(result) {
        showBmiNumber = false; showGauge = false; showDetails = false
        showAdvice = false; showActions = false; showSavedBanner = false

        delay(200)
        showBmiNumber = true
        numberScale.snapTo(0.3f)
        numberScale.animateTo(1f, tween(600, easing = EaseOutBack))

        delay(200); showGauge = true
        delay(300); showDetails = true
        delay(200); showAdvice = true
        delay(200); showActions = true
        delay(300); showSavedBanner = true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Saved to History Banner ───────────────────────────────────
        AnimatedVisibility(
            visible = showSavedBanner && isSavedToHistory,
            enter = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(400, easing = EaseOutCubic)
            )
        ) {
            SavedToHistoryBanner()
        }

        // ── BMI Number Display ────────────────────────────────────────
        AnimatedVisibility(
            visible = showBmiNumber,
            enter = fadeIn(tween(400))
        ) {
            BmiNumberCard(result = result, categoryColor = categoryColor, scale = numberScale.value)
        }

        // ── Visual Gauge ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = showGauge,
            enter = fadeIn(tween(500)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(500, easing = EaseOutCubic)
            )
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "BMI Scale",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BmiGauge(bmiValue = result.bmiValue, category = result.category, animate = true)
                }
            }
        }

        // ── Health Details ────────────────────────────────────────────
        AnimatedVisibility(
            visible = showDetails,
            enter = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(400, easing = EaseOutCubic)
            )
        ) {
            HealthDetailsCard(result = result, categoryColor = categoryColor)
        }

        // ── Weight Advice ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = showAdvice,
            enter = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(400, easing = EaseOutCubic)
            )
        ) {
            WeightAdviceCard(result = result, categoryColor = categoryColor)
        }

        // ── Additional Metrics ────────────────────────────────────────
        AnimatedVisibility(
            visible = showAdvice,
            enter = fadeIn(tween(400, delayMillis = 200)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(400, delayMillis = 200, easing = EaseOutCubic)
            )
        ) {
            BmiAdditionalMetricsSection(
                metrics = result.additionalMetrics,
                bmiValue = result.bmiValue
            )
        }

        // ── Age/Gender Comparison ─────────────────────────────────────
        AnimatedVisibility(
            visible = showAdvice,
            enter = fadeIn(tween(400, delayMillis = 300)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(400, delayMillis = 300, easing = EaseOutCubic)
            )
        ) {
            BmiComparisonSection(
                comparisonData = result.comparisonData,
                bmiValue = result.bmiValue
            )
        }

        // ── Action Buttons ────────────────────────────────────────────
        AnimatedVisibility(
            visible = showActions,
            enter = fadeIn(tween(300))
        ) {
            ActionButtonsSection(
                shareText = shareText,
                context = context,
                onRecalculate = onRecalculate
            )
        }
    }
}

// ─── Saved to History Banner ──────────────────────────────────────────────────

@Composable
private fun SavedToHistoryBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF43A047).copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF43A047).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.BookmarkAdded,
                    contentDescription = null,
                    tint = Color(0xFF43A047),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Saved to History",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF43A047)
                )
                Text(
                    text = "View all calculations in the History tab",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ─── Action Buttons Section ───────────────────────────────────────────────────

@Composable
private fun ActionButtonsSection(
    shareText: String,
    context: Context,
    onRecalculate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Share & Recalculate row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Share button
            OutlinedButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share BMI Result")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF7B1FA2)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, Color(0xFF7B1FA2).copy(alpha = 0.3f)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Share",
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Recalculate button
            OutlinedButton(
                onClick = onRecalculate,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BmiAccent
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, BmiAccent.copy(alpha = 0.3f)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Edit & Redo",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Disclaimer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(14.dp)
                    .offset { IntOffset(0, 4) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BMI does not directly measure body fat. Consult a healthcare professional for a complete assessment.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                lineHeight = 16.sp
            )
        }
    }
}

// ─── BMI Number Card ──────────────────────────────────────────────────────────

@Composable
private fun BmiNumberCard(
    result: BmiResult,
    categoryColor: Color,
    scale: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = categoryColor.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = result.category.emoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your BMI",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("%.1f", result.bmiValue),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 64.sp,
                    letterSpacing = (-2).sp
                ),
                color = categoryColor
            )
            Text(
                text = "kg/m²",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = categoryColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = result.category.label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = categoryColor,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// ─── Health Details Card ──────────────────────────────────────────────────────

@Composable
private fun HealthDetailsCard(
    result: BmiResult,
    categoryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp, 20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(categoryColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Health Assessment",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(
                icon = { Icon(Icons.Filled.Warning, null, tint = categoryColor, modifier = Modifier.size(18.dp)) },
                label = "Health Risk Level",
                value = result.category.riskLevel,
                valueColor = categoryColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = result.category.riskDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 18.sp,
                modifier = Modifier.padding(start = 30.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            val minDisplay = formatWeight(result.healthyWeightMinKg, result.displayWeightUnit)
            val maxDisplay = formatWeight(result.healthyWeightMaxKg, result.displayWeightUnit)
            val unitLabel = result.displayWeightUnit.displayName

            DetailRow(
                icon = { Icon(Icons.Outlined.MonitorWeight, null, tint = Color(0xFF43A047), modifier = Modifier.size(18.dp)) },
                label = "Healthy Weight Range",
                value = "$minDisplay – $maxDisplay $unitLabel",
                valueColor = Color(0xFF43A047)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "For your height of ${result.inputHeightCm.toCleanDisplay()} cm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 30.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(
                icon = { Icon(Icons.Filled.Straighten, null, tint = BmiAccent, modifier = Modifier.size(18.dp)) },
                label = "Your Measurements",
                value = "${formatWeight(result.inputWeightKg, result.displayWeightUnit)} ${result.displayWeightUnit.displayName} • ${result.inputHeightCm.toCleanDisplay()} cm",
                valueColor = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${result.inputGender.displayName} • Age ${result.inputAge}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 30.dp)
            )
        }
    }
}

// ─── Weight Advice Card ───────────────────────────────────────────────────────

@Composable
private fun WeightAdviceCard(result: BmiResult, categoryColor: Color) {
    if (result.weightAdvice == WeightAdvice.MAINTAIN) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF43A047).copy(alpha = 0.06f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF43A047).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFF43A047).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, null, tint = Color(0xFF43A047), modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Great job! 🎉", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF43A047))
                    Text("Your BMI is in the healthy normal range. Keep maintaining your current weight!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), lineHeight = 18.sp)
                }
            }
        }
    } else {
        val isGain = result.weightAdvice == WeightAdvice.GAIN
        val adviceColor = if (isGain) Color(0xFF1E88E5) else Color(0xFFFF9800)
        val adviceIcon = if (isGain) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
        val adviceVerb = if (isGain) "gain" else "lose"
        val diffDisplay = formatWeight(result.weightDifferenceKg, result.displayWeightUnit)
        val targetDisplay = formatWeight(result.targetWeightKg, result.displayWeightUnit)
        val unitLabel = result.displayWeightUnit.displayName

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = adviceColor.copy(alpha = 0.06f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(adviceColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(adviceIcon, null, tint = adviceColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("To reach a healthy BMI", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                        Text("Based on WHO normal range (18.5–24.9)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("You need to $adviceVerb", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("~$diffDisplay", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = adviceColor)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(unitLabel, style = MaterialTheme.typography.bodyMedium, color = adviceColor.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 2.dp))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Target weight", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("~$targetDisplay $unitLabel", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF43A047))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("⚕️ Please consult a healthcare professional before starting any weight ${if (isGain) "gain" else "loss"} program. Gradual changes are safest.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), lineHeight = 18.sp)
            }
        }
    }
}

// ─── Detail Row Component ─────────────────────────────────────────────────────

@Composable
private fun DetailRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = valueColor
            )
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatWeight(kg: Double, unit: WeightUnit): String {
    val value = when (unit) {
        WeightUnit.KG -> kg
        WeightUnit.LBS -> kgToLbs(kg)
    }
    return value.toCleanDisplay()
}

private fun Double.toCleanDisplay(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        String.format("%.1f", this)
    }
}
