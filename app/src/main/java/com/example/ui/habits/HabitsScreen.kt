package com.example.ui.habits

import com.example.i18n.tr
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.entity.Habit
import com.example.ui.components.IOSAlert
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@Composable
fun HabitsScreen(
    onOpenStats: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HabitsViewModel = viewModel()
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val state by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var showTodayStats by remember { mutableStateOf(false) }

    // Current time of day (minutes): re-evaluated every 30 s; also rolls over at midnight.
    fun currentMinutes(): Int =
        java.util.Calendar.getInstance().let { it.get(java.util.Calendar.HOUR_OF_DAY) * 60 + it.get(java.util.Calendar.MINUTE) }
    var nowMinutes by remember { mutableStateOf(currentMinutes()) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            nowMinutes = currentMinutes()
            viewModel.refreshDay()
            kotlinx.coroutines.delay(30_000L)
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("habits_screen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Large title
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.habits_title),
                style = mindTypography.largeTitle,
                color = mindColors.textPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (state.isEmpty) {
                // Empty state
                EmptyHabitsState(
                    onAdd = { showAddSheet = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Strict Table with Header [Time | Name | ✅ | ❌]
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item(key = "day_curve") {
                        HabitsCurveCard(
                            items = state.groups.flatMap { it.habits },
                            onOpenStats = { showTodayStats = true },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    item(key = "table_header") {
                        HabitsTableHeader(modifier = Modifier.padding(bottom = 4.dp))
                    }

                    val groups = state.groups
                    groups.forEach { group ->
                        if (group.habits.isNotEmpty()) {
                            items(
                                items = group.habits,
                                key = { it.habit.id }
                            ) { habitWithLog ->
                                HabitTableRow(
                                    habitWithLog = habitWithLog,
                                    onToggle = { isDone ->
                                        viewModel.toggleHabit(
                                            habitWithLog.habit,
                                            isDone
                                        )
                                    },
                                    doneLocked = nowMinutes < habitWithLog.habit.timeHour * 60 +
                                        habitWithLog.habit.timeMinute + habitWithLog.habit.durationMinutes,
                                    onLongPress = { habitToDelete = habitWithLog.habit },
                                    onEdit = { habitToEdit = habitWithLog.habit }
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB
        if (!state.isEmpty) {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = mindColors.primary,
                contentColor = mindColors.textInverse,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.habits_new),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    habitToDelete?.let { habit ->
        IOSAlert(
            title = stringResource(R.string.habits_delete),
            message = habit.name,
            confirmText = stringResource(R.string.habits_delete),
            onConfirm = {
                viewModel.deleteHabit(habit)
                habitToDelete = null
            },
            dismissText = stringResource(R.string.common_cancel),
            onDismiss = { habitToDelete = null }
        )
    }

    if (showTodayStats) {
        TodayStatsSheet(
            items = state.groups.flatMap { it.habits },
            onDismiss = { showTodayStats = false },
            onOpenFullStats = {
                showTodayStats = false
                onOpenStats()
            }
        )
    }

    habitToEdit?.let { habit ->
        AddHabitSheet(
            initial = habit,
            onDismiss = { habitToEdit = null },
            onSave = { name, description, iconName, colorHex, hour, minute, duration, reminder, frequency ->
                viewModel.updateHabit(habit, name, description, iconName, colorHex, hour, minute, duration, reminder, frequency)
                habitToEdit = null
            }
        )
    }

    if (showAddSheet) {
        AddHabitSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, description, iconName, colorHex, hour, minute, duration, reminder, frequency ->
                viewModel.addHabit(
                    name = name,
                    description = description,
                    iconName = iconName,
                    colorHex = colorHex,
                    timeHour = hour,
                    timeMinute = minute,
                    durationMinutes = duration,
                    reminderEnabled = reminder,
                    frequency = frequency
                )
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun HabitsTableHeader(
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(mindColors.surfaceElevated)
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tr("الوقت", "Time"),
            style = mindTypography.footnote.copy(fontWeight = FontWeight.Bold),
            color = mindColors.textSecondary,
            modifier = Modifier.width(55.dp)
        )
        Text(
            text = tr("العادة", "Habit"),
            style = mindTypography.footnote.copy(fontWeight = FontWeight.Bold),
            color = mindColors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "✅",
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "❌",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun HabitTableRow(
    habitWithLog: HabitWithLog,
    onToggle: (Boolean) -> Unit,
    onLongPress: () -> Unit = {},
    onEdit: () -> Unit = {},
    doneLocked: Boolean = false
) {
    val mindColors = MaterialTheme.mindColors
    val habit = habitWithLog.habit
    val isDone = habitWithLog.todayLog?.status == "DONE"
    val isMarkedNotDone = habitWithLog.todayLog?.status == "NOT_DONE"

    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        mindColors.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(mindColors.surface)
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(14.dp))
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress() })
            }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Time Column
        Text(
            text = "%02d:%02d".format(habit.timeHour, habit.timeMinute),
            color = mindColors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(55.dp)
        )

        // 2. Name Column (tap to edit)
        Row(
            modifier = Modifier.weight(1f).clickable(onClick = onEdit),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(habitColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getHabitIcon(habit.iconName),
                    contentDescription = null,
                    tint = habitColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = habit.name,
                    color = mindColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        color = mindColors.textTertiary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (doneLocked && !isDone && !isMarkedNotDone) {
                    val unlock = habit.timeHour * 60 + habit.timeMinute + habit.durationMinutes
                    Text(
                        text = "🔒 " + tr("متاح بعد", "Opens at") + " %02d:%02d".format((unlock / 60) % 24, unlock % 60),
                        color = mindColors.textTertiary,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            if (habit.streak > 0) {
                Spacer(Modifier.width(6.dp))
                com.example.ui.components.StreakPill(
                    streak = habit.streak
                )
            }
        }

        // 3. ✅ Button
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            HabitToggleButton(
                isChecked = isDone,
                type = ToggleType.DONE,
                enabled = !doneLocked || isDone,
                onClick = { onToggle(true) }
            )
        }

        Spacer(Modifier.width(8.dp))

        // 4. ❌ Button
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            HabitToggleButton(
                isChecked = isMarkedNotDone,
                type = ToggleType.NOT_DONE,
                enabled = !doneLocked || isMarkedNotDone,
                onClick = { onToggle(false) }
            )
        }
    }
}

enum class ToggleType { DONE, NOT_DONE }

@Composable
private fun HabitToggleButton(
    isChecked: Boolean,
    type: ToggleType,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val mindColors = MaterialTheme.mindColors
    val view = LocalView.current
    val bgColor = when {
        isChecked && type == ToggleType.DONE -> mindColors.accentGreen
        isChecked && type == ToggleType.NOT_DONE -> mindColors.accentRed
        else -> Color.Transparent
    }
    val borderColor = when {
        isChecked -> Color.Transparent
        else -> mindColors.separatorStrong
    }
    val iconTint = if (isChecked) Color.White else mindColors.textTertiary
    val icon = if (type == ToggleType.DONE) Icons.Default.Check else Icons.Default.Close

    val scale by animateFloatAsState(
        targetValue = if (isChecked) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "toggle_scale"
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .alpha(if (enabled) 1f else 0.35f)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.5.dp, borderColor, CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (enabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun EmptyHabitsState(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = mindColors.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.habits_empty),
            color = mindColors.textPrimary,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(24.dp))
        IOSButton(
            text = stringResource(R.string.habits_add_first),
            onClick = onAdd,
            style = IOSButtonStyle.Filled
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Habits Screen Preview")
@Composable
fun HabitsScreenPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0F))
        ) {
            HabitsScreen()
        }
    }
}
