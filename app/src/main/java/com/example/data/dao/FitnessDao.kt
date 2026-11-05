package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.DailyActivity
import com.example.data.entity.WorkoutLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    // ── Workouts ──
    @Query("SELECT * FROM workout_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getWorkoutsForDate(date: Long): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentWorkouts(limit: Int = 20): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutLog>>

    @Query("SELECT COUNT(*) FROM workout_logs")
    fun getWorkoutCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWorkouts(workouts: List<WorkoutLog>)

    @Update
    suspend fun updateWorkout(workout: WorkoutLog)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutLog)

    @Query("SELECT SUM(durationMinutes) FROM workout_logs WHERE date = :date")
    fun getTotalWorkoutMinutesForDate(date: Long): Flow<Int?>

    @Query("SELECT SUM(caloriesBurned) FROM workout_logs WHERE date = :date")
    fun getTotalWorkoutCaloriesForDate(date: Long): Flow<Int?>

    // ── Daily Activities (Steps & Water) ──
    @Query("SELECT * FROM daily_activities WHERE date = :date LIMIT 1")
    fun getDailyActivity(date: Long): Flow<DailyActivity?>

    @Query("SELECT * FROM daily_activities WHERE date = :date LIMIT 1")
    suspend fun getDailyActivityDirect(date: Long): DailyActivity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyActivity(activity: DailyActivity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDailyActivities(activities: List<DailyActivity>)

    @Query("SELECT * FROM daily_activities WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getDailyActivitiesRange(startDate: Long, endDate: Long): Flow<List<DailyActivity>>

    @Query("SELECT * FROM daily_activities ORDER BY date DESC")
    fun getAllDailyActivities(): Flow<List<DailyActivity>>

    @Query("DELETE FROM workout_logs")
    suspend fun deleteAllWorkouts()

    @Query("DELETE FROM daily_activities")
    suspend fun deleteAllDailyActivities()

    @Query("SELECT SUM(steps) FROM daily_activities")
    fun getTotalSteps(): Flow<Int?>

    @Query("SELECT MAX(steps) FROM daily_activities")
    fun getMaxDailySteps(): Flow<Int?>

    @Query("SELECT date FROM daily_activities WHERE waterGlasses >= 8 ORDER BY date ASC")
    fun getDaysWithTargetWater(): Flow<List<Long>>

    @Query("SELECT DISTINCT date FROM workout_logs UNION SELECT date FROM daily_activities WHERE steps >= 5000")
    fun getActiveDays(): Flow<List<Long>>
}
