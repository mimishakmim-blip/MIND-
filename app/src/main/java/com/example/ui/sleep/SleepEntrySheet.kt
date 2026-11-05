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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSSheet
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography
import java.util.Calendar

@Composable
fun SleepEntrySheet(
    onSave: (bedTime: Long, wakeTime: Long, quality: Int, interruptions: Int, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    // Bed time: default 11:00 PM yesterday
    var bedHour by remember { mutableIntStateOf(11) }
    var bedMinute by remember { mutableIntStateOf(0) }
    var bedIsAm by remember { mutableStateOf(false) } // PM

    // Wake time: default 7:00 AM today
    var wakeHour by remember { mutableIntStateOf(7) }
    var wakeMinute by remember { mutableIntStateOf(0) }
    var wakeIsAm by remember { mutableStateOf(true) } // AM

    var quality by remember { mutableIntStateOf(4) }
    var interruptions by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }

    // Calculate timestamps
    val (bedMillis, wakeMillis, durationMinutes) = remember(
        bedHour, bedMinute, bedIsAm,
        wakeHour, wakeMinute, wakeIsAm
    ) {
        val now = Calendar.getInstance()

        val wakeCal = Calendar.getInstance().apply {
            set(Calendar.HOUR, if (wakeHour == 12) 0 else wakeHour)
            set(Calendar.AM_PM, if (wakeIsAm) Calendar.AM else Calendar.PM)
            set(Calendar.MINUTE, wakeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val bedCal = Calendar.getInstance().apply {
            timeInMillis = wakeCal.timeInMillis
            // Bedtime was likely earlier: if wake is morning and bed is PM, bed was yesterday
            set(Calendar.HOUR, if (bedHour == 12) 0 else bedHour)
            set(Calendar.AM_PM, if (bedIsAm) Calendar.AM else Calendar.PM)
            set(Calendar.MINUTE, bedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis >= wakeCal.timeInMillis) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
        }

        val diff = (wakeCal.timeInMillis - bedCal.timeInMillis).coerceAtLeast(0)
        val minutes = (diff / 60000L).toInt()
        Triple(bedCal.timeInMillis, wakeCal.timeInMillis, minutes)
    }

    val hours = durationMinutes / 60
    val remainingMinutes = durationMinutes % 60

    val qualityOptions = listOf(
        1 to tr("😫 سيء", "😫 Poor"),
        2 to tr("🥱 مقبول", "🥱 Fair"),
        3 to tr("😴 جيد", "😴 Good"),
        4 to tr("🌙 ممتاز", "🌙 Excellent"),
        5 to tr("✨ رائع", "✨ Great")
    )

    IOSSheet(
        onDismiss = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("🌙 تسجيل النوم", "🌙 Log sleep"),
                    style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                    color = mindColors.textPrimary
                )

                // Calculated Duration Badge
                Box(
                    modifier = Modifier
                        .background(mindColors.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tr("${hours}س ${remainingMinutes}د", "${hours}h ${remainingMinutes}m"),
                        style = mindTypography.caption.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.primary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Bedtime Selector
            Text(
                text = tr("🛏️ وقت الذهاب للنوم", "🛏️ Bedtime"),
                style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            TimePickerRow(
                hour = bedHour,
                minute = bedMinute,
                isAm = bedIsAm,
                onHourChange = { bedHour = it },
                onMinuteChange = { bedMinute = it },
                onAmToggle = { bedIsAm = !bedIsAm }
            )

            Spacer(Modifier.height(18.dp))

            // Wake time Selector
            Text(
                text = tr("⏰ وقت الاستيقاظ", "⏰ Wake-up time"),
                style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            TimePickerRow(
                hour = wakeHour,
                minute = wakeMinute,
                isAm = wakeIsAm,
                onHourChange = { wakeHour = it },
                onMinuteChange = { wakeMinute = it },
                onAmToggle = { wakeIsAm = !wakeIsAm }
            )

            Spacer(Modifier.height(20.dp))

            // Quality
            Text(
                text = tr("⭐ تقييم جودة النوم", "⭐ Rate your sleep quality"),
                style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textPrimary
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                qualityOptions.forEach { (rating, label) ->
                    val isSelected = quality == rating
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) mindColors.primary.copy(alpha = 0.22f)
                                else mindColors.surfaceElevated
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) mindColors.primary else mindColors.separatorThin,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { quality = rating }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = mindTypography.caption.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) mindColors.primary else mindColors.textPrimary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Interruptions Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tr("👀 مرات الاستيقاظ ليلاً", "👀 Night wake-ups"),
                        style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                        color = mindColors.textPrimary
                    )
                    Text(
                        text = tr("عدد مرات الانقطاع أثناء النوم", "Number of interruptions during sleep"),
                        style = mindTypography.caption,
                        color = mindColors.textSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(mindColors.surfaceElevated)
                            .border(0.5.dp, mindColors.separatorThin, CircleShape)
                            .clickable { if (interruptions > 0) interruptions-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                            color = mindColors.textPrimary
                        )
                    }

                    Text(
                        text = "$interruptions",
                        style = mindTypography.headline.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.textPrimary
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(mindColors.surfaceElevated)
                            .border(0.5.dp, mindColors.separatorThin, CircleShape)
                            .clickable { interruptions++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                            color = mindColors.textPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Notes
            Text(
                text = tr("📝 ملاحظات إضافية (اختياري)", "📝 Extra notes (optional)"),
                style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(mindColors.surfaceElevated)
                    .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                if (notes.isEmpty()) {
                    Text(
                        text = tr("مثال: أحلام جميلة، استيقظت نشيطاً...", "e.g. Sweet dreams, woke up energized..."),
                        style = mindTypography.subhead,
                        color = mindColors.textTertiary
                    )
                }
                BasicTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    textStyle = mindTypography.subhead.copy(color = mindColors.textPrimary),
                    cursorBrush = SolidColor(mindColors.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IOSButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = onDismiss,
                    style = IOSButtonStyle.Text,
                    modifier = Modifier.weight(1f)
                )

                IOSButton(
                    text = stringResource(R.string.common_save),
                    onClick = {
                        onSave(bedMillis, wakeMillis, quality, interruptions, notes)
                    },
                    style = IOSButtonStyle.Filled,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

@Composable
private fun TimePickerRow(
    hour: Int,
    minute: Int,
    isAm: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onAmToggle: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(mindColors.surfaceElevated)
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour Stepper
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(mindColors.background)
                    .clickable {
                        val next = if (hour <= 1) 12 else hour - 1
                        onHourChange(next)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "▼", fontSize = 12.sp, color = mindColors.textPrimary)
            }

            Text(
                text = String.format("%02d", hour),
                style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                color = mindColors.textPrimary
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(mindColors.background)
                    .clickable {
                        val next = if (hour >= 12) 1 else hour + 1
                        onHourChange(next)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "▲", fontSize = 12.sp, color = mindColors.textPrimary)
            }
        }

        Text(
            text = ":",
            style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
            color = mindColors.textSecondary
        )

        // Minute Stepper (00, 15, 30, 45 or 5-min intervals)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(mindColors.background)
                    .clickable {
                        val next = if (minute <= 0) 45 else ((minute - 15) / 15) * 15
                        onMinuteChange(next.coerceAtLeast(0))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "▼", fontSize = 12.sp, color = mindColors.textPrimary)
            }

            Text(
                text = String.format("%02d", minute),
                style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                color = mindColors.textPrimary
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(mindColors.background)
                    .clickable {
                        val next = if (minute >= 45) 0 else ((minute + 15) / 15) * 15
                        onMinuteChange(next.coerceAtMost(59))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "▲", fontSize = 12.sp, color = mindColors.textPrimary)
            }
        }

        // AM/PM Toggle Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(mindColors.primary.copy(alpha = 0.18f))
                .border(1.dp, mindColors.primary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .clickable { onAmToggle() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isAm) tr("صباحاً (AM)", "AM") else tr("مساءً (PM)", "PM"),
                style = mindTypography.caption.copy(fontWeight = FontWeight.Bold),
                color = mindColors.primary
            )
        }
    }
}
