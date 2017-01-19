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

package com.ohelshem.app.android.dashboard

import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Test
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.main.ScreenManager
import com.ohelshem.app.android.main.ScreenType
import com.ohelshem.app.android.utils.BasePresenter
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.api.ApiController.UpdatedApi
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.toCalendar
import java.util.*
import java.util.concurrent.TimeUnit

class DashboardPresenter(private val storage: Storage, private val timetableController: TimetableController) : BasePresenter<DashboardView>(), ApiController.Callback {
    private val userClazz = storage.userData.clazz
    override fun onCreate() {
        update()
    }

    override fun onDestroy() = Unit

    fun update() {
        view?.apply {
            val hourData = timetableController.getHourData()
            showLessonInfo(hourData,
                    isEndOfDay = TimetableController.isEndOfDay(hourData.hour.hourOfDay, timetableController[hourData.hour.day - 1]),
                    isTomorrow = (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(hourData.timeToHour.toLong())).toCalendar().clearTime().timeInMillis >
                            Calendar.getInstance().clearTime().timeInMillis,
                    isFuture = (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(hourData.timeToHour.toLong())).toCalendar().clearTime().timeInMillis >
                            Calendar.getInstance().clearTime().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis, changes = generateChanges(hourData.hour.day, userClazz))
            showTests(testsForWeek)
        }
    }

    private fun generateChanges(day: Int, clazz: Int): List<Change>? {
        return if (storage.changesDate.toCalendar()[Calendar.DAY_OF_WEEK] == day) storage.changes?.filter { it.clazz == clazz } else null
    }

    override fun onSuccess(apis: Set<UpdatedApi>) {
        if (UpdatedApi.Tests in apis || UpdatedApi.Timetable in apis || UpdatedApi.Changes in apis) update()
    }

    override fun onFail(error: UpdateError) = Unit // Ignored

    override fun onChoosingClass() = Unit

    fun launchTestsScreen(screenManager: ScreenManager) {
        screenManager.setScreen(ScreenType.Dates, backStack = true)
    }

    fun launchTodayPlan(screenManager: ScreenManager) {
        if (storage.hasChanges(storage.userData.clazz))
            screenManager.setScreen(ScreenType.Changes, backStack = true)
        else screenManager.setScreen(ScreenType.Timetable, backStack = true)
    }

    val testsForWeek: List<Test>
        get() {
            val time = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
            val now = Date().time
            if (storage.userData.isTeacher()) {
                val primaryClass = storage.primaryClass
                if (primaryClass != null)
                    return storage.getTestsForClass(primaryClass.layer, primaryClass.clazz).filter { it.date in now..time }
                else
                    return emptyList()
            }
            return storage.tests?.filter { it.date in now..time } ?: emptyList()
        }
}