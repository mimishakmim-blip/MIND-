package com.example.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepSensorManager(context: Context) : SensorEventListener {

    private val prefs = context.applicationContext.getSharedPreferences("step_sensor", Context.MODE_PRIVATE)

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    val isSensorAvailable: Boolean = stepCounterSensor != null || stepDetectorSensor != null

    private val _liveSensorSteps = MutableStateFlow(0)
    val liveSensorSteps: StateFlow<Int> = _liveSensorSteps.asStateFlow()

    private var onStepDetectedListener: ((Int) -> Unit)? = null

    fun startListening(onStepDetected: ((Int) -> Unit)? = null) {
        if (!isSensorAvailable || sensorManager == null) return
        this.onStepDetectedListener = onStepDetected

        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        if (sensorManager == null) return
        try {
            sensorManager.unregisterListener(this)
        } catch (_: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                // The hardware counter is cumulative since boot: persist the last value and
                // report only the INCREMENT (also covers steps taken while the app was closed).
                val total = (event.values.firstOrNull() ?: return).toLong()
                val last = prefs.getLong(KEY_LAST_TOTAL, -1L)
                val delta = when {
                    last < 0L -> 0L          // first ever reading: baseline only
                    total >= last -> total - last
                    else -> total            // device rebooted, counter restarted
                }
                prefs.edit().putLong(KEY_LAST_TOTAL, total).apply()
                val steps = delta.toInt()
                if (steps > 0) {
                    _liveSensorSteps.value += steps
                    onStepDetectedListener?.invoke(steps)
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values.firstOrNull() == 1.0f) {
                    _liveSensorSteps.value += 1
                    onStepDetectedListener?.invoke(1)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private companion object {
        const val KEY_LAST_TOTAL = "last_counter_total"
    }
}
