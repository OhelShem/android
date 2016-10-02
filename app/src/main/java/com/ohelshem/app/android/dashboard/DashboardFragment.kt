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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.ohelshem.api.model.Test
import com.ohelshem.app.android.base.fragment.BaseMvpFragment
import com.ohelshem.app.android.util.hide
import com.ohelshem.app.android.util.show
import com.ohelshem.app.android.util.stringResource
import com.ohelshem.app.model.HourData
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.dashboard_fragment.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.textResource
import java.text.SimpleDateFormat
import java.util.*


class DashboardFragment: BaseMvpFragment<DashboardView, DashboardPresenter>(), DashboardView {
    override val layoutId: Int = R.layout.dashboard_fragment

    private val windowLesson by stringResource(R.string.window_lesson)
    private val tomorrow by stringResource(R.string.tomorrow)
    private val shortMinute by stringResource(R.string.short_minute)
    private val toStart by stringResource(R.string.to_start)
    private val left by stringResource(R.string.left)
    private val endOfDay by stringResource(R.string.end_of_day)
    private val with by lazy { " " + getString(R.string.with) + " " }

    val timeTick = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            presenter?.update()
        }
    }

    override fun createPresenter(): DashboardPresenter = DashboardPresenter()

    override fun init() {
        todayPlan.onClick { presenter.launchTodayPlan(screenManager) }
        more?.onClick { presenter.launchTestsScreen(screenManager) }
        presenter.init()
        screenManager.screenTitle = ""
    }


    override fun showLessonInfo(data: HourData, isEndOfDay: Boolean,  isTomorrow: Boolean) {
        try {
            val subWithTeacherHTML: String = "<b>"+data.hour.name+"</b>" + with + data.hour.teacher
            lessonName.text = if (data.hour.isEmpty()) windowLesson else Html.fromHtml(subWithTeacherHTML)
            progress.progress = data.progress
            if (isEndOfDay)
                nextLessonName.text = Html.fromHtml("<b>"+endOfDay+"</b>")
            else nextLessonName.text = if (data.nextHour.isEmpty()) windowLesson else Html.fromHtml(subWithTeacherHTML)
            if (isTomorrow)
                timeLeft.text = tomorrow
            else if (data.isBefore)
                timeLeft.text = data.timeToHour.toString() + " " + shortMinute + " " + toStart
            else
                timeLeft.text = data.timeToHour.toString() + " " + shortMinute + " " + left
        } catch (e: Exception) {
            e.printStackTrace()
            timeLeft.text = ""
            progress.progress = 45
            lessonName.textResource = R.string.error
            nextLessonName.text = ""
        }
    }

    override fun showTests(tests: List<Test>) {
        if (testsCard != null) {
            if (tests.size == 0) {
                testsCard.hide()
                testsHeader.textResource = R.string.no_tests_next_week
            } else {
                val now = Date().time
                testsCard.show()
                testsHeader.textResource = R.string.tests_in_next_week
                list.removeAllViews()
                tests.forEach { test ->
                    val view = LayoutInflater.from(activity).inflate(R.layout.item_1_line, list, false)
                    view.apply {
                        this as ViewGroup
                        (getChildAt(1) as TextView).text = test.content
                        (getChildAt(0) as TextView).text = TestDateFormat.format(Date(test.date))
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
        presenter.update()
    }

    override fun onPause() {
        super.onPause()
        activity.unregisterReceiver(timeTick)
    }


    companion object {
        private val TestDateFormat = SimpleDateFormat("dd/MM/yy")
    }
}