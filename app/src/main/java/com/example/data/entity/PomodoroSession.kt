package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pomodoro_sessions",
    indices = [Index("date")]
)
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val durationMinutes: Int,
    val type: String,
    val completed: Boolean,
    val startedAt: Long = System.currentTimeMillis()
)
