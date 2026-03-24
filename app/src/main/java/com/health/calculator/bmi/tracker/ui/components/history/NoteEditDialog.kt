package com.health.calculator.bmi.tracker.ui.components.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NoteEditDialog(
    currentNote: String?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf(currentNote ?: "") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.StickyNote2, contentDescription = null)
        },
        title = {
            Text(
                text = if (currentNote.isNullOrBlank()) "Add Note" else "Edit Note",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { if (it.length <= 200) noteText = it },
                placeholder = { Text("e.g., After morning walk, Fasting reading...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                supportingText = {
                    Text("${noteText.length}/200")
                }
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(noteText.trim()) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DeleteConfirmDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete ${if (count == 1) "Entry" else "$count Entries"}?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (count == 1)
                    "This action cannot be undone. The history entry will be permanently removed."
                else
                    "This action cannot be undone. $count history entries will be permanently removed."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
