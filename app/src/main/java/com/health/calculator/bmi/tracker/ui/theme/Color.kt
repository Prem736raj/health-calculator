package com.health.calculator.bmi.tracker.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PREMIUM PRIMARY PALETTE - Glassmorphism & Neon Focus
// ============================================================================

// Light Theme Primary
val PrimaryLight = Color(0xFF1E1E24)          // Deep slate/almost black
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE2E2F0)
val OnPrimaryContainerLight = Color(0xFF111116)

// Dark Theme Primary
val PrimaryDark = Color(0xFFFFFFFF)
val OnPrimaryDark = Color(0xFF1E1E24)
val PrimaryContainerDark = Color(0xFF2B2D42)
val OnPrimaryContainerDark = Color(0xFFFFFFFF)

// ============================================================================
// SECONDARY PALETTE - Sleek Accents
// ============================================================================

// Light Theme Secondary
val SecondaryLight = Color(0xFF4A4E69)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFF2E9E4)
val OnSecondaryContainerLight = Color(0xFF22223B)

// Dark Theme Secondary
val SecondaryDark = Color(0xFF9A8C98)
val OnSecondaryDark = Color(0xFF22223B)
val SecondaryContainerDark = Color(0xFF4A4E69)
val OnSecondaryContainerDark = Color(0xFFF2E9E4)

// ============================================================================
// TERTIARY PALETTE
// ============================================================================

val TertiaryLight = Color(0xFFC9ADA7)
val OnTertiaryLight = Color(0xFF22223B)
val TertiaryContainerLight = Color(0xFFF2E9E4)
val OnTertiaryContainerLight = Color(0xFF4A4E69)

val TertiaryDark = Color(0xFFC9ADA7)
val OnTertiaryDark = Color(0xFF22223B)
val TertiaryContainerDark = Color(0xFF4A4E69)
val OnTertiaryContainerDark = Color(0xFFF2E9E4)

// ============================================================================
// ERROR PALETTE
// ============================================================================

val ErrorLight = Color(0xFFD90429)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFB3B3)
val OnErrorContainerLight = Color(0xFF410002)

val ErrorDark = Color(0xFFEF233C)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// ============================================================================
// BACKGROUND & SURFACE - Light Theme (Ultra Clean)
// ============================================================================

val BackgroundLight = Color(0xFFF8F9FA)
val OnBackgroundLight = Color(0xFF1E1E24)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1E1E24)
val SurfaceVariantLight = Color(0xFFE9ECEF)
val OnSurfaceVariantLight = Color(0xFF495057)
val SurfaceTintLight = PrimaryLight
val InverseSurfaceLight = Color(0xFF212529)
val InverseOnSurfaceLight = Color(0xFFF8F9FA)
val InversePrimaryLight = PrimaryDark

// ============================================================================
// BACKGROUND & SURFACE - Dark Theme (Deep Modern)
// ============================================================================

val BackgroundDark = Color(0xFF0F1014) // Very deep dark, not pure black
val OnBackgroundDark = Color(0xFFE9ECEF)
val SurfaceDark = Color(0xFF181A20)
val OnSurfaceDark = Color(0xFFE9ECEF)
val SurfaceVariantDark = Color(0xFF242731)
val OnSurfaceVariantDark = Color(0xFFAEB1B9)
val SurfaceTintDark = PrimaryDark
val InverseSurfaceDark = Color(0xFFE9ECEF)
val InverseOnSurfaceDark = Color(0xFF0F1014)
val InversePrimaryDark = PrimaryLight

// ============================================================================
// OUTLINE & SCRIM
// ============================================================================

val OutlineLight = Color(0xFFCED4DA)
val OutlineVariantLight = Color(0xFFDEE2E6)

val OutlineDark = Color(0xFF343A40)
val OutlineVariantDark = Color(0xFF212529)

val ScrimLight = Color(0xFF000000)
val ScrimDark = Color(0xFF000000)

// ============================================================================
// HEALTH CATEGORY COLORS
// ============================================================================

object HealthColors {
    val Healthy = Color(0xFF06D6A0)              
    val HealthyLight = Color(0xFFE6F9F5)         
    val HealthyDark = Color(0xFF06D6A0)           

    val Good = Color(0xFF118AB2)                  
    val GoodLight = Color(0xFFE7F3F8)
    val GoodDark = Color(0xFF118AB2)

    val Warning = Color(0xFFFFD166)               
    val WarningLight = Color(0xFFFFFBE6)
    val WarningDark = Color(0xFFFFD166)

    val Caution = Color(0xFFF77F00)               
    val CautionLight = Color(0xFFFEF2E5)
    val CautionDark = Color(0xFFF77F00)

    val Danger = Color(0xFFEF476F)                
    val DangerLight = Color(0xFFFDECEF)
    val DangerDark = Color(0xFFEF476F)

    val Severe = Color(0xFF7209B7)                
    val SevereLight = Color(0xFFF1E6F8)
    val SevereDark = Color(0xFF7209B7)

    val BelowNormal = Color(0xFF4CC9F0)           
    val BelowNormalLight = Color(0xFFEDFAFD)
    val BelowNormalDark = Color(0xFF4CC9F0)

    val Info = Color(0xFF3A86FF)                  
    val InfoLight = Color(0xFFEBF3FF)
    val InfoDark = Color(0xFF3A86FF)
}

object ChartColors {
    val Primary = Color(0xFF3A86FF)
    val Secondary = Color(0xFF8338EC)
    val Tertiary = Color(0xFFFF006E)
    val Accent1 = Color(0xFFFB5607)
    val Accent2 = Color(0xFFFFBE0B)
    val Accent3 = Color(0xFF06D6A0)

    val GridLine = Color(0xFFE9ECEF)
    val GridLineDark = Color(0xFF212529)

    val GradientStart = Color(0xFF3A86FF)
    val GradientEnd = Color(0x333A86FF)
    val GradientStartDark = Color(0xFF3A86FF)
    val GradientEndDark = Color(0x333A86FF)
}

object CalculatorColors {
    val BMI = Color(0xFF06D6A0)                   
    val BMR = Color(0xFFFFBE0B)                    
    val BloodPressure = Color(0xFFFF006E)          
    val WaistToHip = Color(0xFF8338EC)             
    val WaterIntake = Color(0xFF3A86FF)             
    val MetabolicSyndrome = Color(0xFFFB5607)      
    val BSA = Color(0xFF4CC9F0)                     
    val IdealWeight = Color(0xFF06D6A0)             
    val DailyCalorie = Color(0xFFFFBE0B)            
    val HeartRateZone = Color(0xFFFF006E)           
}

val HealthGreen = Color(0xFF06D6A0)
val HealthYellow = Color(0xFFFFBE0B)
val HealthOrange = Color(0xFFFB5607)
val HealthRed = Color(0xFFFF006E)
val HealthBlue = Color(0xFF3A86FF)
val HealthTeal = Color(0xFF4CC9F0)
