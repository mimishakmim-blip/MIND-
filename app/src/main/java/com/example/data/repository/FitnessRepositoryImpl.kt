package com.example.data.repository

import com.example.data.dao.FitnessDao
import com.example.data.entity.DailyActivity
import com.example.data.entity.WorkoutLog
import com.example.domain.repository.FitnessRepository
import kotlinx.coroutines.flow.Flow

class FitnessRepositoryImpl(
    private val fitnessDao: FitnessDao
) : FitnessRepository {

    override fun getWorkoutsForDate(date: Long): Flow<List<WorkoutLog>> =
        fitnessDao.getWorkoutsForDate(date)

    override fun getRecentWorkouts(limit: Int): Flow<List<WorkoutLog>> =
        fitnessDao.getRecentWorkouts(limit)

    override fun getAllWorkouts(): Flow<List<WorkoutLog>> =
        fitnessDao.getAllWorkouts()

    override fun getWorkoutCount(): Flow<Int> =
        fitnessDao.getWorkoutCount()

    override suspend fun insertWorkout(workout: WorkoutLog): Long {
        val id = fitnessDao.insertWorkout(workout)
        // Also update daily activity calories and minutes
        val current = fitnessDao.getDailyActivityDirect(workout.date) ?: DailyActivity(date = workout.date)
        fitnessDao.upsertDailyActivity(
            current.copy(
                activeCalories = current.activeCalories + workout.caloriesBurned,
                activeMinutes = current.activeMinutes + workout.durationMinutes,
                steps = current.steps + workout.steps
            )
        )
        return id
    }

    override suspend fun deleteWorkout(workout: WorkoutLog) {
        fitnessDao.deleteWorkout(workout)
        val current = fitnessDao.getDailyActivityDirect(workout.date)
        if (current != null) {
            fitnessDao.upsertDailyActivity(
                current.copy(
                    activeCalories = (current.activeCalories - workout.caloriesBurned).coerceAtLeast(0),
                    activeMinutes = (current.activeMinutes - workout.durationMinutes).coerceAtLeast(0),
                    steps = (current.steps - workout.steps).coerceAtLeast(0)
                )
            )
        }
    }

    override fun getDailyActivity(date: Long): Flow<DailyActivity?> =
        fitnessDao.getDailyActivity(date)

    override suspend fun getDailyActivityDirect(date: Long): DailyActivity? =
        fitnessDao.getDailyActivityDirect(date)

    override suspend fun updateDailySteps(date: Long, steps: Int) {
        val current = fitnessDao.getDailyActivityDirect(date) ?: DailyActivity(date = date)
        fitnessDao.upsertDailyActivity(current.copy(steps = steps.coerceAtLeast(0)))
    }

    override suspend fun addSteps(date: Long, count: Int) {
        val current = fitnessDao.getDailyActivityDirect(date) ?: DailyActivity(date = date)
        fitnessDao.upsertDailyActivity(current.copy(steps = (current.steps + count).coerceAtLeast(0)))
    }

    override suspend fun updateWaterGlasses(date: Long, glasses: Int) {
        val current = fitnessDao.getDailyActivityDirect(date) ?: DailyActivity(date = date)
        fitnessDao.upsertDailyActivity(current.copy(waterGlasses = glasses.coerceAtLeast(0)))
    }

    override suspend fun incrementWater(date: Long, delta: Int) {
        val current = fitnessDao.getDailyActivityDirect(date) ?: DailyActivity(date = date)
        val newCount = (current.waterGlasses + delta).coerceAtLeast(0)
        fitnessDao.upsertDailyActivity(current.copy(waterGlasses = newCount))
    }

    override fun getDailyActivitiesRange(startDate: Long, endDate: Long): Flow<List<DailyActivity>> =
        fitnessDao.getDailyActivitiesRange(startDate, endDate)

    override fun getAllDailyActivities(): Flow<List<DailyActivity>> =
        fitnessDao.getAllDailyActivities()

    override fun getTotalSteps(): Flow<Int?> =
        fitnessDao.getTotalSteps()

    override fun getMaxDailySteps(): Flow<Int?> =
        fitnessDao.getMaxDailySteps()

    override fun getDaysWithTargetWater(): Flow<List<Long>> =
        fitnessDao.getDaysWithTargetWater()

    override fun getActiveDays(): Flow<List<Long>> =
        fitnessDao.getActiveDays()
}
