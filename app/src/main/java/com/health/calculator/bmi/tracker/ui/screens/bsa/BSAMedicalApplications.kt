package com.health.calculator.bmi.tracker.ui.screens.bsa

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.ui.theme.*

@Composable
fun BSAMedicalApplicationsSection(
    bsa: Float,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Toggle header
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    expanded = !expanded
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Medical Applications of BSA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "How BSA is used in medical practice",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(400)) + fadeIn(tween(400)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(200))
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                // === Critical Disclaimer Banner ===
                CriticalDisclaimerBanner()

                Spacer(modifier = Modifier.height(14.dp))

                // === 1. Drug Dosage ===
                DrugDosageSection(bsa = bsa)

                Spacer(modifier = Modifier.height(12.dp))

                // === 2. Burn Assessment ===
                BurnAssessmentSection(bsa = bsa)

                Spacer(modifier = Modifier.height(12.dp))

                // === 3. Renal Function ===
                RenalFunctionSection(bsa = bsa)

                Spacer(modifier = Modifier.height(12.dp))

                // === 4. Cardiac Index ===
                CardiacIndexSection(bsa = bsa)

                Spacer(modifier = Modifier.height(14.dp))

                // === Final Disclaimer ===
                FinalDisclaimerCard()
            }
        }
    }
}

@Composable
private fun CriticalDisclaimerBanner() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "⚠️ Educational Information Only",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "The medical applications described below are for educational purposes ONLY. This information is NOT intended for self-medication, self-diagnosis, or self-treatment. NEVER calculate your own medication doses. ALWAYS follow your doctor's instructions and prescriptions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// SECTION 1: Drug Dosage
// ═══════════════════════════════════════════

@Composable
private fun DrugDosageSection(bsa: Float) {
    var sectionExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    MedicalApplicationCard(
        number = "1",
        emoji = "💊",
        title = "Drug Dosage Calculation",
        subtitle = "How BSA helps determine medication amounts",
        accentColor = HealthRed,
        expanded = sectionExpanded,
        onToggle = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            sectionExpanded = !sectionExpanded
        }
    ) {
        Column {
            Text(
                text = "Many medications — especially chemotherapy drugs, immunosuppressants, and some antibiotics — are dosed based on Body Surface Area rather than weight alone. BSA-based dosing is considered more accurate because it better correlates with metabolic rate and organ function than weight alone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Educational Example",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = HealthRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Example calculation cards
            val exampleDoses = listOf(
                Triple("Example Drug A", "100 mg/m²", 100f),
                Triple("Example Drug B", "75 mg/m²", 75f),
                Triple("Example Drug C", "250 mg/m²", 250f)
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Your BSA: ${"%.4f".format(bsa)} m²",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Table header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Drug",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Dose Rate",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.8f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Your Dose",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.8f),
                            textAlign = TextAlign.End
                        )
                    }

                    exampleDoses.forEach { (name, rateStr, rate) ->
                        val calculatedDose = rate * bsa
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = rateStr,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${"%.1f".format(calculatedDose)} mg",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Strong disclaimer for this section
            Surface(
                color = HealthRed.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🚫", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "These are FICTIONAL examples for education only. Drug names are made up. NEVER calculate or adjust your own medication doses. Your doctor considers many factors beyond BSA when prescribing medication.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HealthRed,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Why BSA instead of weight?",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            val reasons = listOf(
                "BSA correlates better with blood volume and metabolic rate",
                "Helps ensure proper drug concentration in the blood",
                "More accurate for obese or underweight patients",
                "Reduces risk of under-dosing or toxic over-dosing",
                "Standard practice for chemotherapy since the 1950s"
            )

            reasons.forEach { reason ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(HealthRed.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// SECTION 2: Burn Assessment
// ═══════════════════════════════════════════

@Composable
private fun BurnAssessmentSection(bsa: Float) {
    var sectionExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    MedicalApplicationCard(
        number = "2",
        emoji = "🔥",
        title = "Burn Assessment Reference",
        subtitle = "Rule of Nines for burn area estimation",
        accentColor = HealthOrange,
        expanded = sectionExpanded,
        onToggle = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            sectionExpanded = !sectionExpanded
        }
    ) {
        Column {
            Text(
                text = "Medical professionals use the \"Rule of Nines\" to quickly estimate the percentage of body surface area affected by burns. This helps determine treatment urgency and fluid resuscitation needs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Rule of Nines Diagram
            RuleOfNinesDiagram(bsa = bsa)

            Spacer(modifier = Modifier.height(14.dp))

            // Burn area percentages table
            Text(
                text = "Rule of Nines — Body Region Percentages (Adults)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = HealthOrange
            )

            Spacer(modifier = Modifier.height(8.dp))

            val bodyParts = listOf(
                Triple("🧑 Head & Neck", "9%", 0.09f),
                Triple("💪 Right Arm", "9%", 0.09f),
                Triple("💪 Left Arm", "9%", 0.09f),
                Triple("👕 Front Torso (Chest & Abdomen)", "18%", 0.18f),
                Triple("🔙 Back Torso", "18%", 0.18f),
                Triple("🦵 Right Leg", "18%", 0.18f),
                Triple("🦵 Left Leg", "18%", 0.18f),
                Triple("🩲 Groin / Perineum", "1%", 0.01f)
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                HealthOrange.copy(alpha = 0.08f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Body Region",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = "% of BSA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.5f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Area (m²)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.6f),
                            textAlign = TextAlign.End
                        )
                    }

                    bodyParts.forEach { (region, percentStr, fraction) ->
                        val area = bsa * fraction
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = region,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = percentStr,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.Center,
                                color = HealthOrange
                            )
                            Text(
                                text = "${"%.4f".format(area)}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(0.6f),
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                    }

                    // Total
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(0.5f),
                            textAlign = TextAlign.Center,
                            color = HealthOrange
                        )
                        Text(
                            text = "${"%.4f".format(bsa)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(0.6f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Burn severity info
            Text(
                text = "Burn Severity Classification",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            BurnSeverityItem("Minor", "< 10% BSA (adults)", HealthGreen)
            BurnSeverityItem("Moderate", "10-20% BSA or critical areas", HealthYellow)
            BurnSeverityItem("Major", "> 20% BSA, inhalation, or critical areas", HealthRed)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ℹ️ The Rule of Nines is modified for children, where the head represents a larger percentage. This reference is for adult proportions only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun RuleOfNinesDiagram(bsa: Float) {
    val orangeColor = HealthOrange
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val density = LocalDensity.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(16.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .width(140.dp)
                    .height(260.dp)
            ) {
                val w = size.width
                val h = size.height
                val strokeW = 2.dp.toPx()
                val textPaint = android.graphics.Paint().apply {
                    color = onSurface.hashCode()
                    textSize = with(density) { 10.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                val percentPaint = android.graphics.Paint().apply {
                    color = orangeColor.hashCode()
                    textSize = with(density) { 11.sp.toPx() }
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }

                // Head circle
                val headCX = w * 0.5f
                val headCY = h * 0.065f
                val headR = w * 0.12f
                drawCircle(
                    color = orangeColor.copy(alpha = 0.15f),
                    radius = headR,
                    center = Offset(headCX, headCY)
                )
                drawCircle(
                    color = orangeColor,
                    radius = headR,
                    center = Offset(headCX, headCY),
                    style = Stroke(strokeW)
                )
                drawContext.canvas.nativeCanvas.drawText("9%", headCX, headCY + 4.dp.toPx(), percentPaint)

                // Torso front
                val torsoTop = h * 0.14f
                val torsoBottom = h * 0.45f
                val torsoLeft = w * 0.3f
                val torsoRight = w * 0.7f
                drawRect(
                    color = orangeColor.copy(alpha = 0.12f),
                    topLeft = Offset(torsoLeft, torsoTop),
                    size = androidx.compose.ui.geometry.Size(torsoRight - torsoLeft, torsoBottom - torsoTop)
                )
                drawRect(
                    color = orangeColor,
                    topLeft = Offset(torsoLeft, torsoTop),
                    size = androidx.compose.ui.geometry.Size(torsoRight - torsoLeft, torsoBottom - torsoTop),
                    style = Stroke(strokeW)
                )
                drawContext.canvas.nativeCanvas.drawText("18%", w * 0.5f, h * 0.31f, percentPaint)
                drawContext.canvas.nativeCanvas.drawText("Front", w * 0.5f, h * 0.35f, textPaint)

                // Left arm
                drawRect(
                    color = orangeColor.copy(alpha = 0.1f),
                    topLeft = Offset(w * 0.08f, torsoTop),
                    size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.28f)
                )
                drawRect(
                    color = orangeColor,
                    topLeft = Offset(w * 0.08f, torsoTop),
                    size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.28f),
                    style = Stroke(strokeW)
                )
                drawContext.canvas.nativeCanvas.drawText("9%", w * 0.17f, h * 0.29f, percentPaint)

                // Right arm
                drawRect(
                    color = orangeColor.copy(alpha = 0.1f),
                    topLeft = Offset(w * 0.74f, torsoTop),
                    size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.28f)
                )
                drawRect(
                    color = orangeColor,
                    topLeft = Offset(w * 0.74f, torsoTop),
                    size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.28f),
                    style = Stroke(strokeW)
                )
                drawContext.canvas.nativeCanvas.drawText("9%", w * 0.83f, h * 0.29f, percentPaint)

                // Groin
                drawRect(
                    color = orangeColor.copy(alpha = 0.08f),
                    topLeft = Offset(w * 0.38f, torsoBottom),
                    size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.04f)
                )
                drawRect(
                    color = orangeColor,
                    topLeft = Offset(w * 0.38f, torsoBottom),
                    size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.04f),
                    style = Stroke(strokeW)
                )
                drawContext.canvas.nativeCanvas.drawText("1%", w * 0.5f, h * 0.48f, percentPaint)

                // Left leg
                val legTop = h * 0.49f
                val legBottom = h * 0.95f
                drawRect(
                    color = orangeColor.copy(alpha = 0.1f),
                    topLeft = Offset(w * 0.28f, legTop),
                    size = androidx.compose.ui.geometry.Size(w * 0.2f, legBottom - legTop)
                )
                drawRect(
                    color = orangeColor,
                    topLeft = Offset(w * 0.28f, legTop),
                    size = androidx.compose.ui.geometry.Size(w * 0.2f, legBottom - legTop),
                    style = Stroke(strokeW)
                )
                drawContext.canvas.nativeCanvas.drawText("18%", w * 0.38f, h * 0.72f, percentPaint)

                // Right leg
                drawRect(
                    color = orangeColor.copy(alpha = 0.1f),
                    topLeft = Offset(w * 0.52f, legTop),
                    size = androidx.compose.ui.geometry.Size(w * 0.2f, legBottom - legTop)
                )
                drawRect(
                    color = orangeColor,
                    topLeft = Offset(w * 0.52f, legTop),
                    size = androidx.compose.ui.geometry.Size(w * 0.2f, legBottom - legTop),
                    style = Stroke(strokeW)
                )
                drawContext.canvas.nativeCanvas.drawText("18%", w * 0.62f, h * 0.72f, percentPaint)

                // Back label (pointer)
                drawContext.canvas.nativeCanvas.drawText("Back: 18%", w * 0.5f, h * 0.99f, textPaint)
            }
        }
    }
}

@Composable
private fun BurnSeverityItem(severity: String, criteria: String, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$severity: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = criteria,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═══════════════════════════════════════════
// SECTION 3: Renal Function
// ═══════════════════════════════════════════

@Composable
private fun RenalFunctionSection(bsa: Float) {
    var sectionExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val standardBSA = 1.73f

    MedicalApplicationCard(
        number = "3",
        emoji = "🫘",
        title = "Renal Function (GFR)",
        subtitle = "How BSA relates to kidney function assessment",
        accentColor = HealthTeal,
        expanded = sectionExpanded,
        onToggle = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            sectionExpanded = !sectionExpanded
        }
    ) {
        Column {
            Text(
                text = "Glomerular Filtration Rate (GFR) measures how well your kidneys filter blood. In clinical practice, GFR is normalized to a standard BSA of 1.73 m² to allow meaningful comparisons between individuals of different sizes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Standard BSA comparison
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = HealthTeal.copy(alpha = 0.06f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Standard Reference BSA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = HealthTeal
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Standard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("1.73 m²", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = HealthTeal)
                        }
                        Text("vs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Yours", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${"%.4f".format(bsa)} m²",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val ratio = bsa / standardBSA
                    Text(
                        text = "Your BSA is ${"%.1f".format((ratio - 1f) * 100)}% ${if (ratio > 1) "above" else "below"} the standard reference",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "GFR Adjustment Formula",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Adjusted GFR = Measured GFR × (1.73 / Patient BSA)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "For your BSA: factor = ${"%.4f".format(standardBSA / bsa)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Normal GFR by Age",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            GfrRangeItem("Young adults (20-29)", "> 90 mL/min/1.73m²", HealthGreen)
            GfrRangeItem("Ages 30-39", "~85 mL/min/1.73m²", HealthGreen)
            GfrRangeItem("Ages 40-49", "~80 mL/min/1.73m²", HealthGreen)
            GfrRangeItem("Ages 50-59", "~75 mL/min/1.73m²", HealthYellow)
            GfrRangeItem("Ages 60-69", "~65 mL/min/1.73m²", HealthYellow)
            GfrRangeItem("Ages 70+", "~55 mL/min/1.73m²", HealthOrange)
            GfrRangeItem("Kidney disease", "< 60 mL/min/1.73m²", HealthRed)
        }
    }
}

@Composable
private fun GfrRangeItem(age: String, range: String, color: Color) {
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
            text = age,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = range,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

// ═══════════════════════════════════════════
// SECTION 4: Cardiac Index
// ═══════════════════════════════════════════

@Composable
private fun CardiacIndexSection(bsa: Float) {
    var sectionExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    MedicalApplicationCard(
        number = "4",
        emoji = "❤️",
        title = "Cardiac Index",
        subtitle = "Heart output normalized to body size",
        accentColor = HealthRed,
        expanded = sectionExpanded,
        onToggle = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            sectionExpanded = !sectionExpanded
        }
    ) {
        Column {
            Text(
                text = "Cardiac Index (CI) is the cardiac output (the amount of blood your heart pumps per minute) divided by your Body Surface Area. This normalization allows comparison of heart performance across people of different body sizes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Formula
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cardiac Index = Cardiac Output ÷ BSA",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "CI = CO (L/min) ÷ ${"%.4f".format(bsa)} (m²)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Example calculations
            Text(
                text = "Example: If cardiac output is 5 L/min",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            val exampleCO = 5.0f
            val ci = exampleCO / bsa

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = HealthRed.copy(alpha = 0.06f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("5.0 L/min", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("÷", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BSA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.2f".format(bsa)} m²", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("=", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${"%.1f".format(ci)} L/min/m²",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Normal ranges
            Text(
                text = "Cardiac Index Reference Ranges",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            CIRangeItem("Low (Shock)", "< 2.0 L/min/m²", HealthRed, "Indicates inadequate circulation")
            CIRangeItem("Below Normal", "2.0 - 2.4 L/min/m²", HealthOrange, "May indicate reduced function")
            CIRangeItem("Normal", "2.5 - 4.0 L/min/m²", HealthGreen, "Healthy cardiac output for body size")
            CIRangeItem("Elevated", "> 4.0 L/min/m²", HealthYellow, "May indicate hyperdynamic state")

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "ℹ️ Cardiac Index is measured using echocardiography or cardiac catheterization, not calculable from BSA alone. Your doctor uses CI to assess heart performance relative to your body size.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun CIRangeItem(label: String, range: String, color: Color, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row {
                Text(
                    text = "$label: ",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = range,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════
// Shared Components
// ═══════════════════════════════════════════

@Composable
private fun MedicalApplicationCard(
    number: String,
    emoji: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.04f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(emoji, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = accentColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = number,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(350)) + fadeIn(tween(350)),
                exit = shrinkVertically(tween(250)) + fadeOut(tween(200))
            ) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                ) {
                    HorizontalDivider(
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
private fun FinalDisclaimerCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
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
                    text = "Important Reminder",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "All medical applications shown above are for educational understanding only. BSA calculations in this app should not be used for clinical decision-making, drug dosing, or medical treatment. Always consult with qualified healthcare professionals for any medical decisions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
