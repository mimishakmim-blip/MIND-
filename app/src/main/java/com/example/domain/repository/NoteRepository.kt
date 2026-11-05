package com.example.domain.repository

import com.example.data.entity.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAll(): Flow<List<Note>>
    suspend fun getById(id: Long): Note?
    suspend fun save(note: Note): Long
    suspend fun delete(note: Note)
    suspend fun setPinned(id: Long, pinned: Boolean)
    fun getCount(): Flow<Int>
}
