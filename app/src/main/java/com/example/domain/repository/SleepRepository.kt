package com.example.domain.repository

import com.example.data.dao.DailySleepSummary
import com.example.data.entity.SleepLog
import kotlinx.coroutines.flow.Flow

interface SleepRepository {
    fun getAllLogs(): Flow<List<SleepLog>>
    fun getLogForDate(date: Long): Flow<SleepLog?>
    fun getLogsSince(since: Long): Flow<List<SleepLog>>
    fun getLatestLog(): Flow<SleepLog?>
    fun getAverageDurationSince(since: Long): Flow<Float?>
    fun getAverageQualitySince(since: Long): Flow<Float?>
    fun getTotalLogsCount(): Flow<Int>
    fun getDailySleepSince(since: Long): Flow<List<DailySleepSummary>>
    suspend fun saveLog(log: SleepLog): Long
    suspend fun updateLog(log: SleepLog)
    suspend fun deleteLog(log: SleepLog)
    suspend fun deleteLogById(id: Long)
}
