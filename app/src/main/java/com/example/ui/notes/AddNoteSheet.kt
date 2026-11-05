package com.example.ui.notes

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.Note
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSSheet
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@Composable
fun AddNoteSheet(
    existingNote: Note?,
    onSave: (title: String, content: String, color: String, pinned: Boolean) -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var color by remember { mutableStateOf(existingNote?.color ?: "#E8C547") }
    var pinned by remember { mutableStateOf(existingNote?.pinned ?: false) }

    val colors = listOf(
        "#E8C547", // Yellow / Gold
        "#3B82F6", // Blue
        "#10B981", // Emerald
        "#8B5CF6", // Purple
        "#F43F5E", // Rose
        "#64748B"  // Slate
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingNote != null) stringResource(R.string.notes_title) else stringResource(R.string.notes_new),
                    style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                    color = mindColors.textPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { pinned = !pinned },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (pinned) stringResource(R.string.notes_unpin) else stringResource(R.string.notes_pin),
                            tint = if (pinned) mindColors.primary else mindColors.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (onDelete != null) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.common_delete),
                                tint = mindColors.accentRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Color palette selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colors.forEach { hex ->
                    val c = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (e: Exception) {
                        mindColors.primary
                    }
                    val isSelected = color.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (isSelected) 2.5.dp else 0.dp,
                                color = if (isSelected) mindColors.textPrimary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { color = hex }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Title Input (Optional)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(mindColors.surfaceElevated)
                    .border(1.dp, mindColors.separatorThin, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (title.isEmpty()) {
                    Text(
                        text = stringResource(R.string.notes_title_hint),
                        style = mindTypography.body,
                        color = mindColors.textTertiary
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = mindTypography.body.copy(color = mindColors.textPrimary, fontWeight = FontWeight.SemiBold),
                    cursorBrush = SolidColor(mindColors.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            // Content Input (Multiline)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(mindColors.surfaceElevated)
                    .border(1.dp, mindColors.separatorThin, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                if (content.isEmpty()) {
                    Text(
                        text = stringResource(R.string.notes_hint),
                        style = mindTypography.body,
                        color = mindColors.textTertiary
                    )
                }
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    textStyle = mindTypography.body.copy(color = mindColors.textPrimary),
                    cursorBrush = SolidColor(mindColors.primary),
                    modifier = Modifier.fillMaxWidth()
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
                        val trimmed = content.trim()
                        if (trimmed.isNotEmpty() || title.trim().isNotEmpty()) {
                            val finalTitle = title.ifBlank {
                                trimmed.lines().firstOrNull()?.take(30) ?: tr("ملاحظة", "Note")
                            }
                            onSave(finalTitle, trimmed, color, pinned)
                        }
                    },
                    style = IOSButtonStyle.Filled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
