package com.ohelshem.app.android.notifications

import android.app.IntentService
import android.content.Intent
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.app.android.main.MainActivity.Companion.Shortcut_LaunchMyClass
import com.ohelshem.app.controller.storage.ContactsProvider
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.model.Contact
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import java.util.*

class BirthdayNotificationService : IntentService("OhelShemHolidayNotificationService"), LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)

    val storage: Storage by kodein.instance()
    val contactsProvider: ContactsProvider by kodein.instance()

    override fun onHandleIntent(intent: Intent?) {
        if (storage.isSetup() && storage.notificationsForBirthdays && storage.lastNotificationTimeBirthday.toCalendar()[Calendar.DAY_OF_YEAR] != Calendar.getInstance()[Calendar.DAY_OF_YEAR]) {
            val today = Calendar.getInstance()
            val (day, month) = today[Calendar.DAY_OF_MONTH] to today[Calendar.MONTH]
            val contacts = contactsProvider.getContacts(storage.userData.layer, storage.userData.clazz).filter {
                val cal = it.birthday.toCalendar()
                cal[Calendar.DAY_OF_MONTH] == day && cal[Calendar.MONTH] == month
            }
            if (contacts.isNotEmpty()) {
                notifyBirthdays(contacts)
            }
            storage.lastNotificationTimeBirthday = System.currentTimeMillis()
        }
    }

    private fun notifyBirthdays(contacts: List<Contact>) {
        var text = getString(R.string.birthday_message, if (storage.userData.gender == 0) "תשכחי" else "תשכח", toFullName(contacts[0].name))
        if (contacts.size > 1) {
            for (i in 1 until contacts.size)
                text += " ו" + toFullName(contacts[i].name)
        }
        text += "!"
        sendNotification(getString(R.string.birthday_title), text, action = Shortcut_LaunchMyClass, big = true, id = 1005)
    }

    private fun toFullName(name: String): String {
        val arr = name.split(" ")
        if (arr.size > 2) return name
        return arr[1] + " " + arr[0]
    }

}