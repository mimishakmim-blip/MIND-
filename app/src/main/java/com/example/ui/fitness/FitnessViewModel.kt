package com.example.ui.fitness

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.DailyActivity
import com.example.data.entity.WorkoutLog
import com.example.data.preferences.UserPreferences
import com.example.di.DatabaseModule
import com.example.domain.repository.FitnessRepository
import com.example.domain.usecase.AchievementChecker
import com.example.domain.util.DateUtils
import com.example.service.StepSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DailyActivityBar(
    val dateMillis: Long,
    val dayInitial: String,
    val dayName: String,
    val steps: Int,
    val stepGoal: Int,
    val isToday: Boolean
)

data class FitnessUiState(
    val currentDate: Long = DateUtils.startOfDay(),
    val dayNameAr: String = DateUtils.dayOfWeekAr(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)),
    val steps: Int = 0,
    val stepGoal: Int = 10000,
    val waterGlasses: Int = 0,
    val waterGoal: Int = 8,
    val activeCalories: Int = 0,
    val calorieGoal: Int = 500,
    val activeMinutes: Int = 0,
    val exerciseGoal: Int = 30,
    val todayWorkouts: List<WorkoutLog> = emptyList(),
    val recentWorkouts: List<WorkoutLog> = emptyList(),
    val weeklyActivities: List<DailyActivityBar> = emptyList(),
    val walkingStreak: Int = 0,
    val isSensorAvailable: Boolean = false,
    val userWeight: Float = 70f
) {
    val stepProgress: Float
        get() = if (stepGoal > 0) (steps.toFloat() / stepGoal).coerceIn(0f, 1.5f) else 0f

    val waterProgress: Float
        get() = if (waterGoal > 0) (waterGlasses.toFloat() / waterGoal).coerceIn(0f, 1.5f) else 0f

    val calorieProgress: Float
        get() = if (calorieGoal > 0) (activeCalories.toFloat() / calorieGoal).coerceIn(0f, 1.5f) else 0f

    val exerciseProgress: Float
        get() = if (exerciseGoal > 0) (activeMinutes.toFloat() / exerciseGoal).coerceIn(0f, 1.5f) else 0f

    val distanceKm: Float
        get() = (steps * 0.00075f)
}

class FitnessViewModel(
    application: Application,
    private val fitnessRepository: FitnessRepository,
    private val achievementChecker: AchievementChecker,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        fitnessRepository = DatabaseModule.provideFitnessRepository(application),
        achievementChecker = DatabaseModule.provideAchievementChecker(application),
        userPreferences = UserPreferences.getInstance(application)
    )

    private val stepSensorManager = StepSensorManager(application)

    val uiState: StateFlow<FitnessUiState> = combine(
        fitnessRepository.getAllWorkouts(),
        fitnessRepository.getAllDailyActivities(),
        userPreferences.settingsFlow,
        userPreferences.userProfileFlow
    ) { allWorkouts, allActivities, settings, profile ->
        val today = DateUtils.startOfDay()
        val weekDays = DateUtils.currentWeekDays(today)

        val todayWorkouts = allWorkouts.filter { it.date == today }
        val recentWorkouts = allWorkouts.take(20)

        val activityMap = allActivities.associateBy { it.date }
        val todayActivity = activityMap[today]

        val activeDates = mutableSetOf<Long>()
        allWorkouts.forEach { activeDates.add(it.date) }
        allActivities.filter { it.steps >= 5000 }.forEach { activeDates.add(it.date) }
        val (streak, _) = DateUtils.calculateStreak(activeDates)

        val weeklyBars = weekDays.map { dayMillis ->
            val act = activityMap[dayMillis]
            DailyActivityBar(
                dateMillis = dayMillis,
                dayInitial = DateUtils.dayInitialAr(dayMillis),
                dayName = DateUtils.dayShortNameAr(dayMillis),
                steps = act?.steps ?: 0,
                stepGoal = settings.dailyGoalSteps,
                isToday = dayMillis == today
            )
        }

        val totalCalories = todayActivity?.activeCalories ?: todayWorkouts.sumOf { it.caloriesBurned }
        val totalMinutes = todayActivity?.activeMinutes ?: todayWorkouts.sumOf { it.durationMinutes }

        FitnessUiState(
            currentDate = today,
            dayNameAr = DateUtils.dayOfWeekAr(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)),
            steps = todayActivity?.steps ?: 0,
            stepGoal = settings.dailyGoalSteps,
            waterGlasses = todayActivity?.waterGlasses ?: 0,
            waterGoal = settings.dailyGoalWater,
            activeCalories = totalCalories,
            calorieGoal = 500,
            activeMinutes = totalMinutes,
            exerciseGoal = 30,
            todayWorkouts = todayWorkouts,
            recentWorkouts = recentWorkouts,
            weeklyActivities = weeklyBars,
            walkingStreak = streak,
            isSensorAvailable = stepSensorManager.isSensorAvailable,
            userWeight = profile.userWeight
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FitnessUiState(
            isSensorAvailable = stepSensorManager.isSensorAvailable
        )
    )

    fun addWater() {
        viewModelScope.launch {
            val today = DateUtils.startOfDay()
            fitnessRepository.incrementWater(today, 1)
            com.example.widget.WaterWidget.refresh(getApplication())
            achievementChecker.checkAll()
        }
    }

    fun removeWater() {
        viewModelScope.launch {
            val today = DateUtils.startOfDay()
            fitnessRepository.incrementWater(today, -1)
        }
    }

    fun addSteps(count: Int) {
        viewModelScope.launch {
            val today = DateUtils.startOfDay()
            fitnessRepository.addSteps(today, count)
            achievementChecker.checkAll()
        }
    }

    fun setSteps(count: Int) {
        viewModelScope.launch {
            val today = DateUtils.startOfDay()
            fitnessRepository.updateDailySteps(today, count)
            achievementChecker.checkAll()
        }
    }

    fun logWorkout(
        type: String,
        durationMinutes: Int,
        calories: Int,
        distanceKm: Float,
        steps: Int,
        intensity: String,
        notes: String
    ) {
        viewModelScope.launch {
            val today = DateUtils.startOfDay()
            val workout = WorkoutLog(
                type = type,
                durationMinutes = durationMinutes,
                caloriesBurned = calories,
                distanceKm = distanceKm,
                steps = steps,
                intensity = intensity,
                timestamp = System.currentTimeMillis(),
                date = today,
                notes = notes
            )
            fitnessRepository.insertWorkout(workout)
            achievementChecker.checkAll()
        }
    }

    fun deleteWorkout(workout: WorkoutLog) {
        viewModelScope.launch {
            fitnessRepository.deleteWorkout(workout)
            achievementChecker.checkAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepSensorManager.stopListening()
    }
}
