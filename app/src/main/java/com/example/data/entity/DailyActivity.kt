package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_activities")
data class DailyActivity(
    @PrimaryKey
    val date: Long, // startOfDay millis
    val steps: Int = 0,
    val waterGlasses: Int = 0, // 250ml per glass
    val activeCalories: Int = 0,
    val activeMinutes: Int = 0
)
