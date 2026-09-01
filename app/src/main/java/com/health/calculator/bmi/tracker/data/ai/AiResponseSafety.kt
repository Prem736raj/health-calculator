package com.health.calculator.bmi.tracker.data.ai

/** Deterministic last-line protection for model output shown in the chat UI. */
object AiResponseSafety {
    private const val SAFE_FALLBACK =
        "I can share general wellness information, but I cannot diagnose a condition or tell you to start, stop, or change medication. Please discuss personal medical decisions with a qualified healthcare professional."

    private val medicationAction = Regex(
        "\\b(start|stop|increase|decrease|change|double|skip|replace)\\b.{0,80}\\b(medication|medicine|dose|prescription|tablet|pill|mg)\\b",
        RegexOption.IGNORE_CASE
    )
    private val diagnosisCertainty = Regex(
        "\\b(you (have|may have|might have|likely have|probably have|do not have|don't have)|you are (diabetic|hypertensive)|this (is|looks like|proves|means)|diagnosed with)\\b.{0,100}\\b(diabetes|hypertension|cancer|disease|condition|disorder|infection)\\b",
        RegexOption.IGNORE_CASE
    )

    fun sanitize(text: String, potentiallyUrgent: Boolean = false): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return SAFE_FALLBACK
        if (medicationAction.containsMatchIn(trimmed) || diagnosisCertainty.containsMatchIn(trimmed)) {
            return SAFE_FALLBACK
        }

        if (potentiallyUrgent && !containsEscalation(trimmed)) {
            return "$trimmed\n\nIf this may be an emergency or symptoms are severe or worsening, contact local emergency services or urgent medical care now."
        }
        return trimmed
    }

    /** Used while streaming so an unsafe actionable sentence is never rendered verbatim. */
    fun sanitizeStreaming(text: String): String =
        if (medicationAction.containsMatchIn(text) || diagnosisCertainty.containsMatchIn(text)) SAFE_FALLBACK else text

    private fun containsEscalation(text: String): Boolean {
        val lower = text.lowercase()
        return listOf("emergency services", "urgent medical", "immediate medical", "seek medical", "call your local").any {
            lower.contains(it)
        }
    }
}
