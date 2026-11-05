package com.example.ui.sleep

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.SleepLog
import com.example.di.DatabaseModule
import com.example.domain.repository.SleepRepository
import com.example.domain.usecase.AchievementChecker
import com.example.domain.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class SleepDayBar(
    val date: Long,
    val dayInitial: String,
    val dayShortName: String,
    val durationMinutes: Int,
    val hours: Float,
    val quality: Int,
    val isToday: Boolean
)

data class SleepUiState(
    val latestSleep: SleepLog? = null,
    val allLogs: List<SleepLog> = emptyList(),
    val weeklyBars: List<SleepDayBar> = emptyList(),
    val weeklyAvgHours: Float = 0f,
    val weeklyAvgQuality: Float = 0f,
    val totalLogsCount: Int = 0,
    val targetHours: Float = 8.0f,
    val sleepDebtHours: Float = 0f,
    val isLoading: Boolean = false
)

class SleepViewModel(
    application: Application,
    private val sleepRepo: SleepRepository,
    private val achievementChecker: AchievementChecker
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        sleepRepo = DatabaseModule.provideSleepRepository(application),
        achievementChecker = DatabaseModule.provideAchievementChecker(application)
    )

    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<SleepUiState> = combine(
        sleepRepo.getAllLogs(),
        sleepRepo.getLatestLog(),
        _isLoading
    ) { logs, latest, loading ->
        val weekDays = DateUtils.currentWeekDays()
        val today = DateUtils.startOfDay()

        val logsByDate = logs.groupBy { it.date }

        val bars = weekDays.map { dayMillis ->
            val dayLog = logsByDate[dayMillis]?.maxByOrNull { it.wakeTime }
            val duration = dayLog?.durationMinutes ?: 0
            val hours = duration / 60f
            val quality = dayLog?.quality ?: 0
            SleepDayBar(
                date = dayMillis,
                dayInitial = DateUtils.dayInitialAr(dayMillis),
                dayShortName = DateUtils.dayShortNameAr(dayMillis),
                durationMinutes = duration,
                hours = hours,
                quality = quality,
                isToday = dayMillis == today
            )
        }

        val past7Days = logs.filter { it.date >= (today - 7L * 24 * 60 * 60 * 1000) }
        val avgHours = if (past7Days.isNotEmpty()) {
            past7Days.map { it.durationMinutes / 60f }.average().toFloat()
        } else {
            0f
        }

        val avgQuality = if (past7Days.isNotEmpty()) {
            past7Days.map { it.quality }.average().toFloat()
        } else {
            0f
        }

        val target = 8.0f
        val debt = if (past7Days.isNotEmpty()) {
            val totalHours = past7Days.sumOf { it.durationMinutes } / 60f
            val expected = past7Days.size * target
            (expected - totalHours).coerceAtLeast(0f)
        } else {
            0f
        }

        SleepUiState(
            latestSleep = latest,
            allLogs = logs,
            weeklyBars = bars,
            weeklyAvgHours = avgHours,
            weeklyAvgQuality = avgQuality,
            totalLogsCount = logs.size,
            targetHours = target,
            sleepDebtHours = debt,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SleepUiState()
    )

    fun saveSleepLog(
        bedTime: Long,
        wakeTime: Long,
        quality: Int,
        interruptions: Int = 0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val diff = (wakeTime - bedTime).coerceAtLeast(0)
                val durationMinutes = (diff / 60000L).toInt()
                val date = DateUtils.startOfDay(wakeTime)

                // Approximate deep sleep ~ 20% of duration
                val deepSleep = (durationMinutes * 0.22f).toInt()

                val log = SleepLog(
                    bedTime = bedTime,
                    wakeTime = wakeTime,
                    durationMinutes = durationMinutes,
                    quality = quality.coerceIn(1, 5),
                    date = date,
                    deepSleepMinutes = deepSleep,
                    interruptions = interruptions,
                    notes = notes
                )
                sleepRepo.saveLog(log)
                achievementChecker.checkAll()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSleepLog(log: SleepLog) {
        viewModelScope.launch {
            sleepRepo.deleteLog(log)
        }
    }
}
