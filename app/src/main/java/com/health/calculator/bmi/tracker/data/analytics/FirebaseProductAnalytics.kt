package com.health.calculator.bmi.tracker.data.analytics

import android.content.Context
import android.os.Bundle
import com.health.calculator.bmi.tracker.data.datastore.SettingsDataStore
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalytics
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalyticsEvent
import com.health.calculator.bmi.tracker.domain.analytics.ProductAnalyticsPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Analytics adapter with an explicit opt-in gate.
 *
 * The manifest disables automatic collection as a second safety net. This
 * adapter enables collection only after the user turns on the optional product
 * usage setting, and forwards only [ProductAnalyticsPolicy.sanitize] output.
 */
@Singleton
class FirebaseProductAnalytics @Inject constructor(
    @ApplicationContext context: Context,
    settingsDataStore: SettingsDataStore
) : ProductAnalytics {

    private val firebaseAnalytics: FirebaseAnalyticsBridge? =
        FirebaseAnalyticsBridge.create(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var enabled: Boolean = false

    init {
        // Keep the default closed even if a future manifest or SDK default changes.
        firebaseAnalytics?.setCollectionEnabled(false)
        scope.launch {
            settingsDataStore.productAnalyticsEnabledFlow
                .distinctUntilChanged()
                .catch { emit(false) }
                .collect { setCollectionEnabled(it) }
        }
    }

    private fun setCollectionEnabled(value: Boolean) {
        enabled = value
        firebaseAnalytics?.setCollectionEnabled(value)
    }

    override fun track(
        event: ProductAnalyticsEvent,
        parameters: Map<String, String>
    ) {
        if (!enabled) return
        val analytics = firebaseAnalytics ?: return
        val safeParameters = ProductAnalyticsPolicy.sanitize(event, parameters)
        val bundle = Bundle().apply {
            safeParameters.forEach { (key, value) -> putString(key, value) }
        }
        analytics.logEvent(event.eventName, bundle)
    }
}

/**
 * Reflection bridge keeps the analytics provider optional at compile time.
 * Firebase Analytics is packaged at runtime, but a missing/changed provider
 * simply makes tracking a no-op instead of blocking app startup.
 */
private class FirebaseAnalyticsBridge private constructor(
    private val instance: Any,
    private val setCollectionEnabledMethod: java.lang.reflect.Method,
    private val logEventMethod: java.lang.reflect.Method
) {
    fun setCollectionEnabled(enabled: Boolean) {
        runCatching { setCollectionEnabledMethod.invoke(instance, enabled) }
    }

    fun logEvent(eventName: String, bundle: Bundle) {
        runCatching { logEventMethod.invoke(instance, eventName, bundle) }
    }

    companion object {
        private const val CLASS_NAME = "com.google.firebase.analytics.FirebaseAnalytics"

        fun create(context: Context): FirebaseAnalyticsBridge? = runCatching {
            val analyticsClass = Class.forName(CLASS_NAME)
            val instance = analyticsClass
                .getMethod("getInstance", Context::class.java)
                .invoke(null, context)
                ?: error("Firebase Analytics returned no instance")
            val setCollectionEnabledMethod = analyticsClass.getMethod(
                "setAnalyticsCollectionEnabled",
                java.lang.Boolean.TYPE
            )
            val logEventMethod = analyticsClass.getMethod(
                "logEvent",
                String::class.java,
                Bundle::class.java
            )
            FirebaseAnalyticsBridge(instance, setCollectionEnabledMethod, logEventMethod)
        }.getOrNull()
    }
}
