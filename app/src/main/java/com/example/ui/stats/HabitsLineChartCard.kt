package com.example.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IOSCard
import com.example.ui.theme.mindColors

@Composable
fun HabitsLineChartCard(
    data: List<Float>,
    labels: List<String>,
    isEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    IOSCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.stats_habits_chart),
                color = mindColors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            if (isEmpty || data.isEmpty()) {
                EmptyChartState(
                    message = stringResource(R.string.stats_empty)
                )
            } else {
                HabitsLineChart(
                    data = data,
                    labels = labels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }
    }
}

@Composable
private fun HabitsLineChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 26.dp)
        ) {
            val width = size.width
            val height = size.height

            // 0%, 50%, 100% horizontal guidelines
            val levels = listOf(0f, 0.5f, 1f)
            levels.forEach { level ->
                val y = height * (1f - level)
                drawLine(
                    color = mindColors.separatorThin.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }

            val maxVal = 100f
            val count = data.size

            val points = data.mapIndexed { index, value ->
                val x = if (count > 1) (index.toFloat() / (count - 1)) * width else width / 2f
                val y = height * (1f - (value / maxVal).coerceIn(0f, 1f))
                Offset(x, y)
            }

            // Fill area
            if (points.isNotEmpty()) {
                val fillPath = Path().apply {
                    points.forEachIndexed { i, pt ->
                        if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                    }
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            mindColors.primary.copy(alpha = 0.35f),
                            mindColors.primary.copy(alpha = 0.0f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Line
                val strokePath = Path().apply {
                    points.forEachIndexed { i, pt ->
                        if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = mindColors.primary,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Dots
                points.forEach { pt ->
                    drawCircle(
                        color = mindColors.background,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = mindColors.primary,
                        radius = 2.5.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        // Labels at bottom
        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        color = mindColors.textSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
