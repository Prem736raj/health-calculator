package com.health.calculator.bmi.tracker.ui.screens.ibw

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IBWEducationalContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Learn About Ideal Body Weight",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 1. What is IBW?
        EducationalExpandableCard(
            icon = Icons.Default.HelpOutline,
            iconColor = Color(0xFF2196F3),
            title = "What is Ideal Body Weight?",
            content = {
                WhatIsIBWContent()
            }
        )

        // 2. Why Different Results?
        EducationalExpandableCard(
            icon = Icons.Default.Calculate,
            iconColor = Color(0xFF9C27B0),
            title = "Why Do Formulas Give Different Results?",
            content = {
                WhyDifferentResultsContent()
            }
        )

        // 3. Ideal vs Healthy Weight
        EducationalExpandableCard(
            icon = Icons.Default.Balance,
            iconColor = Color(0xFF4CAF50),
            title = "Ideal Weight vs Healthy Weight",
            content = {
                IdealVsHealthyContent()
            }
        )

        // 4. Frame Size and Body Type
        EducationalExpandableCard(
            icon = Icons.Default.Accessibility,
            iconColor = Color(0xFFFF9800),
            title = "Frame Size and Body Type",
            content = {
                FrameSizeContent()
            }
        )

        // Medical Disclaimer
        MedicalDisclaimerCard()
    }
}

@Composable
private fun EducationalExpandableCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (expanded) 4.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = iconColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(300)) + fadeOut(tween(200))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
private fun WhatIsIBWContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EducationalParagraph(
            text = "Ideal Body Weight (IBW) is an estimated weight range that medical professionals use as a reference point. It was originally developed to help calculate proper medication dosages, not as a strict health target."
        )

        KeyPointCard(
            emoji = "📋",
            title = "An Estimate, Not a Rule",
            text = "IBW formulas provide a rough estimate based on height and gender. They don't account for muscle mass, bone density, body composition, or ethnic differences. Think of it as a starting point, not a destination."
        )

        KeyPointCard(
            emoji = "💊",
            title = "Medical Origins",
            text = "The most common formula (Devine, 1974) was created for calculating drug dosages in hospitals, not for setting personal weight goals. It was never intended to define what everyone should weigh."
        )

        KeyPointCard(
            emoji = "⚖️",
            title = "Not the Only Measure",
            text = "IBW should never be the sole measure of your health. Fitness level, blood pressure, cholesterol, blood sugar, mental health, and overall well-being are far more important indicators."
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2196F3).copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("💡", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "A muscular person may weigh significantly more than their calculated IBW and be in excellent health. Conversely, someone at their IBW could have poor health markers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2196F3).copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun WhyDifferentResultsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EducationalParagraph(
            text = "You'll notice that different formulas give different ideal weight values. This is completely normal and expected. Here's why:"
        )

        FormulaHistoryItem(
            name = "Devine (1974)",
            detail = "Created for calculating medication doses. Based on limited data from a specific population. Most widely used but not the most accurate for health assessment.",
            highlight = "Most Common"
        )

        FormulaHistoryItem(
            name = "Robinson (1983)",
            detail = "Updated version of earlier formulas with refined coefficients based on larger population studies.",
            highlight = "Refined"
        )

        FormulaHistoryItem(
            name = "Miller (1983)",
            detail = "Tends to give higher values, which may be more realistic for many people.",
            highlight = "More Generous"
        )

        FormulaHistoryItem(
            name = "Hamwi (1964)",
            detail = "One of the oldest formulas. Simple but based on limited 1960s data.",
            highlight = "Classic"
        )

        FormulaHistoryItem(
            name = "BMI-based Range",
            detail = "Uses the WHO healthy BMI range (18.5-24.9) to calculate a weight range. This is generally considered the most holistic approach.",
            highlight = "Most Holistic"
        )

        FormulaHistoryItem(
            name = "Broca Index",
            detail = "Very simple formula (height - 100) with adjustments. Popular in some regions but oversimplified.",
            highlight = "Simplest"
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF9C27B0).copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("🎯", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Recommendation: Focus on the BMI-based healthy weight range rather than any single formula's exact number. A range is more realistic and practical than a specific number.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF9C27B0).copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun IdealVsHealthyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EducationalParagraph(
            text = "Understanding the difference between \"ideal\" weight and \"healthy\" weight is crucial for a healthy relationship with your body."
        )

        // Comparison cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ComparisonCard(
                modifier = Modifier.weight(1f),
                title = "Ideal Weight",
                emoji = "📍",
                color = Color(0xFFFF9800),
                points = listOf(
                    "A single number",
                    "From a formula",
                    "Based on height only",
                    "One-size approach",
                    "Oversimplified"
                )
            )
            ComparisonCard(
                modifier = Modifier.weight(1f),
                title = "Healthy Weight",
                emoji = "🎯",
                color = Color(0xFF4CAF50),
                points = listOf(
                    "A weight RANGE",
                    "From medical evidence",
                    "Considers many factors",
                    "Personalized",
                    "More realistic"
                )
            )
        }

        KeyPointCard(
            emoji = "✅",
            title = "What Matters More Than the Number",
            text = "• Can you perform daily activities with energy?\n• Are your blood markers (BP, cholesterol, glucose) healthy?\n• Do you feel strong and capable?\n• Is your mental health positive?\n• Can you sustain your current lifestyle?\n\nIf you answer yes to these, your weight is likely healthy — regardless of what any formula says."
        )

        KeyPointCard(
            emoji = "📊",
            title = "The Range Approach",
            text = "Being anywhere within the BMI 18.5-24.9 range for your height is considered healthy. That's a range of about 15-20 kg for most adults. Aim for a weight within this range that feels sustainable and energetic for you."
        )
    }
}

@Composable
private fun FrameSizeContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EducationalParagraph(
            text = "Not all bodies are built the same. Your skeletal frame size affects what's a healthy weight for you."
        )

        KeyPointCard(
            emoji = "🦴",
            title = "What is Frame Size?",
            text = "Frame size refers to your bone structure — the width and thickness of your bones. People with larger frames naturally weigh more because bone is heavy, dense tissue."
        )

        // Frame size descriptions
        FrameTypeItem(
            type = "Small Frame",
            emoji = "🔹",
            description = "Narrower shoulders, smaller wrist circumference, lighter bone structure. May naturally weigh 10% less than medium-frame reference values.",
            wristGuide = "Wrist feels delicate, thumb and middle finger overlap when wrapped around"
        )

        FrameTypeItem(
            type = "Medium Frame",
            emoji = "🔶",
            description = "Average bone structure. Most IBW formulas are calibrated for medium-frame individuals. The \"standard\" reference.",
            wristGuide = "Thumb and middle finger just touch when wrapped around wrist"
        )

        FrameTypeItem(
            type = "Large Frame",
            emoji = "🔷",
            description = "Broader shoulders, wider wrist, denser bone structure. May naturally weigh 10% more than medium-frame reference values.",
            wristGuide = "Thumb and middle finger don't meet when wrapped around wrist"
        )

        KeyPointCard(
            emoji = "🧬",
            title = "Body Type Theory (Somatotypes)",
            text = "While not strictly scientific, three general body types are often discussed:\n\n• Ectomorph: Naturally lean, long limbs, fast metabolism\n• Mesomorph: Athletic build, gains muscle easily\n• Endomorph: Wider build, gains weight more easily\n\nMost people are a combination of these types."
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFF9800).copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("💡", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Remember: Frame size explains why two people of the same height can have different healthy weights. One size truly doesn't fit all!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9800).copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EducationalParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        lineHeight = 22.sp
    )
}

@Composable
private fun KeyPointCard(
    emoji: String,
    title: String,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun FormulaHistoryItem(
    name: String,
    detail: String,
    highlight: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = highlight,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ComparisonCard(
    modifier: Modifier = Modifier,
    title: String,
    emoji: String,
    color: Color,
    points: List<String>
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = color,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            points.forEach { point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "•",
                        color = color,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = point,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FrameTypeItem(
    type: String,
    emoji: String,
    description: String,
    wristGuide: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Quick test: $wristGuide",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MedicalDisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336).copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.MedicalInformation,
                contentDescription = null,
                tint = Color(0xFFF44336).copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Medical Disclaimer",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF44336).copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "This information is for educational purposes only and should not replace professional medical advice. Ideal body weight is an estimate and may not be appropriate for everyone. Always consult a healthcare provider for personalized guidance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336).copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
