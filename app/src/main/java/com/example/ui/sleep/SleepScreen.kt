package com.example.ui.sleep

import com.example.i18n.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.entity.SleepLog
import com.example.ui.components.IOSAlert
import com.example.ui.components.IOSGlassContainer
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SleepScreen(
    onBack: () -> Unit,
    viewModel: SleepViewModel = viewModel()
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val state by viewModel.uiState.collectAsState()

    var showEntrySheet by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<SleepLog?>(null) }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = stringResource(R.string.qa_sleep),
                onBack = onBack,
                trailing = {
                    IconButton(
                        onClick = { showEntrySheet = true },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("sleep_add_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(mindColors.primary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = tr("تسجيل النوم", "Log sleep"),
                                tint = mindColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEntrySheet = true },
                containerColor = mindColors.primary,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("sleep_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = tr("تسجيل النوم", "Log sleep"),
                        style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        containerColor = mindColors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Last Night's Sleep Hero Card
            item {
                LastNightSleepCard(
                    latestLog = state.latestSleep,
                    onLogClick = { showEntrySheet = true }
                )
            }

            // 2. Weekly Trend Bar Chart
            item {
                WeeklySleepChart(
                    bars = state.weeklyBars,
                    targetHours = state.targetHours,
                    avgHours = state.weeklyAvgHours,
                    avgQuality = state.weeklyAvgQuality,
                    sleepDebt = state.sleepDebtHours
                )
            }

            // 3. Sleep Hygiene Insights
            item {
                SleepHygieneCard()
            }

            // 4. Sleep History Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("سجل الليالي السابقة", "Previous nights"),
                        style = mindTypography.headline.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.textPrimary
                    )
                    Text(
                        text = tr("${state.allLogs.size} تسجيلات", "${state.allLogs.size} entries"),
                        style = mindTypography.caption,
                        color = mindColors.textSecondary
                    )
                }
            }

            // History list
            if (state.allLogs.isEmpty()) {
                item {
                    EmptySleepState(onAddClick = { showEntrySheet = true })
                }
            } else {
                items(state.allLogs, key = { it.id }) { log ->
                    SleepHistoryCard(
                        log = log,
                        onDelete = { logToDelete = log }
                    )
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    if (showEntrySheet) {
        SleepEntrySheet(
            onSave = { bedTime, wakeTime, quality, interruptions, notes ->
                viewModel.saveSleepLog(bedTime, wakeTime, quality, interruptions, notes)
                showEntrySheet = false
            },
            onDismiss = { showEntrySheet = false }
        )
    }

    logToDelete?.let { log ->
        IOSAlert(
            title = tr("حذف سجل النوم", "Delete sleep entry"),
            message = tr("هل أنت متأكد من حذف هذا السجل؟", "Are you sure you want to delete this entry?"),
            onConfirm = {
                viewModel.deleteSleepLog(log)
                logToDelete = null
            },
            onDismiss = { logToDelete = null },
            confirmText = stringResource(R.string.common_delete),
            dismissText = stringResource(R.string.common_cancel)
        )
    }
}

@Composable
private fun LastNightSleepCard(
    latestLog: SleepLog?,
    onLogClick: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    IOSGlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, mindColors.separatorThin, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌙", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = tr("آخر ليلة نوم", "Last night's sleep"),
                        style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                        color = mindColors.textSecondary
                    )
                }

                if (latestLog != null) {
                    val qualityLabel = when (latestLog.quality) {
                        5 -> tr("✨ رائع", "✨ Great")
                        4 -> tr("🌙 ممتاز", "🌙 Excellent")
                        3 -> tr("😴 جيد", "😴 Good")
                        2 -> tr("🥱 مقبول", "🥱 Fair")
                        else -> tr("😫 سيء", "😫 Poor")
                    }
                    Box(
                        modifier = Modifier
                            .background(mindColors.primary.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = qualityLabel,
                            style = mindTypography.caption.copy(fontWeight = FontWeight.Bold),
                            color = mindColors.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (latestLog != null) {
                val hours = latestLog.durationMinutes / 60
                val mins = latestLog.durationMinutes % 60

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$hours",
                        style = mindTypography.largeTitle.copy(fontWeight = FontWeight.Black, fontSize = 42.sp),
                        color = mindColors.textPrimary
                    )
                    Text(
                        text = tr("س ", "h "),
                        style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "$mins",
                        style = mindTypography.largeTitle.copy(fontWeight = FontWeight.Black, fontSize = 34.sp),
                        color = mindColors.textPrimary
                    )
                    Text(
                        text = tr("د", "m"),
                        style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Schedule times row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(mindColors.surfaceElevated)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bedtime, contentDescription = null, tint = mindColors.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(text = tr("النوم", "Sleep"), style = mindTypography.caption, color = mindColors.textSecondary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = timeFormatter.format(Date(latestLog.bedTime)),
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                            color = mindColors.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(mindColors.separatorThin)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(text = tr("الاستيقاظ", "Wake-up"), style = mindTypography.caption, color = mindColors.textSecondary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = timeFormatter.format(Date(latestLog.wakeTime)),
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                            color = mindColors.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(mindColors.separatorThin)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = tr("الاستيقاظ ليلاً", "Night wake-ups"), style = mindTypography.caption, color = mindColors.textSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = tr("${latestLog.interruptions} مرات", "${latestLog.interruptions} times"),
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                            color = mindColors.textPrimary
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tr("لم تسجل نومك لليلة الماضية بعد", "You haven't logged last night's sleep yet"),
                        style = mindTypography.subhead,
                        color = mindColors.textSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(mindColors.primary)
                            .clickable { onLogClick() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tr("+ سجّل نوم البارحة الآن", "+ Log last night's sleep"),
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklySleepChart(
    bars: List<SleepDayBar>,
    targetHours: Float,
    avgHours: Float,
    avgQuality: Float,
    sleepDebt: Float
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    IOSGlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, mindColors.separatorThin, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("مخطط الأسبوع", "Weekly chart"),
                    style = mindTypography.headline.copy(fontWeight = FontWeight.Bold),
                    color = mindColors.textPrimary
                )

                Text(
                    text = tr("الهدف: ${targetHours.toInt()} ساعات", "Goal: ${targetHours.toInt()} hours"),
                    style = mindTypography.caption.copy(fontWeight = FontWeight.Medium),
                    color = mindColors.primary
                )
            }

            Spacer(Modifier.height(20.dp))

            // Bars row
            val maxHours = 11f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { bar ->
                    val heightRatio = (bar.hours / maxHours).coerceIn(0.06f, 1f)
                    val isGoalMet = bar.hours >= targetHours

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Duration label
                        if (bar.hours > 0) {
                            Text(
                                text = String.format(Locale.US, "%.1f", bar.hours),
                                style = mindTypography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = if (bar.isToday) mindColors.primary else mindColors.textSecondary
                            )
                        } else {
                            Text(
                                text = "-",
                                style = mindTypography.caption.copy(fontSize = 10.sp),
                                color = mindColors.textTertiary
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Bar
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(85.dp * heightRatio)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(
                                    brush = if (bar.hours > 0) {
                                        Brush.verticalGradient(
                                            colors = if (isGoalMet) listOf(
                                                mindColors.primary,
                                                mindColors.primary.copy(alpha = 0.5f)
                                            ) else listOf(
                                                Color(0xFF818CF8), // Indigo
                                                Color(0xFF4F46E5).copy(alpha = 0.5f)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                mindColors.surfaceElevated,
                                                mindColors.surfaceElevated
                                            )
                                        )
                                    }
                                )
                                .then(
                                    if (bar.isToday) {
                                        Modifier.border(1.5.dp, mindColors.primary, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                    } else Modifier
                                )
                        )

                        Spacer(Modifier.height(6.dp))

                        // Day initial
                        Text(
                            text = bar.dayInitial,
                            style = mindTypography.caption.copy(
                                fontWeight = if (bar.isToday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (bar.isToday) mindColors.primary else mindColors.textSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 3 Stat Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBadge(
                    label = tr("المعدل", "Average"),
                    value = String.format(Locale.US, tr("%.1f س", "%.1f h"), avgHours),
                    modifier = Modifier.weight(1f)
                )

                StatBadge(
                    label = tr("الجودة", "Quality"),
                    value = if (avgQuality > 0) String.format(Locale.US, "%.1f/5", avgQuality) else "-",
                    modifier = Modifier.weight(1f)
                )

                StatBadge(
                    label = tr("دين النوم", "Sleep debt"),
                    value = if (sleepDebt > 0) String.format(Locale.US, tr("%.1f س", "%.1f h"), sleepDebt) else tr("0 س", "0 h"),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(mindColors.surfaceElevated)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = mindTypography.caption, color = mindColors.textSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
            color = mindColors.textPrimary
        )
    }
}

@Composable
private fun SleepHygieneCard() {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    val tips = listOf(
        tr("🌙 التزم بموعد ثابت: النوم والاستيقاظ في نفس الساعة يومياً يقوي إيقاعك البيولوجي.", "🌙 Keep a fixed schedule: sleeping and waking at the same time strengthens your body clock."),
        tr("📵 احجب الضوء الأزرق: أطفئ الشاشات الرقمية قبل 45 دقيقة من النوم لرفع إفراز الميلاتونين.", "📵 Block blue light: turn screens off 45 minutes before bed to boost melatonin."),
        tr("❄️ الغرفة الباردة: درجة الحرارة المثالية للنوم العميق هي ما بين 18 إلى 20 درجة مئوية.", "❄️ Cool room: the ideal temperature for deep sleep is 18–20 °C.")
    )

    var currentTipIndex by remember { mutableStateOf(0) }

    IOSGlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, mindColors.separatorThin, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = tr("نصائح لصحة النوم", "Sleep health tips"),
                        style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { currentTipIndex = (currentTipIndex + 1) % tips.size }
                        .background(mindColors.surfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = tr("التالية →", "Next →"), style = mindTypography.caption, color = mindColors.primary)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = tips[currentTipIndex],
                style = mindTypography.subhead,
                color = mindColors.textSecondary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun SleepHistoryCard(
    log: SleepLog,
    onDelete: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    val dateFormatter = remember { SimpleDateFormat("EEEE, d MMMM", Locale("ar")) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val hours = log.durationMinutes / 60
    val minutes = log.durationMinutes % 60

    val qualityEmoji = when (log.quality) {
        5 -> "✨"
        4 -> "🌙"
        3 -> "😴"
        2 -> "🥱"
        else -> "😫"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(mindColors.surfaceElevated)
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = qualityEmoji, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = dateFormatter.format(Date(log.date)),
                        style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.textPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${timeFormatter.format(Date(log.bedTime))} — ${timeFormatter.format(Date(log.wakeTime))}",
                        style = mindTypography.caption,
                        color = mindColors.textSecondary
                    )
                    if (log.notes.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = log.notes,
                            style = mindTypography.caption,
                            color = mindColors.primary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tr("${hours}س ${minutes}د", "${hours}h ${minutes}m"),
                        style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.primary
                    )
                    if (log.interruptions > 0) {
                        Text(
                            text = tr("${log.interruptions} استيقاظ", "${log.interruptions} wake-ups"),
                            style = mindTypography.caption,
                            color = mindColors.textTertiary
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = tr("حذف", "Delete"),
                        tint = mindColors.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySleepState(onAddClick: () -> Unit) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🛌", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = tr("لا توجد سجلات نوم حتى الآن", "No sleep records yet"),
            style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
            color = mindColors.textPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = tr("ابدأ بتسجيل أوقات نومك واستيقاظك لمراقبة جودة وصحة نومك اليومية.", "Start logging your bedtime and wake time to track your daily sleep quality."),
            style = mindTypography.subhead,
            color = mindColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
