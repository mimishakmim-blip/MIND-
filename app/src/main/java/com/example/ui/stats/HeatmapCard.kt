package com.example.ui.stats

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IOSCard
import com.example.ui.theme.mindColors

data class HeatmapDay(
    val date: Long,
    val intensity: Float,  // 0.0 to 1.0
    val activityCount: Int
)

@Composable
fun HeatmapCard(
    days: List<HeatmapDay>,
    title: String,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    IOSCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = mindColors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            // 12 weeks × 7 days grid
            HeatmapGrid(days = days)

            Spacer(Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.heatmap_less),
                    color = mindColors.textTertiary,
                    fontSize = 10.sp
                )
                Spacer(Modifier.width(6.dp))

                for (i in 0..4) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(heatmapColor(i / 4f, mindColors.surfaceElevated, mindColors.primary))
                    )
                    Spacer(Modifier.width(3.dp))
                }

                Spacer(Modifier.width(3.dp))
                Text(
                    text = stringResource(R.string.heatmap_more),
                    color = mindColors.textTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun HeatmapGrid(days: List<HeatmapDay>) {
    val mindColors = MaterialTheme.mindColors
    val weeks = days.chunked(7)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weeks.forEach { week ->
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(heatmapColor(day.intensity, mindColors.surfaceElevated, mindColors.primary))
                    )
                }
            }
        }
    }
}

private fun heatmapColor(intensity: Float, surfaceColor: Color, primaryColor: Color): Color {
    return when {
        intensity <= 0f -> surfaceColor
        intensity < 0.25f -> primaryColor.copy(alpha = 0.25f)
        intensity < 0.5f -> primaryColor.copy(alpha = 0.5f)
        intensity < 0.75f -> primaryColor.copy(alpha = 0.75f)
        else -> primaryColor
    }
}
