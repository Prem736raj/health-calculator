package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// ============================================================
// Real-time BMI Preview Card
// ============================================================
@Composable
fun RealTimeBMIPreview(
    weightKg: Float,
    heightCm: Float,
    modifier: Modifier = Modifier
) {
    val heightM = heightCm / 100f
    val bmi = if (heightM > 0f && weightKg > 0f) weightKg / (heightM * heightM) else 0f
    val category = getBMICategoryLabel(bmi)
    val categoryColor = animateColorAsState(
        targetValue = getBMICategoryColor(bmi),
        animationSpec = tween(300),
        label = "bmiColorAnim"
    )
    val isValid = bmi in 5f..80f

    AnimatedVisibility(
        visible = isValid,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = categoryColor.value.copy(alpha = 0.1f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.5.dp,
                color = categoryColor.value.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live BMI Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        val animatedBMI by animateFloatAsState(
                            targetValue = bmi,
                            animationSpec = tween(200),
                            label = "bmiValueAnim"
                        )
                        Text(
                            text = String.format("%.1f", animatedBMI),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor.value
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "kg/m²",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // Category badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = categoryColor.value.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        categoryColor.value.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor.value,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Mini color bar
            MiniCategoryBar(
                bmi = bmi,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MiniCategoryBar(
    bmi: Float,
    modifier: Modifier = Modifier
) {
    val segments = listOf(
        16f to Color(0xFFB71C1C),
        18.5f to Color(0xFFFF9800),
        25f to Color(0xFF4CAF50),
        30f to Color(0xFFFFC107),
        35f to Color(0xFFFF9800),
        40f to Color(0xFFD32F2F),
        50f to Color(0xFFB71C1C)
    )
    val markerPosition = ((bmi - 12f) / (50f - 12f)).coerceIn(0f, 1f)

    val animatedPosition by animateFloatAsState(
        targetValue = markerPosition,
        animationSpec = tween(300),
        label = "markerAnim"
    )
    val markerColor = getBMICategoryColor(bmi)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        val barHeight = 6.dp.toPx()
        val barY = size.height / 2 - barHeight / 2

        // Draw colored segments
        val segmentRanges = listOf(
            0f to 0.105f,      // Severe/Moderate Thinness (<16)
            0.105f to 0.171f,  // Mild Thinness (16-18.5)
            0.171f to 0.342f,  // Normal (18.5-25)
            0.342f to 0.474f,  // Overweight (25-30)
            0.474f to 0.605f,  // Obese I (30-35)
            0.605f to 0.737f,  // Obese II (35-40)
            0.737f to 1f       // Obese III (40+)
        )
        val segmentColors = listOf(
            Color(0xFFB71C1C),
            Color(0xFFFF9800),
            Color(0xFF4CAF50),
            Color(0xFFFFC107),
            Color(0xFFFF9800),
            Color(0xFFD32F2F),
            Color(0xFFB71C1C)
        )

        segmentRanges.forEachIndexed { i, (start, end) ->
            drawRoundRect(
                color = segmentColors[i],
                topLeft = Offset(start * size.width, barY),
                size = Size((end - start) * size.width, barHeight),
                cornerRadius = CornerRadius(barHeight / 2)
            )
        }

        // Marker
        val markerX = animatedPosition * size.width
        drawCircle(
            color = Color.White,
            radius = 7.dp.toPx(),
            center = Offset(markerX, size.height / 2)
        )
        drawCircle(
            color = markerColor,
            radius = 5.dp.toPx(),
            center = Offset(markerX, size.height / 2)
        )
    }
}

// ============================================================
// Weight Slider Picker
// ============================================================
@Composable
fun WeightSliderPicker(
    weightKg: Float,
    isUnitKg: Boolean,
    onWeightChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val displayValue = if (isUnitKg) weightKg else weightKg * 2.20462f
    val unit = if (isUnitKg) "kg" else "lbs"
    val range = if (isUnitKg) 20f..250f else 44f..551f

    // Track last integer for haptic
    var lastTickValue by remember { mutableIntStateOf(displayValue.roundToInt()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.MonitorWeight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${String.format("%.1f", displayValue)} $unit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = displayValue.coerceIn(range),
            onValueChange = { newVal ->
                val newTick = newVal.roundToInt()
                if (newTick != lastTickValue) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    lastTickValue = newTick
                }
                val kg = if (isUnitKg) newVal else newVal / 2.20462f
                onWeightChange(kg)
            },
            valueRange = range,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${range.start.roundToInt()} $unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${range.endInclusive.roundToInt()} $unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Fine-tune buttons
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = {
                    val step = if (isUnitKg) 0.5f else 1.0f
                    val newDisplay = (displayValue - step).coerceAtLeast(range.start)
                    val kg = if (isUnitKg) newDisplay else newDisplay / 2.20462f
                    onWeightChange(kg)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Filled.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Fine-tune",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            FilledTonalIconButton(
                onClick = {
                    val step = if (isUnitKg) 0.5f else 1.0f
                    val newDisplay = (displayValue + step).coerceAtMost(range.endInclusive)
                    val kg = if (isUnitKg) newDisplay else newDisplay / 2.20462f
                    onWeightChange(kg)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============================================================
// Height Slider Picker
// ============================================================
@Composable
fun HeightSliderPicker(
    heightCm: Float,
    isUnitCm: Boolean,
    onHeightChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val rangeCm = 50f..250f

    val displayText = if (isUnitCm) {
        "${heightCm.roundToInt()} cm"
    } else {
        val totalInches = heightCm / 2.54
        val feet = (totalInches / 12).toInt()
        val inches = (totalInches % 12).roundToInt()
        "${feet}ft ${inches}in"
    }

    var lastTickValue by remember { mutableIntStateOf(heightCm.roundToInt()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Height,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Height",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = displayText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Visual height representation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Slider takes most width
            Column(modifier = Modifier.weight(1f)) {
                Slider(
                    value = heightCm.coerceIn(rangeCm),
                    onValueChange = { newVal ->
                        val newTick = newVal.roundToInt()
                        if (newTick != lastTickValue) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastTickValue = newTick
                        }
                        onHeightChange(newVal)
                    },
                    valueRange = rangeCm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isUnitCm) "50 cm" else "1ft 8in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isUnitCm) "250 cm" else "8ft 2in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Mini person icon with height indicator
            HeightVisualIndicator(
                heightCm = heightCm,
                modifier = Modifier
                    .width(32.dp)
                    .height(80.dp)
            )
        }

        // Fine-tune buttons
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = {
                    val newCm = (heightCm - 1f).coerceAtLeast(rangeCm.start)
                    onHeightChange(newCm)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Filled.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Fine-tune (1 cm)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            FilledTonalIconButton(
                onClick = {
                    val newCm = (heightCm + 1f).coerceAtMost(rangeCm.endInclusive)
                    onHeightChange(newCm)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun HeightVisualIndicator(
    heightCm: Float,
    modifier: Modifier = Modifier
) {
    val normalizedHeight = ((heightCm - 50f) / (250f - 50f)).coerceIn(0f, 1f)
    val animatedHeight by animateFloatAsState(
        targetValue = normalizedHeight,
        animationSpec = tween(200),
        label = "heightBarAnim"
    )
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val barWidth = 12.dp.toPx()
        val barX = (size.width - barWidth) / 2

        // Track background
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(barX, 0f),
            size = Size(barWidth, size.height),
            cornerRadius = CornerRadius(barWidth / 2)
        )

        // Filled portion from bottom
        val filledHeight = animatedHeight * size.height
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(barX, size.height - filledHeight),
            size = Size(barWidth, filledHeight),
            cornerRadius = CornerRadius(barWidth / 2)
        )

        // Person icon indicator at top of fill
        val iconY = size.height - filledHeight
        drawCircle(
            color = primaryColor,
            radius = 6.dp.toPx(),
            center = Offset(size.width / 2, iconY.coerceAtLeast(6.dp.toPx()))
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = Offset(size.width / 2, iconY.coerceAtLeast(6.dp.toPx()))
        )
    }
}

// ============================================================
// Input Mode Toggle (Keyboard vs Slider)
// ============================================================
@Composable
fun InputModeToggle(
    useSliders: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = !useSliders,
                onClick = { onToggle(false) },
                label = {
                    Text(
                        "⌨️ Keyboard",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            FilterChip(
                selected = useSliders,
                onClick = { onToggle(true) },
                label = {
                    Text(
                        "🎚️ Sliders",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// ============================================================
// Shared color helpers
// ============================================================
internal fun getBMICategoryColor(bmi: Float): Color {
    return when {
        bmi <= 0f -> Color.Gray
        bmi < 16f -> Color(0xFFB71C1C)
        bmi < 17f -> Color(0xFFD32F2F)
        bmi < 18.5f -> Color(0xFFFF9800)
        bmi < 25f -> Color(0xFF4CAF50)
        bmi < 30f -> Color(0xFFFFC107)
        bmi < 35f -> Color(0xFFFF9800)
        bmi < 40f -> Color(0xFFD32F2F)
        else -> Color(0xFFB71C1C)
    }
}

internal fun getBMICategoryLabel(bmi: Float): String {
    return when {
        bmi <= 0f -> "—"
        bmi < 16f -> "Severe Thinness"
        bmi < 17f -> "Moderate Thinness"
        bmi < 18.5f -> "Mild Thinness"
        bmi < 25f -> "Normal"
        bmi < 30f -> "Overweight"
        bmi < 35f -> "Obese I"
        bmi < 40f -> "Obese II"
        else -> "Obese III"
    }
}
