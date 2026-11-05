package com.example.data.cloud

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.room.InvalidationTracker
import com.example.di.DatabaseModule
import com.example.reminder.ReminderScheduler
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * Two-way sync between the local Room database and Cloud Firestore (users/{uid}).
 *
 *  • Local changes (any table) are detected with Room's InvalidationTracker and uploaded
 *    (at most once every 20 s, and as soon as the network returns).
 *  • A Firestore snapshot listener brings remote changes in real time.
 *  • Conflicts: the most recent change wins. Before local data is replaced, a safety copy is
 *    saved to files/pre_sync_backup.json.
 */
object SyncManager {

    enum class State { SIGNED_OUT, SYNCING, SYNCED, OFFLINE, ERROR, UNVERIFIED, PAUSED }

    private val _state = MutableStateFlow(State.SIGNED_OUT)
    val state: StateFlow<State> = _state

    private const val PREFS = "cloud_sync"
    private const val MAX_CHARS = 900_000
    private const val UPLOAD_DELAY_MS = 20_000L

    private var scope: CoroutineScope? = null
    private var registration: ListenerRegistration? = null
    private var observer: InvalidationTracker.Observer? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var uploadJob: Job? = null
    private var started = false

    @Volatile
    private var applyingRemote = false

    fun start(context: Context) {
        val app = context.applicationContext
        if (started || !CloudAccount.isConfigured(app)) return
        val user = CloudAccount.currentUser() ?: run {
            _state.value = State.SIGNED_OUT
            return
        }
        if (!AccountProfile.autoSync(app)) {
            _state.value = State.PAUSED
            return
        }
        if (!CloudAccount.canSync(user)) {
            _state.value = State.UNVERIFIED
            return
        }
        started = true
        _state.value = State.SYNCING
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope = newScope
        listenRemote(app, user.uid)
        newScope.launch { watchLocal(app) }
        watchNetwork(app)
    }

    fun stop(context: Context? = null) {
        registration?.remove()
        registration = null
        observer?.let { DatabaseModule.provideDatabase(appContext ?: return@let).invalidationTracker.removeObserver(it) }
        observer = null
        try {
            networkCallback?.let {
                (appContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
                    ?.unregisterNetworkCallback(it)
            }
        } catch (_: Exception) {
        }
        networkCallback = null
        uploadJob?.cancel()
        scope?.cancel()
        scope = null
        started = false
        _state.value = State.SIGNED_OUT
        context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()
            ?.remove("last_sync")?.remove("last_local_change")?.apply()
    }

    private var appContext: Context? = null

    /** Called every time the app comes to the foreground: start (if signed in) and push pending changes. */
    fun onAppOpened(context: Context) {
        val app = context.applicationContext
        val user = CloudAccount.currentUser() ?: return
        if (!CloudAccount.canSync(user)) {
            // The user may have just opened the verification link: refresh the account, then retry.
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    user.reload().awaitResult()
                } catch (_: Exception) {
                }
                if (CloudAccount.canSync(user)) start(app)
            }
            return
        }
        if (!started) start(app)
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getLong("last_local_change", 0L) > prefs.getLong("last_sync", 0L)) {
            scope?.launch { upload(app) }
        }
    }

    /** Upload local changes right now (used by the "Sync now" button). */
    fun syncNow(context: Context) {
        val app = context.applicationContext
        if (!started) start(app)
        scope?.launch { upload(app) }
    }

    /** Downloads the cloud copy and applies it (first sign-in on a new phone). */
    suspend fun pullNow(context: Context): Boolean {
        val app = context.applicationContext
        val uid = CloudAccount.currentUser()?.uid ?: return false
        return try {
            val snapshot = FirebaseFirestore.getInstance().collection("users").document(uid).get().awaitResult()
            val json = snapshot.getString("app_backup") ?: return false
            applyRemote(app, json, snapshot.getLong("app_updatedAt") ?: 0L)
        } catch (_: Exception) {
            false
        }
    }

    // ───────────────────────── remote → local ─────────────────────────

    private fun listenRemote(app: Context, uid: String) {
        appContext = app
        registration = FirebaseFirestore.getInstance().collection("users").document(uid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    _state.value = if (isOnline(app)) State.ERROR else State.OFFLINE
                    return@addSnapshotListener
                }
                if (snap == null || snap.metadata.hasPendingWrites()) return@addSnapshotListener
                val remoteAt = snap.getLong("app_updatedAt") ?: 0L
                val json = snap.getString("app_backup")
                val device = snap.getString("app_device")
                scope?.launch { reconcile(app, json, remoteAt, device) }
            }
    }

    private suspend fun reconcile(app: Context, json: String?, remoteAt: Long, device: String?) {
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastSync = prefs.getLong("last_sync", 0L)
        val localChange = prefs.getLong("last_local_change", 0L)
        val remoteIsNewer = json != null && remoteAt > lastSync && device != deviceId(app)
        val localIsNewer = localChange > lastSync

        when {
            json == null -> upload(app) // nothing in the cloud yet: publish local data
            remoteIsNewer && (!localIsNewer || remoteAt >= localChange) -> applyRemote(app, json, remoteAt)
            localIsNewer -> upload(app)
            else -> _state.value = State.SYNCED
        }
    }

    private suspend fun applyRemote(app: Context, json: String, remoteAt: Long): Boolean {
        val backup = DatabaseModule.provideBackupManager(app)
        applyingRemote = true
        return try {
            _state.value = State.SYNCING
            try {
                File(app.filesDir, "pre_sync_backup.json").writeText(backup.exportJsonString())
            } catch (_: Exception) {
            }
            val ok = backup.restoreFromText(app, json, silent = true)
            if (ok) {
                app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong("last_sync", remoteAt).apply()
                try {
                    ReminderScheduler.rescheduleAll(app)
                } catch (_: Exception) {
                }
                _state.value = State.SYNCED
            } else {
                _state.value = State.ERROR
            }
            ok
        } finally {
            delay(1500L)
            applyingRemote = false
        }
    }

    // ───────────────────────── local → remote ─────────────────────────

    private suspend fun watchLocal(app: Context) {
        appContext = app
        val db = DatabaseModule.provideDatabase(app)
        val tables = db.openHelper.readableDatabase
            .query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' AND name NOT LIKE 'room_%'")
            .use { cursor ->
                buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
            }
        if (tables.isEmpty()) return
        val obs = object : InvalidationTracker.Observer(tables.toTypedArray()) {
            override fun onInvalidated(tables: Set<String>) {
                if (applyingRemote) return
                app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putLong("last_local_change", System.currentTimeMillis()).apply()
                scheduleUpload(app)
            }
        }
        observer = obs
        db.invalidationTracker.addObserver(obs)
    }

    private fun scheduleUpload(app: Context) {
        if (uploadJob?.isActive == true) return
        uploadJob = scope?.launch {
            delay(UPLOAD_DELAY_MS)
            upload(app)
        }
    }

    private suspend fun upload(app: Context) {
        val uid = CloudAccount.currentUser()?.uid ?: return
        if (!isOnline(app)) {
            _state.value = State.OFFLINE
            return
        }
        try {
            _state.value = State.SYNCING
            val json = DatabaseModule.provideBackupManager(app).exportJsonString()
            if (json.length > MAX_CHARS) {
                _state.value = State.ERROR
                return
            }
            val now = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(
                    mapOf("app_backup" to json, "app_updatedAt" to now, "app_device" to deviceId(app)),
                    SetOptions.merge()
                ).awaitResult()
            app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong("last_sync", now).apply()
            _state.value = State.SYNCED
        } catch (_: Exception) {
            _state.value = if (isOnline(app)) State.ERROR else State.OFFLINE
        }
    }

    // ───────────────────────── helpers ─────────────────────────

    private fun deviceId(app: Context): String {
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString("device_id", null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString("device_id", it).apply()
        }
    }

    fun isOnline(app: Context): Boolean {
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun watchNetwork(app: Context) {
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Back online: push anything that changed while offline.
                val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                if (prefs.getLong("last_local_change", 0L) > prefs.getLong("last_sync", 0L)) {
                    scope?.launch { upload(app) }
                } else {
                    _state.value = State.SYNCED
                }
            }

            override fun onLost(network: Network) {
                _state.value = State.OFFLINE
            }
        }
        try {
            cm.registerNetworkCallback(
                NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
                callback
            )
            networkCallback = callback
        } catch (_: Exception) {
        }
    }
}
