package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * iOS 26 Liquid Glass Design System Palette for Mind OS.
 * Strictly adheres to muted, sophisticated dark/light palettes.
 */
data class MindColors(
    val background: Color,
    val backgroundElevated: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceHighlight: Color,

    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val primaryGlow: Color,

    val accentBlue: Color,
    val accentGreen: Color,
    val accentOrange: Color,
    val accentPurple: Color,
    val accentPink: Color,
    val accentRed: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textQuaternary: Color,
    val textInverse: Color,

    val success: Color,
    val error: Color,
    val warning: Color,
    val info: Color,

    val separatorThin: Color,
    val separatorStrong: Color,

    val isDark: Boolean,

    val heroGradient: Brush,
    val cardGradient: Brush
)

val DarkColors = MindColors(
    background = Color(0xFF0D0D0F),
    backgroundElevated = Color(0xFF141416),
    surface = Color(0xFF1C1C1F),
    surfaceElevated = Color(0xFF26262A),
    surfaceHighlight = Color(0xFF2E2E33),

    primary = Color(0xFFE8C547), // Muted yellow
    primaryLight = Color(0xFFF0D060),
    primaryDark = Color(0xFFC9A935),
    primaryGlow = Color(0x40E8C547),

    accentBlue = Color(0xFF5B9BD5),
    accentGreen = Color(0xFF52C97B),
    accentOrange = Color(0xFFE8965A),
    accentPurple = Color(0xFFA78BC7),
    accentPink = Color(0xFFE17BA5),
    accentRed = Color(0xFFE57171),

    textPrimary = Color(0xFFF5F5F7),
    textSecondary = Color(0xFFA0A0A8),
    textTertiary = Color(0xFF6E6E78),
    textQuaternary = Color(0xFF48484E),
    textInverse = Color(0xFF1C1C1F),

    success = Color(0xFF52C97B),
    error = Color(0xFFE57171),
    warning = Color(0xFFE8965A),
    info = Color(0xFF5B9BD5),

    separatorThin = Color(0xFF2A2A2E),
    separatorStrong = Color(0xFF3A3A40),

    isDark = true,

    heroGradient = Brush.linearGradient(
        listOf(Color(0xFFE8C547), Color(0xFFD4A942))
    ),
    cardGradient = Brush.verticalGradient(
        listOf(Color(0xFF1C1C1F), Color(0xFF161618))
    )
)

val LightColors = MindColors(
    background = Color(0xFFF5F5F7),
    backgroundElevated = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFAFAFC),
    surfaceHighlight = Color(0xFFE8E8EE),

    primary = Color(0xFFB8942E), // Dark amber for contrast
    primaryLight = Color(0xFFD4A942),
    primaryDark = Color(0xFF8F721F),
    primaryGlow = Color(0x33B8942E),

    accentBlue = Color(0xFF3B7DD8),
    accentGreen = Color(0xFF2FA85C),
    accentOrange = Color(0xFFD97A3D),
    accentPurple = Color(0xFF8A6DAF),
    accentPink = Color(0xFFC75A87),
    accentRed = Color(0xFFC84D4D),

    textPrimary = Color(0xFF1C1C1F),
    textSecondary = Color(0xFF6E6E78),
    textTertiary = Color(0xFFA0A0A8),
    textQuaternary = Color(0xFFC8C8CE),
    textInverse = Color(0xFFF5F5F7),

    success = Color(0xFF2FA85C),
    error = Color(0xFFC84D4D),
    warning = Color(0xFFD97A3D),
    info = Color(0xFF3B7DD8),

    separatorThin = Color(0xFFE0E0E5),
    separatorStrong = Color(0xFFC8C8CE),

    isDark = false,

    heroGradient = Brush.linearGradient(
        listOf(Color(0xFFB8942E), Color(0xFFD4A942))
    ),
    cardGradient = Brush.verticalGradient(
        listOf(Color(0xFFFFFFFF), Color(0xFFF2F2F6))
    )
)

val LocalMindColors = staticCompositionLocalOf { DarkColors }

val MaterialTheme.mindColors: MindColors
    @Composable
    @ReadOnlyComposable
    get() = LocalMindColors.current
