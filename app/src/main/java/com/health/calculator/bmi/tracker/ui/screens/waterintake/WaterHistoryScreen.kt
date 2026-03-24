// ui/screens/waterintake/WaterHistoryScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

private val WaterBlueLight = Color(0xFF64B5F6)
private val WaterBlueMedium = Color(0xFF2196F3)
private val WaterBlueDark = Color(0xFF1565C0)
private val WaterBluePale = Color(0xFFBBDEFB)
private val WaterBlueSurface = Color(0xFFE3F2FD)
private val GoalGreen = Color(0xFF4CAF50)
private val GoalYellow = Color(0xFFFFC107)
private val GoalOrange = Color(0xFFFF9800)
private val GoalRed = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterHistoryScreen(
    viewModel: WaterHistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val stats by viewModel.stats.collectAsState()
    val dailyData by viewModel.dailyData.collectAsState()
    val selectedDayLogs by viewModel.selectedDayLogs.collectAsState()
    val weeklyReport by viewModel.weeklyReport.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📊", fontSize = 22.sp)
                        Text("Water History", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = WaterBlueMedium
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Calendar", fontSize = 13.sp) },
                    icon = { Text("📅", fontSize = 16.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Trends", fontSize = 13.sp) },
                    icon = { Text("📈", fontSize = 16.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Stats", fontSize = 13.sp) },
                    icon = { Text("🏆", fontSize = 16.sp) }
                )
            }

            when (selectedTab) {
                0 -> CalendarTab(
                    dailyData = dailyData,
                    goalMl = viewModel.dailyGoalMl,
                    selectedDayLogs = selectedDayLogs,
                    onDaySelected = { viewModel.selectDay(it) },
                    onMonthChanged = { viewModel.changeMonth(it) },
                    currentMonth = viewModel.currentMonth,
                    isVisible = isVisible
                )
                1 -> TrendsTab(
                    dailyData = dailyData,
                    goalMl = viewModel.dailyGoalMl,
                    weeklyReport = weeklyReport,
                    isVisible = isVisible
                )
                2 -> StatsTab(
                    stats = stats,
                    isVisible = isVisible
                )
            }
        }
    }
}

// ─── Calendar Tab ────────────────────────────────────────────────────────────

@Composable
private fun CalendarTab(
    dailyData: Map<String, DailyWaterData>,
    goalMl: Int,
    selectedDayLogs: SelectedDayInfo?,
    onDaySelected: (Calendar) -> Unit,
    onMonthChanged: (Int) -> Unit,
    currentMonth: Calendar,
    isVisible: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 }
            ) {
                CalendarCard(
                    dailyData = dailyData,
                    goalMl = goalMl,
                    onDaySelected = onDaySelected,
                    onMonthChanged = onMonthChanged,
                    currentMonth = currentMonth
                )
            }
        }

        // Legend
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 100))
            ) {
                CalendarLegend()
            }
        }

        // Selected day detail
        item {
            AnimatedVisibility(
                visible = selectedDayLogs != null,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                selectedDayLogs?.let { dayInfo ->
                    SelectedDayDetailCard(dayInfo, goalMl)
                }
            }
        }
    }
}

@Composable
private fun CalendarCard(
    dailyData: Map<String, DailyWaterData>,
    goalMl: Int,
    onDaySelected: (Calendar) -> Unit,
    onMonthChanged: (Int) -> Unit,
    currentMonth: Calendar
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChanged(-1) }) {
                    Icon(Icons.Default.ChevronLeft, "Previous month")
                }
                Text(
                    text = monthFormat.format(currentMonth.time),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WaterBlueDark
                )
                IconButton(onClick = { onMonthChanged(1) }) {
                    Icon(Icons.Default.ChevronRight, "Next month")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Day of week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar grid
            val cal = currentMonth.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val today = Calendar.getInstance()

            val totalSlots = firstDayOfWeek + daysInMonth
            val rows = (totalSlots + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dayIndex = row * 7 + col - firstDayOfWeek + 1
                        if (dayIndex in 1..daysInMonth) {
                            val dayCal = currentMonth.clone() as Calendar
                            dayCal.set(Calendar.DAY_OF_MONTH, dayIndex)
                            val dateKey = dateKeyFormat.format(dayCal.time)
                            val data = dailyData[dateKey]
                            val isToday = dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                    dayCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                            val isFuture = dayCal.after(today)

                            CalendarDayCell(
                                day = dayIndex,
                                data = data,
                                goalMl = goalMl,
                                isToday = isToday,
                                isFuture = isFuture,
                                onClick = { onDaySelected(dayCal) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    data: DailyWaterData?,
    goalMl: Int,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = if (data != null && goalMl > 0) {
        (data.totalMl.toFloat() / goalMl * 100)
    } else 0f

    val bgColor = when {
        isFuture -> Color.Transparent
        data == null || data.totalMl == 0 -> Color.Gray.copy(alpha = 0.08f)
        percentage >= 100 -> GoalGreen.copy(alpha = 0.2f)
        percentage >= 75 -> GoalYellow.copy(alpha = 0.2f)
        percentage >= 50 -> GoalOrange.copy(alpha = 0.2f)
        else -> GoalRed.copy(alpha = 0.2f)
    }

    val dotColor = when {
        isFuture -> Color.Transparent
        data == null || data.totalMl == 0 -> Color.Transparent
        percentage >= 100 -> GoalGreen
        percentage >= 75 -> GoalYellow
        percentage >= 50 -> GoalOrange
        else -> GoalRed
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(
                if (isToday) Modifier.border(2.dp, WaterBlueMedium, RoundedCornerShape(8.dp))
                else Modifier
            )
            .clickable(enabled = !isFuture && data != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    isToday -> WaterBlueDark
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (dotColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun CalendarLegend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem("100%+", GoalGreen)
            LegendItem("75%+", GoalYellow)
            LegendItem("50%+", GoalOrange)
            LegendItem("<50%", GoalRed)
            LegendItem("None", Color.Gray.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun SelectedDayDetailCard(dayInfo: SelectedDayInfo, goalMl: Int) {
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val percentage = if (goalMl > 0) (dayInfo.totalMl.toFloat() / goalMl * 100).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(dayInfo.date),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = WaterBlueDark
                )
                val statusColor = when {
                    percentage >= 100 -> GoalGreen
                    percentage >= 75 -> GoalYellow
                    percentage >= 50 -> GoalOrange
                    else -> GoalRed
                }
                Text(
                    text = "$percentage%",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = statusColor
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    percentage >= 100 -> GoalGreen
                    percentage >= 75 -> GoalYellow
                    percentage >= 50 -> GoalOrange
                    else -> GoalRed
                },
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )

            Text(
                text = "${dayInfo.totalMl}ml of ${goalMl}ml • ${dayInfo.entries.size} entries",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            if (dayInfo.entries.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                dayInfo.entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                when {
                                    entry.amountMl >= 500 -> "🍶"
                                    entry.amountMl >= 250 -> "🥛"
                                    else -> "💧"
                                },
                                fontSize = 16.sp
                            )
                            Text(
                                "+${entry.amountMl}ml",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            timeFormat.format(Date(entry.timestamp)),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

// ─── Trends Tab ──────────────────────────────────────────────────────────────

@Composable
private fun TrendsTab(
    dailyData: Map<String, DailyWaterData>,
    goalMl: Int,
    weeklyReport: WeeklyReport?,
    isVisible: Boolean
) {
    var selectedRange by remember { mutableIntStateOf(7) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Range selector
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(7 to "7 Days", 14 to "14 Days", 30 to "30 Days").forEach { (days, label) ->
                        FilterChip(
                            selected = selectedRange == days,
                            onClick = { selectedRange = days },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WaterBlueMedium,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Bar chart
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 40 }
            ) {
                BarChartCard(
                    dailyData = dailyData,
                    goalMl = goalMl,
                    days = selectedRange
                )
            }
        }

        // Weekly report
        item {
            AnimatedVisibility(
                visible = isVisible && weeklyReport != null,
                enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 40 }
            ) {
                weeklyReport?.let { WeeklyReportCard(it) }
            }
        }
    }
}

@Composable
private fun BarChartCard(
    dailyData: Map<String, DailyWaterData>,
    goalMl: Int,
    days: Int
) {
    val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayLabelFormat = SimpleDateFormat("dd", Locale.getDefault())
    val monthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    val chartData = remember(dailyData, days) {
        val result = mutableListOf<Pair<String, Int>>()
        val cal = Calendar.getInstance()
        for (i in days - 1 downTo 0) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val key = dateKeyFormat.format(dayCal.time)
            val label = if (days <= 14) dayLabelFormat.format(dayCal.time)
            else monthDayFormat.format(dayCal.time)
            val totalMl = dailyData[key]?.totalMl ?: 0
            result.add(label to totalMl)
        }
        result
    }

    val maxMl = (chartData.maxOfOrNull { it.second } ?: goalMl).coerceAtLeast(goalMl)
    val avgMl = if (chartData.isNotEmpty()) {
        chartData.filter { it.second > 0 }.let { active ->
            if (active.isNotEmpty()) active.sumOf { it.second } / active.size else 0
        }
    } else 0

    // Animation
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "bar_anim"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📊", fontSize = 18.sp)
                Text("Daily Intake", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            // Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height - 30f
                val barCount = chartData.size
                val barSpacing = 4f
                val barWidth = ((chartWidth - (barCount - 1) * barSpacing) / barCount).coerceAtLeast(4f)

                // Goal line
                val goalY = chartHeight * (1f - goalMl.toFloat() / maxMl)
                drawLine(
                    color = GoalGreen.copy(alpha = 0.6f),
                    start = Offset(0f, goalY),
                    end = Offset(chartWidth, goalY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )

                // Goal label
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "Goal",
                        chartWidth - 50f,
                        goalY - 8f,
                        android.graphics.Paint().apply {
                            color = GoalGreen.copy(alpha = 0.8f).toArgb()
                            textSize = 24f
                            isAntiAlias = true
                        }
                    )
                }

                // Average line
                if (avgMl > 0) {
                    val avgY = chartHeight * (1f - avgMl.toFloat() / maxMl)
                    drawLine(
                        color = WaterBlueMedium.copy(alpha = 0.5f),
                        start = Offset(0f, avgY),
                        end = Offset(chartWidth, avgY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                }

                // Bars
                chartData.forEachIndexed { index, (_, ml) ->
                    val barHeight = if (maxMl > 0) {
                        (chartHeight * (ml.toFloat() / maxMl) * animProgress).coerceAtLeast(0f)
                    } else 0f

                    val x = index * (barWidth + barSpacing)
                    val percentage = if (goalMl > 0) ml.toFloat() / goalMl * 100 else 0f

                    val barColor = when {
                        ml == 0 -> Color.Gray.copy(alpha = 0.15f)
                        percentage >= 100 -> GoalGreen
                        percentage >= 75 -> GoalYellow
                        percentage >= 50 -> GoalOrange
                        else -> GoalRed.copy(alpha = 0.7f)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, chartHeight - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f)
                    )
                }
            }

            // X-axis labels (show subset)
            if (chartData.size <= 14) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val step = if (chartData.size > 7) 2 else 1
                    chartData.filterIndexed { index, _ -> index % step == 0 }.forEach { (label, _) ->
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Legend row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                ChartLegendItem("Goal", GoalGreen)
                ChartLegendItem("Average", WaterBlueMedium)
            }
        }
    }
}

@Composable
private fun ChartLegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(2.dp)
                .background(color)
        )
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun WeeklyReportCard(report: WeeklyReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(WaterBlueMedium, WaterBlueDark)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "📋 This Week's Report",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeeklyStatItem(
                        label = "Average",
                        value = "${report.averageMl}ml",
                        icon = "📊"
                    )
                    WeeklyStatItem(
                        label = "Goal Met",
                        value = "${report.daysGoalMet}/7",
                        icon = "✅"
                    )
                    WeeklyStatItem(
                        label = "Total",
                        value = "${String.format(Locale.US, "%.1f", report.totalLiters)}L",
                        icon = "💧"
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                // Trend direction
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val trendIcon = when (report.trend) {
                        TrendDirection.IMPROVING -> Icons.AutoMirrored.Filled.TrendingUp
                        TrendDirection.DECLINING -> Icons.AutoMirrored.Filled.TrendingDown
                        TrendDirection.STEADY -> Icons.Filled.TrendingFlat
                    }
                    val trendColor = when (report.trend) {
                        TrendDirection.IMPROVING -> Color(0xFF81C784)
                        TrendDirection.DECLINING -> Color(0xFFEF9A9A)
                        TrendDirection.STEADY -> Color(0xFFFFE082)
                    }
                    Icon(trendIcon, null, tint = trendColor, modifier = Modifier.size(20.dp))
                    Text(
                        text = report.trendMessage,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }

                // Motivational message
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        text = report.motivationalMessage,
                        modifier = Modifier.padding(12.dp),
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyStatItem(label: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

// ─── Stats Tab ───────────────────────────────────────────────────────────────

@Composable
private fun StatsTab(
    stats: WaterStats?,
    isVisible: Boolean
) {
    if (stats == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌊", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("No data yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "Start tracking your water intake to see stats",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Streak card
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.9f)
            ) {
                StreakCard(
                    currentStreak = stats.currentStreak,
                    longestStreak = stats.longestStreak
                )
            }
        }

        // Stats grid
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 40 }
            ) {
                StatsGridCard(stats)
            }
        }

        // Achievement cards
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 40 }
            ) {
                RecordsCard(stats)
            }
        }
    }
}

@Composable
private fun StreakCard(currentStreak: Int, longestStreak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = if (currentStreak > 0)
                            listOf(Color(0xFFFF9800), Color(0xFFF44336))
                        else
                            listOf(Color.Gray, Color.DarkGray)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥", fontSize = 36.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "$currentStreak",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Current Streak",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Text(
                        "days",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(80.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆", fontSize = 36.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "$longestStreak",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Longest Streak",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Text(
                        "days",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsGridCard(stats: WaterStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📈", fontSize = 18.sp)
                Text("Statistics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                StatGridItem(
                    label = "Avg Daily",
                    value = "${stats.averageDailyMl}ml",
                    icon = "📊",
                    modifier = Modifier.weight(1f)
                )
                StatGridItem(
                    label = "Goal Met",
                    value = "${stats.daysGoalMet} days",
                    icon = "✅",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                StatGridItem(
                    label = "Success Rate",
                    value = "${stats.goalMetPercentage}%",
                    icon = "🎯",
                    modifier = Modifier.weight(1f)
                )
                StatGridItem(
                    label = "This Week",
                    value = "${String.format(Locale.US, "%.1f", stats.thisWeekTotalL)}L",
                    icon = "📅",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                StatGridItem(
                    label = "This Month",
                    value = "${String.format(Locale.US, "%.1f", stats.thisMonthTotalL)}L",
                    icon = "📆",
                    modifier = Modifier.weight(1f)
                )
                StatGridItem(
                    label = "Total Days",
                    value = "${stats.totalDaysTracked}",
                    icon = "🗓️",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatGridItem(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WaterBlueSurface.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 20.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaterBlueDark)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun RecordsCard(stats: WaterStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🏅", fontSize = 18.sp)
                Text("Personal Records", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            RecordRow("🏆", "Best Day Ever", "${stats.bestDayMl}ml", stats.bestDayDate)
            RecordRow("📈", "Best Week Total", "${String.format(Locale.US, "%.1f", stats.bestWeekTotalL)}L", null)
            RecordRow("🔥", "Longest Streak", "${stats.longestStreak} days", null)
        }
    }
}

@Composable
private fun RecordRow(icon: String, label: String, value: String, date: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WaterBlueSurface.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(icon, fontSize = 22.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            if (date != null) {
                Text(date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WaterBlueDark)
    }
}
