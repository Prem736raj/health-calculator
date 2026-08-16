package com.health.calculator.bmi.tracker.ui.screens.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.R

@Composable
fun ExportDataScreen(
    onNavigateBack: () -> Unit,
    onOpenHistoryExport: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenDataManagement: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.export_backup_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(id = R.string.export_backup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onOpenHistoryExport) {
                Text(stringResource(id = R.string.history_export_button))
            }
            TextButton(onClick = onOpenBackup) {
                Text(stringResource(id = R.string.backup_restore_button))
            }
            TextButton(onClick = onOpenDataManagement) {
                Text(stringResource(id = R.string.data_management_button))
            }
            TextButton(onClick = onNavigateBack) {
                Text(stringResource(id = R.string.go_back_button))
            }
        }
    }
}
