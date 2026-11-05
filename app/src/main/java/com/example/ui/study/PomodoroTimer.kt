package com.example.ui.study

import com.example.i18n.tr
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.IOSProgressRing
import com.example.ui.theme.mindColors

enum class PomodoroState {
    IDLE, FOCUS, SHORT_BREAK, LONG_BREAK, PAUSED
}

@Composable
fun PomodoroTimer(
    state: PomodoroState,
    remainingSeconds: Int,
    totalSeconds: Int,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val progress = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds
    } else 1f

    // Timer numbers must always be LTR (English digits MM:SS)
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        Box(
            modifier = modifier
                .size(220.dp)
                .testTag("pomodoro_timer_ring"),
            contentAlignment = Alignment.Center
        ) {
            IOSProgressRing(
                progress = progress,
                size = 200.dp,
                strokeWidth = 10.dp,
                color = mindColors.primary,
                trackColor = mindColors.surfaceElevated
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(remainingSeconds),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = mindColors.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stateLabel(state),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = mindColors.textSecondary
                )
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

fun stateLabel(state: PomodoroState): String =
    when (state) {
        PomodoroState.IDLE -> tr("جاهز", "Ready")
        PomodoroState.FOCUS -> tr("تركيز", "Focus")
        PomodoroState.SHORT_BREAK -> tr("راحة قصيرة", "Short break")
        PomodoroState.LONG_BREAK -> tr("راحة طويلة", "Long break")
        PomodoroState.PAUSED -> tr("متوقف", "Paused")
    }
