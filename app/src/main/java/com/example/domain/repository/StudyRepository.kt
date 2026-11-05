package com.example.domain.repository

import com.example.data.dao.DailyMinutes
import com.example.data.entity.PomodoroSession
import com.example.data.entity.StudySession
import kotlinx.coroutines.flow.Flow

interface StudyRepository {
    fun getAllSessions(): Flow<List<StudySession>>
    fun getSessionsForDate(date: Long): Flow<List<StudySession>>
    fun getSessionsSince(since: Long): Flow<List<StudySession>>
    fun getTotalMinutesForDate(date: Long): Flow<Int?>
    fun getDailyMinutesSince(since: Long): Flow<List<DailyMinutes>>
    fun getAverageFocusForDate(date: Long): Flow<Float?>
    fun getSessionCountForDate(date: Long): Flow<Int>
    suspend fun saveSession(session: StudySession): Long
    suspend fun deleteSession(session: StudySession)

    fun getAllPomodoroSessions(): Flow<List<PomodoroSession>>
    fun getPomodoroSessionsForDate(date: Long): Flow<List<PomodoroSession>>
    fun getCompletedPomodoroCountForDate(date: Long): Flow<Int>
    fun getCompletedPomodoroMinutesForDate(date: Long): Flow<Int?>
    suspend fun savePomodoroSession(session: PomodoroSession): Long
    suspend fun deletePomodoroSession(session: PomodoroSession)
}
