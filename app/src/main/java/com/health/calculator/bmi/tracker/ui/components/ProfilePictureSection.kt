package com.health.calculator.bmi.tracker.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ProfilePictureSection(
    profilePictureUri: String?,
    initials: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { onEditClick() },
            contentAlignment = Alignment.Center
        ) {
            if (profilePictureUri != null && profilePictureUri.isNotBlank()) {
                AsyncImage(
                    model = profilePictureUri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initials.uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Edit overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp, start = 16.dp, end = 16.dp, top = 8.dp)
        ) {
            Text(
                text = "Profile Picture",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ListItem(
                headlineContent = { Text("Take a photo") },
                leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                modifier = Modifier.clickable { onCameraClick() }
            )
            ListItem(
                headlineContent = { Text("Choose from gallery") },
                leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                modifier = Modifier.clickable { onGalleryClick() }
            )
            ListItem(
                headlineContent = { Text("Remove current picture", color = MaterialTheme.colorScheme.error) },
                leadingContent = { Icon(Icons.Default.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { 
                    onImageSelected(null)
                    onDismiss()
                }
            )
        }
    }
}
