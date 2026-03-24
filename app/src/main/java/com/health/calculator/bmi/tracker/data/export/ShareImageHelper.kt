package com.health.calculator.bmi.tracker.data.export

import android.content.Context
import android.graphics.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ShareImageHelper(private val context: Context) {

    private val primaryColor = Color.rgb(0, 121, 145)
    private val bgColor = Color.rgb(250, 252, 254)

    fun generateHealthReportCard(card: HealthReportCard): File {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(bgColor)

        // Header gradient
        val headerPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), 200f,
                primaryColor, Color.rgb(0, 150, 136),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 220f, headerPaint)

        // App title
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Health Calculator", width / 2f, 90f, titlePaint)

        // Subtitle
        val subPaint = Paint().apply {
            color = Color.rgb(200, 240, 240)
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("My Health Summary", width / 2f, 140f, subPaint)

        // User name
        card.userName?.let {
            val namePaint = Paint().apply {
                color = Color.WHITE
                textSize = 32f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(it, width / 2f, 190f, namePaint)
        }

        var yPos = 280f
        val cardMargin = 50f
        val cardWidth = width - 2 * cardMargin

        // Health score circle
        card.healthScore?.let { score ->
            val centerX = width / 2f
            val radius = 80f

            val scoreBg = Paint().apply {
                color = Color.rgb(240, 245, 248)
                isAntiAlias = true
            }
            canvas.drawCircle(centerX, yPos + radius, radius + 10f, scoreBg)

            val scoreColor = when {
                score >= 80 -> Color.rgb(76, 175, 80)
                score >= 60 -> Color.rgb(33, 150, 243)
                score >= 40 -> Color.rgb(255, 193, 7)
                else -> Color.rgb(244, 67, 54)
            }

            val arcPaint = Paint().apply {
                color = scoreColor
                style = Paint.Style.STROKE
                strokeWidth = 12f
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
            }

            val arcRect = RectF(
                centerX - radius, yPos, centerX + radius, yPos + 2 * radius
            )
            canvas.drawArc(arcRect, -90f, 360f * score / 100f, false, arcPaint)

            val scorePaint = Paint().apply {
                color = scoreColor
                textSize = 56f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("$score", centerX, yPos + radius + 20f, scorePaint)

            val labelPaint = Paint().apply {
                color = Color.rgb(120, 120, 120)
                textSize = 20f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Health Score", centerX, yPos + radius + 50f, labelPaint)

            yPos += 2 * radius + 80f
        }

        // Metric cards
        fun drawMetricRow(label: String, value: String?, category: String?, color: Int) {
            if (value == null) return

            val rowPaint = Paint().apply {
                this.color = Color.WHITE
                isAntiAlias = true
                setShadowLayer(4f, 0f, 2f, Color.argb(30, 0, 0, 0))
            }
            canvas.drawRoundRect(
                cardMargin, yPos, cardMargin + cardWidth, yPos + 80f,
                16f, 16f, rowPaint
            )

            // Color indicator
            val indicatorPaint = Paint().apply { this.color = color; isAntiAlias = true }
            canvas.drawRoundRect(cardMargin, yPos, cardMargin + 8f, yPos + 80f, 4f, 4f, indicatorPaint)

            val metricLabelPaint = Paint().apply {
                this.color = Color.rgb(100, 100, 100)
                textSize = 24f
                isAntiAlias = true
            }
            canvas.drawText(label, cardMargin + 24f, yPos + 35f, metricLabelPaint)

            val valuePaint = Paint().apply {
                this.color = Color.rgb(33, 33, 33)
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText(value, cardMargin + cardWidth - 20f, yPos + 35f, valuePaint)

            category?.let {
                val catPaint = Paint().apply {
                    this.color = color
                    textSize = 22f
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                canvas.drawText(it, cardMargin + cardWidth - 20f, yPos + 62f, catPaint)
            }

            yPos += 100f
        }

        val green = Color.rgb(76, 175, 80)
        val blue = Color.rgb(33, 150, 243)
        val orange = Color.rgb(255, 152, 0)

        drawMetricRow("BMI", card.bmi, card.bmiCategory, green)
        drawMetricRow("Blood Pressure", card.bp, card.bpCategory, blue)
        drawMetricRow("Weight", card.weight, null, primaryColor)
        drawMetricRow("Water Today", card.waterProgress, null, Color.rgb(0, 188, 212))
        drawMetricRow("Calories Today", card.calorieProgress, null, orange)

        // Date footer
        yPos = height - 80f
        val datePaint = Paint().apply {
            color = Color.rgb(160, 160, 160)
            textSize = 22f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(card.generatedDate, width / 2f, yPos, datePaint)

        val footerPaint = Paint().apply {
            color = Color.rgb(180, 180, 180)
            textSize = 18f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Health Calculator: BMI Tracker", width / 2f, yPos + 35f, footerPaint)

        // Save
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(exportDir, "HealthReport_$timestamp.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        bitmap.recycle()

        return file
    }
}
