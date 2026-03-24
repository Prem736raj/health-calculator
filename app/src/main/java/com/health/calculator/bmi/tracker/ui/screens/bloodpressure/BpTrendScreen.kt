package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BpTrendScreen(
    onNavigateBack: () -> Unit,
    viewModel: BpTrendViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(uiState.selectedTabIndex) {
        pagerState.animateScrollToPage(uiState.selectedTabIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(pagerState.currentPage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("BP Trends", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (uiState.dataPoints.isEmpty()) {
            BpTrendEmptyState(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Trend Arrow Card
                BpTrendArrowCard(statistics = uiState.statistics)

                // Tab Row
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("Graph", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Outlined.Timeline, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("Calendar", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 2,
                        onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                        text = { Text("Stats", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Outlined.Analytics, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> BpGraphTab(
                            uiState = uiState,
                            viewModel = viewModel,
                            onDataPointClicked = viewModel::onDataPointClicked
                        )
                        1 -> BpCalendarTab(
                            calendarMonth = uiState.calendarMonth,
                            calendarDayData = uiState.calendarDayData,
                            onMonthChange = viewModel::onCalendarMonthChange,
                            onDayClicked = viewModel::onDayClicked
                        )
                        2 -> BpStatsTab(statistics = uiState.statistics)
                    }
                }
            }
        }
    }

    // Day detail dialog
    if (uiState.showDayDetail && uiState.selectedDayReadings != null) {
        BpDayDetailDialog(
            dayData = uiState.selectedDayReadings!!,
            onDismiss = { viewModel.onDismissDayDetail() }
        )
    }

    // Data point detail dialog
    if (uiState.showDataPointDetail && uiState.selectedDataPoint != null) {
        BpDataPointDetailDialog(
            dataPoint = uiState.selectedDataPoint!!,
            onDismiss = { viewModel.onDismissDataPointDetail() }
        )
    }
}

// ─── Trend Arrow Card ──────────────────────────────────────────────────────────

@Composable
private fun BpTrendArrowCard(statistics: BpStatistics) {
    val trend = statistics.trendDirection
    if (trend == BpTrendDirection.INSUFFICIENT) return

    val (bgColor, iconColor, icon) = when (trend) {
        BpTrendDirection.IMPROVING -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.1f),
            Color(0xFF4CAF50),
            Icons.AutoMirrored.Filled.TrendingDown
        )
        BpTrendDirection.WORSENING -> Triple(
            Color(0xFFF44336).copy(alpha = 0.1f),
            Color(0xFFF44336),
            Icons.AutoMirrored.Filled.TrendingUp
        )
        BpTrendDirection.STEADY -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.1f),
            Color(0xFF2196F3),
            Icons.AutoMirrored.Filled.TrendingUp
        )
        else -> return
    }

    val sysChangeText = if (statistics.trendSystolicChange >= 0) "+${statistics.trendSystolicChange}"
    else "${statistics.trendSystolicChange}"
    val diaChangeText = if (statistics.trendDiastolicChange >= 0) "+${statistics.trendDiastolicChange}"
    else "${statistics.trendDiastolicChange}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${trend.emoji} ${trend.label}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                Text(
                    "SYS $sysChangeText / DIA $diaChangeText mmHg vs previous readings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ─── Graph Tab ─────────────────────────────────────────────────────────────────

@Composable
private fun BpGraphTab(
    uiState: BpTrendUiState,
    viewModel: BpTrendViewModel,
    onDataPointClicked: (BpDataPoint) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dataPoints = uiState.filteredDataPoints
    val selectedTimeRange = uiState.selectedTimeRange
    val onTimeRangeSelected = viewModel::onTimeRangeSelected

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time range filter
        item {
            BpTimeRangeSelector(
                selectedRange = selectedTimeRange,
                onRangeSelected = onTimeRangeSelected
            )
        }

        // Toggle Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Show:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                FilterChip(
                    selected = uiState.showPulsePressure,
                    onClick = { viewModel.onTogglePulsePressure() },
                    label = { Text("PP", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (uiState.showPulsePressure) {
                        { Icon(Icons.Filled.Check, null, Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF7E57C2).copy(alpha = 0.15f)
                    )
                )
                FilterChip(
                    selected = uiState.showMAP,
                    onClick = { viewModel.onToggleMAP() },
                    label = { Text("MAP", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (uiState.showMAP) {
                        { Icon(Icons.Filled.Check, null, Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00897B).copy(alpha = 0.15f)
                    )
                )
                FilterChip(
                    selected = uiState.showPulse,
                    onClick = { viewModel.onTogglePulse() },
                    label = { Text("Pulse", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (uiState.showPulse) {
                        { Icon(Icons.Filled.Check, null, Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE91E63).copy(alpha = 0.15f)
                    )
                )
            }
        }

        // Line Graph
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            "Blood Pressure Trend",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${dataPoints.size} readings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp).horizontalScroll(rememberScrollState())
                    ) {
                        LegendItem(color = Color(0xFFE53935), label = "Systolic")
                        LegendItem(color = Color(0xFF1E88E5), label = "Diastolic")
                        if (uiState.showPulsePressure) LegendItem(color = Color(0xFF7E57C2), label = "PP")
                        if (uiState.showMAP) LegendItem(color = Color(0xFF00897B), label = "MAP")
                        if (uiState.showPulse) LegendItem(color = Color(0xFFE91E63), label = "Pulse")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (dataPoints.size >= 2) {
                        BpLineChart(
                            dataPoints = dataPoints,
                            onDataPointClicked = onDataPointClicked,
                            showPP = uiState.showPulsePressure,
                            showMAP = uiState.showMAP,
                            showPulse = uiState.showPulse,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Need at least 2 readings for a graph",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// ─── Line Chart ────────────────────────────────────────────────────────────────

@Composable
private fun BpLineChart(
    dataPoints: List<BpDataPoint>,
    onDataPointClicked: (BpDataPoint) -> Unit,
    showPP: Boolean = false,
    showMAP: Boolean = false,
    showPulse: Boolean = false,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "chart_anim"
    )

    val textColor = MaterialTheme.colorScheme.onSurface
    var tappedIndex by remember { mutableIntStateOf(-1) }

    val zoneColors = listOf(
        Color(0xFF4CAF50).copy(alpha = 0.06f),  // Optimal <120
        Color(0xFF8BC34A).copy(alpha = 0.06f),  // Normal 120-130
        Color(0xFFFFC107).copy(alpha = 0.06f),  // High Normal 130-140
        Color(0xFFFF9800).copy(alpha = 0.06f),  // Grade 1 140-160
        Color(0xFFF44336).copy(alpha = 0.06f),  // Grade 2 160-180
        Color(0xFFB71C1C).copy(alpha = 0.06f),  // Grade 3 180+
    )

    Canvas(
        modifier = modifier
            .pointerInput(dataPoints) {
                detectTapGestures { offset ->
                    val paddingLeft = 48.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val chartWidth = size.width - paddingLeft - paddingRight

                    if (dataPoints.size < 2) return@detectTapGestures

                    val step = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

                    val index = ((offset.x - paddingLeft) / step).toInt()
                        .coerceIn(0, dataPoints.size - 1)

                    // Check proximity
                    val pointX = paddingLeft + index * step
                    if (kotlin.math.abs(offset.x - pointX) < 30.dp.toPx()) {
                        tappedIndex = index
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDataPointClicked(dataPoints[index])
                    }
                }
            }
    ) {
        val paddingLeft = 48.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 32.dp.toPx()

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val allValues = buildList {
            addAll(dataPoints.flatMap { listOf(it.systolic, it.diastolic) })
            if (showPP) addAll(dataPoints.map { it.systolic - it.diastolic })
            if (showMAP) addAll(dataPoints.map { (it.diastolic + (it.systolic - it.diastolic) / 3.0).toInt() })
            if (showPulse) addAll(dataPoints.mapNotNull { it.pulse })
        }
        val minVal = (allValues.minOrNull() ?: 40) - 10
        val maxVal = (allValues.maxOrNull() ?: 200) + 10

        fun valueToY(value: Int): Float {
            return paddingTop + chartHeight * (1 - (value - minVal).toFloat() / (maxVal - minVal).toFloat())
        }

        // Background zones
        val zoneBounds = listOf(120, 130, 140, 160, 180, maxVal.coerceAtLeast(200))
        var prevBound = minVal
        zoneBounds.forEachIndexed { i, bound ->
            if (i < zoneColors.size) {
                val top = valueToY(bound.coerceAtMost(maxVal))
                val bottom = valueToY(prevBound.coerceAtLeast(minVal))
                if (bottom > top) {
                    drawRect(
                        color = zoneColors[i],
                        topLeft = Offset(paddingLeft, top),
                        size = Size(chartWidth, bottom - top)
                    )
                }
            }
            prevBound = bound
        }

        // Grid lines
        val gridValues = listOf(80, 90, 120, 140, 160, 180)
        val labelPaint = android.graphics.Paint().apply {
            color = textColor.copy(alpha = 0.4f).hashCode()
            textSize = with(density) { 10.sp.toPx() }
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        gridValues.forEach { value ->
            if (value in minVal..maxVal) {
                val y = valueToY(value)
                drawLine(
                    color = textColor.copy(alpha = 0.08f),
                    start = Offset(paddingLeft, y),
                    end = Offset(size.width - paddingRight, y),
                    strokeWidth = 1.dp.toPx()
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "$value",
                    paddingLeft - 8.dp.toPx(),
                    y + 4.dp.toPx(),
                    labelPaint
                )
            }
        }

        if (dataPoints.size < 2) return@Canvas

        val step = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

        // Animated clipping
        val animatedWidth = chartWidth * animationProgress

        // Systolic line path
        val sysPath = Path()
        val diaPath = Path()

        dataPoints.forEachIndexed { index, point ->
            val x = paddingLeft + index * step
            if (x > paddingLeft + animatedWidth) return@forEachIndexed

            val sysY = valueToY(point.systolic)
            val diaY = valueToY(point.diastolic)

            if (index == 0) {
                sysPath.moveTo(x, sysY)
                diaPath.moveTo(x, diaY)
            } else {
                val prevX = paddingLeft + (index - 1) * step
                val prevSysY = valueToY(dataPoints[index - 1].systolic)
                val prevDiaY = valueToY(dataPoints[index - 1].diastolic)

                val cpX = (prevX + x) / 2

                sysPath.cubicTo(cpX, prevSysY, cpX, sysY, x, sysY)
                diaPath.cubicTo(cpX, prevDiaY, cpX, diaY, x, diaY)
            }
        }

        // Draw lines
        drawPath(
            path = diaPath,
            color = Color(0xFF1E88E5),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Pulse Pressure line
        if (showPP && dataPoints.size >= 2) {
            val ppPath = Path()
            dataPoints.forEachIndexed { index, point ->
                val x = paddingLeft + index * step
                if (x > paddingLeft + animatedWidth) return@forEachIndexed
                val ppValue = point.systolic - point.diastolic
                val ppY = valueToY(ppValue)
                if (index == 0) ppPath.moveTo(x, ppY)
                else {
                    val prevX = paddingLeft + (index - 1) * step
                    val prevPP = dataPoints[index - 1].systolic - dataPoints[index - 1].diastolic
                    val prevPPY = valueToY(prevPP)
                    val cpX = (prevX + x) / 2
                    ppPath.cubicTo(cpX, prevPPY, cpX, ppY, x, ppY)
                }
            }
            drawPath(
                ppPath, Color(0xFF7E57C2),
                style = Stroke(
                    2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                )
            )

            // PP dots
            dataPoints.forEachIndexed { index, point ->
                val x = paddingLeft + index * step
                if (x > paddingLeft + animatedWidth) return@forEachIndexed
                val ppY = valueToY(point.systolic - point.diastolic)
                drawCircle(Color.White, 3.dp.toPx(), Offset(x, ppY))
                drawCircle(Color(0xFF7E57C2), 2.5.dp.toPx(), Offset(x, ppY))
            }
        }

        // MAP line
        if (showMAP && dataPoints.size >= 2) {
            val mapPath = Path()
            dataPoints.forEachIndexed { index, point ->
                val x = paddingLeft + index * step
                if (x > paddingLeft + animatedWidth) return@forEachIndexed
                val mapValue = (point.diastolic + (point.systolic - point.diastolic) / 3.0).toInt()
                val mapY = valueToY(mapValue)
                if (index == 0) mapPath.moveTo(x, mapY)
                else {
                    val prevX = paddingLeft + (index - 1) * step
                    val prev = dataPoints[index - 1]
                    val prevMAP = (prev.diastolic + (prev.systolic - prev.diastolic) / 3.0).toInt()
                    val prevMAPY = valueToY(prevMAP)
                    val cpX = (prevX + x) / 2
                    mapPath.cubicTo(cpX, prevMAPY, cpX, mapY, x, mapY)
                }
            }
            drawPath(
                mapPath, Color(0xFF00897B),
                style = Stroke(
                    2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                )
            )

            dataPoints.forEachIndexed { index, point ->
                val x = paddingLeft + index * step
                if (x > paddingLeft + animatedWidth) return@forEachIndexed
                val mapY = valueToY((point.diastolic + (point.systolic - point.diastolic) / 3.0).toInt())
                drawCircle(Color.White, 3.dp.toPx(), Offset(x, mapY))
                drawCircle(Color(0xFF00897B), 2.5.dp.toPx(), Offset(x, mapY))
            }
        }

        // Pulse line
        if (showPulse && dataPoints.size >= 2) {
            val pulsePath = Path()
            var hasStarted = false
            dataPoints.forEachIndexed { index, point ->
                val x = paddingLeft + index * step
                if (x > paddingLeft + animatedWidth) return@forEachIndexed
                point.pulse?.let { pulseVal ->
                    val pulseY = valueToY(pulseVal)
                    if (!hasStarted) {
                        pulsePath.moveTo(x, pulseY); hasStarted = true
                    } else {
                        val prevX = paddingLeft + (index - 1) * step
                        val prevPulse = dataPoints[index - 1].pulse ?: pulseVal
                        val prevPulseY = valueToY(prevPulse)
                        val cpX = (prevX + x) / 2
                        pulsePath.cubicTo(cpX, prevPulseY, cpX, pulseY, x, pulseY)
                    }
                }
            }
            if (hasStarted) {
                drawPath(pulsePath, Color(0xFFE91E63), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

                dataPoints.forEachIndexed { index, point ->
                    val x = paddingLeft + index * step
                    if (x > paddingLeft + animatedWidth) return@forEachIndexed
                    point.pulse?.let { pulseVal ->
                        val pulseY = valueToY(pulseVal)
                        drawCircle(Color.White, 3.dp.toPx(), Offset(x, pulseY))
                        drawCircle(Color(0xFFE91E63), 2.5.dp.toPx(), Offset(x, pulseY))
                    }
                }
            }
        }

        // Data points
        dataPoints.forEachIndexed { index, point ->
            val x = paddingLeft + index * step
            if (x > paddingLeft + animatedWidth) return@forEachIndexed

            val sysY = valueToY(point.systolic)
            val diaY = valueToY(point.diastolic)
            val isSelected = index == tappedIndex
            val dotRadius = if (isSelected) 6.dp.toPx() else 4.dp.toPx()

            // Systolic dot
            drawCircle(color = Color.White, radius = dotRadius + 1.dp.toPx(), center = Offset(x, sysY))
            drawCircle(color = Color(0xFFE53935), radius = dotRadius, center = Offset(x, sysY))

            // Diastolic dot
            drawCircle(color = Color.White, radius = dotRadius + 1.dp.toPx(), center = Offset(x, diaY))
            drawCircle(color = Color(0xFF1E88E5), radius = dotRadius, center = Offset(x, diaY))

            // Selection highlight
            if (isSelected) {
                drawLine(
                    color = textColor.copy(alpha = 0.15f),
                    start = Offset(x, paddingTop),
                    end = Offset(x, size.height - paddingBottom),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Date labels (show a few)
        val dateLabelPaint = android.graphics.Paint().apply {
            color = textColor.copy(alpha = 0.4f).hashCode()
            textSize = with(density) { 9.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val labelCount = minOf(5, dataPoints.size)
        if (labelCount > 0) {
            val labelStep = (dataPoints.size - 1) / (labelCount - 1).coerceAtLeast(1)
            for (i in 0 until labelCount) {
                val idx = (i * labelStep).coerceAtMost(dataPoints.size - 1)
                val x = paddingLeft + idx * step
                val label = dataPoints[idx].dateTime.format(DateTimeFormatter.ofPattern("MM/dd"))
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    size.height - 6.dp.toPx(),
                    dateLabelPaint
                )
            }
        }
    }
}

// ─── Calendar Tab ──────────────────────────────────────────────────────────────

@Composable
private fun BpCalendarTab(
    calendarMonth: YearMonth,
    calendarDayData: Map<LocalDate, BpDayData>,
    onMonthChange: (YearMonth) -> Unit,
    onDayClicked: (LocalDate) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMonthChange(calendarMonth.minusMonths(1))
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
                        }
                        Text(
                            calendarMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMonthChange(calendarMonth.plusMonths(1))
                        }) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Next month")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Day of week headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    day,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar grid
                    val firstDay = calendarMonth.atDay(1)
                    val firstDayOfWeek = firstDay.dayOfWeek.value // Monday=1
                    val daysInMonth = calendarMonth.lengthOfMonth()
                    val totalCells = firstDayOfWeek - 1 + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - (firstDayOfWeek - 1) + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val date = calendarMonth.atDay(dayNumber)
                                    val dayData = calendarDayData[date]
                                    val isToday = date == LocalDate.now()

                                    CalendarDayCell(
                                        dayNumber = dayNumber,
                                        dayData = dayData,
                                        isToday = isToday,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onDayClicked(date)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CalendarLegendItem(color = Color(0xFF4CAF50), label = "Optimal")
                        CalendarLegendItem(color = Color(0xFFFFC107), label = "Elevated")
                        CalendarLegendItem(color = Color(0xFFFF9800), label = "High")
                        CalendarLegendItem(color = Color(0xFFF44336), label = "Very High")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    dayData: BpDayData?,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = dayData?.worstCategory?.let { getBpCategoryColor(it) }
    val hasData = dayData != null && dayData.readings.isNotEmpty()

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    hasData -> bgColor!!.copy(alpha = 0.2f)
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isToday) Modifier.border(
                    1.5.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                )
                else Modifier
            )
            .clickable(enabled = hasData, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "$dayNumber",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday || hasData) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    hasData -> bgColor!!
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
            if (hasData) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(bgColor!!)
                )
            }
        }
    }
}

@Composable
private fun CalendarLegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// ─── Stats Tab ─────────────────────────────────────────────────────────────────

@Composable
private fun BpStatsTab(statistics: BpStatistics) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overview card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Overview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatBox(
                            label = "Avg Systolic",
                            value = "${statistics.avgSystolic}",
                            color = Color(0xFFE53935)
                        )
                        StatBox(
                            label = "Avg Diastolic",
                            value = "${statistics.avgDiastolic}",
                            color = Color(0xFF1E88E5)
                        )
                        statistics.avgPulse?.let {
                            StatBox(
                                label = "Avg Pulse",
                                value = "$it",
                                color = Color(0xFFE91E63)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Text(
                        "Total Readings: ${statistics.totalReadings}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Highs & Lows
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Highs & Lows", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HighLowCard(
                            label = "Highest",
                            systolic = statistics.highestSystolic,
                            diastolic = statistics.highestDiastolic,
                            color = Color(0xFFF44336),
                            icon = Icons.Filled.KeyboardArrowUp,
                            modifier = Modifier.weight(1f)
                        )
                        HighLowCard(
                            label = "Lowest",
                            systolic = statistics.lowestSystolic,
                            diastolic = statistics.lowestDiastolic,
                            color = Color(0xFF4CAF50),
                            icon = Icons.Filled.KeyboardArrowDown,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Morning vs Evening
        if (statistics.morningAvgSystolic != null || statistics.eveningAvgSystolic != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Morning vs Evening", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Morning
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3E0)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🌅 Morning", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (statistics.morningAvgSystolic != null) {
                                        Text(
                                            "${statistics.morningAvgSystolic!!.toInt()}/${statistics.morningAvgDiastolic!!.toInt()}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("mmHg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    } else {
                                        Text("No data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    }
                                }
                            }

                            // Evening
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE8EAF6)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🌆 Evening", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (statistics.eveningAvgSystolic != null) {
                                        Text(
                                            "${statistics.eveningAvgSystolic!!.toInt()}/${statistics.eveningAvgDiastolic!!.toInt()}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("mmHg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    } else {
                                        Text("No data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Distribution Donut
        if (statistics.categoryDistribution.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Category Distribution", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BpDonutChart(
                                distribution = statistics.categoryDistribution,
                                modifier = Modifier.size(120.dp)
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                statistics.categoryDistribution.forEach { dist ->
                                    val color = getBpCategoryColor(dist.category)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Text(
                                            dist.category.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "${(dist.percentage * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun HighLowCard(
    label: String,
    systolic: Int?,
    diastolic: Int?,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (systolic != null && diastolic != null) {
                Text(
                    "$systolic/$diastolic",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text("mmHg", style = MaterialTheme.typography.bodySmall, color = color.copy(alpha = 0.6f))
            } else {
                Text("--", style = MaterialTheme.typography.titleLarge, color = color.copy(alpha = 0.4f))
            }
        }
    }
}

// ─── Donut Chart ───────────────────────────────────────────────────────────────

@Composable
private fun BpDonutChart(
    distribution: List<BpCategoryDistribution>,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "donut_anim"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 24.dp.toPx()
        val radius = (minOf(size.width, size.height) - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        var startAngle = -90f

        distribution.forEach { dist ->
            val sweepAngle = dist.percentage * 360f * animatedProgress
            val color = getBpCategoryColor(dist.category)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            startAngle += sweepAngle
        }
    }
}

// ─── Day Detail Dialog ─────────────────────────────────────────────────────────

@Composable
private fun BpDayDetailDialog(
    dayData: BpDayData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                dayData.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (dayData.avgSystolic != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Day Average", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(
                                "${dayData.avgSystolic}/${dayData.avgDiastolic} mmHg",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    "${dayData.readings.size} reading(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                dayData.readings.forEach { entity ->
                    val cat = try { BpCategory.valueOf(entity.category) } catch (e: Exception) { BpCategory.OPTIMAL }
                    val catColor = getBpCategoryColor(cat)
                    val time = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(entity.measurementTimestamp),
                        ZoneId.systemDefault()
                    ).format(DateTimeFormatter.ofPattern("hh:mm a"))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(8.dp).clip(CircleShape).background(catColor)
                            )
                            Column {
                                Text(
                                    "${entity.systolic}/${entity.diastolic} mmHg",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    time,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                        Text(
                            cat.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = catColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// ─── Data Point Detail Dialog ──────────────────────────────────────────────────

@Composable
private fun BpDataPointDetailDialog(
    dataPoint: BpDataPoint,
    onDismiss: () -> Unit
) {
    val catColor = getBpCategoryColor(dataPoint.category)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${dataPoint.systolic}/${dataPoint.diastolic}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = catColor
                )
                Text("mmHg", style = MaterialTheme.typography.bodySmall, color = catColor.copy(alpha = 0.6f))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = catColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        dataPoint.category.displayName,
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = catColor
                    )
                }

                Text(
                    dataPoint.dateTime.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy 'at' hh:mm a")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                dataPoint.pulse?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.FavoriteBorder, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pulse: $it BPM", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }

                if (dataPoint.entity.note.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.StickyNote2, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(
                                dataPoint.entity.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// ─── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun BpTrendEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Timeline, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Text("No Trend Data Yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(
                "Take multiple blood pressure readings\nover time to see your trends here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BpTimeRangeSelector(
    selectedRange: BpTimeRange,
    onRangeSelected: (BpTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = BpTimeRange.values().indexOf(selectedRange),
        edgePadding = 0.dp,
        containerColor = Color.Transparent,
        divider = {},
        modifier = modifier.fillMaxWidth()
    ) {
        BpTimeRange.values().forEachIndexed { index, range ->
            val selected = selectedRange == range
            Tab(
                selected = selected,
                onClick = { onRangeSelected(range) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = range.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
