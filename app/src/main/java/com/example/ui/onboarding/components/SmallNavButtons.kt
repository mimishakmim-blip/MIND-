package com.example.ui.onboarding.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.mindColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SmallNavButtons(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    canGoBack: Boolean = true,
    isLastPage: Boolean = false,
    modifier: Modifier = Modifier,
    nextTextOverride: String? = null,
    previousTextOverride: String? = null,
    onValidateNext: () -> Boolean = { true }
) {
    val mindColors = MaterialTheme.mindColors
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        scope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 350
                    -8f at 50
                    8f at 100
                    -8f at 150
                    8f at 200
                    -4f at 250
                    4f at 300
                    0f at 350
                }
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // PREVIOUS BUTTON (Start / Right in RTL) - 90x44dp capsule
        Box(
            modifier = Modifier
                .size(width = 90.dp, height = 44.dp)
                .alpha(if (canGoBack) 1.0f else 0.3f)
                .clip(RoundedCornerShape(999.dp))
                .border(1.dp, mindColors.separatorStrong, RoundedCornerShape(999.dp))
                .clickable(enabled = canGoBack) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onPrevious()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = previousTextOverride ?: stringResource(id = R.string.btn_previous),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = mindColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // NEXT / FINISH BUTTON (End / Left in RTL) - 100x44dp capsule
        Box(
            modifier = Modifier
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                .size(width = 100.dp, height = 44.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(mindColors.primary)
                .clickable {
                    if (onValidateNext()) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        onNext()
                    } else {
                        triggerShake()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = nextTextOverride
                        ?: if (isLastPage) stringResource(id = R.string.btn_finish)
                        else stringResource(id = R.string.btn_next),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = mindColors.textInverse
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isLastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = mindColors.textInverse,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
