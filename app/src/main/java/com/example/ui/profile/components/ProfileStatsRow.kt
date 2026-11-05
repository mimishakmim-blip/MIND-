package com.example.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IOSCard
import com.example.ui.theme.mindColors
import java.util.Locale

@Composable
fun ProfileStatsRow(
    bestStreak: Int,
    totalSteps: Int,
    totalStudyMinutes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileStatTile(
            value = bestStreak.toString(),
            label = stringResource(R.string.profile_stat_streak),
            icon = Icons.Default.LocalFireDepartment,
            color = MaterialTheme.mindColors.accentOrange,
            modifier = Modifier.weight(1f)
        )
        ProfileStatTile(
            value = formatNumber(totalSteps),
            label = stringResource(R.string.profile_stat_steps),
            icon = Icons.Default.DirectionsWalk,
            color = MaterialTheme.mindColors.accentBlue,
            modifier = Modifier.weight(1f)
        )
        ProfileStatTile(
            value = "$totalStudyMinutes",
            label = stringResource(R.string.profile_stat_study),
            icon = Icons.Default.School,
            color = MaterialTheme.mindColors.accentPurple,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileStatTile(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    IOSCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                color = mindColors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = mindColors.textSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatNumber(n: Int): String = when {
    n >= 1000000 -> String.format(Locale.US, "%.1fM", n / 1000000f)
    n >= 1000 -> String.format(Locale.US, "%.1fK", n / 1000f)
    else -> n.toString()
}
