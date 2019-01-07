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

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import com.ohelshem.app.android.utils.MaterialTapTargetPrompt
import com.ohelshem.app.controller.storage.UIStorage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.daysBetween
import com.ohelshem.app.getIsraelCalendar
import com.ohelshem.app.model.HourData
import com.ohelshem.app.model.NumberedHour
import com.ohelshem.app.testDateFormat
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.BuildConfig
import com.yoavst.changesystemohelshem.R
import kotlinx.android.synthetic.main.dashboard_fragment.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk15.listeners.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textResource
import java.util.*

class DashboardFragment : BaseMvpFragment<DashboardView, DashboardPresenter>(), DashboardView {
    override val layoutId: Int = R.layout.dashboard_fragment

    private var defaultTextColor: Int = 0

    private val storage: UIStorage by kodein.instance()

    private val timeTick = object : BroadcastReceiver() {
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
    private val emptyTimetable by stringResource(R.string.empty_timetable)
    private val with by lazy { " " + getString(R.string.with) + " " }
    private val daysOfWeek by lazy { resources.getStringArray(R.array.week_days) }

    override fun showLessonInfo(data: HourData, isEndOfDay: Boolean, isTomorrow: Boolean, isFuture: Boolean, changes: List<Change>?) {
        if (!showHolidayInfo(isTomorrow, isFuture)) {
            progress?.progress = data.progress
            showCurrentLessonInfo(data, changes)
            showTimeLeft(data, isFuture, isTomorrow)
            showNextLessonInfo(data, changes, isEndOfDay, isFuture)
            todayPlan.text = when {
                isFuture -> getString(R.string.future_plan, daysOfWeek[data.hour.day - 1])
                isTomorrow -> getString(R.string.tomorrow_plan)
                else -> getString(R.string.today_plan)
            }
        }
    }

    override fun showHolidayInfo(isTomorrow: Boolean, isFuture: Boolean): Boolean {
        if (storage.disableHolidayCard)
            return false
        val today = getIsraelCalendar()
        val tomorrow = getIsraelCalendar()
        tomorrow.add(Calendar.DATE, 1)

        val todayHoliday = TimetableController.getHoliday(today)
        val tomorrowHoliday = TimetableController.getHoliday(tomorrow)

        val isBeforeHoliday = todayHoliday == null && tomorrowHoliday != null && (isTomorrow || isFuture) // today is not a holiday, tomorrow is a holiday, schedule for today has ended
        val isInHoliday = todayHoliday != null && tomorrowHoliday != null // today is a holiday and tomorrow is a holiday
        val isEndOfHoliday = todayHoliday != null && tomorrowHoliday == null && !(isTomorrow || isFuture) // today is a holiday, tomorrow is not a holiday, schedule for today hasn't ended yet

        if (isBeforeHoliday || isInHoliday || isEndOfHoliday) {
            // Don't show the regular timetable if we're in the middle of any holiday (incl. Summer)
            lessonsContainer.removeAllViews()
            val holidayText = TextView(context)
            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            holidayText.layoutParams = params
            holidayText.setPadding(16, 16, 16, 16)
            holidayText.gravity = Gravity.CENTER
            holidayText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_beach, 0, 0)
            holidayText.setTextAppear(context!!, R.style.TextAppearance_AppCompat_Title)
            holidayText.setTypeface(null, Typeface.BOLD)
            holidayText.textColor = Color.WHITE
            if (isBeforeHoliday)
                holidayText.text = if (tomorrowHoliday!!.isOneDay()) tomorrowHoliday.name else "${tomorrowHoliday.name} (${daysBetween(today, tomorrowHoliday.endTime.toCalendar())} ימים נותרו" + ")"
            else
                holidayText.text = if (todayHoliday!!.isOneDay()) todayHoliday.name else "${todayHoliday.name} (${daysBetween(today, todayHoliday.endTime.toCalendar())} ימים נותרו" + ")"
            lessonsContainer.backgroundColor = context!!.primaryLightColor
            lessonsContainer.addView(holidayText)
            return true
        }
        return false
    }

    override fun showEmptyCard() {
        lessonsContainer.removeAllViews()
        val emptyText = TextView(context)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        emptyText.layoutParams = params
        emptyText.setPadding(16, 16, 16, 16)
        emptyText.gravity = Gravity.CENTER
        emptyText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_empty_timetable, 0, 0)
        emptyText.compoundDrawables[1].colorFilter = PorterDuffColorFilter(defaultTextColor, PorterDuff.Mode.SRC_IN)
        emptyText.setTextAppear(context!!, R.style.TextAppearance_AppCompat_Title)
        emptyText.setTypeface(null, Typeface.BOLD)
        emptyText.textColor = defaultTextColor
        emptyText.text = emptyTimetable
        lessonsContainer.addView(emptyText)
    }

    private var hasModifiedCurrentLessonView = true
    private fun showCurrentLessonInfo(data: HourData, changes: List<Change>?) {
        val change = changes?.firstOrNull { it.hour - 1 == data.hour.hourOfDay }
        if (change != null) {

            hasModifiedCurrentLessonView = true
            lessonName.htmlText = bold { change.content } + " " + nameOriginalHour(change.content, data.hour.represent(showRoom = false))
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
                lessonName.htmlText = bold { data.hour.represent() } + if (data.hour.teacher.isNotEmpty()) with + data.hour.teacher else ""
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

    @SuppressLint("SetTextI18n")
    private fun showTimeLeft(data: HourData, isFuture: Boolean, isTomorrow: Boolean) {
        timeLeft.text = when {
            isFuture -> daysOfWeek[data.hour.day - 1]
            isTomorrow -> tomorrow
            data.isBefore -> "${data.timeToHour} $shortMinute $toStart"
            else -> "${data.timeToHour} $shortMinute $left"
        }
        if ((isTomorrow || isFuture) && data.hour.hourOfDay > 0)
            timeLeft.append(", ${TimetableController.DayHours[data.hour.hourOfDay * 2]}")
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

                hasModifiedNextLessonView = true
                nextLessonName.htmlText = bold { change.content } + " " + nameOriginalHour(change.content, data.nextHour.represent(showRoom = false))
                next_lesson.backgroundColor = change.color
                nextLessonName.textColor = Color.WHITE
                nextHourIcon.setColorFilter(Color.WHITE)
            } else {
                if (hasModifiedNextLessonView)
                    clearNextLessonView()

                if (data.nextHour.isEmpty())
                    nextLessonName.htmlText = bold { windowLesson }
                else {
                    nextLessonName.htmlText = bold { data.nextHour.represent() } + if (data.nextHour.teacher.isNotEmpty()) with + data.nextHour.teacher else ""
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


    private fun NumberedHour.represent(showRoom: Boolean = true) = if (isEmpty()) windowLesson else if (room != 0 && room != -1 && showRoom) "$name ($room)" else name
    //endregion


    private fun showIntro() {
        dashboardLogo?.post {
            var prompt: MaterialTapTargetPrompt? = null
            prompt = MaterialTapTargetPrompt.Builder(activity)
                    .setPrimaryText(R.string.intro_dashboard_primary_text)
                    .setSecondaryText(R.string.intro_dashboard_secondary_text)
                    .setTarget(dashboardLogo)
                    .setBackgroundColour(activity!!.primaryColor)
                    .setCaptureTouchEventOutsidePrompt(true)
                    .setAutoFinish(false)
                    .setOnHidePromptListener(object : MaterialTapTargetPrompt.OnHidePromptListener {
                        override fun onHidePromptComplete() = screenManager.startTour()

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
                        (getChildAt(0) as TextView).text = testDateFormat.format(Date(test.date))
                        if (now > test.date)
                            (getChildAt(2) as TextView).text = "✓"
                        else
                            (getChildAt(2) as TextView).text = ""
                    }
                    view.onClick {}
                    list.addView(view)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity!!.registerReceiver(timeTick, IntentFilter(Intent.ACTION_TIME_TICK))
        presenter.update()
    }

    override fun onPause() {
        super.onPause()
        activity!!.unregisterReceiver(timeTick)
    }
}
