package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.dao.AchievementDao
import com.example.data.dao.HabitDao
import com.example.data.dao.HabitLogDao
import com.example.data.dao.JournalDao
import com.example.data.dao.MealLogDao
import com.example.data.dao.NoteDao
import com.example.data.dao.NutritionGoalsDao
import com.example.data.dao.PomodoroSessionDao
import com.example.data.dao.StudySessionDao
import com.example.data.local.MindDatabase
import com.example.data.dao.FitnessDao
import com.example.data.dao.SleepDao
import com.example.data.repository.AchievementRepositoryImpl
import com.example.data.repository.FitnessRepositoryImpl
import com.example.data.repository.HabitRepositoryImpl
import com.example.data.repository.JournalRepositoryImpl
import com.example.data.repository.NoteRepositoryImpl
import com.example.data.repository.NutritionRepositoryImpl
import com.example.data.repository.SleepRepositoryImpl
import com.example.data.repository.StudyRepositoryImpl
import com.example.domain.repository.AchievementRepository
import com.example.domain.repository.FitnessRepository
import com.example.domain.repository.HabitRepository
import com.example.domain.repository.JournalRepository
import com.example.domain.repository.NoteRepository
import com.example.domain.repository.NutritionRepository
import com.example.domain.repository.SleepRepository
import com.example.domain.repository.StudyRepository
import com.example.domain.usecase.AchievementChecker

object DatabaseModule {
    @Volatile
    private var databaseInstance: MindDatabase? = null

    @Volatile
    private var habitRepositoryInstance: HabitRepository? = null

    @Volatile
    private var studyRepositoryInstance: StudyRepository? = null

    @Volatile
    private var nutritionRepositoryInstance: NutritionRepository? = null

    @Volatile
    private var achievementRepositoryInstance: AchievementRepository? = null

    @Volatile
    private var journalRepositoryInstance: JournalRepository? = null

    @Volatile
    private var noteRepositoryInstance: NoteRepository? = null

    @Volatile
    private var sleepRepositoryInstance: SleepRepository? = null

    @Volatile
    private var fitnessRepositoryInstance: FitnessRepository? = null

    @Volatile
    private var achievementCheckerInstance: AchievementChecker? = null

    fun provideDatabase(context: Context): MindDatabase {
        return databaseInstance ?: synchronized(this) {
            databaseInstance ?: Room.databaseBuilder(
                context.applicationContext,
                MindDatabase::class.java,
                "mind_database"
            )
            .addMigrations(*com.example.data.local.Migrations.ALL)
            // Only pre-release development schemas (1..6) may be wiped. From version 7 on,
            // every schema change MUST ship a Migration so user data is never lost.
            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6)
            .build()
            .also { databaseInstance = it }
        }
    }

    fun provideHabitDao(db: MindDatabase): HabitDao = db.habitDao()
    fun provideHabitLogDao(db: MindDatabase): HabitLogDao = db.habitLogDao()
    fun provideStudySessionDao(db: MindDatabase): StudySessionDao = db.studySessionDao()
    fun providePomodoroSessionDao(db: MindDatabase): PomodoroSessionDao = db.pomodoroSessionDao()
    fun provideMealLogDao(db: MindDatabase): MealLogDao = db.mealLogDao()
    fun provideNutritionGoalsDao(db: MindDatabase): NutritionGoalsDao = db.nutritionGoalsDao()
    fun provideAchievementDao(db: MindDatabase): AchievementDao = db.achievementDao()
    fun provideJournalDao(db: MindDatabase): JournalDao = db.journalDao()
    fun provideNoteDao(db: MindDatabase): NoteDao = db.noteDao()
    fun provideSleepDao(db: MindDatabase): SleepDao = db.sleepDao()
    fun provideFitnessDao(db: MindDatabase): FitnessDao = db.fitnessDao()

    fun provideHabitRepository(context: Context): HabitRepository {
        return habitRepositoryInstance ?: synchronized(this) {
            val db = provideDatabase(context)
            habitRepositoryInstance ?: HabitRepositoryImpl(
                db.habitDao(),
                db.habitLogDao()
            ).also { habitRepositoryInstance = it }
        }
    }

    fun provideStudyRepository(context: Context): StudyRepository {
        return studyRepositoryInstance ?: synchronized(this) {
            val db = provideDatabase(context)
            studyRepositoryInstance ?: StudyRepositoryImpl(
                db.studySessionDao(),
                db.pomodoroSessionDao()
            ).also { studyRepositoryInstance = it }
        }
    }

    fun provideNutritionRepository(context: Context): NutritionRepository {
        return nutritionRepositoryInstance ?: synchronized(this) {
            val db = provideDatabase(context)
            nutritionRepositoryInstance ?: NutritionRepositoryImpl(
                db.mealLogDao(),
                db.nutritionGoalsDao()
            ).also { nutritionRepositoryInstance = it }
        }
    }

    fun provideAchievementRepository(context: Context): AchievementRepository {
        return achievementRepositoryInstance ?: synchronized(this) {
            val db = provideDatabase(context)
            achievementRepositoryInstance ?: AchievementRepositoryImpl(
                db.achievementDao()
            ).also { achievementRepositoryInstance = it }
        }
    }

    fun provideJournalRepository(context: Context): JournalRepository {
        return journalRepositoryInstance ?: synchronized(this) {
            val db = provideDatabase(context)
            journalRepositoryInstance ?: JournalRepositoryImpl(
                db.journalDao()
            ).also { journalRepositoryInstance = it }
        }
    }

    fun provideNoteRepository(context: Context): NoteRepository {
        return noteRepositoryInstance ?: synchronized(this) {
            val db = provideDatabase(context)
            noteRepositoryInstance ?: NoteRepositoryImpl(
                db.noteDao()
            ).also { noteRepositoryInstance = it }
        }
    }

    fun provideSleepRepository(context: Context): SleepRepository {
        return sleepRepositoryInstance ?: synchronized(this) {
            val db = provideDatabase(context)
            sleepRepositoryInstance ?: SleepRepositoryImpl(
                db.sleepDao()
            ).also { sleepRepositoryInstance = it }
        }
    }

    fun provideFitnessRepository(context: Context): FitnessRepository {
        return fitnessRepositoryInstance ?: synchronized(this) {
            val db = provideDatabase(context)
            fitnessRepositoryInstance ?: FitnessRepositoryImpl(
                db.fitnessDao()
            ).also { fitnessRepositoryInstance = it }
        }
    }

    fun provideAchievementChecker(context: Context): AchievementChecker {
        return achievementCheckerInstance ?: synchronized(this) {
            val db = provideDatabase(context)
            achievementCheckerInstance ?: AchievementChecker(
                db.achievementDao(),
                db.habitDao(),
                db.studySessionDao(),
                db.mealLogDao(),
                db.noteDao(),
                db.journalDao(),
                db.sleepDao(),
                db.fitnessDao()
            ).also { achievementCheckerInstance = it }
        }
    }

    fun provideBackupManager(context: Context): com.example.data.backup.BackupManager {
        val db = provideDatabase(context)
        return com.example.data.backup.BackupManager(db)
    }
}
