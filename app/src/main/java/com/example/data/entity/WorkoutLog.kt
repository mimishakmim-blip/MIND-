package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_logs",
    indices = [Index("date"), Index("timestamp")]
)
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "walking", "running", "cycling", "strength", "swimming", "hiit", "yoga", "sports"
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val distanceKm: Float = 0f,
    val steps: Int = 0,
    val intensity: String = "medium", // "low", "medium", "high"
    val timestamp: Long = System.currentTimeMillis(),
    val date: Long, // startOfDay millis
    val notes: String = ""
)
