package com.ohelshem.app.controller.timetable

import android.graphics.Color
import com.ohelshem.api.model.Hour
import com.ohelshem.app.*
import com.ohelshem.app.model.HourData
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

    /**
     * Get hour data. Note: [day] is 1 based index.
     */
    fun getHourData(day: Int = getIsraelCalendar().get(Calendar.DAY_OF_WEEK),
                    hour: Int = getIsraelCalendar().get(Calendar.HOUR_OF_DAY),
                    minutesNow: Int = getIsraelCalendar().get(Calendar.MINUTE)): HourData

    val hasDoneLearningToday: Boolean

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
                7 to 55,
                8 to 45,
                9 to 35,
                10 to 35,
                11 to 25,
                12 to 30,
                13 to 20,
                14 to 15,
                15 to 5,
                15 to 55,
                16 to 45,
                17 to 35

        )

        val Holidays = arrayOf(
                Holiday("ראש השנה", "20/09/2017", "22/09/2017"),
                Holiday("יום כיפור", "29/09/2017", "30/09/2017"),
                Holiday("סוכות", "04/10/2017", "13/10/2017"),
                Holiday("חנוכה", "14/12/2017", "20/12/2017"),
                Holiday("פורים", "28/02/2018", "02/03/2018"),
                Holiday("פסח", "22/03/2018", "07/04/2018"),
                Holiday("יום העצמאות", "19/04/2018"),
                Holiday("ל\"ג בעומר", "03/05/2018"),
                Holiday("שבועות", "19/05/2018", "21/05/2018"))

        val StartOfTheYear: Date
            get() {
                val cal = getIsraelCalendar()
                var year = cal[Calendar.YEAR]
                if (cal[Calendar.MONTH] < Calendar.SEPTEMBER)
                    year--
                return Holiday.Format.parse("01/09/$year")
            }

        val Summer: Holiday
            get() {
                val cal = getIsraelCalendar()
                var year = cal[Calendar.YEAR]
                if (cal[Calendar.MONTH] >= Calendar.SEPTEMBER)
                    year++
                return Holiday("קיץ", "21/06/$year", "31/08/$year")
            }


        fun getStartOfHour(hour: Int): String = DayHours[hour * 2 - 2]

        fun getEndOfHour(hour: Int): String = DayHours[hour * 2 - 1]

        fun isEndOfDay(hour: Int, day: Array<Hour>): Boolean {
            return hour >= day.indexOfLast { !it.isEmpty() }
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

        fun getDayType(calendar: Calendar = getIsraelCalendar(), learnsOnFriday: Boolean): DayType {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == 7) return DayType.Saturday
            val time = calendar.clearTime().timeInMillis
            for (holiday in Holidays) {
                if (holiday.isOneDay()) {
                    if (time == holiday.startTime) return DayType.Holiday
                } else {
                    if (time >= holiday.startTime && time <= holiday.endTime) return DayType.Holiday
                }
            }
            if (time in dateFormat.parse(Summer.start).time..dateFormat.parse(Summer.end).time) return DayType.Summer
            if (dayOfWeek == 6 && !learnsOnFriday) return DayType.Friday
            else return DayType.Regular
        }

        fun getHoliday(calendar: Calendar = getIsraelCalendar()): Holiday? {
            val time = calendar.clearTime().timeInMillis
            for (holiday in Holidays) {
                if (holiday.isOneDay()) {
                    if (time == holiday.startTime) return holiday
                } else {
                    if (time >= holiday.startTime && time <= holiday.endTime) return holiday
                }
            }
            if (time >= dateFormat.parse(Summer.start).time && time <= dateFormat.parse(Summer.end).time) return Summer
            return null
        }

        data class Holiday(val name: String, val start: String, val end: String = "") {
            fun isOneDay(): Boolean = end.isEmpty()

            val startTime = start.parseDate()
            val endTime = if (end.isEmpty()) startTime else end.parseDate()
            val length = daysBetween(startTime.toCalendar(), endTime.toCalendar()) + 1

            companion object {
                val Format = dateFormat

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

        val ColorEmpty = Color.parseColor("#B0BEC5")

    }
}