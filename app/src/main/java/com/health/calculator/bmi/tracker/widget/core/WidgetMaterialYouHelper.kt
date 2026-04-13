package com.health.calculator.bmi.tracker.widget.core

import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import com.health.calculator.bmi.tracker.widget.WidgetTheme
import com.health.calculator.bmi.tracker.widget.WidgetPreferencesManager

/**
 * Applies Material You (Monet) dynamic colors to widgets
 * on Android 12+ devices. Falls back gracefully on older APIs.
 */
object WidgetMaterialYouHelper {

    fun applyToWidget(
        context: Context,
        views: RemoteViews,
        widgetId: Int,
        rootId: Int,
        cardIds: List<Int> = emptyList(),
        primaryTextIds: List<Int> = emptyList(),
        secondaryTextIds: List<Int> = emptyList()
    ) {
        val prefs  = WidgetPreferencesManager(context)
        val theme  = prefs.getWidgetTheme(widgetId)
        val opacity= prefs.getWidgetOpacity(widgetId)

        // Try Material You first on Android 12+
        val scheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            WidgetThemeEngine.resolveMaterialYouScheme(context, opacity)
                ?: WidgetThemeEngine.resolveScheme(context, theme, opacity)
        } else {
            WidgetThemeEngine.resolveScheme(context, theme, opacity)
        }

        // Apply to root
        views.setInt(rootId, "setBackgroundColor", scheme.surfacePrimary)

        // Apply to cards
        cardIds.forEach { cardId ->
            views.setInt(cardId, "setBackgroundColor", scheme.surfaceCard)
        }

        // Apply text colors
        primaryTextIds.forEach   { views.setTextColor(it, scheme.onSurfacePrimary) }
        secondaryTextIds.forEach { views.setTextColor(it, scheme.onSurfaceSecondary) }
    }

    fun isMaterialYouSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun getDynamicAccent(context: Context): Int? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        return try {
            context.resources.getColor(
                android.R.color.system_accent1_500,
                context.theme
            )
        } catch (e: Exception) {
            null
        }
    }
}
