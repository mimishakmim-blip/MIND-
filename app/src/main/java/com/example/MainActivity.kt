package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.navigation.MindNavGraph
import com.example.ui.theme.MindTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "ar") ?: "ar"
        com.example.i18n.AppLanguage.isArabic = lang == "ar"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleRoute(intent)
    }

    private fun handleRoute(intent: android.content.Intent?) {
        intent?.getStringExtra(com.example.navigation.DeepLink.EXTRA_ROUTE)?.let {
            com.example.navigation.DeepLink.route.value = it
        }
    }

    override fun onStart() {
        super.onStart()
        // Starts step counting once the activity-recognition permission has been granted.
        com.example.service.StepTracker.start(this)
        com.example.data.cloud.SyncManager.onAppOpened(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleRoute(intent)
        setContent {
            MindApp()
        }
    }
}

@Composable
fun MindApp() {
    val context = LocalContext.current

    val sharedPrefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    val themeMode = remember {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).getString("theme", "dark") ?: "dark"
    }
    val isDarkTheme = when (themeMode) {
        "light" -> false
        "system" -> androidx.compose.foundation.isSystemInDarkTheme()
        else -> true
    }
    val currentLang by remember { mutableStateOf(sharedPrefs.getString("language", "ar") ?: "ar") }
    val isArabic = currentLang == "ar"

    MindTheme(darkTheme = isDarkTheme, isArabic = isArabic) {
        MindNavGraph()
    }
}
