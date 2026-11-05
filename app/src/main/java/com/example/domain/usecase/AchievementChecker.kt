package com.example.domain.usecase

import com.example.data.constants.ALL_ACHIEVEMENTS
import com.example.data.dao.AchievementDao
import com.example.data.dao.FitnessDao
import com.example.data.dao.HabitDao
import com.example.data.dao.JournalDao
import com.example.data.dao.MealLogDao
import com.example.data.dao.NoteDao
import com.example.data.dao.SleepDao
import com.example.data.dao.StudySessionDao
import com.example.data.entity.Achievement
import com.example.domain.util.DateUtils
import com.example.ui.events.AchievementEventBus
import kotlinx.coroutines.flow.first

class AchievementChecker(
    private val achievementDao: AchievementDao,
    private val habitDao: HabitDao,
    private val studyDao: StudySessionDao,
    private val mealDao: MealLogDao,
    private val noteDao: NoteDao,
    private val journalDao: JournalDao,
    private val sleepDao: SleepDao? = null,
    private val fitnessDao: FitnessDao? = null
) {

    suspend fun initializeIfNeeded() {
        if (achievementDao.getCount() == 0) {
            achievementDao.insertAll(
                ALL_ACHIEVEMENTS.map {
                    Achievement(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        iconName = it.iconName,
                        category = it.category,
                        target = it.target,
                        currentProgress = 0,
                        unlockedAt = null
                    )
                }
            )
        }
    }

    suspend fun checkAll() {
        checkHabitAchievements()
        checkFitnessAchievements()
        checkStudyAchievements()
        checkNutritionAchievements()
        checkGeneralAchievements()
        checkSleepAchievements()
    }

    private suspend fun checkHabitAchievements() {
        val habitCount = habitDao.getHabitCount().first()
        checkAndUnlock("FIRST_HABIT", habitCount, 1)
        checkAndUnlock("HABITS_5", habitCount, 5)
        checkAndUnlock("HABITS_10", habitCount, 10)
        checkAndUnlock("HABITS_20", habitCount, 20)
    }

    private suspend fun checkFitnessAchievements() {
        val fDao = fitnessDao ?: return
        val workoutCount = fDao.getWorkoutCount().first()
        checkAndUnlock("FIRST_WORKOUT", workoutCount, 1)
        checkAndUnlock("WORKOUT_10", workoutCount, 10)
        checkAndUnlock("WORKOUT_50", workoutCount, 50)
        checkAndUnlock("WORKOUT_100", workoutCount, 100)
        checkAndUnlock("WORKOUT_500", workoutCount, 500)

        val maxSteps = fDao.getMaxDailySteps().first() ?: 0
        checkAndUnlock("STEPS_1K", maxSteps, 1000)
        checkAndUnlock("STEPS_5K", maxSteps, 5000)
        checkAndUnlock("STEPS_10K", maxSteps, 10000)
        checkAndUnlock("STEPS_20K", maxSteps, 20000)
        checkAndUnlock("STEPS_30K", maxSteps, 30000)
        checkAndUnlock("STEPS_50K", maxSteps, 50000)

        val totalSteps = fDao.getTotalSteps().first() ?: 0
        checkAndUnlock("STEP_TOTAL_100K", totalSteps, 100000)
        checkAndUnlock("STEP_TOTAL_500K", totalSteps, 500000)
        checkAndUnlock("STEP_TOTAL_1M", totalSteps, 1000000)
        checkAndUnlock("STEP_TOTAL_5M", totalSteps, 5000000)

        val waterTargetDays = fDao.getDaysWithTargetWater().first()
        if (waterTargetDays.isNotEmpty()) {
            checkAndUnlock("WATER_8", 1, 1)
            val (currentWaterStreak, bestWaterStreak) = DateUtils.calculateStreak(waterTargetDays.toSet())
            val maxWaterStreak = maxOf(currentWaterStreak, bestWaterStreak)
            checkAndUnlock("WATER_WEEK", maxWaterStreak, 7)
            checkAndUnlock("WATER_MONTH", maxWaterStreak, 30)
        }

        val activeDays = fDao.getActiveDays().first()
        if (activeDays.isNotEmpty()) {
            val (currentWalkStreak, bestWalkStreak) = DateUtils.calculateStreak(activeDays.toSet())
            val maxWalkStreak = maxOf(currentWalkStreak, bestWalkStreak)
            checkAndUnlock("WALK_STREAK_7", maxWalkStreak, 7)
            checkAndUnlock("WALK_STREAK_30", maxWalkStreak, 30)
        }
    }

    private suspend fun checkStudyAchievements() {
        val studySessions = studyDao.getAll().first()
        val totalMinutes = studySessions.sumOf { it.durationMinutes }
        checkAndUnlock("FIRST_STUDY", studySessions.size, 1)
        checkAndUnlock("STUDY_10H", totalMinutes, 600)
        checkAndUnlock("STUDY_50H", totalMinutes, 3000)
        checkAndUnlock("STUDY_100H", totalMinutes, 6000)
        checkAndUnlock("STUDY_500H", totalMinutes, 30000)
    }

    private suspend fun checkNutritionAchievements() {
        val todayCalories = mealDao.getTotalCaloriesForDate(DateUtils.startOfDay()).first() ?: 0
        if (todayCalories > 0) {
            checkAndUnlock("FIRST_MEAL", 1, 1)
        }
        val meals = mealDao.getForDate(DateUtils.startOfDay()).first()
        checkAndUnlock("MEALS_10", meals.size, 10)
    }

    private suspend fun checkGeneralAchievements() {
        val notes = noteDao.getAll().first()
        checkAndUnlock("FIRST_NOTE", notes.size, 1)
        checkAndUnlock("NOTES_50", notes.size, 50)

        val journalCount = journalDao.getCount().first()
        checkAndUnlock("FIRST_JOURNAL", journalCount, 1)
        checkAndUnlock("JOURNAL_7", journalCount, 7)
        checkAndUnlock("JOURNAL_30", journalCount, 30)
        checkAndUnlock("JOURNAL_100", journalCount, 100)
    }

    private suspend fun checkSleepAchievements() {
        val sleep = sleepDao ?: return
        val count = sleep.getTotalLogsCount().first()
        if (count >= 1) {
            checkAndUnlock("EARLY_BIRD", 1, 1)
        }
        if (count >= 30) {
            checkAndUnlock("CONSISTENT", count, 30)
        }
    }

    suspend fun checkAndUnlock(
        id: String,
        current: Int,
        target: Int
    ) {
        val achievement = achievementDao.getById(id) ?: return
        if (achievement.unlockedAt != null) return

        val clampedProgress = current.coerceAtMost(target)
        achievementDao.updateProgress(id, clampedProgress)

        if (current >= target) {
            val now = System.currentTimeMillis()
            achievementDao.unlock(id, now)
            val updated = achievementDao.getById(id) ?: return
            AchievementEventBus.emit(updated)
        }
    }
}
