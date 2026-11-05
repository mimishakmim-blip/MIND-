package com.example.data.cloud

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
}

/** Stores the user's backup JSON in Firestore under users/{uid}. */
object CloudSync {
    private const val MAX_CHARS = 900_000 // Firestore documents are limited to 1 MB

    suspend fun upload(json: String): Boolean {
        val uid = CloudAccount.currentUser()?.uid ?: return false
        if (json.length > MAX_CHARS) return false
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .set(mapOf("app_backup" to json, "app_updatedAt" to System.currentTimeMillis()), SetOptions.merge())
            .awaitResult()
        return true
    }

    suspend fun download(): String? {
        val uid = CloudAccount.currentUser()?.uid ?: return null
        val snapshot = FirebaseFirestore.getInstance().collection("users").document(uid).get().awaitResult()
        return snapshot.getString("app_backup")
    }
}
