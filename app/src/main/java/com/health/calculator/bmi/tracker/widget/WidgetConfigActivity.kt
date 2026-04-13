package com.health.calculator.bmi.tracker.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.health.calculator.bmi.tracker.R

class WidgetConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var spinnerSlot1: Spinner
    private lateinit var spinnerSlot2: Spinner
    private lateinit var spinnerSlot3: Spinner
    private lateinit var spinnerSlot4: Spinner
    private lateinit var seekbarOpacity: SeekBar
    private lateinit var opacityLabel: TextView
    private lateinit var btnAdd: Button
    private lateinit var btnCancel: Button
    private lateinit var themeLightView: View
    private lateinit var themeDarkView: View
    private lateinit var themeTransparentView: View

    private var selectedTheme = WidgetTheme.SYSTEM
    private lateinit var prefsManager: WidgetPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set result to CANCELED initially (widget won't be added if user backs out)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_widget_config)

        // Get widget ID from intent
        appWidgetId = intent.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        prefsManager = WidgetPreferencesManager(this)
        initViews()
        setupSpinners()
        setupThemeSelection()
        setupOpacitySlider()
        setupButtons()
    }

    private fun initViews() {
        spinnerSlot1       = findViewById(R.id.spinner_slot_1)
        spinnerSlot2       = findViewById(R.id.spinner_slot_2)
        spinnerSlot3       = findViewById(R.id.spinner_slot_3)
        spinnerSlot4       = findViewById(R.id.spinner_slot_4)
        seekbarOpacity     = findViewById(R.id.seekbar_opacity)
        opacityLabel       = findViewById(R.id.opacity_label)
        btnAdd             = findViewById(R.id.btn_add_widget)
        btnCancel          = findViewById(R.id.btn_cancel_config)
        themeLightView     = findViewById(R.id.theme_light)
        themeDarkView      = findViewById(R.id.theme_dark)
        themeTransparentView = findViewById(R.id.theme_transparent)
    }

    private fun setupSpinners() {
        val calcLabels = CalculatorType.spinnerLabels()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, calcLabels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Apply same adapter to all spinners
        listOf(spinnerSlot1, spinnerSlot2, spinnerSlot3, spinnerSlot4).forEach { spinner ->
            spinner.adapter = adapter
        }

        // Set defaults from most-used or previous config
        val mostUsed = prefsManager.getMostUsedCalculators(4)
        spinnerSlot1.setSelection(CalculatorType.values().indexOf(mostUsed.getOrElse(0) { CalculatorType.BMI }))
        spinnerSlot2.setSelection(CalculatorType.values().indexOf(mostUsed.getOrElse(1) { CalculatorType.BLOOD_PRESSURE }))
        spinnerSlot3.setSelection(CalculatorType.values().indexOf(mostUsed.getOrElse(2) { CalculatorType.WATER }))
        spinnerSlot4.setSelection(CalculatorType.values().indexOf(mostUsed.getOrElse(3) { CalculatorType.CALORIES }))
    }

    private fun setupThemeSelection() {
        updateThemeSelection(WidgetTheme.SYSTEM)

        themeLightView.setOnClickListener {
            selectedTheme = WidgetTheme.LIGHT
            updateThemeSelection(WidgetTheme.LIGHT)
        }
        themeDarkView.setOnClickListener {
            selectedTheme = WidgetTheme.DARK
            updateThemeSelection(WidgetTheme.DARK)
        }
        themeTransparentView.setOnClickListener {
            selectedTheme = WidgetTheme.TRANSPARENT
            updateThemeSelection(WidgetTheme.TRANSPARENT)
        }
    }

    private fun updateThemeSelection(selected: WidgetTheme) {
        val selectedStroke = resources.getDrawable(R.drawable.config_theme_selected, theme)
        val unselectedStroke = null

        themeLightView.foreground       = if (selected == WidgetTheme.LIGHT)        selectedStroke else unselectedStroke
        themeDarkView.foreground        = if (selected == WidgetTheme.DARK)         selectedStroke else unselectedStroke
        themeTransparentView.foreground = if (selected == WidgetTheme.TRANSPARENT)  selectedStroke else unselectedStroke
    }

    private fun setupOpacitySlider() {
        seekbarOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                opacityLabel.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun setupButtons() {
        btnAdd.setOnClickListener { saveAndFinish() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun saveAndFinish() {
        val slot1 = CalculatorType.values()[spinnerSlot1.selectedItemPosition]
        val slot2 = CalculatorType.values()[spinnerSlot2.selectedItemPosition]
        val slot3 = CalculatorType.values()[spinnerSlot3.selectedItemPosition]
        val slot4 = CalculatorType.values()[spinnerSlot4.selectedItemPosition]
        val opacity = seekbarOpacity.progress

        // Save config
        prefsManager.saveQuickCalcConfig(
            widgetId = appWidgetId,
            slot1 = slot1, slot2 = slot2,
            slot3 = slot3, slot4 = slot4,
            theme = selectedTheme,
            opacity = opacity
        )

        // Push widget update
        val manager = AppWidgetManager.getInstance(this)
        QuickCalculateWidget.updateWidget(this, manager, appWidgetId)

        // Return OK
        val resultIntent = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
