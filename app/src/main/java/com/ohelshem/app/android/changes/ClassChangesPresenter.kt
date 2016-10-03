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

package com.ohelshem.app.android.changes

import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Hour
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.getDay
import com.ohelshem.app.toCalendar

class ClassChangesPresenter(storage: Storage, timetableController: TimetableController) : BaseChangesPresenter(storage, timetableController) {
    override fun List<Change>.isConsideredAsChanges(): Boolean {
        val clazz = storage.userData.clazz
        return any { it.clazz == clazz }
    }

    override fun List<Change>.filterNeeded(): List<Change> {
        val clazz = storage.userData.clazz
        return filter { it.clazz == clazz }
    }

    val dailyTimetable: Array<Hour>
        get() = getTimetableForDay(changesDate.toCalendar().getDay() - 1)

    private fun getTimetableForDay(day: Int): Array<Hour> {
        if (day >= timetableController.size) return emptyArray()
        else return timetableController[day]
    }
}