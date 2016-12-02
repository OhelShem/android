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
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.main.ScreenManager
import com.ohelshem.app.android.main.ScreenType
import com.ohelshem.app.android.utils.BasePresenter
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.api.ApiController.UpdatedApi
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.getHour
import com.ohelshem.app.isToday
import com.ohelshem.app.isTomorrow
import com.ohelshem.app.toCalendar

abstract class BaseChangesPresenter(protected val storage: Storage, protected val timetableController: TimetableController) : BasePresenter<ChangesView>(), ApiController.Callback {
    var lastChanges: List<Change>? = null

    override fun onCreate() {
        update()
    }

    override fun onDestroy() = Unit

    fun update() {
        if (areChangesUpdated()) {
            val changes = storage.changes
            if (changes == null || !changes.isConsideredAsChanges()) {
                lastChanges = null
                view?.onEmptyData(TimetableController.getDayType(changesDate.toCalendar(), timetableController.learnsOnFriday))
            }
            else {
                changes.filterNeeded().let {
                    lastChanges = it
                    view?.setData(it)
                }
            }
        } else view?.onError(UpdateError.NoData)
    }

    override fun onSuccess(apis: Set<UpdatedApi>) {
        if (UpdatedApi.Changes in apis)
            update()
    }

    override fun onFail(error: UpdateError) {
        view?.let {
            if (it.isShowingData) {
                if (error == UpdateError.Exception)
                    it.onError(error)
            } else if (error == UpdateError.Connection && areChangesUpdated()) {
                update()
            } else
                it.onError(error)
        }
    }

    override fun onChoosingClass() {
        //FIXME implement code
    }

    fun refresh(screen: ScreenManager) = screen.refresh()

    fun launchTimetableScreen(screen: ScreenManager) = screen.setScreen(ScreenType.Timetable)

    val changesDate: Long
        get() = storage.changesDate

    protected fun areChangesUpdated() = changesDate.toCalendar().let { (it.isToday() && getHour() < 21) || it.isTomorrow() }

    val hasData: Boolean
        get() = lastChanges?.isNotEmpty() ?: false

    abstract fun List<Change>.isConsideredAsChanges(): Boolean

    abstract fun List<Change>.filterNeeded(): List<Change>
}