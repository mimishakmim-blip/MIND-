package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_goals")
data class NutritionGoals(
    @PrimaryKey val id: Int = 1,
    val weight: Float = 70f,
    val height: Float = 170f,
    val age: Int = 25,
    val gender: String = "male",
    val activityLevel: String = "moderate",
    val goal: String = "maintain",
    val dailyCalories: Int = 2000,
    val dailyProtein: Int = 100,
    val dailyCarbs: Int = 250,
    val dailyFat: Int = 70
)
