package com.example.domain.repository

import com.example.data.entity.Habit
import com.example.data.entity.HabitLog
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun observeHabitById(id: Long): Flow<Habit?>
    suspend fun getHabitById(id: Long): Habit?
    suspend fun addHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)

    fun observeLogsForDate(date: Long): Flow<List<HabitLog>>
    suspend fun getLog(habitId: Long, date: Long): HabitLog?
    fun observeLog(habitId: Long, date: Long): Flow<HabitLog?>

    fun observeCompletedCountForDate(date: Long): Flow<Int>
    suspend fun getCompletedCountForDate(date: Long): Int
    fun getHabitCount(): Flow<Int>

    fun observeTotalCompletedCount(): Flow<Int>
    fun observeDistinctCompletedDates(): Flow<List<Long>>
    fun observeAllLogs(): Flow<List<HabitLog>>
    fun observeLogsSince(since: Long): Flow<List<HabitLog>>

    suspend fun toggleHabit(habitId: Long, date: Long, isDone: Boolean)
    suspend fun clearAllData()
}
