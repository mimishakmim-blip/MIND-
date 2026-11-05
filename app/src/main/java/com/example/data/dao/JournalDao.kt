package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAll(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: Long): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE date = :date LIMIT 1")
    fun observeForDate(date: Long): Flow<JournalEntry?>

    @Query("SELECT * FROM journal_entries ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE text LIKE '%' || :query || '%' ORDER BY date DESC")
    fun search(query: String): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: JournalEntry): Long

    @Delete
    suspend fun delete(entry: JournalEntry)

    @Query("SELECT COUNT(*) FROM journal_entries")
    fun getCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<JournalEntry>)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAll()
}
