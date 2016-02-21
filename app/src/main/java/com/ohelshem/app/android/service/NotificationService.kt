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

package com.ohelshem.app.android.service

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.support.v7.app.NotificationCompat
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.activity.GuessingActivity
import com.ohelshem.app.android.activity.MainActivity
import com.ohelshem.app.android.isNetworkAvailable
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.controller.TimetableController.Companion.DayType.Holiday
import com.ohelshem.app.getHour
import com.ohelshem.app.model.DrawerActivity.Companion.FragmentType
import com.ohelshem.app.controller.ApiController
import com.ohelshem.app.controller.ApiController.Api
import com.ohelshem.api.model.AuthData
import com.ohelshem.api.model.Test
import com.ohelshem.api.model.UpdateError
import org.jetbrains.anko.notificationManager
import uy.kohesive.injekt.injectLazy
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationService : IntentService("OhelShemNotificationService") {
    val databaseController: DBController by injectLazy()
    val timetableController: TimetableController by injectLazy()
    val apiController: ApiController by injectLazy()

    private val handler = Handler()

    override fun onHandleIntent(intent: Intent?) {
        if (databaseController.isSetup()) {
            val day = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }.clearTime()
            if (getHour() >= 21 && databaseController.lastNotificationTime < Calendar.getInstance().clearTime().timeInMillis) {
                val dayType = timetableController.getDayType(day)
                if (dayType == Holiday) {
                    if (databaseController.notificationsForHolidaysEnabled)
                        notifyHoliday()
                }
                if (databaseController.notificationsForTestsEnabled) {
                    checkForTests(day)
                }
                if (databaseController.notificationsForChangesEnabled)
                    checkData(day)
            }
        }
    }


    private fun checkData(day: Calendar) {
        if (isNetworkAvailable()) {
            apiController[CallbackId] = object : ApiController.Callback {
                override fun onSuccess(apis: List<Api>) {
                    if (Api.Changes !in apis || databaseController.changesDate != day.timeInMillis) {
                        // Changes were not updated yet
                        handler.postDelayed({ notifyChanges() }, TimeUnit.MINUTES.toMillis(1))
                    } else {
                        val clazz = databaseController.userData.clazz
                        if (databaseController.changes?.any { it.clazz == clazz } ?: false)
                            notifyChanges()
                        else notifyNoChanges()
                    }
                    apiController -= CallbackId
                }

                override fun onFail(error: UpdateError) {
                    if (error == UpdateError.Connection || error == UpdateError.NoData)
                        handler.postDelayed({ notifyChanges() }, TimeUnit.MINUTES.toMillis(1))
                    else {
                        databaseController.lastNotificationTime = System.currentTimeMillis()
                    }
                    apiController -= CallbackId
                }

            }
            apiController.authData = AuthData(databaseController.userData.identity, databaseController.password)
            apiController.update()
        } else {
            handler.postDelayed({ checkData(day) }, TimeUnit.MINUTES.toMillis(2))
        }
    }

    private fun checkForTests(day: Calendar) {
        val now = day.timeInMillis
        day.add(Calendar.DAY_OF_YEAR, 7)
        val inAWeek = day.timeInMillis
        var tests = databaseController.tests?.filter { it.date >= now && it.date <= inAWeek }?.sortedBy { it.date } ?: emptyList<Test>()
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
        notificationManager.notify(1002, notification(getString(R.string.holiday_notification), "", hasChanges = false, fragmentType = FragmentType.Holidays))
    }

    private fun notifyChanges() {
        notificationManager.notify(1001, notification(getString(R.string.notification_about_changes), getString(R.string.enter_to_see_changes),
                hasChanges = true, fragmentType = FragmentType.Changes))

    }

    private fun notifyNoChanges() {
        notificationManager.notify(1001, notification(getString(R.string.notification_about_changes), getString(R.string.no_changes),
                hasChanges = false, fragmentType = FragmentType.Dashboard))
    }

    private fun notifyTestTomorrow(test: Test) {
        notificationManager.notify(1003, notification(getString(R.string.test_tomorrow), test.content, hasChanges = false, fragmentType = FragmentType.Tests))
    }

    private fun notifyTestsInAWeek(tests: List<Test>) {
        val text = if (tests.size == 1) tests.first().content else getString(R.string.tests_this_week_subtitle)
        notificationManager.notify(1004, notification(getString(R.string.tests_this_week), text, hasChanges = false, fragmentType = FragmentType.Tests))
    }

    private fun notification(title: String, text: String, hasChanges: Boolean, fragmentType: FragmentType): Notification? {
                val intent =
                        if (hasChanges && databaseController.guessingGameEnabled)
                            Intent(applicationContext, GuessingActivity::class.java)
                        else
                            Intent(applicationContext, MainActivity::class.java).putExtra(MainActivity.Key_Fragment, fragmentType.ordinal)
                val pIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
                return NotificationCompat.Builder(applicationContext)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setContentIntent(pIntent).build()
    }
    //endregion

    companion object {
        private const val CallbackId: Int = 402
    }
}