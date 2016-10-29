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

package com.ohelshem.app.android.dates.calendar

import android.content.res.Configuration
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Test
import com.ohelshem.app.android.dates.TestsPresenter
import com.ohelshem.app.android.dates.TestsView
import com.ohelshem.app.android.holidays.calendar.HolidayDecorator
import com.ohelshem.app.android.primaryColor
import com.ohelshem.app.android.show
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.android.utils.view.OneDayDecorator
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.controller.timetable.TimetableController.Companion.Holiday
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.calendar_fragment.*
import java.text.SimpleDateFormat
import java.util.*

class DatesCalendarFragment : BaseMvpFragment<TestsView, TestsPresenter>(), TestsView {
    override val layoutId: Int = R.layout.calendar_fragment
    override fun createPresenter(): TestsPresenter = with(kodein()) { TestsPresenter(instance()) }

    private var tests: List<Test> = emptyList()

    private val now = Date().time

    override fun init() {
        calendarView.currentDate = CalendarDay.today()
        calendarView.isPagingEnabled = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        calendarView.state().edit().apply {
            setMinimumDate(CalendarDay.from(TimetableController.StartOfTheYear))
            setMaximumDate(CalendarDay.from(Date(TimetableController.Summer.endTime)))
        }.commit()
        calendarView.setOnDateChangedListener { materialCalendarView, calendarDay, selected ->
            var hasFound = false
            val time = calendarDay.date.time
            for (test in tests) {
                if (test.date == time) {
                    update(test)
                    hasFound = true
                    break
                }
            }
            if (!hasFound) {
                for (holiday in TimetableController.Holidays) {
                    if (holiday.isOneDay()) {
                        if (time == holiday.startTime) {
                            update(holiday)
                            break
                        }
                    } else {
                        if (time >= holiday.startTime && time <= holiday.endTime) {
                            update(holiday)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun update(test: Test?) {
        if (test != null) {
            title.text = test.content
            extra.text = TestsDateFormat.format(Date(test.date))
            if (now > test.date)
                indicator.text = "✓"
            else
                indicator.text = ""
        } else clear()
    }

    private fun update(holiday: Holiday?) {
        if (holiday != null) {
            data.show()
            title.text = holiday.name
            if (holiday.isOneDay())
                extra.text = holiday.start
            else
                extra.text = holiday.start + " - " + holiday.end

            if (now > holiday.startTime)
                indicator.text = "✓"
            else
                indicator.text = ""
        } else clear()
    }

    private fun clear() {
        title.text = ""
        extra.text = ""
    }

    override fun update(tests: List<Test>) {
        this.tests = tests
        calendarView.removeDecorators()
        calendarView.addDecorator(HolidayDecorator.generate(context, TimetableController.Holidays))
        calendarView.addDecorator(TestDecorator(context.primaryColor, tests))
        calendarView.addDecorator(OneDayDecorator())
    }


    class TestDecorator(private val color: Int, val tests: List<Test>) : DayViewDecorator {

        override fun shouldDecorate(day: CalendarDay): Boolean {
            val time = day.date.time
            return tests.any { it.date == time }
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(10f, color))
        }
    }

    companion object {
        private val TestsDateFormat = SimpleDateFormat("dd/MM/yy")
    }
}

