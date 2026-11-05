package com.example.ui.nutrition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.MealLog
import com.example.data.entity.NutritionGoals
import com.example.data.preferences.PreferencesModule
import com.example.data.preferences.UserPreferences
import com.example.di.DatabaseModule
import com.example.domain.repository.NutritionRepository
import com.example.domain.usecase.NutritionCalculator
import com.example.domain.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NutritionUiState(
    val todayMeals: List<MealLog> = emptyList(),
    val caloriesConsumed: Int = 0,
    val caloriesGoal: Int = 2000,
    val proteinConsumed: Float = 0f,
    val proteinGoal: Int = 100,
    val carbsConsumed: Float = 0f,
    val carbsGoal: Int = 250,
    val fatConsumed: Float = 0f,
    val fatGoal: Int = 70,
    val breakfastMeals: List<MealLog> = emptyList(),
    val lunchMeals: List<MealLog> = emptyList(),
    val dinnerMeals: List<MealLog> = emptyList(),
    val snackMeals: List<MealLog> = emptyList(),
    val waterGlasses: Int = 0,
    val weeklyCalories: List<Int> = List(7) { 0 },
    val weeklyLabels: List<String> = emptyList(),
    val isLoading: Boolean = false
)

class NutritionViewModel(
    application: Application,
    private val nutritionRepo: NutritionRepository,
    private val preferences: UserPreferences
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        nutritionRepo = DatabaseModule.provideNutritionRepository(application),
        preferences = PreferencesModule.provideUserPreferences(application)
    )

    private val _waterGlasses = MutableStateFlow(0)
    private val _weeklyCalories = MutableStateFlow(List(7) { 0 })
    private val _weeklyLabels = MutableStateFlow<List<String>>(emptyList())

    val uiState: StateFlow<NutritionUiState> = combine(
        nutritionRepo.getTodayMeals(),
        nutritionRepo.getGoals(),
        _waterGlasses,
        _weeklyCalories,
        _weeklyLabels
    ) { meals, goals, water, weeklyCal, weeklyLbls ->
        val effectiveGoal = goals ?: NutritionGoals()
        val totalCalories = meals.sumOf { it.calories }
        val totalProtein = meals.fold(0f) { acc, m -> acc + m.protein }
        val totalCarbs = meals.fold(0f) { acc, m -> acc + m.carbs }
        val totalFat = meals.fold(0f) { acc, m -> acc + m.fat }

        NutritionUiState(
            todayMeals = meals,
            caloriesConsumed = totalCalories,
            caloriesGoal = effectiveGoal.dailyCalories,
            proteinConsumed = totalProtein,
            proteinGoal = effectiveGoal.dailyProtein,
            carbsConsumed = totalCarbs,
            carbsGoal = effectiveGoal.dailyCarbs,
            fatConsumed = totalFat,
            fatGoal = effectiveGoal.dailyFat,
            breakfastMeals = meals.filter { it.mealType.equals("BREAKFAST", ignoreCase = true) || it.mealType == "فطور" },
            lunchMeals = meals.filter { it.mealType.equals("LUNCH", ignoreCase = true) || it.mealType == "غداء" },
            dinnerMeals = meals.filter { it.mealType.equals("DINNER", ignoreCase = true) || it.mealType == "عشاء" },
            snackMeals = meals.filter { it.mealType.equals("SNACK", ignoreCase = true) || it.mealType == "سناك" },
            waterGlasses = water,
            weeklyCalories = weeklyCal,
            weeklyLabels = weeklyLbls,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NutritionUiState()
    )

    init {
        initGoalsIfMissing()
        observeWeeklyCalories()
    }

    private fun initGoalsIfMissing() {
        viewModelScope.launch {
            val existing = nutritionRepo.getGoals().first()
            if (existing == null) {
                recalculateGoals()
            }
        }
    }

    private fun observeWeeklyCalories() {
        viewModelScope.launch {
            val weekDays = DateUtils.currentWeekDays()
            val labels = weekDays.map { DateUtils.dayInitialAr(it) }
            val since = weekDays.firstOrNull() ?: DateUtils.startOfDay()

            _weeklyLabels.value = labels
            nutritionRepo.getDailyCaloriesSince(since).collect { dailyList ->
                val dailyMap = dailyList.associate { it.date to it.totalCalories }
                _weeklyCalories.value = weekDays.map { dailyMap[it] ?: 0 }
            }
        }
    }

    fun addMeal(
        mealType: String,
        foodName: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float
    ) {
        viewModelScope.launch {
            nutritionRepo.addMeal(
                MealLog(
                    date = DateUtils.startOfDay(),
                    mealType = mealType,
                    foodName = foodName,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    time = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteMeal(meal: MealLog) {
        viewModelScope.launch {
            nutritionRepo.deleteMeal(meal)
        }
    }

    fun recalculateGoals() {
        viewModelScope.launch {
            val profile = preferences.userProfileFlow.first()
            val bmr = NutritionCalculator.calculateBMR(
                weight = profile.userWeight.takeIf { it > 0 } ?: 70f,
                height = profile.userHeight.takeIf { it > 0 } ?: 170f,
                age = profile.userAge.takeIf { it > 0 } ?: 25,
                gender = profile.userGender.ifBlank { "male" }
            )
            val tdee = NutritionCalculator.calculateTDEE(bmr, "moderate")
            val calories = NutritionCalculator.adjustForGoal(tdee, "maintain")
            val (p, c, f) = NutritionCalculator.calculateMacros(calories, "maintain")

            nutritionRepo.updateGoals(
                NutritionGoals(
                    id = 1,
                    weight = profile.userWeight.takeIf { it > 0 } ?: 70f,
                    height = profile.userHeight.takeIf { it > 0 } ?: 170f,
                    age = profile.userAge.takeIf { it > 0 } ?: 25,
                    gender = profile.userGender.ifBlank { "male" },
                    activityLevel = "moderate",
                    goal = "maintain",
                    dailyCalories = calories,
                    dailyProtein = p,
                    dailyCarbs = c,
                    dailyFat = f
                )
            )
        }
    }

    fun incrementWater() {
        _waterGlasses.update { (it + 1).coerceAtMost(8) }
    }

    fun decrementWater() {
        _waterGlasses.update { (it - 1).coerceAtLeast(0) }
    }
}
