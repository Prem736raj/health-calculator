package com.health.calculator.bmi.tracker.core.security

import android.app.Application

interface AppCheckInitializer {
    fun initialize(application: Application)
}
