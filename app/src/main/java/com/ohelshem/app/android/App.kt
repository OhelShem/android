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

package com.ohelshem.app.android

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import com.chibatching.kotpref.Kotpref
import com.orhanobut.logger.Logger as ExternalLogger
import com.yoavst.changesystemohelshem.R
import com.ohelshem.app.android.service.NotificationService
import com.ohelshem.app.android.service.OngoingNotificationService
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.DBController
import com.ohelshem.app.controller.TimetableController
import com.ohelshem.app.injection.ControllerInjectionModule
import com.ohelshem.api.controller.declaration.ApiController
import com.ohelshem.api.controller.declaration.ApiParser
import com.ohelshem.api.controller.declaration.ColorProvider
import com.ohelshem.api.controller.implementation.ApiControllerImpl
import com.ohelshem.api.controller.implementation.ApiParserImpl
import com.ohelshem.api.controller.implementation.ColorProviderImpl
import com.ohelshem.api.model.AuthData
import org.jetbrains.anko.alarmManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.*
import java.util.*
import org.jetbrains.anko.intentFor

/**
 * The [Application] class of this application.
 * Currently it is only used as static [Context] and as a initializer
 * for injection and controllers.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Injekt.importModule(ControllerInjectionModule)
        addColorProvider()
        Injekt.addSingleton<ApiParser>(ApiParserImpl(Injekt.get()))
        Injekt.addSingleton<ApiController>(ApiControllerImpl(Injekt.get(), Injekt.get(), Injekt.get()))

        Instance = this

        Kotpref.init(this)
        com.orhanobut.logger.Logger.init("OhelShem")

        Injekt.get<ApiController>().setNetworkAvailabilityProvider { isNetworkAvailable() }
        Injekt.get<ApiParser>().timetableColors = getThemeColors()
        initDatabase(Injekt.get<DBController>())
        initTimetable(this, Injekt.get<TimetableController>())

        setAlarm(this)
    }

    private fun addColorProvider() {
        val filters = resources.getStringArray(R.array.changesFilters)
        val colors = colorArrayRes(R.array.changesColors)
        Injekt.addSingleton<ColorProvider>(ColorProviderImpl(colors zip filters, colorRes(R.color.changeDefaultColor)))
    }

    private fun initDatabase(databaseController: DBController) {
        databaseController.init()
        databaseController.migration()
        if (databaseController.isSetup()) {
            Injekt.get<ApiController>().authData = AuthData(databaseController.userData.identity, databaseController.password)
        }
    }

    private fun initTimetable(context: Context, timetableController: TimetableController) {
        timetableController.colors = context.getThemeColors()
        timetableController.init()
    }

    companion object {
        lateinit var Instance: App

        fun setAlarm(context: Context) {
            setAlarmForNightNotification(context)
            setAlarmForDayNotification(context)
        }

        private fun setAlarmForNightNotification(context: Context) {
            val intent = context.intentFor<NotificationService>()
            val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
            // Cancel the current alarm
            context.alarmManager.cancel(pendingIntent)
            // Set alarm for 21:05
            val cal = GregorianCalendar()
            cal.timeInMillis = System.currentTimeMillis()
            cal.set(Calendar.HOUR_OF_DAY, 21)
            cal.set(Calendar.MINUTE, 0)
            context.alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        }

        private fun setAlarmForDayNotification(context: Context) {
            val intent = context.intentFor<OngoingNotificationService>()
            var i = 0
            for ((hour, minute) in TimetableController.AlarmHours) {
                i++
                intent.action = "NOTIFY_ME_$i"

                val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
                context.alarmManager.cancel(pendingIntent)

                val cal = Calendar.getInstance().clearTime()
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                context.alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        cal.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
            }
            OngoingNotificationService.update(context)
        }

        private fun Context.getThemeColors(): IntArray {
            val ta = resources.obtainTypedArray(R.array.colors)
            val colors = IntArray(ta.length())
            for (i in 0..ta.length() - 1)
                colors[i] = ta.getColor(i, 0)
            ta.recycle()
            return colors
        }
    }
}