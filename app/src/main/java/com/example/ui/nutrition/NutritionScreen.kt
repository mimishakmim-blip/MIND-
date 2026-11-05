package com.example.ui.nutrition

import com.example.i18n.tr
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.entity.MealLog
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSGlassContainer
import com.example.ui.components.IOSLargeTitle
import com.example.ui.components.IOSProgressRing
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@Composable
fun NutritionScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val view = LocalView.current

    var showAddMealSheet by remember { mutableStateOf(false) }
    var selectedMealTypeForAdd by remember { mutableStateOf("BREAKFAST") }

    if (showAddMealSheet) {
        AddMealSheet(
            initialMealType = selectedMealTypeForAdd,
            onDismiss = { showAddMealSheet = false },
            onSave = { type, name, cal, p, c, f ->
                viewModel.addMeal(type, name, cal, p, c, f)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mindColors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
            .testTag("nutrition_screen")
    ) {
        // 1. Top Bar
        com.example.ui.components.ScreenHeader(
            title = stringResource(R.string.nutrition_title),
            onBack = onBack
        )

        Spacer(Modifier.height(8.dp))

        // 2. Calories Summary Card
        CaloriesSummaryCard(
            consumed = state.caloriesConsumed,
            goal = state.caloriesGoal,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 3. Macros Card
        MacrosCard(
            proteinConsumed = state.proteinConsumed,
            proteinGoal = state.proteinGoal,
            carbsConsumed = state.carbsConsumed,
            carbsGoal = state.carbsGoal,
            fatConsumed = state.fatConsumed,
            fatGoal = state.fatGoal,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 4. Meal Sections (4 Expandable Cards)
        MealSectionCard(
            title = stringResource(R.string.nutrition_breakfast),
            meals = state.breakfastMeals,
            onAddClick = {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                selectedMealTypeForAdd = "BREAKFAST"
                showAddMealSheet = true
            },
            onDeleteMeal = { viewModel.deleteMeal(it) },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        MealSectionCard(
            title = stringResource(R.string.nutrition_lunch),
            meals = state.lunchMeals,
            onAddClick = {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                selectedMealTypeForAdd = "LUNCH"
                showAddMealSheet = true
            },
            onDeleteMeal = { viewModel.deleteMeal(it) },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        MealSectionCard(
            title = stringResource(R.string.nutrition_dinner),
            meals = state.dinnerMeals,
            onAddClick = {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                selectedMealTypeForAdd = "DINNER"
                showAddMealSheet = true
            },
            onDeleteMeal = { viewModel.deleteMeal(it) },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        MealSectionCard(
            title = stringResource(R.string.nutrition_snack),
            meals = state.snackMeals,
            onAddClick = {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                selectedMealTypeForAdd = "SNACK"
                showAddMealSheet = true
            },
            onDeleteMeal = { viewModel.deleteMeal(it) },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 5. Water Tracker Card
        WaterTrackerCard(
            cups = state.waterGlasses,
            onIncrement = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.incrementWater()
            },
            onDecrement = {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                viewModel.decrementWater()
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 6. Weekly Calories Chart
        WeeklyCaloriesChartCard(
            weeklyCalories = state.weeklyCalories,
            weeklyLabels = state.weeklyLabels,
            targetCalories = state.caloriesGoal,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 7. Settings Card (BMR/TDEE Goals)
        NutritionSettingsCard(
            dailyCalories = state.caloriesGoal,
            protein = state.proteinGoal,
            carbs = state.carbsGoal,
            fat = state.fatGoal,
            onRecalculate = {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                viewModel.recalculateGoals()
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun CaloriesSummaryCard(
    consumed: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val progress = if (goal > 0) consumed.toFloat() / goal else 0f
    val remaining = (goal - consumed).coerceAtLeast(0)

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
            Box(
                modifier = Modifier.size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                IOSProgressRing(
                    progress = progress,
                    size = 180.dp,
                    strokeWidth = 12.dp,
                    color = mindColors.primary,
                    trackColor = mindColors.surfaceElevated
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$consumed",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = mindColors.textPrimary
                    )
                    Text(
                        text = "/ $goal",
                        style = mindTypography.subhead,
                        color = mindColors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.nutrition_calories_today),
                style = mindTypography.headline,
                color = mindColors.textPrimary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.nutrition_remaining, remaining),
                style = mindTypography.subhead,
                color = mindColors.textSecondary
            )
        }
    }
}

@Composable
private fun MacrosCard(
    proteinConsumed: Float,
    proteinGoal: Int,
    carbsConsumed: Float,
    carbsGoal: Int,
    fatConsumed: Float,
    fatGoal: Int,
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
            Text(
                text = stringResource(R.string.nutrition_macros),
                style = mindTypography.headline,
                color = mindColors.textPrimary
            )

            Spacer(Modifier.height(14.dp))

            MacroBar(
                title = stringResource(R.string.nutrition_protein),
                consumed = proteinConsumed,
                goal = proteinGoal,
                color = Color(0xFFEF5350)
            )

            Spacer(Modifier.height(10.dp))

            MacroBar(
                title = stringResource(R.string.nutrition_carbs),
                consumed = carbsConsumed,
                goal = carbsGoal,
                color = Color(0xFFFFA726)
            )

            Spacer(Modifier.height(10.dp))

            MacroBar(
                title = stringResource(R.string.nutrition_fat),
                consumed = fatConsumed,
                goal = fatGoal,
                color = Color(0xFF66BB6A)
            )
        }
    }
}

@Composable
private fun MacroBar(
    title: String,
    consumed: Float,
    goal: Int,
    color: Color
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val progress = if (goal > 0) (consumed / goal).coerceIn(0f, 1f) else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = mindTypography.subhead, color = mindColors.textPrimary)
            Text(
                text = tr("${consumed.toInt()} / $goal غ", "${consumed.toInt()} / $goal g"),
                style = mindTypography.caption.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textSecondary
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = mindColors.surfaceElevated,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun MealSectionCard(
    title: String,
    meals: List<MealLog>,
    onAddClick: () -> Unit,
    onDeleteMeal: (MealLog) -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    var isExpanded by remember { mutableStateOf(meals.isNotEmpty()) }
    val totalCal = meals.sumOf { it.calories }

    IOSGlassContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = mindTypography.headline,
                        color = mindColors.textPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = tr("$totalCal سعرة", "$totalCal kcal"),
                        style = mindTypography.caption,
                        color = if (totalCal > 0) mindColors.primary else mindColors.textTertiary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(mindColors.primary.copy(alpha = 0.15f))
                            .clickable { onAddClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = tr("إضافة وجبة", "Add meal"),
                            tint = mindColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = mindColors.textSecondary
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (meals.isEmpty()) {
                        Text(
                            text = stringResource(R.string.nutrition_empty_meal),
                            style = mindTypography.caption,
                            color = mindColors.textTertiary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        meals.forEach { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "• ${meal.foodName}",
                                        style = mindTypography.subhead,
                                        color = mindColors.textPrimary
                                    )
                                    if (meal.protein > 0 || meal.carbs > 0 || meal.fat > 0) {
                                        Text(
                                            text = tr("بروتين: ${meal.protein.toInt()}غ | كارب: ${meal.carbs.toInt()}غ | دهون: ${meal.fat.toInt()}غ", "Protein: ${meal.protein.toInt()}g | Carbs: ${meal.carbs.toInt()}g | Fat: ${meal.fat.toInt()}g"),
                                            fontSize = 11.sp,
                                            color = mindColors.textTertiary
                                        )
                                    }
                                }
                                Text(
                                    text = tr("${meal.calories} سعرة", "${meal.calories} kcal"),
                                    style = mindTypography.caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = mindColors.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onDeleteMeal(meal) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = tr("حذف", "Delete"),
                                        tint = mindColors.textTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterTrackerCard(
    cups: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💧 " + stringResource(R.string.nutrition_water),
                    style = mindTypography.headline,
                    color = mindColors.textPrimary
                )
                Text(
                    text = stringResource(R.string.nutrition_water_count, cups),
                    style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                    color = mindColors.primary
                )
            }

            Spacer(Modifier.height(14.dp))

            // 8 cups icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..8).forEach { cupIndex ->
                    val isFilled = cupIndex <= cups
                    Text(
                        text = if (isFilled) "🥤" else "⚪",
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(mindColors.surfaceElevated)
                        .clickable(enabled = cups > 0) { onDecrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = tr("إنقاص كوب", "Remove a glass"),
                        tint = if (cups > 0) mindColors.textPrimary else mindColors.textTertiary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(mindColors.primary)
                        .clickable(enabled = cups < 8) { onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = tr("زيادة كوب", "Add a glass"),
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyCaloriesChartCard(
    weeklyCalories: List<Int>,
    weeklyLabels: List<String>,
    targetCalories: Int,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val maxCal = (weeklyCalories.maxOrNull() ?: 0).coerceAtLeast(targetCalories).coerceAtLeast(1000)

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
                text = "📈 " + stringResource(R.string.nutrition_weekly),
                style = mindTypography.headline,
                color = mindColors.textPrimary
            )

            Spacer(Modifier.height(16.dp))

            if (weeklyCalories.all { it == 0 }) {
                Text(
                    text = tr("لا توجد سجلات سعرات بعد لهذا الأسبوع", "No calorie records yet this week"),
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
                    weeklyCalories.forEachIndexed { index, cal ->
                        val label = weeklyLabels.getOrNull(index) ?: ""
                        val ratio = cal.toFloat() / maxCal

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (cal > 0) {
                                Text(
                                    text = "$cal",
                                    fontSize = 9.sp,
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
                                        if (cal > 0) mindColors.primary
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

@Composable
private fun NutritionSettingsCard(
    dailyCalories: Int,
    protein: Int,
    carbs: Int,
    fat: Int,
    onRecalculate: () -> Unit,
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
            Text(
                text = "⚙️ " + stringResource(R.string.nutrition_goals),
                style = mindTypography.headline,
                color = mindColors.textPrimary
            )

            Spacer(Modifier.height(12.dp))

            SettingItemRow(stringResource(R.string.nutrition_daily_calories), tr("$dailyCalories سعرة", "$dailyCalories kcal"))
            Spacer(Modifier.height(6.dp))
            SettingItemRow(stringResource(R.string.nutrition_protein), tr("$protein غ", "$protein g"))
            Spacer(Modifier.height(6.dp))
            SettingItemRow(stringResource(R.string.nutrition_carbs), tr("$carbs غ", "$carbs g"))
            Spacer(Modifier.height(6.dp))
            SettingItemRow(stringResource(R.string.nutrition_fat), tr("$fat غ", "$fat g"))

            Spacer(Modifier.height(16.dp))

            IOSButton(
                text = stringResource(R.string.nutrition_recalculate),
                onClick = onRecalculate,
                style = IOSButtonStyle.Glass,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recalculate_nutrition_goals_button")
            )
        }
    }
}

@Composable
private fun SettingItemRow(label: String, value: String) {
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

@Preview(name = "Nutrition Screen Dark", showBackground = true)
@Composable
private fun NutritionScreenPreview() {
    MaterialTheme {
        NutritionScreen()
    }
}
