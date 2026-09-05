package com.health.calculator.bmi.tracker.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.health.calculator.bmi.tracker.core.util.launchAsync
import com.health.calculator.bmi.tracker.data.WaterWidgetRepository

/**
 * Private receiver for mutating water data from app widgets.
 *
 * Widget providers are exported by Android so launchers can update them. Mutating
 * health data through those exported receivers made the custom add-water actions
 * callable by any installed app. All mutations now terminate here instead.
 */
class WaterWidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val amountMl = when (intent.action) {
            WaterWidgetActions.ACTION_ADD_GLASS -> WaterWidgetActions.AMOUNT_GLASS_ML
            WaterWidgetActions.ACTION_ADD_BOTTLE -> WaterWidgetActions.AMOUNT_BOTTLE_ML
            else -> return
        }

        launchAsync {
            WaterWidgetRepository.getInstance(context).addWaterPersisted(amountMl)
            WidgetDataNotifier.notifyWaterChanged(context)
        }
    }
}

