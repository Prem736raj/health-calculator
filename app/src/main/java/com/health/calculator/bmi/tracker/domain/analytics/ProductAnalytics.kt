package com.health.calculator.bmi.tracker.domain.analytics

import java.util.Locale

/**
 * Product events describe behaviour, never a person's measurements.
 *
 * Keep this contract vendor-neutral so the app can change analytics providers
 * without changing feature code. Every parameter is deliberately allowlisted
 * and uses a small vocabulary in [ProductAnalyticsPolicy].
 */
enum class ProductAnalyticsEvent(
    val eventName: String,
    val allowedParameterKeys: Set<String> = emptySet()
) {
    APP_OPENED(
        eventName = "app_opened",
        allowedParameterKeys = setOf("launch_source")
    ),
    ONBOARDING_COMPLETED(eventName = "onboarding_completed"),
    ONBOARDING_ACTION_SELECTED(
        eventName = "onboarding_action_selected",
        allowedParameterKeys = setOf("action")
    ),
    SURFACE_OPENED(
        eventName = "surface_opened",
        allowedParameterKeys = setOf("surface")
    ),
    CALCULATOR_OPENED(
        eventName = "calculator_opened",
        allowedParameterKeys = setOf("calculator_id", "entry_point")
    ),
    CALCULATOR_COMPLETED(
        eventName = "calculator_completed",
        allowedParameterKeys = setOf("calculator_id", "entry_point")
    ),
    TRACKER_OPENED(
        eventName = "tracker_opened",
        allowedParameterKeys = setOf("tracker_type", "entry_point")
    ),
    WATER_LOGGED(
        eventName = "water_logged",
        allowedParameterKeys = setOf("source")
    ),
    WEIGHT_LOGGED(
        eventName = "weight_logged",
        allowedParameterKeys = setOf("source")
    ),
    HEALTH_CONNECT_CONNECTED(
        eventName = "health_connect_connected",
        allowedParameterKeys = setOf("permission_type")
    ),
    INSIGHT_OPENED(
        eventName = "insight_opened",
        allowedParameterKeys = setOf("insight_type")
    ),
    AI_ASSISTANT_OPENED(
        eventName = "ai_assistant_opened",
        allowedParameterKeys = setOf("entry_point")
    ),
    WEEKLY_REPORT_OPENED(
        eventName = "weekly_report_opened",
        allowedParameterKeys = setOf("report_type")
    ),
    REPORT_EXPORTED(
        eventName = "report_exported",
        allowedParameterKeys = setOf("report_type", "format")
    ),
    REMINDER_ENABLED(
        eventName = "reminder_enabled",
        allowedParameterKeys = setOf("reminder_type")
    )
}
/** Vendor-neutral analytics boundary used by feature code. */
interface ProductAnalytics {
    fun track(
        event: ProductAnalyticsEvent,
        parameters: Map<String, String> = emptyMap()
    )
}

/** Safe default for tests, offline mode, and builds without a configured backend. */
object NoOpProductAnalytics : ProductAnalytics {
    override fun track(
        event: ProductAnalyticsEvent,
        parameters: Map<String, String>
    ) = Unit
}

/**
 * Privacy boundary for product analytics.
 *
 * Unknown keys and values are dropped instead of forwarded. This prevents a
 * future call site from accidentally sending BMI, weight, blood pressure,
 * notes, AI text, profile fields, or any other sensitive value.
 */
object ProductAnalyticsPolicy {
    const val MAX_PARAMETER_VALUE_LENGTH = 40

    private val safeValuePattern = Regex("[a-z0-9_/-]{1,$MAX_PARAMETER_VALUE_LENGTH}")

    private val allowedValuesByKey: Map<String, Set<String>> = mapOf(
        "launch_source" to setOf("direct", "notification", "widget", "deep_link"),
        "surface" to setOf("home", "track", "calculators", "insights", "profile"),
        "action" to setOf("water", "weight", "steps", "calculator"),
        "calculator_id" to setOf(
            "bmi",
            "bmr",
            "tdee",
            "ideal_weight",
            "water",
            "waist_to_height",
            "waist_to_hip",
            "blood_pressure",
            "metabolic_syndrome",
            "bsa",
            "heart_rate"
        ),
        "entry_point" to setOf(
            "home",
            "track",
            "calculators",
            "insights",
            "profile",
            "notification",
            "widget",
            "deep_link"
        ),
        "tracker_type" to setOf("weight", "water", "steps", "calories", "blood_pressure"),
        "source" to setOf("home", "track", "calculator", "widget", "notification", "manual"),
        "permission_type" to setOf("steps", "weight"),
        "insight_type" to setOf("weight", "hydration", "steps", "blood_pressure", "goal", "no_data"),
        "report_type" to setOf("weekly", "monthly", "weight", "blood_pressure", "hydration"),
        "format" to setOf("pdf", "csv", "json", "text", "image") ,
        "reminder_type" to setOf("all", "water", "weight", "blood_pressure", "inactivity", "evening")
    )

    /** Names that must never be added as analytics parameters. */
    val forbiddenParameterNames: Set<String> = setOf(
        "value",
        "result",
        "score",
        "bmi",
        "bmr",
        "tdee",
        "weight",
        "height",
        "waist",
        "hip",
        "systolic",
        "diastolic",
        "pulse",
        "heart_rate",
        "body_fat",
        "calories",
        "water_ml",
        "steps",
        "sleep",
        "note",
        "notes",
        "message",
        "prompt",
        "response",
        "profile",
        "user_id",
        "email"
    )

    /**
     * Returns only event-specific, allowlisted metadata.
     * Values are normalized to lower case so no free-form text can leak.
     */
    fun sanitize(
        event: ProductAnalyticsEvent,
        parameters: Map<String, String>
    ): Map<String, String> = parameters
        .asSequence()
        .filter { (key, _) ->
            key in event.allowedParameterKeys &&
                key !in forbiddenParameterNames
        }
        .mapNotNull { (key, value) ->
            val normalized = value.trim().lowercase(Locale.ROOT)
            val allowedValues = allowedValuesByKey[key] ?: return@mapNotNull null
            if (normalized in allowedValues && safeValuePattern.matches(normalized)) {
                key to normalized
            } else {
                null
            }
        }
        .toMap()

    fun isSafeEventName(eventName: String): Boolean =
        ProductAnalyticsEvent.entries.any { it.eventName == eventName }
}
