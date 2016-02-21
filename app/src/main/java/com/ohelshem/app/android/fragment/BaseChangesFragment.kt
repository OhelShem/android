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

package com.ohelshem.app.android.fragment

import android.support.v7.widget.RecyclerView
import com.vlonjatg.progressactivity.ProgressActivity
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.controller.TimetableController.Companion.DayType
import com.ohelshem.app.getDay
import com.ohelshem.app.model.ApiUpdatable
import com.ohelshem.app.model.DrawerActivity
import com.ohelshem.app.toCalendar
import com.ohelshem.app.controller.ApiController
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.UpdateError
import uy.kohesive.injekt.injectLazy
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseChangesFragment : BaseFragment(), ApiUpdatable<List<Change>> {
    protected abstract val emptyLayout: ProgressActivity
    protected abstract val recyclerView: RecyclerView
    protected val databaseController: DBController by injectLazy()
    protected val timetableController: TimetableController by injectLazy()
    private val weekDays by lazy { resources.getStringArray(R.array.week_days) }
    private val day by lazy { getString(R.string.day) }

    override val api: ApiController.Api? = ApiController.Api.Changes

    override fun onUpdate() {
        iffData()
    }

    override fun onError(error: UpdateError) {
        when (error) {
            UpdateError.Connection -> {
                if (databaseController.isCacheUpdated()) {
                    iffData()
                } else {
                    recyclerView.adapter = null
                    drawerActivity.setToolbarTitle(getString(R.string.changes))
                    emptyLayout.showError(activity.drawableRes(R.drawable.ic_sync_problem), getString(R.string.no_connection), getString(R.string.no_connection_subtitle),
                            getString(R.string.try_again)) {
                        drawerActivity.refresh()
                    }
                }
            }
            UpdateError.NoData -> {
                recyclerView.adapter = null
                drawerActivity.setToolbarTitle(getString(R.string.changes))
                emptyLayout.showError(activity.drawableRes(R.drawable.ic_sync_problem), getString(R.string.no_data), getString(R.string.no_data_subtitle),
                        getString(R.string.refresh)) {
                    drawerActivity.refresh()
                }
            }

            else -> {
                recyclerView.adapter = null
                setTitle()
                emptyLayout.showError(activity.drawableRes(R.drawable.ic_error), getString(R.string.general_error), getString(R.string.try_again), getString(R.string.try_again)) {
                    drawerActivity.refresh()
                }
            }
        }
    }

    protected fun noData() {
        recyclerView.adapter = null
        if (emptyLayout.isError)
            emptyLayout.showContent()
        setTitle()
        val dayType = timetableController.getDayType(databaseController.changesDate.toCalendar())
        if (dayType == DayType.Holiday || dayType == DayType.Summer) {
            emptyLayout.showEmpty(activity.drawableRes(R.drawable.ic_beach), getString(R.string.holiday_today), getString(R.string.holiday_today_subtitle))
        } else {
            val day = databaseController.changesDate.toCalendar().getDay()
            if (day == 7)
                emptyLayout.showEmpty(activity.drawableRes(R.drawable.ic_beach), getString(R.string.shabat_today), getString(R.string.shabat_today_subtitle))
            else if (!timetableController.learnsOnFriday && day == 6)
                emptyLayout.showEmpty(activity.drawableRes(R.drawable.ic_beach), getString(R.string.friday_today), getString(R.string.friday_today_subtitle))
            else
                emptyLayout.showError(activity.drawableRes(R.drawable.ic_error), getString(R.string.no_changes), getString(R.string.no_changes_subtitle), getString(R.string.go_to_timetable)) {
                    drawerActivity.setFragment(DrawerActivity.Companion.FragmentType.Timetable)
                }
        }
    }

    protected fun setTitle() {
        val data = databaseController.changesDate
        drawerActivity.setToolbarTitle(day + " " + weekDays[data.toCalendar().getDay() - 1] + " " + DataFormat.format(Date(data)))
    }

    protected fun iffData() {
        val changes = databaseController.changes
        if (changes != null) onUpdate(changes)
        else noData()
    }

    companion object {
        private val DataFormat = SimpleDateFormat("dd/MM")
    }
}