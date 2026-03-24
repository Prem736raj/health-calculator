package com.health.calculator.bmi.tracker.ui.screens.heartrate

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.health.calculator.bmi.tracker.util.HeartRateZoneCalculator
import com.health.calculator.bmi.tracker.util.HeartRateZoneResult
import com.health.calculator.bmi.tracker.ui.components.HeartRateFormula
import java.io.File
import java.io.FileOutputStream

// ============================================================
// FORMULA COMPARISON CARD
// ============================================================

@Composable
fun FormulaComparisonCard(
    age: Int,
    selectedFormula: HeartRateFormula,
    gender: String?,
    customMaxHR: Int?,
    modifier: Modifier = Modifier
) {
    val allResults = remember(age, gender, customMaxHR) {
        HeartRateZoneCalculator.calculateAllFormulaMHR(age, gender, customMaxHR)
    }

    val bestForUser = remember(age, gender) {
        when {
            gender?.lowercase() == "female" -> HeartRateFormula.GULATI
            age >= 40 -> HeartRateFormula.TANAKA
            else -> HeartRateFormula.STANDARD
        }
    }

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📐", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Formula Comparison",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MHR results from all formulas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Always show selected formula result
            val selectedMHR = allResults[selectedFormula] ?: (220 - age)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Selected: ${selectedFormula.label}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "$selectedMHR BPM",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expanded comparison
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(spring(dampingRatio = 0.8f, stiffness = 300f)) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    allResults.entries
                        .filter { it.key != HeartRateFormula.CUSTOM || customMaxHR != null }
                        .sortedByDescending { it.value }
                        .forEach { (formula, mhr) ->
                            val isSelected = formula == selectedFormula
                            val isBest = formula == bestForUser
                            val maxMHR = allResults.values.maxOrNull() ?: mhr

                            FormulaComparisonRow(
                                formula = formula,
                                mhr = mhr,
                                maxMHR = maxMHR,
                                isSelected = isSelected,
                                isBestForUser = isBest
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Recommendation note
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2196F3).copy(alpha = 0.06f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("💡", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = buildString {
                                    append("Most accurate for you: ${bestForUser.label}. ")
                                    when (bestForUser) {
                                        HeartRateFormula.GULATI -> append("Gulati was designed specifically for women and tends to be more accurate for female heart rate estimation.")
                                        HeartRateFormula.TANAKA -> append("Tanaka is more accurate for adults over 40, as the standard 220-age formula tends to overestimate MHR with age.")
                                        HeartRateFormula.KARVONEN -> append("Karvonen provides the most personalized zones by using your resting heart rate.")
                                        else -> append("The standard formula is a reliable starting point for most adults under 40.")
                                    }
                                    append("\n\nNote: All formulas are estimates with ±10-12 BPM variation. A supervised stress test is the only way to determine true MHR.")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormulaComparisonRow(
    formula: HeartRateFormula,
    mhr: Int,
    maxMHR: Int,
    isSelected: Boolean,
    isBestForUser: Boolean
) {
    val fraction = if (maxMHR > 0) mhr.toFloat() / maxMHR else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "formula_bar_${formula.name}"
    )

    val barColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isBestForUser -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Formula name
        Column(modifier = Modifier.width(80.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formula.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
                if (isBestForUser && !isSelected) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("⭐", fontSize = 8.sp)
                }
            }
            formula.badge?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        // Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .padding(horizontal = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor.copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor.copy(alpha = if (isSelected) 0.7f else 0.3f))
            )
        }

        // MHR value
        Text(
            text = "$mhr",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = barColor,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}

// ============================================================
// EDGE CASE WARNINGS
// ============================================================

@Composable
fun HeartRateEdgeCaseWarnings(
    age: Int,
    restingHR: Int?,
    maxHR: Int,
    modifier: Modifier = Modifier
) {
    val warnings = remember(age, restingHR, maxHR) {
        buildList {
            // Very young
            if (age < 15) {
                add(
                    EdgeWarning(
                        emoji = "👶",
                        message = "For children under 15, the 220-age formula may underestimate max heart rate. " +
                                "Children can safely reach higher heart rates than the formula suggests. " +
                                "Consult a pediatrician for age-appropriate exercise guidance.",
                        severity = WarningSeverity.INFO
                    )
                )
            }

            // Very old
            if (age > 75) {
                add(
                    EdgeWarning(
                        emoji = "👴",
                        message = "For adults over 75, max HR formulas may overestimate your true maximum. " +
                                "The Tanaka formula (208 - 0.7×age) is generally more accurate for older adults. " +
                                "Always exercise within your comfort zone and consult your doctor.",
                        severity = WarningSeverity.WARNING
                    )
                )
            }

            // Very high resting HR
            if (restingHR != null && restingHR > 100) {
                add(
                    EdgeWarning(
                        emoji = "⚠️",
                        message = "A resting heart rate above 100 BPM (tachycardia) may indicate a health concern " +
                                "such as dehydration, stress, anemia, thyroid issues, or a cardiac condition. " +
                                "Consider consulting a doctor, especially if this is consistently elevated.",
                        severity = WarningSeverity.DANGER
                    )
                )
            }

            // Athletic resting HR
            if (restingHR != null && restingHR < 40) {
                add(
                    EdgeWarning(
                        emoji = "🏆",
                        message = "A resting heart rate below 40 BPM is exceptionally low. If you're a trained athlete, " +
                                "this can be normal (athletic bradycardia). If you're NOT a regular athlete and experience " +
                                "dizziness, fatigue, or fainting, consult a doctor as it may indicate bradycardia.",
                        severity = WarningSeverity.INFO
                    )
                )
            }

            // MHR seems very low
            if (maxHR < 140) {
                add(
                    EdgeWarning(
                        emoji = "📉",
                        message = "Your estimated max HR of $maxHR BPM is quite low. This may affect zone accuracy. " +
                                "Consider using the Tanaka formula or entering a custom max HR if you know it from testing.",
                        severity = WarningSeverity.INFO
                    )
                )
            }

            // Resting HR close to calculated zone boundaries
            if (restingHR != null && restingHR > maxHR * 0.5) {
                add(
                    EdgeWarning(
                        emoji = "📊",
                        message = "Your resting HR is above 50% of your estimated max HR. Zone 1 may be very narrow. " +
                                "This typically improves with regular cardiovascular exercise.",
                        severity = WarningSeverity.INFO
                    )
                )
            }
        }
    }

    if (warnings.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            warnings.forEach { warning ->
                EdgeWarningCard(warning = warning)
            }
        }
    }
}

private data class EdgeWarning(
    val emoji: String,
    val message: String,
    val severity: WarningSeverity
)

private enum class WarningSeverity { INFO, WARNING, DANGER }

@Composable
private fun EdgeWarningCard(warning: EdgeWarning) {
    val (containerColor, borderColor) = when (warning.severity) {
        WarningSeverity.INFO -> Color(0xFF2196F3).copy(alpha = 0.06f) to Color(0xFF2196F3).copy(alpha = 0.2f)
        WarningSeverity.WARNING -> Color(0xFFFF9800).copy(alpha = 0.06f) to Color(0xFFFF9800).copy(alpha = 0.2f)
        WarningSeverity.DANGER -> Color(0xFFF44336).copy(alpha = 0.06f) to Color(0xFFF44336).copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = warning.emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = warning.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                lineHeight = 17.sp
            )
        }
    }
}

// ============================================================
// RESTING HR TREND TRACKER
// ============================================================

@Composable
fun RestingHRTrendCard(
    restingHRHistory: List<Pair<String, Int>>, // date to resting HR
    modifier: Modifier = Modifier
) {
    if (restingHRHistory.size < 2) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("💤", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Track Your Resting HR Over Time",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Calculate your zones regularly to see how your resting heart rate improves with training. " +
                            "A decreasing resting HR is one of the best signs of improving fitness!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        return
    }

    val minHR = restingHRHistory.minOf { it.second }
    val maxHR = restingHRHistory.maxOf { it.second }
    val avgHR = restingHRHistory.map { it.second }.average().toInt()
    val latestHR = restingHRHistory.last().second
    val firstHR = restingHRHistory.first().second
    val change = latestHR - firstHR
    val isImproving = change < 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📈", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Resting HR Trend",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mini graph
            RestingHRMiniGraph(
                data = restingHRHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrendStat(emoji = "💤", label = "Latest", value = "$latestHR BPM")
                TrendStat(emoji = "📊", label = "Average", value = "$avgHR BPM")
                TrendStat(emoji = "⬇️", label = "Lowest", value = "$minHR BPM")
                TrendStat(
                    emoji = if (isImproving) "✅" else "📈",
                    label = "Change",
                    value = "${if (change > 0) "+" else ""}$change BPM"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trend message
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isImproving) Color(0xFF4CAF50).copy(alpha = 0.08f)
                else Color(0xFFFF9800).copy(alpha = 0.08f)
            ) {
                Text(
                    text = if (isImproving)
                        "👍 Your resting HR has decreased by ${-change} BPM — your fitness is improving!"
                    else if (change == 0) "📊 Your resting HR is stable."
                    else "📈 Your resting HR has increased by $change BPM. This could be due to stress, illness, or overtraining.",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isImproving) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.padding(10.dp),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun RestingHRMiniGraph(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val lineColor = Color(0xFFE53935)
    val minVal = (data.minOf { it.second } - 5).coerceAtLeast(30)
    val maxVal = (data.maxOf { it.second } + 5).coerceAtMost(120)
    val range = (maxVal - minVal).toFloat().coerceAtLeast(1f)

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        // Grid lines
        for (i in 0..4) {
            val y = size.height * i / 4f
            drawLine(
                color = lineColor.copy(alpha = 0.06f),
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Line path
        val points = data.mapIndexed { index, (_, hr) ->
            val x = index * stepX
            val y = size.height - ((hr - minVal) / range * size.height)
            androidx.compose.ui.geometry.Offset(x, y)
        }

        // Draw fill area
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(points.first().x, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height)
            close()
        }
        drawPath(
            path = path,
            color = lineColor.copy(alpha = 0.08f)
        )

        // Draw line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Draw dots
        points.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun TrendStat(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 14.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            fontSize = 9.sp
        )
    }
}

// ============================================================
// SHARE AS IMAGE
// ============================================================

fun shareHeartRateZonesAsImage(
    context: Context,
    result: HeartRateZoneResult
) {
    val width = 1080
    val height = 1400
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint().apply { color = 0xFFF8F9FA.toInt() }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val titlePaint = Paint().apply {
        color = 0xFF212121.toInt()
        textSize = 52f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val subtitlePaint = Paint().apply {
        color = 0xFF757575.toInt()
        textSize = 32f
        isAntiAlias = true
    }

    val mhrPaint = Paint().apply {
        color = 0xFFE53935.toInt()
        textSize = 80f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    val mhrLabelPaint = Paint().apply {
        color = 0xFFE53935.toInt()
        textSize = 28f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    // Title
    canvas.drawText("❤️ My Heart Rate Zones", 60f, 80f, titlePaint)
    canvas.drawText("Age: ${result.age} | ${result.formulaUsed.label} Formula", 60f, 130f, subtitlePaint)

    // MHR
    canvas.drawText("${result.maxHeartRate}", width / 2f, 250f, mhrPaint)
    canvas.drawText("Max Heart Rate (BPM)", width / 2f, 290f, mhrLabelPaint)

    if (result.restingHeartRate != null) {
        canvas.drawText("Resting: ${result.restingHeartRate} BPM | Reserve: ${result.heartRateReserve} BPM",
            width / 2f, 330f, subtitlePaint.apply { textAlign = Paint.Align.CENTER })
    }

    // Zone bars
    val zoneColors = listOf(
        0xFF90CAF9.toInt(), 0xFF42A5F5.toInt(), 0xFF66BB6A.toInt(),
        0xFFFFA726.toInt(), 0xFFEF5350.toInt()
    )

    var yPos = 400f
    val barHeight = 120f
    val barMargin = 20f
    val barLeft = 60f
    val barRight = width - 60f

    result.zones.forEachIndexed { index, zone ->
        // Zone bar background
        val barPaint = Paint().apply {
            color = zoneColors.getOrElse(index) { 0xFF9E9E9E.toInt() }
            isAntiAlias = true
        }
        val barPaintLight = Paint().apply {
            color = (zoneColors.getOrElse(index) { 0xFF9E9E9E.toInt() } and 0x00FFFFFF) or 0x20000000
            isAntiAlias = true
        }

        canvas.drawRoundRect(RectF(barLeft, yPos, barRight, yPos + barHeight), 20f, 20f, barPaintLight)

        val fraction = zone.bpmHigh.toFloat() / result.maxHeartRate
        val filledWidth = barLeft + (barRight - barLeft) * fraction
        canvas.drawRoundRect(RectF(barLeft, yPos, filledWidth, yPos + barHeight), 20f, 20f, barPaint)

        // Zone text
        val zoneTextPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val zoneDetailPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 24f
            isAntiAlias = true
        }

        canvas.drawText("Zone ${zone.zoneNumber} — ${zone.zoneName}", barLeft + 20f, yPos + 45f, zoneTextPaint)
        canvas.drawText("${zone.bpmLow}-${zone.bpmHigh} BPM (${zone.percentLow}-${zone.percentHigh}%)",
            barLeft + 20f, yPos + 80f, zoneDetailPaint)
        canvas.drawText(zone.purpose.take(50) + if (zone.purpose.length > 50) "..." else "",
            barLeft + 20f, yPos + 105f, zoneDetailPaint.apply { textSize = 20f })

        yPos += barHeight + barMargin
    }

    // Footer
    yPos += 20f
    val footerPaint = Paint().apply {
        color = 0xFF9E9E9E.toInt()
        textSize = 24f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("📱 Generated by Health Calculator: BMI Tracker", width / 2f, yPos, footerPaint)

    // Save and share
    try {
        val file = File(context.cacheDir, "heart_rate_zones.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "My Heart Rate Zones")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Heart Rate Zones"))
    } catch (e: Exception) {
        // Fallback to text share
        shareHeartRateZonesText(context, result)
    }
}

fun shareHeartRateZonesText(context: Context, result: HeartRateZoneResult) {
    val text = buildString {
        appendLine("❤️ My Heart Rate Zones (Age: ${result.age}, MHR: ${result.maxHeartRate} BPM)")
        result.restingHeartRate?.let {
            appendLine("Resting HR: $it BPM | HR Reserve: ${result.heartRateReserve} BPM")
        }
        appendLine("Formula: ${result.formulaUsed.label}")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
        result.zones.forEach { zone ->
            appendLine("${zone.icon} Zone ${zone.zoneNumber} — ${zone.zoneName}: ${zone.bpmLow}-${zone.bpmHigh} BPM (${zone.percentLow}-${zone.percentHigh}%)")
        }
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("📱 Calculated with Health Calculator: BMI Tracker")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "My Heart Rate Zones")
    }
    context.startActivity(Intent.createChooser(intent, "Share Heart Rate Zones"))
}
