package com.example.ui.study

import com.example.i18n.tr
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.entity.StudySession
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSGlassContainer
import com.example.ui.components.IOSLargeTitle
import com.example.ui.components.IOSToggle
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudyScreen(
    onBack: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: StudyViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val view = LocalView.current
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mindColors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
            .testTag("study_screen")
    ) {
        // 1. Top Bar
        com.example.ui.components.ScreenHeader(
            title = stringResource(R.string.study_title),
            onBack = onBack
        )

        Spacer(Modifier.height(8.dp))

        // 2. Today Summary Card
        TodaySummaryCard(
            totalMinutes = state.todayMinutes,
            sessionsCount = state.todaySessions,
            avgFocus = state.avgFocus,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Notes & journal live under Study
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            com.example.ui.components.ShortcutCard(
                emoji = "📝",
                title = tr("المذكرات", "Notes"),
                subtitle = tr("ملاحظاتك الدراسية", "Study notes"),
                onClick = onNavigateToNotes,
                modifier = Modifier.weight(1f)
            )
            com.example.ui.components.ShortcutCard(
                emoji = "📔",
                title = tr("اليوميات", "Journal"),
                subtitle = tr("دوّن يومك", "Write your day"),
                onClick = onNavigateToJournal,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        StudyWeekCard(modifier = Modifier.padding(horizontal = 20.dp))

        Spacer(Modifier.height(16.dp))

        AppLockCard(modifier = Modifier.padding(horizontal = 20.dp))

        Spacer(Modifier.height(16.dp))

        // 3. Pomodoro Card (Main)
        PomodoroMainCard(
            state = state,
            onStart = {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                viewModel.startPomodoro()
            },
            onPause = {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                viewModel.pausePomodoro()
            },
            onReset = {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                viewModel.resetPomodoro()
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 4. Pomodoro Settings Card (Collapsible)
        PomodoroSettingsCard(
            isExpanded = showSettings,
            onToggleExpand = { showSettings = !showSettings },
            focusMinutes = state.focusMinutes,
            shortBreakMinutes = state.shortBreakMinutes,
            longBreakMinutes = state.longBreakMinutes,
            sessionsCount = state.sessionsBeforeLongBreak,
            autoStartNext = state.autoStartNext,
            onSettingsChanged = { f, sb, lb, sc, auto ->
                viewModel.updateSettings(f, sb, lb, sc, auto)
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 5. Ambient Sounds Card
        AmbientSoundsCard(
            selectedSound = state.playingSound,
            volume = state.volume,
            onSoundSelect = { soundId ->
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.toggleSound(soundId)
            },
            onVolumeChange = { viewModel.setVolume(it) },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 6. Recent Sessions Card
        RecentSessionsCard(
            sessions = state.recentSessions,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 7. Weekly Chart Card
        WeeklyStudyChartCard(
            weeklyMinutes = state.weeklyMinutes,
            weeklyLabels = state.weeklyLabels,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun TodaySummaryCard(
    totalMinutes: Int,
    sessionsCount: Int,
    avgFocus: Float,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    IOSGlassContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "📚 " + stringResource(R.string.study_today),
                style = mindTypography.headline,
                color = mindColors.textPrimary
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricItem(
                    emoji = "⏱️",
                    label = stringResource(R.string.study_minutes, totalMinutes)
                )
                MetricItem(
                    emoji = "🎯",
                    label = stringResource(R.string.study_sessions, sessionsCount)
                )
                val focusDisplay = if (avgFocus > 0f) "%.1f".format(avgFocus) else "-"
                MetricItem(
                    emoji = "⭐",
                    label = focusDisplay
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    emoji: String,
    label: String
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = mindColors.textPrimary
        )
    }
}

@Composable
private fun PomodoroMainCard(
    state: StudyUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    IOSGlassContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large circular progress ring
            PomodoroTimer(
                state = state.pomodoroState,
                remainingSeconds = state.remainingSeconds,
                totalSeconds = state.totalSeconds,
                onStart = onStart,
                onPause = onPause,
                onReset = onReset
            )

            Spacer(Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isRunning = state.pomodoroState == PomodoroState.FOCUS ||
                        state.pomodoroState == PomodoroState.SHORT_BREAK ||
                        state.pomodoroState == PomodoroState.LONG_BREAK

                if (isRunning) {
                    IOSButton(
                        text = "⏸️ " + stringResource(R.string.study_pause),
                        onClick = onPause,
                        style = IOSButtonStyle.Filled,
                        modifier = Modifier.width(140.dp)
                    )
                } else {
                    IOSButton(
                        text = "▶️ " + stringResource(R.string.study_start),
                        onClick = onStart,
                        style = IOSButtonStyle.Filled,
                        modifier = Modifier.width(140.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                IOSButton(
                    text = "🔄 " + stringResource(R.string.study_reset),
                    onClick = onReset,
                    style = IOSButtonStyle.Glass,
                    modifier = Modifier.width(140.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Session counter
            val currentSessionInCycle = (state.sessionsCompleted % state.sessionsBeforeLongBreak) + 1
            Text(
                text = stringResource(
                    R.string.study_session_counter,
                    currentSessionInCycle,
                    state.sessionsBeforeLongBreak
                ),
                style = mindTypography.footnote,
                color = mindColors.textSecondary
            )
        }
    }
}

@Composable
private fun PomodoroSettingsCard(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    sessionsCount: Int,
    autoStartNext: Boolean,
    onSettingsChanged: (Int, Int, Int, Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    IOSGlassContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚙️", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.study_settings),
                        style = mindTypography.headline,
                        color = mindColors.textPrimary
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = mindColors.textSecondary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    SettingRow(
                        label = stringResource(R.string.study_focus_duration),
                        value = "$focusMinutes " + stringResource(R.string.study_minutes_suffix)
                    )
                    Spacer(Modifier.height(8.dp))
                    SettingRow(
                        label = stringResource(R.string.study_short_break),
                        value = "$shortBreakMinutes " + stringResource(R.string.study_minutes_suffix)
                    )
                    Spacer(Modifier.height(8.dp))
                    SettingRow(
                        label = stringResource(R.string.study_long_break),
                        value = "$longBreakMinutes " + stringResource(R.string.study_minutes_suffix)
                    )
                    Spacer(Modifier.height(8.dp))
                    SettingRow(
                        label = stringResource(R.string.study_sessions_before_long),
                        value = "$sessionsCount"
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.study_auto_start),
                            style = mindTypography.subhead,
                            color = mindColors.textPrimary
                        )
                        IOSToggle(
                            checked = autoStartNext,
                            onCheckedChange = { checked ->
                                onSettingsChanged(
                                    focusMinutes,
                                    shortBreakMinutes,
                                    longBreakMinutes,
                                    sessionsCount,
                                    checked
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = mindTypography.subhead, color = mindColors.textSecondary)
        Text(
            text = value,
            style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
            color = mindColors.textPrimary
        )
    }
}

@Composable
private fun AmbientSoundsCard(
    selectedSound: String?,
    volume: Float,
    onSoundSelect: (String) -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    val context = androidx.compose.ui.platform.LocalContext.current
    var gallery by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(GallerySounds.list(context))
    }
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            GallerySounds.add(context, uri)
            gallery = GallerySounds.list(context)
            onSoundSelect(GalleryUriPrefix + uri.toString())
        }
    }

    val sounds = listOf(
        SoundOption("rain", "🌧️", stringResource(R.string.study_rain)),
        SoundOption("forest", "🌲", stringResource(R.string.study_forest)),
        SoundOption("cafe", "☕", stringResource(R.string.study_cafe)),
        SoundOption("white_noise", "⚪", stringResource(R.string.study_white_noise)),
        SoundOption("calm_rain", "🌦️", tr("مطر هادئ", "Soft rain")),
        SoundOption("calm_ocean", "🌊", tr("أمواج", "Ocean")),
        SoundOption("calm_wind", "🍃", tr("نسيم", "Breeze")),
        SoundOption("calm_pad", "🎹", tr("نغمة هادئة", "Calm pad")),
        SoundOption("calm_bells", "🔔", tr("أجراس ناعمة", "Soft bells"))
    ) + gallery.map { (name, uri) ->
        SoundOption(GalleryUriPrefix + uri, "🎧", name.substringBeforeLast('.').take(10))
    }

    IOSGlassContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "🎵 " + stringResource(R.string.study_sounds),
                style = mindTypography.headline,
                color = mindColors.textPrimary
            )

            Spacer(Modifier.height(14.dp))

            // 4 Sound buttons (80dp, rounded 20dp, selected: primary border)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                sounds.forEach { option ->
                    val isSelected = selectedSound == option.id
                    SoundButton(
                        option = option,
                        isSelected = isSelected,
                        onClick = { onSoundSelect(option.id) }
                    )
                }
                SoundButton(
                    option = SoundOption("add", "➕", tr("من المعرض", "From gallery")),
                    isSelected = false,
                    onClick = { picker.launch(arrayOf("audio/*")) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Volume Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔊 ${(volume * 100).toInt()}%",
                    style = mindTypography.footnote,
                    color = mindColors.textSecondary,
                    modifier = Modifier.width(54.dp)
                )
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = mindColors.primary,
                        activeTrackColor = mindColors.primary,
                        inactiveTrackColor = mindColors.surfaceElevated
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private data class SoundOption(
    val id: String,
    val emoji: String,
    val name: String
)

@Composable
private fun SoundButton(
    option: SoundOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) mindColors.primary.copy(alpha = 0.15f)
                else mindColors.surfaceElevated
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) mindColors.primary else mindColors.separatorThin,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .testTag("sound_button_${option.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = option.emoji, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = option.name,
                style = mindTypography.caption,
                color = if (isSelected) mindColors.primary else mindColors.textSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentSessionsCard(
    sessions: List<StudySession>,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }

    IOSGlassContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "📖 " + stringResource(R.string.study_recent),
                style = mindTypography.headline,
                color = mindColors.textPrimary
            )

            Spacer(Modifier.height(12.dp))

            if (sessions.isEmpty()) {
                Text(
                    text = stringResource(R.string.study_no_sessions),
                    style = mindTypography.subhead,
                    color = mindColors.textTertiary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                sessions.take(5).forEachIndexed { index, session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 " + dateFormat.format(Date(session.date)),
                            style = mindTypography.subhead,
                            color = mindColors.textSecondary
                        )
                        Text(
                            text = stringResource(R.string.study_minutes, session.durationMinutes),
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                            color = mindColors.textPrimary
                        )
                        Text(
                            text = "⭐ ${session.focusScore}",
                            style = mindTypography.caption,
                            color = mindColors.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyStudyChartCard(
    weeklyMinutes: List<Int>,
    weeklyLabels: List<String>,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val maxMinutes = (weeklyMinutes.maxOrNull() ?: 0).coerceAtLeast(60)

    IOSGlassContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "📈 " + stringResource(R.string.study_weekly),
                style = mindTypography.headline,
                color = mindColors.textPrimary
            )

            Spacer(Modifier.height(16.dp))

            if (weeklyMinutes.all { it == 0 }) {
                Text(
                    text = stringResource(R.string.study_no_sessions),
                    style = mindTypography.subhead,
                    color = mindColors.textTertiary,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyMinutes.forEachIndexed { index, minutes ->
                        val label = weeklyLabels.getOrNull(index) ?: ""
                        val ratio = minutes.toFloat() / maxMinutes

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (minutes > 0) {
                                Text(
                                    text = "$minutes",
                                    fontSize = 10.sp,
                                    color = mindColors.textSecondary
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height((ratio * 90).dp.coerceAtLeast(4.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (minutes > 0) mindColors.primary
                                        else mindColors.surfaceElevated
                                    )
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = label,
                                style = mindTypography.caption,
                                color = mindColors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Study Screen Dark", showBackground = true)
@Composable
private fun StudyScreenPreview() {
    MaterialTheme {
        StudyScreen()
    }
}
