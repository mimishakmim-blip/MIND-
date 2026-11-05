package com.example.ui.study

import com.example.i18n.tr
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.PomodoroSession
import com.example.data.entity.StudySession
import com.example.di.DatabaseModule
import com.example.domain.repository.StudyRepository
import com.example.domain.util.DateUtils
import com.example.service.PomodoroService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudyUiState(
    val pomodoroState: PomodoroState = PomodoroState.IDLE,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val sessionsCompleted: Int = 0,
    val autoStartNext: Boolean = false,
    val playingSound: String? = null,
    val volume: Float = 0.5f,
    val todayMinutes: Int = 0,
    val todaySessions: Int = 0,
    val avgFocus: Float = 0f,
    val recentSessions: List<StudySession> = emptyList(),
    val weeklyMinutes: List<Int> = List(7) { 0 },
    val weeklyLabels: List<String> = emptyList(),
    val isLoading: Boolean = false
)

class StudyViewModel(
    application: Application,
    private val studyRepo: StudyRepository,
    private val soundPlayer: AmbientSoundPlayer
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        studyRepo = DatabaseModule.provideStudyRepository(application),
        soundPlayer = AmbientSoundPlayer(application)
    )

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTime: Long = 0

    init {
        observeTodayData()
        observeRecentSessions()
        observeWeeklyData()
    }

    private fun observeTodayData() {
        viewModelScope.launch {
            val today = DateUtils.startOfDay()
            combine(
                studyRepo.getTotalMinutesForDate(today),
                studyRepo.getSessionCountForDate(today),
                studyRepo.getAverageFocusForDate(today),
                studyRepo.getCompletedPomodoroMinutesForDate(today),
                studyRepo.getCompletedPomodoroCountForDate(today)
            ) { studyMin, studyCount, avgFocus, pomoMin, pomoCount ->
                val totalMin = (studyMin ?: 0) + (pomoMin ?: 0)
                val totalSessions = studyCount + pomoCount
                val focus = avgFocus ?: if (totalSessions > 0) 8.0f else 0.0f
                Triple(totalMin, totalSessions, focus)
            }.collect { (minutes, sessions, focus) ->
                _uiState.update {
                    it.copy(
                        todayMinutes = minutes,
                        todaySessions = sessions,
                        avgFocus = focus
                    )
                }
            }
        }
    }

    private fun observeRecentSessions() {
        viewModelScope.launch {
            studyRepo.getAllSessions().collect { sessions ->
                _uiState.update {
                    it.copy(recentSessions = sessions.take(10))
                }
            }
        }
    }

    private fun observeWeeklyData() {
        viewModelScope.launch {
            val weekDays = DateUtils.currentWeekDays()
            val labels = weekDays.map { DateUtils.dayInitialAr(it) }
            val since = weekDays.firstOrNull() ?: DateUtils.startOfDay()

            studyRepo.getDailyMinutesSince(since).collect { dailyList ->
                val dailyMap = dailyList.associate { it.date to it.totalMinutes }
                val minutesList = weekDays.map { dailyMap[it] ?: 0 }
                _uiState.update {
                    it.copy(
                        weeklyMinutes = minutesList,
                        weeklyLabels = labels
                    )
                }
            }
        }
    }

    fun startPomodoro() {
        val state = _uiState.value.pomodoroState
        val duration = when (state) {
            PomodoroState.IDLE -> _uiState.value.focusMinutes * 60
            PomodoroState.PAUSED -> _uiState.value.remainingSeconds
            PomodoroState.FOCUS -> _uiState.value.remainingSeconds
            PomodoroState.SHORT_BREAK, PomodoroState.LONG_BREAK -> _uiState.value.remainingSeconds
        }

        sessionStartTime = System.currentTimeMillis()

        val newState = when (state) {
            PomodoroState.SHORT_BREAK, PomodoroState.LONG_BREAK -> state
            PomodoroState.PAUSED -> pausedFrom ?: PomodoroState.FOCUS
            else -> PomodoroState.FOCUS
        }
        pausedFrom = null

        _uiState.update {
            it.copy(
                pomodoroState = newState,
                remainingSeconds = duration,
                totalSeconds = if (state == PomodoroState.PAUSED) it.totalSeconds else duration
            )
        }

        PomodoroService.start(
            getApplication(),
            duration,
            stateLabel(newState),
            newState == PomodoroState.FOCUS
        )

        startTimer()
    }

    private var pausedFrom: PomodoroState? = null

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // Wall-clock based so the timer never drifts, even if the process is throttled.
            val endAtMillis = System.currentTimeMillis() + _uiState.value.remainingSeconds * 1000L
            while (_uiState.value.remainingSeconds > 0) {
                delay(250L)
                val left = ((endAtMillis - System.currentTimeMillis() + 999L) / 1000L)
                    .toInt().coerceAtLeast(0)
                if (left != _uiState.value.remainingSeconds) {
                    _uiState.update { it.copy(remainingSeconds = left) }
                }
            }
            onPomodoroComplete()
        }
    }

    private suspend fun onPomodoroComplete() {
        val state = _uiState.value
        PomodoroService.notifyPhaseFinished(getApplication(), stateLabel(state.pomodoroState))
        val durationMinutes = (state.totalSeconds / 60).coerceAtLeast(1)

        studyRepo.savePomodoroSession(
            PomodoroSession(
                date = DateUtils.startOfDay(),
                durationMinutes = durationMinutes,
                type = state.pomodoroState.name,
                completed = true
            )
        )

        // Also record a study session for the analytics history if it was a FOCUS session
        if (state.pomodoroState == PomodoroState.FOCUS) {
            studyRepo.saveSession(
                StudySession(
                    startTime = sessionStartTime,
                    endTime = System.currentTimeMillis(),
                    durationMinutes = durationMinutes,
                    focusScore = 8,
                    notes = tr("جلسة بومودورو مكتملة", "Completed Pomodoro session"),
                    date = DateUtils.startOfDay()
                )
            )

            val sessionCount = state.sessionsCompleted + 1
            val nextState = if (sessionCount % state.sessionsBeforeLongBreak == 0) {
                PomodoroState.LONG_BREAK
            } else {
                PomodoroState.SHORT_BREAK
            }
            val breakMinutes = if (nextState == PomodoroState.LONG_BREAK) {
                state.longBreakMinutes
            } else {
                state.shortBreakMinutes
            }

            _uiState.update {
                it.copy(
                    pomodoroState = nextState,
                    remainingSeconds = breakMinutes * 60,
                    totalSeconds = breakMinutes * 60,
                    sessionsCompleted = sessionCount
                )
            }

            PomodoroService.update(
                getApplication(),
                breakMinutes * 60,
                stateLabel(nextState),
                nextState == PomodoroState.FOCUS
            )

            if (state.autoStartNext) {
                startTimer()
            } else {
                PomodoroService.stop(getApplication())
            }
        } else {
            // Completed break -> return to ready IDLE
            _uiState.update {
                it.copy(
                    pomodoroState = PomodoroState.IDLE,
                    remainingSeconds = it.focusMinutes * 60,
                    totalSeconds = it.focusMinutes * 60
                )
            }
            PomodoroService.stop(getApplication())
        }
    }

    fun pausePomodoro() {
        timerJob?.cancel()
        val current = _uiState.value.pomodoroState
        if (current == PomodoroState.FOCUS || current == PomodoroState.SHORT_BREAK ||
            current == PomodoroState.LONG_BREAK
        ) {
            pausedFrom = current
        }
        _uiState.update { it.copy(pomodoroState = PomodoroState.PAUSED) }
        PomodoroService.stop(getApplication())
    }

    fun resetPomodoro() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                pomodoroState = PomodoroState.IDLE,
                remainingSeconds = it.focusMinutes * 60,
                totalSeconds = it.focusMinutes * 60
            )
        }
        PomodoroService.stop(getApplication())
    }

    fun updateSettings(
        focusMin: Int,
        shortBreakMin: Int,
        longBreakMin: Int,
        sessionsCount: Int,
        autoStart: Boolean
    ) {
        _uiState.update {
            it.copy(
                focusMinutes = focusMin,
                shortBreakMinutes = shortBreakMin,
                longBreakMinutes = longBreakMin,
                sessionsBeforeLongBreak = sessionsCount,
                autoStartNext = autoStart,
                remainingSeconds = if (it.pomodoroState == PomodoroState.IDLE) focusMin * 60 else it.remainingSeconds,
                totalSeconds = if (it.pomodoroState == PomodoroState.IDLE) focusMin * 60 else it.totalSeconds
            )
        }
    }

    fun playSound(soundId: String) {
        soundPlayer.play(soundId)
        _uiState.update { it.copy(playingSound = soundId) }
    }

    fun stopSound() {
        soundPlayer.stop()
        _uiState.update { it.copy(playingSound = null) }
    }

    fun toggleSound(soundId: String) {
        if (_uiState.value.playingSound == soundId) {
            stopSound()
        } else {
            playSound(soundId)
        }
    }

    fun setVolume(volume: Float) {
        soundPlayer.setVolume(volume)
        _uiState.update { it.copy(volume = volume) }
    }

    fun saveManualSession(minutes: Int, focusScore: Int, notes: String) {
        viewModelScope.launch {
            studyRepo.saveSession(
                StudySession(
                    startTime = System.currentTimeMillis() - minutes * 60 * 1000L,
                    endTime = System.currentTimeMillis(),
                    durationMinutes = minutes,
                    focusScore = focusScore,
                    notes = notes,
                    date = DateUtils.startOfDay()
                )
            )
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        soundPlayer.stop()
        PomodoroService.stop(getApplication())
        super.onCleared()
    }
}
