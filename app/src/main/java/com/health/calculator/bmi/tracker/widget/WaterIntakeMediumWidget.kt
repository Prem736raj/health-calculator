package com.health.calculator.bmi.tracker.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.health.calculator.bmi.tracker.widget.core.PolishedWidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Medium (3x2) Water Intake Widget Provider.
 * Mutating actions are handled by the non-exported WaterWidgetActionReceiver.
 */
class WaterIntakeMediumWidget : AppWidgetProvider() {

    override fun onUpdate(
        @ApplicationContext context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateWidget(
            @ApplicationContext context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            PolishedWidgetUpdater.updateWater(
                context,
                manager,
                widgetId,
                isMedium = true
            )
        }
    }
}

