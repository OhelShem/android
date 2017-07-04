package com.ohelshem.app.controller.timetable

import com.ohelshem.api.model.Hour
import com.ohelshem.app.getIsraelCalendar
import com.ohelshem.app.model.HourData
import com.ohelshem.app.model.NumberedHour
import com.ohelshem.app.model.WrappedHour
import java.util.*
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY

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
        protected set

    override var colors: IntArray = intArrayOf()

    override fun get(day: Int): Array<Hour> = timetable[day]

    override fun get(day: Int, hour: Int): Hour = timetable[day][hour]

    fun getHourDataFromTimetable(day: Int, hour: Int, minutesNow: Int, timetable: Array<Array<Hour>>): HourData = getHourData(day, hour, minutesNow, timetable)!!

    override final fun getHourData(day: Int, hour: Int, minutesNow: Int): HourData = getHourData(day, hour, minutesNow, timetable)!!

    /**
     * **Note:** Day is 1 based index. SUNDAY = 1, SATURDAY = 7.
     */
    fun getHourData(day: Int, hour: Int, minutes: Int, timetable: Array<Array<Hour>>, startingDay: Int = day, isStartingNow: Boolean = true): HourData? {
        /*
         * Saturday
         * Friday - for those who doesn't learn on friday.
         -> Sunday
         */
        if (day > timetable.size) {
            return stillRunning(day, startingDay, isStartingNow) therefore { getHourData(SUNDAY, hour, minutes, timetable, startingDay, isStartingNow = false) }
        }

        val timeNow = TimetableController.getTimeToCompare(minutes, hour)
        val today = timetable[day - 1]

        /*
         * Today is empty.
         * After all the hours of today
         -> Tomorrow
         */
        if (today.all(Hour::isEmpty) || (day == startingDay && timeNow >= getLessonTimeToCompare(lastHourOfDay(today), false))) {
            return stillRunning(day, startingDay, isStartingNow) therefore { getHourData(day.plusDay(), hour, minutes, timetable, startingDay, isStartingNow = false) }
        }

        for (schoolHour in 0..lastHourOfDay(today)) {
            val h = today[schoolHour]
            if (h.isEmpty()) continue
            else if (day != startingDay) {
                // Therefore timeNow doesn't matter since it is not the same day
                var days = 0
                var newDay = startingDay
                do {
                    days++
                    newDay = newDay.plusDay()
                } while (newDay != day)
                days--

                val minutesLeft = getLessonTimeToCompare(schoolHour) + (days * 60 * 24) + (60 * 24 - timeNow)
                val now = NumberedHour(h, day, schoolHour, h.room())
                val nextHour = if (schoolHour + 1 != today.size) {
                    NumberedHour(today[schoolHour + 1], day, schoolHour + 1, today[schoolHour + 1].room())
                } else {
                    timetable.getFirstExisting(day.plusDay())
                }
                return HourData(now, nextHour!!, minutesLeft, 0, isBefore = true)

            } else if (getLessonTimeToCompare(schoolHour) > timeNow) {
                // lesson is today but in the future
                val minutesLeft = getLessonTimeToCompare(schoolHour) - timeNow
                val now = NumberedHour(h, day, schoolHour, h.room())
                val nextHour = if (schoolHour + 1 != today.size) {
                    NumberedHour(today[schoolHour + 1], day, schoolHour + 1, today[schoolHour + 1].room())
                } else {
                    timetable.getFirstExisting(day.plusDay())
                }

                return HourData(now, nextHour!!, minutesLeft, 0, isBefore = true)
            } else if (getLessonTimeToCompare(schoolHour, false) > timeNow) {
                // lesson is now
                val minutesLeft = getLessonTimeToCompare(schoolHour, start = false) - timeNow
                val now = NumberedHour(h, day, schoolHour, h.room())
                val nextHour = if (schoolHour + 1 != today.size) {
                    NumberedHour(today[schoolHour + 1], day, schoolHour + 1, today[schoolHour + 1].room())
                } else {
                    timetable.getFirstExisting(day.plusDay())
                }

                return HourData(now, nextHour!!, minutesLeft, HourData.HourLength - minutesLeft, isBefore = false)
            }
        }
        return null
    }

    override val hasDoneLearningToday: Boolean
        get() {
            val cal = getIsraelCalendar()
            val today = cal[Calendar.DAY_OF_WEEK]
            if (today > timetable.size || timetable[today - 1].isEmpty()) return true
            return TimetableController.getTimeToCompare(cal[Calendar.MINUTE], cal[Calendar.HOUR_OF_DAY]) >= getLessonTimeToCompare(lastHourOfDay(timetable[today - 1]), start = false)
        }

    private fun Array<Array<Hour>>.getFirstExisting(day: Int, startingDay: Int = day, isStartingNow: Boolean = false): NumberedHour? {
        if (day > timetable.size || timetable[day - 1].isEmpty()) {
            return stillRunning(day, startingDay, isStartingNow) therefore { getFirstExisting(day.plusDay(), startingDay, isStartingNow = false) }
        }

        val hour = timetable[day - 1][0]
        return NumberedHour(hour, day, 0, hour.room())
    }

    private fun Hour.room() = (this as? WrappedHour)?.room ?: 0

    private fun stillRunning(day: Int, startingDay: Int, isStartingNow: Boolean) = !(day == startingDay && !isStartingNow)
    private fun Int.plusDay() = if (this == SATURDAY) SUNDAY else this + 1

    private inline infix fun <K> Boolean.therefore(func: () -> K): K? = if (!this) null else func()

    companion object {
        private fun lastHourOfDay(day: Array<Hour>): Int = day.indexOfLast { !it.isEmpty() }

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
            return TimetableController.HoursToCompare[if (start) lessonNum * 2 else lessonNum * 2 + 1]
        }
    }
}