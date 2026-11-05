package com.example.service

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.MainActivity
import com.example.i18n.tr

/** Settings for the "App lock during focus sessions" feature. */
object FocusLockSettings {
    private const val PREFS = "focus_lock"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("enabled", false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("enabled", enabled).apply()
    }

    fun blocked(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet("blocked", emptySet()) ?: emptySet()

    fun setBlocked(context: Context, packages: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet("blocked", packages.toSet()).apply()
    }

    fun hasUsageAccess(context: Context): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun usageAccessIntent(): Intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun isReady(context: Context): Boolean =
        isEnabled(context) && blocked(context).isNotEmpty() &&
            hasUsageAccess(context) && Settings.canDrawOverlays(context)
}

/**
 * While a focus session runs, watches the foreground app and covers the screen with a
 * "back to study" panel whenever one of the selected apps is opened.
 * Needs: Usage access (to see the foreground app) and Display over other apps (to cover it).
 */
class FocusBlocker(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val usage = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val handler = Handler(Looper.getMainLooper())
    private var panel: View? = null
    private var running = false
    private var lastPackage: String? = null
    private var blocked: Set<String> = emptySet()

    private val poll = object : Runnable {
        override fun run() {
            if (!running) return
            check()
            handler.postDelayed(this, 1000L)
        }
    }

    fun start() {
        if (running) return
        blocked = FocusLockSettings.blocked(context)
        if (blocked.isEmpty()) return
        running = true
        handler.post(poll)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(poll)
        hidePanel()
    }

    private fun check() {
        val manager = usage ?: return
        val now = System.currentTimeMillis()
        val events = manager.queryEvents(now - 15_000L, now)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) lastPackage = event.packageName
        }
        val current = lastPackage
        if (current != null && current in blocked) showPanel(current) else hidePanel()
    }

    private fun appLabel(packageName: String): String = try {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    } catch (_: Exception) {
        packageName
    }

    private fun showPanel(packageName: String) {
        if (panel != null || !Settings.canDrawOverlays(context)) return
        val density = context.resources.displayMetrics.density
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F20D0D0F"))
            setPadding((32 * density).toInt(), 0, (32 * density).toInt(), 0)
        }
        layout.addView(TextView(context).apply {
            text = "🔒"
            textSize = 56f
            gravity = Gravity.CENTER
        })
        layout.addView(TextView(context).apply {
            text = tr("وضع التركيز مفعّل", "Focus mode is on")
            setTextColor(Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
        })
        layout.addView(TextView(context).apply {
            text = appLabel(packageName) + " — " + tr("محجوب أثناء جلسة الدراسة", "is blocked during your study session")
            setTextColor(Color.parseColor("#A2A2AB"))
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, (8 * density).toInt(), 0, (24 * density).toInt())
        })
        layout.addView(Button(context).apply {
            text = tr("العودة إلى MIND", "Back to MIND")
            setOnClickListener {
                hidePanel()
                context.startActivity(
                    Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
        })

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        try {
            windowManager.addView(layout, params)
            panel = layout
        } catch (_: Exception) {
            panel = null
        }
    }

    private fun hidePanel() {
        panel?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        panel = null
    }
}
