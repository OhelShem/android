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

package com.ohelshem.app.android.tests

import com.marcohc.robotocalendar.RobotoCalendarView
import com.ohelshem.api.model.Test
import com.ohelshem.app.android.base.fragment.BaseMvpFragment
import com.ohelshem.app.android.util.hide
import com.ohelshem.app.android.util.show
import com.ohelshem.app.clearTime
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.item_1_line.*
import kotlinx.android.synthetic.main.tests_calendar_fragment.*
import java.text.SimpleDateFormat
import java.util.*

class TestsCalendarFragment: BaseMvpFragment<TestsChildView, TestsChildPresenter>(), TestsChildView, RobotoCalendarView.RobotoCalendarListener {
    override val layoutId: Int = R.layout.tests_calendar_fragment
    private var dates: Map<Long, Test>? = null
    private var calendar = now()
    private val now = Date().time

    override fun createPresenter(): TestsChildPresenter = TestsChildPresenter()

    override fun init() {
        calendarView.setRobotoCalendarListener(this)
        calendarView.markDayAsCurrentDay(Date())
        (parentFragment as? TestsView)?.onFragmentLoaded()
    }

    override fun update(tests: List<Test>) {
        dates = tests.map { it.date to it }.toMap()
        update(tests.firstOrNull { now <= it.date })
    }

    override fun onRightButtonClick() {
        calendar.add(Calendar.MONTH, 1)
        calendarView.initializeCalendar(calendar)
        updateEvents()
    }

    override fun onDateSelected(date: Date) {
        if (dates != null) {
            calendarView.markDayAsSelectedDay(date)
            val test = dates!![date.clearTime().time]
            if (test == null) clear()
            else update(test)
        }
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
            extra.text = TestsDateFormat.format(Date(test.date))
            if (now > test.date)
                indicator.text = "✓"
            else
                indicator.text = ""
        } else clear()
    }

    private fun clear() {
        data.hide()
    }

    private fun updateEvents() {
        if (dates != null) {
            val start = calendar.timeInMillis
            val end = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, 1); add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
            dates!!.values.forEach {
                if (it.date >= start && it.date <= end) {
                    calendarView.markFirstUnderlineWithStyle(R.color.colorPrimary, Date(it.date))
                }
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
    private fun now(): Calendar = Calendar.getInstance().clearTime().apply { set(Calendar.DAY_OF_MONTH, 1) }

    companion object {
        private val TestsDateFormat = SimpleDateFormat("dd/MM/yy")
    }
}