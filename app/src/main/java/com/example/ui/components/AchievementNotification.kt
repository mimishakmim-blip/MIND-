package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.ui.theme.mindColors
import com.example.data.constants.achievementTitle
import com.example.data.constants.achievementDescription



@Composable
fun AchievementObserver() {
    var current by remember { mutableStateOf<Achievement?>(null) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        AchievementEventBus.events.collect { a ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            current = a
        }
    }

    AchievementNotification(
        achievement = current,
        onDismiss = { current = null }
    )
}

/**
 * Achievement card: slides in from the top, an animated check mark draws itself, and the
 * achievement name sits beside it with the goal underneath.
 */
@Composable
fun AchievementNotification(
    achievement: Achievement?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (achievement == null) return
    val colors = MaterialTheme.mindColors
    var visible by remember(achievement.id) { mutableStateOf(false) }
    val progress = remember(achievement.id) { Animatable(0f) }

    LaunchedEffect(achievement.id) {
        visible = true
        progress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
        delay(3200)
        visible = false
        delay(450)
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(tween(450)) { -it } + fadeIn(tween(300)),
            exit = slideOutVertically(tween(350)) { -it } + fadeOut(tween(300))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .widthIn(max = 380.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF17171A))
                    .border(0.5.dp, colors.separatorStrong, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedCheck(progress = progress.value, color = colors.accentGreen)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = achievementTitle(achievement.id, achievement.title),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = achievementDescription(achievement.id, achievement.description),
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/** Circle that draws itself, then a check mark. */
@Composable
private fun AnimatedCheck(progress: Float, color: Color) {
    Canvas(modifier = Modifier.size(46.dp)) {
        val strokeWidth = 4.dp.toPx()
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val circleProgress = (progress / 0.6f).coerceIn(0f, 1f)
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * circleProgress,
            useCenter = false,
            style = stroke,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(size.width - strokeWidth, size.height - strokeWidth)
        )
        val t = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f)
        if (t > 0f) {
            val p1 = Offset(size.width * 0.28f, size.height * 0.52f)
            val p2 = Offset(size.width * 0.44f, size.height * 0.68f)
            val p3 = Offset(size.width * 0.74f, size.height * 0.34f)
            val path = Path().apply {
                moveTo(p1.x, p1.y)
                if (t < 0.4f) {
                    val e = lerp(p1, p2, t / 0.4f)
                    lineTo(e.x, e.y)
                } else {
                    lineTo(p2.x, p2.y)
                    val e = lerp(p2, p3, (t - 0.4f) / 0.6f)
                    lineTo(e.x, e.y)
                }
            }
            drawPath(path = path, color = color, style = stroke)
        }
    }
}
