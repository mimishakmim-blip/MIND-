package com.example.ui.components

import com.example.i18n.tr
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.util.DateUtils
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

/**
 * Custom Canvas-rendered Flame with Duolingo-style aesthetics and animations.
 * When streak == 0, appears unlit (slate gray). When > 0, bursts with fiery gradients and pulse.
 */
@Composable
fun FlameCanvas(
    streak: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_glow"
    )

    val isActive = streak > 0

    Canvas(
        modifier = modifier
            .scale(if (isActive) pulseScale else 1f)
            .testTag("flame_canvas")
    ) {
        val width = size.width
        val height = size.height

        if (isActive) {
            // Radial halo glow behind active flame
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF9100).copy(alpha = glowAlpha),
                        Color(0xFFFF3D00).copy(alpha = glowAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.55f),
                    radius = width * 0.65f
                )
            )
        }

        // Outer Flame Path
        val outerPath = Path().apply {
            moveTo(width * 0.5f, height * 0.05f) // Flame top tip
            cubicTo(
                width * 0.75f, height * 0.25f,
                width * 0.95f, height * 0.55f,
                width * 0.78f, height * 0.82f
            )
            cubicTo(
                width * 0.68f, height * 0.96f,
                width * 0.32f, height * 0.96f,
                width * 0.22f, height * 0.82f
            )
            cubicTo(
                width * 0.05f, height * 0.55f,
                width * 0.25f, height * 0.25f,
                width * 0.5f, height * 0.05f
            )
            close()
        }

        val outerColors = if (isActive) {
            listOf(
                Color(0xFFFFEA00), // golden yellow at top
                Color(0xFFFF9100), // vibrant orange mid
                Color(0xFFFF3D00)  // intense flame red at base
            )
        } else {
            listOf(
                Color(0xFF8E8E98),
                Color(0xFF5A5A64),
                Color(0xFF383840)
            )
        }

        drawPath(
            path = outerPath,
            brush = Brush.verticalGradient(outerColors)
        )

        // Inner Core Flame Path
        val innerPath = Path().apply {
            moveTo(width * 0.5f, height * 0.35f)
            cubicTo(
                width * 0.65f, height * 0.50f,
                width * 0.70f, height * 0.70f,
                width * 0.60f, height * 0.84f
            )
            cubicTo(
                width * 0.55f, height * 0.90f,
                width * 0.45f, height * 0.90f,
                width * 0.40f, height * 0.84f
            )
            cubicTo(
                width * 0.30f, height * 0.70f,
                width * 0.35f, height * 0.50f,
                width * 0.5f, height * 0.35f
            )
            close()
        }

        val innerColors = if (isActive) {
            listOf(
                Color(0xFFFFFFFF).copy(alpha = 0.95f),
                Color(0xFFFFF59D),
                Color(0xFFFFD54F)
            )
        } else {
            listOf(
                Color(0xFFA0A0AA).copy(alpha = 0.7f),
                Color(0xFF6E6E78)
            )
        }

        drawPath(
            path = innerPath,
            brush = Brush.verticalGradient(innerColors)
        )

        // Subtle outline for crisp vector definition
        drawPath(
            path = outerPath,
            color = if (isActive) Color(0xFFFFD54F).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Duolingo-style Streak Flame Icon + Badge.
 */
@Composable
fun StreakFlame(
    streak: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showCount: Boolean = true
) {
    val mindColors = MaterialTheme.mindColors
    val isActive = streak > 0

    Column(
        modifier = modifier.testTag("streak_flame_component"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            FlameCanvas(
                streak = streak,
                modifier = Modifier.size(size)
            )
        }

        if (showCount) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$streak",
                color = if (isActive) mindColors.primary else mindColors.textSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Compact Pill for top navigation headers and list rows.
 */
@Composable
fun StreakPill(
    streak: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val mindColors = MaterialTheme.mindColors
    val isActive = streak > 0
    val view = LocalView.current

    val bgColor = if (isActive) {
        mindColors.primary.copy(alpha = 0.16f)
    } else {
        mindColors.surface.copy(alpha = 0.6f)
    }

    val borderColor = if (isActive) {
        mindColors.primary.copy(alpha = 0.4f)
    } else {
        mindColors.separatorThin
    }

    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(17.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FlameCanvas(
            streak = streak,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$streak",
            color = if (isActive) mindColors.primary else mindColors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Duolingo-style Streak Card with 7-day progress strip and liquid glass styling.
 */
@Composable
fun DuolingoStreakCard(
    currentStreak: Int,
    bestStreak: Int,
    completedDaysThisWeek: Set<Long>,
    modifier: Modifier = Modifier,
    onStreakClick: () -> Unit = {}
) {
    val mindColors = MaterialTheme.mindColors
    val isActive = currentStreak > 0
    val weekDays = remember { DateUtils.currentWeekDays() }
    val today = remember { DateUtils.startOfDay() }

    IOSGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .shadow(
                elevation = if (isActive) 12.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isActive) Color(0xFFFF9100) else Color.Transparent
            )
            .testTag("duolingo_streak_card"),
        shape = RoundedCornerShape(24.dp),
        tintAlpha = 0.85f,
        blurRadius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Big Flame + Streak Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FlameCanvas(
                        streak = currentStreak,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isActive) tr("شعلة $currentStreak أيام!", "$currentStreak-day streak!") else tr("شعلتك غير مشتعلة", "Your flame is out"),
                            color = if (isActive) mindColors.primary else mindColors.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isActive) tr("حافظ على استمراريتك اليوم!", "Keep your streak going today!") else tr("أكمل عادتك الأولى لإشعال الشعلة", "Complete your first habit to light the flame"),
                            color = mindColors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // Best streak badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(mindColors.surfaceElevated)
                        .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = tr("الأفضل 🏆", "Best 🏆"),
                            color = mindColors.textSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = tr("$bestStreak يوم", "$bestStreak days"),
                            color = mindColors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 7 Days Week Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(mindColors.surfaceElevated.copy(alpha = 0.5f))
                    .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEach { dayMillis ->
                    val isDone = completedDaysThisWeek.contains(dayMillis)
                    val isToday = dayMillis == today
                    val dayLabel = DateUtils.dayInitialAr(dayMillis)

                    DayPill(
                        dayLabel = dayLabel,
                        isDone = isDone,
                        isToday = isToday
                    )
                }
            }
        }
    }
}

@Composable
private fun DayPill(
    dayLabel: String,
    isDone: Boolean,
    isToday: Boolean
) {
    val mindColors = MaterialTheme.mindColors

    val circleBg = when {
        isDone -> mindColors.primary.copy(alpha = 0.22f)
        isToday -> mindColors.primary.copy(alpha = 0.08f)
        else -> mindColors.surface.copy(alpha = 0.4f)
    }

    val circleBorder = when {
        isDone -> mindColors.primary
        isToday -> mindColors.primary.copy(alpha = 0.5f)
        else -> mindColors.separatorThin
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(circleBg)
                .border(1.5.dp, circleBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                FlameCanvas(
                    streak = 1,
                    modifier = Modifier.size(20.dp)
                )
            } else if (isToday) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(mindColors.primary)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(mindColors.textTertiary.copy(alpha = 0.4f))
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = dayLabel,
            color = if (isToday) mindColors.primary else mindColors.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(name = "Active Streak Flame")
@Composable
fun StreakFlameActivePreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(Color(0xFF0D0D0F))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StreakFlame(streak = 5, size = 64.dp)
            Spacer(Modifier.height(16.dp))
            StreakPill(streak = 5)
            Spacer(Modifier.height(16.dp))
            DuolingoStreakCard(
                currentStreak = 5,
                bestStreak = 12,
                completedDaysThisWeek = setOf(DateUtils.startOfDay())
            )
        }
    }
}

@Preview(name = "Zero Streak Flame")
@Composable
fun StreakFlameZeroPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(Color(0xFF0D0D0F))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StreakFlame(streak = 0, size = 64.dp)
            Spacer(Modifier.height(16.dp))
            StreakPill(streak = 0)
            Spacer(Modifier.height(16.dp))
            DuolingoStreakCard(
                currentStreak = 0,
                bestStreak = 0,
                completedDaysThisWeek = emptySet()
            )
        }
    }
}
