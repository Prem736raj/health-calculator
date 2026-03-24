package com.health.calculator.bmi.tracker.ui.screens.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.health.calculator.bmi.tracker.data.backup.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.backupState.collectAsState()
    val showRestoreConfirm by viewModel.showRestoreConfirm.collectAsState()
    val backupToRestore by viewModel.backupToRestore.collectAsState()
    val restoreMode by viewModel.restoreMode.collectAsState()
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            // How to handle file picked from system?
            // Currently, we'll just show a generic "Importing..." state
            // and maybe suggest moving to appropriate folder first? 
            // Or just restore directly.
            // For now, let's assume we can restore directly.
            viewModel.setRestoreMode(RestoreMode.MERGE) // Default to merge for external files
            //viewModel.restoreFromLocal(it, RestoreMode.MERGE)
        }
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.initDrive { success ->
            if (success) {
                viewModel.fetchDriveBackups()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Backup Actions
            item {
                BackupActionsSection(
                    isBackingUp = state.isBackingUp,
                    progress = state.progress,
                    statusMessage = state.statusMessage,
                    onCreateLocal = { viewModel.createLocalBackup() },
                    onCloudBackup = {
                        if (GoogleDriveBackupManager.getInstance(context).isSignedIn()) {
                            viewModel.backupToGoogleDrive()
                        } else {
                            signInLauncher.launch(GoogleDriveBackupManager.getInstance(context).getSignInIntent())
                        }
                    },
                    onDeviceTransfer = { viewModel.generateTransferQr() }
                )
            }

            // Auto-Backup Settings
            item {
                AutoBackupSection(
                    enabled = state.autoBackupEnabled,
                    frequency = state.autoBackupFrequency,
                    wifiOnly = state.wifiOnlyBackup,
                    onToggleEnabled = { viewModel.toggleAutoBackup(it) },
                    onFrequencyChange = { viewModel.updateAutoBackupFrequency(it) },
                    onWifiToggle = { viewModel.updateWifiOnly(it) }
                )
            }

            // Available Backups Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Backups",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TextButton(onClick = { 
                        if (GoogleDriveBackupManager.getInstance(context).isSignedIn()) {
                            viewModel.fetchDriveBackups()
                        }
                        viewModel.resetState() // Refresh local
                    }) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Refresh")
                    }
                }
            }

            // Backups List
            if (state.availableBackups.isEmpty()) {
                item {
                    EmptyBackupsPlaceholder()
                }
            } else {
                items(state.availableBackups) { backup ->
                    BackupItemCard(
                        backup = backup,
                        onClick = { viewModel.restoreFromBackup(backup) },
                        onDelete = { viewModel.deleteBackup(backup) }
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }

        // Restore Confirmation Dialog
        if (showRestoreConfirm && backupToRestore != null) {
            RestoreConfirmDialog(
                backup = backupToRestore!!,
                currentMode = restoreMode,
                onModeChange = { viewModel.setRestoreMode(it) },
                onConfirm = { viewModel.confirmRestore() },
                onCancel = { viewModel.cancelRestore() }
            )
        }

        // QR Transfer Dialog
        if (state.showTransferQr && state.transferQrContent != null) {
            TransferQrDialog(
                qrContent = state.transferQrContent!!,
                onClose = { viewModel.hideTransferQr() }
            )
        }

        // Universal Progress Overlay
        if (state.isBackingUp || state.isRestoring) {
            ProgressOverlay(
                title = if (state.isBackingUp) "Backing Up..." else "Restoring...",
                progress = state.progress,
                statusMessage = state.statusMessage
            )
        }
    }
}

@Composable
fun BackupActionsSection(
    isBackingUp: Boolean,
    progress: Float,
    statusMessage: String,
    onCreateLocal: () -> Unit,
    onCloudBackup: () -> Unit,
    onDeviceTransfer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Backup, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Manual Backup", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BackupActionButton(
                    icon = Icons.Default.SdCard,
                    label = "Local",
                    modifier = Modifier.weight(1f),
                    onClick = onCreateLocal
                )
                BackupActionButton(
                    icon = Icons.Default.CloudUpload,
                    label = "Cloud",
                    modifier = Modifier.weight(1f),
                    onClick = onCloudBackup
                )
                BackupActionButton(
                    icon = Icons.Default.QrCode,
                    label = "Transfer",
                    modifier = Modifier.weight(1f),
                    onClick = onDeviceTransfer
                )
            }
        }
    }
}

@Composable
fun BackupActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun AutoBackupSection(
    enabled: Boolean,
    frequency: BackupFrequency,
    wifiOnly: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onFrequencyChange: (BackupFrequency) -> Unit,
    onWifiToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Autorenew, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Auto Backup", fontWeight = FontWeight.Bold)
                    Text("Keep your data synced automatically", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = enabled, onCheckedChange = onToggleEnabled)
            }

            AnimatedVisibility(visible = enabled) {
                Column {
                    Divider(Modifier.padding(vertical = 12.dp))
                    
                    Text("Backup Frequency", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BackupFrequency.values().forEach { freq ->
                            FilterChip(
                                selected = frequency == freq,
                                onClick = { onFrequencyChange(freq) },
                                label = { Text(freq.label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Backup over Wi-Fi only", style = MaterialTheme.typography.bodyMedium)
                        Checkbox(checked = wifiOnly, onCheckedChange = onWifiToggle)
                    }
                }
            }
        }
    }
}

@Composable
fun BackupItemCard(
    backup: BackupMetadata,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (backup.source == BackupSource.LOCAL)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (backup.source == BackupSource.LOCAL) Icons.Default.SdStorage else Icons.Default.Cloud,
                    null,
                    tint = if (backup.source == BackupSource.LOCAL)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(backup.formattedDate, fontWeight = FontWeight.Medium)
                Text(
                    "${backup.formattedSize} • ${backup.entryCount} entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, null)
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Restore") },
                        onClick = { showMenu = false; onClick() },
                        leadingIcon = { Icon(Icons.Default.Restore, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBackupsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.History,
            null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No backups found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Create your first backup above",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RestoreConfirmDialog(
    backup: BackupMetadata,
    currentMode: RestoreMode,
    onModeChange: (RestoreMode) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Confirm Restore") },
        text = {
            Column {
                Text("Restore data from ${backup.formattedDate}?")
                Spacer(Modifier.height(16.dp))
                
                Text("Restore Mode:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RestoreMode.values().forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onModeChange(mode) }
                                .background(if (currentMode == mode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .padding(8.dp)
                        ) {
                            RadioButton(selected = currentMode == mode, onClick = { onModeChange(mode) })
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(mode.label, fontWeight = FontWeight.Bold)
                                Text(mode.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (currentMode == RestoreMode.REPLACE) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "WARNING: This will delete all current data on this device and replace it with backup data.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentMode == RestoreMode.REPLACE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}

@Composable
fun TransferQrDialog(
    qrContent: String,
    onClose: () -> Unit
) {
    val qrManager = QrTransferManager.getInstance(LocalContext.current)
    val qrBitmap = remember(qrContent) { qrManager.generateQrBitmap(qrContent) }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Device-to-Device Transfer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Scan this code on your new device to transfer your health data.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(24.dp))
                
                androidx.compose.foundation.Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Transfer QR Code",
                    modifier = Modifier.size(240.dp).clip(RoundedCornerShape(8.dp))
                )
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun ProgressOverlay(
    title: String,
    progress: Float,
    statusMessage: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                )
                
                Spacer(Modifier.height(12.dp))
                Text(statusMessage, style = MaterialTheme.typography.bodyMedium)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
