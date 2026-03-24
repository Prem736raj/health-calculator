// ui/screens/bloodpressure/BpExportScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BpExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: BpExportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export & Share", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ExportStatItem(
                        value = "${uiState.totalReadings}",
                        label = "Total Readings",
                        icon = Icons.Outlined.MonitorHeart
                    )
                    ExportStatItem(
                        value = uiState.dateRange,
                        label = "Date Range",
                        icon = Icons.Outlined.DateRange
                    )
                }
            }

            // Export options
            Text(
                "Full Report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // PDF Report
            ExportOptionCard(
                icon = Icons.Outlined.PictureAsPdf,
                iconColor = Color(0xFFE53935),
                title = "PDF Report",
                description = "Complete report with all readings, averages, and distribution. Perfect for personal records.",
                buttonText = "Generate PDF",
                isLoading = uiState.isGeneratingPdf,
                isEnabled = uiState.totalReadings > 0,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onExportPdf()
                }
            )

            // CSV Export
            ExportOptionCard(
                icon = Icons.Outlined.TableChart,
                iconColor = Color(0xFF4CAF50),
                title = "CSV Spreadsheet",
                description = "Raw data in spreadsheet format. Import into Excel, Google Sheets, or other tools.",
                buttonText = "Export CSV",
                isLoading = uiState.isGeneratingCsv,
                isEnabled = uiState.totalReadings > 0,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onExportCsv()
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Doctor Report
            Text(
                "For Your Doctor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ExportOptionCard(
                icon = Icons.Outlined.LocalHospital,
                iconColor = Color(0xFF1E88E5),
                title = "Doctor-Ready Report",
                description = "Professional medical report with last 30 readings, morning/evening averages, medication status, and clinical formatting. Print-friendly layout.",
                buttonText = "Generate Doctor Report",
                isLoading = uiState.isGeneratingDoctorReport,
                isEnabled = uiState.totalReadings > 0,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onExportDoctorReport()
                },
                isPrimary = true
            )

            // Doctor report info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Text(
                        "The doctor report includes your last 30 readings, morning vs evening comparison, medication history, and is formatted for easy clinical review. Show this to your healthcare provider during your next visit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1565C0).copy(alpha = 0.8f)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Share Last Reading
            Text(
                "Share Reading",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (uiState.hasLatestReading) {
                // Share as text
                ExportOptionCard(
                    icon = Icons.Outlined.TextSnippet,
                    iconColor = Color(0xFF7B1FA2),
                    title = "Share as Text",
                    description = "Share your latest reading (${uiState.latestReadingText}) as formatted text message.",
                    buttonText = "Share Text",
                    isLoading = false,
                    isEnabled = true,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onShareLatestAsText()
                    }
                )

                // Share as image
                ExportOptionCard(
                    icon = Icons.Outlined.Image,
                    iconColor = Color(0xFFFF6F00),
                    title = "Share as Image",
                    description = "Share your latest reading as a beautifully styled card image.",
                    buttonText = "Share Image",
                    isLoading = uiState.isGeneratingImage,
                    isEnabled = true,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.onShareLatestAsImage()
                    }
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "No readings to share yet. Take a BP reading first!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Success snackbar
    if (uiState.showSuccess) {
        val context = LocalContext.current
        LaunchedEffect(uiState.showSuccess) {
            viewModel.dismissSuccess()
        }
    }
}

@Composable
private fun ExportStatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ExportOptionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    buttonText: String,
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }

            if (isPrimary) {
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEnabled && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = iconColor
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(buttonText, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEnabled && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = iconColor
                    ),
                    border = BorderStroke(1.dp, iconColor.copy(alpha = 0.3f))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = iconColor,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...", fontWeight = FontWeight.Medium)
                    } else {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(buttonText, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
