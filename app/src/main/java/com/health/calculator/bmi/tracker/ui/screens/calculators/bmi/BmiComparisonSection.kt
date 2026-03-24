package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

// ─── Colors ───────────────────────────────────────────────────────────────────

private val ComparisonAccent = Color(0xFF0097A7)

/**
 * BMI comparison section that shows age/gender-specific BMI insights.
 * Displays differently for pediatric (2-19) vs adult (20+) users.
 */
@Composable
fun BmiComparisonSection(
    comparisonData: BmiComparisonData,
    bmiValue: Double,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val expandRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "comp_expand"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(tween(400, easing = EaseOutCubic)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // ── Header ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isExpanded = !isExpanded }
                    )
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ComparisonAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (comparisonData.isPediatric)
                            Icons.Outlined.ChildCare
                        else
                            Icons.Outlined.People,
                        contentDescription = null,
                        tint = ComparisonAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Age & Gender Insights",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = comparisonData.ageGroupLabel,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = ComparisonAccent,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(expandRotation)
                )
            }

            // ── Expandable Content ────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(300)) + expandVertically(tween(400, easing = EaseOutCubic)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(300))
            ) {
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HorizontalDivider(
                        color = ComparisonAccent.copy(alpha = 0.1f),
                        thickness = 0.5.dp
                    )

                    if (comparisonData.isPediatric) {
                        PediatricSection(comparisonData, bmiValue)
                    } else {
                        AdultSection(comparisonData, bmiValue)
                    }

                    // Age Context Note
                    AgeContextCard(comparisonData.ageContextNote)

                    // Disclaimer
                    ComparisonDisclaimer(comparisonData.isPediatric)
                }
            }
        }
    }
}

// ─── Pediatric Section (Ages 2-19) ────────────────────────────────────────────

@Composable
private fun PediatricSection(
    data: BmiComparisonData,
    bmiValue: Double
) {
    val catColor = Color(data.pediatricCategory.colorHex)

    // Pediatric label
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF7B1FA2).copy(alpha = 0.06f),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, Color(0xFF7B1FA2).copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ChildCare, null,
                tint = Color(0xFF7B1FA2),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "Child/Teen BMI Assessment",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF7B1FA2)
                )
                Text(
                    "BMI-for-age percentile method (WHO standard)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }

    // Percentile result
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = catColor.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${data.pediatricCategory.emoji} ${data.pediatricCategory.label}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = catColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.pediatricCategory.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Percentile display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format("%.0f", data.estimatedPercentile),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    ),
                    color = catColor
                )
                Text(
                    text = getOrdinalSuffix(data.estimatedPercentile.toInt()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = catColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "percentile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Percentile bar
            PercentileBar(percentile = data.estimatedPercentile, catColor = catColor)
        }
    }

    // Percentile category table
    PediatricCategoryTable(currentCategory = data.pediatricCategory)

    // Population comparison
    PopulationComparisonRow(data = data, bmiValue = bmiValue)
}

@Composable
private fun PercentileBar(percentile: Double, catColor: Color) {
    val animatedPosition = remember { Animatable(0f) }
    LaunchedEffect(percentile) {
        animatedPosition.snapTo(0f)
        animatedPosition.animateTo(
            (percentile / 100.0).toFloat(),
            tween(1000, delayMillis = 200, easing = FastOutSlowInEasing)
        )
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF9800).copy(alpha = 0.3f),
                            Color(0xFF43A047).copy(alpha = 0.4f),
                            Color(0xFF43A047).copy(alpha = 0.4f),
                            Color(0xFFFFC107).copy(alpha = 0.3f),
                            Color(0xFFE53935).copy(alpha = 0.3f)
                        ),
                        startX = 0f,
                        endX = Float.POSITIVE_INFINITY
                    )
                )
        ) {
            // Marker
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPosition.value)
                    .height(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.5.dp, catColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text("0", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.weight(0.05f))
            Text("5th", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFFFF9800).copy(alpha = 0.6f))
            Spacer(modifier = Modifier.weight(0.8f))
            Text("85th", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFFFFC107).copy(alpha = 0.6f))
            Spacer(modifier = Modifier.weight(0.1f))
            Text("95th", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFFE53935).copy(alpha = 0.6f))
            Spacer(modifier = Modifier.weight(0.05f))
            Text("100", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun PediatricCategoryTable(currentCategory: PediatricBmiCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "WHO BMI-for-Age Categories",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))

            PediatricBmiCategory.entries.forEach { cat ->
                val isSelected = cat == currentCategory
                val color = Color(cat.colorHex)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) color.copy(alpha = 0.06f) else Color.Transparent)
                        .border(
                            if (isSelected) 1.dp else 0.dp,
                            if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        cat.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        cat.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = if (isSelected) color.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.End
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("◀", fontSize = 9.sp, color = color)
                    }
                }
            }
        }
    }
}

// ─── Adult Section (Ages 20+) ─────────────────────────────────────────────────

@Composable
private fun AdultSection(
    data: BmiComparisonData,
    bmiValue: Double
) {
    // Range visualization
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "Recommended Range for Your Age",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${data.gender.displayName} • ${data.ageGroupLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Range bar
            AdultRangeBar(
                bmiValue = bmiValue,
                rangeMin = data.recommendedRange.first,
                rangeMax = data.recommendedRange.second,
                isWithinRange = data.isWithinRange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status
            val statusColor = if (data.isWithinRange) Color(0xFF43A047) else Color(0xFFFF9800)
            val statusEmoji = if (data.isWithinRange) "✅" else "⚠️"
            val statusText = if (data.isWithinRange)
                "Your BMI of ${String.format("%.1f", bmiValue)} is within the recommended range for your age and gender."
            else
                "Your BMI of ${String.format("%.1f", bmiValue)} is outside the recommended range of ${data.recommendedRange.first}–${data.recommendedRange.second} for your age group."

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.06f)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Text(statusEmoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Population comparison
    PopulationComparisonRow(data = data, bmiValue = bmiValue)

    // Age-specific note for seniors
    if (data.ageGroup == AgeGroupType.SENIOR) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E88E5).copy(alpha = 0.05f),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E88E5).copy(alpha = 0.12f))
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Text("👴", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Note for Older Adults",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF1E88E5)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Research suggests that for adults over 65, a BMI in the range of 23–27 may be associated with the lowest mortality risk, which is slightly higher than the standard \"normal\" range. Discuss your optimal weight with your healthcare provider.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AdultRangeBar(
    bmiValue: Double,
    rangeMin: Double,
    rangeMax: Double,
    isWithinRange: Boolean
) {
    val barMin = (rangeMin - 5).coerceAtLeast(12.0)
    val barMax = (rangeMax + 10).coerceAtMost(50.0)
    val totalRange = barMax - barMin

    val bmiPosition = ((bmiValue - barMin) / totalRange).toFloat().coerceIn(0f, 1f)
    val rangeStartPos = ((rangeMin - barMin) / totalRange).toFloat()
    val rangeEndPos = ((rangeMax - barMin) / totalRange).toFloat()

    val animPosition = remember { Animatable(0f) }
    LaunchedEffect(bmiValue) {
        animPosition.snapTo(0f)
        animPosition.animateTo(bmiPosition, tween(1000, 200, FastOutSlowInEasing))
    }

    val markerColor = if (isWithinRange) Color(0xFF43A047) else Color(0xFFFF9800)

    Column {
        // Labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(String.format("%.0f", barMin), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            Text("Recommended: ${rangeMin}–${rangeMax}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold), color = Color(0xFF43A047).copy(alpha = 0.7f))
            Text(String.format("%.0f", barMax), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            // Normal range highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(start = (rangeStartPos * 300).dp.coerceAtMost(280.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((rangeEndPos - rangeStartPos).coerceIn(0.05f, 1f))
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF43A047).copy(alpha = 0.15f))
                        .border(0.5.dp, Color(0xFF43A047).copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                )
            }

            // BMI marker
            Box(
                modifier = Modifier
                    .fillMaxWidth(animPosition.value.coerceAtLeast(0.01f))
                    .height(20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.5.dp, markerColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(markerColor))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Your BMI label
        Box(
            modifier = Modifier
                .fillMaxWidth(animPosition.value.coerceAtLeast(0.01f)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                "Your BMI: ${String.format("%.1f", bmiValue)}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = markerColor
            )
        }
    }
}

// ─── Population Comparison Row ────────────────────────────────────────────────

@Composable
private fun PopulationComparisonRow(
    data: BmiComparisonData,
    bmiValue: Double
) {
    val diffAbs = abs(data.differenceFromAvg)
    val isAbove = data.differenceFromAvg > 0.05
    val isBelow = data.differenceFromAvg < -0.05
    val diffColor = when {
        diffAbs < 0.5 -> Color(0xFF43A047)
        diffAbs < 2.0 -> Color(0xFFFFC107)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(ComparisonAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Group, null, tint = ComparisonAccent, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "vs. Population Average",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                val compText = when {
                    isAbove -> "${String.format("%.1f", diffAbs)} above average"
                    isBelow -> "${String.format("%.1f", diffAbs)} below average"
                    else -> "At the average"
                }
                Text(
                    compText,
                    style = MaterialTheme.typography.bodySmall,
                    color = diffColor
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Avg: ${String.format("%.1f", data.populationAvgBmi)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    "You: ${String.format("%.1f", bmiValue)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = diffColor
                )
            }
        }
    }
}

// ─── Age Context Card ─────────────────────────────────────────────────────────

@Composable
private fun AgeContextCard(note: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ComparisonAccent.copy(alpha = 0.04f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ComparisonAccent.copy(alpha = 0.12f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Text("💡", fontSize = 14.sp, modifier = Modifier.padding(top = 1.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "Age-Specific Note",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ComparisonAccent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ─── Disclaimer ───────────────────────────────────────────────────────────────

@Composable
private fun ComparisonDisclaimer(isPediatric: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Info, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(14.dp).offset { IntOffset(0, 4) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isPediatric)
                "Pediatric BMI percentiles are estimates based on WHO growth reference data. For accurate assessment, consult your child's pediatrician who can use clinical growth charts."
            else
                "Population averages vary by region and ethnicity. The values shown are global estimates. Your doctor can provide personalized guidance based on your complete health profile.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            lineHeight = 16.sp
        )
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

private fun getOrdinalSuffix(number: Int): String {
    val mod100 = number % 100
    val mod10 = number % 10
    return when {
        mod100 in 11..13 -> "th"
        mod10 == 1 -> "st"
        mod10 == 2 -> "nd"
        mod10 == 3 -> "rd"
        else -> "th"
    }
}
