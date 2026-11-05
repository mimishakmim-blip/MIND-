package com.example.ui.stats

import com.example.i18n.tr
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IOSCard
import com.example.ui.theme.mindColors

@Composable
fun SummaryCard(
    todayCompleted: Int,
    todayTotal: Int,
    yesterdayCompleted: Int,
    yesterdayTotal: Int,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    val todayPercent = if (todayTotal > 0) (todayCompleted * 100) / todayTotal else 0
    val yesterdayPercent = if (yesterdayTotal > 0) (yesterdayCompleted * 100) / yesterdayTotal else 0
    val diff = todayPercent - yesterdayPercent

    IOSCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.stats_summary),
                color = mindColors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            SummaryRow(
                label = stringResource(R.string.stats_today),
                value = tr("$todayCompleted من $todayTotal", "$todayCompleted of $todayTotal"),
                percent = "$todayPercent%",
                emoji = ratingEmoji(todayPercent)
            )

            Spacer(Modifier.height(12.dp))

            SummaryRow(
                label = stringResource(R.string.stats_yesterday),
                value = tr("$yesterdayCompleted من $yesterdayTotal", "$yesterdayCompleted of $yesterdayTotal"),
                percent = "$yesterdayPercent%",
                emoji = ratingEmoji(yesterdayPercent)
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = mindColors.separatorThin)
            Spacer(Modifier.height(12.dp))

            ComparisonIndicator(diff = diff)
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    percent: String,
    emoji: String
) {
    val mindColors = MaterialTheme.mindColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = mindColors.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = value,
            color = mindColors.textPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percent,
            color = mindColors.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(8.dp))
        Text(text = emoji, fontSize = 16.sp)
    }
}

@Composable
private fun ComparisonIndicator(diff: Int) {
    val mindColors = MaterialTheme.mindColors

    val (color, text, icon) = when {
        diff > 0 -> Triple(
            mindColors.accentGreen,
            stringResource(R.string.stats_improvement, diff),
            "✅"
        )
        diff < 0 -> Triple(
            mindColors.accentRed,
            stringResource(R.string.stats_decline, -diff),
            "⚠️"
        )
        else -> Triple(
            mindColors.textSecondary,
            stringResource(R.string.stats_same),
            "➡️"
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun ratingEmoji(percent: Int): String = when {
    percent >= 100 -> "😄"
    percent >= 80 -> "😊"
    percent >= 60 -> "🙂"
    percent >= 40 -> "😐"
    else -> "😕"
}
