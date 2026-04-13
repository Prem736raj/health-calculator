package com.health.calculator.bmi.tracker.widget.core

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import com.health.calculator.bmi.tracker.widget.WidgetTheme

/**
 * Advanced theme engine supporting:
 * - System light/dark detection
 * - Manual override (light/dark/transparent)
 * - Material You dynamic colors (Android 12+)
 * - Background opacity
 * - Consistent color tokens across all widgets
 */
object WidgetThemeEngine {

    data class WidgetColorScheme(
        // Surfaces
        val surfacePrimary: Int,
        val surfaceCard: Int,
        val surfaceAccent: Int,

        // Text
        val onSurfacePrimary: Int,
        val onSurfaceSecondary: Int,
        val onSurfaceTertiary: Int,

        // Status / accent
        val accentBlue: Int,
        val accentGreen: Int,
        val accentRed: Int,
        val accentOrange: Int,
        val accentPurple: Int,

        // Borders
        val borderColor: Int,
        val dividerColor: Int,

        // Misc
        val progressTrack: Int,
        val shadowColor: Int,
        val isLight: Boolean
    )

    // ── Resolve scheme from theme setting + system ────────────────────

    fun resolveScheme(
        context: Context,
        theme: WidgetTheme,
        opacity: Int = 100
    ): WidgetColorScheme {
        val isDarkSystem = isSystemDark(context)
        val alpha        = opacityToAlpha(opacity)

        return when (theme) {
            WidgetTheme.SYSTEM      -> if (isDarkSystem) darkScheme(alpha) else lightScheme(alpha)
            WidgetTheme.LIGHT       -> lightScheme(alpha)
            WidgetTheme.DARK        -> darkScheme(alpha)
            WidgetTheme.TRANSPARENT -> transparentScheme(alpha, isDarkSystem)
        }
    }

    // ── Material You (Android 12+) ────────────────────────────────────

    fun resolveMaterialYouScheme(
        context: Context,
        opacity: Int = 100
    ): WidgetColorScheme? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null

        return try {
            val isDark  = isSystemDark(context)
            val alpha   = opacityToAlpha(opacity)
            val res     = context.resources

            // Extract Material You dynamic colors
            val colorPrimary    = res.getColor(android.R.color.system_accent1_500, context.theme)
            val colorSurface    = res.getColor(
                if (isDark) android.R.color.system_neutral1_800
                else android.R.color.system_neutral1_100,
                context.theme
            )
            val colorOnSurface  = res.getColor(
                if (isDark) android.R.color.system_neutral1_100
                else android.R.color.system_neutral1_900,
                context.theme
            )
            val colorCardBg     = res.getColor(
                if (isDark) android.R.color.system_neutral1_700
                else android.R.color.system_neutral1_50,
                context.theme
            )
            val colorSecondary  = res.getColor(android.R.color.system_accent2_500, context.theme)

            WidgetColorScheme(
                surfacePrimary    = withAlpha(colorSurface, alpha),
                surfaceCard       = withAlpha(colorCardBg, alpha),
                surfaceAccent     = withAlpha(colorPrimary, 30),
                onSurfacePrimary  = colorOnSurface,
                onSurfaceSecondary= if (isDark) Color.parseColor("#90A4AE") else Color.parseColor("#607D8B"),
                onSurfaceTertiary = if (isDark) Color.parseColor("#546E7A") else Color.parseColor("#90A4AE"),
                accentBlue        = colorPrimary,
                accentGreen       = Color.parseColor("#4CAF50"),
                accentRed         = Color.parseColor("#F44336"),
                accentOrange      = Color.parseColor("#FF9800"),
                accentPurple      = colorSecondary,
                borderColor       = withAlpha(colorOnSurface, 25),
                dividerColor      = withAlpha(colorOnSurface, 18),
                progressTrack     = withAlpha(colorOnSurface, 20),
                shadowColor       = Color.parseColor("#33000000"),
                isLight           = !isDark
            )
        } catch (e: Exception) {
            null // Fall back to static theme
        }
    }

    // ── Light Scheme ──────────────────────────────────────────────────

    private fun lightScheme(alpha: Int) = WidgetColorScheme(
        surfacePrimary     = withAlpha(Color.parseColor("#FAFEFF"), alpha),
        surfaceCard        = withAlpha(Color.parseColor("#FFFFFF"), alpha),
        surfaceAccent      = Color.parseColor("#E3F2FD"),
        onSurfacePrimary   = Color.parseColor("#1A237E"),
        onSurfaceSecondary = Color.parseColor("#546E7A"),
        onSurfaceTertiary  = Color.parseColor("#90A4AE"),
        accentBlue         = Color.parseColor("#2196F3"),
        accentGreen        = Color.parseColor("#4CAF50"),
        accentRed          = Color.parseColor("#F44336"),
        accentOrange       = Color.parseColor("#FF9800"),
        accentPurple       = Color.parseColor("#9C27B0"),
        borderColor        = Color.parseColor("#E3EDF7"),
        dividerColor       = Color.parseColor("#EEEEEE"),
        progressTrack      = Color.parseColor("#ECEFF1"),
        shadowColor        = Color.parseColor("#1A000000"),
        isLight            = true
    )

    // ── Dark Scheme ───────────────────────────────────────────────────

    private fun darkScheme(alpha: Int) = WidgetColorScheme(
        surfacePrimary     = withAlpha(Color.parseColor("#1A2332"), alpha),
        surfaceCard        = withAlpha(Color.parseColor("#1E2D40"), alpha),
        surfaceAccent      = Color.parseColor("#0D47A1"),
        onSurfacePrimary   = Color.parseColor("#E3F2FD"),
        onSurfaceSecondary = Color.parseColor("#90A4AE"),
        onSurfaceTertiary  = Color.parseColor("#607D8B"),
        accentBlue         = Color.parseColor("#64B5F6"),
        accentGreen        = Color.parseColor("#81C784"),
        accentRed          = Color.parseColor("#EF9A9A"),
        accentOrange       = Color.parseColor("#FFB74D"),
        accentPurple       = Color.parseColor("#CE93D8"),
        borderColor        = Color.parseColor("#263547"),
        dividerColor       = Color.parseColor("#1E2D40"),
        progressTrack      = Color.parseColor("#2C3E50"),
        shadowColor        = Color.parseColor("#33000000"),
        isLight            = false
    )

    // ── Transparent Scheme ────────────────────────────────────────────

    private fun transparentScheme(alpha: Int, isDark: Boolean): WidgetColorScheme {
        val bgAlpha   = (alpha * 0.25f).toInt()
        val cardAlpha = (alpha * 0.15f).toInt()
        return WidgetColorScheme(
            surfacePrimary     = withAlpha(if (isDark) Color.parseColor("#1A2332") else Color.parseColor("#FFFFFF"), bgAlpha),
            surfaceCard        = withAlpha(Color.parseColor("#FFFFFF"), cardAlpha),
            surfaceAccent      = withAlpha(Color.parseColor("#2196F3"), 20),
            onSurfacePrimary   = Color.parseColor("#FFFFFF"),
            onSurfaceSecondary = Color.parseColor("#CFD8DC"),
            onSurfaceTertiary  = Color.parseColor("#90A4AE"),
            accentBlue         = Color.parseColor("#64B5F6"),
            accentGreen        = Color.parseColor("#81C784"),
            accentRed          = Color.parseColor("#EF9A9A"),
            accentOrange       = Color.parseColor("#FFB74D"),
            accentPurple       = Color.parseColor("#CE93D8"),
            borderColor        = withAlpha(Color.WHITE, 30),
            dividerColor       = withAlpha(Color.WHITE, 20),
            progressTrack      = withAlpha(Color.WHITE, 25),
            shadowColor        = Color.parseColor("#33000000"),
            isLight            = false
        )
    }

    // ── Apply scheme to RemoteViews root ──────────────────────────────

    fun applySchemeToRoot(
        views: android.widget.RemoteViews,
        rootId: Int,
        scheme: WidgetColorScheme
    ) {
        views.setInt(rootId, "setBackgroundColor", scheme.surfacePrimary)
    }

    fun applySchemeToCard(
        views: android.widget.RemoteViews,
        cardId: Int,
        scheme: WidgetColorScheme
    ) {
        views.setInt(cardId, "setBackgroundColor", scheme.surfaceCard)
    }

    fun applyTextColors(
        views: android.widget.RemoteViews,
        primaryIds: List<Int>,
        secondaryIds: List<Int>,
        scheme: WidgetColorScheme
    ) {
        primaryIds.forEach   { views.setTextColor(it, scheme.onSurfacePrimary) }
        secondaryIds.forEach { views.setTextColor(it, scheme.onSurfaceSecondary) }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    fun isSystemDark(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

    private fun opacityToAlpha(opacity: Int): Int =
        ((opacity.coerceIn(0, 100) / 100f) * 255).toInt()

    // ── Status color (health-aware) ───────────────────────────────────

    fun statusColor(value: Float, thresholds: List<Pair<Float, String>>): Int {
        val entry = thresholds.lastOrNull { value >= it.first }
        return when (entry?.second) {
            "green"  -> Color.parseColor("#4CAF50")
            "yellow" -> Color.parseColor("#FFC107")
            "orange" -> Color.parseColor("#FF9800")
            "red"    -> Color.parseColor("#F44336")
            "blue"   -> Color.parseColor("#2196F3")
            else     -> Color.parseColor("#9E9E9E")
        }
    }
}
