package com.example.ui.onboarding

import com.example.i18n.tr
import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.IOSCard
import com.example.ui.onboarding.components.SmallNavButtons
import com.example.ui.theme.MindTheme
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    onNavigateToReady: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PermissionsViewModel = viewModel()
) {
    val context = LocalContext.current
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val statuses by viewModel.permissionStatuses.collectAsState()

    var activePermissionId by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        activePermissionId?.let { id ->
            viewModel.updateStatus(id, isGranted)
        }
    }

    // Re-check when the user comes back (e.g. after granting from system settings).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAllPermissions(context.applicationContext as Application)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun requestPerm(id: String, permission: String?) {
        if (permission == null) {
            viewModel.updateStatus(id, true)
            return
        }
        if (statuses[id] == PermissionStatus.DENIED) {
            // Android won't show the dialog again after a denial: send the user to app settings.
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
            )
            return
        }
        activePermissionId = id
        permissionLauncher.launch(permission)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mindColors.background)
            .statusBarsPadding()
            .testTag("permissions_screen")
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("permissions_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = mindColors.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.permissions_title),
                style = mindTypography.largeTitle,
                color = mindColors.textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(id = R.string.permissions_subtitle),
                style = mindTypography.body,
                color = mindColors.textSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // BASIC PERMISSIONS GROUP
            Text(
                text = stringResource(id = R.string.perm_basic_group),
                style = mindTypography.headline,
                color = mindColors.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            IOSCard(modifier = Modifier.fillMaxWidth()) {
                // 1. Notifications
                PermissionRow(
                    icon = Icons.Default.Notifications,
                    iconTint = mindColors.accentOrange,
                    title = stringResource(id = R.string.perm_notifications),
                    desc = stringResource(id = R.string.perm_notifications_desc),
                    status = statuses["notifications"] ?: PermissionStatus.NOT_REQUESTED,
                    onRequest = {
                        val perm = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null
                        requestPerm("notifications", perm)
                    }
                )

                HorizontalDivider(thickness = 0.5.dp, color = mindColors.separatorThin)

                // 2. Activity
                PermissionRow(
                    icon = Icons.Default.DirectionsRun,
                    iconTint = mindColors.accentGreen,
                    title = stringResource(id = R.string.perm_activity),
                    desc = stringResource(id = R.string.perm_activity_desc),
                    status = statuses["activity"] ?: PermissionStatus.NOT_REQUESTED,
                    onRequest = {
                        val perm = if (Build.VERSION.SDK_INT >= 29) Manifest.permission.ACTIVITY_RECOGNITION else null
                        requestPerm("activity", perm)
                    }
                )

                HorizontalDivider(thickness = 0.5.dp, color = mindColors.separatorThin)

                // 3. Display over other apps (floating Pomodoro timer)
                PermissionRow(
                    icon = Icons.Default.Layers,
                    iconTint = mindColors.primary,
                    title = tr("الظهور فوق التطبيقات الأخرى", "Display over other apps"),
                    desc = tr("يعرض مؤقت التركيز في فقاعة عائمة أثناء استخدام تطبيقات أخرى", "Shows the focus timer in a floating bubble while you use other apps"),
                    status = statuses["overlay"] ?: PermissionStatus.NOT_REQUESTED,
                    onRequest = {
                        context.startActivity(
                            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        )
                    }
                )

                HorizontalDivider(thickness = 0.5.dp, color = mindColors.separatorThin)

                // 4. Usage access (app lock during study)
                PermissionRow(
                    icon = Icons.Default.Check,
                    iconTint = mindColors.primary,
                    title = tr("الوصول لاستخدام التطبيقات", "Usage access"),
                    desc = tr("لازم لقفل التطبيقات المشتتة أثناء الدراسة", "Needed to block distracting apps while you study"),
                    status = statuses["usage"] ?: PermissionStatus.NOT_REQUESTED,
                    onRequest = {
                        context.startActivity(com.example.service.FocusLockSettings.usageAccessIntent())
                    }
                )

            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // FIXED BOTTOM: SmallNavButtons
        SmallNavButtons(
            onPrevious = onBack,
            onNext = onNavigateToReady,
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    desc: String,
    status: PermissionStatus,
    onRequest: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconTint.copy(alpha = 0.16f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = mindTypography.body.copy(fontWeight = FontWeight.SemiBold),
                color = mindColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = mindTypography.footnote,
                color = mindColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Trailing Status Button
        when (status) {
            PermissionStatus.NOT_REQUESTED -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(mindColors.primary)
                        .clickable(onClick = onRequest)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.perm_allow),
                        style = mindTypography.footnote.copy(fontWeight = FontWeight.Bold),
                        color = mindColors.textInverse
                    )
                }
            }
            PermissionStatus.GRANTED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(mindColors.success.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = mindColors.success,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.perm_granted),
                        style = mindTypography.footnote.copy(fontWeight = FontWeight.SemiBold),
                        color = mindColors.success
                    )
                }
            }
            PermissionStatus.DENIED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(mindColors.error.copy(alpha = 0.15f))
                        .clickable(onClick = onRequest)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = mindColors.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.perm_denied),
                        style = mindTypography.footnote.copy(fontWeight = FontWeight.SemiBold),
                        color = mindColors.error
                    )
                }
            }
            PermissionStatus.UNSUPPORTED -> {
                Text(
                    text = stringResource(id = R.string.perm_unsupported),
                    style = mindTypography.caption,
                    color = mindColors.textTertiary
                )
            }
        }
    }
}

@Preview(name = "Permissions Dark", showBackground = true)
@Composable
fun PermissionsScreenPreview() {
    MindTheme(darkTheme = true) {
        PermissionsScreen(onBack = {}, onNavigateToReady = {})
    }
}
