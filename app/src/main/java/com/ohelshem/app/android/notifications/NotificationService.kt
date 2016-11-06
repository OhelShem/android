package com.ohelshem.app.android.notifications

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.support.v7.app.NotificationCompat
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.ohelshem.api.model.Test
import com.ohelshem.app.android.main.MainActivity
import com.ohelshem.app.clearTime
import com.ohelshem.app.controller.storage.ContactsProvider
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.controller.timetable.TimetableController
import com.ohelshem.app.controller.timetable.TimetableController.Companion.DayType
import com.ohelshem.app.getHour
import com.ohelshem.app.model.Contact
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.notificationManager
import java.util.*

class NotificationService : IntentService("OhelShemNotificationService"), LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)

    val storage: Storage by kodein.instance()
    val contactsProvider: ContactsProvider by kodein.instance()
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
                if (storage.notificationsForTests) {
                    checkForTests(day)
                }
                if (storage.notificationsForBirthdays) {
                    checkForBirthdays(day)
                }
                storage.lastNotificationTime = System.currentTimeMillis()
            }
        }
    }

    private fun checkForBirthdays(today: Calendar) {
        val (day, month) = today[Calendar.DAY_OF_MONTH] to today[Calendar.MONTH]
        val contacts = contactsProvider.getContacts(storage.userData.layer, storage.userData.clazz).filter {
            val cal = it.birthday.toCalendar()
            cal[Calendar.DAY_OF_MONTH] == day && cal[Calendar.MONTH] == month
        }
        if (contacts.isNotEmpty()) {
            notifyBirthdays(contacts)
        }

    }


    private fun checkForTests(day: Calendar) {
        val now = day.timeInMillis
        day.add(Calendar.DAY_OF_YEAR, 7)
        val inAWeek = day.timeInMillis
        var tests = storage.tests?.filter { it.date >= now && it.date <= inAWeek }?.sortedBy(Test::date) ?: emptyList()
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
        notificationManager.notify(1002, notification(getString(R.string.holiday_notification), "", hasChanges = false, action = MainActivity.Shortcut_LaunchDates))
    }

    private fun notifyTestTomorrow(test: Test) {
        notificationManager.notify(1003, notification(getString(R.string.test_tomorrow), test.content, hasChanges = false, action = MainActivity.Shortcut_LaunchDates))
    }

    private fun notifyTestsInAWeek(tests: List<Test>) {
        val text = if (tests.size == 1) tests.first().content else getString(R.string.tests_this_week_subtitle)
        notificationManager.notify(1004, notification(getString(R.string.tests_this_week), text, hasChanges = false, action = MainActivity.Shortcut_LaunchDates))
    }

    private fun notifyBirthdays(contacts: List<Contact>) {
        var text = getString(R.string.bday_msg, if (storage.userData.gender==0) "תשכחי" else "תשכח", contacts[0].name)
        if (contacts.size>1) {
            for (i in 1..contacts.size)
                text += " ו" + contacts[i].name
        }
        text+="!"
        notificationManager.notify(1005, notification(getString(R.string.bday_title), text, hasChanges = false, action = MainActivity.Shortcut_LaunchDates))
    }

    private fun notification(title: String, text: String, hasChanges: Boolean, action: String): Notification? {
        val intent = Intent(applicationContext, MainActivity::class.java).setAction(action)
        val pIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        return NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pIntent).build()
    }
    //endregion
}