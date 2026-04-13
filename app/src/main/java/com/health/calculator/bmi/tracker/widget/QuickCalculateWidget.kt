package com.health.calculator.bmi.tracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.health.calculator.bmi.tracker.MainActivity
import com.health.calculator.bmi.tracker.R

class QuickCalculateWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = WidgetPreferencesManager(context)
        appWidgetIds.forEach { prefs.clearWidgetPrefs(it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager   = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, QuickCalculateWidget::class.java)
            manager.getAppWidgetIds(component).forEach {
                updateWidget(context, manager, it)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.health.calculator.bmi.tracker.QUICK_CALC_REFRESH"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs  = WidgetPreferencesManager(context)
            val theme  = prefs.getWidgetTheme(appWidgetId)
            val opacity= prefs.getWidgetOpacity(appWidgetId)
            val colors = WidgetThemeHelper.getColors(theme, opacity)

            val slot1  = prefs.getQuickCalcSlot(appWidgetId, 1)
            val slot2  = prefs.getQuickCalcSlot(appWidgetId, 2)
            val slot3  = prefs.getQuickCalcSlot(appWidgetId, 3)
            val slot4  = prefs.getQuickCalcSlot(appWidgetId, 4)

            val views  = RemoteViews(context.packageName, R.layout.widget_quick_calculate)

            // Apply theme background
            views.setInt(R.id.quick_calc_root, "setBackgroundColor", colors.backgroundColor)

            // ── Slot 1 ────────────────────────────────────────────
            applySlot(context, views, appWidgetId, 1,
                R.id.quick_slot_1, R.id.quick_icon_1,
                R.id.quick_label_1, R.id.quick_result_1,
                slot1, prefs, colors)

            // ── Slot 2 ────────────────────────────────────────────
            applySlot(context, views, appWidgetId, 2,
                R.id.quick_slot_2, R.id.quick_icon_2,
                R.id.quick_label_2, R.id.quick_result_2,
                slot2, prefs, colors)

            // ── Slot 3 ────────────────────────────────────────────
            applySlot(context, views, appWidgetId, 3,
                R.id.quick_slot_3, R.id.quick_icon_3,
                R.id.quick_label_3, R.id.quick_result_3,
                slot3, prefs, colors)

            // ── Slot 4 ────────────────────────────────────────────
            applySlot(context, views, appWidgetId, 4,
                R.id.quick_slot_4, R.id.quick_icon_4,
                R.id.quick_label_4, R.id.quick_result_4,
                slot4, prefs, colors)

            // ── Settings icon → open config ───────────────────────
            val configIntent = Intent(context, WidgetConfigActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val configPending = PendingIntent.getActivity(
                context, appWidgetId + 9000, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.quick_calc_settings, configPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun applySlot(
            context: Context,
            views: RemoteViews,
            widgetId: Int,
            slot: Int,
            slotViewId: Int,
            iconViewId: Int,
            labelViewId: Int,
            resultViewId: Int,
            calcType: CalculatorType,
            prefs: WidgetPreferencesManager,
            colors: WidgetThemeHelper.ThemeColors
        ) {
            // Icon
            views.setImageViewResource(iconViewId, calcType.iconRes)

            // Label
            views.setTextViewText(labelViewId, calcType.shortName)
            views.setTextColor(labelViewId, colors.textPrimary)

            // Last result
            val lastResult = prefs.getLastResult(calcType)
            views.setTextViewText(resultViewId, lastResult)
            views.setTextColor(resultViewId, colors.textSecondary)

            // Card background tint
            views.setInt(slotViewId, "setBackgroundColor", colors.cardBackground)

            // Tap to open calculator
            val navIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", calcType.navDestination)
            }
            val pending = PendingIntent.getActivity(
                context,
                widgetId * 10 + slot,
                navIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(slotViewId, pending)
        }

        fun refreshAll(context: Context) {
            val manager   = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, QuickCalculateWidget::class.java)
            manager.getAppWidgetIds(component).forEach {
                updateWidget(context, manager, it)
            }
        }
    }
}
