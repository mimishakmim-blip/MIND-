package com.example.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.DatabaseModule
import com.example.domain.util.DateUtils
import com.example.i18n.AppLanguage
import com.example.i18n.tr
import com.example.ui.theme.mindColors
import java.util.Calendar

/** Study statistics for the last 7 days (minutes per day, total, daily average, best day). */
@Composable
fun StudyWeekCard(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.mindColors
    val context = LocalContext.current
    val flow = remember { DatabaseModule.provideDatabase(context).studySessionDao().getAll() }
    val sessions by flow.collectAsState(initial = emptyList())

    val dayStarts = remember(sessions) {
        val today = DateUtils.startOfDay()
        (6 downTo 0).map { back ->
            Calendar.getInstance().apply {
                timeInMillis = today
                add(Calendar.DAY_OF_MONTH, -back)
            }.timeInMillis
        }
    }
    val minutes = dayStarts.map { start ->
        val end = start + 24L * 60 * 60 * 1000
        sessions.filter { it.date >= start && it.date < end }.sumOf { it.durationMinutes }
    }
    val total = minutes.sum()
    val max = (minutes.maxOrNull() ?: 0).coerceAtLeast(1)
    val bestIndex = minutes.indices.maxByOrNull { minutes[it] } ?: 0
    val locale = java.util.Locale(if (AppLanguage.isArabic) "ar" else "en")
    val dayName = { start: Long ->
        java.text.SimpleDateFormat("EEE", locale).format(java.util.Date(start))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(0.5.dp, colors.separatorThin, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "📈 " + tr("إحصائيات الدراسة — آخر 7 أيام", "Study stats — last 7 days"),
            color = colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat(tr("المجموع", "Total"), "%d:%02d".format(total / 60, total % 60) + " " + tr("س", "h"), Modifier.weight(1f))
            Stat(tr("المعدل اليومي", "Daily avg"), "${total / 7} " + tr("د", "min"), Modifier.weight(1f))
            Stat(
                tr("أفضل يوم", "Best day"),
                if (total == 0) "—" else dayName(dayStarts[bestIndex]),
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            minutes.forEachIndexed { i, m ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((80f * m / max).coerceAtLeast(3f).dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (i == 6) colors.primary else colors.primary.copy(alpha = 0.45f))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = dayName(dayStarts[i]), color = colors.textTertiary, fontSize = 10.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    val colors = MaterialTheme.mindColors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary.copy(alpha = 0.1f))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = colors.textSecondary, fontSize = 11.sp)
    }
}
