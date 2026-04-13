package com.health.calculator.bmi.tracker.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.health.calculator.bmi.tracker.data.WaterWidgetRepository

/**
 * Call this from your WaterTracker screen/viewmodel
 * whenever the user logs water from within the app.
 * This ensures widgets stay in sync with the app.
 */
object WaterWidgetSyncManager {

    fun syncToWidgets(
        context: Context,
        todayIntakeMl: Int,
        glassesCount: Int,
        goalMl: Int
    ) {
        // Update the widget prefs
        WaterWidgetRepository.getInstance(context).syncFromApp(
            intakeMl = todayIntakeMl,
            glassesCount = glassesCount,
            goalMl = goalMl
        )

        // Trigger widget refresh
        refreshAllWidgets(context)
    }

    fun refreshAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // Refresh small widgets
        val smallIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, WaterIntakeSmallWidget::class.java)
        )
        smallIds.forEach { id ->
            WaterIntakeWidget.updateSmallWidget(context, appWidgetManager, id)
        }

        // Refresh medium widgets
        val mediumIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, WaterIntakeMediumWidget::class.java)
        )
        mediumIds.forEach { id ->
            WaterIntakeWidget.updateMediumWidget(context, appWidgetManager, id)
        }
    }

    fun updateGoal(context: Context, goalMl: Int) {
        WaterWidgetRepository.getInstance(context).updateGoal(goalMl)
        refreshAllWidgets(context)
    }
}
