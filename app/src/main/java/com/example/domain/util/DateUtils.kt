package com.example.domain.util

import com.example.i18n.tr
import java.util.Calendar

object DateUtils {
    fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun yesterday(): Long = startOfDay() - 24L * 60 * 60 * 1000

    fun dayOfWeekAr(dayIndex: Int): String = when (dayIndex) {
        Calendar.SATURDAY -> tr("السبت", "Saturday")
        Calendar.SUNDAY -> tr("الأحد", "Sunday")
        Calendar.MONDAY -> tr("الاثنين", "Monday")
        Calendar.TUESDAY -> tr("الثلاثاء", "Tuesday")
        Calendar.WEDNESDAY -> tr("الأربعاء", "Wednesday")
        Calendar.THURSDAY -> tr("الخميس", "Thursday")
        Calendar.FRIDAY -> tr("الجمعة", "Friday")
        else -> ""
    }

    fun dayInitialAr(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> tr("س", "S")
            Calendar.SUNDAY -> tr("ح", "S")
            Calendar.MONDAY -> tr("ن", "M")
            Calendar.TUESDAY -> tr("ث", "T")
            Calendar.WEDNESDAY -> tr("ر", "W")
            Calendar.THURSDAY -> tr("خ", "T")
            Calendar.FRIDAY -> tr("ج", "F")
            else -> ""
        }
    }

    fun dayShortNameAr(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return dayOfWeekAr(cal.get(Calendar.DAY_OF_WEEK))
    }

    /**
     * Returns 7 timestamps for the current week starting Saturday
     */
    fun currentWeekDays(refMillis: Long = System.currentTimeMillis()): List<Long> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = refMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
        }
        return (0..6).map {
            val dayMillis = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            dayMillis
        }
    }

    /**
     * Calculates (currentStreak, bestStreak) strictly from completed dates with zero fake data.
     */
    fun calculateStreak(completedDates: Set<Long>): Pair<Int, Int> {
        if (completedDates.isEmpty()) return 0 to 0
        val today = startOfDay()
        val yesterday = today - 24L * 60 * 60 * 1000

        var current = 0
        var checkDate = when {
            completedDates.contains(today) -> today
            completedDates.contains(yesterday) -> yesterday
            else -> null
        }

        if (checkDate != null) {
            while (completedDates.contains(checkDate)) {
                current++
                checkDate -= 24L * 60 * 60 * 1000
            }
        }

        // Best streak across history
        val sorted = completedDates.sorted()
        var best = 0
        var temp = 0
        var prevDate: Long? = null
        for (date in sorted) {
            if (prevDate == null || date == prevDate + 24L * 60 * 60 * 1000) {
                temp++
            } else {
                temp = 1
            }
            if (temp > best) best = temp
            prevDate = date
        }
        if (current > best) best = current
        return current to best
    }
}
