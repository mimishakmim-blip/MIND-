package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.HabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun observeLogsForDate(date: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLog(habitId: Long, date: Long): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    fun observeLog(habitId: Long, date: Long): Flow<HabitLog?>

    @Query("SELECT * FROM habit_logs WHERE date >= :since ORDER BY date ASC")
    fun observeLogsSince(since: Long): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog): Long

    @Update
    suspend fun updateLog(log: HabitLog)

    @Delete
    suspend fun deleteLog(log: HabitLog)

    @Query("SELECT COUNT(*) FROM habit_logs WHERE date = :date AND status = 'DONE'")
    fun observeCompletedCountForDate(date: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE date = :date AND status = 'DONE'")
    suspend fun getCompletedCountForDate(date: Long): Int

    @Query("SELECT COUNT(*) FROM habit_logs WHERE status = 'DONE'")
    fun observeTotalCompletedCount(): Flow<Int>

    @Query("SELECT DISTINCT date FROM habit_logs WHERE status = 'DONE' ORDER BY date DESC")
    fun observeDistinctCompletedDates(): Flow<List<Long>>

    @Query("SELECT * FROM habit_logs ORDER BY date DESC")
    fun observeAllLogs(): Flow<List<HabitLog>>

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<HabitLog>)
}
