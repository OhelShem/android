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

package com.ohelshem.app.android.dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.util.hide
import com.ohelshem.app.android.util.show
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.model.ApiUpdatable
import com.ohelshem.app.model.DrawerActivity
import com.ohelshem.app.toCalendar
import com.ohelshem.app.controller.ApiController
import com.ohelshem.api.model.Test
import com.ohelshem.api.model.UpdateError
import com.ohelshem.app.android.util.fragment.BaseFragment
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.dashboard_fragment.*
import org.jetbrains.anko.*
import uy.kohesive.injekt.injectLazy
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * The fragment that is shown on open the main activity. show the current & next lessons
 * and the user's tests in the next week.
 */
class DashboardFragment : BaseFragment(), ApiUpdatable<Any> {
    override val api: ApiController.Api? = null
    private val databaseController: DBController by injectLazy()
    private val timetableController: TimetableController by injectLazy()
    private val windowLesson by lazy { getString(R.string.window_lesson) }
    private val with by lazy { " " + getString(R.string.with) + " " }
    override val layoutId: Int = R.layout.dashboard_fragment
    val timeTick = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onUpdate()
        }
    }

    override fun onUpdate() {
        try {
            updateTests()
            val data = timetableController.getHourData()
            lessonName.text = if (data.hour.isEmpty()) windowLesson else data.hour.name + with + data.hour.teacher
            progress.progress = data.progress
            if (TimetableController.isEndOfDay(data.hour.hourOfDay, timetableController[data.hour.day]))
                nextLessonName.text = getString(R.string.end_of_day)
            else nextLessonName.text = if (data.nextHour.isEmpty()) windowLesson else data.nextHour.name + with + data.nextHour.teacher
            if ((System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(data.timeToHour.toLong())).toCalendar().clearTime().timeInMillis >
                    Calendar.getInstance().clearTime().timeInMillis) {
                timeLeft.textResource = R.string.tomorrow
            } else if (data.isBefore)
                timeLeft.text = data.timeToHour.toString() + " " + activity.getString(R.string.short_minute) + " " + activity.getString(R.string.to_start)
            else
                timeLeft.text = data.timeToHour.toString() + " " + activity.getString(R.string.short_minute) + " " + activity.getString(R.string.left)
        } catch (e: Exception) {
            e.printStackTrace()
            timeLeft.text = ""
            progress.progress = 45
            lessonName.textResource = R.string.error
            nextLessonName.text = ""
        }
    }

    override fun onUpdate(data: Any) {
        onUpdate()
    }

    override fun onError(error: UpdateError) {
        // Ignore
    }

    override fun init() {
        if (view != null) {
            todayPlan.onClick {
                if (databaseController.hasChanges())
                    drawerActivity.setFragment(DrawerActivity.Companion.FragmentType.Changes, true)
                else drawerActivity.setFragment(DrawerActivity.Companion.FragmentType.Timetable, true)
            }
            more?.onClick {
                drawerActivity.setFragment(DrawerActivity.Companion.FragmentType.Tests, true)
            }
            onUpdate()
            if (resources.getBoolean(R.bool.dashboard_show_tests)) {
                drawerActivity.setToolbarTitle(" ")
                updateTests()
            }
        } else Handler().postDelayed({ init() }, 500)
    }

    private fun updateTests() {
        if (view != null && testsCard != null) {
            val time = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
            val now = Date().time
            val tests = databaseController.tests?.filter { it.date <= time && it.date >= now } ?: emptyList<Test>()
            if (tests.size == 0) {
                testsCard.hide()
                testsHeader.textResource = R.string.no_tests_next_week
            } else {
                testsCard.show()
                testsHeader.textResource = R.string.tests_in_next_week
                list.removeAllViews()
                tests.forEach { test ->
                    val view = LayoutInflater.from(activity).inflate(R.layout.item_1_line, list, false)
                    view.apply {
                        this as ViewGroup
                        (getChildAt(1) as TextView).text = test.content
                        (getChildAt(0) as TextView).text = DateFormat.format(Date(test.date))
                        if (now > test.date)
                            (getChildAt(2) as TextView).text = "V"
                        else
                            (getChildAt(2) as TextView).text = ""
                    }
                    view.onClick {

                    }
                    list.addView(view)

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity.registerReceiver(timeTick, IntentFilter(Intent.ACTION_TIME_TICK))
        onUpdate()
    }

    override fun onPause() {
        super.onPause()
        activity.unregisterReceiver(timeTick)
    }

    companion object {
        private val DateFormat = SimpleDateFormat("dd/MM/yy")
    }

}