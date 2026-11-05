package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.data.entity.PomodoroSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC")
    fun getAll(): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE date = :date")
    fun getForDate(date: Long): Flow<List<PomodoroSession>>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE date = :date AND completed = 1")
    fun getCompletedCountForDate(date: Long): Flow<Int>

    @Query("SELECT SUM(durationMinutes) FROM pomodoro_sessions WHERE date = :date AND completed = 1")
    fun getCompletedMinutesForDate(date: Long): Flow<Int?>

    @Insert
    suspend fun insert(session: PomodoroSession): Long

    @Delete
    suspend fun delete(session: PomodoroSession)
}
