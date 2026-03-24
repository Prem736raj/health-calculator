package com.health.calculator.bmi.tracker.ui.screens.whr

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhrEducationalScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Learn About WHR", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            EducationalHeader()

            // Section 1: What is WHR
            WhatIsWhrSection()

            // Section 2: WHR vs BMI
            WhrVsBmiSection()

            // Section 3: Central Obesity & Disease Risk
            CentralObesityRiskSection()

            // Section 4: How to Reduce Waist Circumference
            ReduceWaistSection()

            // Section 5: Correct Measurement Technique
            MeasurementTechniqueSection()

            // Medical Disclaimer
            MedicalDisclaimerCard()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EducationalHeader() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📚", fontSize = 28.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "WHR Knowledge Center",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Everything you need to know about Waist-to-Hip Ratio and its impact on your health",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION 1: What is WHR
// ═══════════════════════════════════════════════════════════

@Composable
private fun WhatIsWhrSection() {
    ExpandableEducationalSection(
        icon = Icons.Outlined.HelpOutline,
        emoji = "📐",
        title = "What is Waist-to-Hip Ratio?",
        subtitle = "Understanding the basics of WHR",
        accentColor = MaterialTheme.colorScheme.primary,
        initialExpanded = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoParagraph(
                "Waist-to-Hip Ratio (WHR) is a simple measurement that compares the circumference of your waist to the circumference of your hips. It's calculated by dividing your waist measurement by your hip measurement."
            )

            FormulaCard(
                formula = "WHR = Waist Circumference ÷ Hip Circumference",
                example = "Example: 80 cm waist ÷ 100 cm hip = 0.80 WHR"
            )

            SectionSubheading("Why does WHR matter?")

            InfoParagraph(
                "WHR is one of the best indicators of how your body distributes fat. Unlike weight or BMI alone, WHR specifically reveals whether you carry excess fat around your midsection — a pattern strongly linked to serious health conditions."
            )

            KeyPointCard(
                emoji = "🔬",
                title = "Research Finding",
                text = "Studies published in The Lancet show that WHR is a stronger predictor of heart attack risk than BMI. People with a high WHR have up to 2x the risk of cardiovascular events, regardless of their overall weight."
            )

            SectionSubheading("WHO Classifications")

            WhrReferenceTable()

            InfoParagraph(
                "A healthy WHR indicates that you don't carry excess fat around your abdomen. Even people with normal BMI can have a high WHR, which still puts them at increased risk."
            )
        }
    }
}

@Composable
private fun WhrReferenceTable() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    "Risk Level",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Male",
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Female",
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TableRow("Low Risk", "< 0.90", "< 0.80", Color(0xFF4CAF50))
            TableRow("Moderate", "0.90 – 0.99", "0.80 – 0.84", Color(0xFFFFA726))
            TableRow("High Risk", "≥ 1.00", "≥ 0.85", Color(0xFFF44336))
        }
    }
}

@Composable
private fun TableRow(
    label: String,
    maleValue: String,
    femaleValue: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
        Text(
            maleValue,
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Text(
            femaleValue,
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION 2: WHR vs BMI
// ═══════════════════════════════════════════════════════════

@Composable
private fun WhrVsBmiSection() {
    ExpandableEducationalSection(
        icon = Icons.Outlined.CompareArrows,
        emoji = "⚖️",
        title = "WHR vs BMI",
        subtitle = "Why using both gives the complete picture",
        accentColor = Color(0xFF2196F3)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoParagraph(
                "BMI and WHR measure different aspects of health. BMI estimates overall body fat based on height and weight, while WHR specifically measures where your body stores fat. Using both together provides a much more complete health assessment."
            )

            // Comparison cards side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComparisonMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "BMI",
                    emoji = "📊",
                    color = Color(0xFF2196F3),
                    points = listOf(
                        "Overall body fat estimate",
                        "Based on height & weight",
                        "Simple and widely used",
                        "Cannot distinguish fat vs muscle",
                        "Misses central obesity"
                    )
                )
                ComparisonMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "WHR",
                    emoji = "📐",
                    color = Color(0xFF9C27B0),
                    points = listOf(
                        "Fat distribution pattern",
                        "Based on waist & hip",
                        "Better for disease risk",
                        "Identifies central obesity",
                        "Complements BMI data"
                    )
                )
            }

            SectionSubheading("When WHR is more informative")

            BulletList(
                items = listOf(
                    Pair("Normal BMI but high WHR", "You may have a normal weight but carry too much fat around your middle — this is called \"metabolically obese normal weight\" (MONW) and still poses health risks."),
                    Pair("Athletes and muscular individuals", "BMI can overestimate body fat in muscular people. WHR helps determine if the weight is distributed healthily."),
                    Pair("Older adults", "As we age, muscle is often replaced by fat, especially around the abdomen. WHR captures this shift even when BMI stays the same."),
                    Pair("Post-menopausal women", "Hormonal changes cause fat redistribution toward the abdomen. WHR tracks this important change.")
                )
            )

            KeyPointCard(
                emoji = "💡",
                title = "Best Practice",
                text = "Health professionals recommend using BMI and WHR together. BMI assesses overall weight status, while WHR evaluates fat distribution. A person with normal BMI but high WHR may be at greater risk than someone with slightly elevated BMI but healthy WHR."
            )

            SectionSubheading("The 4-quadrant approach")

            QuadrantGrid()
        }
    }
}

@Composable
private fun ComparisonMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    emoji: String,
    color: Color,
    points: List<String>
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 16.sp)
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            points.forEach { point ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("•", fontSize = 10.sp, color = color)
                    Text(
                        point,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuadrantGrid() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Combined BMI + WHR Assessment",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuadrantCell(
                    Modifier.weight(1f),
                    "Normal BMI\nNormal WHR",
                    "✅ Lowest Risk",
                    Color(0xFF4CAF50)
                )
                QuadrantCell(
                    Modifier.weight(1f),
                    "Normal BMI\nHigh WHR",
                    "⚠️ Hidden Risk",
                    Color(0xFFFFA726)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuadrantCell(
                    Modifier.weight(1f),
                    "High BMI\nNormal WHR",
                    "⚠️ Moderate Risk",
                    Color(0xFFFFA726)
                )
                QuadrantCell(
                    Modifier.weight(1f),
                    "High BMI\nHigh WHR",
                    "🔴 Highest Risk",
                    Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun QuadrantCell(
    modifier: Modifier,
    label: String,
    risk: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
            Text(
                risk,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION 3: Central Obesity & Disease Risk
// ═══════════════════════════════════════════════════════════

@Composable
private fun CentralObesityRiskSection() {
    ExpandableEducationalSection(
        icon = Icons.Outlined.MonitorHeart,
        emoji = "❤️🔥",
        title = "Central Obesity and Disease Risk",
        subtitle = "How fat distribution affects your health",
        accentColor = Color(0xFFF44336)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoParagraph(
                "Central obesity — excess fat stored in the abdominal area — is one of the strongest predictors of chronic disease, independent of overall body weight. The location of fat matters as much as the amount."
            )

            SectionSubheading("Cardiovascular Disease")

            DiseaseRiskCard(
                emoji = "❤️",
                disease = "Heart Disease & Stroke",
                riskInfo = "People with high WHR have 1.5–2x the risk of heart attack compared to those with normal WHR.",
                source = "INTERHEART Study (52 countries, 27,000+ participants)",
                color = Color(0xFFF44336),
                details = listOf(
                    "Central obesity increases LDL cholesterol and triglycerides",
                    "Visceral fat promotes arterial inflammation",
                    "High WHR is linked to 24% higher risk of stroke",
                    "Risk exists even in people with normal overall weight"
                )
            )

            SectionSubheading("Metabolic Conditions")

            DiseaseRiskCard(
                emoji = "🩸",
                disease = "Type 2 Diabetes",
                riskInfo = "Central obesity increases diabetes risk by 3-5x compared to lower body obesity.",
                source = "WHO Global Report on Diabetes",
                color = Color(0xFFFFA726),
                details = listOf(
                    "Visceral fat interferes with insulin signaling",
                    "Waist circumference is a stronger diabetes predictor than BMI",
                    "Each 5 cm increase in waist raises diabetes risk by 11%",
                    "Waist reduction can improve insulin sensitivity within weeks"
                )
            )

            DiseaseRiskCard(
                emoji = "⚠️",
                disease = "Metabolic Syndrome",
                riskInfo = "Central obesity is the primary criterion — present in over 80% of metabolic syndrome cases.",
                source = "International Diabetes Federation",
                color = Color(0xFF9C27B0),
                details = listOf(
                    "Defined as 3+ of: large waist, high BP, high sugar, high triglycerides, low HDL",
                    "Doubles the risk of cardiovascular disease",
                    "Increases diabetes risk 5-fold",
                    "Affects approximately 25% of adults worldwide"
                )
            )

            SectionSubheading("Other Health Impacts")

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniRiskItem("🧠", "Alzheimer's Disease", "Central obesity in midlife linked to 3x higher dementia risk in later life")
                MiniRiskItem("🫁", "Sleep Apnea", "High WHR associated with 2x greater risk of obstructive sleep apnea")
                MiniRiskItem("🎗️", "Certain Cancers", "Increased risk of breast, colorectal, and liver cancers linked to abdominal fat")
                MiniRiskItem("💉", "Hypertension", "Visceral fat releases hormones that constrict blood vessels and raise blood pressure")
                MiniRiskItem("🦴", "Joint Problems", "Excess abdominal weight places additional stress on the lower back and knees")
                MiniRiskItem("😞", "Depression", "Studies show a bidirectional relationship between central obesity and depression")
            }

            KeyPointCard(
                emoji = "📊",
                title = "Key Statistic",
                text = "The INTERHEART study found that WHR explained 24.3% of heart attack risk — making it the single most important modifiable risk factor, ahead of smoking (19.4%) and diabetes (9.9%)."
            )
        }
    }
}

@Composable
private fun DiseaseRiskCard(
    emoji: String,
    disease: String,
    riskInfo: String,
    source: String,
    color: Color,
    details: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(emoji, fontSize = 22.sp)
                    Text(
                        disease,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Text(
                riskInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )

            Text(
                "Source: $source",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontStyle = FontStyle.Italic,
                fontSize = 10.sp
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(color = color.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(4.dp))
                    details.forEach { detail ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", color = color, fontSize = 12.sp)
                            Text(
                                detail,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniRiskItem(emoji: String, title: String, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, fontSize = 16.sp)
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 15.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION 4: How to Reduce Waist
// ═══════════════════════════════════════════════════════════

@Composable
private fun ReduceWaistSection() {
    ExpandableEducationalSection(
        icon = Icons.Outlined.FitnessCenter,
        emoji = "💪",
        title = "How to Reduce Waist Circumference",
        subtitle = "Evidence-based strategies for a healthier waistline",
        accentColor = Color(0xFF4CAF50)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoParagraph(
                "Reducing waist circumference involves targeting visceral fat through a combination of exercise, diet, and lifestyle changes. Here are the most effective strategies backed by research."
            )

            SectionSubheading("🏃 Exercise Strategies")

            NumberedStrategy(
                number = 1,
                title = "Aerobic Exercise (Most Effective)",
                description = "Brisk walking, jogging, cycling, or swimming for 150-300 minutes per week. Aerobic exercise is the single most effective method for reducing visceral fat.",
                keyFact = "Research shows 150 min/week of moderate cardio reduces waist by 2-3 cm over 12 weeks",
                color = Color(0xFF4CAF50)
            )

            NumberedStrategy(
                number = 2,
                title = "High-Intensity Interval Training (HIIT)",
                description = "Alternating between intense bursts (30-60 seconds) and recovery periods. HIIT is 28% more effective at reducing abdominal fat than continuous moderate exercise.",
                keyFact = "3 HIIT sessions per week for 12 weeks can reduce waist by 3-4 cm",
                color = Color(0xFF4CAF50)
            )

            NumberedStrategy(
                number = 3,
                title = "Resistance Training",
                description = "Weight training 2-3 times per week increases muscle mass, which boosts metabolism and helps burn visceral fat even at rest.",
                keyFact = "Combining cardio with strength training is 50% more effective than cardio alone",
                color = Color(0xFF4CAF50)
            )

            SectionSubheading("🥗 Dietary Approaches")

            NumberedStrategy(
                number = 4,
                title = "Increase Soluble Fiber",
                description = "Eat more oats, beans, lentils, flaxseeds, avocados, and Brussels sprouts. Soluble fiber reduces belly fat by slowing digestion and reducing calorie absorption.",
                keyFact = "Each 10g increase in daily soluble fiber reduces visceral fat by 3.7% over 5 years",
                color = Color(0xFFFFA726)
            )

            NumberedStrategy(
                number = 5,
                title = "Reduce Added Sugars & Refined Carbs",
                description = "Cut back on sugary drinks, white bread, pastries, and processed snacks. Excess sugar is directly converted to visceral fat by the liver.",
                keyFact = "Eliminating sugary drinks alone can reduce waist circumference by 1-2 cm in 10 weeks",
                color = Color(0xFFFFA726)
            )

            NumberedStrategy(
                number = 6,
                title = "Eat More Protein",
                description = "Aim for 25-30% of calories from protein. Higher protein intake reduces appetite, boosts metabolism, and preferentially helps retain lean mass during weight loss.",
                keyFact = "Higher protein diets lead to 2x more reduction in waist circumference",
                color = Color(0xFFFFA726)
            )

            NumberedStrategy(
                number = 7,
                title = "Mediterranean Diet Pattern",
                description = "Focus on olive oil, fish, nuts, vegetables, fruits, and whole grains. This dietary pattern is proven to reduce visceral fat even without calorie counting.",
                keyFact = "Mediterranean diet reduces waist by 2-4 cm more than low-fat diets over 12 months",
                color = Color(0xFFFFA726)
            )

            SectionSubheading("🧘 Lifestyle Changes")

            NumberedStrategy(
                number = 8,
                title = "Prioritize Sleep (7-9 Hours)",
                description = "Short sleep (<6 hours) increases cortisol and ghrelin, promoting visceral fat storage. Quality sleep is essential for waist reduction.",
                keyFact = "People sleeping <5 hours gain 2.5x more abdominal fat over 5 years",
                color = Color(0xFF2196F3)
            )

            NumberedStrategy(
                number = 9,
                title = "Manage Stress",
                description = "Chronic stress elevates cortisol, which directly triggers visceral fat storage. Practice meditation, deep breathing, yoga, or regular relaxation.",
                keyFact = "8 weeks of mindfulness meditation reduces cortisol by 25% and waist by 1-2 cm",
                color = Color(0xFF2196F3)
            )

            NumberedStrategy(
                number = 10,
                title = "Limit Alcohol Consumption",
                description = "Excess alcohol is metabolized by the liver and converted to fat, much of it stored viscerally. Moderate or eliminate alcohol for best results.",
                keyFact = "Reducing alcohol to ≤1 drink/day can decrease waist by 2-3 cm over 6 months",
                color = Color(0xFF2196F3)
            )

            SectionSubheading("📅 Realistic Timeline")

            TimelineCard()

            KeyPointCard(
                emoji = "⭐",
                title = "Most Important",
                text = "Consistency beats intensity. A sustainable routine of moderate exercise, balanced diet, good sleep, and stress management will produce lasting results. Crash diets and extreme exercise often backfire, as the body protects visceral fat during extreme restriction."
            )
        }
    }
}

@Composable
private fun NumberedStrategy(
    number: Int,
    title: String,
    description: String,
    keyFact: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$number",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = color.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("📊", fontSize = 11.sp)
                    Text(
                        keyFact,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 14.sp,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Expected Timeline for Waist Reduction",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            TimelineItem("Weeks 1-2", "Internal changes begin — insulin sensitivity improves, inflammation decreases. Visible changes may not be apparent yet.", "🌱")
            TimelineItem("Weeks 3-4", "First measurable changes — expect 1-2 cm reduction in waist circumference.", "📏")
            TimelineItem("Months 2-3", "Noticeable changes — 3-5 cm reduction with consistent effort. Clothes fit differently.", "👖")
            TimelineItem("Months 4-6", "Significant progress — 5-10 cm total reduction possible. WHR category may improve.", "📉")
            TimelineItem("6-12 Months", "Substantial transformation — 10+ cm reduction achievable. Risk category likely changed.", "🏆")

            Text(
                "Note: Results vary based on starting point, genetics, and adherence. A safe rate is 1-2 cm per month.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun TimelineItem(period: String, description: String, emoji: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 16.sp)
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            )
        }
        Column {
            Text(
                period,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SECTION 5: Correct Measurement Technique
// ═══════════════════════════════════════════════════════════

@Composable
private fun MeasurementTechniqueSection() {
    ExpandableEducationalSection(
        icon = Icons.Outlined.Straighten,
        emoji = "📏",
        title = "Correct Measurement Technique",
        subtitle = "Step-by-step guide with do's and don'ts",
        accentColor = Color(0xFF795548)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoParagraph(
                "Accurate and consistent measurements are essential for meaningful tracking. Even small errors in technique can change your WHR by 0.05-0.10, potentially shifting your risk category."
            )

            SectionSubheading("📋 Step-by-Step Guide")

            MeasurementStep(
                step = 1,
                title = "Prepare",
                instructions = listOf(
                    "Use a flexible, non-stretchy measuring tape (cloth or fiberglass)",
                    "Measure in light clothing or directly on skin",
                    "Remove any belt or tight waistband",
                    "Stand in front of a mirror if possible"
                )
            )

            MeasurementStep(
                step = 2,
                title = "Position Yourself",
                instructions = listOf(
                    "Stand upright with feet hip-width apart",
                    "Arms relaxed at your sides",
                    "Distribute weight evenly on both feet",
                    "Don't lean to one side"
                )
            )

            MeasurementStep(
                step = 3,
                title = "Find Waist Landmark",
                instructions = listOf(
                    "Place hands on hips and find the top of your hip bones (iliac crest)",
                    "The waist is measured at the narrowest point of your torso",
                    "This is usually just above the belly button, at or slightly above the iliac crest",
                    "If no narrowing is visible, measure at the navel level"
                )
            )

            MeasurementStep(
                step = 4,
                title = "Measure Waist",
                instructions = listOf(
                    "Wrap the tape around your waist at the identified point",
                    "Ensure the tape is snug but not compressing the skin",
                    "The tape must be level and parallel to the floor all the way around",
                    "Breathe normally — take the reading after a gentle exhale",
                    "Don't hold your breath or suck in your stomach"
                )
            )

            MeasurementStep(
                step = 5,
                title = "Find Hip Landmark",
                instructions = listOf(
                    "The hip is measured at the widest point of the buttocks",
                    "Stand sideways in front of a mirror to identify the widest point",
                    "This is typically at the level of the greater trochanter (top of the thigh bone)"
                )
            )

            MeasurementStep(
                step = 6,
                title = "Measure Hips",
                instructions = listOf(
                    "Wrap the tape around the widest part of the buttocks",
                    "Keep the tape parallel to the floor",
                    "The tape should be snug without indenting the skin",
                    "Stand naturally — don't clench or tighten your glutes"
                )
            )

            MeasurementStep(
                step = 7,
                title = "Record & Repeat",
                instructions = listOf(
                    "Read to the nearest 0.5 cm or 0.25 inch",
                    "Take 2-3 measurements for each (waist and hip)",
                    "Use the average of the readings",
                    "If readings vary by >1 cm, remeasure until consistent"
                )
            )

            SectionSubheading("⏰ Best Time to Measure")

            BestTimingCard()

            SectionSubheading("✅ Do's and Don'ts")

            DosAndDontsCard()

            SectionSubheading("⚠️ Common Mistakes")

            CommonMistakesList()

            SectionSubheading("📊 Consistency Tips")

            ConsistencyTipsCard()
        }
    }
}

@Composable
private fun MeasurementStep(
    step: Int,
    title: String,
    instructions: List<String>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$step",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            instructions.forEach { instruction ->
                Row(
                    modifier = Modifier.padding(start = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    Text(
                        instruction,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BestTimingCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌅", fontSize = 20.sp)
                Text(
                    "Best: First thing in the morning",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "• After using the bathroom\n• Before eating or drinking\n• Before exercising\n• In minimal clothing\n• At the same time each measurement day",
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                "Waist measurements can vary by 2-4 cm throughout the day due to food, water, and bloating.",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun DosAndDontsCard() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50).copy(alpha = 0.06f)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "✅ DO",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                DoItem("Measure at same time")
                DoItem("Use same tape measure")
                DoItem("Stand relaxed & straight")
                DoItem("Take multiple readings")
                DoItem("Measure on bare skin")
                DoItem("Keep tape level")
                DoItem("Breathe normally")
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF44336).copy(alpha = 0.06f)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "❌ DON'T",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
                DontItem("Suck in your stomach")
                DontItem("Hold your breath")
                DontItem("Measure over thick clothes")
                DontItem("Pull tape too tight")
                DontItem("Measure after big meal")
                DontItem("Let tape twist or tilt")
                DontItem("Rush the measurement")
            }
        }
    }
}

@Composable
private fun DoItem(text: String) {
    Text(
        "• $text",
        style = MaterialTheme.typography.bodySmall,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}

@Composable
private fun DontItem(text: String) {
    Text(
        "• $text",
        style = MaterialTheme.typography.bodySmall,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}

@Composable
private fun CommonMistakesList() {
    val mistakes = listOf(
        Pair("Measuring at the wrong waist point", "The waist is NOT at your belt line. It's the narrowest point of your torso, usually above the belly button. Measuring at the belt line gives a larger reading."),
        Pair("Uneven tape placement", "If the tape is higher in the back than the front (or vice versa), your reading will be inaccurate. Always check in a mirror or ask someone to help."),
        Pair("Pulling the tape too tight", "Compressing the skin gives a falsely low reading. The tape should touch the skin all around without indenting it."),
        Pair("Measuring after eating", "Meals can temporarily increase waist circumference by 2-4 cm. Always measure before eating."),
        Pair("Tensing muscles while measuring", "Flexing your abs or glutes during measurement gives inaccurate results. Stay completely relaxed."),
        Pair("Using a stretched-out tape", "Old fabric tapes can stretch over time. Replace your tape annually or use a fiberglass tape.")
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        mistakes.forEachIndexed { index, (title, description) ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⚠️", fontSize = 12.sp)
                            Text(
                                title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Text(
                            description,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 6.dp, start = 22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsistencyTipsCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tips = listOf(
                Pair("📅", "Measure on the same day each week (e.g., every Monday morning)"),
                Pair("⏰", "Always measure at the same time of day"),
                Pair("📏", "Use the same tape measure every time"),
                Pair("🪞", "Stand in the same spot (use a bathroom mirror for reference)"),
                Pair("📝", "Record immediately — don't rely on memory"),
                Pair("🔄", "Take 2-3 readings and use the average"),
                Pair("📊", "Track in this app for automatic trend analysis"),
                Pair("📸", "Optional: take progress photos monthly from the same angle")
            )

            tips.forEach { (emoji, tip) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(emoji, fontSize = 14.sp)
                    Text(
                        tip,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// REUSABLE COMPONENTS
// ═══════════════════════════════════════════════════════════

@Composable
private fun ExpandableEducationalSection(
    icon: ImageVector,
    emoji: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    initialExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpanded) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 2.dp else 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 20.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }

                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = accentColor
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun InfoParagraph(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        lineHeight = 20.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    )
}

@Composable
private fun SectionSubheading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun FormulaCard(formula: String, example: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                formula,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                example,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun KeyPointCard(emoji: String, title: String, text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 16.sp)
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun BulletList(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (title, description) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicalDisclaimerCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.MedicalServices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Medical Disclaimer",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    "The information provided here is for educational purposes only and is not intended as medical advice. It should not be used for diagnosing or treating any health condition. Always consult a qualified healthcare provider for personal medical guidance. Statistics and research findings are simplified summaries and may not reflect individual circumstances.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}
