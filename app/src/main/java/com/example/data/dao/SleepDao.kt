package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.SleepLog
import kotlinx.coroutines.flow.Flow

data class DailySleepSummary(
    val date: Long,
    val totalMinutes: Int,
    val avgQuality: Float
)

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_logs ORDER BY wakeTime DESC")
    fun getAll(): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs WHERE date = :date LIMIT 1")
    fun getForDate(date: Long): Flow<SleepLog?>

    @Query("SELECT * FROM sleep_logs WHERE date >= :since ORDER BY date ASC")
    fun getSince(since: Long): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs ORDER BY wakeTime DESC LIMIT 1")
    fun getLatest(): Flow<SleepLog?>

    @Query("SELECT AVG(durationMinutes) FROM sleep_logs WHERE date >= :since")
    fun getAverageDurationSince(since: Long): Flow<Float?>

    @Query("SELECT AVG(quality) FROM sleep_logs WHERE date >= :since")
    fun getAverageQualitySince(since: Long): Flow<Float?>

    @Query("SELECT COUNT(*) FROM sleep_logs")
    fun getTotalLogsCount(): Flow<Int>

    @Query("SELECT date, SUM(durationMinutes) as totalMinutes, AVG(quality) as avgQuality FROM sleep_logs WHERE date >= :since GROUP BY date ORDER BY date ASC")
    fun getDailySleepSince(since: Long): Flow<List<DailySleepSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SleepLog): Long

    @Update
    suspend fun update(log: SleepLog)

    @Delete
    suspend fun delete(log: SleepLog)

    @Query("DELETE FROM sleep_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sleep_logs")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<SleepLog>)
}
