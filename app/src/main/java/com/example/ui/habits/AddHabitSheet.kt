package com.example.ui.habits

import com.example.i18n.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSCard
import com.example.ui.components.IOSSegmentedControl
import com.example.ui.components.IOSSheet
import com.example.ui.components.IOSTextField
import com.example.ui.components.IOSToggle
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitSheet(
    onDismiss: () -> Unit,
    initial: com.example.data.entity.Habit? = null,
    onSave: (String, String, String, String, Int, Int, Int, Boolean, String) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var iconName by remember { mutableStateOf(initial?.iconName ?: "star") }
    var colorHex by remember { mutableStateOf(initial?.colorHex ?: "#E8C547") }
    var hour by remember { mutableIntStateOf(initial?.timeHour ?: 8) }
    var minute by remember { mutableIntStateOf(initial?.timeMinute ?: 0) }
    var duration by remember { mutableIntStateOf(initial?.durationMinutes ?: 60) }
    var reminderEnabled by remember { mutableStateOf(initial?.reminderEnabled ?: true) }
    var frequency by remember { mutableStateOf(initial?.frequency ?: "DAILY") }
    var showTimePicker by remember { mutableStateOf(false) }

    IOSSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (initial == null) stringResource(R.string.habits_new) else tr("تعديل العادة", "Edit habit"),
                style = mindTypography.largeTitle,
                color = mindColors.textPrimary
            )

            Spacer(Modifier.height(20.dp))

            // Name Input
            IOSTextField(
                value = name,
                onValueChange = { name = it.take(50) },
                placeholder = stringResource(R.string.habits_name_hint)
            )

            Spacer(Modifier.height(12.dp))

            IOSTextField(
                value = description,
                onValueChange = { description = it.take(120) },
                placeholder = tr("وصف العادة (اختياري)", "Description (optional)")
            )

            Spacer(Modifier.height(20.dp))

            // Icon Picker (5x4)
            Text(
                text = stringResource(R.string.habits_icon),
                color = mindColors.textSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            IconPicker(
                selected = iconName,
                onSelect = { iconName = it }
            )

            Spacer(Modifier.height(20.dp))

            // Color Picker
            Text(
                text = stringResource(R.string.habits_color),
                color = mindColors.textSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            ColorPicker(
                selected = colorHex,
                onSelect = { colorHex = it }
            )

            Spacer(Modifier.height(20.dp))

            // Time Selector Card
            Text(
                text = stringResource(R.string.habits_time),
                color = mindColors.textSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            IOSCard(
                onClick = { showTimePicker = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.habits_time),
                        color = mindColors.textPrimary
                    )
                    Text(
                        text = "%02d:%02d".format(hour, minute),
                        color = mindColors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Duration
            Text(
                text = stringResource(R.string.habits_duration),
                color = mindColors.textSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            IOSSegmentedControl(
                options = listOf(tr("30 د", "30 m"), tr("ساعة", "1 hr"), tr("ساعتان", "2 hrs"), tr("مخصص", "Custom")),
                selectedIndex = when (duration) {
                    30 -> 0
                    60 -> 1
                    120 -> 2
                    else -> 3
                },
                onSelect = {
                    duration = when (it) {
                        0 -> 30
                        1 -> 60
                        2 -> 120
                        else -> 90
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            // Reminder toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.habits_reminder),
                    color = mindColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IOSToggle(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Frequency
            Text(
                text = stringResource(R.string.habits_frequency),
                color = mindColors.textSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            IOSSegmentedControl(
                options = listOf(
                    stringResource(R.string.habits_daily),
                    stringResource(R.string.habits_weekly),
                    stringResource(R.string.habits_custom)
                ),
                selectedIndex = when (frequency) {
                    "DAILY" -> 0
                    "WEEKLY" -> 1
                    else -> 2
                },
                onSelect = {
                    frequency = when (it) {
                        0 -> "DAILY"
                        1 -> "WEEKLY"
                        else -> "CUSTOM"
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            // Actions
            IOSButton(
                text = stringResource(R.string.habits_save),
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name.trim(),
                            description.trim(),
                            iconName,
                            colorHex,
                            hour,
                            minute,
                            duration,
                            reminderEnabled,
                            frequency
                        )
                    }
                },
                style = IOSButtonStyle.Filled,
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            IOSButton(
                text = stringResource(R.string.habits_cancel),
                onClick = onDismiss,
                style = IOSButtonStyle.Text,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hour = timePickerState.hour
                    minute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text(
                        stringResource(R.string.common_ok),
                        color = mindColors.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(
                        stringResource(R.string.common_cancel),
                        color = mindColors.textSecondary
                    )
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
private fun IconPicker(
    selected: String,
    onSelect: (String) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val icons = listOf(
        "star", "book", "fitness", "water", "sleep",
        "walk", "run", "food", "study", "code",
        "music", "art", "translate", "psychology", "spa",
        "coffee", "eco", "bike", "trophy", "timer"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(200.dp)
    ) {
        items(icons) { icon ->
            val isSelected = icon == selected
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) mindColors.primary 
                        else mindColors.surfaceElevated
                    )
                    .clickable { onSelect(icon) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getHabitIcon(icon),
                    contentDescription = null,
                    tint = if (isSelected) 
                        mindColors.textInverse 
                        else mindColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorPicker(
    selected: String,
    onSelect: (String) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val colors = listOf(
        "#E8C547", // Primary yellow
        "#5B9BD5", // Blue
        "#52C97B", // Green
        "#E8965A", // Orange
        "#A78BC7"  // Purple
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        colors.forEach { hex ->
            val color = Color(android.graphics.Color.parseColor(hex))
            val isSelected = hex == selected
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) mindColors.textPrimary 
                                else mindColors.separatorThin,
                        shape = CircleShape
                    )
                    .clickable { onSelect(hex) }
            )
        }
    }
}

fun getHabitIcon(iconName: String): ImageVector = when (iconName) {
    "star" -> Icons.Default.Star
    "book" -> Icons.Default.MenuBook
    "fitness" -> Icons.Default.FitnessCenter
    "water" -> Icons.Default.WaterDrop
    "sleep" -> Icons.Default.Bedtime
    "walk" -> Icons.Default.DirectionsWalk
    "run" -> Icons.Default.DirectionsRun
    "food" -> Icons.Default.Restaurant
    "study" -> Icons.Default.School
    "code" -> Icons.Default.Code
    "music" -> Icons.Default.MusicNote
    "art" -> Icons.Default.Brush
    "translate" -> Icons.Default.Translate
    "psychology" -> Icons.Default.Psychology
    "spa" -> Icons.Default.Spa
    "coffee" -> Icons.Default.Coffee
    "eco" -> Icons.Default.Eco
    "bike" -> Icons.Default.DirectionsBike
    "trophy" -> Icons.Default.EmojiEvents
    "timer" -> Icons.Default.Timer
    else -> Icons.Default.Star
}
