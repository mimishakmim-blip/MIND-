package com.example.ui.stats

import com.example.i18n.tr
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IOSCard
import com.example.ui.theme.mindColors
import java.util.Locale

@Composable
fun StatsGrid(
    totalHabits: Int,
    totalSteps: Int,
    totalWater: Int,
    totalStudyMinutes: Int,
    totalSleepMinutes: Int,
    avgHabitPercent: Int,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                icon = Icons.Default.CheckCircle,
                iconColor = mindColors.primary,
                label = stringResource(R.string.stats_habits),
                value = "$totalHabits",
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Default.DirectionsWalk,
                iconColor = mindColors.accentBlue,
                label = stringResource(R.string.stats_steps),
                value = formatNumber(totalSteps),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                icon = Icons.Default.WaterDrop,
                iconColor = mindColors.accentBlue,
                label = stringResource(R.string.stats_water),
                value = "$totalWater",
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Default.School,
                iconColor = mindColors.accentPurple,
                label = stringResource(R.string.stats_study),
                value = tr("$totalStudyMinutes د", "$totalStudyMinutes m"),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                icon = Icons.Default.Bedtime,
                iconColor = mindColors.accentPurple,
                label = stringResource(R.string.stats_sleep),
                value = formatSleepDuration(totalSleepMinutes),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = Icons.Default.Percent,
                iconColor = mindColors.primary,
                label = stringResource(R.string.stats_avg_habits),
                value = "$avgHabitPercent%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    IOSCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                color = mindColors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                color = mindColors.textSecondary,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatNumber(n: Int): String {
    return when {
        n >= 1000000 -> String.format(Locale.US, "%.1fM", n / 1000000f)
        n >= 1000 -> String.format(Locale.US, "%.1fK", n / 1000f)
        else -> n.toString()
    }
}

private fun formatSleepDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return String.format(Locale.US, "%d:%02d", h, m)
}
