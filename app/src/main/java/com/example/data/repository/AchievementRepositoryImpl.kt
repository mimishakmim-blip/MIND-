package com.example.data.repository

import com.example.data.dao.AchievementDao
import com.example.data.entity.Achievement
import com.example.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow

class AchievementRepositoryImpl(
    private val achievementDao: AchievementDao
) : AchievementRepository {

    override fun getAll(): Flow<List<Achievement>> = achievementDao.getAll()

    override fun getUnlockedCount(): Flow<Int> = achievementDao.getUnlockedCount()

    override suspend fun getById(id: String): Achievement? = achievementDao.getById(id)

    override fun observeById(id: String): Flow<Achievement?> = achievementDao.observeById(id)

    override suspend fun unlock(id: String, unlockedAt: Long) {
        achievementDao.unlock(id, unlockedAt)
    }

    override suspend fun updateProgress(id: String, progress: Int) {
        achievementDao.updateProgress(id, progress)
    }
}
