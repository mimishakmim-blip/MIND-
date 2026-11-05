package com.example.data.backup

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.room.withTransaction
import com.example.R
import com.example.data.entity.DailyActivity
import com.example.data.entity.Habit
import com.example.data.entity.HabitLog
import com.example.data.entity.JournalEntry
import com.example.data.entity.MealLog
import com.example.data.entity.Note
import com.example.data.entity.NutritionGoals
import com.example.data.entity.SleepLog
import com.example.data.entity.StudySession
import com.example.data.entity.WorkoutLog
import com.example.data.local.MindDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * Manual backup / restore / export.
 *
 * Files are written to a location chosen by the user (Storage Access Framework), so they
 * survive uninstalling the app and can be saved to Drive, Downloads, a USB stick, etc.
 */
class BackupManager(
    private val database: MindDatabase
) {

    // ───────────────────────────── Backup ─────────────────────────────

    suspend fun createJsonBackup(context: Context, destination: Uri): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val json = buildBackupJson()
                writeText(context, destination, json.toString(2))
                toast(context, context.getString(R.string.backup_success, destination.lastPathSegment ?: ""))
                true
            } catch (e: Exception) {
                toast(context, context.getString(R.string.backup_failed))
                false
            }
        }

    private suspend fun buildBackupJson(): JSONObject {
        val habits = database.habitDao().getAllHabits().first()
        val habitLogs = database.habitLogDao().observeAllLogs().first()
        val studySessions = database.studySessionDao().getAll().first()
        val meals = database.mealLogDao().getAllMeals().first()
        val notes = database.noteDao().getAll().first()
        val journal = database.journalDao().getAll().first()
        val sleepLogs = database.sleepDao().getAll().first()
        val workouts = database.fitnessDao().getAllWorkouts().first()
        val dailyActivities = database.fitnessDao().getAllDailyActivities().first()
        val goals = database.nutritionGoalsDao().get().first()

        return JSONObject().apply {
            put("app", "MIND")
            put("version", 2)
            put("createdAt", System.currentTimeMillis())

            put("habits", JSONArray().also { arr ->
                habits.forEach { h ->
                    arr.put(JSONObject().apply {
                        put("id", h.id)
                        put("name", h.name)
                        put("description", h.description)
                        put("iconName", h.iconName)
                        put("colorHex", h.colorHex)
                        put("timeHour", h.timeHour)
                        put("timeMinute", h.timeMinute)
                        put("durationMinutes", h.durationMinutes)
                        put("reminderEnabled", h.reminderEnabled)
                        put("frequency", h.frequency)
                        put("streak", h.streak)
                        put("bestStreak", h.bestStreak)
                        put("lastCheckedDate", h.lastCheckedDate ?: JSONObject.NULL)
                        put("createdAt", h.createdAt)
                    })
                }
            })

            put("habitLogs", JSONArray().also { arr ->
                habitLogs.forEach { l ->
                    arr.put(JSONObject().apply {
                        put("id", l.id)
                        put("habitId", l.habitId)
                        put("date", l.date)
                        put("status", l.status)
                        put("markedAt", l.markedAt)
                    })
                }
            })

            put("studySessions", JSONArray().also { arr ->
                studySessions.forEach { s ->
                    arr.put(JSONObject().apply {
                        put("id", s.id)
                        put("startTime", s.startTime)
                        put("endTime", s.endTime)
                        put("durationMinutes", s.durationMinutes)
                        put("focusScore", s.focusScore)
                        put("notes", s.notes)
                        put("date", s.date)
                    })
                }
            })

            put("meals", JSONArray().also { arr ->
                meals.forEach { m ->
                    arr.put(JSONObject().apply {
                        put("id", m.id)
                        put("foodName", m.foodName)
                        put("calories", m.calories)
                        put("protein", m.protein.toDouble())
                        put("carbs", m.carbs.toDouble())
                        put("fat", m.fat.toDouble())
                        put("mealType", m.mealType)
                        put("time", m.time)
                        put("date", m.date)
                    })
                }
            })

            put("notes", JSONArray().also { arr ->
                notes.forEach { n ->
                    arr.put(JSONObject().apply {
                        put("id", n.id)
                        put("title", n.title)
                        put("content", n.content)
                        put("color", n.color)
                        put("pinned", n.pinned)
                        put("createdAt", n.createdAt)
                        put("updatedAt", n.updatedAt)
                    })
                }
            })

            put("journal", JSONArray().also { arr ->
                journal.forEach { j ->
                    arr.put(JSONObject().apply {
                        put("id", j.id)
                        put("date", j.date)
                        put("mood", j.mood)
                        put("text", j.text)
                        put("createdAt", j.createdAt)
                        put("updatedAt", j.updatedAt)
                    })
                }
            })

            put("sleepLogs", JSONArray().also { arr ->
                sleepLogs.forEach { sl ->
                    arr.put(JSONObject().apply {
                        put("id", sl.id)
                        put("bedTime", sl.bedTime)
                        put("wakeTime", sl.wakeTime)
                        put("durationMinutes", sl.durationMinutes)
                        put("quality", sl.quality)
                        put("date", sl.date)
                        put("deepSleepMinutes", sl.deepSleepMinutes)
                        put("interruptions", sl.interruptions)
                        put("notes", sl.notes)
                    })
                }
            })

            put("workouts", JSONArray().also { arr ->
                workouts.forEach { w ->
                    arr.put(JSONObject().apply {
                        put("id", w.id)
                        put("type", w.type)
                        put("durationMinutes", w.durationMinutes)
                        put("caloriesBurned", w.caloriesBurned)
                        put("distanceKm", w.distanceKm.toDouble())
                        put("steps", w.steps)
                        put("intensity", w.intensity)
                        put("timestamp", w.timestamp)
                        put("date", w.date)
                        put("notes", w.notes)
                    })
                }
            })

            put("dailyActivities", JSONArray().also { arr ->
                dailyActivities.forEach { da ->
                    arr.put(JSONObject().apply {
                        put("date", da.date)
                        put("steps", da.steps)
                        put("waterGlasses", da.waterGlasses)
                        put("activeCalories", da.activeCalories)
                        put("activeMinutes", da.activeMinutes)
                    })
                }
            })

            if (goals != null) {
                put("nutritionGoals", JSONObject().apply {
                    put("weight", goals.weight.toDouble())
                    put("height", goals.height.toDouble())
                    put("age", goals.age)
                    put("gender", goals.gender)
                    put("activityLevel", goals.activityLevel)
                    put("goal", goals.goal)
                    put("dailyCalories", goals.dailyCalories)
                    put("dailyProtein", goals.dailyProtein)
                    put("dailyCarbs", goals.dailyCarbs)
                    put("dailyFat", goals.dailyFat)
                })
            }
        }
    }

    // ───────────────────────────── Restore ─────────────────────────────

    /**
     * Parses the whole file FIRST and only then replaces the data inside ONE database
     * transaction: a corrupt / wrong file can never leave the app with half-deleted data.
     */
    suspend fun exportJsonString(): String = withContext(Dispatchers.IO) { buildBackupJson().toString() }

    suspend fun restoreFromJson(context: Context, source: Uri): Boolean {
        val text = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(source)
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
            } catch (e: Exception) {
                null
            }
        }
        if (text == null) {
            toast(context, context.getString(R.string.restore_failed))
            return false
        }
        return restoreFromText(context, text)
    }

    suspend fun restoreFromText(context: Context, text: String, silent: Boolean = false): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val root = JSONObject(text)
                if (!root.has("habits") && !root.has("studySessions") && !root.has("meals") &&
                    !root.has("sleepLogs") && !root.has("journal") && !root.has("notes")
                ) {
                    if (!silent) toast(context, context.getString(R.string.restore_invalid_file))
                    return@withContext false
                }

                val habits = root.list("habits") { o ->
                    Habit(
                        id = o.optLong("id", 0L),
                        name = o.getString("name"),
                        description = o.optString("description", ""),
                        iconName = o.optString("iconName", "star"),
                        colorHex = o.optString("colorHex", "#E8C547"),
                        timeHour = o.optInt("timeHour", 8),
                        timeMinute = o.optInt("timeMinute", 0),
                        durationMinutes = o.optInt("durationMinutes", 15),
                        reminderEnabled = o.optBoolean("reminderEnabled", false),
                        frequency = o.optString("frequency", "DAILY"),
                        streak = o.optInt("streak", 0),
                        bestStreak = o.optInt("bestStreak", 0),
                        lastCheckedDate = if (o.has("lastCheckedDate") && !o.isNull("lastCheckedDate"))
                            o.getLong("lastCheckedDate") else null,
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                }
                val habitIds = habits.map { it.id }.toSet()
                val habitLogs = root.list("habitLogs") { o ->
                    HabitLog(
                        id = o.optLong("id", 0L),
                        habitId = o.getLong("habitId"),
                        date = o.getLong("date"),
                        status = o.optString("status", "DONE"),
                        markedAt = o.optLong("markedAt", System.currentTimeMillis())
                    )
                }.filter { it.habitId in habitIds } // keep foreign keys valid

                val studySessions = root.list("studySessions") { o ->
                    StudySession(
                        id = o.optLong("id", 0L),
                        startTime = o.getLong("startTime"),
                        endTime = o.getLong("endTime"),
                        durationMinutes = o.getInt("durationMinutes"),
                        focusScore = o.optInt("focusScore", 5),
                        notes = o.optString("notes", ""),
                        date = o.getLong("date")
                    )
                }
                val meals = root.list("meals") { o ->
                    MealLog(
                        id = o.optLong("id", 0L),
                        date = o.optLong("date", System.currentTimeMillis()),
                        mealType = o.optString("mealType", "SNACK"),
                        foodName = o.optString("foodName", o.optString("name", "Meal")),
                        calories = o.getInt("calories"),
                        protein = o.optDouble("protein", 0.0).toFloat(),
                        carbs = o.optDouble("carbs", 0.0).toFloat(),
                        fat = o.optDouble("fat", 0.0).toFloat(),
                        time = o.optLong("time", System.currentTimeMillis())
                    )
                }
                val notes = root.list("notes") { o ->
                    Note(
                        id = o.optLong("id", 0L),
                        title = o.optString("title", ""),
                        content = o.optString("content", ""),
                        color = o.optString("color", "#E8C547"),
                        pinned = o.optBoolean("pinned", false),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                }
                val journal = root.list("journal") { o ->
                    JournalEntry(
                        id = o.optLong("id", 0L),
                        date = o.getLong("date"),
                        mood = o.optString("mood", "NEUTRAL"),
                        text = o.optString("text", ""),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                }
                val sleepLogs = root.list("sleepLogs") { o ->
                    SleepLog(
                        id = o.optLong("id", 0L),
                        bedTime = o.getLong("bedTime"),
                        wakeTime = o.getLong("wakeTime"),
                        durationMinutes = o.getInt("durationMinutes"),
                        quality = o.optInt("quality", 3),
                        date = o.getLong("date"),
                        deepSleepMinutes = o.optInt("deepSleepMinutes", 0),
                        interruptions = o.optInt("interruptions", 0),
                        notes = o.optString("notes", "")
                    )
                }
                val workouts = root.list("workouts") { o ->
                    WorkoutLog(
                        id = o.optLong("id", 0L),
                        type = o.optString("type", "walking"),
                        durationMinutes = o.optInt("durationMinutes", 30),
                        caloriesBurned = o.optInt("caloriesBurned", 150),
                        distanceKm = o.optDouble("distanceKm", 0.0).toFloat(),
                        steps = o.optInt("steps", 0),
                        intensity = o.optString("intensity", "medium"),
                        timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                        date = o.optLong("date", System.currentTimeMillis()),
                        notes = o.optString("notes", "")
                    )
                }
                val dailyActivities = root.list("dailyActivities") { o ->
                    DailyActivity(
                        date = o.getLong("date"),
                        steps = o.optInt("steps", 0),
                        waterGlasses = o.optInt("waterGlasses", 0),
                        activeCalories = o.optInt("activeCalories", 0),
                        activeMinutes = o.optInt("activeMinutes", 0)
                    )
                }
                val goals = root.optJSONObject("nutritionGoals")?.let { o ->
                    NutritionGoals(
                        weight = o.optDouble("weight", 70.0).toFloat(),
                        height = o.optDouble("height", 170.0).toFloat(),
                        age = o.optInt("age", 25),
                        gender = o.optString("gender", "male"),
                        activityLevel = o.optString("activityLevel", "moderate"),
                        goal = o.optString("goal", "maintain"),
                        dailyCalories = o.optInt("dailyCalories", 2000),
                        dailyProtein = o.optInt("dailyProtein", 100),
                        dailyCarbs = o.optInt("dailyCarbs", 250),
                        dailyFat = o.optInt("dailyFat", 70)
                    )
                }

                // Everything parsed successfully → now swap the data atomically.
                database.withTransaction {
                    database.habitLogDao().deleteAllLogs()
                    database.habitDao().deleteAllHabits()
                    database.studySessionDao().deleteAll()
                    database.mealLogDao().deleteAll()
                    database.noteDao().deleteAll()
                    database.journalDao().deleteAll()
                    database.sleepDao().deleteAll()
                    database.fitnessDao().deleteAllWorkouts()
                    database.fitnessDao().deleteAllDailyActivities()

                    if (habits.isNotEmpty()) database.habitDao().insertAll(habits)
                    if (habitLogs.isNotEmpty()) database.habitLogDao().insertAll(habitLogs)
                    if (studySessions.isNotEmpty()) database.studySessionDao().insertAll(studySessions)
                    if (meals.isNotEmpty()) database.mealLogDao().insertAll(meals)
                    if (notes.isNotEmpty()) database.noteDao().insertAll(notes)
                    if (journal.isNotEmpty()) database.journalDao().insertAll(journal)
                    if (sleepLogs.isNotEmpty()) database.sleepDao().insertAll(sleepLogs)
                    if (workouts.isNotEmpty()) database.fitnessDao().insertAllWorkouts(workouts)
                    if (dailyActivities.isNotEmpty()) database.fitnessDao().insertAllDailyActivities(dailyActivities)
                    if (goals != null) database.nutritionGoalsDao().upsert(goals)
                }

                if (!silent) toast(context, context.getString(R.string.restore_success))
                true
            } catch (e: Exception) {
                if (!silent) toast(context, context.getString(R.string.restore_failed))
                false
            }
        }

    // ───────────────────────────── CSV export ─────────────────────────────

    suspend fun exportToCsv(context: Context, destination: Uri): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val habits = database.habitDao().getAllHabits().first()
                val csv = buildString {
                    append('\uFEFF') // UTF-8 BOM so Excel shows Arabic text correctly
                    appendLine("name,time,frequency,streak,best_streak")
                    habits.forEach { h ->
                        appendLine(
                            listOf(
                                csvCell(h.name),
                                String.format(Locale.US, "%02d:%02d", h.timeHour, h.timeMinute),
                                h.frequency,
                                h.streak.toString(),
                                h.bestStreak.toString()
                            ).joinToString(",")
                        )
                    }
                }
                writeText(context, destination, csv)
                toast(context, context.getString(R.string.export_success, destination.lastPathSegment ?: ""))
                true
            } catch (e: Exception) {
                toast(context, context.getString(R.string.export_failed))
                false
            }
        }

    // ───────────────────────────── Helpers ─────────────────────────────

    private fun csvCell(value: String): String {
        // Quote the cell and neutralise spreadsheet formula injection.
        val safe = if (value.firstOrNull() in listOf('=', '+', '-', '@')) "'$value" else value
        return "\"" + safe.replace("\"", "\"\"") + "\""
    }

    private fun writeText(context: Context, destination: Uri, text: String) {
        val stream = context.contentResolver.openOutputStream(destination, "wt")
            ?: throw IllegalStateException("Cannot open destination")
        stream.use { it.write(text.toByteArray(Charsets.UTF_8)) }
    }

    private suspend fun toast(context: Context, message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    private inline fun <T> JSONObject.list(key: String, parse: (JSONObject) -> T): List<T> {
        val array = optJSONArray(key) ?: return emptyList()
        return List(array.length()) { parse(array.getJSONObject(it)) }
    }
}
