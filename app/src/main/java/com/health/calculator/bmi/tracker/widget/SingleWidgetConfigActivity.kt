package com.health.calculator.bmi.tracker.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.health.calculator.bmi.tracker.R

class SingleWidgetConfigActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var prefsManager: WidgetPreferencesManager
    private lateinit var rgCalculator: RadioGroup
    private lateinit var themeSpinner: Spinner
    private lateinit var btnAdd: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_single_widget_config)

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
        setupThemeSpinner()
        setupButtons()
    }

    private fun initViews() {
        rgCalculator = findViewById(R.id.rg_calculator_select)
        themeSpinner = findViewById(R.id.single_theme_spinner)
        btnAdd       = findViewById(R.id.btn_single_add_widget)
        btnCancel    = findViewById(R.id.btn_single_cancel)
    }

    private fun setupThemeSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            WidgetTheme.spinnerLabels()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        themeSpinner.adapter = adapter
    }

    private fun getSelectedCalculator(): CalculatorType {
        return when (rgCalculator.checkedRadioButtonId) {
            R.id.rb_bmi        -> CalculatorType.BMI
            R.id.rb_bp         -> CalculatorType.BLOOD_PRESSURE
            R.id.rb_water      -> CalculatorType.WATER
            R.id.rb_calories   -> CalculatorType.CALORIES
            R.id.rb_heart_rate -> CalculatorType.HEART_RATE
            else               -> CalculatorType.BMI
        }
    }

    private fun setupButtons() {
        btnAdd.setOnClickListener {
            val selectedCalc  = getSelectedCalculator()
            val selectedTheme = WidgetTheme.values()[themeSpinner.selectedItemPosition]

            prefsManager.saveSingleCalcConfig(
                widgetId  = appWidgetId,
                calcType  = selectedCalc,
                theme     = selectedTheme,
                opacity   = 100
            )

            val manager = AppWidgetManager.getInstance(this)
            SingleCalculatorWidget.updateWidget(this, manager, appWidgetId)

            val resultIntent = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        btnCancel.setOnClickListener { finish() }
    }
}
