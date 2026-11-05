package com.example.navigation

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val QUESTIONNAIRE = "questionnaire"
    const val PROFILE_SETUP = "profile_setup"
    const val PERMISSIONS = "permissions"
    const val READY = "ready"
    const val AUTH = "auth/{onboarding}"
    fun auth(onboarding: Boolean) = "auth/$onboarding"
    const val MAIN = "main"

    // Sub-screens
    const val SLEEP = "sleep"
    const val FITNESS = "fitness"
    const val STUDY = "study"
    const val NUTRITION = "nutrition"
    const val JOURNAL = "journal"
    const val NOTES = "notes"
}
