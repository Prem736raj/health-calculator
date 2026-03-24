package com.health.calculator.bmi.tracker.ui.components.history

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BatchOperationsBar(
    selectedCount: Int,
    totalCount: Int,
    isVisible: Boolean,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onExportSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(tween(200)) { -it } + fadeIn(tween(200)),
        exit = slideOutVertically(tween(200)) { -it } + fadeOut(tween(200)),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Close / deselect all
                    IconButton(onClick = onDeselectAll, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Close,
                            "Cancel selection",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "$selectedCount selected",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Select all
                    if (selectedCount < totalCount) {
                        TextButton(
                            onClick = onSelectAll,
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                "Select All",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Export selected
                    IconButton(onClick = onExportSelected, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.FileDownload,
                            "Export selected",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete selected
                    IconButton(onClick = onDeleteSelected, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            "Delete selected",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
