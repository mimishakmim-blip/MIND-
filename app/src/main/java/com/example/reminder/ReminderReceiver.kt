package com.example.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.preferences.UserPreferences
import com.example.di.DatabaseModule
import com.example.domain.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Fires when a habit / sleep alarm goes off: shows the notification and schedules the next one. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                handle(appContext, intent)
            } catch (_: Exception) {
                // Never crash from a background alarm.
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handle(context: Context, intent: Intent) {
        val settings = UserPreferences.getInstance(context).settingsFlow.first()
        val localized = context.withLanguage(settings.language)
        NotificationChannels.ensure(localized)

        when (intent.action) {
            ReminderScheduler.ACTION_HABIT -> {
                val habitId = intent.getLongExtra(ReminderScheduler.EXTRA_HABIT_ID, -1L)
                if (habitId < 0) return
                val db = DatabaseModule.provideDatabase(context)
                val habit = db.habitDao().getHabitById(habitId) ?: return
                if (!habit.reminderEnabled) return

                // Always queue the next occurrence first.
                ReminderScheduler.scheduleHabit(context, habit)

                if (!settings.notificationsEnabled) return
                val alreadyDone = db.habitLogDao()
                    .getLog(habitId, DateUtils.startOfDay())?.status == "DONE"
                if (alreadyDone) return

                show(
                    context = localized,
                    notificationId = NOTIFICATION_HABIT_BASE + habitId.toInt(),
                    channelId = if (settings.vibrationEnabled) NotificationChannels.HABITS
                    else NotificationChannels.HABITS_QUIET,
                    title = localized.getString(R.string.reminder_habit_title),
                    text = habit.name
                )
            }

            ReminderScheduler.ACTION_SLEEP -> {
                if (!settings.sleepReminderEnabled) return
                ReminderScheduler.scheduleSleep(context, settings.sleepReminderTime)
                if (!settings.notificationsEnabled) return

                show(
                    context = localized,
                    notificationId = NOTIFICATION_SLEEP,
                    channelId = if (settings.vibrationEnabled) NotificationChannels.SLEEP
                    else NotificationChannels.SLEEP_QUIET,
                    title = localized.getString(R.string.reminder_sleep_title),
                    text = localized.getString(R.string.reminder_sleep_text)
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun show(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        text: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_mind)
            .setColor(BRAND_COLOR)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val BRAND_COLOR = 0xFFE1BA40.toInt()
        private const val NOTIFICATION_HABIT_BASE = 3000
        private const val NOTIFICATION_SLEEP = 2002
    }
}
