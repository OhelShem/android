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

package com.ohelshem.app.controller

import com.ohelshem.app.model.HourData
import com.ohelshem.app.model.NumberedHour
import com.ohelshem.api.model.Hour
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A base class for classes extend [TimetableController]
 */
abstract class BaseTimetableController : TimetableController {
    private var _timetable: Array<Array<Hour>>? = null
    protected var timetable: Array<Array<Hour>>
        get() = _timetable!!
        set(value) {
            _timetable = value
            hasData = true
            timetableUpdatedCallback()

        }

    var timetableUpdatedCallback: () -> Unit = {}

    override val size: Int
        get() = timetable.size

    override var hasData: Boolean = false

    override var colors: IntArray = intArrayOf()

    override fun get(day: Int): Array<Hour> = timetable[day]

    override fun get(day: Int, hour: Int): Hour = timetable[day][hour]

    final fun getHourDataFromTimetable(day: Int, hour: Int, minutesNow: Int, timetable: Array<Array<Hour>>): HourData = getHourData(day, hour, minutesNow, timetable)

    override final fun getHourData(day: Int, hour: Int, minutesNow: Int): HourData = getHourData(day, hour, minutesNow, timetable)

    final fun getHourData(day: Int, hour: Int, minutesNow: Int, timetable: Array<Array<Hour>>): HourData {
        val timeNow = TimetableController.getTimeToCompare(minutesNow, hour)
        val millis = with(Calendar.getInstance()) {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minutesNow)
            timeInMillis
        }
        /**
         * End of week handling.
         *  - today is saturday
         *  - today is friday and:
         *    - user doesn't learn on friday
         *    - user finished learning
         *  - today is thursday and:
         *    - user doesn't learn on friday and user finished learning
         */
        if (day == 7 || (day == 6 && (!learnsOnFriday || timeNow >= getLessonTimeToCompare(lastHourOfDay(timetable[day - 1]), false))) ||
                (day == 5 && !learnsOnFriday && timeNow >= getLessonTimeToCompare(lastHourOfDay(timetable[day - 1]), false))) {
            return getNextWeek(timetable, millis, day)
        }
        /**
         * After 21:00 handling.
         */
        if (hour >= 21) {
            var tomorrow = getLessonTimeToCompare(1).getCalendarFromTimeToCompare()
            tomorrow.add(Calendar.DAY_OF_YEAR, 1)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(tomorrow.timeInMillis - millis)
            return HourData(NumberedHour(timetable[day][0], day, hourOfDay = 0), NumberedHour(timetable[day][1], day, hourOfDay = 1), minutes.toInt(), 0)
        }
        /**
         * After school finished handling
         */
        if (timeNow >= getLessonTimeToCompare(lastHourOfDay(timetable[day - 1]), false)) {
            var tomorrow = getLessonTimeToCompare(1).getCalendarFromTimeToCompare()
            tomorrow.add(Calendar.DAY_OF_YEAR, 1)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(tomorrow.timeInMillis - millis)
            return HourData(NumberedHour(timetable[day][0], day, hourOfDay = 0), NumberedHour(timetable[day][1], day, hourOfDay = 1), minutes.toInt(), 0)
        }

        /**
         * Go through every hour of the day.
         */
        var data = getHourOfDay(timetable[day - 1], day, timeNow)
        if (data != null) return data
        /**
         * The day is empty or the user has override the day to be empty
         */
        if (day == 6 || (day == 5 && !learnsOnFriday)) return getNextWeek(timetable, millis, day)
        for (futureDay in day..timetable.size) {
            data = getHourOfDay(timetable[futureDay - 1], futureDay, 0, isToday = false)
            if (data != null) return data
        }
        return getNextWeek(timetable, millis, day)
    }

    companion object {
        private fun lastHourOfDay(day: Array<Hour>): Int {
            if (day.size == 0) return 0
            var last = day.size
            while (last - 1 >= 0) {
                if (!day[last - 1].isEmpty()) return last
                else last--
            }
            return 1
        }

        private fun getNextWeek(timetable: Array<Array<Hour>>, millis: Long, day: Int): HourData {
            val sunday = getLessonTimeToCompare(1).getCalendarFromTimeToCompare()
            sunday.add(Calendar.DAY_OF_YEAR, 7 - day + 1)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(sunday.timeInMillis - millis)
            return HourData(NumberedHour(timetable[0][0], day = 0, hourOfDay = 0), NumberedHour(timetable[0][1], day = 0, hourOfDay = 1), minutes.toInt(), 0)
        }

        private fun getHourOfDay(dayArray: Array<Hour>, day: Int, timeNow: Int, isToday: Boolean = true): HourData? {
            for (schoolHour in 1..lastHourOfDay(dayArray)) {
                val isEmpty = dayArray[schoolHour - 1].isEmpty()
                if (getLessonTimeToCompare(schoolHour) > timeNow && !isEmpty) {
                    val minutes = getLessonTimeToCompare(schoolHour) - timeNow
                    return HourData(NumberedHour(dayArray[schoolHour - 1], day - 1, schoolHour - 1),
                            if (schoolHour != dayArray.size)
                                NumberedHour(dayArray[schoolHour], day - 1, schoolHour)
                            else NumberedHour(), if (isToday) minutes else HourData.Companion.DayBefore, 0)
                } else if (getLessonTimeToCompare(schoolHour, false) > timeNow && !isEmpty) {
                    val minutes = getLessonTimeToCompare(schoolHour, false) - timeNow
                    return HourData(NumberedHour(dayArray[schoolHour - 1], day - 1, schoolHour - 1),
                            if (schoolHour != dayArray.size)
                                NumberedHour(dayArray[schoolHour], day - 1, schoolHour)
                            else NumberedHour(), if (isToday) minutes else HourData.Companion.DayBefore, if (isToday) 45 - minutes else HourData.Companion.DayBefore, false)
                }
            }
            return null
        }

        /**
         * A numeric representation of a a lesson. Used for compare time.
         *
         * @param lessonNum The number of the lesson
         * @param start True if for the start of the lesson, False for the end
         *
         * **Note: Default is start of the lesson.**
         *
         * @return A numeric representation of a hour in day (used for comparing).
         */
        fun getLessonTimeToCompare(lessonNum: Int, start: Boolean = true): Int {
            return TimetableController.HoursToCompare[if (start) lessonNum * 2 - 2 else lessonNum * 2 - 1]
        }

        /**
         * Convert the calendar to "time compare" format
         * *
         * @return The date in "time compare" format
         */
        private fun Int.getCalendarFromTimeToCompare(): Calendar {
            val cal = Calendar.getInstance().clearTime()
            cal.set(Calendar.HOUR_OF_DAY, this / 60)
            cal.set(Calendar.MINUTE, this % 60)
            return cal
        }

        /**
         * Returns the given date with the time values cleared.
         */
        private fun Calendar.clearTime(): Calendar {
            this.set(Calendar.HOUR_OF_DAY, 0)
            this.set(Calendar.MINUTE, 0)
            this.set(Calendar.SECOND, 0)
            this.set(Calendar.MILLISECOND, 0)
            return this
        }
    }
}