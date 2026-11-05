package com.example.i18n

import android.content.Context

/** Current UI language, set when the activity / application starts or the user switches language. */
object AppLanguage {
    @Volatile
    var isArabic: Boolean = true

    fun load(context: Context) {
        val lang = context.applicationContext
            .getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("language", "ar") ?: "ar"
        isArabic = lang == "ar"
    }
}

/** Picks the Arabic or English text according to the app language. */
fun tr(ar: String, en: String): String = if (AppLanguage.isArabic) ar else en
