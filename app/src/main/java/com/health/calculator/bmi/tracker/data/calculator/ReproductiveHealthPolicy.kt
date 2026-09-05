package com.health.calculator.bmi.tracker.data.calculator

import com.health.calculator.bmi.tracker.data.model.HealthStatus

/** Shared safety wording for calculators that expose pregnancy or lactation context. */
object ReproductiveHealthPolicy {
    const val DISCLAIMER =
        "Pregnancy and breastfeeding can change fluid and energy needs. This app does not personalize those needs or replace prenatal/postpartum care. Use this as general information and ask your care team for advice specific to you."

    const val NO_AUTOMATIC_ADJUSTMENT =
        "No automatic target is added; needs vary with stage, feeding, climate, activity and medical advice."

    fun disclaimerFor(status: HealthStatus): String? = when (status) {
        HealthStatus.PREGNANT, HealthStatus.BREASTFEEDING -> DISCLAIMER
        else -> null
    }
}
