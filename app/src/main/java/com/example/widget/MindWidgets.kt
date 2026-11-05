package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.di.DatabaseModule
import com.example.domain.util.DateUtils
import com.example.i18n.AppLanguage
import com.example.i18n.tr
import com.example.navigation.DeepLink
import com.example.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private fun openApp(context: Context, route: String?, requestCode: Int): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (route != null) putExtra(DeepLink.EXTRA_ROUTE, route)
    }
    return PendingIntent.getActivity(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun broadcast(context: Context, cls: Class<*>, action: String, requestCode: Int): PendingIntent =
    PendingIntent.getBroadcast(
        context, requestCode,
        Intent(context, cls).apply { this.action = action },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

/** Clock + date. */
class ClockWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_clock)
            views.setOnClickPendingIntent(R.id.widget_root, openApp(context, null, 100))
            manager.updateAppWidget(id, views)
        }
    }
}

/** Water counter with a "+" button. */
class WaterWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                render(context, manager, ids)
            } finally {
                pending.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_ADD) {
            val pending = goAsync()
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    DatabaseModule.provideFitnessRepository(context.applicationContext)
                        .incrementWater(DateUtils.startOfDay(), 1)
                    refreshNow(context)
                } catch (_: Exception) {
                } finally {
                    pending.finish()
                }
            }
        } else {
            super.onReceive(context, intent)
        }
    }

    companion object {
        const val ACTION_ADD = "com.mind.app.widget.WATER_ADD"

        private suspend fun refreshNow(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WaterWidget::class.java))
            if (ids.isNotEmpty()) render(context, manager, ids)
        }

        suspend fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            AppLanguage.load(context)
            val glasses = try {
                DatabaseModule.provideFitnessRepository(context.applicationContext)
                    .getDailyActivityDirect(DateUtils.startOfDay())?.waterGlasses ?: 0
            } catch (_: Exception) {
                0
            }
            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_water)
                views.setTextViewText(R.id.water_title, "💧 " + tr("الماء اليوم", "Water today"))
                views.setTextViewText(R.id.water_count, "$glasses / 8")
                views.setTextViewText(R.id.water_add, "+ " + tr("كوب", "Glass"))
                views.setOnClickPendingIntent(R.id.water_add, broadcast(context, WaterWidget::class.java, ACTION_ADD, 200))
                views.setOnClickPendingIntent(R.id.widget_root, openApp(context, Routes.FITNESS, 201))
                manager.updateAppWidget(id, views)
            }
        }

        /** Call after the water count changes inside the app. */
        fun refresh(context: Context) {
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    refreshNow(context.applicationContext)
                } catch (_: Exception) {
                }
            }
        }
    }
}

/** Opens the focus timer. */
class PomodoroWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        AppLanguage.load(context)
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_pomodoro)
            views.setTextViewText(R.id.pomo_title, "🍅 " + tr("بومودورو", "Pomodoro"))
            views.setTextViewText(R.id.pomo_start, "▶ " + tr("ابدأ", "Start"))
            val open = openApp(context, Routes.STUDY, 300)
            views.setOnClickPendingIntent(R.id.pomo_start, open)
            views.setOnClickPendingIntent(R.id.widget_root, open)
            manager.updateAppWidget(id, views)
        }
    }
}

/** Play / pause / next / previous for whichever music app is active. */
class MusicWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        AppLanguage.load(context)
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_music)
            views.setTextViewText(R.id.music_title, "🎵 " + tr("الموسيقى", "Music"))
            views.setOnClickPendingIntent(R.id.music_prev, broadcast(context, MusicWidget::class.java, "PREV", 400))
            views.setOnClickPendingIntent(R.id.music_play, broadcast(context, MusicWidget::class.java, "PLAY", 401))
            views.setOnClickPendingIntent(R.id.music_next, broadcast(context, MusicWidget::class.java, "NEXT", 402))
            manager.updateAppWidget(id, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val key = when (intent.action) {
            "PREV" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            "PLAY" -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            "NEXT" -> KeyEvent.KEYCODE_MEDIA_NEXT
            else -> null
        }
        if (key == null) {
            super.onReceive(context, intent)
            return
        }
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, key))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, key))
    }
}
