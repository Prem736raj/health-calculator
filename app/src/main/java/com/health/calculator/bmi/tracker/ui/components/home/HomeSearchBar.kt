package com.health.calculator.bmi.tracker.ui.components.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.health.calculator.bmi.tracker.data.model.SearchResult
import com.health.calculator.bmi.tracker.data.model.SearchResultType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<SearchResult>,
    recentSearches: List<String>,
    onClearRecent: () -> Unit,
    onRemoveRecent: (String) -> Unit,
    onResultClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        SearchTextField(
            query = query,
            onQueryChange = {
                onQueryChange(it)
                isExpanded = it.isNotEmpty() || recentSearches.isNotEmpty()
            },
            onFocusChange = { focused ->
                if (focused) isExpanded = true
            },
            onClear = {
                onQueryChange("")
                isExpanded = recentSearches.isNotEmpty()
            },
            focusRequester = focusRequester
        )

        if (isExpanded) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        if (query.isEmpty() && recentSearches.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Recent Searches", style = MaterialTheme.typography.titleSmall)
                                    TextButton(onClick = onClearRecent) {
                                        Text("Clear All")
                                    }
                                }
                            }
                            items(recentSearches) { recent ->
                                RecentSearchItem(
                                    text = recent,
                                    onClick = { onQueryChange(recent) },
                                    onRemove = { onRemoveRecent(recent) }
                                )
                            }
                        } else if (results.isNotEmpty()) {
                            items(results) { result ->
                                SearchResultItem(
                                    result = result,
                                    onClick = {
                                        onResultClick(result)
                                        isExpanded = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        } else if (query.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No results for \"$query\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search health tools, terms...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { state -> onFocusChange(state.isFocused) }
    )
}

@Composable
fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(result.title, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(result.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(result.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        trailingContent = {
            Badge(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(result.type.label, modifier = Modifier.padding(horizontal = 4.dp))
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun RecentSearchItem(
    text: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
        trailingContent = {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove")
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}

