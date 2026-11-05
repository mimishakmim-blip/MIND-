package com.example.ui.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.cloud.SyncManager
import com.example.i18n.tr
import com.example.ui.components.AccountAvatar
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSSegmentedControl
import com.example.ui.onboarding.AuthPanel
import com.example.ui.profile.ProfileSettingsViewModel
import com.example.ui.theme.mindColors

/**
 * One floating card over a blurred page. Same shell, different content:
 *  • signed out → sign-in options
 *  • signed in  → profile picture half outside the card, then the account settings.
 */
@Composable
fun AccountDialog(
    cloud: ProfileSettingsViewModel.CloudUi,
    syncState: SyncManager.State,
    userName: String,
    localAvatar: String?,
    versionText: String,
    onDismiss: () -> Unit,
    onPickPhoto: () -> Unit,
    onSave: (name: String, phone: String, channel: String) -> Unit,
    onSendVerification: () -> Unit,
    onReload: () -> Unit,
    onAutoSync: (Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onSignedIn: () -> Unit,
    onSignOut: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.mindColors
    val shape = RoundedCornerShape(30.dp)
    var confirmDelete by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .widthIn(max = 420.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                // The card (starts 48dp lower so the avatar can sit half outside)
                Column(
                    modifier = Modifier
                        .padding(top = if (cloud.signedIn) 48.dp else 0.dp)
                        .fillMaxWidth()
                        .clip(shape)
                        .background(colors.background)
                        .border(0.5.dp, colors.separatorStrong, shape)
                        .heightIn(max = 640.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp)
                        .padding(top = if (cloud.signedIn) 60.dp else 26.dp, bottom = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!cloud.signedIn) {
                        AuthPanel(onboarding = false, onDone = { onSignedIn() })
                    } else {
                        SignedInContent(
                            cloud = cloud,
                            syncState = syncState,
                            userName = userName,
                            versionText = versionText,
                            onPickPhoto = onPickPhoto,
                            onSave = onSave,
                            onSendVerification = onSendVerification,
                            onReload = onReload,
                            onAutoSync = onAutoSync,
                            onSyncNow = onSyncNow,
                            onSignOut = onSignOut,
                            onAskDelete = { confirmDelete = true }
                        )
                    }
                }

                if (cloud.signedIn) {
                    AccountAvatar(
                        size = 96.dp,
                        localUri = localAvatar,
                        remoteUrl = cloud.photoUrl,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .border(4.dp, colors.background, CircleShape)
                    )
                }

                Text(
                    text = "✕",
                    color = colors.textSecondary,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = if (cloud.signedIn) 60.dp else 12.dp, end = 14.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss)
                        .padding(8.dp)
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(tr("حذف الحساب", "Delete account")) },
            text = {
                Text(
                    tr(
                        "سيُحذف حسابك وبياناتك السحابية وبيانات التطبيق على هذا الجهاز نهائياً. لا يمكن التراجع.",
                        "Your account, cloud data and the app data on this device will be deleted permanently. This cannot be undone."
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text(tr("حذف نهائياً", "Delete forever"), color = colors.accentRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(tr("إلغاء", "Cancel")) }
            }
        )
    }
}

@Composable
private fun SignedInContent(
    cloud: ProfileSettingsViewModel.CloudUi,
    syncState: SyncManager.State,
    userName: String,
    versionText: String,
    onPickPhoto: () -> Unit,
    onSave: (String, String, String) -> Unit,
    onSendVerification: () -> Unit,
    onReload: () -> Unit,
    onAutoSync: (Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit,
    onAskDelete: () -> Unit
) {
    val colors = MaterialTheme.mindColors
    var name by remember(userName, cloud.displayName) { mutableStateOf(userName.ifBlank { cloud.displayName ?: "" }) }
    var phone by remember(cloud.phone) { mutableStateOf(cloud.phone) }
    var channel by remember(cloud.channel) { mutableStateOf(cloud.channel) }

    TextButton(onClick = onPickPhoto) {
        Text("📷 " + tr("تغيير الصورة من المعرض", "Change photo from gallery"), color = colors.primary)
    }

    OutlinedTextField(
        value = name,
        onValueChange = { name = it.take(40) },
        singleLine = true,
        label = { Text(tr("الاسم", "Name")) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = cloud.email ?: "",
        onValueChange = {},
        enabled = false,
        singleLine = true,
        label = { Text(tr("البريد الإلكتروني", "E-mail")) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = phone,
        onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' || c == ' ' }.take(18) },
        singleLine = true,
        label = { Text(tr("رقم الهاتف", "Phone number")) },
        placeholder = { Text("+213 …") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(14.dp))
    Text(
        text = tr("إرسال رموز التحقق عبر", "Send verification via"),
        color = colors.textSecondary,
        fontSize = 12.sp,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(6.dp))
    IOSSegmentedControl(
        options = listOf("✉️ " + tr("البريد", "E-mail"), "💬 SMS"),
        selectedIndex = if (channel == "sms") 1 else 0,
        onSelect = { channel = if (it == 1) "sms" else "email" },
        modifier = Modifier.fillMaxWidth()
    )

    if (cloud.needsVerification) {
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, colors.accentRed.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                "⚠️ " + tr("البريد غير موثّق — المزامنة معطّلة حتى تؤكد ملكيته", "E-mail not verified — sync is off until you confirm it"),
                color = colors.textPrimary,
                fontSize = 13.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onSendVerification) {
                    Text(tr("إرسال رابط التحقق", "Send link"), color = colors.primary)
                }
                TextButton(onClick = onReload) {
                    Text(tr("تحقّقت", "I verified"), color = colors.primary)
                }
            }
        }
    }

    Spacer(Modifier.height(14.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(tr("المزامنة التلقائية", "Auto sync"), color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(syncStatusText(syncState), color = colors.textSecondary, fontSize = 12.sp)
        }
        SyncButton(state = syncState, onClick = onSyncNow)
        Switch(checked = cloud.autoSync, onCheckedChange = onAutoSync)
    }

    cloud.message?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, color = colors.primary, fontSize = 12.sp, textAlign = TextAlign.Center)
    }

    Spacer(Modifier.height(16.dp))
    IOSButton(
        text = tr("حفظ التغييرات", "Save changes"),
        onClick = { onSave(name, phone, channel) },
        enabled = !cloud.busy,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(6.dp))
    IOSButton(
        text = tr("تسجيل الخروج", "Sign out"),
        onClick = onSignOut,
        style = IOSButtonStyle.Glass,
        modifier = Modifier.fillMaxWidth()
    )
    TextButton(onClick = onAskDelete) {
        Text(tr("حذف الحساب", "Delete account"), color = colors.accentRed)
    }
    Text(text = versionText, color = colors.textTertiary, fontSize = 11.sp)
}
