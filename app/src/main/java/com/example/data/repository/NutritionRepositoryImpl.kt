package com.example.data.repository

import com.example.data.dao.DailyCalories
import com.example.data.dao.MealLogDao
import com.example.data.dao.NutritionGoalsDao
import com.example.data.entity.MealLog
import com.example.data.entity.NutritionGoals
import com.example.domain.repository.NutritionRepository
import com.example.domain.util.DateUtils
import kotlinx.coroutines.flow.Flow

class NutritionRepositoryImpl(
    private val mealLogDao: MealLogDao,
    private val nutritionGoalsDao: NutritionGoalsDao
) : NutritionRepository {

    override fun getTodayMeals(): Flow<List<MealLog>> =
        mealLogDao.getForDate(DateUtils.startOfDay())

    override fun getMealsForDate(date: Long): Flow<List<MealLog>> =
        mealLogDao.getForDate(date)

    override fun getTodayCalories(): Flow<Int?> =
        mealLogDao.getTotalCaloriesForDate(DateUtils.startOfDay())

    override fun getTodayProtein(): Flow<Float?> =
        mealLogDao.getTotalProteinForDate(DateUtils.startOfDay())

    override fun getTodayCarbs(): Flow<Float?> =
        mealLogDao.getTotalCarbsForDate(DateUtils.startOfDay())

    override fun getTodayFat(): Flow<Float?> =
        mealLogDao.getTotalFatForDate(DateUtils.startOfDay())

    override fun getDailyCaloriesSince(since: Long): Flow<List<DailyCalories>> =
        mealLogDao.getDailyCaloriesSince(since)

    override suspend fun addMeal(meal: MealLog): Long =
        mealLogDao.insert(meal)

    override suspend fun deleteMeal(meal: MealLog) =
        mealLogDao.delete(meal)

    override fun getGoals(): Flow<NutritionGoals?> =
        nutritionGoalsDao.get()

    override suspend fun updateGoals(goals: NutritionGoals) =
        nutritionGoalsDao.upsert(goals)
}
