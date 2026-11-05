package com.example.ui.study

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.i18n.tr
import com.example.service.FocusLockSettings
import com.example.ui.theme.mindColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** "App lock": choose apps that are blocked while a Pomodoro focus session is running. */
@Composable
fun AppLockCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = MaterialTheme.mindColors
    var enabled by remember { mutableStateOf(FocusLockSettings.isEnabled(context)) }
    var blocked by remember { mutableStateOf(FocusLockSettings.blocked(context)) }
    var usageOk by remember { mutableStateOf(FocusLockSettings.hasUsageAccess(context)) }
    var overlayOk by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var showPicker by remember { mutableStateOf(false) }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageOk = FocusLockSettings.hasUsageAccess(context)
                overlayOk = Settings.canDrawOverlays(context)
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(0.5.dp, colors.separatorThin, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🔒 " + tr("قفل التطبيقات أثناء الدراسة", "App lock while studying"),
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = tr(
                        "يحجب التطبيقات المشتتة أثناء جلسة التركيز فقط",
                        "Blocks distracting apps during a focus session only"
                    ),
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    FocusLockSettings.setEnabled(context, it)
                }
            )
        }

        if (enabled) {
            Spacer(Modifier.height(12.dp))
            PermissionLine(
                ok = usageOk,
                label = tr("إذن الوصول لاستخدام التطبيقات", "Usage access permission"),
                onGrant = { context.startActivity(FocusLockSettings.usageAccessIntent()) }
            )
            PermissionLine(
                ok = overlayOk,
                label = tr("إذن الظهور فوق التطبيقات", "Display over other apps"),
                onGrant = {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showPicker = true }) {
                Text(
                    text = tr("اختيار التطبيقات المحجوبة", "Choose blocked apps") + " (${blocked.size})",
                    color = colors.primary
                )
            }
        }
    }

    if (showPicker) {
        AppPickerDialog(
            initial = blocked,
            context = context,
            onDismiss = { showPicker = false },
            onSave = {
                FocusLockSettings.setBlocked(context, it)
                blocked = it
                showPicker = false
            }
        )
    }
}

@Composable
private fun PermissionLine(ok: Boolean, label: String, onGrant: () -> Unit) {
    val colors = MaterialTheme.mindColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = (if (ok) "✅ " else "⚠️ ") + label,
            color = if (ok) colors.textSecondary else colors.textPrimary,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        if (!ok) {
            TextButton(onClick = onGrant) {
                Text(tr("منح", "Grant"), color = colors.primary)
            }
        }
    }
}

@Composable
private fun AppPickerDialog(
    initial: Set<String>,
    context: Context,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit
) {
    var apps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var selected by remember { mutableStateOf(initial) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            pm.queryIntentActivities(launcher, 0)
                .map { it.loadLabel(pm).toString() to it.activityInfo.packageName }
                .filter { it.second != context.packageName }
                .distinctBy { it.second }
                .sortedBy { it.first.lowercase() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("اختر التطبيقات المشتتة", "Pick distracting apps")) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                items(apps, key = { it.second }) { (label, pkg) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (pkg in selected) selected - pkg else selected + pkg
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = pkg in selected, onCheckedChange = null)
                        Spacer(Modifier.width(10.dp))
                        Text(text = label, maxLines = 1)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selected) }) { Text(tr("حفظ", "Save")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("إلغاء", "Cancel")) }
        }
    )
}
