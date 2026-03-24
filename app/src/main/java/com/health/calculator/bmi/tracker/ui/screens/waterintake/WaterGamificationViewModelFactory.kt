package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.health.calculator.bmi.tracker.data.repository.WaterGamificationRepository
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository

class WaterGamificationViewModelFactory(
    private val application: Application,
    private val gamificationRepo: WaterGamificationRepository,
    private val waterRepo: WaterIntakeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterGamificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterGamificationViewModel(application, gamificationRepo, waterRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
