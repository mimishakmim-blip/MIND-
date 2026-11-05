package com.example.ui.profile.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.cloud.SyncManager
import com.example.i18n.tr
import com.example.ui.components.AccountAvatar
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.theme.mindColors

fun syncStatusText(state: SyncManager.State): String = when (state) {
    SyncManager.State.SYNCING -> tr("جارٍ المزامنة…", "Syncing…")
    SyncManager.State.SYNCED -> tr("تمت المزامنة ✓", "Synced ✓")
    SyncManager.State.OFFLINE -> tr("بلا اتصال", "Offline")
    SyncManager.State.ERROR -> tr("تعذّرت المزامنة", "Sync failed")
    SyncManager.State.UNVERIFIED -> tr("وثّق بريدك لتفعيل المزامنة", "Verify your e-mail to sync")
    SyncManager.State.PAUSED -> tr("المزامنة متوقفة", "Sync paused")
    SyncManager.State.SIGNED_OUT -> ""
}

/**
 * Top of the account page.
 *  • signed out: small avatar + "Sign in" button at the top.
 *  • signed in: centered profile picture, name, e-mail, a recycling sync button and "Edit account".
 */
@Composable
fun AccountHeader(
    signedIn: Boolean,
    name: String,
    email: String?,
    localAvatar: String?,
    photoUrl: String?,
    syncState: SyncManager.State,
    onAvatarClick: () -> Unit,
    onPrimary: () -> Unit,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.mindColors
    val shape = RoundedCornerShape(22.dp)

    if (!signedIn) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface)
                .border(0.5.dp, colors.separatorThin, shape)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccountAvatar(
                size = 56.dp,
                localUri = localAvatar,
                remoteUrl = null,
                modifier = Modifier.clickable(onClick = onAvatarClick)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifBlank { "MIND" },
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = tr("غير مسجّل", "Not signed in"),
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
            IOSButton(
                text = tr("تسجيل الدخول", "Sign in"),
                onClick = onPrimary,
                modifier = Modifier.width(130.dp)
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface)
                .border(0.5.dp, colors.separatorThin, shape)
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AccountAvatar(size = 96.dp, localUri = localAvatar, remoteUrl = photoUrl)
            Spacer(Modifier.height(10.dp))
            Text(
                text = name.ifBlank { email ?: "MIND" },
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (email != null) {
                Text(text = email, color = colors.textSecondary, fontSize = 13.sp, maxLines = 1)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                SyncButton(state = syncState, onClick = onSync)
                Text(
                    text = syncStatusText(syncState),
                    color = if (syncState == SyncManager.State.SYNCED) colors.accentGreen else colors.textSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            IOSButton(
                text = tr("تعديل الحساب", "Edit account"),
                onClick = onPrimary,
                style = IOSButtonStyle.Glass,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}

/** Recycling-arrows button: tap to sync; spins while syncing. */
@Composable
fun SyncButton(state: SyncManager.State, onClick: () -> Unit) {
    val colors = MaterialTheme.mindColors
    val transition = rememberInfiniteTransition(label = "sync")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "syncAngle"
    )
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(
            imageVector = Icons.Default.Sync,
            contentDescription = tr("مزامنة", "Sync"),
            tint = if (state == SyncManager.State.SYNCING) colors.primary else colors.textSecondary,
            modifier = Modifier
                .size(22.dp)
                .rotate(if (state == SyncManager.State.SYNCING) angle else 0f)
        )
    }
}
