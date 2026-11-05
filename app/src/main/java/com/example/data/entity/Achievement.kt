package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val category: String,
    val target: Int = 1,
    val currentProgress: Int = 0,
    val unlockedAt: Long? = null
)
