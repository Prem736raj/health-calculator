package com.health.calculator.bmi.tracker.ui.screens.aicoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.ai.GeminiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val geminiHelper: GeminiHelper
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hi there! I'm your AI Health Coach. Ask me anything about your diet, workouts, or health stats.", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Add user message
        _messages.value = _messages.value + ChatMessage(text, isUser = true)
        
        // Add empty AI message that is loading
        _messages.value = _messages.value + ChatMessage("", isUser = false, isLoading = true)
        _isTyping.value = true

        viewModelScope.launch {
            try {
                // Build a prompt that tells the AI to act as a health coach
                val systemPrompt = """
                    You are a friendly, professional AI Health Coach. 
                    Keep your answers concise, encouraging, and easy to read on a mobile device.
                    User asks: $text
                """.trimIndent()

                var responseText = ""
                geminiHelper.generateContentStream(systemPrompt).collect { chunk ->
                    responseText += chunk
                    
                    // Update the last message (AI response) with new text chunk and remove loading state
                    val currentList = _messages.value.toMutableList()
                    val lastIndex = currentList.lastIndex
                    currentList[lastIndex] = currentList[lastIndex].copy(
                        text = responseText, 
                        isLoading = false
                    )
                    _messages.value = currentList
                }
            } catch (e: Exception) {
                val currentList = _messages.value.toMutableList()
                val lastIndex = currentList.lastIndex
                currentList[lastIndex] = currentList[lastIndex].copy(
                    text = "Oops, I encountered an error. Please try again later.", 
                    isLoading = false
                )
                _messages.value = currentList
            } finally {
                _isTyping.value = false
            }
        }
    }
}
