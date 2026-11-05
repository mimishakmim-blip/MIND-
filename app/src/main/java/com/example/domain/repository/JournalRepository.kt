package com.example.domain.repository

import com.example.data.entity.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun getAll(): Flow<List<JournalEntry>>
    fun observeForDate(date: Long): Flow<JournalEntry?>
    fun observeToday(): Flow<JournalEntry?>
    fun getRecent(limit: Int): Flow<List<JournalEntry>>
    suspend fun saveTodayEntry(mood: String, text: String)
    suspend fun deleteEntry(entry: JournalEntry)
    fun search(query: String): Flow<List<JournalEntry>>
    fun getCount(): Flow<Int>
}
