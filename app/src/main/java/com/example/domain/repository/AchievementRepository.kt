package com.example.domain.repository

import com.example.data.entity.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAll(): Flow<List<Achievement>>
    fun getUnlockedCount(): Flow<Int>
    suspend fun getById(id: String): Achievement?
    fun observeById(id: String): Flow<Achievement?>
    suspend fun unlock(id: String, unlockedAt: Long)
    suspend fun updateProgress(id: String, progress: Int)
}
