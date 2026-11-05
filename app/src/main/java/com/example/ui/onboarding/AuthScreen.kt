package com.example.ui.onboarding

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.cloud.CloudAccount
import com.example.i18n.AppLanguage
import com.example.i18n.tr
import com.example.ui.components.FacebookBadge
import com.example.ui.components.GoogleBadge
import com.example.ui.components.IOSButton
import com.example.ui.components.emailHint
import com.example.ui.theme.mindColors

private fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** Sign-in / create-account content, shared by the onboarding page and the floating account card. */
@Composable
fun AuthPanel(
    onboarding: Boolean,
    onDone: (restored: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val colors = MaterialTheme.mindColors
    val ui by viewModel.ui.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var createMode by remember { mutableStateOf(false) }
    val hint = emailHint(email, AppLanguage.isArabic)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (createMode) tr("إنشاء حساب", "Create account") else tr("تسجيل الدخول", "Sign in"),
            color = colors.textPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = tr(
                "احفظ بياناتك في حسابك وزامنها بين أجهزتك",
                "Keep your data in your account and sync it across devices"
            ),
            color = colors.textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        ProviderButton(
            badge = { GoogleBadge() },
            text = tr("المتابعة مع Google", "Continue with Google"),
            enabled = !ui.busy,
            onClick = { activity?.let { viewModel.google(it, onboarding, onDone) } }
        )
        Spacer(Modifier.height(10.dp))
        ProviderButton(
            badge = { FacebookBadge() },
            text = tr("المتابعة مع Facebook", "Continue with Facebook"),
            enabled = !ui.busy,
            onClick = {
                activity?.let { viewModel.provider(it, CloudAccount.PROVIDER_FACEBOOK, onboarding, onDone) }
            }
        )

        Spacer(Modifier.height(16.dp))
        Text(tr("أو", "or"), color = colors.textTertiary, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.take(100).replace(" ", "") },
            singleLine = true,
            isError = hint != null && email.contains("@"),
            label = { Text(tr("البريد الإلكتروني", "E-mail")) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        hint?.let { (message, fix) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    color = colors.primary,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                if (fix != null) {
                    TextButton(onClick = { email = fix }) {
                        Text(tr("تصحيح", "Fix"), color = colors.primary)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.take(64) },
            singleLine = true,
            label = { Text(tr("كلمة المرور (6 أحرف على الأقل)", "Password (min 6 characters)")) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        ui.error?.let {
            Text(it, color = colors.accentRed, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
        }
        ui.info?.let {
            Text(it, color = colors.accentGreen, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
        }

        if (ui.busy) {
            CircularProgressIndicator(color = colors.primary)
        } else {
            IOSButton(
                text = if (createMode) tr("إنشاء الحساب وإرسال رابط التحقق", "Create account & send verification link")
                else tr("تسجيل الدخول", "Sign in"),
                onClick = { viewModel.email(email, password, createMode, onboarding, onDone) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(4.dp))
        TextButton(onClick = { createMode = !createMode }) {
            Text(
                text = if (createMode) tr("لدي حساب بالفعل", "I already have an account")
                else tr("إنشاء حساب جديد", "Create a new account"),
                color = colors.primary
            )
        }
        if (!createMode) {
            TextButton(onClick = { viewModel.resetPassword(email) }) {
                Text(tr("نسيت كلمة المرور؟", "Forgot password?"), color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun ProviderButton(
    badge: @Composable () -> Unit,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.mindColors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(colors.surface)
            .border(0.5.dp, colors.separatorStrong, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        badge()
        Text(
            text = text,
            color = colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(end = 28.dp)
        )
    }
}

/** Full page used at the END of onboarding (after permissions), with a dim "Skip". */
@Composable
fun AuthScreen(
    onboarding: Boolean,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onDone: (restored: Boolean) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val colors = MaterialTheme.mindColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthPanel(onboarding = onboarding, onDone = onDone, viewModel = viewModel)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = tr("رجوع", "Back"),
                color = colors.textTertiary,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onBack).padding(12.dp)
            )
            Spacer(Modifier.width(24.dp))
            if (onboarding) {
                Text(
                    text = tr("تخطّي", "Skip"),
                    color = colors.textTertiary.copy(alpha = 0.55f),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(onClick = onSkip).padding(12.dp)
                )
            }
        }
    }
}
