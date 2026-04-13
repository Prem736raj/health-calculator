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

class StreakWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            updateWidget(context, appWidgetManager, id)
        }
    }

    companion object {

        private val milestones = listOf(3, 7, 14, 21, 30, 60, 90, 180, 365)

        private val motivations = listOf(
            "You're unstoppable! 💪",
            "Consistency is your superpower!",
            "Champions never quit. Keep going!",
            "Your future self thanks you 🙌",
            "Small steps = big results!",
            "One more day, one step closer!",
            "You're on fire! Keep it up 🔥",
            "Progress over perfection!",
            "Discipline beats motivation every time.",
            "Building habits that last a lifetime!"
        )

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs      = WidgetPreferencesManager(context)
            val streakData = com.health.calculator.bmi.tracker.data.repository.HealthSummaryRepository.getInstance(context)
                .getHealthSummaryData().streak // Note: adjust to your actual Repo if needed

            val streakDays = streakData.days
            val views      = RemoteViews(context.packageName, R.layout.widget_streak)

            // Streak count
            views.setTextViewText(R.id.streak_count, "$streakDays")

            // Days label
            val daysLabel = when {
                streakDays == 0 -> "Start today!"
                streakDays == 1 -> "day in a row"
                else            -> "days in a row"
            }
            views.setTextViewText(R.id.streak_days_label, daysLabel)

            // Streak type label
            val typeLabel = when {
                streakDays == 0 -> "No Streak Yet"
                streakDays < 7  -> "Getting Started 🌱"
                streakDays < 14 -> "Building Habit 📈"
                streakDays < 30 -> "Consistent 🏃"
                streakDays < 60 -> "Dedicated 🏆"
                else            -> "Legend Status 👑"
            }
            views.setTextViewText(R.id.streak_type_label, typeLabel)

            // Next milestone
            val nextMilestone = milestones.firstOrNull { it > streakDays }
            val milestoneText = if (nextMilestone != null) {
                val daysLeft = nextMilestone - streakDays
                "🎯 $daysLeft day${if (daysLeft > 1) "s" else ""} to $nextMilestone-day milestone"
            } else {
                "🌟 You've hit all milestones!"
            }
            views.setTextViewText(R.id.streak_milestone, milestoneText)

            // Motivation message (rotates daily)
            val dayIndex = (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
                + streakDays) % motivations.size
            views.setTextViewText(R.id.streak_motivation, motivations[dayIndex])

            // Streak color
            val streakColor = when {
                streakDays == 0  -> Color.parseColor("#9E9E9E")
                streakDays < 7   -> Color.parseColor("#FF9800")
                streakDays < 30  -> Color.parseColor("#F44336")
                else             -> Color.parseColor("#FF1744")
            }
            views.setTextColor(R.id.streak_count, streakColor)

            // Theme
            val theme  = prefs.getWidgetTheme(appWidgetId)
            val opacity= prefs.getWidgetOpacity(appWidgetId)
            val colors = WidgetThemeHelper.getColors(theme, opacity)
            views.setInt(R.id.streak_widget_root, "setBackgroundColor", colors.backgroundColor)
            views.setTextColor(R.id.streak_type_label, colors.textPrimary)
            views.setTextColor(R.id.streak_days_label, colors.textSecondary)
            views.setTextColor(R.id.streak_motivation, colors.textSecondary)

            // Milestone color
            val milestoneColor = if (streakDays >= (nextMilestone ?: 0) - 3)
                Color.parseColor("#4CAF50") else Color.parseColor("#FF9800")
            views.setTextColor(R.id.streak_milestone, milestoneColor)

            // Tap to open dashboard
            val navIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "dashboard")
            }
            val pending = PendingIntent.getActivity(
                context, appWidgetId + 6000, navIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.streak_widget_root, pending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun refreshAll(context: Context) {
            val manager   = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, StreakWidget::class.java)
            manager.getAppWidgetIds(component).forEach {
                updateWidget(context, manager, it)
            }
        }
    }
}
