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

package com.ohelshem.app.android.holidays

import android.support.v7.widget.LinearLayoutManager
import com.ohelshem.app.android.base.fragment.BaseMvpFragment
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.TimetableController.Companion.Holiday
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.holidays_fragment.*
import java.util.*
import java.util.concurrent.TimeUnit

class HolidaysFragment : BaseMvpFragment<HolidaysView, HolidaysPresenter>(), HolidaysView {
    override val layoutId: Int = R.layout.holidays_fragment
    override fun createPresenter(): HolidaysPresenter = HolidaysPresenter()

    override fun init() {
        presenter.init()
        initView()
    }

    private fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
    }

    override fun showHolidays(holidays: Array<Holiday>, summer: Holiday) {
        val now = Calendar.getInstance().clearTime().timeInMillis
        daysToSummer.text = TimeUnit.MILLISECONDS.toDays(summer.startTime - now).toString()
        val nextHoliday = holidays.find { it.startTime >= now }
        if (nextHoliday == null)
            daysToHoliday.text = daysToSummer.text
        else {
            var days = TimeUnit.MILLISECONDS.toDays(nextHoliday.startTime - now)
            if (days < 0) days = 0
            daysToHoliday.text = days.toString()
        }
        recyclerView.adapter = HolidaysAdapter(holidays)
    }
}