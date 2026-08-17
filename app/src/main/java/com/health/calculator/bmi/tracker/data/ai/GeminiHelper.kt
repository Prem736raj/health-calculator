package com.health.calculator.bmi.tracker.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.health.calculator.bmi.tracker.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiHelper @Inject constructor() {

    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    // Using gemini-1.5-flash which is fast and often free (within limits)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    fun generateContentStream(prompt: String): Flow<String> = flow {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            emit("Error: Please set your Gemini API key in local.properties as GEMINI_API_KEY=... and rebuild.")
            return@flow
        }
        
        try {
            generativeModel.generateContentStream(prompt).collect { response ->
                response.text?.let { emit(it) }
            }
        } catch (e: Exception) {
            emit("\n[Error processing request: ${e.message}]")
        }
    }
}
