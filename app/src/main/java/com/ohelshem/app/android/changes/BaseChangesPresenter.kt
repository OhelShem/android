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

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.base.presenter.InitializerPresenter
import com.ohelshem.app.android.main.ScreenManager
import com.ohelshem.app.android.main.ScreenType
import com.ohelshem.app.controller.ApiController
import com.ohelshem.app.controller.ApiController.Api
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.toCalendar
import uy.kohesive.injekt.injectLazy

abstract class BaseChangesPresenter : MvpBasePresenter<ChangesView>(), InitializerPresenter, ApiController.Callback {
    protected val databaseController: DBController by injectLazy()
    protected val timetableController: TimetableController by injectLazy()

    override fun init() {
        update()
    }

    fun update() {
        if (databaseController.areChangesUpdated()) {
            val changes = databaseController.changes
            if (changes == null || !changes.isConsideredAsChanges())
                view?.onEmptyData(TimetableController.getDayType(changesDate.toCalendar(), timetableController.learnsOnFriday))
            else
                view?.setData(changes.filterNeeded())
        } else view?.onError(UpdateError.NoData)
    }

    override fun onSuccess(apis: List<Api>) {
        if (Api.Changes in apis)
            update()
    }

    override fun onFail(error: UpdateError) {
        view?.let {
            if (it.isShowingData) {
                if (error == UpdateError.Exception)
                    it.onError(error)
            } else if (error == UpdateError.Connection && databaseController.areChangesUpdated()) {
                update()
            } else
                it.onError(error)
        }
    }

    fun refresh(screen: ScreenManager) = screen.refresh()

    fun launchTimetableScreen(screen: ScreenManager) = screen.setScreen(ScreenType.Timetable)

    val changesDate: Long
        get() = databaseController.changesDate


    abstract fun List<Change>.isConsideredAsChanges(): Boolean

    abstract fun List<Change>.filterNeeded(): List<Change>
}