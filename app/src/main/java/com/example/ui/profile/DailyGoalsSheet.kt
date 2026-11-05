package com.example.ui.profile

import com.example.i18n.tr
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSSheet
import com.example.ui.components.IOSStepper
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@Composable
fun DailyGoalsSheet(
    state: ProfileSettingsUiState,
    onSave: (steps: Int, water: Int, study: Int, habits: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    var stepsGoal by remember { mutableIntStateOf(state.dailyGoalSteps) }
    var waterGoal by remember { mutableIntStateOf(state.dailyGoalWater) }
    var studyGoal by remember { mutableIntStateOf(state.dailyGoalStudy) }
    var habitsGoal by remember { mutableIntStateOf(state.dailyGoalHabits) }

    IOSSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.settings_daily_goals),
                style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                color = mindColors.textPrimary
            )

            Spacer(Modifier.height(20.dp))

            GoalRow(
                label = stringResource(R.string.goal_steps),
                value = stepsGoal,
                unit = tr("خطوة", "steps"),
                step = 500,
                min = 1000,
                max = 30000,
                onValueChange = { stepsGoal = it }
            )

            Spacer(Modifier.height(16.dp))

            GoalRow(
                label = stringResource(R.string.goal_water),
                value = waterGoal,
                unit = tr("كوب", "glass"),
                step = 1,
                min = 1,
                max = 20,
                onValueChange = { waterGoal = it }
            )

            Spacer(Modifier.height(16.dp))

            GoalRow(
                label = stringResource(R.string.goal_study),
                value = studyGoal,
                unit = tr("دقيقة", "min"),
                step = 15,
                min = 15,
                max = 480,
                onValueChange = { studyGoal = it }
            )

            Spacer(Modifier.height(16.dp))

            GoalRow(
                label = stringResource(R.string.goal_habits),
                value = habitsGoal,
                unit = tr("عادة", "habit"),
                step = 1,
                min = 1,
                max = 20,
                onValueChange = { habitsGoal = it }
            )

            Spacer(Modifier.height(24.dp))

            IOSButton(
                text = stringResource(R.string.common_save),
                onClick = {
                    onSave(stepsGoal, waterGoal, studyGoal, habitsGoal)
                },
                style = IOSButtonStyle.Filled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GoalRow(
    label: String,
    value: Int,
    unit: String,
    step: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit
) {
    val mindColors = MaterialTheme.mindColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = mindColors.textPrimary,
                fontSize = 15.sp
            )
            Text(
                text = "$value $unit",
                color = mindColors.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        IOSStepper(
            value = value,
            onValueChange = onValueChange,
            min = min,
            max = max,
            step = step
        )
    }
}
