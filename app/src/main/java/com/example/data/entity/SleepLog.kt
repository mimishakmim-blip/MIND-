package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_logs",
    indices = [Index("date"), Index("wakeTime")]
)
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bedTime: Long,
    val wakeTime: Long,
    val durationMinutes: Int,
    val quality: Int = 3, // 1: Poor, 2: Fair, 3: Good, 4: Very Good, 5: Excellent
    val date: Long, // Start of day epoch millis for grouping
    val deepSleepMinutes: Int = 0,
    val interruptions: Int = 0,
    val notes: String = ""
)
