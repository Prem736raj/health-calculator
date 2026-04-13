package com.health.calculator.bmi.tracker.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.widget.RemoteViews
import com.health.calculator.bmi.tracker.R
import com.health.calculator.bmi.tracker.MainActivity
import com.health.calculator.bmi.tracker.data.WaterWidgetRepository
import java.util.Calendar

class WaterIntakeWidget : AppWidgetProvider() {

    // ─── onUpdate ─────────────────────────────────────────────────────

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            val widgetInfo = appWidgetManager.getAppWidgetInfo(widgetId)
            when {
                isSmallWidget(widgetInfo?.initialLayout) ->
                    updateSmallWidget(context, appWidgetManager, widgetId)
                else ->
                    updateMediumWidget(context, appWidgetManager, widgetId)
            }
        }
        scheduleMidnightReset(context)
    }

    // ─── onReceive ────────────────────────────────────────────────────

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val repo = WaterWidgetRepository.getInstance(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val smallComponent = ComponentName(context, WaterIntakeSmallWidget::class.java)
        val mediumComponent = ComponentName(context, WaterIntakeMediumWidget::class.java)

        when (intent.action) {
            WaterWidgetActions.ACTION_ADD_GLASS -> {
                repo.addWater(WaterWidgetActions.AMOUNT_GLASS_ML)
                refreshAllWidgets(context, appWidgetManager, smallComponent, mediumComponent)
            }
            WaterWidgetActions.ACTION_ADD_BOTTLE -> {
                repo.addWater(WaterWidgetActions.AMOUNT_BOTTLE_ML)
                refreshAllWidgets(context, appWidgetManager, smallComponent, mediumComponent)
            }
            WaterWidgetActions.ACTION_REFRESH -> {
                refreshAllWidgets(context, appWidgetManager, smallComponent, mediumComponent)
            }
            WaterWidgetActions.ACTION_MIDNIGHT_RESET -> {
                // Data resets automatically in repo via checkAndResetForNewDay()
                refreshAllWidgets(context, appWidgetManager, smallComponent, mediumComponent)
                scheduleMidnightReset(context) // reschedule for next night
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleMidnightReset(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelMidnightReset(context)
    }

    // ─── Helper: refresh all widget instances ─────────────────────────

    private fun refreshAllWidgets(
        context: Context,
        manager: AppWidgetManager,
        small: ComponentName,
        medium: ComponentName
    ) {
        manager.getAppWidgetIds(small).forEach { id ->
            updateSmallWidget(context, manager, id)
        }
        manager.getAppWidgetIds(medium).forEach { id ->
            updateMediumWidget(context, manager, id)
        }
    }

    private fun isSmallWidget(layoutId: Int?): Boolean {
        return layoutId == R.layout.widget_water_small
    }

    // ─── Midnight Reset Alarm ─────────────────────────────────────────

    fun scheduleMidnightReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 5) // 5 seconds after midnight
            set(Calendar.MILLISECOND, 0)
        }

        val intent = Intent(context, WaterIntakeWidget::class.java).apply {
            action = WaterWidgetActions.ACTION_MIDNIGHT_RESET
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            7001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                midnight.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Fallback for devices without exact alarm permission
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                midnight.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelMidnightReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterIntakeWidget::class.java).apply {
            action = WaterWidgetActions.ACTION_MIDNIGHT_RESET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 7001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {

        // ─── Update Small Widget ──────────────────────────────────────

        fun updateSmallWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val repo = WaterWidgetRepository.getInstance(context)
            val data = repo.getWidgetData()

            val views = RemoteViews(context.packageName, R.layout.widget_water_small)

            // Set text values
            views.setTextViewText(R.id.small_intake_amount, data.intakeFormatted)
            views.setTextViewText(R.id.small_percentage, "${data.percentage}%")
            views.setTextViewText(R.id.small_goal_text, "Goal: ${data.goalFormatted}")

            // Draw progress arc
            val progressBitmap = drawProgressArc(
                size = 120,
                progress = data.percentage / 100f,
                strokeWidthDp = 8f,
                context = context
            )
            views.setImageViewBitmap(R.id.small_progress_arc, progressBitmap)

            // Tap to open app
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "water_tracker")
            }
            val openPending = PendingIntent.getActivity(
                context, appWidgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_small_root, openPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        // ─── Update Medium Widget ─────────────────────────────────────

        fun updateMediumWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val repo = WaterWidgetRepository.getInstance(context)
            val data = repo.getWidgetData()

            val views = RemoteViews(context.packageName, R.layout.widget_water_medium)

            // Set text values
            views.setTextViewText(R.id.medium_intake_amount, data.intakeFormatted)
            views.setTextViewText(R.id.medium_percentage, "${data.percentage}%")
            views.setTextViewText(
                R.id.medium_progress_text,
                "${data.intakeFormatted} / ${data.goalFormatted}"
            )
            views.setTextViewText(
                R.id.medium_glasses_count,
                "\uD83E\uDD5B ${data.glassesCount} glasses"
            )
            views.setTextViewText(
                R.id.medium_last_logged,
                if (data.lastLoggedTime.isEmpty()) "⏰ Not logged today"
                else "⏰ Last: ${data.lastLoggedTime}"
            )

            // Draw large progress arc
            val progressBitmap = drawProgressArc(
                size = 180,
                progress = data.percentage / 100f,
                strokeWidthDp = 10f,
                context = context
            )
            views.setImageViewBitmap(R.id.medium_progress_arc, progressBitmap)

            // Tap root to open app
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "water_tracker")
            }
            val openPending = PendingIntent.getActivity(
                context, appWidgetId + 100, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_medium_root, openPending)

            // +Glass button - add 250ml without opening app
            val glassIntent = Intent(context, WaterIntakeWidget::class.java).apply {
                action = WaterWidgetActions.ACTION_ADD_GLASS
            }
            val glassPending = PendingIntent.getBroadcast(
                context,
                appWidgetId + 200,
                glassIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_add_glass, glassPending)

            // +Bottle button - add 500ml without opening app
            val bottleIntent = Intent(context, WaterIntakeWidget::class.java).apply {
                action = WaterWidgetActions.ACTION_ADD_BOTTLE
            }
            val bottlePending = PendingIntent.getBroadcast(
                context,
                appWidgetId + 300,
                bottleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_add_bottle, bottlePending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        // ─── Draw Progress Arc on Canvas ──────────────────────────────

        fun drawProgressArc(
            size: Int,
            progress: Float,
            strokeWidthDp: Float,
            context: Context
        ): Bitmap {
            val density = context.resources.displayMetrics.density
            val strokeWidth = strokeWidthDp * density
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val padding = strokeWidth / 2f
            val rectF = RectF(padding, padding, size - padding, size - padding)

            // Track paint (background ring)
            val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                color = Color.parseColor("#BBDEFB") // light blue track
                strokeCap = Paint.Cap.ROUND
            }

            // Progress paint
            val progressColor = when {
                progress >= 1f -> Color.parseColor("#00BCD4") // cyan when complete
                progress >= 0.7f -> Color.parseColor("#2196F3") // blue
                progress >= 0.4f -> Color.parseColor("#42A5F5") // lighter blue
                else -> Color.parseColor("#90CAF9") // very light blue
            }

            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                color = progressColor
                strokeCap = Paint.Cap.ROUND
                shader = SweepGradient(
                    size / 2f, size / 2f,
                    intArrayOf(Color.parseColor("#64B5F6"), progressColor),
                    floatArrayOf(0f, progress.coerceAtMost(1f))
                )
            }

            // Draw background track (full circle)
            canvas.drawArc(rectF, -90f, 360f, false, trackPaint)

            // Draw progress arc
            if (progress > 0f) {
                val sweepAngle = (progress.coerceAtMost(1f) * 360f)
                canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)
            }

            // Completion sparkle effect
            if (progress >= 1f) {
                val sparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#E0F7FA")
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(size / 2f, padding, strokeWidth * 0.6f, sparklePaint)
            }

            return bitmap
        }

        // ─── Notify all widget instances to refresh ───────────────────

        fun triggerRefresh(context: Context) {
            val intent = Intent(context, WaterIntakeWidget::class.java).apply {
                action = WaterWidgetActions.ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}
