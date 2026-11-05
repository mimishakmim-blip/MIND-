package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.R

/** Central place that creates every notification channel used by the app. */
object NotificationChannels {
    const val POMODORO = "pomodoro_timer_channel"
    const val POMODORO_DONE = "pomodoro_done_channel"
    const val HABITS = "habit_reminders_channel"
    const val HABITS_QUIET = "habit_reminders_quiet_channel"
    const val SLEEP = "sleep_reminder_channel"
    const val SLEEP_QUIET = "sleep_reminder_quiet_channel"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        fun create(
            id: String,
            nameRes: Int,
            descRes: Int?,
            importance: Int,
            vibrate: Boolean,
            badge: Boolean = true
        ) {
            val channel = NotificationChannel(id, context.getString(nameRes), importance).apply {
                if (descRes != null) description = context.getString(descRes)
                enableVibration(vibrate)
                setShowBadge(badge)
            }
            manager.createNotificationChannel(channel)
        }

        create(
            POMODORO, R.string.notif_channel_pomodoro, R.string.notif_channel_pomodoro_desc,
            NotificationManager.IMPORTANCE_LOW, vibrate = false, badge = false
        )
        create(
            POMODORO_DONE, R.string.notif_channel_pomodoro_done, null,
            NotificationManager.IMPORTANCE_HIGH, vibrate = true
        )
        create(
            HABITS, R.string.notif_channel_habits, R.string.notif_channel_habits_desc,
            NotificationManager.IMPORTANCE_DEFAULT, vibrate = true
        )
        create(
            HABITS_QUIET, R.string.notif_channel_habits, R.string.notif_channel_habits_desc,
            NotificationManager.IMPORTANCE_DEFAULT, vibrate = false
        )
        create(
            SLEEP, R.string.notif_channel_sleep, R.string.notif_channel_sleep_desc,
            NotificationManager.IMPORTANCE_DEFAULT, vibrate = true
        )
        create(
            SLEEP_QUIET, R.string.notif_channel_sleep, R.string.notif_channel_sleep_desc,
            NotificationManager.IMPORTANCE_DEFAULT, vibrate = false
        )
    }
}
