package com.ohelshem.app.android.notifications

import android.app.IntentService
import android.content.Intent
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.app.controller.storage.ContactsProvider
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.model.Contact
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import org.jetbrains.anko.notificationManager
import java.util.*

class BirthdayNotificationService : IntentService("OhelShemHolidayNotificationService"), LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)

    val storage: Storage by kodein.instance()
    val contactsProvider: ContactsProvider by kodein.instance()

    override fun onHandleIntent(intent: Intent?) {
        if (storage.notificationsForBirthdays) {
            val today = Calendar.getInstance()
            val (day, month) = today[Calendar.DAY_OF_MONTH] to today[Calendar.MONTH]
            val contacts = contactsProvider.getContacts(storage.userData.layer, storage.userData.clazz).filter {
                val cal = it.birthday.toCalendar()
                cal[Calendar.DAY_OF_MONTH] == day && cal[Calendar.MONTH] == month
            }
            if (contacts.isNotEmpty()) {
                notifyBirthdays(contacts)
            }
        }
    }

    private fun notifyBirthdays(contacts: List<Contact>) {
        var text = getString(R.string.bday_msg, if (storage.userData.gender == 0) "תשכחי" else "תשכח", toFullName(contacts[0].name))
        if (contacts.size > 1) {
            for (i in 1 until contacts.size)
                text += " ו" + toFullName(contacts[i].name)
        }
        text += "!"
        notificationManager.notify(1005, NotificationService.notification(getString(R.string.bday_title), text, action = ""))
    }

    private fun toFullName(name: String): String {
        val arr = name.split(" ")
        return arr[1] + " " + arr[0]
    }

}