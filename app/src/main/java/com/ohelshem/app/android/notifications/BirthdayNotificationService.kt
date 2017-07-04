package com.ohelshem.app.android.notifications

import android.app.IntentService
import android.content.Intent
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.erased.instance
import com.ohelshem.app.android.flipName
import com.ohelshem.app.android.main.MainActivity.Companion.Shortcut_LaunchMyClass
import com.ohelshem.app.controller.storage.ContactsProvider
import com.ohelshem.app.controller.storage.Storage
import com.ohelshem.app.getIsraelCalendar
import com.ohelshem.app.model.Contact
import com.ohelshem.app.toCalendar
import com.yoavst.changesystemohelshem.R
import java.util.*

class BirthdayNotificationService : IntentService("OhelShemHolidayNotificationService"), LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)

    val storage: Storage by kodein.instance()
    val contactsProvider: ContactsProvider by kodein.instance()

    override fun onHandleIntent(intent: Intent?) {
        if (storage.isSetup() && storage.notificationsForBirthdays && storage.lastNotificationTimeBirthday.toCalendar()[Calendar.DAY_OF_YEAR] != getIsraelCalendar()[Calendar.DAY_OF_YEAR]) {
            val today = getIsraelCalendar()
            val (day, month) = today[Calendar.DAY_OF_MONTH] to today[Calendar.MONTH]
            val allContacts =
                    if (storage.isStudent())
                        contactsProvider.getContacts(storage.userData.layer, storage.userData.clazz)
                    else
                        storage.primaryClass?.let { contactsProvider.getContacts(it.layer, it.clazz) } ?: emptyList()

            val contacts = allContacts.filter {
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
        var text = getString(R.string.birthday_message, contacts[0].name.flipName())
        if (contacts.size > 1) {
            for (i in 1 until contacts.size)
                text += " ו" + contacts[i].name.flipName()
        }
        text += "!"
        sendNotification(getString(R.string.birthday_title), text, action = Shortcut_LaunchMyClass, big = true, id = 1005, sound = true, icon = R.drawable.ic_birthday)
    }

}