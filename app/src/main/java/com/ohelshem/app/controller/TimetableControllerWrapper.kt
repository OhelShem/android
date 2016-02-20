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

import android.graphics.Color
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.model.HourData
import com.ohelshem.app.model.OverrideData
import com.ohelshem.app.model.Updatable
import com.ohelshem.app.model.WrappedHour
import org.jetbrains.anko.collections.forEachReversedWithIndex
import uy.kohesive.injekt.injectLazy
import java.util.*

/**
 * A wrapper for [TimetableController] with support for override data.
 * Getting updated from databaseController callback.
 */
class TimetableControllerWrapper(internal val timetableController: BaseTimetableController) : TimetableController by timetableController, Updatable<Array<OverrideData>> {
    private val databaseController: DBController by injectLazy()
    private var overrideTimetable: Array<Array<Hour>>? = null

    init {
        databaseController.attachOverridesListener(1, this)
        timetableController.timetableUpdatedCallback = {
            onUpdate(databaseController.overrides)
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


    override fun onUpdate(data: Array<OverrideData>) {
        val daysOfWeek = timetableController.size - 1
        val goodOverrides = data.toMutableList()
        data.forEachReversedWithIndex { i, overrideData ->
            if (overrideData.day > daysOfWeek || overrideData.hour > timetableController[overrideData.day].size - 1) goodOverrides.removeAt(i)
        }
        if (goodOverrides.size != data.size) {
            databaseController.overrides = goodOverrides.toTypedArray()
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
                            add(WrappedHour("", "", original.name, original.teacher, ColorEmpty))
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

    override fun onError(error: UpdateError) {
        // Ignored
    }

    operator private fun Array<OverrideData>.get(day: Int, hour: Int): OverrideData? {
        return firstOrNull { it.day == day && it.hour == hour }
    }

    companion object {
        val ColorEmpty = Color.parseColor("#B0BEC5")
    }
}