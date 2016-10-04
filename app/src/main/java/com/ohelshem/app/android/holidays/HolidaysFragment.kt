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

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.holidays.calendar.HolidaysCalendarFragment
import com.ohelshem.app.android.holidays.list.HolidaysListFragment
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.timetable.TimetableController.Companion.Holiday
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.holidays_fragment.*
import org.jetbrains.anko.support.v4.onPageChangeListener
import java.util.*
import java.util.concurrent.TimeUnit

class HolidaysFragment : BaseMvpFragment<HolidaysView, HolidaysPresenter>(), HolidaysView {
    override val layoutId: Int = R.layout.holidays_fragment
    override fun createPresenter(): HolidaysPresenter = HolidaysPresenter()

    override fun init() {
        screenManager.setToolbarElevation(false)
        screenManager.screenTitle = getString(R.string.holidays)

        initPager()
        initFragments()
    }


    private fun initPager() {
        // portrait
        if (pager != null) {
            pager.adapter = HolidaysPagesAdapter(childFragmentManager)
            pager.onPageChangeListener {
                onPageSelected {
                    if (it == 1) {
                        appBarLayout.setExpanded(false, true)
                    }
                }
            }
            tabs.setupWithViewPager(pager)
            tabs.getTabAt(0)!!.icon = drawableRes(R.drawable.ic_list)
            tabs.getTabAt(1)!!.icon = drawableRes(R.drawable.ic_calendar)
        }
    }

    private fun initFragments() {
        // landscape
        if (leftFragment != null) {
            val adapter = HolidaysPagesAdapter(childFragmentManager)
            childFragmentManager.beginTransaction()
                    .replace(R.id.leftFragment, adapter.getItem(0))
                    .replace(R.id.rightFragment, adapter.getItem(1))
                    .commit()
        }
    }

    override fun showHolidays(holidays: Array<Holiday>, summer: Holiday) {
        val now = Calendar.getInstance().clearTime().timeInMillis
        daysToSummer?.text = TimeUnit.MILLISECONDS.toDays(summer.startTime - now).toString()
        val nextHoliday = holidays.find { it.startTime >= now }
        if (nextHoliday == null)
            daysToHoliday?.text = daysToSummer.text
        else {
            var days = TimeUnit.MILLISECONDS.toDays(nextHoliday.startTime - now)
            if (days < 0) days = 0
            daysToHoliday?.text = days.toString()
        }
        weeksToSummer?.let { weeksToSummer ->
            weeksToSummer.text = (daysToSummer.text.toString().toInt() / 7).toString()
        }
    }


    class HolidaysPagesAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment = when (position) {
            0 -> HolidaysListFragment()
            else -> HolidaysCalendarFragment()
        }

        override fun getCount(): Int = 2
    }
}