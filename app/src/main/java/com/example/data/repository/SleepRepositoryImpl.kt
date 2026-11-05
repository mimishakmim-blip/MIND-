package com.example.data.repository

import com.example.data.dao.DailySleepSummary
import com.example.data.dao.SleepDao
import com.example.data.entity.SleepLog
import com.example.domain.repository.SleepRepository
import kotlinx.coroutines.flow.Flow

class SleepRepositoryImpl(
    private val sleepDao: SleepDao
) : SleepRepository {

    override fun getAllLogs(): Flow<List<SleepLog>> = sleepDao.getAll()

    override fun getLogForDate(date: Long): Flow<SleepLog?> = sleepDao.getForDate(date)

    override fun getLogsSince(since: Long): Flow<List<SleepLog>> = sleepDao.getSince(since)

    override fun getLatestLog(): Flow<SleepLog?> = sleepDao.getLatest()

    override fun getAverageDurationSince(since: Long): Flow<Float?> = sleepDao.getAverageDurationSince(since)

    override fun getAverageQualitySince(since: Long): Flow<Float?> = sleepDao.getAverageQualitySince(since)

    override fun getTotalLogsCount(): Flow<Int> = sleepDao.getTotalLogsCount()

    override fun getDailySleepSince(since: Long): Flow<List<DailySleepSummary>> = sleepDao.getDailySleepSince(since)

    override suspend fun saveLog(log: SleepLog): Long = sleepDao.insert(log)

    override suspend fun updateLog(log: SleepLog) = sleepDao.update(log)

    override suspend fun deleteLog(log: SleepLog) = sleepDao.delete(log)

    override suspend fun deleteLogById(id: Long) = sleepDao.deleteById(id)
}
