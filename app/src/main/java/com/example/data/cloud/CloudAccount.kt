package com.example.data.cloud

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.example.i18n.tr
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider

/**
 * Real accounts through Firebase Authentication: Google, Apple, Facebook and e-mail / password.
 * Works once app/google-services.json is added and each provider is enabled in Firebase Console.
 */
object CloudAccount {

    const val PROVIDER_FACEBOOK = "facebook.com"
    const val PROVIDER_APPLE = "apple.com"

    fun isConfigured(context: Context): Boolean = try {
        FirebaseApp.getApps(context).isNotEmpty()
    } catch (_: Exception) {
        false
    }

    /**
     * The Web client ID. The Google-services plugin generates `default_web_client_id` automatically
     * once Google sign-in is enabled and the SHA-1 is registered in Firebase (google-services.json
     * then contains an oauth_client). Falls back to the value in google_config.xml.
     */
    private fun googleClientId(context: Context): String {
        val generated = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (generated != 0) {
            val value = context.getString(generated)
            if (value.isNotBlank()) return value
        }
        return context.getString(R.string.google_web_client_id)
    }

    fun isGoogleReady(context: Context): Boolean =
        isConfigured(context) && googleClientId(context).let { it.isNotBlank() && !it.startsWith("YOUR_") }

    fun currentUser(): FirebaseUser? = try {
        FirebaseAuth.getInstance().currentUser
    } catch (_: Exception) {
        null
    }

    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> = runCatching {
        require(isGoogleReady(activity)) { "GOOGLE_NOT_READY" }
        val option = GetSignInWithGoogleOption.Builder(googleClientId(activity)).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val credential = CredentialManager.create(activity).getCredential(activity, request).credential
        require(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) { "Unsupported credential" }
        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        FirebaseAuth.getInstance()
            .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .awaitResult().user ?: error("No user")
    }

    /** Facebook / Apple through Firebase's generic OAuth flow (no extra SDK needed). */
    suspend fun signInWithProvider(activity: Activity, providerId: String): Result<FirebaseUser> = runCatching {
        val scopes = if (providerId == PROVIDER_APPLE) listOf("email", "name") else listOf("email", "public_profile")
        val provider = OAuthProvider.newBuilder(providerId).setScopes(scopes).build()
        val auth = FirebaseAuth.getInstance()
        val pending = auth.pendingAuthResult
        val result = if (pending != null) pending.awaitResult()
        else auth.startActivityForSignInWithProvider(activity, provider).awaitResult()
        result.user ?: error("No user")
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).awaitResult().user ?: error("No user")
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).awaitResult().user ?: error("No user")
    }

    /** E-mail/password accounts must confirm ownership of the address before syncing. */
    fun canSync(user: FirebaseUser): Boolean =
        user.isEmailVerified || user.providerData.none { it.providerId == "password" }

    suspend fun sendVerification(user: FirebaseUser): Result<Unit> = runCatching {
        user.sendEmailVerification().awaitResult()
        Unit
    }

    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).awaitResult()
        Unit
    }

    suspend fun signOut(context: Context) {
        try {
            SyncManager.stop(context)
            FirebaseAuth.getInstance().signOut()
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
        }
    }

    /** Deletes the cloud copy, the Firebase account and ALL local data. */
    suspend fun deleteAccount(context: Context): Result<Unit> = runCatching {
        val user = FirebaseAuth.getInstance().currentUser ?: error("Not signed in")
        SyncManager.stop(context)
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(user.uid).delete().awaitResult()
        user.delete().awaitResult() // may require a recent sign-in
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.example.di.DatabaseModule.provideDatabase(context).clearAllTables()
        }
        com.example.data.preferences.UserPreferences.getInstance(context).resetOnboarding()
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
        }
        Unit
    }

    /** Human-readable error in the current app language. */
    fun friendlyError(e: Throwable): String {
        val code = (e as? FirebaseAuthException)?.errorCode.orEmpty()
        return when {
            e is androidx.credentials.exceptions.GetCredentialCancellationException ||
                code == "ERROR_WEB_CONTEXT_CANCELED" -> tr("تم إلغاء تسجيل الدخول", "Sign-in cancelled")
            code == "ERROR_INVALID_CREDENTIAL" || code == "ERROR_WRONG_PASSWORD" || code == "ERROR_USER_NOT_FOUND" ->
                tr("البريد أو كلمة المرور غير صحيحة", "Incorrect e-mail or password")
            e.message == "GOOGLE_NOT_READY" ->
                tr("تسجيل الدخول بجوجل لم يُفعَّل بعد لهذا التطبيق", "Google sign-in is not enabled for this app yet")
            code == "ERROR_REQUIRES_RECENT_LOGIN" -> tr("لأسباب أمنية: سجّل الخروج ثم الدخول من جديد ثم أعد المحاولة", "For security: sign out, sign in again, then retry")
            code == "ERROR_EMAIL_ALREADY_IN_USE" -> tr("هذا البريد مستخدم بالفعل", "This e-mail is already registered")
            code == "ERROR_WEAK_PASSWORD" -> tr("كلمة المرور ضعيفة (6 أحرف على الأقل)", "Password too weak (min 6 characters)")
            code == "ERROR_INVALID_EMAIL" -> tr("صيغة البريد غير صحيحة", "Invalid e-mail address")
            code == "ERROR_OPERATION_NOT_ALLOWED" || code == "ERROR_CONFIGURATION_NOT_FOUND" ->
                tr("هذه الطريقة غير مفعّلة بعد في إعدادات Firebase", "This method is not enabled in Firebase yet")
            code == "ERROR_NETWORK_REQUEST_FAILED" || e is java.io.IOException ->
                tr("تحقق من اتصالك بالإنترنت", "Check your internet connection")
            else -> tr("تعذّر تسجيل الدخول", "Sign-in failed") + (e.message?.let { ": ${it.take(80)}" } ?: "")
        }
    }
}
