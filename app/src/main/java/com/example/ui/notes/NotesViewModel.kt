package com.example.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Note
import com.example.di.DatabaseModule
import com.example.domain.repository.NoteRepository
import com.example.domain.usecase.AchievementChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val editingNote: Note? = null
)

class NotesViewModel(
    application: Application,
    private val noteRepo: NoteRepository,
    private val checker: AchievementChecker
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        DatabaseModule.provideNoteRepository(application),
        DatabaseModule.provideAchievementChecker(application)
    )

    private val _editingNote = MutableStateFlow<Note?>(null)

    val uiState: StateFlow<NotesUiState> = combine(
        noteRepo.getAll(),
        _editingNote
    ) { notes, editing ->
        NotesUiState(
            notes = notes,
            editingNote = editing
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NotesUiState()
    )

    fun edit(note: Note) {
        _editingNote.value = note
    }

    fun cancelEdit() {
        _editingNote.value = null
    }

    fun save(title: String, content: String, color: String, pinned: Boolean) {
        viewModelScope.launch {
            val current = _editingNote.value
            val toSave = current?.copy(
                title = title,
                content = content,
                color = color,
                pinned = pinned,
                updatedAt = System.currentTimeMillis()
            ) ?: Note(
                title = title,
                content = content,
                color = color,
                pinned = pinned,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            noteRepo.save(toSave)
            _editingNote.value = null
            checker.checkAll()
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch {
            noteRepo.delete(note)
            if (_editingNote.value?.id == note.id) {
                _editingNote.value = null
            }
            checker.checkAll()
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteRepo.setPinned(note.id, !note.pinned)
        }
    }
}
