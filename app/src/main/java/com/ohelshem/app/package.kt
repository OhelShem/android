/*
 * Copyright 2016 Yoav Sternberg.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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

fun Calendar?.isTomorrow(): Boolean {
    val tomorrow = Calendar.getInstance()
    tomorrow.add(Calendar.DAY_OF_YEAR, 1)
    return tomorrow.isSameDay(if (this == null) Calendar.getInstance() else this)
}

fun Calendar?.isSameDay(cal2: Calendar?): Boolean {
    if (this == null || cal2 == null) {
        throw IllegalArgumentException("The dates must not be null")
    }
    return (this.get(Calendar.ERA) == cal2.get(Calendar.ERA) && this.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && this.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))
}

fun Calendar.isToday(): Boolean {
    return this.isSameDay(Calendar.getInstance())
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

    if (dayOne.get(Calendar.YEAR) === dayTwo.get(Calendar.YEAR)) {
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