package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtil {

    private fun calendarForEpochDay(epochDay: Long): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = TimeUnit.DAYS.toMillis(epochDay)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun todayEpochDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return TimeUnit.MILLISECONDS.toDays(cal.timeInMillis)
    }

    fun formatShortDate(epochDay: Long): String {
        val sdf = SimpleDateFormat("MMM d", Locale.US)
        return sdf.format(calendarForEpochDay(epochDay).time)
    }

    fun formatDayHeader(epochDay: Long): String {
        val sdf = SimpleDateFormat("EEEE, MMM d", Locale.US)
        return sdf.format(calendarForEpochDay(epochDay).time)
    }

    fun formatMonthYear(epochDay: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        return sdf.format(calendarForEpochDay(epochDay).time)
    }

    fun formatFullDate(epochDay: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return sdf.format(calendarForEpochDay(epochDay).time)
    }

    fun dayOfWeekInitial(epochDay: Long): String {
        val sdf = SimpleDateFormat("EEEEE", Locale.US)
        return sdf.format(calendarForEpochDay(epochDay).time)
    }

    fun dayOfMonth(epochDay: Long): Int {
        return calendarForEpochDay(epochDay).get(Calendar.DAY_OF_MONTH)
    }

    fun formatTimestamp(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d • h:mm a", Locale.US)
        return sdf.format(millis)
    }

    fun calculateCycleDay(
        epochDay: Long,
        lastPeriodStartEpochDay: Long,
        cycleLengthDays: Int
    ): Int {
        val safeLen = cycleLengthDays.coerceIn(21, 45)
        val diff = (epochDay - lastPeriodStartEpochDay).toInt()
        val mod = ((diff % safeLen) + safeLen) % safeLen
        return mod + 1
    }

    fun calculateCycleNumber(
        epochDay: Long,
        lastPeriodStartEpochDay: Long,
        cycleLengthDays: Int
    ): Int {
        val safeLen = cycleLengthDays.coerceIn(21, 45)
        val diff = (epochDay - lastPeriodStartEpochDay).toInt()
        return if (diff >= 0) {
            (diff / safeLen) + 2
        } else {
            val offset = ((-diff - 1) / safeLen)
            (1 - offset).coerceAtLeast(1)
        }
    }

    fun cyclePhaseForDay(
        cycleDay: Int,
        cycleLengthDays: Int,
        periodDurationDays: Int
    ): String {
        val safePeriod = periodDurationDays.coerceIn(2, 10)
        val ovulationStart = (cycleLengthDays / 2).coerceAtLeast(safePeriod + 2)
        return when {
            cycleDay <= safePeriod -> "Menstrual phase"
            cycleDay < ovulationStart - 1 -> "Follicular phase"
            cycleDay <= ovulationStart + 2 -> "Ovulatory phase"
            else -> "Luteal phase"
        }
    }
}
