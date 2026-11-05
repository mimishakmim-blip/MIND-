package com.example.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.PreferencesModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuestionnaireUiState(
    val language: String = "ar",
    val goal: String = "",
    val dailyMinutes: Int = 30,
    val distraction: String = "",
    val wakeHourIndex: Int = 7, // 07:00
    val wakeMinuteIndex: Int = 0, // 00
    val userName: String = ""
)

class QuestionnaireViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PreferencesModule.provideUserPreferences(application)

    private val _uiState = MutableStateFlow(QuestionnaireUiState())
    val uiState: StateFlow<QuestionnaireUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val current = preferences.getUserProfile()
            val hour = current.userWakeTime.split(":").getOrNull(0)?.toIntOrNull() ?: 7
            val minuteStr = current.userWakeTime.split(":").getOrNull(1) ?: "00"
            val minuteIdx = when (minuteStr) {
                "15" -> 1
                "30" -> 2
                "45" -> 3
                else -> 0
            }
            _uiState.value = _uiState.value.copy(
                language = current.language,
                goal = current.userGoal,
                dailyMinutes = current.userDailyMinutes,
                distraction = current.userDistraction,
                wakeHourIndex = hour.coerceIn(0, 23),
                wakeMinuteIndex = minuteIdx,
                userName = current.userName
            )
        }
    }

    fun setLanguage(lang: String) {
        _uiState.value = _uiState.value.copy(language = lang)
        viewModelScope.launch {
            preferences.setLanguage(lang)
        }
    }

    fun setGoal(goal: String) {
        _uiState.value = _uiState.value.copy(goal = goal)
        viewModelScope.launch {
            preferences.setUserGoal(goal)
        }
    }

    fun setDailyMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(dailyMinutes = minutes)
        viewModelScope.launch {
            preferences.setUserDailyMinutes(minutes)
        }
    }

    fun setDistraction(distraction: String) {
        _uiState.value = _uiState.value.copy(distraction = distraction)
        viewModelScope.launch {
            preferences.setUserDistraction(distraction)
        }
    }

    fun setWakeTime(hourIndex: Int, minuteIndex: Int) {
        _uiState.value = _uiState.value.copy(wakeHourIndex = hourIndex, wakeMinuteIndex = minuteIndex)
        val hourStr = String.format("%02d", hourIndex)
        val minStr = when (minuteIndex) {
            1 -> "15"
            2 -> "30"
            3 -> "45"
            else -> "00"
        }
        val wakeTimeStr = "$hourStr:$minStr"
        viewModelScope.launch {
            preferences.setUserWakeTime(wakeTimeStr)
        }
    }

    fun setUserName(name: String) {
        _uiState.value = _uiState.value.copy(userName = name)
        viewModelScope.launch {
            preferences.setUserName(name)
        }
    }

    fun saveAll(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val hourStr = String.format("%02d", state.wakeHourIndex)
            val minStr = when (state.wakeMinuteIndex) {
                1 -> "15"
                2 -> "30"
                3 -> "45"
                else -> "00"
            }
            val wakeTimeStr = "$hourStr:$minStr"

            preferences.setLanguage(state.language)
            preferences.setUserGoal(state.goal)
            preferences.setUserDailyMinutes(state.dailyMinutes)
            preferences.setUserDistraction(state.distraction)
            preferences.setUserWakeTime(wakeTimeStr)
            preferences.setUserName(state.userName.trim())
            onSuccess()
        }
    }
}
