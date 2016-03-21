/*
 * Copyright 2016 Yoav Sternberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ohelshem.app.android.timetable

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter
import com.ohelshem.api.model.Hour
import com.ohelshem.app.android.base.presenter.InitializerPresenter
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.getDay
import com.ohelshem.app.getHour
import com.ohelshem.app.model.OverrideData
import uy.kohesive.injekt.injectLazy
import java.util.*

class TimetablePresenter : MvpBasePresenter<TimetableView>(), InitializerPresenter {
    private val databaseController: DBController by injectLazy()
    private val timetableController: TimetableController by injectLazy()
    var isEditModeOn: Boolean = false

    override fun init() {
        view?.showDayTimetable()
        currentDay = today
        setDay(currentDay)
    }

    fun setDay(day: Int) {
        currentDay = day
        if (day == 0)
            view?.showWeekTimetable()
        else
            view?.showDayTimetable()
        view?.setDay(currentDay, Array(timetableController.size) { timetableController[it] })
    }


    fun startEdit(hour: Hour, day: Int, position: Int) {
        if (isEditModeOn)
            view?.showEditScreen(hour, day, position)
    }

    fun edit(hour: Hour, day: Int, position: Int, newLesson: String, newTeacher: String, editAll: Boolean) {
        if (newLesson.isNotEmpty() || newTeacher.isNotEmpty()) {
            if (!editAll) {
                var overrides = databaseController.overrides
                val index = overrides.indexOfFirst { it.day == day && it.hour == position }
                val override = OverrideData(day, position, newLesson.or(hour.name), newTeacher.or(hour.teacher))
                if (index < 0) overrides += override
                else overrides[index] = override
                databaseController.overrides = overrides
            } else {
                val overrides = databaseController.overrides.toCollection(LinkedList())
                val name = hour.name
                for (dayOfWeek in 0 until timetableController.size) {
                    timetableController[dayOfWeek].forEachIndexed { i, hour ->
                        if (hour.name == name) {
                            val index = overrides.indexOfFirst { it.day == dayOfWeek && it.hour == i }
                            val override = OverrideData(dayOfWeek, i, newLesson.or(hour.name), newTeacher.or(hour.teacher))
                            if (index < 0) overrides += override
                            else overrides[index] = override
                        }
                    }
                }
                databaseController.overrides = overrides.toTypedArray()
            }
        }
        view?.flush()
        setDay(currentDay)
    }

    private fun String.or(data: String) = if (isEmpty()) data else this

    val weekDays: Int
        get() = timetableController.size


    var currentDay: Int = -1
        private set

    private val today: Int
        get() {
            val weekDays = if (timetableController.learnsOnFriday) 5 else 4
            var day: Int
            if (databaseController.areChangesUpdated()) {
                val nextLessons = timetableController.getHourData()
                day = nextLessons.hour.day + 1
            } else {
                var temp = Calendar.getInstance().getDay()
                if (getHour() >= 21) temp++
                if (temp > weekDays + 1)
                    temp = 1
                day = temp
            }
            return day
        }
}