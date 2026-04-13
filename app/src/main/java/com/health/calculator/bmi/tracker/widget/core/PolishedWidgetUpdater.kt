package com.health.calculator.bmi.tracker.widget.core

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.health.calculator.bmi.tracker.MainActivity
import com.health.calculator.bmi.tracker.R
import com.health.calculator.bmi.tracker.data.WaterWidgetRepository
import com.health.calculator.bmi.tracker.widget.*

/**
 * Final polished update functions for all widget types.
 * Integrates: state management, error states, accessibility,
 * Material You, theming, and performance throttling.
 */
object PolishedWidgetUpdater {

    // ── Water Widget (Small & Medium) ──────────────────────────────────

    fun updateWater(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        isMedium: Boolean,
        isUserTriggered: Boolean = false
    ) {
        if (!WidgetPerformanceManager.shouldUpdate(context, widgetId, isUserTriggered)) return

        val repo      = WaterWidgetRepository.getInstance(context)
        try {
            val data      = repo.getWidgetData()
            val state     = WidgetStateManager.getState(context, widgetId, data.todayIntakeMl > 0 || data.goalMl > 0)
            
            val layoutId  = if (isMedium) R.layout.widget_water_medium else R.layout.widget_water_small
            val views     = RemoteViews(context.packageName, layoutId)

            // Dynamic IDs based on layout
            val intakeId   = if (isMedium) R.id.medium_intake_amount else R.id.small_intake_amount
            val pctId      = if (isMedium) R.id.medium_percentage else R.id.small_percentage
            val goalId     = if (isMedium) R.id.medium_goal_text else R.id.small_goal_text // Map accordingly or check XML
            val rootId     = if (isMedium) R.id.widget_medium_root else R.id.widget_small_root
            val arcId      = if (isMedium) R.id.medium_progress_arc else R.id.small_progress_arc

            // Apply theme
            WidgetMaterialYouHelper.applyToWidget(
                context, views, widgetId,
                rootId         = rootId,
                primaryTextIds = listOf(intakeId),
                secondaryTextIds = listOf(pctId)
            )

            when {
                state == WidgetStateManager.WidgetState.HEALTHY ||
                state == WidgetStateManager.WidgetState.STALE -> {
                    // Normal content
                    views.setTextViewText(intakeId, data.intakeFormatted)
                    views.setTextViewText(pctId,    "${data.percentage}%")
                    
                    if (isMedium) {
                        views.setTextViewText(R.id.medium_progress_text, "${data.intakeFormatted} / ${data.goalFormatted}")
                        views.setTextViewText(R.id.medium_glasses_count, "${data.glassesCount} glasses")
                        views.setTextViewText(R.id.medium_last_logged, 
                            if (data.lastLoggedTime.isEmpty()) "Not logged today" else "Last: ${data.lastLoggedTime}")
                        
                        // Setup buttons
                        views.setOnClickPendingIntent(R.id.btn_add_glass, 
                            buildActionIntent(context, WaterWidgetActions.ACTION_ADD_GLASS, widgetId, 201))
                        views.setOnClickPendingIntent(R.id.btn_add_bottle, 
                            buildActionIntent(context, WaterWidgetActions.ACTION_ADD_BOTTLE, widgetId, 202))
                    }

                    // Draw progress arc
                    val cacheKey = "water_arc_${widgetId}_${data.percentage}_${if(isMedium) "M" else "S"}"
                    val arc = WidgetPerformanceManager.getCachedBitmap(cacheKey)
                        ?: run {
                            val size = if (isMedium) 180 else 120
                            val stroke = if (isMedium) 10f else 8f
                            WaterIntakeWidget.drawProgressArc(data.percentage / 100f, size, stroke, context)
                                .also { WidgetPerformanceManager.cacheBitmap(cacheKey, it) }
                        }
                    views.setImageViewBitmap(arcId, arc)
                }
                else -> {
                    // Error / empty state
                    val config = WidgetErrorHandler.getErrorConfig(state)
                    views.setTextViewText(intakeId, config.emoji)
                    views.setTextViewText(pctId,    "")
                }
            }

            // Tap intent
            views.setOnClickPendingIntent(rootId, buildNavIntent(context, "water_tracker", widgetId))

            manager.updateAppWidget(widgetId, views)
            WidgetPerformanceManager.recordUpdate(context, widgetId)
        } catch(e: Exception) {
            Log.e("PolishedUpdater", "Water update failed", e)
        }
    }

    private fun buildActionIntent(context: Context, action: String, widgetId: Int, code: Int): PendingIntent {
        val intent = Intent(context, WaterIntakeSmallWidget::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(context, widgetId + code, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    // ── Health Summary Widget ─────────────────────────────────────────

    fun updateHealthSummary(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        isLarge: Boolean,
        isUserTriggered: Boolean = false
    ) {
        if (!WidgetPerformanceManager.shouldUpdate(context, widgetId, isUserTriggered)) return

        val prefs = WidgetPreferencesManager(context)
        val score = prefs.getHealthScore()
        val state = WidgetStateManager.getState(context, widgetId, score > 0)
        
        val layoutId = if (isLarge) R.layout.widget_health_large else R.layout.widget_health_medium
        val views    = RemoteViews(context.packageName, layoutId)

        // Apply theme (Background & Root text)
        WidgetMaterialYouHelper.applyToWidget(
            context, views, widgetId,
            rootId = if (isLarge) R.id.score_container else R.id.score_container, // Apply to top container
            primaryTextIds = listOf(R.id.txt_health_score),
            secondaryTextIds = listOf(R.id.txt_score_status)
        )

        when {
            state == WidgetStateManager.WidgetState.HEALTHY || 
            state == WidgetStateManager.WidgetState.STALE -> {
                
                // 1. Draw Health Arc
                val arcKey = "health_arc_${score}_${if(isLarge) "L" else "M"}"
                val arcBmp = WidgetPerformanceManager.getCachedBitmap(arcKey) ?: run {
                    val size = if (isLarge) 100 else 80
                    drawHealthArc(context, score, size).also {
                        WidgetPerformanceManager.cacheBitmap(arcKey, it)
                    }
                }
                views.setImageViewBitmap(R.id.img_health_arc, arcBmp)
                views.setTextViewText(R.id.txt_health_score, score.toString())
                
                // 2. Status message
                val status = when {
                    score >= 90 -> "Excellent! Keep it up."
                    score >= 70 -> "Looking good. Some improvements possible."
                    score >= 50 -> "Fair. Stay consistent."
                    else -> "Needs attention. Open app to check."
                }
                views.setTextViewText(R.id.txt_score_status, 
                    if (state == WidgetStateManager.WidgetState.STALE) "⚠ Outdated" else status)

                // 3. Fill Metric Cards
                views.setTextViewText(R.id.bmi_value,   prefs.getHealthBmi().let { if(it>0) String.format("%.1f", it) else "--" })
                views.setTextViewText(R.id.bp_value,    prefs.getHealthBp().let { if(it.first>0) "${it.first}/${it.second}" else "--" })
                views.setTextViewText(R.id.water_value, "${prefs.getHealthWaterPct()}%")

                // Nav Intents for cards
                views.setOnClickPendingIntent(R.id.card_bmi,   buildNavIntent(context, "bmi_calculator", widgetId, 1))
                views.setOnClickPendingIntent(R.id.card_bp,    buildNavIntent(context, "blood_pressure_checker", widgetId, 2))
                views.setOnClickPendingIntent(R.id.card_water, buildNavIntent(context, "water_tracker", widgetId, 3))

                if (isLarge) {
                    views.setTextViewText(R.id.calories_value, "${prefs.getHealthCalPct()}%")
                    views.setTextViewText(R.id.hr_value,       "--")
                    views.setTextViewText(R.id.streak_value,   "7 days")
                    
                    views.setOnClickPendingIntent(R.id.card_calories, buildNavIntent(context, "calorie_calculator", widgetId, 4))
                    views.setOnClickPendingIntent(R.id.card_hr,       buildNavIntent(context, "heart_rate_calculator", widgetId, 5))
                    // Streak opens main dashboard
                    views.setOnClickPendingIntent(R.id.card_streak,   buildNavIntent(context, "dashboard", widgetId, 6))

                    // Update sync time
                    views.setTextViewText(R.id.txt_last_sync, "Last sync: Just now")
                }
            }
            else -> {
                val config = WidgetErrorHandler.getErrorConfig(state)
                views.setTextViewText(R.id.txt_health_score, config.emoji)
                views.setTextViewText(R.id.txt_score_status, config.subtitle)
                
                // Reset card values if in error/empty state
                views.setTextViewText(R.id.bmi_value, "--")
                views.setTextViewText(R.id.bp_value, "--")
                views.setTextViewText(R.id.water_value, "--")
                if (isLarge) {
                    views.setTextViewText(R.id.calories_value, "--")
                    views.setTextViewText(R.id.hr_value, "--")
                    views.setTextViewText(R.id.streak_value, "--")
                }
            }
        }

        // Accessibility
        views.setContentDescription(R.id.img_health_arc, "Overall health score is $score percent")
        
        manager.updateAppWidget(widgetId, views)
    }

    private fun bindMetricCard(
        context: Context,
        views: RemoteViews,
        containerId: Int,
        type: CalculatorType,
        valueString: String
    ) {
        // Obsolete - functionality integrated into updateHealthSummary for unique ID support
    }

    // ── Helper: build nav PendingIntent ──────────────────────────────

    fun buildNavIntent(
        context: Context,
        destination: String,
        widgetId: Int,
        requestOffset: Int = 0
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", destination)
        }
        return PendingIntent.getActivity(
            context,
            widgetId + requestOffset,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ── Draw health arc bitmap ────────────────────────────────────────

    fun drawHealthArc(context: Context, score: Int, size: Int): android.graphics.Bitmap {
        val density = context.resources.displayMetrics.density
        val strokeWidth = 8f * density
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val rect = android.graphics.RectF(strokeWidth, strokeWidth, size - strokeWidth, size - strokeWidth)

        val trackPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = Color.parseColor("#E0E0E0")
            strokeCap = android.graphics.Paint.Cap.ROUND
        }

        val progressPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = when {
                score >= 80 -> Color.parseColor("#4CAF50")
                score >= 60 -> Color.parseColor("#FFC107")
                else -> Color.parseColor("#F44336")
            }
            strokeCap = android.graphics.Paint.Cap.ROUND
        }

        canvas.drawArc(rect, 135f, 270f, false, trackPaint)
        canvas.drawArc(rect, 135f, (score / 100f) * 270f, false, progressPaint)

        return bitmap
    }

    // ── Draw progress bar bitmap ──────────────────────────────────────

    fun drawProgressBarBitmap(
        context: Context,
        percentage: Int,
        color: Int,
        maxWidthDp: Float = 100f,
        heightDp: Float = 4f
    ): android.graphics.Bitmap? {
        val density    = context.resources.displayMetrics.density
        val maxWidthPx = (maxWidthDp * density).toInt()
        val heightPx   = (heightDp * density).toInt().coerceAtLeast(2)
        val widthPx    = ((percentage / 100f) * maxWidthPx).toInt().coerceAtLeast(2)

        return try {
            val bmp    = android.graphics.Bitmap.createBitmap(widthPx, heightPx, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bmp)
            val paint  = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
            }
            canvas.drawRoundRect(
                0f, 0f, widthPx.toFloat(), heightPx.toFloat(),
                heightPx / 2f, heightPx / 2f, paint
            )
            bmp
        } catch (e: Exception) {
            null
        }
    }
}
