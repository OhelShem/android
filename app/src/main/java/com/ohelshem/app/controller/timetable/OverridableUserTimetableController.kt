package com.ohelshem.app.controller.timetable

import com.ohelshem.api.model.Hour
import com.ohelshem.app.controller.storage.SharedStorage
import com.ohelshem.app.model.HourData
import com.ohelshem.app.model.OverrideData
import com.ohelshem.app.model.WrappedHour
import org.jetbrains.anko.collections.forEachReversedWithIndex
import java.util.*

class OverridableUserTimetableController(internal val timetableController: BaseTimetableController, private val storage: SharedStorage)
: TimetableController by timetableController {
    private var overrideTimetable: Array<Array<Hour>>? = null

    init {
        storage.attachOverridesListener(1) { onUpdate(it) }
        timetableController.timetableUpdatedCallback = {
            onUpdate(storage.overrides)
        }
    }

    override fun get(day: Int, hour: Int): Hour = this[day][hour]

    override fun get(day: Int): Array<Hour> {
        val timetableForDay = overrideTimetable!![day]
        var temp = overrideTimetable!![day].toList()
        var shouldNotExit = true
        var i = timetableForDay.size - 1
        do {
            if (i >= 0 && timetableForDay[i].isEmpty()) {
                temp = temp.dropLast(1)
            } else shouldNotExit = false
            i--
        } while (shouldNotExit)
        if (temp.isEmpty()) return timetableForDay
        else return temp.toTypedArray()
    }

    override fun getHourData(day: Int, hour: Int, minutesNow: Int): HourData = timetableController.getHourDataFromTimetable(day, hour, minutesNow, overrideTimetable!!)


    fun onUpdate(data: Array<OverrideData>) {
        val daysOfWeek = timetableController.size - 1
        val goodOverrides = data.toMutableList()
        data.forEachReversedWithIndex { i, overrideData ->
            if (overrideData.day > daysOfWeek || overrideData.hour >= timetableController[overrideData.day].size)
                goodOverrides.removeAt(i)
        }
        if (goodOverrides.size != data.size) {
            storage.overrides = goodOverrides.toTypedArray()
        } else {
            val colors = timetableController.colors
            val lessons = HashMap<String, Int>(20)
            var c = 0
            val timetable = ArrayList<ArrayList<Hour>>(timetableController.size)
            for (day in 0..timetableController.size - 1) {
                timetable.add(ArrayList<Hour>(timetableController[day].size).apply {
                    for (hour in 0..timetableController[day].size - 1) {
                        val original = timetableController[day][hour]
                        val override = data[day, hour]
                        val lesson = if (override == null) original.name else override.newName
                        val teacher = if (override == null) original.teacher else override.newTeacher
                        if (lesson.isBlank()) {
                            add(WrappedHour("", "", original.name, original.teacher, TimetableController.ColorEmpty))
                        } else {
                            var color = lessons[lesson]
                            if (color == null) {
                                c++
                                if (c == colors.size) c = 0
                                color = colors[c]
                                lessons[lesson] = color
                            }
                            if (override == null)
                                add(Hour(lesson, teacher, color))
                            else add(WrappedHour(override.newName, override.newTeacher, original.name, original.teacher, color))
                        }
                    }
                })
            }
            if (timetable.size == 6 && timetable[5].size == 0) timetable.removeAt(5)
            overrideTimetable = timetable.map { it.toTypedArray() }.toTypedArray()
        }
    }

    operator private fun Array<OverrideData>.get(day: Int, hour: Int): OverrideData? {
        return firstOrNull { it.day == day && it.hour == hour }
    }

}