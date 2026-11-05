package com.example.data.cloud

import android.content.Context
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/** Extra account details (phone, verification channel, auto-sync switch). */
object AccountProfile {
    private const val PREFS = "account"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun phone(c: Context): String = prefs(c).getString("phone", "") ?: ""
    fun channel(c: Context): String = prefs(c).getString("channel", "email") ?: "email"
    fun autoSync(c: Context): Boolean = prefs(c).getBoolean("auto_sync", true)

    fun save(c: Context, phone: String, channel: String) {
        prefs(c).edit().putString("phone", phone).putString("channel", channel).apply()
    }

    fun setAutoSync(c: Context, on: Boolean) {
        prefs(c).edit().putBoolean("auto_sync", on).apply()
    }

    /** Pushes name / phone / channel to Firebase (display name) and Firestore (profile map). */
    suspend fun pushRemote(name: String, phone: String, channel: String) {
        val user = CloudAccount.currentUser() ?: return
        if (name.isNotBlank()) {
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).awaitResult()
        }
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .set(mapOf("profile" to mapOf("name" to name, "phone" to phone, "channel" to channel)), SetOptions.merge())
            .awaitResult()
    }
}
