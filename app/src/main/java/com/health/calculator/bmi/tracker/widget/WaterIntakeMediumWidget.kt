package com.health.calculator.bmi.tracker.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.data.WaterWidgetRepository
import com.health.calculator.bmi.tracker.widget.core.PolishedWidgetUpdater

/**
 * Medium (3x2) Water Intake Widget Provider
 */
class WaterIntakeMediumWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            WaterWidgetActions.ACTION_ADD_GLASS -> {
                WaterWidgetRepository.getInstance(context)
                    .addWater(WaterWidgetActions.AMOUNT_GLASS_ML)
                WidgetDataNotifier.notifyWaterChanged(context)
            }
            WaterWidgetActions.ACTION_ADD_BOTTLE -> {
                WaterWidgetRepository.getInstance(context)
                    .addWater(WaterWidgetActions.AMOUNT_BOTTLE_ML)
                WidgetDataNotifier.notifyWaterChanged(context)
            }
            WaterWidgetActions.ACTION_REFRESH -> {
                val manager = AppWidgetManager.getInstance(context)
                onUpdate(context, manager, manager.getAppWidgetIds(
                    android.content.ComponentName(context, WaterIntakeMediumWidget::class.java)
                ))
            }
        }
    }

    companion object {
        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            PolishedWidgetUpdater.updateWater(context, manager, widgetId, isMedium = true)
        }
    }
}
