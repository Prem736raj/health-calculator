package com.health.calculator.bmi.tracker.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.content.getSystemService

class HapticManager(
    private val hapticFeedback: HapticFeedback,
    private val context: Context
) {
    // Light tap — for button presses, toggles, selections
    fun lightTap() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // Medium impact — for result appearing, confirmations
    fun mediumImpact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService<VibratorManager>()
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.vibrate(
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    )
                }
            }
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Heavy impact — for errors, warnings
    fun heavyImpact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService<VibratorManager>()
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                it.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Success pattern — for save confirmation, goal reached
    fun successPattern() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService<VibratorManager>()
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                val pattern = longArrayOf(0, 30, 60, 30)
                it.vibrate(
                    VibrationEffect.createWaveform(pattern, -1)
                )
            }
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Error pattern — for validation failures
    fun errorPattern() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService<VibratorManager>()
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                val pattern = longArrayOf(0, 40, 30, 40, 30, 40)
                it.vibrate(
                    VibrationEffect.createWaveform(pattern, -1)
                )
            }
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Tick — for slider changes
    fun tick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService<VibratorManager>()
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.vibrate(
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    )
                }
            }
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

@Composable
fun rememberHapticManager(): HapticManager {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    return remember(hapticFeedback, context) {
        HapticManager(hapticFeedback, context)
    }
}
