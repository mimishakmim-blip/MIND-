package com.example.ui.fitness

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

data class WorkoutTypeItem(
    val key: String,
    val nameAr: String,
    val icon: ImageVector,
    val emoji: String,
    val defaultMet: Float
)

val WORKOUT_TYPES = listOf(
    WorkoutTypeItem("walking", tr("مشي", "Walking"), Icons.Default.DirectionsWalk, "🚶‍♂️", 3.8f),
    WorkoutTypeItem("running", tr("جري", "Running"), Icons.Default.DirectionsRun, "🏃‍♂️", 9.8f),
    WorkoutTypeItem("cycling", tr("دراجة", "Cycling"), Icons.Default.DirectionsBike, "🚴‍♂️", 7.5f),
    WorkoutTypeItem("strength", tr("حديد وتمارين", "Weights & strength"), Icons.Default.FitnessCenter, "🏋️‍♂️", 5.0f),
    WorkoutTypeItem("hiit", tr("كارديو HIIT", "HIIT cardio"), Icons.Default.LocalFireDepartment, "🔥", 10.0f),
    WorkoutTypeItem("swimming", tr("سباحة", "Swimming"), Icons.Default.Pool, "🏊‍♂️", 6.0f),
    WorkoutTypeItem("yoga", tr("يوغا وإطالة", "Yoga & stretching"), Icons.Default.SelfImprovement, "🧘‍♂️", 2.8f),
    WorkoutTypeItem("sports", tr("كرة ورياضة", "Ball sports"), Icons.Default.SportsSoccer, "⚽", 7.0f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    userWeightKg: Float = 70f,
    onSaveWorkout: (
        type: String,
        durationMinutes: Int,
        calories: Int,
        distanceKm: Float,
        steps: Int,
        intensity: String,
        notes: String
    ) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    var selectedType by remember { mutableStateOf(WORKOUT_TYPES.first()) }
    var durationMinutes by remember { mutableIntStateOf(30) }
    var intensity by remember { mutableStateOf("medium") } // "low", "medium", "high"
    var customCalories by remember { mutableStateOf<Int?>(null) }
    var distanceKmText by remember { mutableStateOf("") }
    var stepsText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    // MET multiplier based on intensity
    val intensityMultiplier = when (intensity) {
        "low" -> 0.85f
        "high" -> 1.25f
        else -> 1.0f
    }

    val calculatedCalories = remember(selectedType, durationMinutes, intensity, userWeightKg) {
        val calories = selectedType.defaultMet * intensityMultiplier * userWeightKg * (durationMinutes / 60f)
        calories.toInt().coerceAtLeast(10)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = mindColors.surfaceElevated,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(mindColors.textTertiary.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.workout_add_title),
                    style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                    color = mindColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = mindColors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 1. Workout Type Selector
            Text(
                text = stringResource(R.string.workout_type_label),
                style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textSecondary
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(WORKOUT_TYPES) { item ->
                    val isSelected = selectedType.key == item.key
                    val chipBg = if (isSelected) mindColors.primary else mindColors.surfaceHighlight
                    val chipContent = if (isSelected) Color.White else mindColors.textPrimary

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(chipBg)
                            .clickable { selectedType = item }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.emoji, fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.nameAr,
                            style = mindTypography.footnote.copy(fontWeight = FontWeight.Medium),
                            color = chipContent
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 2. Duration Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.workout_duration_label),
                    style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                    color = mindColors.textSecondary
                )
                Text(
                    text = tr("$durationMinutes دقيقة", "$durationMinutes min"),
                    style = mindTypography.callout.copy(fontWeight = FontWeight.Bold),
                    color = mindColors.primary
                )
            }
            Spacer(Modifier.height(10.dp))

            val durationPresets = listOf(15, 20, 30, 45, 60, 90)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durationPresets.forEach { preset ->
                    val isSelected = durationMinutes == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) mindColors.primary.copy(alpha = 0.15f) else mindColors.surfaceHighlight)
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) mindColors.primary else mindColors.separatorThin,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { durationMinutes = preset }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$preset",
                            style = mindTypography.footnote.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isSelected) mindColors.primary else mindColors.textPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 3. Intensity Selector
            Text(
                text = stringResource(R.string.workout_intensity_label),
                style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textSecondary
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Triple("low", stringResource(R.string.intensity_low), "🌱"),
                    Triple("medium", stringResource(R.string.intensity_medium), "⚡"),
                    Triple("high", stringResource(R.string.intensity_high), "🔥")
                ).forEach { (key, label, emoji) ->
                    val isSelected = intensity == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) mindColors.primary.copy(alpha = 0.15f) else mindColors.surfaceHighlight)
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) mindColors.primary else mindColors.separatorThin,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { intensity = key }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = emoji, fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = mindTypography.footnote.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) mindColors.primary else mindColors.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 4. Calorie Burn Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(mindColors.surfaceHighlight)
                    .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.workout_calories_burned),
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                            color = mindColors.textPrimary
                        )
                        Text(
                            text = tr("محسوبة وفق وزنك والشدة", "Calculated from your weight and intensity"),
                            style = mindTypography.caption,
                            color = mindColors.textSecondary
                        )
                    }
                }
                Text(
                    text = tr("${customCalories ?: calculatedCalories} سعرة", "${customCalories ?: calculatedCalories} kcal"),
                    style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFF5252)
                )
            }

            Spacer(Modifier.height(16.dp))

            // 5. Distance and Steps Inputs (Optional)
            if (selectedType.key in listOf("walking", "running", "cycling")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = distanceKmText,
                        onValueChange = { distanceKmText = it },
                        label = { Text(stringResource(R.string.workout_distance_km)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mindColors.primary,
                            unfocusedBorderColor = mindColors.separatorThin
                        )
                    )

                    if (selectedType.key in listOf("walking", "running")) {
                        OutlinedTextField(
                            value = stepsText,
                            onValueChange = { stepsText = it },
                            label = { Text(stringResource(R.string.workout_steps)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = mindColors.primary,
                                unfocusedBorderColor = mindColors.separatorThin
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // 6. Notes Input
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text(stringResource(R.string.workout_notes_hint)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = mindColors.primary,
                    unfocusedBorderColor = mindColors.separatorThin
                )
            )

            Spacer(Modifier.height(24.dp))

            // 7. Save Button
            IOSButton(
                text = stringResource(R.string.workout_save_button),
                onClick = {
                    val finalCalories = customCalories ?: calculatedCalories
                    val distance = distanceKmText.toFloatOrNull() ?: 0f
                    val steps = stepsText.toIntOrNull() ?: if (selectedType.key == "walking") (durationMinutes * 100) else if (selectedType.key == "running") (durationMinutes * 150) else 0

                    onSaveWorkout(
                        selectedType.key,
                        durationMinutes,
                        finalCalories,
                        distance,
                        steps,
                        intensity,
                        notesText
                    )
                    onDismiss()
                },
                style = IOSButtonStyle.Filled,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_workout_button")
            )
        }
    }
}
