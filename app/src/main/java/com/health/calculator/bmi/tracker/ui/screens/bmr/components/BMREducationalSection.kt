// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/BMREducationalSection.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.BMREducationalContent
import com.health.calculator.bmi.tracker.data.model.BMRFormula
import com.health.calculator.bmi.tracker.data.model.EducationalParagraph
import com.health.calculator.bmi.tracker.data.model.EducationalSection

@Composable
fun BMREducationalSection(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
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
                    Text(text = "📚", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Learn About BMR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Understand your metabolism better",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Educational sections
                BMREducationalContent.sections.forEachIndexed { index, section ->
                    ExpandableEducationalCard(
                        section = section,
                        defaultExpanded = index == 0
                    )
                    if (index < BMREducationalContent.sections.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Medical disclaimer
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "This educational content is for informational purposes only and should not replace professional medical or nutritional advice. Always consult a healthcare provider for personalized guidance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableEducationalCard(
    section: EducationalSection,
    defaultExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column {
            // Header
            Surface(
                onClick = { expanded = !expanded },
                color = Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = section.emoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        animationSpec = tween(300),
                        label = "eduChevron"
                    )
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
            }

            // Content
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(300)) + expandVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(tween(200)) + shrinkVertically(
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 14.dp, end = 14.dp, bottom = 14.dp
                    )
                ) {
                    section.content.forEachIndexed { index, paragraph ->
                        ParagraphItem(paragraph = paragraph)
                        if (index < section.content.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParagraphItem(paragraph: EducationalParagraph) {
    when {
        paragraph.isBullet -> BulletParagraph(paragraph)
        paragraph.isHighlight -> HighlightParagraph(paragraph)
        paragraph.isTip -> TipParagraph(paragraph)
        paragraph.isWarning -> WarningParagraph(paragraph)
        else -> NormalParagraph(paragraph)
    }
}

@Composable
private fun NormalParagraph(paragraph: EducationalParagraph) {
    Text(
        text = paragraph.text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 18.sp
    )
}

@Composable
private fun BulletParagraph(paragraph: EducationalParagraph) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = paragraph.text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HighlightParagraph(paragraph: EducationalParagraph) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Text(text = "💡", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = paragraph.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun TipParagraph(paragraph: EducationalParagraph) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "✅", fontSize = 13.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = paragraph.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun WarningParagraph(paragraph: EducationalParagraph) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336).copy(alpha = 0.06f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            Color(0xFFF44336).copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "⚠️", fontSize = 13.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = paragraph.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

// ============================================================
// Formula Comparison Card
// ============================================================
@Composable
fun BMRFormulaComparisonSection(
    allResults: Map<BMRFormula, Float>,
    selectedFormula: BMRFormula,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
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
                    Text(text = "🔬", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Formula Insights",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Why formulas give different results",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explanation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Different BMR formulas use different mathematical models derived from different study populations. This is why they produce varying results:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FormulaInsightBullet(
                            text = "Older formulas (Harris-Benedict 1919) tend to overestimate by 5-15%"
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FormulaInsightBullet(
                            text = "Body-fat-based formulas (Katch-McArdle) are more accurate for people with unusual body composition"
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FormulaInsightBullet(
                            text = "WHO equations use simplified age groups and are designed for global applicability"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick comparison
                if (allResults.size > 1) {
                    val sorted = allResults.entries.sortedByDescending { it.value }
                    val range = (sorted.first().value - sorted.last().value).toInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickStatCard(
                            label = "Highest",
                            value = "${sorted.first().value.toInt()}",
                            formula = sorted.first().key.displayName.split(" ").first(),
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        QuickStatCard(
                            label = "Lowest",
                            value = "${sorted.last().value.toInt()}",
                            formula = sorted.last().key.displayName.split(" ").first(),
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        QuickStatCard(
                            label = "Range",
                            value = "$range",
                            formula = "kcal diff",
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recommendation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.06f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp, Color(0xFF4CAF50).copy(alpha = 0.15f)
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(text = "⭐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Recommendation",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF388E3C)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "The Mifflin-St Jeor equation (1990) is considered the most accurate formula for estimating BMR in most adults. It's recommended by the Academy of Nutrition and Dietetics and has been validated in numerous studies. If you know your body fat percentage, the Katch-McArdle formula may be even more precise.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormulaInsightBullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun QuickStatCard(
    label: String,
    value: String,
    formula: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.06f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = formula,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}
