package com.health.calculator.bmi.tracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.health.calculator.bmi.tracker.MainActivity
import com.health.calculator.bmi.tracker.R

class SingleCalculatorWidget : AppWidgetProvider() {

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

    companion object {

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs    = WidgetPreferencesManager(context)
            val calcType = prefs.getSingleCalcType(appWidgetId)
            val theme    = prefs.getWidgetTheme(appWidgetId)
            val opacity  = prefs.getWidgetOpacity(appWidgetId)
            val colors   = WidgetThemeHelper.getColors(theme, opacity)

            val lastResult   = prefs.getLastResult(calcType)
            val lastSubtitle = prefs.getLastResultSubtitle(calcType)

            val views = RemoteViews(context.packageName, R.layout.widget_single_calculator)

            // Apply theme
            views.setInt(R.id.single_calc_root, "setBackgroundColor", colors.backgroundColor)
            views.setTextColor(R.id.single_calc_name, colors.textPrimary)
            views.setTextColor(R.id.single_calc_subtitle, colors.textSecondary)

            // Accent color for result
            val accentColor = Color.parseColor(calcType.accentColorHex)
            views.setTextColor(R.id.single_calc_result, accentColor)

            // Content
            views.setImageViewResource(R.id.single_calc_icon, calcType.iconRes)
            views.setTextViewText(R.id.single_calc_name, calcType.displayName)
            views.setTextViewText(
                R.id.single_calc_result,
                if (lastResult == "--") "No data yet" else lastResult
            )
            views.setTextViewText(
                R.id.single_calc_subtitle,
                if (lastResult == "--") "Tap to calculate" else lastSubtitle
            )

            // Icon background tint
            views.setInt(R.id.single_icon_bg, "setBackgroundColor",
                Color.parseColor(calcType.accentColorHex + "22")) // 13% alpha

            // Tap to open calculator
            val navIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", calcType.navDestination)
            }
            val pending = PendingIntent.getActivity(
                context, appWidgetId + 5000, navIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.single_calc_root, pending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun refreshAll(context: Context) {
            val manager   = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, SingleCalculatorWidget::class.java)
            manager.getAppWidgetIds(component).forEach {
                updateWidget(context, manager, it)
            }
        }

        fun saveResultAndRefresh(
            context: Context,
            calcType: CalculatorType,
            result: String,
            subtitle: String = ""
        ) {
            WidgetPreferencesManager(context).saveLastResult(calcType, result, subtitle)
            refreshAll(context)
            // Also refresh quick calc grids
            QuickCalculateWidget.refreshAll(context)
        }
    }
}
