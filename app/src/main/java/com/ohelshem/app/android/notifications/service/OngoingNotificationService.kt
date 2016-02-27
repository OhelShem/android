/*
 * Copyright 2010-2015 Yoav Sternberg.
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

package com.ohelshem.app.android.notifications.service

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v7.app.NotificationCompat
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.MainActivity
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.getDay
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.startService
import uy.kohesive.injekt.injectLazy
import java.util.*

class OngoingNotificationService : IntentService("OhelShemOngoingNotificationService") {
    val databaseController: DBController by injectLazy()
    val timetableController: TimetableController by injectLazy()

    override fun onHandleIntent(intent: Intent?) {
        val cal = Calendar.getInstance()
        val day = cal.getDay()
        if (databaseController.isSetup()) {
            if (databaseController.notificationsForTimetableEnabled && day != Calendar.SATURDAY) {
                var data = timetableController.getHourData(day)
                if (data.hour.day != day - 1) {
                    // Day has ended
                    notificationManager.cancel(NotificationId)
                } else {
                    if ((data.isBefore || data.timeToHour <= 5) && cal[Calendar.HOUR_OF_DAY] >= 8) {
                        cal.add(Calendar.MINUTE, data.timeToHour + 1)
                        data = timetableController.getHourData(day, cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE])
                    }
                    if (data.hour.day != day - 1) {
                        // Day has ended
                        notificationManager.cancel(NotificationId)
                    } else {
                        val hours = TimetableController.DayHours[data.hour.hourOfDay * 2] + " - " + TimetableController.DayHours[data.hour.hourOfDay * 2 + 1]
                        val name = if (data.hour.isEmpty()) getString(R.string.window_lesson) else data.hour.name
                        if (data.nextHour.day != day - 1 || TimetableController.isEndOfDay(data.hour.hourOfDay, timetableController[data.hour.day]))
                            notificationManager.notify(NotificationId, createNotification(name, hours, getString(R.string.end_of_day)))
                        else notificationManager.notify(NotificationId,
                                createNotification(name, hours, if (data.nextHour.isEmpty()) getString(R.string.window_lesson) else data.nextHour.name))
                    }
                }
            } else notificationManager.cancel(NotificationId)
        } else notificationManager.cancel(NotificationId)
    }

    companion object {
        private const val NotificationId = 1342

        fun update(context: Context) {
            context.startService<OngoingNotificationService>()
        }

        private fun Context.createNotification(lesson: String, hours: String, nextLesson: String? = null): Notification {
            val intent = Intent(applicationContext, MainActivity::class.java)
            val pIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
            return NotificationCompat.Builder(this)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pIntent)
                    .setContentTitle(lesson)
                    .setContentText(hours)
                    .setSubText(nextLesson)
                    .build()
        }
    }
}