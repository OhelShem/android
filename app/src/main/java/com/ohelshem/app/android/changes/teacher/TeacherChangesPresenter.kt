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

package com.ohelshem.app.android.changes.teacher

import com.ohelshem.app.android.changes.ChangesView
import com.ohelshem.app.android.changes.IBaseChangesPresenter
import com.ohelshem.app.android.main.ScreenManager
import com.ohelshem.app.android.main.ScreenType
import com.ohelshem.app.android.utils.BasePresenter
import com.ohelshem.app.controller.info.SchoolInfo
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.toCalendar

class TeacherChangesPresenter(val storage: Storage, private val schoolInfo: SchoolInfo) : BasePresenter<ChangesView>(), IBaseChangesPresenter {
    override fun onCreate() {
        val currentLayer = storage.primaryClass?.layer ?: 9
        (view as? TeacherChangesView)?.setSelectedLayer(currentLayer)
        onLayerSelected(currentLayer)
    }

    var currentLayer: Int = 9

    fun onLayerSelected(layer: Int) {
        currentLayer = layer
        update()
    }

    override fun update() {
        hasData = false
        if (!storage.hasSchoolChanges)
            view?.onEmptyData(TimetableController.getDayType(changesDate.toCalendar(), true))
        else {
            val data = (1..schoolInfo[currentLayer]).flatMap { storage.getChangesForClass(currentLayer, it) }
            if (data.isEmpty())
                view?.onEmptyData(TimetableController.getDayType(changesDate.toCalendar(), true))
            else {
                hasData = true
                view?.setData(data)
            }
        }
    }

    override var hasData: Boolean = false
        private set

    override fun refresh(screen: ScreenManager): Boolean = screen.refresh()

    override fun launchTimetableScreen(screen: ScreenManager) = screen.setScreen(ScreenType.Timetable)

    override val changesDate: Long
        get() = storage.changesDate

    val classesAtLayer: Int
        get() = schoolInfo[currentLayer]

    override fun onDestroy() = Unit
    override fun onChoosingClass() = Unit

}