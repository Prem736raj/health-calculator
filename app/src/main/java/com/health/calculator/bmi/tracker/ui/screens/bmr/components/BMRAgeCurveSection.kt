// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/BMRAgeCurveSection.kt
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.BMRAgeCurveData
import com.health.calculator.bmi.tracker.ui.utils.CascadeAnimatedItem

@Composable
fun BMRAgeCurveSection(
    userBMR: Float,
    userAge: Int,
    isMale: Boolean,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val averageBMR = BMRAgeCurveData.getAverageBMRForAge(userAge, isMale)
    val comparisonText = BMRAgeCurveData.getComparisonText(userBMR, userAge, isMale)
    val declineText = BMRAgeCurveData.getDecadeDeclineText(userAge)
    val diff = userBMR - averageBMR
    val diffPercent = if (averageBMR > 0) (diff / averageBMR * 100f) else 0f

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
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
                    Text(text = "📈", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "BMR & Age Comparison",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "How your BMR compares with age averages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comparison stat cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ComparisonStatCard(
                        label = "Your BMR",
                        value = "${userBMR.toInt()}",
                        unit = "kcal",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    ComparisonStatCard(
                        label = "Average (${if (isMale) "M" else "F"}, ${userAge}yr)",
                        value = "${averageBMR.toInt()}",
                        unit = "kcal",
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    ComparisonStatCard(
                        label = "Difference",
                        value = "${if (diff >= 0) "+" else ""}${diff.toInt()}",
                        unit = "${if (diffPercent >= 0) "+" else ""}${diffPercent.toInt()}%",
                        color = if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Age curve chart
                BMRAgeCurveChart(
                    userBMR = userBMR,
                    userAge = userAge,
                    isMale = isMale,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Chart legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChartLegendDot(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                        label = "Average ${if (isMale) "Male" else "Female"} BMR"
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    ChartLegendDot(
                        color = MaterialTheme.colorScheme.primary,
                        label = "Your BMR"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Comparison insight
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(text = "💬", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = comparisonText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Age decline note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(text = "📉", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Age & Metabolism",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = declineText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // General decline fact
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BMR decreases approximately 1-2% per decade after age 20, primarily due to loss of lean muscle mass.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonStatCard(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.06f)
        ),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                lineHeight = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

// ============================================================
// BMR Age Curve Chart (Canvas)
// ============================================================
@Composable
private fun BMRAgeCurveChart(
    userBMR: Float,
    userAge: Int,
    isMale: Boolean,
    modifier: Modifier = Modifier
) {
    val points = BMRAgeCurveData.ageCurvePoints
    val curveColor = MaterialTheme.colorScheme.tertiary
    val userDotColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fillColor = curveColor.copy(alpha = 0.08f)
    val textMeasurer = rememberTextMeasurer()

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(
            1f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    val userDotScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800)
        userDotScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    Canvas(modifier = modifier) {
        val paddingLeft = 45.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 30.dp.toPx()

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val minAge = 15f
        val maxAge = 80f

        val bmrValues = points.map { if (isMale) it.maleBMR else it.femaleBMR }
        val allBMRs = bmrValues + userBMR
        val minBMR = (allBMRs.min() - 100f).coerceAtLeast(800f)
        val maxBMR = allBMRs.max() + 100f

        fun ageToX(age: Float): Float = paddingLeft + ((age - minAge) / (maxAge - minAge)) * chartWidth
        fun bmrToY(bmr: Float): Float = paddingTop + chartHeight - ((bmr - minBMR) / (maxBMR - minBMR)) * chartHeight

        // Grid lines
        val gridBMRValues = generateSequence(
            (minBMR / 200).toInt() * 200f
        ) { it + 200f }.takeWhile { it <= maxBMR }.toList()

        gridBMRValues.forEach { bmrVal ->
            val y = bmrToY(bmrVal)
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(size.width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )
            // Label
            val labelResult = textMeasurer.measure(
                text = "${bmrVal.toInt()}",
                style = TextStyle(fontSize = 9.sp, color = labelColor)
            )
            drawText(
                textLayoutResult = labelResult,
                topLeft = Offset(2.dp.toPx(), y - labelResult.size.height / 2)
            )
        }

        // Age labels
        listOf(20, 30, 40, 50, 60, 70, 80).forEach { age ->
            val x = ageToX(age.toFloat())
            drawLine(
                color = gridColor,
                start = Offset(x, paddingTop),
                end = Offset(x, size.height - paddingBottom),
                strokeWidth = 0.5.dp.toPx()
            )
            val ageLabel = textMeasurer.measure(
                text = "$age",
                style = TextStyle(fontSize = 9.sp, color = labelColor)
            )
            drawText(
                textLayoutResult = ageLabel,
                topLeft = Offset(x - ageLabel.size.width / 2, size.height - paddingBottom + 4.dp.toPx())
            )
        }

        // Curve path
        val curvePath = Path()
        val fillPath = Path()

        points.forEachIndexed { index, point ->
            val x = ageToX(point.age.toFloat())
            val bmr = if (isMale) point.maleBMR else point.femaleBMR
            val y = bmrToY(bmr)

            val animatedY = paddingTop + chartHeight - ((y - paddingTop - chartHeight) * -1 * animProgress.value)
            val finalY = paddingTop + chartHeight - ((bmr - minBMR) / (maxBMR - minBMR)) * chartHeight * animProgress.value

            if (index == 0) {
                curvePath.moveTo(x, finalY)
                fillPath.moveTo(x, size.height - paddingBottom)
                fillPath.lineTo(x, finalY)
            } else {
                val prevPoint = points[index - 1]
                val prevX = ageToX(prevPoint.age.toFloat())
                val prevBMR = if (isMale) prevPoint.maleBMR else prevPoint.femaleBMR
                val prevY = paddingTop + chartHeight - ((prevBMR - minBMR) / (maxBMR - minBMR)) * chartHeight * animProgress.value

                val cpX = (prevX + x) / 2
                curvePath.cubicTo(cpX, prevY, cpX, finalY, x, finalY)
                fillPath.cubicTo(cpX, prevY, cpX, finalY, x, finalY)
            }
        }

        // Close fill path
        val lastPoint = points.last()
        fillPath.lineTo(ageToX(lastPoint.age.toFloat()), size.height - paddingBottom)
        fillPath.close()

        // Draw fill
        drawPath(
            path = fillPath,
            color = fillColor
        )

        // Draw curve
        drawPath(
            path = curvePath,
            color = curveColor.copy(alpha = 0.6f),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Data points on curve
        points.forEach { point ->
            val x = ageToX(point.age.toFloat())
            val bmr = if (isMale) point.maleBMR else point.femaleBMR
            val y = paddingTop + chartHeight - ((bmr - minBMR) / (maxBMR - minBMR)) * chartHeight * animProgress.value

            drawCircle(
                color = curveColor.copy(alpha = 0.3f),
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // User's BMR dot
        val userX = ageToX(userAge.toFloat().coerceIn(minAge, maxAge))
        val userY = paddingTop + chartHeight - ((userBMR - minBMR) / (maxBMR - minBMR)) * chartHeight * animProgress.value
        val dotRadius = 8.dp.toPx() * userDotScale.value

        // Glow ring
        drawCircle(
            color = userDotColor.copy(alpha = 0.15f),
            radius = dotRadius * 1.8f,
            center = Offset(userX, userY)
        )
        // Outer ring
        drawCircle(
            color = Color.White,
            radius = dotRadius,
            center = Offset(userX, userY)
        )
        // Inner dot
        drawCircle(
            color = userDotColor,
            radius = dotRadius * 0.7f,
            center = Offset(userX, userY)
        )

        // User label
        val youLabel = textMeasurer.measure(
            text = "You: ${userBMR.toInt()}",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = userDotColor
            )
        )
        drawText(
            textLayoutResult = youLabel,
            topLeft = Offset(
                (userX - youLabel.size.width / 2).coerceIn(paddingLeft, size.width - paddingRight - youLabel.size.width.toFloat()),
                (userY - dotRadius - youLabel.size.height - 4.dp.toPx()).coerceAtLeast(0f)
            )
        )
    }
}
