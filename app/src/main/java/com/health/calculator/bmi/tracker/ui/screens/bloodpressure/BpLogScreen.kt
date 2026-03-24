package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity
import com.health.calculator.bmi.tracker.data.model.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BpLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: BpLogViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BP Log", fontWeight = FontWeight.Bold)
                        if (uiState.readingsCount > 0) {
                            Text(
                                "${uiState.readingsCount} readings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.readings.isEmpty()) {
            BpLogEmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(
                    items = uiState.readings,
                    key = { _, item -> item.id }
                ) { index, entity ->
                    val enterAnim = remember {
                        MutableTransitionState(false).apply { targetState = true }
                    }

                    AnimatedVisibility(
                        visibleState = enterAnim,
                        enter = slideInVertically(
                            initialOffsetY = { 60 },
                            animationSpec = tween(
                                400,
                                delayMillis = (index * 50).coerceAtMost(500)
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                400,
                                delayMillis = (index * 50).coerceAtMost(500)
                            )
                        )
                    ) {
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onDeleteRequested(entity)
                                    false // Don't auto dismiss, show dialog
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.error),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.padding(end = 20.dp)
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            BpLogEntryCard(
                                entity = entity,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onReadingClicked(entity)
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Detail bottom sheet
    if (uiState.selectedDetail.isVisible) {
        BpLogDetailSheet(
            detailState = uiState.selectedDetail,
            onDismiss = { viewModel.onDismissDetail() },
            onDelete = {
                uiState.selectedDetail.entity?.let { viewModel.onDeleteRequested(it) }
            },
            onEditNote = { id, note ->
                viewModel.onEditNote(id, note)
            }
        )
    }

    // Delete confirmation
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            icon = {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Reading?") },
            text = {
                val entity = uiState.deletingEntity
                if (entity?.isAveragedResult == true) {
                    Text("This will delete the averaged reading and all ${entity.readingsInAverage} individual readings in this group.")
                } else {
                    Text("This reading will be permanently removed from your log.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmDelete() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.onDismissDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Note dialog
    if (uiState.showNoteDialog) {
        BpNoteDialog(
            noteText = uiState.editingNoteText,
            onNoteChange = viewModel::onNoteTextChange,
            onSave = { viewModel.onSaveNote() },
            onDismiss = { viewModel.onDismissNoteDialog() }
        )
    }
}

// ─── Log Entry Card ────────────────────────────────────────────────────────────

@Composable
private fun BpLogEntryCard(
    entity: BloodPressureEntity,
    onClick: () -> Unit
) {
    val category = try {
        BpCategory.valueOf(entity.category)
    } catch (e: Exception) {
        BpCategory.OPTIMAL
    }
    val categoryColor = getBpCategoryColor(category)

    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(entity.measurementTimestamp),
        ZoneId.systemDefault()
    )
    val isToday = dateTime.toLocalDate() == java.time.LocalDate.now()
    val isYesterday = dateTime.toLocalDate() == java.time.LocalDate.now().minusDays(1)

    val dateLabel = when {
        isToday -> "Today"
        isYesterday -> "Yesterday"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }
    val timeLabel = dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

    val timeOfDayEmoji = when (entity.timeOfDay) {
        BpTimeOfDay.MORNING.name -> "🌅"
        BpTimeOfDay.AFTERNOON.name -> "☀️"
        BpTimeOfDay.EVENING.name -> "🌆"
        BpTimeOfDay.NIGHT.name -> "🌙"
        else -> ""
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${entity.systolic}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "${entity.systolic}/${entity.diastolic}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "mmHg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    if (entity.isAveragedResult) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "AVG",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Text(
                        category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "$timeOfDayEmoji $dateLabel • $timeLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }

                if (entity.onMedication) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Medication,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF1E88E5)
                        )
                        Text(
                            if (entity.medicationName.isNotEmpty()) entity.medicationName else "On Medication",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E88E5).copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Pulse if available
            entity.pulse?.let { pulse ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFE91E63).copy(alpha = 0.6f)
                    )
                    Text(
                        "$pulse",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "BPM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "View details",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
        }
    }
}

// ─── Log Empty State ───────────────────────────────────────────────────────────

@Composable
private fun BpLogEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.MonitorHeart,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "No Readings Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Your blood pressure readings will appear here.\nCheck your BP to start tracking!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Log Detail Bottom Sheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BpLogDetailSheet(
    detailState: BpLogDetailState,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEditNote: (Long, String) -> Unit
) {
    val entity = detailState.entity ?: return
    val reading = detailState.reading ?: return
    val category = reading.category
    val categoryColor = getBpCategoryColor(category)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Reading Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    reading.formattedDateTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Main reading
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = categoryColor.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${reading.systolic}/${reading.diastolic}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                    Text(
                        "mmHg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = categoryColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = categoryColor.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            category.displayName,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }

                    if (entity.isAveragedResult) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "📊 Average of ${entity.readingsInAverage} readings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Details grid
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Measurement Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Metrics
                    DetailRow("Pulse Pressure", "${entity.pulsePressure} mmHg")
                    DetailRow(
                        "Mean Arterial Pressure",
                        "${String.format("%.1f", entity.meanArterialPressure)} mmHg"
                    )

                    entity.pulse?.let {
                        DetailRow("Heart Rate", "$it BPM")
                    }

                    entity.arm?.let {
                        val armDisplay = try {
                            BpArm.valueOf(it).displayName
                        } catch (e: Exception) { it }
                        DetailRow("Arm", armDisplay)
                    }

                    entity.position?.let {
                        val posDisplay = try {
                            BpPosition.valueOf(it).displayName
                        } catch (e: Exception) { it }
                        DetailRow("Position", posDisplay)
                    }

                    entity.timeOfDay?.let {
                        val todDisplay = try {
                            BpTimeOfDay.valueOf(it).displayName
                        } catch (e: Exception) { it }
                        val emoji = when (it) {
                            BpTimeOfDay.MORNING.name -> "🌅"
                            BpTimeOfDay.AFTERNOON.name -> "☀️"
                            BpTimeOfDay.EVENING.name -> "🌆"
                            BpTimeOfDay.NIGHT.name -> "🌙"
                            else -> ""
                        }
                        DetailRow("Time of Day", "$emoji $todDisplay")
                    }

                    val riskDisplay = try {
                        BpRiskLevel.valueOf(entity.riskLevel).displayName
                    } catch (e: Exception) { entity.riskLevel }
                    val riskColor = try {
                        getBpRiskColor(BpRiskLevel.valueOf(entity.riskLevel))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.onSurface
                    }
                    DetailRow("Risk Level", riskDisplay, valueColor = riskColor)

                    if (entity.onMedication) {
                        DetailRow(
                            "Medication",
                            if (entity.medicationName.isNotEmpty()) entity.medicationName else "Yes",
                            valueColor = Color(0xFF1E88E5)
                        )
                    }
                }
            }

            // Individual readings if averaged
            if (entity.isAveragedResult && detailState.groupReadings.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Individual Readings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        detailState.groupReadings
                            .filter { it.isPartOfAverage }
                            .forEachIndexed { index, r ->
                                val rCat = try {
                                    BpCategory.valueOf(r.category)
                                } catch (e: Exception) {
                                    BpCategory.OPTIMAL
                                }
                                val rColor = getBpCategoryColor(rCat)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.3f
                                            )
                                        )
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "#${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.4f
                                            )
                                        )
                                        Text(
                                            "${r.systolic}/${r.diastolic}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "mmHg",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.4f
                                            )
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(rColor)
                                    )
                                }
                            }
                    }
                }
            }

            // Note section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = {
                                onEditNote(entity.id, entity.note)
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (entity.note.isEmpty()) "Add Note" else "Edit")
                        }
                    }

                    if (entity.note.isNotEmpty()) {
                        Text(
                            entity.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            "No notes added",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                }
            }

            // Delete button
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Reading", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

// ─── Note Dialog ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BpNoteDialog(
    noteText: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val quickNotes = listOf(
        "After exercise", "Morning reading", "Evening reading",
        "Before medication", "After medication", "Feeling stressed",
        "After rest", "After coffee", "Routine check"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Add Note",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = onNoteChange,
                    label = { Text("Note") },
                    placeholder = { Text("e.g., after exercise, morning reading...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    "Quick notes:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                // Quick note chips - wrapped
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickNotes.forEach { quickNote ->
                        SuggestionChip(
                            onClick = {
                                val newText = if (noteText.isNotEmpty()) {
                                    "$noteText, $quickNote"
                                } else {
                                    quickNote
                                }
                                onNoteChange(newText)
                            },
                            label = {
                                Text(
                                    quickNote,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
