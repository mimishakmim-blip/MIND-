package com.example.data.repository

import com.example.data.dao.DailyMinutes
import com.example.data.dao.PomodoroSessionDao
import com.example.data.dao.StudySessionDao
import com.example.data.entity.PomodoroSession
import com.example.data.entity.StudySession
import com.example.domain.repository.StudyRepository
import kotlinx.coroutines.flow.Flow

class StudyRepositoryImpl(
    private val studySessionDao: StudySessionDao,
    private val pomodoroSessionDao: PomodoroSessionDao
) : StudyRepository {

    override fun getAllSessions(): Flow<List<StudySession>> = studySessionDao.getAll()

    override fun getSessionsForDate(date: Long): Flow<List<StudySession>> =
        studySessionDao.getForDate(date)

    override fun getSessionsSince(since: Long): Flow<List<StudySession>> =
        studySessionDao.getSince(since)

    override fun getTotalMinutesForDate(date: Long): Flow<Int?> =
        studySessionDao.getTotalMinutesForDate(date)

    override fun getDailyMinutesSince(since: Long): Flow<List<DailyMinutes>> =
        studySessionDao.getDailyMinutesSince(since)

    override fun getAverageFocusForDate(date: Long): Flow<Float?> =
        studySessionDao.getAverageFocusForDate(date)

    override fun getSessionCountForDate(date: Long): Flow<Int> =
        studySessionDao.getSessionCountForDate(date)

    override suspend fun saveSession(session: StudySession): Long =
        studySessionDao.insert(session)

    override suspend fun deleteSession(session: StudySession) =
        studySessionDao.delete(session)

    override fun getAllPomodoroSessions(): Flow<List<PomodoroSession>> =
        pomodoroSessionDao.getAll()

    override fun getPomodoroSessionsForDate(date: Long): Flow<List<PomodoroSession>> =
        pomodoroSessionDao.getForDate(date)

    override fun getCompletedPomodoroCountForDate(date: Long): Flow<Int> =
        pomodoroSessionDao.getCompletedCountForDate(date)

    override fun getCompletedPomodoroMinutesForDate(date: Long): Flow<Int?> =
        pomodoroSessionDao.getCompletedMinutesForDate(date)

    override suspend fun savePomodoroSession(session: PomodoroSession): Long =
        pomodoroSessionDao.insert(session)

    override suspend fun deletePomodoroSession(session: PomodoroSession) =
        pomodoroSessionDao.delete(session)
}
