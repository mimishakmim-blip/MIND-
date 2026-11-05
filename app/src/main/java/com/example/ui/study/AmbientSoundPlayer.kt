package com.example.ui.study

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.R

// Ambient sound player with loopable audio tracks
// Note: Replace res/raw files with actual audio assets in production if needed
class AmbientSoundPlayer(
    private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentSoundId: String? = null
    private var currentVolume: Float = 0.5f

    fun play(soundId: String) {
        if (currentSoundId == soundId && mediaPlayer?.isPlaying == true) return

        stop()

        try {
            if (soundId.startsWith(GalleryUriPrefix)) {
                // A sound the user picked from the phone's gallery / files.
                val uri = android.net.Uri.parse(soundId.removePrefix(GalleryUriPrefix))
                mediaPlayer = MediaPlayer.create(context, uri)?.apply {
                    isLooping = true
                    setVolume(currentVolume, currentVolume)
                    start()
                }
                currentSoundId = soundId
                return
            }
            val resId = when (soundId) {
                "rain" -> R.raw.rain
                "forest" -> R.raw.forest
                "cafe" -> R.raw.cafe
                "white_noise" -> R.raw.white_noise
                "calm_rain" -> R.raw.calm_rain
                "calm_ocean" -> R.raw.calm_ocean
                "calm_wind" -> R.raw.calm_wind
                "calm_pad" -> R.raw.calm_pad
                "calm_bells" -> R.raw.calm_bells
                else -> return
            }
            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                isLooping = true
                setVolume(currentVolume, currentVolume)
                start()
            }
            currentSoundId = soundId
        } catch (e: Exception) {
            Log.e("AmbientSoundPlayer", "Failed to play sound: $soundId", e)
            mediaPlayer = null
            currentSoundId = soundId
        }
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AmbientSoundPlayer", "Error stopping player", e)
        }
        mediaPlayer = null
        currentSoundId = null
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(currentVolume, currentVolume)
        } catch (e: Exception) {
            Log.e("AmbientSoundPlayer", "Error setting volume", e)
        }
    }

    fun isPlaying(): Boolean = try {
        mediaPlayer?.isPlaying == true
    } catch (_: Exception) {
        false
    }

    fun getCurrentSound(): String? = currentSoundId
}

/** Prefix of a sound id that points to a user-picked audio file (content URI). */
const val GalleryUriPrefix = "uri:"

/** User sounds picked from the gallery / files; kept in app preferences. */
object GallerySounds {
    private const val PREFS = "gallery_sounds"

    /** Pairs of (display name, content uri). */
    fun list(context: android.content.Context): List<Pair<String, String>> =
        (context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            .getStringSet("items", emptySet()) ?: emptySet())
            .mapNotNull { entry ->
                val i = entry.indexOf('|')
                if (i <= 0) null else entry.substring(0, i) to entry.substring(i + 1)
            }
            .sortedBy { it.first.lowercase() }

    fun add(context: android.content.Context, uri: android.net.Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }
        var name = uri.lastPathSegment ?: "Sound"
        try {
            context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { if (it.moveToFirst()) name = it.getString(0) ?: name }
        } catch (_: Exception) {
        }
        val prefs = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        val set = (prefs.getStringSet("items", emptySet()) ?: emptySet()).toMutableSet()
        set.add(name.replace("|", " ") + "|" + uri.toString())
        prefs.edit().putStringSet("items", set).apply()
    }
}
