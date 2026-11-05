package com.example.ui.home

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches a fresh quote from a public quotes service when the device is online and keeps
 * the last ones on the device. Offline, the built-in quotes are used, so nothing ever breaks.
 * Only a quote text is downloaded; no personal data is sent.
 */
object QuoteSync {
    private const val PREFS = "quotes"
    private const val KEY_LIST = "fetched"
    private const val KEY_LAST_FETCH = "last_fetch"
    private const val MIN_INTERVAL_MS = 6 * 60 * 60 * 1000L // at most every 6 hours
    private const val MAX_STORED = 100

    @Volatile
    private var cache: List<Quote> = emptyList()

    /** Newest downloaded quote (English), or null if none was ever downloaded. */
    fun latest(): Quote? = cache.lastOrNull()

    fun load(context: Context) {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LIST, null)
        cache = parse(raw)
    }

    /** Call from a background thread. */
    fun refreshIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        if (now - prefs.getLong(KEY_LAST_FETCH, 0L) < MIN_INTERVAL_MS) return

        try {
            val connection = (URL("https://dummyjson.com/quotes/random").openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
            }
            try {
                if (connection.responseCode != HttpURLConnection.HTTP_OK) return
                val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val obj = JSONObject(body)
                val text = obj.optString("quote").trim()
                val author = obj.optString("author").trim()
                if (text.isEmpty() || text.length > 300) return

                val updated = (cache.filter { it.text != text } + Quote(text, author.ifEmpty { "—" }))
                    .takeLast(MAX_STORED)
                val array = JSONArray()
                updated.forEach { array.put(JSONObject().put("t", it.text).put("a", it.author)) }
                prefs.edit()
                    .putString(KEY_LIST, array.toString())
                    .putLong(KEY_LAST_FETCH, now)
                    .apply()
                cache = updated
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            // Offline or service unavailable: keep using the saved / built-in quotes.
        }
    }

    private fun parse(raw: String?): List<Quote> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            List(array.length()) {
                val o = array.getJSONObject(it)
                Quote(o.getString("t"), o.optString("a", "—"))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
