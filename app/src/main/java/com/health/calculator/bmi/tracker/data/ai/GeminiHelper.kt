package com.health.calculator.bmi.tracker.data.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiHelper @Inject constructor(
    @ApplicationContext context: Context
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager?.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /*
     * IMPORTANT:
     *
     * AI Coach is informational only.
     * It must not diagnose diseases,
     * prescribe medication,
     * modify medication,
     * or replace professional care.
     */

    private val model by lazy {

        Firebase
            .ai(
                backend = GenerativeBackend.googleAI()
            )
            .generativeModel(
                modelName = "gemini-3.7-flash",

                systemInstruction = content {

                    text(
                        """
                        You are the AI Wellness Assistant inside a consumer
                        health and wellness tracking application.

                        Your purpose is educational wellness guidance only.

                        RULES:

                        1. Never claim to diagnose a disease or medical condition.

                        2. Never claim that calculator or tracker results prove
                           that the user has or does not have a disease.

                        3. Never prescribe prescription medication.

                        4. Never instruct a user to start, stop, increase,
                           decrease, or replace prescribed medication.

                        5. Do not interpret emergency symptoms as safe.

                        6. When the user describes potentially serious or
                           emergency symptoms, encourage them to seek immediate
                           professional medical help or local emergency services.

                        7. Clearly distinguish:
                           - general wellness information
                           - calculator estimates
                           - medical diagnosis

                        8. Never fabricate medical measurements, laboratory
                           values, sources, or guidelines.

                        9. Do not tell a user that they are "healthy" merely
                           because an app score or calculator result is normal.

                        10. Encourage consultation with a qualified healthcare
                            professional when a user's question requires
                            individualized medical assessment.

                        11. Keep answers concise and easy to understand on a
                            mobile screen.

                        12. You may explain general topics such as:
                            - BMI
                            - BMR
                            - hydration
                            - nutrition
                            - exercise
                            - sleep
                            - heart-rate zones
                            - general healthy lifestyle practices

                        13. Never present yourself as a doctor.

                        Always communicate uncertainty appropriately.
                        """.trimIndent()
                    )
                }
            )
    }

    fun generateContentStream(
        prompt: String
    ): Flow<String> = flow {

        if (prompt.isBlank()) {
            return@flow
        }

        try {

            model
                .generateContentStream(prompt)
                .collect { chunk ->

                    chunk.text?.let { text ->

                        if (text.isNotBlank()) {
                            emit(text)
                        }
                    }
                }

        } catch (e: Exception) {

            if (e is CancellationException) throw e
            throw AiCoachException(
                message = "AI service is temporarily unavailable.",
                cause = e,
                reason = AiCoachFailureReason.from(e)
            )
        }
    }
}

enum class AiCoachFailureReason {
    NETWORK,
    RATE_LIMITED,
    SERVICE_UNAVAILABLE,
    UNKNOWN;

    companion object {
        fun from(error: Throwable): AiCoachFailureReason {
            val message = error.message.orEmpty().lowercase()
            return when {
                listOf("429", "quota", "rate limit", "resource exhausted").any(message::contains) -> RATE_LIMITED
                listOf("network", "timeout", "unable to resolve", "offline", "connect").any(message::contains) -> NETWORK
                listOf("unavailable", "server", "internal", "503", "500").any(message::contains) -> SERVICE_UNAVAILABLE
                else -> UNKNOWN
            }
        }
    }
}

class AiCoachException(
    message: String,
    cause: Throwable? = null,
    val reason: AiCoachFailureReason = AiCoachFailureReason.UNKNOWN
) : Exception(message, cause)
