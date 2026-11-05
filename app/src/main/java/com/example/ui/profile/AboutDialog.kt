package com.example.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.BuildConfig
import com.example.R
import com.example.ui.components.IOSAlert

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    IOSAlert(
        title = stringResource(R.string.app_name),
        message = stringResource(R.string.app_tagline) + "\n\n" +
            stringResource(R.string.welcome_subtitle) + "\n\n" +
            stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
        onConfirm = onDismiss,
        onDismiss = onDismiss,
        confirmText = stringResource(R.string.common_ok),
        dismissText = null
    )
}

@Composable
fun PrivacyDialog(onDismiss: () -> Unit) {
    IOSAlert(
        title = stringResource(R.string.privacy_title),
        message = stringResource(R.string.privacy_body),
        onConfirm = onDismiss,
        onDismiss = onDismiss,
        confirmText = stringResource(R.string.common_ok),
        dismissText = null
    )
}
