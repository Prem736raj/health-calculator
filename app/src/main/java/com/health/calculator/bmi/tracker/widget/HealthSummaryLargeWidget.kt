package com.health.calculator.bmi.tracker.widget

import dagger.hilt.android.qualifiers.ApplicationContext

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import com.health.calculator.bmi.tracker.widget.core.PolishedWidgetUpdater

class HealthSummaryLargeWidget : AppWidgetProvider() {
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
        fun updateWidget(@ApplicationContext context: Context, manager: AppWidgetManager, widgetId: Int) {
            PolishedWidgetUpdater.updateHealthSummary(context, manager, widgetId, isLarge = true)
        }

        fun refreshAll(@ApplicationContext context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, HealthSummaryLargeWidget::class.java)
            manager.getAppWidgetIds(component).forEach {
                updateWidget(context, manager, it)
            }
        }
    }
}
