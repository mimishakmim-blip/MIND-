package com.example.reminder

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** Returns a context whose resources are resolved in [language] (e.g. "ar" / "en"). */
fun Context.withLanguage(language: String): Context {
    val locale = Locale(language)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
