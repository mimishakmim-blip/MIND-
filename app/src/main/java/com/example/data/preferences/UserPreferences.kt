package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserProfile(
    val language: String = "ar",
    val onboardingCompleted: Boolean = false,
    val userName: String = "",
    val userGoal: String = "",
    val userDailyMinutes: Int = 30,
    val userWakeTime: String = "07:00",
    val userDistraction: String = "",
    val userWeight: Float = 70f,
    val userHeight: Float = 170f,
    val userAge: Int = 25,
    val userGender: String = "male",
    val authSkipped: Boolean = false,
    val memberSince: Long = 0L
)

data class UserSettings(
    val memberSince: Long = 0L,
    val avatarUri: String? = null,
    val language: String = "ar",
    val notificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val sleepReminderEnabled: Boolean = false,
    val sleepReminderTime: String = "23:00",
    val dailyGoalSteps: Int = 10000,
    val dailyGoalWater: Int = 8,
    val dailyGoalStudy: Int = 60,
    val dailyGoalHabits: Int = 5,
    val walkingStreak: Int = 0,
    val lastWalkDate: Long? = null
)

class UserPreferences private constructor(private val context: Context) {

    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("language")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_GOAL = stringPreferencesKey("user_goal")
        val USER_DAILY_MINUTES = intPreferencesKey("user_daily_minutes")
        val USER_WAKE_TIME = stringPreferencesKey("user_wake_time")
        val USER_DISTRACTION = stringPreferencesKey("user_distraction")
        val USER_WEIGHT = floatPreferencesKey("user_weight")
        val USER_HEIGHT = floatPreferencesKey("user_height")
        val USER_AGE = intPreferencesKey("user_age")
        val USER_GENDER = stringPreferencesKey("user_gender")
        val AUTH_SKIPPED = booleanPreferencesKey("auth_skipped")

        // Settings keys
        val MEMBER_SINCE = longPreferencesKey("member_since")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SLEEP_REMINDER_ENABLED = booleanPreferencesKey("sleep_reminder_enabled")
        val SLEEP_REMINDER_TIME = stringPreferencesKey("sleep_reminder_time")
        val DAILY_GOAL_STEPS = intPreferencesKey("daily_goal_steps")
        val DAILY_GOAL_WATER = intPreferencesKey("daily_goal_water")
        val DAILY_GOAL_STUDY = intPreferencesKey("daily_goal_study")
        val DAILY_GOAL_HABITS = intPreferencesKey("daily_goal_habits")
        val WALKING_STREAK = intPreferencesKey("walking_streak")
        val LAST_WALK_DATE = longPreferencesKey("last_walk_date")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserProfile(
                language = preferences[PreferencesKeys.LANGUAGE] ?: "ar",
                onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
                userName = preferences[PreferencesKeys.USER_NAME] ?: "",
                userGoal = preferences[PreferencesKeys.USER_GOAL] ?: "",
                userDailyMinutes = preferences[PreferencesKeys.USER_DAILY_MINUTES] ?: 30,
                userWakeTime = preferences[PreferencesKeys.USER_WAKE_TIME] ?: "07:00",
                userDistraction = preferences[PreferencesKeys.USER_DISTRACTION] ?: "",
                userWeight = preferences[PreferencesKeys.USER_WEIGHT] ?: 70f,
                userHeight = preferences[PreferencesKeys.USER_HEIGHT] ?: 170f,
                userAge = preferences[PreferencesKeys.USER_AGE] ?: 25,
                userGender = preferences[PreferencesKeys.USER_GENDER] ?: "male",
                authSkipped = preferences[PreferencesKeys.AUTH_SKIPPED] ?: false,
                memberSince = preferences[PreferencesKeys.MEMBER_SINCE] ?: 0L
            )
        }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                memberSince = preferences[PreferencesKeys.MEMBER_SINCE] ?: 0L,
                avatarUri = preferences[PreferencesKeys.AVATAR_URI],
                language = preferences[PreferencesKeys.LANGUAGE] ?: "ar",
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
                sleepReminderEnabled = preferences[PreferencesKeys.SLEEP_REMINDER_ENABLED] ?: false,
                sleepReminderTime = preferences[PreferencesKeys.SLEEP_REMINDER_TIME] ?: "23:00",
                dailyGoalSteps = preferences[PreferencesKeys.DAILY_GOAL_STEPS] ?: 10000,
                dailyGoalWater = preferences[PreferencesKeys.DAILY_GOAL_WATER] ?: 8,
                dailyGoalStudy = preferences[PreferencesKeys.DAILY_GOAL_STUDY] ?: 60,
                dailyGoalHabits = preferences[PreferencesKeys.DAILY_GOAL_HABITS] ?: 5,
                walkingStreak = preferences[PreferencesKeys.WALKING_STREAK] ?: 0,
                lastWalkDate = preferences[PreferencesKeys.LAST_WALK_DATE]
            )
        }

    suspend fun getUserProfile(): UserProfile = userProfileFlow.first()

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
            if (completed && (preferences[PreferencesKeys.MEMBER_SINCE] ?: 0L) == 0L) {
                preferences[PreferencesKeys.MEMBER_SINCE] = System.currentTimeMillis()
            }
        }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = false
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun setUserGoal(goal: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_GOAL] = goal
        }
    }

    suspend fun setUserDailyMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_DAILY_MINUTES] = minutes
        }
    }

    suspend fun setUserWakeTime(wakeTime: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_WAKE_TIME] = wakeTime
        }
    }

    suspend fun setUserDistraction(distraction: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_DISTRACTION] = distraction
        }
    }

    suspend fun setPhysicalProfile(weight: Float, height: Float, age: Int, gender: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_WEIGHT] = weight
            preferences[PreferencesKeys.USER_HEIGHT] = height
            preferences[PreferencesKeys.USER_AGE] = age
            preferences[PreferencesKeys.USER_GENDER] = gender
        }
    }

    suspend fun setAuthSkipped(skipped: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_SKIPPED] = skipped
        }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setVibration(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setSleepReminder(enabled: Boolean, time: String = "23:00") {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SLEEP_REMINDER_ENABLED] = enabled
            preferences[PreferencesKeys.SLEEP_REMINDER_TIME] = time
        }
    }

    suspend fun setDailyGoals(steps: Int, water: Int, study: Int, habits: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_GOAL_STEPS] = steps
            preferences[PreferencesKeys.DAILY_GOAL_WATER] = water
            preferences[PreferencesKeys.DAILY_GOAL_STUDY] = study
            preferences[PreferencesKeys.DAILY_GOAL_HABITS] = habits
        }
    }

    suspend fun setAvatarUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[PreferencesKeys.AVATAR_URI] = uri
            } else {
                preferences.remove(PreferencesKeys.AVATAR_URI)
            }
        }
    }

    suspend fun setMemberSince(millis: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MEMBER_SINCE] = millis
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
