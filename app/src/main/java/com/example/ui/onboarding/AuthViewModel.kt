package com.example.ui.onboarding

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.cloud.CloudAccount
import com.example.data.cloud.SyncManager
import com.example.data.preferences.UserPreferences
import com.example.i18n.tr
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUi(
    val configured: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
    val info: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val _ui = MutableStateFlow(AuthUi(configured = CloudAccount.isConfigured(application)))
    val ui: StateFlow<AuthUi> = _ui

    fun google(activity: Activity, onboarding: Boolean, onDone: (Boolean) -> Unit) =
        run(onboarding, onDone) { CloudAccount.signInWithGoogle(activity) }

    fun provider(activity: Activity, providerId: String, onboarding: Boolean, onDone: (Boolean) -> Unit) =
        run(onboarding, onDone) { CloudAccount.signInWithProvider(activity, providerId) }

    fun email(email: String, password: String, create: Boolean, onboarding: Boolean, onDone: (Boolean) -> Unit) {
        val clean = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(clean).matches()) {
            _ui.update { it.copy(error = tr("صيغة البريد غير صحيحة", "Invalid e-mail address"), info = null) }
            return
        }
        if (password.length < 6) {
            _ui.update { it.copy(error = tr("كلمة المرور 6 أحرف على الأقل", "Password must be at least 6 characters"), info = null) }
            return
        }
        run(onboarding, onDone) {
            if (create) {
                val result = CloudAccount.signUpWithEmail(clean, password)
                result.getOrNull()?.let { CloudAccount.sendVerification(it) }
                result
            } else {
                CloudAccount.signInWithEmail(clean, password)
            }
        }
    }

    fun resetPassword(email: String) {
        val clean = email.trim()
        if (clean.isEmpty()) {
            _ui.update { it.copy(error = tr("اكتب بريدك أولاً", "Enter your e-mail first"), info = null) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null, info = null) }
            val result = CloudAccount.resetPassword(clean)
            _ui.update {
                if (result.isSuccess) it.copy(busy = false, info = tr("أرسلنا رابط إعادة التعيين إلى بريدك", "Reset link sent to your e-mail"))
                else it.copy(busy = false, error = CloudAccount.friendlyError(result.exceptionOrNull()!!))
            }
        }
    }

    private fun run(onboarding: Boolean, onDone: (Boolean) -> Unit, block: suspend () -> Result<FirebaseUser>) {
        if (!_ui.value.configured) {
            _ui.update { it.copy(error = tr("تسجيل الدخول السحابي غير مُفعَّل في هذه النسخة بعد (ملف Firebase مفقود)", "Cloud sign-in is not set up in this build yet (Firebase file missing)")) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null, info = null) }
            val result = block()
            val user = result.getOrNull()
            if (user == null) {
                _ui.update { it.copy(busy = false, error = CloudAccount.friendlyError(result.exceptionOrNull()!!)) }
                return@launch
            }
            var restored = false
            if (onboarding) {
                // New install on a (possibly) new phone: bring the saved data back.
                try {
                    if (SyncManager.pullNow(app)) {
                        UserPreferences.getInstance(app).setOnboardingCompleted(true)
                        restored = true
                    }
                } catch (_: Exception) {
                }
                user.displayName?.takeIf { it.isNotBlank() }?.let { UserPreferences.getInstance(app).setUserName(it) }
            }
            SyncManager.start(app)
            _ui.update { it.copy(busy = false) }
            onDone(restored)
        }
    }
}
