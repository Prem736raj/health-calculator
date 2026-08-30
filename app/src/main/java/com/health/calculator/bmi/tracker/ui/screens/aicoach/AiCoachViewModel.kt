package com.health.calculator.bmi.tracker.ui.screens.aicoach

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.R
import com.health.calculator.bmi.tracker.data.ai.GeminiHelper
import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore
import com.health.calculator.bmi.tracker.data.local.dao.ChatDao
import com.health.calculator.bmi.tracker.data.local.entity.ChatMessageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val geminiHelper: GeminiHelper,
    private val chatDao: ChatDao,
    private val settingsDataStore: SettingsDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isDisclosureAccepted: StateFlow<Boolean> = settingsDataStore.aiDisclosureAcceptedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        viewModelScope.launch {
            chatDao.getAllMessages().collectLatest { entities ->
                if (entities.isEmpty()) {
                    val welcomeMsg = ChatMessage(context.getString(R.string.ai_coach_welcome), isUser = false)
                    _messages.value = listOf(welcomeMsg)
                    chatDao.insertMessage(ChatMessageEntity(text = welcomeMsg.text, isUser = welcomeMsg.isUser))
                } else {
                    _messages.value = entities.map { ChatMessage(it.text, it.isUser) }
                }
            }
        }
    }

    fun acceptDisclosure() {
        viewModelScope.launch {
            settingsDataStore.setAiDisclosureAccepted(true)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            chatDao.insertMessage(ChatMessageEntity(text = text, isUser = true))
            
            // The UI will update automatically because of collectLatest above, but we also want to show a loading bubble
            _messages.value = _messages.value + ChatMessage("", isUser = false, isLoading = true)
            _isTyping.value = true

            try {
                var responseText = ""
                geminiHelper.generateContentStream(text).collect { chunk ->
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
                
                // Once stream finishes, save the final response to database
                chatDao.insertMessage(ChatMessageEntity(text = responseText, isUser = false))
            } catch (e: Exception) {
                val currentList = _messages.value.toMutableList()
                val lastIndex = currentList.lastIndex
                currentList[lastIndex] = currentList[lastIndex].copy(
                    text = context.getString(R.string.ai_coach_error), 
                    isLoading = false
                )
                _messages.value = currentList
            } finally {
                _isTyping.value = false
            }
        }
    }
}
