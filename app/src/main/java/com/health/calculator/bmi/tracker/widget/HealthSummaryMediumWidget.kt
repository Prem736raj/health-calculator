package com.health.calculator.bmi.tracker.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.health.calculator.bmi.tracker.widget.core.PolishedWidgetUpdater

class HealthSummaryMediumWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            PolishedWidgetUpdater.updateHealthSummary(context, manager, widgetId, isLarge = false)
        }
    }
}
