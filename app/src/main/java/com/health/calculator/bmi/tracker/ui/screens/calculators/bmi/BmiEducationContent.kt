package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Colors ───────────────────────────────────────────────────────────────────

private val EducationAccent = Color(0xFF1E88E5)
private val EducationAccentLight = Color(0xFF64B5F6)

// ─── Section Data ─────────────────────────────────────────────────────────────

private data class EducationSection(
    val id: String,
    val title: String,
    val emoji: String,
    val icon: ImageVector,
    val iconColor: Color
)

private val educationSections = listOf(
    EducationSection(
        id = "what_is_bmi",
        title = "What is BMI?",
        emoji = "📖",
        icon = Icons.Filled.MenuBook,
        iconColor = Color(0xFF1E88E5)
    ),
    EducationSection(
        id = "how_calculated",
        title = "How is BMI Calculated?",
        emoji = "🧮",
        icon = Icons.Outlined.Calculate,
        iconColor = Color(0xFF00897B)
    ),
    EducationSection(
        id = "categories",
        title = "BMI Categories",
        emoji = "📊",
        icon = Icons.Outlined.Category,
        iconColor = Color(0xFF7B1FA2)
    ),
    EducationSection(
        id = "limitations",
        title = "Limitations of BMI",
        emoji = "⚠️",
        icon = Icons.Outlined.ReportProblem,
        iconColor = Color(0xFFFF9800)
    ),
    EducationSection(
        id = "tips",
        title = "Tips for Healthy Weight",
        emoji = "💡",
        icon = Icons.Outlined.Lightbulb,
        iconColor = Color(0xFF43A047)
    ),
    EducationSection(
        id = "when_doctor",
        title = "When to See a Doctor",
        emoji = "🏥",
        icon = Icons.Filled.LocalHospital,
        iconColor = Color(0xFFE53935)
    )
)

// ─── Main Education Content ───────────────────────────────────────────────────

/**
 * Complete BMI educational content section with 6 expandable/collapsible topics.
 * Provides accessible, friendly health information alongside the calculator.
 */
@Composable
fun BmiEducationContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        EducationHeader()

        Spacer(modifier = Modifier.height(4.dp))

        // ── Expandable Sections ───────────────────────────────────────────
        educationSections.forEach { section ->
            ExpandableEducationCard(section = section)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Footer Disclaimer ─────────────────────────────────────────────
        EducationDisclaimer()
    }
}

// ─── Education Header ─────────────────────────────────────────────────────────

@Composable
private fun EducationHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = EducationAccent.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                EducationAccent.copy(alpha = 0.15f),
                                EducationAccentLight.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📚", fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Learn About BMI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Understand your results and what they mean for your health",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ─── Expandable Education Card ────────────────────────────────────────────────

@Composable
private fun ExpandableEducationCard(
    section: EducationSection
) {
    var isExpanded by rememberSaveable(section.id) { mutableStateOf(false) }

    val expandRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "expand_${section.id}"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(400, easing = EaseOutCubic)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // ── Clickable Header ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isExpanded = !isExpanded }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(section.iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        tint = section.iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "${section.emoji} ${section.title}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = section.iconColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(expandRotation)
                )
            }

            // ── Expandable Content ────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(300)) + expandVertically(
                    animationSpec = tween(400, easing = EaseOutCubic)
                ),
                exit = fadeOut(tween(200)) + shrinkVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    HorizontalDivider(
                        color = section.iconColor.copy(alpha = 0.1f),
                        thickness = 0.5.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    when (section.id) {
                        "what_is_bmi" -> WhatIsBmiContent()
                        "how_calculated" -> HowCalculatedContent()
                        "categories" -> CategoriesContent()
                        "limitations" -> LimitationsContent()
                        "tips" -> TipsContent()
                        "when_doctor" -> WhenDoctorContent()
                    }
                }
            }
        }
    }
}

// ─── 1. What is BMI? ──────────────────────────────────────────────────────────

@Composable
private fun WhatIsBmiContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ContentText(
            "Body Mass Index (BMI) is a simple screening tool that estimates whether a person's weight is in a healthy proportion to their height. It was developed by Belgian mathematician Adolphe Quetelet in the 19th century and has been adopted by the World Health Organization (WHO) as a standard measure."
        )

        HighlightBox(
            emoji = "🎯",
            text = "BMI gives you a quick snapshot of where your weight falls on a scale from underweight to obese, helping you and your doctor identify potential health risks."
        )

        ContentText(
            "While BMI is widely used because it's easy to calculate and requires only your height and weight, it's important to remember that it's a screening tool — not a diagnostic one. It provides a starting point for health conversations, not a final answer."
        )

        KeyPoint(
            label = "Used by",
            value = "World Health Organization, doctors, and health professionals worldwide"
        )

        KeyPoint(
            label = "Measures",
            value = "The relationship between your weight and height"
        )

        KeyPoint(
            label = "Purpose",
            value = "Screen for potential weight-related health risks"
        )
    }
}

// ─── 2. How is BMI Calculated? ────────────────────────────────────────────────

@Composable
private fun HowCalculatedContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ContentText(
            "BMI uses a straightforward mathematical formula that compares your weight to the square of your height:"
        )

        // Formula card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF00897B).copy(alpha = 0.06f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BMI Formula",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF00897B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Metric formula
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Metric",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("BMI = ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00897B))) {
                                    append("weight (kg)")
                                }
                                append(" ÷ ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00897B))) {
                                    append("height (m)²")
                                }
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Imperial formula
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Imperial",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("BMI = ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00897B))) {
                                    append("weight (lbs)")
                                }
                                append(" ÷ ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00897B))) {
                                    append("height (in)²")
                                }
                                append(" × 703")
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Example
        HighlightBox(
            emoji = "📝",
            text = "Example: A person weighing 70 kg and 1.75 m tall:\nBMI = 70 ÷ (1.75 × 1.75) = 70 ÷ 3.0625 = 22.9"
        )

        ContentText(
            "The result is expressed in kg/m² (kilograms per square meter). This number is then compared against standard WHO ranges to determine your weight category."
        )
    }
}

// ─── 3. BMI Categories ────────────────────────────────────────────────────────

@Composable
private fun CategoriesContent() {
    data class CategoryRow(
        val name: String,
        val range: String,
        val color: Color,
        val risk: String,
        val emoji: String
    )

    val categories = listOf(
        CategoryRow("Severe Thinness", "< 16.0", Color(0xFFB71C1C), "Very High", "🔴"),
        CategoryRow("Moderate Thinness", "16.0 – 16.9", Color(0xFFE53935), "High", "🟠"),
        CategoryRow("Mild Thinness", "17.0 – 18.4", Color(0xFFFF9800), "Moderate", "🟡"),
        CategoryRow("Normal Weight", "18.5 – 24.9", Color(0xFF43A047), "Low", "🟢"),
        CategoryRow("Overweight", "25.0 – 29.9", Color(0xFFFFC107), "Increased", "🟡"),
        CategoryRow("Obese Class I", "30.0 – 34.9", Color(0xFFFF9800), "High", "🟠"),
        CategoryRow("Obese Class II", "35.0 – 39.9", Color(0xFFE53935), "Very High", "🔴"),
        CategoryRow("Obese Class III", "≥ 40.0", Color(0xFFB71C1C), "Extremely High", "🔴")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ContentText(
            "The World Health Organization classifies BMI into the following categories. Each category carries different levels of health risk:"
        )

        // Category table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1.1f)
                    )
                    Text(
                        text = "BMI Range",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Risk",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.End
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                categories.forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .let {
                                if (cat.name == "Normal Weight")
                                    it.background(cat.color.copy(alpha = 0.06f))
                                        .border(0.5.dp, cat.color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                else it
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color dot + name
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(cat.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (cat.name == "Normal Weight") FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (cat.name == "Normal Weight")
                                cat.color
                            else
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        // BMI range
                        Text(
                            text = cat.range,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.7f),
                            textAlign = TextAlign.Center
                        )

                        // Risk level
                        Text(
                            text = "${cat.emoji} ${cat.risk}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = cat.color,
                            modifier = Modifier.weight(0.7f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        HighlightBox(
            emoji = "🟢",
            text = "The \"Normal Weight\" range (18.5–24.9) is associated with the lowest risk of weight-related health problems for most adults."
        )
    }
}

// ─── 4. Limitations of BMI ────────────────────────────────────────────────────

@Composable
private fun LimitationsContent() {
    data class LimitationItem(
        val emoji: String,
        val title: String,
        val description: String
    )

    val limitations = listOf(
        LimitationItem(
            emoji = "💪",
            title = "Doesn't distinguish muscle from fat",
            description = "Athletes and muscular individuals may have a high BMI despite having low body fat. Muscle weighs more than fat per unit volume."
        ),
        LimitationItem(
            emoji = "🦴",
            title = "Ignores bone density",
            description = "People with denser bones will weigh more, potentially skewing their BMI higher without being overweight."
        ),
        LimitationItem(
            emoji = "👴",
            title = "Doesn't account for age",
            description = "Older adults tend to have more body fat than younger adults at the same BMI. The standard ranges may not be ideal for all age groups."
        ),
        LimitationItem(
            emoji = "⚧️",
            title = "Same cutoffs for both sexes",
            description = "Women naturally carry more body fat than men. Using the same BMI thresholds for both may not reflect individual health accurately."
        ),
        LimitationItem(
            emoji = "🌍",
            title = "Ethnic variations",
            description = "Different ethnic groups may have different body compositions and health risk levels at the same BMI. Asian populations, for example, may face higher risks at lower BMI values."
        ),
        LimitationItem(
            emoji = "📍",
            title = "No fat distribution info",
            description = "BMI doesn't tell you where your body stores fat. Belly fat (visceral fat) is more dangerous than fat stored in hips and thighs, but BMI can't differentiate."
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ContentText(
            "While BMI is a useful starting point, it has several important limitations you should be aware of:"
        )

        limitations.forEach { item ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = item.emoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        HighlightBox(
            emoji = "💡",
            text = "For a more complete picture of your health, combine BMI with other measurements like waist circumference, body fat percentage, and waist-to-hip ratio."
        )
    }
}

// ─── 5. Tips for Healthy Weight ───────────────────────────────────────────────

@Composable
private fun TipsContent() {
    data class TipItem(
        val number: Int,
        val emoji: String,
        val title: String,
        val description: String,
        val color: Color
    )

    val tips = listOf(
        TipItem(
            number = 1,
            emoji = "🥗",
            title = "Eat a balanced diet",
            description = "Focus on whole grains, fruits, vegetables, lean proteins, and healthy fats. Avoid highly processed foods and excessive sugar.",
            color = Color(0xFF43A047)
        ),
        TipItem(
            number = 2,
            emoji = "🏃",
            title = "Stay physically active",
            description = "Aim for at least 150 minutes of moderate exercise per week. Find activities you enjoy — walking, swimming, cycling, or dancing.",
            color = Color(0xFF1E88E5)
        ),
        TipItem(
            number = 3,
            emoji = "💧",
            title = "Stay well hydrated",
            description = "Drink plenty of water throughout the day. Sometimes thirst is mistaken for hunger. Carry a water bottle as a reminder.",
            color = Color(0xFF0277BD)
        ),
        TipItem(
            number = 4,
            emoji = "😴",
            title = "Get quality sleep",
            description = "Aim for 7–9 hours of sleep per night. Poor sleep disrupts hunger hormones and can lead to weight gain over time.",
            color = Color(0xFF5C6BC0)
        ),
        TipItem(
            number = 5,
            emoji = "🧘",
            title = "Manage stress",
            description = "Chronic stress increases cortisol, which can promote fat storage. Try mindfulness, meditation, deep breathing, or yoga.",
            color = Color(0xFF7B1FA2)
        ),
        TipItem(
            number = 6,
            emoji = "📝",
            title = "Track your progress",
            description = "Monitor your weight and health metrics regularly — but not obsessively. Focus on long-term trends, not daily fluctuations.",
            color = Color(0xFFE65100)
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ContentText(
            "Maintaining a healthy weight isn't about extreme diets or intense exercise. It's about sustainable, balanced habits:"
        )

        tips.forEach { tip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = tip.color.copy(alpha = 0.04f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Number circle
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(tip.color.copy(alpha = 0.1f))
                            .border(1.dp, tip.color.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tip.number.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = tip.color
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "${tip.emoji} ${tip.title}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tip.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        HighlightBox(
            emoji = "❤️",
            text = "Remember: Health is more than a number on a scale. Focus on how you feel, your energy levels, and your overall wellbeing."
        )
    }
}

// ─── 6. When to See a Doctor ──────────────────────────────────────────────────

@Composable
private fun WhenDoctorContent() {
    data class DoctorTrigger(
        val emoji: String,
        val text: String
    )

    val triggers = listOf(
        DoctorTrigger("🔴", "Your BMI is below 16 (Severe Thinness) — this may indicate malnutrition or an underlying medical condition"),
        DoctorTrigger("🔴", "Your BMI is 30 or above (Obese) — your doctor can help create a safe, effective plan"),
        DoctorTrigger("📉", "You've experienced unexplained rapid weight loss or gain"),
        DoctorTrigger("💊", "You're taking medications that affect your weight"),
        DoctorTrigger("🍽️", "You have concerns about your eating habits or relationship with food"),
        DoctorTrigger("🫀", "You have existing health conditions like diabetes, heart disease, or high blood pressure"),
        DoctorTrigger("🤰", "You're pregnant or planning to become pregnant"),
        DoctorTrigger("👶", "You're assessing a child's weight — pediatric BMI uses different charts")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ContentText(
            "While this calculator provides useful information, certain situations call for professional medical guidance. Consider seeing a doctor if:"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE53935).copy(alpha = 0.03f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                triggers.forEach { trigger ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = trigger.emoji,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = trigger.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Emergency note
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE53935).copy(alpha = 0.06f),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp, Color(0xFFE53935).copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Your health matters",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFE53935)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "A healthcare professional can provide personalized guidance based on your complete health picture — not just a single number. Don't hesitate to reach out.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ─── Reusable Components ──────────────────────────────────────────────────────

@Composable
private fun ContentText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        lineHeight = 22.sp
    )
}

@Composable
private fun HighlightBox(
    emoji: String,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = EducationAccent.copy(alpha = 0.04f),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, EducationAccent.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = emoji,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun KeyPoint(
    label: String,
    value: String
) {
    Row(modifier = Modifier.padding(start = 4.dp)) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = EducationAccent,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append("$label: ")
                }
                append(value)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            lineHeight = 18.sp
        )
    }
}

// ─── Education Disclaimer ─────────────────────────────────────────────────────

@Composable
private fun EducationDisclaimer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
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
            text = "This educational content is for informational purposes only and does not constitute medical advice. The information provided is based on WHO guidelines and general health recommendations. Always consult a qualified healthcare professional for personalized medical guidance.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            lineHeight = 16.sp
        )
    }
}
