package com.example.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.di.DatabaseModule
import com.example.domain.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** App-wide step counting (not tied to a screen). Safe to call [start] repeatedly. */
object StepTracker {
    private var manager: StepSensorManager? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var lastAchievementCheck = 0L

    @Synchronized
    fun start(context: Context) {
        if (manager != null) return
        val app = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(app, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val sensor = StepSensorManager(app)
        if (!sensor.isSensorAvailable) return
        val repo = DatabaseModule.provideFitnessRepository(app)
        sensor.startListening { delta ->
            scope.launch {
                mutex.withLock { repo.addSteps(DateUtils.startOfDay(), delta) }
                val now = System.currentTimeMillis()
                if (now - lastAchievementCheck > 60_000L) {
                    lastAchievementCheck = now
                    try {
                        DatabaseModule.provideAchievementChecker(app).checkAll()
                    } catch (_: Exception) {
                    }
                }
            }
        }
        manager = sensor
    }
}
