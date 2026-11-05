package com.example.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.reminder.NotificationChannels
import com.example.reminder.ReminderReceiver
import com.example.reminder.withLanguage

/**
 * Foreground service that keeps the Pomodoro timer alive (and visible in the notification
 * shade with a native count-down) while the app is in the background.
 */
class PomodoroService : Service() {

    companion object {
        const val CHANNEL_ID = NotificationChannels.POMODORO
        const val NOTIFICATION_ID = 2001
        private const val DONE_NOTIFICATION_ID = 2003

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_UPDATE = "com.example.service.ACTION_UPDATE"

        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"
        const val EXTRA_STATE_LABEL = "extra_state_label"
        const val EXTRA_FOCUS = "extra_focus"

        fun start(context: Context, remainingSeconds: Int, stateLabel: String, focus: Boolean = false) {
            val intent = timerIntent(context, ACTION_START, remainingSeconds, stateLabel).putExtra(EXTRA_FOCUS, focus)
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Exception) {
                // Background-start restrictions: the in-app timer keeps working regardless.
            }
        }

        fun update(context: Context, remainingSeconds: Int, stateLabel: String, focus: Boolean = false) {
            val intent = timerIntent(context, ACTION_UPDATE, remainingSeconds, stateLabel).putExtra(EXTRA_FOCUS, focus)
            try {
                context.startService(intent)
            } catch (_: Exception) {
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {
            }
        }

        /** Heads-up alert (sound + vibration) when a focus / break phase finishes. */
        @SuppressLint("MissingPermission")
        fun notifyPhaseFinished(context: Context, finishedLabel: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return

            val localized = localizedContext(context)
            NotificationChannels.ensure(localized)

            val notification = NotificationCompat.Builder(localized, NotificationChannels.POMODORO_DONE)
                .setSmallIcon(R.drawable.ic_stat_mind)
                .setColor(ReminderReceiver.BRAND_COLOR)
                .setContentTitle(localized.getString(R.string.pomodoro_done_title))
                .setContentText(localized.getString(R.string.pomodoro_done_text, finishedLabel))
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openAppIntent(localized))
                .build()
            try {
                NotificationManagerCompat.from(localized).notify(DONE_NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {
            }
        }

        private fun timerIntent(
            context: Context,
            action: String,
            remainingSeconds: Int,
            stateLabel: String
        ) = Intent(context, PomodoroService::class.java).apply {
            this.action = action
            putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
            putExtra(EXTRA_STATE_LABEL, stateLabel)
        }

        private fun localizedContext(context: Context): Context {
            val lang = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getString("language", "ar") ?: "ar"
            return context.withLanguage(lang)
        }

        private fun openAppIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    private var overlay: OverlayTimer? = null
    private var blocker: FocusBlocker? = null

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensure(this)
    }

    override fun onDestroy() {
        blocker?.stop()
        blocker = null
        overlay?.hide()
        overlay = null
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                blocker?.stop()
                overlay?.hide()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START, ACTION_UPDATE -> {
                val remainingSeconds = intent.getIntExtra(EXTRA_REMAINING_SECONDS, 0)
                val stateLabel = intent.getStringExtra(EXTRA_STATE_LABEL).orEmpty()
                val notification = buildNotification(remainingSeconds, stateLabel)
                // App lock: only while a FOCUS phase runs and the user enabled it in Study.
                val focus = intent.getBooleanExtra(EXTRA_FOCUS, false)
                if (focus && FocusLockSettings.isReady(this)) {
                    (blocker ?: FocusBlocker(this).also { blocker = it }).start()
                } else {
                    blocker?.stop()
                }
                if (remainingSeconds > 0) {
                    val bubble = overlay ?: OverlayTimer(this).also { overlay = it }
                    bubble.show(System.currentTimeMillis() + remainingSeconds * 1000L)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }

            else -> {
                // Started without a valid command (e.g. by the system): don't linger.
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(remainingSeconds: Int, stateLabel: String): Notification {
        val localized = localizedContext(this)
        val endAt = System.currentTimeMillis() + remainingSeconds.coerceAtLeast(0) * 1000L

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(localized.getString(R.string.pomodoro_notif_title, stateLabel))
            .setContentText(localized.getString(R.string.pomodoro_notif_text))
            .setSmallIcon(R.drawable.ic_stat_mind)
            .setColor(ReminderReceiver.BRAND_COLOR)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setWhen(endAt)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setContentIntent(openAppIntent(this))
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
