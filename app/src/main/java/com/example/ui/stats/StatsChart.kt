package com.example.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.mindColors

data class ChartPoint(
    val x: Float,
    val y: Float,
    val label: String
)

@Composable
fun StatsChart(
    todayData: List<ChartPoint>,
    yesterdayData: List<ChartPoint>,
    showYesterday: Boolean,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    val maxY = 2f
    val minY = 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp)
    ) {
        // Y-axis emoji indicators on start edge
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(text = "😊", fontSize = 16.sp)
                Text(text = "😐", fontSize = 16.sp)
                Text(text = "😕", fontSize = 16.sp)
            }
        }

        // Line Chart Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 36.dp, end = 12.dp, top = 12.dp, bottom = 24.dp)
        ) {
            val width = size.width
            val height = size.height

            // Horizontal reference guide lines
            val yPositions = listOf(0f, height * 0.5f, height)
            yPositions.forEach { y ->
                drawLine(
                    color = mindColors.separatorThin.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }

            fun calculateOffset(point: ChartPoint, totalPoints: Int): Offset {
                val xNorm = if (totalPoints > 1) point.x / (totalPoints - 1).toFloat() else 0.5f
                val yNorm = 1f - ((point.y - minY) / (maxY - minY)).coerceIn(0f, 1f)
                return Offset(xNorm * width, yNorm * height)
            }

            // Draw Yesterday line (gray dashed)
            if (showYesterday && yesterdayData.isNotEmpty()) {
                val yesterdayOffsets = yesterdayData.map { calculateOffset(it, yesterdayData.size) }
                val yesterdayPath = Path().apply {
                    yesterdayOffsets.forEachIndexed { i, offset ->
                        if (i == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                    }
                }
                drawPath(
                    path = yesterdayPath,
                    color = mindColors.textTertiary,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )
                yesterdayOffsets.forEach { offset ->
                    drawCircle(
                        color = mindColors.textTertiary,
                        radius = 3.5.dp.toPx(),
                        center = offset
                    )
                }
            }

            // Draw Today line (solid yellow with gradient fill)
            if (todayData.isNotEmpty()) {
                val todayOffsets = todayData.map { calculateOffset(it, todayData.size) }

                // Area gradient
                val fillPath = Path().apply {
                    todayOffsets.forEachIndexed { i, offset ->
                        if (i == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                    }
                    lineTo(todayOffsets.last().x, height)
                    lineTo(todayOffsets.first().x, height)
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

                // Stroke line
                val strokePath = Path().apply {
                    todayOffsets.forEachIndexed { i, offset ->
                        if (i == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
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

                // Dots at data points
                todayOffsets.forEach { offset ->
                    drawCircle(
                        color = mindColors.background,
                        radius = 5.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = mindColors.primary,
                        radius = 3.5.dp.toPx(),
                        center = offset
                    )
                }
            }
        }
    }
}
