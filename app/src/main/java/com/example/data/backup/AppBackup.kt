package com.example.data.backup

import com.example.data.entity.Achievement
import com.example.data.entity.Habit
import com.example.data.entity.HabitLog
import com.example.data.entity.JournalEntry
import com.example.data.entity.MealLog
import com.example.data.entity.Note
import com.example.data.entity.NutritionGoals
import com.example.data.entity.PomodoroSession
import com.example.data.entity.SleepLog
import com.example.data.entity.StudySession

data class AppBackup(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val habits: List<Habit> = emptyList(),
    val habitLogs: List<HabitLog> = emptyList(),
    val studySessions: List<StudySession> = emptyList(),
    val pomodoroSessions: List<PomodoroSession> = emptyList(),
    val meals: List<MealLog> = emptyList(),
    val notes: List<Note> = emptyList(),
    val journal: List<JournalEntry> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val sleepLogs: List<SleepLog> = emptyList()
)
