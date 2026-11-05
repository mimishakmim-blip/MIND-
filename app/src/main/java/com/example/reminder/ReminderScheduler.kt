package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.entity.Habit
import com.example.data.preferences.UserPreferences
import com.example.di.DatabaseModule
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Schedules the daily habit reminders and the sleep reminder with [AlarmManager].
 * Uses inexact, Doze-friendly alarms so no special "exact alarm" permission is required.
 * Every alarm re-schedules itself when it fires (see [ReminderReceiver]) and everything
 * is restored after reboot / app update / time change (see [BootReceiver]).
 */
object ReminderScheduler {
    const val ACTION_HABIT = "com.mind.app.action.HABIT_REMINDER"
    const val ACTION_SLEEP = "com.mind.app.action.SLEEP_REMINDER"
    const val EXTRA_HABIT_ID = "extra_habit_id"

    private const val HABIT_REQUEST_BASE = 100_000
    private const val SLEEP_REQUEST_CODE = 900_001

    fun scheduleHabit(context: Context, habit: Habit) {
        if (!habit.reminderEnabled) {
            cancelHabit(context, habit.id)
            return
        }
        val trigger = nextTrigger(
            hour = habit.timeHour,
            minute = habit.timeMinute,
            weekly = habit.frequency == "WEEKLY",
            anchorMillis = habit.createdAt
        )
        set(context, habitIntent(context, habit.id), trigger)
    }

    fun cancelHabit(context: Context, habitId: Long) {
        cancel(context, habitIntent(context, habitId))
    }

    fun scheduleSleep(context: Context, time: String) {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 23
        val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        set(context, sleepIntent(context), nextTrigger(hour, minute))
    }

    fun cancelSleep(context: Context) {
        cancel(context, sleepIntent(context))
    }

    /** Re-creates every alarm from the database + settings. Safe to call at any time. */
    suspend fun rescheduleAll(context: Context) {
        val app = context.applicationContext
        val habits = DatabaseModule.provideDatabase(app).habitDao().getAllHabits().first()
        habits.forEach { scheduleHabit(app, it) }

        val settings = UserPreferences.getInstance(app).settingsFlow.first()
        if (settings.sleepReminderEnabled) {
            scheduleSleep(app, settings.sleepReminderTime)
        } else {
            cancelSleep(app)
        }
    }

    internal fun nextTrigger(
        hour: Int,
        minute: Int,
        weekly: Boolean = false,
        anchorMillis: Long = 0L,
        now: Long = System.currentTimeMillis()
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (weekly) {
            val anchorDay = Calendar.getInstance().apply { timeInMillis = anchorMillis }
                .get(Calendar.DAY_OF_WEEK)
            while (cal.get(Calendar.DAY_OF_WEEK) != anchorDay || cal.timeInMillis <= now) {
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
        } else if (cal.timeInMillis <= now) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.timeInMillis
    }

    private fun habitIntent(context: Context, habitId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_HABIT
            putExtra(EXTRA_HABIT_ID, habitId)
        }
        return PendingIntent.getBroadcast(
            context,
            HABIT_REQUEST_BASE + habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun sleepIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_SLEEP
        }
        return PendingIntent.getBroadcast(
            context,
            SLEEP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun set(context: Context, pendingIntent: PendingIntent, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancel(context: Context, pendingIntent: PendingIntent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
