package com.example.ui.stats

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IOSCard
import com.example.ui.theme.mindColors

@Composable
fun ChartCard(
    todayData: List<ChartPoint>,
    yesterdayData: List<ChartPoint>,
    showYesterday: Boolean,
    onToggleYesterday: () -> Unit,
    isEmpty: Boolean,
    period: StatsPeriod,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    IOSCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header row: Title + "الأمس" button NEXT TO TITLE
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (period) {
                        StatsPeriod.TODAY -> stringResource(R.string.stats_today_chart)
                        StatsPeriod.WEEK -> stringResource(R.string.stats_week_chart)
                        StatsPeriod.MONTH -> stringResource(R.string.stats_month_chart)
                    },
                    color = mindColors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                // "الأمس" button NEXT TO TITLE
                YesterdayToggleButton(
                    isActive = showYesterday,
                    onClick = onToggleYesterday
                )
            }

            Spacer(Modifier.height(16.dp))

            // Chart area
            if (isEmpty) {
                EmptyChartState(
                    message = stringResource(R.string.stats_empty)
                )
            } else {
                StatsChart(
                    todayData = todayData,
                    yesterdayData = if (showYesterday) yesterdayData else emptyList(),
                    showYesterday = showYesterday
                )
            }

            // Legend (only when yesterday visible)
            if (showYesterday && !isEmpty) {
                Spacer(Modifier.height(12.dp))
                ChartLegend()
            }
        }
    }
}

@Composable
fun YesterdayToggleButton(
    isActive: Boolean,
    onClick: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val view = LocalView.current

    val bgColor by animateColorAsState(
        targetValue = if (isActive) mindColors.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "btn_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) mindColors.primary else mindColors.separatorStrong,
        animationSpec = tween(200),
        label = "btn_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) mindColors.textInverse else mindColors.textSecondary,
        animationSpec = tween(200),
        label = "btn_text"
    )

    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (isActive) stringResource(R.string.stats_hide) else stringResource(R.string.stats_show_yesterday),
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChartLegend() {
    val mindColors = MaterialTheme.mindColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(
            color = mindColors.primary,
            label = stringResource(R.string.stats_today),
            isDashed = false
        )
        Spacer(Modifier.width(24.dp))
        LegendItem(
            color = mindColors.textTertiary,
            label = stringResource(R.string.stats_yesterday),
            isDashed = true
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isDashed: Boolean
) {
    val mindColors = MaterialTheme.mindColors

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(3.dp)
                .then(
                    if (isDashed) {
                        Modifier.drawBehind {
                            drawLine(
                                color = color,
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = size.height,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(8f, 8f)
                                )
                            )
                        }
                    } else {
                        Modifier.background(
                            color,
                            RoundedCornerShape(2.dp)
                        )
                    }
                )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = mindColors.textSecondary,
            fontSize = 12.sp
        )
    }
}
