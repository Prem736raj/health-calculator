package com.health.calculator.bmi.tracker.ui.screens.calorie

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieHistoryScreen(
    logs: List<DailyFoodLog>,
    stats: CalorieHistoryStats,
    weeklySummaries: List<WeeklyCalorieSummary>,
    onNavigateBack: () -> Unit,
    onDayTapped: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var selectedTrendDays by remember { mutableIntStateOf(30) }
    var calendarYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    val analytics = remember { com.health.calculator.bmi.tracker.domain.usecase.CalorieHistoryAnalyticsUseCase() }

    val calendarData by remember(logs, calendarYear, calendarMonth) {
        derivedStateOf { analytics.getCalendarData(logs, calendarYear, calendarMonth) }
    }
    val trendData by remember(logs, selectedTrendDays) {
        derivedStateOf { analytics.getFilteredTrendData(logs, selectedTrendDays) }
    }
    val macroTrend by remember(logs, selectedTrendDays) {
        derivedStateOf { analytics.getMacroTrend(logs, selectedTrendDays) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calorie History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (logs.isEmpty() || logs.none { it.entries.isNotEmpty() }) {
                EmptyHistoryState()
                return@Column
            }

            // Statistics Summary
            StatisticsSummaryCard(stats)

            // Calendar View
            CalorieCalendarCard(
                calendarData = calendarData,
                year = calendarYear,
                month = calendarMonth,
                onPreviousMonth = {
                    if (calendarMonth == 0) { calendarYear--; calendarMonth = 11 }
                    else calendarMonth--
                },
                onNextMonth = {
                    if (calendarMonth == 11) { calendarYear++; calendarMonth = 0 }
                    else calendarMonth++
                },
                onDayTapped = onDayTapped
            )

            // Trend Graph
            CalorieTrendGraph(
                trendData = trendData,
                targetCalories = stats.targetCalories,
                selectedDays = selectedTrendDays,
                onDaysChanged = { selectedTrendDays = it }
            )

            // Weekly Summary
            WeeklySummaryCard(weeklySummaries)

            // Macro Trends
            MacroTrendCard(
                avgProtein = macroTrend.first,
                avgCarbs = macroTrend.second,
                avgFat = macroTrend.third,
                stats = stats
            )

            // Detailed Statistics
            DetailedStatsCard(stats)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📊", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No tracking history yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                "Start logging your food intake to see trends, calendar highlights, and insights here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun StatisticsSummaryCard(stats: CalorieHistoryStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "📈 Your Overview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Top stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatsGridItem("Avg/Day", "${"%.0f".format(stats.averageDailyCalories)} kcal", Color(0xFF2196F3))
                StatsGridItem("Days Tracked", "${stats.totalDaysTracked}", MaterialTheme.colorScheme.primary)
                StatsGridItem("On Target", "${stats.daysAtTarget} days", Color(0xFF4CAF50))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatsGridItem("Streak 🔥", "${stats.currentStreak} days", Color(0xFFFF9800))
                StatsGridItem("Best Streak", "${stats.longestStreak} days", Color(0xFF9C27B0))
                StatsGridItem("Adherence", "${"%.0f".format(stats.adherencePercent)}%", Color(0xFF4CAF50))
            }

            // Calorie balance
            Spacer(modifier = Modifier.height(12.dp))
            val isDeficit = stats.calorieDeficitOrSurplus < 0
            val balanceColor = when {
                kotlin.math.abs(stats.calorieDeficitOrSurplus) < 100 -> Color(0xFF4CAF50)
                isDeficit -> Color(0xFF2196F3)
                else -> Color(0xFFFF9800)
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = balanceColor.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        kotlin.math.abs(stats.calorieDeficitOrSurplus) < 100 ->
                            "✅ Your average intake is very close to target!"
                        isDeficit ->
                            "📉 Averaging ${"%.0f".format(-stats.calorieDeficitOrSurplus)} kcal below target per day"
                        else ->
                            "📈 Averaging ${"%.0f".format(stats.calorieDeficitOrSurplus)} kcal above target per day"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = balanceColor,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun StatsGridItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalorieCalendarCard(
    calendarData: Map<Int, DayCalorieStatus>,
    year: Int,
    month: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayTapped: (String) -> Unit
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val todayDay = Calendar.getInstance().let {
        if (it.get(Calendar.YEAR) == year && it.get(Calendar.MONTH) == month)
            it.get(Calendar.DAY_OF_MONTH) else -1
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous")
                }
                Text(
                    "${monthNames[month]} $year",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day labels
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEach { day ->
                    Text(
                        day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Calendar grid
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1

                        if (day < 1 || day > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val status = calendarData[day]
                            val bgColor = when (status?.statusColor) {
                                CalorieAdherenceStatus.ON_TARGET -> Color(0xFF4CAF50)
                                CalorieAdherenceStatus.CLOSE -> Color(0xFFFFC107)
                                CalorieAdherenceStatus.OVER -> Color(0xFFFF5722)
                                CalorieAdherenceStatus.UNDER -> Color(0xFF2196F3)
                                else -> Color.Transparent
                            }
                            val isToday = day == todayDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (status?.hasData == true) bgColor.copy(alpha = 0.7f)
                                        else Color.Transparent
                                    )
                                    .then(
                                        if (isToday) Modifier.background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            CircleShape
                                        ) else Modifier
                                    )
                                    .clickable(enabled = status?.hasData == true) {
                                        status?.date?.let { onDayTapped(it) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$day",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = when {
                                        status?.hasData == true -> Color.White
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    },
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalendarLegendItem(Color(0xFF4CAF50), "On Target")
                CalendarLegendItem(Color(0xFFFFC107), "±200 cal")
                CalendarLegendItem(Color(0xFFFF5722), "Over")
                CalendarLegendItem(Color(0xFF2196F3), "Under")
            }
        }
    }
}

@Composable
private fun CalendarLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
    }
}

@Composable
private fun CalorieTrendGraph(
    trendData: List<Pair<String, Double>>,
    targetCalories: Double,
    selectedDays: Int,
    onDaysChanged: (Int) -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "trend"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Calorie Trend",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(7, 14, 30, 90).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { onDaysChanged(days) },
                        label = { Text("${days}d", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (trendData.size < 2) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Not enough data for the selected period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                return@Column
            }

            val values = trendData.map { it.second }
            val minVal = (minOf(values.minOrNull() ?: 0.0, targetCalories) * 0.9).toFloat()
            val maxVal = (maxOf(values.maxOrNull() ?: 0.0, targetCalories) * 1.1).toFloat()
            val valRange = maxVal - minVal

            val graphColor = MaterialTheme.colorScheme.primary
            val targetColor = Color(0xFF4CAF50)
            val overColor = Color(0xFFFF9800)
            val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width; val h = size.height
                    val padL = 40.dp.toPx(); val padR = 12.dp.toPx()
                    val padT = 8.dp.toPx(); val padB = 24.dp.toPx()
                    val graphW = w - padL - padR; val graphH = h - padT - padB
                    val pointCount = trendData.size
                    val stepX = graphW / (pointCount - 1).toFloat()

                    fun yPos(value: Float) = padT + graphH - ((value - minVal) / valRange * graphH)
                    fun xPos(index: Int) = padL + index * stepX

                    // Grid lines
                    for (i in 0..4) {
                        val y = padT + graphH * i / 4
                        drawLine(gridColor, Offset(padL, y), Offset(w - padR, y), strokeWidth = 1.dp.toPx())
                    }

                    // Target line
                    val targetY = yPos(targetCalories.toFloat())
                    drawLine(
                        color = targetColor,
                        start = Offset(padL, targetY),
                        end = Offset((w - padR) * animatedProgress, targetY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )

                    // Fill area under graph
                    if (pointCount >= 2) {
                        val fillPath = Path()
                        trendData.forEachIndexed { index, (_, value) ->
                            val x = xPos(index) * animatedProgress
                            val y = yPos(value.toFloat())
                            if (index == 0) fillPath.moveTo(x, y) else fillPath.lineTo(x, y)
                        }
                        fillPath.lineTo(xPos(pointCount - 1) * animatedProgress, padT + graphH)
                        fillPath.lineTo(xPos(0) * animatedProgress, padT + graphH)
                        fillPath.close()
                        drawPath(fillPath, color = graphColor.copy(alpha = 0.1f))

                        // Main line
                        val linePath = Path()
                        trendData.forEachIndexed { index, (_, value) ->
                            val x = xPos(index) * animatedProgress
                            val y = yPos(value.toFloat())
                            if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                        }
                        drawPath(linePath, color = graphColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

                        // Points
                        trendData.forEachIndexed { index, (_, value) ->
                            val x = xPos(index) * animatedProgress
                            val y = yPos(value.toFloat())
                            val ptColor = if (value > targetCalories + 200) overColor else graphColor
                            drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(x, y))
                            drawCircle(ptColor, radius = 3.5.dp.toPx(), center = Offset(x, y))
                        }
                    }
                }
            }

            // X-axis labels
            if (trendData.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val labelCount = minOf(trendData.size, 5)
                    val step = if (labelCount > 1) (trendData.size - 1) / (labelCount - 1) else 0
                    repeat(labelCount) { i ->
                        val idx = i * step
                        Text(
                            trendData.getOrNull(idx)?.first ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Legend
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Intake", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.width(16.dp).height(2.dp).background(Color(0xFF4CAF50)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Target (${"%.0f".format(targetCalories)} kcal)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun WeeklySummaryCard(weeklySummaries: List<WeeklyCalorieSummary>) {
    if (weeklySummaries.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Weekly Summary",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            weeklySummaries.take(4).forEachIndexed { index, week ->
                val diff = week.averageCalories - week.targetCalories
                val diffColor = when {
                    kotlin.math.abs(diff) < 100 -> Color(0xFF4CAF50)
                    diff > 0 -> Color(0xFFFF9800)
                    else -> Color(0xFF2196F3)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                week.weekLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                "${week.daysTracked} days tracked • Total: ${"%.0f".format(week.totalCalories)} kcal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${"%.0f".format(week.averageCalories)} kcal/day",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = diffColor
                            )
                            Text(
                                "${if (diff > 0) "+" else ""}${"%.0f".format(diff)} vs target",
                                style = MaterialTheme.typography.labelSmall,
                                color = diffColor.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (index < weeklySummaries.size - 1) Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun MacroTrendCard(
    avgProtein: Double,
    avgCarbs: Double,
    avgFat: Double,
    stats: CalorieHistoryStats
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Macro Trends",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            MacroTrendRow(
                label = "Protein",
                emoji = "🥩",
                average = avgProtein,
                target = stats.targetProtein,
                color = Color(0xFFF44336),
                unit = "g"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MacroTrendRow(
                label = "Carbs",
                emoji = "🍞",
                average = avgCarbs,
                target = stats.targetCarbs,
                color = Color(0xFFFFEB3B),
                unit = "g"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MacroTrendRow(
                label = "Fat",
                emoji = "🥑",
                average = avgFat,
                target = stats.targetFat,
                color = Color(0xFF4CAF50),
                unit = "g"
            )
        }
    }
}

@Composable
private fun MacroTrendRow(
    label: String, emoji: String, average: Double,
    target: Double, color: Color, unit: String
) {
    val hasTarget = target > 0
    val progress = if (hasTarget) (average / target).toFloat().coerceIn(0f, 1.5f) else 0f
    val animProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000), label = "macro"
    )

    val statusText = when {
        !hasTarget -> "${"%.0f".format(average)} $unit/day (no target set)"
        kotlin.math.abs(average - target) < target * 0.1 ->
            "${"%.0f".format(average)} / ${"%.0f".format(target)} $unit ✅"
        average < target ->
            "Avg ${"%.0f".format(average)} $unit/day (target: ${"%.0f".format(target)} $unit) — Consider increasing"
        else ->
            "Avg ${"%.0f".format(average)} $unit/day (target: ${"%.0f".format(target)} $unit) — Slightly high"
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            }
            Text(
                statusText,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontSize = 10.sp,
                modifier = Modifier.weight(1f, fill = false).padding(start = 8.dp),
                textAlign = TextAlign.End
            )
        }
        if (hasTarget) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(6.dp)
                    .clip(RoundedCornerShape(3.dp)).background(color.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(animProgress)
                        .clip(RoundedCornerShape(3.dp)).background(color)
                )
            }
        }
    }
}

@Composable
private fun DetailedStatsCard(stats: CalorieHistoryStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Detailed Statistics",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            stats.highestCalorieDay?.let { (date, cal) ->
                StatDetailRow("🔺 Highest Day", "$date — ${"%.0f".format(cal)} kcal")
            }
            stats.lowestCalorieDay?.let { (date, cal) ->
                StatDetailRow("🔻 Lowest Day", "$date — ${"%.0f".format(cal)} kcal")
            }
            StatDetailRow(
                "📅 Weekly Average",
                "${"%.0f".format(stats.weeklyAverage)} kcal/day (target: ${"%.0f".format(stats.targetCalories)} kcal)"
            )
            StatDetailRow(
                "📦 Weekly Total",
                "${"%.0f".format(stats.weeklyTotal)} kcal this week"
            )
            stats.mostLoggedFood?.let {
                StatDetailRow("⭐ Most Logged Food", it)
            }
            StatDetailRow(
                "🎯 Target Adherence",
                "${"%.0f".format(stats.adherencePercent)}% (${stats.daysAtTarget}/${stats.totalDaysTracked} days within ±100 kcal)"
            )
            StatDetailRow(
                "🔥 Current Streak",
                "${stats.currentStreak} day${if (stats.currentStreak != 1) "s" else ""} tracking consistently"
            )
            StatDetailRow(
                "🏆 Longest Streak",
                "${stats.longestStreak} day${if (stats.longestStreak != 1) "s" else ""}"
            )
        }
    }
}

@Composable
private fun StatDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(0.45f),
            fontSize = 11.sp
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.55f),
            fontSize = 11.sp
        )
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
