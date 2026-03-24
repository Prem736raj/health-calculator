package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Gauge Color Stops ────────────────────────────────────────────────────────

private val gaugeColors = listOf(
    Color(0xFFB71C1C),  // Severe thinness (0-16)
    Color(0xFFE53935),  // Moderate thinness (16-17)
    Color(0xFFFF9800),  // Mild thinness (17-18.5)
    Color(0xFF43A047),  // Normal (18.5-25)
    Color(0xFFFFC107),  // Overweight (25-30)
    Color(0xFFFF9800),  // Obese I (30-35)
    Color(0xFFE53935),  // Obese II (35-40)
    Color(0xFFB71C1C)   // Obese III (40+)
)

// BMI boundaries on the gauge (maps to position 0.0 → 1.0)
private val bmiBoundaries = listOf(0.0, 16.0, 17.0, 18.5, 25.0, 30.0, 35.0, 40.0, 50.0)

/**
 * Maps a BMI value to a position on the gauge (0.0 to 1.0).
 */
private fun bmiToPosition(bmi: Double): Float {
    val clampedBmi = bmi.coerceIn(10.0, 50.0)

    for (i in 0 until bmiBoundaries.size - 1) {
        val low = bmiBoundaries[i]
        val high = bmiBoundaries[i + 1]
        if (clampedBmi >= low && clampedBmi < high) {
            val segmentFraction = (clampedBmi - low) / (high - low)
            val segmentStart = i.toFloat() / (bmiBoundaries.size - 1)
            val segmentEnd = (i + 1).toFloat() / (bmiBoundaries.size - 1)
            return (segmentStart + segmentFraction * (segmentEnd - segmentStart)).toFloat()
        }
    }
    return 1f
}

// ─── Main BMI Gauge ───────────────────────────────────────────────────────────

/**
 * A beautiful horizontal BMI gauge that shows where the user's BMI falls
 * on the WHO classification scale.
 *
 * Features:
 * - Gradient color bar representing all BMI categories
 * - Animated pointer/marker that slides to the correct position
 * - Category labels below the bar
 * - BMI value label above the pointer
 */
@Composable
fun BmiGauge(
    bmiValue: Double,
    category: BmiCategory,
    modifier: Modifier = Modifier,
    barHeight: Dp = 14.dp,
    animate: Boolean = true
) {
    val position = bmiToPosition(bmiValue)

    // Animated position
    val animatedPosition = remember { Animatable(0f) }
    LaunchedEffect(bmiValue) {
        if (animate) {
            animatedPosition.snapTo(0f)
            animatedPosition.animateTo(
                targetValue = position,
                animationSpec = tween(
                    durationMillis = 1200,
                    delayMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            animatedPosition.snapTo(position)
        }
    }

    val categoryColor = Color(category.colorHex)
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        // ── Pointer and BMI Value ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                val barWidth = size.width
                val pointerX = animatedPosition.value * barWidth

                // Draw pointer triangle
                val triangleSize = 10.dp.toPx()
                val triangleY = size.height

                val path = Path().apply {
                    moveTo(pointerX, triangleY)
                    lineTo(pointerX - triangleSize / 2, triangleY - triangleSize)
                    lineTo(pointerX + triangleSize / 2, triangleY - triangleSize)
                    close()
                }

                drawPath(
                    path = path,
                    color = categoryColor
                )
            }

            // BMI value label above pointer
            val offsetX = with(density) {
                val totalWidth = 300.dp // Approximate, will be calculated
                (animatedPosition.value * totalWidth.toPx() - 24.dp.toPx()).toDp()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor,
                    modifier = Modifier.offset(
                        x = with(density) {
                            val parentWidthPx = 300.dp.toPx() // Will be recalculated
                            (animatedPosition.value * parentWidthPx).toDp() - 20.dp
                        }.coerceAtLeast(0.dp)
                    )
                ) {
                    Text(
                        text = String.format("%.1f", bmiValue),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // ── Gradient Bar ──────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            val barWidth = size.width
            val barHeightPx = size.height
            val cornerRadius = barHeightPx / 2

            // Draw gradient bar with segments
            val segmentCount = gaugeColors.size
            val segmentWidth = barWidth / segmentCount

            for (i in 0 until segmentCount) {
                val startX = i * segmentWidth
                val endX = (i + 1) * segmentWidth
                val startColor = gaugeColors[i]
                val endColor = if (i < segmentCount - 1) gaugeColors[i + 1] else gaugeColors[i]

                // First and last segments get rounded corners
                if (i == 0) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(startColor, endColor),
                            startX = startX,
                            endX = endX
                        ),
                        topLeft = Offset(startX, 0f),
                        size = Size(segmentWidth, barHeightPx),
                        cornerRadius = CornerRadius(cornerRadius)
                    )
                } else if (i == segmentCount - 1) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(startColor, endColor),
                            startX = startX,
                            endX = endX
                        ),
                        topLeft = Offset(startX, 0f),
                        size = Size(segmentWidth, barHeightPx),
                        cornerRadius = CornerRadius(cornerRadius)
                    )
                } else {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(startColor, endColor),
                            startX = startX,
                            endX = endX
                        ),
                        topLeft = Offset(startX, 0f),
                        size = Size(segmentWidth, barHeightPx)
                    )
                }
            }

            // Draw pointer line on the bar
            val pointerX = animatedPosition.value * barWidth
            drawLine(
                color = Color.White,
                start = Offset(pointerX, 0f),
                end = Offset(pointerX, barHeightPx),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw pointer circle
            drawCircle(
                color = Color.White,
                radius = barHeightPx / 2 + 2.dp.toPx(),
                center = Offset(pointerX, barHeightPx / 2)
            )
            drawCircle(
                color = categoryColor,
                radius = barHeightPx / 2 - 1.dp.toPx(),
                center = Offset(pointerX, barHeightPx / 2)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Category Labels ───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            val labels = listOf(
                Pair("16", Color(0xFFE53935)),
                Pair("18.5", Color(0xFFFF9800)),
                Pair("25", Color(0xFF43A047)),
                Pair("30", Color(0xFFFFC107)),
                Pair("35", Color(0xFFFF9800)),
                Pair("40", Color(0xFFE53935))
            )

            Text(
                text = "Underweight",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Normal",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF43A047).copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Overweight",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Obese",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        // BMI boundary numbers
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(0.15f))
            Text("16", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.weight(0.08f))
            Text("18.5", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.weight(0.2f))
            Text("25", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.weight(0.15f))
            Text("30", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.weight(0.12f))
            Text("35", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.weight(0.1f))
            Text("40", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.weight(0.05f))
        }
    }
}
