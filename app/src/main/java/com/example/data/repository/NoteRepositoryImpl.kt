package com.example.data.repository

import com.example.data.dao.NoteDao
import com.example.data.entity.Note
import com.example.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAll(): Flow<List<Note>> = noteDao.getAll()

    override suspend fun getById(id: Long): Note? = noteDao.getById(id)

    override suspend fun save(note: Note): Long {
        val now = System.currentTimeMillis()
        val toSave = if (note.id == 0L) {
            note.copy(createdAt = now, updatedAt = now)
        } else {
            note.copy(updatedAt = now)
        }
        return noteDao.upsert(toSave)
    }

    override suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) {
        noteDao.setPinned(id, pinned)
    }

    override fun getCount(): Flow<Int> = noteDao.getCount()
}
