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
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.api.model.Change
import com.ohelshem.api.model.Test
import com.ohelshem.app.android.*
import com.ohelshem.app.android.utils.BaseMvpFragment
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.storage.UIStorage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.controller.timetable.TimetableController.Companion.DayType
import com.ohelshem.app.controller.timetable.TimetableController.Companion.Holiday
import com.ohelshem.app.model.HourData
import com.ohelshem.app.model.NumberedHour
import com.yoavst.changesystemohelshem.BuildConfig
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.dashboard_fragment.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.onClick
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textResource
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.text.SimpleDateFormat
import java.util.*


class DashboardFragment : BaseMvpFragment<DashboardView, DashboardPresenter>(), DashboardView {
    override val layoutId: Int = R.layout.dashboard_fragment

    private var defaultTextColor: Int = 0

    private val storage: UIStorage by kodein.instance()

    val timeTick = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            presenter?.update()
        }
    }

    override fun createPresenter(): DashboardPresenter = with(kodein()) { DashboardPresenter(instance(), instance()) }

    override fun init() {
        defaultTextColor = todayPlan.currentTextColor
        screenManager.setToolbarElevation(false)
        todayPlan.onClick {
            presenter.launchTodayPlan(screenManager)
        }
        more?.apply {
            onClick {
                presenter.launchTestsScreen(screenManager)
            }
        }
        screenManager.screenTitle = ""

        clearCurrentLessonView()
        clearNextLessonView()

        if (holidayData != null) {
            showHoliday()
        }

        if (storage.firstTimeInApp && !BuildConfig.DEBUG)
            showIntro()
    }

    //region Lesson Info
    private val windowLesson by stringResource(R.string.window_lesson)
    private val tomorrow by stringResource(R.string.tomorrow)
    private val shortMinute by stringResource(R.string.short_minute)
    private val toStart by stringResource(R.string.to_start)
    private val left by stringResource(R.string.left)
    private val endOfDay by stringResource(R.string.end_of_day)
    private val instead by stringResource(R.string.instead)
    private val with by lazy { " " + getString(R.string.with) + " " }
    private val daysOfWeek by lazy { resources.getStringArray(R.array.week_days) }

    override fun showLessonInfo(data: HourData, isEndOfDay: Boolean, isTomorrow: Boolean, isFuture: Boolean, changes: List<Change>?) {
        if (!showHolidayInfo()) {
            progress.progress = data.progress
            showCurrentLessonInfo(data, changes)
            showTimeLeft(data, isFuture, isTomorrow)
            showNextLessonInfo(data, changes, isEndOfDay, isFuture)
        }
    }


    private fun showHolidayInfo(): Boolean {
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DATE, 1)

        val todayType = TimetableController.getDayType(today, false)
        val tomorrowType = TimetableController.getDayType(tomorrow, false)

        if ((todayType == DayType.Holiday || todayType == DayType.Summer) && (tomorrowType == DayType.Holiday || tomorrowType == DayType.Summer)) {
            lessonsContainer.removeAllViews()
            lessonsContainer.minimumHeight = 144
            val holidayText = TextView(context)
            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            holidayText.layoutParams = params
            holidayText.setPadding(16, 16, 16, 16)
            holidayText.gravity = Gravity.CENTER
            holidayText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_beach, 0, 0)
            holidayText.setTextAppear(context, R.style.TextAppearance_AppCompat_Title)
            holidayText.setTypeface(null, Typeface.BOLD)
            holidayText.textColor = defaultTextColor
            holidayText.text = getString(R.string.dashboard_holiday)
            holidayText.compoundDrawables[1].setColorFilter(defaultTextColor, PorterDuff.Mode.SRC_IN)
            lessonsContainer.addView(holidayText)
            return true
        }
        return false
    }

    private var hasModifiedCurrentLessonView = true
    private fun showCurrentLessonInfo(data: HourData, changes: List<Change>?) {
        val change = changes?.firstOrNull { it.hour - 1 == data.hour.hourOfDay }
        if (change != null) {

            val changeIsWithout = " בלי " in change.content
            val changeIsMikbatz = "מקבץ" in change.content || "מקבצים" in change.content
            val withoutNoMikbatz = changeIsWithout && !changeIsMikbatz
            val withoutYesMikbatz = changeIsWithout && changeIsMikbatz
            val withoutNoName = change.content.startsWith("בלי")

            hasModifiedCurrentLessonView = true
            lessonName.htmlText =
                    if (withoutNoMikbatz) bold { change.content }
                    else bold { change.content } + " (${if (withoutYesMikbatz || withoutNoName) "" else instead + " "}${data.hour.represent()})"

            currentLesson.backgroundColor = change.color
            firstSpace.backgroundColor = change.color
            lessonName.textColor = Color.WHITE
            timeLeft.textColor = Color.WHITE
            hourIcon.setColorFilter(Color.WHITE)
        } else {
            if (hasModifiedCurrentLessonView) {
                clearCurrentLessonView()
            }

            if (data.hour.isEmpty())
                lessonName.htmlText = bold { windowLesson }
            else {
                lessonName.htmlText = bold { data.hour.name } + if (data.hour.teacher.isNotEmpty()) with + data.hour.teacher else ""
            }
        }
    }

    private fun clearCurrentLessonView() {
        currentLesson.backgroundColor = Color.TRANSPARENT
        lessonName.textColor = defaultTextColor
        timeLeft.textColor = defaultTextColor
        hourIcon.setColorFilter(defaultTextColor)
        firstSpace.backgroundColor = Color.parseColor("#e0e0e0")
        hasModifiedCurrentLessonView = false
    }

    private fun showTimeLeft(data: HourData, isFuture: Boolean, isTomorrow: Boolean) {
        if (isFuture)
            timeLeft.text = daysOfWeek[data.hour.day - 1]
        else if (isTomorrow)
            timeLeft.text = tomorrow
        else if (data.isBefore)
            timeLeft.text = "${data.timeToHour} $shortMinute $toStart"
        else
            timeLeft.text = "${data.timeToHour} $shortMinute $left"
    }

    private var hasModifiedNextLessonView = true
    private fun showNextLessonInfo(data: HourData, changes: List<Change>?, isEndOfDay: Boolean, isFuture: Boolean) {
        if (isEndOfDay) {
            if (hasModifiedNextLessonView)
                clearNextLessonView()

            nextLessonName.htmlText = bold { endOfDay }
        } else {
            val change = changes?.firstOrNull { it.hour - 1 == data.nextHour.hourOfDay }
            if (!isFuture && change != null) {

                val nextChangeIsWithout = " בלי " in change.content
                val nextChangeIsMikbatz = "מקבץ" in change.content || "מקבצים" in change.content
                val withoutNoMikbatz = nextChangeIsWithout && !nextChangeIsMikbatz
                val withoutYesMikbatz = nextChangeIsWithout && nextChangeIsMikbatz
                val withoutNoName = change.content.startsWith("בלי")

                hasModifiedNextLessonView = true
                nextLessonName.htmlText = if (withoutNoMikbatz) bold { change.content } else bold { change.content } + " (${if (withoutYesMikbatz || withoutNoName) "" else instead + " "}${data.nextHour.represent()})"
                next_lesson.backgroundColor = change.color
                nextLessonName.textColor = Color.WHITE
                nextHourIcon.setColorFilter(Color.WHITE)
            } else {
                if (hasModifiedNextLessonView)
                    clearNextLessonView()

                if (data.nextHour.isEmpty())
                    nextLessonName.htmlText = bold { windowLesson }
                else {
                    nextLessonName.htmlText = bold { data.nextHour.name } + if (data.nextHour.teacher.isNotEmpty()) with + data.nextHour.teacher else ""
                }
            }
        }
    }

    private fun clearNextLessonView() {
        next_lesson.backgroundColor = Color.TRANSPARENT
        nextLessonName.textColor = defaultTextColor
        nextHourIcon.setColorFilter(defaultTextColor)
        hasModifiedNextLessonView = false
    }


    private fun NumberedHour.represent() = if (isEmpty()) windowLesson else name
    //endregion


    private fun showIntro() {
        dashboardLogo?.post {
            var prompt: MaterialTapTargetPrompt? = null
            prompt = MaterialTapTargetPrompt.Builder(act)
                    .setPrimaryText(R.string.intro_dashboard_primary_text)
                    .setSecondaryText(R.string.intro_dashboard_secondary_text)
                    .setTarget(dashboardLogo)
                    .setBackgroundColour(act.primaryColor)
                    .setCaptureTouchEventOutsidePrompt(true)
                    .setAutoFinish(false)
                    .setOnHidePromptListener(object : MaterialTapTargetPrompt.OnHidePromptListener {
                        override fun onHidePromptComplete() {
                            screenManager.startTour()
                        }

                        override fun onHidePrompt(event: MotionEvent?, tappedTarget: Boolean) {
                            if (tappedTarget) {
                                prompt?.finish()
                            }
                        }
                    }).show()
        }
    }

    override fun showTests(tests: List<Test>) {
        if (testsCard != null) {
            if (tests.isEmpty()) {
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
                        @Suppress("CAST_NEVER_SUCCEEDS")
                        (this as ViewGroup)
                        (getChildAt(1) as TextView).text = test.content
                        (getChildAt(0) as TextView).text = TestDateFormat.format(Date(test.date))
                        if (now > test.date)
                            (getChildAt(2) as TextView).text = "✓"
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

    private fun showHoliday() {
        val time = Calendar.getInstance().clearTime().timeInMillis
        var holiday: Holiday? = TimetableController.Holidays.firstOrNull { if (it.isOneDay()) it.startTime == time else time >= it.startTime && time <= it.endTime }
        if (holiday == null) holiday = TimetableController.Holidays.firstOrNull { it.startTime > time }
        if (holiday == null) holiday = TimetableController.Summer

        holidayText.text = holiday.name
        holidayDate.text = if (holiday.isOneDay()) holiday.start.substring(0, 5) else holiday.start.substring(0, 5) + " - " + holiday.end.substring(0, 5)
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
