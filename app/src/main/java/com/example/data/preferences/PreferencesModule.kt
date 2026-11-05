package com.example.data.preferences

import android.content.Context

object PreferencesModule {
    fun provideUserPreferences(context: Context): UserPreferences {
        return UserPreferences.getInstance(context)
    }
}
