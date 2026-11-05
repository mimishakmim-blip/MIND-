package com.example.ui.fitness

import com.example.i18n.tr
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.entity.WorkoutLog
import com.example.ui.components.IOSAlert
import com.example.ui.components.IOSGlassContainer
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(
    onBack: () -> Unit,
    onNavigateToNutrition: () -> Unit = {},
    viewModel: FitnessViewModel = viewModel()
) {
    BackHandler { onBack() }

    val state by viewModel.uiState.collectAsState()
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    var showAddWorkoutSheet by remember { mutableStateOf(false) }
    var workoutToDelete by remember { mutableStateOf<WorkoutLog?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = mindColors.background,
        topBar = {
            ScreenHeader(
                title = stringResource(R.string.fitness_title),
                onBack = onBack,
                trailing = {
                    IconButton(
                        onClick = { showAddWorkoutSheet = true },
                        modifier = Modifier.testTag("add_workout_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.workout_add_title),
                            tint = mindColors.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // 1. Triple Activity Rings Hero Card
            item {
                ActivityRingsHeroCard(state = state)
            }

            // 2. Hydration Water Tracker Card
            item {
                WaterHydrationCard(
                    waterGlasses = state.waterGlasses,
                    waterGoal = state.waterGoal,
                    onAddWater = { viewModel.addWater() },
                    onRemoveWater = { viewModel.removeWater() }
                )
            }

            // Nutrition & calories live under Fitness
            item {
                com.example.ui.components.ShortcutCard(
                    emoji = "🍽️",
                    title = tr("التغذية والسعرات", "Nutrition & calories"),
                    subtitle = tr("سجّل وجباتك والماكروز", "Log meals and macros"),
                    onClick = onNavigateToNutrition
                )
            }

            // 3. Step Counter Card with Quick Adds
            item {
                StepCounterCard(
                    steps = state.steps,
                    stepGoal = state.stepGoal,
                    distanceKm = state.distanceKm,
                    isSensorAvailable = state.isSensorAvailable,
                    walkingStreak = state.walkingStreak,
                    onAddSteps = { viewModel.addSteps(it) }
                )
            }

            // 4. Weekly Steps Bar Chart
            item {
                WeeklyStepsChartCard(
                    weeklyActivities = state.weeklyActivities,
                    stepGoal = state.stepGoal
                )
            }

            // 5. Workouts Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.fitness_workouts_title),
                        style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.textPrimary
                    )
                    Text(
                        text = tr("+ تسجيل تمرين", "+ Log workout"),
                        style = mindTypography.footnote.copy(fontWeight = FontWeight.SemiBold),
                        color = mindColors.primary,
                        modifier = Modifier
                            .clickable { showAddWorkoutSheet = true }
                            .padding(4.dp)
                    )
                }
            }

            // Workouts List
            if (state.todayWorkouts.isEmpty() && state.recentWorkouts.isEmpty()) {
                item {
                    EmptyWorkoutsCard(onAddClick = { showAddWorkoutSheet = true })
                }
            } else {
                val displayWorkouts = if (state.todayWorkouts.isNotEmpty()) state.todayWorkouts else state.recentWorkouts.take(5)
                items(displayWorkouts, key = { it.id }) { workout ->
                    WorkoutItemCard(
                        workout = workout,
                        onDelete = { workoutToDelete = workout }
                    )
                }
            }

            // 6. Healthy Lifestyle & Recovery Advice
            item {
                FitnessTipsCard()
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // Add Workout Modal Sheet
    if (showAddWorkoutSheet) {
        AddWorkoutSheet(
            sheetState = sheetState,
            onDismiss = { showAddWorkoutSheet = false },
            userWeightKg = state.userWeight,
            onSaveWorkout = { type, duration, calories, distance, steps, intensity, notes ->
                viewModel.logWorkout(type, duration, calories, distance, steps, intensity, notes)
            }
        )
    }

    // Delete Workout Confirmation Alert
    workoutToDelete?.let { workout ->
        IOSAlert(
            title = stringResource(R.string.workout_delete_title),
            message = stringResource(R.string.workout_delete_confirm),
            confirmText = stringResource(R.string.common_delete),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                viewModel.deleteWorkout(workout)
                workoutToDelete = null
            },
            onDismiss = { workoutToDelete = null }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Activity Rings Hero Card (Apple Fitness Style)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ActivityRingsHeroCard(
    state: FitnessUiState,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    val redMove = Color(0xFFFF2D55)    // Move / Active Calories
    val greenExercise = Color(0xFF30D158) // Exercise Minutes
    val blueSteps = Color(0xFF007AFF)    // Steps / Stand

    val animatedCalorieProgress by animateFloatAsState(
        targetValue = state.calorieProgress.coerceIn(0f, 1f),
        label = "calorie_ring"
    )
    val animatedExerciseProgress by animateFloatAsState(
        targetValue = state.exerciseProgress.coerceIn(0f, 1f),
        label = "exercise_ring"
    )
    val animatedStepProgress by animateFloatAsState(
        targetValue = state.stepProgress.coerceIn(0f, 1f),
        label = "step_ring"
    )

    IOSGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Concentric 3 Rings Canvas
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)

                    // Outer Ring: Red Move (Calories)
                    val radius1 = (size.minDimension / 2f) - (strokeWidth / 2f)
                    drawCircle(redMove.copy(alpha = 0.30f), radius = radius1, center = centerOffset, style = Stroke(strokeWidth))
                    val sweep1 = maxOf(14f, 360f * animatedCalorieProgress)
                    drawArc(
                        color = redMove,
                        startAngle = -90f,
                        sweepAngle = sweep1,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius1, centerOffset.y - radius1),
                        size = Size(radius1 * 2f, radius1 * 2f),
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )

                    // Middle Ring: Green Exercise (Minutes)
                    val radius2 = radius1 - strokeWidth - 4.dp.toPx()
                    drawCircle(greenExercise.copy(alpha = 0.30f), radius = radius2, center = centerOffset, style = Stroke(strokeWidth))
                    val sweep2 = maxOf(14f, 360f * animatedExerciseProgress)
                    drawArc(
                        color = greenExercise,
                        startAngle = -90f,
                        sweepAngle = sweep2,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius2, centerOffset.y - radius2),
                        size = Size(radius2 * 2f, radius2 * 2f),
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )

                    // Inner Ring: Blue Steps
                    val radius3 = radius2 - strokeWidth - 4.dp.toPx()
                    drawCircle(blueSteps.copy(alpha = 0.30f), radius = radius3, center = centerOffset, style = Stroke(strokeWidth))
                    val sweep3 = maxOf(14f, 360f * animatedStepProgress)
                    drawArc(
                        color = blueSteps,
                        startAngle = -90f,
                        sweepAngle = sweep3,
                        useCenter = false,
                        topLeft = Offset(centerOffset.x - radius3, centerOffset.y - radius3),
                        size = Size(radius3 * 2f, radius3 * 2f),
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center Icon
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = redMove,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Right: Ring Stats Metrics
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Red: Calories
                RingMetricRow(
                    label = tr("الحركة", "Movement"),
                    current = "${state.activeCalories}",
                    target = tr("/ ${state.calorieGoal} سعرة", "/ ${state.calorieGoal} kcal"),
                    color = redMove
                )
                // Green: Exercise
                RingMetricRow(
                    label = tr("التمرين", "Workout"),
                    current = "${state.activeMinutes}",
                    target = tr("/ ${state.exerciseGoal} دقيقة", "/ ${state.exerciseGoal} min"),
                    color = greenExercise
                )
                // Blue: Steps
                RingMetricRow(
                    label = tr("الخطوات", "Steps"),
                    current = "${state.steps}",
                    target = "/ ${state.stepGoal}",
                    color = blueSteps
                )
                // Heart Rate: 0 BPM
                RingMetricRow(
                    label = tr("النبض", "Heart rate"),
                    current = "0",
                    target = "0 BPM",
                    color = Color(0xFFFF3B30)
                )
            }
        }
    }
}

@Composable
private fun RingMetricRow(
    label: String,
    current: String,
    target: String,
    color: Color
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = mindTypography.micro.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textSecondary
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = current,
                    style = mindTypography.callout.copy(fontWeight = FontWeight.Bold),
                    color = mindColors.textPrimary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = target,
                    style = mindTypography.caption,
                    color = mindColors.textTertiary
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Water Hydration Card
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WaterHydrationCard(
    waterGlasses: Int,
    waterGoal: Int,
    onAddWater: () -> Unit,
    onRemoveWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val cyanWater = Color(0xFF00C7BE)

    val currentMl = waterGlasses * 250
    val targetMl = waterGoal * 250
    val percent = if (waterGoal > 0) ((waterGlasses.toFloat() / waterGoal) * 100).toInt() else 0

    IOSGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(20.dp)),
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
                    Text(text = "💧", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.water_hydration_title),
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                            color = mindColors.textPrimary
                        )
                        Text(
                            text = tr("$currentMl / $targetMl مل ($percent%)", "$currentMl / $targetMl ml ($percent%)"),
                            style = mindTypography.caption,
                            color = mindColors.textSecondary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Minus Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(mindColors.surfaceHighlight)
                            .clickable(enabled = waterGlasses > 0) { onRemoveWater() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "−",
                            style = mindTypography.headline.copy(fontWeight = FontWeight.Bold),
                            color = if (waterGlasses > 0) mindColors.textPrimary else mindColors.textTertiary
                        )
                    }

                    // Plus Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(cyanWater)
                            .clickable { onAddWater() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add water",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 8 Cups Visual Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..8).forEach { cupIndex ->
                    val isFilled = cupIndex <= waterGlasses
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            if (cupIndex <= waterGlasses) {
                                onRemoveWater()
                            } else {
                                onAddWater()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isFilled) Icons.Default.WaterDrop else Icons.Default.Opacity,
                            contentDescription = null,
                            tint = if (isFilled) cyanWater else mindColors.textTertiary.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "$cupIndex",
                            style = mindTypography.micro,
                            color = if (isFilled) cyanWater else mindColors.textTertiary
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Step Counter Card
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StepCounterCard(
    steps: Int,
    stepGoal: Int,
    distanceKm: Float,
    isSensorAvailable: Boolean,
    walkingStreak: Int,
    onAddSteps: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val blueColor = Color(0xFF007AFF)

    IOSGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(20.dp)),
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
                    Text(text = "🚶‍♂️", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.steps_tracker_title),
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                            color = mindColors.textPrimary
                        )
                        Text(
                            text = String.format(Locale.US, tr("%.2f كم تقريباً", "≈ %.2f km"), distanceKm),
                            style = mindTypography.caption,
                            color = mindColors.textSecondary
                        )
                    }
                }

                if (walkingStreak > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF9500).copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = tr("$walkingStreak أيام", "$walkingStreak days"),
                            style = mindTypography.caption.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF9500)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Big Steps Count
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%,d", steps),
                    style = mindTypography.largeTitle.copy(fontWeight = FontWeight.ExtraBold),
                    color = mindColors.textPrimary
                )
                Text(
                    text = "/ ${String.format(Locale.US, "%,d", stepGoal)} " + tr("خطوة", "steps"),
                    style = mindTypography.callout,
                    color = mindColors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Step Progress Bar
            val progress = if (stepGoal > 0) (steps.toFloat() / stepGoal).coerceIn(0f, 1f) else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(blueColor.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(blueColor)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Quick Add Step Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(500, 1000, 2500).forEach { addCount ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(mindColors.surfaceHighlight)
                            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(12.dp))
                            .clickable { onAddSteps(addCount) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$addCount",
                            style = mindTypography.footnote.copy(fontWeight = FontWeight.SemiBold),
                            color = blueColor
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Weekly Steps Chart Card
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WeeklyStepsChartCard(
    weeklyActivities: List<DailyActivityBar>,
    stepGoal: Int,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val blueColor = Color(0xFF007AFF)

    val maxSteps = weeklyActivities.maxOfOrNull { it.steps }?.coerceAtLeast(stepGoal) ?: stepGoal

    IOSGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(20.dp)),
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
                Text(
                    text = stringResource(R.string.weekly_steps_trend),
                    style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                    color = mindColors.textPrimary
                )
                Text(
                    text = tr("الهدف اليومي: $stepGoal", "Daily goal: $stepGoal"),
                    style = mindTypography.caption,
                    color = mindColors.textSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyActivities.forEach { bar ->
                    val barHeightFraction = (bar.steps.toFloat() / maxSteps).coerceIn(0.08f, 1f)
                    val isGoalMet = bar.steps >= bar.stepGoal
                    val barColor = when {
                        bar.isToday -> mindColors.primary
                        isGoalMet -> Color(0xFF30D158)
                        else -> blueColor.copy(alpha = 0.5f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (bar.steps > 0) {
                            Text(
                                text = "${(bar.steps / 1000f).let { String.format(Locale.US, "%.1f", it) }}k",
                                style = mindTypography.micro,
                                color = mindColors.textSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                        }

                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .fillMaxHeight(barHeightFraction)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(barColor)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = bar.dayInitial,
                            style = mindTypography.footnote.copy(
                                fontWeight = if (bar.isToday) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (bar.isToday) mindColors.primary else mindColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Workout Item Card
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WorkoutItemCard(
    workout: WorkoutLog,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    val typeItem = WORKOUT_TYPES.find { it.key == workout.type } ?: WORKOUT_TYPES.first()
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(workout.timestamp) { timeFormat.format(Date(workout.timestamp)) }

    IOSGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(mindColors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = typeItem.emoji, fontSize = 22.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = typeItem.nameAr,
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                            color = mindColors.textPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = formattedTime,
                            style = mindTypography.micro,
                            color = mindColors.textTertiary
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("${workout.durationMinutes} دقيقة", "${workout.durationMinutes} min"),
                            style = mindTypography.caption,
                            color = mindColors.textSecondary
                        )
                        Text(text = "•", color = mindColors.textTertiary)
                        Text(
                            text = tr("${workout.caloriesBurned} سعرة", "${workout.caloriesBurned} kcal"),
                            style = mindTypography.caption.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFFFF5252)
                        )
                        if (workout.distanceKm > 0f) {
                            Text(text = "•", color = mindColors.textTertiary)
                            Text(
                                text = String.format(Locale.US, tr("%.1f كم", "%.1f km"), workout.distanceKm),
                                style = mindTypography.caption,
                                color = mindColors.textSecondary
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete workout",
                    tint = mindColors.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Empty Workouts Card
// ═══════════════════════════════════════════════════════════════

@Composable
private fun EmptyWorkoutsCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    IOSGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🏃‍♂️", fontSize = 44.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.workout_empty_title),
                style = mindTypography.headline.copy(fontWeight = FontWeight.Bold),
                color = mindColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.workout_empty_subtitle),
                style = mindTypography.footnote,
                color = mindColors.textSecondary
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(mindColors.primary)
                    .clickable { onAddClick() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.workout_log_now),
                    style = mindTypography.callout.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Fitness Tips Card
// ═══════════════════════════════════════════════════════════════

@Composable
private fun FitnessTipsCard(
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    IOSGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💡", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.fitness_tips_title),
                    style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                    color = mindColors.textPrimary
                )
            }

            Text(
                text = tr("• المشي 30 دقيقة يومياً يقلل خطر أمراض القلب بنسبة 35% ويعزز النشاط الذهني.", "• Walking 30 minutes a day lowers heart-disease risk by 35% and boosts mental energy."),
                style = mindTypography.caption,
                color = mindColors.textSecondary,
                lineHeight = 18.sp
            )
            Text(
                text = tr("• شرب كوب ماء فور الاستيقاظ ينشط الدورة الدموية ويعيد ترطيب الجسم بعد النوم.", "• Drinking a glass of water right after waking boosts circulation and rehydrates you."),
                style = mindTypography.caption,
                color = mindColors.textSecondary,
                lineHeight = 18.sp
            )
            Text(
                text = tr("• الإطالة الخفيفة بعد التمرين تساعد على تسريع التعافي وتقليل الشد العضلي.", "• Light stretching after exercise speeds up recovery and reduces muscle tension."),
                style = mindTypography.caption,
                color = mindColors.textSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
