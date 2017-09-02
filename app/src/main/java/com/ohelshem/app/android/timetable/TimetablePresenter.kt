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

import com.ohelshem.api.model.ClassInfo
import com.ohelshem.api.model.Hour
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.utils.BasePresenter
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.api.ApiController.UpdatedApi
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.model.OverrideData
import com.ohelshem.app.removeAtIfPositive

class TimetablePresenter(private val storage: Storage, private val userTimetableController: TimetableController,
                         private val teacherTimetableControllerGenerator: (layer: Int, clazz: Int) -> TimetableController, isEditModeSupported: Boolean) :
        BasePresenter<TimetableView>(), ApiController.Callback {
    //region Lifecycle
    override fun onDestroy() = Unit

    override fun onCreate() {
        if (cachedClass != currentClass) {
            invalidateCache()
        }
        view?.flushMenu()
        currentDay = if (timetableController.hasData) today else TimetableLayout.Day_Week
        setDay(currentDay)
    }
    //endregion

    //region Api Update
    override fun onSuccess(apis: Set<UpdatedApi>) {
        if (UpdatedApi.Timetable in apis) {
            view?.flush()
            setDay(currentDay)
        }
    }

    override fun onFail(error: UpdateError) = Unit
    //endregion

    //region Edit
    var isEditModeSupported: Boolean = isEditModeSupported
        private set

    var isEditModeEnabled: Boolean = false
        set(value) {
            field = isEditModeSupported && value
        }

    fun startEdit(hour: Hour, day: Int, position: Int) {
        if (isEditModeEnabled)
            view?.showEditScreen(hour, day, position, storage.overrides.any { it.day == day && it.hour == position })
    }

    fun returnToDefault(hour: Hour, day: Int, position: Int, editAll: Boolean) {
        if (!editAll) {
            storage.overrides = storage.overrides.toMutableList().apply {
                removeAtIfPositive(indexOfFirst { it.day == day && it.hour == position })
            }
        } else {
            storage.overrides = storage.overrides.toMutableList().apply { removeAll { it.newName == hour.name } }
        }
        view?.flush()
        setDay(currentDay)
    }

    fun edit(hour: Hour, day: Int, position: Int, newLesson: String, newTeacher: String, newRoom: Int, editAll: Boolean) {
        if (newLesson.isNotEmpty() || newTeacher.isNotEmpty() || newRoom >= 0) {
            if (!editAll) {
                val overrides = storage.overrides.toMutableList()
                val index = overrides.indexOfFirst { it.day == day && it.hour == position }
                val override = OverrideData(day, position, newLesson.or(hour.name), newTeacher.or(hour.teacher), if (newRoom < 0) 0 else newRoom)
                if (index < 0) overrides.add(override)
                else overrides[index] = override
                storage.overrides = overrides
            } else {
                val overrides = storage.overrides.toMutableList()
                val name = hour.name
                for (dayOfWeek in 0 until timetableController.size) {
                    timetableController[dayOfWeek].forEachIndexed { i, newHour ->
                        if (newHour.name == name) {
                            val index = overrides.indexOfFirst { it.day == dayOfWeek && it.hour == i }
                            val override = OverrideData(dayOfWeek, i, newLesson.or(newHour.name), newTeacher.or(newHour.teacher),
                                    if (newRoom >= 0) newRoom else if (index > 0) overrides[index].newRoom else 0 )
                            if (index < 0) overrides += override
                            else overrides[index] = override
                        }
                    }
                }
                storage.overrides = overrides
            }
        }
        view?.flush()
        setDay(currentDay)
    }
    //endregion

    private var cachedClass: ClassInfo? = null
    private var cachedTimetableController: TimetableController? = null
    val timetableController: TimetableController
        get() {
            val currentClass = currentClass ?: return userTimetableController
            if (cachedTimetableController == null) {
                cachedClass = currentClass
                cachedTimetableController = teacherTimetableControllerGenerator(currentClass.layer, currentClass.clazz)
            }
            return cachedTimetableController!!
        }

    private fun invalidateCache() {
        cachedTimetableController = null
        cachedClass = null
    }

    fun setDay(day: Int) {
        val timetableController = timetableController
        if (timetableController.hasData) {
            currentDay = day
            if (day == 0) {
                view?.setDay(currentDay, Array(timetableController.size) { timetableController[it] })
            } else {
                val daysLearning = this.daysLearning
                var position = day - 1
                while (!daysLearning[position]) {
                    position++
                    if (position >= daysLearning.size)
                        position = 0
                }
                currentDay = position + 1
                view?.setDay(currentDay, Array(timetableController.size) { timetableController[it] })
            }
        } else {
            //FIXME
        }
    }

    private fun String.or(data: String) = if (isEmpty()) data else this

    val daysLearning: BooleanArray
        get() {
            val days = BooleanArray(6)

            val timetableController = timetableController

            (0 until timetableController.size)
                    .filter { timetableController[it].isNotEmpty() }
                    .forEach { days[it] = true }
            return days
        }

    var currentDay: Int = -1
        private set

    private val today: Int
        get() = timetableController.getHourData().hour.day

    private val isTeacher: Boolean
        get() = !storage.isStudent()

    val groupFormatting: Boolean
        get() = currentClass == null && isTeacher

    override fun onReselected() {
        if (currentDay == 0 && timetableController.hasData)
            setDay(today)
        else
            setDay(0)
    }

    override fun onChoosingClass() {
        isEditModeSupported = currentClass == null
        cachedTimetableController = null
        if (!isEditModeSupported)
            view?.disableEditMode()

        view?.flush()
        view?.flushMenu()
        setDay(currentDay)
    }
}