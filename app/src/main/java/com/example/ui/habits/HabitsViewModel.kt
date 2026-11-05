package com.example.ui.habits

import com.example.i18n.tr
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Habit
import com.example.data.entity.HabitLog
import com.example.di.DatabaseModule
import com.example.domain.repository.HabitRepository
import com.example.domain.util.DateUtils
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.Calendar
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitsUiState(
    val groups: List<HabitGroup> = emptyList(),
    val isEmpty: Boolean = true,
    val isLoading: Boolean = true
)

data class HabitGroup(
    val id: String,
    val title: String,
    val habits: List<HabitWithLog>
)

data class HabitWithLog(
    val habit: Habit,
    val todayLog: HabitLog?,
    val yesterdayLog: HabitLog? = null
)

class HabitsViewModel(
    application: Application,
    private val habitRepo: HabitRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        habitRepo = DatabaseModule.provideHabitRepository(application)
    )

    private val dayStart = MutableStateFlow(DateUtils.startOfDay())

    /** Call periodically: rolls the screen over to the new day after midnight. */
    fun refreshDay() {
        val now = DateUtils.startOfDay()
        if (now != dayStart.value) dayStart.value = now
    }

    private fun previousDay(day: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = day
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HabitsUiState> = dayStart.flatMapLatest { day ->
        combine(
            habitRepo.getAllHabits(),
            habitRepo.observeLogsForDate(day),
            habitRepo.observeLogsForDate(previousDay(day))
        ) { habits, logs, yLogs -> Triple(habits, logs, yLogs) }
    }.map { (habits, logs, yLogs) ->
        val logsByHabit = logs.associateBy { it.habitId }
        val yesterdayByHabit = yLogs.associateBy { it.habitId }
        
        val withLogs = habits.map { habit ->
            HabitWithLog(
                habit = habit,
                todayLog = logsByHabit[habit.id],
                yesterdayLog = yesterdayByHabit[habit.id]
            )
        }
        
        val groups = listOf(
            HabitGroup(
                id = "morning",
                title = tr("🌅 الصباح", "🌅 Morning"),
                habits = withLogs.filter { 
                    it.habit.timeHour in 5..11 
                }
            ),
            HabitGroup(
                id = "noon",
                title = tr("☀️ الظهر", "☀️ Noon"),
                habits = withLogs.filter { 
                    it.habit.timeHour in 12..16 
                }
            ),
            HabitGroup(
                id = "evening",
                title = tr("🌆 المساء", "🌆 Evening"),
                habits = withLogs.filter { 
                    it.habit.timeHour in 17..20 
                }
            ),
            HabitGroup(
                id = "night",
                title = tr("🌙 الليل", "🌙 Night"),
                habits = withLogs.filter { 
                    it.habit.timeHour >= 21 || 
                    it.habit.timeHour < 5 
                }
            )
        )
        
        HabitsUiState(
            groups = groups,
            isEmpty = habits.isEmpty(),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsUiState()
    )

    fun addHabit(
        name: String,
        description: String = "",
        iconName: String,
        colorHex: String,
        timeHour: Int,
        timeMinute: Int,
        durationMinutes: Int,
        reminderEnabled: Boolean,
        frequency: String
    ) {
        viewModelScope.launch {
            val habit = Habit(
                name = name,
                description = description,
                iconName = iconName,
                colorHex = colorHex,
                timeHour = timeHour,
                timeMinute = timeMinute,
                durationMinutes = durationMinutes,
                reminderEnabled = reminderEnabled,
                frequency = frequency
            )
            val id = habitRepo.addHabit(habit)
            if (reminderEnabled) {
                ReminderScheduler.scheduleHabit(getApplication(), habit.copy(id = id))
            }
        }
    }

    fun updateHabit(
        habit: Habit,
        name: String,
        description: String,
        iconName: String,
        colorHex: String,
        timeHour: Int,
        timeMinute: Int,
        durationMinutes: Int,
        reminderEnabled: Boolean,
        frequency: String
    ) {
        viewModelScope.launch {
            val updated = habit.copy(
                name = name,
                description = description,
                iconName = iconName,
                colorHex = colorHex,
                timeHour = timeHour,
                timeMinute = timeMinute,
                durationMinutes = durationMinutes,
                reminderEnabled = reminderEnabled,
                frequency = frequency
            )
            habitRepo.updateHabit(updated)
            ReminderScheduler.scheduleHabit(getApplication(), updated)
        }
    }

    fun toggleHabit(habit: Habit, isDone: Boolean) {
        viewModelScope.launch {
            habitRepo.toggleHabit(
                habitId = habit.id,
                date = DateUtils.startOfDay(),
                isDone = isDone
            )
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepo.deleteHabit(habit)
            ReminderScheduler.cancelHabit(getApplication(), habit.id)
        }
    }
}
