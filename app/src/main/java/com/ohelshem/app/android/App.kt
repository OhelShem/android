package com.ohelshem.app.android

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import com.chibatching.kotpref.Kotpref
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.erased.instance
import com.github.salomonbrys.kodein.lazy
import com.ohelshem.api.ApiFactory
import com.ohelshem.app.android.notifications.BirthdayNotificationService
import com.ohelshem.app.android.notifications.NotificationService
import com.ohelshem.app.android.notifications.OngoingNotificationService
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.api.ApiController
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.storage.implementation.Contacts
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.injection.Modules
import com.yoavst.changesystemohelshem.BuildConfig
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.alarmManager
import org.jetbrains.anko.intentFor
import java.util.*

class App : Application(), KodeinAware {
    override val kodein by Kodein.lazy {
        import(Modules.Util)
        import(Modules.Info)
        import(Modules.Storage)
        import(Modules.Timetable)
        import(Modules.analytics(this@App))
        initApiModule()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Kotpref.init(this)
        Contacts.init(this)

        initApi()
        initStorage()

        setAlarm(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(p0: Activity?) {
                isForeground = false
            }

            override fun onActivityResumed(p0: Activity?) {
                isForeground = true
            }

            override fun onActivityStarted(p0: Activity?) = Unit

            override fun onActivityDestroyed(p0: Activity?) = Unit

            override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) = Unit

            override fun onActivityStopped(p0: Activity?) = Unit

            override fun onActivityCreated(p0: Activity?, p1: Bundle?) = Unit
        })

    }

    private fun initApi() {
        kodein.instance<ApiController>().setNetworkAvailabilityProvider { isNetworkAvailable() }
    }

    private fun initStorage() {
        with(kodein.instance<Storage>()) {
            prepare()
            initTimetable()
            migration()
            if (isSetup())
                kodein.instance<ApiController>().setAuthData(id, password)

            val version = BuildConfig.VERSION_CODE
            if (version != appVersion) {
                // Migration comes here
                updatedFromVersion = version
                appVersion = version
            }
        }
    }

    private fun initTimetable() {
        with(kodein.instance<TimetableController>()) {
            colors = colorArrayRes(R.array.colors)
            init()
        }
    }

    private fun Kodein.Builder.initApiModule() {
        val filters = stringArrayRes(R.array.changesFilters)
        val colors = colorArrayRes(R.array.changesColors)

        import(Modules.api(ApiFactory.defaultColorProvider(colorRes(R.color.changeDefaultColor), colors zip filters, colorArrayRes(R.array.colors))))
    }

    companion object {
        var isForeground: Boolean = false
        var messageCallback: ((String, String) -> Unit)? = null
        lateinit var instance: App

        var updatedFromVersion: Int = -1

        fun setAlarm(context: Context) {
            setAlarmForNightNotification(context)
            setAlarmForDayNotification(context)
            setAlarmForBirthdaysNotification(context)
        }

        private fun setAlarmForNightNotification(context: Context) {
            val intent = context.intentFor<NotificationService>()
            val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
            // Cancel the current alarm
            context.alarmManager.cancel(pendingIntent)
            // Set alarm for 21:00
            val cal = GregorianCalendar()
            cal.timeInMillis = System.currentTimeMillis()
            cal.set(Calendar.HOUR_OF_DAY, 21)
            cal.set(Calendar.MINUTE, 10) //server clock is not accurate
            context.alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        }

        private fun setAlarmForBirthdaysNotification(context: Context) {
            val intent = context.intentFor<BirthdayNotificationService>()
            val pendingIntent = PendingIntent.getService(context, 0, intent, 0)
            // Cancel the current alarm
            context.alarmManager.cancel(pendingIntent)
            // Set alarm for 07:00
            val cal = GregorianCalendar()
            cal.timeInMillis = System.currentTimeMillis()
            cal.set(Calendar.HOUR_OF_DAY, 7)
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
    }
}
