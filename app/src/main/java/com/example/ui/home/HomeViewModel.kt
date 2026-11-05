package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.PreferencesModule
import com.example.data.preferences.UserPreferences
import com.example.di.DatabaseModule
import com.example.domain.repository.HabitRepository
import com.example.domain.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val userName: String = "",
    val totalHabits: Int = 0,
    val completedToday: Int = 0,
    val dailyProgress: Float = 0f,
    val currentStreak: Int = 0,
    val todayQuote: Quote = Quote("", ""),
    val todaySteps: Int = 0,
    val todayWater: Int = 0,
    val isLoading: Boolean = true
)

class HomeViewModel(
    application: Application,
    private val habitRepo: HabitRepository,
    private val preferences: UserPreferences
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        habitRepo = DatabaseModule.provideHabitRepository(application),
        preferences = PreferencesModule.provideUserPreferences(application)
    )

    val uiState: StateFlow<HomeUiState> = combine(
        habitRepo.getAllHabits(),
        habitRepo.observeCompletedCountForDate(DateUtils.startOfDay()),
        habitRepo.observeDistinctCompletedDates(),
        preferences.userProfileFlow
    ) { habits, completed, distinctDates, profile ->
        val total = habits.size
        val progress = if (total > 0) 
            completed.toFloat() / total.toFloat() 
            else 0f
        
        val (currentStreak, _) = DateUtils.calculateStreak(distinctDates.toSet())
        
        HomeUiState(
            userName = profile.userName,
            totalHabits = total,
            completedToday = completed,
            dailyProgress = progress,
            currentStreak = currentStreak,
            todayQuote = getTodayQuote(),
            todaySteps = 0,
            todayWater = 0,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}
