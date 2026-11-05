package com.example.ui.nutrition

import com.example.data.constants.foodDisplayName
import com.example.i18n.tr
import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.constants.COMMON_FOODS
import com.example.data.constants.FoodItem
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSSegmentedControl
import com.example.ui.components.IOSSheet
import com.example.ui.components.IOSTextField
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@Composable
fun AddMealSheet(
    initialMealType: String = tr("فطور", "Breakfast"),
    onDismiss: () -> Unit,
    onSave: (mealType: String, foodName: String, calories: Int, protein: Float, carbs: Float, fat: Float) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val view = LocalView.current

    val mealTypes = listOf(
        stringResource(R.string.nutrition_breakfast_label),
        stringResource(R.string.nutrition_lunch_label),
        stringResource(R.string.nutrition_dinner_label),
        stringResource(R.string.nutrition_snack_label)
    )

    val initialIndex = when (initialMealType.lowercase()) {
        "breakfast", "فطور" -> 0
        "lunch", "غداء" -> 1
        "dinner", "عشاء" -> 2
        "snack", "سناك" -> 3
        else -> 0
    }

    var selectedTypeIndex by remember { mutableIntStateOf(initialIndex) }
    var foodName by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }

    IOSSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.add_meal_title),
                style = mindTypography.title,
                color = mindColors.textPrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            // 1. Meal Type Segmented Control
            IOSSegmentedControl(
                options = mealTypes,
                selectedIndex = selectedTypeIndex,
                onSelect = { selectedTypeIndex = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // 2. Food Name
            IOSTextField(
                value = foodName,
                onValueChange = { foodName = it },
                placeholder = stringResource(R.string.add_meal_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_meal_name_input")
            )

            Spacer(Modifier.height(12.dp))

            // 3. Quick Pick (30 common foods)
            Text(
                text = stringResource(R.string.add_meal_quick),
                style = mindTypography.footnote.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textSecondary
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(COMMON_FOODS) { food ->
                    QuickFoodChip(
                        food = food,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            foodName = foodDisplayName(food.name)
                            caloriesText = food.calories.toString()
                            proteinText = "%.1f".format(food.protein)
                            carbsText = "%.1f".format(food.carbs)
                            fatText = "%.1f".format(food.fat)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. Calories
            IOSTextField(
                value = caloriesText,
                onValueChange = { caloriesText = it.filter { ch -> ch.isDigit() } },
                placeholder = tr("مثال: 250", "e.g. 250"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_meal_calories_input")
            )

            Spacer(Modifier.height(14.dp))

            // 5. Macros (Row of 3)
            Text(
                text = stringResource(R.string.nutrition_macros),
                style = mindTypography.footnote.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textSecondary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.add_meal_protein),
                        style = mindTypography.caption,
                        color = mindColors.textSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    IOSTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it },
                        placeholder = tr("0 غ", "0 g"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.add_meal_carbs),
                        style = mindTypography.caption,
                        color = mindColors.textSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    IOSTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it },
                        placeholder = tr("0 غ", "0 g"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.add_meal_fat),
                        style = mindTypography.caption,
                        color = mindColors.textSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    IOSTextField(
                        value = fatText,
                        onValueChange = { fatText = it },
                        placeholder = tr("0 غ", "0 g"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 6. Action Buttons
            val isSaveEnabled = foodName.isNotBlank() && (caloriesText.toIntOrNull() ?: 0) > 0
            IOSButton(
                text = stringResource(R.string.add_meal_save),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    val rawType = when (selectedTypeIndex) {
                        0 -> "BREAKFAST"
                        1 -> "LUNCH"
                        2 -> "DINNER"
                        3 -> "SNACK"
                        else -> "BREAKFAST"
                    }
                    val cal = caloriesText.toIntOrNull() ?: 0
                    val p = proteinText.toFloatOrNull() ?: 0f
                    val c = carbsText.toFloatOrNull() ?: 0f
                    val f = fatText.toFloatOrNull() ?: 0f

                    onSave(rawType, foodName.trim(), cal, p, c, f)
                    onDismiss()
                },
                enabled = isSaveEnabled,
                style = IOSButtonStyle.Filled,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_meal_save_button")
            )

            Spacer(Modifier.height(8.dp))

            IOSButton(
                text = stringResource(R.string.add_meal_cancel),
                onClick = onDismiss,
                style = IOSButtonStyle.Text,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_meal_cancel_button")
            )
        }
    }
}

@Composable
private fun QuickFoodChip(
    food: FoodItem,
    onClick: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(mindColors.surfaceElevated)
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = foodDisplayName(food.name),
                style = mindTypography.caption.copy(fontWeight = FontWeight.Medium),
                color = mindColors.textPrimary
            )
            Text(
                text = tr("${food.calories} سعرة", "${food.calories} kcal"),
                fontSize = 11.sp,
                color = mindColors.primary
            )
        }
    }
}
