package com.example.ui.onboarding

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PermissionStatus {
    GRANTED,
    DENIED,
    NOT_REQUESTED,
    UNSUPPORTED
}

data class PermissionItem(
    val id: String,
    val titleRes: Int,
    val descRes: Int,
    val manifestPermission: String?,
    val minSdk: Int = 23,
    val isAdvanced: Boolean = false
)

class PermissionsViewModel(application: Application) : AndroidViewModel(application) {

    private val _permissionStatuses = MutableStateFlow<Map<String, PermissionStatus>>(emptyMap())
    val permissionStatuses: StateFlow<Map<String, PermissionStatus>> = _permissionStatuses.asStateFlow()

    fun updateStatus(id: String, granted: Boolean) {
        val current = _permissionStatuses.value.toMutableMap()
        current[id] = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
        _permissionStatuses.value = current
    }

    fun checkAllPermissions(context: Application) {
        val previous = _permissionStatuses.value
        val statuses = mutableMapOf<String, PermissionStatus>()

        // Below [minSdk] the permission does not exist and the capability is granted automatically.
        fun check(id: String, permission: String, minSdk: Int) {
            if (Build.VERSION.SDK_INT < minSdk) {
                statuses[id] = PermissionStatus.GRANTED
                return
            }
            val isGranted =
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            statuses[id] = when {
                isGranted -> PermissionStatus.GRANTED
                previous[id] == PermissionStatus.DENIED -> PermissionStatus.DENIED
                else -> PermissionStatus.NOT_REQUESTED
            }
        }

        // Only what the app really uses: reminders + step counting.
        check("notifications", Manifest.permission.POST_NOTIFICATIONS, 33)
        check("activity", Manifest.permission.ACTIVITY_RECOGNITION, 29)

        // "Display over other apps": used by the floating Pomodoro timer.
        statuses["overlay"] = when {
            android.provider.Settings.canDrawOverlays(context) -> PermissionStatus.GRANTED
            else -> PermissionStatus.NOT_REQUESTED
        }

        statuses["usage"] = when {
            com.example.service.FocusLockSettings.hasUsageAccess(context) -> PermissionStatus.GRANTED
            else -> PermissionStatus.NOT_REQUESTED
        }

        _permissionStatuses.value = statuses
    }

    init {
        checkAllPermissions(application)
    }
}
