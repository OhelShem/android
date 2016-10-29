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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.Menu
import android.view.MenuInflater
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Test
import com.ohelshem.app.android.dates.calendar.DatesCalendarFragment
import com.ohelshem.app.android.dates.list.DatesListFragment
import com.ohelshem.app.android.dates.list.HolidaysListFragment
import com.ohelshem.app.android.drawableRes
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.daysBetween
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.dates_fragment.*
import org.jetbrains.anko.support.v4.onPageChangeListener
import java.util.*
import java.util.concurrent.TimeUnit

class TestsFragment : BaseMvpFragment<TestsView, TestsPresenter>(), TestsView {
    override val layoutId: Int = R.layout.dates_fragment
    override var menuId: Int = R.menu.tests
    override fun createPresenter(): TestsPresenter = with(kodein()) { TestsPresenter(instance()) }

    private val isTablet by lazy { resources.getBoolean(R.bool.isTablet) }

    override fun init() {
        screenManager.setToolbarElevation(false)
        screenManager.screenTitle = getString(R.string.tests)

        initPager()
        initFragments()
    }

    private fun initTabs() {
        val tabs = screenManager.inlineTabs
        tabs.setupWithViewPager(pager)
        tabs.getTabAt(0)!!.icon = drawableRes(R.drawable.ic_list)
        tabs.getTabAt(1)!!.icon = drawableRes(R.drawable.ic_calendar)
        tabs.getTabAt(2)!!.icon = drawableRes(R.drawable.ic_beach)

    }

    private fun initPager() {
        // portrait
        if (pager != null) {
            pager.adapter = DatesFragmentAdapter(childFragmentManager)
            pager.onPageChangeListener {
                onPageSelected {
                    if (it == 1 && !isTablet) {
                        appBarLayout?.setExpanded(false, true)
                    } else if (it == 0 && !isTablet) {
                        appBarLayout?.setExpanded(true, true)
                    }
                }
            }
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
        val now = Calendar.getInstance().clearTime().timeInMillis
        val nextTest = tests.firstOrNull { now <= it.date }
        if (nextTest != null) {
            daysToTest?.text = daysBetween(now.toCalendar(), nextTest.date.toCalendar()).toString()
            testName?.text = nextTest.content
            totalTests?.text = tests.size.toString()
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

    override fun onBecomingVisible() {
        if (pager != null)
            initTabs()
    }

    class DatesFragmentAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment {
            return if (position == 0) DatesListFragment()
            else if (position == 2) HolidaysListFragment()
            else DatesCalendarFragment()
        }

        override fun getCount(): Int = 3

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (isTablet) menu.findItem(R.id.menu_mashov).isVisible = false
        else
            menu.findItem(R.id.menu_mashov).setOnMenuItemClickListener {
                val intent = activity.packageManager.getLaunchIntentForPackage("com.yoavst.mashov")
                if (isGraderInstalled() && intent != null) {
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    activity.startActivity(intent)
                } else {
                    MaterialStyledDialog.Builder(activity)
                            .setTitle(R.string.grader_dialog_title)
                            .setDescription(R.string.grader_dialog_description)
                            .setStyle(Style.HEADER_WITH_ICON)
                            .setIcon(R.drawable.mashov)
                            .autoDismiss(true)
                            .setPositiveText(R.string.download)
                            .onPositive { materialDialog, dialogAction ->
                                materialDialog.cancel()
                                launchPlayStore("com.yoavst.mashov")
                            }
                            .setNegativeText(R.string.no_thanks)
                            .onNegative { materialDialog, dialogAction ->
                                materialDialog.cancel()
                            }
                            .show()
                }
                true
            }
    }

    private fun isGraderInstalled(): Boolean {
        try {
            context.packageManager.getApplicationInfo("com.yoavst.mashov", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

    }

    private fun launchPlayStore(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)))
        }
    }
}