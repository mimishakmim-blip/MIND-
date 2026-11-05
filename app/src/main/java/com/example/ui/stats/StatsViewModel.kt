package com.example.ui.stats

import com.example.i18n.tr
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Habit
import com.example.data.entity.HabitLog
import com.example.data.entity.SleepLog
import com.example.data.entity.StudySession
import com.example.di.DatabaseModule
import com.example.domain.repository.HabitRepository
import com.example.domain.repository.SleepRepository
import com.example.domain.repository.StudyRepository
import com.example.domain.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.TODAY,
    val showYesterday: Boolean = false,
    val todayCompleted: Int = 0,
    val todayTotal: Int = 0,
    val yesterdayCompleted: Int = 0,
    val yesterdayTotal: Int = 0,
    val todayChartData: List<ChartPoint> = emptyList(),
    val yesterdayChartData: List<ChartPoint> = emptyList(),
    val chartEmpty: Boolean = true,
    val totalHabitsCompleted: Int = 0,
    val totalSteps: Int = 0,
    val totalWater: Int = 0,
    val totalStudyMinutes: Int = 0,
    val totalSleepMinutes: Int = 0,
    val avgHabitPercent: Int = 0,
    val habitsLine: List<Float> = emptyList(),
    val chartLabels: List<String> = emptyList(),
    val habitsLineEmpty: Boolean = true,
    val heatmapDays: List<HeatmapDay> = emptyList(),
    val isLoading: Boolean = false
)

class StatsViewModel(
    application: Application,
    private val habitRepo: HabitRepository,
    private val studyRepo: StudyRepository,
    private val sleepRepo: SleepRepository,
    private val context: Context
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        habitRepo = DatabaseModule.provideHabitRepository(application),
        studyRepo = DatabaseModule.provideStudyRepository(application),
        sleepRepo = DatabaseModule.provideSleepRepository(application),
        context = application
    )

    private val _period = MutableStateFlow(StatsPeriod.TODAY)
    private val _showYesterday = MutableStateFlow(false)

    private val ninetyDaysAgo = DateUtils.startOfDay() - 90L * 24 * 60 * 60 * 1000

    val uiState: StateFlow<StatsUiState> = combine(
        combine(_period, _showYesterday) { p, y -> p to y },
        habitRepo.getAllHabits(),
        habitRepo.observeLogsSince(ninetyDaysAgo),
        studyRepo.getSessionsSince(ninetyDaysAgo),
        sleepRepo.getLogsSince(ninetyDaysAgo)
    ) { (period, showYest), habits, habitLogs, studySessions, sleepLogs ->
        buildState(
            period = period,
            showYesterday = showYest,
            habits = habits,
            habitLogs = habitLogs,
            studySessions = studySessions,
            sleepLogs = sleepLogs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    fun setPeriod(p: StatsPeriod) {
        _period.value = p
    }

    fun toggleYesterday() {
        _showYesterday.value = !_showYesterday.value
    }

    fun shareProgress() {
        viewModelScope.launch {
            val state = uiState.value
            val bitmap = generateProgressImage(state)
            shareImage(bitmap)
        }
    }

    private fun buildState(
        period: StatsPeriod,
        showYesterday: Boolean,
        habits: List<Habit>,
        habitLogs: List<HabitLog>,
        studySessions: List<StudySession>,
        sleepLogs: List<SleepLog>
    ): StatsUiState {
        val today = DateUtils.startOfDay()
        val yesterday = today - 24L * 60 * 60 * 1000

        // Today's and yesterday's logs
        val todayLogs = habitLogs.filter { it.date == today }
        val yesterdayLogs = habitLogs.filter { it.date == yesterday }
        val todayCompleted = todayLogs.count { it.status == "DONE" }
        val yesterdayCompleted = yesterdayLogs.count { it.status == "DONE" }

        // Chart data
        val chartData = when (period) {
            StatsPeriod.TODAY -> buildTodayChartData(habits, todayLogs)
            StatsPeriod.WEEK -> buildWeekChartData(habitLogs, habits, today)
            StatsPeriod.MONTH -> buildMonthChartData(habitLogs, habits, today)
        }

        val yesterdayChartData = when (period) {
            StatsPeriod.TODAY -> buildTodayChartData(habits, yesterdayLogs)
            StatsPeriod.WEEK -> buildWeekChartData(habitLogs, habits, yesterday)
            StatsPeriod.MONTH -> buildMonthChartData(habitLogs, habits, yesterday)
        }

        // Aggregate totals for period
        val periodLogs = filterHabitLogsByPeriod(habitLogs, today, period)
        val periodStudy = filterStudySessionsByPeriod(studySessions, today, period)
        val periodSleep = filterSleepLogsByPeriod(sleepLogs, today, period)

        val habitsLineData = buildHabitsLineData(habitLogs, habits, today, period)
        val chartLabels = buildChartLabels(today, period)

        return StatsUiState(
            period = period,
            showYesterday = showYesterday,
            todayCompleted = todayCompleted,
            todayTotal = habits.size,
            yesterdayCompleted = yesterdayCompleted,
            yesterdayTotal = habits.size,
            todayChartData = chartData,
            yesterdayChartData = yesterdayChartData,
            chartEmpty = habits.isEmpty() || chartData.isEmpty(),
            totalHabitsCompleted = periodLogs.count { it.status == "DONE" },
            totalSteps = 0,
            totalWater = 0,
            totalStudyMinutes = periodStudy.sumOf { it.durationMinutes },
            totalSleepMinutes = periodSleep.sumOf { it.durationMinutes },
            avgHabitPercent = calculateAvgPercent(periodLogs, habits, period),
            habitsLine = habitsLineData,
            chartLabels = chartLabels,
            habitsLineEmpty = habitsLineData.isEmpty() || habits.isEmpty(),
            heatmapDays = buildHeatmap(habitLogs),
            isLoading = false
        )
    }

    private fun buildTodayChartData(
        habits: List<Habit>,
        logs: List<HabitLog>
    ): List<ChartPoint> {
        if (habits.isEmpty()) return emptyList()

        val sorted = habits.sortedBy { it.timeHour * 60 + it.timeMinute }
        val points = mutableListOf<ChartPoint>()
        var runningLevel = 1f // Middle level

        sorted.forEachIndexed { index, habit ->
            val log = logs.find { it.habitId == habit.id }
            val isDone = log?.status == "DONE"

            runningLevel = if (isDone) {
                (runningLevel + 1f).coerceAtMost(2f)
            } else {
                (runningLevel - 1f).coerceAtLeast(0f)
            }

            points.add(
                ChartPoint(
                    x = index.toFloat(),
                    y = runningLevel,
                    label = String.format("%02d:%02d", habit.timeHour, habit.timeMinute)
                )
            )
        }

        return points
    }

    private fun buildWeekChartData(
        logs: List<HabitLog>,
        habits: List<Habit>,
        referenceDay: Long
    ): List<ChartPoint> {
        return (0..6).map { offset ->
            val day = referenceDay - (6 - offset) * 24L * 60 * 60 * 1000
            val dayLogs = logs.filter { it.date == day }
            val done = dayLogs.count { it.status == "DONE" }
            val maxCount = habits.size.coerceAtLeast(1)
            val level = ((done.toFloat() / maxCount) * 2f).coerceIn(0f, 2f)
            ChartPoint(
                x = offset.toFloat(),
                y = level,
                label = dayLabel(day)
            )
        }
    }

    private fun buildMonthChartData(
        logs: List<HabitLog>,
        habits: List<Habit>,
        referenceDay: Long
    ): List<ChartPoint> {
        return (0..29).map { offset ->
            val day = referenceDay - (29 - offset) * 24L * 60 * 60 * 1000
            val dayLogs = logs.filter { it.date == day }
            val done = dayLogs.count { it.status == "DONE" }
            val maxCount = habits.size.coerceAtLeast(1)
            val level = ((done.toFloat() / maxCount) * 2f).coerceIn(0f, 2f)
            ChartPoint(
                x = offset.toFloat(),
                y = level,
                label = dayShort(day)
            )
        }
    }

    private fun buildHeatmap(
        habitLogs: List<HabitLog>
    ): List<HeatmapDay> {
        val today = DateUtils.startOfDay()
        return (0..83).map { i ->
            val day = today - (83 - i) * 24L * 60 * 60 * 1000
            val habitCount = habitLogs.count { it.date == day && it.status == "DONE" }
            val total = habitCount

            HeatmapDay(
                date = day,
                intensity = (total / 5f).coerceIn(0f, 1f),
                activityCount = total
            )
        }
    }

    private fun filterHabitLogsByPeriod(
        logs: List<HabitLog>,
        today: Long,
        period: StatsPeriod
    ): List<HabitLog> {
        val since = when (period) {
            StatsPeriod.TODAY -> today
            StatsPeriod.WEEK -> today - 6L * 24 * 60 * 60 * 1000
            StatsPeriod.MONTH -> today - 29L * 24 * 60 * 60 * 1000
        }
        return logs.filter { it.date >= since }
    }

    private fun filterStudySessionsByPeriod(
        sessions: List<StudySession>,
        today: Long,
        period: StatsPeriod
    ): List<StudySession> {
        val since = when (period) {
            StatsPeriod.TODAY -> today
            StatsPeriod.WEEK -> today - 6L * 24 * 60 * 60 * 1000
            StatsPeriod.MONTH -> today - 29L * 24 * 60 * 60 * 1000
        }
        return sessions.filter { it.date >= since }
    }

    private fun filterSleepLogsByPeriod(
        logs: List<SleepLog>,
        today: Long,
        period: StatsPeriod
    ): List<SleepLog> {
        val since = when (period) {
            StatsPeriod.TODAY -> today
            StatsPeriod.WEEK -> today - 6L * 24 * 60 * 60 * 1000
            StatsPeriod.MONTH -> today - 29L * 24 * 60 * 60 * 1000
        }
        return logs.filter { it.date >= since }
    }

    private fun calculateAvgPercent(
        logs: List<HabitLog>,
        habits: List<Habit>,
        period: StatsPeriod
    ): Int {
        if (habits.isEmpty()) return 0
        val done = logs.count { it.status == "DONE" }
        val days = when (period) {
            StatsPeriod.TODAY -> 1
            StatsPeriod.WEEK -> 7
            StatsPeriod.MONTH -> 30
        }
        val totalPossible = habits.size * days
        return if (totalPossible > 0) (done * 100) / totalPossible else 0
    }

    private fun buildHabitsLineData(
        logs: List<HabitLog>,
        habits: List<Habit>,
        today: Long,
        period: StatsPeriod
    ): List<Float> {
        if (habits.isEmpty()) return emptyList()

        val days = when (period) {
            StatsPeriod.WEEK -> 7
            StatsPeriod.MONTH -> 30
            else -> 0
        }

        if (days == 0) return emptyList()

        return (0 until days).map { offset ->
            val day = today - (days - 1 - offset) * 24L * 60 * 60 * 1000
            val dayLogs = logs.filter { it.date == day }
            val done = dayLogs.count { it.status == "DONE" }
            (done.toFloat() / habits.size) * 100f
        }
    }

    private fun buildChartLabels(
        today: Long,
        period: StatsPeriod
    ): List<String> {
        return when (period) {
            StatsPeriod.WEEK -> (0..6).map {
                dayLabel(today - (6 - it) * 24L * 60 * 60 * 1000)
            }
            StatsPeriod.MONTH -> (0..29 step 5).map {
                dayShort(today - (29 - it) * 24L * 60 * 60 * 1000)
            }
            else -> emptyList()
        }
    }

    private fun dayLabel(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> tr("السبت", "Saturday")
            Calendar.SUNDAY -> tr("الأحد", "Sunday")
            Calendar.MONDAY -> tr("الاثنين", "Monday")
            Calendar.TUESDAY -> tr("الثلاثاء", "Tuesday")
            Calendar.WEDNESDAY -> tr("الأربعاء", "Wednesday")
            Calendar.THURSDAY -> tr("الخميس", "Thursday")
            Calendar.FRIDAY -> tr("الجمعة", "Friday")
            else -> ""
        }
    }

    private fun dayShort(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return "${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun generateProgressImage(state: StatsUiState): Bitmap {
        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(android.graphics.Color.parseColor("#0D0D0F"))

        // Header
        val paint = Paint().apply {
            color = android.graphics.Color.parseColor("#E8C547")
            textSize = 80f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("MIND", 60f, 200f, paint)

        // Tagline
        paint.apply {
            color = android.graphics.Color.parseColor("#A0A0A8")
            textSize = 40f
            typeface = Typeface.DEFAULT
        }
        canvas.drawText(tr("عقلك، وقتك، حياتك", "Your mind, your time, your life"), 60f, 260f, paint)

        // Stats
        paint.apply {
            color = android.graphics.Color.parseColor("#F5F5F7")
            textSize = 50f
        }

        val stats = listOf(
            tr("عادات: ${state.totalHabitsCompleted}", "Habits: ${state.totalHabitsCompleted}"),
            tr("خطوات: ${state.totalSteps}", "Steps: ${state.totalSteps}"),
            tr("ماء: ${state.totalWater}", "Water: ${state.totalWater}"),
            tr("دراسة: ${state.totalStudyMinutes} د", "Study: ${state.totalStudyMinutes} m"),
            tr("نوم: ${state.totalSleepMinutes / 60} س", "Sleep: ${state.totalSleepMinutes / 60} h"),
            tr("متوسط العادات: ${state.avgHabitPercent}%", "Average habits: ${state.avgHabitPercent}%")
        )

        var y = 400f
        stats.forEach {
            canvas.drawText(it, 60f, y, paint)
            y += 100f
        }

        // Footer
        paint.apply {
            color = android.graphics.Color.parseColor("#6E6E78")
            textSize = 35f
        }
        canvas.drawText(tr("تم الإنشاء بواسطة MIND", "Created with MIND"), 60f, 1850f, paint)

        return bitmap
    }

    private fun shareImage(bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "progress.png")
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(
                Intent.createChooser(intent, tr("شارك تقدمك", "Share your progress")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
