package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    
    val name: String,
    val description: String = "",
    val iconName: String = "star",
    val colorHex: String = "#E8C547",
    
    // Time window
    val timeHour: Int = 8,
    val timeMinute: Int = 0,
    val durationMinutes: Int = 60,
    
    // Reminder
    val reminderEnabled: Boolean = true,
    
    // Frequency (DAILY, WEEKLY, CUSTOM)
    val frequency: String = "DAILY",
    
    // Stats
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastCheckedDate: Long? = null,
    
    // Metadata
    val createdAt: Long = System.currentTimeMillis()
)
