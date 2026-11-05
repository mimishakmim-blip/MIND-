package com.example.ui.journal

import com.example.i18n.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.entity.JournalEntry
import com.example.domain.util.DateUtils
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSCard
import com.example.ui.components.IOSSheet
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun moodToEmoji(mood: String): String = when (mood.uppercase()) {
    "GREAT" -> "😄"
    "GOOD" -> "😊"
    "NEUTRAL" -> "😐"
    "BAD" -> "😕"
    "AWFUL" -> "😢"
    else -> "😐"
}

@Composable
fun JournalScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: JournalViewModel = viewModel()
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val state by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mindColors.background)
            .testTag("journal_screen")
    ) {
        ScreenHeader(
            title = stringResource(R.string.journal_title),
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Today's entry card
            item {
                TodayEntryCard(
                    entry = state.todayEntry,
                    onEdit = { showAddSheet = true },
                    modifier = Modifier.padding(20.dp)
                )
            }

            // Recent entries title
            if (state.recentEntries.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.journal_recent),
                        color = mindColors.textSecondary,
                        style = mindTypography.footnote.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                items(state.recentEntries, key = { it.id }) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddJournalSheet(
            existingEntry = state.todayEntry,
            onSave = { mood, text ->
                viewModel.saveTodayEntry(mood, text)
                showAddSheet = false
            },
            onDismiss = { showAddSheet = false }
        )
    }
}

@Composable
private fun TodayEntryCard(
    entry: JournalEntry?,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    IOSCard(
        modifier = modifier.fillMaxWidth()
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
                    Text(
                        text = if (entry != null) moodToEmoji(entry.mood) else "📖",
                        fontSize = 24.sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.journal_today),
                        style = mindTypography.headline.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.textPrimary
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = tr("تعديل", "Edit"),
                        tint = mindColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (entry != null && entry.text.isNotBlank()) {
                Text(
                    text = entry.text,
                    style = mindTypography.body,
                    color = mindColors.textPrimary,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = stringResource(R.string.journal_empty_hint),
                    style = mindTypography.subhead,
                    color = mindColors.textTertiary
                )
                Spacer(Modifier.height(14.dp))
                IOSButton(
                    text = stringResource(R.string.journal_how_was_day),
                    onClick = onEdit,
                    style = IOSButtonStyle.Filled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale("ar")) }
    val dateStr = remember(entry.date) { dateFormat.format(Date(entry.date)) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(mindColors.surfaceElevated)
            .border(1.dp, mindColors.separatorThin, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = mindTypography.footnote,
                    color = mindColors.textSecondary
                )
                Text(
                    text = moodToEmoji(entry.mood),
                    fontSize = 20.sp
                )
            }
            if (entry.text.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = entry.text,
                    style = mindTypography.body,
                    color = mindColors.textPrimary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AddJournalSheet(
    existingEntry: JournalEntry?,
    onSave: (mood: String, text: String) -> Unit,
    onDismiss: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    var mood by remember { mutableStateOf(existingEntry?.mood ?: "NEUTRAL") }
    var text by remember { mutableStateOf(existingEntry?.text ?: "") }

    val moods = listOf(
        "GREAT" to "😄",
        "GOOD" to "😊",
        "NEUTRAL" to "😐",
        "BAD" to "😕",
        "AWFUL" to "😢"
    )

    IOSSheet(
        onDismiss = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.journal_how_was_day),
                style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                color = mindColors.textPrimary
            )

            Spacer(Modifier.height(16.dp))

            // Mood selector (5 emojis)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                moods.forEach { (mKey, emoji) ->
                    val isSelected = mood == mKey
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) mindColors.primary.copy(alpha = 0.2f) else mindColors.surfaceElevated)
                            .border(
                                1.5.dp,
                                if (isSelected) mindColors.primary else Color.Transparent,
                                CircleShape
                            )
                            .clickable { mood = mKey },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 26.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Multiline text input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(mindColors.surfaceElevated)
                    .border(1.dp, mindColors.separatorThin, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.journal_notes_hint),
                        style = mindTypography.body,
                        color = mindColors.textTertiary
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = mindTypography.body.copy(color = mindColors.textPrimary),
                    cursorBrush = SolidColor(mindColors.primary),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(20.dp))

            // Action Buttons
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
                        onSave(mood, text)
                    },
                    style = IOSButtonStyle.Filled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
