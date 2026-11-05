package com.example

import android.app.Application
import com.example.reminder.NotificationChannels
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.i18n.AppLanguage.load(this)
        NotificationChannels.ensure(this)
        com.example.service.StepTracker.start(this)
        com.example.ui.home.QuoteSync.load(this)
        com.example.data.cloud.SyncManager.start(this)
        // Make sure every reminder alarm exists (cheap and idempotent).
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                ReminderScheduler.rescheduleAll(this@MindApplication)
            } catch (_: Exception) {
            }
            com.example.ui.home.QuoteSync.refreshIfNeeded(this@MindApplication)
        }
    }
}
