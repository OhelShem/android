package com.ohelshem.app

import java.util.*

fun Long.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    return cal
}

/**
 * Returns the given date with the time values cleared.
 */
fun Calendar.clearTime(): Calendar {
    this.set(Calendar.HOUR_OF_DAY, 0)
    this.set(Calendar.MINUTE, 0)
    this.set(Calendar.SECOND, 0)
    this.set(Calendar.MILLISECOND, 0)
    return this
}

/**
 * Returns the given date with the time values cleared.
 */
fun Date.clearTime(): Date {
    return time.toCalendar().clearTime().time
}

fun Calendar.isTomorrow(): Boolean {
    val tomorrow = Calendar.getInstance()
    tomorrow.add(Calendar.DAY_OF_YEAR, 1)
    return tomorrow.isSameDay(this)
}

fun Calendar?.isSameDay(cal2: Calendar?): Boolean {
    if (this == null || cal2 == null) {
        throw IllegalArgumentException("The dates must not be null")
    }
    return (get(Calendar.ERA) == cal2.get(Calendar.ERA) && get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))
}

fun Calendar.isToday(): Boolean {
    return isSameDay(Calendar.getInstance())
}

fun getHour(): Int {
    val cal = Calendar.getInstance()
    return cal.get(Calendar.HOUR_OF_DAY)
}

fun Calendar.getDay(): Int {
    return get(Calendar.DAY_OF_WEEK)
}

fun daysBetween(day1: Calendar, day2: Calendar): Int {
    var dayOne = day1.clone() as Calendar
    var dayTwo = day2.clone() as Calendar

    if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
        return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR))
    } else {
        if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
            //swap them
            val temp = dayOne
            dayOne = dayTwo
            dayTwo = temp
        }
        var extraDays = 0

        val dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR)

        while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
            dayOne.add(Calendar.YEAR, -1)
            // getActualMaximum() important for leap years
            extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR)
        }

        return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays
    }
}

fun MutableList<*>.removeAtIfPositive(position: Int) {
    if (position >= 0)
        removeAt(position)
}
