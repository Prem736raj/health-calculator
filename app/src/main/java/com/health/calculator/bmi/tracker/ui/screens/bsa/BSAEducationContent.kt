package com.health.calculator.bmi.tracker.ui.screens.bsa

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.ui.theme.*

@Composable
fun BSAEducationScreen(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Learn About BSA",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Everything you need to know about Body Surface Area",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Section 1
        BSAEducationSection(
            icon = "📐",
            title = "What is Body Surface Area?",
            accentColor = HealthBlue
        ) {
            WhatIsBSAContent()
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 2
        BSAEducationSection(
            icon = "🧮",
            title = "Why So Many Formulas?",
            accentColor = HealthOrange
        ) {
            WhySoManyFormulasContent()
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 3
        BSAEducationSection(
            icon = "🎯",
            title = "Which Formula Should I Use?",
            accentColor = HealthGreen
        ) {
            WhichFormulaContent()
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 4
        BSAEducationSection(
            icon = "⚖️",
            title = "BSA vs BMI",
            accentColor = HealthTeal
        ) {
            BSAvsBMIContent()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Disclaimer
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "This educational content is for informational purposes only. For any medical questions or concerns, please consult a qualified healthcare professional.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BSAEducationSection(
    icon: String,
    title: String,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 3.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = !expanded
            }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = accentColor
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(350)) + fadeIn(tween(350)),
                exit = shrinkVertically(tween(250)) + fadeOut(tween(200))
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    HorizontalDivider(
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                    content()
                }
            }
        }
    }
}

// ─────────────────────────────────────────
// Section 1: What is BSA?
// ─────────────────────────────────────────

@Composable
private fun WhatIsBSAContent() {
    Column {
        // Relatable analogy
        AnalygyCard(
            emoji = "🛏️",
            text = "Imagine wrapping your entire body in a thin sheet — from the top of your head to the tips of your toes, including your arms, legs, and everything in between. The total area of that sheet is your Body Surface Area (BSA)."
        )

        Spacer(modifier = Modifier.height(14.dp))

        Paragraph(
            text = "Body Surface Area is the total area of the external surface of the human body, measured in square meters (m²). While it might seem like a simple concept, accurately measuring or estimating this area is surprisingly complex because of the irregular shape of the human body."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Key facts
        Text(
            text = "Key Facts About BSA",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = HealthBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FactItem(
            emoji = "👤",
            title = "Average Adult BSA",
            description = "An average adult male has a BSA of about 1.9 m², while an average adult female is about 1.6 m². A newborn baby has a BSA of only about 0.25 m²."
        )
        FactItem(
            emoji = "📏",
            title = "Based on Weight & Height",
            description = "Since directly measuring body surface is impractical, BSA is calculated from your weight and height using mathematical formulas."
        )
        FactItem(
            emoji = "🏥",
            title = "Critical in Medicine",
            description = "Doctors use BSA for calculating medication doses (especially chemotherapy), assessing burn injuries, determining kidney function, and evaluating heart performance."
        )
        FactItem(
            emoji = "🔬",
            title = "More Accurate Than Weight Alone",
            description = "BSA correlates better with metabolic rate, blood volume, and organ size than body weight alone, making it a more precise basis for many medical calculations."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Visual comparison
        Card(
            colors = CardDefaults.cardColors(
                containerColor = HealthBlue.copy(alpha = 0.06f)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "To Put It in Perspective...",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = HealthBlue
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PerspectiveItem(emoji = "👶", label = "Baby", value = "~0.25 m²", description = "About the size\nof a pillow")
                    PerspectiveItem(emoji = "🧒", label = "Child (10yr)", value = "~1.1 m²", description = "About the size\nof a desk")
                    PerspectiveItem(emoji = "🧑", label = "Adult", value = "~1.7 m²", description = "About the size\nof a door")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Paragraph(
            text = "Fun fact: Your skin is actually your largest organ! It performs vital functions like temperature regulation, protection from infection, and sensation. Understanding BSA helps medical professionals work with this important organ system."
        )
    }
}

@Composable
private fun PerspectiveItem(emoji: String, label: String, value: String, description: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(95.dp)
    ) {
        Text(emoji, fontSize = 28.sp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.ExtraBold,
            color = HealthBlue
        )
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            fontSize = 9.sp
        )
    }
}

// ─────────────────────────────────────────
// Section 2: Why So Many Formulas?
// ─────────────────────────────────────────

@Composable
private fun WhySoManyFormulasContent() {
    Column {
        Paragraph(
            text = "Over the past century, scientists have developed numerous formulas to estimate BSA. Each was created in a different era, using different populations and measurement techniques. Here's the fascinating story:"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Timeline
        Text(
            text = "A Century of BSA Research",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = HealthOrange,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        TimelineItem(
            year = "1916",
            formula = "Du Bois & Du Bois",
            highlight = "The Original",
            description = "Two brothers used a coating method on just 9 individuals to create the first practical BSA formula. Despite its small sample size, it became the gold standard and remains the most widely used formula over 100 years later. A remarkable achievement in medical history.",
            color = HealthBlue
        )

        TimelineItem(
            year = "1925",
            formula = "Takahira",
            highlight = "Asian Perspective",
            description = "Developed using Japanese population data, recognizing that body proportions vary across ethnicities. One of the first formulas to address population-specific differences.",
            color = HealthTeal
        )

        TimelineItem(
            year = "1935",
            formula = "Boyd",
            highlight = "Expanded Data",
            description = "Used a much larger dataset than Du Bois, improving accuracy across a wider range of body sizes. Introduced a more complex mathematical approach.",
            color = HealthYellow
        )

        TimelineItem(
            year = "1968",
            formula = "Fujimoto",
            highlight = "Japanese Population",
            description = "Specifically designed for the Japanese population using local population measurements, providing better accuracy for East Asian body types.",
            color = HealthTeal
        )

        TimelineItem(
            year = "1970",
            formula = "Gehan & George",
            highlight = "Large Dataset",
            description = "Based on 401 subjects — the largest dataset at the time. Provided robust results across a wide range of body sizes from children to large adults.",
            color = HealthGreen
        )

        TimelineItem(
            year = "1978",
            formula = "Haycock",
            highlight = "Pediatric Focus",
            description = "Specifically validated for infants, children, and adolescents. Addressed the limitation that adult-derived formulas were less accurate for small body sizes. Now the preferred formula for pediatric use.",
            color = HealthOrange
        )

        TimelineItem(
            year = "1987",
            formula = "Mosteller",
            highlight = "Simplicity",
            description = "Created an elegantly simple formula using just a square root calculation. Easy to compute even without a calculator. Produces results very close to Du Bois for most adults.",
            color = HealthGreen
        )

        TimelineItem(
            year = "2000",
            formula = "Shuter & Aslani",
            highlight = "CT-Based",
            description = "Used modern CT (computed tomography) scanning to directly measure body surface area, then developed a formula from these precise measurements. Considered the most anatomically accurate.",
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Why no single formula is perfect
        HighlightCard(
            emoji = "🤔",
            title = "Why Can't We Have Just One Perfect Formula?",
            color = HealthOrange,
            content = "No single formula works perfectly for every person because:\n\n• Body proportions vary significantly between individuals\n• Different ethnic groups have different average body shapes\n• Children have different proportions than adults\n• Very obese or very thin individuals deviate from averages\n• Each formula was derived from a specific population sample\n• Measurement techniques have improved over time\n\nThe good news? For most practical purposes, all formulas give results within about 5% of each other."
        )

        Spacer(modifier = Modifier.height(10.dp))

        QuickStatRow(
            stats = listOf(
                Pair("107+ years", "since the first formula"),
                Pair("8+ formulas", "commonly used today"),
                Pair("< 5%", "typical variation between them")
            )
        )
    }
}

@Composable
private fun TimelineItem(
    year: String,
    formula: String,
    highlight: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Timeline connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(44.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(color.copy(alpha = 0.2f))
            )
        }

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = color.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = year,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formula,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = highlight,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

// ─────────────────────────────────────────
// Section 3: Which Formula Should I Use?
// ─────────────────────────────────────────

@Composable
private fun WhichFormulaContent() {
    Column {
        Paragraph(
            text = "Choosing the right formula depends on who you are and what the calculation is for. Here's a simple guide:"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Decision guide cards
        FormulaGuideCard(
            emoji = "🏥",
            scenario = "General Adult Use",
            recommendation = "Du Bois & Du Bois",
            reason = "The most widely accepted formula worldwide. Used by the vast majority of hospitals and clinical guidelines. When your doctor mentions BSA, they almost certainly use this formula.",
            color = HealthBlue,
            tag = "RECOMMENDED"
        )

        Spacer(modifier = Modifier.height(8.dp))

        FormulaGuideCard(
            emoji = "⚡",
            scenario = "Quick Calculation",
            recommendation = "Mosteller",
            reason = "The simplest formula — just multiply weight by height, divide by 3600, and take the square root. Results are very close to Du Bois for most adults. Perfect when you need a quick estimate.",
            color = HealthGreen,
            tag = "EASIEST"
        )

        Spacer(modifier = Modifier.height(8.dp))

        FormulaGuideCard(
            emoji = "👶",
            scenario = "Children & Infants",
            recommendation = "Haycock",
            reason = "Specifically validated for pediatric patients from newborns to adolescents. Adult formulas can significantly overestimate or underestimate BSA in children because their body proportions are different.",
            color = HealthOrange,
            tag = "PEDIATRIC"
        )

        Spacer(modifier = Modifier.height(8.dp))

        FormulaGuideCard(
            emoji = "🌏",
            scenario = "Japanese / East Asian",
            recommendation = "Fujimoto or Takahira",
            reason = "These formulas were derived from Asian population data and may provide more accurate results for individuals of Japanese or East Asian descent. Commonly used in Japan and some other Asian countries.",
            color = HealthTeal,
            tag = "REGIONAL"
        )

        Spacer(modifier = Modifier.height(8.dp))

        FormulaGuideCard(
            emoji = "🔬",
            scenario = "Research / Maximum Accuracy",
            recommendation = "Shuter & Aslani or Gehan & George",
            reason = "Shuter & Aslani uses modern CT-based measurements for the highest anatomical accuracy. Gehan & George is based on the largest traditional dataset (401 subjects). Both are good choices for research applications.",
            color = MaterialTheme.colorScheme.tertiary,
            tag = "RESEARCH"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Simple decision tree
        HighlightCard(
            emoji = "🤷",
            title = "Still Not Sure? Use This Simple Rule:",
            color = HealthGreen,
            content = "• If you're an adult → Du Bois\n• If it's for a child → Haycock\n• If you want it simple → Mosteller\n• If you're East Asian → Fujimoto\n• If your doctor told you a specific formula → Use that one!\n\nWhen in doubt, Du Bois is always a safe choice. It's been the worldwide standard for over a century."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Agreement note
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("💡", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Good news: For most adults, all formulas give results within about 3-5% of each other. The differences are usually too small to matter in most practical situations. The formula choice matters most at extreme body sizes (very small children or very large adults).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun FormulaGuideCard(
    emoji: String,
    scenario: String,
    recommendation: String,
    reason: String,
    color: Color,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scenario,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Use: $recommendation",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

// ─────────────────────────────────────────
// Section 4: BSA vs BMI
// ─────────────────────────────────────────

@Composable
private fun BSAvsBMIContent() {
    Column {
        Paragraph(
            text = "BSA and BMI are both calculated from your weight and height, but they measure very different things and serve entirely different purposes. Understanding the distinction helps you know when each matters."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Side-by-side comparison
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Head-to-Head Comparison",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(0.8f))
                    Text(
                        text = "BSA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = HealthBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "BMI",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = HealthGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                ComparisonRow("Stands for", "Body Surface Area", "Body Mass Index")
                ComparisonRow("Measures", "External body surface", "Weight relative to height")
                ComparisonRow("Unit", "m² (square meters)", "kg/m² (no unit name)")
                ComparisonRow("Formula type", "Empirical power equations", "Simple division")
                ComparisonRow("Main use", "Medical dosing & assessment", "Health status indicator")
                ComparisonRow("WHO categories", "No categories", "Underweight to Obese")
                ComparisonRow("Typical adult", "1.6 – 2.0 m²", "18.5 – 24.9")
                ComparisonRow("Used by", "Doctors & pharmacists", "Everyone & public health")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // When to use each
        Text(
            text = "When Each Measurement Matters",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        UseCaseCard(
            emoji = "📐",
            title = "Use BSA when...",
            color = HealthBlue,
            useCases = listOf(
                "Calculating medication doses (especially chemotherapy)",
                "Assessing burn injury extent",
                "Evaluating kidney function (GFR normalization)",
                "Measuring cardiac output (Cardiac Index)",
                "Determining radiation therapy doses",
                "Research involving metabolic rate"
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        UseCaseCard(
            emoji = "⚖️",
            title = "Use BMI when...",
            color = HealthGreen,
            useCases = listOf(
                "Screening for weight-related health risks",
                "Setting weight management goals",
                "Public health population studies",
                "Insurance and general health assessment",
                "Tracking your weight status over time",
                "Quick general health check"
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Key insight
        HighlightCard(
            emoji = "💡",
            title = "The Key Difference",
            color = HealthTeal,
            content = "Think of it this way:\n\n• BMI tells you \"Is my weight healthy for my height?\" → It's about YOUR health status.\n\n• BSA tells doctors \"How large is this person's body?\" → It's about dosing and medical calculations.\n\nBMI is a health indicator for you to track. BSA is a medical tool your doctor uses behind the scenes. Both are calculated from weight and height, but they answer completely different questions."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Common misconceptions
        Text(
            text = "Common Misconceptions",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MythBusterItem(
            myth = "BSA and BMI are interchangeable",
            truth = "They measure completely different things. A person with a high BMI could have an average BSA, and vice versa."
        )
        MythBusterItem(
            myth = "BSA tells you if you're overweight",
            truth = "BSA has no \"healthy\" or \"unhealthy\" ranges — it's simply a body size measurement, not a health indicator."
        )
        MythBusterItem(
            myth = "You need to know your BSA for daily health tracking",
            truth = "BSA is primarily a medical/clinical tool. For general health tracking, BMI, weight, and waist circumference are more useful."
        )
        MythBusterItem(
            myth = "A higher BSA means you're less healthy",
            truth = "BSA simply reflects body size. Taller and heavier people naturally have higher BSA regardless of health status."
        )
    }
}

@Composable
private fun ComparisonRow(label: String, bsaValue: String, bmiValue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.8f)
        )
        Text(
            text = bsaValue,
            style = MaterialTheme.typography.labelSmall,
            color = HealthBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            lineHeight = 14.sp
        )
        Text(
            text = bmiValue,
            style = MaterialTheme.typography.labelSmall,
            color = HealthGreen,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun UseCaseCard(
    emoji: String,
    title: String,
    color: Color,
    useCases: List<String>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            useCases.forEach { useCase ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = useCase,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MythBusterItem(myth: String, truth: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text("❌", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Myth: \"$myth\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthRed
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Top) {
                Text("✅", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Truth: $truth",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────
// Shared Components
// ─────────────────────────────────────────

@Composable
private fun Paragraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp
    )
}

@Composable
private fun AnalygyCard(emoji: String, text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FactItem(emoji: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, fontSize = 20.sp, modifier = Modifier.width(30.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun HighlightCard(emoji: String, title: String, color: Color, content: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun QuickStatRow(stats: List<Pair<String, String>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        stats.forEach { (value, label) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = HealthOrange
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
