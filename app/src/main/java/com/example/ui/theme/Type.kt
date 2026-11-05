package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal)
)

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal)
)

data class MindTypographyData(
    val display: TextStyle,
    val largeTitle: TextStyle,
    val title: TextStyle,
    val headline: TextStyle,
    val body: TextStyle,
    val callout: TextStyle,
    val subhead: TextStyle,
    val footnote: TextStyle,
    val caption: TextStyle,
    val micro: TextStyle
)

fun createMindTypography(isArabic: Boolean = false): MindTypographyData {
    val activeFont = if (isArabic) CairoFontFamily else InterFontFamily

    return MindTypographyData(
        display = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 41.sp,
            letterSpacing = (-0.5).sp
        ),
        largeTitle = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = 0.sp
        ),
        title = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        headline = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 25.sp,
            letterSpacing = 0.sp
        ),
        body = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp
        ),
        callout = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            letterSpacing = 0.sp
        ),
        subhead = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        ),
        footnote = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp
        ),
        caption = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp
        ),
        micro = TextStyle(
            fontFamily = activeFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.sp
        )
    )
}

val MindTypography = createMindTypography(false)

val LocalMindTypography = staticCompositionLocalOf { MindTypography }

val MaterialTheme.mindTypography: MindTypographyData
    @Composable
    @ReadOnlyComposable
    get() = LocalMindTypography.current

fun createM3Typography(mind: MindTypographyData): Typography {
    return Typography(
        displayLarge = mind.display,
        headlineLarge = mind.largeTitle,
        headlineMedium = mind.title,
        titleMedium = mind.headline,
        bodyLarge = mind.body,
        bodyMedium = mind.callout,
        bodySmall = mind.subhead,
        labelLarge = mind.footnote,
        labelMedium = mind.caption,
        labelSmall = mind.micro
    )
}
