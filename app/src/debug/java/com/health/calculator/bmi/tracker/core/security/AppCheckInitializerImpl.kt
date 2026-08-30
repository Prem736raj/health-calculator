package com.health.calculator.bmi.tracker.core.security

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class AppCheckInitializerImpl : AppCheckInitializer {
    override fun initialize(application: Application) {
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }
}
