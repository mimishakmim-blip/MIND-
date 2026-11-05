package com.example.domain.repository

import com.example.data.entity.DailyActivity
import com.example.data.entity.WorkoutLog
import kotlinx.coroutines.flow.Flow

interface FitnessRepository {
    fun getWorkoutsForDate(date: Long): Flow<List<WorkoutLog>>
    fun getRecentWorkouts(limit: Int = 20): Flow<List<WorkoutLog>>
    fun getAllWorkouts(): Flow<List<WorkoutLog>>
    fun getWorkoutCount(): Flow<Int>
    suspend fun insertWorkout(workout: WorkoutLog): Long
    suspend fun deleteWorkout(workout: WorkoutLog)
    fun getDailyActivity(date: Long): Flow<DailyActivity?>
    suspend fun getDailyActivityDirect(date: Long): DailyActivity?
    suspend fun updateDailySteps(date: Long, steps: Int)
    suspend fun addSteps(date: Long, count: Int)
    suspend fun updateWaterGlasses(date: Long, glasses: Int)
    suspend fun incrementWater(date: Long, delta: Int = 1)
    fun getDailyActivitiesRange(startDate: Long, endDate: Long): Flow<List<DailyActivity>>
    fun getAllDailyActivities(): Flow<List<DailyActivity>>
    fun getTotalSteps(): Flow<Int?>
    fun getMaxDailySteps(): Flow<Int?>
    fun getDaysWithTargetWater(): Flow<List<Long>>
    fun getActiveDays(): Flow<List<Long>>
}
