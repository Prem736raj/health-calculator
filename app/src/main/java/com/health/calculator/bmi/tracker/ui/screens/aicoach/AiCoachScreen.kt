package com.health.calculator.bmi.tracker.ui.screens.aicoach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiCoachViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isTyping by viewModel.isTyping.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Health Coach") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = false,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }
            }

            // Input Area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask about your health...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        },
                        enabled = inputText.isNotBlank() && !isTyping,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (inputText.isNotBlank() && !isTyping) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isTyping) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val annotatedString = remember(message.text) { buildAnnotatedStringWithLinks(message.text) }
    val uriHandler = LocalUriHandler.current

    val bubbleColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    val alignment = if (message.isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (!message.isUser) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "AI",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (message.isUser) 20.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 20.dp
                        )
                    )
                    .background(bubbleColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (message.isLoading && message.text.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = textColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    SelectionContainer {
                        Text(
                            text = annotatedString,
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Extract first URL for a dedicated action button (from KI)
        val firstUrl = remember(message.text) {
            val matcher = java.util.regex.Pattern.compile("(ht|f)tp(s?):\\/\\/[^\\s]+").matcher(message.text)
            if (matcher.find()) matcher.group() else null
        }
        
        if (firstUrl != null && !message.isUser) {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { uriHandler.openUri(firstUrl) },
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text("Open Link", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
fun buildAnnotatedStringWithLinks(text: String) = buildAnnotatedString {
    append(text)
    val urlPattern = java.util.regex.Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)" +
        "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*" +
        "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        java.util.regex.Pattern.CASE_INSENSITIVE
    )
    val matcher = urlPattern.matcher(text)
    
    while (matcher.find()) {
        val matchStart = matcher.start(1)
        val matchEnd = matcher.end()
        val url = text.substring(matchStart, matchEnd)
        addUrlAnnotation(UrlAnnotation(url), start = matchStart, end = matchEnd)
        addStyle(
            style = SpanStyle(textDecoration = TextDecoration.Underline),
            start = matchStart,
            end = matchEnd
        )
    }
}
