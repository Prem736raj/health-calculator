package com.health.calculator.bmi.tracker.ui.screens.aicoach

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.R
import com.health.calculator.bmi.tracker.data.ai.AiCoachException
import com.health.calculator.bmi.tracker.data.ai.AiCoachFailureReason
import com.health.calculator.bmi.tracker.data.ai.AiPromptPolicy
import com.health.calculator.bmi.tracker.data.ai.AiResponseSafety
import com.health.calculator.bmi.tracker.data.ai.AiWellnessContextBuilder
import com.health.calculator.bmi.tracker.data.ai.GeminiHelper
import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore
import com.health.calculator.bmi.tracker.data.local.dao.ChatDao
import com.health.calculator.bmi.tracker.data.local.entity.ChatMessageEntity
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository
import com.health.calculator.bmi.tracker.data.repository.WeightRepository
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalytics
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalyticsEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.CompletableDeferred
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val geminiHelper: GeminiHelper,
    private val chatDao: ChatDao,
    private val settingsDataStore: SettingsDataStore,
    private val weightRepository: WeightRepository,
    private val waterIntakeRepository: WaterIntakeRepository,
    private val productAnalytics: ProductAnalytics,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isDisclosureAccepted: StateFlow<Boolean> = settingsDataStore.aiDisclosureAcceptedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isContextSharingEnabled: StateFlow<Boolean> = settingsDataStore.aiContextSharingEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    private val _canRetry = MutableStateFlow(false)
    val canRetry: StateFlow<Boolean> = _canRetry.asStateFlow()

    private var lastRequestMillis: Long? = null
    private var lastFailedPrompt: String? = null
    private val messagesLoaded = CompletableDeferred<Unit>()
    private var activeRequest: Job? = null

    init {
        productAnalytics.track(
            ProductAnalyticsEvent.AI_ASSISTANT_OPENED,
            mapOf("entry_point" to "insights")
        )
        viewModelScope.launch {
            // Load the persisted conversation once. A live Room collector used
            // to overwrite the transient streaming bubble whenever the user or
            // assistant message was inserted, which could duplicate bubbles or
            // reset partial streaming text. This screen is the sole writer;
            // updates are applied explicitly below and remain process-safe via
            // Room persistence.
            try {
                val entities = chatDao.getAllMessages().first()
                if (entities.isEmpty()) {
                    val welcomeMsg = ChatMessage(context.getString(R.string.ai_coach_welcome), isUser = false)
                    _messages.value = listOf(welcomeMsg)
                    chatDao.insertMessage(ChatMessageEntity(text = welcomeMsg.text, isUser = welcomeMsg.isUser))
                } else {
                    _messages.value = entities.map { ChatMessage(it.text, it.isUser) }
                }
            } finally {
                messagesLoaded.complete(Unit)
            }
        }
    }

    fun acceptDisclosure() {
        viewModelScope.launch {
            settingsDataStore.setAiDisclosureAccepted(true)
        }
    }

    fun setContextSharingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAiContextSharingEnabled(enabled)
        }
    }

    fun dismissNotice() {
        _notice.value = null
    }

    fun clearConversation() {
        activeRequest?.cancel()
        activeRequest = null
        _isTyping.value = false
        viewModelScope.launch {
            messagesLoaded.await()
            chatDao.clearHistory()
            val welcomeMsg = ChatMessage(context.getString(R.string.ai_coach_welcome), isUser = false)
            chatDao.insertMessage(ChatMessageEntity(text = welcomeMsg.text, isUser = false))
            _messages.value = listOf(welcomeMsg)
            _notice.value = null
            _canRetry.value = false
            lastFailedPrompt = null
        }
    }

    /** Returns false when the message was rejected before any network call. */
    fun sendMessage(text: String): Boolean {
        val validation = AiPromptPolicy.validate(
            rawText = text,
            nowMillis = System.currentTimeMillis(),
            lastRequestMillis = lastRequestMillis,
            isBusy = _isTyping.value
        )
        if (!validation.accepted) {
            _notice.value = validation.message
            _canRetry.value = false
            return false
        }

        lastRequestMillis = System.currentTimeMillis()
        _notice.value = null
        _canRetry.value = false
        lastFailedPrompt = null
        // Reserve the in-flight slot before waiting for the initial Room load;
        // two taps during startup must not create concurrent model requests.
        _isTyping.value = true

        activeRequest = viewModelScope.launch {
            messagesLoaded.await()
            val priorMessages = _messages.value
                .filter { !it.isLoading && it.text.isNotBlank() }
                .takeLast(6)
                .map { it.isUser to it.text }
            val userMessage = ChatMessage(
                text = validation.normalizedText,
                isUser = true
            )
            chatDao.insertMessage(ChatMessageEntity(text = validation.normalizedText, isUser = true))
            
            // Render the user's bubble immediately, then keep the streaming
            // assistant bubble in UI state while the final response is written
            // to Room. This keeps the conversation honest during slow streams
            // and avoids relying on a later database emission for the bubble.
            _messages.value = _messages.value + userMessage + ChatMessage("", isUser = false, isLoading = true)

            try {
                if (!geminiHelper.isNetworkAvailable()) {
                    throw AiCoachException(
                        message = "No network connection.",
                        reason = AiCoachFailureReason.NETWORK
                    )
                }
                val prompt = if (isContextSharingEnabled.value) {
                    val optionalContext = buildOptionalContext()
                    AiPromptPolicy.buildModelPrompt(validation.normalizedText, optionalContext, priorMessages)
                } else {
                    AiPromptPolicy.buildModelPrompt(validation.normalizedText, recentDialogue = priorMessages)
                }
                var responseText = ""
                geminiHelper.generateContentStream(prompt).collect { chunk ->
                    responseText += chunk

                    updateAssistantBubble(
                        text = AiResponseSafety.sanitizeStreaming(responseText),
                        isLoading = false
                    )
                }

                val safeResponse = AiResponseSafety.sanitize(responseText, validation.potentiallyUrgent)
                updateAssistantBubble(text = safeResponse, isLoading = false)
                // Once stream finishes, save the final response to database
                chatDao.insertMessage(ChatMessageEntity(text = safeResponse, isUser = false))
                _canRetry.value = false
            } catch (e: CancellationException) {
                throw e
            } catch (e: AiCoachException) {
                lastFailedPrompt = validation.normalizedText
                _notice.value = failureMessage(e.reason)
                _canRetry.value = true
                updateAssistantBubble(
                    text = context.getString(R.string.ai_coach_error),
                    isLoading = false,
                    isError = true
                )
            } catch (_: Exception) {
                lastFailedPrompt = validation.normalizedText
                _notice.value = failureMessage(AiCoachFailureReason.UNKNOWN)
                _canRetry.value = true
                updateAssistantBubble(
                    text = context.getString(R.string.ai_coach_error),
                    isLoading = false,
                    isError = true
                )
            } finally {
                _isTyping.value = false
            }
        }
        return true
    }

    fun retryLastMessage(): Boolean = lastFailedPrompt?.let { sendMessage(it) } ?: false

    private suspend fun buildOptionalContext(): String {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val weights = weightRepository.getAllWeights().first()
        val waterLogs = waterIntakeRepository.getAllWaterLogs().first()
        val goal = waterIntakeRepository.getLatestCalculation()?.recommendedIntakeMl
        return AiWellnessContextBuilder
            .build(today, zone, weights, waterLogs, goal)
            .toPromptSection()
    }

    private fun updateAssistantBubble(text: String, isLoading: Boolean, isError: Boolean = false) {
        val currentList = _messages.value.toMutableList()
        val index = currentList.indexOfLast { !it.isUser && it.isLoading }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(
                text = text,
                isLoading = isLoading,
                isError = isError
            )
        } else if (currentList.lastOrNull()?.isUser == true) {
            currentList += ChatMessage(text = text, isUser = false, isLoading = isLoading, isError = isError)
        }
        _messages.value = currentList
    }

    private fun failureMessage(reason: AiCoachFailureReason): String = when (reason) {
        AiCoachFailureReason.NETWORK -> "You appear to be offline. Check your connection and try again."
        AiCoachFailureReason.RATE_LIMITED -> "The assistant is busy right now. Please try again in a little while."
        AiCoachFailureReason.SERVICE_UNAVAILABLE -> "The assistant is temporarily unavailable. Please try again later."
        AiCoachFailureReason.UNKNOWN -> "We couldn't get a safe reply. Please try again later."
    }
}
