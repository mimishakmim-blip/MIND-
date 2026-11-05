package com.example.ui.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.JournalEntry
import com.example.di.DatabaseModule
import com.example.domain.repository.JournalRepository
import com.example.domain.usecase.AchievementChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JournalUiState(
    val todayEntry: JournalEntry? = null,
    val recentEntries: List<JournalEntry> = emptyList(),
    val isLoading: Boolean = false
)

class JournalViewModel(
    application: Application,
    private val journalRepo: JournalRepository,
    private val checker: AchievementChecker
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        DatabaseModule.provideJournalRepository(application),
        DatabaseModule.provideAchievementChecker(application)
    )

    val uiState: StateFlow<JournalUiState> = combine(
        journalRepo.observeToday(),
        journalRepo.getRecent(30)
    ) { today, recent ->
        JournalUiState(
            todayEntry = today,
            recentEntries = recent.filter { it.id != today?.id }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        JournalUiState()
    )

    fun saveTodayEntry(mood: String, text: String) {
        viewModelScope.launch {
            journalRepo.saveTodayEntry(mood, text)
            checker.checkAll()
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            journalRepo.deleteEntry(entry)
            checker.checkAll()
        }
    }
}
