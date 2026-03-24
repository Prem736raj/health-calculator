package com.health.calculator.bmi.tracker.ui.screens.metabolicsyndrome

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
fun MetabolicSyndromeEducationScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Learn About Metabolic Syndrome",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Evidence-based information to help you understand and manage your health",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Section 1: What is Metabolic Syndrome
        EducationSection(
            icon = "🫀",
            title = "What is Metabolic Syndrome?",
            accentColor = HealthRed
        ) {
            WhatIsMetabolicSyndromeContent()
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 2: The 5 Risk Factors
        EducationSection(
            icon = "🔬",
            title = "The 5 Risk Factors Explained",
            accentColor = HealthOrange
        ) {
            FiveRiskFactorsContent()
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 3: Who is at Risk
        EducationSection(
            icon = "👥",
            title = "Who is at Risk?",
            accentColor = HealthYellow
        ) {
            WhoIsAtRiskContent()
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 4: Prevention and Treatment
        EducationSection(
            icon = "💪",
            title = "Prevention and Treatment",
            accentColor = HealthGreen
        ) {
            PreventionTreatmentContent()
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Section 5: Understanding Blood Work
        EducationSection(
            icon = "📋",
            title = "Understanding Your Blood Work",
            accentColor = HealthBlue
        ) {
            BloodWorkGuideContent()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Medical Disclaimer
        MedicalDisclaimerCard()

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EducationSection(
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
            // Header
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
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = accentColor
                )
            }

            // Expandable content
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

// ─── Section 1: What is Metabolic Syndrome ───────────────────

@Composable
private fun WhatIsMetabolicSyndromeContent() {
    Column {
        InfoParagraph(
            text = "Metabolic Syndrome is a cluster of interconnected metabolic abnormalities that significantly increase your risk of developing heart disease, stroke, and type 2 diabetes. It's not a single disease, but rather a group of risk factors that occur together."
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatHighlightCard(
            emoji = "🌍",
            stat = "1 in 4",
            description = "adults worldwide are affected by metabolic syndrome, making it one of the most common health conditions globally."
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoParagraph(
            text = "Also known by other names:"
        )

        Spacer(modifier = Modifier.height(6.dp))

        AlsoKnownAsChips(
            names = listOf(
                "Syndrome X",
                "Insulin Resistance Syndrome",
                "Dysmetabolic Syndrome",
                "Reaven's Syndrome"
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        KeyPointCard(
            title = "The Core Problem: Insulin Resistance",
            content = "At the heart of metabolic syndrome is insulin resistance — a condition where your cells don't respond properly to insulin. This causes your body to produce more insulin, leading to a cascade of metabolic problems including high blood sugar, abnormal fat storage, and inflammation."
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoParagraph(
            text = "The diagnosis is made when at least 3 of the 5 risk factors are present. The more criteria you meet, the higher your health risk. However, even having 1-2 factors should prompt lifestyle changes."
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatRow(
            stats = listOf(
                StatItem("2x", "Heart disease risk"),
                StatItem("5x", "Diabetes risk"),
                StatItem("2-4x", "Stroke risk")
            )
        )
    }
}

// ─── Section 2: The 5 Risk Factors ───────────────────────────

@Composable
private fun FiveRiskFactorsContent() {
    Column {
        InfoParagraph(
            text = "Each of the 5 criteria targets a different aspect of metabolic health. While each is a risk factor on its own, together they create a synergistic effect — the combined risk is greater than the sum of individual risks."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Factor 1: Central Obesity
        RiskFactorDetailCard(
            number = "1",
            name = "Central Obesity (Large Waistline)",
            icon = "📏",
            threshold = "Men: > 102 cm (40 in) | Women: > 88 cm (35 in)",
            color = HealthRed,
            explanation = "Excess fat around the abdomen (visceral fat) is metabolically active tissue that releases inflammatory chemicals and hormones. Unlike fat on hips or thighs, belly fat directly contributes to insulin resistance and cardiovascular disease.",
            whyItMatters = "Visceral fat wraps around your internal organs and releases fatty acids directly into the liver via the portal vein, disrupting normal metabolism. It also produces inflammatory cytokines that damage blood vessels."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Factor 2: High Triglycerides
        RiskFactorDetailCard(
            number = "2",
            name = "Elevated Triglycerides",
            icon = "🩸",
            threshold = "≥ 150 mg/dL (1.7 mmol/L)",
            color = HealthOrange,
            explanation = "Triglycerides are the most common type of fat in your body. When elevated, they contribute to atherosclerosis (hardening and narrowing of arteries). High triglycerides often accompany low HDL cholesterol — a dangerous combination.",
            whyItMatters = "Insulin resistance causes the liver to produce more triglyceride-rich lipoproteins. These particles are small and dense, making them especially effective at penetrating and damaging artery walls."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Factor 3: Low HDL
        RiskFactorDetailCard(
            number = "3",
            name = "Reduced HDL Cholesterol",
            icon = "💛",
            threshold = "Men: < 40 mg/dL | Women: < 50 mg/dL",
            color = HealthYellow,
            explanation = "HDL (high-density lipoprotein) is your \"good\" cholesterol. It acts like a cleanup crew, removing excess cholesterol from your bloodstream and artery walls and transporting it back to the liver for disposal. Low HDL means less protection.",
            whyItMatters = "Without adequate HDL, cholesterol accumulates in your arteries, forming plaques that can rupture and cause heart attacks or strokes. HDL also has anti-inflammatory and antioxidant properties that protect blood vessels."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Factor 4: High Blood Pressure
        RiskFactorDetailCard(
            number = "4",
            name = "Elevated Blood Pressure",
            icon = "❤️",
            threshold = "Systolic ≥ 130 OR Diastolic ≥ 85 mmHg",
            color = HealthRed,
            explanation = "Blood pressure is the force of blood pushing against artery walls. When consistently elevated, it damages the delicate lining of blood vessels, accelerates atherosclerosis, and forces the heart to work harder.",
            whyItMatters = "Insulin resistance increases sodium retention by the kidneys and stimulates the sympathetic nervous system, both of which raise blood pressure. The damaged artery walls become sites for cholesterol plaque buildup."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Factor 5: High Fasting Glucose
        RiskFactorDetailCard(
            number = "5",
            name = "Elevated Fasting Glucose",
            icon = "🍯",
            threshold = "≥ 100 mg/dL (5.6 mmol/L)",
            color = HealthOrange,
            explanation = "Fasting blood glucose measures your blood sugar after 8+ hours without food. Elevated levels indicate your body is struggling to regulate blood sugar properly — an early sign of insulin resistance and potential progression to type 2 diabetes.",
            whyItMatters = "Chronic high blood sugar damages blood vessels, nerves, and organs over time. Even \"pre-diabetic\" levels (100-125 mg/dL) significantly increase cardiovascular risk and indicate the metabolic machinery is already impaired."
        )

        Spacer(modifier = Modifier.height(14.dp))

        KeyPointCard(
            title = "How They're Interconnected",
            content = "These 5 factors don't exist in isolation — they share a common root in insulin resistance. When cells resist insulin:\n\n• The pancreas produces more insulin → promotes fat storage\n• The liver produces more triglycerides → raises blood fats\n• Kidneys retain more sodium → raises blood pressure\n• Cells can't absorb glucose efficiently → raises blood sugar\n• Fat accumulates centrally → worsens insulin resistance\n\nThis creates a vicious cycle where each factor worsens the others."
        )
    }
}

// ─── Section 3: Who is at Risk ───────────────────────────────

@Composable
private fun WhoIsAtRiskContent() {
    Column {
        InfoParagraph(
            text = "While anyone can develop metabolic syndrome, certain factors significantly increase your risk. Understanding these can help you take preventive action."
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Non-Modifiable Risk Factors",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = HealthRed,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "These factors you cannot change, but knowing them helps you stay vigilant:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RiskFactorItem(
            emoji = "🎂",
            title = "Age",
            description = "Risk increases significantly after age 40, though it can occur at any age. Over 40% of adults aged 60+ have metabolic syndrome."
        )
        RiskFactorItem(
            emoji = "👨👩👧",
            title = "Family History",
            description = "A family history of diabetes, heart disease, or metabolic syndrome increases your risk. Genetics influence how your body handles insulin and stores fat."
        )
        RiskFactorItem(
            emoji = "🌍",
            title = "Ethnicity",
            description = "South Asian, Hispanic, African American, and Native American populations have higher rates. Different ethnic groups may need lower waist cutoffs."
        )
        RiskFactorItem(
            emoji = "♀️",
            title = "Gender-Specific Risks",
            description = "Post-menopausal women face increased risk due to hormonal changes. Women with PCOS (Polycystic Ovary Syndrome) are at 2-3x higher risk."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Modifiable Risk Factors",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = HealthGreen,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "These factors you can change — and they make the biggest difference:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RiskFactorItem(
            emoji = "⚖️",
            title = "Obesity (especially central obesity)",
            description = "The strongest modifiable risk factor. Excess abdominal fat drives insulin resistance. Even modest weight loss (5-10%) significantly reduces risk."
        )
        RiskFactorItem(
            emoji = "🛋️",
            title = "Sedentary Lifestyle",
            description = "Physical inactivity is independently linked to all 5 criteria. Sitting for prolonged periods impairs glucose metabolism even in otherwise active people."
        )
        RiskFactorItem(
            emoji = "🍔",
            title = "Unhealthy Diet",
            description = "Diets high in refined carbohydrates, added sugars, processed foods, and saturated fats directly contribute. The Western diet is strongly associated with metabolic syndrome."
        )
        RiskFactorItem(
            emoji = "😴",
            title = "Poor Sleep & Sleep Apnea",
            description = "Sleeping less than 6 hours or having obstructive sleep apnea significantly increases risk. Sleep deprivation disrupts hormones that regulate hunger and blood sugar."
        )
        RiskFactorItem(
            emoji = "😰",
            title = "Chronic Stress",
            description = "Prolonged stress raises cortisol, which promotes abdominal fat storage, raises blood sugar, and increases blood pressure."
        )
        RiskFactorItem(
            emoji = "🚬",
            title = "Smoking",
            description = "Smoking worsens insulin resistance, raises triglycerides, lowers HDL, and increases central fat distribution. Quitting can rapidly improve metabolic markers."
        )
        RiskFactorItem(
            emoji = "🍺",
            title = "Excessive Alcohol",
            description = "Heavy drinking raises triglycerides, blood pressure, and contributes to weight gain. Moderate consumption may have neutral or slightly positive effects."
        )

        Spacer(modifier = Modifier.height(14.dp))

        KeyPointCard(
            title = "Conditions Linked to Higher Risk",
            content = "• Polycystic Ovary Syndrome (PCOS) — 2-3x higher risk\n• Non-alcoholic Fatty Liver Disease (NAFLD)\n• History of gestational diabetes\n• Hypothyroidism\n• Cushing's syndrome\n• Certain medications (antipsychotics, steroids, some HIV medications)"
        )
    }
}

// ─── Section 4: Prevention and Treatment ─────────────────────

@Composable
private fun PreventionTreatmentContent() {
    Column {
        InfoParagraph(
            text = "The encouraging news is that metabolic syndrome is largely preventable and often reversible. Lifestyle modifications are the first-line treatment and can dramatically improve all 5 risk factors simultaneously."
        )

        Spacer(modifier = Modifier.height(14.dp))

        StatHighlightCard(
            emoji = "💡",
            stat = "5-10%",
            description = "weight loss can improve ALL metabolic syndrome factors — reducing waist, lowering triglycerides, raising HDL, reducing blood pressure, and improving blood sugar."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Weight Loss
        TreatmentCard(
            icon = "⚖️",
            title = "Weight Management",
            color = HealthGreen,
            points = listOf(
                "Aim for gradual, sustainable weight loss of 0.5-1 kg per week",
                "Even 5-7% of body weight loss significantly improves all markers",
                "Focus on losing visceral (belly) fat specifically through exercise and diet",
                "Avoid crash diets — they worsen metabolic health long-term",
                "Set realistic goals and celebrate small victories"
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Exercise
        TreatmentCard(
            icon = "🏃",
            title = "Physical Activity",
            color = HealthBlue,
            points = listOf(
                "Aim for at least 150 minutes of moderate aerobic activity per week",
                "Include resistance/strength training 2-3 times per week",
                "Even 10-minute walking sessions after meals significantly reduce blood sugar spikes",
                "Reduce prolonged sitting — stand or walk every 30-60 minutes",
                "Any movement is better than none — start small and build gradually",
                "Activities like brisk walking, cycling, swimming, and dancing are excellent"
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Diet
        TreatmentCard(
            icon = "🥗",
            title = "Dietary Changes",
            color = HealthTeal,
            points = listOf(
                "Mediterranean diet: Rich in olive oil, fish, nuts, vegetables, whole grains — clinically proven to reduce metabolic syndrome risk by 50%",
                "DASH diet: Designed for blood pressure, also improves other markers",
                "Reduce refined carbohydrates and added sugars significantly",
                "Increase fiber intake to 25-30g daily from vegetables, legumes, and whole grains",
                "Eat fatty fish 2-3x per week for omega-3 fatty acids",
                "Limit sodium to under 2,300 mg/day (ideally 1,500 mg)",
                "Choose whole, unprocessed foods over packaged alternatives"
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Lifestyle
        TreatmentCard(
            icon = "🧘",
            title = "Lifestyle Modifications",
            color = HealthYellow,
            points = listOf(
                "Quit smoking — improvements in metabolic markers begin within weeks",
                "Manage stress through meditation, deep breathing, yoga, or therapy",
                "Prioritize 7-9 hours of quality sleep per night",
                "Get screened for sleep apnea if you snore or feel chronically tired",
                "Limit alcohol to moderate levels (1 drink/day women, 2 men)",
                "Build a support system — friends, family, or health communities"
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Medication
        TreatmentCard(
            icon = "💊",
            title = "Medication Options",
            color = HealthOrange,
            points = listOf(
                "Medications are not a replacement for lifestyle changes — they work together",
                "For blood pressure: ACE inhibitors, ARBs, or other antihypertensives",
                "For triglycerides/cholesterol: Statins, fibrates, or fish oil supplements",
                "For blood sugar: Metformin is often the first choice for insulin resistance",
                "For weight: Some medications may be appropriate — discuss with your doctor",
                "Never self-medicate — always consult your healthcare provider"
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        KeyPointCard(
            title = "Your Action Plan",
            content = "1. Start with one change at a time — don't overwhelm yourself\n2. Walk 30 minutes daily — the single most impactful change\n3. Reduce sugary drinks and processed foods\n4. Get regular check-ups every 3-6 months\n5. Track your progress — small improvements matter!\n6. Be patient — metabolic improvements can take 3-6 months to appear in blood work"
        )
    }
}

// ─── Section 5: Understanding Blood Work ─────────────────────

@Composable
private fun BloodWorkGuideContent() {
    Column {
        InfoParagraph(
            text = "Understanding your blood test results empowers you to have meaningful conversations with your doctor and track your progress. Here's a guide to the key values related to metabolic syndrome."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Preparation
        KeyPointCard(
            title = "📌 Before Your Blood Test",
            content = "• Fast for 8-12 hours before the test (water is okay)\n• Avoid alcohol for 24 hours before\n• Inform your doctor about all medications\n• Try to get tested at the same lab for consistency\n• Morning appointments tend to give the most accurate fasting results"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Blood values guide
        Text(
            text = "Key Lab Values Explained",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        BloodValueCard(
            name = "Fasting Blood Glucose",
            normalRange = "70-99 mg/dL (3.9-5.5 mmol/L)",
            preDiabetic = "100-125 mg/dL (5.6-6.9 mmol/L)",
            diabetic = "≥ 126 mg/dL (≥ 7.0 mmol/L)",
            whatItMeasures = "The amount of sugar in your blood after fasting. Reflects how well your body manages blood sugar overnight.",
            icon = "🍯"
        )

        Spacer(modifier = Modifier.height(8.dp))

        BloodValueCard(
            name = "Triglycerides",
            normalRange = "< 150 mg/dL (< 1.7 mmol/L)",
            preDiabetic = "150-199 mg/dL (borderline high)",
            diabetic = "≥ 200 mg/dL (high) | ≥ 500 mg/dL (very high)",
            whatItMeasures = "The most common type of fat in your blood. Affected by diet, exercise, and alcohol. Can vary significantly day to day.",
            icon = "🩸"
        )

        Spacer(modifier = Modifier.height(8.dp))

        BloodValueCard(
            name = "HDL Cholesterol",
            normalRange = "Men: ≥ 40 mg/dL | Women: ≥ 50 mg/dL",
            preDiabetic = "Optimal: ≥ 60 mg/dL (protective)",
            diabetic = "Men: < 40 mg/dL | Women: < 50 mg/dL (low)",
            whatItMeasures = "\"Good\" cholesterol that removes harmful cholesterol from arteries. Higher is better — one of the few values where MORE is good.",
            icon = "💛"
        )

        Spacer(modifier = Modifier.height(8.dp))

        BloodValueCard(
            name = "HbA1c (Hemoglobin A1c)",
            normalRange = "< 5.7%",
            preDiabetic = "5.7-6.4% (pre-diabetes)",
            diabetic = "≥ 6.5% (diabetes)",
            whatItMeasures = "Reflects your average blood sugar over the past 2-3 months. More reliable than a single fasting glucose test. Ask your doctor about this test.",
            icon = "📊"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // When to retest
        Text(
            text = "When to Retest",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RetestScheduleCard(
            items = listOf(
                RetestItem(
                    condition = "All values normal",
                    frequency = "Every 1-2 years",
                    icon = "✅"
                ),
                RetestItem(
                    condition = "1-2 criteria abnormal",
                    frequency = "Every 3-6 months",
                    icon = "⚠️"
                ),
                RetestItem(
                    condition = "Metabolic syndrome (3+ criteria)",
                    frequency = "Every 3 months initially",
                    icon = "🔴"
                ),
                RetestItem(
                    condition = "After starting new medication",
                    frequency = "After 6-8 weeks",
                    icon = "💊"
                ),
                RetestItem(
                    condition = "After significant lifestyle change",
                    frequency = "After 3 months",
                    icon = "🏃"
                )
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        KeyPointCard(
            title = "Tips for Talking to Your Doctor",
            content = "• Bring your tracked results from this app\n• Ask about each abnormal value specifically\n• Ask: \"What can I do about this?\"\n• Ask about target values for YOUR situation\n• Don't be afraid to ask for explanations in simple terms\n• Request copies of all your lab results\n• Ask when you should retest"
        )
    }
}

// ─── Reusable Content Components ─────────────────────────────

@Composable
private fun InfoParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp
    )
}

@Composable
private fun StatHighlightCard(emoji: String, stat: String, description: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stat,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
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
}

@Composable
private fun AlsoKnownAsChips(names: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        names.forEach { name ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun KeyPointCard(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(6.dp))
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
private fun StatRow(stats: List<StatItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        stats.forEach { stat ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = HealthRed
                )
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private data class StatItem(val value: String, val label: String)

@Composable
private fun RiskFactorDetailCard(
    number: String,
    name: String,
    icon: String,
    threshold: String,
    color: Color,
    explanation: String,
    whyItMatters: String
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
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = number,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = color
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "$icon $name",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = threshold,
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Why it matters:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = whyItMatters,
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
private fun RiskFactorItem(emoji: String, title: String, description: String) {
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
private fun TreatmentCard(
    icon: String,
    title: String,
    color: Color,
    points: List<String>
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
                Text(icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            points.forEach { point ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = point,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BloodValueCard(
    name: String,
    normalRange: String,
    preDiabetic: String,
    diabetic: String,
    whatItMeasures: String,
    icon: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = whatItMeasures,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Value ranges
            ValueRangeRow(label = "Normal", value = normalRange, color = HealthGreen)
            ValueRangeRow(label = "Borderline", value = preDiabetic, color = HealthOrange)
            ValueRangeRow(label = "Abnormal", value = diabetic, color = HealthRed)
        }
    }
}

@Composable
private fun ValueRangeRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class RetestItem(
    val condition: String,
    val frequency: String,
    val icon: String
)

@Composable
private fun RetestScheduleCard(items: List<RetestItem>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.icon, fontSize = 16.sp, modifier = Modifier.width(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.condition,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.frequency,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                if (item != items.last()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(vertical = 2.dp)
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
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.MedicalServices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Medical Disclaimer",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "This educational content is for informational purposes only and does not constitute medical advice, diagnosis, or treatment. The information provided should not be used as a substitute for professional medical consultation. Always consult with a qualified healthcare provider regarding any medical condition or treatment decisions. Individual health needs vary — what's appropriate for one person may not be suitable for another.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
