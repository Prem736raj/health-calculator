package com.health.calculator.bmi.tracker.ui.components.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.GroupedHistoryEntries

@Composable
fun HistoryList(
    groupedEntries: List<GroupedHistoryEntries>,
    isSelectionMode: Boolean,
    selectedIds: Set<Long>,
    onEntryClick: (Long) -> Unit,
    onEntryLongClick: (Long) -> Unit,
    onNoteClick: (Long, String?) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onExportClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedEntries.forEach { group ->
            item(key = "header_${group.dateGroup.label}") {
                HistoryDateHeader(
                    dateGroup = group.dateGroup,
                    entryCount = group.entries.size,
                    isCollapsed = group.isCollapsed,
                    onToggleCollapse = {}
                )
            }

            if (!group.isCollapsed) {
                items(group.entries, key = { it.id }) { entry ->
                    HistoryEntryCard(
                        entry = entry.copy(isSelected = entry.id in selectedIds),
                        isInSelectionMode = isSelectionMode,
                        onTap = { onEntryClick(entry.id) },
                        onLongPress = { onEntryLongClick(entry.id) },
                        onShare = onExportClick,
                        onDelete = { onDeleteClick(entry.id) },
                        onEditNote = { onNoteClick(entry.id, entry.note) },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
