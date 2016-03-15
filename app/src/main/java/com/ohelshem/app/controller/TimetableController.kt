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

import com.ohelshem.app.clearTime
import com.ohelshem.app.daysBetween
import com.ohelshem.app.model.HourData
import com.ohelshem.app.toCalendar
import com.ohelshem.api.model.Hour
import java.text.SimpleDateFormat
import java.util.*

/**
 * An interface for classes that provide the timetable data
 */
interface TimetableController {
    operator fun get(day: Int): Array<Hour>
    operator fun get(day: Int, hour: Int): Hour

    val hasData: Boolean
    val size: Int
    val learnsOnFriday: Boolean
        get() = size == 6

    fun getHourData(day: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
                    hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    minutesNow: Int = Calendar.getInstance().get(Calendar.MINUTE)): HourData

    var colors: IntArray

    fun init()

    companion object {
        /**
         * The lessons' hours.
         * The `2k` hour is the start of the lesson `k+1`.
         * The `2k+1` hour is the end of the lesson `k+1`
         */
        val DayHours: Array<String> = arrayOf(
                "08:00", "08:45",
                "08:50", "09:35",
                "09:50", "10:35",
                "10:40", "11:25",
                "11:45", "12:30",
                "12:35", "13:20",
                "13:30", "14:15",
                "14:20", "15:05",
                "15:10", "15:55",
                "16:00", "16:45",
                "16:50", "17:35")

        /**
         * Hours for alarm, the first int is hour and the second is minute.
         */
        val AlarmHours: Array<Pair<Int, Int>> = arrayOf(
                7 to 0,
                8 to 43,
                9 to 33,
                10 to 33,
                11 to 23,
                12 to 28,
                13 to 18,
                14 to 13,
                15 to 3,
                15 to 53,
                16 to 43,
                17 to 33
        )

        val Holidays = arrayOf(
                Holiday("ראש השנה", "13/09/2015", "15/09/2015"),
                Holiday("יום כיפור", "22/09/2015", "23/09/2015"),
                Holiday("סוכות", "27/09/2015", "06/10/2015"),
                Holiday("חנוכה", "08/12/2015", "14/12/2015"),
                Holiday("פורים", "23/03/2016", "25/03/2016"),
                Holiday("פסח", "13/04/2016", "30/04/2016"),
                Holiday("יום העצמאות", "12/05/2016"),
                Holiday("ל\"ג בעומר", "26/05/2016"),
                Holiday("שבועות", "11/06/2016", "13/06/2016"))

        val Summer = Holiday("קיץ", "21/06/2016", "29/08/2016")


        fun getStartOfHour(hour: Int): String = DayHours[hour * 2 - 2]

        fun getEndOfHour(hour: Int): String = DayHours[hour * 2 - 1]

        fun isEndOfDay(hour: Int, day: Array<Hour>): Boolean {
            if (hour >= day.size) return true
            var i = day.size - 1
            do {
                if (i == hour) return true
                else if (!(i >= 0 && day[i].isEmpty()))
                    return false
                i--
            } while (true)
            throw IllegalStateException()
        }

        /**
         * A numeric representation for all the lessons. Used for compare time.
         */
        internal val HoursToCompare: IntArray by lazy {
            val array = IntArray(DayHours.size)
            DayHours.forEachIndexed { i, s -> array[i] = getTimeToCompare(s.substring(3, 5).toInt(), s.substring(0, 2).toInt()) }
            array
        }

        /**
         * Convert the data to "time compare" format
         * @param minutes The minutes
         * @param hours   The hours
         * @return The time in "time compare" format
         */
        internal fun getTimeToCompare(minutes: Int, hours: Int): Int {
            return hours * 60 + minutes
        }

        fun getDayType(calendar: Calendar = Calendar.getInstance(), learnsOnFriday: Boolean): DayType {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == 7) return DayType.Saturday
            val time = calendar.clearTime().timeInMillis
            val parser = SimpleDateFormat("dd/MM/yyyy")
            for (holiday in Holidays) {
                if (holiday.isOneDay()) {
                    if (time == holiday.startTime) return DayType.Holiday
                } else {
                    if (time >= holiday.startTime && time <= holiday.endTime) return DayType.Holiday
                }
            }
            if (time >= parser.parse(Summer.start).time && time <= parser.parse(Summer.end).time) return DayType.Summer
            if (dayOfWeek == 6 && !learnsOnFriday) return DayType.Friday
            else return DayType.Regular
        }

        data class Holiday(val name: String, val start: String, val end: String = "") {
            fun isOneDay(): Boolean = end.isEmpty()

            val startTime = start.parseDate()
            val endTime = if (end.isEmpty()) startTime else end.parseDate()
            val length = daysBetween(startTime.toCalendar(), endTime.toCalendar()) + 1

            companion object {
                val Format = SimpleDateFormat("dd/MM/yyyy")

                private fun String.parseDate(): Long = Holiday.Companion.Format.parse(this).time
            }
        }

        enum class DayType {
            Holiday,
            Saturday,
            Friday,
            Summer,
            Regular
        }

    }
}