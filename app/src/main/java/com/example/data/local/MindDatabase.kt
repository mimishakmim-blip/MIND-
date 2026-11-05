package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.AchievementDao
import com.example.data.dao.HabitDao
import com.example.data.dao.HabitLogDao
import com.example.data.dao.JournalDao
import com.example.data.dao.MealLogDao
import com.example.data.dao.NoteDao
import com.example.data.dao.NutritionGoalsDao
import com.example.data.dao.PomodoroSessionDao
import com.example.data.dao.StudySessionDao
import com.example.data.entity.Achievement
import com.example.data.entity.Habit
import com.example.data.entity.HabitLog
import com.example.data.entity.JournalEntry
import com.example.data.entity.MealLog
import com.example.data.dao.FitnessDao
import com.example.data.dao.SleepDao
import com.example.data.entity.DailyActivity
import com.example.data.entity.Note
import com.example.data.entity.NutritionGoals
import com.example.data.entity.PomodoroSession
import com.example.data.entity.SleepLog
import com.example.data.entity.StudySession
import com.example.data.entity.WorkoutLog

@Database(
    entities = [
        Habit::class,
        HabitLog::class,
        StudySession::class,
        PomodoroSession::class,
        MealLog::class,
        NutritionGoals::class,
        Achievement::class,
        JournalEntry::class,
        Note::class,
        SleepLog::class,
        WorkoutLog::class,
        DailyActivity::class
    ],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MindDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun nutritionGoalsDao(): NutritionGoalsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun journalDao(): JournalDao
    abstract fun noteDao(): NoteDao
    abstract fun sleepDao(): SleepDao
    abstract fun fitnessDao(): FitnessDao
}
