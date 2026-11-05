package com.example.ui.profile

import com.example.i18n.tr
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BuildConfig
import com.example.R
import com.example.data.constants.ALL_ACHIEVEMENTS
import com.example.ui.components.IOSAlert
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSListItem
import com.example.ui.components.IOSSegmentedControl
import com.example.ui.profile.components.ProfileHeaderCard
import com.example.ui.profile.components.ProfileStatsRow
import com.example.ui.profile.components.SettingsGroup
import com.example.ui.profile.components.SettingsRowWithControl
import com.example.ui.profile.components.SettingsToggleRow
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@Composable
fun ProfileSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileSettingsViewModel = viewModel(),
    onNavigateToAchievements: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    onOpenAuth: () -> Unit = {}
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val state by viewModel.uiState.collectAsState()

    var showGoalsSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var showPrivacyDialog by remember { mutableStateOf(false) }
    val cloud by viewModel.cloud.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    var showAccount by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.refreshCloud() }
    var showEditProfile by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf("") }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.setAvatar(it) }
    }
    var showRestoreConfirm by remember { mutableStateOf<Uri?>(null) }

    // Restore picker (asks for confirmation because it replaces the current data)
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) showRestoreConfirm = uri
    }

    // Backup / export: the user chooses where the file is saved (Drive, Downloads, ...)
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.createBackup(context, it) }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportData(context, it) }
    }

    // Android 13+: reminders need the notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                context,
                context.getString(R.string.notifications_permission_needed),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    fun stamp(): String =
        java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.US).format(java.util.Date())

    val settingsTitle: @Composable ColumnScope.() -> Unit = {
        // Title
        Text(
            text = stringResource(R.string.settings_title),
            style = mindTypography.largeTitle.copy(fontWeight = FontWeight.Bold),
            color = mindColors.textPrimary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(20.dp))


    }
    val settingsLeft: @Composable ColumnScope.() -> Unit = {
        // 1. Profile header card
        com.example.ui.profile.components.AccountHeader(
            signedIn = cloud.signedIn,
            name = state.userName.ifBlank { cloud.displayName ?: "" },
            email = cloud.email,
            localAvatar = state.avatarUri,
            photoUrl = cloud.photoUrl,
            syncState = syncState,
            onAvatarClick = {
                nameDraft = state.userName
                showEditProfile = true
            },
            onPrimary = { showAccount = true },
            onSync = viewModel::syncNow,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(20.dp))

        // 2. Stats row
        ProfileStatsRow(
            bestStreak = state.bestStreak,
            totalSteps = state.totalSteps,
            totalStudyMinutes = state.totalStudyMinutes,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(20.dp))

        // 3. Achievements row
        SettingsGroup {
            IOSListItem(
                title = stringResource(R.string.settings_achievements),
                trailingText = "${state.achievementsUnlocked}/${ALL_ACHIEVEMENTS.size}",
                leadingIcon = Icons.Default.EmojiEvents,
                onClick = onNavigateToAchievements
            )
        }

        Spacer(Modifier.height(8.dp))

        // 4. Language Switch
        SettingsGroup {
            SettingsRowWithControl(
                icon = Icons.Default.Language,
                title = stringResource(R.string.settings_language),
                control = {
                    IOSSegmentedControl(
                        options = listOf("العربية", "English"),
                        selectedIndex = if (state.language == "ar") 0 else 1,
                        onSelect = { index ->
                            val newLang = if (index == 0) "ar" else "en"
                            if (newLang != state.language) {
                                viewModel.setLanguage(newLang)
                                context.getSharedPreferences(
                                    "settings", Context.MODE_PRIVATE
                                ).edit()
                                    .putString("language", newLang)
                                    .apply()
                                activity?.recreate()
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        // Appearance (dark / light / system)
        SettingsGroup {
            val themePrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val themeOptions = listOf("dark", "light", "system")
            val currentTheme = themePrefs.getString("theme", "dark") ?: "dark"
            SettingsRowWithControl(
                icon = Icons.Default.Language,
                title = tr("المظهر", "Appearance"),
                control = {
                    IOSSegmentedControl(
                        options = listOf(tr("داكن", "Dark"), tr("فاتح", "Light"), tr("النظام", "System")),
                        selectedIndex = themeOptions.indexOf(currentTheme).coerceAtLeast(0),
                        onSelect = { index ->
                            val chosen = themeOptions[index]
                            if (chosen != currentTheme) {
                                themePrefs.edit().putString("theme", chosen).apply()
                                activity?.recreate()
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            )
        }

        Spacer(Modifier.height(8.dp))


    }
    val settingsRight: @Composable ColumnScope.() -> Unit = {
        // 5. Notifications & Preferences
        SettingsGroup {
            SettingsToggleRow(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.settings_notifications),
                checked = state.notificationsEnabled,
                onCheckedChange = { enabled ->
                    viewModel.setNotifications(enabled)
                    if (enabled) ensureNotificationPermission()
                }
            )
            HorizontalDivider(
                color = mindColors.separatorThin,
                modifier = Modifier.padding(start = 56.dp)
            )
            SettingsToggleRow(
                icon = Icons.Default.Vibration,
                title = stringResource(R.string.settings_vibration),
                checked = state.vibrationEnabled,
                onCheckedChange = viewModel::setVibration
            )
            HorizontalDivider(
                color = mindColors.separatorThin,
                modifier = Modifier.padding(start = 56.dp)
            )
            SettingsToggleRow(
                icon = Icons.Default.Bedtime,
                title = stringResource(R.string.settings_sleep_reminder),
                subtitle = if (state.sleepReminderEnabled) state.sleepReminderTime else null,
                checked = state.sleepReminderEnabled,
                onCheckedChange = { enabled ->
                    viewModel.setSleepReminder(enabled)
                    if (enabled) ensureNotificationPermission()
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        // 6. Daily goals
        SettingsGroup {
            IOSListItem(
                title = stringResource(R.string.settings_daily_goals),
                leadingIcon = Icons.Default.Flag,
                onClick = { showGoalsSheet = true }
            )
        }

        Spacer(Modifier.height(8.dp))

        // 7. Data section
        SettingsGroup {
            IOSListItem(
                title = stringResource(R.string.settings_export),
                leadingIcon = Icons.Default.FileDownload,
                onClick = { exportLauncher.launch("MIND_habits_${stamp()}.csv") }
            )
            HorizontalDivider(
                color = mindColors.separatorThin,
                modifier = Modifier.padding(start = 56.dp)
            )
            IOSListItem(
                title = stringResource(R.string.settings_backup),
                leadingIcon = Icons.Default.Backup,
                onClick = { backupLauncher.launch("MIND_backup_${stamp()}.json") }
            )
            HorizontalDivider(
                color = mindColors.separatorThin,
                modifier = Modifier.padding(start = 56.dp)
            )
            IOSListItem(
                title = stringResource(R.string.settings_restore),
                leadingIcon = Icons.Default.Restore,
                onClick = {
                    restoreLauncher.launch(
                        arrayOf("application/json", "text/plain", "*/*")
                    )
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        // 8. About section
        SettingsGroup {
            IOSListItem(
                title = stringResource(R.string.settings_about),
                leadingIcon = Icons.Default.Info,
                onClick = { showAboutDialog = true }
            )
            HorizontalDivider(
                color = mindColors.separatorThin,
                modifier = Modifier.padding(start = 56.dp)
            )
            IOSListItem(
                title = stringResource(R.string.settings_privacy),
                leadingIcon = Icons.Default.PrivacyTip,
                onClick = { showPrivacyDialog = true }
            )
        }

        Spacer(Modifier.height(20.dp))

        // 9. Logout button
        IOSButton(
            text = stringResource(R.string.settings_logout),
            onClick = { showLogoutDialog = true },
            style = IOSButtonStyle.Text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        // 10. Version
        Text(
            text = stringResource(R.string.app_name) + " " + stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
            color = mindColors.textTertiary,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )


    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_settings_screen")
            .then(if (showAccount && Build.VERSION.SDK_INT >= 31) Modifier.blur(18.dp) else Modifier)
            .padding(bottom = 100.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))
        settingsTitle()
        if (com.example.ui.components.isWideLayout()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) { settingsLeft() }
                Column(modifier = Modifier.weight(1f)) { settingsRight() }
            }
        } else {
            settingsLeft()
            settingsRight()
        }
        Spacer(Modifier.height(24.dp))
    }

    // Dialogs & Sheets
    if (showGoalsSheet) {
        DailyGoalsSheet(
            state = state,
            onSave = { steps, water, study, habits ->
                viewModel.setDailyGoals(steps, water, study, habits)
                showGoalsSheet = false
            },
            onDismiss = { showGoalsSheet = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showEditProfile) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text(stringResource(R.string.profile_edit)) },
            text = {
                Column {
                    androidx.compose.material3.OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it.take(40) },
                        singleLine = true,
                        label = { Text(stringResource(R.string.q_name_title)) }
                    )
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.TextButton(onClick = {
                        photoLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }) { Text(tr("اختيار صورة", "Choose photo")) }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.setUserName(nameDraft)
                    showEditProfile = false
                }) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEditProfile = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showAccount) {
        com.example.ui.profile.components.AccountDialog(
            cloud = cloud,
            syncState = syncState,
            userName = state.userName,
            localAvatar = state.avatarUri,
            versionText = stringResource(R.string.app_name) + " " + stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
            onDismiss = { showAccount = false },
            onPickPhoto = {
                photoLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onSave = viewModel::saveAccount,
            onSendVerification = viewModel::sendVerification,
            onReload = viewModel::reloadUser,
            onAutoSync = viewModel::setAutoSync,
            onSyncNow = viewModel::syncNow,
            onSignedIn = { viewModel.refreshCloud() },
            onSignOut = viewModel::signOutCloud,
            onDelete = {
                viewModel.deleteAccount {
                    showAccount = false
                    onLogoutSuccess()
                }
            }
        )
    }

    if (showPrivacyDialog) {
        PrivacyDialog(onDismiss = { showPrivacyDialog = false })
    }

    showRestoreConfirm?.let { pendingUri ->
        IOSAlert(
            title = stringResource(R.string.settings_restore),
            message = stringResource(R.string.restore_confirm_message),
            confirmText = stringResource(R.string.settings_restore),
            onConfirm = {
                viewModel.restoreBackup(pendingUri)
                showRestoreConfirm = null
            },
            dismissText = stringResource(R.string.common_cancel),
            onDismiss = { showRestoreConfirm = null }
        )
    }

    if (showLogoutDialog) {
        IOSAlert(
            title = stringResource(R.string.logout_confirm_title),
            message = stringResource(R.string.logout_confirm_message),
            confirmText = stringResource(R.string.logout_confirm_yes),
            onConfirm = {
                viewModel.logout()
                showLogoutDialog = false
                onLogoutSuccess()
            },
            dismissText = stringResource(R.string.common_cancel),
            onDismiss = { showLogoutDialog = false }
        )
    }
}
