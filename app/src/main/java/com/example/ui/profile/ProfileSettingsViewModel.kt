package com.example.ui.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferences
import com.example.di.DatabaseModule
import com.example.domain.repository.AchievementRepository
import com.example.domain.repository.HabitRepository
import com.example.domain.repository.StudyRepository
import com.example.data.cloud.awaitResult
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileSettingsUiState(
    val userName: String = "",
    val memberSince: Long = 0,
    val avatarUri: String? = null,
    val language: String = "ar",
    val notificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val sleepReminderEnabled: Boolean = false,
    val sleepReminderTime: String = "23:00",
    val dailyGoalSteps: Int = 10000,
    val dailyGoalWater: Int = 8,
    val dailyGoalStudy: Int = 60,
    val dailyGoalHabits: Int = 5,
    val achievementsUnlocked: Int = 0,
    val bestStreak: Int = 0,
    val totalSteps: Int = 0,
    val totalStudyMinutes: Int = 0,
    val isLoading: Boolean = false
)

data class LiveStats(
    val bestStreak: Int = 0,
    val totalSteps: Int = 0,
    val totalStudyMinutes: Int = 0
)

class ProfileSettingsViewModel(
    application: Application,
    private val preferences: UserPreferences,
    private val habitRepo: HabitRepository,
    private val studyRepo: StudyRepository,
    private val achievementRepo: AchievementRepository,
    private val backupManager: com.example.data.backup.BackupManager,
    private val context: Context
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        preferences = UserPreferences.getInstance(application),
        habitRepo = DatabaseModule.provideHabitRepository(application),
        studyRepo = DatabaseModule.provideStudyRepository(application),
        achievementRepo = DatabaseModule.provideAchievementRepository(application),
        backupManager = DatabaseModule.provideBackupManager(application),
        context = application
    )

    data class CloudUi(
        val signedIn: Boolean = false,
        val email: String? = null,
        val displayName: String? = null,
        val photoUrl: String? = null,
        val needsVerification: Boolean = false,
        val phone: String = "",
        val channel: String = "email",
        val autoSync: Boolean = true,
        val busy: Boolean = false,
        val message: String? = null
    )

    private val _cloud = MutableStateFlow(CloudUi())
    val cloud: StateFlow<CloudUi> = _cloud
    val syncState: StateFlow<com.example.data.cloud.SyncManager.State> = com.example.data.cloud.SyncManager.state

    fun refreshCloud() {
        val user = com.example.data.cloud.CloudAccount.currentUser()
        _cloud.update {
            it.copy(
                signedIn = user != null,
                email = user?.email,
                displayName = user?.displayName,
                photoUrl = user?.photoUrl?.toString(),
                needsVerification = user != null && !com.example.data.cloud.CloudAccount.canSync(user),
                phone = com.example.data.cloud.AccountProfile.phone(context),
                channel = com.example.data.cloud.AccountProfile.channel(context),
                autoSync = com.example.data.cloud.AccountProfile.autoSync(context)
            )
        }
        if (user != null) com.example.data.cloud.SyncManager.start(context)
    }

    fun syncNow() {
        com.example.data.cloud.SyncManager.syncNow(context)
    }

    fun setAutoSync(on: Boolean) {
        com.example.data.cloud.AccountProfile.setAutoSync(context, on)
        if (on) com.example.data.cloud.SyncManager.start(context) else com.example.data.cloud.SyncManager.stop(context)
        refreshCloud()
    }

    fun sendVerification() {
        val user = com.example.data.cloud.CloudAccount.currentUser() ?: return
        viewModelScope.launch {
            val ok = com.example.data.cloud.CloudAccount.sendVerification(user).isSuccess
            _cloud.update {
                it.copy(message = if (ok) com.example.i18n.tr("أرسلنا رابط التحقق إلى بريدك", "Verification link sent to your e-mail")
                else com.example.i18n.tr("تعذّر إرسال الرابط", "Could not send the link"))
            }
        }
    }

    /** After the user opens the link in the e-mail: refresh the account and start syncing. */
    fun reloadUser() {
        viewModelScope.launch {
            try {
                com.example.data.cloud.CloudAccount.currentUser()?.reload()?.let { it.awaitResult() }
            } catch (_: Exception) {
            }
            refreshCloud()
        }
    }

    fun saveAccount(name: String, phone: String, channel: String) {
        viewModelScope.launch {
            _cloud.update { it.copy(busy = true, message = null) }
            val clean = name.trim().take(40)
            if (clean.isNotEmpty()) preferences.setUserName(clean)
            com.example.data.cloud.AccountProfile.save(context, phone.trim(), channel)
            val msg = try {
                com.example.data.cloud.AccountProfile.pushRemote(clean, phone.trim(), channel)
                com.example.i18n.tr("تم حفظ بيانات الحساب ✓", "Account saved ✓")
            } catch (e: Exception) {
                com.example.data.cloud.CloudAccount.friendlyError(e)
            }
            refreshCloud()
            _cloud.update { it.copy(busy = false, message = msg) }
        }
    }

    fun signOutCloud() {
        viewModelScope.launch {
            com.example.data.cloud.CloudAccount.signOut(context)
            refreshCloud()
        }
    }

    fun deleteAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            _cloud.update { it.copy(busy = true, message = null) }
            val result = com.example.data.cloud.CloudAccount.deleteAccount(context)
            if (result.isSuccess) {
                refreshCloud()
                onDone()
            } else {
                _cloud.update {
                    it.copy(busy = false, message = com.example.data.cloud.CloudAccount.friendlyError(result.exceptionOrNull()!!))
                }
            }
        }
    }

    private val _liveStats = MutableStateFlow(LiveStats())

    val uiState: StateFlow<ProfileSettingsUiState> = combine(
        preferences.userProfileFlow,
        preferences.settingsFlow,
        achievementRepo.getUnlockedCount(),
        _liveStats
    ) { profile, settings, unlocked, stats ->
        ProfileSettingsUiState(
            userName = profile.userName,
            memberSince = if (profile.memberSince != 0L) profile.memberSince else settings.memberSince,
            avatarUri = settings.avatarUri,
            language = settings.language,
            notificationsEnabled = settings.notificationsEnabled,
            vibrationEnabled = settings.vibrationEnabled,
            sleepReminderEnabled = settings.sleepReminderEnabled,
            sleepReminderTime = settings.sleepReminderTime,
            dailyGoalSteps = settings.dailyGoalSteps,
            dailyGoalWater = settings.dailyGoalWater,
            dailyGoalStudy = settings.dailyGoalStudy,
            dailyGoalHabits = settings.dailyGoalHabits,
            achievementsUnlocked = unlocked,
            bestStreak = stats.bestStreak,
            totalSteps = stats.totalSteps,
            totalStudyMinutes = stats.totalStudyMinutes,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileSettingsUiState()
    )

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            habitRepo.getAllHabits().collect { habits ->
                val best = habits.maxOfOrNull { it.bestStreak } ?: 0
                _liveStats.update { it.copy(bestStreak = best) }
            }
        }
        viewModelScope.launch {
            studyRepo.getSessionsSince(0).collect { sessions ->
                val totalMinutes = sessions.sumOf { it.durationMinutes }
                _liveStats.update { it.copy(totalStudyMinutes = totalMinutes) }
            }
        }
    }

    fun setUserName(name: String) {
        val clean = name.trim().take(40)
        if (clean.isEmpty()) return
        viewModelScope.launch { preferences.setUserName(clean) }
    }

    /** Copies the picked photo into app storage so it stays available after restarts. */
    fun setAvatar(uri: Uri) {
        viewModelScope.launch {
            try {
                val saved = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val dir = context.filesDir
                    dir.listFiles { f -> f.name.startsWith("avatar_") }?.forEach { it.delete() }
                    val file = java.io.File(dir, "avatar_${System.currentTimeMillis()}.jpg")
                    val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
                    input.use { i -> file.outputStream().use { o -> i.copyTo(o) } }
                    file
                }
                if (saved != null) preferences.setAvatarUri(Uri.fromFile(saved).toString())
            } catch (_: Exception) {
            }
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferences.setLanguage(lang)
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotifications(enabled)
        }
    }

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setVibration(enabled)
        }
    }

    fun setSleepReminder(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                // Bedtime = wake-up time chosen in onboarding minus 8 hours of sleep.
                val wake = preferences.getUserProfile().userWakeTime
                val parts = wake.split(":")
                val wakeHour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 7
                val wakeMinute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
                val bedHour = (wakeHour - 8 + 24) % 24
                val bedTime = String.format(java.util.Locale.US, "%02d:%02d", bedHour, wakeMinute)
                preferences.setSleepReminder(true, bedTime)
                ReminderScheduler.scheduleSleep(context, bedTime)
            } else {
                preferences.setSleepReminder(false)
                ReminderScheduler.cancelSleep(context)
            }
        }
    }

    fun setDailyGoals(steps: Int, water: Int, study: Int, habits: Int) {
        viewModelScope.launch {
            preferences.setDailyGoals(steps, water, study, habits)
        }
    }

    fun exportData(context: Context, destination: Uri) {
        viewModelScope.launch {
            backupManager.exportToCsv(context, destination)
        }
    }

    fun createBackup(context: Context, destination: Uri) {
        viewModelScope.launch {
            backupManager.createJsonBackup(context, destination)
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            val restored = backupManager.restoreFromJson(context, uri)
            if (restored) {
                // Habits may have changed: rebuild every reminder alarm.
                try {
                    ReminderScheduler.rescheduleAll(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferences.resetOnboarding()
        }
    }
}
