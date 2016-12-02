package com.ohelshem.app.android.notifications

import android.app.IntentService
import android.content.Intent
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.api.model.Test
import com.ohelshem.app.android.main.MainActivity
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.controller.timetable.TimetableController.Companion.DayType
import com.ohelshem.app.getHour
import com.yoavst.changesystemohelshem.R
import java.util.*

class NotificationService : IntentService("OhelShemNotificationService"), LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)

    val storage: Storage by kodein.instance()
    val timetableController: TimetableController by kodein.instance()

    override fun onHandleIntent(intent: Intent?) {
        if (storage.isSetup()) {
            val day = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }.clearTime()
            if (getHour() >= 21 && storage.lastNotificationTime < Calendar.getInstance().clearTime().timeInMillis) {
                val dayType = TimetableController.getDayType(day, timetableController.learnsOnFriday)
                if (dayType == DayType.Holiday) {
                    if (storage.notificationsForHolidays)
                        notifyHoliday()
                }
                if (storage.notificationsForChanges) {
                    ChangesNotificationGenerator(this).prepareNotification()
                }
                if (storage.notificationsForTests) {
                    checkForTests(day)
                }
                storage.lastNotificationTime = System.currentTimeMillis()
            }
        }
    }


    private fun checkForTests(day: Calendar) {
        val allTests = if (storage.isStudent()) storage.tests else storage.primaryClass?.let { storage.getTestsForClass(it.layer, it.clazz) }
        val now = day.timeInMillis
        day.add(Calendar.DAY_OF_YEAR, 7)
        val inAWeek = day.timeInMillis
        var tests = allTests?.filter { it.date >= now && it.date <= inAWeek }?.sortedBy(Test::date) ?: emptyList()
        if (tests.isNotEmpty()) {
            val first = tests.first()
            if (first.date == now) {
                notifyTestTomorrow(first)
                tests = tests.subList(0, tests.size)
            }
            if (tests.isNotEmpty())
                notifyTestsInAWeek(tests)
        }
    }

    //region Notifications
    private fun notifyHoliday() {
        sendNotification(getString(R.string.holiday_notification), "", action = MainActivity.Shortcut_LaunchDates, big = false, id = 1002)
    }

    private fun notifyTestTomorrow(test: Test) {
        sendNotification(getString(R.string.test_tomorrow), test.content, action = MainActivity.Shortcut_LaunchDates, big = false, id = 1003)
    }

    private fun notifyTestsInAWeek(tests: List<Test>) {
        val text = if (tests.size == 1) tests.first().content else getString(R.string.tests_this_week_subtitle)
        sendNotification(getString(R.string.tests_this_week), text, action = MainActivity.Shortcut_LaunchDates, big = false, id = 1004)
    }
    //endregion
}