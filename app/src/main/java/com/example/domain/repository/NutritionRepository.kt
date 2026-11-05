package com.example.domain.repository

import com.example.data.dao.DailyCalories
import com.example.data.entity.MealLog
import com.example.data.entity.NutritionGoals
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun getTodayMeals(): Flow<List<MealLog>>
    fun getMealsForDate(date: Long): Flow<List<MealLog>>
    fun getTodayCalories(): Flow<Int?>
    fun getTodayProtein(): Flow<Float?>
    fun getTodayCarbs(): Flow<Float?>
    fun getTodayFat(): Flow<Float?>
    fun getDailyCaloriesSince(since: Long): Flow<List<DailyCalories>>
    suspend fun addMeal(meal: MealLog): Long
    suspend fun deleteMeal(meal: MealLog)

    fun getGoals(): Flow<NutritionGoals?>
    suspend fun updateGoals(goals: NutritionGoals)
}
