package com.health.calculator.bmi.tracker.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.data.management.*
import com.health.calculator.bmi.tracker.data.model.CalculatorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    onResetToOnboarding: () -> Unit,
    viewModel: DataManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar with undo
    LaunchedEffect(uiState.undoableDelete) {
        uiState.undoableDelete?.let {
            val result = snackbarHostState.showSnackbar(
                message = "Entry deleted",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearUndo()
            }
        }
    }

    // Handle general snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    // Handle delete everything completion
    LaunchedEffect(uiState.operationComplete) {
        if (uiState.operationComplete && uiState.deleteEverythingStep >= 3) {
            onResetToOnboarding()
        }
    }

    // Dialogs
    if (uiState.showCleanupByAge) {
        CleanupByAgeDialog(
            selectedAge = uiState.selectedCleanupAge,
            preview = uiState.cleanupPreview,
            isClearing = uiState.isClearing,
            onSelectAge = { viewModel.previewCleanupByAge(it) },
            onConfirm = { viewModel.executeCleanupByAge() },
            onDismiss = { viewModel.dismissCleanupByAge() }
        )
    }

    if (uiState.showCleanupByCalculator) {
        CleanupByCalculatorDialog(
            selectedTypes = uiState.selectedCleanupTypes,
            isClearing = uiState.isClearing,
            onToggleType = { viewModel.toggleCleanupType(it) },
            onConfirm = { viewModel.executeCleanupByCalculator() },
            onDismiss = { viewModel.dismissCleanupByCalculator() }
        )
    }

    if (uiState.showDeleteEverything) {
        DeleteEverythingDialog(
            step = uiState.deleteEverythingStep,
            confirmText = uiState.deleteConfirmText,
            isDeleting = uiState.isDeleting,
            onConfirmTextChange = { viewModel.updateDeleteConfirmText(it) },
            onNextStep = { viewModel.nextDeleteStep() },
            onConfirmDelete = { viewModel.executeDeleteEverything() },
            onDismiss = { viewModel.dismissDeleteEverything() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === STORAGE INFO ===
            item(key = "storage_header") {
                SectionLabel("💾 Storage Usage")
            }

            item(key = "storage_card") {
                StorageCard(
                    storageInfo = uiState.storageInfo,
                    isLoading = uiState.isLoadingStorage
                )
            }

            // === CLEANUP OPTIONS ===
            item(key = "cleanup_header") {
                SectionLabel("🧹 Data Cleanup")
            }

            item(key = "cleanup_age") {
                ActionCard(
                    icon = Icons.Default.DateRange,
                    iconColor = Color(0xFFFF9800),
                    title = "Clear Old Data",
                    subtitle = "Remove entries older than a specified period",
                    onClick = { viewModel.showCleanupByAge() }
                )
            }

            item(key = "cleanup_calculator") {
                ActionCard(
                    icon = Icons.Default.Calculate,
                    iconColor = Color(0xFF2196F3),
                    title = "Clear by Calculator",
                    subtitle = "Remove history for specific calculators only",
                    onClick = { viewModel.showCleanupByCalculator() }
                )
            }

            item(key = "cleanup_cache") {
                ActionCard(
                    icon = Icons.Default.CleaningServices,
                    iconColor = Color(0xFF4CAF50),
                    title = "Clear Cache",
                    subtitle = "Remove temporary files and cached data (${uiState.storageInfo.cacheFormatted})",
                    onClick = { viewModel.clearCache() }
                )
            }

            item(key = "cleanup_exports") {
                ActionCard(
                    icon = Icons.Default.FilePresent,
                    iconColor = Color(0xFF9C27B0),
                    title = "Clear Exports",
                    subtitle = "Remove exported PDF, CSV, and JSON files (${uiState.storageInfo.exportsFormatted})",
                    onClick = { viewModel.clearExports() }
                )
            }

            // === DATA INTEGRITY ===
            item(key = "integrity_header") {
                SectionLabel("🔍 Data Integrity")
            }

            item(key = "integrity_card") {
                IntegrityCard(
                    report = uiState.integrityReport,
                    onCheck = { viewModel.runIntegrityCheck() },
                    onFix = { viewModel.fixIntegrityIssues() }
                )
            }

            // === DANGER ZONE ===
            item(key = "danger_header") {
                SectionLabel("⚠️ Danger Zone")
            }

            item(key = "danger_card") {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Delete Everything",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    "Permanently remove all data and reset the app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.showDeleteEverything() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteForever, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Delete All Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun StorageCard(storageInfo: StorageInfo, isLoading: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Analyzing storage...", style = MaterialTheme.typography.bodySmall)
            } else {
                // Storage ring
                val segments = listOf(
                    Triple("History", storageInfo.historyBytes, Color(0xFF2196F3)),
                    Triple("Cache", storageInfo.cacheBytes, Color(0xFFFF9800)),
                    Triple("Settings", storageInfo.settingsBytes, Color(0xFF4CAF50)),
                    Triple("Exports", storageInfo.exportsBytes, Color(0xFF9C27B0)),
                    Triple("Backups", storageInfo.backupsBytes, Color(0xFFF44336))
                ).filter { it.second > 0 }

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .drawBehind {
                            val strokeWidth = 20f
                            val radius = size.minDimension / 2 - strokeWidth
                            var startAngle = -90f

                            // Background ring
                            drawCircle(
                                color = Color.LightGray.copy(alpha = 0.2f),
                                radius = radius,
                                style = Stroke(width = strokeWidth)
                            )

                            if (storageInfo.totalBytes > 0) {
                                segments.forEach { (_, bytes, color) ->
                                    val sweep = (bytes.toFloat() / storageInfo.totalBytes) * 360f
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep.coerceAtLeast(2f),
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                        topLeft = Offset(strokeWidth/2, strokeWidth/2),
                                        size = Size(size.width - strokeWidth, size.height - strokeWidth)
                                    )
                                    startAngle += sweep
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            storageInfo.totalFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Breakdown legend
                segments.forEach { (label, bytes, color) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            StorageInfo.formatBytes(bytes),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                if (segments.isEmpty()) {
                    Text(
                        "No significant data stored",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.ChevronRight, null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun IntegrityCard(
    report: IntegrityReport,
    onCheck: () -> Unit,
    onFix: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.VerifiedUser,
                    null,
                    tint = if (report.isComplete && report.isHealthy) Color(0xFF4CAF50)
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("Data Verification", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            if (report.isChecking) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Checking data integrity...", style = MaterialTheme.typography.bodySmall)
                }
            } else if (report.isComplete) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (report.isHealthy) Color(0xFF4CAF50).copy(alpha = 0.08f)
                    else Color(0xFFF44336).copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (report.isHealthy) Icons.Default.CheckCircle else Icons.Default.Error,
                            null,
                            tint = if (report.isHealthy) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                report.statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Checked ${report.totalEntries} entries",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                if (!report.isHealthy) {
                    Spacer(Modifier.height(8.dp))

                    if (report.corruptedEntries > 0) {
                        IssueRow("Corrupted entries", report.corruptedEntries, Color(0xFFF44336))
                    }
                    if (report.duplicateEntries > 0) {
                        IssueRow("Duplicate entries", report.duplicateEntries, Color(0xFFFF9800))
                    }
                    if (report.orphanedEntries > 0) {
                        IssueRow("Orphaned entries", report.orphanedEntries, Color(0xFF9E9E9E))
                    }

                    if (report.issuesFixed > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "✓ ${report.issuesFixed} issues fixed",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = onFix,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Build, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Fix ${report.totalIssues} Issues")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCheck,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !report.isChecking
            ) {
                Icon(Icons.Default.Search, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (report.isComplete) "Re-check" else "Verify Data")
            }
        }
    }
}

@Composable
private fun IssueRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun CleanupByAgeDialog(
    selectedAge: CleanupAge?,
    preview: CleanupPreview?,
    isClearing: Boolean,
    onSelectAge: (CleanupAge) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isClearing) onDismiss() },
        icon = { Icon(Icons.Default.DateRange, null) },
        title = { Text("Clear Old Data", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Delete history entries older than:",
                    style = MaterialTheme.typography.bodyMedium
                )

                CleanupAge.entries.forEach { age ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !isClearing) { onSelectAge(age) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAge == age,
                            onClick = { onSelectAge(age) },
                            enabled = !isClearing
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(age.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                preview?.let { p ->
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (p.entriesAffected > 0)
                            Color(0xFFFF9800).copy(alpha = 0.1f)
                        else
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                if (p.entriesAffected > 0)
                                    "${p.entriesAffected} entries will be deleted"
                                else
                                    "No entries to delete for this period",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (p.entriesAffected > 0) {
                                Text(
                                    "~${StorageInfo.formatBytes(p.spaceFreed)} freed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                if (isClearing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Clearing...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedAge != null && (preview?.entriesAffected ?: 0) > 0 && !isClearing,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isClearing) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun CleanupByCalculatorDialog(
    selectedTypes: Set<CalculatorType>,
    isClearing: Boolean,
    onToggleType: (CalculatorType) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isClearing) onDismiss() },
        icon = { Icon(Icons.Default.Calculate, null) },
        title = { Text("Clear by Calculator", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Select calculators to clear history for:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))

                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        CalculatorType.entries.forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = !isClearing) { onToggleType(type) }
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = type in selectedTypes,
                                    onCheckedChange = { onToggleType(type) },
                                    enabled = !isClearing
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(type.emoji)
                                Spacer(Modifier.width(6.dp))
                                Text(type.displayName, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (isClearing) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedTypes.isNotEmpty() && !isClearing,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) { Text("Delete Selected") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isClearing) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun DeleteEverythingDialog(
    step: Int,
    confirmText: String,
    isDeleting: Boolean,
    onConfirmTextChange: (String) -> Unit,
    onNextStep: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        icon = {
            Icon(
                Icons.Default.DeleteForever,
                null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                when (step) {
                    0 -> "Delete Everything?"
                    1 -> "Are you absolutely sure?"
                    else -> "Final Confirmation"
                },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                when (step) {
                    0 -> {
                        Text("This will permanently delete:")
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "📊 All calculation history",
                            "👤 Your profile data",
                            "⚙️ All settings and preferences",
                            "🏆 All achievements and streaks",
                            "💾 All local backups",
                            "📁 All exported files"
                        ).forEach { item ->
                            Text(
                                item,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "The app will return to its initial state as if freshly installed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    1 -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    Icons.Default.Warning, null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "This action CANNOT be undone. There is no way to recover your data after deletion.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Type DELETE to confirm:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmText,
                            onValueChange = onConfirmTextChange,
                            placeholder = { Text("Type DELETE here") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }

                if (isDeleting) {
                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Deleting all data...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (step) {
                0 -> {
                    Button(
                        onClick = onNextStep,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Yes, Continue") }
                }
                1 -> {
                    Button(
                        onClick = onConfirmDelete,
                        enabled = confirmText.uppercase() == "DELETE" && !isDeleting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete Everything", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
