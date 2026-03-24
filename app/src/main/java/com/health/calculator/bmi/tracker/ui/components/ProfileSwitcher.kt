package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.FamilyProfile

/**
 * A horizontal bar of mini-avatars displayed at the top of the profile screen.
 * Allows quick switching between family members.
 */
@Composable
fun ProfileSwitcherBar(
    profiles: List<FamilyProfile>,
    activeProfileId: String?,
    onProfileClick: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(profiles) { profile ->
            val isActive = profile.profileId == activeProfileId
            ProfileAvatar(
                profile = profile,
                isActive = isActive,
                onClick = { onProfileClick(profile.profileId) }
            )
        }

        if (profiles.size < FamilyProfile.MAX_PROFILES) {
            item {
                AddProfileButton(onClick = onAddClick)
            }
        }
    }
}

@Composable
fun ProfileAvatar(
    profile: FamilyProfile,
    isActive: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(profile.profileColor))
                .then(
                    if (isActive) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.initials,
                color = Color.White,
                fontSize = (size.value / 2.5f).sp,
                fontWeight = FontWeight.Bold
            )
            
            if (isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(size / 3f)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(size / 4.5f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = profile.displayName.take(8),
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
fun AddProfileButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Profile",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Add",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Bottom sheet content for managing profiles.
 */
@Composable
fun ProfileSwitcherSheet(
    profiles: List<FamilyProfile>,
    activeProfileId: String?,
    onProfileSelect: (String) -> Unit,
    onAddProfile: () -> Unit,
    onDeleteProfile: (FamilyProfile) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Switch Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        profiles.forEach { profile ->
            ProfileRowItem(
                profile = profile,
                isActive = profile.profileId == activeProfileId,
                onSelect = { onProfileSelect(profile.profileId) },
                onDelete = { onDeleteProfile(profile) }
            )
        }

        if (profiles.size < FamilyProfile.MAX_PROFILES) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onAddProfile,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Family Member")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileRowItem(
    profile: FamilyProfile,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isActive) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(profile.profileColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isActive) "Active Profile" else "Family Member",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isActive) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Profile",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
