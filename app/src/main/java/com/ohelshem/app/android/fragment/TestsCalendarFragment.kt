/*
 * Copyright 2010-2015 Yoav Sternberg.
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

package com.ohelshem.app.android.fragment

import com.marcohc.robotocalendar.RobotoCalendarView.RobotoCalendarListener
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.hide
import com.ohelshem.app.android.show
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.DBController
import com.ohelshem.api.model.Test
import kotlinx.android.synthetic.main.item_1_line.*
import kotlinx.android.synthetic.main.tests_calendar_fragment.*
import uy.kohesive.injekt.injectLazy
import java.text.SimpleDateFormat
import java.util.*

/**
 * A fragment for showing tests in a calendar view.
 */
class TestsCalendarFragment : BaseFragment(), RobotoCalendarListener {
    override val layoutId: Int = R.layout.tests_calendar_fragment
    protected val databaseController: DBController by injectLazy()
    private val dates by lazy { databaseController.tests!!.map { it.date to it }.toMap() }
    private var calendar = now()
    private val now = Date().time

    override fun init() {
        calendarView.setRobotoCalendarListener(this)
        calendarView.markDayAsCurrentDay(Date())
        if (dates.size > 0) {
            update(databaseController.tests!!.firstOrNull { now <= it.date })
        }
    }

    override fun onRightButtonClick() {
        calendar.add(Calendar.MONTH, 1)
        calendarView.initializeCalendar(calendar)
        updateEvents()
    }

    override fun onDateSelected(date: Date) {
        calendarView.markDayAsSelectedDay(date)
        val test = dates[date.clearTime().time]
        if (test == null) clear()
        else update(test)
    }

    override fun onLeftButtonClick() {
        calendar.add(Calendar.MONTH, -1)
        calendarView.initializeCalendar(calendar)
        updateEvents()
    }

    private fun update(test: Test?) {
        if (test != null) {
            data.show()
            title.text = test.content
            extra.text = DateFormat.format(Date(test.date))
            if (now > test.date)
                indicator.text = "V"
            else
                indicator.text = ""
        } else clear()
    }

    private fun clear() {
        data.hide()
    }


    private fun now(): Calendar = Calendar.getInstance().clearTime().apply { set(Calendar.DAY_OF_MONTH, 1) }

    private fun updateEvents() {
        val start = calendar.timeInMillis
        val end = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, 1); add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
        dates.values.forEach {
            if (it.date >= start && it.date <= end) {
                calendarView.markFirstUnderlineWithStyle(R.color.colorPrimary, Date(it.date))
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            calendar = now()
            calendarView.initializeCalendar(calendar)
            updateEvents()
        }
    }

    companion object {
        private val DateFormat = SimpleDateFormat("dd/MM/yy")
    }
}