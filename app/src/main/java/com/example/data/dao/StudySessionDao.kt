package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.StudySession
import kotlinx.coroutines.flow.Flow

data class DailyMinutes(
    val date: Long,
    val totalMinutes: Int
)

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAll(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE date = :date")
    fun getForDate(date: Long): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE date >= :since ORDER BY date")
    fun getSince(since: Long): Flow<List<StudySession>>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE date = :date")
    fun getTotalMinutesForDate(date: Long): Flow<Int?>

    @Query("SELECT date, SUM(durationMinutes) as totalMinutes FROM study_sessions WHERE date >= :since GROUP BY date")
    fun getDailyMinutesSince(since: Long): Flow<List<DailyMinutes>>

    @Query("SELECT AVG(focusScore) FROM study_sessions WHERE date = :date")
    fun getAverageFocusForDate(date: Long): Flow<Float?>

    @Query("SELECT COUNT(*) FROM study_sessions WHERE date = :date")
    fun getSessionCountForDate(date: Long): Flow<Int>

    @Insert
    suspend fun insert(session: StudySession): Long

    @Delete
    suspend fun delete(session: StudySession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<StudySession>)

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAll()
}
