package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import java.util.Locale

@Composable
fun MindTheme(
    darkTheme: Boolean = true,
    isArabic: Boolean = Locale.getDefault().language == "ar",
    content: @Composable () -> Unit
) {
    val mindColors = if (darkTheme) DarkColors else LightColors
    val mindTypography = createMindTypography(isArabic)

    val colorScheme = (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
        primary = mindColors.primary,
        onPrimary = mindColors.textInverse,
        primaryContainer = mindColors.surfaceElevated,
        onPrimaryContainer = mindColors.textPrimary,
        secondary = mindColors.accentBlue,
        onSecondary = mindColors.textPrimary,
        tertiary = mindColors.accentOrange,
        background = mindColors.background,
        onBackground = mindColors.textPrimary,
        surface = mindColors.surface,
        onSurface = mindColors.textPrimary,
        surfaceVariant = mindColors.surfaceElevated,
        onSurfaceVariant = mindColors.textSecondary,
        error = mindColors.error,
        onError = mindColors.textInverse,
        outline = mindColors.separatorThin,
        outlineVariant = mindColors.separatorStrong
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = mindColors.background.toArgb()
                window.navigationBarColor = mindColors.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalMindColors provides mindColors,
        LocalMindTypography provides mindTypography,
        LocalMindShapes provides MindShapes,
        LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = createM3Typography(mindTypography),
            shapes = M3Shapes,
            content = content
        )
    }
}
