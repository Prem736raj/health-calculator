package com.health.calculator.bmi.tracker.widget.core

import android.widget.RemoteViews

/**
 * Centralized error / empty state rendering for all widgets.
 * Injects appropriate messaging into RemoteViews based on state.
 */
object WidgetErrorHandler {

    data class ErrorConfig(
        val emoji: String,
        val title: String,
        val subtitle: String,
        val actionText: String
    )

    fun getErrorConfig(state: WidgetStateManager.WidgetState): ErrorConfig {
        return when (state) {
            WidgetStateManager.WidgetState.EMPTY -> ErrorConfig(
                emoji      = "📊",
                title      = "No data yet",
                subtitle   = "Open app to get started",
                actionText = "Open App"
            )
            WidgetStateManager.WidgetState.STALE -> ErrorConfig(
                emoji      = "🔄",
                title      = "Data is outdated",
                subtitle   = "Tap to refresh",
                actionText = "Refresh"
            )
            WidgetStateManager.WidgetState.SETUP_REQUIRED -> ErrorConfig(
                emoji      = "⚙️",
                title      = "Setup required",
                subtitle   = "Open app to set up",
                actionText = "Set Up"
            )
            WidgetStateManager.WidgetState.ERROR -> ErrorConfig(
                emoji      = "⚠️",
                title      = "Something went wrong",
                subtitle   = "Tap to try again",
                actionText = "Retry"
            )
            WidgetStateManager.WidgetState.HEALTHY -> ErrorConfig(
                emoji      = "✅",
                title      = "Healthy",
                subtitle   = "",
                actionText = ""
            )
        }
    }

    /**
     * Applies error state to a widget that has
     * a standard error overlay (error_title, error_subtitle)
     */
    fun applyErrorState(
        views: RemoteViews,
        state: WidgetStateManager.WidgetState,
        titleViewId: Int,
        subtitleViewId: Int,
        emojiViewId: Int? = null
    ) {
        val config = getErrorConfig(state)
        views.setTextViewText(titleViewId, config.title)
        views.setTextViewText(subtitleViewId, config.subtitle)
        emojiViewId?.let { views.setTextViewText(it, config.emoji) }
    }

    /**
     * Returns true if the widget should show error UI
     * instead of normal content.
     */
    fun shouldShowError(state: WidgetStateManager.WidgetState): Boolean {
        return state != WidgetStateManager.WidgetState.HEALTHY
    }

    /**
     * Returns a stale badge text (or null if fresh).
     */
    fun getStaleBadge(state: WidgetStateManager.WidgetState): String? {
        return when (state) {
            WidgetStateManager.WidgetState.STALE          -> "⚠ Outdated"
            WidgetStateManager.WidgetState.SETUP_REQUIRED -> "⚙ Setup needed"
            WidgetStateManager.WidgetState.EMPTY          -> "📊 No data"
            else                                           -> null
        }
    }
}
