package com.example.data.repository

import com.example.data.dao.JournalDao
import com.example.data.entity.JournalEntry
import com.example.domain.repository.JournalRepository
import com.example.domain.util.DateUtils
import kotlinx.coroutines.flow.Flow

class JournalRepositoryImpl(
    private val journalDao: JournalDao
) : JournalRepository {

    override fun getAll(): Flow<List<JournalEntry>> = journalDao.getAll()

    override fun observeForDate(date: Long): Flow<JournalEntry?> = journalDao.observeForDate(date)

    override fun observeToday(): Flow<JournalEntry?> =
        journalDao.observeForDate(DateUtils.startOfDay())

    override fun getRecent(limit: Int): Flow<List<JournalEntry>> = journalDao.getRecent(limit)

    override suspend fun saveTodayEntry(mood: String, text: String) {
        val today = DateUtils.startOfDay()
        val existing = journalDao.getForDate(today)
        val now = System.currentTimeMillis()
        val entry = if (existing != null) {
            existing.copy(
                mood = mood,
                text = text,
                updatedAt = now
            )
        } else {
            JournalEntry(
                date = today,
                mood = mood,
                text = text,
                createdAt = now,
                updatedAt = now
            )
        }
        journalDao.upsert(entry)
    }

    override suspend fun deleteEntry(entry: JournalEntry) {
        journalDao.delete(entry)
    }

    override fun search(query: String): Flow<List<JournalEntry>> = journalDao.search(query)

    override fun getCount(): Flow<Int> = journalDao.getCount()
}
