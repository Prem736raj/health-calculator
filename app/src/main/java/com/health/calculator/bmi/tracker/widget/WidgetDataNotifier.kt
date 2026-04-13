package com.health.calculator.bmi.tracker.widget

import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.widget.core.WidgetDataChangeReceiver

/**
 * Single entry point for notifying widgets that app data changed.
 * Call from your ViewModels after every save/update operation.
 */
object WidgetDataNotifier {

    fun notifyBmiChanged(context: Context) = notify(context,
        WidgetDataChangeReceiver.DATA_BMI)

    fun notifyBpChanged(context: Context) = notify(context,
        WidgetDataChangeReceiver.DATA_BP)

    fun notifyWaterChanged(context: Context) = notify(context,
        WidgetDataChangeReceiver.DATA_WATER)

    fun notifyCaloriesChanged(context: Context) = notify(context,
        WidgetDataChangeReceiver.DATA_CALORIES)

    fun notifyWeightChanged(context: Context) = notify(context,
        WidgetDataChangeReceiver.DATA_WEIGHT)

    fun notifyStreakChanged(context: Context) = notify(context,
        WidgetDataChangeReceiver.DATA_STREAK)

    fun notifyAll(context: Context) = notify(context,
        WidgetDataChangeReceiver.DATA_ALL)

    private fun notify(context: Context, dataType: String) {
        val intent = Intent(context, WidgetDataChangeReceiver::class.java).apply {
            action = WidgetDataChangeReceiver.ACTION_DATA_CHANGED
            putExtra(WidgetDataChangeReceiver.EXTRA_DATA_TYPE, dataType)
        }
        context.sendBroadcast(intent)
    }
}
