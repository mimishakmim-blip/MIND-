package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.MainActivity

/**
 * Small draggable bubble that shows the running Pomodoro countdown above other apps.
 * Only appears when the user has granted "Display over other apps".
 */
class OverlayTimer(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var bubble: TextView? = null
    private var endAtMillis = 0L

    private val tick = object : Runnable {
        override fun run() {
            val left = ((endAtMillis - System.currentTimeMillis() + 999L) / 1000L).coerceAtLeast(0L)
            bubble?.text = "🍅 %02d:%02d".format(left / 60, left % 60)
            if (left > 0) handler.postDelayed(this, 500L)
        }
    }

    fun show(endAt: Long) {
        if (!Settings.canDrawOverlays(context)) return
        endAtMillis = endAt
        if (bubble == null) createBubble()
        handler.removeCallbacks(tick)
        handler.post(tick)
    }

    fun hide() {
        handler.removeCallbacks(tick)
        bubble?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        bubble = null
    }

    private fun createBubble() {
        val density = context.resources.displayMetrics.density
        val view = TextView(context).apply {
            setTextColor(Color.parseColor("#15130A"))
            textSize = 16f
            setPadding((14 * density).toInt(), (8 * density).toInt(), (14 * density).toInt(), (8 * density).toInt())
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E6E1BA40"))
                cornerRadius = 40 * density
            }
            elevation = 8 * density
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (16 * density).toInt()
            y = (160 * density).toInt()
        }

        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        var moved = false
        view.setOnTouchListener { v: View, e: MotionEvent ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX; downY = e.rawY
                    startX = params.x; startY = params.y
                    moved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - downX).toInt()
                    val dy = (e.rawY - downY).toInt()
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) moved = true
                    params.x = startX + dx
                    params.y = startY + dy
                    try {
                        windowManager.updateViewLayout(v, params)
                    } catch (_: Exception) {
                    }
                }
                MotionEvent.ACTION_UP -> if (!moved) {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                }
            }
            true
        }

        try {
            windowManager.addView(view, params)
            bubble = view
        } catch (_: Exception) {
            bubble = null
        }
    }
}
