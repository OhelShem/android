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

package com.ohelshem.app.android.dates

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.api.model.Test
import com.ohelshem.app.*
import com.ohelshem.app.android.dates.calendar.HolidayDecorator
import com.ohelshem.app.android.dates.list.DatesListFragment
import com.ohelshem.app.android.dates.list.HolidaysListFragment
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.hide
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
import kotlinx.android.synthetic.main.calendar_fragment.view.*
import kotlinx.android.synthetic.main.dates_fragment.*
import org.jetbrains.anko.sdk15.listeners.onClick
import java.util.*
import java.util.concurrent.TimeUnit

class DatesFragment : BaseMvpFragment<DatesView, DatesPresenter>(), DatesView {
    override val layoutId: Int = R.layout.dates_fragment
    override var menuId: Int = R.menu.tests
    override fun createPresenter(): DatesPresenter = with(kodein()) { DatesPresenter(instance()) }

    private var dialog: AlertDialog? = null

    override fun init() {
        screenManager.setToolbarElevation(false)
        screenManager.screenTitle = getString(R.string.tests)

        initPager()
        initFragments()

        calendar.onClick {
            openCalendarView()
        }
    }

    private fun initTabs() {
        val tabs = screenManager.inlineTabs
        tabs.setupWithViewPager(pager)
        tabs.getTabAt(0)!!.icon = drawableRes(R.drawable.ic_list)
        tabs.getTabAt(1)!!.icon = drawableRes(R.drawable.ic_beach)

    }

    @Suppress("UNSAFE_CALL_ON_PARTIALLY_DEFINED_RESOURCE")
    private fun initPager() {
        // portrait
        if (pager != null) {
            pager.adapter = DatesFragmentAdapter(childFragmentManager)
            initTabs()
        }
    }

    private fun initFragments() {
        // landscape
        if (leftFragment != null) {
            val adapter = DatesFragmentAdapter(childFragmentManager)
            childFragmentManager.beginTransaction()
                    .replace(R.id.leftFragment, adapter.getItem(0))
                    .replace(R.id.rightFragment, adapter.getItem(1))
                    .commit()
        }
    }

    override fun update(tests: List<Test>) {
        if (tests.isEmpty() && presenter.isTeacher) teacherErrorView.show()
        else {
            teacherErrorView.hide()
            val now = getIsraelCalendar().clearTime().timeInMillis
            val nextTest = tests.firstOrNull { now <= it.date }
            if (nextTest != null) {
                daysToTest?.text = daysBetween(now.toCalendar(), nextTest.date.toCalendar()).toString()
            }

            val nextHoliday = TimetableController.Holidays.find { it.startTime >= now }
            if (nextHoliday == null)
                daysToHoliday?.text = TimeUnit.MILLISECONDS.toDays(TimetableController.Summer.startTime - now).toString()
            else {
                var days = TimeUnit.MILLISECONDS.toDays(nextHoliday.startTime - now)
                if (days < 0) days = 0
                daysToHoliday?.text = days.toString()
            }
        }
    }

    override fun onBecomingVisible() {
        if (pager != null)
            initTabs()
    }

    class DatesFragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment = if (position == 0) DatesListFragment() else HolidaysListFragment()

        override fun getCount(): Int = 2

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (presenter.isTeacher)
            menu.findItem(R.id.menu_mashov).isVisible = false
        else
            menu.findItem(R.id.menu_mashov).setOnMenuItemClickListener {
                val intent = activity!!.packageManager.getLaunchIntentForPackage("com.yoavst.mashov")
                if (isGraderInstalled() && intent != null) {
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    activity!!.startActivity(intent)
                } else {
                    MaterialStyledDialog.Builder(activity)
                            .setTitle(R.string.grader_dialog_title)
                            .setDescription(R.string.grader_dialog_description)
                            .setStyle(Style.HEADER_WITH_ICON)
                            .setIcon(R.drawable.mashov)
                            .autoDismiss(true)
                            .setPositiveText(R.string.download)
                            .onPositive { materialDialog, _ ->
                                materialDialog.cancel()
                                launchPlayStore("com.yoavst.mashov")
                            }
                            .setNegativeText(R.string.no_thanks)
                            .onNegative { materialDialog, _ ->
                                materialDialog.cancel()
                            }
                            .show()
                }
                true
            }
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
        dialog = null
    }

    @SuppressLint("InflateParams")
    private fun openCalendarView() {
        val tests = presenter.tests
        val view = activity!!.layoutInflater.inflate(R.layout.calendar_fragment, null, false)
        val calendarView = view.calendarView
        val title = view.title
        val extra = view.extra
        val indicator = view.indicator
        val data = view.data
        val now = getIsraelCalendar().timeInMillis

        fun update(test: Test) {
            data.show()
            title.text = test.content
            extra.text = testDateFormat.format(Date(test.date))
            if (now > test.date)
                indicator.text = "✓"
            else
                indicator.text = ""
        }

        @SuppressLint("SetTextI18n")
        fun update(holiday: Holiday) {
            data.show()
            title.text = holiday.name
            if (holiday.isOneDay())
                extra.text = holiday.start
            else
                extra.text = "${holiday.start} - ${holiday.end}"

            if (now > holiday.startTime)
                indicator.text = "✓"
            else
                indicator.text = ""
        }

        fun clear() {
            title.text = ""
            extra.text = ""
            indicator.text = ""
        }

        calendarView.currentDate = CalendarDay.today()
        calendarView.state().edit().apply {
            setMinimumDate(CalendarDay.from(TimetableController.StartOfTheYear))
            setMaximumDate(CalendarDay.from(Date(TimetableController.Summer.endTime)))
        }.commit()
        calendarView.setOnDateChangedListener { _, calendarDay, _ ->
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
                clear()
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
        calendarView.addDecorator(HolidayDecorator.generate(context!!, TimetableController.Holidays))
        calendarView.addDecorator(TestDecorator(context!!.primaryColor, tests))
        calendarView.addDecorator(OneDayDecorator())


        dialog = AlertDialog.Builder(context!!)
                .setNeutralButton(R.string.dialog_close) { dialog, _ -> dialog.dismiss() }
                .setView(view)
                .show()
    }

    private fun isGraderInstalled(): Boolean = try {
        context!!.packageManager.getApplicationInfo("com.yoavst.mashov", 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun launchPlayStore(packageName: String) = try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)))
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)))
    }

    private class TestDecorator(private val color: Int, val tests: List<Test>) : DayViewDecorator {

        override fun shouldDecorate(day: CalendarDay): Boolean {
            val time = day.date.time
            return tests.any { it.date == time }
        }

        override fun decorate(view: DayViewFacade) = view.addSpan(DotSpan(10f, color))
    }
}