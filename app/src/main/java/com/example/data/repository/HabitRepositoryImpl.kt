package com.example.data.repository

import com.example.data.dao.HabitDao
import com.example.data.dao.HabitLogDao
import com.example.data.entity.Habit
import com.example.data.entity.HabitLog
import com.example.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()
    
    override fun observeHabitById(id: Long): Flow<Habit?> = 
        habitDao.observeHabitById(id)
    
    override suspend fun getHabitById(id: Long): Habit? = 
        habitDao.getHabitById(id)
    
    override suspend fun addHabit(habit: Habit): Long = 
        habitDao.insertHabit(habit)
    
    override suspend fun updateHabit(habit: Habit) = 
        habitDao.updateHabit(habit)
    
    override suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
        // Logs cascade-delete via foreign key
    }

    override fun observeLogsForDate(date: Long): Flow<List<HabitLog>> = 
        habitLogDao.observeLogsForDate(date)

    override suspend fun getLog(habitId: Long, date: Long): HabitLog? = 
        habitLogDao.getLog(habitId, date)

    override fun observeLog(habitId: Long, date: Long): Flow<HabitLog?> = 
        habitLogDao.observeLog(habitId, date)

    override fun observeCompletedCountForDate(date: Long): Flow<Int> = 
        habitLogDao.observeCompletedCountForDate(date)

    override suspend fun getCompletedCountForDate(date: Long): Int = 
        habitLogDao.getCompletedCountForDate(date)

    override fun getHabitCount(): Flow<Int> = habitDao.getHabitCount()

    override fun observeTotalCompletedCount(): Flow<Int> = 
        habitLogDao.observeTotalCompletedCount()

    override fun observeDistinctCompletedDates(): Flow<List<Long>> = 
        habitLogDao.observeDistinctCompletedDates()

    override fun observeAllLogs(): Flow<List<HabitLog>> = 
        habitLogDao.observeAllLogs()

    override fun observeLogsSince(since: Long): Flow<List<HabitLog>> =
        habitLogDao.observeLogsSince(since)

    override suspend fun clearAllData() {
        habitLogDao.deleteAllLogs()
        habitDao.deleteAllHabits()
    }

    override suspend fun toggleHabit(
        habitId: Long, 
        date: Long, 
        isDone: Boolean
    ) {
        val existing = habitLogDao.getLog(habitId, date)
        val status = if (isDone) "DONE" else "NOT_DONE"

        if (existing == null) {
            habitLogDao.insertLog(
                HabitLog(
                    habitId = habitId,
                    date = date,
                    status = status
                )
            )
        } else {
            habitLogDao.updateLog(
                existing.copy(status = status)
            )
        }

        // Update streak logic
        val habit = habitDao.getHabitById(habitId) ?: return
        val yesterday = date - 24L * 60 * 60 * 1000

        val newStreak = when {
            !isDone -> 0
            habit.lastCheckedDate == yesterday -> habit.streak + 1
            habit.lastCheckedDate == date -> habit.streak
            else -> 1
        }

        val newBest = maxOf(habit.bestStreak, newStreak)

        habitDao.updateHabit(
            habit.copy(
                streak = newStreak,
                bestStreak = newBest,
                lastCheckedDate = if (isDone) date else null
            )
        )
    }
}
