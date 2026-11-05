package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements")
    fun getAll(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getById(id: String): Achievement?

    @Query("SELECT * FROM achievements WHERE id = :id")
    fun observeById(id: String): Flow<Achievement?>

    @Query("SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL")
    fun getUnlockedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(achievements: List<Achievement>)

    @Query("UPDATE achievements SET unlockedAt = :unlockedAt, currentProgress = target WHERE id = :id")
    suspend fun unlock(id: String, unlockedAt: Long)

    @Query("UPDATE achievements SET currentProgress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)
}
